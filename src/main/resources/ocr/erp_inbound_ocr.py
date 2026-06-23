import json
import re
import sys
from pathlib import Path
from datetime import date
from decimal import Decimal, InvalidOperation

import erp_ocr


def dec(value):
    if value is None:
        return None
    cleaned = str(value).replace(",", "").strip()
    if not cleaned:
        return None
    try:
        return Decimal(cleaned)
    except InvalidOperation:
        return None


def dec_str(value, scale="0.0000"):
    if value is None:
        return None
    return format(Decimal(value).quantize(Decimal(scale)), "f")


def normalize_date(value):
    value = (value or "").strip()
    if re.match(r"^\d{4}-\d{2}-\d{2}$", value):
        return value + " 00:00:00"
    return None


def valid_date(year, month, day):
    try:
        date(year, month, day)
        return True
    except ValueError:
        return False


def normalize_short_date(value):
    value = (value or "").strip()
    value = value.replace("[", "").replace("]", "").replace("年", ".").replace("月", ".").replace("日", "")
    value = re.sub(r"[^0-9.\-]", "", value)
    match = re.match(r"^(20)?(\d{2})[.\-](\d{1,2})[.\-](\d{1,2})", value)
    if match:
        year = int(match.group(2)) + 2000
        month = int(match.group(3))
        day = int(match.group(4))
        if year >= 2020 and valid_date(year, month, day):
            return f"{year:04d}-{month:02d}-{day:02d} 00:00:00"
    match = re.match(r"^(\d{2})(\d{1,2})[.\-](\d{1,2})", value)
    if match:
        year = int(match.group(1)) + 2000
        month = int(match.group(2))
        day = int(match.group(3))
        if year >= 2020 and valid_date(year, month, day):
            return f"{year:04d}-{month:02d}-{day:02d} 00:00:00"
    match = re.match(r"^(\d{2})(\d)(\d{1,2})$", value)
    if match:
        year = int(match.group(1)) + 2000
        month = int(match.group(2))
        day = int(match.group(3))
        if year >= 2020 and valid_date(year, month, day):
            return f"{year:04d}-{month:02d}-{day:02d} 00:00:00"
    return None


def merge_date_tokens(tokens):
    merged = []
    idx = 0
    while idx < len(tokens):
        current = tokens[idx].strip()
        nxt = tokens[idx + 1].strip() if idx + 1 < len(tokens) else ""
        if re.match(r"^\d{4}-$", current) and re.match(r"^\d{2}-\d{2}$", nxt):
            merged.append(current + nxt)
            idx += 2
            continue
        if re.match(r"^\d{4}$", current) and re.match(r"^-\d{2}-\d{2}$", nxt):
            merged.append(current + nxt)
            idx += 2
            continue
        merged.append(current)
        idx += 1
    return merged


def extract_header_value(text, pattern):
    match = re.search(pattern, text)
    return match.group(1).strip() if match else None


def detect_truck_no(text):
    match = re.search(r"([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼][A-Z][A-Z0-9]{5,6})", text)
    return match.group(1) if match else None


def clean_product_name(value):
    value = (value or "").strip()
    value = re.sub(r"-?C\d{5}$", "", value).strip()
    value = value.rstrip("一").strip()
    return value


def normalize_sku(product_code, container_token):
    product_code = (product_code or "").strip()
    container_token = (container_token or "").strip()
    if not product_code:
        return None
    if not container_token:
        return f"C{product_code}-"
    container_token = container_token.replace(" ", "")
    if not container_token.startswith("MCRU"):
        return f"C{product_code}-"
    return f"C{product_code}-{container_token}"


def normalize_number_token(value):
    value = (value or "").strip()
    value = value.replace(",", ".").replace("，", ".").replace(":", ".").replace("：", ".")
    value = value.replace("O", "0").replace("o", "0").replace("I", "1").replace("l", "1")
    return value


def extract_date_and_container(tokens):
    joined = "".join(tokens)
    date_match = re.search(r"(20\d{2}-\d{2}-\d{2})", joined)
    inbound_date = date_match.group(1) if date_match else None
    container_match = re.search(r"([MN4O0]?CRU\d{7})", joined)
    container_token = None
    if container_match:
        container_token = container_match.group(1)
        container_token = re.sub(r"^[N4O0]?CRU", "MCRU", container_token)
    return inbound_date, container_token


def likely_product_code(token):
    token = (token or "").strip()
    if re.search(r"[A-Z]*RU\d{7}", token, re.IGNORECASE) or re.search(r"20\d{2}-\d{2}-\d{2}", token):
        return None
    match = re.match(r"^[CO]?(\d{5})-?$", token, re.IGNORECASE)
    if match:
        return match.group(1)
    match = re.match(r"^[0O](\d{5})-?$", token, re.IGNORECASE)
    if not match:
        return None
    return match.group(1)


def parse_wanyue_inbound_row(tokens):
    tokens = merge_date_tokens([token.strip() for token in tokens if token and token.strip()])
    row_text = " | ".join(tokens)
    if "合计" in row_text or "商品代码" in row_text or "实收客户板数" in row_text:
        return None

    product_code = None
    product_code_index = -1
    for idx, token in enumerate(tokens):
        code = likely_product_code(token)
        if code:
            product_code = code
            product_code_index = idx
            break
    if not product_code:
        return None

    product_name_parts = []
    for token in tokens[product_code_index + 1:]:
        if "冰鲜" in token or product_name_parts:
            product_name_parts.append(token)
            if ")" in token or "）" in token:
                break
            continue
        if re.match(r"^[5S][*#\+]\d+$", token) or normalize_short_date(token):
            break
    product_name = clean_product_name("".join(product_name_parts)) if product_name_parts else None
    if not product_name:
        for token in tokens:
            if "冰鲜" in token:
                product_name = clean_product_name(token)
                break

    production_date = None
    for token in tokens[product_code_index + 1:]:
        production_date = normalize_short_date(token)
        if production_date:
            break

    temp_zone = None
    if "冷冻" in row_text:
        temp_zone = "冷冻"
    elif "冷" in row_text:
        temp_zone = "冷藏"

    long_number_indexes = []
    for idx, token in enumerate(tokens):
        normalized = normalize_number_token(token)
        if re.match(r"^\d{6,10}$", normalized):
            value = int(normalized)
            if value > 100000:
                long_number_indexes.append((idx, value))
    total_weight = None
    weight_idx = -1
    if long_number_indexes:
        weight_idx, weight_value = long_number_indexes[0]
        nearby = " ".join(tokens[max(0, weight_idx - 2): min(len(tokens), weight_idx + 3)])
        if re.search(r"\b1\s*[.。]", nearby) and weight_value < 10000000:
            weight_value += 10000000
        total_weight = Decimal(weight_value) / Decimal("10000")

    quantity_values = []
    explicit_box_qty = None
    for idx, token in enumerate(tokens):
        if idx <= product_code_index or (weight_idx >= 0 and idx <= weight_idx):
            continue
        # 数量列在“温区/生产日期/失效日期/保质期”之前，不能把最右侧保质期 100 当成预期数/实收数。
        if token in ("冷藏", "冷冻", "常温", "冷鲜") or re.search(r"20\d{2}", token) or normalize_short_date(token):
            break
        box_match = re.search(r"(\d{1,4})\s*箱", token)
        if box_match:
            explicit_box_qty = int(box_match.group(1))
            quantity_values.append(explicit_box_qty)
            continue
        normalized = normalize_number_token(token)
        if re.match(r"^\d+$", normalized):
            value = int(normalized)
            if 0 < value <= 1000:
                quantity_values.append(value)
    if not quantity_values:
        return None
    expected_qty = quantity_values[-2] if len(quantity_values) >= 2 else quantity_values[-1]
    actual_qty = explicit_box_qty if explicit_box_qty is not None else quantity_values[-1]
    if actual_qty <= 2 and expected_qty > 2:
        actual_qty = expected_qty
    if actual_qty * 2 < expected_qty:
        actual_qty = expected_qty

    return {
        "productCode": product_code,
        "skuCode": normalize_sku(product_code, extract_date_and_container(tokens)[1]),
        "productName": product_name,
        "specWeight": None,
        "unit": "箱",
        "expectedQty": expected_qty,
        "actualQty": actual_qty,
        "factoryNo": None,
        "temperatureZone": temp_zone,
        "productionDate": production_date,
        "expiryDate": None,
        "shelfLifeDays": None,
        "totalWeightKg": dec_str(total_weight, "0.00") if total_weight is not None else None
    }


def parse_relaxed_inbound_row(tokens):
    tokens = merge_date_tokens([token.strip() for token in tokens if token and token.strip()])
    if len(tokens) < 5:
        return None
    row_text = " | ".join(tokens)
    if any(marker in row_text for marker in ("SKU", "品名", "发货人", "收货人", "出门证号", "打单时间", "单时间")):
        return None
    if "合计" in row_text and not re.search(r"\d{5}", row_text):
        return None

    product_code = None
    product_code_index = -1
    for idx, token in enumerate(tokens):
        code = likely_product_code(token)
        if code:
            product_code = code
            product_code_index = idx
            break

    product_name = None
    for token in tokens:
        if "冰鲜" in token and "收货清单" not in token:
            product_name = clean_product_name(token)
            break

    numeric_values = []
    for idx, token in enumerate(tokens):
        if idx == product_code_index:
            continue
        normalized = normalize_number_token(token)
        if re.match(r"^\d+(?:\.\d+)?$", normalized):
            numeric_values.append(normalized)

    int_values = [int(value) for value in numeric_values if re.match(r"^\d+$", value) and int(value) <= 10000]
    decimal_values = [value for value in numeric_values if re.match(r"^\d+\.\d+$", value)]
    inbound_date, container_token = extract_date_and_container(tokens)

    expected_qty = int_values[0] if len(int_values) >= 1 else None
    actual_qty = int_values[1] if len(int_values) >= 2 else None
    factory_no = str(int_values[2]) if len(int_values) >= 3 else None
    spec_weight = dec(decimal_values[0]) if decimal_values else None
    total_weight = dec(decimal_values[-1]) if len(decimal_values) >= 2 else (spec_weight * Decimal(actual_qty) if spec_weight is not None and actual_qty is not None else None)

    if not any([product_code, product_name, expected_qty is not None, actual_qty is not None, inbound_date, container_token, spec_weight is not None]):
        return None

    return {
        "productCode": product_code,
        "skuCode": normalize_sku(product_code, container_token) if product_code else None,
        "productName": product_name,
        "specWeight": dec_str(spec_weight, "0.0000") if spec_weight is not None else None,
        "unit": "箱" if expected_qty is not None or actual_qty is not None else None,
        "expectedQty": expected_qty,
        "actualQty": actual_qty,
        "factoryNo": factory_no,
        "temperatureZone": "冷鲜" if "冰鲜" in row_text else None,
        "productionDate": normalize_date(inbound_date),
        "expiryDate": None,
        "shelfLifeDays": None,
        "totalWeightKg": dec_str(total_weight, "0.00") if total_weight is not None else None
    }


def parse_row_tokens(tokens):
    tokens = merge_date_tokens(tokens)
    joined = " | ".join(tokens)
    product_code_match = re.search(r"C(\d{5})", joined)
    if not product_code_match:
        return parse_relaxed_inbound_row(tokens)
    product_code = product_code_match.group(1)
    product_code_index = -1
    for idx, token in enumerate(tokens):
        if re.search(r"C%s" % product_code, token):
            product_code_index = idx
            break

    container_match = re.search(r"(MCRU\d{7}(?:-\d+)?)", joined)
    container_token = container_match.group(1) if container_match else None

    product_name = None
    for token in tokens:
      if "冰鲜" in token and "SKU" not in token:
        product_name = clean_product_name(token)
        break

    spec_weight = None
    unit = None
    expected_qty = None
    actual_qty = None
    temp_zone = None
    production_date = None
    expiry_date = None
    shelf_life_days = None
    factory_no = None
    total_weight = None

    for idx, token in enumerate(tokens):
        if spec_weight is None and re.match(r"^\d+\.\d+$", token):
            spec_weight = dec(token)
            continue
        if unit is None and token in ("箱", "件", "KG", "公斤"):
            unit = token
            tail = tokens[idx + 1:]
            int_values = [int(value) for value in tail if re.match(r"^\d+$", value)]
            date_values = [value for value in tail if re.match(r"^\d{4}-\d{2}-\d{2}$", value)]
            temp_values = [value for value in tail if value in ("冷藏", "冷冻", "常温", "冷鲜")]
            if len(int_values) >= 1:
                expected_qty = int_values[0]
            if len(int_values) >= 2:
                actual_qty = int_values[1]
            if temp_values:
                temp_zone = temp_values[0]
            if len(date_values) >= 1:
                production_date = date_values[0]
            if len(date_values) >= 2:
                expiry_date = date_values[1]
            if len(int_values) >= 3:
                shelf_life_days = int_values[2]
            break

    if expected_qty is None or actual_qty is None:
        numeric_values = []
        int_positions = []
        decimal_values = []
        for idx, token in enumerate(tokens):
            if idx == product_code_index:
                continue
            if re.search(r"[A-Z]*RU\d{7}", token, re.IGNORECASE) or re.search(r"20\d{2}-\d{2}-\d{2}", token):
                continue
            normalized = normalize_number_token(token)
            if re.match(r"^\d+$", normalized):
                value = int(normalized)
                if value <= 10000:
                    numeric_values.append(normalized)
                    int_positions.append((idx, value))
            elif re.match(r"^\d+\.\d+$", normalized):
                numeric_values.append(normalized)
                decimal_values.append(normalized)
        if len(int_positions) >= 1 and expected_qty is None:
            expected_qty = int_positions[0][1]
        if len(int_positions) >= 2 and actual_qty is None:
            actual_qty = int_positions[1][1]
        if container_match and len(int_positions) >= 3:
            factory_no = str(int_positions[2][1])
        elif len(int_positions) >= 3:
            factory_no = str(int_positions[2][1])
        if spec_weight is None and decimal_values:
            spec_weight = dec(decimal_values[0])
        if len(decimal_values) >= 2:
            total_weight = dec(decimal_values[-1])
        elif spec_weight is not None and actual_qty is not None:
            total_weight = spec_weight * Decimal(actual_qty)

    if factory_no is None and container_match:
        after_container = joined[container_match.end():]
        factory_match = re.search(r"\b(\d{1,4})\b", after_container)
        if factory_match:
            factory_no = factory_match.group(1)

    return {
        "productCode": product_code,
        "skuCode": normalize_sku(product_code, container_token),
        "productName": product_name,
        "specWeight": dec_str(spec_weight, "0.0000") if spec_weight is not None else None,
        "unit": unit,
        "expectedQty": expected_qty,
        "actualQty": actual_qty,
        "factoryNo": factory_no,
        "temperatureZone": temp_zone,
        "productionDate": normalize_date(production_date),
        "expiryDate": normalize_date(expiry_date),
        "shelfLifeDays": shelf_life_days,
        "totalWeightKg": dec_str(total_weight, "0.00") if total_weight is not None else None
    }


def parse_file(path):
    if Path(path).suffix.lower() == ".pdf":
        items, _ = erp_ocr.load_pdf_image_items(path)
    else:
        items, _ = erp_ocr.load_image_items(path)
    rows = erp_ocr.group_rows(items, tolerance=18)
    text_rows = [" | ".join(row["texts"]) for row in rows]
    text = "\n".join(text_rows)

    header = {
        "customerName": extract_header_value(text, r"客户[:：]\s*([^\n|]+)")
            or extract_header_value(text, r"货主名称\s*\|\s*([^\n|]+)"),
        "wmsOrderNo": extract_header_value(text, r"WMS订单号[:：]\s*([A-Z0-9]+)")
            or extract_header_value(text, r"\b(ASNEQ[0-9A-Z]+)\b"),
        "customerOrderNo": extract_header_value(text, r"客户订单号[:：]\s*([^\n|]+)")
            or extract_header_value(text, r"货主单号\s*\|\s*([^\n|]+)"),
        "driverName": extract_header_value(text, r"司机(?:姓名)?[:：]\s*([^\s|]+)"),
        "driverPhone": extract_header_value(text, r"司机(?:电话|手机)[:：]\s*([0-9]+)")
            or extract_header_value(text, r"联系(?:电话|方式)[:：]\s*([0-9]+)"),
        "idCardNo": extract_header_value(text, r"司机身份证[:：]?\s*([0-9Xx]+)")
            or extract_header_value(text, r"身份证[:：]?\s*([0-9Xx]+)"),
        "truckNo": detect_truck_no(text),
        "rawText": text
    }

    item_list = []
    table_started = False
    row_idx = 0
    while row_idx < len(rows):
        tokens = [token.strip() for token in rows[row_idx]["texts"] if token.strip()]
        row_text = " | ".join(tokens)
        if not table_started:
            if ("SKU" in row_text and "品名" in row_text) or ("商品代码" in row_text and "商品名称" in row_text):
                table_started = True
            row_idx += 1
            continue
        if "发货人" in row_text or "第" in row_text and "页" in row_text:
            row_idx += 1
            continue
        if "合计" in row_text:
            break

        combined = list(tokens)
        has_code = re.search(r"(?:C)?\d{5}", row_text) is not None
        has_container = re.search(r"[MN4O0]?CRU\d{7}", row_text) is not None
        if row_idx + 1 < len(rows):
            next_tokens = [token.strip() for token in rows[row_idx + 1]["texts"] if token.strip()]
            next_text = " | ".join(next_tokens)
            next_is_data = re.search(r"([MN4O0]?CRU\d{7}|(?:C)?\d{5})", next_text) is not None and "SKU" not in next_text and "发货人" not in next_text
            if next_is_data and not has_code and (not has_container or len([token for token in tokens if re.match(r"^\d+\.\d+$", token)]) == 0):
                combined.extend(next_tokens)
                row_idx += 1

        parsed = parse_wanyue_inbound_row(combined) or parse_row_tokens(combined)
        if parsed:
            item_list.append(parsed)
        row_idx += 1

    return header, item_list


def recognize(paths):
    header = {}
    item_list = []
    raw_text_parts = []
    for path in paths:
        parsed_header, parsed_items = parse_file(Path(path))
        raw_text_parts.append(parsed_header.get("rawText") or "")
        for key in ("customerName", "wmsOrderNo", "customerOrderNo", "driverName", "driverPhone", "idCardNo", "truckNo"):
            if not header.get(key) and parsed_header.get(key):
                header[key] = parsed_header.get(key)
        item_list.extend(parsed_items)

    return {
        "docType": "INBOUND_RECEIPT",
        "rawText": "\n\n".join([part for part in raw_text_parts if part]),
        "inboundDraft": {
            "customerName": header.get("customerName"),
            "driverName": header.get("driverName"),
            "truckNo": header.get("truckNo"),
            "driverPhone": header.get("driverPhone"),
            "idCardNo": header.get("idCardNo"),
            "customerOrderNo": header.get("customerOrderNo"),
            "wmsOrderNo": header.get("wmsOrderNo"),
            "itemList": item_list
        }
    }


def main():
    if len(sys.argv) < 2:
        print(json.dumps({"success": False, "message": "missing path list"}))
        return
    path_file = Path(sys.argv[1])
    try:
        paths = [line.strip() for line in path_file.read_text(encoding="utf-8-sig").splitlines() if line.strip()]
        result = recognize(paths)
        result["success"] = True
        print(json.dumps(result, ensure_ascii=False))
    except Exception as exc:
        print(json.dumps({"success": False, "message": str(exc)}, ensure_ascii=False))


if __name__ == "__main__":
    main()

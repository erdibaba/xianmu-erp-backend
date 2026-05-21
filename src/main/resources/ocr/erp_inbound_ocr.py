import json
import re
import sys
from pathlib import Path
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


def parse_row_tokens(tokens):
    tokens = merge_date_tokens(tokens)
    joined = " | ".join(tokens)
    product_code_match = re.search(r"C(\d{5})", joined)
    if not product_code_match:
        return None
    product_code = product_code_match.group(1)

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

    return {
        "productCode": product_code,
        "skuCode": normalize_sku(product_code, container_token),
        "productName": product_name,
        "specWeight": dec_str(spec_weight, "0.0000") if spec_weight is not None else None,
        "unit": unit,
        "expectedQty": expected_qty,
        "actualQty": actual_qty,
        "temperatureZone": temp_zone,
        "productionDate": normalize_date(production_date),
        "expiryDate": normalize_date(expiry_date),
        "shelfLifeDays": shelf_life_days
    }


def parse_file(path):
    items, _ = erp_ocr.load_image_items(path)
    rows = erp_ocr.group_rows(items, tolerance=18)
    text_rows = [" | ".join(row["texts"]) for row in rows]
    text = "\n".join(text_rows)

    header = {
        "customerName": extract_header_value(text, r"客户[:：]\s*([^\n|]+)"),
        "wmsOrderNo": extract_header_value(text, r"WMS订单号[:：]\s*([A-Z0-9]+)"),
        "customerOrderNo": extract_header_value(text, r"客户订单号[:：]\s*([^\n|]+)"),
        "driverName": extract_header_value(text, r"司机姓名[:：]\s*([^\s|]+)"),
        "driverPhone": extract_header_value(text, r"司机电话[:：]\s*([0-9]+)"),
        "idCardNo": extract_header_value(text, r"司机身份证[:：]?\s*([0-9Xx]+)"),
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
            if "SKU" in row_text and "品名" in row_text:
                table_started = True
            row_idx += 1
            continue
        if "发货人" in row_text or "第" in row_text and "页" in row_text or "合计" in row_text:
            row_idx += 1
            continue

        combined = list(tokens)
        has_code = re.search(r"C\d{5}", row_text) is not None
        has_container = re.search(r"MCRU\d{7}", row_text) is not None
        if row_idx + 1 < len(rows):
            next_tokens = [token.strip() for token in rows[row_idx + 1]["texts"] if token.strip()]
            next_text = " | ".join(next_tokens)
            next_is_data = re.search(r"(MCRU\d{7}|C\d{5})", next_text) is not None and "SKU" not in next_text and "发货人" not in next_text
            if next_is_data and (not has_container or len([token for token in tokens if re.match(r"^\d+\.\d+$", token)]) == 0):
                combined.extend(next_tokens)
                row_idx += 1

        parsed = parse_row_tokens(combined)
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

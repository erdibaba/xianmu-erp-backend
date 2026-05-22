import json
import re
import sys
from decimal import Decimal, InvalidOperation
from pathlib import Path

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


def dec_str(value, scale="0.000"):
    if value is None:
        return None
    return format(Decimal(value).quantize(Decimal(scale)), "f")


def extract_header_value(text, pattern):
    match = re.search(pattern, text)
    return match.group(1).strip() if match else None


def normalize_product_code(value):
    match = re.search(r"C\s*(\d{5})", value or "", re.I)
    if match:
        return match.group(1)
    match = re.search(r"\b(\d{5})\b", value or "")
    return match.group(1) if match else None


def parse_row_tokens(tokens):
    joined = " | ".join(tokens)
    product_code = normalize_product_code(joined)
    if not product_code:
        return None

    container_match = re.search(r"(MCRU\d{7,8}(?:-\d+)?)", joined.replace(" ", ""))
    container_no = container_match.group(1) if container_match else None

    unit_index = None
    for idx, token in enumerate(tokens):
        if token in ("箱", "件", "KG", "公斤"):
            unit_index = idx
            break
    if unit_index is None:
        return None

    integer_values = []
    for token in tokens[unit_index + 1:]:
        if re.match(r"^\d+$", token):
            integer_values.append(int(token))
        if len(integer_values) >= 2:
            break

    decimal_values = []
    for token in tokens:
        if re.match(r"^\d+\.\d+$", token):
            value = dec(token)
            if value is not None:
                decimal_values.append(value)

    product_name = None
    for token in tokens:
        if ("冰鲜" in token or "SYB" in token or "牛" in token or product_code in token) and "产品" not in token:
            product_name = token
            break

    spec = dec_str(decimal_values[0], "0.0000") if decimal_values else None
    avg_weight = dec_str(decimal_values[-2], "0.0000") if len(decimal_values) >= 2 else spec
    total_weight = dec_str(decimal_values[-1], "0.000") if decimal_values else None

    return {
        "recognizedProductCode": product_code,
        "productSpec": spec,
        "unit": tokens[unit_index],
        "orderQty": integer_values[0] if len(integer_values) >= 1 else None,
        "shippedQty": integer_values[1] if len(integer_values) >= 2 else (integer_values[0] if len(integer_values) == 1 else None),
        "containerNo": container_no,
        "factoryNo": None,
        "avgWeight": avg_weight,
        "totalWeight": total_weight,
        "recognizedProductName": product_name
    }


def parse_file(path):
    items, _ = erp_ocr.load_image_items(path)
    rows = erp_ocr.group_rows(items, tolerance=18)
    text_rows = [" | ".join(row["texts"]) for row in rows]
    text = "\n".join(text_rows)

    header = {
        "wmsOrderNo": extract_header_value(text, r"WMS\s*单号[:：]?\s*([A-Z0-9]+)"),
        "outboundOrderNo": extract_header_value(text, r"订单编号[:：]?\s*([^|\n]+)"),
        "customerCode": extract_header_value(text, r"客户编码[:：]?\s*([A-Z0-9]+)"),
        "customerName": extract_header_value(text, r"客户名称[:：]?\s*([^|\n]+)"),
        "rawText": text
    }

    item_list = []
    table_started = False
    row_idx = 0
    while row_idx < len(rows):
        tokens = [token.strip() for token in rows[row_idx]["texts"] if token.strip()]
        row_text = " | ".join(tokens)
        if not table_started:
            if "产品编码" in row_text and "发货数" in row_text:
                table_started = True
            row_idx += 1
            continue
        if "合计" in row_text or "备注" in row_text or "第" in row_text and "页" in row_text:
            row_idx += 1
            continue

        combined = list(tokens)
        if row_idx + 1 < len(rows):
            next_tokens = [token.strip() for token in rows[row_idx + 1]["texts"] if token.strip()]
            next_text = " | ".join(next_tokens)
            current_has_container = re.search(r"MCRU\d{7,8}", row_text) is not None
            next_is_continuation = re.search(r"MCRU\d{7,8}|C\d{5}", next_text) is not None and "产品编码" not in next_text
            if next_is_continuation and not current_has_container:
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
        for key in ("wmsOrderNo", "outboundOrderNo", "customerCode", "customerName"):
            if not header.get(key) and parsed_header.get(key):
                header[key] = parsed_header.get(key)
        item_list.extend(parsed_items)

    return {
        "success": True,
        "docType": "OUTBOUND_RECEIPT",
        "rawText": "\n\n".join([part for part in raw_text_parts if part]),
        "receipt": {
            "wmsOrderNo": header.get("wmsOrderNo"),
            "outboundOrderNo": header.get("outboundOrderNo"),
            "customerCode": header.get("customerCode"),
            "customerName": header.get("customerName"),
            "itemList": item_list
        }
    }


def main():
    if len(sys.argv) < 2:
        raise SystemExit("usage: erp_outbound_ocr.py path-list-file")
    list_file = Path(sys.argv[1])
    paths = [line.strip() for line in list_file.read_text(encoding="utf-8").splitlines() if line.strip()]
    result = recognize(paths)
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()

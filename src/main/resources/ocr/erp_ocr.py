import json
import re
import sys
from decimal import Decimal, InvalidOperation, ROUND_HALF_UP
from pathlib import Path

from pypdf import PdfReader

KW_SIGN_DATE = "签订日期"
KW_PARTY_A = "甲方"
KW_PARTY_B = "乙方"
KW_CONFIRM = "客户订单确认函"
KW_SELLER = "卖 方"
KW_CONTRACT_NO = "合同号码"
KW_CONTAINER = "集装箱箱号"
KW_EXPECT = "预计到港时间"
KW_PAY_NOTICE = "请贵司于"

DEFAULT_SALES_WAREHOUSE = "上海万纬-万呈"
DEFAULT_PURCHASE_WAREHOUSE = "采购方指定港口仓库"
DOC_STAGE_ESTIMATE = "ESTIMATE"
DOC_STAGE_CONFIRMED = "CONFIRMED"


def load_image_items(file_path):
    from rapidocr_onnxruntime import RapidOCR

    ocr = RapidOCR()
    result, _ = ocr(str(file_path))
    if not result:
        return [], []
    items = []
    for box, text, score in result:
        if not text or not text.strip():
            continue
        left = min(p[0] for p in box)
        top = min(p[1] for p in box)
        right = max(p[0] for p in box)
        bottom = max(p[1] for p in box)
        items.append({
            "text": text.strip(),
            "left": left,
            "top": top,
            "right": right,
            "bottom": bottom,
            "cx": (left + right) / 2,
            "cy": (top + bottom) / 2,
            "score": score
        })
    items = sorted(items, key=lambda item: (item["top"], item["left"]))
    lines = [item["text"] for item in items]
    return items, lines


def load_pdf_text(file_path):
    reader = PdfReader(str(file_path))
    texts = []
    for page in reader.pages:
        texts.append(page.extract_text() or "")
    return "\n".join(texts)


def dec(value):
    if value is None:
        return None
    cleaned = str(value).replace(",", "").replace("\u00a5", "").replace("\uffe5", "").strip()
    if not cleaned:
        return None
    try:
        return Decimal(cleaned)
    except InvalidOperation:
        return None


def dec_str(value, scale="0.00"):
    if value is None:
        return None
    quant = Decimal(scale)
    return str(Decimal(value).quantize(quant, rounding=ROUND_HALF_UP))


def format_date(value):
    if not value:
        return None
    value = value.replace("/", "-").strip()
    if re.match(r"^\d{4}-\d{1,2}-\d{1,2}$", value):
        parts = value.split("-")
        return f"{parts[0]}-{int(parts[1]):02d}-{int(parts[2]):02d} 00:00:00"
    return None


def normalize_text(text):
    return re.sub(r"[ \t]+", " ", text).replace("\r", "\n")


def group_rows(items, tolerance=18):
    rows = []
    for item in sorted(items, key=lambda value: (value["cy"], value["left"])):
        matched = None
        for row in rows:
            if abs(row["cy"] - item["cy"]) <= tolerance:
                matched = row
                break
        if matched is None:
            matched = {"cy": item["cy"], "items": []}
            rows.append(matched)
        matched["items"].append(item)
        matched["cy"] = sum(value["cy"] for value in matched["items"]) / len(matched["items"])
    for row in rows:
        row["items"] = sorted(row["items"], key=lambda value: value["left"])
        row["texts"] = [value["text"] for value in row["items"]]
    rows = sorted(rows, key=lambda value: value["cy"])
    return rows


def first_match(text, pattern):
    if not text:
        return None
    match = re.search(pattern, text, re.M)
    return match.group(1).strip() if match else None


def infer_tax_rate(amount, tax_amount):
    if not amount or not tax_amount or amount == 0:
        return Decimal("0.00")
    return (tax_amount * Decimal("100") / amount).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def extract_purchase_total_amount(flat_text, lines):
    for pattern in [
        r"([\d,]+\.\d+)\s*金额[:：]?\s*CNY",
        r"金额[:：]?\s*CNY\s*([\d,]+\.\d+)"
    ]:
        value = first_match(flat_text, pattern)
        amount = dec(value)
        if amount:
            return amount

    for idx, line in enumerate(lines):
        if "预计到港时间" not in line:
            continue
        for offset in [-3, -2, -1, 0, 1, 2, 3, 4, 5]:
            pos = idx + offset
            if pos < 0 or pos >= len(lines):
                continue
            for value in re.findall(r"[\d,]+\.\d+", lines[pos]):
                amount = dec(value)
                if amount and amount >= Decimal("1000"):
                    return amount
    return None


def parse_sales(items, text):
    order_date = format_date(first_match(text, rf"{KW_SIGN_DATE}[:：]\s*(\d{{4}}[/-]\d{{1,2}}[/-]\d{{1,2}})"))
    supplier = first_match(text, rf"{KW_PARTY_A}[:：]\s*([^\n]+)")
    customer = first_match(text, rf"{KW_PARTY_B}[:：]\s*([^\n]+)")
    rows = group_rows(items)
    item_list = []
    contract_values = []
    for row in rows:
        contract = first_match(" ".join(row["texts"]), r"(CY-\d{4}-\d{2}-\d{3})")
        if not contract:
            continue
        texts = row["texts"]
        container = next((value for value in texts if re.match(r"^[A-Z]{4}\d{7}$", value)), None)
        warehouse = next((value for value in texts if "上海" in value or "冷库" in value), DEFAULT_SALES_WAREHOUSE)
        product = next((value for value in texts if ("牛" in value or "冰鲜" in value)), None)
        price_token = next((value for value in texts if "\u00a5" in value or "\uffe5" in value), None)
        decimal_values = [value for value in texts if re.match(r"^\d+\.\d+$", value)]
        integer_values = [value for value in texts if re.match(r"^\d+$", value)]
        qty = dec(decimal_values[0]) if len(decimal_values) >= 1 else None
        amount = dec(decimal_values[1]) if len(decimal_values) >= 2 else None
        tax_amount = dec(decimal_values[2]) if len(decimal_values) >= 3 else None
        pieces = integer_values[0] if integer_values else ""
        price_incl = dec(price_token) if price_token else None
        if not product or not qty or not price_incl:
            continue
        item_list.append({
            "productCode": "SYB-52-CHUCK",
            "productName": product,
            "productSpec": "52",
            "warehouseName": warehouse,
            "quantity": dec_str(qty, "0.00"),
            "unitPrice": dec_str(price_incl / Decimal("1.09"), "0.0000"),
            "taxRate": dec_str(infer_tax_rate(amount, tax_amount), "0.00"),
            "remark": f"contract {contract}, container {container or ''}, pieces {pieces}"
        })
        contract_values.append(contract)

    expense_list = [{
        "expenseType": "STORAGE",
        "expenseName": "StorageFee",
        "amount": "300.00",
        "taxRate": "0.00",
        "remark": "Auto filled by OCR draft, editable"
    }]
    contract_value = " / ".join(contract_values) if contract_values else None
    return {
        "docType": "SALE",
        "orderDraft": {
            "orderType": "SALE",
            "partnerName": customer,
            "contractNo": contract_value,
            "containerNo": None,
            "warehouseName": DEFAULT_SALES_WAREHOUSE,
            "orderDate": order_date,
            "expectedDate": None,
            "paymentDueDate": order_date,
            "currency": "CNY",
            "status": 0,
            "bizType": "NORMAL_OUTBOUND",
            "orderSource": "IMAGE_OCR",
            "docStage": DOC_STAGE_CONFIRMED,
            "remark": f"OCR draft. seller={supplier or ''}",
            "itemList": item_list,
            "expenseList": expense_list,
            "sourceType": "IMAGE_OCR"
        }
    }


def parse_presale_pdf(text):
    flat_text = normalize_text(text)
    customer = first_match(flat_text, r"Customer Reference[:：]?\s*([^\n]+)")
    if not customer:
        customer = first_match(flat_text, r"TO:\s*([^\n]+)")
    supplier = first_match(flat_text, r"FROM:\s*([^\n]+)")
    contract_no = first_match(flat_text, r"Seller's Contract No:\s*([A-Z0-9]+)")
    order_date = format_date(first_match(flat_text, r"DATE:\s*(\d{1,2}\s+[A-Za-z]{3}\s+\d{4})"))
    if order_date is None:
        order_date = format_date(first_match(flat_text, r"DATE:\s*(\d{4}[/-]\d{1,2}[/-]\d{1,2})"))
    arrival_date = format_date(first_match(flat_text, r"Est Arrival\s+(\d{1,2}\s+[A-Za-z]{3}\s+\d{4})"))
    delivery_place = first_match(flat_text, r"PLACE OF DELIVERY\s+([^\n]+)")

    def parse_english_date(value):
        if not value:
            return None
        months = {
            "Jan": 1, "Feb": 2, "Mar": 3, "Apr": 4, "May": 5, "Jun": 6,
            "Jul": 7, "Aug": 8, "Sep": 9, "Oct": 10, "Nov": 11, "Dec": 12
        }
        match = re.match(r"^\s*(\d{1,2})\s+([A-Za-z]{3})\s+(\d{4})\s*$", value)
        if not match:
            return None
        day, month_text, year = match.groups()
        month = months.get(month_text.title())
        if not month:
            return None
        return f"{year}-{month:02d}-{int(day):02d} 00:00:00"

    order_date = order_date or parse_english_date(first_match(flat_text, r"DATE:\s*(\d{1,2}\s+[A-Za-z]{3}\s+\d{4})"))
    arrival_date = arrival_date or parse_english_date(first_match(flat_text, r"Est Arrival\s+(\d{1,2}\s+[A-Za-z]{3}\s+\d{4})"))

    lines = [line.strip() for line in text.splitlines() if line.strip()]
    item_list = []
    idx = 0
    while idx < len(lines):
        line = lines[idx]
        combined = line
        cursor = idx + 1
        while cursor < len(lines):
            next_line = lines[cursor]
            if re.match(r"^\d+(?:\.\d+)?\s+MT\s+\d{5}\s+", next_line):
                break
            if "TOTAL" in next_line.upper() or "PLACE OF DELIVERY" in next_line.upper():
                break
            if re.match(r"^\d+(?:\.\d+)?\s+CNY/KG$", next_line):
                combined += " " + next_line
                cursor += 1
                break
            combined += " " + next_line
            cursor += 1
        match = re.match(r"^(?P<qty>\d+(?:\.\d+)?)\s+MT\s+(?P<code>\d{5})\s+(?P<desc>.+?)\s+(?P<price>\d+(?:\.\d+)?)\s+CNY/KG$", combined)
        if match:
            qty_mt = dec(match.group("qty"))
            price = dec(match.group("price"))
            product_desc = match.group("desc").strip()
            item_list.append({
                "productCode": match.group("code"),
                "sourceProductCode": match.group("code"),
                "productNameEn": product_desc,
                "productSpec": None,
                "warehouseName": delivery_place or DEFAULT_SALES_WAREHOUSE,
                "quantityTon": dec_str(qty_mt, "0.0000"),
                "quantityKg": dec_str((qty_mt or Decimal("0")) * Decimal("1000"), "0.00"),
                "priceAmount": dec_str(price, "0.0000"),
                "priceCurrency": "CNY",
                "priceUnit": "KG",
                "remark": "Presale estimate from sales confirmation PDF"
            })
        idx = cursor if cursor > idx + 1 else idx + 1

    return {
        "docType": "PRESALE_ESTIMATE",
        "orderDraft": {
            "orderType": "SALE",
            "partnerName": customer,
            "brandName": supplier,
            "contractNo": contract_no,
            "containerNo": None,
            "warehouseName": delivery_place or DEFAULT_SALES_WAREHOUSE,
            "orderDate": order_date,
            "expectedDate": arrival_date,
            "paymentDueDate": order_date,
            "currency": "CNY",
            "status": 0,
            "bizType": "PRESALE",
            "orderSource": "OCR_PRESALE_PDF",
            "docStage": DOC_STAGE_ESTIMATE,
            "remark": f"Presale estimate draft. seller={supplier or ''}",
            "itemList": item_list,
            "expenseList": [],
            "sourceType": "PDF_TEXT"
        }
    }


def parse_purchase(text):
    flat_text = normalize_text(text)
    partner = first_match(flat_text, rf"{KW_SELLER}[:：]\s*([^\n]+)")
    buyer = first_match(flat_text, r"采购方[:：]?\s*([^\n]+)")
    contract_no = first_match(flat_text, rf"{KW_CONTRACT_NO}[:：]?\s*([A-Z0-9/ ]+)")
    container_no = first_match(flat_text, rf"{KW_CONTAINER}[:：]?\s*([A-Z0-9]+)")
    expected_date = format_date(first_match(flat_text, rf"{KW_EXPECT}[:：]?\s*(\d{{4}}-\d{{2}}-\d{{2}})"))
    payment_due = format_date(first_match(flat_text, rf"{KW_PAY_NOTICE}\s*(\d{{4}}-\d{{2}}-\d{{2}})"))
    order_date = payment_due or expected_date

    lines = [line.strip() for line in text.splitlines() if line.strip()]
    total_amount = extract_purchase_total_amount(flat_text, lines)

    items = []
    seen = set()
    idx = 0
    while idx < len(lines):
        line = lines[idx]
        if re.match(r"^\d{5}[A-Z]\s+", line):
            combined = line
            cursor = idx + 1
            while cursor < len(lines):
                next_line = lines[cursor]
                if re.match(r"^\d{5}[A-Z]\s+", next_line):
                    break
                if next_line.startswith("合计") or next_line.startswith("북셍"):
                    break
                if next_line.startswith("采购方") or next_line.startswith("卖 方"):
                    break
                if next_line.startswith("日期") or next_line.startswith("第"):
                    break
                if next_line.startswith("客户订单确认函") or next_line.startswith("Customer Sales Order Confirmation"):
                    break
                combined += " " + next_line
                cursor += 1
            match = re.match(
                r"^(?P<code>\d{5}[A-Z])\s+"
                r"(?P<cn>[^\s]+)\s+"
                r"(?P<en>.+?)\s*"
                r"(?P<unit>KG|TON|箱|件)\s+"
                r"(?P<qty>[\d,]+\.\d+)\s+"
                r"(?P<price>[\d,]+\.\d+)\s+"
                r"(?P<total>[\d,]+\.\d+)$",
                combined
            )
            if match:
                code = match.group("code")
                qty = dec(match.group("qty"))
                price_incl = dec(match.group("price"))
                key = (code, str(qty), str(price_incl))
                if qty and price_incl and key not in seen:
                    seen.add(key)
                    total = dec(match.group("total"))
                    items.append({
                        "productCode": code,
                        "sourceProductCode": code,
                        "productName": match.group("cn").strip(),
                        "productNameEn": match.group("en").strip(),
                        "productSpec": None,
                        "warehouseName": DEFAULT_PURCHASE_WAREHOUSE,
                        "unit": match.group("unit").strip(),
                        "quantity": dec_str(qty, "0.00"),
                        "unitPriceInclTax": dec_str(price_incl, "0.0000"),
                        "lineTotalInclTax": dec_str(total, "0.00"),
                        "taxRate": "9.00",
                        "remark": f"OCR gross price {match.group('price')}"
                    })
            idx = cursor
            continue
        idx += 1

    if total_amount is None and items:
        total_amount = sum((dec(item.get("lineTotalInclTax")) or Decimal("0.00")) for item in items)

    expense_list = [{
        "expenseType": "WAREHOUSE",
        "expenseName": "PortMisc",
        "amount": "0.00",
        "taxRate": "0.00",
        "remark": "Auto filled by OCR draft, editable"
    }]
    return {
        "docType": "PURCHASE_CONFIRM",
        "orderDraft": {
            "orderType": "PURCHASE",
            "partnerName": partner,
            "brandName": partner,
            "buyerPartnerName": buyer,
            "contractNo": contract_no,
            "containerNo": container_no,
            "warehouseName": DEFAULT_PURCHASE_WAREHOUSE,
            "orderDate": order_date,
            "expectedDate": expected_date,
            "paymentDueDate": payment_due,
            "currency": "CNY",
            "status": 0,
            "bizType": "PURCHASE_INBOUND",
            "orderSource": "OCR_CONFIRM_NOTICE_PDF",
            "docStage": DOC_STAGE_CONFIRMED,
            "totalAmount": dec_str(total_amount, "0.00"),
            "remark": "OCR draft from purchase confirmation/payment notice",
            "itemList": items,
            "expenseList": expense_list,
            "sourceType": "PDF_TEXT"
        }
    }


def add_days(date_text, days):
    match = re.match(r"^(\d{4})-(\d{2})-(\d{2})", date_text or "")
    if not match:
        return None
    from datetime import date, timedelta
    year, month, day = map(int, match.groups())
    expiry = date(year, month, day) + timedelta(days=days)
    return f"{expiry.isoformat()} 00:00:00"


def parse_packing(text):
    flat_text = normalize_text(text)
    contract_no = first_match(flat_text, r"\b(B\d+/\d+)\b")
    container_no = first_match(flat_text, r"CONTAINER NO:\s*([A-Z]{4}\d{7})")
    if not container_no:
        container_no = first_match(flat_text, r"([A-Z]{4}\d{7})\s+[A-Z0-9]+CONTAINER NO:")
    shelf_life_days = first_match(flat_text, r"EXPIRATION DATES\s+(\d+)\s+DAYS FROM PRODUCTION DATE")
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    item_list = []
    idx = 0
    while idx < len(lines):
      line = lines[idx]
      item_match = re.match(r"^(?P<boxes>\d+)\s+CT\s+(?P<name>[A-Z0-9 \-]+?)\s+(?P<weight>[\d,]+\.\d+)Production Number\(s\)$", line)
      if item_match:
        name = item_match.group("name").strip()
        batch_list = []
        line_total_boxes = dec(item_match.group("boxes")) or Decimal("0")
        line_total_weight = dec(item_match.group("weight").replace(",", "")) or Decimal("0.00")
        if idx + 1 < len(lines):
          batch_line = lines[idx + 1]
          for box_count, production_date in re.findall(r"(\d+)\s+CT\s+(\d{4}-\d{2}-\d{2})", batch_line):
            box_count_dec = dec(box_count) or Decimal("0")
            weight = Decimal("0.00")
            if line_total_boxes:
              weight = (line_total_weight * box_count_dec / line_total_boxes).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            batch_list.append({
              "productionDate": f"{production_date} 00:00:00",
              "expiryDate": add_days(production_date, int(shelf_life_days or "0")),
              "boxCount": box_count,
              "weight": dec_str(weight, "0.00")
            })
        item_list.append({
          "productNameEn": name,
          "totalBoxes": str(int(line_total_boxes)),
          "totalWeight": dec_str(line_total_weight, "0.00"),
          "shelfLifeDays": shelf_life_days or "0",
          "batchList": batch_list
        })
        idx += 2
        continue
      idx += 1

    total_boxes = str(sum(int(item.get("totalBoxes") or 0) for item in item_list))
    total_weight = dec_str(sum((dec(item.get("totalWeight")) or Decimal("0.00")) for item in item_list), "0.00")
    return {
      "docType": "PACKING_LIST",
      "packingDraft": {
        "contractNo": contract_no,
        "containerNo": container_no,
        "shelfLifeDays": shelf_life_days or "0",
        "totalBoxes": total_boxes,
        "totalWeight": total_weight,
        "itemList": item_list
      }
    }


def is_presale_pdf(text):
    upper_text = text.upper()
    return "SALE CONFIRMATION" in upper_text and "SELLER'S CONTRACT NO" in upper_text


def is_confirm_notice_pdf(text):
    return KW_CONFIRM in text or "付款通知书" in text or KW_PAY_NOTICE in text or "采购方" in text


def is_packing_pdf(text):
    return "STATEMENT OF PRODUCTION DATES" in text.upper()


def recognize(file_path, order_type_hint=None):
    path = Path(file_path)
    suffix = path.suffix.lower()
    if suffix == ".pdf":
        text = load_pdf_text(path)
        if is_packing_pdf(text) or order_type_hint == "PACKING":
            result = parse_packing(text)
        elif is_presale_pdf(text):
            result = parse_presale_pdf(text)
        elif is_confirm_notice_pdf(text) or order_type_hint == "PURCHASE":
            result = parse_purchase(text)
        else:
            result = parse_presale_pdf(text)
        result["rawText"] = text
        return result

    items, lines = load_image_items(path)
    text = "\n".join(lines)
    upper_text = text.upper()
    if order_type_hint == "PACKING" or "STATEMENT OF PRODUCTION DATES" in upper_text:
        result = parse_packing(text)
    elif order_type_hint == "PURCHASE" or KW_CONFIRM in text or "CUSTOMER SALES ORDER CONFIRMATION" in upper_text:
        result = parse_purchase(text)
    else:
        result = parse_sales(items, text)
    result["rawText"] = text
    return result


def main():
    if len(sys.argv) < 2:
        print(json.dumps({"success": False, "message": "missing file path"}))
        return
    file_path = sys.argv[1]
    order_type_hint = sys.argv[2] if len(sys.argv) > 2 else None
    try:
        result = recognize(file_path, order_type_hint)
        result["success"] = True
        print(json.dumps(result, ensure_ascii=False))
    except Exception as exc:
        print(json.dumps({"success": False, "message": str(exc)}, ensure_ascii=False))


if __name__ == "__main__":
    main()

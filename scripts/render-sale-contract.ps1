param(
  [Parameter(Mandatory = $true)][string]$TemplatePath,
  [Parameter(Mandatory = $true)][string]$PayloadPath,
  [Parameter(Mandatory = $true)][string]$OutputXlsx,
  [Parameter(Mandatory = $true)][string]$OutputPdf,
  [Parameter(Mandatory = $false)][string]$StampPath
)

$ErrorActionPreference = 'Stop'

$payload = Get-Content -LiteralPath $PayloadPath -Raw -Encoding UTF8 | ConvertFrom-Json
$rows = @($payload.rows)
if ($rows.Count -le 0) {
  throw 'Contract rows cannot be empty'
}

function Get-VisualTextLength([string]$Text) {
  if ([string]::IsNullOrWhiteSpace($Text)) {
    return 0
  }
  $length = 0.0
  foreach ($ch in $Text.ToCharArray()) {
    if ([int][char]$ch -le 127) {
      $length += 0.55
    } else {
      $length += 1.0
    }
  }
  return [math]::Ceiling($length)
}

$excel = $null
$workbook = $null

try {
  $excel = New-Object -ComObject Excel.Application
  $excel.Visible = $false
  $excel.DisplayAlerts = $false
  $workbook = $excel.Workbooks.Open($TemplatePath)
  $sheet = $workbook.Worksheets.Item(1)

  $headerText = [string]$sheet.Range('A1').Value2
  $headerText = [regex]::Replace($headerText, '\d{4}/\d{1,2}/\d{1,2}$', [string]$payload.signDateText)
  $sheet.Range('A1').Value2 = $headerText

  $bodyText = [string]$sheet.Range('A2').Value2
  $bodyLines = [System.Collections.Generic.List[string]]::new()
  (($bodyText -split "`r?`n")) | ForEach-Object { [void]$bodyLines.Add($_) }
  if ($bodyLines.Count -ge 3) {
    $bodyLines[2] = ([string][char]0x4E59) + ([string][char]0x65B9) + ([string][char]0xFF1A) + ([string]$payload.secondaryPartnerName)
    $sheet.Range('A2').Value2 = [string]::Join("`r`n", $bodyLines.ToArray())
  }

  for ($index = 1; $index -lt $rows.Count; $index++) {
    $sheet.Rows.Item(5).Insert() | Out-Null
    $sheet.Rows.Item(4).Copy($sheet.Rows.Item(5)) | Out-Null
  }

  $dataStartRow = 4
  $dataEndRow = $dataStartRow + $rows.Count - 1
  $totalRow = $dataEndRow + 1
  $amountRow = $totalRow + 2
  $bottomPartyRow = 16 + ($rows.Count - 1)
  $feeOffset = $rows.Count - 1
  $spotFeeHeaderRow = 11 + $feeOffset
  $spotFeeValueRow = 12 + $feeOffset
  $coldStorageFreeDays = 7
  if ($payload.coldStorageFreeDays -ne $null) {
    $coldStorageFreeDays = [int]$payload.coldStorageFreeDays
  }
  if ($coldStorageFreeDays -le 0) {
    $coldStorageFreeDays = 7
  }
  $coldStorageFeeText = ([string][char]0x51B7) + ([string][char]0x5E93) + ([string][char]0x6536) + ([string][char]0x8D39) + ([string][char]0x6807) + ([string][char]0x51C6)
  $freeText = ([string][char]0x51CF) + ([string][char]0x514D)
  $dayText = [string][char]0x5929
  $insideText = [string][char]0x5185
  $includeText = [string][char]0x542B
  $leftParen = [string][char]0xFF08
  $rightParen = [string][char]0xFF09
  $coldStorageFreeText = $coldStorageFeeText + "`r`n" + $leftParen + $freeText + $coldStorageFreeDays + $dayText + $rightParen
  $spotFeeRangeText = ('{0}-30' -f ($coldStorageFreeDays + 1)) + $dayText + $insideText + $leftParen + $includeText + '30' + $dayText + $rightParen

  for ($index = 0; $index -lt $rows.Count; $index++) {
    $rowIndex = $dataStartRow + $index
    $row = $rows[$index]
    $sheet.Cells.Item($rowIndex, 1).Value2 = [string]$row.contractNo
    $sheet.Cells.Item($rowIndex, 2).Value2 = [string]$row.factoryNo
    $sheet.Cells.Item($rowIndex, 3).Value2 = [string]$row.containerNo
    $sheet.Cells.Item($rowIndex, 4).Value2 = [string]$row.productName
    $sheet.Cells.Item($rowIndex, 5).Value2 = [int]$row.boxes
    $sheet.Cells.Item($rowIndex, 6).Value2 = [double]$row.quantityKg
    $sheet.Cells.Item($rowIndex, 7).Value2 = [double]$row.salePriceKg
    $sheet.Cells.Item($rowIndex, 11).NumberFormat = '@'
    $sheet.Cells.Item($rowIndex, 11).Value2 = [string]$row.portCold
    $sheet.Cells.Item($rowIndex, 12).Value2 = [string]$row.arrivalText
    $sheet.Cells.Item($rowIndex, 8).Formula = "=J$rowIndex/1.09"
    $sheet.Cells.Item($rowIndex, 9).Formula = "=H$rowIndex*0.09"
    $sheet.Cells.Item($rowIndex, 10).Formula = "=F$rowIndex*G$rowIndex"

    $contractCell = $sheet.Cells.Item($rowIndex, 1)
    $contractCell.WrapText = $true
    $contractCell.ShrinkToFit = $false
    $productCell = $sheet.Cells.Item($rowIndex, 4)
    $productCell.WrapText = $true
    $productCell.ShrinkToFit = $false
    $sheet.Rows.Item($rowIndex).VerticalAlignment = -4160
    $sheet.Rows.Item($rowIndex).AutoFit() | Out-Null

    $contractLength = Get-VisualTextLength([string]$row.contractNo)
    $productLength = Get-VisualTextLength([string]$row.productName)
    $contractLineCount = [math]::Max(1, [math]::Ceiling($contractLength / 8.0))
    $productLineCount = [math]::Max(1, [math]::Ceiling($productLength / 13.0))
    $lineCount = [math]::Max($contractLineCount, $productLineCount)
    $targetHeight = [math]::Max(24, $lineCount * 18)
    if ($sheet.Rows.Item($rowIndex).RowHeight -lt $targetHeight) {
      $sheet.Rows.Item($rowIndex).RowHeight = $targetHeight
    }
  }

  $sheet.Cells.Item($totalRow, 5).Formula = ('=SUM(E{0}:E{1})' -f $dataStartRow, $dataEndRow)
  $sheet.Cells.Item($totalRow, 6).Formula = ('=SUM(F{0}:F{1})' -f $dataStartRow, $dataEndRow)
  $sheet.Cells.Item($totalRow, 10).Formula = ('=SUM(J{0}:J{1})' -f $dataStartRow, $dataEndRow)
  $sheet.Cells.Item($amountRow, 2).Formula = "=J$totalRow"
  $sheet.Cells.Item($spotFeeHeaderRow, 3).Value2 = $coldStorageFreeText
  $sheet.Cells.Item($spotFeeValueRow, 4).Value2 = [int]$coldStorageFreeDays
  $sheet.Cells.Item($spotFeeHeaderRow, 5).Value2 = $spotFeeRangeText
  $sheet.Cells.Item($bottomPartyRow, 7).Value2 = ('           ' + ([string][char]0x4E59) + ([string][char]0x65B9) + ':' + [string]$payload.secondaryPartnerName)

  if (-not [string]::IsNullOrWhiteSpace($StampPath) -and (Test-Path -LiteralPath $StampPath)) {
    $stampAnchor = $sheet.Cells.Item($bottomPartyRow, 3)
    $stampLeft = [double]$stampAnchor.Left - 45
    $stampTop = [double]$stampAnchor.Top - 85
    $stampSize = 224
    $stamp = $sheet.Shapes.AddPicture($StampPath, $false, $true, $stampLeft, $stampTop, $stampSize, $stampSize)
    $stamp.Placement = 1
  }

  $outputDir = Split-Path -Parent $OutputPdf
  if (-not (Test-Path -LiteralPath $outputDir)) {
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
  }

  $xlsxDir = Split-Path -Parent $OutputXlsx
  if (-not (Test-Path -LiteralPath $xlsxDir)) {
    New-Item -ItemType Directory -Force -Path $xlsxDir | Out-Null
  }

  $workbook.SaveAs($OutputXlsx, 51)
  $workbook.ExportAsFixedFormat(0, $OutputPdf)
}
finally {
  if ($workbook -ne $null) {
    try {
      $workbook.Close($false) | Out-Null
    } catch {
    }
  }
  if ($excel -ne $null) {
    try {
      $excel.Quit()
    } catch {
    }
  }
  [System.GC]::Collect()
  [System.GC]::WaitForPendingFinalizers()
}

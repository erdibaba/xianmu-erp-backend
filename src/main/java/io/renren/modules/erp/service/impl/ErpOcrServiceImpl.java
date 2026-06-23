package io.renren.modules.erp.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.modules.erp.service.ErpOcrService;
import io.renren.modules.erp.vo.ErpRecognizedPackingDraftVo;
import io.renren.modules.erp.vo.ErpRecognizedPackingItemVo;
import io.renren.modules.erp.vo.ErpRecognizeResultVo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("erpOcrService")
public class ErpOcrServiceImpl implements ErpOcrService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Override
  public ErpRecognizeResultVo recognize(MultipartFile file, String orderTypeHint) throws Exception {
    Path tempFile = null;
    try {
      String suffix = getSuffix(file.getOriginalFilename());
      tempFile = Files.createTempFile("erp-ocr-", suffix);
      file.transferTo(tempFile.toFile());
      Path savedPath = saveUploadFile(file, tempFile, suffix);

      File scriptFile = ensureScriptFile();
      ProcessBuilder processBuilder = new ProcessBuilder(
          "python",
          "-X",
          "utf8",
          scriptFile.getAbsolutePath(),
          tempFile.toAbsolutePath().toString(),
          StringUtils.defaultString(orderTypeHint, "")
      );
      processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();

      String output;
      try (InputStream inputStream = process.getInputStream();
           ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
        byte[] bytes = new byte[4096];
        int len;
        while ((len = inputStream.read(bytes)) != -1) {
          buffer.write(bytes, 0, len);
        }
        output = new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new RuntimeException("OCR脚本执行失败: " + output);
      }

      ErpRecognizeResultVo result = OBJECT_MAPPER.readValue(output, ErpRecognizeResultVo.class);
      if (result == null) {
        throw new RuntimeException("OCR结果为空");
      }
      if (result.getSuccess() == null) {
        result.setSuccess(Boolean.TRUE);
      }
      result.setSavedFilePath(savedPath.toAbsolutePath().toString());
      return result;
    } finally {
      if (tempFile != null) {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  @Override
  public ErpRecognizeResultVo recognize(MultipartFile[] files, String orderTypeHint) throws Exception {
    if (files == null || files.length == 0) {
      throw new RuntimeException("请上传单据图片或 PDF");
    }
    if (files.length == 1) {
      return recognize(files[0], orderTypeHint);
    }
    List<ErpRecognizeResultVo> results = new ArrayList<>();
    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        continue;
      }
      results.add(recognize(file, orderTypeHint));
    }
    if (results.isEmpty()) {
      throw new RuntimeException("请上传单据图片或 PDF");
    }
    if (!"PACKING".equalsIgnoreCase(StringUtils.defaultString(orderTypeHint))) {
      return results.get(0);
    }
    return mergePackingResults(results);
  }

  private ErpRecognizeResultVo mergePackingResults(List<ErpRecognizeResultVo> results) {
    ErpRecognizeResultVo merged = new ErpRecognizeResultVo();
    merged.setSuccess(Boolean.TRUE);
    merged.setDocType("PACKING_LIST");
    StringBuilder rawText = new StringBuilder();
    StringBuilder savedPaths = new StringBuilder();
    ErpRecognizedPackingDraftVo draft = new ErpRecognizedPackingDraftVo();
    List<ErpRecognizedPackingItemVo> itemList = new ArrayList<>();
    BigDecimal totalBoxes = BigDecimal.ZERO;
    BigDecimal totalWeight = BigDecimal.ZERO;
    for (ErpRecognizeResultVo result : results) {
      if (Boolean.FALSE.equals(result.getSuccess())) {
        merged.setSuccess(Boolean.FALSE);
        merged.setMessage(result.getMessage());
        return merged;
      }
      if (StringUtils.isNotBlank(result.getRawText())) {
        if (rawText.length() > 0) {
          rawText.append("\n\n");
        }
        rawText.append(result.getRawText());
      }
      if (StringUtils.isNotBlank(result.getSavedFilePath())) {
        if (savedPaths.length() > 0) {
          savedPaths.append(";");
        }
        savedPaths.append(result.getSavedFilePath());
      }
      ErpRecognizedPackingDraftVo current = result.getPackingDraft();
      if (current == null) {
        continue;
      }
      if (StringUtils.isBlank(draft.getContractNo())) {
        draft.setContractNo(current.getContractNo());
      }
      if (StringUtils.isBlank(draft.getContainerNo())) {
        draft.setContainerNo(current.getContainerNo());
      }
      if (StringUtils.isBlank(draft.getShelfLifeDays())) {
        draft.setShelfLifeDays(current.getShelfLifeDays());
      }
      if (current.getItemList() != null) {
        itemList.addAll(current.getItemList());
      }
      totalBoxes = totalBoxes.add(decimal(current.getTotalBoxes()));
      totalWeight = totalWeight.add(decimal(current.getTotalWeight()));
    }
    if (BigDecimal.ZERO.compareTo(totalBoxes) == 0) {
      for (ErpRecognizedPackingItemVo item : itemList) {
        totalBoxes = totalBoxes.add(decimal(item.getTotalBoxes()));
      }
    }
    if (BigDecimal.ZERO.compareTo(totalWeight) == 0) {
      for (ErpRecognizedPackingItemVo item : itemList) {
        totalWeight = totalWeight.add(decimal(item.getTotalWeight()));
      }
    }
    draft.setTotalBoxes(String.valueOf(totalBoxes.setScale(0, RoundingMode.HALF_UP).intValue()));
    draft.setTotalWeight(totalWeight.setScale(2, RoundingMode.HALF_UP).toPlainString());
    draft.setItemList(itemList);
    merged.setRawText(rawText.toString());
    merged.setSavedFilePath(savedPaths.toString());
    merged.setPackingDraft(draft);
    return merged;
  }

  private BigDecimal decimal(String value) {
    if (StringUtils.isBlank(value)) {
      return BigDecimal.ZERO;
    }
    try {
      return new BigDecimal(value.replace(",", "").trim());
    } catch (Exception ignored) {
      return BigDecimal.ZERO;
    }
  }

  private File ensureScriptFile() throws Exception {
    File target = new File(System.getProperty("java.io.tmpdir"), "erp_ocr.py");
    ClassPathResource resource = new ClassPathResource("ocr/erp_ocr.py");
    try (InputStream inputStream = resource.getInputStream()) {
      Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    FileUtils.touch(target);
    return target;
  }

  private String getSuffix(String filename) {
    if (StringUtils.isBlank(filename) || filename.lastIndexOf(".") < 0) {
      return ".tmp";
    }
    return filename.substring(filename.lastIndexOf("."));
  }

  private Path saveUploadFile(MultipartFile file, Path tempFile, String suffix) throws Exception {
    String baseDir = "D:\\renren-fast-vue\\renren-fast\\uploads\\ocr";
    String dayFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
    Path dir = Paths.get(baseDir, dayFolder);
    Files.createDirectories(dir);
    String originalName = StringUtils.defaultString(file.getOriginalFilename(), "ocr-file")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">");
    String safeName = originalName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    if (StringUtils.isBlank(safeName)) {
      safeName = "ocr-file" + suffix;
    }
    if (!safeName.toLowerCase().endsWith(suffix.toLowerCase())) {
      safeName = safeName + suffix;
    }
    Path target = dir.resolve(System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + safeName);
    Files.copy(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    return target;
  }
}

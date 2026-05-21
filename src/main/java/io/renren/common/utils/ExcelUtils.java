package io.renren.common.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel导出工具类
 * @author 404
 * headerTitle 表格标题
 * sheetTitle sheet名
 * fileName 文件名
 */
public class ExcelUtils {
	
	/**
	 * 工作薄对象
	 */
	private Workbook wb;
	
	/**
	 * 导出
	 * @param <E>
	 * @param list
	 * @param headerTitle
	 * @param sheetTitle
	 * @param fileName
	 * @param obj
	 * @return
	 */
	public static <E> ResponseEntity<byte[]> exportExcel(List<E> list,String headerTitle,String sheetTitle,String fileName,Class<?> obj){
		HttpHeaders headers = null;
		ByteArrayOutputStream baos = null;
    	try {
    		 Workbook workbook =ExcelExportUtil.exportExcel(new ExportParams(headerTitle,sheetTitle),obj, list);
    		 headers = new HttpHeaders();
             headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
             headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
             baos = new ByteArrayOutputStream();
             workbook.write(baos);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}
	
	/**
	 * 导入excel文件
	 * @param file
	 * @param obj
	 * @return
	 */
	public <E> List<E> importExcel(MultipartFile file,Class<?> obj){
		try {
			this.ImportExcel(file.getOriginalFilename(), file.getInputStream());
			ImportParams params = new ImportParams();
			params.setTitleRows(1);// 第一行标题
			params.setHeadRows(1);// 第二行表头
			params.setStartRows(1);//第三行才是导入数据
			List<E> dataList = ExcelImportUtil.importExcel(file.getInputStream(), obj, params);
			
			return dataList;
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void ImportExcel(String fileName, InputStream is) 
			throws InvalidFormatException, IOException {
		if (StringUtils.isBlank(fileName)){
			throw new RuntimeException("导入文档为空!");
		}else if(fileName.toLowerCase().endsWith("xls")){    
			this.wb = new HSSFWorkbook(is);    
        }else if(fileName.toLowerCase().endsWith("xlsx")){  
        	this.wb = new XSSFWorkbook(is);
        }else{  
        	throw new RuntimeException("文档格式不正确!");
        }  
		if (this.wb.getNumberOfSheets()<0){
			throw new RuntimeException("文档中没有工作表!");
		}
	}

	/**
	 * 动态导出
	 * @param list
	 * @param headerTitle
	 * @param sheetTitle
	 * @param fileName
	 * @return
	 */
	public static <E> ResponseEntity<byte[]> dynamicExportExcel(List<ExcelExportEntity> colList,String headerTitle,String sheetTitle,String fileName,List<Map<String, Object>> list){
		HttpHeaders headers =new HttpHeaders();
		ByteArrayOutputStream baos = null;
		Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(headerTitle, sheetTitle), colList,
				list);
		try {
			headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			baos = new ByteArrayOutputStream();
			workbook.write(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}


	/**
	 * 导入excel文件
	 * @param file
	 * @return
	 */
	public static List<Map<String, Object>> dynamicImportExcel(MultipartFile file,int startNumber){
		ImportParams params = new ImportParams();
		//params.setTitleRows(0);// 第一行标题
		params.setHeadRows(1);// 第二行表头
		params.setStartRows(startNumber);//第三行才是导入数据
		try {
			List<Map<String, Object>> list = ExcelImportUtil.importExcel(
					file.getInputStream(), Map.class, params);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

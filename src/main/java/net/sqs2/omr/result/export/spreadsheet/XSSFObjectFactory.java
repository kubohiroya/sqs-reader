/**
 * 
 */
package net.sqs2.omr.result.export.spreadsheet;

import net.sqs2.spreadsheet.SpreadSheetWorkbook;


import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class XSSFObjectFactory extends SpreadSheetObjectFactory {

	/**
	 * @param excelExportModule
	 */
	XSSFObjectFactory() {
	}
	
	public Workbook createWorkbook(){
		return new XSSFWorkbook();
	}
	
	public String getSuffix(){
		return "xlsx";
	}
	
	public RichTextString createRichTextString(String value){
		return new XSSFRichTextString(value);
	}
	
	public CellStyle getSelectSingleCellStyle(SpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.BLACK.index, HSSFColor.WHITE.index);
	}
	
	public CellStyle getSelectMultipleCellStyle(SpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.DARK_BLUE.index, HSSFColor.WHITE.index);
	}
	
	public CellStyle getErrorCellStyle(SpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.RED.index, HSSFColor.WHITE.index);
	}
	
	public CellStyle getTextAreaCellStyle(SpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.BLACK.index, HSSFColor.GREY_25_PERCENT.index);
	}
	
	public CellStyle getNoAnswerCellStyle(SpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.BLACK.index, HSSFColor.YELLOW.index);
	}
	
	public CellStyle getMultipleAnswersCellStyle(SpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.BLACK.index, HSSFColor.LIGHT_ORANGE.index);
	}

	@Override
	protected short getSolidForegroundCellStyle() {
		return XSSFCellStyle.SOLID_FOREGROUND;
	}
}
/**
 * 
 */
package net.sqs2.omr.result.export.spreadsheet;

import net.sqs2.spreadsheet.VirtualSpreadSheetWorkbook;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;

class HSSFObjectFactory extends AbstractSpreadSheetObjectFactory implements SpreadSheetObjectFactory{

	/**
	 * @param excelExportModule
	 */
	
	HSSFObjectFactory() {
	}
	
	public Workbook createWorkbook(){
		return new HSSFWorkbook();
	}
	
	public String getSuffix(){
		return "xls";
	}
	
	public RichTextString createRichTextString(String value){
		return new HSSFRichTextString(value);
	}
	

	protected short getSolidForegroundCellStyle(){
		return HSSFCellStyle.SOLID_FOREGROUND;
	}

	public CellStyle getSelectSingleCellStyle(VirtualSpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.BLACK.index, HSSFColor.WHITE.index);
	}
	
	public CellStyle getSelectMultipleCellStyle(VirtualSpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.DARK_BLUE.index, HSSFColor.WHITE.index);
	}
	
	public CellStyle getErrorCellStyle(VirtualSpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.RED.index, HSSFColor.WHITE.index);
	}
	
	public CellStyle getTextAreaCellStyle(VirtualSpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.BLACK.index, HSSFColor.GREY_25_PERCENT.index);
	}
	
	public CellStyle getNoAnswerCellStyle(VirtualSpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.BLACK.index, HSSFColor.YELLOW.index);
	}
	
	public CellStyle getMultipleAnswersCellStyle(VirtualSpreadSheetWorkbook spreadSheetWorkbook){
		return getCellStyle(spreadSheetWorkbook, HSSFColor.BLACK.index, HSSFColor.LIGHT_ORANGE.index);
	}

}
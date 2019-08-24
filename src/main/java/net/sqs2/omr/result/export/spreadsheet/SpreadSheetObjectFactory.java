package net.sqs2.omr.result.export.spreadsheet;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import net.sqs2.spreadsheet.SpreadSheetWorkbook;

public abstract class SpreadSheetObjectFactory{
	
	Map<String,CellStyle> cellStyleCache = new HashMap<String,CellStyle>();
	Map<Short,Font> fontCache = new HashMap<Short,Font>();
	

	public CellStyle getCellStyle(SpreadSheetWorkbook spreadSheetWorkbook, short fontColor, short backgroundColor) {
		String key = fontColor + "," + backgroundColor;
		CellStyle style = cellStyleCache.get(key);
		
		if (style == null) {
			style = createCellStyle(spreadSheetWorkbook, fontColor, backgroundColor);
			cellStyleCache.put(key, style);
		}
		return style;
	}
	
	protected CellStyle createCellStyle(SpreadSheetWorkbook spreadSheetWorkbook, short fontColor, short backgroundColor) {
		Font font = getFont(spreadSheetWorkbook, fontColor);
		CellStyle style = spreadSheetWorkbook.createCellStyle();
		style.setFont(font);
		style.setFillForegroundColor(backgroundColor);
		style.setFillPattern(getSolidForegroundCellStyle());
		return style;
	}

	private Font getFont(SpreadSheetWorkbook spreadSheetWorkbook, short fontColor) {
		Font font = fontCache.get(fontColor);
		if (font == null) {
			font = spreadSheetWorkbook.createFont();
			font.setColor(fontColor);
			fontCache.put(fontColor, font);
		}
		return font;
	}
	
	protected abstract short getSolidForegroundCellStyle();

	public abstract String getSuffix();


}
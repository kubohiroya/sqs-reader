package net.sqs2.omr.result.export.spreadsheet;

import net.sqs2.spreadsheet.VirtualSpreadSheetWorkbook;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

public abstract class AbstractSpreadSheetObjectFactory {

	public CellStyle getCellStyle(VirtualSpreadSheetWorkbook spreadSheetWorkbook, short fontColor, short backgroundColor) {
		String key = fontColor + "," + backgroundColor;
		CellStyle style = spreadSheetWorkbook.getCellStyleCache().get(key);
		if (style == null) {
			style = createCellStyle(spreadSheetWorkbook, fontColor, backgroundColor);
			spreadSheetWorkbook.getCellStyleCache().put(key, style);
		}
		return style;
	}
	
	protected CellStyle createCellStyle(VirtualSpreadSheetWorkbook spreadSheetWorkbook, short fontColor, short backgroundColor) {
		Font font = getFont(spreadSheetWorkbook, fontColor);
		CellStyle style = spreadSheetWorkbook.createCellStyle();
		style.setFont(font);
		style.setFillForegroundColor(backgroundColor);
		style.setFillPattern(getSolidForegroundCellStyle());
		return style;
	}

	private Font getFont(VirtualSpreadSheetWorkbook spreadSheetWorkbook, short fontColor) {
		Font font = spreadSheetWorkbook.getFontCache().get(fontColor);
		if (font == null) {
			font = spreadSheetWorkbook.createFont();
			font.setColor(fontColor);
			spreadSheetWorkbook.getFontCache().put(fontColor, font);
		}
		return font;
	}
	
	protected abstract short getSolidForegroundCellStyle();



}
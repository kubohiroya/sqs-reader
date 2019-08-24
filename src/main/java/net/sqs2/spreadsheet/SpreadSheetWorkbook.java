/*
  Copyright 2011 KUBO Hiroya (hiroya@cuc.ac.jp).
  
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2011/12/03

 */
package net.sqs2.spreadsheet;

import java.io.IOException;

import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class SpreadSheetWorkbook{
	protected Workbook currentWorkbook = null;
	private Sheet currentSheet;
	private String currentSheetName;
	protected Row currentRow;
	private int currentRowIndex = -1;

	public SpreadSheetWorkbook(Workbook workbook) {
		this.currentWorkbook = workbook;
	}

	public Workbook getWorkbook() {
		return this.currentWorkbook;
	}

	public void writeTo(OutputStream xlsOutputStream) throws IOException {
		this.currentWorkbook.write(xlsOutputStream);
	}

	public int getSheetIndex(String sheetName) {
		getSheet(sheetName);
		return this.currentWorkbook.getSheetIndex(sheetName);
	}

	public Sheet getSheet(String sheetName) {
		Sheet sheet = this.currentWorkbook.getSheet(sheetName);
		if (sheet == null) {
			// if the sheetName has not been defined, create it. 
			sheet = createSheet(sheetName);
		}
		return sheet;
	}

	protected Sheet createSheet(String sheetName) {
		return this.currentWorkbook.createSheet(sheetName);
	}

	public Row getRow(String sheetName, int rowIndex) {
		if(sheetName.equals(this.currentSheetName)) {
			if (this.currentRowIndex == rowIndex){
				// returns cached row instance
				return this.currentRow;
			}
		}else{
			this.currentSheet = getSheet(sheetName);
			this.currentSheetName = sheetName;
		}
		
		Row row = this.currentSheet.getRow(rowIndex);
		if (row == null) {
			// if the row has not been defined, create it. 
			row = this.currentSheet.createRow(rowIndex);
		}
		this.currentRowIndex = rowIndex;
		return this.currentRow = row;
	}

	public Cell getCell(String sheetName, int rowIndex, int columnIndex) {
		return getCell(getRow(sheetName, rowIndex), columnIndex);
	}

	public Cell getCell(Row row, int columnIndex) {
		Cell cell = row.getCell(columnIndex);
		if (cell == null) {
			// if the cell has not been defined, create it. 
			cell = row.createCell(columnIndex);
		}
		return cell;
	}
	
	public Font createFont(){
		return this.currentWorkbook.createFont();
	}
	
	public CellStyle createCellStyle(){
		return this.currentWorkbook.createCellStyle();
	}

}

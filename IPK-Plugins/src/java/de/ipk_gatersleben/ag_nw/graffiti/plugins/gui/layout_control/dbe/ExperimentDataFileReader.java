/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 06.04.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.BackgroundTaskStatusProvider;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression.TextFileColumnInformation;

public abstract class ExperimentDataFileReader
		implements BackgroundTaskStatusProvider {
	protected String status1 = "-";
	protected String status2 = "";
	private boolean please_stop = false;
	
	protected String fileName;
	
	protected double progressDouble = 0d;
	protected String optCoordinatorValue;
	protected String optExperimentName;
	protected String optTimeUnit;
	protected String optMeasurementUnit;
	
	public static TableData getExcelTableData(File excelFile) {
		return getExcelTableData(excelFile, null);
	}
	
	public static TableData getExcelTableData(File excelFile, ArrayList<String> optRelevantColumn) {
		final TableData myData = new TableData();
		try {
			FileInputStream fin = new FileInputStream(excelFile);
			ArrayList<TextFileColumnInformation> optValidColumn = null;
			if (optRelevantColumn != null) {
				optValidColumn = new ArrayList<TextFileColumnInformation>();
				for (String col : optRelevantColumn)
					optValidColumn.add(new TextFileColumnInformation(col, -1, -1));
			}
			
			if (excelFile.getName().toUpperCase().endsWith(".CSV") ||
					excelFile.getName().toUpperCase().endsWith(".DAT") ||
					excelFile.getName().toUpperCase().endsWith(".TXT") ||
					excelFile.getName().toUpperCase().endsWith(".LIST")) {
				processCSVExcelFile(myData, fin, -1, optValidColumn, null);
			} else {
				/*
				 * the old method is commented out, because you cant choose the work sheet and
				 * if you have several sheets in a file, the values of different sheets are mixed together.
				 * the new methods allows to choose the sheet (in every case the first)
				 */
				// if (excelFile.getName().toUpperCase().endsWith(".XLS"))
				// processBinaryExcelFile(myData, fin, -1, optValidColumn);
				// else
				processExcelFile(myData, fin, -1);
			}
			return myData;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("Could not read file: " + e);
			return null;
		}
	}
	
	public static TableData getExcelTableDataPeak(File excelFile, int maximumRowToBeProcessed) {
		return getExcelTableData(excelFile, maximumRowToBeProcessed, null, null);
	}
	
	public static TableData getExcelTableData(File excelFile,
			int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		return getExcelTableData(excelFile, maximumRowToBeProcessed, optValidColumns,
				optStatus, false);
	}
	
	public static TableData getExcelTableData(File excelFile,
			int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			boolean throwErrors) {
		final TableData myData = new TableData();
		try {
			FileInputStream fin = new FileInputStream(excelFile);
			if (excelFile.getName().toUpperCase().endsWith(".CSV") ||
					excelFile.getName().toUpperCase().endsWith(".DAT") ||
					excelFile.getName().toUpperCase().endsWith(".TXT") ||
					excelFile.getName().toUpperCase().endsWith(".LIST")) {
				processCSVExcelFile(myData, fin, maximumRowToBeProcessed, optValidColumns, optStatus);
			} else {
				/*
				 * the old method is commented out, because you cant choose the work sheet and
				 * if you have several sheets in a file, the values of different sheets are mixed together.
				 * the new methods allows to choose the sheet (in every case the first)
				 */
				// if (excelFile.getName().toUpperCase().endsWith(".XLS"))
				// processBinaryExcelFile(myData, fin, maximumRowToBeProcessed, optValidColumns);
				// else
				processExcelFile(myData, fin, maximumRowToBeProcessed);
			}
			return myData;
		} catch (InvalidFormatException e) {
			if (throwErrors)
				throw new UnsupportedOperationException(e);
			ErrorMsg.addErrorMessage("Could not read excel file: " + e);
			return null;
		} catch (Exception e) {
			if (throwErrors)
				throw new UnsupportedOperationException(e);
			ErrorMsg.addErrorMessage("Could not read file: " + e);
			return null;
		}
	}
	
	public static TableData getExcelTableData(String tableData,
			int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		final TableData myData = new TableData();
		processCSVExcelFile(myData, tableData, maximumRowToBeProcessed, optValidColumns, optStatus);
		return myData;
	}
	
	public static TableData getExcelTableData(BufferedReader csvFile,
			int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		TableData myData = new TableData();
		processCSVExcelFile(myData, csvFile, maximumRowToBeProcessed, optValidColumns, optStatus);
		return myData;
	}
	
	public static TableData getCSVdata(File textFile,
			ArrayList<String> arrayList, BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		try {
			HashSet<Integer> validColumns = new HashSet<Integer>();
			TableData headerData = new TableData();
			// read header and find relevant columns
			FileInputStream fin = new FileInputStream(textFile);
			processCSVExcelFile(headerData, fin, 1, null, null);
			for (int col = 1; col <= headerData.getMaximumCol(); col++) {
				String header = headerData.getUnicodeStringCellData(col, 1);
				if (header != null && header.length() > 0 && arrayList.contains(header)) {
					validColumns.add(col);
					System.out.println("valid: " + col);
				}
			}
			
			// read complete file, ignore non-relevant columns
			fin = new FileInputStream(textFile);
			TableData myData = new TableData();
			if (validColumns.size() > 0)
				processCSVExcelFile(myData, fin, -1, null, optStatus, validColumns);
			return myData;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	private static void processCSVExcelFile(TableData myData, BufferedReader in,
			int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		processCSVExcelFile(myData, in, maximumRowToBeProcessed, optValidColumns, optStatus, null);
	}
	
	@SuppressWarnings("deprecation")
	private static void processCSVExcelFile(TableData myData, BufferedReader in,
			int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			HashSet<Integer> validColumns) {
		int row = 0;
		try {
			String s;
			boolean first = true;
			String separator = "";
			while ((s = in.readLine()) != null) {
				if (s.length() > 0) {
					if (first) {
						String[] cellsComma = s.split(",");
						String[] cellsTab = s.split("\t");
						String[] cellsSemi = s.split(";");
						
						if (cellsComma.length > cellsTab.length && cellsComma.length > cellsSemi.length)
							separator = ",";
						else {
							if (cellsSemi.length > cellsTab.length)
								separator = ";";
							else
								separator = "\t";
						}
						first = false;
					}
					String[] cells;
					if (s.indexOf("\"") >= 0) {
						StringBuffer analysis = new StringBuffer();
						char[] chars = s.toCharArray();
						boolean inString = false;
						for (char c : chars) {
							if (c == '\"')
								inString = !inString;
							if (!inString) {
								if (c == separator.toCharArray()[0]) {
									c = '\\';
								}
							}
							analysis.append(c);
						}
						s = analysis.toString();
						cells = s.split("\\\\");
					} else
						cells = s.split(separator);
					int col = 0;
					for (String cell : cells) {
						if (col != 0 && !validColumn(col + 1, optValidColumns)) {
							col++;
							continue;
						}
						if (validColumns != null && !validColumns.contains(col + 1)) {
							col++;
							continue;
						}
						if (cell != null && cell.length() > 0) {
							if (cell.endsWith(";"))
								cell = cell.substring(0, cell.length() - 1);
							if (cell.startsWith("\"") && cell.endsWith("\"")) {
								cell = cell.substring(1, cell.length() - 1);
							}
							Object data = null;
							try {
								String cellN = StringManipulationTools.stringReplace(cell, ",", ".");
								Double d = Double.parseDouble(cellN);
								data = d;
							} catch (NumberFormatException nfe) {
								try {
									long ldt = Date.parse(cell);
									Date dt = new Date(ldt);
									data = dt;
								} catch (Exception err) {
									data = cell;
								}
							}
							if (data != null)
								myData.addCellData(col, row, data);
						}
						col++;
					}
				}
				row++;
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Row: " + row);
				if (maximumRowToBeProcessed > 0 && row > maximumRowToBeProcessed)
					break;
			}
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		try {
			in.close();
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void processCSVExcelFile(TableData myData, String ins,
			int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		int row = 0;
		boolean first = true;
		String separator = "";
		ins = StringManipulationTools.stringReplace(ins, "\r\n", "\n");
		for (String s : ins.split("\n")) {
			if (s.length() > 0) {
				if (first) {
					String[] cellsComma = s.split(",");
					String[] cellsTab = s.split("\t");
					String[] cellsSemi = s.split(";");
					
					if (cellsComma.length > cellsTab.length && cellsComma.length > cellsSemi.length)
						separator = ",";
					else {
						if (cellsSemi.length > cellsTab.length)
							separator = ";";
						else
							separator = "\t";
					}
					first = false;
				}
				String[] cells;
				if (s.indexOf("\"") > 0) {
					String specialSplit = "\\\\";
					StringBuilder analysis = new StringBuilder();
					String[] checkCells = s.split(separator);
					for (String cc : checkCells) {
						if (cc.indexOf("\"") >= 0) {
							int numberOfParenthesis = cc.length() - StringManipulationTools.stringReplace(cc, "\"", "").length();
							if (numberOfParenthesis == 1) {
								analysis.append(cc + separator);
							} else {
								analysis.append(cc + specialSplit);
							}
						} else
							analysis.append(cc + specialSplit);
					}
					s = analysis.toString();
					cells = s.split(specialSplit);
				} else
					cells = s.split(separator);
				int col = 0;
				for (String cell : cells) {
					if (!validColumn(col, optValidColumns)) {
						col++;
						continue;
					}
					if (cell != null && cell.length() > 0) {
						if (cell.endsWith(";"))
							cell = cell.substring(0, cell.length() - 1);
						if (cell.startsWith("\"") && cell.endsWith("\"")) {
							cell = cell.substring(1, cell.length() - 2);
						}
						Object data = null;
						try {
							String cellN = StringManipulationTools.stringReplace(cell, ",", ".");
							Double d = Double.parseDouble(cellN);
							data = d;
						} catch (NumberFormatException nfe) {
							try {
								long ldt = Date.parse(cell);
								Date dt = new Date(ldt);
								data = dt;
							} catch (Exception err) {
								data = cell;
							}
						}
						if (data != null)
							myData.addCellData(col, row, data);
					}
					col++;
				}
			}
			row++;
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Row: " + row);
			if (maximumRowToBeProcessed > 0 && row > maximumRowToBeProcessed)
				break;
		}
	}
	
	private static boolean validColumn(int col, ArrayList<TextFileColumnInformation> optValidColumns) {
		if (col == 0) // the first column will always be loaded; containing gene ids
			return true;
		
		if (optValidColumns == null)
			return true;
		for (TextFileColumnInformation tci : optValidColumns) {
			if (tci.getSignalColumn() == col)
				return true;
			if (tci.getDetectionColumn() != null && tci.getDetectionColumn().intValue() == col)
				return true;
		}
		return false;
	}
	
	private static void processCSVExcelFile(TableData myData, FileInputStream fin, int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		BufferedReader in = new BufferedReader(new InputStreamReader(fin));
		processCSVExcelFile(myData, in, maximumRowToBeProcessed, optValidColumns, optStatus);
	}
	
	private static void processCSVExcelFile(TableData myData, FileInputStream fin, int maximumRowToBeProcessed,
			ArrayList<TextFileColumnInformation> optValidColumns,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			HashSet<Integer> optValidColumnsNew) {
		BufferedReader in = new BufferedReader(new InputStreamReader(fin));
		processCSVExcelFile(myData, in, maximumRowToBeProcessed, optValidColumns, optStatus, optValidColumnsNew);
	}
	
	private static void processBinaryExcelFile(final TableData myData, FileInputStream fin,
			final int maximumRowToBeProcessed, ArrayList<TextFileColumnInformation> optValidColumn) throws IOException {
		POIFSFileSystem poifs = new POIFSFileSystem(fin);
		InputStream din = poifs.createDocumentInputStream("Workbook"); //$NON-NLS-1$
		HSSFRequest req = new HSSFRequest();
		HashSet<Integer> colidx = null;
		if (optValidColumn != null) {
			colidx = new HashSet<Integer>();
			for (TextFileColumnInformation t : optValidColumn) {
				if (t.getSignalColumn() >= 0)
					colidx.add(t.getSignalColumn());
			}
		}
		new HashSet<Integer>();
		
		// req.addListenerForAllRecords(new HSSFListener()
		req.addListener(new HSSFListener() {
			@Override
			public void processRecord(Record rec) {
				LabelSSTRecord recT = (LabelSSTRecord) rec;
				if (maximumRowToBeProcessed < 0 || recT.getRow() < maximumRowToBeProcessed)
					myData.addCellData(
							recT.getColumn(),
							recT.getRow(),
							myData.getStringRec().getString(recT.getSSTIndex()));
			}
		}, LabelSSTRecord.sid);
		
		req.addListener(new HSSFListener() {
			@Override
			public void processRecord(Record rec) {
				NumberRecord recT = (NumberRecord) rec;
				if (maximumRowToBeProcessed < 0 || recT.getRow() < maximumRowToBeProcessed)
					myData.addCellData(
							recT.getColumn(),
							recT.getRow(),
							new Double(recT.getValue()));
			}
		}, NumberRecord.sid);
		
		req.addListener(new HSSFListener() {
			@Override
			public void processRecord(Record rec) {
				FormulaRecord recT = (FormulaRecord) rec;
				if (maximumRowToBeProcessed < 0 || recT.getRow() < maximumRowToBeProcessed)
					myData.addCellData(
							recT.getColumn(),
							recT.getRow(),
							new Double(recT.getValue()));
			}
		}, FormulaRecord.sid);
		
		req.addListener(new HSSFListener() {
			@Override
			public void processRecord(Record rec) {
				SSTRecord recT = (SSTRecord) rec;
				myData.setStringRec(recT);
			}
		}, SSTRecord.sid);
		
		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processEvents(req, din);
	}
	
	private static void processExcelFile(TableData myData, FileInputStream fin,
			final int maximumRowToBeProcessed) throws IOException, InvalidFormatException {
		try {
			Workbook workbook = WorkbookFactory.create(fin);
			Sheet sheet = workbook.getSheetAt(0);
			int celltype;
			for (Row row : sheet) {
				if (maximumRowToBeProcessed > 0 && row.getRowNum() >= maximumRowToBeProcessed)
					break;
				for (Cell cell : row) {
					celltype = cell.getCellType();
					if (celltype == Cell.CELL_TYPE_FORMULA)
						celltype = cell.getCachedFormulaResultType();
					switch (celltype) {
						case Cell.CELL_TYPE_STRING:
							myData.addCellData(cell.getColumnIndex(), cell.getRowIndex(), cell.getStringCellValue());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							myData.addCellData(cell.getColumnIndex(), cell.getRowIndex(), new Double(cell.getNumericCellValue()));
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							myData.addCellData(cell.getColumnIndex(), cell.getRowIndex(), new Boolean(cell.getBooleanCellValue()));
							break;
					}
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		} catch (OutOfMemoryError e) {
			ErrorMsg.addErrorMessage("<html>Not enough memory to load such files! Please start the application with increased memory.<br>"
					+ e.getLocalizedMessage());
		} catch (Error e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
		} finally {
			fin.close();
		}
	}
	
	public ExperimentInterface getXMLdata(File excelFile, TableData myData,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider) {
		if (excelFile != null)
			fileName = excelFile.getName();
		else
			fileName = "not available";
		
		status1 = "Process data...";
		status2 = "";
		return getXMLDataFromExcelTable(excelFile, myData, statusProvider);
	}
	
	public abstract ExperimentInterface getXMLDataFromExcelTable(File excelFile, TableData myData,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider);
	
	/**
	 * @return
	 */
	protected boolean checkStopp() {
		if (please_stop) {
			status1 = "Processing incomplete, aborted";
			return true;
		}
		return false;
	}
	
	/**
	 * searches for a column that looks like this: 0test_3
	 * 0 is the time index (might be also 00, 01, 02, 2, 3, ...
	 * test is here the analysed part of a plant, in the dbe data
	 * termonology this is the analyzed "plant", the last
	 * _2, _3, _0, _1 is about double measurements, here we have
	 * the replicate number!
	 */
	protected static int getFirstDataColumn(TableData myData) {
		assert myData != null;
		int row = 1;
		int result = -1;
		for (int col = 1; col <= myData.getMaximumCol(); col++) {
			String txt = myData.getUnicodeStringCellData(col, row);
			if (txt != null && txt.length() > 2) {
				DataColumnHeader dc = new DataColumnHeader(txt, col);
				if (dc.isValid()) {
					result = col;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * get the column (in row 1) which matches the given pattern.
	 * Possible patterns are: "*searchText" -> any text in front and then the
	 * searchText,
	 * "*searchText*" -> return first column which contains "searchText",
	 * "searchText*" -> return first column which starts with searchText,
	 * "searchText", "searchText*searchText2" -> return first column which
	 * matches
	 * the given argument exactly.
	 * 
	 * @param myData
	 *           The TableData
	 * @param search
	 *           The pattern to look for
	 * @return The index of the column or -1, if from column 1 to
	 *         TableData.MAX_COLUMN
	 *         no match is found.
	 */
	protected static int getHeaderColumn(TableData myData, String search) {
		assert (search != null && search.length() > 0);
		int row = 1;
		int result = -1;
		for (int col = 1; col <= myData.getMaximumCol(); col++) {
			String txt = myData.getUnicodeStringCellData(col, row);
			if (txt == null || txt.length() <= 0)
				continue;
			search = search.toLowerCase();
			txt = txt.toLowerCase();
			boolean found = false;
			if (search.startsWith("*") && search.endsWith("*"))
				found = txt.contains(search.replace("*", ""));
			else
				if (search.startsWith("*"))
					found = txt.endsWith(search.replace("*", ""));
				else
					if (search.endsWith("*"))
						found = txt.startsWith(search.replace("*", ""));
					else
						found = txt.equals(search);
			if (found) {
				result = col;
				break;
			}
		}
		return result;
	}
	
	@Override
	public int getCurrentStatusValue() {
		return (int) progressDouble;
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		progressDouble = new Double(value);
	}
	
	@Override
	public double getCurrentStatusValueFine() {
		return progressDouble;
	}
	
	@Override
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	@Override
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	@Override
	public void pleaseStop() {
		please_stop = true;
	}
	
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	@Override
	public void pleaseContinueRun() {
		// empty
	}
	
	public void setCoordinator(String desiredCoordinatorValue) {
		this.optCoordinatorValue = desiredCoordinatorValue;
	}
	
	public void setExperimentName(String desiredExperimentName) {
		this.optExperimentName = desiredExperimentName;
	}
	
	public void setTimeUnit(String desiredTimeUnit) {
		this.optTimeUnit = desiredTimeUnit;
	}
	
	public void setMeasurementUnit(String desiredMeasurementUnit) {
		this.optMeasurementUnit = desiredMeasurementUnit;
	}
}

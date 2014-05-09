package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.AttributeHelper;
import org.ErrorMsg;
import org.SystemAnalysis;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * @author Sebastian Fröhlich (PBI),
 *         conversion to XLSX generation (instead of XLS), format improvements and error correction Christian Klukas (BA)
 */
public class ExperimentDataFileWriter {
	
	private Workbook workbook;
	private Sheet worksheet;
	private CreationHelper createHelper;
	
	private Font font;
	private CellStyle cellStyleHeadline;
	private CellStyle cellStyleGrey, cellStyleGreyCenter, cellStyleYellow, cellStyleYellowCenter;
	
	private CellStyle cellStyleBigHeadline;
	private CellStyle cellStyleData;
	private int rowStart;
	
	private ExperimentDataFileWriter() {
		initHSSFObjects();
		this.rowStart = 10;
		
	}
	
	/**
	 * Creates global objects for excel-java interaction, for instance cell styling: color, font.
	 */
	private void initHSSFObjects() {
		workbook = new XSSFWorkbook();
		worksheet = workbook.createSheet("Numeric Experiment Data");
		createHelper = workbook.getCreationHelper();
		
		// headline style
		cellStyleHeadline = workbook.createCellStyle();
		cellStyleHeadline.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyleHeadline.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
		cellStyleHeadline.setFillBackgroundColor(HSSFColor.BLACK.index);
		cellStyleHeadline.setAlignment(CellStyle.ALIGN_CENTER);
		
		cellStyleGrey = workbook.createCellStyle();
		cellStyleGrey.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyleGrey.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleGrey.setFillBackgroundColor(HSSFColor.BLACK.index);
		
		cellStyleGreyCenter = workbook.createCellStyle();
		cellStyleGreyCenter.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyleGreyCenter.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleGreyCenter.setFillBackgroundColor(HSSFColor.BLACK.index);
		cellStyleGreyCenter.setAlignment(CellStyle.ALIGN_CENTER);
		
		cellStyleYellow = workbook.createCellStyle();
		cellStyleYellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyleYellow.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		cellStyleYellow.setFillBackgroundColor(HSSFColor.BLACK.index);
		
		cellStyleYellowCenter = workbook.createCellStyle();
		cellStyleYellowCenter.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyleYellowCenter.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		cellStyleYellowCenter.setFillBackgroundColor(HSSFColor.BLACK.index);
		cellStyleYellowCenter.setAlignment(CellStyle.ALIGN_CENTER);
		// data style
		
		cellStyleData = workbook.createCellStyle();
		cellStyleData.setAlignment(CellStyle.ALIGN_CENTER);
		
		font = workbook.createFont();
		font.setFontHeightInPoints((short) 12);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setFontName(HSSFFont.FONT_ARIAL);
		
		// big headline style
		cellStyleBigHeadline = workbook.createCellStyle();
		cellStyleBigHeadline.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyleBigHeadline.setFillBackgroundColor(new HSSFColor.BLUE().getIndex());
		cellStyleBigHeadline.setFont(font);
		
	}
	
	public void addHeader(ExperimentInterface md) {
		ExperimentHeaderInterface header = md.getHeader();
		
		// fix
		createHeadline(2, 0, "Experiment");
		
		// Start of Experiment
		createCellIfNotExistsAndSet(3, 0, "Start of Experiment (Date):", cellStyleGrey);
		
		Cell cell2 = createCellIfNotExists(3, 1);
		
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setDataFormat(
				createHelper.createDataFormat().getFormat("m/d/yy"));
		cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		cellStyle.setFillBackgroundColor(HSSFColor.BLACK.index);
		cell2.setCellValue(header.getStartdate() == null ? new Date() : header.getStartdate());
		cell2.setCellStyle(cellStyle);
		
		// Remark*
		createKeyValuePairStyled(4, 0, "Remark*", header.getRemark(), cellStyleGrey, cellStyleYellow);
		createKeyValuePairStyled(5, 0, "ExperimentName (ID)", header.getExperimentname(), cellStyleGrey, cellStyleYellow);
		// Coordinator
		createKeyValuePairStyled(6, 0, "Coordinator", header.getCoordinator(), cellStyleGrey, cellStyleYellow);
		// Sequence-Name*
		createKeyValuePairStyled(7, 0, "Sequence-Name*", header.getSequence(), cellStyleGrey, cellStyleYellow);
		
		worksheet.autoSizeColumn(0);
		
		createHeadline(0, 0, "Experiment-Data - exported by " + SystemAnalysis.getUserName() + " using " +
				DBEgravistoHelper.DBE_GRAVISTO_VERSION +
				" (" + AttributeHelper.getDateString(new Date()) + ")");
		
	}
	
	private void addImportInfo() {
		createHeadline(2, 4, "Help");
		createCellIfNotExistsAndSet(3, 4, "- Fields with a * are optional", cellStyleGrey);
		createCellIfNotExistsAndSet(4, 4, "- Yellow cells allow input", cellStyleGrey);
		createCellIfNotExistsAndSet(5, 4, "** These cells must contain numbers as 1, 2, 3, ...", cellStyleGrey);
		createCellIfNotExistsAndSet(6, 4, "*** These cells must correlate to the numbers in **", cellStyleGrey);
		
		createCellIfNotExistsAndSet(3, 5, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(3, 6, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(4, 5, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(4, 6, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(5, 5, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(4, 6, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(5, 6, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(6, 6, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(6, 5, (String) null, cellStyleGrey);
		createCellIfNotExistsAndSet(4, 7, (String) null, cellStyleYellow);
		createCellIfNotExistsAndSet(5, 7, (String) null, cellStyleHeadline);
		createCellIfNotExistsAndSet(6, 7, (String) null, cellStyleHeadline);
	}
	
	private void addInternalInfo(ExperimentInterface md) {
		createHeadline(2, 9, "Internal Info");
		createOrdinaryCell(3, 9, "V1.2");
	}
	
	public void addSpeciesInformation(ExperimentInterface md) {
		// fix headline
		createHeadline(rowStart, 0, "Plants/Genotypes**");
		
		createCellIfNotExistsAndSet((rowStart + 1), 0, "Species", cellStyleGrey);
		createCellIfNotExistsAndSet((rowStart + 2), 0, "Variety*", cellStyleGrey);
		createCellIfNotExistsAndSet((rowStart + 3), 0, "Genotype", cellStyleGrey);
		createCellIfNotExistsAndSet((rowStart + 4), 0, "Growth conditions*", cellStyleGrey);
		createCellIfNotExistsAndSet((rowStart + 5), 0, "Treatment*", cellStyleGrey);
		
		int actualRow = this.rowStart;
		int actualSubstance = 1;
		md.numberConditions();
		// prevents double entries of conditions.
		HashSet<ConditionInterface> knownConditions = new HashSet<ConditionInterface>();
		for (SubstanceInterface substance : md) {
			for (ConditionInterface condition : substance) {
				if (!knownConditions.contains(condition)) {
					knownConditions.add(condition);
					// System.out.println("RowID:" + condition.getRowId());
					createCellIfNotExistsAndSet(actualRow, actualSubstance, condition.getRowId(), cellStyleHeadline);
					createCellIfNotExistsAndSet(actualRow + 1, actualSubstance, condition.getSpecies(), cellStyleYellowCenter);
					createCellIfNotExistsAndSet(actualRow + 2, actualSubstance, condition.getVariety(), cellStyleYellowCenter);
					createCellIfNotExistsAndSet(actualRow + 3, actualSubstance, condition.getGenotype(), cellStyleYellowCenter);
					createCellIfNotExistsAndSet(actualRow + 4, actualSubstance, condition.getGrowthconditions(), cellStyleYellowCenter);
					createCellIfNotExistsAndSet(actualRow + 5, actualSubstance, condition.getTreatment(), cellStyleYellowCenter);
					actualSubstance++;
				}
			}
		}
		
		rowStart = rowStart + 9;
	}
	
	/**
	 * Creates all constant Headlines for Measurement-Values.
	 * Prepares the Data in the given {@link ExperimentInterface} for easy creation in Excel-worksheet. Uses {@link DataRowExportExcel} for representation one
	 * row in Excel-worksheet.
	 * 
	 * @param md
	 */
	public void addGenoType(ExperimentInterface md) {
		final int ROW_TO_START = rowStart;
		
		// fix headlines
		createCellIfNotExistsAndSet(rowStart, 4, "Substance", cellStyleGreyCenter);
		createCellIfNotExistsAndSet((rowStart + 1), 4, "Meas.-Tool*", cellStyleGreyCenter);
		createCellIfNotExistsAndSet((rowStart + 2), 4, "Unit", cellStyleGreyCenter);
		
		createCellIfNotExistsAndSet((rowStart + 2), 0, "Plant/Genotype***", cellStyleGreyCenter);
		createCellIfNotExistsAndSet((rowStart + 2), 1, "ReplicateID #", cellStyleGreyCenter);
		createCellIfNotExistsAndSet((rowStart + 2), 2, "Time*", cellStyleGreyCenter);
		createCellIfNotExistsAndSet((rowStart + 2), 3, "Unit (Time)*", cellStyleGreyCenter);
		// big headline
		createHeadline(ROW_TO_START, 0, "Measurements");
		
		final int acutal_substance_column = 5;
		
		int actual_substance = 0;
		ListOfDataRowsExcelExport orderedList = new ListOfDataRowsExcelExport();
		
		ArrayList<SubstanceInterface> substances = new ArrayList<SubstanceInterface>(md);
		
		// prepare the Data to easily write in Excel-Worksheet
		for (SubstanceInterface substance : substances) {
			
			rowStart = ROW_TO_START;
			
			// because of start column = 4
			
			createCellIfNotExistsAndSet(rowStart, acutal_substance_column + actual_substance, substance.getName(), cellStyleYellowCenter);
			
			for (ConditionInterface series : substance) {
				for (SampleInterface sample : series) {
					for (NumericMeasurementInterface meas : sample) {
						// try to get from orderedList the entry with the following keys: ConditionID, TimeID, ReplicateID
						DataRowExcelExport data = orderedList.get(series.getConditionId(), sample.getTime(), meas.getReplicateID());
						if (null != data)
							data.addValue(substance.getName(), meas.getValue());
						// if entry does not exists create a new one
						else {
							DataRowExcelExport newDataRow = new DataRowExcelExport();
							newDataRow.setConditionID(series.getConditionId());
							newDataRow.setTimeID(sample.getTime());
							newDataRow.setTimeUnit(sample.getTimeUnit());
							newDataRow.setReplicateID(meas.getReplicateID());
							newDataRow.addValue(substance.getName(), meas.getValue());
							orderedList.add(newDataRow);
						}
						createCellIfNotExistsAndSet((rowStart + 1), acutal_substance_column + actual_substance, sample.getMeasurementtool(), cellStyleYellowCenter);
						createCellIfNotExistsAndSet((rowStart + 2), acutal_substance_column + actual_substance, meas.getUnit(), cellStyleYellowCenter);
					}
				}
			}
			// next substance - in excel worksheet next column
			++actual_substance;
			// System.out.println("Substance: " + substance.getName() + " no.: " + acutal_substance);
		}
		
		Collections.sort(orderedList, new Comparator<DataRowExcelExport>() {
			@Override
			public int compare(DataRowExcelExport o1, DataRowExcelExport o2) {
				if (new Integer(o1.conditionID).compareTo(new Integer(o2.conditionID)) == 0) {
					if (new Integer(o1.timeID).compareTo(new Integer(o2.timeID)) == 0)
						return new Integer(o1.replicateID).compareTo(new Integer(o2.replicateID));
					return new Integer(o1.timeID).compareTo(new Integer(o2.timeID));
				}
				return new Integer(o1.conditionID).compareTo(new Integer(o2.conditionID));
			}
		});
		
		// write the dataRows in Excel-Worksheet
		// write all DataRow-IDs
		int acual_mesurment = 0;
		Iterator<DataRowExcelExport> itDataRows = orderedList.iterator();
		for (int i = 0; i < orderedList.size(); i++) {
			DataRowExcelExport dataRow = itDataRows.next();
			createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 0, dataRow.getConditionID(), cellStyleHeadline);
			if (dataRow.getReplicateID() != -1)
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 1, dataRow.getReplicateID(), cellStyleYellowCenter);
			if (dataRow.getTimeID() != -1)
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 2, dataRow.getTimeID(), cellStyleYellowCenter);
			else
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 2, (String) null, cellStyleYellowCenter);
			if (!dataRow.getTimeUnit().equals("-1"))
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 3, dataRow.getTimeUnit(), cellStyleYellowCenter);
			else
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 3, (String) null, cellStyleYellowCenter);
			// write in every substance-column (excel-worksheet) the measurment-values
			HashMap<String, Double> values = dataRow.getValues();
			actual_substance = 0;
			for (SubstanceInterface substance : md) {
				
				Double numericValueBySubstance = values.get(substance.getName());
				// if measure value for these substance is null - do not write any measure value in these column
				if (numericValueBySubstance == null) {
					createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), acutal_substance_column + actual_substance, (Double) null,
							cellStyleYellowCenter);
					actual_substance++;
				} else {
					if (!numericValueBySubstance.isNaN())
						createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), acutal_substance_column + actual_substance, numericValueBySubstance,
								cellStyleYellowCenter);
					else
						createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), acutal_substance_column + actual_substance, "NaN",
								cellStyleYellowCenter);
					actual_substance++;
				}
			}
			
			acual_mesurment++;
		}
		for (int c = 1; c < acutal_substance_column + actual_substance; c++)
			worksheet.setColumnWidth(c, (int) (8000 * 15.29 / 30.57));
	}
	
	/**
	 * Creates for given index the row in the actual worksheet.
	 * 
	 * @param rowIndex
	 * @return
	 */
	private Row createRowIfNotExists(int rowIndex) {
		Row row = worksheet.getRow(rowIndex);
		if (null == row)
			row = worksheet.createRow(rowIndex);
		return row;
	}
	
	/**
	 * creates the worksheet cell for given row and column index.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	private Cell createCellIfNotExists(int rowIndex, int columnIndex) {
		Row row = createRowIfNotExists(rowIndex);
		Cell cell = row.getCell(columnIndex);
		if (null == cell)
			cell = row.createCell(columnIndex);
		return cell;
	}
	
	/**
	 * creates the worksheet cell for given row and column, fills it with the given text and the given style.
	 * If style is null, the cell is styled default
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *           - Text of cell
	 * @param style
	 *           - CellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, String text, CellStyle style) {
		Cell cell = createCellIfNotExists(rowIndex, columnIndex);
		if (text != null && !text.equals("null"))
			cell.setCellValue(text);
		if (style != null)
			cell.setCellStyle(style);
		
	}
	
	/**
	 * creates the worksheet cell for given row and column, fills it with the given text and the given style.
	 * If style is null, the cell is styled default
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *           - Text of cell
	 * @param style
	 *           - CellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, Integer text, CellStyle style) {
		Cell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		if (style != null)
			cell.setCellStyle(style);
	}
	
	/**
	 * creates the worksheet cell for given row and column, fills it with the given text and the given style.
	 * If style is null, the cell is styled default
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *           - Numeric value of cell
	 * @param style
	 *           - CellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, Double value, CellStyle style) {
		Cell cell = createCellIfNotExists(rowIndex, columnIndex);
		if (value != null)
			cell.setCellValue(value);
		if (style != null)
			cell.setCellStyle(style);
	}
	
	/**
	 * creates the worksheet cell for given row and column, fills it with the given text.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 *           - Text of cell
	 */
	private void createOrdinaryCell(int rowIndex, int columnIndex, String text) {
		Cell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
	}
	
	/**
	 * Creates two cells in direct neighborhood. Use these for (key,value)-pairs
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param key
	 * @param value
	 * @param cellStyle
	 */
	private void createKeyValuePairStyled(int rowIndex, int columnIndex, String key, String value,
			CellStyle cellStyleHeader, CellStyle cellStyleValue) {
		Cell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(key);
		if (cellStyleHeader != null)
			cell.setCellStyle(cellStyleHeader);
		
		Cell cell2 = createCellIfNotExists(rowIndex, columnIndex + 1);
		cell2.setCellValue(value);
		if (cellStyleValue != null)
			cell2.setCellStyle(cellStyleValue);
		
	}
	
	/**
	 * Creates a cell as headline. The Headline-Style is fix coded in method initHSSFObjects().
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 */
	private void createHeadline(int rowIndex, int columnIndex, String text) {
		createHeadline(rowIndex, columnIndex, text, 12);
	}
	
	private void createHeadline(int rowIndex, int columnIndex, String text, int fontSize) {
		Cell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		CellStyle cellStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) fontSize);
		font.setFontName("Gothic L");
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		cellStyle.setFont(font);
		cell.setCellStyle(cellStyle);
	}
	
	public static void writeExcel(File excelfile, ExperimentInterface md) {
		writeExcel(excelfile, md, false);
	}
	
	/**
	 * Export the given {@link ExperimentInterface} to the given {@link File}.
	 * 
	 * @param excelfile
	 * @param md
	 * @return
	 */
	public static void writeExcel(File excelfile, ExperimentInterface md, boolean transposed) {
		if (excelfile != null) {
			try {
				md = checkAndRemoveDoubleEntries(md);
				ExperimentDataFileWriter edfw = new ExperimentDataFileWriter();
				edfw.addHeader(md);
				edfw.addImportInfo();
				edfw.addInternalInfo(md);
				edfw.addSpeciesInformation(md);
				edfw.addGenoType(md);
				edfw.write(excelfile);
			} catch (Exception err) {
				ErrorMsg.addErrorMessage(err);
			}
		}
	}
	
	public static TableData getTableData(ExperimentInterface md) {
		md = checkAndRemoveDoubleEntries(md);
		ExperimentDataFileWriter edfw = new ExperimentDataFileWriter();
		edfw.addHeader(md);
		edfw.addImportInfo();
		edfw.addInternalInfo(md);
		edfw.addSpeciesInformation(md);
		edfw.addGenoType(md);
		return edfw.getTableData();
	}
	
	/**
	 * Some files contain double tuples (conditionID,sampleID,replicateID), which results in drop of some measurement values. we fix
	 * this by adjusting the replicateid for all measurements of one sample
	 * 
	 * @param md
	 * @return
	 */
	private static ExperimentInterface checkAndRemoveDoubleEntries(ExperimentInterface md) {
		md = md.clone();
		for (SubstanceInterface s : md)
			for (ConditionInterface c : s)
				for (SampleInterface sam : c) {
					HashMap<Integer, NumericMeasurementInterface> replid2meas = new HashMap<Integer, NumericMeasurementInterface>();
					for (NumericMeasurementInterface m : sam) {
						while (replid2meas.containsKey(m.getReplicateID()))
							m.setReplicateID(m.getReplicateID() + 1);
						replid2meas.put(m.getReplicateID(), m);
					}
				}
		
		return md;
	}
	
	/**
	 * Writes the given {@link File} in a buffered {@link FileOutputStream}.
	 * 
	 * @param excelfile
	 * @throws Exception
	 */
	private void write(File excelfile) throws Exception {
		OutputStream outStream = new FileOutputStream(excelfile);
		workbook.write(outStream);
		outStream.close();
	}
	
	private TableData getTableData() {
		TableData td = new TableData();
		
		for (int row = worksheet.getFirstRowNum(); row < worksheet.getLastRowNum(); row++) {
			Row r = worksheet.getRow(row);
			if (r != null)
				for (int cell = r.getFirstCellNum(); cell < r.getLastCellNum(); cell++) {
					Cell c = r.getCell(cell);
					if (c != null) {
						int cellType = c.getCellType();
						if (cellType == HSSFCell.CELL_TYPE_STRING)
							td.addCellData(cell, row, c.getStringCellValue());
						else
							if (cellType == HSSFCell.CELL_TYPE_NUMERIC)
								td.addCellData(cell, row, c.getNumericCellValue());
							else
								if (cellType == HSSFCell.CELL_TYPE_FORMULA)
									td.addCellData(cell, row, c.getCellFormula());
								else
									if (cellType == HSSFCell.CELL_TYPE_ERROR)
										td.addCellData(cell, row, c.getErrorCellValue());
									else
										if (cellType == HSSFCell.CELL_TYPE_BOOLEAN)
											td.addCellData(cell, row, c.getBooleanCellValue());
					}
				}
		}
		return td;
	}
}
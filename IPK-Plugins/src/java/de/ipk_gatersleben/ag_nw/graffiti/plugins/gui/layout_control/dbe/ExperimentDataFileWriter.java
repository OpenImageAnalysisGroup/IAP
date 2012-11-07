package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class ExperimentDataFileWriter {
	
	private HSSFWorkbook workbook;
	private HSSFSheet worksheet;
	private CreationHelper createHelper;
	
	private HSSFFont font;
	private HSSFCellStyle cellStyleHeadline;
	private HSSFCellStyle cellStyleGrey;
	
	private HSSFCellStyle cellStyleBigHeadline;
	private HSSFCellStyle cellStyleData;
	private int rowStart;
	
	private ExperimentDataFileWriter() {
		initHSSFObjects();
		this.rowStart = 10;
		
	}
	
	/**
	 * Creates global objects for excel-java interaction, for instance cell styling: color, font.
	 */
	private void initHSSFObjects() {
		workbook = new HSSFWorkbook();
		worksheet = workbook.createSheet("POI Worksheet");
		createHelper = workbook.getCreationHelper();
		
		// headline style
		cellStyleHeadline = workbook.createCellStyle();
		cellStyleHeadline.setFillPattern(HSSFCellStyle.BIG_SPOTS);
		cellStyleHeadline.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleHeadline.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		cellStyleGrey = workbook.createCellStyle();
		cellStyleGrey.setFillPattern(HSSFCellStyle.BIG_SPOTS);
		cellStyleGrey.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
		
		// data style
		
		cellStyleData = workbook.createCellStyle();
		cellStyleData.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		font = workbook.createFont();
		font.setFontHeightInPoints((short) 12);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setFontName(HSSFFont.FONT_ARIAL);
		
		// big headline style
		cellStyleBigHeadline = workbook.createCellStyle();
		cellStyleBigHeadline.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleBigHeadline.setFillBackgroundColor(new HSSFColor.BLUE().getIndex());
		cellStyleBigHeadline.setFont(font);
		
	}
	
	private void autoFitCells() {
		// fit the content to cell size
		for (int i = 0; i < 4; i++)
			worksheet.autoSizeColumn(i);
	}
	
	public void addHeader(ExperimentInterface md) {
		ExperimentHeaderInterface header = md.getHeader();
		
		HSSFRow row1 = worksheet.createRow((short) 0);
		HSSFCell cellA1 = row1.createCell(0, HSSFCell.CELL_TYPE_STRING);
		
		String name = "";
		if (MainFrame.getInstance() != null)
			name = " by " + MainFrame.getInstance().getTitle();
		
		cellA1.setCellValue("Exported" + name + " on " + AttributeHelper.getDateString(new Date()));
		
		// fix
		createHeadline(2, 0, "Experiment:");
		
		// Start of Experiment
		createCellIfNotExistsAndSet(3, 0, "Start of Experiment (Date):", cellStyleGrey);
		
		HSSFCell cell2 = createCellIfNotExists(3, 1);
		
		HSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setDataFormat(
				createHelper.createDataFormat().getFormat("m/d/yy"));
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		// cellStyle.setFillBackgroundColor(HSSFColor.GOLD.index);
		// cellStyle.setFillPattern(HSSFCellStyle.BIG_SPOTS);
		cell2.setCellValue(header.getStartdate() == null ? new Date() : header.getStartdate());
		cell2.setCellStyle(cellStyle);
		
		/*
		 * HSSFCellStyle cellStyleYellow = workbook.createCellStyle();
		 * cellStyleYellow.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
		 * cellStyleYellow.setFillPattern(HSSFCellStyle.BIG_SPOTS);
		 */

		// Remark*
		createKeyValuePairStyled(4, 0, "Remark*", header.getRemark(), cellStyleHeadline);
		createKeyValuePairStyled(5, 0, "ExperimentName (ID)", header.getExperimentname(), cellStyleHeadline);
		// Coordinator
		createKeyValuePairStyled(6, 0, "Coordinator", header.getCoordinator(), cellStyleHeadline);
		// Sequence-Name*
		createKeyValuePairStyled(7, 0, "Sequence-Name*", header.getSequence(), cellStyleHeadline);
		autoFitCells();
	}
	
	private void addImportInfo() {
		createHeadline(2, 4, "Help");
		createCellIfNotExistsAndSet(3, 4, "- Fields with a * are optional", cellStyleGrey);
		createCellIfNotExistsAndSet(4, 4, "** These cells must contain numbers as 1, 2, 3, ...", cellStyleGrey);
		createCellIfNotExistsAndSet(5, 4, "*** These cells must correlate to the numbers in **", cellStyleGrey);
		createCellIfNotExistsAndSet(6, 4,
				"- Using \"NaN\" instead of a blank input value makes sure, that the number of bars in the display is equal for all substances, gaps will be left",
				cellStyleGrey);
		createCellIfNotExistsAndSet(7, 4,
				"- If input values are left blank, the number of replicates is decreased for the sample, if no value is available for ", cellStyleGrey);
		
		createCellIfNotExistsAndSet(3, 5, "", cellStyleGrey);
		createCellIfNotExistsAndSet(3, 6, "", cellStyleGrey);
		createCellIfNotExistsAndSet(3, 7, "", cellStyleGrey);
		createCellIfNotExistsAndSet(4, 5, "", cellStyleGrey);
		createCellIfNotExistsAndSet(4, 6, "", cellStyleGrey);
		createCellIfNotExistsAndSet(4, 7, "", cellStyleGrey);
		createCellIfNotExistsAndSet(5, 5, "", cellStyleGrey);
		createCellIfNotExistsAndSet(5, 6, "", cellStyleGrey);
		createCellIfNotExistsAndSet(5, 7, "", cellStyleGrey);
		createCellIfNotExistsAndSet(6, 5, "", cellStyleGrey);
		createCellIfNotExistsAndSet(6, 6, "", cellStyleGrey);
		createCellIfNotExistsAndSet(6, 7, "", cellStyleGrey);
		createCellIfNotExistsAndSet(7, 5, "", cellStyleGrey);
		createCellIfNotExistsAndSet(7, 6, "", cellStyleGrey);
		createCellIfNotExistsAndSet(7, 7, "", cellStyleGrey);
		
		autoFitCells();
	}
	
	private void addInternalInfo(ExperimentInterface md) {
		createHeadline(2, 10, "Internal Info");
		createOrdinaryCell(3, 10, "V1.1");
		
		// HSSFCellStyle cellUnderlinedBold = workbook.createCellStyle();
		// font = workbook.createFont();
		// font.setFontHeightInPoints((short) 12);
		// font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		// font.setUnderline(Font.U_SINGLE);
		// cellUnderlinedBold.setFont(font);
		// createCellIfNotExistsAndSet(4, 10, "known Substances: ", cellUnderlinedBold);
		// createCellIfNotExistsAndSet(5, 10, "known Units: ", cellUnderlinedBold);
		// HashSet<String> uniqueSubstances = new HashSet<String>();
		// HashSet<String> uniqueUnits = new HashSet<String>();
		//		
		// for (SubstanceInterface substance : md) {
		// for (ConditionInterface series : substance) {
		// uniqueSubstances.add(substance.getName());
		// for (SampleInterface sample : series) {
		// for (NumericMeasurementInterface meas : sample) {
		// uniqueUnits.add(meas.getUnit());
		//						
		// }
		// }
		// }
		// }
		// int i = 0;
		// for (String unitName : uniqueUnits) {
		// createOrdinaryCell(4, 11 + i, unitName);
		// i++;
		// }
		// i = 0;
		// for (String substanceName : uniqueSubstances) {
		// createOrdinaryCell(5, 11 + i, substanceName);
		// i++;
		// }
		
	}
	
	public void addSpeciesInformation(ExperimentInterface md) {
		// fix headline
		createHeadline(rowStart, 0, "Plants/Genotypes**");
		
		createCellIfNotExistsAndSet((rowStart + 1), 0, "Species", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 0, "Variety*", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 3), 0, "Genotype", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 4), 0, "Growth conditions*", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 5), 0, "Treatment*", cellStyleHeadline);
		
		int actualRow = this.rowStart;
		int actualSubstance = 1;
		// prevents double entries of conditions.
		TreeSet<ConditionInterface> treesetOfConditions = new TreeSet<ConditionInterface>();
		for (SubstanceInterface substance : md) {
			for (ConditionInterface condition : substance) {
				if (!treesetOfConditions.contains(condition)) {
					treesetOfConditions.add(condition);
					// System.out.println("RowID:" + condition.getRowId());
					createCellIfNotExistsAndSet(actualRow, actualSubstance, condition.getRowId(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 1, actualSubstance, condition.getSpecies(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 2, actualSubstance, condition.getVariety(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 3, actualSubstance, condition.getGenotype(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 4, actualSubstance, condition.getGrowthconditions(), cellStyleData);
					createCellIfNotExistsAndSet(actualRow + 5, actualSubstance, condition.getTreatment(), cellStyleData);
					actualSubstance++;
				}
			}
		}
		
		rowStart = rowStart + 9;
		autoFitCells();
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
		createCellIfNotExistsAndSet(rowStart, 4, "Substance", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 1), 4, "Measur-Tool", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 4, "Unit", cellStyleHeadline);
		
		createCellIfNotExistsAndSet((rowStart + 2), 0, "Plant/Genotype***", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 1, "ReplicateID: ", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 2, "Time: ", cellStyleHeadline);
		createCellIfNotExistsAndSet((rowStart + 2), 3, "Unit (Time)", cellStyleHeadline);
		// big headline
		createHeadline(ROW_TO_START, 0, "Measurements");
		
		final int acutal_substance_column = 5;
		
		int acutal_substance = 0;
		ListOfDataRowsExcelExport orderedList = new ListOfDataRowsExcelExport();
		
		ArrayList<SubstanceInterface> substances = new ArrayList<SubstanceInterface>(md);
		
		if (substances.size() > 245) {
			MainFrame.showMessageDialog("<html>There are more than 245 substances available in this dataset.<br>" +
														"As the excel export is experimental at the moment, only the first<br>" +
														"245 substances will be written to the spreadsheet.", "Warning");
			for (int i = md.size() - 1; i >= 245; i--)
				substances.remove(i);
		}
		
		// prepare the Data to easily write in Excel-Worksheet
		for (SubstanceInterface substance : substances) {
			
			rowStart = ROW_TO_START;
			
			// because of start column = 4
			
			createCellIfNotExistsAndSet(rowStart, acutal_substance_column + acutal_substance, substance.getName(), cellStyleData);
			
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
						createCellIfNotExistsAndSet((rowStart + 1), acutal_substance_column + acutal_substance, sample.getMeasurementtool(), cellStyleData);
						createCellIfNotExistsAndSet((rowStart + 2), acutal_substance_column + acutal_substance, meas.getUnit(), cellStyleData);
					}
				}
			}
			// next substance - in excel worksheet next column
			++acutal_substance;
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
			createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 0, dataRow.getConditionID(), cellStyleData);
			if (dataRow.getReplicateID() != -1)
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 1, dataRow.getReplicateID(), cellStyleData);
			if (dataRow.getTimeID() != -1)
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 2, dataRow.getTimeID(), cellStyleData);
			if (!dataRow.getTimeUnit().equals("-1"))
				createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), 3, dataRow.getTimeUnit(), cellStyleData);
			// write in every substance-column (excel-worksheet) the measurment-values
			HashMap<String, String> values = dataRow.getValues();
			acutal_substance = 0;
			for (SubstanceInterface substance : md) {
				
				String measureBySubstance = values.get(substance.getName());
				// if measure value for these substance is null - do not write any measure value in these column
				if (null != measureBySubstance) {
					createCellIfNotExistsAndSet((acual_mesurment + ROW_TO_START + 3), acutal_substance_column + acutal_substance, measureBySubstance, cellStyleData);
					acutal_substance++;
				} else
					acutal_substance++;
			}
			
			acual_mesurment++;
		}
		autoFitCells();
	}
	
	/**
	 * Creates for given index the row in the actual worksheet.
	 * 
	 * @param rowIndex
	 * @return
	 */
	private HSSFRow createRowIfNotExists(int rowIndex) {
		HSSFRow row = worksheet.getRow(rowIndex);
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
	private HSSFCell createCellIfNotExists(int rowIndex, int columnIndex) {
		HSSFRow row = createRowIfNotExists(rowIndex);
		HSSFCell cell = row.getCell(columnIndex);
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
	 *           - HSSFCellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, String text, HSSFCellStyle style) {
		HSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
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
	 *           - HSSFCellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, Integer text, HSSFCellStyle style) {
		HSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
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
	 *           - HSSFCellStyle - if null - default style.
	 */
	private void createCellIfNotExistsAndSet(int rowIndex, int columnIndex, Double text, HSSFCellStyle style) {
		HSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
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
		HSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
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
	private void createKeyValuePairStyled(int rowIndex, int columnIndex, String key, String value, HSSFCellStyle cellStyle) {
		HSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(key);
		
		HSSFCell cell2 = createCellIfNotExists(rowIndex, columnIndex + 1);
		cell2.setCellValue(value);
		if (null != cellStyle)
			cell.setCellStyle(cellStyle);
		
	}
	
	/**
	 * Creates a cell as headline. The Headline-Style is fix codes in method initHSSFObjects().
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param text
	 */
	private void createHeadline(int rowIndex, int columnIndex, String text) {
		HSSFCell cell = createCellIfNotExists(rowIndex, columnIndex);
		cell.setCellValue(text);
		HSSFCellStyle cellStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) 12);
		font.setFontName("Gothic L");
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		cellStyle.setFont(font);
		cell.setCellStyle(cellStyle);
		autoFitCells();
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
		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(excelfile));
		try {
			workbook.write(outStream);
			outStream.flush();
		} finally {
			outStream.close();
		}
	}
	
	private TableData getTableData() {
		TableData td = new TableData();
		
		for (int row = worksheet.getFirstRowNum(); row < worksheet.getLastRowNum(); row++) {
			HSSFRow r = worksheet.getRow(row);
			if (r != null)
				for (int cell = r.getFirstCellNum(); cell < r.getLastCellNum(); cell++) {
					HSSFCell c = r.getCell(cell);
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
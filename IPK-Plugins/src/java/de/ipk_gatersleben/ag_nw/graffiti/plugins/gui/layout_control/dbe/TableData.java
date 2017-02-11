/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.graffiti.editor.MainFrame;
import org.graffiti.util.StringSplitter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class TableData {
	private static final String ENTREZ_GENE_AFY = "Entrez Gene";
	private static final String ENTREZ_GENE_AGI = "EntrezGeneID";
	private static final String PROBE_SET_ID_AFY = "Probe Set ID";
	private static final String PROBE_SET_ID_AGI = "ProbeID";
	
	public static final int MAX_COLUMN = 1024 * 1024; // 255;
	
	private final HashMap<Integer, Hashtable<Integer, Object>> worksheetData = new HashMap<Integer, Hashtable<Integer, Object>>();
	private final Hashtable<Integer, Integer> maxRowForColumn = new Hashtable<Integer, Integer>();
	
	private SSTRecord stringRec;
	
	private int maxRow = Integer.MIN_VALUE;
	private int maxCol = Integer.MIN_VALUE;
	
	// private long getIndex(long col, long row) {
	// return row * MAX_COLUMN + col;
	// }
	
	public TableData() {
	}
	
	public TableData(TableData copyData, boolean transposedWrote, int fromRow) {
		final int mCol = copyData.getMaximumCol();
		final int mRow = copyData.getMaximumRow();
		for (int col = 0; col <= mCol; col++) {
			for (int row = 0; row <= mRow; row++) {
				Object o;
				int targetCol;
				int targetRow;
				if (row >= fromRow) {
					targetCol = row - fromRow;
					targetRow = col + fromRow;
					o = copyData.getCellData(col, row, null);
					// System.out.println("Add Data / Source [col x row] ["+col+" x "+row+"] -> Target ["+targetCol+" x "+targetRow+"] max Col="+getMaximumCol()+"");
				} else {
					targetCol = col;
					targetRow = row;
					o = copyData.getCellData(col, row, null);
				}
				if (o != null && o instanceof String) {
					o = StringManipulationTools.htmlToUnicode((String) o);
				}
				if (o != null)
					addCellData(targetCol - 1, targetRow - 1, o);
			}
		}
		// int tCol = getMaximumCol();
		// int tRow = getMaximumRow();
		// System.out.println("Transposed Data from row "+fromRow+". Source ["+mCol+" x "+mRow+"] -> Target ["+tCol+" x "+tRow+"]");
	}
	
	public static TableData getTableData(File file, boolean throwErrors) {
		return ExperimentDataFileReader.getExcelTableData(file, -1, null, null, throwErrors);
	}
	
	public static TableData getTableData(File file) {
		return ExperimentDataFileReader.getExcelTableData(file, -1, null, null);
	}
	
	public int processAdditionaldentifiers(boolean processAllIDs, boolean processAllNewIDs,
			final ExperimentInterface substanceNodes, BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			double optStartProgress, double optEndProgress, StringBuilder statusMessage, boolean skipFirstRow,
			HashSet<Integer> ignoreColumns) {
		
		// first row description of columns
		// ArrayList<org.w3c.dom.Node> substanceNodes =
		// XPathHelper.getSubstanceNodes(substancenodes);
		double nodeCnt = substanceNodes.size();
		double workLoad = optEndProgress - optStartProgress;
		double nodeIdx = 0;
		HashMap<String, ArrayList<String>> mainId2alternativeIDs = new HashMap<String, ArrayList<String>>();
		int maxRow = getMaximumRow();
		String firstMapping = "";
		String lastMapping = "";
		int startRow = (skipFirstRow ? 2 : 1);
		for (int row = startRow; row <= maxRow; row++) {
			for (int mainCol = 1; mainCol <= getMaximumCol(); mainCol++) {
				if (mainCol == 1 || processAllNewIDs) {
					String mainID = getUnicodeStringCellData(mainCol, row);
					if (mainID != null && mainID.endsWith(".0"))
						mainID = mainID.substring(0, mainID.length() - 2);
					if (mainID != null && mainID.length() > 0) {
						if (!mainId2alternativeIDs.containsKey(mainID))
							mainId2alternativeIDs.put(mainID, new ArrayList<String>());
						ArrayList<String> knownAlternatives = mainId2alternativeIDs.get(mainID);
						int maxCol = 0;
						for (int col = 1; col <= getMaximumCol(); col++) {
							if (col == mainCol)
								continue;
							String alternativeID = getUnicodeStringCellData(col, row);
							if (alternativeID != null && alternativeID.length() > 0)
								maxCol = col;
						}
						for (int col = 1; col <= maxCol; col++) {
							if (col == mainCol)
								continue;
							String alternativeID = getUnicodeStringCellData(col, row);
							if (alternativeID == null)
								alternativeID = "";
							knownAlternatives.add(alternativeID);
						}
						if (firstMapping.length() <= 0) {
							String altIds = "";
							if (mainId2alternativeIDs.get(mainID) == null || mainId2alternativeIDs.get(mainID).size() <= 0)
								altIds = " / empty /";
							else {
								for (String altId : mainId2alternativeIDs.get(mainID)) {
									if (altIds.length() <= 0)
										altIds = "'" + altId + "'";
									else
										altIds += ", '" + altId + "'";
								}
							}
							firstMapping = "<li>row " + row + ": '" + mainID + "' ==> (" + altIds + ")";
						}
						if (row == maxRow) {
							String altIds = "";
							if (mainId2alternativeIDs.get(mainID) == null || mainId2alternativeIDs.get(mainID).size() <= 0)
								altIds = " / empty /";
							else {
								for (String altId : mainId2alternativeIDs.get(mainID)) {
									if (altIds.length() <= 0)
										altIds = "'" + altId + "'";
									else
										altIds += ", '" + altId + "'";
								}
								if (altIds.length() > 50)
									altIds = altIds.substring(0, 50) + "... // id count="
											+ mainId2alternativeIDs.get(mainID).size();
							}
							lastMapping = "<li>row " + row + ": '" + mainID + "' ==> (" + altIds + ")";
						}
					}
				}
			}
		}
		int matches = 0;
		int idCnt = 0;
		String matchStrings = "";
		int matchStringCnt = 0;
		for (SubstanceInterface xmlSubstanceNode : substanceNodes) {
			if (optStatus != null) {
				if (optStatus.wantsToStop())
					break;
			}
			
			String name = xmlSubstanceNode.getName();
			if (name != null) {
				if (optStatus != null)
					optStatus.setCurrentStatusText2("Process Substance " + (int) (nodeIdx + 1) + "/" + (int) nodeCnt + " ("
							+ name + ") (" + (processAllIDs ? "match all current IDs" : "match current main ID") + ")...");
				if (name.endsWith(".0"))
					name = name.substring(0, name.length() - 2);
				
				ArrayList<String> alternativeIds = mainId2alternativeIDs.get(name);
				String matchName = name;
				if (processAllIDs) {
					if (alternativeIds == null)
						alternativeIds = new ArrayList<String>();
					Collection<String> ids = xmlSubstanceNode.getSynonyms();
					if (ids != null)
						for (String nn : ids) {
							ArrayList<String> altIds = mainId2alternativeIDs.get(nn);
							if (altIds != null && altIds.size() > 0) {
								matchName += ";" + nn;
								alternativeIds.addAll(altIds);
							}
						}
				}
				String allMatch = "";
				if (alternativeIds != null && alternativeIds.size() > 0) {
					// process data
					matches += alternativeIds.size();
					xmlSubstanceNode.setSynonyme(0, name);
					int thisIdCnt = xmlSubstanceNode.getMaximumSynonymeIndex(1);
					for (String alternativeID : alternativeIds) {
						if (matchStringCnt < 5)
							allMatch += alternativeID + ";";
						xmlSubstanceNode.setSynonyme(thisIdCnt++, alternativeID);
					}
					if (matchStringCnt < 5)
						matchStrings += matchName + " --> " + allMatch + "<br>";
					matchStringCnt++;
				} else {
					// System.out.println("No match for: "+name);
				}
				idCnt += xmlSubstanceNode.getSynonyms().size();
			}
			nodeIdx = nodeIdx + 1;
			if (optStatus != null)
				optStatus.setCurrentStatusText2("Processed Substance: " + (int) nodeIdx + "/" + (int) nodeCnt
						+ " (main ID: " + name + ") (" + (processAllIDs ? "match all IDs" : "match main ID") + ")...");
			if (optStatus != null)
				optStatus.setCurrentStatusValueFine(optStartProgress + nodeIdx / nodeCnt * workLoad);
		}
		statusMessage.append("" + "<ul>Definition of first and last alternative identifier:" + firstMapping + ""
				+ lastMapping + ""
				+
				// (matchStrings.length()>0 ?
				// "<li>Matches:<br>"+matchStrings : "")+
				(matchStrings.length() > 0 ? "<li>First 5 matches:<br>" + matchStrings : "") + "</ul>" + "Matches: "
				+ matches + ", overall alternative ID count (empty values omitted): " + idCnt);
		return idCnt;
	}
	
	public synchronized boolean isDBEinputForm() {
		String v = getUnicodeStringCellData(10, 4);
		String v2 = getUnicodeStringCellData(11, 4);
		if (v != null || v2 != null) {
			if (v != null && v.toUpperCase().endsWith("_T")) {
				v = v.substring(0, v.length() - 2); // the T means "transposed from
				// a certain row on
			}
			if (v != null && v.toUpperCase().endsWith("T")) {
				v = v.substring(0, v.length() - 1); // the T means "transposed from
				// a certain row on
			}
			if (v2 != null && v2.toUpperCase().endsWith("_T")) {
				v2 = v2.substring(0, v2.length() - 2); // the T means "transposed
				// from a certain row on
			}
			if (v2 != null && v2.toUpperCase().endsWith("T")) {
				v2 = v2.substring(0, v2.length() - 1); // the T means "transposed
				// from a certain row on
			}
			if (v != null)
				if (v.equalsIgnoreCase("V1.2") || v.equalsIgnoreCase("V1.1"))
					return true;
			if (v2 != null)
				if (v2.equalsIgnoreCase("V1.2") || v2.equalsIgnoreCase("V1.1"))
					return true;
			return false;
		} else
			return false;
	}
	
	private static String[] knownHeaders = new String[] { "spot", "info", "score", "EST clust-ID", "Unique funcat" };
	
	public synchronized boolean isGeneExpressionFileFormatForm() {
		HashSet<String> headers = new HashSet<String>();
		for (int col = 1; col < 5; col++) {
			String v = getUnicodeStringCellData(col, 1);
			if (v != null && v.length() > 0)
				headers.add(v);
		}
		int hitCount = 0;
		for (String s : knownHeaders) {
			if (headers.contains(s))
				hitCount++;
		}
		return hitCount > 1;
	}
	
	public synchronized boolean isDBEtransposedInputForm() {
		String v = getUnicodeStringCellData(10, 4);
		String v2 = getUnicodeStringCellData(11, 4);
		if (v != null || v2 != null) {
			boolean transposed = false;
			if (v != null && v.toUpperCase().endsWith("_T")) {
				v = v.substring(0, v.length() - 2); // the T means "transposed from
				// a certain row on
				transposed = true;
			}
			if (v != null && v.toUpperCase().endsWith("T")) {
				v = v.substring(0, v.length() - 1); // the T means "transposed from
				// a certain row on
				transposed = true;
			}
			if (v2 != null && v2.toUpperCase().endsWith("_T")) {
				v2 = v2.substring(0, v2.length() - 2); // the T means "transposed
				// from a certain row on
				transposed = true;
			}
			if (v2 != null && v2.toUpperCase().endsWith("T")) {
				v2 = v2.substring(0, v2.length() - 1); // the T means "transposed
				// from a certain row on
				transposed = true;
			}
			if (v != null)
				if (v.equalsIgnoreCase("V1.2") || v.equalsIgnoreCase("V1.1"))
					return transposed;
			if (v2 != null)
				if (v2.equalsIgnoreCase("V1.2") || v2.equalsIgnoreCase("V1.1"))
					return transposed;
			return false;
		} else
			return false;
	}
	
	public synchronized String getTableStringData(int startRow, int endRow, int startCol, int endCol, String rowDivider,
			String colDivider) {
		StringBuilder res = new StringBuilder();
		for (int row = startRow; row <= endRow; row++) {
			for (int col = startCol; col <= endCol; col++) {
				String txt = getUnicodeStringCellData(col, row);
				res.append(txt);
				if (col < endCol)
					res.append(colDivider);
			}
			res.append(rowDivider);
		}
		return res.toString();
	}
	
	/**
	 * @param col
	 *           Column 0..n (different from getCellData)
	 * @param row
	 *           Row 0..n (different from getCellData)
	 * @param data
	 */
	public synchronized void addCellData(int col, int row, Object data) {
		// Trimming of Strings, because Excel Reader was not very strict and
		// added some space after the cell values
		if (!worksheetData.containsKey(col + 1))
			worksheetData.put(col + 1, new Hashtable<Integer, Object>());
		if (data instanceof String) {
			String str = ((String) data).trim();
			worksheetData.get(col + 1).put(row + 1, str);
		} else
			if (data instanceof UnicodeString) {
				String str = ((UnicodeString) data).getString().trim();
				worksheetData.get(col + 1).put(row + 1, str);
			} else {
				worksheetData.get(col + 1).put(row + 1, data);
			}
		if (row > maxRow)
			maxRow = row;
		if (col > maxCol)
			maxCol = col;
		if (maxRowForColumn.containsKey(col)) {
			int currMaxRowForCol = maxRowForColumn.get(col);
			if (row > currMaxRowForCol) {
				maxRowForColumn.remove(col);
				maxRowForColumn.put(col, row);
			}
		} else
			maxRowForColumn.put(col, row);
	}
	
	/**
	 * @param col
	 *           Column 1..n
	 * @param row
	 *           Row 1..n
	 * @param data
	 */
	public synchronized void addCellDataNG(int col, int row, Object data) {
		// Trimming of Strings, because Excel Reader was not very strict and
		// added some space after the cell values
		if (!worksheetData.containsKey(col))
			worksheetData.put(col, new Hashtable<Integer, Object>());
		if (data instanceof String) {
			String str = ((String) data).trim();
			worksheetData.get(col).put(row, str);
		} else
			if (data instanceof UnicodeString) {
				String str = ((UnicodeString) data).getString().trim();
				worksheetData.get(col).put(row, str);
			} else {
				worksheetData.get(col).put(row, data);
			}
		if (row > maxRow)
			maxRow = row;
		if (col > maxCol)
			maxCol = col;
		if (maxRowForColumn.containsKey(col)) {
			int currMaxRowForCol = maxRowForColumn.get(col);
			if (row > currMaxRowForCol) {
				maxRowForColumn.remove(col);
				maxRowForColumn.put(col, row);
			}
		} else
			maxRowForColumn.put(col, row);
	}
	
	/**
	 * @param col
	 *           Column (1...n)
	 * @param row
	 *           Row (1..n)
	 * @param expectIfNULL
	 *           Return value in case cell is empty
	 * @return
	 */
	public synchronized Object getCellData(int col, Integer row, Object expectIfNULL) {
		if (!worksheetData.containsKey(col))
			return expectIfNULL;
		Object result = worksheetData.get(col).get(row);
		if (result != null)
			return result;
		else
			return expectIfNULL;
	}
	
	public synchronized String getCellDataDate(int col, int row, String expectIfNULL) {
		try {
			Object o = getCellData(col, row, expectIfNULL);
			if (o != null && (o instanceof Date))
				return AttributeHelper.getDateString(((Date) o));
			Date jd1 = HSSFDateUtil.getJavaDate((Double) o);
			return AttributeHelper.getDateString(jd1);
		} catch (ClassCastException cce) {
			ErrorMsg.addErrorMessage("Could not return date data from column " + getExcelColumnName(col) + ", row " + row
					+ " [cell=" + getCellData(col, row, expectIfNULL) + "]!");
			return expectIfNULL;
		}
	}
	
	public synchronized Date getCellDataDateObject(int col, int row, Date expectIfNULL) {
		try {
			Object o = getCellData(col, row, expectIfNULL);
			if (o != null && (o instanceof Date))
				return ((Date) o);
			Date jd1 = HSSFDateUtil.getJavaDate((Double) o);
			return jd1;
		} catch (ClassCastException cce) {
			ErrorMsg.addErrorMessage("Could not return date data from column " + getExcelColumnName(col) + ", row " + row
					+ " [cell=" + getCellData(col, row, expectIfNULL) + "]!");
			return expectIfNULL;
		}
	}
	
	public synchronized String getUnicodeStringCellData(int col, int row) {
		Object r = getCellData(col, row, null);
		if (r != null && r instanceof String) {
			return (String) r; // //////// ErrorMsg.htmlToUnicode((String)r);
		} else
			if (r != null && r instanceof Double) {
				String rr = r.toString();
				if (rr.endsWith(".0"))
					return rr.substring(0, rr.length() - ".0".length());
				else
					return rr;
			} else
				return null;
	}
	
	public void setStringRec(SSTRecord stringRec) {
		this.stringRec = stringRec;
	}
	
	public SSTRecord getStringRec() {
		return stringRec;
	}
	
	public int getMaximumRow() {
		return maxRow + 1;
	}
	
	public int getMaximumCol() {
		return maxCol + 1;
	}
	
	public ArrayList<SampleEntry> getSamples(SubstanceColumnInformation sci, long plantOrGenotypeColumnRefID) {
		HashMap<String, ArrayList<ReplicateDouble>> plant_time_timeunit = new HashMap<String, ArrayList<ReplicateDouble>>();
		HashMap<String, Integer> plant_time_timeunit2time = new HashMap<String, Integer>();
		HashMap<String, Long> plant_time_timeunit2fineTime = new HashMap<String, Long>();
		
		HashMap<String, String> plant_time_timeunit2timeunit = new HashMap<String, String>();
		HashMap<String, String> plant_time_timeunit2mesunit = new HashMap<String, String>();
		for (int column : sci.getColumns()) {
			if (worksheetData.containsKey(column)) {
				for (Integer row : worksheetData.get(column).keySet()) {
					if (row < 23)
						continue;
					Object val = getDoubleCellData(column, row, null, false);
					boolean ok = false;
					if (val == null || !(val instanceof Double)) {
						if (val != null) {
							String sr = val.toString();
							if (val instanceof String) {
								sr = getUnicodeStringCellData(column, row);
								if (sr != null
										&& (sr.equalsIgnoreCase("-") || sr.equalsIgnoreCase("n/a") || sr.equalsIgnoreCase("na")))
									ok = true;
							}
							if (!ok)
								ErrorMsg.addErrorMessage("Non-Numeric value (" + sr + ") in column "
										+ getExcelColumnName(column) + ", row " + row + "!");
						}
						if (!ok)
							continue;
					}
					Object plantObj = getCellData(col("A"), row, null);
					if (plantObj != null) {
						if (!(plantObj instanceof Double) && !(plantObj instanceof Long)) {
							ErrorMsg.addErrorMessage("Non-Numeric value in column A, row " + row + "!");
							continue;
						}
						if (plantObj instanceof Double) {
							Double plantID = (Double) plantObj;
							if (plantID.intValue() == plantOrGenotypeColumnRefID) {
								processData(plant_time_timeunit, plant_time_timeunit2time,
										plant_time_timeunit2fineTime,
										plant_time_timeunit2timeunit,
										plant_time_timeunit2mesunit, column, row, val, plantID.longValue());
							}
						} else {
							Long plantID = (Long) plantObj;
							if (plantID == plantOrGenotypeColumnRefID) {
								processData(plant_time_timeunit, plant_time_timeunit2time,
										plant_time_timeunit2fineTime,
										plant_time_timeunit2timeunit,
										plant_time_timeunit2mesunit, column, row, val, plantID);
							}
						}
					}
				}
			}
		}
		ArrayList<SampleEntry> result = new ArrayList<SampleEntry>();
		for (String key : plant_time_timeunit.keySet()) {
			SampleEntry se = new SampleEntry(plant_time_timeunit2time.get(key),
					plant_time_timeunit2fineTime.get(key),
					plant_time_timeunit2timeunit.get(key),
					plant_time_timeunit2mesunit.get(key), plant_time_timeunit.get(key));
			result.add(se);
		}
		return result;
	}
	
	private Object getDoubleCellData(int column, Integer row, Object espectIfNull, boolean warnNonNumeric) {
		Object o = getCellData(column, row, espectIfNull);
		if (o != null && o instanceof String) {
			String os = getUnicodeStringCellData(column, row);
			os = os.replace(',', '.');
			try {
				Double od = Double.parseDouble(os);
				return od;
			} catch (Exception e) {
				if (warnNonNumeric)
					ErrorMsg.addErrorMessage("Input Format Warning: Instead of Numeric Value  a " + "Text Value (" + os
							+ ") that could not be converted to a numeric value " + getExcelColumnName(column) + " row " + row
							+ "!");
				// empty
			}
		}
		return o;
	}
	
	private void processData(HashMap<String, ArrayList<ReplicateDouble>> plant_time_timeunit,
			HashMap<String, Integer> plant_time_timeunit2time,
			HashMap<String, Long> plant_time_timeunit2optFineTime,
			HashMap<String, String> plant_time_timeunit2timeunit,
			HashMap<String, String> plant_time_timeunit2mesunit, int column, int row, Object val, Long plantID) {
		String plant = plantID.toString();
		Integer time = -1;
		Long optFineTime = null;
		String replicateNum = "-1";
		Object timeObj = getCellData(col("C"), row, null);
		if (timeObj != null && timeObj instanceof Double) {
			time = ((Double) timeObj).intValue();
			if (Math.abs((Double) timeObj - time) > 0.001)
				ErrorMsg.addErrorMessage("Warning: time value in " +
						"row " + row + ", column C is not a whole number! Time fraction is ignored!");
		} else
			if (timeObj != null && timeObj instanceof Integer) {
				time = (Integer) timeObj;
			} else {
				if (timeObj != null && timeObj instanceof String) {
					String timeString = (String) timeObj;
					time = Integer.parseInt(timeString.split(";", 2)[0]);
					optFineTime = Long.parseLong(timeString.split(";", 2)[1]);
				}
			}
			
		// plant_time_timeunit2optFineTime
		//
		String optQualityAnnotation = null;
		Object replObj = getCellData(col("B"), row, null);
		String optPosition = null;
		if (replObj != null && replObj instanceof Double)
			replicateNum = ((Double) replObj).toString();
		else
			if (replObj != null && replObj instanceof Integer)
				replicateNum = ((Integer) replObj).toString();
			else {
				if (replObj != null && replObj instanceof String) {
					String replicateString = (String) replObj;
					replicateNum = replicateString.split(";", 3)[0];
					optQualityAnnotation = replicateString.split(";", 3)[1];
					optPosition = replicateString.split(";", 3)[2];
				}
			}
		
		ReplicateDouble measureValue = new ReplicateDouble(val, replicateNum, optQualityAnnotation, optPosition);
		
		String timeUnit = getUnicodeStringCellData(col("D"), row);
		if (timeUnit == null)
			timeUnit = "-1";
		
		String plantIDandTime = plant + "$" + time + "$" + timeUnit + "$" + optFineTime;
		if (!plant_time_timeunit.containsKey(plantIDandTime)) {
			plant_time_timeunit.put(plantIDandTime, new ArrayList<ReplicateDouble>());
			plant_time_timeunit2time.put(plantIDandTime, time);
			plant_time_timeunit2timeunit.put(plantIDandTime, timeUnit);
			if (optFineTime != null)
				plant_time_timeunit2optFineTime.put(plantIDandTime, optFineTime);
			String mesUnit = getUnicodeStringCellData(column, 22);
			if (mesUnit == null) {
				ErrorMsg.addErrorMessage("Warning: No measurement unit in dataset in row 22, column "
						+ getExcelColumnName(column) + "!");
				mesUnit = "no unit set";
			}
			plant_time_timeunit2mesunit.put(plantIDandTime, mesUnit);
		}
		ArrayList<ReplicateDouble> measurementValues = plant_time_timeunit.get(plantIDandTime);
		measurementValues.add(measureValue);
	}
	
	/**
	 * converts the column from an integer to Excel column notation
	 * <p>
	 * column 1..26 -> A..Z<br>
	 * column 27..702 -> AA..ZZ<br>
	 * column 703..16384 -> AAA..XFD<br>
	 */
	public static String getExcelColumnName(int column) {
		String excelCol = "";
		int residue;
		while (column > 0) {
			residue = (column - 1) % 26 + 1;
			excelCol = (char) (residue + 64) + excelCol;
			column = (column - residue) / 26;
		}
		return excelCol;
	}
	
	private int col(String col) {
		if (col.length() == 1) {
			char c1 = col.charAt(0);
			return c1 - 64;
		} else {
			ErrorMsg.addErrorMessage("Invalid column specification / Internal Error!");
			return -1;
		}
	}
	
	public Collection<SubstanceColumnInformation> getSubstanceColumnInformation(String firstCol) {
		return getSubstanceColumnInformation(firstCol, false);
	}
	
	public Collection<SubstanceColumnInformation> getSubstanceColumnInformation(String firstCol, boolean keepDoubleSubstancenames) {
		HashMap<String, SubstanceColumnInformation> res = new HashMap<String, SubstanceColumnInformation>();
		ArrayList<SubstanceColumnInformation> resWithDoublettes = new ArrayList<SubstanceColumnInformation>();
		for (int colSubst = col(firstCol); colSubst <= getMaximumCol(); colSubst++) {
			String substName = getUnicodeStringCellData(colSubst, 20);
			if (substName != null) {
				if (keepDoubleSubstancenames) {
					SubstanceColumnInformation sci = new SubstanceColumnInformation();
					sci.addDataColumn(colSubst);
					resWithDoublettes.add(sci);
				} else {
					
					if (!res.keySet().contains(substName)) {
						SubstanceColumnInformation sci = new SubstanceColumnInformation();
						res.put(substName, sci);
					}
					SubstanceColumnInformation sci = res.get(substName);
					sci.addDataColumn(colSubst);
				}
			}
		}
		return keepDoubleSubstancenames ? resWithDoublettes : res.values();
	}
	
	public void showDataDialog() {
		showDataDialog(null);
	}
	
	// show a dialog with the table data
	public void showDataDialog(HashMap<Integer, String> optHeaders) {
		XlsTableModel xtm = new XlsTableModel(this, Integer.MAX_VALUE, Integer.MAX_VALUE);
		xtm.setColumnNames(optHeaders);
		JTable tbl = new MyDataJTable(xtm);
		tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane jsp = new JScrollPane(tbl);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		MainFrame.showMessageDialog("Table Data", jsp);
	}
	
	public JScrollPane getDataInScrollbars(HashMap<Integer, String> optHeaders) {
		XlsTableModel xtm = new XlsTableModel(this, Integer.MAX_VALUE, Integer.MAX_VALUE);
		// if (optHeaders != null)
		xtm.setColumnNames(optHeaders);
		JTable tbl = new MyDataJTable(xtm);
		tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane jsp = new JScrollPane(tbl);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		return jsp;
	}
	
	public int getMaximumRow(int column) {
		if (maxRowForColumn.containsKey(column))
			return maxRowForColumn.get(column);
		else
			return 0;
	}
	
	public TableData getTransposedDataset() {
		return new TableData(this, true, 0);
	}
	
	@Override
	public String toString() {
		return "Table Data (col/rows " + maxCol + " x " + maxRow + ")";
	}
	
	public void splitCells(String splitChar) {
		for (int row = 1; row <= getMaximumRow(); row++) {
			ArrayList<Object> valuesInRow = new ArrayList<Object>();
			for (int col = 1; col <= getMaximumCol(); col++) {
				Object v = getCellData(col, row, null);
				if (v != null) {
					if (v instanceof String) {
						String s = (String) v;
						if (s.indexOf(splitChar) >= 0) {
							String[] ss = StringSplitter.split(s, splitChar);
							for (String sss : ss) {
								valuesInRow.add(sss.trim());
							}
						} else
							valuesInRow.add(v);
					} else
						valuesInRow.add(v);
				}
			}
			if (valuesInRow.size() > 0) {
				clearRow(row);
				for (int col = 1; col <= valuesInRow.size(); col++) {
					addCellData(col - 1, row - 1, valuesInRow.get(col - 1));
				}
			}
		}
	}
	
	public void processGOanno(String splitChar, String preString, int minNumericLength) {
		int offRow = 0;
		for (int row = 1; row <= getMaximumRow(); row++) {
			ArrayList<Object> valuesInRow = new ArrayList<Object>();
			for (int col = 1; col <= getMaximumCol(); col++) {
				Object v = getCellData(col, row, null);
				if (v != null) {
					if (col > 1 && v instanceof String) {
						String s = (String) v;
						if (s.indexOf(splitChar) >= 0) {
							String[] ss = StringSplitter.split(s, splitChar);
							for (String sss : ss) {
								valuesInRow.add(sss.trim());
							}
						} else
							valuesInRow.add(v);
					} else
						valuesInRow.add(v);
				}
			}
			Object firstObject = null;
			if (valuesInRow.size() > 0) {
				clearRow(row);
				TreeSet<String> res = new TreeSet<String>();
				boolean first = true;
				for (Object o : valuesInRow) {
					String val = null;
					if (o instanceof String)
						val = (String) o;
					if (o instanceof Double)
						val = "" + ((Double) o).intValue();
					if (first) {
						if (val != null)
							firstObject = val;
						first = false;
						continue;
					}
					if (val != null && val.length() > 0) {
						try {
							int a = Integer.parseInt(val);
							String r = "" + a;
							while (r.length() < minNumericLength)
								r = "0" + r;
							res.add(preString + r);
						} catch (Exception e) {
							// ignore
						}
					}
				}
				valuesInRow.clear();
				valuesInRow.addAll(res);
				for (int col = 1; col <= valuesInRow.size(); col++) {
					if (col == 1) {
						if (firstObject != null)
							addCellData(col - 1, row - 1, firstObject);
					} else
						addCellData(col - 1, row - 1, valuesInRow.get(col - 1));
				}
			} else {
				offRow--;
				clearRow(row);
			}
		}
	}
	
	private void clearRow(int row) {
		int maxCol = getMaximumCol();
		for (int col = 1; col <= maxCol; col++) {
			Hashtable<Integer, Object> valuesInColumn = worksheetData.get(col);
			if (valuesInColumn != null) {
				valuesInColumn.remove(row);
			}
		}
	}
	
	public void processCellContentRemoveStringTags(String start, String end) {
		if (start == null)
			start = "";
		if (end == null)
			end = "";
		int aaa = 0;
		System.out.println("Rows: " + getMaximumRow());
		System.out.println("Cols: " + getMaximumCol());
		for (int col = 1; col <= getMaximumCol(); col++) {
			for (int row = 2; row <= getMaximumRow(col); row++) {
				// System.out.println("Rows: "+getMaximumRow(col));
				// System.out.print(".");
				Object o = getCellData(col, row, null);
				if (o != null && (o instanceof String)) {
					String s = (String) o;
					// String so = s;
					aaa++;
					if (start.length() <= 0 && end.length() > 0) {
						if (s.indexOf(end) > 0) {
							s = s.substring(0, s.indexOf(end));
							s = s.trim();
							addCellData(col - 1, row - 1, s);
						}
					} else
						if (start.length() > 0 && end.length() <= 0) {
							if (s.indexOf(start) >= 0) {
								s = s.substring(s.indexOf(start) + start.length());
								s = s.trim();
								addCellData(col - 1, row - 1, s);
							}
						} else
							if (start.length() > 0 && end.length() > 0) {
								s = StringManipulationTools.removeTags(s, start, end);
								s = s.trim();
								// System.out.println(s+" <-- "+so);
								addCellData(col - 1, row - 1, s);
								// System.out.println("--> "+getCellData(col, row, ""));
							}
				}
				Object o2 = getCellData(col, row, null);
				if (o2 != null && (o2 instanceof String)) {
					String s = (String) o2;
					if (s.indexOf(start) > 0) {
						System.out.println("ERR");
					}
				}
			}
		}
		System.out.println("AAA " + aaa);
	}
	
	public static ArrayList<String> getRelevantAffymetrixAnnotationColumnHeaders() {
		ArrayList<String> result = new ArrayList<String>();
		// result.add(SPECIES_SCIENTIFIC_NAME);
		result.add(PROBE_SET_ID_AFY);
		result.add(ENTREZ_GENE_AFY);
		result.add(PROBE_SET_ID_AGI);
		result.add(ENTREZ_GENE_AGI);
		return result;
	}
	
	/**
	 * @return set of processed columns which should be ignored by other
	 *         annotation column processing
	 */
	public HashSet<Integer> processAffymetrixAnnotationColumns(boolean processAffyGO, boolean processAffyEntrez) {
		HashSet<Integer> columnsToIgnoreColumns = new HashSet<Integer>();
		
		for (int col = 1; col <= getMaximumCol(); col++) {
			columnsToIgnoreColumns.add(col);
		}
		
		// int species_scientific_name_column = -1;
		// for (int col = 1; col<=getMaximumCol(); col++) {
		// String header = getUnicodeStringCellData(col, 1);
		// if (header!=null && header.equalsIgnoreCase(SPECIES_SCIENTIFIC_NAME)) {
		// species_scientific_name_column = col;
		// break;
		// }
		// }
		// if (species_scientific_name_column<0) {
		// ErrorMsg.addErrorMessage("Could not find 'Species Scientific Name'-column.");
		// return columnsToIgnoreColumns;
		// }
		
		int probe_set_id_column = -1;
		for (int col = 1; col <= getMaximumCol(); col++) {
			String header = getUnicodeStringCellData(col, 1);
			if (header != null && header.equalsIgnoreCase(PROBE_SET_ID_AFY)) {
				probe_set_id_column = col;
				columnsToIgnoreColumns.remove(probe_set_id_column);
				break;
			}
			if (header != null && header.equalsIgnoreCase(PROBE_SET_ID_AGI)) {
				probe_set_id_column = col;
				columnsToIgnoreColumns.remove(probe_set_id_column);
				break;
			}
		}
		if (probe_set_id_column < 0) {
			ErrorMsg.addErrorMessage("Could not find 'Probe Set ID' (Affymetrix Annotation) or 'ProbeID' (Agilent Annotation) column.");
			return columnsToIgnoreColumns;
		}
		
		int entrez_gene_column = -1;
		for (int col = 1; col <= getMaximumCol(); col++) {
			String header = getUnicodeStringCellData(col, 1);
			if (header != null && header.equalsIgnoreCase(ENTREZ_GENE_AFY)) {
				entrez_gene_column = col;
				columnsToIgnoreColumns.remove(entrez_gene_column);
				break;
			}
			if (header != null && header.equalsIgnoreCase(ENTREZ_GENE_AGI)) {
				entrez_gene_column = col;
				columnsToIgnoreColumns.remove(entrez_gene_column);
				break;
			}
		}
		if (entrez_gene_column < 0) {
			ErrorMsg.addErrorMessage("Could not find 'Entrez Gene'-column.");
			return columnsToIgnoreColumns;
		}
		
		// process data
		// probe_set_id_column
		// species_scientific_name_column
		// species_scientific_name2keggShortName
		// entrez_gene_column
		int filledRow = 1;
		String headerA = getUnicodeStringCellData(probe_set_id_column, 1);
		String headerB = getUnicodeStringCellData(entrez_gene_column, 1);
		clearRow(1);
		addCellData(0, 0, headerA);
		addCellData(1, 0, headerB);
		
		for (int row = 2; row <= getMaximumRow(); row++) {
			String probeID = getUnicodeStringCellData(probe_set_id_column, row);
			if (probeID == null || probeID.length() == 0 || probeID.equals("---")) {
				clearRow(row);
				continue;
			}
			String entrez = getUnicodeStringCellData(entrez_gene_column, row);
			clearRow(row);
			if (entrez == null || entrez.length() == 0 || entrez.equals("---")) {
				continue;
			}
			while (entrez.contains(" "))
				entrez = StringManipulationTools.stringReplace(entrez, " ", "");
			addCellData(0, filledRow, probeID);
			addCellData(1, filledRow, entrez);
			filledRow++;
		}
		return columnsToIgnoreColumns;
	}
	
	@SuppressWarnings("unchecked")
	public String getSampleValues(boolean headerRow, int col, int maxValues, String div, String ifNoValues) {
		LinkedHashSet<String> values = new LinkedHashSet();
		int startRow = headerRow ? 2 : 1;
		for (int row = startRow; row < getMaximumRow(); row++) {
			if (values.size() > maxValues)
				break;
			values.add(getUnicodeStringCellData(col, row));
		}
		
		if (values.size() == 0)
			return ifNoValues;
		else {
			ArrayList varr = new ArrayList(values);
			if (varr.size() > maxValues) {
				varr.remove(varr.size() - 1);
				varr.add("...");
			}
			return StringManipulationTools.getStringList(varr, div);
		}
	}
	
	public ArrayList<TableDataStringRow> getRowsAsStringValues() {
		ArrayList<TableDataStringRow> res = new ArrayList<TableDataStringRow>();
		for (int row = 1; row <= getMaximumRow(); row++) {
			TableDataStringRow r = new TableDataStringRow();
			for (int col = 1; col <= getMaximumCol(); col++) {
				r.setValue(col, getCellData(col, row, null));
			}
			res.add(r);
		}
		return res;
	}
	
	public void saveToExcelFile(String worksheetName, File annotationFile, BackgroundTaskStatusProviderSupportingExternalCall status) throws FileNotFoundException, IOException {
		if (status != null)
			status.setCurrentStatusValue(-1);
		if (status != null)
			status.setCurrentStatusText1("Set table data");
		SXSSFWorkbook wb = new SXSSFWorkbook();
		wb.setCompressTempFiles(true);
		CellStyle style = null;
		CellStyle styleTL = null;
		
		CellStyle cellStyleDate = null;
		
		Sheet infoSheet = wb.createSheet(worksheetName);
		Row header = infoSheet.createRow(0);
		style = wb.createCellStyle();
		styleTL = wb.createCellStyle();
		styleTL.setAlignment(CellStyle.ALIGN_LEFT);
		styleTL.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		styleTL.setWrapText(false);
		
		wb.createCellStyle();
		
		CreationHelper createHelper = wb.getCreationHelper();
		cellStyleDate = wb.createCellStyle();
		cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
		
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		
		for (int col = 0; col < getMaximumCol(); col++) {
			Cell c = header.createCell(col);
			c.setCellValue(getUnicodeStringCellData(col, 0));
			c.setCellStyle(styleTL);
		}
		
		for (int row = 1; row <= getMaximumRow(); row++) {
			Row r = infoSheet.createRow(row - 1);
			for (int col = 1; col <= getMaximumCol(); col++) {
				Object val = getCellData(col, row, null);
				if (val == null)
					continue;
				
				if (val instanceof Date) {
					Cell cc = r.createCell(col - 1);
					cc.setCellStyle(cellStyleDate);
					cc.setCellValue((Date) val);
				} else
					if (val instanceof Double)
						r.createCell(col - 1).setCellValue((Double) val);
					else
						if (val instanceof Integer)
							r.createCell(col - 1).setCellValue((Integer) val);
						else
							if (val instanceof Long)
								r.createCell(col - 1).setCellValue((Long) val);
							else
								if (!("" + val).isEmpty()) {
									Cell c = r.createCell(col - 1);
									c.setCellValue("" + val);
									c.setCellStyle(styleTL);
								}
			}
		}
		
		if (status != null)
			status.setCurrentStatusText1("Generate XSLX");
		wb.write(new FileOutputStream(annotationFile));
		wb.dispose();
		if (status != null)
			status.setCurrentStatusValueFine(100d);
	}
}

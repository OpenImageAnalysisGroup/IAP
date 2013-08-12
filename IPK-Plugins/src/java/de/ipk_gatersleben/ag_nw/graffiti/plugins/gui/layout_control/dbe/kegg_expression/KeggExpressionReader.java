/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.ErrorMsg;
import org.FolderPanel;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

public class KeggExpressionReader {
	
	/*
	 * http://www.genome.jp/download/KegArray/ReadMe-0_5_1Beta.txt
	 * - KEGG/EXPRESSION formet
	 * First column KEGG/GENES id which is the unique identifier of
	 * the ORF in the organism.
	 * Second column Y-axis coordinate information of the ORF on the
	 * microarray.
	 * Third column X-axis coordinate information of the gene.
	 * The second and third columns are used for
	 * specifying the location of the ORF in the schematic
	 * of the DNA microarray in the ArrayViewer application.
	 * Fourth column Intensity of the signal of the control channel.
	 * Fifth column Intensity of the background of the control channel.
	 * Sixth column Intensity of the signal of the target channel.
	 * Seventh column Intensity of the background of the target channel.
	 * When the subtraction of the background from the signal intensity
	 * (4th column - 5th column and 6th column - 7th column) becomes negative,
	 * KegArray considers the value as 1.
	 */

	TableData tabledata;
	int maxCommentRow = -1;
	
	public KeggExpressionReader(TableData tabledata) {
		this.tabledata = tabledata;
		maxCommentRow = getMaxCommentRow();
	}
	
	private int getMaxCommentRow() {
		if (tabledata == null)
			return -1;
		for (int row = 1; row <= tabledata.getMaximumRow(); row++) {
			String val = tabledata.getUnicodeStringCellData(1, row);
			if (val != null && !val.startsWith("#")) {
				return row - 1;
			}
		}
		return tabledata.getMaximumRow();
	}
	
	private String getCommentValue(String option) {
		for (int row = 1; row <= maxCommentRow; row++) {
			String val = tabledata.getUnicodeStringCellData(1, row);
			if (val != null && val.startsWith("#") && val.contains(option) && val.contains(":")) {
				String organism = val.substring(val.indexOf(":") + 1).trim();
				return organism;
			}
		}
		return null;
	}
	
	public String getOrganism() {
		return getCommentValue("organism");
	}
	
	public String getSubmitter() {
		return getCommentValue("submitter");
	}
	
	public String getCreationDate() {
		return getCommentValue("created");
	}
	
	public int getColumn(String columnDescription) {
		int descRow = findCommentRowStartingWith("ORF");
		if (descRow < 0)
			ErrorMsg.addErrorMessage("No row with header description (ORF X Y ...) found!");
		if (descRow >= 0) {
			for (int col = 1; col <= tabledata.getMaximumCol(); col++) {
				String val = tabledata.getUnicodeStringCellData(col, descRow);
				if (val != null && val.startsWith("#"))
					val = val.substring("#".length());
				if (val != null && val.equalsIgnoreCase(columnDescription)) {
					return col;
				}
			}
		}
		ErrorMsg.addErrorMessage("Column " + columnDescription + " not found!");
		return -1;
	}
	
	public int findCommentRowStartingWith(String startstring) {
		for (int row = 1; row <= tabledata.getMaximumRow(); row++) {
			String val = tabledata.getUnicodeStringCellData(1, row);
			if (val != null && val.startsWith("#" + startstring)) {
				return row;
			}
		}
		return -1;
	}
	
	public ArrayList<TextFileColumnInformation> getRawTextFileColumnInformation(
						boolean askUserForRelevantColumns) {
		ArrayList<TextFileColumnInformation> result = new ArrayList<TextFileColumnInformation>();
		HashSet<Integer> identifiedQualityTagColumns = new HashSet<Integer>();
		HashMap<String, Integer> columnName2columnIdx = new HashMap<String, Integer>();
		int row = 1;
		for (int col = 2; col <= tabledata.getMaximumCol(); col++) {
			String val = tabledata.getUnicodeStringCellData(col, row);
			if (val != null)
				columnName2columnIdx.put(val, col);
		}
		for (int col = 2; col <= tabledata.getMaximumCol(); col++) {
			if (identifiedQualityTagColumns.contains(col))
				continue;
			String val = tabledata.getUnicodeStringCellData(col, row);
			if (val != null && val.length() > 0) {
				if (val.endsWith(".0")) {
					val = val.substring(0, val.length() - ".0".length());
				}
				if (val.endsWith("_Signal")) {
					val = val.substring(0, val.length() - "_Signal".length());
					String checkDetectionColumnName = val + "_Detection";
					if (columnName2columnIdx.containsKey(checkDetectionColumnName)) {
						int detectionColumn = columnName2columnIdx.get(checkDetectionColumnName);
						TextFileColumnInformation ci = new TextFileColumnInformation(val, col, detectionColumn);
						result.add(ci);
						identifiedQualityTagColumns.add(detectionColumn);
					} else {
						TextFileColumnInformation ci = new TextFileColumnInformation(val, col, null);
						result.add(ci);
					}
				} else {
					TextFileColumnInformation ci = new TextFileColumnInformation(val, col, null);
					result.add(ci);
				}
			}
		}
		if (askUserForRelevantColumns) {
			FolderPanel checkBoxFp = new FolderPanel("Relevant Data Columns", false, true, false, null);
			checkBoxFp.setMaximumRowCount(10);
			// checkBoxFp.enableSearch(true);
			checkBoxFp.addCollapseListenerDialogSizeUpdate();
			HashMap<TextFileColumnInformation, JCheckBox> ci2cb = new HashMap<TextFileColumnInformation, JCheckBox>();
			for (TextFileColumnInformation ci : result) {
				JCheckBox cb = new JCheckBox(ci.getName() + " - Sign./Det. Col. " + ci.getSignalColumn() + "/" + ci.getDetectionColumn(), false);
				cb.setOpaque(false);
				checkBoxFp.addGuiComponentRow(new JLabel(""), cb, false);
				ci2cb.put(ci, cb);
			}
			checkBoxFp.layoutRows();
			Object[] res = MyInputHelper.getInput(
								"<html>" +
													"Please select the relevant data columns. If no selection is made,<br>" +
													"all columns will be processed. Depending on the amount of data,<br>" +
													"this process might take a few moments.",
								"Data-Column Selection",
								new Object[] {
													"", checkBoxFp
					});
			if (res == null)
				result.clear();
			else {
				ArrayList<TextFileColumnInformation> removeThese = new ArrayList<TextFileColumnInformation>();
				for (TextFileColumnInformation ci : result) {
					JCheckBox cb = ci2cb.get(ci);
					if (!cb.isSelected()) {
						removeThese.add(ci);
					}
				}
				if (removeThese.size() < result.size())
					result.removeAll(removeThese);
			}
		}
		return result;
	}
	
}

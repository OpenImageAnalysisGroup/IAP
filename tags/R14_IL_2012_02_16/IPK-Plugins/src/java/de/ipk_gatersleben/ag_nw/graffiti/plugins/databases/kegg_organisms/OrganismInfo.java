/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_organisms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.ErrorMsg;
import org.ReleaseInfo;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

public class OrganismInfo {
	private static boolean init = false;
	
	private static final TableData td = ExperimentDataFileReader.getExcelTableData(
						getFileReader("organism_list.txt"), -1, null, null);
	
	synchronized public static String getOrganismHierarchyInfo(
						String organismCode, String divide, String orgDesc) {
		String result = null;
		
		if (!init) {
			preProcessOrganismTable();
			init = true;
		}
		
		result = findLine(organismCode, divide, orgDesc);
		if (result != null)
			return result;
		else
			return "";
	}
	
	private static String findLine(String organismCode, String divide, String orgDesc) {
		for (int row = 1; row <= td.getMaximumRow(); row++) {
			String lookUp = td.getUnicodeStringCellData(7, row);
			if (lookUp != null && lookUp.equalsIgnoreCase(organismCode)) {
				String result = "";
				for (int col = 2; col <= 4; col++) {
					String val = td.getUnicodeStringCellData(col, row);
					if (val != null && val.length() > 0)
						result = result + divide + val;
				}
				String orgName = td.getUnicodeStringCellData(6, row);
				if (orgName.indexOf("(") > 0) {
					int pp = orgName.indexOf("(");
					int pp2 = orgName.indexOf(")", pp);
					if (pp > 0 && pp2 > 0) {
						String anno = orgName.substring(pp + 1, pp2);
						if (orgDesc.indexOf(anno) <= 0) {
							result = result + " [" + anno + "]";
						}
					}
				}
				if (result.startsWith(divide))
					return result.substring(divide.length());
			}
		}
		return null;
	}
	
	private static void preProcessOrganismTable() {
		// fill table from top to bottom, from left to right
		// int maxCol = td.getMaximumCol();
		int maxRow = td.getMaximumRow();
		for (int col = 2; col <= 4; col++) { // start with 2nd column, first one needs to be completely filled
			for (int row = 2; row <= maxRow; row++) { // start with 2nd row, first row needs to be filled completely
				String checkValue = td.getUnicodeStringCellData(col, row);
				if (checkValue != null && checkValue.trim().length() > 0)
					continue; // cell is filled, no need to fill it up with a value
				// from the prior row (same column)
				
				boolean fillOK = false;
				// fill only, if hierarchy info is the same for current row
				// as for prior row
				String left_val = td.getUnicodeStringCellData(col - 1, row);
				if (left_val != null) {
					String left_prior_val = td.getUnicodeStringCellData(col - 1, row - 1);
					fillOK = left_val.equalsIgnoreCase(left_prior_val);
				}
				if (fillOK) {
					String newValue = td.getUnicodeStringCellData(col, row - 1);
					if (newValue != null) {
						td.addCellData(col - 1, row - 1, newValue); // -1 , -1 because
					}
					// of differences in add / get code
				}
			}
		}
	}
	
	private static BufferedReader getFileReader(String fileName) {
		try {
			return new BufferedReader(new FileReader(ReleaseInfo.getAppFolderWithFinalSep() + fileName));
		} catch (Exception e) {
			try {
				ClassLoader cl = OrganismInfo.class.getClassLoader();
				String path = OrganismInfo.class.getPackage().getName().replace('.', '/');
				return new BufferedReader(new InputStreamReader(cl.getResourceAsStream(path + "/" + fileName)));
			} catch (Exception e2) {
				ErrorMsg.addErrorMessage("<html><b>Click Help/Database Status</b> to fix this problem: File not found: " + fileName);
				return null;
			}
		}
	}
}

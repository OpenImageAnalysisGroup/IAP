package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.HashMap;

import org.SystemOptions;

/**
 * Column 1: Plant ID
 * Column 2: Species Name
 * Column 3: Genotype
 * Column 4: Treatment
 * Column 5: Sequence
 * 
 * @author klukas
 */
public class TableDataStringRow {
	HashMap<Integer, String> col2val = new HashMap<Integer, String>();
	
	private String getString(int colFirstIs1) {
		return col2val.get(colFirstIs1);
	}
	
	public String getPlantID() {
		return getString(SystemOptions.getInstance().getInteger("File_Import", "Column_Plant-ID", 1));
	}
	
	public String getSpecies() {
		return getString(SystemOptions.getInstance().getInteger("File_Import", "Column_Species", 2));
	}
	
	public String getGenotype() {
		return getString(SystemOptions.getInstance().getInteger("File_Import", "Column_Genotype", 3));
	}
	
	public String getTreatment() {
		return getString(SystemOptions.getInstance().getInteger("File_Import", "Column_Treatment", 4));
	}
	
	public String getSequence() {
		return getString(SystemOptions.getInstance().getInteger("File_Import", "Column_Sequence", 5));
	}
	
	public void setValue(int col, Object cellData) {
		if (cellData != null)
			col2val.put(col, cellData + "");
	}
}

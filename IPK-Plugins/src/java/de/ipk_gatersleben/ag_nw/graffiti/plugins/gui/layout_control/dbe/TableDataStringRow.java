package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.Collection;
import java.util.HashMap;

import org.StringManipulationTools;

/**
 * @author klukas
 */
public class TableDataStringRow {
	HashMap<Integer, String> col2val = new HashMap<Integer, String>();
	
	public String getString(int colFirstIs1) {
		return col2val.get(colFirstIs1);
	}
	
	public void setValue(int col, Object cellData) {
		if (cellData != null)
			col2val.put(col, cellData + "");
	}
	
	public Collection<String> getValues() {
		return col2val.values();
	}
	
	public HashMap<Integer, String> getMap() {
		return col2val;
	}
	
	public String getString(Integer[] cols, String separator) {
		return StringManipulationTools.getStringList(col2val, cols, separator);
	}
}

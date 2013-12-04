package de.ipk.ag_ba.image.operations.blocks;

import ij.measure.ResultsTable;

import java.util.HashMap;

public class ResultsTableWithUnits extends ResultsTable implements Cloneable {
	HashMap<Integer, String> col2unit;
	
	/** Creates a copy of this ResultsTable. */
	@Override
	public synchronized Object clone() {
		ResultsTableWithUnits rt2 = (ResultsTableWithUnits) super.clone();
		rt2.col2unit = (HashMap<Integer, String>) col2unit.clone();
		return rt2;
	}
	
	public String getColumnHeadingUnit(int col) {
		synchronized (col2unit) {
			if (col2unit == null)
				return null;
			else
				return col2unit.get(col);
		}
	}
	
	public void addValue(String column, double value, String unit) {
		addValue(column, value);
		synchronized (col2unit) {
			if (col2unit == null)
				col2unit = new HashMap<Integer, String>();
			Integer colIdx = getColumnIndex(column);
			if (!col2unit.containsKey(colIdx))
				col2unit.put(colIdx, unit);
		}
	}
}

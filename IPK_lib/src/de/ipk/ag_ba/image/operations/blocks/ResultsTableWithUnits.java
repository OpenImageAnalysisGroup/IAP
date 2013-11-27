package de.ipk.ag_ba.image.operations.blocks;

import ij.measure.ResultsTable;

public class ResultsTableWithUnits extends ResultsTable implements Cloneable {
	/** Creates a copy of this ResultsTable. */
	@Override
	public synchronized Object clone() {
		ResultsTableWithUnits rt2 = (ResultsTableWithUnits) super.clone();
		return rt2;
	}
	
	public String getColumnHeadingUnit(int col) {
		return null;
	}
}

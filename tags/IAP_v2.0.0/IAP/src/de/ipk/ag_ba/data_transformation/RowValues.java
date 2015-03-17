package de.ipk.ag_ba.data_transformation;

import java.util.Map;

/**
 * @author klukas
 */
public class RowValues {
	private final Map<ColumnDescription, DynamicValue> columnValues;
	
	public RowValues(Map<ColumnDescription, DynamicValue> columnValues) {
		this.columnValues = columnValues;
	}
}

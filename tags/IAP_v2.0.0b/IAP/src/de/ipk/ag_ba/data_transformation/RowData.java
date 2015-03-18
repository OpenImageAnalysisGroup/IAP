package de.ipk.ag_ba.data_transformation;

/**
 * @author klukas
 */
public class RowData {
	private final RowData description;
	private final RowValues values;
	
	public RowData(RowData description, RowValues values) {
		this.description = description;
		this.values = values;
	}
}

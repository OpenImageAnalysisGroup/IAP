package de.ipk.ag_ba.data_transformation;

import java.util.Collection;

/**
 * @author klukas
 */
public class DataTable {
	private final Collection<RowData> rows;
	private final Collection<ColumnDescription> columns;
	
	public DataTable(Collection<ColumnDescription> columns, Collection<RowData> rows) {
		this.columns = columns;
		this.rows = rows;
	}
	
	public void debugPrintColumnNames() {
		boolean first = true;
		columns.forEach((cd) -> {
			
			System.out.print((first ? "\t" : "") + cd.getNiceName());
		});
		System.out.println();
	}
	
	public Collection<ColumnDescription> getColumns() {
		return columns;
	}
}

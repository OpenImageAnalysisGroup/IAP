/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 07.11.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

import org.StringManipulationTools;

public class XlsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	
	private TableData data;
	
	private int showMaxCol;
	
	private int showMaxRow;
	
	private HashMap<Integer, String> tableHeaders = null;
	
	public XlsTableModel(TableData data, int showMaxCol, int showMaxRow) {
		this.data = data;
		this.showMaxCol = showMaxCol;
		this.showMaxRow = showMaxRow;
	}
	
	public int getRowCount() {
		return data.getMaximumRow() > showMaxRow ? showMaxRow : data.getMaximumRow();
	}
	
	public int getColumnCount() {
		return data.getMaximumCol() > showMaxCol ? showMaxCol : data.getMaximumCol();
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object o = data.getCellData(columnIndex + 1, rowIndex + 1, null);
		if (o != null && o instanceof String) {
			return StringManipulationTools.htmlToUnicode((String) o);
		} else
			return o;
	}
	
	public void setColumnNames(HashMap<Integer, String> tableHeaders) {
		this.tableHeaders = tableHeaders;
	}
	
	@Override
	public String getColumnName(int arg0) {
		if (tableHeaders != null && tableHeaders.containsKey(arg0))
			return tableHeaders.get(arg0);
		else
			return super.getColumnName(arg0);
	}
	
}

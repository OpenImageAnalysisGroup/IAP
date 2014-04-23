/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (09.12.2001 22:54:36)
 * 
 * @author:
 */
public class TableDataTest extends javax.swing.table.AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected qmwi.kseg.som.DataSet dataSet;
	
	/**
	 * LeftWindowTableData constructor comment.
	 */
	public TableDataTest() {
		super();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (12.12.2001 15:03:23)
	 * 
	 * @param dataSet
	 *           quantitative.klukas.DataSet
	 */
	public TableDataTest(qmwi.kseg.som.DataSet dataSetTemp) {
		
		dataSet = dataSetTemp;
		
	}
	
	/**
	 * getColumnCount method comment.
	 */
	public int getColumnCount() {
		// return dataSet.groups.size();
		return 20;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (12.12.2001 15:06:52)
	 * 
	 * @param c
	 *           int
	 */
	@Override
	public String getColumnName(int c) {
		/*
		 * String end = (String) dataSet.groups.elementAt(c);
		 * String start = String.valueOf(c)+"  ";
		 * String name = start+end;
		 * return name;
		 */
		return new String("Test" + c);
	}
	
	/**
	 * getRowCount method comment.
	 */
	public int getRowCount() {
		// return dataSet.data.size();
		return 10;
	}
	
	/**
	 * getValueAt method comment.
	 */
	public Object getValueAt(int row, int column) {
		
		// quantitative.klukas.DataEntry dataEntry = (quantitative.klukas.DataEntry) dataSet.data.elementAt(row);
		
		// return dataEntry.eingaben[column];
		
		return new String("TestString" + row + " " + column);
	}
}

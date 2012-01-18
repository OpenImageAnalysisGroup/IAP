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
public class DiagrammPaneData extends javax.swing.table.AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ClassesAll cAll;
	public AttributsAll aAll;
	public java.lang.String[] columnNames;
	public int extraColumns;
	
	/**
	 * LeftWindowTableData constructor comment.
	 */
	public DiagrammPaneData() {
		super();
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (12.12.2001 15:03:23)
	 * 
	 * @param dataSet
	 *           quantitative.klukas.DataSet
	 */
	public DiagrammPaneData(ClassesAll c, AttributsAll a) {
		
		cAll = c;
		
		aAll = a;
		
		extraColumns = 1;
		
		setColumnNames();
		
	}
	
	/**
	 * getColumnCount method comment.
	 */
	public int getColumnCount() {
		return aAll.getCountAttributs() + extraColumns;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 13:16:03)
	 * 
	 * @return java.lang.String
	 */
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
	/**
	 * getRowCount method comment.
	 */
	public int getRowCount() {
		return aAll.getCountClasses();
	}
	
	/**
	 * getValueAt method comment.
	 */
	public Object getValueAt(int row, int column) {
		
		if (column == 0)
			return new Integer(cAll.getCountObjects(row));
		
		else {
			/*
			 * Aus.a("row",row);
			 * Aus.a("col",column);
			 * Aus.a("aAll.getCountClasses()",aAll.getCountClasses());
			 * Aus.a("aAll.getCountAttributs()",aAll.getCountAttributs());
			 * Aus.a("aAll.getClass(row).getAttribut(column).average",aAll.getClass(row).getAttribut(column-extraColumns).average);
			 */

			// return new Float(aAll.getClass(row).getAttribut(column).values.size());
			
			return new Integer(0);
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 12:49:34)
	 */
	public void setColumnNames() {
		
		columnNames = new String[getColumnCount()];
		
		columnNames[0] = "Anzahl der Klassenelemente";
		
		for (int i = extraColumns; i < cAll.getCountAttributs() + extraColumns; i++) {
			
			columnNames[i] = cAll.getAttributName(i - extraColumns);
		}
		
	}
}

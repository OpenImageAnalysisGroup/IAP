package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.xml_data_tree_table_model;

import javax.swing.tree.TreeModel;

/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/**
 * TreeTableModel is the model used by a JTreeTable. It extends TreeModel to add
 * methods for getting information about the set of columns each node in the
 * TreeTableModel may have. Each column, like a column in a TableModel, has a
 * name and a type associated with it. Each node in the TreeTableModel can
 * return a value for each of the columns and set that value if isCellEditable()
 * returns true.
 * 
 * @version %I% %G%
 * @author Philip Milne
 * @author Scott Violet
 */
public interface TreeTableModel extends TreeModel {
	/**
	 * Returns the number ofs availible column.
	 */
	public int getColumnCount();
	
	/**
	 * Returns the name for column number <code>column</code>.
	 */
	public String getColumnName(int column);
	
	/**
	 * Returns the type for column number <code>column</code>.
	 */
	public Class<?> getColumnClass(int column);
	
	/**
	 * Returns the value to be displayed for node <code>node</code>, at column
	 * number <code>column</code>.
	 */
	public Object getValueAt(Object node, int column);
	
	/**
	 * Indicates whether the the value for node <code>node</code>, at column
	 * number <code>column</code> is editable.
	 */
	public boolean isCellEditable(Object node, int column);
	
	/**
	 * Sets the value for node <code>node</code>, at column number <code>column</code>.
	 */
	public void setValueAt(Object aValue, Object node, int column);
}

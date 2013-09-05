/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 03.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.awt.Color;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.jfree.ui.DateCellRenderer;

public class MyDataJTable extends JTable {
	
	private static final long serialVersionUID = 1L;
	
	public MyDataJTable(XlsTableModel xtm) {
		super(xtm);
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		TableCellRenderer tcr = null;
		
		Object value = getValueAt(row, column);
		if (value instanceof String) {
			tcr = new DefaultTableCellRenderer();
			JComponent res = (JComponent) tcr;
			res.setBackground(new Color(255, 240, 255));
		}
		if (value instanceof Date) {
			tcr = new DateCellRenderer();
			JComponent res = (JComponent) tcr;
			res.setBackground(new Color(240, 255, 255));
		}
		if (value instanceof Double) {
			tcr = new DefaultTableCellRenderer();
			JComponent res = (JComponent) tcr;
			res.setBackground(new Color(255, 255, 240));
		}
		if (value instanceof Integer) {
			tcr = new DefaultTableCellRenderer();
			JComponent res = (JComponent) tcr;
			res.setBackground(new Color(240, 240, 240));
		}
		if (tcr == null)
			tcr = new DefaultTableCellRenderer();
		return tcr;
	}
	
}

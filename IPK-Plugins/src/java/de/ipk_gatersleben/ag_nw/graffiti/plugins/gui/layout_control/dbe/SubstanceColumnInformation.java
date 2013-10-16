/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 07.04.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.ArrayList;

public class SubstanceColumnInformation {
	ArrayList<Integer> columns = new ArrayList<Integer>();
	
	public String getColumnList() {
		String res = "";
		for (Integer col : columns) {
			if (res.length() > 0)
				res = res + "," + col.toString();
			else
				res = col.toString();
		}
		return res;
	}
	
	public Integer[] getColumns() {
		return columns.toArray(new Integer[] {});
	}
	
	public int getFirstColumn() {
		return columns.get(0);
	}
	
	public void addDataColumn(int colSubst) {
		columns.add(colSubst);
	}
	
}

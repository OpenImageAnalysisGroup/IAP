/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 09.11.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.ArrayList;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class MyDynamicObjectListModel implements ListModel {
	
	private ArrayList<Object> items = new ArrayList<Object>();
	ArrayList<ListDataListener> ll = new ArrayList<ListDataListener>();
	
	public int getSize() {
		return items.size();
	}
	
	public Object getElementAt(int index) {
		return items.get(index);
	}
	
	public void addListDataListener(ListDataListener l) {
		ll.add(l);
	}
	
	public void removeListDataListener(ListDataListener l) {
		ll.remove(l);
	}
	
	public void clearItems() {
		items.clear();
		ListDataEvent de = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, 0);
		for (ListDataListener dl : ll) {
			dl.contentsChanged(de);
		}
	}
	
}

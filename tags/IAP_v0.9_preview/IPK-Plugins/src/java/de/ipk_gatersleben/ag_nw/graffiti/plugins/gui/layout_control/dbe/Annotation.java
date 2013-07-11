/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class Annotation {
	
	private String title;
	private int column;
	
	public Annotation(String title, int column) {
		this.title = title;
		this.column = column;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getColumn() {
		return column;
	}
}

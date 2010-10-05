/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Sep 3, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui;

/**
 * @author klukas
 * 
 */
public enum MongoCollection {
	IMAGES("images"), VOLUMES("volumes");

	String col;

	MongoCollection(String col) {
		this.col = col;
	}

	@Override
	public String toString() {
		return col;
	}
}

/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

/**
 * Used for specifying coordinates in the Kegg Graphics element
 * 
 * @author Christian Klukas
 *         (c) 2006 IPK-Gatersleben
 */
public class kgmlNumber {
	private int value;
	
	public kgmlNumber(int value) {
		assert value >= 0;
		if (value == 0)
			value = 1;
		this.value = value;
	}
	
	public static kgmlNumber getNumber(String s) {
		try {
			return new kgmlNumber(Integer.parseInt(s));
		} catch (NumberFormatException nfe) {
			System.out.println("Number format exception: " + s);
			return null;
		} catch (Exception e) {
			System.out.println("NN: " + s);
			return null;
		}
	}
	
	@Override
	public String toString() {
		return value + "";
	}
	
	public int getValue() {
		return value;
	}
}

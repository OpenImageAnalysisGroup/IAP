/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

public class MapNumber {
	
	private String number;
	
	public MapNumber(String number) {
		// assert number!=null && number.length()>=normLength;
		this.number = number;
	}
	
	// public MapNumber(int number) {
	// assert number>=0;
	// this.number = ensureLength(number+"");
	// }
	//
	// private static String ensureLength(String n) {
	// String result = n;
	// while (result.length()<normLength)
	// result = "0"+normLength;
	// return result;
	// }
	
	@Override
	public String toString() {
		return number;
	}
}

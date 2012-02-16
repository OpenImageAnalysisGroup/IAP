/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 24.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

public class IndexAndString {
	
	private int index;
	private String value;
	
	public IndexAndString(int index, String value) {
		this.index = index;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "idx=" + index + ", value=" + value;
	}
	
	public String getValue() {
		return value;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getSplitValue(String divide, int i) {
		try {
			String[] values = value.split(divide);
			return values[i];
		} catch (Exception e) {
			return null;
		}
	}
	
}

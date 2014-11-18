/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 07.06.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors;

import org.graffiti.plugin.editcomponent.ValueEditComponent;

public class LineModeSetting {
	public float a, b;
	private boolean empty;
	public static final float epsilon = 0.00001f;
	
	public LineModeSetting(float a, float b) {
		this.a = a;
		this.b = b;
		empty = false;
	}
	
	public LineModeSetting() {
		empty = true;
	}
	
	public boolean isEmptyValue() {
		return empty;
	}
	
	@Override
	public String toString() {
		if (empty)
			return ValueEditComponent.EMPTY_STRING;
		int ai = (int) a / 5;
		int bi = (int) b / 5;
		if (ai == 0 && bi == 0)
			ai = 1;
		StringBuilder res = new StringBuilder();
		String full = "-"; // "&mdash;";
		String empty = " ";
		int len = 0;
		int maxLen = 5;
		while (len < maxLen) {
			for (int i = 0; i < ai && len < maxLen; i++) {
				len++;
				res.append(full);
			}
			for (int i = 0; i < bi && len < maxLen; i++) {
				len++;
				res.append(empty);
			}
		}
		return "<html>" + res.toString();
	}
	
	public float[] getDashArray() {
		if (Math.abs(a) < epsilon && Math.abs(b) < epsilon)
			return null;
		else
			return new float[] { a, b };
	}
}

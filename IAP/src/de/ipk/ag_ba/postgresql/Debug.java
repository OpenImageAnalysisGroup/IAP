/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.postgresql;

/**
 * @author entzian
 */
public class Debug {

	public static final boolean TEST = false;

	public static final void print(String text) {
		print("Executing this command: ", text);
	}

	public static final void print(String text1, String text2) {
		System.out.println(text1 + text2 + "\n");
	}

	public static final void print(String text1, int zahl1) {
		print(text1, String.valueOf(zahl1));
	}

	public static final void print(String text1, boolean b1) {
		print(text1, String.valueOf(b1));
	}

}

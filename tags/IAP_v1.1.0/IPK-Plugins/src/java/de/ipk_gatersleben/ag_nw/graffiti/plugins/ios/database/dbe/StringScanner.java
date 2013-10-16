/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.06.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe;

import org.ErrorMsg;
import org.HelperClass;
import org.StringManipulationTools;

/**
 * @author klukas
 */
public class StringScanner implements HelperClass {
	
	private String del1, del2, del3;
	private String work;
	
	/**
	 * @param val
	 * @param string
	 * @param string2
	 * @param string3
	 */
	public StringScanner(String input, String del1, String del2, String del3) {
		this.del1 = del1;
		this.del2 = del2;
		this.del3 = del3;
		work = input;
	}
	
	/**
	 * @return
	 */
	public int nextInt() {
		skipBlank();
		int intLen = 1;
		int val = Integer.MAX_VALUE;
		try {
			if (work.startsWith("-"))
				intLen += 1;
			while (true) {
				val = Integer.parseInt(work.substring(0, intLen));
				intLen += 1;
			}
		} catch (Exception e) {
			work = work.substring(intLen - 1);
			return val;
		}
	}
	
	/**
	 * Returns the next String value, must be enclosed by a given start and end character
	 * e.g. " for the input "test" will result in _test_ the " will be removed from the input.
	 * 
	 * @param startEndCharacter
	 * @return
	 */
	public String nextString(String startEndCharacter) {
		skipBlank();
		if (work.startsWith(startEndCharacter)) {
			work = work.substring(startEndCharacter.length());
			int len = work.indexOf(startEndCharacter);
			if (len > 0) {
				String res = work.substring(0, len);
				work = work.substring(len + startEndCharacter.length());
				return res;
			} else {
				String res = work;
				work = "";
				return res;
			}
		} else
			return null;
	}
	
	/**
	 * @return
	 */
	public boolean stillInputAvailable() {
		if (work == null || work.length() <= 0)
			return false;
		skipBlank();
		if (work == null || work.length() <= 0)
			return false;
		return true;
	}
	
	/**
	 * @return
	 */
	public double nextDouble() {
		skipBlank();
		if (work == null || work.length() <= 0)
			return Double.NaN;
		int len = 0;
		String workSearch = work;
		while (workSearch.startsWith("0") ||
							workSearch.startsWith("1") ||
							workSearch.startsWith("2") ||
							workSearch.startsWith("3") ||
							workSearch.startsWith("4") ||
							workSearch.startsWith("5") ||
							workSearch.startsWith("6") ||
							workSearch.startsWith("7") ||
							workSearch.startsWith("8") ||
							workSearch.startsWith("9") ||
							workSearch.startsWith(".") ||
							workSearch.startsWith(",") ||
							workSearch.startsWith("e") ||
							workSearch.startsWith("E") ||
							workSearch.startsWith("+") ||
							workSearch.startsWith("-")) {
			workSearch = workSearch.substring(1);
			len += 1;
		}
		if (len <= 0)
			return Double.NaN;
		try {
			String s = work.substring(0, len);
			if (s.indexOf(",") > 0) {
				s = StringManipulationTools.stringReplace(s, ",", ".");
			}
			double res = Double.parseDouble(s);
			work = work.substring(len);
			return res;
		} catch (NumberFormatException nfe) {
			ErrorMsg.addErrorMessage(nfe);
			return Double.NaN;
		}
	}
	
	/**
	 * 
	 */
	private void skipBlank() {
		if (del1.length() > 0 || del2.length() > 0 || del3.length() > 0)
			while ((del1.length() > 0 && work.startsWith(del1))
								|| (del2.length() > 0 && work.startsWith(del2))
								|| (del3.length() > 0 && work.startsWith(del3))) {
				if (del1.length() > 0)
					if (work.startsWith(del1))
						work = work.substring(del1.length());
				if (del2.length() > 0)
					if (work.startsWith(del2))
						work = work.substring(del2.length());
				if (del3.length() > 0)
					if (work.startsWith(del3))
						work = work.substring(del3.length());
			}
	}
	
	public boolean contains(String string) {
		return work.contains(string);
	}
	
	public String nextNotQuotedString() {
		work = "§" + StringManipulationTools.stringReplace(work, " ", "§");
		String result = nextString("§");
		work = StringManipulationTools.stringReplace(work, "§", " ");
		return result;
	}
}

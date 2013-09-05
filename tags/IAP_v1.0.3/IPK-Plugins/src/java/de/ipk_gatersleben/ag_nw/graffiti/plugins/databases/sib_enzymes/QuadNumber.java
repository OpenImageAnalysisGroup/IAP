/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes;

public class QuadNumber {
	String number1, number2, number3, number4;
	int number1int, number2int, number3int, number4int;
	String restOfLine = null;
	
	public QuadNumber(String number1, String number2, String number3, String number4) {
		this.number1 = number1;
		this.number2 = number2;
		this.number3 = number3;
		this.number4 = number4;
		updateNumberInts();
	}
	
	private void updateNumberInts() {
		if (number1.equals("-")) {
			number1int = Integer.MIN_VALUE;
		} else {
			try {
				number1int = Integer.parseInt(number1);
			} catch (NumberFormatException e) {
				number1int = Integer.MAX_VALUE;
			}
		}
		if (number2.equals("-")) {
			number2int = Integer.MIN_VALUE;
		} else {
			try {
				number2int = Integer.parseInt(number2);
			} catch (NumberFormatException e) {
				number2int = Integer.MAX_VALUE;
			}
		}
		if (number3.equals("-")) {
			number3int = Integer.MIN_VALUE;
		} else {
			try {
				number3int = Integer.parseInt(number3);
			} catch (NumberFormatException e) {
				number3int = Integer.MAX_VALUE;
			}
		}
		if (number4.equals("-")) {
			number4int = Integer.MIN_VALUE;
		} else {
			try {
				number4int = Integer.parseInt(number4);
			} catch (NumberFormatException e) {
				number4int = Integer.MAX_VALUE;
			}
		}
	}
	
	@Override
	public String toString() {
		return number1 + "." + number2 + "." + number3 + "." + number4;
	}
	
	/**
	 * @param line
	 *           A complete ec number name like a.b.c.d
	 */
	public QuadNumber(String line) throws StringIndexOutOfBoundsException {
		String testString = line.replaceAll("\\.", ""); // ErrorMsg.stringReplace(line, ".", "");
		if (line.length() - testString.length() == 2) {
			// EC:2.1.4 --> EC:2.1.4.-
			line = line + ".-";
			testString = line.replaceAll("\\.", ""); // ErrorMsg.stringReplace(line, ".", "");
		}
		if (line.length() - testString.length() != 3) {
			number1 = "";
			number2 = "";
			number3 = "";
			number4 = "";
			updateNumberInts();
		} else {
			if (line.toUpperCase().startsWith("EC:"))
				line = line.substring("ec:".length());
			if (line.toUpperCase().startsWith("EC"))
				line = line.substring("ec".length());
			String number1 = line.substring(0, line.indexOf("."));
			line = line.substring(number1.length() + 1);
			String number2 = line.substring(0, line.indexOf("."));
			line = line.substring(number2.length() + 1);
			String number3 = line.substring(0, line.indexOf("."));
			line = line.substring(number3.length() + 1);
			String number4;
			if (line.indexOf("  ") <= 0) {
				number4 = line;
				line = "";
			} else {
				number4 = line.substring(0, line.indexOf("  "));
				line = line.substring(number4.length() + 1).trim();
			}
			this.number1 = number1.trim();
			this.number2 = number2.trim();
			this.number3 = number3.trim();
			this.number4 = number4.trim();
			updateNumberInts();
		}
		this.restOfLine = line;
	}
	
	public boolean isValidQuadNumber() {
		return (number1int != Integer.MAX_VALUE &&
							number2int != Integer.MAX_VALUE &&
							number3int != Integer.MAX_VALUE && number4int != Integer.MAX_VALUE);
	}
	
	public boolean isValidMatchFor(QuadNumber quadNumber) {
		return isValidNumberMatch(number1, quadNumber.number1) &&
							isValidNumberMatch(number2, quadNumber.number2) &&
							isValidNumberMatch(number3, quadNumber.number3) &&
							isValidNumberMatch(number4, quadNumber.number4);
	}
	
	private boolean isValidNumberMatch(String numberB, String numberA) {
		if (numberA.equals("-"))
			return true;
		else
			return numberA.equals(numberB);
	}
	
	public boolean isValidMatchFor_Inversed(QuadNumber quadNumber) {
		return isValidNumberMatch_Inversed(number1, quadNumber.number1) &&
							isValidNumberMatch_Inversed(number2, quadNumber.number2) &&
							isValidNumberMatch_Inversed(number3, quadNumber.number3) &&
							isValidNumberMatch_Inversed(number4, quadNumber.number4);
	}
	
	private boolean isValidNumberMatch_Inversed(String numberA, String numberB) {
		if (numberA.equals("-"))
			return true;
		else
			if (numberB.equals("-"))
				return true;
			else
				return numberA.equals(numberB);
	}
}
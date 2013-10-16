/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes;

/**
 * @author klukas
 */
public class EnzClassEntry {
	QuadNumber quadNumber;
	String description;
	
	@Override
	public String toString() {
		return quadNumber.toString() + " " + description;
	}
	
	public EnzClassEntry(EnzClassEntry template) {
		description = template.description;
		quadNumber = template.quadNumber;
	}
	
	/**
	 * @param number12
	 * @param number22
	 * @param number32
	 * @param number42
	 * @param description2
	 */
	public EnzClassEntry(String number1, String number2,
						String number3, String number4, String description) {
		quadNumber = new QuadNumber(number1, number2, number3, number4);
		this.description = description;
	}
	
	/**
	 * @param line
	 *           A enzyme class entry, e.g.
	 *           "1. 1. 5.-    weiterer Text."
	 *           The format is anum.bnum.cnum.dnum[space]description
	 *           each number is either a "-" or a number. Spaces are removed for this part.
	 *           The numbers are divided with the description by two or more spaces.
	 *           The last point (which must be existent!) is removed autmatically be this constructor.
	 */
	public static EnzClassEntry getEnzClassEntry(String line) {
		try {
			// remove last point
			if (line.endsWith("."))
				line = line.substring(0, line.length() - 1);
			QuadNumber qn = new QuadNumber(line);
			line = qn.restOfLine;
			String description = line;
			
			if (qn.isValidQuadNumber())
				return new EnzClassEntry(qn.number1, qn.number2, qn.number3, qn.number4, description);
			else
				return null;
		} catch (StringIndexOutOfBoundsException sie) {
			return null;
		}
	}
	
	/**
	 * Determines if a given line is a valid start for a enzyme class entry.
	 * Example for a valid entry: " 6. 3. 1.-"
	 * 
	 * @param line
	 * @return True, if the line (and possibily following lines) should be treated
	 *         as a enzyme entry. Fals, if this is a comment or anything other.
	 */
	public static boolean isValidEnzymeStart(String line) {
		boolean result = false;
		line = line.trim();
		if (line.length() > 2 && line.contains(".")) {
			String startUntilPoint = line.substring(0, line.indexOf("."));
			try {
				Integer.parseInt(startUntilPoint);
				result = true;
			} catch (NumberFormatException nfe) {
				// line does not start with a number
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * If a entry is the same as the given, true is returned.
	 * The entry may contain "-", but then the entry must be same
	 * for this number. The other way around, if the object contains
	 * some "-" any number can be given for this entry, the match
	 * will be valid. Object="1.2.-.-" => ec_number "1.2.3.4" will match.
	 * Reversed this is not true.
	 * 
	 * @param ec_number
	 * @return True, if the given ec_number is a valid match
	 *         for the category, this object instance represents.
	 */
	public boolean isValidMatchFor(String ec_number) {
		if (!quadNumber.isValidQuadNumber())
			return false;
		try {
			QuadNumber testNumber = new QuadNumber(ec_number);
			if (!testNumber.isValidQuadNumber())
				return false;
			else {
				return testNumber.isValidMatchFor(quadNumber);
			}
		} catch (StringIndexOutOfBoundsException e) {
			// ec_number is not a valid quadnumber
			return false;
		}
	}
	
	/**
	 * @param id
	 * @return
	 */
	public boolean isValidMatchFor_Inversed(String ec_number) {
		if (!quadNumber.isValidQuadNumber())
			return false;
		try {
			QuadNumber testNumber = new QuadNumber(ec_number);
			if (!testNumber.isValidQuadNumber())
				return false;
			else {
				return testNumber.isValidMatchFor_Inversed(quadNumber);
			}
		} catch (StringIndexOutOfBoundsException e) {
			// ec_number is not a valid quadnumber
			return false;
		}
	}
	
	public boolean isValidMatchFor_Inversed(QuadNumber testNumber) {
		if (!quadNumber.isValidQuadNumber())
			return false;
		try {
			if (testNumber == null || !testNumber.isValidQuadNumber())
				return false;
			else {
				return testNumber.isValidMatchFor_Inversed(quadNumber);
			}
		} catch (StringIndexOutOfBoundsException e) {
			// ec_number is not a valid quadnumber
			return false;
		}
	}
	
}
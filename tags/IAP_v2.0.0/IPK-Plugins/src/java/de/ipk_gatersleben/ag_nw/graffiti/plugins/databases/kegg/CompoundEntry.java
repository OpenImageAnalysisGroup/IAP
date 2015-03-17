/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.10.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg;

import java.util.ArrayList;
import java.util.List;

import org.ErrorMsg;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class CompoundEntry {
	private String entryID = null;
	private List<String> name = new ArrayList<String>();
	private final List<String> formula = new ArrayList<String>();
	private String mass = null;
	private static boolean readMass = true;
	
	// _exists tags are by definition always existant for an entry
	private static final String entryTag_exists = "ENTRY";
	private static final String nameTag_exists = "NAME";
	private static final String formulaTag = "FORMULA";
	private static final String massTag = "MASS";
	public static final String endTag_exists = "///";
	
	public CompoundEntry() {
		//
	}
	
	public CompoundEntry(String entryID, List<String> name) {
		this.entryID = entryID;
		this.name = name;
	}
	
	public static boolean isValidCompoundStart(String line) {
		boolean res = (line != null && line.startsWith(entryTag_exists));
		return res;
	}
	
	@Override
	public String toString() {
		return entryID + " (" + name.size() + ")";
	}
	
	public void processInputLine(String line) {
		if (line == null || line.length() < 3)
			return;
		
		if (line.startsWith(entryTag_exists)) {
			line = line.substring(entryTag_exists.length()).trim();
			if (line.indexOf(" ") > 0)
				line = line.substring(0, line.indexOf(" "));
			entryID = line;
		} else
			if (line.startsWith(formulaTag))
				formula.add(line.substring(formulaTag.length()).trim());
			else
				if (readMass && line.startsWith(massTag)) {
					mass = line.substring(massTag.length()).trim();
				} else
					if (line.startsWith(nameTag_exists)) {
						// System.out.println(line);
						String newName = line.substring(nameTag_exists.length()).trim();
						if (newName.endsWith(";"))
							newName = newName.substring(0, newName.length() - 1);
						if (!name.contains(newName))
							name.add(newName);
						String greekName = getGreekName(newName);
						if (!greekName.equals(newName) && !name.contains(greekName))
							name.add(greekName);
					}
		// all other types are ignored
	}
	
	public static String getGreekName(String newName) {
		return newName;
		/*
		 * String result = newName;
		 * result = result.replaceAll("alpha-", "&#945;-");
		 * result = result.replaceAll("beta-", "&#946;-");
		 * result = result.replaceAll("gamma-", "&#947;-");
		 * result = result.replaceAll("delta-", "&#948;-");
		 * if (!result.startsWith("<html>") && !result.equals(newName))
		 * return "<html>"+result;
		 * else
		 * return result;
		 */
	}
	
	public boolean isValid() {
		return entryID != null && name.size() > 0;
	}
	
	/**
	 * @return Identification (Compound ID)
	 */
	public String getID() {
		return entryID;
	}
	
	public String getFormula() {
		String result = "";
		for (String s : formula) {
			if (result.length() > 0)
				result = result + "§" + s;
			else
				result = s;
		}
		return result.replace("\"", "");
	}
	
	/**
	 * @return Description (Official name)
	 */
	public List<String> getNames() {
		return name;
	}
	
	/**
	 * Returns the mass of the compound, if compound - reading has been enabled
	 * in the source code. If compound reading is not supported (disabled),
	 * this method returns <code>null</code> and logs an error message.
	 * 
	 * @return The mass of the compound.
	 */
	public String getMass() {
		if (!readMass) {
			ErrorMsg.addErrorMessage("Processing of compound mass is disabled in the source code.");
			return null;
		}
		return mass;
	}
}

/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
/*
 * Created on Aug 10, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import java.util.ArrayList;

import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;

/**
 * Example use case and unit test, see MyScannerTest.
 * 
 * @author klukas
 */
public class FileNameScanner {
	
	private int replicateID;
	private String species, genotype, variety, treatment;
	private int dateYear;
	private int dateMonth;
	private int dateDay;
	private String substance;
	private double rotation;
	private String fileName;
	private Condition conditionTemplate;
	
	/**
	 * needed by JUnit, don't use this constructor
	 */
	public FileNameScanner() {
	}
	
	public void setSubstance(String substance) {
		this.substance = substance;
	}
	
	public void setCondition(String condition) {
		this.genotype = condition;
	}
	
	/**
	 * G = genotype, R = replicate ID, X = ignore, A = rotation (degree), D =
	 * date (yyyy-mm-dd), 'some string' = some string (ignored, but may be used
	 * to divide strings), S = substance, V = variety, P = species, T = treatment
	 * Examples: "R_D X_X_X_X_S_S_A'Grad'", "G_X_R_S_A_X_X_D_X", "G_X_R_S_S_D_X"
	 * 
	 * @param fn
	 * @param string
	 * @throws Exception
	 */
	public FileNameScanner(String type, String fn) throws Exception {
		
		this.fileName = fn;
		
		// private int replicateID;
		// private String condition;
		// private int dateYear;
		// private int dateMonth;
		// private int dateDay;
		// private String substance;
		// private double rotation;
		
		ArrayList<String> res = StringManipulationTools.removeTagsGetTextAndRemovedTexts(type, "'", "'");
		res.remove(0);
		for (String ignore : res) {
			type = StringManipulationTools.stringReplace(type, "'" + ignore + "'", "_");
			fn = StringManipulationTools.stringReplace(fn, ignore, "_");
		}
		if (fn.endsWith(".jpg"))
			fn = fn.substring(0, fn.length() - ".jpg".length());
		if (fn.endsWith(".png"))
			fn = fn.substring(0, fn.length() - ".png".length());
		if (fn.endsWith(".tif"))
			fn = fn.substring(0, fn.length() - ".tif".length());
		
		String remaining = fn;
		char[] types = (type + "#").toCharArray();
		for (int iii = 0; iii < types.length;) {
			char currType = types[iii++];
			char nextType = types[iii++];
			String input;
			if (currType == '_' && remaining.startsWith("_")) {
				remaining = remaining.substring(1);
				input = scan(remaining, nextType);
			} else {
				input = scan(remaining, nextType);
				try {
					remaining = remaining.substring(input.length() + 1);
				} catch (StringIndexOutOfBoundsException err) {
					remaining = "";
				}
			}
			if (currType == 'R') {
				replicateID = Integer.parseInt(StringManipulationTools.getNumbersFromString(input));
			}
			if (currType == 'G') {
				genotype = input;
			}
			if (currType == 'P') {
				species = input;
			}
			if (currType == 'V') {
				variety = input;
			}
			if (currType == 'T') {
				treatment = input;
			}
			if (currType == 'S') {
				if (substance == null)
					substance = input;
				else
					substance += input;
			}
			if (currType == 'A') {
				double degree = Integer.parseInt(input);
				rotation = degree;// / 180 * Math.PI;
			}
			if (currType == 'D') {
				String[] parts = input.split("-");
				dateYear = Integer.parseInt(parts[0]);
				dateMonth = Integer.parseInt(parts[1]);
				dateDay = Integer.parseInt(parts[2]);
			}
			if (currType == 'X') {
				// ignore
			}
			if (nextType == '#' || remaining.isEmpty())
				break;
		}
		
		boolean b = false;
		if (b)
			throw new Exception("Could not parse " + fn + " (" + type + ")");
	}
	
	private String scan(String remaining, char stop) {
		StringBuilder res = new StringBuilder();
		for (char c : remaining.toCharArray()) {
			if (c == stop)
				break;
			res.append(c);
		}
		return res.toString();
	}
	
	public int getReplicateID() {
		return replicateID;
	}
	
	public String getGenotype() {
		return genotype;
	}
	
	public String getVariety() {
		return variety;
	}
	
	public int getDateYear() {
		return dateYear;
	}
	
	public int getDateMonth() {
		return dateMonth;
	}
	
	public int getDateDay() {
		return dateDay;
	}
	
	public String getSubstance() {
		return substance;
	}
	
	public double getRotation() {
		return rotation;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setConditionTemplate(Condition c) {
		this.conditionTemplate = c;
	}
	
	public Condition getConditionTemplate() {
		return conditionTemplate;
	}
	
	public String getSpecies() {
		return species;
	}
	
	public void setSpecies(String species) {
		this.species = species;
	}
	
	public String getTreatment() {
		return treatment;
	}
	
	public void setTreatment(String treatment) {
		this.treatment = treatment;
	}
}

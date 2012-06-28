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
public class MyScanner {
	
	private int replicateID;
	private String condition;
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
	public MyScanner() {
	}
	
	public void setSubstance(String substance) {
		this.substance = substance;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	/**
	 * G = genotype, R = replicate ID, X = ignore, A = rotation (degree), D =
	 * date (yyyy-mm-dd), 'some string' = some string (ignored, but may be used
	 * to divide strings)
	 * Examples: "R_D X_X_X_X_S_S_A'Grad'", "G_X_R_S_A_X_X_D_X", "G_X_R_S_S_D_X"
	 * 
	 * @param fn
	 * @param string
	 * @throws Exception
	 */
	public MyScanner(String type, String fn) throws Exception {
		
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
		
		char lastChar = 0;
		String remaining = fn;
		for (char c : type.toCharArray()) {
			if (lastChar > 0) {
				String input = scan(remaining, c);
				try {
					remaining = remaining.substring(input.length() + 1);
				} catch (StringIndexOutOfBoundsException err) {
					System.out.println("check: " + remaining + " // " + type);
					break;
				}
				if (lastChar == 'R') {
					replicateID = Integer.parseInt(input);
				}
				if (lastChar == 'G') {
					condition = input;
				}
				if (lastChar == 'S') {
					if (substance == null)
						substance = input;
					else
						substance += input;
				}
				if (lastChar == 'A') {
					double degree = Integer.parseInt(input);
					rotation = degree;// / 180 * Math.PI;
				}
				if (lastChar == 'D') {
					String[] parts = input.split("-");
					dateYear = Integer.parseInt(parts[0]);
					dateMonth = Integer.parseInt(parts[1]);
					dateDay = Integer.parseInt(parts[2]);
				}
				if (lastChar == 'X') {
					// ignore
				}
				c = 0;
			}
			lastChar = c;
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
	
	public String getCondition() {
		return condition;
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
}

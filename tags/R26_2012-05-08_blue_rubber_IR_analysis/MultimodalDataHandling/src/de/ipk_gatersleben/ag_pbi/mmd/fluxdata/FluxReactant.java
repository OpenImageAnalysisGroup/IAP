package de.ipk_gatersleben.ag_pbi.mmd.fluxdata;

import org.ErrorMsg;
import org.StringManipulationTools;

public class FluxReactant {
	
	private double coeff = 1d;
	private String name;
	private boolean isCorrect = true;
	
	public FluxReactant(String reactant) {
		reactant = reactant.trim();
		if (isCoefficientSpecified(reactant)) {
			String coeffstr = getCoefficient(reactant);
			try {
				coeff = Double.parseDouble(coeffstr);
			} catch (Exception e) {
				try {
					coeffstr = StringManipulationTools.stringReplace(coeffstr, ",", ".");
					coeff = Double.parseDouble(coeffstr);
				} catch (Exception e2) {
					ErrorMsg.addErrorMessage("<html>Could not parse stoichiometric coefficient of \"" + reactant + "\"!<br>" +
							"Was \" + \" used to separate the reactants?");
					isCorrect = false;
				}
			}
			name = reactant.substring(coeffstr.length()).trim();
		} else {
			coeff = 1d;
			name = reactant.trim();
		}
	}
	
	private boolean isCoefficientSpecified(String reactant) {
		// String ".*[0-9]{1} [0-9]*[A-Z]+.*"
		// String startsWithNumberPattern = "[0-9]+.*";
		// String startsWithDotPattern = "\\.[0-9]+.*";
		return reactant.indexOf(" ") > 0;
	}
	
	private String getCoefficient(String reactant) {
		return reactant.substring(0, reactant.indexOf(" "));
	}
	
	public double getCoeff() {
		return coeff;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isCorrect() {
		return isCorrect;
	}
	
	/**
	 * add Fluxreactants with the same name only!
	 */
	public void add(FluxReactant otherReactant) {
		if (getName().equals(otherReactant.getName()))
			coeff += otherReactant.getCoeff();
		else
			ErrorMsg.addErrorMessage("Merging of two Fluxreactants with different names (" + getName() + "," + otherReactant.getName() + ") was ignored");
		
	}
	
}
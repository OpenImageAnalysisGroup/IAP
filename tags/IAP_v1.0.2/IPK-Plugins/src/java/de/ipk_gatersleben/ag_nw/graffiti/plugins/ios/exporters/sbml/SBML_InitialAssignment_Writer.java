/**
 * This class set the InitialAssignment attributes
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.text.parser.ParseException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLInitialAssignment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLInitialAssignmentHelper;

public class SBML_InitialAssignment_Writer extends SBML_SBase_Writer {
	
	/**
	 * Adds an initial assignment and its variables to the model
	 * 
	 * @param g contains the values for the export
	 * @param model the initial assignments will be added to this model
	 * @param headline indicates where the information should be read from
	 * @param internHeadline intern representation of headline
	 */
	public void addInitialAssignment(Graph g, Model model, String internHeadline, SBMLInitialAssignmentHelper iaHelperObject) {
		InitialAssignment initialAssignment = model.createInitialAssignment();
		SBMLInitialAssignment iaHelper = iaHelperObject.addInitialAssignment(g, internHeadline);
		addSBaseAttributes(initialAssignment, g, internHeadline);
		if (iaHelper.isSetSymbol()) {
			initialAssignment.setVariable(iaHelper.getSymbol());
		}
		if (iaHelper.isSetFunction()) {
			try {
				initialAssignment.setMath(ASTNode.parseFormula(iaHelper.getFunction()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
/**
 * This class sets the Constraint attributes
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLConstraint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLConstraintHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Constraint_Writer extends SBML_SBase_Writer {
	
	/**
	 * Adds a constraint and its variables to the model
	 * 
	 * @param g
	 *        contains the values for the export
	 * @param model
	 *        the constraint will be added to this model
	 * @param headline
	 *        indicates where the information should be read from
	 * @param niceID
	 *        intern representation of headline
	 */
	public void addConstraint(Graph g, Model model, String internHeadline,
			SBMLConstraintHelper constraintHelperObject) {
		SBMLConstraint constraintHelper = constraintHelperObject.addConstraint(
				g, internHeadline);
		Constraint constraint = model.createConstraint();
		addSBaseAttributes(constraint, g, internHeadline);
		if (constraintHelper.isSetMessage()) {
			String message = constraintHelper.getMessage();
			if (null != message) {
				message = "<message><body xmlns=\"http://www.w3.org/1999/xhtml\"><p>"
						+ message + "</p></body></message>";
				XMLNode xmlMessage = new XMLNode(message);
				constraint.setMessage(message);
			}
		}
		if (constraintHelper.isSetFunction()) {
			String constraintFormula = constraintHelper.getFunction();
			if (!SBML_Constants.EMPTY.equals(constraintFormula)) {
				try {
					constraint.setMath(ASTNode.parseFormula(constraintFormula));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
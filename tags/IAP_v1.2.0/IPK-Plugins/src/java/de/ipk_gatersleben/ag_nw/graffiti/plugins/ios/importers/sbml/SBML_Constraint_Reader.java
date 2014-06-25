/**
 * This class reads in Constraints
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.Iterator;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.ListOf;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLConstraint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLConstraintHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Constraint_Reader extends SBML_SBase_Reader {
	
	/**
	 * Method reads in constraints and is called from class SBML_XML_Reader.java
	 * 
	 * @param listOfConstraints
	 *        contains the constraints for the import
	 * @param g
	 *        the data structure for reading in the information
	 */
	public void addConstraint(ListOf<Constraint> listOfConstraints, Graph g) {
		int constraintCount = 1;
		SBMLConstraintHelper constraintHelperObject = new SBMLConstraintHelper();
		Iterator<Constraint> itConstraint = listOfConstraints.iterator();
		while (itConstraint.hasNext()) {
			Constraint constraint = itConstraint.next();
			
			String internHeadline = new StringBuffer(
					SBML_Constants.SBML_CONSTRAINT).append(constraintCount)
					.toString();
			String presentedHeadline = new StringBuffer("SBML Constraint ")
					.append(constraintCount).toString();
			// initConstraintNideIDs(internHeadline, presentedHeadline);
			SBMLConstraint constraintHelper = constraintHelperObject
					.addConstraint(g, internHeadline, presentedHeadline);
			
			String metaID = constraint.getMetaId();
			String sboTerm = constraint.getSBOTermID();
			String mathFormula = "";
			if (constraint.isSetMath()) {
				if (null != constraint.getMath()) {
					mathFormula = constraint.getMath().toString();
				}
			}
			String message = "";
			if (constraint.isSetMessage()) {
				message = removeTagFromString(constraint.getMessageString());
				constraintHelper.setMessage(message);
			}
			if (constraint.isSetSBOTerm()) {
				constraintHelper.setSBOTerm(sboTerm);
			}
			if (constraint.isSetMetaId()) {
				constraintHelper.setMetaId(metaID);
			}
			if (constraint.isSetNotes()) {
				constraintHelper.setNotes(constraint.getNotesString(),
						constraint.getNotes());
			}
			if (constraint.isSetAnnotation()) {
				if (constraint.getAnnotation().isSetRDFannotation()) {
					constraintHelper.setAnnotation(constraint.getAnnotation());
				}
				if (constraint.getAnnotation().isSetNonRDFannotation()) {
					constraintHelper.setNonRDFAnnotation(constraint
							.getAnnotation().getNonRDFannotation());
				}
			}
			if (constraint.isSetMath()) {
				constraintHelper.setFunction(mathFormula);
			}
			
			constraintCount++;
		}
	}
	
}
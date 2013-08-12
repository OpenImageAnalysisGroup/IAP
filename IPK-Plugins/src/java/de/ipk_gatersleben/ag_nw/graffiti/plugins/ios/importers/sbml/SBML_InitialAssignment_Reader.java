/**
 * This class reads in InitialAssignments
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.Iterator;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.SBMLException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLInitialAssignment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLInitialAssignmentHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_InitialAssignment_Reader {
	
	/**
	 * Method reads in initial assignments and is called from class
	 * SBML_XML_Reader.java
	 * 
	 * @param initialAssignmentList
	 *        contains the initial assignments for the import
	 * @param g
	 *        the data structure for reading in the information
	 */
	public void addInitialAssignments(
			ListOf<InitialAssignment> initialAssignmentList, Graph g) {
		Iterator<InitialAssignment> itInitialAssignment = initialAssignmentList
				.iterator();
		int countInitialAssignment = 1;
		SBMLInitialAssignmentHelper initialAssignmentHelperObject = new SBMLInitialAssignmentHelper();
		while (itInitialAssignment.hasNext()) {
			InitialAssignment initialAssignment = itInitialAssignment.next();
			
			String internHeadline = new StringBuffer(
					SBML_Constants.SBML_INITIAL_ASSIGNMENT).append(
					countInitialAssignment).toString();
			String presentedHeadline = new StringBuffer(
					"SBML Initial Assignment ").append(countInitialAssignment)
					.toString();
			SBMLInitialAssignment initialAssignmentHelper = initialAssignmentHelperObject
					.addInitialAssignment(g, internHeadline, presentedHeadline);
			// initInitialAssignmentNideIDs(internHeadline, presentedHeadline);
			
			/*
			 * String presentedHeadline = "SBML Initial Assignment " +
			 * countInitialAssignment; String internHeadline =
			 * getNiceHeadline(presentedHeadline);
			 * SBML_Constants.put(internHeadline, presentedHeadline);
			 */
			
			/*
			 * String keySymbol =
			 * SBML_Constants.addToNiceIdList(presentedHeadline, "Symbol");
			 * String keyMetaId =
			 * SBML_Constants.addToNiceIdList(presentedHeadline, "Meta ID");
			 * String keySBOTerm =
			 * SBML_Constants.addToNiceIdList(presentedHeadline, "SBOTerm");
			 * String keyToolTip =
			 * SBML_Constants.addToNiceIdList(presentedHeadline, "ToolTip");
			 * String keyFunction =
			 * SBML_Constants.addToNiceIdList(presentedHeadline, "Function");
			 */
			
			String formula = "";
			try {
				if (initialAssignment.isSetMath()) {
					ASTNode mathTree = initialAssignment.getMath();
					formula = mathTree.toFormula();
				}
			} catch (SBMLException e) {
			}
			// addNotes(initialAssignment.getNotes(),
			// initialAssignment.getNotesString(),g, internHeadline, new
			// StringBuffer(internHeadline).append(SBML_Constants.NOTES).toString());
			String sboTerm = initialAssignment.getSBOTermID();
			String metaID = initialAssignment.getMetaId();
			// method getSymbol is for compatibility with libSBML only -
			// deprecated
			String symbol = initialAssignment.getVariable();
			
			if (initialAssignment.isSetSBOTerm()) {
				initialAssignmentHelper.setSBOTerm(sboTerm);
			}
			if (initialAssignment.isSetMetaId()) {
				initialAssignmentHelper.setMetaID(metaID);
			}
			if (initialAssignment.isSetSymbol()) {
				initialAssignmentHelper.setSymbol(symbol);
			}
			if (initialAssignment.isSetMath()) {
				initialAssignmentHelper.setFunction(formula);
			}
			if (initialAssignment.isSetNotes()) {
				initialAssignmentHelper.setNotes(
						initialAssignment.getNotesString(),
						initialAssignment.getNotes());
			}
			if (initialAssignment.isSetAnnotation()) {
				if (initialAssignment.getAnnotation().isSetRDFannotation()) {
					initialAssignmentHelper.setAnnotation(initialAssignment
							.getAnnotation());
				}
				if (initialAssignment.getAnnotation().isSetNonRDFannotation()) {
					initialAssignmentHelper
							.setNonRDFAnnotation(initialAssignment
									.getAnnotation().getNonRDFannotation());
				}
			}
			
			countInitialAssignment++;
		}
	}
	
}
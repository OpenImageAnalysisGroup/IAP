/**
 * This class reads in Rules
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.Iterator;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Rule;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLAlgebraicRule;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLAssignmentRule;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLRateRule;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLRuleHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Rule_Reader {
	
	/**
	 * Method reads in rules and is called from class SBML_XML_Reader.java
	 * 
	 * @param listOfRules
	 *        contains the rules for the import
	 * @param g
	 *        the data structure for reading in the information
	 */
	public void addRule(ListOf<Rule> listOfRules, Graph g) {
		Iterator<Rule> itRule = listOfRules.iterator();
		int rateCount = 1;
		int assignmentCount = 1;
		int algebraicCount = 1;
		SBMLRuleHelper ruleHelperObject = new SBMLRuleHelper();
		while (itRule.hasNext()) {
			Rule rule = itRule.next();
			String metaID = rule.getMetaId();
			String sboTerm = rule.getSBOTermID();
			
			String math = "";
			try {
				if (rule.isSetMath()) {
					if (null != rule.getMath()) {
						math = rule.getMath().toFormula();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String variable = "";
			if (rule.isAssignment()) {
				AssignmentRule assignment = (AssignmentRule) rule;
				
				String internHeadline = new StringBuffer(
						SBML_Constants.SBML_ASSIGNMENT_RULE).append(
						assignmentCount).toString();
				String presentedHeadline = new StringBuffer(
						"SBML Assignment Rule ").append(assignmentCount)
						.toString();
				SBMLAssignmentRule assignmentRuleHelper = ruleHelperObject
						.addAssignmentRule(g, internHeadline, presentedHeadline);
				// initAssignmnetNiceIDs(internHeadline, presentedHeadline);
				
				/*
				 * String presentedHeadline = "SBML Assignment Rule " +
				 * assignmentCount; String internHeadline =
				 * getNiceHeadline(presentedHeadline);
				 * SBML_Constants.put(internHeadline, presentedHeadline);
				 */
				
				/*
				 * String keyVariable =
				 * SBML_Constants.addToNiceIdList(presentedHeadline,
				 * "Variable"); String keyFormula =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "Formula");
				 * String keyMetaId =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "Meta ID");
				 * String keySBOTerm =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "SBOTerm");
				 * String keyToolTip =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "ToolTip");
				 */
				
				variable = assignment.getVariable();
				if (assignment.isSetMetaId()) {
					assignmentRuleHelper.setMetaID(metaID);
				}
				if (assignment.isSetSBOTerm()) {
					assignmentRuleHelper.setSBOTerm(sboTerm);
				}
				if (assignment.isSetMath()) {
					assignmentRuleHelper.setFunction(math);
				}
				if (assignment.isSetVariable()) {
					assignmentRuleHelper.setVariable(variable);
				}
				if (assignment.isSetNotes()) {
					assignmentRuleHelper.setNotes(rule.getNotesString(),
							rule.getNotes());
				}
				if (assignment.isSetAnnotation()) {
					if (assignment.getAnnotation().isSetRDFannotation()) {
						assignmentRuleHelper.setAnnotation(assignment
								.getAnnotation());
					}
					if (assignment.getAnnotation().isSetNonRDFannotation()) {
						assignmentRuleHelper.setNonRDFAnnotation(assignment
								.getAnnotation().getNonRDFannotation());
					}
				}
				
				assignmentCount++;
			}
			if (rule.isAlgebraic()) {
				AlgebraicRule algebraic = (AlgebraicRule) rule;
				
				String internHeadline = new StringBuffer(
						SBML_Constants.SBML_ALGEBRAIC_RULE).append(
						algebraicCount).toString();
				String presentedHeadline = new StringBuffer(
						"SBML Algebraic Rule ").append(algebraicCount)
						.toString();
				
				SBMLAlgebraicRule algebraicRuleHelper = ruleHelperObject
						.addAlgebraicRule(g, internHeadline, presentedHeadline);
				// initAlgebraicNiceIDs(internHeadline, presentedHeadline);
				
				/*
				 * String presentedHeadline = "SBML Algebraic Rule " +
				 * algebraicCount; String internHeadline =
				 * getNiceHeadline(presentedHeadline);
				 * SBML_Constants.put(internHeadline, presentedHeadline);
				 * String keyFormula =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "Formula");
				 * String keyMetaId =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "Meta ID");
				 * String keySBOTerm =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "SBOTerm");
				 * String keyToolTip =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "ToolTip");
				 */
				
				if (algebraic.isSetMetaId()) {
					algebraicRuleHelper.setMetaID(metaID);
				}
				if (algebraic.isSetSBOTerm()) {
					algebraicRuleHelper.setSBOTerm(sboTerm);
				}
				if (algebraic.isSetMath()) {
					algebraicRuleHelper.setFunction(math);
				}
				if (algebraic.isSetNotes()) {
					algebraicRuleHelper.setNotes(rule.getNotesString(),
							rule.getNotes());
				}
				if (algebraic.isSetAnnotation()) {
					if (algebraic.getAnnotation().isSetRDFannotation()) {
						algebraicRuleHelper.setAnnotation(algebraic
								.getAnnotation());
					}
					if (algebraic.getAnnotation().isSetNonRDFannotation()) {
						algebraicRuleHelper.setNonRDFAnnotation(algebraic
								.getAnnotation().getNonRDFannotation());
					}
				}
				
				algebraicCount++;
			}
			if (rule.isRate()) {
				RateRule rate = (RateRule) rule;
				
				String internHeadline = new StringBuffer(
						SBML_Constants.SBML_RATE_RULE).append(rateCount)
						.toString();
				String presentedHeadline = new StringBuffer("SBML Rate Rule ")
						.append(rateCount).toString();
				SBMLRateRule rateRuleHelper = ruleHelperObject.addRateRule(g,
						internHeadline, presentedHeadline);
				// initRateNiceIDs(internHeadline, presentedHeadline);
				
				/*
				 * String presentedHeadline = "SBML Rate Rule " + rateCount;
				 * String internHeadline = getNiceHeadline(presentedHeadline);
				 * SBML_Constants.put(internHeadline, presentedHeadline);
				 * String keyVariable =
				 * SBML_Constants.addToNiceIdList(presentedHeadline,
				 * "Variable"); String keyFormula =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "Formula");
				 * String keyMetaId =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "Meta ID");
				 * String keySBOTerm =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "SBOTerm");
				 * String keyToolTip =
				 * SBML_Constants.addToNiceIdList(presentedHeadline, "ToolTip");
				 */
				
				variable = rate.getVariable();
				if (rate.isSetMetaId()) {
					rateRuleHelper.setMetaID(metaID);
				}
				if (rate.isSetSBOTerm()) {
					rateRuleHelper.setSBOTerm(sboTerm);
				}
				if (rate.isSetMath()) {
					rateRuleHelper.setFunction(math);
				}
				if (rate.isSetVariable()) {
					rateRuleHelper.setVariable(variable);
				}
				if (rate.isSetNotes()) {
					rateRuleHelper.setNotes(rule.getNotesString(),
							rule.getNotes());
				}
				if (rate.isSetAnnotation()) {
					if (rate.getAnnotation().isSetRDFannotation()) {
						rateRuleHelper.setAnnotation(rate.getAnnotation());
					}
					if (rate.getAnnotation().isSetNonRDFannotation()) {
						rateRuleHelper.setNonRDFAnnotation(rate.getAnnotation()
								.getNonRDFannotation());
					}
				}
				
				rateCount++;
			}
		}
	}
	
}

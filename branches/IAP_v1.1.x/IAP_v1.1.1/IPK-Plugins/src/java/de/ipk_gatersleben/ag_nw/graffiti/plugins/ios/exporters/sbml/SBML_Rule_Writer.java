/**
 * This class sets the attributes of Rule
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import java.util.ArrayList;
import java.util.Iterator;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.text.parser.ParseException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLAlgebraicRule;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLAssignmentRule;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLRateRule;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLRuleHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Rule_Writer extends SBML_SBase_Writer {
	
	/**
	 * Controls the processing of rules
	 * 
	 * @param g
	 *        contains the values for the export
	 * @param model
	 *        the rules will be added to this model
	 */
	public void addRules(Graph g, Model model) {
		SBMLRuleHelper ruleHelperObject = new SBMLRuleHelper();
		ArrayList<String> assignmentRules = headlineHelper(g,
				SBML_Constants.SBML_ASSIGNMENT_RULE);
		if (assignmentRules.size() > 0) {
			Iterator<String> itAssignmentRules = assignmentRules.iterator();
			int assignmentRuleCount = 1;
			while (itAssignmentRules.hasNext()) {
				String assignmentRuleHeadline = (String) itAssignmentRules
						.next();
				// String presentedHeadline = "SBML Assignment Rule " +
				// assignmentRuleCount;
				addAssignmentRule(g, model, assignmentRuleHeadline,
						ruleHelperObject);
				assignmentRuleCount++;
			}
		}
		
		ArrayList<String> algebraicRules = headlineHelper(g,
				SBML_Constants.SBML_ALGEBRAIC_RULE);
		if (algebraicRules.size() > 0) {
			Iterator<String> itAlgebraicRules = algebraicRules.iterator();
			int algebraicRuleCount = 1;
			while (itAlgebraicRules.hasNext()) {
				String algebraicRuleHeadline = (String) itAlgebraicRules.next();
				// String presentedHeadline = "SBML Algebraic Rule " +
				// algebraicRuleCount;
				addAlgebraicRule(g, model, algebraicRuleHeadline,
						ruleHelperObject);
				algebraicRuleCount++;
			}
		}
		
		ArrayList<String> rateRules = headlineHelper(g,
				SBML_Constants.SBML_RATE_RULE);
		if (rateRules.size() > 0) {
			Iterator<String> itRateRules = rateRules.iterator();
			int rateRuleCount = 1;
			while (itRateRules.hasNext()) {
				String rateRuleHeadline = (String) itRateRules.next();
				// String presentedHeadline = "SBML Rate Rule " + rateRuleCount;
				addRateRules(g, model, rateRuleHeadline, ruleHelperObject);
				rateRuleCount++;
			}
		}
	}
	
	/**
	 * Adds an algebraic rule and its variables to the model
	 * 
	 * @param g
	 *        contains the values for the export
	 * @param model
	 *        the algebraic rule will be added to this model
	 * @param headline
	 *        indicates where the information should be read from
	 * @niceID intern representation of headline
	 */
	private void addAlgebraicRule(Graph g, Model model, String internHeadline,
			SBMLRuleHelper ruleHelperObject) {
		SBMLAlgebraicRule algebraicRuleHelper = ruleHelperObject
				.addAlgebraicRule(g, internHeadline);
		AlgebraicRule algebraicRule = (AlgebraicRule) model
				.createAlgebraicRule();
		addSBaseAttributes(algebraicRule, g, internHeadline);
		if (algebraicRuleHelper.isSetFunction()) {
			try {
				algebraicRule.setMath(ASTNode.parseFormula(algebraicRuleHelper
						.getFunction()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * if(AttributeHelper.hasAttribute(annotationNode, "SBML",
		 * "algebraic"+algebraicRuleCount+"annotation")){ Annotation anno =
		 * (Annotation)AttributeHelper.getAttributeValue(annotationNode, "SBML",
		 * "algebraic"+algebraicRuleCount+"annotation", SBML_Constants.EMPTY,
		 * null); algebraicRule.setAnnotation(anno); }
		 */
	}
	
	/**
	 * Adds an assignment rule and its variables to the model
	 * 
	 * @param g
	 *        contains the values for the export
	 * @param model
	 *        the assignment rule will be added to this model
	 * @param interHeadline
	 *        indicates where the information should be read from
	 * @niceID intern representation of headline
	 */
	private void addAssignmentRule(Graph g, Model model, String internHeadline,
			SBMLRuleHelper ruleHelperObject) {
		SBMLAssignmentRule assignmentRuleHelper = ruleHelperObject
				.addAssignmentRule(g, internHeadline);
		AssignmentRule assignmentRule = (AssignmentRule) model
				.createAssignmentRule();
		addSBaseAttributes(assignmentRule, g, internHeadline);
		if (assignmentRuleHelper.isSetVariable()) {
			assignmentRule.setVariable(assignmentRuleHelper.getVariable());
		}
		if (assignmentRuleHelper.isSetFunction()) {
			try {
				assignmentRule.setMath(ASTNode
						.parseFormula(assignmentRuleHelper.getFunction()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * if(AttributeHelper.hasAttribute(annotationNode, "SBML",
		 * "assignmet"+assignmentRuleCount+"annotation")){ Annotation anno =
		 * (Annotation)AttributeHelper.getAttributeValue(annotationNode, "SBML",
		 * "assignmet"+assignmentRuleCount+"annotation", SBML_Constants.EMPTY,
		 * null); assignmentRule.setAnnotation(anno); }
		 */
		
	}
	
	/**
	 * Adds a rate rule and its variables to the model
	 * 
	 * @param g
	 *        contains the values for the export
	 * @param model
	 *        the rate rule will be added to this model
	 * @param internHeadline
	 *        indicates where the information should be read from
	 * @niceID intern representation of headline
	 */
	private void addRateRules(Graph g, Model model, String internHeadline,
			SBMLRuleHelper ruleHelperObject) {
		SBMLRateRule rateRuleHelper = ruleHelperObject.addRateRule(g,
				internHeadline);
		RateRule rateRule = (RateRule) model.createRateRule();
		addSBaseAttributes(rateRule, g, internHeadline);
		if (rateRuleHelper.isSetVariable()) {
			rateRule.setVariable(rateRuleHelper.getVariable());
		}
		if (rateRuleHelper.isSetFunction()) {
			try {
				rateRule.setMath(ASTNode.parseFormula(rateRuleHelper
						.getFunction()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * if(AttributeHelper.hasAttribute(annotationNode, "SBML",
		 * "rate"+RateRuleCount+"annotation")){ Annotation anno =
		 * (Annotation)AttributeHelper.getAttributeValue(annotationNode, "SBML",
		 * "rate"+RateRuleCount+"annotation", SBML_Constants.EMPTY, null);
		 * rateRule.setAnnotation(anno); }
		 */
	}
}
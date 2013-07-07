package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;

public class SBMLRuleHelper {

List<SBMLAssignmentRule> assignmentRuleList;
List<SBMLAlgebraicRule> algebraicRuleList;
List<SBMLRateRule> rateRuleList;
	
	public SBMLRuleHelper(){
		assignmentRuleList = new ArrayList<SBMLAssignmentRule>();
		algebraicRuleList = new ArrayList<SBMLAlgebraicRule>();
		rateRuleList = new ArrayList<SBMLRateRule>();
	}
	
	public SBMLAssignmentRule addAssignmentRule(Graph g, String internHeadline){
		SBMLAssignmentRule assignmentRule = new SBMLAssignmentRule(g, internHeadline);
		assignmentRuleList.add(assignmentRule);
		return assignmentRule;
	}
	
	public SBMLAssignmentRule addAssignmentRule(Graph g, String internHeadline, String presentedHeadline){
		SBMLAssignmentRule assignmentRule = new SBMLAssignmentRule(g, internHeadline, presentedHeadline);
		assignmentRuleList.add(assignmentRule);
		return assignmentRule;
	}
	
	public SBMLAlgebraicRule addAlgebraicRule(Graph g, String internHeadline){
		SBMLAlgebraicRule algebraicRule = new SBMLAlgebraicRule(g, internHeadline);
		algebraicRuleList.add(algebraicRule);
		return algebraicRule;
	}
	
	public SBMLAlgebraicRule addAlgebraicRule(Graph g, String internHeadline, String presentedHeadline){
		SBMLAlgebraicRule algebraicRule = new SBMLAlgebraicRule(g, internHeadline, presentedHeadline);
		algebraicRuleList.add(algebraicRule);
		return algebraicRule;
	}
	
	public SBMLRateRule addRateRule(Graph g, String internHeadline){
		SBMLRateRule rateRule = new SBMLRateRule(g, internHeadline);
		rateRuleList.add(rateRule);
		return rateRule;
	}
	
	public SBMLRateRule addRateRule(Graph g, String internHeadline, String presentedHeadline){
		SBMLRateRule rateRule = new SBMLRateRule(g, internHeadline, presentedHeadline);
		rateRuleList.add(rateRule);
		return rateRule;
	}
	
	public List<String> getAssignmentRuleHeadlines(Graph g){
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_ASSIGNMENT_RULE);
	}
	
	public List<SBMLAssignmentRule> addAssignmentRules(Graph g, List<String> internHeadlines){
		List<SBMLAssignmentRule> returnList = new ArrayList<SBMLAssignmentRule>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while(internHeadlineIt.hasNext()){
			returnList.add(addAssignmentRule(g, internHeadlineIt.next()));
		}
		return returnList;
	}
	
	public List<String> getRateRuleHeadlines(Graph g){
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_RATE_RULE);
	}
	
	public List<SBMLRateRule> addRateRules(Graph g, List<String> internHeadlines){
		List<SBMLRateRule> returnList = new ArrayList<SBMLRateRule>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while(internHeadlineIt.hasNext()){
			returnList.add(addRateRule(g, internHeadlineIt.next()));
		}
		return returnList;
	}
	
	public List<String> getAlgebraicRuleHeadlines(Graph g){
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_ALGEBRAIC_RULE);
	}
	
	public List<SBMLAlgebraicRule> addAlgebraicRules(Graph g, List<String> internHeadlines){
		List<SBMLAlgebraicRule> returnList = new ArrayList<SBMLAlgebraicRule>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while(internHeadlineIt.hasNext()){
			returnList.add(addAlgebraicRule(g, internHeadlineIt.next()));
		}
		return returnList;
	}
}

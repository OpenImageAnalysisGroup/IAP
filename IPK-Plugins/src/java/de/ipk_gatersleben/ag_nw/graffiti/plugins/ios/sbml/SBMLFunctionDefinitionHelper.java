package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;

public class SBMLFunctionDefinitionHelper {

	/**
	 * Stores the SBMLFunctionDefinition objects
	 */
	List<SBMLFunctionDefinition> functionDefinitionList;
	
	public SBMLFunctionDefinitionHelper(){
		functionDefinitionList = new ArrayList<SBMLFunctionDefinition>();
	}
	
	public SBMLFunctionDefinition addFunctionDefinition(Graph g, String internHeadline){
		SBMLFunctionDefinition functionDefinition = new SBMLFunctionDefinition(g, internHeadline);
		functionDefinitionList.add(functionDefinition);
		return functionDefinition;
	}
	
	public SBMLFunctionDefinition addFunctionDefinition(Graph g, String internHeadline, String presentedHeadline){
		SBMLFunctionDefinition functionDefinition = new SBMLFunctionDefinition(g, internHeadline, presentedHeadline);
		functionDefinitionList.add(functionDefinition);
		return functionDefinition;
	}
	
	public List<String> getFunctionDefinitionHeadlines(Graph g){
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_FUNCTION_DEFINITION);
	}
	
	public List<SBMLFunctionDefinition> addFunctionDefinitions(Graph g, List<String> internHeadlines){
		List<SBMLFunctionDefinition> returnList = new ArrayList<SBMLFunctionDefinition>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while(internHeadlineIt.hasNext()){
			returnList.add(addFunctionDefinition(g, internHeadlineIt.next()));
		}
		return returnList;
	}
}

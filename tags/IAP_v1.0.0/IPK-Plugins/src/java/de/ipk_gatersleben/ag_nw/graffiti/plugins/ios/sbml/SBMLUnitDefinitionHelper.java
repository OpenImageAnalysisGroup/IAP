package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;

public class SBMLUnitDefinitionHelper {

	/**
	 * Stores the SBMLUnitDefinition objects
	 */
	List<SBMLUnitDefinition> unitDefinitionList;
	
	public SBMLUnitDefinitionHelper(){
		unitDefinitionList = new ArrayList<SBMLUnitDefinition>();
	}
	
	public SBMLUnitDefinition addUnitDefinition(Graph g, String internHeadline){
		SBMLUnitDefinition unitDefinition = new SBMLUnitDefinition(g, internHeadline);
		unitDefinitionList.add(unitDefinition);
		return unitDefinition;
	}
	
	public SBMLUnitDefinition addUnitDefinition(Graph g, String internHeadline, String presentedHeadline){
		SBMLUnitDefinition unitDefinition = new SBMLUnitDefinition(g, internHeadline, presentedHeadline);
		unitDefinitionList.add(unitDefinition);
		return unitDefinition;
	}
	
	public List<String> getUnitDefinitionHeadlines(Graph g){
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_UNIT_DEFINITION);
	}
	
	public List<SBMLUnitDefinition> addUnitDefinitions(Graph g, List<String> internHeadlines){
		List<SBMLUnitDefinition> returnList = new ArrayList<SBMLUnitDefinition>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while(internHeadlineIt.hasNext()){
			returnList.add(addUnitDefinition(g, internHeadlineIt.next()));
		}
		return returnList;
	}
}

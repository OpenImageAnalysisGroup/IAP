package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;

public class SBMLInitialAssignmentHelper {

List<SBMLInitialAssignment> initialAssignmentList;
	
	public SBMLInitialAssignmentHelper(){
		initialAssignmentList = new ArrayList<SBMLInitialAssignment>();
	}
	
	public SBMLInitialAssignment addInitialAssignment(Graph g, String internHeadline){
		SBMLInitialAssignment initialAssignment = new SBMLInitialAssignment(g, internHeadline);
		initialAssignmentList.add(initialAssignment);
		return initialAssignment;
	}
	
	public SBMLInitialAssignment addInitialAssignment(Graph g, String internHeadline, String presentedHeadline){
		SBMLInitialAssignment initialAssignmet = new SBMLInitialAssignment(g, internHeadline, presentedHeadline);
		initialAssignmentList.add(initialAssignmet);
		return initialAssignmet;
	}
	
	public List<String> getInitialAssignmentHeadlines(Graph g){
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_INITIAL_ASSIGNMENT);
	}
	
	public List<SBMLInitialAssignment> addInitialAssignments(Graph g, List<String> internHeadlines){
		List<SBMLInitialAssignment> returnList = new ArrayList<SBMLInitialAssignment>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while(internHeadlineIt.hasNext()){
			returnList.add(addInitialAssignment(g, internHeadlineIt.next()));
		}
		return returnList;
	}
}

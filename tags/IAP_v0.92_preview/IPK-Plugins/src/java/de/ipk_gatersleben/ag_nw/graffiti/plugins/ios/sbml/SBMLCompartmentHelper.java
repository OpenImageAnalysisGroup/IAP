package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;

public class SBMLCompartmentHelper {
	
	/**
	 * Stores the SBMLCompartment objects
	 */
	List<SBMLCompartment> compartmentList;
	
	public SBMLCompartmentHelper(){
		compartmentList = new ArrayList<SBMLCompartment>();
	}
	
	public SBMLCompartment addCompartment(Graph g, String internHeadline){
		SBMLCompartment compartment = new SBMLCompartment(g, internHeadline);
		compartmentList.add(compartment);
		return compartment;
	}
	
	public SBMLCompartment addCompartment(Graph g, String internHeadline, String presentedHeadline){
		SBMLCompartment compartment = new SBMLCompartment(g, internHeadline, presentedHeadline);
		compartmentList.add(compartment);
		return compartment;
	}
	
	public List<String> getCompartmentHeadlines(Graph g){
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_COMPARTMENT);
	}
	
	public List<SBMLCompartment> addCompartments(Graph g, List<String> internHeadlines){
		List<SBMLCompartment> returnList = new ArrayList<SBMLCompartment>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while(internHeadlineIt.hasNext()){
			returnList.add(addCompartment(g, internHeadlineIt.next()));
		}
		return returnList;
	}
}

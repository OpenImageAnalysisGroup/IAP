package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;

public class SBMLParameterHelper {

	/**
	 * Stores the SBMLParameter objects
	 */
	List<SBMLParameter> parameterList;

	public SBMLParameterHelper() {
		parameterList = new ArrayList<SBMLParameter>();
	}

	public SBMLParameter addParameter(Graph g, String internHeadline) {
		SBMLParameter parameter = new SBMLParameter(g, internHeadline);
		parameterList.add(parameter);
		return parameter;
	}

	public SBMLParameter addParameter(Graph g, String internHeadline,
			String presentedHeadline) {
		SBMLParameter parameter = new SBMLParameter(g, internHeadline,
				presentedHeadline);
		parameterList.add(parameter);
		return parameter;
	}

	public List<String> getParameterHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_PARAMETER);
	}

	public List<SBMLParameter> addParameters(Graph g,
			List<String> internHeadlines) {
		List<SBMLParameter> returnList = new ArrayList<SBMLParameter>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while (internHeadlineIt.hasNext()) {
			returnList.add(addParameter(g, internHeadlineIt.next()));
		}
		return returnList;
	}
}

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;

public class SBMLConstraintHelper {

	/**
	 * Stores the SBMLConstraint objects
	 */
	List<SBMLConstraint> constraintList;

	public SBMLConstraintHelper() {
		constraintList = new ArrayList<SBMLConstraint>();
	}

	public SBMLConstraint addConstraint(Graph g, String internHeadline) {
		SBMLConstraint constraint = new SBMLConstraint(g, internHeadline);
		constraintList.add(constraint);
		return constraint;
	}

	public SBMLConstraint addConstraint(Graph g, String internHeadline,
			String presentedHeadline) {
		SBMLConstraint constraint = new SBMLConstraint(g, internHeadline,
				presentedHeadline);
		constraintList.add(constraint);
		return constraint;
	}

	public List<String> getConstraintHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_CONSTRAINT);
	}

	public List<SBMLConstraint> addConstraints(Graph g,
			List<String> internHeadlines) {
		List<SBMLConstraint> returnList = new ArrayList<SBMLConstraint>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while (internHeadlineIt.hasNext()) {
			returnList.add(addConstraint(g, internHeadlineIt.next()));
		}
		return returnList;
	}
}

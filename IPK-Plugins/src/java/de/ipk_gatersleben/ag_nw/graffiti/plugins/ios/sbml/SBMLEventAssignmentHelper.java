package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.Collection;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

public class SBMLEventAssignmentHelper {

	/**
	 * Intern graph object
	 */
	Graph g;

	/**
	 * Provides necessary methods
	 */
	SBML_SBase_Writer attWriter;

	/**
	 * Provides necessary methods
	 */
	SBML_SBase_Reader attReader;

	/**
	 * intern representation of the headline
	 */
	String internHeadline = SBML_Constants.EMPTY;

	/**
	 * The user will see the headline this way
	 */
	String presentedHeadline;

	int eventAssignmentCount;

	/**
	 * The hidden event attributes are stored here
	 */
	HashSet<String> discardedRowIDs;

	public SBMLEventAssignmentHelper(Graph g, String internHeadline,
			String presentedHeadline, int eventAssignmentCount) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.presentedHeadline = presentedHeadline;
		this.internHeadline = internHeadline;
		this.eventAssignmentCount = eventAssignmentCount;
		initEventAssignmentNideIDs(eventAssignmentCount);

		Collection<String> colDiscardedRowIDs = DefaultEditPanel
				.getDiscardedRowIDs();
		discardedRowIDs = new HashSet<String>(colDiscardedRowIDs);
		DefaultEditPanel.setDiscardedRowIDs(discardedRowIDs);
	}

	public SBMLEventAssignmentHelper(Graph g, String internHeadline,
			int eventAssignmentCount) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.eventAssignmentCount = eventAssignmentCount;
		this.internHeadline = internHeadline;
	}

	public void setVariable(String variable) {
		if (!variable.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.VARIABLE).toString(),
					variable);
		}
	}

	public Boolean isSetVariable() {
		if (AttributeHelper.hasAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.VARIABLE).toString())) {
			return true;
		} else {
			return false;
		}
	}

	public void setFunction(String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.FUNCTION).toString(),
					function);
		}
	}

	public void setMetaID(String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.EVENT_ASSIGNMENT)
					.append(eventAssignmentCount)
					.append(SBML_Constants.META_ID).toString(), metaID);
		}
	}

	public void setSBOTerm(String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.EVENT_ASSIGNMENT)
					.append(eventAssignmentCount)
					.append(SBML_Constants.SBOTERM).toString(), sboTerm);
		}
	}

	public void setAnnotation(Annotation annotation) {
		AttributeHelper.setAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.ANNOTATION).toString(),
				annotation);

		discardedRowIDs.add(new StringBuffer(internHeadline)
				.append(SBML_Constants.EVENT_ASSIGNMENT)
				.append(eventAssignmentCount).append(SBML_Constants.ANNOTATION)
				.toString());
	}

	public void setNonRDFAnnotation(String nonRDFAnnotation) {
		AttributeHelper.setAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.NON_RDF_ANNOTATION).toString(),
				nonRDFAnnotation);

		discardedRowIDs.add(new StringBuffer(internHeadline)
				.append(SBML_Constants.EVENT_ASSIGNMENT)
				.append(eventAssignmentCount)
				.append(SBML_Constants.NON_RDF_ANNOTATION).toString());
	}

	public void setNotes(String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(
					notesObj,
					notes,
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.NOTES).toString());
		}

		discardedRowIDs.add(new StringBuffer(internHeadline)
				.append(SBML_Constants.EVENT_ASSIGNMENT)
				.append(eventAssignmentCount).append(SBML_Constants.NOTES)
				.toString());
	}

	public Boolean isSetFunction() {
		if (AttributeHelper.hasAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.FUNCTION).toString())) {
			return true;
		} else {
			return false;
		}
	}

	public String getVariable() {
		if (isSetVariable()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.VARIABLE).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public String getFunction() {
		if (isSetFunction()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	private void initEventAssignmentNideIDs(int eventAssignmentCount) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.VARIABLE).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " Variable");

		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.FUNCTION).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " Function");

		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.META_ID).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " Meta ID");

		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.SBOTERM).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " SBOTerm");

		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.NOTES).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " Notes");

	}
}

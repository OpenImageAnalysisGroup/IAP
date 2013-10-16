package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

public class SBMLEventHelper {

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
	String internHeadline;

	/**
	 * The user will see the headline this way
	 */
	String presentedHeadline;

	List<SBMLEventAssignmentHelper> eaHelperList;

	/**
	 * The hidden event attributes are stored here
	 */
	HashSet<String> discardedRowIDs;

	public SBMLEventHelper(Graph g, String internHeadline,
			String presentedHeadline) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.presentedHeadline = presentedHeadline;
		this.internHeadline = internHeadline;
		eaHelperList = new ArrayList<SBMLEventAssignmentHelper>();
		initEventNideIDs();

		Collection<String> colDiscardedRowIDs = DefaultEditPanel
				.getDiscardedRowIDs();
		discardedRowIDs = new HashSet<String>(colDiscardedRowIDs);
		DefaultEditPanel.setDiscardedRowIDs(discardedRowIDs);
	}

	public SBMLEventHelper(Graph g, String internHeadline) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.internHeadline = internHeadline;
		eaHelperList = new ArrayList<SBMLEventAssignmentHelper>();
	}

	public SBMLEventAssignmentHelper addEventAssignment(Graph g,
			String internHeadline, String presentedHeadline,
			int eventAssignmentCount) {
		SBMLEventAssignmentHelper eaHelper = new SBMLEventAssignmentHelper(g,
				internHeadline, presentedHeadline, eventAssignmentCount);
		eaHelperList.add(eaHelper);
		return eaHelper;
	}

	public SBMLEventAssignmentHelper addEventAssignment(Graph g,
			String internHeadline, int eaCount) {
		SBMLEventAssignmentHelper eaHelper = new SBMLEventAssignmentHelper(g,
				internHeadline, eaCount);
		eaHelperList.add(eaHelper);
		return eaHelper;
	}

	public void setUseValuesFromTriggerTime(Boolean useValuesFromTriggerTime) {
		if (!useValuesFromTriggerTime.equals(null)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME)
							.toString(), useValuesFromTriggerTime);
		}
	}

	public void setID(String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.EVENT_ID).toString(),
					ID);
		}
	}

	public void setName(String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.EVENT_NAME)
					.toString(), name);
		}
	}

	public void setMetaID(String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.META_ID).toString(),
					metaID);
		}
	}

	public void setSBOTerm(String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.SBOTERM).toString(),
					sboTerm);
		}
	}

	public void setAnnotation(Annotation annotation) {
		AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.ANNOTATION).toString(),
				annotation);

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.ANNOTATION).toString());
	}

	public void setNonRDFAnnotation(String nonRDFAnnotation) {
		AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.NON_RDF_ANNOTATION)
				.toString(), nonRDFAnnotation);

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.NON_RDF_ANNOTATION).toString());
	}

	public void setNotes(String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(
					notesObj,
					notes,
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.NOTES).toString());
		}

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.NOTES).toString());
	}

	public Boolean isSetName() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.EVENT_NAME).toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean isSetID() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.EVENT_ID).toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean isSetUseValuesFromTriggerTime() {
		if (AttributeHelper
				.hasAttribute(
						g,
						internHeadline,
						new StringBuffer(internHeadline).append(
								SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME)
								.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public String getID() {
		if (isSetID()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.EVENT_ID).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public String getName() {
		if (isSetName()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.EVENT_NAME).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public Boolean getUseValuesFromTriggerTime() {
		if (isSetUseValuesFromTriggerTime()) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME)
							.toString());
		} else {
			return null;
		}
	}

	public void setInitialValue(Boolean initialValue) {
		if (!initialValue.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.INITIAL_VALUE)
					.toString(), initialValue);
		}
	}

	public void setPersistent(Boolean persistent) {
		if (!persistent.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PERSISTENT)
					.toString(), persistent);
		}
	}

	public void setTriggerFunction(String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.TRIGGER_FUNCTION)
					.toString(), function);
		}
	}

	public Boolean isSetTriggerFunction() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.TRIGGER_FUNCTION)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean isSetInitialValue() {
		if (AttributeHelper
				.hasAttribute(g, internHeadline, new StringBuffer(
						internHeadline).append(SBML_Constants.INITIAL_VALUE)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean isSetPersistent() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PERSISTENT).toString())) {
			return true;
		} else {
			return false;
		}
	}

	public String getTriggerFunction() {
		if (isSetTriggerFunction()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.TRIGGER_FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public Boolean getInitialValue() {
		if (isSetInitialValue()) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.INITIAL_VALUE).toString());
		} else {
			return null;
		}
	}

	public Boolean getPersistent() {
		if (isSetPersistent()) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PERSISTENT).toString());
		} else {
			return null;
		}
	}

	public void setPriorityFunction(String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PRIORITY_FUNCTION)
					.toString(), function);
		}
	}

	public void setPriorityMetaID(String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PRIORITY_META_ID)
					.toString(), metaID);
		}
	}

	public void setPrioritySBOTerm(String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PRIORITY_SBOTERM)
					.toString(), sboTerm);
		}
	}

	public void setPriorityAnnotation(Annotation annotation) {
		AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PRIORITY_ANNOTATION)
				.toString(), annotation);

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.PRIORITY_ANNOTATION).toString());
	}

	public void setPriorityNonRDFAnnotation(String nonRDFAnnotation) {
		AttributeHelper.setAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_NON_RDF_ANNOTATION).toString(),
				nonRDFAnnotation);

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.PRIORITY_NON_RDF_ANNOTATION).toString());
	}

	public void setPriorityNotes(String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(
					notesObj,
					notes,
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PRIORITY_NOTES).toString());
		}

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.PRIORITY_NOTES).toString());
	}

	public Boolean isSetPriorityFunction() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PRIORITY_FUNCTION)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public String getPriorityFunction() {
		if (isSetPriorityFunction()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PRIORITY_FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public void setDelayFunction(String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.DELAY_FUNCTION)
					.toString(), function);
		}
	}

	public void setDelayMetaID(String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.DELAY_META_ID)
					.toString(), metaID);
		}
	}

	public void setdelayAnnotation(Annotation annotation) {
		AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.DELAY_ANNOTATION)
				.toString(), annotation);

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.DELAY_ANNOTATION).toString());
	}

	public void setdelayNonRDFAnnotation(String nonRDFAnnotation) {
		AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.DELAY_NON_RDF_ANNOTATION)
				.toString(), nonRDFAnnotation);

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.DELAY_NON_RDF_ANNOTATION).toString());
	}

	public void setDelaySBOTerm(String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.DELAY_SBOTERM)
					.toString(), sboTerm);
		}
	}

	public void setDelayNotes(String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(
					notesObj,
					notes,
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.DELAY_NOTES).toString());
		}

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.DELAY_NOTES).toString());
	}

	public Boolean isSetDelayFunction() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.DELAY_FUNCTION)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public String getDelayFunction() {
		if (isSetDelayFunction()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.DELAY_FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	private void initEventNideIDs() {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ID).toString(),
				presentedHeadline + ": ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.EVENT_NAME).toString(),
				presentedHeadline + ": Name");
		AttributeHelper
				.setNiceId(
						new StringBuffer(internHeadline).append(
								SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME)
								.toString(), presentedHeadline
								+ ": UseValuesFromTriggerTime");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.INITIAL_VALUE).toString(),
				presentedHeadline + ": Trigger Initial Value");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PERSISTENT).toString(),
				presentedHeadline + ": Trigger Persistent");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.TRIGGER_FUNCTION).toString(),
				presentedHeadline + ": Trigger Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_META_ID).toString(),
				presentedHeadline + ": Priority Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_NOTES).toString(),
				presentedHeadline + ": Priority Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_SBOTERM).toString(),
				presentedHeadline + ": Priority SBOTerm");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_FUNCTION).toString(),
				presentedHeadline + ": Priority Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.DELAY_META_ID).toString(),
				presentedHeadline + ": Delay Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.DELAY_NOTES).toString(),
				presentedHeadline + ": Delay Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.DELAY_SBOTERM).toString(),
				presentedHeadline + ": Delay SBOTerm");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.DELAY_FUNCTION).toString(),
				presentedHeadline + ": Delay Function");
	}
}

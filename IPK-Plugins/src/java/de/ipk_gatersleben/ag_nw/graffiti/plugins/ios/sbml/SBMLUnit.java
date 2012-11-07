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

public class SBMLUnit {

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

	/**
	 * The number of the current sub unit
	 */
	int subUnitCount;

	/**
	 * The hidden unit attributes are stored here
	 */
	HashSet<String> discardedRowIDs;

	public SBMLUnit(Graph g, String internHeadline, String presentedHeadline,
			int subUnitCount) {
		this.g = g;
		this.subUnitCount = subUnitCount;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.presentedHeadline = presentedHeadline;
		this.internHeadline = internHeadline;
		initSubUnitDefinitionNideIDs();

		Collection<String> colDiscardedRowIDs = DefaultEditPanel
				.getDiscardedRowIDs();
		discardedRowIDs = new HashSet<String>(colDiscardedRowIDs);
		DefaultEditPanel.setDiscardedRowIDs(discardedRowIDs);
	}

	public SBMLUnit(Graph g, String internHeadline, int subUnitCount) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.internHeadline = internHeadline;
		this.subUnitCount = subUnitCount;
	}

	public void setMetaID(String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.SUB_UNIT)
							.append(subUnitCount)
							.append(SBML_Constants.META_ID).toString(), metaID);
		}
	}

	public void setSBOTerm(String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper
					.setAttribute(g, internHeadline, new StringBuffer(
							internHeadline).append(SBML_Constants.SUB_UNIT)
							.append(subUnitCount)
							.append(SBML_Constants.SBOTERM).toString(), sboTerm);
		}
	}

	public void setNotes(String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(
					notesObj,
					notes,
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.SUB_UNIT)
							.append(subUnitCount).append(SBML_Constants.NOTES)
							.toString());
		}
		discardedRowIDs.add(new StringBuffer(internHeadline)
				.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
				.append(SBML_Constants.NOTES).toString());
	}

	public void setAnnotation(Annotation annotation) {
		AttributeHelper.setAttribute(g, internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append(SBML_Constants.ANNOTATION).toString(),
				annotation);

		discardedRowIDs.add(new StringBuffer(internHeadline)
				.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
				.append(SBML_Constants.ANNOTATION).toString());
	}

	public void setNonRDFAnnotation(String nonRDFAnnotation) {
		AttributeHelper.setAttribute(g, internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append(SBML_Constants.NON_RDF_ANNOTATION).toString(),
				nonRDFAnnotation);

		discardedRowIDs.add(new StringBuffer(internHeadline)
				.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
				.append(SBML_Constants.NON_RDF_ANNOTATION).toString());
	}

	public void setComposedSubUnit(String composedSubUnit) {
		if (!composedSubUnit.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.SUB_UNIT)
							.append(subUnitCount).append("_").toString(),
					composedSubUnit);
		}
	}

	public void setComposedUnit(String composedUnit) {
		if (!composedUnit.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.UNIT).toString(),
					composedUnit);
		}
	}

	private void initSubUnitDefinitionNideIDs() {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append("_").toString(),
				new StringBuffer(presentedHeadline).append(": Sub Unit ")
						.append(subUnitCount).toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.UNIT)
						.toString(), new StringBuffer(presentedHeadline)
						.append(": Unit").toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append(SBML_Constants.META_ID).toString(),
				new StringBuffer(presentedHeadline).append(": Sub Unit ")
						.append(subUnitCount).append(" Meta ID").toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append(SBML_Constants.SBOTERM).toString(),
				new StringBuffer(presentedHeadline).append(": Sub Unit ")
						.append(subUnitCount).append(" SBOTerm").toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append(SBML_Constants.NOTES).toString(),
				new StringBuffer(presentedHeadline).append(": Sub Unit ")
						.append(subUnitCount).append(" Notes").toString());
	}
}

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.Collection;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

public class SBMLFunctionDefinition {

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
	 * The hidden function definition attributes are stored here
	 */
	HashSet<String> discardedRowIDs;

	/**
	 * Constructor. Initializes the graph. Use in
	 * SBML_FunctionDefinition_Reader.java
	 * 
	 * @param g
	 *            the graph where the information is read from
	 */
	public SBMLFunctionDefinition(Graph g, String internHeadline,
			String presentedHeadline) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.presentedHeadline = presentedHeadline;
		this.internHeadline = internHeadline;
		initFunctionDefinitionNideIDs();

		Collection<String> colDiscardedRowIDs = DefaultEditPanel
				.getDiscardedRowIDs();
		discardedRowIDs = new HashSet<String>(colDiscardedRowIDs);
		DefaultEditPanel.setDiscardedRowIDs(discardedRowIDs);
	}

	/**
	 * Constructor. Initializes the graph. Use in
	 * SBML_FunctionDefinition_Writer.java
	 * 
	 * @param g
	 *            the graph where the information is read from
	 * @param internHeadline
	 *            the intern headline of the current Compartment
	 */
	public SBMLFunctionDefinition(Graph g, String internHeadline) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.internHeadline = internHeadline;
	}

	public Boolean isSetID() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.FUNCTION_DEFINITION_ID)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean isSetName() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.FUNCTION_DEFINITION_NAME)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean isSetFunction() {
		if (AttributeHelper
				.hasAttribute(
						g,
						internHeadline,
						new StringBuffer(internHeadline).append(
								SBML_Constants.FUNCTION_DEFINITION_FUNCTION)
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
							SBML_Constants.FUNCTION_DEFINITION_ID).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public String getName() {
		if (isSetName()) {
			return (String) attWriter
					.getAttribute(
							g,
							internHeadline,
							new StringBuffer(internHeadline).append(
									SBML_Constants.FUNCTION_DEFINITION_NAME)
									.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public ASTNode getFunction() {
		if (isSetFunction()) {
			try {
				return ASTNode.parseFormula((String) attWriter.getAttribute(
						g,
						internHeadline,
						new StringBuffer(internHeadline).append(
								SBML_Constants.FUNCTION_DEFINITION_FUNCTION)
								.toString()));
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public void setName(String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper
					.setAttribute(
							g,
							internHeadline,
							new StringBuffer(internHeadline).append(
									SBML_Constants.FUNCTION_DEFINITION_NAME)
									.toString(), name);

		}
	}

	public void setFunction(String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.FUNCTION_DEFINITION_FUNCTION)
							.toString(), function);
		}
	}

	public void setID(String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.FUNCTION_DEFINITION_ID).toString(),
					ID);
		}
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
		discardedRowIDs.add(internHeadline + "_notes");
	}

	public void setMetaID(String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.META_ID).toString(),
					metaID);
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

	public void setSBOTerm(String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.SBOTERM).toString(),
					sboTerm);
		}
	}

	private void initFunctionDefinitionNideIDs() {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		AttributeHelper
				.setNiceId(
						new StringBuffer(internHeadline).append(
								SBML_Constants.FUNCTION_DEFINITION_FUNCTION)
								.toString(), presentedHeadline + ": Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.FUNCTION_DEFINITION_ID).toString(),
				presentedHeadline + ": ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.FUNCTION_DEFINITION_NAME).toString(),
				presentedHeadline + ": Name");
	}
}

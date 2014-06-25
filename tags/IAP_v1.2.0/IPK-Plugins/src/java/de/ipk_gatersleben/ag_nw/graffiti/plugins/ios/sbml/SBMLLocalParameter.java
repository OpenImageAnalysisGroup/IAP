package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.Collection;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

public class SBMLLocalParameter {
	
	Graph g;
	
	/**
	 * Provides necessary methods
	 */
	SBML_SBase_Writer attWriter;
	
	/**
	 * Provides necessary methods
	 */
	SBML_SBase_Reader attReader;
	
	String internAttributeName;
	
	int localParameterCount;
	
	/**
	 * The hidden local parameter attributes are stored here
	 */
	HashSet<String> discardedRowIDs;
	
	/**
	 * Constructor. Initializes the graph. In Reader
	 * 
	 * @param g
	 *        the graph where the information is read from
	 */
	public SBMLLocalParameter(Graph g, String presentedAttributeName,
			String internAttributeName) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.internAttributeName = internAttributeName;
		initLocalParameterNideIDs(presentedAttributeName, internAttributeName,
				"Kinetic Law");
		
		Collection<String> colDiscardedRowIDs = DefaultEditPanel
				.getDiscardedRowIDs();
		discardedRowIDs = new HashSet<String>(colDiscardedRowIDs);
		DefaultEditPanel.setDiscardedRowIDs(discardedRowIDs);
	}
	
	// In writer
	public SBMLLocalParameter(Graph g, int localParameterCount) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.localParameterCount = localParameterCount;
	}
	
	public void setID(Node reactionNode, String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(internAttributeName).append(
							SBML_Constants.LOCAL_PARAMETER_ID).toString(), ID);
		}
	}
	
	public void setName(Node reactionNode, String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(internAttributeName).append(
							SBML_Constants.LOCAL_PARAMETER_NAME).toString(),
					name);
		}
	}
	
	public void setValue(Node reactionNode, Double value) {
		if (!value.equals(null)) {
			AttributeHelper.setAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(internAttributeName).append(
							SBML_Constants.LOCAL_PARAMETER_VALUE).toString(),
					value);
		}
	}
	
	public void setUnits(Node reactionNode, String units) {
		if (!units.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(internAttributeName).append(
							SBML_Constants.LOCAL_PARAMETER_UNITS).toString(),
					units);
		}
	}
	
	public void setMetaID(Node reactionNode, String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
							internAttributeName).append(SBML_Constants.META_ID)
							.toString(), metaID);
		}
	}
	
	public String getMetaID(Node node) {
		if (isSetMetaID(node)) {
			return (String) attWriter.getAttribute(node,
					SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
							internAttributeName).append(SBML_Constants.META_ID)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public Boolean isSetMetaID(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(
						internAttributeName).append(SBML_Constants.META_ID)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteMetaID(Node node) {
		if (isSetMetaID(node)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(
							internAttributeName).append(SBML_Constants.META_ID)
							.toString());
		}
	}
	
	public void setSBOTerm(Node reactionNode, String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
							internAttributeName).append(SBML_Constants.SBOTERM)
							.toString(), sboTerm);
		}
	}
	
	public Boolean isSetSBOTerm(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(
						internAttributeName).append(SBML_Constants.SBOTERM)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteSBOTerm(Node node) {
		if (isSetSBOTerm(node)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(
							internAttributeName).append(SBML_Constants.SBOTERM)
							.toString());
		}
	}
	
	public String getSBOTerm(Node node) {
		if (isSetSBOTerm(node)) {
			return (String) attWriter.getAttribute(node,
					SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
							internAttributeName).append(SBML_Constants.SBOTERM)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void setAnnotation(Node reactionNode, Annotation annotation) {
		AttributeHelper.setAttribute(reactionNode,
				SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
						internAttributeName).append(SBML_Constants.ANNOTATION)
						.toString(), annotation);
		discardedRowIDs.add(new StringBuffer(internAttributeName).append(
				SBML_Constants.ANNOTATION).toString());
	}
	
	public Boolean isSetAnnotation(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(
						internAttributeName).append(SBML_Constants.ANNOTATION)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteAnnotation(Node node) {
		if (isSetAnnotation(node)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(
							internAttributeName).append(SBML_Constants.ANNOTATION)
							.toString());
		}
	}
	
	public Annotation getAnnotation(Node node) {
		if (isSetAnnotation(node)) {
			return (Annotation) attWriter.getAttribute(node,
					SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
							internAttributeName).append(SBML_Constants.ANNOTATION)
							.toString());
		} else {
			return null;
		}
	}
	
	public void setNonRDFAnnotation(Node reactionNode, String nonRDFAnnotation) {
		AttributeHelper.setAttribute(
				reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(internAttributeName).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(),
				nonRDFAnnotation);
		// discardedRowIDs.add(new StringBuffer(internAttributeName).append(
		// SBML_Constants.NON_RDF_ANNOTATION).toString());
	}
	
	public Boolean isSetNonRDFAnnotation(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(
						internAttributeName).append(SBML_Constants.NON_RDF_ANNOTATION)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteNonRDFAnnotation(Node node) {
		if (isSetNonRDFAnnotation(node)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(
							internAttributeName).append(SBML_Constants.NON_RDF_ANNOTATION)
							.toString());
		}
	}
	
	public String getNonRDFAnnotation(Node node) {
		if (isSetNonRDFAnnotation(node)) {
			return (String) attWriter.getAttribute(node,
					SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
							internAttributeName).append(SBML_Constants.NON_RDF_ANNOTATION)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void setNotes(Node reactionNode, String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(notesObj, notes, reactionNode,
					SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
							internAttributeName).append(SBML_Constants.NOTES)
							.toString());
		}
		// discardedRowIDs.add(new StringBuffer(internAttributeName).append(
		// SBML_Constants.NOTES).toString());
	}
	
	public void deleteNotes(Node node) {
		if (isSetNotes(node)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(
							internAttributeName).append(SBML_Constants.NOTES)
							.toString());
		}
	}
	
	public Boolean isSetNotes(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(
						internAttributeName).append(SBML_Constants.NOTES)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getNotes(Node node) {
		if (isSetNotes(node)) {
			return (String) attWriter.getAttribute(node,
					SBML_Constants.SBML_KINETIC_LAW, new StringBuffer(
							internAttributeName).append(SBML_Constants.NOTES)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public Boolean isSetID(Node reactionNode) {
		if (AttributeHelper.hasAttribute(
				reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.LOCAL_PARAMETER_ID).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetName(Node reactionNode) {
		if (AttributeHelper
				.hasAttribute(
						reactionNode,
						SBML_Constants.SBML_KINETIC_LAW,
						new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
								.append(localParameterCount)
								.append(SBML_Constants.LOCAL_PARAMETER_NAME)
								.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetValue(Node reactionNode) {
		if (AttributeHelper.hasAttribute(
				reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.LOCAL_PARAMETER_VALUE)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetUnits(Node reactionNode) {
		if (AttributeHelper.hasAttribute(
				reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.LOCAL_PARAMETER_UNITS)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getID(Node reactionNode) {
		if (isSetID(reactionNode)) {
			return (String) attWriter.getAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.LOCAL_PARAMETER_ID)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public String getName(Node reactionNode) {
		if (isSetName(reactionNode)) {
			return (String) attWriter.getAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.LOCAL_PARAMETER_NAME)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public Double getValue(Node reactionNode) {
		if (isSetValue(reactionNode)) {
			return (Double) attWriter.getAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.LOCAL_PARAMETER_VALUE)
							.toString());
		} else {
			return null;
		}
	}
	
	public String getUnits(Node reactionNode) {
		if (isSetUnits(reactionNode)) {
			return (String) attWriter.getAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.LOCAL_PARAMETER_UNITS)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	private void initLocalParameterNideIDs(String presentedAttributeName,
			String internAttributeName, String presentedHeadline) {
		presentedHeadline = "SBML " + presentedHeadline;
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.META_ID).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Meta ID")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.SBOTERM).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" SBOTerm")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.NOTES).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Notes")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.LOCAL_PARAMETER_ID).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" ID")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.LOCAL_PARAMETER_NAME).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Name")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.LOCAL_PARAMETER_VALUE).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Value")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.LOCAL_PARAMETER_UNITS).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Units")
						.toString());
	}
}

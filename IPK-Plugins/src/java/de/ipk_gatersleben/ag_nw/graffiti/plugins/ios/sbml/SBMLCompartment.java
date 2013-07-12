package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.Collection;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

@SuppressWarnings("unused")
public class SBMLCompartment {

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
	 * The hidden compartment attributes are stored here
	 */
	HashSet<String> discardedRowIDs;

	/**
	 * Constructor. Initializes the graph. Use in SBML_Compartment_Reader.java
	 * 
	 * @param g
	 *            the graph where the information is read from
	 */
	public SBMLCompartment(Graph g, String internHeadline,
			String presentedHeadline) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.presentedHeadline = presentedHeadline;
		this.internHeadline = internHeadline;
		initCompartmentNideIDs();

		Collection<String> colDiscardedRowIDs = DefaultEditPanel
				.getDiscardedRowIDs();
		discardedRowIDs = new HashSet<String>(colDiscardedRowIDs);
		DefaultEditPanel.setDiscardedRowIDs(discardedRowIDs);
	}

	/**
	 * Constructor. Initializes the graph. Use in SBML_Compartmetn_Writer.java
	 * 
	 * @param g
	 *            the graph where the information is read from
	 * @param internHeadline
	 *            the intern headline of the current Compartment
	 */
	public SBMLCompartment(Graph g, String internHeadline) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		this.internHeadline = internHeadline;
	}

	public void countCompartments() {
		int i = 0;

	}

	public Compartment getCompartment() {
		Compartment comartment = new Compartment();
		comartment.setId(getID());
		comartment.setName(getName());
		comartment.setSpatialDimensions(getSpatialDimensions());
		comartment.setSize(getSize());
		comartment.setUnits(getUnits());
		comartment.setConstant(getConstant());
		return comartment;
	}

	public Boolean isSetID() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.COMPARTMENT_ID)
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
							SBML_Constants.COMPARTMENT_ID).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public void setID(String id) {
		if (!id.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.COMPARTMENT_ID)
					.toString(), id);
		}
	}

	public void setName(String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.COMPARTMENT_NAME)
					.toString(), name);
		}
	}

	public Boolean isSetName() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.COMPARTMENT_NAME)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public String getName() {
		if (isSetName()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.COMPARTMENT_NAME).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public void setSpatialDimensions(Double spatialDimensions) {
		if (!spatialDimensions.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.SPATIAL_DIMENSIONS)
					.toString(), spatialDimensions);
		}
	}

	public Boolean isSetSpatialDimensions() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.SPATIAL_DIMENSIONS)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Double getSpatialDimensions() {
		if (isSetSpatialDimensions()) {
			return (Double) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.SPATIAL_DIMENSIONS).toString());
		} else {
			return null;
		}
	}

	public void setSize(Double size) {
		if (!size.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.SIZE).toString(),
					Double.toString(size));
		}
	}

	public Boolean isSetSize() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.SIZE).toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Double getSize() {
		if (isSetSize()) {
			return Double.parseDouble((String) attWriter.getAttribute(g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.SIZE).toString()));
		} else {
			return null;
		}
	}

	public void setUnits(String units) {
		if (!units.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.UNITS).toString(),
					units);
		}
	}

	public Boolean isSetUnits() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.UNITS).toString())) {
			return true;
		} else {
			return false;
		}
	}

	public String getUnits() {
		if (isSetUnits()) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.UNITS).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}

	public void setConstant(Boolean constant) {
		if (!constant.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.CONSTANT).toString(),
					constant);
		}
	}

	public Boolean isSetConstant() {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.CONSTANT).toString())) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean getConstant() {
		if (isSetConstant()) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.CONSTANT).toString());
		} else {
			return null;
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

	public void setOutside(String outside) {
		if (!outside.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.OUTSIDE).toString(),
					outside);
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

		discardedRowIDs.add(new StringBuffer(internHeadline).append(
				SBML_Constants.NOTES).toString());
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

	/**
	 * Sets the nice id
	 */
	private void initCompartmentNideIDs() {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.COMPARTMENT_ID).toString(),
				presentedHeadline + ": ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.COMPARTMENT_NAME).toString(),
				presentedHeadline + ": Name");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.SPATIAL_DIMENSIONS).toString(),
				presentedHeadline + ": Spatial Dimensions");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SIZE)
						.toString(), presentedHeadline + ": Size");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.UNITS)
						.toString(), presentedHeadline + ": Units");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.CONSTANT).toString(),
				presentedHeadline + ": Constant");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.OUTSIDE)
						.toString(), presentedHeadline + ": Outside");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
	}

}

/*
 * if(AttributeHelper.hasAttribute(g, internHeadline, new
 * StringBuffer(internHeadline
 * ).append(SBML_Constants.SPATIAL_DIMENSIONS).toString())){ Object spDim =
 * (Object)getAttribute(g, internHeadline, new
 * StringBuffer(internHeadline).append
 * (SBML_Constants.SPATIAL_DIMENSIONS).toString()); if(spDim instanceof Double){
 * compartment.setSpatialDimensions((Double)spDim); } }
 */
/*
 * if(AttributeHelper.hasAttribute(g, internHeadline,new
 * StringBuffer(internHeadline).append(SBML_Constants.SIZE).toString())){ try{
 * Double size = Double.parseDouble((String)getAttribute(g, internHeadline,new
 * StringBuffer(internHeadline). append(SBML_Constants.SIZE).toString()));
 * compartment.setSize(size); }catch(NumberFormatException e){
 * 
 * } }
 */

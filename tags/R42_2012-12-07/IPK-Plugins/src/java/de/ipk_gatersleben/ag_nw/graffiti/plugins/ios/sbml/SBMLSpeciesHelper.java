package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.PositionGridGenerator;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

public class SBMLSpeciesHelper extends SBMLNodesNiceIdHelper {
	
	/**
	 * Intern graph object
	 */
	static Graph _g;
	
	/**
	 * Provides necessary methods
	 */
	SBML_SBase_Writer attWriter;
	
	/**
	 * Provides necessary methods
	 */
	SBML_SBase_Reader attReader;
	
	public static Map<String, Node> speciesMap;
	
	/**
	 * contains all SpeicesId and all nodes that belong to that species id
	 */
	private Map<String, List<Node>> _speicesClones;
	
	public Map<String, List<Node>> getSpeicesClones() {
		return _speicesClones;
	}
	
	public Node getSpeciesNode(String layoutId, String speciesId) {
		List<Node> nodes = _speicesClones.get(speciesId);
		for (Node speciesNode : nodes) {
			String speciesNodeLayoutId = (String) AttributeHelper.getAttributeValue(speciesNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, null, null);
			if (speciesNodeLayoutId != null && speciesNodeLayoutId.equals(layoutId)) {
				return speciesNode;
			}
		}
		return null;
	}
	
	/**
	 * Constructor. Initializes the graph
	 * 
	 * @param g
	 *        the graph where the information is read from
	 */
	public SBMLSpeciesHelper(Graph g) {
		_g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		if (speciesMap == null) {
			speciesMap = new HashMap<String, Node>();
		}
		_speicesClones = new HashMap<String, List<Node>>();
	}
	
	public void addCloneToList(String speciesId, Node speciesNode) {
		List<Node> speciesNodes = _speicesClones.get(speciesId);
		if (speciesNodes == null) {
			speciesNodes = new ArrayList<Node>();
		}
		speciesNodes.add(speciesNode);
		_speicesClones.put(speciesId, speciesNodes);
	}
	
	public String getCompartmentName(Node speciesNode) {
		String id = getCompartment(speciesNode);
		
		if (AttributeHelper.hasAttribute(_g, SBML_Constants.SBML_COMPARTMENT
				+ id, new StringBuffer(SBML_Constants.SBML_COMPARTMENT + id)
				.append("_id").toString())) {
			return (String) attWriter
					.getAttribute(
							_g,
							SBML_Constants.SBML_COMPARTMENT + id,
							new StringBuffer(SBML_Constants.SBML_COMPARTMENT
									+ id).append(
									SBML_Constants.COMPARTMENT_NAME).toString());
		}
		return null;
	}
	
	/**
	 * Returns all species nodes of the graph
	 * 
	 * @return a list of all species nodes of the graph
	 */
	public List<Node> getSpeciesNodes() {
		List<Node> speciesNodeList = new ArrayList<Node>();
		Iterator<Node> itNode = _g.getNodesIterator();
		while (itNode.hasNext()) {
			Node node = itNode.next();
			if (AttributeHelper.getSBMLrole(node).equals("species")) {
				speciesNodeList.add(node);
			}
		}
		return speciesNodeList;
	}
	
	/*
	 * public Collection<Node> getSpeciesNodes(){ return speciesMap.values(); }
	 */
	
	/**
	 * Returns the species with a distinct id
	 * 
	 * @param id
	 *        the id of the asked node
	 * @return the node with a certain id or null if no node has this id
	 */
	public static Node getSpeciesNode(String id) {
		return speciesMap.get(id);
	}
	
	/**
	 * Indicates if the compartment id of a species is set
	 * 
	 * @param node
	 *        the node where the information is read from
	 * @return true if the compartment id is set else false
	 */
	public Boolean isSetCompartment(Node speciesNode) {
		if (!NodeTools.getClusterID(speciesNode, SBML_Constants.EMPTY).equals(
				SBML_Constants.EMPTY)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the id of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if the id is set else false
	 */
	public Boolean isSetID(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the name of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if the name is set else false
	 */
	public Boolean isSetName(Node speciesNode) {
		if (!AttributeHelper.getLabel(speciesNode, SBML_Constants.EMPTY)
				.equals(SBML_Constants.EMPTY)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the initial amount of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if the initial amount is set else false
	 */
	public Boolean isSetInitialAmount(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.INITIAL_AMOUNT)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the initial concentration of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if the initial concentration is set else false
	 */
	public Boolean isSetInitialConcentration(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.INITIAL_CONCENTRATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if substance units of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if substance units is set else false
	 */
	public Boolean isSetSubstanceUnits(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_SUBSTANCE_UNITS)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if hasOnlySubstanceUnits of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if hasOnlySubstanceUnits is set else false
	 */
	public Boolean isSetHasOnlySubstanceUnits(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if boundary condition of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if boundary condition is set else false
	 */
	public Boolean isSetBoundaryCondition(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if constant of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if constant is set else false
	 */
	public Boolean isSetConstant(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_CONSTANT)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if conversion factor of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if conversion factor is set else false
	 */
	public Boolean isSetConversionFactor(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_CONVERSION_FACTOR)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the compartment id of a species
	 * 
	 * @param Node
	 *        the node where the information is read from
	 * @return the compartment id if it is set else the empty string
	 */
	public String getCompartment(Node speciesNode) {
		return NodeTools.getClusterID(speciesNode, SBML_Constants.EMPTY);
	}
	
	/**
	 * Returns the id of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return the species id if it is set else the empty string
	 */
	public String getID(Node speciesNode) {
		if (isSetID(speciesNode)) {
			return (String) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the name of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return the species name if it is set else the empty string
	 */
	public String getName(Node speciesNode) {
		return AttributeHelper.getLabel(speciesNode, SBML_Constants.EMPTY);
	}
	
	/**
	 * Returns the initial amount of a species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return initial amount if it is set else null
	 */
	public Double getInitialAmount(Node speciesNode) {
		if (isSetInitialAmount(speciesNode)) {
			return (Double) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.INITIAL_AMOUNT);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the initial concentration of a species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return initial concentration if it is set else null
	 */
	public Double getInitialConcentration(Node speciesNode) {
		if (isSetInitialConcentration(speciesNode)) {
			return (Double) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.INITIAL_CONCENTRATION);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the substance units of a species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return substance Units if it is set else the empty string
	 */
	public String getSubstanceUnits(Node speciesNode) {
		if (isSetSubstanceUnits(speciesNode)) {
			return (String) attWriter
					.getAttribute(speciesNode, SBML_Constants.SBML,
							SBML_Constants.SPECIES_SUBSTANCE_UNITS);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the boolean value hasOnlySubstanceUnits of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return a boolean value if the attribute hasOnlySubstanceUnits is set
	 *         else null
	 */
	public Boolean getHasOnlySubstanceUnits(Node speciesNode) {
		if (isSetHasOnlySubstanceUnits(speciesNode)) {
			return (Boolean) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML,
					SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the boolean value boundaryCondition of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return a boolean value if the attribute boundaryCondition is set else
	 *         null
	 */
	public Boolean getBoundaryCondition(Node speciesNode) {
		if (isSetBoundaryCondition(speciesNode)) {
			return (Boolean) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.BOUNDARY_CONDITION);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the boolean value constant of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return a boolean value if the attribute constant is set else null
	 */
	public Boolean getConstant(Node speciesNode) {
		if (isSetConstant(speciesNode)) {
			return (Boolean) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_CONSTANT);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the conversion factor units of a species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return conversion factor if it is set else null
	 */
	public String getConversionFactor(Node speciesNode) {
		if (isSetConversionFactor(speciesNode)) {
			return (String) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONVERSION_FACTOR);
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the id of a species node
	 * 
	 * @param speciesNode
	 *        where the information should be read in
	 * @param id
	 *        the id to set
	 */
	public void setID(Node speciesNode, String id) {
		if (!id.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_ID, id);
			speciesMap.put(id, speciesNode);
		}
	}
	
	/**
	 * Sets the label of a node. The id string will be the label if name is not
	 * set
	 * 
	 * @param speciesNode
	 *        where the information should be read in
	 * @param name
	 *        the name to set
	 * @param id
	 *        will be set if name is empty
	 * @param pgg
	 *        helps to set the position of the node
	 */
	public void setLabel(Node speciesNode, String name, String id,
			PositionGridGenerator pgg) {
		String label = null;
		if (!name.equals(SBML_Constants.EMPTY)) {
			label = name;
		} else {
			label = id;
		}
		if (!label.equals(SBML_Constants.EMPTY)) {
			attReader.setAttributes(speciesNode, Color.white, label,
					pgg.getNextPosition(), label.length() + 7);
		}
	}
	
	/**
	 * Sets the compartment of a node
	 * 
	 * @param speciesNode
	 *        the compartment belongs to this node
	 * @param compartment
	 *        the id of the compartment that will be set
	 */
	public void setCompartment(Node speciesNode, String compartment) {
		if (!compartment.equals(SBML_Constants.EMPTY)) {
			NodeTools.setClusterID(speciesNode, compartment);
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.COMPARTMENT, compartment);
		}
	}
	
	public void setCompartmentName(Node speciesNode) {
		String name = getCompartmentName(speciesNode);
		if (!SBML_Constants.EMPTY.equals(name)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_COMPARTMENT_NAME, name);
		}
	}
	
	/**
	 * Sets the initial amount of a node
	 * 
	 * @param speciesNode
	 *        the initial amount belongs to this node
	 * @param initialAmount
	 *        the value that will be set
	 */
	public void setInitialAmount(Node speciesNode, Double initialAmount) {
		if (!initialAmount.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.INITIAL_AMOUNT, initialAmount);
		}
	}
	
	/**
	 * Sets the initial concentration of a node
	 * 
	 * @param speciesNode
	 *        the initial concentration belongs to this node
	 * @param initialConcentration
	 *        the value that will be set
	 */
	public void setInitialConcentration(Node speciesNode,
			Double initialConcentration) {
		if (!initialConcentration.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.INITIAL_CONCENTRATION, initialConcentration);
		}
	}
	
	/**
	 * Sets the substance units of a species node
	 * 
	 * @param speciesNode
	 *        the substance units belong to this node
	 * @param substanceUnits
	 *        the substance units to set
	 */
	public void setSubstanceUnits(Node speciesNode, String substanceUnits) {
		if (!substanceUnits.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_SUBSTANCE_UNITS, substanceUnits);
		}
	}
	
	/**
	 * Sets the attribute hasOnlySubstanceUnits of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param hasOnlySubstanceUnits
	 *        the value that will be set
	 */
	public void setHasOnlySubstanceUnits(Node speciesNode,
			Boolean hasOnlySubstanceUnits) {
		if (!hasOnlySubstanceUnits.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS,
					hasOnlySubstanceUnits);
		}
	}
	
	/**
	 * Sets the attribute boundary condition of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param boundaryCondition
	 *        the value that will be set
	 */
	public void setBoundaryConsition(Node speciesNode, Boolean boundaryCondition) {
		if (!boundaryCondition.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.BOUNDARY_CONDITION, boundaryCondition);
		}
	}
	
	/**
	 * Sets the attribute constant of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param constant
	 *        the value that will be set
	 */
	public void setConstant(Node speciesNode, Boolean constant) {
		if (!constant.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONSTANT, constant);
		}
	}
	
	/**
	 * Sets the attribute conversion factor of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param conversionFactor
	 *        the value that will be set
	 */
	public void setConversionFactor(Node speciesNode, String conversionFactor) {
		if (!conversionFactor.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONVERSION_FACTOR, conversionFactor);
		}
	}
	
	/**
	 * Sets the attribute meta id of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param metaID
	 *        the value that will be set
	 */
	public void setMetaID(Node speciesNode, String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_META_ID, metaID);
		}
	}
	
	public Boolean isSetMetaID(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_META_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteMetaID(Node speciesNode) {
		if (isSetMetaID(speciesNode)) {
			AttributeHelper.deleteAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_META_ID);
		}
	}
	
	public String getMetaID(Node speciesNode) {
		if (isSetMetaID(speciesNode)) {
			return (String) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_META_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void setAnnotation(Node speciesNode, Annotation annotation) {
		AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_ANNOTATION, annotation);
	}
	
	public Boolean isSetAnnotation(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteAnnotation(Node speciesNode) {
		if (isSetAnnotation(speciesNode)) {
			AttributeHelper.deleteAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_ANNOTATION);
		}
	}
	
	public Annotation getAnnotation(Node speciesNode) {
		if (isSetAnnotation(speciesNode)) {
			return (Annotation) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_ANNOTATION);
		} else {
			return null;
		}
	}
	
	public void setNonRDFAnnotation(Node speciesNode, String nonRDFAnnotation) {
		AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_NON_RDF_ANNOTATION, nonRDFAnnotation);
	}
	
	public Boolean isSetNonRDFAnnotation(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_NON_RDF_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteNonRDFAnnotation(Node speciesNode) {
		if (isSetNonRDFAnnotation(speciesNode)) {
			AttributeHelper.deleteAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_NON_RDF_ANNOTATION);
		}
	}
	
	public Annotation getNonRDFAnnotation(Node speciesNode) {
		if (isSetNonRDFAnnotation(speciesNode)) {
			return (Annotation) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_NON_RDF_ANNOTATION);
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the attribute sboTerm of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param sboTerm
	 *        the value that will be set
	 */
	public void setSBOTerm(Node speciesNode, String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_SBOTERM, sboTerm);
		}
	}
	
	public Boolean isSetSBOTerm(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_SBOTERM)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteSBOTerm(Node speciesNode) {
		if (isSetSBOTerm(speciesNode)) {
			AttributeHelper.deleteAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_SBOTERM);
		}
	}
	
	public String getSBOTerm(Node speciesNode) {
		if (isSetSBOTerm(speciesNode)) {
			return (String) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_SBOTERM);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Sets the attribute notes of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param notes
	 *        the value that will be set
	 * @param notesObj
	 *        the Object that will be set
	 */
	public void setNotes(Node speciesNode, String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(notesObj, notes, speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_NOTES);
		}
	}
	
	public Boolean isSetNotes(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_NOTES)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteNotes(Node speciesNode) {
		if (isSetNotes(speciesNode)) {
			AttributeHelper.deleteAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_NOTES);
		}
	}
	
	public XMLNode getNotes(Node speciesNode) {
		if (isSetNotes(speciesNode)) {
			return (XMLNode) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_NOTES);
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the deprecated attribute charge of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param charge
	 *        the value that will be set
	 */
	public void setCharge(Node speciesNode, Integer charge) {
		if (!charge.equals(0)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.CHARGE, charge);
		}
	}
	
	/**
	 * Sets the hidden label of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param id
	 *        the value that will be set
	 */
	public void setHiddenLabel(Node speciesNode, String id) {
		AttributeHelper.setLabel(AttributeHelper.getLabels(speciesNode).size(),
				speciesNode, id, null, AlignmentSetting.HIDDEN.toGMLstring());
	}
	
}
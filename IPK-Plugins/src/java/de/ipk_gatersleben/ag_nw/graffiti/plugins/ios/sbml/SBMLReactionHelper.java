package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.AttributeHelper;
import org.PositionGridGenerator;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

public class SBMLReactionHelper {
	
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
	 * contains all reaction ids and all nodes that belong to that reaction id
	 */
	private Map<String, List<Node>> _reactionClones;
	
	/**
	 * the array contains the reaction id and the species id which is an reactant in this reaction
	 */
	private Map<ReactionIDSpeciesIDWrapper, List<Edge>> _reactantClones;
	
	private Map<ReactionIDSpeciesIDWrapper, List<Edge>> _productClones;
	
	private Map<ReactionIDSpeciesIDWrapper, List<Edge>> _modifierClones;
	
	/**
	 * Constructor. Initializes the graph
	 * 
	 * @param g
	 *        the graph where the information is read from
	 */
	public SBMLReactionHelper(Graph g) {
		this.g = g;
		_reactionClones = new HashMap<String, List<Node>>();
		_reactantClones = new HashMap<ReactionIDSpeciesIDWrapper, List<Edge>>();
		_productClones = new HashMap<ReactionIDSpeciesIDWrapper, List<Edge>>();
		_modifierClones = new HashMap<ReactionIDSpeciesIDWrapper, List<Edge>>();
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		
		initReactionNideIDs(SBML_Constants.SBML, SBML_Constants.SBML_HEADLINE);
	}
	
	public Map<String, List<Node>> getReactionClones() {
		return _reactionClones;
	}
	
	public Map<ReactionIDSpeciesIDWrapper, List<Edge>> getReactantClones() {
		return _reactantClones;
	}
	
	public Map<ReactionIDSpeciesIDWrapper, List<Edge>> getProductClones() {
		return _productClones;
	}
	
	public Map<ReactionIDSpeciesIDWrapper, List<Edge>> getModifierClones() {
		return _modifierClones;
	}
	
	public Node getReactionNode(String layoutId, String reactionId) {
		List<Node> nodes = _reactionClones.get(reactionId);
		for (Node reactionsNode : nodes) {
			String reactionNodeLayoutId = (String) AttributeHelper
					.getAttributeValue(reactionsNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, null, null);
			if (reactionNodeLayoutId != null && reactionNodeLayoutId.equals(layoutId)) {
				return reactionsNode;
			}
		}
		return null;
	}
	
	public void addReactionCloneToList(String reactionId, Node reactionNode) {
		List<Node> reactionNodes = _reactionClones.get(reactionId);
		if (reactionNodes == null) {
			reactionNodes = new ArrayList<Node>();
		}
		reactionNodes.add(reactionNode);
		_reactionClones.put(reactionId, reactionNodes);
	}
	
	public void addReactantCloneToList(String reactionId, String speciesId, Edge reactantEdge) {
		ReactionIDSpeciesIDWrapper reactionSpeciesPair = new ReactionIDSpeciesIDWrapper(reactionId, speciesId);
		List<Edge> edgeList = _reactantClones.get(reactionSpeciesPair);
		if (edgeList == null) {
			edgeList = new ArrayList<Edge>();
		}
		edgeList.add(reactantEdge);
		_reactantClones.put(reactionSpeciesPair, edgeList);
	}
	
	public void addProductCloneToList(String reactionId, String speciesId, Edge reactantEdge) {
		ReactionIDSpeciesIDWrapper reactionSpeciesPair = new ReactionIDSpeciesIDWrapper(reactionId, speciesId);
		List<Edge> edgeList = _productClones.get(reactionSpeciesPair);
		if (edgeList == null) {
			edgeList = new ArrayList<Edge>();
		}
		edgeList.add(reactantEdge);
		_productClones.put(reactionSpeciesPair, edgeList);
	}
	
	public void addModifierCloneToList(String reactionId, String speciesId, Edge reactantEdge) {
		ReactionIDSpeciesIDWrapper reactionSpeciesPair = new ReactionIDSpeciesIDWrapper(reactionId, speciesId);
		List<Edge> edgeList = _modifierClones.get(reactionSpeciesPair);
		if (edgeList == null) {
			edgeList = new ArrayList<Edge>();
		}
		edgeList.add(reactantEdge);
		_modifierClones.put(reactionSpeciesPair, edgeList);
	}
	
	public Boolean isSetCompartment(Node reactionNode) {
		if (!NodeTools.getClusterID(reactionNode, SBML_Constants.EMPTY).equals(
				SBML_Constants.EMPTY)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetFast(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.FAST)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetReversible(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REVERSIBLE)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetName(Node reactionNode) {
		if (!AttributeHelper.getLabel(reactionNode, SBML_Constants.EMPTY)
				.equals(SBML_Constants.EMPTY)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetID(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getID(Node reactionNode) {
		if (isSetID(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REACTION_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public String getName(Node reactionNode) {
		return AttributeHelper.getLabel(reactionNode, SBML_Constants.EMPTY);
	}
	
	public Boolean getReversible(Node reactionNode) {
		if (isSetReversible(reactionNode)) {
			return (Boolean) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REVERSIBLE);
		} else {
			return null;
		}
	}
	
	public Boolean getFast(Node reactionNode) {
		if (isSetFast(reactionNode)) {
			return (Boolean) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.FAST);
		} else {
			return null;
		}
	}
	
	public String getCompartment(Node reactionNode) {
		return NodeTools.getClusterID(reactionNode, SBML_Constants.EMPTY);
	}
	
	public void setID(Node reactionNode, String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.REACTION_ID, ID);
		}
	}
	
	public void setLabel(Node reactionNode, String name, String id,
			PositionGridGenerator pgg) {
		String label = null;
		if (!name.equals(SBML_Constants.EMPTY)) {
			label = name;
		} else {
			label = id;
		}
		if (!label.equals(SBML_Constants.EMPTY)) {
			attReader.setAttributes(reactionNode, Color.white, label,
					pgg.getNextPosition(), 7);
		}
	}
	
	public void setReversible(Node reactionNode, Boolean reversible) {
		if (!reversible.equals(null)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.REVERSIBLE, reversible);
		}
	}
	
	public void setFast(Node reactionNode, Boolean fast) {
		if (!fast.equals(null)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.FAST, fast);
		}
	}
	
	public void setCompartment(Node reactionNode, String compartment) {
		if (!compartment.equals(SBML_Constants.EMPTY)) {
			NodeTools.setClusterID(reactionNode, compartment);
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.COMPARTMENT, compartment);
		}
	}
	
	public void setMetaID(Node reactionNode, String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.REACTION_META_ID, metaID);
		}
	}
	
	public Boolean isSetMetaID(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_META_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteMetaID(Node reactinNode) {
		if (isSetMetaID(reactinNode)) {
			AttributeHelper.deleteAttribute(reactinNode, SBML_Constants.SBML, SBML_Constants.REACTION_META_ID);
		}
	}
	
	public String getMetaID(Node reactionNode) {
		if (isSetMetaID(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REACTION_META_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteSBOTerm(Node reactinNode) {
		if (isSetSBOTerm(reactinNode)) {
			AttributeHelper.deleteAttribute(reactinNode, SBML_Constants.SBML, SBML_Constants.REACTION_SBOTERM);
		}
	}
	
	public void setSBOTerm(Node reactionNode, String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.REACTION_SBOTERM, sboTerm);
		}
	}
	
	public Boolean isSetSBOTerm(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_SBOTERM)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getSBOTerm(Node reactionNode) {
		if (isSetSBOTerm(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REACTION_SBOTERM);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void setAnnotation(Node reactionNode, Annotation annotation) {
		AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_ANNOTATION, annotation);
	}
	
	public Annotation getAnnotation(Node reactionNode) {
		if (isSetAnnotation(reactionNode)) {
			return (Annotation) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REACTION_ANNOTATION);
		} else {
			return null;
		}
	}
	
	public void deleteAnnotation(Node reactinNode) {
		if (isSetAnnotation(reactinNode)) {
			AttributeHelper.deleteAttribute(reactinNode, SBML_Constants.SBML, SBML_Constants.REACTION_ANNOTATION);
		}
	}
	
	public Boolean isSetAnnotation(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setNonRDFAnnotation(Node reactionNode, String nonRDFAnnotation) {
		AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_NON_RDF_ANNOTATION, nonRDFAnnotation);
	}
	
	public Annotation getNonRDFAnnotation(Node reactionNode) {
		if (isSetNonRDFAnnotation(reactionNode)) {
			return (Annotation) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REACTION_NON_RDF_ANNOTATION);
		} else {
			return null;
		}
	}
	
	public void deleteNonRDFAnnotation(Node reactinNode) {
		if (isSetNonRDFAnnotation(reactinNode)) {
			AttributeHelper.deleteAttribute(reactinNode, SBML_Constants.SBML, SBML_Constants.REACTION_NON_RDF_ANNOTATION);
		}
	}
	
	public Boolean isSetNonRDFAnnotation(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_NON_RDF_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setNotes(Node reactionNode, String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(notesObj, notes, reactionNode,
					SBML_Constants.SBML, SBML_Constants.REACTION_NOTES);
		}
	}
	
	public XMLNode getNotes(Node reactionNode) {
		if (isSetNotes(reactionNode)) {
			return (XMLNode) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REACTION_NOTES);
		} else {
			return null;
		}
	}
	
	public Boolean isSetNotes(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_NOTES)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteNotes(Node reactinNode) {
		if (isSetNotes(reactinNode)) {
			AttributeHelper.deleteAttribute(reactinNode, SBML_Constants.SBML, SBML_Constants.REACTION_NOTES);
		}
	}
	
	public void setSpecies(Edge reactionEdge, String species) {
		if (!species.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.SPECIES, species);
		}
	}
	
	public void setID(Edge reactionEdge, String ID) {
		if (!reactionEdge.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.SPECIES_REFERENCE_ID, ID);
		}
	}
	
	public void setName(Edge reactionEdge, String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.SPECIES_REFERENCE_NAME, name);
		}
	}
	
	public Boolean isSetSpecies(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.SPECIES)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetID(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.SPECIES_REFERENCE_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetName(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.SPECIES_REFERENCE_NAME)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getSpecies(Edge reactionEdge) {
		if (isSetSpecies(reactionEdge)) {
			return (String) attWriter.getAttribute(reactionEdge,
					SBML_Constants.SBML, SBML_Constants.SPECIES);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public String getName(Edge reactionEdge) {
		if (isSetName(reactionEdge)) {
			return (String) attWriter.getAttribute(reactionEdge,
					SBML_Constants.SBML, SBML_Constants.SPECIES_REFERENCE_NAME);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public String getID(Edge reactionEdge) {
		if (isSetID(reactionEdge)) {
			return (String) attWriter.getAttribute(reactionEdge,
					SBML_Constants.SBML, SBML_Constants.SPECIES_REFERENCE_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void setMetaIDReactant(Edge reactionEdge, String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.REACTANT_META_ID, metaID);
		}
	}
	
	public String getMetaIDReactant(Edge edge) {
		if (isSetMetaIDReactant(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.REACTANT_META_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteMetaIDReactant(Edge edge) {
		if (isSetMetaIDReactant(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.REACTANT_META_ID);
		}
	}
	
	public Boolean isSetMetaIDReactant(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.REACTANT_META_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setMetaIDProduct(Edge reactionEdge, String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.PRODUCT_META_ID, metaID);
		}
	}
	
	public String getMetaIDProduct(Edge edge) {
		if (isSetMetaIDProduct(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.PRODUCT_META_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteMetaIDProduct(Edge edge) {
		if (isSetMetaIDProduct(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.PRODUCT_META_ID);
		}
	}
	
	public Boolean isSetMetaIDProduct(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.PRODUCT_META_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setMetaIDModifier(Edge reactionEdge, String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.MODIFIER_META_ID, metaID);
		}
	}
	
	public String getMetaIDModifier(Edge edge) {
		if (isSetMetaIDModifier(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.MODIFIER_META_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteMetaIDModifier(Edge edge) {
		if (isSetMetaIDModifier(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.MODIFIER_META_ID);
		}
	}
	
	public Boolean isSetMetaIDModifier(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.MODIFIER_META_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setSBOTermReactant(Edge reactionEdge, String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.REACTANT_SBOTERM, sboTerm);
		}
	}
	
	public String getSBOTermReactant(Edge edge) {
		if (isSetSBOTermReactant(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.REACTANT_SBOTERM);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteSBOTermReactant(Edge edge) {
		if (isSetSBOTermReactant(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.REACTANT_SBOTERM);
		}
	}
	
	public Boolean isSetSBOTermReactant(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.REACTANT_SBOTERM)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setSBOTermProduct(Edge reactionEdge, String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.PRODUCT_SBOTERM, sboTerm);
		}
	}
	
	public String getSBOTermProduct(Edge edge) {
		if (isSetSBOTermProduct(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.PRODUCT_SBOTERM);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteSBOTermProduct(Edge edge) {
		if (isSetSBOTermProduct(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.PRODUCT_SBOTERM);
		}
	}
	
	public Boolean isSetSBOTermProduct(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.PRODUCT_SBOTERM)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setSBOTermModifier(Edge reactionEdge, String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.MODIFIER_SBOTERM, sboTerm);
		}
	}
	
	public String getSBOTermModifier(Edge edge) {
		if (isSetSBOTermModifier(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.MODIFIER_SBOTERM);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteSBOTermModifier(Edge edge) {
		if (isSetSBOTermModifier(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.MODIFIER_SBOTERM);
		}
	}
	
	public Boolean isSetSBOTermModifier(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.MODIFIER_SBOTERM)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setAnnotationProduct(Edge reactionEdge, Annotation annotation) {
		AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.PRODUCT_ANNOTATION, annotation);
	}
	
	public Annotation getAnnotationProduct(Edge edge) {
		if (isSetAnnotationProduct(edge)) {
			return (Annotation) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.PRODUCT_ANNOTATION);
		} else {
			return null;
		}
	}
	
	public void deleteAnnotationProduct(Edge edge) {
		if (isSetAnnotationProduct(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.PRODUCT_ANNOTATION);
		}
	}
	
	public Boolean isSetAnnotationProduct(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.PRODUCT_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setNonRDFAnnotationProduct(Edge reactionEdge,
			String nonRDFAnnotation) {
		AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.PRODUCT_NON_RDF_ANNOTATION, nonRDFAnnotation);
	}
	
	public String getNonRDFAnnotationProduct(Edge edge) {
		if (isSetAnnotationProduct(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.PRODUCT_NON_RDF_ANNOTATION);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteNonRDFAnnotationProduct(Edge edge) {
		if (isSetNonRDFAnnotationProduct(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.PRODUCT_NON_RDF_ANNOTATION);
		}
	}
	
	public Boolean isSetNonRDFAnnotationProduct(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.PRODUCT_NON_RDF_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setAnnotationModifier(Edge reactionEdge, Annotation annotation) {
		AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.MODIFIER_ANNOTATION, annotation);
	}
	
	public Annotation getAnnotationModifier(Edge edge) {
		if (isSetAnnotationModifier(edge)) {
			return (Annotation) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.MODIFIER_ANNOTATION);
		} else {
			return null;
		}
	}
	
	public void deleteAnnotationModifier(Edge edge) {
		if (isSetAnnotationModifier(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.MODIFIER_ANNOTATION);
		}
	}
	
	public Boolean isSetAnnotationModifier(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.MODIFIER_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setNonRDFAnnotationModifier(Edge reactionEdge,
			String nonRDFAnnotation) {
		AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.MODIFIER_NON_RDF_ANNOTATION, nonRDFAnnotation);
	}
	
	public String getNonRDFAnnotationModifier(Edge edge) {
		if (isSetAnnotationModifier(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.MODIFIER_NON_RDF_ANNOTATION);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteNonRDFAnnotationModifier(Edge edge) {
		if (isSetNonRDFAnnotationModifier(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.MODIFIER_NON_RDF_ANNOTATION);
		}
	}
	
	public Boolean isSetNonRDFAnnotationModifier(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.MODIFIER_NON_RDF_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setAnnotationReactant(Edge reactionEdge, Annotation annotation) {
		AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.REACTANT_ANNOTATION, annotation);
	}
	
	public Annotation getAnnotationReactant(Edge edge) {
		if (isSetAnnotationReactant(edge)) {
			return (Annotation) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.REACTANT_ANNOTATION);
		} else {
			return null;
		}
	}
	
	public void deleteAnnotationReactant(Edge edge) {
		if (isSetAnnotationReactant(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.REACTANT_ANNOTATION);
		}
	}
	
	public Boolean isSetAnnotationReactant(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.REACTANT_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setNonRDFAnnotationReactant(Edge reactionEdge,
			String nonRDFAnnotation) {
		AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.REACTANT_NON_RDF_ANNOTATION, nonRDFAnnotation);
	}
	
	public String getNonRDFAnnotationReactant(Edge edge) {
		if (isSetAnnotationReactant(edge)) {
			return (String) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.REACTANT_NON_RDF_ANNOTATION);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteNonRDFAnnotationReactant(Edge edge) {
		if (isSetNonRDFAnnotationReactant(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.REACTANT_NON_RDF_ANNOTATION);
		}
	}
	
	public Boolean isSetNonRDFAnnotationReactant(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.REACTANT_NON_RDF_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setNotesReactant(Edge reactionEdge, String notes,
			XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(notesObj, notes, reactionEdge,
					SBML_Constants.SBML, SBML_Constants.REACTANT_NOTES);
		}
	}
	
	public Object getNotesReactant(Edge edge) {
		if (isSetNotesReactant(edge)) {
			return (Object) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.REACTANT_NOTES);
		} else {
			return null;
		}
	}
	
	public void deleteNotesReactant(Edge edge) {
		if (isSetNotesReactant(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.REACTANT_NOTES);
		}
	}
	
	public Boolean isSetNotesReactant(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.REACTANT_NOTES)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setNotesProduct(Edge reactionEdge, String notes,
			XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(notesObj, notes, reactionEdge,
					SBML_Constants.SBML, SBML_Constants.PRODUCT_NOTES);
		}
	}
	
	public Object getNotesProduct(Edge edge) {
		if (isSetNotesProduct(edge)) {
			return (Object) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.PRODUCT_NOTES);
		} else {
			return null;
		}
	}
	
	public void deleteNotesProduct(Edge edge) {
		if (isSetNotesProduct(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.PRODUCT_NOTES);
		}
	}
	
	public Boolean isSetNotesProduct(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.PRODUCT_NOTES)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setNotesModifier(Edge reactionEdge, String notes,
			XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(notesObj, notes, reactionEdge,
					SBML_Constants.SBML, SBML_Constants.MODIFIER_NOTES);
		}
	}
	
	public Object getNotesModifier(Edge edge) {
		if (isSetNotesModifier(edge)) {
			return (Object) attWriter.getAttribute(edge,
					SBML_Constants.SBML, SBML_Constants.MODIFIER_NOTES);
		} else {
			return null;
		}
	}
	
	public void deleteNotesModifier(Edge edge) {
		if (isSetNotesModifier(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML, SBML_Constants.MODIFIER_NOTES);
		}
	}
	
	public Boolean isSetNotesModifier(Edge edge) {
		if (AttributeHelper.hasAttribute(edge, SBML_Constants.SBML,
				SBML_Constants.MODIFIER_NOTES)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetStoichiometry(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.STOICHIOMETRY)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isSetConstant(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.REACTION_CONSTANT)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getStoichiometry(Edge reactionEdge) {
		if (!AttributeHelper.getSBMLrole(reactionEdge).equals(
				SBML_Constants.ROLE_MODIFIER)) {
			if (isSetStoichiometry(reactionEdge)) {
				return (String) attWriter.getAttribute(reactionEdge,
						SBML_Constants.SBML, SBML_Constants.STOICHIOMETRY);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public Boolean getConstant(Edge reactionEdge) {
		if (!AttributeHelper.getAttributeValue(reactionEdge,
				SBML_Constants.SBML, SBML_Constants.SBML_ROLE,
				SBML_Constants.EMPTY, SBML_Constants.EMPTY).equals(
				SBML_Constants.ROLE_MODIFIER)) {
			if (isSetConstant(reactionEdge)) {
				return (Boolean) attWriter.getAttribute(reactionEdge,
						SBML_Constants.SBML, SBML_Constants.REACTION_CONSTANT);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public void setStoichiometry(Edge reactionEdge, Double stoichiometry) {
		if (!AttributeHelper.getAttributeValue(reactionEdge,
				SBML_Constants.SBML, SBML_Constants.SBML_ROLE,
				SBML_Constants.EMPTY, SBML_Constants.EMPTY).equals(
				SBML_Constants.ROLE_MODIFIER)) {
			if (!stoichiometry.equals(null)) {
				AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
						SBML_Constants.STOICHIOMETRY, stoichiometry);
			}
		}
	}
	
	public void setConstant(Edge reactionEdge, Boolean constant) {
		if (!AttributeHelper.getAttributeValue(reactionEdge,
				SBML_Constants.SBML, SBML_Constants.SBML_ROLE,
				SBML_Constants.EMPTY, SBML_Constants.EMPTY).equals(
				SBML_Constants.ROLE_MODIFIER)) {
			if (!constant.equals(null)) {
				AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
						SBML_Constants.REACTION_CONSTANT, constant);
			}
		}
	}
	
	private void initReactionNideIDs(String internHeadline,
			String presentedHeadline) {
		// AttributeHelper.setNiceId(SBML_Constants.REACTION_NAME,
		// presentedHeadline+": Name");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_ID, presentedHeadline
				+ ": ID");
		AttributeHelper.setNiceId(SBML_Constants.REVERSIBLE, presentedHeadline
				+ ": Reversible");
		AttributeHelper.setNiceId(SBML_Constants.FAST, presentedHeadline
				+ ": Fast");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_COMPARTMENT,
				presentedHeadline + ": Compartment ID");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_META_ID,
				presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_SBOTERM,
				presentedHeadline + ": SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_NOTES,
				presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.STOICHIOMETRY,
				presentedHeadline + ": Stoichiometry");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_CONSTANT,
				presentedHeadline + ": Constant");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES, presentedHeadline
				+ ": Species");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_REFERENCE_NAME,
				presentedHeadline + ": Species Reference Name");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_REFERENCE_ID,
				presentedHeadline + ": Species Reference ID");
		AttributeHelper.setNiceId(SBML_Constants.PRODUCT_META_ID,
				presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.PRODUCT_NOTES,
				presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.PRODUCT_SBOTERM,
				presentedHeadline + ": SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.REACTANT_META_ID,
				presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.REACTANT_NOTES,
				presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.REACTANT_SBOTERM,
				presentedHeadline + ": SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.MODIFIER_META_ID,
				presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.MODIFIER_NOTES,
				presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.MODIFIER_SBOTERM,
				presentedHeadline + ": SBOTerm");
	}
	
}

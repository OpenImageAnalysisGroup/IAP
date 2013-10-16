/**
 * This class reads in the Model tag
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.AttributeHelper;
import org.PositionGridGenerator;
import org.apache.log4j.Logger;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLReactionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class SBML_Model_Reader extends SBML_SBase_Reader {
	
	static Logger logger = Logger.getLogger(SBML_Model_Reader.class);
	
	/**
	 * Passes the import on to other classes
	 * 
	 * @param document
	 *        contains the model for the import
	 * @param g
	 *        the data structure for reading in the information
	 */
	public void controlImport(SBMLDocument document, Graph g,
			BackgroundTaskStatusProviderSupportingExternalCallImpl status) {
		if (document.isSetModel()) {
			
			Model model = document.getModel();
			String modelID = "";
			if (model.isSetId()) {
				if (Model.isValidId(model.getId(), document.getLevel(),
						document.getVersion())) {
					modelID = model.getId();
				}
			}
			PositionGridGenerator pgg = new PositionGridGenerator(100, 100,
					1000);
			
			addModel(model, g);
			
			if (model.isSetListOfFunctionDefinitions()) {
				SBML_FunctionDefinition_Reader readFunctionDef = new SBML_FunctionDefinition_Reader();
				readFunctionDef.addFunktionDefinition(
						model.getListOfFunctionDefinitions(), g);
			}
			
			if (model.isSetListOfUnitDefinitions()) {
				SBML_UnitDefinition_Reader readUnitDef = new SBML_UnitDefinition_Reader();
				readUnitDef.addUnitDefinitions(
						model.getListOfUnitDefinitions(), g);
			}
			
			if (model.isSetListOfCompartments()) {
				SBML_Compartment_Reader readCompartment = new SBML_Compartment_Reader();
				readCompartment
						.addCompartment(model.getListOfCompartments(), g);
			}
			status.setCurrentStatusText1("create species list");
			status.setCurrentStatusValue(40);
			
			SBMLSpeciesHelper speciesHelper = null;
			if (model.isSetListOfSpecies()) {
				speciesHelper = new SBMLSpeciesHelper(g);
				SBML_Species_Reader readSpecies = new SBML_Species_Reader();
				readSpecies.addSpecies(g, model.getListOfSpecies(), pgg, speciesHelper);
			}
			if (model.isSetListOfParameters()) {
				SBML_Parameter_Reader readParameter = new SBML_Parameter_Reader();
				readParameter.addParameter(model.getListOfParameters(), g);
			}
			if (model.isSetListOfInitialAssignments()) {
				SBML_InitialAssignment_Reader readInitialAssignment = new SBML_InitialAssignment_Reader();
				readInitialAssignment.addInitialAssignments(
						model.getListOfInitialAssignments(), g);
			}
			
			if (model.isSetListOfRules()) {
				SBML_Rule_Reader readRule = new SBML_Rule_Reader();
				readRule.addRule(model.getListOfRules(), g);
			}
			
			if (model.isSetListOfConstraints()) {
				SBML_Constraint_Reader readConstraint = new SBML_Constraint_Reader();
				readConstraint.addConstraint(model.getListOfConstraints(), g);
			}
			
			status.setCurrentStatusText1("create reaction list");
			status.setCurrentStatusValue(80);
			
			SBMLReactionHelper reactionHelper = null;;
			if (model.isSetListOfReactions()) {
				reactionHelper = new SBMLReactionHelper(g);
				SBML_Reaction_Reader readReaction = new SBML_Reaction_Reader();
				readReaction.addReactions(g, model.getListOfReactions(),
						modelID, pgg, reactionHelper);
			}
			
			if (model.isSetListOfEvents()) {
				SBML_Event_Reader readEvent = new SBML_Event_Reader();
				readEvent.addEvents(model.getListOfEvents(), g);
			}
			
			// sets background-color for compartments
			AttributeHelper.setAttribute(
					g,
					"",
					ClusterColorAttribute.attributeName,
					ClusterColorAttribute.getDefaultValue(document.getModel()
							.getListOfCompartments().size()));
			AttributeHelper.setAttribute(g, "", "background_coloring",
					new Boolean(true));
			
			ListOf<Compartment> liste = model.getListOfCompartments();
			for (Compartment compartment : liste) {
				if (!compartment.isSetName()) {
					AttributeHelper.deleteAttribute(
							g,
							new StringBuffer(SBML_Constants.SBML_COMPARTMENT)
									.append(compartment.getId()).toString(),
							new StringBuffer(SBML_Constants.SBML_COMPARTMENT)
									.append(compartment.getId())
									.append(SBML_Constants.COMPARTMENT_NAME)
									.toString());
				}
			}
			if (SBML_Constants.isLayoutActive) {
				addAdditionalEdges(g, model, speciesHelper, reactionHelper);
				computeReactionNodePosition(g);
			}
		}
	}
	
	private List<Reaction> getListOfReactions(String speciesID, Model model) {
		List<Reaction> reactionList = model.getListOfReactions();
		List<Reaction> newReactionList = new ArrayList<Reaction>();
		for (Reaction reaction : reactionList) {
			if (reaction.hasReactant(model.getSpecies(speciesID)) || reaction.hasProduct(model.getSpecies(speciesID))
					|| reaction.hasModifier(model.getSpecies(speciesID))) {
				newReactionList.add(reaction);
			}
		}
		return newReactionList;
	}
	
	private List<Point2D> getPointList(List<Node> nodeList) {
		List<Point2D> pointList = new ArrayList<Point2D>();
		for (Node node : nodeList) {
			NodeHelper nodeHelper = new NodeHelper(node);
			pointList.add(nodeHelper.getPosition());
		}
		return pointList;
	}
	
	private Edge getEdge(Node reactionNode, String speciesID) {
		Node speciesNode = null;
		Iterator<Node> itNeighbors = reactionNode.getNeighborsIterator();
		while (itNeighbors.hasNext()) {
			Node neighbor = itNeighbors.next();
			if (SBMLHelper.getSpeciesID(neighbor).equals(speciesID)) {
				speciesNode = neighbor;
			}
		}
		Iterator<Edge> edgeIt = reactionNode.getEdgesIterator();
		while (edgeIt.hasNext()) {
			Edge edge = edgeIt.next();
			if ((edge.getSource() == speciesNode && edge.getTarget() == reactionNode) || (edge.getSource() == reactionNode && edge.getTarget() == speciesNode)) {
				return edge;
			}
		}
		return null;
	}
	
	private void addAdditionalEdges(Graph g, Model model, SBMLSpeciesHelper speciesHelper, SBMLReactionHelper reactionHelper) {
		Map<String, List<Node>> speciesCloneList = speciesHelper.getSpeicesClones();
		Set<Entry<String, List<Node>>> speciesClonesEntrySet = speciesCloneList.entrySet();
		Iterator<Entry<String, List<Node>>> speciesClonesEntrySetIt = speciesClonesEntrySet.iterator();
		while (speciesClonesEntrySetIt.hasNext()) {
			Entry<String, List<Node>> speciesCloneEntry = speciesClonesEntrySetIt.next();
			if (speciesCloneEntry.getValue().size() > 1) {
				List<Reaction> reactionList = getListOfReactions(speciesCloneEntry.getKey(), model);
				if (reactionList.size() > 1) {
					
					Node species = null;
					for (Node node : speciesCloneEntry.getValue()) {
						if (node.getEdges().size() > 0) {
							species = node;
						}
						break;
					}
					
					for (Reaction reaction : reactionList) {
						Node reactionNode = SBMLHelper.getReactionNode(g, reaction.getId());
						NodeHelper reactionNodeHelper = new NodeHelper(reactionNode);
						Point2D positionReaction = reactionNodeHelper.getPosition();
						Double min = Double.POSITIVE_INFINITY;
						Node minSpeciesNode = null;
						for (Node speciesNode : speciesCloneEntry.getValue()) {
							NodeHelper speciesNodeHelper = new NodeHelper(speciesNode);
							Point2D speciesPosition = speciesNodeHelper.getPosition();
							Double difference = positionReaction.distance(speciesPosition);
							if (difference < min) {
								min = difference;
								minSpeciesNode = speciesNode;
							}
						}
						Edge edge = getEdge(reactionNode, speciesCloneEntry.getKey());
						if (AttributeHelper.getSBMLrole(edge.getSource()).equals(SBML_Constants.SPECIES)) {
							edge.setSource(minSpeciesNode);
						}
						else if (AttributeHelper.getSBMLrole(edge.getTarget()).equals(SBML_Constants.SPECIES)) {
							edge.setTarget(minSpeciesNode);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Reads in the model
	 * 
	 * @param model
	 *        contains the model for the import
	 * @param g
	 *        the data structure for reading in the information
	 */
	private void addModel(Model model, Graph g) {
		String modelID = model.getId();
		String modelName = model.getName();
		String substanceUnits = model.getSubstanceUnits();
		String timeUnits = model.getTimeUnits();
		String volumeUnits = model.getVolumeUnits();
		String areaUnits = model.getAreaUnits();
		String lenghtUnits = model.getLengthUnits();
		String extendUnits = model.getExtentUnits();
		String conversionFactor = model.getConversionFactor();
		String metaID = model.getMetaId();
		String sboTerm = model.getSBOTermID();
		
		if (model.isSetNotes()) {
			addNotes(model.getNotes(), model.getNotesString(), g,
					SBML_Constants.SBML, SBML_Constants.MODEL_NOTES);
		}
		if (model.isSetId()) {
			if (!modelID.equals(SBML_Constants.EMPTY)) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.MODEL_ID, modelID);
			}
		}
		if (!modelName.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, SBML_Constants.SBML,
					SBML_Constants.MODEL_NAME, modelName);
		}
		
		if (model.getLevel() == 3 && model.getVersion() == 1) {
			if (model.isSetSubstanceUnits()) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.SUBSTANCE_UNITS, substanceUnits);
			}
			if (model.isSetTimeUnits()) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.TIME_UNITS, timeUnits);
			}
			if (model.isSetVolumeUnits()) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.VOLUME_UNITS, volumeUnits);
			}
			if (!areaUnits.equals(SBML_Constants.EMPTY)) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.AREA_UNITS, areaUnits);
			}
			if (model.isSetLengthUnits()) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.LENGTH_UNITS, lenghtUnits);
			}
			if (model.isSetExtentUnits()) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.EXTENT_UNITS, extendUnits);
			}
			if (!conversionFactor.equals(SBML_Constants.EMPTY)) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.CONVERSION_FACTOR, conversionFactor);
			}
		}
		
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, SBML_Constants.SBML,
					SBML_Constants.MODEL_META_ID, metaID);
		}
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, SBML_Constants.SBML,
					SBML_Constants.MODEL_SBOTERM, sboTerm);
		}
		if (model.isSetAnnotation()) {
			if (model.getAnnotation().isSetRDFannotation()) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.MODEL_ANNOTATION, model.getAnnotation());
			}
			if (model.getAnnotation().isSetNonRDFannotation()) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.MODEL_NON_RDF_ANNOTATION, model
								.getAnnotation().getNonRDFannotation());
			}
		}
	}
	
	/**
	 * iterates over all speices of every reaction and set the reaction position to the average position of all their speices nodes.
	 */
	private void computeReactionNodePosition(Graph g) {
		for (Node reaction : SBMLHelper.getReactionNodes(g)) {
			if (!SBMLHelper.isSetLayoutID(g, reaction)) {
				Set<Node> reactionNeighbours = reaction.getNeighbors();
				if (reactionNeighbours.size() > 0) {
					Point2D newReactionPosition = new Point();
					newReactionPosition.setLocation(1d, 1d);
					for (Node species : reactionNeighbours) {
						double newXPosition = (AttributeHelper.getPosition(species).getX() + newReactionPosition.getX()) / 2d;
						double newYPosition = (AttributeHelper.getPosition(species).getY() + newReactionPosition.getY()) / 2d;
						newReactionPosition.setLocation(newXPosition, newYPosition);
//						System.out.println("process reaction: " + SBMLHelper.getReactionID(reaction) + "  new position: " + newXPosition + ", " + newYPosition
//								);
					}
					AttributeHelper.setPosition(reaction, newReactionPosition);
				}
			}
		}
	}
}
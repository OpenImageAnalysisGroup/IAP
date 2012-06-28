/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.AbstractInputSerializer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

@SuppressWarnings("unchecked")
public class SBML_XML_Reader extends AbstractInputSerializer {
	
	private String fileNameExt = ".sbml";
	
	// set to 0 in order to create always an reaction node
	public static int minReacOrProdCntForReactionCreation = 0; // set to "1" for V1.0 compatibility
	
	public SBML_XML_Reader() {
		super();
	}
	
	@Override
	public void read(String filename, Graph g) throws IOException {
		super.read(filename, g);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.AbstractInputSerializer#read(java.io.InputStream, org.graffiti.graph.Graph)
	 */
	@Override
	public void read(InputStream in, Graph g) throws IOException {
		try {
			readSBML(in, g);
		} catch (JDOMException e) {
			ErrorMsg.addErrorMessage(e);
			e.printStackTrace();
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
			e.printStackTrace();
		}
		in.close();
	}
	
	private void readSBML(InputStream in, Graph g) throws JDOMException,
						IOException {
		
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		InputStream inpStream = null;
		inpStream = in;
		doc = builder.build(inpStream);
		// Lesen des Wurzelelements des JDOM-Dokuments doc
		read(g, doc);
	}
	
	private void read(Graph g, Document doc) {
		Element sbml = doc.getRootElement();
		List<Element> models = sbml.getChildren("model", sbml.getNamespace());
		PositionGridGenerator pgg = new PositionGridGenerator(100, 100, 1000);
		for (Element model : models) {
			String modelID = model.getAttributeValue("id");
			String modelName = model.getAttributeValue("name");
			if (modelID == null || modelID.length() <= 0)
				modelID = modelName;
			// read compounds ("species" in SBML!)
			HashMap<String, org.graffiti.graph.Node> species2graphNode =
								processSpecies(g, pgg, sbml, model, "listOfSpecies", "species", modelID);
			HashMap<String, org.graffiti.graph.Node> specie2graphNode =
								processSpecies(g, pgg, sbml, model, "listOfSpecies", "specie", modelID);
			
			species2graphNode.putAll(specie2graphNode);
			
			// compIDs=processCompounds()
			
			// This HashMap stores information about species nodes and possible
			// layoutIDs. This is neccessary as strangly the layout of a species
			// is defined in the reaction elements and not directly in the species
			// definition. A lot of the layout specification is not straight forward,
			// but let it be, let's try to process it as good as possible
			HashMap<GraphElement, String> graphNode2possibleLayoutID =
								new HashMap<GraphElement, String>();
			
			// read reactions
			// HashMap<String,GraphElement> reaction2graphNode =
			processReactions(g, pgg, sbml, model, "listOfReactions", "reaction", modelID,
								species2graphNode, graphNode2possibleLayoutID);
			
			// read layout information (select one if many)
			// todo --> result: layout-ids
		}
	}
	
	private HashMap<String, org.graffiti.graph.Node> processSpecies(
						Graph g,
						PositionGridGenerator pgg,
						Element sbml,
						Element model,
						String tagList,
						String tagItem,
						String modelID) {
		HashMap<String, org.graffiti.graph.Node> result = new HashMap<String, org.graffiti.graph.Node>();
		List<Element> speciesLists = model.getChildren(tagList, sbml.getNamespace());
		for (Element speciesList : speciesLists) {
			List<Element> speciesEntries = speciesList.getChildren(tagItem, sbml.getNamespace());
			for (Element species : speciesEntries) {
				org.graffiti.graph.Node speciesGraphNode = g.addNode();
				AttributeHelper.setDefaultGraphicsAttribute(speciesGraphNode, pgg.getNextPosition());
				String speciesID = species.getAttributeValue("id");
				String speciesName = species.getAttributeValue("name");
				if (speciesID == null || speciesID.length() <= 0)
					speciesID = speciesName;
				if (speciesName == null || speciesName.length() <= 0)
					speciesName = speciesID;
				String speciesCompartment = species.getAttributeValue("compartment");
				NodeTools.setClusterID(speciesGraphNode, speciesCompartment);
				AttributeHelper.setFillColor(speciesGraphNode, Color.WHITE);
				AttributeHelper.setBorderWidth(speciesGraphNode, 1d);
				AttributeHelper.setLabel(speciesGraphNode, speciesName);
				AttributeHelper.setSBMLmodelID(speciesGraphNode, modelID);
				AttributeHelper.setSBMLid(speciesGraphNode, speciesID);
				AttributeHelper.setShapeEllipse(speciesGraphNode);
				AttributeHelper.setSize(speciesGraphNode, 60d, 20d);
				setStyleFromAnnotation(speciesGraphNode, species);
				result.put(speciesID, speciesGraphNode);
			}
		}
		return result;
	}
	
	private void setStyleFromAnnotation(Node speciesGraphNode, Element species) {
		List<Element> annotations = species.getChildren("annotations", species.getNamespace());
		for (Element annotation : annotations) {
			List<Element> jd_displays = annotation.getChildren("display", annotation.getNamespace("jd"));
			for (Element jd_display : jd_displays) {
				String x = jd_display.getAttributeValue("x");
				String y = jd_display.getAttributeValue("y");
				if (x != null && y != null) {
					try {
						double xd = Double.parseDouble(x);
						double yd = Double.parseDouble(y);
						AttributeHelper.setPosition(speciesGraphNode, xd, yd);
					} catch (Exception e) {
						//
					}
				}
			}
		}
	}
	
	private HashMap<String, GraphElement> processReactions(
						Graph g,
						PositionGridGenerator pgg,
						Element sbml,
						Element model,
						String tagList,
						String tagItem,
						String modelID,
						HashMap<String, Node> speciesGraphNodes,
						HashMap<GraphElement, String> graphNode2possibleLayoutID) {
		HashMap<String, GraphElement> result = new HashMap<String, GraphElement>();
		// create a reaction node in case more than one "Reactant" is found or in case
		// more than one Product for a reaction is found
		// otherwise only a edge is created
		List<Element> listsOfReactions = model.getChildren(tagList, sbml.getNamespace());
		for (Element reactionList : listsOfReactions) {
			List<Element> listOfReaction = reactionList.getChildren(tagItem, sbml.getNamespace());
			for (Element reaction : listOfReaction) {
				String reactionID = reaction.getAttributeValue("id");
				String reactionName = reaction.getAttributeValue("name");
				if (reactionID == null || reactionID.length() <= 0)
					reactionID = reactionName;
				if (reactionName == null || reactionName.length() <= 0)
					reactionName = reactionID;
				String reactionReversible = reaction.getAttributeValue("reversible");
				// read Reactants
				List<Element> reactantsSpeciesReferences = new ArrayList<Element>();
				List<Element> reactantLists = reaction.getChildren("listOfReactants", sbml.getNamespace());
				for (Element listOfReactants : reactantLists) {
					List<Element> speciesRefs = listOfReactants.getChildren("speciesReference", sbml.getNamespace());
					List<Element> specieRefs = listOfReactants.getChildren("specieReference", sbml.getNamespace());
					reactantsSpeciesReferences.addAll(speciesRefs);
					reactantsSpeciesReferences.addAll(specieRefs);
				}
				// read Products
				List<Element> productSpeciesReferences = new ArrayList<Element>();
				List<Element> productLists = reaction.getChildren("listOfProducts", sbml.getNamespace());
				for (Element listOfProducts : productLists) {
					List<Element> speciesRefs = listOfProducts.getChildren("speciesReference", sbml.getNamespace());
					List<Element> specieRefs = listOfProducts.getChildren("specieReference", sbml.getNamespace());
					productSpeciesReferences.addAll(speciesRefs);
					productSpeciesReferences.addAll(specieRefs);
				}
				// read Modifiers
				List<Element> modifierSpeciesReferences = new ArrayList<Element>();
				List<Element> modifiersLists = reaction.getChildren("listOfModifiers", sbml.getNamespace());
				for (Element listOfModifiers : modifiersLists) {
					List<Element> speciesRefs = listOfModifiers.getChildren("modifierSpeciesReference", sbml.getNamespace());
					List<Element> specieRefs = listOfModifiers.getChildren("modifierSpecieReference", sbml.getNamespace());
					modifierSpeciesReferences.addAll(speciesRefs);
					modifierSpeciesReferences.addAll(specieRefs);
				}
				if (reactantsSpeciesReferences.size() > minReacOrProdCntForReactionCreation
									|| productSpeciesReferences.size() > minReacOrProdCntForReactionCreation
									|| reactantsSpeciesReferences.size() == 0
									|| productSpeciesReferences.size() == 0
									|| modifierSpeciesReferences.size() > 0) {
					// create Reaction Node
					Node newReactionNode = g.addNode();
					AttributeHelper.setDefaultGraphicsAttribute(newReactionNode, pgg.getNextPosition());
					AttributeHelper.setFillColor(newReactionNode, Color.WHITE);
					AttributeHelper.setBorderWidth(newReactionNode, 1d);
					AttributeHelper.setLabel(newReactionNode, reactionName);
					AttributeHelper.setSBMLmodelID(newReactionNode, modelID);
					AttributeHelper.setSBMLid(newReactionNode, reactionID);
					AttributeHelper.setSBMLreversibleReaction(newReactionNode, reactionReversible);
					// SBML spec: Default is reversible = true, if nothing is defined
					boolean reversibleReaction = (reactionReversible != null && reactionReversible.equalsIgnoreCase("false")) ? false : true;
					result.put(reactionID, newReactionNode);
					boolean isReactantElement;
					boolean isProductElement;
					boolean isModifierElement;
					// A) connect Reactants to the Reaction Node with new Edges
					// B) connect Reaction Node to Products with new Edges
					// C) connect Modifier Node to Reaction Node
					for (int i = 0; i < 3; i++) {
						assert (i == 0 || i == 1 || i == 2);
						List<Element> workList = null;
						isReactantElement = false;
						isProductElement = false;
						isModifierElement = false;
						// A)
						if (i == 0) {
							isReactantElement = true;
							workList = reactantsSpeciesReferences;
						}
						// B)
						if (i == 1) {
							isProductElement = true;
							workList = productSpeciesReferences;
						}
						// C)
						if (i == 2) {
							isModifierElement = true;
							workList = modifierSpeciesReferences;
						}
						for (Element reactantORproduct : workList) {
							String reactantORproductID = reactantORproduct.getAttributeValue("species");
							if (reactantORproductID == null || reactantORproductID.length() <= 0)
								reactantORproductID = reactantORproduct.getAttributeValue("specie");
							String stoichiometry = reactantORproduct.getAttributeValue("stoichiometry");
							// System.out.println("Connect: "+reactantORproductID);
							Node reactORproductNode = speciesGraphNodes.get(reactantORproductID);
							String speciesLayoutAnnotation4react = null;
							if (speciesLayoutAnnotation4react != null)
								graphNode2possibleLayoutID.put(reactORproductNode, speciesLayoutAnnotation4react);
							if (reactORproductNode == null)
								ErrorMsg.addErrorMessage("Inconsistency found: Graph Node for Species " + reactantORproductID + " was not created as a graph node.");
							else {
								Edge newReactionEdge = null;
								if (isReactantElement) {
									newReactionEdge = g.addEdge(reactORproductNode, newReactionNode, true,
														AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
									AttributeHelper.setSBMLrole(newReactionEdge, "reactant");
									if (reversibleReaction)
										AttributeHelper.setArrowtail(newReactionEdge, true);
								} else
									if (isProductElement) {
										newReactionEdge = g.addEdge(newReactionNode, reactORproductNode, true,
															AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
										AttributeHelper.setSBMLrole(newReactionEdge, "product");
										if (reversibleReaction)
											AttributeHelper.setArrowtail(newReactionEdge, true);
									}
								if (isModifierElement) {
									newReactionEdge = g.addEdge(reactORproductNode, newReactionNode, false,
														AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.DARK_GRAY, Color.DARK_GRAY, true));
									AttributeHelper.setSBMLrole(newReactionEdge, "modifier");
									AttributeHelper.setDashInfo(newReactionEdge, 5, 5);
									AttributeHelper.setBorderWidth(newReactionEdge, 1d);
									AttributeHelper.setArrowtail(newReactionEdge, true);
								}
								assert (newReactionEdge != null);
								AttributeHelper.setSBMLmodelID(newReactionEdge, modelID);
								if (stoichiometry != null && stoichiometry.length() > 0)
									AttributeHelper.setLabel(newReactionEdge, stoichiometry);
								AttributeHelper.setSBMLreversibleReaction(newReactionEdge, reactionReversible);
								AttributeHelper.setSBMLid(newReactionEdge, reactionID);
								// AttributeHelper.setArrowtail(newReactionEdge, true);
								result.put(reactionID, newReactionEdge);
							}
						}
					}
				} else {
					processSingleReactionEdge(g, modelID, speciesGraphNodes, graphNode2possibleLayoutID, result,
										reactionID, reactionReversible, reactantsSpeciesReferences, productSpeciesReferences);
				}
			}
		}
		return result;
	}
	
	private void processSingleReactionEdge(
						Graph g,
						String modelID,
						HashMap<String, Node> speciesGraphNodes,
						HashMap<GraphElement, String> graphNode2possibleLayoutID,
						HashMap<String, GraphElement> result,
						String reactionID,
						String reactionReversible,
						List<Element> reactantsSpeciesReferences,
						List<Element> productSpeciesReferences) {
		
		// just Create a Reaction Edge
		// create a single Edge from the 1 Reactant to the 1 Product
		Element reactant = reactantsSpeciesReferences.get(0);
		Element product = productSpeciesReferences.get(0);
		String reactantID = reactant.getAttributeValue("species");
		String productID = product.getAttributeValue("species");
		Node reactNode = speciesGraphNodes.get(reactantID);
		Node productNode = speciesGraphNodes.get(productID);
		String speciesLayoutAnnotation4react = null;
		String speciesLayoutAnnotation4prod = null;
		if (speciesLayoutAnnotation4react != null)
			graphNode2possibleLayoutID.put(reactNode, speciesLayoutAnnotation4react);
		if (speciesLayoutAnnotation4prod != null)
			graphNode2possibleLayoutID.put(productNode, speciesLayoutAnnotation4prod);
		if (reactNode == null || productNode == null)
			ErrorMsg.addErrorMessage("Inconsistency found: Graph Node for Species " + reactantID + ", or " + productID + " was not created as a graph node.");
		else {
			Edge newReactionEdge = g.addEdge(reactNode, productNode, true,
								AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
			AttributeHelper.setSBMLmodelID(newReactionEdge, modelID);
			AttributeHelper.setSBMLreversibleReaction(newReactionEdge, reactionReversible);
			AttributeHelper.setSBMLid(newReactionEdge, reactionID);
			result.put(reactionID, newReactionEdge);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getExtensions()
	 */
	public String[] getExtensions() {
		return new String[] { fileNameExt, ".xml" };
	}
	
	@Override
	public boolean validFor(InputStream reader) {
		try {
			int maxAnalyze = 5;
			TextFile tf = new TextFile(reader, maxAnalyze);
			for (String line : tf) {
				if (line.toUpperCase().indexOf("<SBML") >= 0)
					return true;
				maxAnalyze--;
				if (maxAnalyze == 0)
					break;
			}
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "SBML2 File", "SBML2 File" };
	}
	
	public void read(Reader reader, Graph newGraph) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		doc = builder.build(reader);
		read(newGraph, doc);
	}
}

/**
 * This class reads in Reactions
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.KineticLawHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLLocalParameter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLReactionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Reaction_Reader {
	
	/**
	 * Method reads in function definitions and is called from class
	 * SBML_XML_Reader.java. Add reaction nodes and edges to the graph
	 * 
	 * @param g
	 *        the data structure for reading in the information
	 * @param reactionList
	 *        contains the reactions for the import
	 * @param modelID
	 *        is the id of the current model
	 * @param pgg
	 *        is needed for drawing the graph
	 */
	public void addReactions(Graph g, ListOf<Reaction> reactionList,
			String modelID, PositionGridGenerator pgg, SBMLReactionHelper reactionhelper) {
		SBMLReactionHelper reactionHelper = reactionhelper;
		String reactionID;
		String reactionName;
		Boolean reversible;
		String compartment;
		String sboTerm;
		String metaID;
		Boolean fast;
		Node reactionNode;
		ListOf<SpeciesReference> reactants;
		ListOf<SpeciesReference> products;
		ListOf<ModifierSpeciesReference> modifiers;
		String metaID2;
		String sboTerm2;
		String kineticFormula;
		KineticLaw kineticLaw;
		KineticLawHelper kineticLawHelper;
		List<LocalParameter> listLocalParameter;
		Iterator<LocalParameter> itLP;
		int countLocalParameter;
		LocalParameter localParameter;
		String internAttributeName;
		String presentedAttributeName;
		SBMLLocalParameter localParameterHelper;
		String id = SBML_Constants.EMPTY;
		String name;
		Double value;
		String unit;
		for (Reaction reaction : reactionList) {
			reactionID = reaction.getId();
			reactionName = reaction.getName();
			// reversible = false;
			// if (reaction.isSetReversible()) {
			reversible = reaction.getReversible();
			// }
			fast = reaction.getFast();
			compartment = reaction.getCompartment();
			sboTerm = reaction.getSBOTermID();
			metaID = reaction.getMetaId();
			
			// Determines the label of the reaction node
			/*
			 * String ex = null; if(!(reactionName == "")){ ex = reactionName; }
			 * else{ ex = reactionID; }
			 */
			reactionNode = g.addNode();
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_REACTION);
			
			// CAUTION working with level 3 or higher CAUTION
			reactionHelper
					.setLabel(reactionNode, reactionName, reactionID, pgg);
			// setAttributes(reactionNode, Color.white, ex,
			// pgg.getNextPosition(), 8);
			AttributeHelper.setBorderWidth(reactionNode, 1d);
			// NodeTools.setClusterID(reactionNode, compartment);
			if (reaction.isSetCompartment()) {
				reactionHelper.setCompartment(reactionNode, compartment);
			}
			if (reaction.isSetId()
					&& Reaction.isValidId(reactionID, reaction.getLevel(),
							reaction.getVersion())) {
				reactionHelper.setID(reactionNode, reactionID);
			}
			// if(reaction.isSetReversible()){
			reactionHelper.setReversible(reactionNode, reversible);
			// }
			if (reaction.isSetFast()) {
				reactionHelper.setFast(reactionNode, fast);
			}
			if (reaction.isSetSBOTerm()) {
				reactionHelper.setSBOTerm(reactionNode, sboTerm);
			}
			if (reaction.isSetMetaId()) {
				reactionHelper.setMetaID(reactionNode, metaID);
			}
			if (reaction.isSetNotes()) {
				reactionHelper.setNotes(reactionNode,
						reaction.getNotesString(), reaction.getNotes());
			}
			
			if (reaction.isSetAnnotation()) {
				if (reaction.getAnnotation().isSetRDFannotation()) {
					reactionHelper.setAnnotation(reactionNode,
							reaction.getAnnotation());
				}
				if (reaction.getAnnotation().isSetNonRDFannotation()) {
					reactionHelper.setNonRDFAnnotation(reactionNode, reaction
							.getAnnotation().getNonRDFannotation());
				}
			}
			
			// Adds the edges between reactant node and reaction node
			reactants = reaction.getListOfReactants();
			addReactants(g, reactants, reactionID, modelID, reactionNode,
					reaction.getReversible(), reactionHelper);
			
			// Adds the edges between product node and reaction node
			products = reaction.getListOfProducts();
			addProducts(g, products, reactionID, modelID, reactionNode,
					reaction.getReversible(), reactionHelper);
			
			// Adds the edges between modifier node and reaction node
			modifiers = reaction.getListOfModifiers();
			addModifier(g, modifiers, reactionNode, reactionHelper, reactionID);
			
			if (reaction.isSetKineticLaw()) {
				kineticLaw = reaction.getKineticLaw();
				kineticLawHelper = new KineticLawHelper(g, reactionHelper.getReactionClones());
				metaID2 = kineticLaw.getMetaId();
				sboTerm2 = kineticLaw.getSBOTermID();
				kineticFormula = "";
				try {
					if (kineticLaw.isSetMath()) {
						if (null != kineticLaw.getMath()) {
							kineticFormula = kineticLaw.getMath().toFormula();
						}
					}
				} catch (SBMLException e) {
					e.printStackTrace();
				}
				
				if (kineticLaw.isSetMath()) {
					kineticLawHelper.setFunction(reactionNode, kineticFormula);
				}
				if (kineticLaw.isSetSBOTerm()) {
					kineticLawHelper.setSBOTerm(reactionNode, sboTerm2);
				}
				if (kineticLaw.isSetMetaId()) {
					kineticLawHelper.setMetaId(reactionNode, metaID2);
				}
				if (kineticLaw.isSetNotes()) {
					kineticLawHelper.setNotes(reactionNode,
							kineticLaw.getNotesString(), kineticLaw.getNotes());
				}
				if (kineticLaw.isSetAnnotation()) {
					if (kineticLaw.getAnnotation().isSetRDFannotation()) {
						kineticLawHelper.setAnnotation(reactionNode,
								kineticLaw.getAnnotation());
					}
					if (kineticLaw.getAnnotation().isSetNonRDFannotation()) {
						kineticLawHelper.setNonRDFAnnotation(reactionNode,
								kineticLaw.getAnnotation()
										.getNonRDFannotation());
					}
				}
				
				// Two ways to read in a Local Parameter. One way is deprecated.
				if (kineticLaw.isSetListOfLocalParameters()
						|| kineticLaw.isSetListOfParameters()) {
					listLocalParameter = null;
					if (reaction.getModel().getLevel() == 3
							&& reaction.getModel().getVersion() == 1) {
						if (kineticLaw.isSetListOfLocalParameters()) {
							listLocalParameter = kineticLaw
									.getListOfLocalParameters();
						}
					} else {
						if (kineticLaw.isSetListOfParameters()) {
							listLocalParameter = kineticLaw
									.getListOfParameters();
						}
					}
					/*
					 * if(kineticLaw.isSetListOfParameters()){
					 * listLocalParameter = kineticLaw.getListOfParameters(); }
					 * else if(kineticLaw.isSetListOfLocalParameters()){
					 * listLocalParameter =
					 * kineticLaw.getListOfLocalParameters(); }
					 */
					
					itLP = listLocalParameter.iterator();
					countLocalParameter = 1;
					while (itLP.hasNext()) {
						localParameter = itLP.next();
						internAttributeName = new StringBuffer(
								SBML_Constants.LOCAL_PARAMETER).append(
								countLocalParameter).toString();
						presentedAttributeName = new StringBuffer(
								SBML_Constants.LOCALPARAMETER_HEADLINE).append(
								countLocalParameter).toString();
						
						localParameterHelper = kineticLawHelper
								.addLocalParemeter(g, presentedAttributeName,
										internAttributeName);
						
						id = localParameter.getId();
						name = localParameter.getName();
						value = localParameter.getValue();
						if (value.equals(Double.NaN)) {
							ErrorMsg.addErrorMessage("Attribute value of reaction "
									+ reactionID
									+ " "
									+ presentedAttributeName
									+ " is not a valid double value.");
						}
						unit = localParameter.getUnits();
						
						if (localParameter.isSetId()) {
							localParameterHelper.setID(reactionNode, id);
						}
						if (localParameter.isSetName()) {
							localParameterHelper.setName(reactionNode, name);
						}
						if (localParameter.isSetValue()) {
							localParameterHelper.setValue(reactionNode, value);
						}
						if (localParameter.isSetUnits()) {
							localParameterHelper.setUnits(reactionNode, unit);
						}
						if (localParameter.isSetMetaId()) {
							localParameterHelper.setMetaID(reactionNode,
									localParameter.getMetaId());
						}
						if (localParameter.isSetSBOTerm()) {
							localParameterHelper.setSBOTerm(reactionNode,
									localParameter.getSBOTermID());
						}
						if (localParameter.isSetNotes()) {
							localParameterHelper.setNotes(reactionNode,
									localParameter.getNotesString(),
									localParameter.getNotes());
						}
						if (localParameter.isSetAnnotation()) {
							if (localParameter.getAnnotation()
									.isSetRDFannotation()) {
								localParameterHelper.setAnnotation(
										reactionNode,
										localParameter.getAnnotation());
							}
							if (localParameter.getAnnotation()
									.isSetNonRDFannotation()) {
								localParameterHelper.setNonRDFAnnotation(
										reactionNode, localParameter
												.getAnnotation()
												.getNonRDFannotation());
							}
						}
						
						countLocalParameter++;
					}
					
				}
			}
			
			AttributeHelper.setLabel(AttributeHelper.getLabels(reactionNode)
					.size(), reactionNode, reactionID, null,
					AlignmentSetting.HIDDEN.toGMLstring());
			
			if (SBML_Constants.isLayoutActive) {
				processLayoutInformation(g, reaction, reactionHelper, reactionNode);
			}
			
		}
	}
	
	private void processLayoutInformation(Graph g, Reaction reaction, SBMLReactionHelper reactionHelper, Node reactionNode) {
		ExtendedLayoutModel layoutModel = (ExtendedLayoutModel) reaction.getModel().getExtension(SBMLHelper.SBML_LAYOUT_EXTENSION_NAMESPACE);
		if (layoutModel != null) {
			
			Layout layout = layoutModel.getListOfLayouts().iterator().next();
			ListOf<ReactionGlyph> currentReactionGlyphs = new ListOf<ReactionGlyph>();
			currentReactionGlyphs.setLevel(layout.getLevel());
			currentReactionGlyphs.setVersion(layout.getVersion());
			Iterator<ReactionGlyph> reactionGlyphListIt = layout.getListOfReactionGlyphs().iterator();
			String reactionID = reaction.getId();
			while (reactionGlyphListIt.hasNext()) {
				ReactionGlyph nextReactionGlyph = reactionGlyphListIt.next();
				if (nextReactionGlyph.getReaction().equals(reactionID)) {
					currentReactionGlyphs.add(nextReactionGlyph);
				}
			}
			// for (int i = 0; i < currentReactionGlyphs.size(); i++) {
			for (int i = 0; i < 1; i++) {
				ReactionGlyph reactionGlyph = currentReactionGlyphs.get(i);
				if (i == 0) {
					reactionHelper.addReactionCloneToList(reactionID, reactionNode);
				}
				if (i >= 1) {
					Node reactionNodeClone = g.addNodeCopy(reactionNode);
					reactionHelper.addReactionCloneToList(reactionID, reactionNodeClone);
					reactionNode = reactionNodeClone;
				}
				
				AttributeHelper.setSize(reactionNode, 40, 40);
				BoundingBox boundingBox = null;
				if (reactionGlyph != null)
					boundingBox = reactionGlyph.getBoundingBox();
				if (boundingBox != null) {
					Dimensions dimensions = boundingBox.getDimensions();
					if (dimensions != null) {
						double width = dimensions.getWidth();
						double height = dimensions.getHeight();
						AttributeHelper.setSize(reactionNode, width, height);
						
						if (reactionGlyph.isSetLayoutId()) {
							String layoutId = reactionGlyph.getLayoutId();
							AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, layoutId);
						}
						else {
							AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, SBML_Constants.EMPTY);
						}
						
					} else
					{
						AttributeHelper.setSize(reactionNode, 34, 34);
//						System.out.println("reaction id '" + reactionID + "' has no width/height information.");
					}
					Point position = boundingBox.getPosition();
					if (position != null) {
						double x = position.getX();
						double y = position.getY();
						AttributeHelper.setPosition(reactionNode, x, y);
						String layoutId = reactionGlyph.getLayoutId();
						AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, layoutId);
					} else {
						System.out.println("species id '" + reactionID + "' has no x/y information.");
					}
				}
			}
		}
	}
	
	/**
	 * The method reads in reactants, products and modifiers. It is called from
	 * the method addReactions
	 * 
	 * @param simpleRef
	 *        the reactant, product or modifier object for the import
	 * @param Edge
	 *        is the reaction node in the graph
	 * @param presentedHeadline
	 *        is visible for the user
	 * @param internHeadline
	 *        intern representation of the headline
	 */
	private void setSimpleSpeciesReferences(SimpleSpeciesReference simpleRef,
			Edge edge, String headline, SBMLReactionHelper reactionHelper) {
		/*
		 * String keySpecies = SBML_Constants.addToNiceIdList(presentedHeadline,
		 * "Species"); String keySpeciesReferenceID =
		 * SBML_Constants.addToNiceIdList(presentedHeadline,
		 * "Species Reference ID"); String keySpeciesReferenceName =
		 * SBML_Constants.addToNiceIdList(presentedHeadline,
		 * "Species Reference Name"); String keyMetaId =
		 * SBML_Constants.addToNiceIdList(presentedHeadline, "Meta ID"); String
		 * keySBOTerm = SBML_Constants.addToNiceIdList(presentedHeadline,
		 * "SBOTerm"); String keyToolTip =
		 * SBML_Constants.addToNiceIdList(presentedHeadline, "ToolTip");
		 */
		
		if (simpleRef instanceof org.sbml.jsbml.SpeciesReference) {
			/*
			 * String keyStoichiometry =
			 * SBML_Constants.addToNiceIdList(presentedHeadline,
			 * "Stoichiometry"); String keyConstant =
			 * SBML_Constants.addToNiceIdList(presentedHeadline, "Constant");
			 */
			if (((SpeciesReference) simpleRef).isSetStoichiometry()) {
				reactionHelper.setStoichiometry(edge,
						((SpeciesReference) simpleRef).getStoichiometry());
			}
			if (((SpeciesReference) simpleRef).isSetConstant()) {
				reactionHelper.setConstant(edge,
						((SpeciesReference) simpleRef).getConstant());
			}
		}
		if (simpleRef.isSetSpecies()) {
			reactionHelper.setSpecies(edge, simpleRef.getSpecies());
		}
		if (simpleRef.isSetId()) {
			reactionHelper.setID(edge, simpleRef.getId());
		}
		if (simpleRef.isSetName()) {
			reactionHelper.setName(edge, simpleRef.getName());
		}
		if (AttributeHelper.getSBMLrole(edge).equals(
				SBML_Constants.ROLE_REACTANT)) {
			if (simpleRef.isSetMetaId()) {
				reactionHelper.setMetaIDReactant(edge, simpleRef.getMetaId());
			}
			if (simpleRef.isSetSBOTerm()) {
				reactionHelper.setSBOTermReactant(edge,
						simpleRef.getSBOTermID());
			}
			if (simpleRef.isSetNotes()) {
				reactionHelper.setNotesReactant(edge,
						simpleRef.getNotesString(), simpleRef.getNotes());
			}
			if (simpleRef.isSetAnnotation()) {
				if (simpleRef.getAnnotation().isSetRDFannotation()) {
					reactionHelper.setAnnotationReactant(edge,
							simpleRef.getAnnotation());
				}
				if (simpleRef.getAnnotation().isSetNonRDFannotation()) {
					reactionHelper.setNonRDFAnnotationReactant(edge, simpleRef
							.getAnnotation().getNonRDFannotation());
				}
			}
		}
		if (AttributeHelper.getSBMLrole(edge).equals(
				SBML_Constants.ROLE_PRODUCT)) {
			if (simpleRef.isSetMetaId()) {
				reactionHelper.setMetaIDProduct(edge, simpleRef.getMetaId());
			}
			if (simpleRef.isSetSBOTerm()) {
				reactionHelper
						.setSBOTermProduct(edge, simpleRef.getSBOTermID());
			}
			if (simpleRef.isSetNotes()) {
				reactionHelper.setNotesProduct(edge,
						simpleRef.getNotesString(), simpleRef.getNotes());
			}
			if (simpleRef.isSetAnnotation()) {
				if (simpleRef.getAnnotation().isSetRDFannotation()) {
					reactionHelper.setAnnotationProduct(edge,
							simpleRef.getAnnotation());
				}
				if (simpleRef.getAnnotation().isSetNonRDFannotation()) {
					reactionHelper.setNonRDFAnnotationProduct(edge, simpleRef
							.getAnnotation().getNonRDFannotation());
				}
			}
		}
		if (AttributeHelper.getSBMLrole(edge).equals(
				SBML_Constants.ROLE_MODIFIER)) {
			if (simpleRef.isSetMetaId()) {
				reactionHelper.setMetaIDModifier(edge, simpleRef.getMetaId());
			}
			if (simpleRef.isSetSBOTerm()) {
				reactionHelper.setSBOTermModifier(edge,
						simpleRef.getSBOTermID());
			}
			if (simpleRef.isSetNotes()) {
				reactionHelper.setNotesModifier(edge,
						simpleRef.getNotesString(), simpleRef.getNotes());
			}
			if (simpleRef.isSetAnnotation()) {
				if (simpleRef.getAnnotation().isSetRDFannotation()) {
					reactionHelper.setAnnotationModifier(edge,
							simpleRef.getAnnotation());
				}
				if (simpleRef.getAnnotation().isSetNonRDFannotation()) {
					reactionHelper.setNonRDFAnnotationModifier(edge, simpleRef
							.getAnnotation().getNonRDFannotation());
				}
			}
		}
	}
	
	/**
	 * Adds an edge between an reaction node and a reactant
	 * 
	 * @param g
	 *        is the data structure for reading in the information.
	 * @param molecules
	 *        contains the reactants of the reaction
	 * @param reactionID
	 *        contains the reaction ID
	 * @param modelID
	 *        contains the model ID
	 * @param reactionNode
	 *        is the reaction node in the graph
	 * @param reversible
	 *        indicates if the reaction is reversible
	 */
	private void addReactants(Graph g, ListOf<SpeciesReference> molecules,
			String reactionID, String modelID, Node reactionNode,
			boolean reversible, SBMLReactionHelper reactionHelper) {
		Iterator<SpeciesReference> it = molecules.iterator();
		String stoichiometry;
		SpeciesReference ref;
		Edge newReactionEdge;
		Node reactantNode;
		while (it.hasNext()) {
			ref = it.next();
			reactantNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			stoichiometry = Double.toString(ref.getStoichiometry());
			if (ref.getStoichiometry() == Double.NaN) {
				ErrorMsg.addErrorMessage("Attribute stochiometry of reaction "
						+ reactionID + " species " + ref.getSpecies()
						+ " is not a valid double value.");
			}
			newReactionEdge = g.addEdge(reactantNode, reactionNode, true,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(
							Color.BLACK, Color.BLACK, true));
			reactionHelper.addReactantCloneToList(reactionID, ref.getSpecies(), newReactionEdge);
//			System.out.println("In Reaction Reader: " + reactionID + " " + ref.getSpecies());
			if (reversible) {
				AttributeHelper.setArrowtail(newReactionEdge, true);
			}
			
			AttributeHelper.setLabel(newReactionEdge, stoichiometry);
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_REACTANT);
			// AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
			// SBML_Constants.REVERSIBLE, reversible);
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.STOICHIOMETRY, stoichiometry);
			setSimpleSpeciesReferences(ref, newReactionEdge,
					SBML_Constants.SBML, reactionHelper);
		}
	}
	
	/**
	 * Adds an edge between an reaction node and a product
	 * 
	 * @param g
	 *        is the data structure for reading in the information
	 * @param molecules
	 *        contains the products of the reaction
	 * @param reactionID
	 *        contains the reaction ID
	 * @param modelID
	 *        contains the model ID
	 * @param reactionNode
	 *        is the reaction node in the graph
	 * @param reversible
	 *        indicates if the reaction is reversible
	 */
	private void addProducts(Graph g, ListOf<SpeciesReference> molecules,
			String reactionID, String modelID, Node reactionNode,
			boolean reversible, SBMLReactionHelper reactionHelper) {
		Iterator<SpeciesReference> it = molecules.iterator();
		Edge newReactionEdge;
		String stoichiometry;
		String label;
		Node productNode;
		SpeciesReference ref;
		while (it.hasNext()) {
			ref = it.next();
			productNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			stoichiometry = Double.toString(ref.getStoichiometry());
			label = stoichiometry;
			
			newReactionEdge = g.addEdge(reactionNode, productNode, true,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(
							Color.BLACK, Color.BLACK, true));
			reactionHelper.addProductCloneToList(reactionID, ref.getSpecies(), newReactionEdge);
			
			if (reversible) {
				AttributeHelper.setArrowtail(newReactionEdge, true);
			}
			if (ref.getStoichiometry() == Double.NaN) {
				ErrorMsg.addErrorMessage("Attribute stochiometry of reaction "
						+ reactionID + " species " + ref.getSpecies()
						+ " is not a valid double value.");
			}
			
			AttributeHelper.setLabel(newReactionEdge, label);
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_PRODUCT);
			// AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
			// SBML_Constants.REVERSIBLE, reversible);
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.STOICHIOMETRY, stoichiometry);
			setSimpleSpeciesReferences(ref, newReactionEdge,
					SBML_Constants.SBML, reactionHelper);
		}
	}
	
	/**
	 * Adds an edge between an reaction node and a modifier
	 * 
	 * @param g
	 *        is the data structure for reading in the information.
	 * @param molecules
	 *        contains the modifiers of the reaction.
	 * @param reactionNode
	 *        is the node that will be connected with the modifier
	 */
	private void addModifier(Graph g,
			ListOf<ModifierSpeciesReference> molecules, Node reactionNode,
			SBMLReactionHelper reactionHelper, String reactionID) {
		Iterator<ModifierSpeciesReference> it = molecules.iterator();
		Edge reactionEdge;
		ModifierSpeciesReference ref;
		Node modifierNode;
		while (it.hasNext()) {
			ref = it.next();
			modifierNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			reactionEdge = g.addEdge(modifierNode, reactionNode, false,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(
							Color.DARK_GRAY, Color.DARK_GRAY, true));
			
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_MODIFIER);
			reactionHelper.addModifierCloneToList(reactionID, ref.getSpecies(), reactionEdge);
			AttributeHelper.setDashInfo(reactionEdge, 5, 5);
			AttributeHelper.setBorderWidth(reactionEdge, 1d);
			setSimpleSpeciesReferences(ref, reactionEdge, SBML_Constants.SBML,
					reactionHelper);
		}
	}
}
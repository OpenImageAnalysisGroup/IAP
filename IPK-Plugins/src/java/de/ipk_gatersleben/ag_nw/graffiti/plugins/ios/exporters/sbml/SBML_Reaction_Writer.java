/**
 * This class sets the attributes of Reactions
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.text.parser.ParseException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.KineticLawHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLLocalParameter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLReactionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Reaction_Writer extends SBML_SBase_Writer {
	
	int localParameterCount = 1;
	
	/**
	 * This method adds the reactions to a model
	 * 
	 * @param g
	 *        contains the required data
	 * @param model
	 *        the data will be added to this model
	 */
	public void addReactions(Graph g, Model model) {
		Iterator<Node> itNodes = g.getNodesIterator();
		SBMLReactionHelper reactionHelper = new SBMLReactionHelper(g);
		while (itNodes.hasNext()) {
			Node node = (Node) itNodes.next();
			if (AttributeHelper.getSBMLrole(node).equals("reaction")) {
				Reaction reaction = model.createReaction();
				
				addSBaseAttributes(reaction, node);
				
				if (reactionHelper.isSetID(node)) {
					String id = reactionHelper.getID(node);
					if (Reaction.isValidId(id, reaction.getLevel(),
							reaction.getVersion())) {
						reaction.setId(reactionHelper.getID(node));
					}
				}
				if (reactionHelper.isSetName(node)) {
					reaction.setName(reactionHelper.getName(node));
				}
				if (reactionHelper.isSetReversible(node)) {
					System.out.println("Reaction: " + reactionHelper.getID(node));
					reaction.setReversible(reactionHelper.getReversible(node));
				}
				if (reactionHelper.isSetFast(node)) {
					reaction.setFast(reactionHelper.getFast(node));
				}
				if (reactionHelper.isSetCompartment(node)) {
					reaction.setCompartment(reactionHelper.getCompartment(node));
				}
				
				Iterator<Edge> itEdges = node.getEdgesIterator();
				while (itEdges.hasNext()) {
					Edge edge = itEdges.next();
					if (AttributeHelper.getSBMLrole(edge).equals("reactant")) {
						
						SpeciesReference reactant = new SpeciesReference(
								model.getLevel(), model.getVersion());
						if (reactionHelper.isSetConstant(edge)) {
							reactant.setConstant(reactionHelper
									.getConstant(edge));
						}
						if (reactionHelper.isSetStoichiometry(edge)) {
							reactant.setStoichiometry(Double
									.parseDouble(reactionHelper
											.getStoichiometry(edge)));
						}
						if (reactionHelper.isSetID(edge)) {
							reactant.setId(reactionHelper.getID(edge));
						}
						if (reactionHelper.isSetName(edge)) {
							reactant.setName(reactionHelper.getName(edge));
						}
						if (reactionHelper.isSetSpecies(edge)) {
							reactant.setSpecies(reactionHelper.getSpecies(edge));
						}
						
						reaction.addReactant(reactant);
						addSBaseAttributes(reactant, edge, SBML_Constants.SBML);
					}
					
					if (AttributeHelper.getSBMLrole(edge).equals("product")) {
						
						SpeciesReference product = new SpeciesReference(
								model.getLevel(), model.getVersion());
						if (reactionHelper.isSetConstant(edge)) {
							product.setConstant(reactionHelper
									.getConstant(edge));
						}
						if (reactionHelper.isSetStoichiometry(edge)) {
							product.setStoichiometry(Double
									.parseDouble(reactionHelper
											.getStoichiometry(edge)));
						}
						if (reactionHelper.isSetID(edge)) {
							product.setId(reactionHelper.getID(edge));
						}
						if (reactionHelper.isSetName(edge)) {
							product.setName(reactionHelper.getName(edge));
						}
						if (reactionHelper.isSetSpecies(edge)) {
							String[] list = reactionHelper.getSpecies(edge)
									.split(Pattern.quote("/"));
							product.setSpecies(list[0].trim());
						}
						
						reaction.addProduct(product);
						
						addSBaseAttributes(product, edge, SBML_Constants.SBML);
						// productCount++;
					}
					
					if (AttributeHelper.getSBMLrole(edge).equals("modifier")) {
						ModifierSpeciesReference modifier = new ModifierSpeciesReference(
								model.getLevel(), model.getVersion());
						
						if (reactionHelper.isSetID(edge)) {
							modifier.setId(reactionHelper.getID(edge));
						}
						if (reactionHelper.isSetName(edge)) {
							modifier.setName(reactionHelper.getName(edge));
						}
						if (reactionHelper.isSetSpecies(edge)) {
							String[] list = reactionHelper.getSpecies(edge)
									.split(Pattern.quote("/"));
							modifier.setSpecies(list[0].trim());
						}
						
						reaction.addModifier(modifier);
						
						addSBaseAttributes(modifier, edge, SBML_Constants.SBML);
					}
				}
				
				ArrayList<String> kineticLawList = headlineHelper(node,
						SBML_Constants.SBML_KINETIC_LAW);
				// kineticLawList has size 1 or 0
				if (kineticLawList.size() > 0) {
					KineticLaw kineticLaw = reaction.createKineticLaw();
					KineticLawHelper kineticLawHelper = new KineticLawHelper(g, reactionHelper.getReactionClones());
					addSBaseAttributes(kineticLaw, node);
					
					if (kineticLawHelper.isSetFunction(node)) {
						try {
							kineticLaw.setMath(ASTNode
									.parseFormula(kineticLawHelper
											.getFunction(node)));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					
					while (AttributeHelper.hasAttribute(
							node,
							SBML_Constants.SBML_KINETIC_LAW,
							new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
									.append(localParameterCount)
									.append(SBML_Constants.LOCAL_PARAMETER_ID)
									.toString())) {
						
						LocalParameter localParameter = null;
						SBMLLocalParameter localParameterHelper = kineticLawHelper
								.addLocalParameter(g, localParameterCount);
						if (localParameterHelper.isSetID(node)) {
							localParameter = new LocalParameter(
									localParameterHelper.getID(node),
									model.getLevel(), model.getVersion());
						} else {
							localParameter = new LocalParameter();
						}
						
						if (localParameterHelper.isSetName(node)) {
							localParameter.setName(localParameterHelper
									.getName(node));
						}
						if (localParameterHelper.isSetValue(node)) {
							localParameter.setValue(localParameterHelper
									.getValue(node));
						}
						if (localParameterHelper.isSetUnits(node)) {
							localParameter.setUnits(localParameterHelper
									.getUnits(node));
						}
						kineticLaw.addLocalParameter(localParameter);
						addSBaseAttributes(localParameter, node);
						
						localParameterCount++;
					}
					localParameterCount = 1;
				}
			}
		}
	}
	// }
}
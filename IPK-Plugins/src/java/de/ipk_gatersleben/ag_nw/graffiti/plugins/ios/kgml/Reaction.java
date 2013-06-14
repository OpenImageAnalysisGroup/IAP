/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Id;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.ReactionType;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Reaction {
	private String name;
	private ReactionType type;
	// Missing: find out meaning and find altSubstrate and altProduct elements in XML
	// private IdRef altSubstrateName;
	// private IdRef altProductName;
	private Collection<Entry> allPossibleSubstrates;
	private Collection<Entry> allPossibleProducts;
	
	private String elementID;
	
	public Reaction(
			String name,
			ReactionType type,
			Collection<Entry> substrates,
			Collection<Entry> products) {
		assert name != null;
		assert type != null;
		
		assert substrates != null;
		assert products != null;
		// if (substrates.size() <= 0)
		// System.out.println("WARNING: No valid substrate IDs for Reaction " + name + "!");
		// if (products.size() <= 0)
		// System.out.println("WARNING: No valid product IDs for Reaction " + name + "!");
		// assert substrates.size()>0;
		// assert products.size()>0;
		if (type == null) {
			ErrorMsg.addErrorMessage("Invalid Reaction Type for Reaction " + getId() + "!");
		}
		
		this.name = name;
		this.type = type;
		this.allPossibleSubstrates = substrates;
		this.allPossibleProducts = products;
		
		for (Entry substrate : substrates) {
			assert substrate.hasCompoundName() || substrate.hasGlycanName();
		}
		for (Entry product : products) {
			assert product.hasCompoundName() || product.hasGlycanName();
		}
		
		this.elementID = null;
		
	}
	
	public Reaction(
			String name,
			ReactionType type,
			Collection<Entry> substrates,
			Collection<Entry> products,
			String elementID) {
		assert name != null;
		assert type != null;
		
		assert substrates != null;
		assert products != null;
		// if (substrates.size() <= 0)
		// System.out.println("WARNING: No valid substrate IDs for Reaction " + name + "!");
		// if (products.size() <= 0)
		// System.out.println("WARNING: No valid product IDs for Reaction " + name + "!");
		// assert substrates.size()>0;
		// assert products.size()>0;
		if (type == null) {
			ErrorMsg.addErrorMessage("Invalid Reaction Type for Reaction " + getId() + "!");
		}
		
		this.name = name;
		this.type = type;
		this.allPossibleSubstrates = substrates;
		this.allPossibleProducts = products;
		
		for (Entry substrate : substrates) {
			assert substrate.hasCompoundName() || substrate.hasGlycanName();
		}
		for (Entry product : products) {
			assert product.hasCompoundName() || product.hasGlycanName();
		}
		
		this.elementID = elementID;
		
	}
	
	public String getId() {
		return name;
	}
	
	public void setId(String name) {
		this.name = name;
	}
	
	public ReactionType getType() {
		return type;
	}
	
	public Collection<Entry> getSubstrates() {
		return allPossibleSubstrates;
	}
	
	public Collection<Entry> getProducts() {
		return allPossibleProducts;
	}
	
	public String getElementID() {
		return this.elementID;
	}
	
	public static Reaction getReactionFromKgmlReactionElement(
			Element reactionElement,
			Collection<Entry> entryElements, String clusterIdForHiddenCompounds) {
		String nameValue = KGMLhelper.getAttributeValue(reactionElement, "name", null);
		String typeValue = KGMLhelper.getAttributeValue(reactionElement, "type", null);
		
		String name = nameValue;
		ReactionType type = ReactionType.getReactiontype(typeValue);
		
		if (type == null)
			return null;
		
		Collection<Entry> substrates = new ArrayList<Entry>();
		List<?> substrateElements = reactionElement.getChildren("substrate");
		
		for (Object o : substrateElements) {
			Element substrateElement = (Element) o;
			String subNameValue = KGMLhelper.getAttributeValue(substrateElement, "name", null);
			if (subNameValue != null) {
				boolean entryFound = false;
				for (Entry e : entryElements) {
					if (subNameValue.equals(e.getName().getId()) || subNameValue.equals(e.getName().getIdGlycanProcessed())) {
						substrates.add(e);
						entryFound = true;
					}
				}
				if (!entryFound) {
					// subNameValue
					Id neId = new Id(subNameValue);
					KeggId neKId = new KeggId(subNameValue);
					if (subNameValue.startsWith("cpd:") || subNameValue.startsWith("gl:") || subNameValue.startsWith("glycan:")) {
						Entry ne = new Entry(neId, neKId, EntryType.hiddenCompound, null, null, null, null, null);
						ne.setSourcePathwayKeggId(clusterIdForHiddenCompounds);
						substrates.add(ne);
						entryElements.add(ne);
						// ErrorMsg.addErrorMessage("Invalid substrate ID used in Reaction element, not defined as Entry element (Dummy Element has been created): "+subNameValue);
					} else {
						ErrorMsg.addErrorMessage("Invalid substrate ID used in Reaction element, not defined as Entry element: " + subNameValue);
					}
				}
			}
		}
		
		Collection<Entry> products = new ArrayList<Entry>();
		List<?> productElements = reactionElement.getChildren("product");
		for (Object o : productElements) {
			Element productElement = (Element) o;
			String prodNameValue = KGMLhelper.getAttributeValue(productElement, "name", null);
			if (prodNameValue != null) {
				boolean entryFound = false;
				for (Entry e : entryElements) {
					if (prodNameValue.equals(e.getName().getId()) || prodNameValue.equals(e.getName().getIdGlycanProcessed())) {
						products.add(e);
						entryFound = true;
					}
				}
				if (!entryFound) {
					// prodNameValue
					Id neId = new Id(prodNameValue);
					KeggId neKId = new KeggId(prodNameValue);
					if (prodNameValue.startsWith("cpd:") || prodNameValue.startsWith("gl:") || prodNameValue.startsWith("glycan:")) {
						Entry ne = new Entry(neId, neKId, EntryType.hiddenCompound, null, null, null, null, null);
						ne.setSourcePathwayKeggId(clusterIdForHiddenCompounds);
						products.add(ne);
						entryElements.add(ne);
						// ErrorMsg.addErrorMessage("Invalid substrate ID used in Reaction element, not defined as Entry element (Dummy Element has been created): "+prodNameValue);
					} else {
						ErrorMsg.addErrorMessage("Invalid substrate ID used in Reaction element, not defined as Entry element: " + prodNameValue);
					}
				}
			}
		}
		
		Reaction result = new Reaction(name, type, substrates, products, KGMLhelper.getAttributeValue(reactionElement, "id", null));
		
		for (Entry e : entryElements) {
			if (e.getReactions() != null) {
				for (KeggId r : e.getReactions()) {
					if (r.getId().equals(result.getId())) {
						r.setReference(result);
					} else
						// in KGML entry one can have an attribute reaction="rn:R####1 rn:R####2"
						// this is stored as two ids (in reactionRef.getId(), two KEGG ids) rn:R####1 and rn:R####2
						// in KGML file the according reaction has an attribute name="rn:R####1 rn:R####2"
						// this is stored as one id (in r.getId()) rn:R####1 rn:R####2
						// equal does not work here
						// this kind of entries and reactions can occur several times
						// compare also entry element id and reaction element id from KGML to get the right reference
						if (result.getId().contains(r.getId()) &&
								result.getElementID() != null && result.getElementID().equals(e.getId().getValue()))
							r.setReference(result);
				}
			}
		}
		return result;
	}
	
	public Element getKgmlReactionElement() {
		Element reactionElement = new Element("reaction");
		KGMLhelper.addNewAttribute(reactionElement, "name", name);
		KGMLhelper.addNewAttribute(reactionElement, "type", type.toString());
		HashSet<String> knownSubs = new HashSet<String>();
		if (allPossibleSubstrates != null) {
			for (Entry e : allPossibleSubstrates) {
				// don't add the same substrate twice
				if (!knownSubs.contains(e.getName().getId())) {
					Element substrateElement = new Element("substrate");
					KGMLhelper.addNewAttribute(substrateElement, "name", e.getName().getId());
					reactionElement.addContent(substrateElement);
					knownSubs.add(e.getName().getId());
				}
			}
		}
		HashSet<String> knownProd = new HashSet<String>();
		if (allPossibleProducts != null) {
			for (Entry e : allPossibleProducts) {
				// don't add product twice
				if (!knownProd.contains(e.getName().getId())) {
					Element productElement = new Element("product");
					KGMLhelper.addNewAttribute(productElement, "name", e.getName().getId());
					reactionElement.addContent(productElement);
					knownProd.add(e.getName().getId());
				}
			}
		}
		return reactionElement;
	}
	
	public Collection<Entry> getEntriesRepresentingThisReaction(Collection<Entry> entryElements) {
		Collection<Entry> entriesRepresentingThisReaction = new LinkedHashSet<Entry>();
		for (Entry e : entryElements) {
			if (e.getReactions() != null) {
				for (KeggId r : e.getReactions()) {
					for (String gid : getId().split(" "))
						for (String rid : r.getId().split(" "))
							if ((rid.equals(gid) || r.getReferenceReaction() == this) &&
									(this.elementID != null && this.elementID.equals(e.getId().getValue()))) {
								entriesRepresentingThisReaction.add(e);
							}
				}
			}
		}
		return entriesRepresentingThisReaction;
	}
	
	public Collection<NodeCombination> getDesiredNodeCombinations(
			HashMap<Entry, Node> entry2graphNode,
			Collection<Entry> entryElements,
			Collection<Relation> relationElements,
			Collection<NodeCombination> relationNodeCombinations) {
		Collection<NodeCombination> result = new ArrayList<NodeCombination>();
		Collection<Entry> entriesRepresentingThisReaction = getEntriesRepresentingThisReaction(entryElements);
		if (entriesRepresentingThisReaction.size() > 0) {
			for (Entry reactionEntity : entriesRepresentingThisReaction) {
				Node b = entry2graphNode.get(reactionEntity);
				if (b != null)
					for (Entry substrate : getDistanceSubset(allPossibleSubstrates, b, entry2graphNode)) {
						Node a = entry2graphNode.get(substrate);
						if (a == null) {
							// System.out.println("No Node for "+substrate);
						} else {
							NodeCombination nc = new NodeCombination(a, b, false, true, getType());
							nc.setSourceInformation(substrate.getName().getId(), getId(), null);
							result.add(nc);
						}
					}
			}
			for (Entry reactionEntity : entriesRepresentingThisReaction) {
				Node a = entry2graphNode.get(reactionEntity);
				if (a != null)
					for (Entry product : getDistanceSubset(allPossibleProducts, a, entry2graphNode)) {
						Node b = entry2graphNode.get(product);
						if (b == null) {
							// System.out.println("No Node for "+product);
						} else {
							NodeCombination nc = new NodeCombination(a, b, true, false, getType());
							nc.setSourceInformation(null, getId(), product.getName().getId());
							result.add(nc);
						}
					}
			}
		} else {
			// a reaction without a entry, representing it, thus products and substrates are directly connected
			// there is no enzyme node in-between
			// connect every substrate with every product
			for (Entry substrate : allPossibleSubstrates) {
				Node a = entry2graphNode.get(substrate);
				for (Entry product : allPossibleProducts) {
					Node b = entry2graphNode.get(product);
					if (a != null && b != null) {
						NodeCombination nc = new NodeCombination(a, b, true, true, getType());
						nc.setSourceInformation(substrate.getName().getId(), null, product.getName().getId());
						result.add(nc);
					}
				}
			}
			createMinimumSubset(result);
		}
		return result;
		// return removeDuplicateNodeCombinationsForPrettyView(relationNodeCombinations, result, entry2graphNode);
	}
	
	private void createMinimumSubset(Collection<NodeCombination> result) {
		// only the smallest connection between a specific kegg id source target definition should be returned
		// thus only one edge from the possible representations of a specific source target definition is
		// returned
		HashMap<String, Double> srcTgt2minConnectionLength = new HashMap<String, Double>();
		HashMap<String, NodeCombination> srcTgt2minDistNC = new HashMap<String, NodeCombination>();
		for (NodeCombination nc : result) {
			String a = nc.sourceInformationSub;
			String b = nc.sourceInformationProd;
			String key = a + "->" + b;
			Node na = nc.getNodeA();
			Node nb = nc.getNodeB();
			double dist = getDistance(na, nb);
			if (!srcTgt2minConnectionLength.containsKey(key)) {
				srcTgt2minConnectionLength.put(key, dist);
				srcTgt2minDistNC.put(key, nc);
			} else {
				double currentMinDist = srcTgt2minConnectionLength.get(key);
				if (dist < currentMinDist) {
					srcTgt2minConnectionLength.put(key, dist);
					srcTgt2minDistNC.put(key, nc);
				}
			}
		}
		result.clear();
		result.addAll(srcTgt2minDistNC.values());
	}
	
	public static Collection<Entry> getDistanceSubset(Collection<Entry> substratesOrProducts, Node b, HashMap<Entry, Node> entry2graphNode) {
		Collection<Entry> result = new ArrayList<Entry>();
		for (Entry sOp : substratesOrProducts) {
			Node aOc = entry2graphNode.get(sOp);
			if (aOc == null || b == null)
				result.add(sOp);
			else {
				double distance = getDistance(aOc, b);
				if (Double.isInfinite(distance) || Double.isNaN(distance))
					result.add(sOp);
				else {
					// search for other substrates with smaller distance and same id, if found, this substrate entry does not need to be considered
					boolean smallerFound = false;
					for (Entry sOp_test : substratesOrProducts) {
						if (sOp_test != sOp) {
							if (sOp_test.getName().getId().equals(sOp.getName().getId())) {
								Node aOc_test = entry2graphNode.get(sOp_test);
								double distance_test = getDistance(aOc_test, b);
								if (!Double.isInfinite(distance_test) && !Double.isNaN(distance_test)) {
									if (distance_test < distance)
										smallerFound = true;
								}
							}
						}
					}
					if (!smallerFound)
						result.add(sOp);
				}
			}
		}
		return result;
	}
	
	private static double getDistance(Node a, Node b) {
		Vector2d p1 = AttributeHelper.getPositionVec2d(a);
		Vector2d p2 = AttributeHelper.getPositionVec2d(b);
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
	}
	
	@Override
	public String toString() {
		if (allPossibleSubstrates != null && allPossibleProducts != null)
			return "Reaction Sub " + allPossibleSubstrates.toString() + " Prod " + allPossibleProducts.toString();
		else
			return super.toString();
	}
	
	public static void processEdgeReactionInformation(
			Edge e,
			ArrayList<ReactionAndInfo> reactionsRequestingThisNodeCombination,
			ArrayList<ReactionAndInfo> reactionsRequestingThisInverseNodeCombination) {
		if (reactionsRequestingThisNodeCombination == null)
			reactionsRequestingThisNodeCombination = new ArrayList<ReactionAndInfo>();
		if (reactionsRequestingThisInverseNodeCombination == null)
			reactionsRequestingThisInverseNodeCombination = new ArrayList<ReactionAndInfo>();
		for (int i = 0; i < reactionsRequestingThisNodeCombination.size() + reactionsRequestingThisInverseNodeCombination.size(); i++) {
			ReactionAndInfo ri;
			if (i < reactionsRequestingThisNodeCombination.size())
				ri = reactionsRequestingThisNodeCombination.get(i);
			else
				ri = reactionsRequestingThisInverseNodeCombination.get(i - reactionsRequestingThisNodeCombination.size());
			if (ri.isProductReq() && ri.isSubstrateReq()) {
				String prodId, substId;
				prodId = ri.getProdId();
				substId = ri.getSubstId();
				KeggGmlHelper.setKeggReactionProduct(e, i, ri.getReaction().getId() + ";" + prodId);
				KeggGmlHelper.setKeggReactionSubstrate(e, i, ri.getReaction().getId() + ";" + substId);
				KeggGmlHelper.setKeggReactionType(e, i, ri.getReaction().getType().toString());
			} else {
				if (ri.isProductReq())
					KeggGmlHelper.setKeggReactionProduct(e, i, ri.getReaction().getId());
				if (ri.isSubstrateReq())
					KeggGmlHelper.setKeggReactionSubstrate(e, i, ri.getReaction().getId());
			}
		}
	}
	
	public static Collection<Reaction> getReactionElementsFromGraphEdges(
			Collection<Entry> entries,
			Collection<Edge> graphEdges,
			Collection<Gml2PathwayWarningInformation> warnings,
			Collection<Gml2PathwayErrorInformation> errors) {
		
		ArrayList<String> allReactionIDs = new ArrayList<String>();
		ArrayList<ReactionType> allReactionTypes = new ArrayList<ReactionType>();
		ArrayList<Edge> allReactionGraphEdges = new ArrayList<Edge>();
		ArrayList<Entry> allSubstrateEntries = new ArrayList<Entry>();
		ArrayList<Entry> allProductEntries = new ArrayList<Entry>();
		
		for (Edge e : graphEdges) {
			ArrayList<IndexAndString> rps = KeggGmlHelper.getKeggReactionProducts(e);
			ArrayList<IndexAndString> rss = KeggGmlHelper.getKeggReactionSubstrates(e);
			// only reaction definitions where product and substrate as well as a reaction type is
			// defined define a complete reaction without a enzyme in-between
			for (IndexAndString productInfo : rps) {
				String productReactionId = productInfo.getSplitValue(";", 0);
				String productReactionValue = productInfo.getSplitValue(";", 1);
				// if (productReactionId==null)
				// errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_ID_INVALID, e));
				// if (productReactionValue==null)
				// errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_PRODUCT_INVALID, e));
				if (productReactionId != null && productReactionValue != null)
					for (IndexAndString substrateInfo : rss) {
						String substrateReactionId = substrateInfo.getSplitValue(";", 0);
						String substrateReactionValue = substrateInfo.getSplitValue(";", 1);
						if (substrateReactionId == null)
							errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_ID_INVALID, e));
						if (substrateReactionValue == null)
							errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_SUBSTRATE_INVALID, e));
						if (substrateReactionId != null && substrateReactionValue != null)
							if (productInfo.getIndex() == substrateInfo.getIndex()
									&& productReactionId.equals(substrateReactionId)) {
								String reactionTypeValue = KeggGmlHelper.getKeggReactionType(e, productInfo.getIndex());
								if (reactionTypeValue != null && reactionTypeValue.length() > 0) {
									ReactionType reactionType = ReactionType.getReactiontype(reactionTypeValue);
									if (reactionType == null) {
										errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_TYPE_INVALID, e));
									} else {
										// search entries for product and substrate
										Entry productEntry = null;
										Entry substrateEntry = null;
										for (Entry entry : entries) {
											if (entry.getSourceGraphNode() == e.getSource() || entry.getSourceGraphNode() == e.getTarget()) {
												String keggID = entry.getName().getId();
												if (keggID.equals(productReactionValue)) {
													if (productEntry != null)
														errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_PRODUCT_INVALID_MORE_THAN_ONE_ENTRY_FITS, e));
													productEntry = entry;
												}
												if (keggID.equals(substrateReactionValue)) {
													if (substrateEntry != null)
														errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_SUBSTRATE_INVALID_MORE_THAN_ONE_ENTRY_FITS, e));
													substrateEntry = entry;
												}
											}
										}
										if (substrateEntry == null || productEntry == null) {
											if (substrateEntry == null)
												errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_SUBSTRATE_INVALID, e));
											if (productEntry == null)
												errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_PRODUCT_INVALID, e));
										} else {
											allReactionIDs.add(productInfo.getSplitValue(";", 0));
											allReactionTypes.add(reactionType);
											allReactionGraphEdges.add(e);
											allProductEntries.add(productEntry);
											allSubstrateEntries.add(substrateEntry);
										}
									}
								}
							}
					}
			}
		}
		
		Collection<Reaction> result = new ArrayList<Reaction>();
		HashSet<String> condensedReactionIds = new HashSet<String>();
		condensedReactionIds.addAll(allReactionIDs);
		for (String uniqueReactionID : condensedReactionIds) {
			ArrayList<ReactionType> relevantReactionTypes = new ArrayList<ReactionType>();
			ArrayList<Edge> relevantGraphEdges = new ArrayList<Edge>();
			HashSet<Entry> relevantSubstrates = new HashSet<Entry>();
			HashSet<Entry> relevantProducts = new HashSet<Entry>();
			for (int i = 0; i < allReactionIDs.size(); i++) {
				String reactionID = allReactionIDs.get(i);
				if (reactionID.equals(uniqueReactionID)) {
					relevantSubstrates.add(allSubstrateEntries.get(i));
					relevantProducts.add(allProductEntries.get(i));
					ReactionType reactionType = allReactionTypes.get(i);
					Edge graphEdge = allReactionGraphEdges.get(i);
					relevantGraphEdges.add(graphEdge);
					relevantReactionTypes.add(reactionType);
				}
			}
			boolean irrever = false;
			boolean revers = false;
			for (ReactionType rt : relevantReactionTypes) {
				if (rt == null)
					continue;
				if (rt == ReactionType.irreversible)
					irrever = true;
				if (rt == ReactionType.reversible)
					revers = true;
			}
			ReactionType reactionType = null;
			if (!irrever && !revers) {
				errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_TYPE_NOTSET, (ArrayList) relevantGraphEdges));
				ErrorMsg.addErrorMessage("No reaction type has been set for reaction " + uniqueReactionID + "! Using setting of reversible reaction instead.");
				irrever = false;
				revers = true;
			}
			if (irrever && revers) {
				errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_TYPE_DIFFERS_AMONG_NODES, (ArrayList) relevantGraphEdges));
				ErrorMsg.addErrorMessage("No unique reaction type (reversible/irreversible) for reaction " + uniqueReactionID
						+ "! Using setting of reversible reaction instead.");
				irrever = false;
				revers = true;
			}
			if (irrever)
				reactionType = ReactionType.irreversible;
			if (revers)
				reactionType = ReactionType.reversible;
			Reaction r = new Reaction(uniqueReactionID, reactionType, relevantSubstrates, relevantProducts);
			result.add(r);
		}
		return result;
	}
	
	public static Collection<Reaction> getReactionElementsFromGraphNodes(
			Collection<Entry> entries,
			List<Node> graphNodes,
			Collection<Gml2PathwayWarningInformation> warnings,
			Collection<Gml2PathwayErrorInformation> errors) {
		ArrayList<String> allReactionIDs = new ArrayList<String>();
		ArrayList<String> allReactionTypes = new ArrayList<String>();
		ArrayList<Node> allReactionGraphNodes = new ArrayList<Node>();
		HashMap<String, HashSet<String>> reactionId2substrates = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> reactionId2products = new HashMap<String, HashSet<String>>();
		
		// two types of reaction definitions in the Graph are possible
		// 1) Reactions catalyzed by an enzyme
		// 2) Reactions without a enzyme representation
		// in case 1 a reaction is defined by a node and two edges, connected to a node
		// also other nodes with the same reaction id are processed (see condensedReactionIDs),
		// so that products and substrates are enumerated for both nodes and merged into one
		// reaction specification.
		// case 2 is processed in another method
		
		// enumerate reactions and corresponding substrates and products from the whole graph network
		for (Node graphNode : graphNodes) {
			ArrayList<IndexAndString> reactionIds = KeggGmlHelper.getKeggReactions(graphNode);
			for (IndexAndString ias : reactionIds) {
				String reactionID = ias.getValue();
				if (reactionID != null && reactionID.length() > 0) {
					String reactionType = KeggGmlHelper.getKeggReactionType(graphNode, ias.getIndex());
					if (reactionType == null) {
						errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_TYPE_MISSING, graphNode));
					} else {
						allReactionIDs.add(reactionID);
						allReactionTypes.add(reactionType);
						allReactionGraphNodes.add(graphNode);
						// enumerate substrate and product
						// only compounds are considered to be valid substrates and products
						Collection<Node> inNodes = graphNode.getNeighbors();
						Collection<Node> outNodes = graphNode.getNeighbors();
						inNodes = getSubSetValidSubstratesOrProducts(true, graphNode, reactionID, inNodes);
						outNodes = getSubSetValidSubstratesOrProducts(false, graphNode, reactionID, outNodes);
						for (Node inNode : inNodes) {
							String keggID = KeggGmlHelper.getKeggId(inNode);
							if (keggID.startsWith("cpd:") || keggID.startsWith("gl:") || keggID.startsWith("glycan:")) {
								if (!reactionId2substrates.containsKey(reactionID))
									reactionId2substrates.put(reactionID, new HashSet<String>());
								reactionId2substrates.get(reactionID).add(keggID);
							}
						}
						for (Node outNode : outNodes) {
							String keggID = KeggGmlHelper.getKeggId(outNode);
							if (keggID.startsWith("cpd:") || keggID.startsWith("gl:") || keggID.startsWith("glycan:")) {
								if (!reactionId2products.containsKey(reactionID))
									reactionId2products.put(reactionID, new HashSet<String>());
								reactionId2products.get(reactionID).add(keggID);
							}
						}
					}
				}
			}
		}
		
		// reaction ids and their products and substrates are enumerated
		// now create reaction elements
		Collection<Reaction> result = processAndGetReactionElements(entries,
				errors, allReactionIDs, allReactionTypes,
				allReactionGraphNodes, reactionId2substrates,
				reactionId2products);
		return result;
	}
	
	private static Collection<Reaction> processAndGetReactionElements(
			Collection<Entry> entries,
			Collection<Gml2PathwayErrorInformation> errors,
			ArrayList<String> allReactionIDs,
			ArrayList<String> allReactionTypes,
			ArrayList<Node> allReactionGraphNodes,
			HashMap<String, HashSet<String>> reactionId2substrates,
			HashMap<String, HashSet<String>> reactionId2products) {
		Collection<Reaction> result = new ArrayList<Reaction>();
		
		HashSet<String> condensedReactionIds = new HashSet<String>();
		condensedReactionIds.addAll(allReactionIDs);
		
		for (String uniqueReactionID : condensedReactionIds) {
			ArrayList<ReactionType> reactionTypes = new ArrayList<ReactionType>();
			ArrayList<Node> relevantGraphNodes = new ArrayList<Node>();
			ReactionType reactionType = processAndGetReactionType(errors,
					allReactionIDs, allReactionTypes, allReactionGraphNodes,
					uniqueReactionID, reactionTypes, relevantGraphNodes);
			
			HashSet<String> substrateIds = reactionId2substrates.get(uniqueReactionID);
			HashSet<String> productIds = reactionId2products.get(uniqueReactionID);
			
			if (substrateIds == null)
				substrateIds = new HashSet<String>();
			if (productIds == null)
				productIds = new HashSet<String>();
			ArrayList<Entry> substrates = new ArrayList<Entry>();
			ArrayList<Entry> products = new ArrayList<Entry>();
			processSubstrateAndProductIds(entries, errors, relevantGraphNodes,
					substrateIds, productIds, substrates, products);
			
			Reaction r = new Reaction(uniqueReactionID, reactionType, substrates, products);
			for (Entry e : entries) {
				if (e.getReactions() != null)
					for (KeggId rkid : e.getReactions()) {
						if (rkid.getId().equals(uniqueReactionID)) {
							rkid.setReference(r);
						}
					}
			}
			result.add(r);
		}
		return result;
	}
	
	private static ReactionType processAndGetReactionType(
			Collection<Gml2PathwayErrorInformation> errors,
			ArrayList<String> allReactionIDs,
			ArrayList<String> allReactionTypes,
			ArrayList<Node> allReactionGraphNodes, String uniqueReactionID,
			ArrayList<ReactionType> reactionTypes,
			ArrayList<Node> relevantGraphNodes) {
		for (int i = 0; i < allReactionIDs.size(); i++) {
			String reactionID = allReactionIDs.get(i);
			if (reactionID.equals(uniqueReactionID)) {
				String reactionTypeValue = allReactionTypes.get(i);
				Node graphNode = allReactionGraphNodes.get(i);
				relevantGraphNodes.add(graphNode);
				ReactionType reactionType = ReactionType.getReactiontype(reactionTypeValue);
				if (reactionType == null) {
					errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_TYPE_INVALID, graphNode));
				}
				reactionTypes.add(reactionType);
			}
		}
		boolean irrever = false;
		boolean revers = false;
		for (ReactionType rt : reactionTypes) {
			if (rt == null)
				continue;
			if (rt == ReactionType.irreversible)
				irrever = true;
			if (rt == ReactionType.reversible)
				revers = true;
		}
		ReactionType reactionType = null;
		if (!irrever && !revers) {
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_TYPE_NOTSET, (Attributable) relevantGraphNodes));
			ErrorMsg.addErrorMessage("No reaction type has been set for reaction " + uniqueReactionID + "! Using setting of reversible reaction instead.");
			irrever = false;
			revers = true;
		}
		if (irrever && revers) {
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.REACTION_TYPE_DIFFERS_AMONG_NODES, (Attributable) relevantGraphNodes));
			ErrorMsg.addErrorMessage("No unique reaction type (reversible/irreversible) for reaction " + uniqueReactionID
					+ "! Using setting of reversible reaction instead.");
			irrever = false;
			revers = true;
		}
		if (irrever)
			reactionType = ReactionType.irreversible;
		if (revers)
			reactionType = ReactionType.reversible;
		return reactionType;
	}
	
	private static void processSubstrateAndProductIds(
			Collection<Entry> entries,
			Collection<Gml2PathwayErrorInformation> errors,
			ArrayList<Node> relevantGraphNodes, HashSet<String> substrateIds,
			HashSet<String> productIds, ArrayList<Entry> substrates,
			ArrayList<Entry> products) {
		for (Entry e : entries) {
			if (substrateIds.contains(e.getName().getId()))
				substrates.add(e);
		}
		// if (substrates.size()<=0)
		// errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.ENTRY_FOR_SUBSTRATE_NOT_FOUND, (ArrayList)relevantGraphNodes,
		// "Substrate ID: "+substrateId));
		for (Entry e : entries) {
			if (productIds.contains(e.getName().getId()))
				products.add(e);
		}
		// if (products.size()<=0)
		// errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.ENTRY_FOR_PRODUCT_NOT_FOUND, (ArrayList)relevantGraphNodes, "Product ID: "+productId));
	}
	
	/**
	 * Analyze connectivity of <code>graphNode</code> to <code>neighbourNodes</code>.
	 * If there is a edge, containing the specified <code>reactionID</code>, the neighbor node
	 * will be included in the return set. Otherwise, not.
	 * 
	 * @param graphNode
	 * @param reactionID
	 *           Look for connecting edges with this reactionID
	 * @param neighbourNodes
	 *           Check these nodes for connectivity to graphNode
	 * @return A subset of the neighbourNodes
	 */
	private static Collection<Node> getSubSetValidSubstratesOrProducts(boolean includeSubstrates, Node graphNode, String reactionID,
			Collection<Node> neighbourNodes) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node nN : neighbourNodes) {
			boolean found = false;
			for (Edge e : graphNode.getEdges()) {
				if (e.getSource() == nN || e.getTarget() == nN) {
					ArrayList<IndexAndString> rl;
					if (includeSubstrates)
						rl = KeggGmlHelper.getKeggReactionSubstrates(e);
					else
						rl = KeggGmlHelper.getKeggReactionProducts(e);
					for (IndexAndString ias : rl) {
						if (ias.getValue().equals(reactionID)) {
							found = true;
							break;
						}
					}
					if (found)
						break;
				}
			}
			if (found)
				result.add(nN);
		}
		return result;
	}
	
	public String toStringWithDetails(boolean html, boolean fullHtml) {
		if (html) {
			if (fullHtml)
				return "<html><b>" + name + "</b> (" + type.toString() + ")" +
						"<br>&nbsp;" +
						"substrates: " + allPossibleSubstrates.toString() + "" +
						"<br>&nbsp;" +
						"products: " + allPossibleProducts.toString();
			else
				return "<html><b>" + name + "</b> (" + type.toString() + ")" +
						"<br>&nbsp;" +
						allPossibleSubstrates.size() + " substrate(s)" +
						"<br>&nbsp;" +
						allPossibleProducts.size() + " product(s)";
		} else {
			if (fullHtml)
				return name + " (" + type.toString() + "), sub: " + allPossibleSubstrates.toString() + ", prod: " + allPossibleProducts.toString();
			else
				return name + " (" + type.toString() + "), sub: " + allPossibleSubstrates.size() + ", prod: " + allPossibleProducts.size();
		}
	}
	
	public void setType(ReactionType reactionType) {
		if (reactionType == null) {
			ErrorMsg.addErrorMessage("Invalid Reaction Type for Reaction " + getId() + "!");
		}
		this.type = reactionType;
	}
	
	public void setSubstrates(Collection<Entry> substrates) {
		this.allPossibleSubstrates = substrates;
	}
	
	public void setProducts(Collection<Entry> products) {
		this.allPossibleProducts = products;
	}
	
	public void removePossibleEntry(Entry invalidEntry) {
		if (allPossibleSubstrates != null)
			allPossibleSubstrates.remove(invalidEntry);
		if (allPossibleProducts != null)
			allPossibleProducts.remove(invalidEntry);
	}
}

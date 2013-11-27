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
import java.util.List;

import org.ErrorMsg;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.RelationType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.SubtypeName;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.SubtypeValue;

public class Relation {
	private IdRef entry1;
	private IdRef entry2;
	private RelationType type;
	private Collection<Subtype> subtypes;
	
	private boolean valid = true;
	
	public Relation(IdRef entry1, IdRef entry2, RelationType type, Collection<Subtype> subtypes) {
		// assert entry1!=null;
		// assert entry2!=null;
		// assert type!=null;
		
		this.entry1 = entry1;
		this.entry2 = entry2;
		this.type = type;
		
		this.subtypes = subtypes;
		
		for (IdRef ir : getSubtypeRefs())
			if (ir.getRef() == entry1.getRef() || ir.getRef() == entry2.getRef())
				valid = false;
		/*
		 * if (subtypes!=null) {
		 * for (Subtype st : subtypes) {
		 * assert (type!=RelationType.ECrel
		 * || ( (st.getName()==SubtypeName.compound) ||
		 * (st.getName()==SubtypeName.hiddenCompound) ));
		 * assert (type!=RelationType.PPrel
		 * || ( (st.getName()==SubtypeName.compound) ||
		 * (st.getName()==SubtypeName.activation) ||
		 * (st.getName()==SubtypeName.inhibition) ||
		 * (st.getName()==SubtypeName.indirectEffect) ||
		 * (st.getName()==SubtypeName.stateChange) ||
		 * (st.getName()==SubtypeName.binding_association) ||
		 * (st.getName()==SubtypeName.dissociation) ||
		 * (st.getName()==SubtypeName.phosphorylation) ||
		 * (st.getName()==SubtypeName.dephosphorylation) ||
		 * (st.getName()==SubtypeName.glycosylation) ||
		 * (st.getName()==SubtypeName.ubiquination) ||
		 * (st.getName()==SubtypeName.methylation)
		 * ));
		 * assert (type!=RelationType.GErel
		 * || ( (st.getName()==SubtypeName.expression) ||
		 * (st.getName()==SubtypeName.repression) ||
		 * (st.getName()==SubtypeName.indirectEffect)
		 * ));
		 * }
		 * }
		 */
	}
	
	public static String getTypeDescription(boolean html) {
		if (html)
			return "<html>The relation element specifies relationship between two proteins (gene products) or two KOs<br>" +
								"(ortholog groups) or protein and compound, which is indicated by an arrow or a line connecting<br>" +
								"two nodes in the KEGG pathways. The relation element has a subelement named the subtype<br>" +
								"element. When the name attribute value of the subtype element is a value with directionality<br>" +
								"like &quot;activation&quot;, the direction of the interaction is from entry1 to entry2.";
		else
			return "The relation element specifies relationship between two proteins (gene products) or two KOs " +
								"(ortholog groups) or protein and compound, which is indicated by an arrow or a line connecting " +
								"two nodes in the KEGG pathways. The relation element has a subelement named the subtype " +
								"element. When the name attribute value of the subtype element is a value with directionality " +
								"like \"activation\", the direction of the interaction is from entry1 to entry2.";
	}
	
	public static Relation getRelationFromKgmlRelationElement(Element relationElement,
						Collection<Entry> entryElements, String clusterIdForHiddenCompounds) {
		try {
			String relation1value = KGMLhelper.getAttributeValue(relationElement, "entry1", null);
			String relation2value = KGMLhelper.getAttributeValue(relationElement, "entry2", null);
			String typeValue = KGMLhelper.getAttributeValue(relationElement, "type", null);
			
			IdRef entry1 = IdRef.getId(relation1value);
			IdRef entry2 = IdRef.getId(relation2value);
			
			for (Entry e : entryElements) {
				if (e.getId().matches(entry1.getValue())) {
					entry1.setRef(e);
					break;
				}
			}
			for (Entry e : entryElements) {
				if (e.getId().matches(entry2.getValue())) {
					entry2.setRef(e);
					break;
				}
			}
			
			RelationType type = RelationType.getRelationType(typeValue);
			
			Collection<Subtype> subtypes = new ArrayList<Subtype>();
			List<?> subtypeElements = relationElement.getChildren("subtype");
			for (Object o : subtypeElements) {
				Element subtypeElement = (Element) o;
				Subtype subtype = Subtype.getSubtypeFromKgmlSubtypeElement(subtypeElement, entryElements, clusterIdForHiddenCompounds);
				if (subtype != null)
					subtypes.add(subtype);
			}
			
			Relation result = new Relation(entry1, entry2, type, subtypes);
			return result;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	public Element getKgmlRelationElement() {
		Element relationElement = new Element("relation");
		KGMLhelper.addNewAttribute(relationElement, "entry1", entry1.toString());
		KGMLhelper.addNewAttribute(relationElement, "entry2", entry2.toString());
		KGMLhelper.addNewAttribute(relationElement, "type", type.toString());
		if (subtypes != null) {
			for (Subtype st : subtypes) {
				Element subtypeElement = st.getKgmlSubtypeElement();
				if (subtypeElement != null)
					relationElement.addContent(subtypeElement);
			}
		}
		return relationElement;
	}
	
	public Collection<NodeCombination> getDesiredNodeCombinations(
						HashMap<Entry, Node> entry2graphNode,
						Collection<Entry> entries) {
		Collection<NodeCombination> result = new ArrayList<NodeCombination>();
		Entry e1 = entry1.getRef();
		Entry e2 = entry2.getRef();
		Node a = entry2graphNode.get(e1);
		Node b = entry2graphNode.get(e2);
		if (a == null || b == null) {
			// ErrorMsg.addErrorMessage("One or both related corresponding graph nodes are not found for " +
			// "this relation! (entry ids "+entry1+" -> "+entry2+")<br>Relation: "+toStringWithKeggNames());
		} else {
			if (subtypes == null || subtypes.size() <= 0) {
				result.add(new NodeCombination(a, b, false, false, null));
			} else {
				boolean notAdded = false;
				for (Subtype st : subtypes) {
					if (st.getValue() != null) {
						Node m = entry2graphNode.get(st.getValue().getRef());
						if (m == null) {
							System.out.println("No graph node for relation-subtype element! (value " + st.getValue().getValue() + ") - relation: id "
												+ e1.getId().getValue() + " -> " + e2.getId().getValue() + ")");
							// ErrorMsg.addErrorMessage("No graph node for relation-subtype element! (value "+st.getValue().getValue()+") - relation: id "+e1.getId().getValue()+" -> "+e2.getId().getValue()+")");
						} else {
							NodeCombination e1_m = new NodeCombination(a, m, false, false, null);
							NodeCombination m_e2 = new NodeCombination(m, b, false, false, null);
							result.add(e1_m);
							result.add(m_e2);
						}
					} else {
						notAdded = true;
					}
				}
				if (notAdded) {
					NodeCombination e1_e2 = new NodeCombination(a, b, false, false, null);
					result.add(e1_e2);
				}
			}
		}
		return result;
	}
	
	public RelationType getType() {
		return type;
	}
	
	public void setType(RelationType type) {
		this.type = type;
	}
	
	public static void processEdgeRelationInformation(
						Edge e,
						ArrayList<Relation> relationsRequestingThisNodeCombination) {
		boolean isIndirectRelationDefiningEdge = false;
		HashSet<SubtypeName> subtypes = new HashSet<SubtypeName>();
		HashSet<RelationType> relationTypes = new HashSet<RelationType>();
		for (int i = 0; i < relationsRequestingThisNodeCombination.size(); i++) {
			Relation r = relationsRequestingThisNodeCombination.get(i);
			relationTypes.add(r.getType());
			if (r.getSubtypes() != null)
				for (SubtypeName stn : r.getSubtypes()) {
					if (stn == SubtypeName.indirect)
						isIndirectRelationDefiningEdge = true;
				}
			IdRef e1 = r.entry1;
			IdRef e2 = r.entry2;
			ArrayList<SubtypeName> subtypeNames = r.getSubtypes();
			KeggGmlHelper.setRelationTypeInformation(e, i, r.getType());
			KeggGmlHelper.setRelationSrcTgtInformation(e, i, e1, e2);
			if (subtypeNames.size() > 1) {
				StringBuilder sb = new StringBuilder();
				for (SubtypeName sn : subtypeNames)
					if (sn != null)
						sb.append(sn.toString() + ";");
				String res = sb.toString();
				if (res.length() > 0)
					res = res.substring(0, res.length() - 1);
				KeggGmlHelper.setRelationSubtypeName(e, i, res);
			} else
				if (subtypeNames.size() == 1) {
					if (subtypeNames.get(0) != null)
						KeggGmlHelper.setRelationSubtypeName(e, i, subtypeNames.get(0).toString());
				}
			subtypes.addAll(subtypeNames);
		}
		
		String lbl = "";
		for (SubtypeName stn : subtypes)
			lbl = addLbl(lbl, getEdgeLbl(stn), ",");
		if (lbl.length() > 0) {
			KeggGmlHelper.setEdgeLabel(e, lbl);
		}
		
		boolean mapLinked = false;
		if (KeggGmlHelper.getKeggType(e.getSource()).equals("map"))
			mapLinked = true;
		if (KeggGmlHelper.getKeggType(e.getTarget()).equals("map"))
			mapLinked = true;
		boolean srcIsCompound = KeggGmlHelper.getKeggType(e.getSource()).equals("compound") ||
							KeggGmlHelper.getKeggType(e.getSource()).equals("glycan");
		boolean tgtIsCompound = KeggGmlHelper.getKeggType(e.getTarget()).equals("compound") ||
							KeggGmlHelper.getKeggType(e.getTarget()).equals("glycan");
		if (srcIsCompound && tgtIsCompound && relationTypes.size() == 1 && relationTypes.iterator().next() == RelationType.maplink)
			mapLinked = true;
		
		if (mapLinked)
			KeggGmlHelper.setKeggGraphicsLineStyleMap(e);
		
		if (isIndirectRelationDefiningEdge)
			KeggGmlHelper.setKeggGraphicsLineStyleIndirect(e);
		
		if (subtypes.contains(SubtypeName.inhibition) || subtypes.contains(SubtypeName.repression))
			KeggGmlHelper.setKeggGraphicsLineStyleInhibitionArrow(e);
	}
	
	private static String getEdgeLbl(SubtypeName fromName) {
		return SubtypeValue.getValue(fromName);
		/*
		 * switch(fromName) {
		 * case activation : return "+a";
		 * case inhibition : return "--|";
		 * case expression : return "e";
		 * case repression : return "r";
		 * case indirectEffect : return "..>";
		 * case stateChange : return "...";
		 * case binding_association : return "---";
		 * case dissociation : return "-+-";
		 * case phosphorylation : return "+p";
		 * case dephosphorylation : return "-p";
		 * case glycosylation : return "+g";
		 * case ubiquination : return "+u";
		 * case methylation : return "+m";
		 * case demethylation : return "-m";
		 * }
		 * return null;
		 */
	}
	
	public boolean isDirectedRelationDefinedBySubtypes() {
		if (subtypes == null || subtypes.size() == 0)
			return false;
		else {
			boolean noReq = false;
			boolean yesReq = false;
			for (Subtype st : subtypes) {
				if (st.getName() == SubtypeName.binding_association)
					noReq = true;
				else
					if (st.getName() == SubtypeName.compound)
						noReq = true;
					else
						yesReq = true;
			}
			return yesReq && !noReq;
		}
		/* return subtypes.contains(SubtypeName.phosphorylation); */
	}
	
	private static String addLbl(String lbl, String addThis, String divisor) {
		if (addThis != null && addThis.length() > 0) {
			if (lbl.length() > 0)
				lbl = lbl + divisor + addThis;
			else
				lbl = addThis;
		}
		return lbl;
	}
	
	public ArrayList<SubtypeName> getSubtypes() {
		ArrayList<SubtypeName> result = new ArrayList<SubtypeName>();
		for (Subtype st : subtypes) {
			result.add(st.getName());
		}
		return result;
	}
	
	public void addSubtypeName(SubtypeName stn) {
		boolean found = false;
		for (Subtype st : subtypes) {
			if (st.getName() == stn) {
				found = true;
				break;
			}
		}
		if (!found)
			subtypes.add(new Subtype(stn, null));
	}
	
	public void removeSubtypeName(SubtypeName stn) {
		ArrayList<Subtype> del = new ArrayList<Subtype>();
		for (Subtype st : subtypes) {
			if (st.getName() == stn)
				del.add(st);
		}
		for (Subtype d : del)
			subtypes.remove(d);
	}
	
	public boolean hasSubtypeName(SubtypeName stn) {
		boolean found = false;
		for (Subtype st : subtypes) {
			if (st.getName() == stn) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	public ArrayList<IdRef> getSubtypeRefs() {
		ArrayList<IdRef> result = new ArrayList<IdRef>();
		for (Subtype st : subtypes) {
			if (st.getValue() != null)
				result.add(st.getValue());
		}
		return result;
	}
	
	public void addSubtypeRef(Entry e) {
		if (e == null)
			return;
		for (IdRef r : getSubtypeRefs()) {
			if (r.getRef() == e)
				return;
		}
		IdRef ir = new IdRef(e, e.getId().getValue());
		Subtype st = null;
		if (e.getType() == EntryType.hiddenCompound)
			st = new Subtype(SubtypeName.hiddenCompound, ir);
		if (e.getType() == EntryType.compound)
			st = new Subtype(SubtypeName.compound, ir);
		if (e.getType() == EntryType.ortholog)
			st = new Subtype(SubtypeName.compound, ir);
		if (st != null) {
			subtypes.add(st);
			if (subtypes.size() > 1)
				ErrorMsg.addErrorMessage("Internal Error/Warning: Relation " + toStringWithKeggIDs() + " contains more than one subtype-ref!");
		}
	}
	
	public void removeSubtypeRef(Entry e) {
		if (e == null)
			return;
		ArrayList<Subtype> del = new ArrayList<Subtype>();
		for (Subtype st : subtypes) {
			IdRef r = st.getValue();
			if (r != null)
				if (r.getRef() == e)
					del.add(st);
		}
		for (Subtype st : del)
			subtypes.remove(st);
	}
	
	public static Collection<Relation> getRelationElementsFromGraph(
						Collection<Entry> entries,
						Graph graph,
						Collection<Gml2PathwayWarningInformation> warnings,
						Collection<Gml2PathwayErrorInformation> errors) {
		
		Collection<Relation> result = new ArrayList<Relation>();
		HashSet<String> knownRelations = new HashSet<String>();
		Collection<Edge> edges = graph.getEdges();
		HashMap<Node, HashSet<Entry>> graphNode2Entries = getGraphNode2EntryStructure(entries);
		graph.numberGraphElements();
		
		HashMap<Edge, ArrayList<IndexAndString>> edge2relationTypes = new HashMap<Edge, ArrayList<IndexAndString>>();
		for (Edge e : edges) {
			edge2relationTypes.put(e, KeggGmlHelper.getRelationTypes(e));
		}
		
		for (Edge e : edges) {
			ArrayList<IndexAndString> relationTypes = KeggGmlHelper.getRelationTypes(e);
			for (IndexAndString ias : relationTypes) {
				String relationTypeValue = ias.getValue();
				RelationType relationType = RelationType.getRelationType(relationTypeValue);
				String relationSubtypeNameValue = KeggGmlHelper.getRelationSubtypeName(e, ias.getIndex());
				if (relationType == null) {
					errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.INVALID_RELATION_TYPE, e));
				} else {
					String relationSrcTgt = KeggGmlHelper.getRelationSourceTarget(e, ias.getIndex());
					if (relationSrcTgt != null && relationSrcTgt.length() > 1 && relationSrcTgt.contains("/")) {
						String src = relationSrcTgt.split("/")[0];
						String tgt = relationSrcTgt.split("/")[1];
						Node a = e.getSource();
						Node b = e.getTarget();
						if (src.equals(tgt) && b.getID() < a.getID())
							continue; // avoid dupplicate creation of relations
						String idA = KeggGmlHelper.getKeggId(a);
						String idB = KeggGmlHelper.getKeggId(b);
						Entry eA = getEntry(idA, a, graphNode2Entries);
						Entry eB = getEntry(idB, b, graphNode2Entries);
						boolean relationIdentified = false;
						if (src.equals(idA)) {
							// check edges connected to node B
							Collection<Relation> rr = getRelationElements(
												e, relationSubtypeNameValue, eA, eB, b, relationSrcTgt, src, tgt, entries, warnings, errors, graphNode2Entries,
												edge2relationTypes);
							for (Relation r : rr) {
								if (!knownRelations.contains(r.toStringWithKeggIDs())) {
									knownRelations.add(r.toStringWithKeggIDs());
									result.add(r);
									relationIdentified = true;
								}
							}
						}
						if (src.equals(idB)) {
							// check edges connected to node B
							Collection<Relation> rr = getRelationElements(
												e, relationSubtypeNameValue, eB, eA, a, relationSrcTgt, src, tgt, entries, warnings, errors, graphNode2Entries,
												edge2relationTypes);
							for (Relation r : rr) {
								if (!knownRelations.contains(r.toStringWithKeggIDs())) {
									knownRelations.add(r.toStringWithKeggIDs());
									result.add(r);
									relationIdentified = true;
								}
							}
						}
						if (src.equals(idA) && tgt.equals(idB) && !relationIdentified) {
							Collection<Subtype> subtypes = new ArrayList<Subtype>();
							if (relationSubtypeNameValue != null && relationSubtypeNameValue.contains(";")) {
								String[] subtypeValues = relationSubtypeNameValue.split(";");
								for (String subtypeValue : subtypeValues) {
									SubtypeName stn = SubtypeName.getSubtypeName(subtypeValue);
									Subtype st = getSubtypeRef(eA, eB, stn);
									subtypes.add(st);
								}
							} else
								if (relationSubtypeNameValue != null && relationSubtypeNameValue.length() > 0) {
									String subtypeValue = relationSubtypeNameValue;
									SubtypeName stn = SubtypeName.getSubtypeName(subtypeValue);
									Subtype st = getSubtypeRef(eA, eB, stn);
									subtypes.add(st);
								}
							IdRef r1, r2;
							r1 = new IdRef(eA, eA.getId().getValue());
							r2 = new IdRef(eB, eB.getId().getValue());
							Relation r = new Relation(r1, r2, relationType, subtypes);
							if (r.isValid())
								result.add(r);
						}
					} else {
						errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.RELATION_SRC_OR_TGT_MISSING, e));
					}
				}
			}
		}
		return result;
	}
	
	private boolean isValid() {
		return valid;
	}
	
	private static Subtype getSubtypeRef(Entry eA, Entry eB, SubtypeName stn) {
		Entry compoundSubtypeEntry = null;
		if (stn == SubtypeName.compound) {
			if (eA.getType() == EntryType.compound)
				compoundSubtypeEntry = eA;
			if (eB.getType() == EntryType.compound)
				compoundSubtypeEntry = eB;
		}
		IdRef stref = null;
		if (compoundSubtypeEntry != null)
			stref = new IdRef(compoundSubtypeEntry, compoundSubtypeEntry.getId().getValue());
		Subtype st = new Subtype(stn, stref);
		return st;
	}
	
	private static Collection<Relation> getRelationElements(
						Edge skipThisEdge,
						String searchForThisRelationSubtypeName,
						Entry sourceEntry, Entry subtypeEntry, Node checkThisNodeEdges,
						String searchRelationSrcTgt,
						String srcE1, String tgtE1,
						Collection<Entry> entries,
						Collection<Gml2PathwayWarningInformation> warnings,
						Collection<Gml2PathwayErrorInformation> errors,
						HashMap<Node, HashSet<Entry>> graphNode2Entries,
						HashMap<Edge, ArrayList<IndexAndString>> edge2relationTypes) {
		Collection<Relation> result = new ArrayList<Relation>();
		if (searchForThisRelationSubtypeName == null)
			return result;
		SubtypeName relationSubtypeName = SubtypeName.getSubtypeName(searchForThisRelationSubtypeName);
		if (relationSubtypeName == null)
			return result;
		
		for (Edge e : checkThisNodeEdges.getEdges()) {
			// if (e==skipThisEdge)
			// continue;
			ArrayList<IndexAndString> relationTypes = edge2relationTypes.get(e);
			for (IndexAndString ias : relationTypes) {
				String relationTypeValue = ias.getValue();
				RelationType relationType = RelationType.getRelationType(relationTypeValue);
				if (relationType == null) {
					errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.INVALID_RELATION_TYPE, e));
				} else {
					String relationSrcTgt = KeggGmlHelper.getRelationSourceTarget(e, ias.getIndex());
					if (relationSrcTgt != null && relationSrcTgt.length() > 0 && relationSrcTgt.contains("/")) {
						if (relationSrcTgt.equals(searchRelationSrcTgt)) {
							String srcE2 = relationSrcTgt.split("/")[0];
							String tgtE2 = relationSrcTgt.split("/")[1];
							String relationSubtypeNameValue = KeggGmlHelper.getRelationSubtypeName(e, ias.getIndex());
							if (relationSubtypeNameValue.equals(searchForThisRelationSubtypeName)) {
								// relation source and target are the same
								// also the relation subtypename value is the same
								// thus the second edge defining a relation has been found
								Node a = e.getSource();
								Node b = e.getTarget();
								String idAe2 = KeggGmlHelper.getKeggId(a);
								String idBe2 = KeggGmlHelper.getKeggId(b);
								Entry eAe2 = getEntry(idAe2, a, graphNode2Entries);
								Entry eBe2 = getEntry(idBe2, b, graphNode2Entries);
								// 4 entries from both edges of the two edges:
								// eAe1, eBe2, eAe2, eBe2
								// two entries should be the same, so that three entries define the relation
								Entry srcE = sourceEntry;
								Entry subtE = subtypeEntry;
								Entry tgtE = null;
								if (eAe2 == subtE)
									tgtE = eBe2;
								else
									tgtE = eAe2;
								Collection<Subtype> sts = new ArrayList<Subtype>();
								sts.add(new Subtype(relationSubtypeName, new IdRef(subtE, subtE.getId().getValue())));
								Relation r = new Relation(new IdRef(srcE, srcE.getId().getValue()), new IdRef(tgtE, tgtE.getId().getValue()), relationType, sts);
								boolean valid = ((srcE.getName().getId().equals(srcE2) && tgtE.getName().getId().equals(tgtE2)) ||
													(tgtE.getName().getId().equals(srcE2) && srcE.getName().getId().equals(tgtE2)));
								
								if (!r.entry1.getRef().getId().matches(r.entry2.getRef().getId().getValue()) && valid) {
									result.add(r);
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	private static HashMap<Node, HashSet<Entry>> getGraphNode2EntryStructure(Collection<Entry> entries) {
		HashMap<Node, HashSet<Entry>> result = new HashMap<Node, HashSet<Entry>>();
		for (Entry e : entries) {
			if (!result.containsKey(e.getSourceGraphNode()))
				result.put(e.getSourceGraphNode(), new HashSet<Entry>());
			result.get(e.getSourceGraphNode()).add(e);
		}
		return result;
	}
	
	private static Entry getEntry(String idA, Node a, HashMap<Node, HashSet<Entry>> graphNode2entries) {
		HashSet<Entry> entriesOfGraphNode = graphNode2entries.get(a);
		if (entriesOfGraphNode != null)
			for (Entry e : entriesOfGraphNode) {
				if (e.getName().getId().equals(idA))
					return e;
			}
		return null;
	}
	
	@Override
	public String toString() {
		return "Relation [" + getSourceID() + " > " + getTargetID() + "]";
	}
	
	public String toStringWithKeggIDs() {
		return "Relation [" + entry1.getRef().getName().getId() + " (" + entry1.getRef().getId().getValue() + ") > " + entry2.getRef().getName().getId() + " ("
							+ entry2.getRef().getId().getValue() + ") ; ST: " + subtypes.toString() + "]";
	}
	
	public String toStringWithKeggNames() {
		return "Relation [" + entry1.getRef().getName().getId() + " > " + entry2.getRef().getName().getId() + "; ST: " + subtypes.toString() + "]";
	}
	
	public String toStringWithShortDesc(boolean showLabels) {
		if (showLabels) {
			String subTypes = "";
			if (subtypes != null)
				for (Subtype st : subtypes) {
					subTypes = subTypes + st.getVisibleName() + ";";
				}
			if (subTypes.endsWith(";"))
				subTypes = subTypes.substring(0, subTypes.length() - 1);
			return "<html>" +
								(entry1 == null ? "" : entry1.getRef().getVisibleName()) +
								"&nbsp;->&nbsp;" +
								(entry2 == null ? "" : entry2.getRef().getVisibleName()) +
								"<br>&nbsp;&nbsp;&nbsp;" + subTypes + "";
		} else
			return "<html>" +
								(entry1 == null ? "" : entry1.getRef().getName().getId()) +
								" -> " +
								(entry2 == null ? "" : entry2.getRef().getName().getId()) +
								"<br>&nbsp;&nbsp;&nbsp;" +
								(subtypes == null ? "" : subtypes.toString()) + "";
	}
	
	public String getSourceID() {
		return entry1.getValue();
	}
	
	public String getTargetID() {
		return entry2.getValue();
	}
	
	public Entry getSourceEntry() {
		if (entry1 == null)
			return null;
		else
			return entry1.getRef();
	}
	
	public Entry getTargetEntry() {
		if (entry2 == null)
			return null;
		else
			return entry2.getRef();
	}
	
	public void setSourceEntry(Entry e) {
		this.entry1 = new IdRef(e, e.getId().getValue());
	}
	
	public void setTargetEntry(Entry e) {
		this.entry2 = new IdRef(e, e.getId().getValue());
	}
	
	public String getCSVline() {
		return entry1.getRef().getId().getValue() + "\t" +
							entry1.getRef().getName().getId() + "\t" +
							entry2.getRef().getId().getValue() + "\t" +
							entry2.getRef().getName().getId();
	}
	
	public void removeSubtypes() {
		subtypes.clear();
	}
}

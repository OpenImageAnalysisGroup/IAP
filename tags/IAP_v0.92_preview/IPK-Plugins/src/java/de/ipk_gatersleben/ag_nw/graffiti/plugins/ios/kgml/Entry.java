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
import java.util.Set;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.jdom.Comment;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Id;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Url;

public class Entry {
	private Id id;
	private KeggId name;
	private EntryType type;
	private Url link;
	private Collection<KeggId> reactions;
	private IdRef map;
	private Collection<IdRef> components;
	private Graphics graphics;
	private String sourcePathwayKeggId = null;
	
	private boolean partOfGroup = false;
	
	/**
	 * This value is only set and available in case this entry has been created from a Graph node and
	 * not from a KGML file.
	 */
	private Node sourceGraphNode;
	
	public Entry(
						Id id,
						KeggId name,
						EntryType type,
						Url link,
						IdRef map,
						Collection<KeggId> reactions,
						Collection<IdRef> components,
						Graphics graphics) {
		assert id != null;
		assert name != null;
		assert type != null;
		
		if (type == null)
			return;
		
		// System.out.println("ID "+id+" NAME "+name.getId());
		
		this.id = id;
		this.name = name;
		this.type = type;
		
		this.link = link;
		this.map = map;
		this.reactions = reactions;
		this.components = components;
		this.graphics = graphics;
		
		/*
		 * assert name.isPathwayMap() || name.isKoOrthologGroup() || name.isEcEnzyme() ||
		 * name.isCpdChemicalCompound() || name.isGlGlycan() || name.isGroupComplexOfKOs()
		 * || name.isGeneProductOfGivenOrganism();
		 * if (reactions!=null) {
		 * for (KeggId reaction : reactions) {
		 * assert reaction.isReaction();
		 * }
		 * }
		 */
	}
	
	public void setSourcePathwayKeggId(String id) {
		this.sourcePathwayKeggId = id;
	}
	
	public String getSourcePathwayKeggId() {
		return sourcePathwayKeggId;
	}
	
	public boolean hasGlycanName() {
		return name.isGlGlycan();
	}
	
	public boolean hasCompoundName() {
		return name.isCpdChemicalCompound();
	}
	
	public EntryType getType() {
		return type;
	}
	
	public KeggId getName() {
		return name;
	}
	
	public Id getId() {
		return id;
	}
	
	public IdRef getMapRef() {
		return map;
	}
	
	public void setMapRef(IdRef map) {
		this.map = map;
	}
	
	public Graphics getGraphics() {
		return graphics;
	}
	
	public void setGraphics(Graphics graphics) {
		this.graphics = graphics;
	}
	
	public static Entry getEntryFromKgmlEntryElement(
						Collection<IdRef> mapLinksWhichNeedToBeUpdated,
						Collection<IdRef> componentsWhichNeedToBeUpdated,
						Element entryElement, String sourcePathwayId) {
		String idValue = KGMLhelper.getAttributeValue(entryElement, "id", null);
		Id id = Id.getId(idValue);
		
		String nameValue = KGMLhelper.getAttributeValue(entryElement, "name", null);
		KeggId name = new KeggId(nameValue);
		
		String typeValue = KGMLhelper.getAttributeValue(entryElement, "type", null);
		EntryType type = EntryType.getEntryType(typeValue);
		if (type == null) {
			System.err.println("Invalid type value: " + typeValue + " (Entry " + idValue + ")");
		}
		
		String urlValue = KGMLhelper.getAttributeValue(entryElement, "link", null);
		Url link = Url.getUrl(urlValue);
		
		// reaction element reference to be set later while reading the reaction elements
		String reactionValues = KGMLhelper.getAttributeValue(entryElement, "reaction", null);
		ArrayList<KeggId> reactions = new ArrayList<KeggId>();
		if (reactionValues != null && reactionValues.length() > 0) {
			String[] reactionValueArr = reactionValues.split(" ");
			for (String reactionValue : reactionValueArr) {
				KeggId reaction = KeggId.getKeggId(reactionValue);
				reactions.add(reaction);
			}
		}
		String mapValue = KGMLhelper.getAttributeValue(entryElement, "map", null);
		IdRef map = null;
		if (mapValue != null) {
			map = IdRef.getId(mapValue);
			mapLinksWhichNeedToBeUpdated.add(map);
		}
		
		Collection<IdRef> components = Component.getComponentElementsFromKgmlElement(entryElement.getChildren("component"));
		componentsWhichNeedToBeUpdated.addAll(components);
		Element graphicsElement = entryElement.getChild("graphics");
		Graphics graphics = Graphics.getGraphicsFromKgmlElement(graphicsElement);
		
		// entryElement.addContent(new Comment(getSourcePathwayKeggId()));
		List<?> cl = entryElement.getContent();
		for (Object o : cl) {
			if (o instanceof Comment) {
				Comment c = (Comment) o;
				sourcePathwayId = c.getText();
			}
		}
		
		Entry e = new Entry(id, name, type, link, map, reactions, components, graphics);
		e.setSourcePathwayKeggId(sourcePathwayId);
		name.setReference(e);
		return e;
	}
	
	public Collection<KeggId> getReactions() {
		return reactions;
	}
	
	public void addReaction(KeggId reaction) {
		if (reactions == null)
			reactions = new ArrayList<KeggId>();
		reactions.add(reaction);
	}
	
	public Collection<IdRef> getComponents() {
		return components;
	}
	
	public void addComponent(IdRef component) {
		if (components == null)
			components = new ArrayList<IdRef>();
		components.add(component);
	}
	
	public Url getLink() {
		return link;
	}
	
	public Element getKgmlEntryElement(boolean addSourcePathwayInformation) {
		if (type == EntryType.hiddenCompound)
			return null;
		Element entryElement = new Element("entry");
		KGMLhelper.addNewAttribute(entryElement, "id", id.toString());
		KGMLhelper.addNewAttribute(entryElement, "name", name.getId());
		KGMLhelper.addNewAttribute(entryElement, "type", type.toString());
		if (reactions != null) {
			String reaction = "";
			for (KeggId r : reactions) {
				reaction = reaction + " " + r.getId();
			}
			if (reaction.startsWith(" "))
				reaction = reaction.substring(" ".length());
			KGMLhelper.addNewAttribute(entryElement, "reaction", reaction);
		}
		if (map != null)
			KGMLhelper.addNewAttribute(entryElement, "map", map.toString());
		if (link != null)
			KGMLhelper.addNewAttribute(entryElement, "link", link.toString());
		if (components != null && components.size() > 0) {
			for (IdRef compRef : components) {
				Element componentElement = Component.getKgmlComponentElement(compRef);
				if (componentElement != null)
					entryElement.addContent(componentElement);
			}
		}
		if (graphics != null) {
			Element graphicsElement = graphics.getKgmlGraphicsElement();
			if (graphicsElement != null) {
				entryElement.addContent(graphicsElement);
			}
		}
		if (getSourcePathwayKeggId() != null && addSourcePathwayInformation)
			entryElement.addContent(new Comment(getSourcePathwayKeggId()));
		return entryElement;
	}
	
	public Node addGraphNode(Graph graph) {
		double x = 10; // will be set later
		double y = 10; // will be set later
		Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(x, y));
		if (type == EntryType.genes || type == EntryType.group)
			n.setViewID(-1);
		// private Id id; // it is not clear if this should be saved and restored or implicitly calculated
		KeggGmlHelper.setKeggId(n, name.getId());
		KeggGmlHelper.setKeggType(n, type.toString());
		String cluster = "";
		if (getSourcePathwayKeggId() != null)
			cluster = getSourcePathwayKeggId();
		NodeTools.setClusterID(n, cluster);
		if (link != null)
			KeggGmlHelper.setKeggLinkUrl(n, link.toString());
		if (reactions != null) {
			int i = 0;
			for (KeggId reaction : reactions) {
				KeggGmlHelper.setKeggReaction(n, i, reaction.getId());
				if (reaction.getReferenceReaction() == null) {
					// System.err.println("Reaction "+reaction.getId()+" has no reference!");
				} else
					if (reaction.getReferenceReaction().getType() == null) {
						System.err.println("Reaction " + reaction.getId() + " has no type set!");
					} else
						KeggGmlHelper.setKeggReactionType(n, i, reaction.getReferenceReaction().getType().toString());
				i++;
			}
		}
		
		// if (name.getId().startsWith("path:")) {
		// URLAttribute ua = new URLAttribute("kegg_map_link", name.getId());
		// n.addAttribute(ua, "kegg");
		// }
		boolean hasComponents = (components != null && components.size() > 0);
		
		if (graphics != null) {
			graphics.processNodeDesign(n, hasComponents);
		} else {
			Graphics.processDefaultNodeDesign(n, this);
		}
		// if (map!=null) {
		// String mapID = map.getRef().getName().getId();
		// KeggGmlHelper.setKeggMapLink(n, mapID);
		// }
		
		KeggGmlHelper.setIsPartOfGroup(n, partOfGroup);
		
		return n;
		// private KeggId reaction;
	}
	
	@Override
	public String toString() {
		return getGraphicsTitle("(no title)") + ": " + getType() + " " + getName().getId(); // +" ("+getId().getValue()+")/";
	}
	
	private String getGraphicsTitle(String ifNotAvailable) {
		if (graphics == null || graphics.getName() == null || graphics.getName().length() <= 0)
			return ifNotAvailable;
		return graphics.getName().replaceAll("<br>", "");
	}
	
	public static Collection<Entry> getEntryElementsFromGraphNodes(
						KgmlIdGenerator idGenerator,
						List<Node> nodes,
						Collection<Gml2PathwayWarningInformation> warnings,
						Collection<Gml2PathwayErrorInformation> errors,
						HashMap<Entry, Node> entry2graphNode) {
		Collection<Entry> result = new ArrayList<Entry>();
		// create entry elements from graph nodes
		if (entry2graphNode == null)
			entry2graphNode = new HashMap<Entry, Node>();
		HashMap<Node, Collection<Entry>> graphNode2entries = new HashMap<Node, Collection<Entry>>();
		for (Node graphNode : nodes) {
			Collection<Entry> entriesOfGraphNode = getEntryElementsFromGraphNode(idGenerator, graphNode, warnings, errors);
			for (Entry e : entriesOfGraphNode)
				entry2graphNode.put(e, graphNode);
			graphNode2entries.put(graphNode, entriesOfGraphNode);
			result.addAll(entriesOfGraphNode);
		}
		// process component information:
		// all nodes/entries, which lay graphically inside a group/genes entry/graphnode,
		// are regarded to be components of the group/genes entry
		// so the component information is retrieved from the graphical representation of the
		// view
		HashSet<Node> assigned = new HashSet<Node>();
		HashMap<Node, Vector2d> node2upperLeftPos = new HashMap<Node, Vector2d>();
		HashMap<Node, Vector2d> node2lowerRightPos = new HashMap<Node, Vector2d>();
		for (Node n : nodes) {
			node2upperLeftPos.put(n, KeggGmlHelper.getNodePointUL(n));
			node2lowerRightPos.put(n, KeggGmlHelper.getNodePointLR(n));
		}
		for (Entry e : result) {
			if (e.getType() == EntryType.genes || e.getType() == EntryType.group) {
				Node n = entry2graphNode.get(e);
				if (n != null) {
					// search all nodes which are located fully inside the node
					// set entries for that node as components of e
					Set<Node> insideNodes = KeggGmlHelper.getNodesInsideThisNode(n, nodes, node2upperLeftPos, node2lowerRightPos);
					for (Node in : insideNodes) {
						if (in == n)
							continue;
						boolean groupPart = KeggGmlHelper.getIsPartOfGroup(in);
						if (groupPart) {
							Collection<Entry> entriesOfInsideNode = graphNode2entries.get(in);
							if (entriesOfInsideNode != null && entriesOfInsideNode.size() > 0) {
								for (Entry componentEntry : entriesOfInsideNode) {
									e.components.add(new IdRef(componentEntry, componentEntry.getId().getValue()));
									assigned.add(in);
								}
							}
						}
					}
				}
			}
		}
		for (Node n : nodes) {
			if (KeggGmlHelper.getIsPartOfGroup(n)) {
				if (!assigned.contains(n)) {
					warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.CORRESPONDING_GROUP_ENTRY_NOT_FOUND_FOR_COMPONENT, n));
				}
			}
		}
		return result;
	}
	
	private static Collection<Entry> getEntryElementsFromGraphNode(KgmlIdGenerator idGenerator,
						Node graphNode,
						Collection<Gml2PathwayWarningInformation> warnings,
						Collection<Gml2PathwayErrorInformation> errors) {
		// normally one entry is created for each graph node
		// the exceptions are map link nodes
		// these nodes contain information about elements in the linked pathway in the form of attributes
		// thus map link nodes represent a number of entry elements
		ArrayList<Entry> result = new ArrayList<Entry>();
		
		Id id = new Id(idGenerator.getNextID() + "");
		String keggIdValue = KeggGmlHelper.getKeggId(graphNode);
		if (keggIdValue == null) {
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.KEGG_ID_MISSING, graphNode));
			keggIdValue = "";
		}
		KeggId name = new KeggId(keggIdValue);
		String entryTypeValue = KeggGmlHelper.getKeggType(graphNode);
		if (entryTypeValue == null) {
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.KEGG_TYPE_MISSING, graphNode));
			entryTypeValue = EntryType.unspecified.getDescription();
		}
		EntryType type = EntryType.getEntryType(entryTypeValue);
		if (type == null) {
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.KEGG_TYPE_INVALID, graphNode));
			type = EntryType.unspecified;
		}
		String linkValue = KeggGmlHelper.getKeggLinkUrl(graphNode);
		Url link = null;
		if (linkValue != null && linkValue.length() > 0) {
			link = Url.getUrl(linkValue);
		}
		
		IdRef map = null; // the map value is only processed for entries, which need to be created
		// out of the attribute values of a kegg graph node
		
		Collection<KeggId> reactions = new ArrayList<KeggId>();
		ArrayList<IndexAndString> reactionNames = KeggGmlHelper.getKeggReactions(graphNode);
		for (int i = 0; i < reactionNames.size(); i++) {
			String rName = reactionNames.get(i).getValue();
			// String rType = reactionTypes.get(i);
			// reaction type is analyzed during creation of reaction elements
			KeggId rId = new KeggId(rName);
			reactions.add(rId);
		}
		Collection<IdRef> components = new ArrayList<IdRef>();
		Graphics graphics = Graphics.getGraphicsFromGraphNode(graphNode, warnings, errors);
		
		Entry mainEntry = new Entry(id, name, type, link, map, reactions, components, graphics);
		String clusterId = NodeTools.getClusterID(graphNode, "");
		if (clusterId.length() <= 0)
			clusterId = null;
		mainEntry.setSourcePathwayKeggId(clusterId);
		mainEntry.setSourceGraphNode(graphNode);
		result.add(mainEntry);
		
		if (KeggGmlHelper.getIsPartOfGroup(graphNode)) {
			mainEntry.setIsPartOfGroup(true);
		}
		
		if (KeggGmlHelper.getKeggType(graphNode) != null && KeggGmlHelper.getKeggType(graphNode).equals("map")) {
			// create entries with no graphics information
			IdRef mapRef = new IdRef(mainEntry, mainEntry.getId().getValue());
			result.addAll(createEntryElementsFromMapNodeAttributes(
								idGenerator,
								graphNode,
								errors,
								mapRef, clusterId));
		}
		return result;
	}
	
	/**
	 * Use this method to set a reference to the source graph node, which was used
	 * as the basis for the creation of this entry.
	 * 
	 * @param graphNode
	 */
	private void setSourceGraphNode(Node graphNode) {
		this.sourceGraphNode = graphNode;
	}
	
	/**
	 * In case this entry has been created from a Graph node, the souce graph node
	 * may be retrieved with this function.
	 * More than one Entry may be created from a single Graph node.
	 * 
	 * @return Souce Graph Node (if Entry is created from a Graph)
	 */
	public Node getSourceGraphNode() {
		return sourceGraphNode;
	}
	
	private static ArrayList<Entry> createEntryElementsFromMapNodeAttributes(
						KgmlIdGenerator idGenerator,
						Node graphNode,
						Collection<Gml2PathwayErrorInformation> errors,
						IdRef mapRef,
						String clusterId) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		ArrayList<IndexAndString> referencedIds = KeggGmlHelper.getKeggIds(graphNode);
		for (IndexAndString ias : referencedIds) {
			Id refMapId = new Id(idGenerator.getNextID() + "");
			String keggIDvalue = ias.getValue();
			KeggId refMapName = new KeggId(keggIDvalue);
			if (keggIDvalue == null) {
				errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.KEGG_REFERENCED_ID_MISSING, graphNode));
			}
			String typeValue = KeggGmlHelper.getKeggType(graphNode, ias.getIndex());
			if (typeValue == null) {
				errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.KEGG_REFERENCED_TYPE_MISSING, graphNode));
			}
			EntryType refMapType = EntryType.getEntryType(typeValue);
			if (refMapType == null) {
				errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.KEGG_REFERENCED_TYPE_INVALID, graphNode));
			}
			String refMapLinkValue = KeggGmlHelper.getKeggLinkUrl(graphNode, ias.getIndex());
			Url refMapLink = null;
			if (refMapLinkValue != null && refMapLinkValue.length() > 0) {
				refMapLink = Url.getUrl(refMapLinkValue);
			}
			Entry refMapEntry = new Entry(refMapId, refMapName, refMapType, refMapLink, mapRef, null, null, null);
			refMapEntry.setSourceGraphNode(graphNode);
			refMapEntry.setSourcePathwayKeggId(clusterId);
			result.add(refMapEntry);
		}
		return result;
	}
	
	public void setIsPartOfGroup(boolean partOfGroup) {
		this.partOfGroup = partOfGroup;
	}
	
	public boolean isPartOfGroup() {
		return partOfGroup;
	}
	
	public String getVisibleName() {
		String res = getGraphicsTitle(null);
		if (res == null)
			res = getName().getId();
		res = StringManipulationTools.stringReplace(res, "<br>", "");
		return res;
	}
	
	public void setLink(String url) {
		this.link = new Url(url);
	}
	
	public void setType(EntryType et) {
		this.type = et;
	}
	
	public void removeReaction(Reaction r) {
		if (reactions != null) {
			ArrayList<KeggId> del = new ArrayList<KeggId>();
			for (KeggId id : reactions) {
				if (id.getReferenceReaction() == r)
					del.add(id);
			}
			for (KeggId d : del)
				reactions.remove(d);
		}
	}
}

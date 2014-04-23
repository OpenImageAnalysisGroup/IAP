/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import java.util.Iterator;
import java.util.List;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.AttributeConstants;

/**
 * Utilities for the handling the attributes on graphs, nodes, and edges and
 * the corresponding definition of the strings. Pattern occurences are
 * represented as attributes of nodes fand edges. Every occurence of a pattern
 * is marked as a set of four attributes: the pattern name, the index of the
 * pattern, the index of the node (or edge) within this special occurence of
 * the pattern, and the internal number of the node (or edge) within the
 * pattern graph. These four attributes are group into a number container.
 * This container is called the position container. Every node or edge may
 * have one or more containers, which are encapsulated by another attribute
 * named "PATTERN". The list of patterns is itself encapsulated by the
 * attribute "AGNW" which is the container for the group in Gatersleben. A
 * simple example:
 * 
 * <pre>
 * AGNW [
 *   PATTERN [
 *     PATTERN_1 [
 *       PATTERN_NAME "X1"
 *       PATTERN_INDEX 1
 *       NODE_INDEX 1
 *       INTERNAL_NODE_INDEX 111
 *     ]
 *     PATTERN_2 [
 *       PATTERN_NAME "X1"
 *       PATTERN_INDEX 2
 *       NODE_INDEX 2
 *       INTERNAL_NODE_INDEX 121
 *     ]
 *     PATTERN_3 [
 *       PATTERN_NAME "X2"
 *       PATTERN_INDEX 1
 *       NODE_INDEX 1
 *       INTERNAL_NODE_INDEX 113
 *     ]
 *   ]
 * ]
 * </pre>
 * 
 * @author Dirk Koschützki
 */
public class PatternAttributeUtils {
	
	/**
	 * The container for all pattern attributes.
	 */
	public static final String PATTERN_PATH =
						AttributeConstants.AGNW_PATH + Attribute.SEPARATOR + "PATTERN";
	
	/**
	 * Path to the attribute for the minimal value of additional incoming
	 * edges.
	 */
	public static final String MIN_ADD_INC_EDGES_PATH =
						AttributeConstants.AGNW_PATH + Attribute.SEPARATOR
											+ "MIN_ADD_INC_EDGES";
	
	/**
	 * Path to the attribute for the maximal value of additional incoming
	 * edges.
	 */
	public static final String MAX_ADD_INC_EDGES_PATH =
						AttributeConstants.AGNW_PATH + Attribute.SEPARATOR
											+ "MAX_ADD_INC_EDGES";
	
	/**
	 * Path to the attribute for the minimal value of additional outgoing
	 * edges.
	 */
	public static final String MIN_ADD_OUT_EDGES_PATH =
						AttributeConstants.AGNW_PATH + Attribute.SEPARATOR
											+ "MIN_ADD_OUT_EDGES";
	
	/**
	 * Path to the attribute for the maximal value of additional outgoing
	 * edges.
	 */
	public static final String MAX_ADD_OUT_EDGES_PATH =
						AttributeConstants.AGNW_PATH + Attribute.SEPARATOR
											+ "MAX_ADD_OUT_EDGES";
	
	/**
	 * The prefix of the attribute name for the pattern record. Every
	 * attribute needs a unique name which has to start with a string.
	 * Therefore we generate a new unique name based on this prefix for every
	 * occurence of a pattern at a node.
	 */
	public static final String PATTERN_RECORD_PREFIX = "PATTERN_";
	
	/**
	 * The attribute name for the pattern name. The pattern name is of type
	 * String and stores the name of the pattern.
	 */
	public static final String PATTERN_NAME = "PATTERN_NAME";
	
	/**
	 * The attribute name for the pattern index. Each pattern can be found
	 * several times in a graph. If a pattern of one type is found two times
	 * in a graph, all nodes which belong to the first accourence will get
	 * the index 1, the second found pattern will get the number 2. This
	 * number will be the same for differnt patterns. So the first found
	 * pattern of a second type will get also the number 1 for the
	 * pattern_index.
	 */
	public static final String PATTERN_INDEX = "PATTERN_INDEX";
	
	/**
	 * The attribute name for the node index. Every node within the occurence
	 * of a pattern will be enumerated within this attribute.
	 */
	public static final String NODE_INDEX = "NODE_INDEX";
	
	/**
	 * The attribute name for the edge index. Every edge within the occurence
	 * of a pattern will be enumerated within this attribute.
	 */
	public static final String EDGE_INDEX = "EDGE_INDEX";
	
	/**
	 * The attribute name for the internal node index. Additionally to the
	 * node index the internal id of the node within the pattern graph is
	 * saved in the target graph. Beware: The internal id may be different
	 * from the node index, as the id is automatically generated during
	 * constrution of the pattern graph!
	 */
	public static final String INTERNAL_NODE_INDEX = "INTERNAL_NODE_INDEX";
	
	/**
	 * The attribute name for the internal edge index. Additionally to the
	 * edge index the internal id of the edge within the pattern graph is
	 * saved in the target graph. Beware: The internal id may be different
	 * from the edge index, as the id is automatically generated during
	 * constrution of the pattern graph!
	 */
	public static final String INTERNAL_EDGE_INDEX = "INTERNAL_EDGE_INDEX";
	
	/**
	 * Checks, if the top level container attribute "AGNW" exists at the
	 * given graph.
	 * 
	 * @param g
	 *           a graph
	 * @return true, if the attribute exists
	 */
	public static boolean checkExistenceAGNWContainer(Graph g) {
		try {
			@SuppressWarnings("unused")
			Attribute tempA = g.getAttribute(AttributeConstants.AGNW_PATH);
			
			return true;
		} catch (AttributeNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Checks, if the top level container attribute "AGNW" exists at the
	 * given node.
	 * 
	 * @param n
	 *           a node
	 * @return true, if the attribute exists
	 */
	public static boolean checkExistenceAGNWContainer(Node n) {
		try {
			@SuppressWarnings("unused")
			Attribute tempA = n.getAttribute(AttributeConstants.AGNW_PATH);
			
			return true;
		} catch (AttributeNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Checks, if the top level container attribute "AGNW" exists at the
	 * given edge.
	 * 
	 * @param e
	 *           an edge
	 * @return true, if the attribute exists
	 */
	public static boolean checkExistenceAGNWContainer(Edge e) {
		try {
			@SuppressWarnings("unused")
			Attribute tempA = e.getAttribute(AttributeConstants.AGNW_PATH);
			
			return true;
		} catch (AttributeNotFoundException ex) {
			return false;
		}
	}
	
	/**
	 * Checks, if the pattern container attribute "AGNW.PATTERN" exists at
	 * the given graph.
	 * 
	 * @param g
	 *           a graph
	 * @return true, if the attribute exists
	 */
	public static boolean checkExistencePatternContainer(Graph g) {
		try {
			@SuppressWarnings("unused")
			Attribute tempA = g.getAttribute(PATTERN_PATH);
			
			return true;
		} catch (AttributeNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Checks, if the pattern container attribute "AGNW.PATTERN" exists at
	 * the given node.
	 * 
	 * @param n
	 *           a node
	 * @return true, if the attribute exists
	 */
	public static boolean checkExistencePatternContainer(Node n) {
		try {
			@SuppressWarnings("unused")
			Attribute tempA = n.getAttribute(PATTERN_PATH);
			
			return true;
		} catch (AttributeNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Checks, if the pattern container attribute "AGNW.PATTERN" exists at
	 * the given edge.
	 * 
	 * @param e
	 *           an edge
	 * @return true, if the attribute exists
	 */
	public static boolean checkExistencePatternContainer(Edge e) {
		try {
			@SuppressWarnings("unused")
			Attribute tempA = e.getAttribute(PATTERN_PATH);
			
			return true;
		} catch (AttributeNotFoundException ex) {
			return false;
		}
	}
	
	/**
	 * Checks, if the pattern container for the pattern position exists at the
	 * given node.
	 * 
	 * @param n
	 *           a node
	 * @param position
	 *           the position
	 * @return true, if the attribute exists.
	 */
	public static boolean checkExistencePattern(Node n, int position) {
		try {
			@SuppressWarnings("unused")
			Attribute tempA =
								n.getAttribute(PATTERN_PATH + Attribute.SEPARATOR
													+ PATTERN_RECORD_PREFIX + position);
			
			return true;
		} catch (AttributeNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Checks, if the pattern container for the pattern position exists at the
	 * given edge.
	 * 
	 * @param e
	 *           an edge
	 * @param position
	 *           the position
	 * @return true, if the attribute exists.
	 */
	public static boolean checkExistencePattern(Edge e, int position) {
		try {
			@SuppressWarnings("unused")
			Attribute tempA =
								e.getAttribute(PATTERN_PATH + Attribute.SEPARATOR
													+ PATTERN_RECORD_PREFIX + position);
			
			return true;
		} catch (AttributeNotFoundException ex) {
			return false;
		}
	}
	
	/**
	 * Adds the top level container attribute "AGNW" to the given graph.
	 * 
	 * @param g
	 *           a graph
	 */
	public static void addAGNWContainer(Graph g) {
		g.addAttribute(new HashMapAttribute(AttributeConstants.AGNW_PATH),
							"");
	}
	
	/**
	 * Adds the top level container attribute "AGNW" to the given node.
	 * 
	 * @param n
	 *           a node
	 */
	public static void addAGNWContainer(Node n) {
		n.addAttribute(new HashMapAttribute(AttributeConstants.AGNW_PATH),
							"");
	}
	
	/**
	 * Adds the top level container attribute "AGNW" to the given edge.
	 * 
	 * @param e
	 *           an edge
	 */
	public static void addAGNWContainer(Edge e) {
		e.addAttribute(new HashMapAttribute(AttributeConstants.AGNW_PATH),
							"");
	}
	
	/**
	 * Adds the pattern container attribute "AGNW.PATTERN" to the given graph.
	 * 
	 * @param g
	 *           a graph
	 */
	public static void addPatternContainer(Graph g) {
		g.addAttribute(new HashMapAttribute("PATTERN"),
							AttributeConstants.AGNW_PATH);
	}
	
	/**
	 * Adds the pattern container attribute "AGNW.PATTERN" to the given node.
	 * 
	 * @param n
	 *           a node
	 */
	public static void addPatternContainer(Node n) {
		n.addAttribute(new HashMapAttribute("PATTERN"),
							AttributeConstants.AGNW_PATH);
	}
	
	/**
	 * Adds the pattern container attribute "AGNW.PATTERN" to the given edge.
	 * 
	 * @param e
	 *           an edge
	 */
	public static void addPatternContainer(Edge e) {
		e.addAttribute(new HashMapAttribute("PATTERN"),
							AttributeConstants.AGNW_PATH);
	}
	
	/**
	 * Adds the pattern container for the given pattern position at the given
	 * node.
	 * 
	 * @param n
	 *           a node
	 * @param position
	 *           the position
	 */
	public static void addPattern(Node n, int position) {
		n.addAttribute(new HashMapAttribute(PATTERN_RECORD_PREFIX + position),
							PATTERN_PATH);
	}
	
	/**
	 * Adds the pattern container for the given pattern position at the given
	 * edge.
	 * 
	 * @param e
	 *           an edge
	 * @param position
	 *           the position
	 */
	public static void addPattern(Edge e, int position) {
		e.addAttribute(new HashMapAttribute(PATTERN_RECORD_PREFIX + position),
							PATTERN_PATH);
	}
	
	/**
	 * Adds the top level container attribute "AGNW" to the given graph, if it
	 * is not already there.
	 * 
	 * @param g
	 *           a graph
	 */
	public static void checkAndAddAGNWContainer(Graph g) {
		if (!checkExistenceAGNWContainer(g)) {
			addAGNWContainer(g);
		}
	}
	
	/**
	 * Adds the top level container attribute "AGNW" to the given node, if it
	 * is not already there.
	 * 
	 * @param n
	 *           a node
	 */
	public static void checkAndAddAGNWContainer(Node n) {
		if (!checkExistenceAGNWContainer(n)) {
			addAGNWContainer(n);
		}
	}
	
	/**
	 * Adds the top level container attribute "AGNW" to the given edge, if it
	 * is not already there.
	 * 
	 * @param e
	 *           an edge
	 */
	public static void checkAndAddAGNWContainer(Edge e) {
		if (!checkExistenceAGNWContainer(e)) {
			addAGNWContainer(e);
		}
	}
	
	/**
	 * Adds the pattern container attribute "AGNW.PATTERN" to the given graph,
	 * if it is not already there.
	 * 
	 * @param g
	 *           a graph
	 */
	public static void checkAndAddPatternContainer(Graph g) {
		if (!checkExistencePatternContainer(g)) {
			addPatternContainer(g);
		}
	}
	
	/**
	 * Adds the pattern container attribute "AGNW.PATTERN" to the given node,
	 * if it is not already there.
	 * 
	 * @param n
	 *           a node
	 */
	public static void checkAndAddPatternContainer(Node n) {
		if (!checkExistencePatternContainer(n)) {
			addPatternContainer(n);
		}
	}
	
	/**
	 * Adds the pattern container attribute "AGNW.PATTERN" to the given edge,
	 * if it is not already there.
	 * 
	 * @param e
	 *           an edge
	 */
	public static void checkAndAddPatternContainer(Edge e) {
		if (!checkExistencePatternContainer(e)) {
			addPatternContainer(e);
		}
	}
	
	/**
	 * Adds the pattern container for the pattern position to the given node,
	 * if it is not already there.
	 * 
	 * @param n
	 *           a node
	 * @param position
	 *           the position
	 */
	public static void checkAndAddPattern(Node n, int position) {
		if (!checkExistencePattern(n, position)) {
			addPattern(n, position);
			
		}
	}
	
	/**
	 * Adds the pattern container for the pattern position to the given edge,
	 * if it is not already there.
	 * 
	 * @param e
	 *           an edge
	 * @param position
	 *           the position
	 */
	public static void checkAndAddPattern(Edge e, int position) {
		if (!checkExistencePattern(e, position)) {
			addPattern(e, position);
			
		}
	}
	
	/**
	 * Returns the pattern name for the given node and position.
	 * 
	 * @param n
	 *           a node
	 * @param position
	 *           the position
	 * @return the pattern name
	 */
	public static String getPatternName(Node n, int position) {
		String patternNameString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position + Attribute.SEPARATOR + PATTERN_NAME;
		
		Attribute patternNameAttribute = null;
		
		try {
			patternNameAttribute = n.getAttribute(patternNameString);
		} catch (AttributeNotFoundException e) {
			/* pattern not available, so ignore this node. */
			return null;
		}
		
		return (String) patternNameAttribute.getValue();
	}
	
	/**
	 * Returns the pattern name for the given edge and position.
	 * 
	 * @param e
	 *           an edge
	 * @param position
	 *           the position
	 * @return the pattern name
	 */
	public static String getPatternName(Edge e, int position) {
		String patternNameString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position + Attribute.SEPARATOR + PATTERN_NAME;
		
		Attribute patternNameAttribute = null;
		
		try {
			patternNameAttribute = e.getAttribute(patternNameString);
		} catch (AttributeNotFoundException ex) {
			/* pattern not available, so ignore this edge. */
			return null;
		}
		
		return (String) patternNameAttribute.getValue();
	}
	
	/**
	 * Returns the pattern index for the given node and position.
	 * 
	 * @param n
	 *           a node
	 * @param position
	 *           the position
	 * @return the pattern index
	 */
	public static Integer getPatternIndex(Node n, int position) {
		String patternIndexString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position + Attribute.SEPARATOR + PATTERN_INDEX;
		
		Attribute patternIndexAttribute = null;
		
		try {
			patternIndexAttribute = n.getAttribute(patternIndexString);
		} catch (AttributeNotFoundException e) {
			/* pattern not available, so ignore this node. */
			return null;
		}
		
		return (Integer) patternIndexAttribute.getValue();
	}
	
	/**
	 * Returns the pattern index for the given edge and position.
	 * 
	 * @param e
	 *           an edge
	 * @param position
	 *           the position
	 * @return the pattern index
	 */
	public static Integer getPatternIndex(Edge e, int position) {
		String patternIndexString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position + Attribute.SEPARATOR + PATTERN_INDEX;
		
		Attribute patternIndexAttribute = null;
		
		try {
			patternIndexAttribute = e.getAttribute(patternIndexString);
		} catch (AttributeNotFoundException ex) {
			/* pattern not available, so ignore this edge. */
			return null;
		}
		
		return (Integer) patternIndexAttribute.getValue();
	}
	
	/**
	 * Returns the node index for the given node and position.
	 * 
	 * @param n
	 *           a node
	 * @param position
	 *           the position
	 * @return the node index
	 */
	public static Integer getNodeIndex(Node n, int position) {
		String nodeIndexString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position + Attribute.SEPARATOR + NODE_INDEX;
		
		Attribute nodeIndexAttribute = null;
		
		try {
			nodeIndexAttribute = n.getAttribute(nodeIndexString);
		} catch (AttributeNotFoundException e) {
			/* pattern not available, so ignore this node. */
			return null;
		}
		
		return (Integer) nodeIndexAttribute.getValue();
	}
	
	/**
	 * Returns the edge index for the given edge and position.
	 * 
	 * @param e
	 *           an edge
	 * @param position
	 *           the position
	 * @return the edge index
	 */
	public static Integer getEdgeIndex(Edge e, int position) {
		String edgeIndexString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position + Attribute.SEPARATOR + EDGE_INDEX;
		
		Attribute edgeIndexAttribute = null;
		
		try {
			edgeIndexAttribute = e.getAttribute(edgeIndexString);
		} catch (AttributeNotFoundException ex) {
			/* pattern not available, so ignore this edge. */
			return null;
		}
		
		return (Integer) edgeIndexAttribute.getValue();
	}
	
	/**
	 * Returns the internal node index for the given node and position.
	 * 
	 * @param n
	 *           a node
	 * @param position
	 *           the position
	 * @return the node index
	 */
	public static Integer getInternalNodeIndex(Node n, int position) {
		String nodeIndexString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position + Attribute.SEPARATOR + INTERNAL_NODE_INDEX;
		
		Attribute internalNodeIndexAttribute = null;
		
		try {
			internalNodeIndexAttribute = n.getAttribute(nodeIndexString);
		} catch (AttributeNotFoundException e) {
			/* pattern not available, so ignore this node. */
			return null;
		}
		
		return (Integer) internalNodeIndexAttribute.getValue();
	}
	
	/**
	 * Returns the internal edge index for the given edge and position.
	 * 
	 * @param e
	 *           an edge
	 * @param position
	 *           the position
	 * @return the edge index
	 */
	public static Integer getInternalEdgeIndex(Edge e, int position) {
		String edgeIndexString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position + Attribute.SEPARATOR + INTERNAL_EDGE_INDEX;
		
		Attribute internalEdgeIndexAttribute = null;
		
		try {
			internalEdgeIndexAttribute = e.getAttribute(edgeIndexString);
		} catch (AttributeNotFoundException ex) {
			/* pattern not available, so ignore this edge. */
			return null;
		}
		
		return (Integer) internalEdgeIndexAttribute.getValue();
	}
	
	/**
	 * Adds a set of pattern information to the given node.
	 * 
	 * @param n
	 *           a node
	 * @param position
	 *           the position
	 * @param patternName
	 *           the name of pattern
	 * @param patternIndex
	 *           the occurance of this pattern in the graph
	 * @param nodeIndex
	 *           the number of node of this pattern occurance
	 * @param patternNode
	 *           the matched node from the pattern graph
	 */
	public static void addPatternInformation(Node n, int position,
						String patternName,
						Integer patternIndex,
						Integer nodeIndex,
						Node patternNode) {
		String patternPositionString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position;
		
		Attribute patternNameAttribute =
							StringAttribute.getTypedStringAttribute(PATTERN_NAME, patternName);
		Attribute patternIndexAttribute =
							new IntegerAttribute(PATTERN_INDEX, patternIndex);
		Attribute nodeIndexAttribute =
							new IntegerAttribute(NODE_INDEX, nodeIndex);
		
		List<Node> listOfNodes = (patternNode.getGraph().getNodes());
		Integer internalNodeId = new Integer(listOfNodes.indexOf(patternNode));
		Attribute internalNodeIdAttribute =
							new IntegerAttribute(INTERNAL_NODE_INDEX, internalNodeId);
		
		checkAndAddAGNWContainer(n);
		checkAndAddPatternContainer(n);
		checkAndAddPattern(n, position);
		n.addAttribute(patternNameAttribute, patternPositionString);
		n.addAttribute(patternIndexAttribute, patternPositionString);
		n.addAttribute(nodeIndexAttribute, patternPositionString);
		n.addAttribute(internalNodeIdAttribute, patternPositionString);
	}
	
	/**
	 * Adds a set of pattern information to the given edge.
	 * 
	 * @param e
	 *           an edge
	 * @param position
	 *           the position
	 * @param patternName
	 *           the name of pattern
	 * @param patternIndex
	 *           the occurance of this pattern in the graph
	 * @param edgeIndex
	 *           the number of the edge of this pattern occurance
	 */
	public static void addPatternInformation(Edge e, int position,
						String patternName,
						Integer patternIndex,
						Integer edgeIndex) {
		String patternPositionString =
							PATTERN_PATH + Attribute.SEPARATOR + PATTERN_RECORD_PREFIX
												+ position;
		
		Attribute patternNameAttribute =
							StringAttribute.getTypedStringAttribute(PATTERN_NAME, patternName);
		Attribute patternIndexAttribute =
							new IntegerAttribute(PATTERN_INDEX, patternIndex);
		Attribute edgeIndexAttribute =
							new IntegerAttribute(EDGE_INDEX, edgeIndex);
		
		checkAndAddAGNWContainer(e);
		checkAndAddPatternContainer(e);
		checkAndAddPattern(e, position);
		e.addAttribute(patternNameAttribute, patternPositionString);
		e.addAttribute(patternIndexAttribute, patternPositionString);
		e.addAttribute(edgeIndexAttribute, patternPositionString);
	}
	
	/**
	 * Returns the maximal position value of the pattern container.
	 * 
	 * @param n
	 *           a node
	 * @return returns the maximal value or zero, if no pattern exists.
	 */
	public static int getMaximumPatternPosition(Node n) {
		if (!checkExistenceAGNWContainer(n)
							|| !checkExistencePatternContainer(n)) {
			return 0;
		}
		
		CollectionAttribute allPatternContainer =
							(CollectionAttribute) n.getAttribute(PATTERN_PATH);
		
		int result = 0;
		Iterator<String> i = allPatternContainer.getCollection().keySet().iterator();
		
		while (i.hasNext()) {
			String attributeId = (String) i.next();
			
			try {
				int startingPosition =
									attributeId.indexOf(PATTERN_RECORD_PREFIX)
														+ PATTERN_RECORD_PREFIX.length();
				
				int value =
									Integer.parseInt(attributeId.substring(
														startingPosition,
														attributeId.length()));
				
				result = value > result
									? value
									: result;
			} catch (Exception e) {
				/* if there was an exception, then we ignore it here! */
				System.out.println(attributeId);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the maximal position value of the pattern container.
	 * 
	 * @param e
	 *           an edge
	 * @return returns the maximal value or zero, if no pattern exists.
	 */
	public static int getMaximumPatternPosition(Edge e) {
		if (!checkExistenceAGNWContainer(e)
							|| !checkExistencePatternContainer(e)) {
			return 0;
		}
		
		CollectionAttribute allPatternContainer =
							(CollectionAttribute) e.getAttribute(PATTERN_PATH);
		
		int result = 0;
		Iterator<String> i = allPatternContainer.getCollection().keySet().iterator();
		
		while (i.hasNext()) {
			String attributeId = (String) i.next();
			
			try {
				int startingPosition =
									attributeId.indexOf(PATTERN_RECORD_PREFIX)
														+ PATTERN_RECORD_PREFIX.length();
				
				int value =
									Integer.parseInt(attributeId.substring(
														startingPosition,
														attributeId.length()));
				
				result = value > result
									? value
									: result;
			} catch (Exception ex) {
				/* if there was an exception, then we ignore it here! */
			}
		}
		
		return result;
	}
	
	/**
	 * Checks if a pattern exists at the given node.
	 * 
	 * @param n
	 *           a node
	 * @return true, if at least one pattern exists, false otherwise
	 */
	public static boolean checkExistenceOfAnyPattern(Node n) {
		return getMaximumPatternPosition(n) == 0
							? false
							: true;
	}
	
	/**
	 * Checks if a pattern exists at the given edge.
	 * 
	 * @param e
	 *           an edge
	 * @return true, if at least one pattern exists, false otherwise
	 */
	public static boolean checkExistenceOfAnyPattern(Edge e) {
		return getMaximumPatternPosition(e) == 0
							? false
							: true;
	}
	
	/**
	 * Returns the position of the given pattern name and pattern index for
	 * the given node.
	 * 
	 * @param n
	 *           a node
	 * @param patternName
	 *           the pattern name
	 * @param patternIndex
	 *           the pattern index
	 * @return the position or zero, if the pattern name and index combination
	 *         does not exists
	 */
	public static int findPatternPosition(Node n, String patternName,
						Integer patternIndex) {
		if (patternName == null || patternIndex == null) {
			return 0;
		}
		
		int maximumPosition = getMaximumPatternPosition(n);
		
		for (int i = 1; i <= maximumPosition; i++) {
			if (patternName.equals(PatternAttributeUtils.getPatternName(n, i))
								&& patternIndex.equals(PatternAttributeUtils
													.getPatternIndex(n, i))) {
				return i;
			}
		}
		
		return 0;
	}
	
	/**
	 * Returns the position of the given pattern name and pattern index for
	 * the given edge.
	 * 
	 * @param e
	 *           an edge
	 * @param patternName
	 *           the pattern name
	 * @param patternIndex
	 *           the pattern index
	 * @return the position or zero, if the pattern name and index combination
	 *         does not exists
	 */
	public static int findPatternPosition(Edge e, String patternName,
						Integer patternIndex) {
		if (patternName == null || patternIndex == null) {
			return 0;
		}
		
		int maximumPosition = getMaximumPatternPosition(e);
		
		for (int i = 1; i <= maximumPosition; i++) {
			if (patternName.equals(PatternAttributeUtils.getPatternName(e, i))
								&& patternIndex.equals(PatternAttributeUtils
													.getPatternIndex(e, i))) {
				return i;
			}
		}
		
		return 0;
	}
	
	/**
	 * Returns the minimal value for the additional incoming edges.
	 * 
	 * @param n
	 *           a node
	 * @return the minimal value for additional incoming edges
	 */
	public static int getMinAddIncEdges(Node n) {
		return getAddIncOutEdges(n, MIN_ADD_INC_EDGES_PATH, 0);
	}
	
	/**
	 * Returns the maximal value for the additional incoming edges.
	 * 
	 * @param n
	 *           a node
	 * @return the maximal value for additional incoming edges
	 */
	public static int getMaxAddIncEdges(Node n) {
		return getAddIncOutEdges(n, MAX_ADD_INC_EDGES_PATH, Integer.MAX_VALUE / 2); // 1000
	}
	
	/**
	 * Returns the minimal value for the additional outgoing edges.
	 * 
	 * @param n
	 *           a node
	 * @return the minimal value for additional outgoing edges
	 */
	public static int getMinAddOutEdges(Node n) {
		return getAddIncOutEdges(n, MIN_ADD_OUT_EDGES_PATH, 0);
	}
	
	/**
	 * Returns the maximal value for the additional outgoing edges.
	 * 
	 * @param n
	 *           a node
	 * @return the maximal value for additional outgoing edges
	 */
	public static int getMaxAddOutEdges(Node n) {
		return getAddIncOutEdges(n, MAX_ADD_OUT_EDGES_PATH, Integer.MAX_VALUE / 2); // 1000
	}
	
	/**
	 * Helper methods for finding the integer value for additional edges
	 * within the node attributes
	 * 
	 * @param n
	 *           the node of interest
	 * @param path
	 *           the attribute path
	 * @param defaultValue
	 *           a default value if the attribute is not found
	 * @return DOCUMENT ME!
	 */
	private static int getAddIncOutEdges(Node n, String path, int defaultValue) {
		Attribute addIncEdgesAttribute = null;
		int result = 0;
		
		try {
			addIncEdgesAttribute = n.getAttribute(path);
		} catch (AttributeNotFoundException e) {
			/* pattern not available, so we use the maximal value. */
			return defaultValue;
		}
		
		try {
			Object o = addIncEdgesAttribute.getValue();
			
			result = Integer.parseInt(o.toString());
		} catch (NumberFormatException e) {
			/* Value is not parseable, therefore we use the maximal value. */
			result = defaultValue;
		}
		
		return result;
	}
}

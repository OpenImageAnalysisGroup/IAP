/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder_no_cache;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeLabelAttribute;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder.PatternAttributeUtils;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.NodeCacheEntry;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class MyTools {
	private static HashMap<Node, CoordinateAttribute> myHash = new HashMap<Node, CoordinateAttribute>(100000);
	
	public static void setXY(Node a, double x, double y) {
		CoordinateAttribute coA = myHash.get(a);
		if (coA == null) {
			coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
			myHash.put(a, coA);
		}
		if (GravistoService.getInstance().pluginSelectionMoveAllowed()) {
			coA.setCoordinate(x, y);
		} else {
			synchronized (GravistoService.getInstance().selectionSyncObject) {
				Selection sel = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getSelectionModel().getActiveSelection();
				if (!sel.getNodes().contains(a)) {
					coA.setCoordinate(x, y);
				}
			}
		}
	}
	
	/**
	 * For geting the x position of a node through attribute access,
	 * 
	 * @param a
	 *           Node to be analysed.
	 * @return X position of node.
	 */
	public static double getX(Node a) {
		CoordinateAttribute coA = myHash.get(a);
		if (coA == null) {
			coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
			myHash.put(a, coA);
		}
		
		return coA.getX();
	}
	
	public static void getPosition(Node a, Vector2d result) {
		CoordinateAttribute coA = myHash.get(a);
		if (coA == null) {
			coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
			myHash.put(a, coA);
		}
		
		Point2D p = coA.getCoordinate();
		
		result.x = p.getX();
		result.y = p.getY();
	}
	
	/**
	 * For geting the y position of a node through attribute access,
	 * 
	 * @param a
	 *           Node to be analysed.
	 * @return Y position of node.
	 */
	public static double getY(Node a) {
		CoordinateAttribute coA = myHash.get(a);
		if (coA == null) {
			coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
			myHash.put(a, coA);
		}
		
		return coA.getY();
	}
	
	/**
	 * As the attribute access is very slow, this method initializes the
	 * NodeCacheEntry structures. And saved all information that needs to
	 * be accessed many times.
	 * 
	 * @param nodeArray
	 *           NodeCacheEntry Vector
	 * @param nodeSearch
	 *           Node - Vector
	 * @param graph
	 *           The graph instance
	 * @param logger
	 *           a logger class
	 */
	public static void initNodeCache(
						ArrayList<NodeCacheEntry> nodeArray,
						HashMap<Node, NodeCacheEntry> nodeSearch,
						Graph graph) {
		nodeArray.ensureCapacity(graph.getNodes().size());
		boolean foundPattern = false;
		int patternCount = 0;
		
		for (int i = 0; i < graph.getNodes().size(); i++) {
			// read Position
			Node myNode = graph.getNodes().get(i);
			CoordinateAttribute cn =
								(CoordinateAttribute) myNode.getAttribute(GraphicAttributeConstants.COORD_PATH);
			Vector2d storeP = new Vector2d(cn.getX(),
								cn.getY());
			
			NodeCacheEntry newInfo = new NodeCacheEntry();
			
			newInfo.node = myNode;
			newInfo.nodeIndex = i;
			// initialize, last time a node has been moved. ( -1 = no movement yet)
			newInfo.lastTouch = -1;
			newInfo.position = storeP;
			
			// read Size ********************************************
			double width;
			double height;
			DoubleAttribute da =
								(DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMW_PATH);
			
			width = ((Double) da.getValue()).doubleValue();
			da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMH_PATH);
			height = ((Double) da.getValue()).doubleValue();
			Vector2d sizeVec = new Vector2d(width, height);
			
			newInfo.size = sizeVec;
			
			// read Size _end_ **************************************
			// read pattern type ************************************
			if (PatternAttributeUtils.getMaximumPatternPosition(myNode) > 1) {
				ErrorMsg.addErrorMessage("Error: Pattern Attribute Count > 1");
			}
			
			ArrayList<Graph> patternGraphs =
								GravistoService.getInstance().getPatternGraphs();
			
			newInfo.patternType = PatternAttributeUtils.getPatternName(myNode, 1);
			if (newInfo.patternType != null) {
				newInfo.patternIndex = PatternAttributeUtils.getPatternIndex(myNode, 1).intValue();
				int indexInPattern = PatternAttributeUtils.getInternalNodeIndex(myNode, 1).intValue();
				
				/**
				 * Number of the Pattern Graph. 0 means first loaded pattern,
				 * 1 second, ... Currently there is no name exchange system.
				 */
				int graphNum;
				
				String patNum = newInfo.patternType.substring(newInfo.patternType.lastIndexOf("_") + 1);
				
				graphNum = Integer.valueOf(patNum).intValue() - 1;
				
				List<Node> patternNodeCollection = patternGraphs.get(graphNum).getNodes();
				
				newInfo.patternNode =
									patternNodeCollection.get(indexInPattern);
				
				foundPattern = true;
			} else {
				newInfo.patternType = "";
				newInfo.patternIndex = -1;
			}
			newInfo.patternTypeEmpty = newInfo.patternType == null || newInfo.patternType.length() <= 0;
			
			// read pattern type _end_ *****************************
			
			nodeArray.add(newInfo);
			nodeSearch.put(newInfo.node, newInfo);
		}
		
		if (!foundPattern) {
			System.err.println("Pattern Layouter running without loaded patters.");
		}
		
		// if (patternCount>0)
		MainFrame.showMessage(patternCount + " patterns found.", MessageType.INFO);
	}
	
	private static String getLabelPath() {
		String LABEL_PATH;
		if (GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH.equals("")) {
			LABEL_PATH = GraphicAttributeConstants.LABEL;
		} else {
			LABEL_PATH = GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH +
								Attribute.SEPARATOR + GraphicAttributeConstants.LABEL;
		}
		return LABEL_PATH;
	}
	
	public static String getLabelFromNode(Node n) {
		NodeLabelAttribute labelAttr;
		try {
			labelAttr = (NodeLabelAttribute) n.getAttribute(getLabelPath());
			return labelAttr.getLabel();
		} catch (Exception e) {
			return "";
		}
	}
	
}

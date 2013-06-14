/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: XGMMLContentHandler.java,v 1.1 2011-01-31 09:00:24 klukas Exp $
 * Created on 03.11.2003 by Burkhard Sell
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.HashMap;

import org.AttributeHelper;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.xml.sax.Attributes;

/**
 * Custom XGMMLHandler for parsing an XGMML input source using SAX2.
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 */

public class XGMMLContentHandler extends XGMMLHandler {
	
	/** The current node to add. */
	Node currentNode;
	
	/** The current edge to add. */
	Edge currentEdge;
	
	/** The source node of the current edge. */
	Node edgeSourceNode;
	
	/** The target node of the current edge. */
	Node edgeTargetNode;
	
	/** <code>true</code> if the graph is directed, <code>flase</code> otherwise. */
	boolean directed = false;
	
	/** ??!!?? */
	boolean directedAcyclic = false;
	
	/** ??!!?? */
	boolean weighted = false;
	
	/** The width of the current element. */
	double width;
	
	/** The height of the current element. */
	double height;
	
	/** The fontname of the current element. */
	String fontName;
	
	/** The color to fill the current element. */
	Color fillColor;
	
	/** The outline color of the current element. */
	Color outlineColor;
	
	/** X coordinate of a node */
	double centerX;
	
	/** Y coordinate of a node */
	double centerY;
	
	/** Contains all points of an edge with more points than two. */
	GeneralPath path;
	
	/** The first points of an edge with more points than two. */
	boolean firstPoint;
	
	/**
	 * Constructs a new handler instance.
	 */
	public XGMMLContentHandler() {
		super();
		nodeIDMap = new HashMap<String, Node>();
	}
	
	/**
	 * Instantiates a new graph.
	 * <strong>Currently only one graph per file is supported</strong>
	 * 
	 * @throws Exception
	 *            if the graph could not be created.
	 */
	@Override
	public void instantiateGraph() throws Exception {
		this.graph = new AdjListGraph();
	}
	
	/**
	 * Instantiates a new node and add it to teh graph.
	 * 
	 * @throws Exception
	 *            if the node could not be created.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void instantiateNode() throws Exception {
		StackElement entry = (StackElement) this.tagStack.peek();
		Attributes attribs = entry.getElementAttributes();
		
		NodeGraphicAttribute graphicAttr =
							new NodeGraphicAttribute(GraphicAttributeConstants.GRAPHICS);
		
		CollectionAttribute collAttr = new HashMapAttribute("");
		collAttr.add(graphicAttr);
		
		currentNode = graph.addNode(collAttr);
		
		// save node in id map
		nodeIDMap.put(
							attribs.getValue(XGMMLConstants.ID_ATTRIBUTE_LITERAL),
							currentNode);
		
		// Label
		AttributeHelper.setLabel(
							currentNode,
							attribs.getValue(XGMMLConstants.LABEL_ATTRIBUTE_LITERAL),
							XGMMLConstants.FONT_ATTRIBUTE_LITERAL, null);
		
		// Position
		AttributeHelper.setPosition(
							currentNode,
							new Point2D.Double(centerX, centerY));
		
		try {
			AttributeHelper.setSize(currentNode, this.width, this.height);
			
		} catch (NullPointerException e) {
		} catch (NumberFormatException e) {
			
		}
		
		// Colors
		if (this.outlineColor != null)
			AttributeHelper.setOutlineColor(currentNode, this.outlineColor);
		if (this.fillColor != null)
			AttributeHelper.setFillColor(currentNode, fillColor);
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void startGraphElement(Attributes attribs) {
		
		this.directed =
							"1".equals(attribs.getValue(XGMMLConstants.DIRECTED_ATTRIBUTE_LITERAL));
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void startAttElement(Attributes attribs) {
		StackElement entry = (StackElement) this.tagStack.peek();
		
		if (entry.getElementName().equals(XGMMLConstants.GRAPH_ELEMENT_LITERAL)) {
			if (attribs
								.getValue(XGMMLConstants.NAME_ATTRIBUTE_LITERAL)
								.equals(XGMMLConstants.ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_WEIGHTED)
								&& attribs.getValue(XGMMLConstants.VALUE_ATTRIBUTE_LITERAL).equals(
													"1")) {
				this.weighted = true;
			}
		}
		
		if (entry.getElementName().equals(XGMMLConstants.NODE_ELEMENT_LITERAL)) {
			this.processNodeAttElement(attribs);
		}
		
		if (entry.getElementName().equals(XGMMLConstants.EDGE_ELEMENT_LITERAL)) {
			this.processEdgeAttElement(attribs);
		}
		
		if (entry
							.getElementName()
							.equals(XGMMLConstants.GRAPHICS_ELEMENT_LITERAL)) {
			this.processGraphicsAttElement(attribs);
		}
		
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void startNodeElement(Attributes attribs) {
		// currently do nothing
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void endNodeElement() throws Exception {
		// currently do nothing
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void startEdgeElement(Attributes attribs) {
		// currently do nothing
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void endEdgeElement() throws Exception {
		StackElement entry = (StackElement) this.tagStack.peek();
		Attributes attribs = entry.getElementAttributes();
		
		String source = attribs.getValue(XGMMLConstants.SOURCE_ATTRIBUTE_LITERAL);
		String target = attribs.getValue(XGMMLConstants.TARGET_ATTRIBUTE_LITERAL);
		
		this.edgeSourceNode = (Node) this.nodeIDMap.get(source);
		this.edgeTargetNode = (Node) this.nodeIDMap.get(target);
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void startGraphicsElement(Attributes attribs) throws Exception {
		try {
			if (attribs.getValue(XGMMLConstants.WIDTH_ATTRIBUTE_LITERAL) != null)
				this.width =
									Double.parseDouble(
														attribs.getValue(XGMMLConstants.WIDTH_ATTRIBUTE_LITERAL));
		} catch (NumberFormatException e) {
		}
		
		try {
			if (attribs.getValue(XGMMLConstants.HEIGHT_ATTRIBUTE_LITERAL) != null)
				this.height =
									Double.parseDouble(
														attribs.getValue(XGMMLConstants.HEIGHT_ATTRIBUTE_LITERAL));
		} catch (NumberFormatException e) {
		}
		
		this.fontName = attribs.getValue(XGMMLConstants.FONT_ATTRIBUTE_LITERAL);
		
		try {
			if (attribs.getValue(XGMMLConstants.FILL_ATTRIBUTE_LITERAL) != null)
				this.fillColor =
									new Color(
														Long
																			.valueOf(
																								attribs.getValue(XGMMLConstants.FILL_ATTRIBUTE_LITERAL),
																								16)
																			.intValue(),
														true);
		} catch (NumberFormatException e) {
		}
		
		try {
			if (attribs.getValue(XGMMLConstants.OUTLINE_ATTRIBUTE_LITERAL) != null)
				this.outlineColor =
									new Color(
														Long
																			.valueOf(
																								attribs.getValue(XGMMLConstants.OUTLINE_ATTRIBUTE_LITERAL),
																								16)
																			.intValue(),
														true);
		} catch (NumberFormatException e) {
		}
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void startCenterElement(Attributes attribs) throws Exception {
		this.centerX =
							Double.parseDouble(attribs.getValue(XGMMLConstants.X_ATTRIBUTE_LITERAL));
		
		this.centerY =
							Double.parseDouble(attribs.getValue(XGMMLConstants.Y_ATTRIBUTE_LITERAL));
	}
	
	public void startLineElement(Attributes attribs) throws Exception {
		this.path = new GeneralPath();
		this.firstPoint = true;
	}
	
	public void startPointElement(Attributes attribs) throws Exception {
		if (this.firstPoint) {
			this.path.moveTo(
								Float.parseFloat(attribs.getValue(XGMMLConstants.X_ATTRIBUTE_LITERAL)),
								Float.parseFloat(
													attribs.getValue(XGMMLConstants.Y_ATTRIBUTE_LITERAL)));
			this.firstPoint = false;
		} else
			this.path.lineTo(
								Float.parseFloat(attribs.getValue(XGMMLConstants.X_ATTRIBUTE_LITERAL)),
								Float.parseFloat(
													attribs.getValue(XGMMLConstants.Y_ATTRIBUTE_LITERAL)));
	}
	
	/**
	 * Instantiates a new edge and add it to teh graph.
	 * 
	 * @throws Exception
	 *            if the node could not be created.
	 */
	@Override
	public void instantiateEdge() throws Exception {
		StackElement entry = (StackElement) this.tagStack.peek();
		Attributes attribs = entry.getElementAttributes();
		
		Edge edge =
							this.graph.addEdge(
												this.edgeSourceNode,
												this.edgeTargetNode,
												this.directed);
		
		AttributeHelper.setLabel(
							edge,
							attribs.getValue(XGMMLConstants.LABEL_ATTRIBUTE_LITERAL),
							XGMMLConstants.FONT_ATTRIBUTE_LITERAL);
		
		if (this.fillColor != null)
			AttributeHelper.setFillColor(currentNode, fillColor);
		
	}
	
	/**
	 * Dynamically invoked by {@link XGMMLDelegatorHandler delegator handler}.
	 */
	public void processNodeAttElement(Attributes attribs) {
		// currently do nothing
	}
	
	/**
	 * Not yet implemented
	 * 
	 * @param attribs
	 */
	public void processEdgeAttElement(Attributes attribs) {
		// currently do nothing
	}
	
	/**
	 * Not yet implemented
	 * 
	 * @param attribs
	 */
	public void processGraphicsAttElement(Attributes attribs) {
		// currently do nothing
	}
	
	/**
	 * @return {@link XGMMLConstants.NODE_ELEMENT_LITERAL
	 *         XGMMLConstants.NODE_ELEMENT_LITERAL}
	 */
	@Override
	public String getNodeElementName() {
		return XGMMLConstants.NODE_ELEMENT_LITERAL;
	}
	
	/**
	 * @return {@link XGMMLConstants.EDGE_ELEMENT_LITERAL
	 *         XGMMLConstants.EDGE_ELEMENT_LITERAL}
	 */
	@Override
	public String getEdgeElementName() {
		return XGMMLConstants.EDGE_ELEMENT_LITERAL;
	}
	
}

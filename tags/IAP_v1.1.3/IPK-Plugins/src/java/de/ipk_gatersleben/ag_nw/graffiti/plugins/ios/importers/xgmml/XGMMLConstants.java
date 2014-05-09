/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: XGMMLConstants.java,v 1.1 2011-01-31 09:00:24 klukas Exp $
 * Created on 14.11.2003 by Burkhard Sell
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml;

/**
 * Defines constants used by XGMML parser.
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 */

public class XGMMLConstants {
	
	/**
	 * Public ID of XGMML
	 */
	public static final String PUBLIC_ID = "-//John Punin//DTD graph description//EN";
	
	/**
	 * System ID / DTD of XGMML
	 */
	public static final String SYSTEM_ID = "http://www.cs.rpi.edu/~puninj/XGMML/xgmml.dtd";
	
	/***/
	public static final String DOCTYPE_NAME = "graph";
	
	/** <code>graph</code> tag */
	public static final String GRAPH_ELEMENT_LITERAL = "graph";
	
	/** <code>vendor</code> attribute */
	public static final String VENDOR_ATTRIBUTE_LITERAL = "Vendor";
	
	/** <code>directed</code> attribute */
	public static final String DIRECTED_ATTRIBUTE_LITERAL = "directed";
	
	/***/
	public static final String GRAPHIC_ATTRIBUTE_LITERAL = "Graphic";
	
	/** <code>att</code> tag */
	public static final String ATT_ELEMENT_LITERAL = "att";
	
	/** <code>weighted</code> attribute */
	public static final String ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_WEIGHTED = "weighted";
	public static final String ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_DAG = "dag";
	public static final String TYPE_ATTRIBUTE_LITERAL = "type";
	public static final String NAME_ATTRIBUTE_LITERAL = "name";
	public static final String VALUE_ATTRIBUTE_LITERAL = "value";
	
	public static final String NODE_ELEMENT_LITERAL = "node";
	
	public static final String EDGE_ELEMENT_LITERAL = "edge";
	public static final String SOURCE_ATTRIBUTE_LITERAL = "source";
	public static final String TARGET_ATTRIBUTE_LITERAL = "target";
	public static final String WEIGHT_ATTRIBUTE_LITERAL = "weight";
	
	public static final String ID_ATTRIBUTE_LITERAL = "id";
	public static final String LABEL_ATTRIBUTE_LITERAL = "label";
	
	public static final String GRAPHICS_ELEMENT_LITERAL = "graphics";
	public static final String WIDTH_ATTRIBUTE_LITERAL = "w";
	public static final String HEIGHT_ATTRIBUTE_LITERAL = "h";
	public static final String FONT_ATTRIBUTE_LITERAL = "font";
	public static final String VISIBLE_ATTRIBUTE_LITERAL = "visible";
	public static final String FILL_ATTRIBUTE_LITERAL = "fill";
	public static final String OUTLINE_ATTRIBUTE_LITERAL = "outline";
	
	public static final String CENTER_ELEMENT_LITERAL = "center";
	public static final String X_ATTRIBUTE_LITERAL = "x";
	public static final String Y_ATTRIBUTE_LITERAL = "y";
	public static final String Z_ATTRIBUTE_LITERAL = "z";
	
	public static final String LINE_ELEMENT_LITERAL = "Line";
	public static final String POINT_ELEMENT_LITERAL = "point";
	
	public static final String ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_GRAPHFACTORY = "graphFactory";
	public static final String ATT_ELEMENT_NAME_ATTRIBUTE_VALUE_TRAVERSAL = "traversal";
	
	public XGMMLConstants() {
	}
}
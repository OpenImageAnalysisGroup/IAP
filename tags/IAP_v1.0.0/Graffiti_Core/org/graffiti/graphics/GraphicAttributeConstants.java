// ==============================================================================
//
// GraphicAttributeConstants.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphicAttributeConstants.java,v 1.2 2012-11-07 14:41:59 klukas Exp $

package org.graffiti.graphics;

import java.awt.BasicStroke;

import org.graffiti.attributes.Attribute;

/**
 * Defines constants used when accessing graphics attributes.
 */
public interface GraphicAttributeConstants {
	// ~ Static fields/initializers =============================================
	
	// the names of the graphic attributes
	public static final String GRAPHICS = "graphics";
	public static final String BGIMAGE = "backgroundImage";
	public static final String FRAMECOLOR = "outline";
	public static final String FILLCOLOR = "fill";
	public static final String ARROWHEAD = "arrowhead";
	public static final String ARROWTAIL = "arrowtail";
	/**
	 * Thickness of the Arrow Head (may be "1" for "auto-size")
	 */
	public static final String THICKNESS = "thickness";
	public static final String DOCKING = "docking";
	public static final String LINETYPE = "linetype";
	public static final String BENDS = "bends";
	public static final String LABEL = "text";
	public static final String LABELGRAPHICS = "labelgraphics";
	/**
	 * Thickness of the edge!!!
	 */
	public static final String FRAMETHICKNESS = "frameThickness";
	public static final String ROUNDING = "rounding";
	public static final String GRADIENT = "gradient";
	public static final String LINEMODE = "linemode";
	public static final String COORDINATE = "coordinate";
	public static final String DIMENSION = "dimension";
	public static final String SHAPE = "shape";
	public static final String SHAPEDESCRIPTION = "shapedescription";
	public static final String PORT = "port";
	public static final String PORTS = "ports";
	public static final String SOURCE = "source";
	public static final String TARGET = "target";
	public static final String RED = "red";
	public static final String GREEN = "green";
	public static final String BLUE = "blue";
	public static final String OPAC = "transparency";
	public static final String X = "x";
	public static final String Y = "y";
	public static final String HEIGHT = "height";
	public static final String WIDTH = "width";
	public static final String NAME = "name";
	public static final String TILED = "tiled";
	public static final String MAXIMIZE = "maximize";
	public static final String IMAGE = "image";
	public static final String REF = "reference";
	public static final String POSITION = "position";
	public static final String ANCHOR = "anchor";
	public static final String ALIGNMENT = "alignment";
	public static final String FONTNAME = "fontName";
	public static final String FONTSIZE = "fontSize";
	public static final String FONTSTYLE = "fontStyle";
	public static final String TEXTCOLOR = "color";
	public static final String CHARTBACKGROUNDCOLOR = "background_color";
	public static final String SHADOWCOLOR = "shadowColor";
	public static final String SHADOWOFFSET = "shadowOffset";
	public static final String GRIDCOLOR = "grid_color";
	public static final String AXISCOLOR = "axis_color";
	public static final String COLOR = "color";
	
	public static final String HEATMAP_LOWER_COL = "hm_lower_col";
	public static final String HEATMAP_MIDDLE_COL = "hm_middle_col";
	public static final String HEATMAP_UPPER_COL = "hm_upper_col";
	
	// for NodeLabelPosition:
	public static final String RELVERT = "relVert";
	public static final String RELHOR = "relHor";
	public static final String LOCALALIGN = "localAlign";
	
	// for EdgeLabelPosition:
	public static final String RELALIGN = "relAlign";
	public static final String ALIGNSEGMENT = "alignSegment";
	public static final String ABSVERT = "absVert";
	public static final String ABSHOR = "absHor";
	public static final String IN = "ingoing";
	public static final String OUT = "outgoing";
	public static final String COMMON = "common";
	public static final String STRAIGHTLINE = "straightline";
	public static final String POLYLINE = "polyline";
	public static final String SQUARESPLINE = "squarespline";
	public static final String CUBICSPLINE = "cubicspline";
	public static final String SMOOTHLINE = "smoothline";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String BELOW = "s";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String ABOVE = "n";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String RIGHT = "e";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String LEFT = "w";
	
	public static final String AUTO_OUTSIDE = "auto_outside";
	
	public static final String HIDDEN = "hidden";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String INSIDEBOTTOM = "b";
	public static final String INSIDEBOTTOMLEFT = "ibl";
	public static final String INSIDEBOTTOMRIGHT = "ibr";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String INSIDETOP = "t";
	public static final String INSIDETOPLEFT = "itl";
	public static final String INSIDETOPRIGHT = "itr";
	public static final String INSIDELEFT = "il";
	public static final String INSIDERIGHT = "ir";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String CENTERED = "c";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String CENTERED_FIT = "cf";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String BELOWRIGHT = "se";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String BELOWLEFT = "sw";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String ABOVELEFT = "nw";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String ABOVERIGHT = "ne";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String NEARSOURCE = "nearsource";
	
	/** Constant used to specify the position of a label according to a node. */
	public static final String NEARTARGET = "neartarget";
	
	public static final String BORDER_TOP_LEFT = "btl";
	public static final String BORDER_TOP_CENTER = "btc";
	public static final String BORDER_TOP_RIGHT = "btr";
	
	public static final String BORDER_BOTTOM_LEFT = "bbl";
	public static final String BORDER_BOTTOM_CENTER = "bbc";
	public static final String BORDER_BOTTOM_RIGHT = "bbr";
	
	public static final String BORDER_LEFT_TOP = "blt";
	public static final String BORDER_LEFT_CENTER = "blc";
	public static final String BORDER_LEFT_BOTTOM = "blb";
	
	public static final String BORDER_RIGHT_TOP = "brt";
	public static final String BORDER_RIGHT_CENTER = "brc";
	public static final String BORDER_RIGHT_BOTTOM = "brb";
	
	/**
	 * Distance between a label and the surrounding rectangle of the according
	 * node.
	 */
	public static final double LABEL_DISTANCE = 2.0d;
	
	/**
	 * Path at which label attributes are placed in the attribute hierarchy by
	 * defult.
	 */
	public static final String LABEL_ATTRIBUTE_PATH = "";
	
	/** Path to coordinate attribute */
	public static final String COORD_PATH = GRAPHICS + Attribute.SEPARATOR + COORDINATE;
	
	/** Path to x-coordinate attribute */
	public static final String COORDX_PATH = COORD_PATH + Attribute.SEPARATOR +
			X;
	
	/** Path to y-coordinate attribute */
	public static final String COORDY_PATH = COORD_PATH + Attribute.SEPARATOR +
			Y;
	
	/** Path to dimension attribute */
	public static final String DIM_PATH = GRAPHICS + Attribute.SEPARATOR + DIMENSION;
	
	/** Path to width attribute */
	public static final String DIMW_PATH = DIM_PATH + Attribute.SEPARATOR +
			WIDTH;
	
	/** Path to height attribute */
	public static final String DIMH_PATH = DIM_PATH + Attribute.SEPARATOR +
			HEIGHT;
	
	/** Path to fill color attribute */
	public static final String FILLCOLOR_PATH = GRAPHICS + Attribute.SEPARATOR +
			FILLCOLOR;
	
	/** Path to outline color attribute */
	public static final String OUTLINE_PATH = GRAPHICS + Attribute.SEPARATOR +
			FRAMECOLOR;
	
	/** Path to line width attribute */
	public static final String LINEWIDTH_PATH = GRAPHICS + Attribute.SEPARATOR +
			FRAMETHICKNESS;
	
	/** Path to shape attribute */
	public static final String SHAPE_PATH = GRAPHICS + Attribute.SEPARATOR +
			SHAPE;
	
	/** Path to shape description attribute */
	public static final String SHAPE_DESC_PATH = GRAPHICS +
			Attribute.SEPARATOR + SHAPEDESCRIPTION;
	
	/** Path to bends attribute */
	public static final String BENDS_PATH = GRAPHICS + Attribute.SEPARATOR +
			BENDS;
	
	/** Path to ports attribute */
	public static final String PORTS_PATH = GRAPHICS + Attribute.SEPARATOR +
			PORTS;
	
	/** Path to docking attribute */
	public static final String DOCKING_PATH = GRAPHICS + Attribute.SEPARATOR +
			DOCKING;
	
	/** Path to frame thickness attribute */
	public static final String FRAMETHICKNESS_PATH = GRAPHICS +
			Attribute.SEPARATOR + FRAMETHICKNESS;
	
	/** rectangle node shape class name */
	public static final String RECTANGLE_CLASSNAME = "org.graffiti.plugins.views.defaults.RectangleNodeShape";
	
	/** ellipse node shape class name */
	public static final String ELLIPSE_CLASSNAME = "org.graffiti.plugins.views.defaults.EllipseNodeShape";
	
	/** circle node shape class name */
	public static final String CIRCLE_CLASSNAME = "org.graffiti.plugins.views.defaults.CircleNodeShape";
	
	/** closed polygonal shape class name */
	public static final String POLYCLOSED_CLASSNAME = "org.graffiti.plugins.views.defaults.PolygonalNodeShape";
	
	/** polyline edge shape class name */
	public static final String POLYLINE_CLASSNAME = "org.graffiti.plugins.views.defaults.PolyLineEdgeShape";
	
	/** simple straight line edge shape class name */
	public static final String STRAIGHTLINE_CLASSNAME = "org.graffiti.plugins.views.defaults.StraightLineEdgeShape";
	
	/** smooth line edge shape class name */
	public static final String SMOOTH_CLASSNAME = "org.graffiti.plugins.views.defaults.SmoothLineEdgeShape";
	
	/** quadratic spline edge shape class name */
	public static final String SQUARESPLINE_CLASSNAME = "org.graffiti.plugins.views.defaults.QuadCurveEdgeShape";
	
	/** cubic spline edge shape class name; not yet implemented. */
	public static final String CUBICSPLINE_CLASSNAME = "org.graffiti.plugins.views.defaults.CubicCurveEdgeShape";
	
	public static final String CATEGORY_BACKGROUND_A = "node_categoryBackgroundColorA";
	public static final String CATEGORY_BACKGROUND_B = "node_categoryBackgroundColorB";
	public static final String CATEGORY_BACKGROUND_C = "node_categoryBackgroundColorC";
	
	/**
	 * The cap used by default.
	 * 
	 * @see java.awt.BasicStroke
	 */
	public static int DEFAULT_CAP_R = BasicStroke.CAP_ROUND;
	public static int DEFAULT_CAP_B = BasicStroke.CAP_BUTT;
	
	/**
	 * The join used by default.
	 * 
	 * @see java.awt.BasicStroke
	 */
	public static int DEFAULT_JOIN = BasicStroke.JOIN_ROUND;
	
	/**
	 * The miter limit used by default.
	 * 
	 * @see java.awt.BasicStroke
	 */
	public static float DEFAULT_MITER = 10.0f;
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

// ==============================================================================
//
// NodeGraphicAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeGraphicAttribute.java,v 1.1 2011-01-31 09:04:47 klukas Exp $

package org.graffiti.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeExistsException;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.FieldAlreadySetException;
import org.graffiti.attributes.IllegalIdException;
import org.graffiti.attributes.NodeShapeAttribute;
import org.graffiti.attributes.StringAttribute;

/**
 * Defines all grahic attributes of a node
 * 
 * @author breu
 * @version $Revision: 1.1 $
 */
public class NodeGraphicAttribute
					extends GraphElementGraphicAttribute {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor that sets the id to the given value, and initializes the
	 * other attributes with default values.
	 * 
	 * @param id
	 *           DOCUMENT ME!
	 */
	public NodeGraphicAttribute(String id) {
		super(id);
		add(new CoordinateAttribute(COORDINATE), false);
		add(new DimensionAttribute(DIMENSION, 16, 16), false);
		add(new PortsAttribute(PORTS), false);
		add(new NodeShapeAttribute(SHAPE, "org.graffiti.plugins.views.defaults.RectangleNodeShape"), false);
	}
	
	@Override
	public void add(Attribute a, boolean inform) throws AttributeExistsException, FieldAlreadySetException {
		if (attributes.containsKey(a.getId())) {
			attributes.get(a.getId()).setValue(a.getValue());
		} else
			super.add(a, inform);
	}
	
	@Override
	public void add(Attribute a) throws AttributeExistsException, FieldAlreadySetException {
		if (attributes.containsKey(a.getId())) {
			attributes.get(a.getId()).setValue(a.getValue());
		} else
			super.add(a);
	}
	
	/**
	 * Constructor for NodeGraphicAttribute.
	 * 
	 * @param c
	 *           the coordinate-value of the attriubte.
	 * @param d
	 *           the dimension-value of the attribute.
	 * @param p
	 *           the ports-value of the attribute.
	 * @throws IllegalIdException
	 */
	public NodeGraphicAttribute(CoordinateAttribute c, DimensionAttribute d,
						PortsAttribute p)
						throws IllegalIdException {
		super(GRAPHICS);
		add(new CoordinateAttribute(COORDINATE, c.getCoordinate()), false);
		add(new DimensionAttribute(DIMENSION, d.getDimension()), false);
		add(new PortsAttribute(PORTS, p.getIngoing(), p.getOutgoing(), p.getCommon()), false);
		add(new NodeShapeAttribute(SHAPE, "org.graffiti.plugins.views.defaults.RectangleNodeShape"), false);
	}
	
	/**
	 * Constructor for NodeGraphicAttribute.
	 * 
	 * @param c
	 *           the coordinate-value of the attriubte.
	 * @param d
	 *           the dimension-value of the attribute.
	 * @param p
	 *           the ports-value of the attribute.
	 * @throws IllegalIdException
	 */
	public NodeGraphicAttribute(Point2D c, Dimension d, PortsAttribute p)
						throws IllegalIdException {
		super(GRAPHICS);
		add(new CoordinateAttribute(COORDINATE, c), false);
		add(new DimensionAttribute(DIMENSION, d), false);
		add(new PortsAttribute(PORTS, p.getIngoing(), p.getOutgoing(), p.getCommon()), false);
		add(new NodeShapeAttribute(SHAPE, "org.graffiti.plugins.views.defaults.RectangleNodeShape"), false);
	}
	
	public NodeGraphicAttribute(Point2D position, Dimension size)
						throws IllegalIdException {
		this(position, size, new PortsAttribute(PORTS));
	}
	
	/**
	 * Constructor for NodeGraphicAttribute.
	 * 
	 * @param x
	 *           the x-coordinate-value of the attriubte.
	 * @param y
	 *           the y-coordinate-value of the attriubte.
	 * @param h
	 *           the height-value of the attribute.
	 * @param w
	 *           the width-value of the attribute.
	 * @param p
	 *           the ports-value of the attribute.
	 * @throws IllegalIdException
	 */
	public NodeGraphicAttribute(double x, double y, double h, double w,
						PortsAttribute p)
						throws IllegalIdException {
		super(GRAPHICS);
		add(new CoordinateAttribute(COORDINATE, x, y), false);
		add(new DimensionAttribute(DIMENSION, h, w), false);
		add(new NodeShapeAttribute(SHAPE, "org.graffiti.plugins.views.defaults.RectangleNodeShape"), false);
		add(new PortsAttribute(PORTS, p.getIngoing(), p.getOutgoing(), p.getCommon()), false);
	}
	
	/**
	 * Constructor for NodeGraphicAttribute.
	 * 
	 * @throws IllegalIdException
	 */
	public NodeGraphicAttribute()
						throws IllegalIdException {
		super(GRAPHICS);
		add(new CoordinateAttribute(COORDINATE), false);
		add(new DimensionAttribute(DIMENSION, 25, 25), false);
		add(new PortsAttribute(PORTS), false);
		add(new NodeShapeAttribute(SHAPE, "org.graffiti.plugins.views.defaults.RectangleNodeShape"), false);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the collection of attributes contained within this <tt>CollectionAttribute</tt>. The known graphic attributes are set,
	 * additional values are simply added (that means that if there exists
	 * already a subattribute with the same id, an exception will be thrown).
	 * 
	 * @param attrs
	 *           the map that contains all attributes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	@Override
	public void setCollection(Map<String, Attribute> attrs) {
		if (attrs.keySet().contains(COORDINATE) &&
							attrs.keySet().contains(DIMENSION) &&
							attrs.keySet().contains(SHAPE) && attrs.keySet().contains(PORTS) &&
							attrs.keySet().contains(BGIMAGE) &&
							attrs.keySet().contains(FRAMECOLOR) &&
							attrs.keySet().contains(FILLCOLOR) &&
							attrs.keySet().contains(FRAMETHICKNESS) &&
							attrs.keySet().contains(LINEMODE)) {
			for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext();) {
				String attrId = (String) it.next();
				
				if (attrId.equals(COORDINATE)) {
					setCoordinate((CoordinateAttribute) attrs.get(COORDINATE));
				} else
					if (attrId.equals(DIMENSION)) {
						setDimension((DimensionAttribute) attrs.get(DIMENSION));
					} else
						if (attrId.equals(SHAPE)) {
							setShape(((StringAttribute) attrs.get(SHAPE)).getString());
						} else
							if (attrId.equals(PORTS)) {
								setPorts((PortsAttribute) attrs.get(PORTS));
							} else
								if (attrId.equals(BGIMAGE)) {
									setBackgroundImage((ImageAttribute) attrs.get(BGIMAGE));
								} else
									if (attrId.equals(FRAMECOLOR)) {
										setFramecolor((ColorAttribute) attrs.get(FRAMECOLOR));
									} else
										if (attrId.equals(FILLCOLOR)) {
											setFillcolor((ColorAttribute) attrs.get(FILLCOLOR));
										} else
											if (attrId.equals(FRAMETHICKNESS)) {
												setFrameThickness(((DoubleAttribute) attrs.get(
																	FRAMETHICKNESS)).getDouble());
											} else
												if (attrId.equals(LINEMODE)) {
													setLineMode((LineModeAttribute) attrs.get(LINEMODE));
												} else {
													this.add(attrs.get(it.next()));
												}
			}
		} else {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * Sets the 'coordinate'-value.
	 * 
	 * @param c
	 *           the 'coordinate'-value to be set.
	 */
	public void setCoordinate(CoordinateAttribute c) {
		remove(COORDINATE);
		add(c, false);
	}
	
	/**
	 * Returns the 'coordinate'-value of the encapsulated node.
	 * 
	 * @return the 'coordinate'-value of the encapsulated node.
	 */
	public CoordinateAttribute getCoordinate() {
		return (CoordinateAttribute) attributes.get(COORDINATE);
	}
	
	/**
	 * Sets the 'dimension'-value.
	 * 
	 * @param d
	 *           the 'dimension'-value to be set.
	 */
	public void setDimension(DimensionAttribute d) {
		remove(DIMENSION);
		add(d);
	}
	
	/**
	 * Returns the 'dimension'-value of the encapsulated node.
	 * 
	 * @return the 'dimension'-value of the encapsulated node.
	 */
	public DimensionAttribute getDimension() {
		return (DimensionAttribute) attributes.get(DIMENSION);
	}
	
	/**
	 * Sets the 'ports'-value.
	 * 
	 * @param p
	 *           the 'ports'-value to be set.
	 */
	public void setPorts(PortsAttribute p) {
		remove(PORTS);
		add(p);
	}
	
	/**
	 * Returns the 'ports'-value of the encapsulated node.
	 * 
	 * @return the 'ports'-value of the encapsulated node.
	 */
	public PortsAttribute getPorts() {
		return (PortsAttribute) attributes.get(PORTS);
	}
	// /**
	// * Returns a deep copy of this object.
	// *
	// * @return A deep copy of this object.
	// */
	// public Object copy()
	// {
	// NodeGraphicAttribute copiedAttribute = new NodeGraphicAttribute(id);
	//
	// // copy ALL of the subattributes
	// for (Attribute subAttribute : attributes.values()) {
	// Attribute subAttributeCopy = (Attribute)subAttribute.copy();
	// copiedAttribute.add(subAttributeCopy, false);
	// // try {
	// // Attribute exAttr = copiedAttribute.getAttribute(subAttributeCopy.getId());
	// // exAttr.setValue(subAttributeCopy.getValue());
	// // } catch (AttributeNotFoundException anfe) {
	// // copiedAttribute.add(subAttributeCopy, false);
	// // }
	// }
	// return copiedAttribute;
	// }
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

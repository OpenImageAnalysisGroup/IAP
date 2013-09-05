// ==============================================================================
//
// CoordinateAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: CoordinateAttribute.java,v 1.1 2011-01-31 09:04:48 klukas Exp $

package org.graffiti.graphics;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.event.AttributeEvent;

/**
 * Contains the coordinate graphic attribute.
 * 
 * @author breu
 * @version $Revision: 1.1 $
 */
public class CoordinateAttribute
					extends HashMapAttribute
					implements GraphicAttributeConstants {
	// ~ Instance fields ========================================================
	
	/** Contains horizontal coordinate */
	private DoubleAttribute x;
	
	/** Contains vertical coordinate */
	private DoubleAttribute y;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for Coordinate that sets the coordinates to a random number.
	 * 
	 * @param id
	 *           the id of the attribute.
	 */
	public CoordinateAttribute(String id) {
		this(id, 100d, 100d /* Math.random() * 400, Math.random() * 400 */);
	}
	
	/**
	 * Constructor for Coordinate.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param c
	 *           the coordinate-value of the attriubte.
	 */
	public CoordinateAttribute(String id, Point2D c) {
		this(id, c.getX(), c.getY());
	}
	
	/**
	 * Constructor for Coordinate.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param x
	 *           the x-value of the attribute.
	 * @param y
	 *           the y-value of the attribute.
	 */
	public CoordinateAttribute(String id, double x, double y) {
		super(id);
		this.x = new DoubleAttribute(X, x);
		this.y = new DoubleAttribute(Y, y);
		this.add(this.x, false);
		this.add(this.y, false);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the collection of attributes contained within this <tt>CollectionAttribute</tt>. The coordinate values are set, additional
	 * values are simply added (that means that if there exists already a
	 * subattribute with the same id, an exception will be thrown).
	 * 
	 * @param attrs
	 *           the map that contains all attributes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	@Override
	public void setCollection(Map<String, Attribute> attrs) {
		if (attrs.keySet().contains(X) && attrs.keySet().contains(Y)) {
			for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext();) {
				String attrId = (String) it.next();
				
				if (attrId.equals(X)) {
					setX(((DoubleAttribute) attrs.get(X)).getDouble());
				} else
					if (attrId.equals(Y)) {
						setY(((DoubleAttribute) attrs.get(Y)).getDouble());
					} else {
						this.add(attrs.get(it.next()));
					}
			}
		} else {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * Sets the x and y values of this coordinate to the given points' values.
	 * 
	 * @param p
	 *           <code>Point2D</code> to which this coordinate should be set.
	 */
	public void setCoordinate(Point2D p) {
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		this.x.value = p.getX();
		this.y.value = p.getY();
		callPostAttributeChanged(ae);
	}
	
	public void setCoordinate(double x, double y) {
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		this.x.value = x;
		this.y.value = y;
		callPostAttributeChanged(ae);
	}
	
	/**
	 * Returns the encapsulated coordinate.
	 * 
	 * @return the encapsulated coordinate.
	 */
	public Point2D getCoordinate() {
		return new Point2D.Double(this.getX(), this.getY());
	}
	
	/**
	 * Sets the 'x1'-value.
	 * 
	 * @param x
	 *           the 'x1'-value to be set.
	 */
	public void setX(double x) {
		this.x.setDouble(x);
	}
	
	/**
	 * Returns the 'x'-value of the encapsulated coordinate.
	 * 
	 * @return the 'x'-value of the encapsulated coordinate.
	 */
	public double getX() {
		return this.x.getDouble();
	}
	
	/**
	 * Sets the 'x2'-value.
	 * 
	 * @param y
	 *           the 'x2'-value to be set.
	 */
	public void setY(double y) {
		this.y.setDouble(y);
	}
	
	/**
	 * Returns the 'y'-value of the encapsulated coordinate.
	 * 
	 * @return the 'y'-value of the encapsulated coordinate.
	 */
	public double getY() {
		return this.y.getDouble();
	}
	
	/**
	 * Returns a deep copy of this object.
	 * 
	 * @return A deep copy of this object.
	 */
	@Override
	public Object copy() {
		CoordinateAttribute copied = new CoordinateAttribute(this.getId(), getX(), getY());
		return copied;
	}
	
	// /**
	// * Sets the value of this <code>Attribute</code> to the given value without
	// * informing the <code>ListenerManager</code>.
	// *
	// * @param v the new value.
	// *
	// * @exception IllegalArgumentException if <code>v</code> is not of the
	// * apropriate type.
	// */
	// protected void doSetValue(Object v)
	// throws IllegalArgumentException {
	// if(v instanceof Point2D) {
	// Point2D coord = (Point2D) v;
	// setCoordinate(coord);
	// } else if(v instanceof Map) {
	// Map map = (Map) v;
	// setCollection(map);
	// } else {
	// throw new IllegalArgumentException("Invalid value type.");
	// }
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

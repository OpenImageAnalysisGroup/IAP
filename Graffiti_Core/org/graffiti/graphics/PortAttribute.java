// ==============================================================================
//
// PortAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PortAttribute.java,v 1.1 2011-01-31 09:04:46 klukas Exp $

package org.graffiti.graphics;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.StringAttribute;

/**
 * Contains information about the port of a node
 * 
 * @author breu
 * @version $Revision: 1.1 $
 */
public class PortAttribute
					extends HashMapAttribute
					implements GraphicAttributeConstants {
	// ~ Instance fields ========================================================
	
	/** Holds the coordinate of the port. */
	private CoordinateAttribute coordinate;
	
	/** The or a port representing the given name and coorinate. */
	private Port port = null;
	
	/** Holds the name of the port. */
	private StringAttribute name;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for Port.
	 * 
	 * @param id
	 *           the id of the attribute.
	 */
	public PortAttribute(String id) {
		super(id);
		this.name = (StringAttribute) StringAttribute.getTypedStringAttribute(NAME, "");
		this.coordinate = new CoordinateAttribute(COORDINATE);
		add(this.name, false);
		add(this.coordinate, false);
	}
	
	/**
	 * Constructor with an ID and a <code>Port</code> object.
	 * 
	 * @param id
	 * @param port
	 */
	public PortAttribute(String id, Port port) {
		this(id, port.getName(), port.getX(), port.getY());
		this.port = port;
	}
	
	/**
	 * Constructor for Port.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param n
	 *           the name-value of the attribute.
	 * @param c
	 *           the coordinate-value of the attribute.
	 */
	public PortAttribute(String id, String n, CoordinateAttribute c) {
		this(id, n, c.getX(), c.getY());
	}
	
	/**
	 * Constructor for Port.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param n
	 *           the name-value of the attribute.
	 * @param c
	 *           the coordinate-value of the attribute.
	 */
	public PortAttribute(String id, String n, Point2D c) {
		this(id, n, c.getX(), c.getY());
	}
	
	/**
	 * Constructor for Port.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param n
	 *           the name-value of the attribute.
	 * @param x
	 *           the x-coordinate-value of the attribute.
	 * @param y
	 *           the y-coordinate-value of the attribute.
	 */
	public PortAttribute(String id, StringAttribute n, double x, double y) {
		this(id, n.getString(), x, y);
	}
	
	/**
	 * Constructor for Port.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param n
	 *           the name-value of the attribute.
	 * @param c
	 *           the coordinate-value of the attribute.
	 */
	public PortAttribute(String id, StringAttribute n, CoordinateAttribute c) {
		this(id, n.getString(), c.getX(), c.getY());
	}
	
	/**
	 * Constructor for Port.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param n
	 *           the name-value of the attribute.
	 * @param c
	 *           the coordinate-value of the attribute.
	 */
	public PortAttribute(String id, StringAttribute n, Point2D c) {
		this(id, n.getString(), c.getX(), c.getY());
	}
	
	/**
	 * Constructor for Port.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param n
	 *           the name-value of the attribute.
	 * @param x
	 *           the x-coordinate-value of the attribute.
	 * @param y
	 *           the y-coordinate-value of the attribute.
	 */
	public PortAttribute(String id, String n, double x, double y) {
		super(id);
		this.name = (StringAttribute) StringAttribute.getTypedStringAttribute(NAME, n);
		this.coordinate = new CoordinateAttribute(COORDINATE, x, y);
		add(this.name, false);
		add(this.coordinate, false);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the collection of attributes contained within this <tt>CollectionAttribute</tt>
	 * 
	 * @param attrs
	 *           the map that contains all attributes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	@Override
	public void setCollection(Map<String, Attribute> attrs) {
		if (attrs.keySet().contains(NAME) &&
							attrs.keySet().contains(COORDINATE)) {
			for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext();) {
				String attrId = (String) it.next();
				
				if (attrId.equals(NAME)) {
					setName(((StringAttribute) attrs.get(NAME)).getString());
				} else
					if (attrId.equals(COORDINATE)) {
						setCoordinate((CoordinateAttribute) attrs.get(COORDINATE));
					} else {
						this.add(attrs.get(it.next()));
					}
			}
		} else {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * Sets the 'port'-value.
	 * 
	 * @param c
	 *           the 'port'-value to be set.
	 */
	public void setCoordinate(CoordinateAttribute c) {
		coordinate.setCoordinate(c.getCoordinate());
	}
	
	/**
	 * Returns the 'port'-value of the encapsulated port.
	 * 
	 * @return the 'port'-value of the encapsulated port.
	 */
	public CoordinateAttribute getCoordinate() {
		return this.coordinate;
	}
	
	/**
	 * Sets the 'name'-value.
	 * 
	 * @param n
	 *           the 'name'-value to be set.
	 */
	public void setName(String n) {
		this.name.setString(n);
	}
	
	/**
	 * Returns the 'name'-value of the encapsulated port.
	 * 
	 * @return the 'name'-value of the encapsulated port.
	 */
	@Override
	public String getName() {
		return this.name.getString();
	}
	
	/**
	 * Returns the encapsulated port.
	 * 
	 * @return Port
	 */
	public Port getPort() {
		if (this.port == null) {
			return new Port(this.name.getString(), this.coordinate.getX(),
								this.coordinate.getY());
		} else {
			return this.port;
		}
	}
	
	/**
	 * Returns a deep copy of this object.
	 * 
	 * @return A deep copy of this object.
	 */
	@Override
	public Object copy() {
		PortAttribute copied = new PortAttribute(this.getId());
		copied.setName(this.getName());
		copied.setCoordinate((CoordinateAttribute) (this.getCoordinate().copy()));
		
		return copied;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof PortAttribute)) {
			return false;
		}
		
		PortAttribute pa = (PortAttribute) obj;
		
		if (!name.getString().equals(pa.getName())) {
			return false;
		}
		
		if (port != null) {
			return port.equals(pa.getPort());
		} else {
			Port paport = pa.getPort();
			
			return (coordinate.getX() == paport.getX()) &&
								(coordinate.getY() == paport.getY());
		}
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
	// throws IllegalArgumentException
	// {
	// try
	// {
	// this.name = new StringAttribute(NAME, ((PortAttribute) v).getName());
	// this.coordinate = (CoordinateAttribute) ((PortAttribute) v).getCoordinate()
	// .copy();
	// }
	// catch(ClassCastException cce)
	// {
	// throw new IllegalArgumentException("Invalid value type.");
	// }
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

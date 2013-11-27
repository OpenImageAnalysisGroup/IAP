// ==============================================================================
//
// PortsAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PortsAttribute.java,v 1.1 2011-01-31 09:04:47 klukas Exp $

package org.graffiti.graphics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.util.MultipleIterator;

/**
 * Contains ingoing, outgoing and common ports
 * 
 * @author breu
 * @version $Revision: 1.1 $
 */
public class PortsAttribute
					extends HashMapAttribute
					implements GraphicAttributeConstants {
	// ~ Instance fields ========================================================
	
	/** Holds all ports. */
	private CollectionAttribute common;
	
	/** Holds all ingoing ports. */
	private CollectionAttribute ingoing;
	
	/** Holds all outgoing ports. */
	private CollectionAttribute outgoing;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for Ports.
	 * 
	 * @param id
	 *           the id of the attribute.
	 */
	public PortsAttribute(String id) {
		super(id);
		this.ingoing = new HashMapAttribute(IN);
		this.outgoing = new HashMapAttribute(OUT);
		this.common = new HashMapAttribute(COMMON);
		add(this.ingoing, false);
		add(this.outgoing, false);
		add(this.common, false);
	}
	
	/**
	 * Constructor for Ports.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param i
	 *           the ingoing-value of the attribute.
	 * @param o
	 *           the outgoing-value of the attribute.
	 * @param c
	 *           the common-value of the attribute.
	 */
	public PortsAttribute(String id, CollectionAttribute i,
						CollectionAttribute o, CollectionAttribute c) {
		super(id);
		this.ingoing = new HashMapAttribute(IN);
		this.ingoing.setCollection(i.getCollection());
		this.outgoing = new HashMapAttribute(OUT);
		this.outgoing.setCollection(o.getCollection());
		this.common = new HashMapAttribute(COMMON);
		this.common.setCollection(c.getCollection());
		add(this.ingoing, false);
		add(this.outgoing, false);
		add(this.common, false);
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
		if (attrs.keySet().contains(IN) && attrs.keySet().contains(OUT) &&
							attrs.keySet().contains(COMMON)) {
			for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext();) {
				String attrId = (String) it.next();
				
				if (attrId.equals(IN)) {
					setIngoing((CollectionAttribute) attrs.get(IN));
				} else
					if (attrId.equals(OUT)) {
						setOutgoing((CollectionAttribute) attrs.get(OUT));
					} else
						if (attrId.equals(COMMON)) {
							setCommon((CollectionAttribute) attrs.get(COMMON));
						} else {
							this.add(attrs.get(it.next()));
						}
			}
		} else {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * Sets the 'common'-value.
	 * 
	 * @param c
	 *           the 'common'-value to be set.
	 */
	public void setCommon(CollectionAttribute c) {
		this.common.setCollection(c.getCollection());
		
		// this.common = c;
	}
	
	/**
	 * Returns the 'common'-value of the encapsulated ports.
	 * 
	 * @return the 'common'-value of the encapsulated ports.
	 */
	public CollectionAttribute getCommon() {
		return this.common;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param ports
	 *           DOCUMENT ME!
	 */
	@SuppressWarnings("unchecked")
	public void setCommonPorts(List ports) {
		Map portsMap = new HashMap();
		int i = 1;
		
		for (Iterator pit = ports.iterator(); pit.hasNext();) {
			Port port = (Port) pit.next();
			PortAttribute portAttr = new PortAttribute("port" + i,
								port.getName(), port.getX(), port.getY());
			portsMap.put("port" + i++, portAttr);
		}
		
		this.common.setCollection(portsMap);
	}
	
	/**
	 * Sets the 'ingoing'-value.
	 * 
	 * @param i
	 *           the 'ingoing'-value to be set.
	 */
	public void setIngoing(CollectionAttribute i) {
		this.ingoing.setCollection(i.getCollection());
		
		// this.ingoing = i;
	}
	
	/**
	 * Returns the 'ingoing'-value of the encapsulated ports.
	 * 
	 * @return the 'ingoing'-value of the encapsulated ports.
	 */
	public CollectionAttribute getIngoing() {
		return this.ingoing;
	}
	
	/**
	 * Sets the 'outgoing'-value.
	 * 
	 * @param o
	 *           the 'outgoing'-value to be set.
	 */
	public void setOutgoing(CollectionAttribute o) {
		this.ingoing.setCollection(o.getCollection());
		
		// this.outgoing = o;
	}
	
	/**
	 * Returns the 'outgoing'-value of the encapsulated ports.
	 * 
	 * @return the 'outgoing'-value of the encapsulated ports.
	 */
	public CollectionAttribute getOutgoing() {
		return this.outgoing;
	}
	
	/**
	 * Look if there is a <code>PortAttribute</code> in this <code>CollectionAttribute</code> called <code>name</code>. Returns <code>null </code> if there is no
	 * such attribute.
	 * 
	 * @param name
	 *           the name of the port attribute wanted.
	 * @param out
	 *           DOCUMENT ME!
	 * @return the <code>PortAttribute</code> named <code>name</code> or <code>null</code> if no such attribute exists.
	 */
	public PortAttribute getPort(String name, boolean out) {
		Map<?, ?> commonPortAttributesMap = this.common.getCollection();
		Collection<?> commonPortAttributes = commonPortAttributesMap.values();
		Map<?, ?> otherPortAttributesMap;
		
		if (out) {
			otherPortAttributesMap = outgoing.getCollection();
		} else {
			otherPortAttributesMap = ingoing.getCollection();
		}
		
		Collection<?> otherPortAttributes = otherPortAttributesMap.values();
		
		MultipleIterator it = new MultipleIterator(commonPortAttributes.iterator(),
							otherPortAttributes.iterator());
		
		for (; it.hasNext();) {
			PortAttribute port = (PortAttribute) it.next();
			
			if (port.getName().equals(name)) {
				return port;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a deep copy of this object.
	 * 
	 * @return A deep copy of this object.
	 */
	@Override
	public Object copy() {
		PortsAttribute copied = new PortsAttribute(this.getId(), getIngoing(), getOutgoing(), getCommon());
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
	// throws IllegalArgumentException
	// {
	// PortsAttribute tmp;
	//
	// try
	// {
	// tmp = (PortsAttribute) v;
	// }
	// catch(ClassCastException cce)
	// {
	// throw new IllegalArgumentException("Invalid value type.");
	// }
	//
	// this.ingoing = (CollectionAttribute) tmp.getIngoing().copy();
	// this.outgoing = (CollectionAttribute) tmp.getOutgoing().copy();
	// this.common = (CollectionAttribute) tmp.getCommon().copy();
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

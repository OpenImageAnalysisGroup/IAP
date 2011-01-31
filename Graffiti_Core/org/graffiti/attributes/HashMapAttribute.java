// ==============================================================================
//
// HashMapAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: HashMapAttribute.java,v 1.1 2011-01-31 09:04:41 klukas Exp $

package org.graffiti.attributes;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.ErrorMsg;

/**
 * Maps a given id to an attribute. (e.g. if this Attribute contains 2 other
 * attributes, one StringAttribute with id &quot;Name&quot; and another
 * CollectionAttribute with id &quot;Color&quot; which contains three
 * IntegerAttributes ('red', 'green' and 'blue'). The map then contains only
 * the two key-value pairs 'Name'-StringAttribute and
 * 'Color'-CollectionAttribute. The subattributes 'red', 'green' and 'blue'
 * are not mapped in this Attribute!
 * 
 * @version $Revision: 1.1 $
 * @see CollectionAttribute
 * @see CompositeAttribute
 */
public class HashMapAttribute
					extends AbstractCollectionAttribute
					implements CollectionAttribute, Comparable<Object> {
	// ~ Constructors ===========================================================
	
	/**
	 * Construct a new instance of a <code>HashMapAttribute</code>. The
	 * internal HashMap is initialized empty.
	 * 
	 * @param id
	 *           the id of the attribute.
	 */
	public HashMapAttribute(String id) {
		super(id);
		this.attributes = new TreeMap<String, Attribute>();
	}
	
	public HashMapAttribute() {
		super("undefined id");
		this.attributes = new TreeMap<String, Attribute>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the collection of attributes contained within this <tt>CollectionAttribute</tt> For each entry in the map, pre- and post-
	 * AttributeAdded events are generated since method <code>add(Attribute
	 * a)</code> is called for each attribute in the map.
	 * 
	 * @param attrs
	 *           the Map that contains all attributes.
	 */
	public void setCollection(Map<String, Attribute> attrs) {
		assert attrs != null;
		attributes = new TreeMap<String, Attribute>();
		
		Iterator<Attribute> it = attrs.values().iterator();
		
		if (getAttributable() == null) {
			while (it.hasNext()) {
				Attribute attr = (Attribute) it.next();
				Object o = attr.copy();
				if (o != null)
					this.add((Attribute) attr.copy(), false);
				else
					ErrorMsg.addErrorMessage("Can't copy attribute: id:" + attr.getId() + ", desc:" + attr.getDescription() + ", val:" + attr.getValue());
			}
		} else {
			while (it.hasNext()) {
				Attribute attr = (Attribute) it.next();
				this.add((Attribute) attr.copy(), false);
			}
		}
	}
	
	/**
	 * Returns a cloned map (shallow copy of map: i.e. <code>this.map.equals(getCollection())</code><b>but
	 * not</b><code>this.map == getCollection()</code>) between attributes'
	 * ids and attributes contained in this attribute.
	 * 
	 * @return a clone of the list of attributes in this attribute.
	 */
	public Map<String, Attribute> getCollection() {
		return attributes;
	}
	
	/**
	 * Already done in constructor for this attribute type.
	 * 
	 * @see org.graffiti.attributes.Attribute#setDefaultValue()
	 */
	public void setDefaultValue() {
		if (attributes == null)
			this.attributes = new TreeMap<String, Attribute>();
	}
	
	/**
	 * Copies this <code>CollectionAttribute</code> and returns the copy. All
	 * sub-attributes will be copied, too, i.e. a deep-copy is returned.
	 * 
	 * @return a copy of the <code>CollectionAttribute</code>.
	 */
	public Object copy() {
		if (this.getClass() == HashMapAttribute.class) {
			HashMapAttribute ha = new HashMapAttribute(getId());
			ha.setId(getId());
			try {
				ha.setCollection(getCollection());
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			return ha;
		} else {
			try {
				Constructor<? extends HashMapAttribute> con = this.getClass().getConstructor(new Class[] { String.class });
				HashMapAttribute copiedAttributes = (HashMapAttribute) con.newInstance(new Object[] { getId() });
				Collection<Attribute> values = attributes.values();
				for (Attribute attr : values) {
					// System.out.println("Copy SubAttr: "+attr.getClass().getSimpleName()+": "+attr.getId());
					Attribute copiedAttribute = (Attribute) attr.copy();
					copiedAttribute.setParent(copiedAttributes);
					copiedAttributes.attributes.put(attr.getId(), copiedAttribute);
				}
				return copiedAttributes;
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		}
	}
	
	/**
	 * Sets the value of the attribute by calling method <code>setCollection(Map attrs)</code>. The "value" is the Collection of
	 * attributes. For each entry in the map, pre- and post- AttributeAdded
	 * events are generated.
	 * 
	 * @param o
	 *           the new value of the attribute.
	 * @exception IllegalArgumentException
	 *               if the parameter has not the
	 *               appropriate class for this attribute.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void doSetValue(Object o)
						throws IllegalArgumentException {
		assert o != null;
		
		Map<String, Attribute> attrs;
		
		try {
			attrs = (Map<String, Attribute>) o;
			setCollection(attrs);
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Wrong argument type " +
								"(Collection's value: HashMap - expected): " + cce.getMessage());
		}
	}
	
	public int compareTo(Object o) {
		if (o instanceof Attribute) {
			return idd.compareTo(((Attribute) o).getId());
		}
		return 0;
	}
	
	public int size() {
		return attributes.size();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

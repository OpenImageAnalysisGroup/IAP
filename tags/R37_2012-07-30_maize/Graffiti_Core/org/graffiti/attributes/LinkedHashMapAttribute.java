// ==============================================================================
//
// LinkedHashMapAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LinkedHashMapAttribute.java,v 1.1 2011-01-31 09:04:42 klukas Exp $

package org.graffiti.attributes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.graffiti.plugin.XMLHelper;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 */
public class LinkedHashMapAttribute
					extends AbstractCollectionAttribute
					implements SortedCollectionAttribute {
	// ~ Constructors ===========================================================
	
	/**
	 * Construct a new instance of a <code>LinkedHashMapAttribute</code>. The
	 * internal LinkedHashMap is initialized empty.
	 * 
	 * @param id
	 *           the id of the attribute.
	 */
	public LinkedHashMapAttribute(String id) {
		super(id);
		this.attributes = new LinkedHashMap<String, Attribute>();
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
		attributes = new LinkedHashMap<String, Attribute>();
		
		Iterator<Attribute> it = attrs.values().iterator();
		
		if (getAttributable() == null) {
			while (it.hasNext()) {
				Attribute attr = (Attribute) it.next();
				this.add((Attribute) attr.copy(), false);
			}
		} else {
			while (it.hasNext()) {
				Attribute attr = (Attribute) it.next();
				this.add((Attribute) attr.copy());
			}
		}
	}
	
	/**
	 * Returns a cloned map (shallow copy of map: i.e. <code>this.map.equals(getCollection())</code><b>but
	 * not</b><code>this.map == getCollection()</code>) between attributes'
	 * ids and attributes contained in this <code>CollectionAttribute</code>.
	 * 
	 * @return a clone of the list of attributes in this <code>CollectionAttribute</code>.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Attribute> getCollection() {
		return (LinkedHashMap) ((LinkedHashMap) attributes).clone();
	}
	
	public LinkedHashMap<String, Attribute> getCollectionNoClone() {
		return (LinkedHashMap<String, Attribute>) attributes;
	}
	
	/**
	 * Already done in constructor for this attribute type.
	 * 
	 * @see org.graffiti.attributes.Attribute#setDefaultValue()
	 */
	public void setDefaultValue() {
	}
	
	/**
	 * Copies this <code>CollectionAttribute</code> and returns the copy. All
	 * sub-attributes will be copied, too, i.e. a deep-copy is returned.
	 * 
	 * @return a copy of the <code>CollectionAttribute</code>.
	 */
	public Object copy() {
		LinkedHashMapAttribute copiedAttributes =
							new LinkedHashMapAttribute(this.getId());
		
		// M.S.: w�re es hier nicht sinnvoller �ber attributes.values() zu
		// iterieren? getId() ist wahrscheinlich schneller als get(Id)
		// bzw. hat eine kleinerer Konstante...
		for (Iterator<String> i = attributes.keySet().iterator(); i.hasNext();) {
			String attrId = (String) i.next();
			Attribute attr = attributes.get(attrId);
			Attribute copiedAttribute = (Attribute) attr.copy();
			copiedAttribute.setParent(this);
			copiedAttributes.attributes.put(attrId, copiedAttribute);
		}
		
		return copiedAttributes;
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
	@SuppressWarnings("unchecked")
	@Override
	protected void doSetValue(Object o)
						throws IllegalArgumentException {
		assert o != null;
		
		HashMap attrs;
		
		try {
			attrs = (LinkedHashMap) o;
		} catch (ClassCastException cce) {
			try {
				attrs = (HashMap) o;
			} catch (ClassCastException cce2) {
				throw new IllegalArgumentException("Wrong argument type " +
									"((Linked)HashMap expected).");
			}
		}
		
		setCollection(attrs);
	}
	
	/**
	 * @see org.graffiti.plugin.Displayable#toXMLString()
	 */
	@Override
	public String toXMLString() {
		StringBuffer valString = new StringBuffer();
		valString.append("<subAttributes>" + XMLHelper.getDelimiter());
		for (Iterator<Attribute> it = attributes.values().iterator(); it.hasNext();) {
			Attribute attr = (Attribute) it.next();
			valString.append(XMLHelper.spc(6) + "<subattr>" +
								attr.toXMLString() + "</subattr>" + XMLHelper.getDelimiter());
		}
		valString.append(XMLHelper.spc(4) + "</subAttributes>" +
							XMLHelper.getDelimiter() + XMLHelper.spc(4) +
							"<sorted>true</sorted>");
		
		return getStandardXML(valString.toString());
	}
	
	public int size() {
		return attributes.size();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

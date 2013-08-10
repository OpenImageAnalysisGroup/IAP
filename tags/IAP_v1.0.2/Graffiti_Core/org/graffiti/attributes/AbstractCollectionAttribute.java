// ==============================================================================
//
// AbstractCollectionAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractCollectionAttribute.java,v 1.1 2011-01-31 09:04:41 klukas Exp $

package org.graffiti.attributes;

import java.util.Iterator;
import java.util.Map;

import org.graffiti.event.AttributeEvent;
import org.graffiti.plugin.XMLHelper;

/**
 * Provides common functionality for <code>CollectionAttribute</code> instances. Calls the <code>ListenerManager</code> and delegates the
 * functionality to the implementing class.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AbstractCollectionAttribute extends AbstractAttribute
					implements CollectionAttribute {
	// ~ Static fields/initializers =============================================
	
	// ~ Instance fields ========================================================
	
	/**
	 * The internal map which maps the ids to the Attributes which are in this <code>CollectionAttribute</code>.
	 */
	protected Map<String, Attribute> attributes;
	
	/**
	 * The <code>Attributable</code> of this <code>Attribute</code>. This
	 * reference is <code>null</code> except for the root.
	 */
	private Attributable attributable;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for setting the id of an <code>AbstractCollectionAttribute</code>.
	 * 
	 * @param id
	 *           the id of the <code>Attribute</code>.
	 * @exception IllegalIdException
	 *               if the given id contains a seperator. This
	 *               is checked for in the constructor of the superclass <code>AbstractAttribute</code>.
	 */
	public AbstractCollectionAttribute(String id) throws IllegalIdException {
		super(id);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the <code>Attribute</code>'s <code>Attributable</code>.
	 * <p>
	 * <b>Implementation Notes:</b> This method should only be called once and only by an <code>addAttribute()</code> method call! The attributable property may
	 * only be set on the root <code>Attribute</code> of a hierarchy
	 * </p>
	 * 
	 * @param att
	 *           the new <code>Attributable</code> of the <code>Attribute</code>.
	 * @throws FieldAlreadySetException
	 *            DOCUMENT ME!
	 */
	public void setAttributable(Attributable att)
						throws FieldAlreadySetException {
		assert att != null : "must not set attributable to null";
		assert this.getParent() == null : "Only the root attribute has a reference to the attributable "
							+ " the hierarchy belongs to. Only call setAttributable on "
							+ "attributes where parent == null.";
		
		// different from setParent, attributable is only null when
		// not set before
		if (this.attributable != null) {
			throw new FieldAlreadySetException("'attributable' field already set");
		} else {
			this.attributable = att;
		}
	}
	
	/**
	 * Returns the <code>Attribute</code>'s <code>Attributable</code>.
	 * 
	 * @return the <code>Attribute</code>'s <code>Attributable</code>.
	 */
	@Override
	public Attributable getAttributable() {
		Attribute parent = getParent();
		
		if (parent == null) {
			return attributable;
		} else {
			return parent.getAttributable();
		}
	}
	
	private static int sepLen = Attribute.SEPARATOR.length();
	
	/**
	 * Returns the attribute located at <code>path</code>.
	 * 
	 * @param path
	 *           the relative path to the attribute from <code>this</code>.
	 * @return the attribute found at <code>path</code>.
	 * @exception AttributeNotFoundException
	 *               if there is no attribute located
	 *               at path.
	 */
	public Attribute getAttribute(String path) throws AttributeNotFoundException {
		assert path != null;
		if (path == null || path.length() <= 0) {
			return this;
		} else {
			int sepPos = path.indexOf(Attribute.SEPARATOR);
			if (sepPos < 0) {
				Attribute attr = attributes.get(path);
				if (attr == null)
					throw new AttributeNotFoundException(null); // "Did not find sub attribute with ID " + path);
				return attr;
			} else {
				String s0 = path.substring(0, sepPos);
				String s1 = path.substring(sepPos + sepLen);
				Attribute attr = attributes.get(s0);
				if (attr == null) {
					throw new AttributeNotFoundException(null); // "Did not find sub attribute with ID " + path);
				} else {
					try {
						return ((CollectionAttribute) attr).getAttribute(s1);
					} catch (ClassCastException cce) {
						throw new AttributeNotFoundException(null); /*
																					 * "Attribute with ID "
																					 * + s0 + " is no "
																					 * + "CollectionAttribute and therefore can't "
																					 * + "contain subattribute with ID " + s1);
																					 */
					}
				}
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if the HashMapAttribute is empty. The same as <code>getCollection().isEmpty()</code> would yield, but this method
	 * should be faster since the map is not copied.
	 * 
	 * @return <code>true</code> if the HashMapAttribute is empty.
	 */
	public boolean isEmpty() {
		return attributes.isEmpty();
	}
	
	/**
	 * Returns the value of this attribute, i.e. map between contained
	 * attributes' ids and these attributes. The behaviour of this method
	 * depends on implementation of method <code>getCollection()</code> in
	 * concret classes which inherit this one. See documentation of concret
	 * classes for more information.
	 * 
	 * @return the value of this attribute.
	 */
	public Object getValue() {
		return getCollection();
	}
	
	/**
	 * Adds a given attribute to the collection. Informs the <code>ListenerManager</code> about the addition.
	 * 
	 * @param a
	 *           the new attribute to add to the list.
	 * @exception AttributeExistsException
	 *               if there is already an attribute
	 *               with the id of a.
	 * @exception FieldAlreadySetException
	 *               thrown if Attribute a already has a
	 *               parent or attributable associated with it.
	 */
	public void add(Attribute a) throws AttributeExistsException,
						FieldAlreadySetException {
		if (a == null)
			return;
		if (a.getId() == null)
			return;
		
		assert a != null;
		
		String attrId = a.getId();
		
		if (attributes.containsKey(attrId)) {
			try {
				attributes.get(attrId).setValue(a.getValue());
			} catch (Exception e) {
				try {
					attributes.remove(attrId);
					AttributeEvent attrEvent = new AttributeEvent(a);
					callPreAttributeAdded(attrEvent);
					a.setParent(this);
					attributes.put(attrId, a);
					callPostAttributeAdded(attrEvent);
				} catch (Exception e2) {
					throw new AttributeExistsException("Attribute with ID " + attrId
										+ " already exists in " + "this HashMapAttribute!");
				}
			}
		} else {
			AttributeEvent attrEvent = new AttributeEvent(a);
			callPreAttributeAdded(attrEvent);
			a.setParent(this);
			attributes.put(attrId, a);
			callPostAttributeAdded(attrEvent);
		}
	}
	
	/**
	 * Adds a given attribute to the collection. Only informs the <code>ListenerManager</code> about the addition when <code>inform</code> is set to true.
	 * 
	 * @param a
	 *           the new attribute to add to the list.
	 * @param inform
	 *           when true, <code>ListenerManager</code> gets informed
	 *           otherwise not
	 * @exception AttributeExistsException
	 *               if there is already an attribute
	 *               with the id of a.
	 * @exception FieldAlreadySetException
	 *               thrown if Attribute a already has a
	 *               parent or attributable associated with it.
	 */
	public void add(Attribute a, boolean inform)
						throws AttributeExistsException, FieldAlreadySetException {
		assert a != null;
		
		if (inform)
			add(a);
		else {
			// logger.warning("Adding Attribute with id " + id + " without " +
			// "informing the ListenerManager.");
			
			if (a == null)
				System.err.println("internal error: try to add null attribute...");
			String attrId = a.getId();
			// if (attributes.containsKey(attrId)) {
			// attributes.remove(attrId);
			// }
			if (attributes.containsKey(attrId)) {
				// System.out.println("Attribute with id " + attrId + " already exists.");
				throw new AttributeExistsException("Attribute with ID " + attrId
									+ "already exists in " + "this HashMapAttribute!");
			} else {
				a.setParent(this);
				attributes.put(attrId, a);
			}
		}
	}
	
	/**
	 * Removes the attribute with the given id from the collection. Notifies <code>ListenerManager</code> with an AttributeRemoved event when the
	 * attribute hierarchy is attached to an <code>Attributable</code>.
	 * 
	 * @param attrId
	 *           the id of the attribute.
	 * @exception AttributeNotFoundException
	 *               if there is no attribute with the
	 *               given id.
	 * @throws IllegalIdException
	 *            DOCUMENT ME!
	 */
	public void remove(String attrId) throws AttributeNotFoundException {
		assert attrId != null;
		if (attrId.indexOf(Attribute.SEPARATOR) != -1) {
			throw new IllegalIdException(
								"An id must not contain the SEPARATOR chararcter.");
		} else {
			Attribute attr = attributes.get(attrId);
			
			if (attr == null) {
				throw new AttributeNotFoundException("Attribute with ID " + attrId
									+ "does not exist in " + "this HashMapAttribute");
			} else {
				// notify ListenerManager
				AttributeEvent attrEvent = new AttributeEvent(attr);
				callPreAttributeRemoved(attrEvent);
				attributes.remove(attrId);
				callPostAttributeRemoved(attrEvent);
			}
		}
	}
	
	/**
	 * Removes the given attribute from the collection by calling <code>remove(String id)</code> with the attribute's id as parameter.
	 * Notifies <code>ListenerManager</code> with an AttributeRemoved event
	 * when the attribute hierarchy is attached to an <code>Attributable</code>.
	 * 
	 * @param attr
	 *           the attribute to be removed.
	 * @exception AttributeNotFoundException
	 *               if the given attribute is not in
	 *               the HashMap.
	 */
	public void remove(Attribute attr) throws AttributeNotFoundException {
		assert attr != null;
		
		CollectionAttribute parent = attr.getParent();
		
		if (parent == null) {
			AttributeEvent attrEvent = new AttributeEvent(attr);
			callPreAttributeRemoved(attrEvent);
			
			// remove all subattributes
			for (Iterator<?> itr = getCollection().keySet().iterator(); itr.hasNext();) {
				String s = (String) itr.next();
				attributes.remove(s);
			}
			
			callPostAttributeRemoved(attrEvent);
		} else {
			// parent.remove cares about sending events to the ListenerManager
			parent.remove(attr.getId());
		}
	}
	
	/**
	 * @see org.graffiti.attributes.Attribute#toString(int)
	 */
	@Override
	public String toString(int n) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getSpaces(n) + idd + " " + getClass().getName() + " {\n");
		
		for (Iterator<Attribute> it = attributes.values().iterator(); it.hasNext();) {
			Attribute attr = (Attribute) it.next();
			sb.append(attr.toString(n + 1) + "\n");
		}
		
		sb.append(getSpaces(n) + "}");
		
		return sb.toString();
	}
	
	/**
	 * @see org.graffiti.plugin.Displayable#toXMLString()
	 */
	@Override
	public String toXMLString() {
		StringBuffer valString = new StringBuffer();
		valString.append(XMLHelper.spc(4) + "<subAttributes>"
							+ XMLHelper.getDelimiter());
		
		for (Iterator<Attribute> it = attributes.values().iterator(); it.hasNext();) {
			Attribute attr = (Attribute) it.next();
			valString.append(XMLHelper.spc(6) + "<subattr>" + attr.toXMLString()
								+ "</subattr>" + XMLHelper.getDelimiter());
		}
		
		valString.append(XMLHelper.spc(4) + "</subAttributes>"
							+ XMLHelper.getDelimiter() + XMLHelper.spc(4)
							+ "<sorted>false</sorted>");
		
		return getStandardXML(valString.toString());
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

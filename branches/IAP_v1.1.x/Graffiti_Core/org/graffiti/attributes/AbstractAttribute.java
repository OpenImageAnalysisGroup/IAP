// ==============================================================================
//
// AbstractAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractAttribute.java,v 1.2 2011-06-05 16:08:37 klukas Exp $

package org.graffiti.attributes;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AttributeHelper;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.ListenerManager;
import org.graffiti.graphics.EdgeLabelAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.graffiti.plugin.XMLHelper;

/**
 * Provides common functionality for classes implementing the <code>Attribute</code> interface. Stores the <code>id</code>, <code>parent</code> and
 * <code>attributable</code> of the <code>Attribute</code>.
 * 
 * @version $Revision: 1.2 $
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractAttribute
					implements Attribute {
	protected static HashMap<String, Class> typedAttributesID2TypeForNodes = getDefaultNodeTypedAttributes();
	protected static HashMap<String, Class> typedAttributesID2TypeForEdges = getDefaultEdgeTypedAttributes();
	
	public void setId(String id) {
		if (id == null)
			return;
		this.idd = knownAttributeNames.get(id);
		if (this.idd == null) {
			knownAttributeNames.put(id, id);
			this.idd = knownAttributeNames.get(id);
		}
	}
	
	public static void addNodeAttributeType(String id, Class<?> type) {
		if (typedAttributesID2TypeForNodes.containsKey(id)) {
			// System.out.println("Information: overwriting previous attribute class mapping for id "+id+"");
		}
		typedAttributesID2TypeForNodes.put(id, type);
	}
	
	public static void addEdgeAttributeType(String id, Class<?> type) {
		if (typedAttributesID2TypeForEdges.containsKey(id)) {
			// System.out.println("Information: overwriting previous attribute class mapping for id "+id+"");
		}
		typedAttributesID2TypeForEdges.put(id, type);
	}
	
	@SuppressWarnings("unchecked")
	public static Attribute getTypedAttribute(String id, boolean isNodeTrue_isEdgeFalse) {
		Class c;
		if (isNodeTrue_isEdgeFalse)
			c = typedAttributesID2TypeForNodes.get(id);
		else
			c = typedAttributesID2TypeForEdges.get(id);
		Constructor con;
		try {
			con = c.getConstructor(new Class[] { String.class });
			Attribute ta = (Attribute) con.newInstance(new Object[] { id });
			return ta;
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Class> getDefaultNodeTypedAttributes() {
		HashMap<String, Class> result = new HashMap<String, Class>();
		result.put("labelgraphics", NodeLabelAttribute.class);
		for (int i = 0; i <= 99; i++) {
			result.put("labelgraphics" + i, NodeLabelAttribute.class);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Class> getDefaultEdgeTypedAttributes() {
		HashMap<String, Class> result = new HashMap<String, Class>();
		// result.put("color", ColorAttribute.class);
		result.put("labelgraphics", EdgeLabelAttribute.class);
		result.put("srcLabel", EdgeLabelAttribute.class);
		result.put("tgtLabel", EdgeLabelAttribute.class);
		return result;
	}
	
	public static boolean isTypedAttributeFromID(String id, boolean isNodeTrue_isEdgeFalse) {
		if (isNodeTrue_isEdgeFalse)
			return typedAttributesID2TypeForNodes.containsKey(id);
		else
			return typedAttributesID2TypeForEdges.containsKey(id);
	}
	
	private static final HashMap<String, String> knownAttributeNames = new HashMap<String, String>();
	
	/** The identifier of this <code>Attribute</code>. */
	protected String idd;
	
	/**
	 * The parent <code>attribute</code>. It is set when the <code>Attribute</code> is added somewhere in the hierarchy.
	 */
	private CollectionAttribute parent;
	
	/** A String describing the function of this attribute. */
	private String description = "";
	
	/**
	 * Indicates wether the <code>parent</code> field has already been set. <code>parent</code> must not be set more then once.
	 */
	private boolean parentNotYetSet = true;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for setting the id of an <code>AbstractAttribute</code>.
	 * 
	 * @param id
	 *           the id of the <code>AbstractAttribute</code>.
	 * @exception IllegalIdException
	 *               if the given id contains a separator.
	 */
	public AbstractAttribute(String id)
						throws IllegalIdException {
		assert id != null;
		// logger.setLevel(Level.OFF);
		
		if (id.indexOf(Attribute.SEPARATOR) != -1) {
			throw new IllegalIdException(
								"An id must not contain the SEPARATOR character.");
		}
		
		setId(id);
		// logger.info("id set to " + id + ".");
		
		setDefaultValue();
	}
	
	// ~ Methods ================================================================
	
	public AbstractAttribute() {
		// empty
	}
	
	/**
	 * Returns the <code>Attribute</code>'s <code>Attributable</code>.
	 * 
	 * @return the <code>Attribute</code>'s <code>Attributable</code>.
	 */
	public Attributable getAttributable() {
		Attribute par = getParent();
		
		// this means parent is null an this is not a CollectionAttribute
		// getAttributable() is overwritten in AbstractCollectionAttribute
		if (par == null) {
			// logger.info("The attribute is not attached (properly) to an " +
			// "Attributable yet");
			
			// return attributable;
			return null;
		} else {
			return par.getAttributable();
		}
	}
	
	private static final HashMap<String, String> knownAttributeDescriptions = new HashMap<String, String>();
	
	/**
	 * Provides a description for this attribute. Used in tooltips etc.
	 * 
	 * @param desc
	 *           DOCUMENT ME!
	 */
	public void setDescription(String desc) {
		if (desc == null)
			return;
		this.description = knownAttributeDescriptions.get(desc);
		if (description == null) {
			knownAttributeDescriptions.put(desc, desc);
			this.description = knownAttributeDescriptions.get(desc);
		}
	}
	
	/**
	 * Returns a description for this attribute. Used in tooltips etc. Returns
	 * an empty string by default.
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Returns the <code>Attribute</code>'s identifier.
	 * 
	 * @return the <code>Attribute</code>'s identifier.
	 */
	public String getId() {
		return idd;
	}
	
	/**
	 * @see org.graffiti.plugin.Displayable#getName()
	 */
	public String getName() {
		return getId();
	}
	
	/**
	 * Sets the attribute's parent.
	 * <p>
	 * <b>Implementation Notes:</b> This method should only be called once and only by an <code>addAttribute()</code> method! The <code>ListenerManager</code> is
	 * already informed by the calling <code>addAttribute</code> method that the <code>Attribute</code> is now added to the hierarchy. Therefore no event is
	 * generated by this method.
	 * </p>
	 * 
	 * @param parent
	 *           the new parent of the <code>Attribute</code>.
	 * @throws FieldAlreadySetException
	 *            DOCUMENT ME!
	 */
	public void setParent(CollectionAttribute parent)
						throws FieldAlreadySetException {
		// "" as parent denotes root Attribute. therefore parent must be null
		assert !(this.getId().equals("") && (parent != null)) : "Empty id (and not root attribute).";
		
		// can't just check whether it's null because root's parent is
		// null. therefore the variable "parentNotYetSet" is used
		if (parentNotYetSet) {
			this.parent = parent;
			parentNotYetSet = false;
			// logger.fine("parent of attribute " + getId() + " set to " +
			// parent.getId());
		} else {
			throw new FieldAlreadySetException("'parent' field already set");
		}
	}
	
	/**
	 * Returns the <code>Attribute</code>'s parent, <code>null</code> if it is
	 * root.
	 * 
	 * @return the <code>Attribute</code>'s parent, <code>null</code> if it is
	 *         root.
	 */
	public CollectionAttribute getParent() {
		return parent;
	}
	
	/**
	 * Returns the <code>Attribute</code>'s location.
	 * <p>
	 * <b>Implementation Note:</b> This function constructs the path by ascending recursivly to the root attribute, using the <code>getParent()</code> and
	 * <code>getId()</code> methods,
	 * </p>
	 * 
	 * @return the path to the given <code>Attribute</code>.
	 */
	public String getPath() {
		Attribute par = this.getParent();
		
		if (par == null) {
			return this.getId();
		} else {
			return par.getPath() + Attribute.SEPARATOR + this.getId();
		}
	}
	
	/**
	 * Sets the value of this <code>Attribute</code> to the given value. If the
	 * value is set via <code>att.setValue(x)</code> and then retrieved via <code>y=att.getValue()</code> it is only guaranteed that x.equals(y)
	 * not x==y, i.e. some particular concret classes can provide x==y but
	 * this behaviour is not general, in contrast x.equals(y) must be always
	 * guaranteed.
	 * 
	 * @param v
	 *           the new value.
	 * @exception IllegalArgumentException
	 *               if v is not of the apropriate type.
	 */
	public void setValue(Object v)
						throws IllegalArgumentException {
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		doSetValue(v);
		callPostAttributeChanged(ae);
	}
	
	/**
	 * Returns the ID of this attribute.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public String toString() {
		return this.getId();
	}
	
	/**
	 * @see org.graffiti.attributes.Attribute#toString(int)
	 */
	public String toString(int n) {
		return getSpaces(n) + getId() + " = " + getValue().toString();
	}
	
	/**
	 * @see org.graffiti.plugin.Displayable#toXMLString()
	 */
	public String toXMLString() {
		String valStr = (getValue() == null) ? "null" : getValue().toString();
		
		return "<attribute classname=\\\"" + getClass().getName() +
							"\\\" path=\\\"" + getPath().substring(1) + "\\\">" +
							XMLHelper.getDelimiter() + XMLHelper.spc(2) + "<value><![CDATA[" +
							valStr + "]]>" + XMLHelper.spc(2) + "<value><![CDATA[" + valStr +
							"]]>" + XMLHelper.getDelimiter() + XMLHelper.spc(2) + "</value>" +
							XMLHelper.getDelimiter() + "</attribute>";
	}
	
	/**
	 * Embeds the given String into an XML String. It includes the classname of
	 * the parameter and a "value" element that gets the given String <code>valueString</code> as content.
	 * 
	 * @param valueString
	 * @return DOCUMENT ME!
	 */
	protected String getStandardXML(String valueString) {
		return "<attribute classname=\\\"" + getClass().getName() + "\\\">" +
							XMLHelper.getDelimiter() + XMLHelper.spc(2) + "<value>" +
							XMLHelper.getDelimiter() + XMLHelper.spc(4) + valueString +
							XMLHelper.getDelimiter() + XMLHelper.spc(2) + "</value>" +
							XMLHelper.getDelimiter() + "</attribute>";
	}
	
	/**
	 * Sets the value of this <code>Attribute</code> to the given value without
	 * informing the <code>ListenerManager</code>.
	 * 
	 * @param v
	 *           the new value.
	 * @exception IllegalArgumentException
	 *               if <code>v</code> is not of the
	 *               appropriate type.
	 */
	protected abstract void doSetValue(Object v)
						throws IllegalArgumentException;
	
	/**
	 * Returns <code>n</code> spaces.
	 * 
	 * @param n
	 *           the number of spaces.
	 * @return <code>n</code> spaces.
	 */
	protected String getSpaces(int n) {
		if (n == 0) {
			return "";
		} else
			if (n == 1) {
				return " ";
			} else
				if (n == 2) {
					return "  ";
				} else
					if (n == 3) {
						return "   ";
					} else {
						StringBuffer sb = new StringBuffer();
						
						for (int i = 0; i < n; ++i) {
							sb.append(" ");
						}
						
						return sb.toString();
					}
	}
	
	/**
	 * Informs the <code>ListenerManager</code> after an <code>Attribute</code> has been added.
	 * 
	 * @param ae
	 *           the <code>Attribute</code> which will be sent to the <code>ListenerManager</code>.
	 */
	protected void callPostAttributeAdded(AttributeEvent ae) {
		assert ae != null : "AttributeEvent is null!";
		
		Attributable attbl = getAttributable();
		
		if (attbl != null) {
			ListenerManager lm = attbl.getListenerManager();
			if (lm != null)
				lm.postAttributeAdded(ae);
		}
	}
	
	/**
	 * Informs the <code>ListenerManager</code> after the value of an <code>Attribute</code> has been changed.
	 * 
	 * @param ae
	 *           the <code>Attribute</code> which will be sent to the <code>ListenerManager</code>.
	 */
	protected void callPostAttributeChanged(AttributeEvent ae) {
		assert ae != null : "AttributeEvent is null!";
		
		Attributable attbl = getAttributable();
		
		if (attbl != null) {
			ListenerManager lm = attbl.getListenerManager();
			if (lm != null)
				lm.postAttributeChanged(ae);
		}
	}
	
	/**
	 * Informs the <code>ListenerManager</code> after an <code>Attribute</code> has been removed.
	 * 
	 * @param ae
	 *           the <code>Attribute</code> which will be sent to the <code>ListenerManager</code>.
	 */
	protected void callPostAttributeRemoved(AttributeEvent ae) {
		assert ae != null : "AttributeEvent is null!";
		
		Attributable attbl = getAttributable();
		
		if (attbl != null) {
			ListenerManager lm = attbl.getListenerManager();
			if (lm != null)
				lm.postAttributeRemoved(ae);
		}
	}
	
	/**
	 * Informs the <code>ListenerManager</code> before an <code>Attribute</code> will be added.
	 * 
	 * @param ae
	 *           the <code>Attribute</code> which will be sent to the <code>ListenerManager</code>.
	 */
	protected void callPreAttributeAdded(AttributeEvent ae) {
		assert ae != null : "AttributeEvent is null!";
		
		Attributable attbl = getAttributable();
		
		if (attbl != null) {
			ListenerManager lm = attbl.getListenerManager();
			if (lm != null)
				lm.preAttributeAdded(ae);
		}
	}
	
	/**
	 * Informs the <code>ListenerManager</code> before the value of an <code>Attribute</code> will be changed.
	 * 
	 * @param ae
	 *           the <code>Attribute</code> which will be sent to the <code>ListenerManager</code>.
	 */
	protected void callPreAttributeChanged(AttributeEvent ae) {
		assert ae != null : "AttributeEvent is null!";
		
		Attributable attbl = getAttributable();
		
		if (attbl != null) {
			ListenerManager lm = attbl.getListenerManager();
			if (lm != null)
				lm.preAttributeChanged(ae);
		}
	}
	
	/**
	 * Informs the <code>ListenerManager</code> before an <code>Attribute</code> will be removed.
	 * 
	 * @param ae
	 *           the <code>Attribute</code> which will be sent to the <code>ListenerManager</code>.
	 */
	protected void callPreAttributeRemoved(AttributeEvent ae) {
		assert ae != null : "AttributeEvent is null!";
		
		Attributable attbl = getAttributable();
		
		if (attbl != null) {
			ListenerManager lm = attbl.getListenerManager();
			if (lm != null)
				lm.preAttributeRemoved(ae);
		}
	}
	
	public JComponent getIcon() {
		ImageIcon icon = AttributeHelper.getDefaultAttributeIconFor(idd);
		if (icon != null)
			return new JLabel(icon);
		else
			return null;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

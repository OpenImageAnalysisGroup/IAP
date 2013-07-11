// ==============================================================================
//
// NodeLabelAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeLabelAttribute.java,v 1.1 2011-01-31 09:04:48 klukas Exp $

package org.graffiti.graphics;

/**
 * DOCUMENT ME!
 * 
 * @author holleis
 * @version $Revision: 1.1 $ Extends LabelAttribute by a PositionAttribute specific for nodes.
 */
public class NodeLabelAttribute
					extends LabelAttribute {
	// ~ Instance fields ========================================================
	
	/** Position of a label within this node. */
	// private NodeLabelPositionAttribute position;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for NodeLabelAttribute.
	 * 
	 * @param id
	 */
	public NodeLabelAttribute(String id) {
		super(id);
		this.add(new NodeLabelPositionAttribute(POSITION), false);
	}
	
	/**
	 * Constructor for NodeLabelAttribute.
	 * 
	 * @param id
	 * @param l
	 *           label string
	 */
	public NodeLabelAttribute(String id, String l) {
		super(id, l);
		this.add(new NodeLabelPositionAttribute(POSITION), false);
	}
	
	// ~ Methods ================================================================
	
	// /**
	// * Sets the collection of attributes contained within this
	// * <tt>CollectionAttribute</tt>
	// *
	// * @param attrs the map that contains all attributes.
	// *
	// * @throws IllegalArgumentException DOCUMENT ME!
	// */
	// public void setCollection(Map<String, Attribute> attrs) {
	// if(true//attrs.keySet().contains(LABEL) // &&
	// // attrs.keySet().contains(POSITION) &&
	// // attrs.keySet().contains(ALIGNMENT) &&
	// // attrs.keySet().contains(FONT) &&
	// // attrs.keySet().contains(TEXTCOLOR)
	// ) {
	// for(Iterator it = attrs.keySet().iterator(); it.hasNext();) {
	// String attrId = (String) it.next();
	//
	// if(attrId.equals(LABEL)) {
	// setLabel(((StringAttribute) attrs.get(LABEL)).getString());
	// } else if(attrId.equals(POSITION)) {
	// setPosition((NodeLabelPositionAttribute) attrs.get(POSITION));
	// } else if(attrId.equals(ALIGNMENT)) {
	// setAlignment(((StringAttribute) attrs.get(ALIGNMENT)).getString());
	// } else if(attrId.equals(FONT)) {
	// setFont(((StringAttribute) attrs.get(FONT)).getString());
	// } else if(attrId.equals(TEXTCOLOR)) {
	// setTextcolor((ColorAttribute) attrs.get(TEXTCOLOR));
	// } else {
	// this.add(attrs.get(it.next()));
	// }
	// }
	//
	// this.attributes = attrs;
	// } else {
	// throw new IllegalArgumentException("Invalid value type.");
	// }
	// }
	
	/**
	 * Sets the 'position'-value.
	 * 
	 * @param p
	 *           the 'position'-value to be set.
	 */
	public void setPosition(NodeLabelPositionAttribute p) {
		remove(POSITION);
		add(p, false);
	}
	
	/**
	 * Returns the NodeLabelPositionAttribute specifying the position of the
	 * encapsulated label.
	 * 
	 * @return the NodeLabelPositionAttribute specifying the position of the
	 *         encapsulated label.
	 */
	public NodeLabelPositionAttribute getPosition() {
		return (NodeLabelPositionAttribute) attributes.get(POSITION);
	}
	
	// /**
	// * Returns a deep copy of this object.
	// *
	// * @return a deep copy of this object.
	// */
	// public Object copy() {
	// NodeLabelAttribute copied = new NodeLabelAttribute(this.getId());
	// copied.setLabel(new String(this.getLabel()));
	//
	// // copied.label = new StringAttribute(LABEL, new String(this.getLabel()));
	// copied.setPosition((NodeLabelPositionAttribute) this.getPosition().copy());
	// copied.setAlignment(new String(this.getAlignment()));
	// copied.setFont(new String(this.getFont()));
	// copied.setTextcolor((ColorAttribute) this.getTextcolor().copy());
	//
	// return copied;
	// }
	
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
	// if(v instanceof Map) {
	// setCollection((Map) v);
	//
	// return;
	// }
	//
	// super.doSetValue(v);
	//
	// try {
	// this.position =
	// (NodeLabelPositionAttribute) ((NodeLabelAttribute) v).getPosition()
	// .copy();
	// } catch(ClassCastException cce) {
	// throw new IllegalArgumentException("Invalid value type.");
	// }
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

// ==============================================================================
//
// EdgeLabelAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeLabelAttribute.java,v 1.1 2011-01-31 09:04:48 klukas Exp $
package org.graffiti.graphics;

import org.graffiti.attributes.HashMapAttribute;

/**
 * DOCUMENT ME!
 * 
 * @author holleis
 * @version $Revision: 1.1 $ Extends LabelAttribute by a PositionAttribute specific for edges.
 */
public class EdgeLabelAttribute extends LabelAttribute {
	/** Position of a label within this node. */
	// private EdgeLabelPositionAttribute position;
	
	/**
	 * Constructor for EdgeLabelAttribute.
	 * 
	 * @param id
	 */
	public EdgeLabelAttribute(String id) {
		super(id);
		add(new EdgeLabelPositionAttribute(POSITION), false);
	}
	
	/**
	 * Constructor for EdgeLabelAttribute.
	 * 
	 * @param id
	 */
	public EdgeLabelAttribute(String id, String l) {
		super(id, l);
		this.add(new EdgeLabelPositionAttribute(POSITION), false);
	}
	
	//
	// /**
	// * Constructor for EdgeLabelAttribute.
	// *
	// * @param id DOCUMENT ME!
	// * @param l DOCUMENT ME!
	// * @param p DOCUMENT ME!
	// * @param a DOCUMENT ME!
	// * @param f DOCUMENT ME!
	// * @param tc DOCUMENT ME!
	// */
	// public EdgeLabelAttribute(String id, String l,
	// EdgeLabelPositionAttribute p, String a, String f, ColorAttribute tc)
	// {
	// super(id, l, p, a, f, tc);
	// this.position = new EdgeLabelPositionAttribute(POSITION);
	// this.add(this.position, false);
	// }
	//
	// /**
	// * Constructor for EdgeLabelAttribute.
	// *
	// * @param id DOCUMENT ME!
	// * @param l DOCUMENT ME!
	// * @param p DOCUMENT ME!
	// * @param a DOCUMENT ME!
	// * @param f DOCUMENT ME!
	// * @param tc DOCUMENT ME!
	// */
	// public EdgeLabelAttribute(String id, StringAttribute l,
	// EdgeLabelPositionAttribute p, StringAttribute a, StringAttribute f,
	// ColorAttribute tc)
	// {
	// super(id, l, p, a, f, tc);
	// this.position = new EdgeLabelPositionAttribute(POSITION);
	// this.add(this.position, false);
	// }
	//
	// /**
	// * Constructor for EdgeLabelAttribute.
	// *
	// * @param id DOCUMENT ME!
	// * @param l DOCUMENT ME!
	// * @param p DOCUMENT ME!
	// * @param a DOCUMENT ME!
	// * @param f DOCUMENT ME!
	// * @param tc DOCUMENT ME!
	// */
	// public EdgeLabelAttribute(String id, String l,
	// EdgeLabelPositionAttribute p, String a, String f, Color tc)
	// {
	// super(id, l, p, a, f, tc);
	// this.position = new EdgeLabelPositionAttribute(POSITION);
	// this.add(this.position, false);
	// }
	
	// /**
	// * Sets the collection of attributes contained within this <tt>CollectionAttribute</tt>
	// *
	// * @param attrs
	// * the map that contains all attributes.
	// * @throws IllegalArgumentException
	// * DOCUMENT ME!
	// */
	// @Override
	// public void setCollection(Map<String, Attribute> attrs) {
	// if (true// attrs.keySet().contains(LABEL) // &&
	// // attrs.keySet().contains(POSITION) &&
	// // attrs.keySet().contains(ALIGNMENT) &&
	// // attrs.keySet().contains(FONT) &&
	// // attrs.keySet().contains(TEXTCOLOR)
	// ) {
	// for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext();) {
	// String attrId = (String) it.next();
	//
	// if (attrId.equals(LABEL)) {
	// setLabel(((StringAttribute) attrs.get(LABEL)).getString());
	// } else
	// if (attrId.equals(POSITION)) {
	// if (!(attrs.get(POSITION) instanceof EdgeLabelPositionAttribute)) {
	// setPosition(new EdgeLabelPositionAttribute(attrs.get(POSITION).getId(), 0, 0, 0, 0));
	// } else {
	// EdgeLabelPositionAttribute elpa = (EdgeLabelPositionAttribute) attrs.get(POSITION);
	// setPosition(elpa);
	// }
	// } else
	// if (attrId.equals(ANCHOR)) {
	// setAlignment(((StringAttribute) attrs.get(ANCHOR)).getString());
	// } else
	// if (attrId.equals(FONTNAME)) {
	// setFontName(((StringAttribute) attrs.get(FONTNAME)).getString());
	// } else
	// if (attrId.equals(TEXTCOLOR)) {
	// setTextcolor(((StringAttribute) attrs.get(TEXTCOLOR)).getString());
	// } else {
	// // empty
	// }
	// }
	//
	// this.attributes = attrs;
	// }
	// // else
	// // {
	// // throw new IllegalArgumentException("Invalid value type.");
	// // }
	// }
	
	/**
	 * Sets the 'position'-value.
	 * 
	 * @param p
	 *           the 'position'-value to be set.
	 */
	public void setPosition(EdgeLabelPositionAttribute p) {
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
	public EdgeLabelPositionAttribute getPosition() {
		if (!(attributes.get(POSITION) instanceof EdgeLabelPositionAttribute)) {
			HashMapAttribute hma = (HashMapAttribute) attributes.get(POSITION);
			EdgeLabelPositionAttribute elpa = new EdgeLabelPositionAttribute(POSITION);
			if (hma != null) {
				elpa.setCollection(hma.getCollection());
				attributes.remove(attributes.get(POSITION));
			}
			attributes.put(POSITION, elpa);
		}
		return (EdgeLabelPositionAttribute) attributes.get(POSITION);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

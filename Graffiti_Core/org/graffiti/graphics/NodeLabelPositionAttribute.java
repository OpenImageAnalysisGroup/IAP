// ==============================================================================
//
// NodeLabelPositionAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeLabelPositionAttribute.java,v 1.1 2011-01-31 09:04:47 klukas Exp $

package org.graffiti.graphics;

import java.util.Iterator;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.DoubleAttribute;

/**
 * DOCUMENT ME!
 * 
 * @author holleis
 * @version $Revision: 1.1 $ Specifies position of a node label providing several parameters.
 */
public class NodeLabelPositionAttribute
					extends PositionAttribute {
	// ~ Instance fields ========================================================
	
	/**
	 * Specifies alignment of the label at the point given by relHor and
	 * relVert.
	 */
	private DoubleAttribute localAlign;
	
	/**
	 * Specifies relative horizontal position (relative to center of node) of
	 * center of label.
	 */
	private DoubleAttribute relHor;
	
	/**
	 * Specifies relative vertical position (relative to center of node) of
	 * center of label.
	 */
	private DoubleAttribute relVert;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for NodeLabelPositionAttribute.
	 * 
	 * @param id
	 */
	public NodeLabelPositionAttribute(String id) {
		this(id, 0d, 0d, 0d);
	}
	
	/**
	 * Constructor for NodeLabelPositionAttribute.
	 * 
	 * @param id
	 *           DOCUMENT ME!
	 * @param relHor
	 * @param relVert
	 * @param localAlign
	 */
	public NodeLabelPositionAttribute(String id, double relHor, double relVert,
						double localAlign) {
		this(id, new DoubleAttribute(RELHOR, relHor),
							new DoubleAttribute(RELVERT, relVert),
							new DoubleAttribute(LOCALALIGN, localAlign));
	}
	
	/**
	 * Constructor for NodeLabelPositionAttribute.
	 * 
	 * @param id
	 * @param relHor
	 * @param relVert
	 * @param localAlign
	 */
	public NodeLabelPositionAttribute(String id, DoubleAttribute relHor,
						DoubleAttribute relVert, DoubleAttribute localAlign) {
		super(id);
		this.relHor = relHor;
		this.relVert = relVert;
		this.localAlign = localAlign;
		add(this.relHor, false);
		add(this.relVert, false);
		add(this.localAlign, false);
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
		if (attrs.keySet().contains(RELHOR) && attrs.keySet().contains(RELVERT) &&
							attrs.keySet().contains(LOCALALIGN)) {
			for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext();) {
				String attrId = (String) it.next();
				
				if (attrId.equals(RELHOR)) {
					setRelHor(((DoubleAttribute) attrs.get(RELHOR)).getDouble());
				} else
					if (attrId.equals(RELVERT)) {
						setRelVert(((DoubleAttribute) attrs.get(RELVERT)).getDouble());
					} else
						if (attrId.equals(LOCALALIGN)) {
							setLocalAlign(((DoubleAttribute) attrs.get(LOCALALIGN)).getDouble());
						} else {
							this.add(attrs.get(it.next()));
						}
			}
		} else {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * Sets the localAlign.
	 * 
	 * @param localAlign
	 *           The localAlign to set
	 */
	public void setLocalAlign(double localAlign) {
		this.localAlign.setDouble(localAlign);
	}
	
	/**
	 * Returns the localAlign.
	 * 
	 * @return double
	 */
	public double getLocalAlign() {
		return this.localAlign.getDouble();
	}
	
	/**
	 * Sets the relHor.
	 * 
	 * @param relHor
	 *           The relHor to set
	 */
	public void setRelHor(double relHor) {
		this.relHor.setDouble(relHor);
	}
	
	/**
	 * Returns the relHor.
	 * 
	 * @return double
	 */
	public double getRelHor() {
		return this.relHor.getDouble();
	}
	
	/**
	 * Sets the relVert.
	 * 
	 * @param relVert
	 *           The relVert to set
	 */
	public void setRelVert(double relVert) {
		this.relVert.setDouble(relVert);
	}
	
	/**
	 * Returns the relVert.
	 * 
	 * @return double
	 */
	public double getRelVert() {
		return this.relVert.getDouble();
	}
	
	/**
	 * Returns a deep copy of this object.
	 * 
	 * @return A deep copy of this object.
	 */
	@Override
	public Object copy() {
		NodeLabelPositionAttribute copied = new NodeLabelPositionAttribute(this.getId());
		copied.setRelHor(this.getRelHor());
		copied.setRelVert(this.getRelVert());
		copied.setLocalAlign(this.getLocalAlign());
		
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
	// NodeLabelPositionAttribute tmp;
	//
	// try
	// {
	// tmp = (NodeLabelPositionAttribute) v;
	// }
	// catch(ClassCastException cce)
	// {
	// throw new IllegalArgumentException("Invalid value type.");
	// }
	//
	// this.setRelHor(tmp.getRelHor());
	// this.setRelVert(tmp.getRelVert());
	// this.setLocalAlign(tmp.getLocalAlign());
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

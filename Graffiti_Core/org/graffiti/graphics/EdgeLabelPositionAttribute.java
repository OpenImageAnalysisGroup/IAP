// ==============================================================================
//
// EdgeLabelPositionAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeLabelPositionAttribute.java,v 1.1 2011-01-31 09:04:47 klukas Exp $

package org.graffiti.graphics;

import java.util.Iterator;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.IntegerAttribute;

/**
 * DOCUMENT ME!
 * 
 * @author holleis
 * @version $Revision: 1.1 $ Specifies position of an edge label providing several parameters.
 */
public class EdgeLabelPositionAttribute
					extends PositionAttribute {
	// ~ Instance fields ========================================================
	
	/**
	 * Specifies horizontal shift (from position given by <code>relAlign</code> and <code>alignSegment</code>) of center of label.
	 */
	private DoubleAttribute absHor;
	
	/**
	 * Specifies vertical shift (from position given by <code>relAlign</code> and <code>alignSegment</code>) of center of label.
	 */
	private DoubleAttribute absVert;
	
	/**
	 * Specifies alignment of the label relative to length of edge or edge
	 * segment (whose number is given by <code>alignSegment</code>). Zero
	 * means close to source, one means close to target.
	 */
	private DoubleAttribute relAlign;
	
	/**
	 * Specifies the number of the line segment relative to which <code>relAlign</code> works. Zero means relative to whole edge.
	 */
	private IntegerAttribute alignSegment;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for NodeLabelPositionAttribute.
	 * 
	 * @param id
	 */
	public EdgeLabelPositionAttribute(String id) {
		this(id, 0.5d, 0, 0d, 0d);
	}
	
	/**
	 * Constructor for NodeLabelPositionAttribute.
	 * 
	 * @param id
	 * @param relAlign
	 * @param alignSeg
	 * @param absHor
	 * @param absVert
	 */
	public EdgeLabelPositionAttribute(String id, double relAlign, int alignSeg,
						double absHor, double absVert) {
		this(id, new DoubleAttribute(RELALIGN, relAlign),
							new IntegerAttribute(ALIGNSEGMENT, alignSeg),
							new DoubleAttribute(ABSHOR, absHor),
							new DoubleAttribute(ABSVERT, absVert));
	}
	
	/**
	 * Constructor for NodeLabelPositionAttribute.
	 * 
	 * @param id
	 * @param relAlign
	 * @param alignSeg
	 * @param absHor
	 * @param absVert
	 */
	public EdgeLabelPositionAttribute(String id, DoubleAttribute relAlign,
						IntegerAttribute alignSeg, DoubleAttribute absHor,
						DoubleAttribute absVert) {
		super(id);
		this.relAlign = relAlign;
		this.alignSegment = alignSeg;
		this.absHor = absHor;
		this.absVert = absVert;
		add(this.relAlign, false);
		add(this.alignSegment, false);
		add(this.absHor, false);
		add(this.absVert, false);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the absHor.
	 * 
	 * @param absHor
	 *           The absHor to set
	 */
	public void setAbsHor(double absHor) {
		this.absHor.setDouble(absHor);
	}
	
	/**
	 * Returns the absHor.
	 * 
	 * @return double
	 */
	public double getAbsHor() {
		return absHor.getDouble();
	}
	
	/**
	 * Sets the absVert.
	 * 
	 * @param absVert
	 *           The absVert to set
	 */
	public void setAbsVert(double absVert) {
		this.absVert.setDouble(absVert);
	}
	
	/**
	 * Returns the absVert.
	 * 
	 * @return double
	 */
	public double getAbsVert() {
		return absVert.getDouble();
	}
	
	/**
	 * Sets the alignSegment.
	 * 
	 * @param alignSegment
	 *           The alignSegment to set
	 */
	public void setAlignSegment(int alignSegment) {
		this.alignSegment.setInteger(alignSegment);
	}
	
	/**
	 * Returns the alignSegment.
	 * 
	 * @return IntegerAttribute
	 */
	public int getAlignSegment() {
		return alignSegment.getInteger();
	}
	
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
		if (attrs.keySet().contains(RELALIGN) &&
							attrs.keySet().contains(ALIGNSEGMENT) &&
							attrs.keySet().contains(ABSHOR) &&
							attrs.keySet().contains(ABSVERT)) {
			for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext();) {
				String attrId = (String) it.next();
				
				if (attrId.equals(RELALIGN)) {
					setRelAlign(((DoubleAttribute) attrs.get(RELALIGN)).getDouble());
				} else
					if (attrId.equals(ALIGNSEGMENT)) {
						setAlignSegment(((IntegerAttribute) attrs.get(ALIGNSEGMENT)).getInteger());
					} else
						if (attrId.equals(ABSHOR)) {
							setAbsHor(((DoubleAttribute) attrs.get(ABSHOR)).getDouble());
						} else
							if (attrId.equals(ABSVERT)) {
								setAbsVert(((DoubleAttribute) attrs.get(ABSVERT)).getDouble());
							} else {
								this.add(attrs.get(it.next()));
							}
			}
		} else {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * Sets the relAlign.
	 * 
	 * @param relAlign
	 *           The relAlign to set
	 */
	public void setRelAlign(double relAlign) {
		this.relAlign.setDouble(relAlign);
	}
	
	/**
	 * Returns the relAlign.
	 * 
	 * @return double
	 */
	public double getRelAlign() {
		return relAlign.getDouble();
	}
	
	/**
	 * Returns a deep copy of this object.
	 * 
	 * @return A deep copy of this object.
	 */
	@Override
	public Object copy() {
		EdgeLabelPositionAttribute copied = new EdgeLabelPositionAttribute(this.getId());
		copied.setRelAlign(this.getRelAlign());
		copied.setAlignSegment(this.getAlignSegment());
		copied.setAbsHor(this.getAbsHor());
		copied.setAbsVert(this.getAbsVert());
		
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
	// EdgeLabelPositionAttribute tmp;
	//
	// try
	// {
	// tmp = (EdgeLabelPositionAttribute) v;
	// }
	// catch(ClassCastException cce)
	// {
	// throw new IllegalArgumentException("Invalid value type.");
	// }
	//
	// this.setRelAlign(tmp.getRelAlign());
	// this.setAlignSegment(tmp.getAlignSegment());
	// this.setAbsHor(tmp.getAbsHor());
	// this.setAbsVert(tmp.getAbsVert());
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

// ==============================================================================
//
// EdgeGraphicAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeGraphicAttribute.java,v 1.1 2011-01-31 09:04:47 klukas Exp $

package org.graffiti.graphics;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.EdgeShapeAttribute;
import org.graffiti.attributes.IllegalIdException;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.attributes.SortedCollectionAttribute;
import org.graffiti.attributes.StringAttribute;

/**
 * Defines all graphic attributes of an edge
 * 
 * @author breu
 * @version $Revision: 1.1 $
 */
public class EdgeGraphicAttribute
					extends GraphElementGraphicAttribute {
	// ~ Instance fields ========================================================
	
	// /** Object for docking specification of an edge at source and target nodes */
	// private DockingAttribute docking;
	//
	// /** The thickness of the edge without edge frame */
	// private DoubleAttribute thickness;
	//
	// /**
	// * Collection of all <code>CoordinateAttribute</code>s for the bends for
	// * the edge.
	// */
	// private SortedCollectionAttribute bends;
	//
	// /** The class name used to generate an arrow at the target node. */
	// private StringAttribute arrowhead;
	//
	// /** The class name used to generate an arrow at the source node. */
	// private StringAttribute arrowtail;
	//
	// /** Specifies the type of the line (e.g. polyline or spline types). */
	// private StringAttribute lineType;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs an EdgeGraphicAttribute and initializes all its members.
	 * 
	 * @param ah
	 *           the arrowhead-value of the attribute.
	 * @param at
	 *           the arrowtail-value of the attribute.
	 * @param t
	 *           the thickness-value of the attribute.
	 * @param d
	 *           the docking-value of the attribute.
	 * @param b
	 *           the <code>CollectionAttriubte</code> containing the bends.
	 * @param lt
	 *           the lineType of the edge.
	 */
	public EdgeGraphicAttribute(String ah, String at, double t,
						DockingAttribute d, LinkedHashMapAttribute b, String lt) {
		super(GRAPHICS);
		add(StringAttribute.getTypedStringAttribute(ARROWHEAD, ah), false);
		add(StringAttribute.getTypedStringAttribute(ARROWTAIL, at), false);
		add(new ThicknessAttribute(THICKNESS, t), false);
		add((DockingAttribute) d.copy(), false);
		add((SortedCollectionAttribute) b.copy(), false);
		add(StringAttribute.getTypedStringAttribute(LINETYPE, lt), false);
		add(new EdgeShapeAttribute(SHAPE, "org.graffiti.plugins.views.defaults.StraightLineEdgeShape"), false);
	}
	
	/**
	 * Constructs an EdgeGraphicAttribute and initializes all its members.
	 * 
	 * @throws IllegalIdException
	 */
	public EdgeGraphicAttribute()
						throws IllegalIdException {
		this("", "", 1d, new DockingAttribute(DOCKING),
							new LinkedHashMapAttribute(BENDS), "");
	}
	
	/**
	 * Constructs an EdgeGraphicAttribute and initializes all its members.
	 * 
	 * @param ah
	 *           the arrowhead-value of the attribute.
	 * @param at
	 *           the arrowtail-value of the attribute.
	 * @param t
	 *           the thickness-value of the attribute.
	 * @param d
	 *           the docking-value of the attribute.
	 * @throws IllegalIdException
	 */
	public EdgeGraphicAttribute(String ah, String at, double t,
						DockingAttribute d)
						throws IllegalIdException {
		this(ah, at, t, d, new LinkedHashMapAttribute(BENDS), "");
	}
	
	/**
	 * Constructs an EdgeGraphicAttribute and initializes all its members.
	 * 
	 * @param ah
	 *           the arrowhead-value of the attribute.
	 * @param at
	 *           the arrowtail-value of the attribute.
	 * @param t
	 *           the thickness-value of the attribute.
	 * @param d
	 *           the docking-value of the attribute.
	 * @throws IllegalIdException
	 */
	public EdgeGraphicAttribute(String ah, String at, DoubleAttribute t,
						DockingAttribute d)
						throws IllegalIdException {
		this(ah, at, t.getDouble(), d, new LinkedHashMapAttribute(BENDS), "");
	}
	
	/**
	 * Constructs an EdgeGraphicAttribute and initializes all its members.
	 * 
	 * @param ah
	 *           the arrowhead-value of the attribute.
	 * @param at
	 *           the arrowtail-value of the attribute.
	 * @param t
	 *           the thickness-value of the attribute.
	 * @param d
	 *           the docking-value of the attribute.
	 * @throws IllegalIdException
	 */
	public EdgeGraphicAttribute(StringAttribute ah, StringAttribute at,
						double t, DockingAttribute d)
						throws IllegalIdException {
		this(ah.getString(), at.getString(), t, d,
							new LinkedHashMapAttribute(BENDS), "");
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the 'arrowhead'-value.
	 * 
	 * @param ah
	 *           the 'arrowhead'-value to be set.
	 */
	public void setArrowhead(String ah) {
		StringAttribute arrowhead = (StringAttribute) attributes.get(ARROWHEAD);
		arrowhead.setString(ah);
	}
	
	/**
	 * Returns the 'arrowhead'-value of the encapsulated edge.
	 * 
	 * @return the 'arrowhead'-value of the encapsulated edge.
	 */
	public String getArrowhead() {
		return ((StringAttribute) attributes.get(ARROWHEAD)).getString();
	}
	
	/**
	 * Sets the 'arrowtail'-value.
	 * 
	 * @param at
	 *           the 'arrowtail'-value to be set.
	 */
	public void setArrowtail(String at) {
		((StringAttribute) attributes.get(ARROWTAIL)).setString(at);
	}
	
	/**
	 * Returns the 'arrowtail'-value of the encapsulated edge.
	 * 
	 * @return the 'arrowtail'-value of the encapsulated edge.
	 */
	public String getArrowtail() {
		return ((StringAttribute) attributes.get(ARROWTAIL)).getString();
	}
	
	/**
	 * Sets the 'bends'-value.
	 * 
	 * @param b
	 *           the 'bends'-value to be set.
	 */
	public void setBends(SortedCollectionAttribute b) {
		SortedCollectionAttribute bends = (SortedCollectionAttribute) attributes.get(BENDS);
		bends.setCollection(b.getCollection());
	}
	
	/**
	 * Returns the collection of <code>CoordinateAttribute</code>s specifying
	 * the bends for this edge.
	 * 
	 * @return the collection of <code>CoordinateAttribute</code>s specifying
	 *         the bends for this edge.
	 */
	public SortedCollectionAttribute getBends() {
		return (SortedCollectionAttribute) attributes.get(BENDS);
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
		if (attrs.keySet().contains(ARROWHEAD) &&
							attrs.keySet().contains(ARROWTAIL) &&
							attrs.keySet().contains(BENDS) && attrs.keySet().contains(DOCKING) &&
							attrs.keySet().contains(THICKNESS) &&
							attrs.keySet().contains(LINETYPE) &&
							attrs.keySet().contains(BGIMAGE) &&
							attrs.keySet().contains(FRAMECOLOR) &&
							attrs.keySet().contains(FILLCOLOR) &&
							attrs.keySet().contains(FRAMETHICKNESS) &&
							attrs.keySet().contains(LINEMODE) &&
							attrs.keySet().contains(SHAPE)) {
			for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext();) {
				String attrId = it.next();
				
				if (attrId.equals(ARROWHEAD)) {
					setArrowhead(((StringAttribute) attrs.get(ARROWHEAD)).getString());
				} else
					if (attrId.equals(ARROWTAIL)) {
						setArrowtail(((StringAttribute) attrs.get(ARROWTAIL)).getString());
					} else
						if (attrId.equals(BENDS)) {
							setBends((SortedCollectionAttribute) attrs.get(BENDS));
						} else
							if (attrId.equals(DOCKING)) {
								setDocking((DockingAttribute) attrs.get(DOCKING));
							} else
								if (attrId.equals(THICKNESS)) {
									setThickness(((DoubleAttribute) attrs.get(THICKNESS)).getDouble());
								} else
									if (attrId.equals(LINETYPE)) {
										setLineType(((StringAttribute) attrs.get(LINETYPE)).getString());
									} else
										if (attrId.equals(BGIMAGE)) {
											setBackgroundImage((ImageAttribute) attrs.get(BGIMAGE));
										} else
											if (attrId.equals(FRAMECOLOR)) {
												setFramecolor((ColorAttribute) attrs.get(FRAMECOLOR));
											} else
												if (attrId.equals(FILLCOLOR)) {
													setFillcolor((ColorAttribute) attrs.get(FILLCOLOR));
												} else
													if (attrId.equals(FRAMETHICKNESS)) {
														setFrameThickness(((DoubleAttribute) attrs.get(
																			FRAMETHICKNESS)).getDouble());
													} else
														if (attrId.equals(LINEMODE)) {
															setLineMode((LineModeAttribute) attrs.get(LINEMODE));
														} else
															if (attrId.equals(SHAPE)) {
																setShape(((StringAttribute) attrs.get(SHAPE)).getString());
															} else {
																this.add(attrs.get(it.next()));
															}
			}
		} else {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * Sets the 'docking'-value.
	 * 
	 * @param d
	 *           the 'docking'-value to be set.
	 */
	public void setDocking(DockingAttribute d) {
		remove(DOCKING);
		add(d);
	}
	
	/**
	 * Returns the 'docking'-value of the encapsulated edge.
	 * 
	 * @return the 'docking'-value of the encapsulated edge.
	 */
	public DockingAttribute getDocking() {
		return (DockingAttribute) attributes.get(DOCKING);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param l
	 */
	public void setLineType(String l) {
		((StringAttribute) attributes.get(LINETYPE)).setString(l);
	}
	
	/**
	 * Returns the line type.
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getLineType() {
		return ((StringAttribute) attributes.get(LINETYPE)).getString();
	}
	
	/**
	 * Sets the 'thickness'-value.
	 * 
	 * @param t
	 *           the 'thickness'-value of the encapsulated edge.
	 */
	public void setThickness(double t) {
		((ThicknessAttribute) attributes.get(THICKNESS)).setDouble(t);
	}
	
	/**
	 * Returns the 'thickness'-value of the encapsulated edge.
	 * 
	 * @return the 'thickness'-value of the encapsulated edge.
	 */
	public double getThickness() {
		return ((ThicknessAttribute) attributes.get(THICKNESS)).getDouble();
	}
	
	/**
	 * Returns a deep copy of this object.
	 * 
	 * @return A deep copy of this object.
	 */
	@Override
	public Object copy() {
		EdgeGraphicAttribute copied = new EdgeGraphicAttribute();
		
		// copy ALL of the subattributes
		for (Iterator<?> iter = attributes.values().iterator(); iter.hasNext();) {
			Attribute attr = (Attribute) iter.next();
			Attribute copAttr = (Attribute) attr.copy();
			
			try {
				Attribute exAttr = copied.getAttribute(copAttr.getId());
				exAttr.setValue(copAttr.getValue());
			} catch (AttributeNotFoundException anfe) {
				copied.add(copAttr, false);
			}
		}
		
		// // first setting the subattributes defined in
		// // GraphElementGrapichAttribute
		// copied.setBackgroundImage((ImageAttribute) this.getBackgroundImage()
		// .copy());
		// copied.setFramecolor((ColorAttribute) this.getFramecolor().copy());
		// copied.setFillcolor((ColorAttribute) this.getFillcolor().copy());
		// copied.setFrameThickness(this.getFrameThickness());
		// copied.setLineMode((LineModeAttribute) this.getLineMode().copy());
		// copied.setShape(this.getShape());
		//
		// copied.setArrowhead(this.getArrowhead());
		// copied.setArrowtail(this.getArrowtail());
		// copied.setBends((SortedCollectionAttribute) this.getBends().copy());
		// copied.setDocking((DockingAttribute) this.getDocking().copy());
		// copied.setLineType(this.getLineType());
		// copied.setThickness(this.getThickness());
		return copied;
	}
	
	public Collection<Attribute> getBendAttributes() {
		LinkedHashMapAttribute bends = (LinkedHashMapAttribute) attributes.get(BENDS);
		return bends.getCollectionNoClone().values();
	}
	
	public int getBendCount() {
		return ((LinkedHashMapAttribute) attributes.get(BENDS)).getCollectionNoClone().size();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

// ==============================================================================
//
// GraphElementGraphicAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphElementGraphicAttribute.java,v 1.1 2011-01-31 09:04:48 klukas Exp $

package org.graffiti.graphics;

import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.IllegalIdException;
import org.graffiti.attributes.StringAttribute;

/**
 * Defines the common graphic attributes for nodes and edges
 * 
 * @version $Revision: 1.1 $
 */
public abstract class GraphElementGraphicAttribute
					extends HashMapAttribute
					implements GraphicAttributeConstants {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for GraphElementGraphicAttribute.
	 * 
	 * @param id
	 * @throws IllegalIdException
	 */
	public GraphElementGraphicAttribute(String id)
						throws IllegalIdException {
		super(id);
		// add(new ImageAttribute(BGIMAGE), false);
		add(new ColorAttribute(FRAMECOLOR, java.awt.Color.BLACK), false);
		add(new ColorAttribute(FILLCOLOR), false);
		add(new DoubleAttribute(FRAMETHICKNESS, 2), false);
		add(new DoubleAttribute(ROUNDING, 5), false);
		add(new GradientFillAttribute(GRADIENT, 0), false);
		add(new LineModeAttribute(LINEMODE), false);
	}
	
	/**
	 * Constructor for GraphElementGraphicAttribute.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param i
	 *           the backgroundimage-value of the attriubte
	 * @param frc
	 *           the framecolor-value of the attribute.
	 * @param fic
	 *           the fillcolor-value of the attribute.
	 * @param l
	 *           DOCUMENT ME!
	 * @param ft
	 *           the framethickness-value of the attribute.
	 * @param lm
	 *           the linemode-value of the attribute.
	 * @param s
	 *           DOCUMENT ME!
	 * @throws IllegalIdException
	 */
	public GraphElementGraphicAttribute(String id, ImageAttribute i,
						ColorAttribute frc, ColorAttribute fic, LabelAttribute l,
						DoubleAttribute ft, LineModeAttribute lm, StringAttribute s)
						throws IllegalIdException {
		super(id);
		// add(new ImageAttribute(BGIMAGE, i.getTiled(), i.getMaximize(), i.getImage(), i.getReference()), false);
		add(new ColorAttribute(FRAMECOLOR, frc), false);
		add(new ColorAttribute(FILLCOLOR, fic), false);
		add(new DoubleAttribute(FRAMETHICKNESS, ft.getDouble()), false);
		add(new DoubleAttribute(FRAMETHICKNESS, 5), false);
		add(new LineModeAttribute(LINEMODE, (Dash) lm.getValue()), false);
		add(new GradientFillAttribute(GRADIENT, 0), false);
	}
	
	/**
	 * Constructor for GraphElementGraphicAttribute.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param i
	 *           the backgroundimage-value of the attriubte
	 * @param frc
	 *           the framecolor-value of the attribute.
	 * @param fic
	 *           the fillcolor-value of the attribute.
	 * @param l
	 *           DOCUMENT ME!
	 * @param ft
	 *           the framethickness-value of the attribute.
	 * @param lm
	 *           the linemode-value of the attribute.
	 * @param s
	 *           DOCUMENT ME!
	 * @throws IllegalIdException
	 */
	public GraphElementGraphicAttribute(String id, java.awt.Image i,
						java.awt.Color frc, java.awt.Color fic, LabelAttribute l, double ft,
						LineModeAttribute lm, String s)
						throws IllegalIdException {
		super(id);
		// add(new ImageAttribute(BGIMAGE, false, false, i, ""), false);
		add(new ColorAttribute(FRAMECOLOR, frc), false);
		add(new ColorAttribute(FILLCOLOR, fic), false);
		add(new DoubleAttribute(FRAMETHICKNESS, ft), false);
		add(new DoubleAttribute(FRAMETHICKNESS, 5), false);
		add(new LineModeAttribute(LINEMODE, (Dash) lm.getValue()), false);
		add(new GradientFillAttribute(GRADIENT, 0), false);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the 'backgroundimage'-value.
	 * 
	 * @param bgi
	 *           the 'backgroundimage'-value to be set.
	 */
	public void setBackgroundImage(ImageAttribute bgi) {
		remove(IMAGE);
		add(bgi, false);
	}
	
	/**
	 * Returns the 'backgroundimage'-value of the encapsulated edge.
	 * 
	 * @return the 'backgroundimage'-value of the encapsulated edge.
	 */
	public ImageAttribute getBackgroundImage() {
		return (ImageAttribute) attributes.get(IMAGE);
	}
	
	/**
	 * Sets the 'fillcolor'-value.
	 * 
	 * @param fic
	 *           the 'fillcolor'-value to be set.
	 */
	public void setFillcolor(ColorAttribute fic) {
		remove(FILLCOLOR);
		add(fic, false);
	}
	
	/**
	 * Returns the 'fillcolor'-value of the encapsulated edge.
	 * 
	 * @return the 'fillcolor'-value of the encapsulated edge.
	 */
	public ColorAttribute getFillcolor() {
		return (ColorAttribute) attributes.get(FILLCOLOR);
	}
	
	/**
	 * Sets the 'frameThickness'-value.
	 * 
	 * @param ft
	 *           the 'frameThickness'-value to be set.
	 */
	public void setFrameThickness(double ft) {
		((DoubleAttribute) attributes.get(FRAMETHICKNESS)).setDouble(ft);
	}
	
	public void setRoundedEdges(double rd) {
		((DoubleAttribute) attributes.get(ROUNDING)).setDouble(rd);
	}
	
	public void setUseGradient(double rd) {
		((DoubleAttribute) attributes.get(GRADIENT)).setDouble(rd);
	}
	
	/**
	 * Returns the 'frameThickness'-value of the encapsulated edge.
	 * 
	 * @return the 'frameThickness'-value of the encapsulated edge.
	 */
	public double getFrameThickness() {
		return ((DoubleAttribute) attributes.get(FRAMETHICKNESS)).getDouble();
	}
	
	/**
	 * Returns the rounding of the edges.
	 * 
	 * @author klukas
	 * @return The rounding value of the edges
	 */
	public double getRoundedEdges() {
		return ((DoubleAttribute) attributes.get(ROUNDING)).getDouble();
	}
	
	public double getUseGradient() {
		return ((GradientFillAttribute) attributes.get(GRADIENT)).getDouble();
	}
	
	/**
	 * Sets the 'framecolor'-value.
	 * 
	 * @param frc
	 *           the 'framecolor'-valueto be set.
	 */
	public void setFramecolor(ColorAttribute frc) {
		remove(FRAMECOLOR);
		add(frc, false);
	}
	
	/**
	 * Returns the 'framecolor'-value of the encapsulated edge.
	 * 
	 * @return the 'framecolor'-value of the encapsulated edge.
	 */
	public ColorAttribute getFramecolor() {
		return (ColorAttribute) attributes.get(FRAMECOLOR);
	}
	
	/**
	 * Sets the 'lineMode'-value.
	 * 
	 * @param lma
	 *           the 'lineMode'-value to be set.
	 */
	public void setLineMode(LineModeAttribute lma) {
		if (attributes.containsKey(LINEMODE))
			attributes.remove(LINEMODE);
		attributes.put(LINEMODE, lma);
		lma.setParent(this);
	}
	
	/**
	 * Returns the 'lineMode'-value of the encapsulated edge.
	 * 
	 * @return the 'lineMode'-value of the encapsulated edge.
	 */
	public LineModeAttribute getLineMode() {
		return (LineModeAttribute) attributes.get(LINEMODE);
	}
	
	/**
	 * Sets the 'shape'-value.
	 * 
	 * @param sn
	 *           the 'shape'-value to be set.
	 */
	public void setShape(String sn) {
		((StringAttribute) attributes.get(SHAPE)).setString(sn);
	}
	
	/**
	 * Returns the 'shape'-value of the encapsulated edge.
	 * 
	 * @return the 'shape'-value of the encapsulated edge.
	 */
	public String getShape() {
		return ((StringAttribute) attributes.get(SHAPE)).getString();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

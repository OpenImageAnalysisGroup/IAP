/*
 * $Id: CubicBezier.java,v 1.1 2012-11-07 14:43:36 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/extensions/layout/src/org/sbml/jsbml/ext/layout/CubicBezier.java $
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2012 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */
package org.sbml.jsbml.ext.layout;

import javax.swing.tree.TreeNode;

/**
 * @author Nicolas Rodriguez
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev: 1116 $
 */
public class CubicBezier extends LineSegment {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -6747242964512145076L;
	
	/**
	 * 
	 */
	private Point basePoint1;
	
	/**
	 * 
	 */
	private Point basePoint2;
	
	/**
	 * 
	 */
	public CubicBezier() {
		
	}
	
	/**
	 * @param cubicBezier
	 */
	public CubicBezier(CubicBezier cubicBezier) {
		super(cubicBezier);
		if (cubicBezier.isSetBasePoint1()) {
			basePoint1 = cubicBezier.getBasePoint1();
		}
		if (cubicBezier.isSetBasePoint2()) {
			basePoint2 = cubicBezier.getBasePoint2();
		}
	}

	/**
	 * 
	 * @param level
	 * @param version
	 */
	public CubicBezier(int level, int version) {
		super(level, version);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.ext.layout.LineSegment#clone()
	 */
	@Override
	public CubicBezier clone() {
		return new CubicBezier(this);
	}
	
	/**
	 * 
	 * @return
	 */
	public Point getBasePoint1() {
		return basePoint1;
	}
	
	/**
	 * 
	 * @return
	 */
	public Point getBasePoint2() {
		return basePoint2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.ext.layout.LineSegment#getChildAt(int)
	 */
	@Override
	public TreeNode getChildAt(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException(Integer.toString(index));
		}
		int count = super.getChildCount(), pos = 0;
		if (index < count) {
			return super.getChildAt(index);
		} else {
			index -= count;
		}
		if (isSetBasePoint1()) {
			if (pos == index) {
				return getBasePoint1();
			}
			pos++;
		}
		if (isSetBasePoint2()) {
			if (pos == index) {
				return getBasePoint2();
			}
			pos++;
		}
		throw new IndexOutOfBoundsException(String.format("Index %d >= %d",
				index, +((int) Math.min(pos, 0))));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.ext.layout.LineSegment#getChildCount()
	 */
	@Override
	public int getChildCount() {
		int count = super.getChildCount();
		if (isSetBasePoint1()) {
			count++;
		}
		if (isSetBasePoint2()) {
			count++;
		}
		return count;
	}

	/**
	 * @return
	 */
	public boolean isSetBasePoint1() {
		return basePoint1 != null;
	}

	/**
	 * @return
	 */
	public boolean isSetBasePoint2() {
		return basePoint2 != null;
	}
	
	/**
	 * 
	 * @param basePoint1
	 */
	public void setBasePoint1(Point basePoint1) {
		if(this.basePoint1 != null){
			Point oldValue = this.basePoint1;
			this.basePoint1 = null;
			oldValue.fireNodeRemovedEvent();
		}
		if (!(basePoint1 instanceof BasePoint1)) {
			basePoint1 = new BasePoint1(basePoint1);
		}
		this.basePoint1 = basePoint1;
		registerChild(this.basePoint1);
	}
	
	/**
	 * 
	 * @param basePoint2
	 */
	public void setBasePoint2(Point basePoint2) {
		if(this.basePoint2 != null){
			Point oldValue = this.basePoint2;
			this.basePoint2 = null;
			oldValue.fireNodeRemovedEvent();
		}
		if (!(basePoint2 instanceof BasePoint1)) {
			basePoint2 = new BasePoint2(basePoint2);
		}
		this.basePoint2 = basePoint2;
		registerChild(this.basePoint2);
	}

	/**
	 * Creates, sets and returns a {@link Point}
	 *
	 * @return new {@link Point} object.
	 */
	public Point createBasePoint1() {
		Point p = new BasePoint1();
		setBasePoint1(p);
		return p;
	}
	
	/**
	 * Creates, sets and returns a {@link Point} based on the
	 * given values.
	 * @param x
	 * @param y
	 * @param z
	 * @return new {@link Point} object.
	 */
	public Point createBasePoint1(double x, double y, double z) {
		Point p = new BasePoint1();
		p.setX(x);
		p.setY(y);
		p.setZ(z);
		setBasePoint1(p);
		return p;
	}

	/**
	 * Creates, sets and returns a {@link Point}
	 *
	 * @return new {@link Point} object.
	 */
	public Point createBasePoint2() {
		Point p = new BasePoint2();
		setBasePoint2(p);
		return p;
	}
	
	/**
	 * Creates, sets and returns a {@link Point} based on the
	 * given values.
	 * @param x
	 * @param y
	 * @param z
	 * @return new {@link Point} object.
	 */
	public Point createBasePoint2(double x, double y, double z) {
		Point p = new BasePoint2();
		p.setX(x);
		p.setY(y);
		p.setZ(z);
		setBasePoint2(p);
		return p;
	}

	
}

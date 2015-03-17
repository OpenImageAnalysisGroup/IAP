/*
 * $Id: Curve.java,v 1.1 2012-11-07 14:43:36 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/extensions/layout/src/org/sbml/jsbml/ext/layout/Curve.java $
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

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.util.TreeNodeChangeListener;

/**
 * 
 * 
 * @author Nicolas Rodriguez
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * @version $Rev: 1116 $
 */
public class Curve extends AbstractNamedSBase {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5435135643993920570L;
	/**
	 * 
	 */
	ListOf<CurveSegment> listOfCurveSegments = new ListOf<CurveSegment>();

	/**
	 * @param curve
	 * 
	 */
	public Curve(Curve curve) {
		super(curve);
		if (curve.isSetListOfCurveSegments()) {
			this.listOfCurveSegments = curve.getListOfCurveSegments().clone();
		}
	}

	/**
	 * 
	 */
	public Curve() {
		super();
		addNamespace(LayoutConstant.namespaceURI);
		
		listOfCurveSegments.addNamespace(LayoutConstant.namespaceURI);
		listOfCurveSegments.setSBaseListType(ListOf.Type.other);
		registerChild(listOfCurveSegments);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.AbstractSBase#clone()
	 */
	@Override
	public Curve clone() {
		return new Curve(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractSBase#getChildAt(int)
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
		if (isSetListOfCurveSegments()) {
			if (pos == index) {
				return getListOfCurveSegments();
			}
			pos++;
		}
		throw new IndexOutOfBoundsException(String.format("Index %d >= %d",
				index, +((int) Math.min(pos, 0))));
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractSBase#getChildCount()
	 */
	@Override
	public int getChildCount() {
		int count = super.getChildCount();
		if (isSetListOfCurveSegments()) {
			count++;
		}
		return count;
	}

	/**
	 * 
	 * @return
	 */
	public ListOf<CurveSegment> getListOfCurveSegments() {
		return listOfCurveSegments;
	}
	
	/* (non-Javadoc)
   * @see org.sbml.jsbml.NamedSBase#isIdMandatory()
   */
  public boolean isIdMandatory() {
    return false;
  }
	
	/**
	 * 
	 * @return
	 */
	public boolean isSetListOfCurveSegments() {
		return (listOfCurveSegments != null) && (listOfCurveSegments.size() > 0);
	}
	
	/**
	 * 
	 * @param listOfCurveSegments
	 */
	public void setListOfCurveSegments(ListOf<CurveSegment> listOfCurveSegments) {
		unsetListOfCurveSegments();
		this.listOfCurveSegments = listOfCurveSegments;
		if ((this.listOfCurveSegments != null) && (this.listOfCurveSegments.getSBaseListType() != ListOf.Type.other)) {
			this.listOfCurveSegments.setSBaseListType(ListOf.Type.other);
		}
		registerChild(this.listOfCurveSegments);
	}

  /**
	 * Removes the {@link #listOfLineSegments} from this {@link Model} and notifies
	 * all registered instances of {@link TreeNodeChangeListener}.
	 * 
	 * @return <code>true</code> if calling this method lead to a change in this
	 *         data structure.
	 */
	public boolean unsetListOfCurveSegments() {
		if (this.listOfCurveSegments != null) {
			ListOf<CurveSegment> oldListOfCurveSegments = this.listOfCurveSegments;
			this.listOfCurveSegments = null;
			oldListOfCurveSegments.fireNodeRemovedEvent();
			return true;
		}
		return false;
	}
}

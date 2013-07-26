/*
 * $Id: ExtendedLayoutModel.java,v 1.1 2012-11-07 14:43:36 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/extensions/layout/src/org/sbml/jsbml/ext/layout/ExtendedLayoutModel.java $
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 * Copyright (C) 2009-2012 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */
package org.sbml.jsbml.ext.layout;

import java.util.Map;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.AbstractSBasePlugin;
import org.sbml.jsbml.util.TreeNodeChangeListener;

/**
 * @author Nicolas Rodriguez
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @since 1.0
 * @version $Rev: 1116 $
 */
public class ExtendedLayoutModel extends AbstractSBasePlugin {
	
	// TODO : need to be adapted to the new way of dealing with L3 packages
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -6666014348571697514L;
	/**
	 * 
	 */
	protected ListOf<Layout> listOfLayouts;
	
	/**
	 * 
	 */
	private Model model;
	
	/**
	 * 
	 */
	public ExtendedLayoutModel(Model model) {
		super(model);
		
		this.model = model;
		createListOfLayout();
	}
	
	/**
	 * Creates a new list of layout
	 */
	private void createListOfLayout() {
		listOfLayouts = new ListOf<Layout>();
		listOfLayouts.addNamespace(LayoutConstant.namespaceURI);
		listOfLayouts.setSBaseListType(ListOf.Type.other);
		model.registerChild(listOfLayouts);
	}
	
	/**
	 * @param elm
	 */
	public ExtendedLayoutModel(ExtendedLayoutModel elm) {
		// We don't clone the pointer to the containing model.
		if (elm.listOfLayouts != null) {
			this.listOfLayouts = elm.listOfLayouts.clone();
		}
	}
	
	public ListOf<SpeciesGlyph> getAllSpeciesGlyphById(String speciesID) {
		ListOf<SpeciesGlyph> speicesGlyphs = new ListOf<SpeciesGlyph>(model.getLevel(), model.getVersion());
		for (Layout layout : listOfLayouts) {
			SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(speciesID);
			if (speciesGlyph != null) {
				speicesGlyphs.add(speciesGlyph);
			}
		}
		return speicesGlyphs;
	}
	
	public ListOf<ReactionGlyph> getAllReactionGlyphById(String reactionID) {
		ListOf<ReactionGlyph> reactionGlyphs = new ListOf<ReactionGlyph>(model.getLevel(), model.getVersion());
		for (Layout layout : listOfLayouts) {
			ReactionGlyph reactionGlyph = layout.getReactionGlyph(reactionID);
			if (reactionGlyph != null) {
				reactionGlyphs.add(reactionGlyph);
			}
		}
		return reactionGlyphs;
	}
	
	/**
	 * @param layout
	 */
	public void add(Layout layout) {
		addLayout(layout);
	}
	
	/**
	 * @param layout
	 */
	public void addLayout(Layout layout) {
		if (layout != null) {
			getListOfLayouts().add(layout);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.Model#clone()
	 */
	@Override
	public ExtendedLayoutModel clone() {
		return new ExtendedLayoutModel(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.Model#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		boolean equals = super.equals(object);
		if (equals) {
			// ExtendedLayoutModel elm = (ExtendedLayoutModel) object;
			// An equals call on the model would cause a cyclic check!
			// Actually, I'm not sure if we should compare the model
			// here at all because this would be like checking a pointer
			// to the parent node in the SBML tree, which we never do.
			// Therefore, there's also no hashCode method here, because
			// nothing to check, in my opinion.
			// Hence, we can delete this method here.
			// equals &= getModel() == elm.getModel();
		}
		return equals;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getAllowsChildren()
	 */
	public boolean getAllowsChildren() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.ext.SBasePlugin#getChildAt(int)
	 */
	public SBase getChildAt(int index) {
		if (isSetListOfLayouts() && (index == getChildCount() - 1)) {
			return getListOfLayouts();
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.ext.SBasePlugin#getChildCount()
	 */
	public int getChildCount() {
		int count = 0;
		if (isSetListOfLayouts()) {
			count++;
		}
		return count;
	}
	
	/**
	 * @param i
	 * @return
	 */
	public Layout getLayout(int i) {
		return listOfLayouts.get(i);
	}
	
	/**
	 * @return
	 */
	public ListOf<Layout> getListOfLayouts() {
		if (listOfLayouts == null) {
			createListOfLayout();
		}
		return listOfLayouts;
	}
	
	public Model getParent() {
		return model;
	}
	
	public Model getParentSBMLObject() {
		return model;
	}
	
	/**
	 * @return
	 */
	public boolean isSetListOfLayouts() {
		return ((listOfLayouts == null) || listOfLayouts.isEmpty()) ? false
				: true;
	}
	
	public boolean readAttribute(String attributeName, String prefix,
			String value) {
		return false;
	}
	
	/**
	 * @param listOfLayouts
	 */
	public void setListOfLayouts(ListOf<Layout> listOfLayouts) {
		unsetListOfLayouts();
		if (listOfLayouts == null) {
			this.listOfLayouts = new ListOf<Layout>();
		} else {
			this.listOfLayouts = listOfLayouts;
		}
		if ((this.listOfLayouts != null) && (this.listOfLayouts.getSBaseListType() != ListOf.Type.other)) {
			this.listOfLayouts.setSBaseListType(ListOf.Type.other);
		}
		model.registerChild(listOfLayouts);
	}
	
	/**
	 * @param layoutId
	 * @return
	 */
	public Layout getLayout(String layoutID) {
		if (isSetListOfLayouts()) {
			for (Layout layout : listOfLayouts) {
				if (layout.getId().equals(layout)) {
					return layout;
				}
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Removes the {@link #listOfLayouts} from this {@link Model} and notifies
	 * all registered instances of {@link TreeNodeChangeListener}.
	 * 
	 * @return <code>true</code> if calling this method lead to a change in this
	 *         data structure.
	 */
	public boolean unsetListOfLayouts() {
		if (this.listOfLayouts != null) {
			ListOf<Layout> oldListOfLayouts = this.listOfLayouts;
			this.listOfLayouts = null;
			oldListOfLayouts.fireNodeRemovedEvent();
			return true;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.ext.SBasePlugin#writeXMLAttributes()
	 */
	public Map<String, String> writeXMLAttributes() {
		return null;
	}
	
	/**
	 * @return
	 */
	public Model getModel() {
		return (Model) extendedSBase;
	}
	
	/**
	 * Creates a new layout and adds it to the current list of layouts.
	 * 
	 * @return new layout.
	 */
	public Layout createLayout() {
		Layout layout = new Layout(model.getLevel(), model.getVersion());
		addLayout(layout);
		return layout;
	}
	
}

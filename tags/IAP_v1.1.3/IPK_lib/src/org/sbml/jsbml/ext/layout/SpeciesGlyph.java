/*
 * $Id: SpeciesGlyph.java,v 1.1 2012-11-07 14:43:36 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/extensions/layout/src/org/sbml/jsbml/ext/layout/SpeciesGlyph.java $
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

import java.util.Map;

import org.sbml.jsbml.util.TreeNodeChangeEvent;

/**
 * @author Nicolas Rodriguez
 * @author Sebastian Fr&ouml;lich
 * @author Andreas Dr&auml;ger
 * @author Clemens Wrzodek
 * @since 1.0
 * @version $Rev: 1241 $
 */
public class SpeciesGlyph extends GraphicalObject {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 1077785483575936434L;
	
	/**
	 * Reference to species id, described by this {@link SpeciesGlyph}.
	 */
	private String speciesId;
	
	/**
	 * 
	 */
	public SpeciesGlyph() {
		addNamespace(LayoutConstant.namespaceURI);
	}	
	
	/**
	 * 
	 * @param speciesGlyph
	 */
	public SpeciesGlyph(SpeciesGlyph speciesGlyph) {
		super(speciesGlyph);

		if (speciesGlyph.isSetSpecies()) {
			this.speciesId = new String(speciesGlyph.getSpecies());
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.ext.layout.GraphicalObject#clone()
	 */
	@Override
	public SpeciesGlyph clone() {
		return new SpeciesGlyph(this);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractNamedSBase#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		boolean equals = super.equals(object);
		if (equals) {
			SpeciesGlyph s = (SpeciesGlyph) object;

			 equals &= s.isSetSpecies() == isSetSpecies();
			 if (equals && isSetSpecies()) {
			 equals &= s.getSpecies().equals(getSpecies());
			 }
		}
		return equals;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getSpecies() {
		return speciesId;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractNamedSBase#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 977;
		int hashCode = super.hashCode();
		if (isSetSpecies()) {
			hashCode += prime * getSpecies().hashCode();
		}
		return hashCode;
	}

	/**
	 * @return the {@link #speciesId}
	 */
	public boolean isSetSpecies() {
		return speciesId != null;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractNamedSBase#readAttribute(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean readAttribute(String attributeName, String prefix,
			String value) {
		boolean isAttributeRead = super.readAttribute(attributeName, prefix,
				value);

		if(!isAttributeRead)
		{
			if (attributeName.equals("species")) {
				setSpecies(value);
			}
			else
			{				
				return false;
			}
		}
			return true;
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.ext.layout.GraphicalObject#writeXMLAttributes()
	 */
	@Override
	public Map<String, String> writeXMLAttributes() {
	  Map<String, String> attributes = super.writeXMLAttributes();
	  
	  if (isSetId()) {
	    attributes.remove("id");
	    attributes.put(LayoutConstant.shortLabel + ":id", getId());
	  }
	  if (isSetSpecies()) {
	    attributes.put(LayoutConstant.shortLabel + ":species", getSpecies());
	  } 
	  
	  return attributes;
	}

	/**
	 * 
	 * @param species
	 */
	public void setSpecies(String species) {
		String oldSpecies = this.speciesId;
		this.speciesId = species;
		firePropertyChange(TreeNodeChangeEvent.species, oldSpecies, this.speciesId);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractNamedSBase#toString()
	 */
	@Override
	public String toString() {
		return "speciesGlyph [" + (isSetSpecies() ? getSpecies() : "") + "]";
	}

}

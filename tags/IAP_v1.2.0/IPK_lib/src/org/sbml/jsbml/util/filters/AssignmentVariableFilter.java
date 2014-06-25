/*
 * $Id: AssignmentVariableFilter.java,v 1.1 2012-11-07 14:43:32 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/util/filters/AssignmentVariableFilter.java $
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

package org.sbml.jsbml.util.filters;

import org.sbml.jsbml.Assignment;

/**
 * This filter only accepts instances of {@link Assignment} with the variable as
 * given in the constructor of this object.
 * 
 * @author rodrigue
 * @author Andreas Dr&auml;ger
 * @since 0.8
 * @version $Rev: 1116 $
 */
public class AssignmentVariableFilter implements Filter {

	/**
	 * The desired identifier for NamedSBases to be acceptable.
	 */
	String id;

	/**
	 * 
	 */
	public AssignmentVariableFilter() {
		this(null);
	}

	/**
	 * 
	 * @param id
	 */
	public AssignmentVariableFilter(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.Filter#fulfilsProperty(java.lang.Object)
	 */
	public boolean accepts(Object o) {
		if (o instanceof Assignment) {
			Assignment er = (Assignment) o;
			if (er.isSetVariable() && (id != null) && er.getVariable().equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

}

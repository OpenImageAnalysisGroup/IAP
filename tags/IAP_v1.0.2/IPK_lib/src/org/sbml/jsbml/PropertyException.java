/*
 * $Id: PropertyException.java,v 1.1 2012-11-07 14:43:34 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/PropertyException.java $
 *
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
package org.sbml.jsbml;

/**
 * This is an error of an undefined property or value for a propterty in some
 * instance of {@link SBase}.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev: 1169 $
 * @since 0.8
 * @date 24.03.2011
 */
public abstract class PropertyException extends SBMLError {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -3416620362835594659L;
	
	/**
	 * 
	 */
	public PropertyException() {
		super();
	}

	/**
	 * @param message
	 */
	public PropertyException(String message) {
		super(message);
	}

	/**
	 * Creates an error message pointing out that the property of the given name is not defined
	 * in the Level/Version combination of the given {@link SBase}.
	 * 
	 * @param property
	 * @param sbase
	 * @return
	 */
	static String createMessage(String baseMessage, String property, SBase sbase) {
		return String.format(baseMessage, property, sbase
				.getElementName(), Integer.valueOf(sbase.getLevel()), Integer
				.valueOf(sbase.getVersion()));
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLError#toString()
	 */
	@Override
	public String toString() {
		return getMessage();
	}

}

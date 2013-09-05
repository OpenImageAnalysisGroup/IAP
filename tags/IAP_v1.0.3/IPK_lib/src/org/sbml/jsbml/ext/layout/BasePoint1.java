/*
 * $Id: BasePoint1.java,v 1.1 2012-11-07 14:43:36 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/extensions/layout/src/org/sbml/jsbml/ext/layout/BasePoint1.java $
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
package org.sbml.jsbml.ext.layout;

/**
 * 
 * @author Sebastian Fr&ouml;hlich
 * @version $Rev: 1116 $
 * @since 1.0
 * @date 21.11.2011
 */
public class BasePoint1 extends Point {
  
	/**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -7544027003069685943L;

  public BasePoint1() {
	super();
  }
  
  public BasePoint1(Point point) {
	  super(point);
	  clonePointAttributes(point, this);
	}

@Override
  public BasePoint1 clone() {
	return new BasePoint1(this);
  }
  
}

/*
 * $Id: IdentifierException.java,v 1.1 2012-11-07 14:43:33 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/IdentifierException.java $
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

import org.apache.log4j.Logger;

/**
 * This exception is thrown when trying to set or add an identifier to an
 * instance of {@link NamedSBase} but if the given identifier is already
 * registered in the containing {@link Model}.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev: 1169 $
 * @since 0.8
 * @date 19.09.2011
 */
public class IdentifierException extends SBMLException {

  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 3203848126194894206L;
  /**
   * 
   */
  private static transient final Logger logger = Logger.getLogger(IdentifierException.class);  
  /**
   * 
   */
  public static final String DUPLICATE_IDENTIFIER_MSG = "Cannot set duplicate %sidentifier '%s' for %s.";

  /**
   * 
   * @param sb
   * @param id
   */
  public IdentifierException(NamedSBase sb, String id) {
    super(String.format(DUPLICATE_IDENTIFIER_MSG, "", id, sb.getElementName()));
    logger.error(String.format(
      "An element with the id '%s' is already present in the SBML model. The identifier of %s will not be set to this value.",
      id, sb.getElementName()));
  }

  /**
   * @param abstractSBase
   * @param metaId
   */
  public IdentifierException(SBase sb, String metaId) {
    super(String.format(DUPLICATE_IDENTIFIER_MSG, "meta ", metaId, sb.getElementName()));
    logger.error(String.format(
      "An element with the metaid '%s' is already present in the SBML document. The element %s will not be annotated with it.",
      metaId, sb.getElementName()));
  }

}

/*
 * $Id: NamedSBaseWithDerivedUnit.java,v 1.1 2012-11-07 14:43:33 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/NamedSBaseWithDerivedUnit.java $
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
 * All objects that implement this interface can be referenced in abstract
 * syntax trees as implemented in {@link ASTNode}. The necessary requirement for
 * this is that these objects can be accessed with an identifier and are
 * associated with some value that can be evaluated to a unit. The value may be
 * defined within the class or have to be computed in a simulation.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-04-22
 * @since 0.8
 * @version $Rev: 1116 $
 */
public interface NamedSBaseWithDerivedUnit extends NamedSBase,
		SBaseWithDerivedUnit {

}

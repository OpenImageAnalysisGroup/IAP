/*
 * $Id: SimpleTreeNodeChangeListener.java,v 1.1 2012-11-07 14:43:37 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/util/SimpleTreeNodeChangeListener.java $
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

package org.sbml.jsbml.util;

import java.beans.PropertyChangeEvent;

import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

/**
 * This very simple implementation of an {@link TreeNodeChangeListener} writes all
 * the events to the standard out stream.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-16
 * @since 0.8
 * @version $Rev: 1116 $
 */
public class SimpleTreeNodeChangeListener implements TreeNodeChangeListener {

  /**
   * 
   */
	private Logger logger;

	/**
	 * Creates an {@link TreeNodeChangeListener} that writes all events to the
	 * standard output.
	 */
	public SimpleTreeNodeChangeListener() {
	  super();
	  logger = Logger.getLogger(SimpleTreeNodeChangeListener.class);
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
	 */
	public void nodeAdded(TreeNode sb) {
            if (logger.isDebugEnabled()) {
		logger.debug(String.format("[ADD]\t%s", sb));		
            }
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(javax.swing.tree.TreeNode)
	 */
	public void nodeRemoved(TreeNode sb) {
            if (logger.isDebugEnabled()) {
		logger.debug(String.format("[DEL]\t%s", sb));
            }
	}

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent ev) {		
            if (logger.isDebugEnabled()) {
		logger.debug(String.format("[CHG]\t%s", ev));
            }
	}

}

/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Mar 25, 2010 by Christian Klukas
 */
package org.graffiti.plugins.modes.defaults;

import java.util.List;

import org.graffiti.graph.GraphElement;

/**
 * @author klukas
 */
public interface DeleteAction {
	
	/**
	 * @param elements
	 */
	void delete(List<GraphElement> elements);
	
}

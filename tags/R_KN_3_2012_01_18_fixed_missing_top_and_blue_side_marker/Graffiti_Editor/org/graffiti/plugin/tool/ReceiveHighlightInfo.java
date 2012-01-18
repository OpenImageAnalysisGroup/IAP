/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Dec 2, 2009 by Christian Klukas
 */

package org.graffiti.plugin.tool;

import org.graffiti.graph.GraphElement;

/**
 * @author klukas
 */
public interface ReceiveHighlightInfo {
	
	/**
	 * @param n
	 */
	void isHighlighted(GraphElement n);
	
}

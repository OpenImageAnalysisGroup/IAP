/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 03.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.services;

import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public interface HandlesAlgorithmData {
	public void setAlgorithm(Algorithm algorithm);
	
	public Algorithm getAlgorithm();
}

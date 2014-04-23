/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.06.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.springembedder_1;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author Christian Klukas
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SpringEmbedderPlugin extends GenericPluginAdapter {
	/**
	 * DOCTODO: Include method header
	 */
	public SpringEmbedderPlugin() {
		this.algorithms = new Algorithm[1];
		this.algorithms[0] = new SpringEmbedderAlgorithm();
	}
	
}

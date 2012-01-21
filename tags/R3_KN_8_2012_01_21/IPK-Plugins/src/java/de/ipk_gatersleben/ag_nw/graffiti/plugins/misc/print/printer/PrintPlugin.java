/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $ID:$
 * Created on 10.07.2003
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.print.printer;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * This plugin provides a print feature.
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 */
public class PrintPlugin extends GenericPluginAdapter {
	
	/**
	 * Empty constructor.
	 * <p>
	 * Creates the <code>PrintAlgorithm</code> instance.
	 * </p>
	 * 
	 * @see PrintAlgorithm
	 */
	public PrintPlugin() {
		super();
		
		this.algorithms = new Algorithm[] { new PrintAlgorithm() };
	}
}

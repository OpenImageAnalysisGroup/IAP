/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 25.11.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.rotate;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

public class RotatePlugin
					extends GenericPluginAdapter {
	public RotatePlugin() {
		this.algorithms = new Algorithm[1];
		this.algorithms[0] = new RotateAlgorithm();
	}
}

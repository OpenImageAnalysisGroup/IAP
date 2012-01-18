/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * DOCUMENT ME!
 * 
 * @author Christian Klukas
 */
public class SBML_XML_WriterPlugin
					extends IPK_PluginAdapter {
	public SBML_XML_WriterPlugin() {
		super();
		this.outputSerializers = new OutputSerializer[] {
				// new SBML_XML_Writer()
				};
	}
}

/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import org.graffiti.plugin.io.InputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * DOCUMENT ME!
 * 
 * @author Christian Klukas
 */
public class SBML_XML_ReaderPlugin
					extends IPK_PluginAdapter {
	public SBML_XML_ReaderPlugin() {
		super();
		this.inputSerializers = new InputSerializer[] {
							new SBML_XML_Reader()
		};
	}
}

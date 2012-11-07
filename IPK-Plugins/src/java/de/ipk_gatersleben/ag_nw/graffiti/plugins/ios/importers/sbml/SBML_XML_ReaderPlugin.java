/*******************************************************************************
 * Copyright (c) 2012 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import org.graffiti.plugin.io.InputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLNodesNiceIdHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

/**
 * DOCUMENT ME!
 * 
 * @author Dagmar Kutz, Matthias Klapperstück
 */
public class SBML_XML_ReaderPlugin extends IPK_PluginAdapter {
	
	public SBML_XML_ReaderPlugin() {
		
		super();
		SBMLNodesNiceIdHelper.initNiceIds();
		SBML_Constants.init();
		this.inputSerializers = new InputSerializer[] {
				new SBML_XML_Reader()
		};
		
	}
	
}

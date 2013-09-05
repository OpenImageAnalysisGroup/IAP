package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax;

import org.graffiti.plugin.io.InputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class BioPAX_OWL_ReaderPlugin extends IPK_PluginAdapter {
	
	/**
	 * constructor initializing InputSerializer
	 */
	public BioPAX_OWL_ReaderPlugin() {
		
		super();
		this.inputSerializers = new InputSerializer[] { new BioPAX_OWL_Reader() };
		
	}
	
}

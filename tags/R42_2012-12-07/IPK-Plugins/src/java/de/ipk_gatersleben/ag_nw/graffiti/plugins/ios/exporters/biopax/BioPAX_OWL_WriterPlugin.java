package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.biopax;

import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class BioPAX_OWL_WriterPlugin extends IPK_PluginAdapter {
	
	/**
	 * constructor initializing OutputSerializer
	 */
	public BioPAX_OWL_WriterPlugin() {
		
		super();
		this.outputSerializers = new OutputSerializer[] { new BioPAX_OWL_Writer() };
		
	}
	
}

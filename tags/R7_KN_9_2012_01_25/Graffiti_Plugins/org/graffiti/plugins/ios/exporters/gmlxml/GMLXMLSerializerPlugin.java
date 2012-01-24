// ==============================================================================
//
// GMLSerializerPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GMLXMLSerializerPlugin.java,v 1.1 2011-01-31 09:03:34 klukas Exp $

package org.graffiti.plugins.ios.exporters.gmlxml;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.io.OutputSerializer;

/**
 * Provides a GML serializer. See
 * http://infosun.fmi.uni-passau.de/Graphlet/GML/ for more details.
 * 
 * @version $Revision: 1.1 $
 */
public class GMLXMLSerializerPlugin
					extends GenericPluginAdapter {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for GMLSerializerPlugin.
	 */
	public GMLXMLSerializerPlugin() {
		super();
		
		// TODO perhaps: merge this and org.graffiti.plugins.io.exporter.gml.GMLReaderPlugin.
		outputSerializers = new OutputSerializer[1];
		outputSerializers[0] = new GMLXMLWriter();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

// ==============================================================================
//
// GMLSerializerPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GMLSerializerPlugin.java,v 1.1 2011-01-31 09:03:39 klukas Exp $

package org.graffiti.plugins.ios.exporters.gml;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.io.OutputSerializer;

/**
 * Provides a GML serializer. See
 * http://infosun.fmi.uni-passau.de/Graphlet/GML/ for more details.
 * 
 * @version $Revision: 1.1 $
 */
public class GMLSerializerPlugin
					extends GenericPluginAdapter {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for GMLSerializerPlugin.
	 */
	public GMLSerializerPlugin() {
		super();
		
		// TODO perhaps: merge this and org.graffiti.plugins.io.exporter.gml.GMLReaderPlugin.
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.EXTENDED_FILE_FORMAT)) {
			outputSerializers = new OutputSerializer[] {
								new GMLWriter(), new GMLgzWriter()
			};
		} else {
			outputSerializers = new OutputSerializer[] {
								new GMLWriter()
			};
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

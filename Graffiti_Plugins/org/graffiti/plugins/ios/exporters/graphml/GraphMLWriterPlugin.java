// ==============================================================================
//
// GraphMLWriterPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphMLWriterPlugin.java,v 1.1 2011-01-31 09:03:25 klukas Exp $

package org.graffiti.plugins.ios.exporters.graphml;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;

/**
 * The plugin class for the graphML writing package.
 * 
 * @author ruediger
 */
public class GraphMLWriterPlugin
					extends GenericPluginAdapter {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GraphMLWriterPlugin</code>.
	 */
	public GraphMLWriterPlugin() {
		super();
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.EXTENDED_FILE_FORMAT)) {
			this.outputSerializers = new GraphMLWriter[] {
								new GraphMLWriter(), new GraphMLgzWriter()
			};
		} else {
			this.outputSerializers = new GraphMLWriter[] {
								new GraphMLWriter()
			};
		}
	}
}
// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

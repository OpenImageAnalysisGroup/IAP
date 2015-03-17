// ==============================================================================
//
// DOTSerializerPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DOTSerializerPlugin.java,v 1.1 2011-01-31 09:03:32 klukas Exp $

package org.graffiti.plugins.ios.exporters.graphviz;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;

/**
 * The plugin for reading and writing files from the Graphviz tools. Graphviz
 * - Graph Drawing Programs from AT&amp;T Research and Lucent Bell Labs.
 * 
 * @version $Revision: 1.1 $
 */
public class DOTSerializerPlugin
					extends GenericPluginAdapter {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new DOTSerializerPlugin object.
	 */
	public DOTSerializerPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.EXTENDED_FILE_FORMAT)) {
			this.outputSerializers = new OutputSerializer[] {
								new DOTSerializer()
			};
		}
		this.inputSerializers = new InputSerializer[] {
							new DOTreader()
		};
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

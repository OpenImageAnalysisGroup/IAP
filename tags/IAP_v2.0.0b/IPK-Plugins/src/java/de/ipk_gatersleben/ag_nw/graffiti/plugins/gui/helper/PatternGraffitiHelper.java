/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 19.11.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper;

import org.HelperClass;
import org.graffiti.editor.GravistoService;
import org.graffiti.managers.pluginmgr.DefaultPluginEntry;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;

/**
 * @author Christian Klukas
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PatternGraffitiHelper implements HelperClass {
	public final static String PATTERN_GRAFFITI_VERSION = "PatternGravisto V1.0";
	
	public static String getPluginStatusText() {
		boolean patternTabsLoaded = GravistoService.getInstance().
							getPluginInstanceFromPluginDescription("IPK Pattern Tabs") != null;
		boolean springEmbedderLoaded = GravistoService.getInstance().
							getAlgorithmInstanceFromFriendlyName(PatternSpringembedder.springName) != null;
		DefaultPluginEntry te = GravistoService.getInstance().getPluginInstanceFromPluginDescription("IPK Editing Tools");
		boolean ipkEditToolsLoaded = te != null && te.getDescription().getAuthor().toUpperCase().indexOf("IPK") >= 0;
		boolean ipkGraffitiViewLoaded = GravistoService.getInstance().
							getPluginInstanceFromPluginDescription("IPK Default View") != null;
		
		return "Plugin Status:\n" +
							"Pattern Control/Layouter: " + patternTabsLoaded + "\n" +
							"Enhanced SpringEmbedder: " + springEmbedderLoaded + "\n" +
							"Enhanced IPK Editing Tools: " + ipkEditToolsLoaded + "\n" +
							"Enhanced IPK Graffiti View: " + ipkGraffitiViewLoaded;
	}
}
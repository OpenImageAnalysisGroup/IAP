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
 */
public class DBEgravistoHelper implements HelperClass {
	public static String DBE_GRAVISTO_VERSION_CODE_SUBVERSION = "release 5 // 03.2014"; // "DBE-Visualisation and Analysis V1.1";
	public static String DBE_GRAVISTO_VERSION_CODE = "1.2"; // "DBE-Visualisation and Analysis V1.1";
	public static String DBE_GRAVISTO_VERSION = "DBE-Gravisto V2014"; // "DBE-Visualisation and Analysis V1.1";
	public static String DBE_GRAVISTO_NAME = "DBE-Gravisto";
	public static String DBE_INFORMATIONSYSTEM_NAME = "DBE Information System";
	public static final String CLUSTER_ANALYSIS_NAME = "Cluster Visualisation";
	public static final String CLUSTER_ANALYSIS_VERSION = "Cluster Visualisation V2014";
	public static String DBE_GRAVISTO_NAME_SHORT = "DBE-Gravisto";
	public static String kgmlFileVersionHint = "<sub><small>v0.7.0</small></sub>";
	
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
				"Pattern Spring Embedder: " + springEmbedderLoaded + "\n" +
				"Enhanced IPK Editing Tools: " + ipkEditToolsLoaded + "\n" +
				"Enhanced IPK Gravisto View: " + ipkGraffitiViewLoaded;
	}
}
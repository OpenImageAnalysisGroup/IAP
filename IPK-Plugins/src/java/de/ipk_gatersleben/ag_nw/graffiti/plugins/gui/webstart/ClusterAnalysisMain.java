/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.attributes.AttributeTypesManager;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.SplashScreenInterface;
import org.graffiti.managers.pluginmgr.DefaultPluginEntry;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.managers.pluginmgr.PluginManager;
import org.graffiti.managers.pluginmgr.PluginManagerException;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.util.PluginHelper;
import org.graffiti.util.ProgressViewer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * Contains the graffiti editor.
 * 
 * @version $Revision: 1.2 $
 */
public class ClusterAnalysisMain {
	// ~ Static fields/initializers =============================================
	
	// ~ Instance fields ========================================================
	
	/** The editor's attribute types manager. */
	private final AttributeTypesManager attributeTypesManager;
	
	/** The editor's main frame. */
	MainFrame mainFrame;
	
	/** The editor's plugin manager. */
	private final PluginManager pluginManager;
	
	public static boolean isClusterAnalysisRunning() {
		return ReleaseInfo.getRunningReleaseStatus() == Release.RELEASE_CLUSTERVIS;
	}
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new instance of the editor.
	 */
	public ClusterAnalysisMain() {
		// URL config,
		SplashScreenInterface splashScreen = new ClusterSplashScreen(
							DBEgravistoHelper.CLUSTER_ANALYSIS_VERSION, "");
		splashScreen.setVisible(true);
		
		GravistoPreferences prefs = GravistoPreferences
							.userNodeForPackage(ClusterAnalysisMain.class);
		pluginManager = new DefaultPluginManager(prefs);
		
		// create an instance of the attribute types manager ...
		attributeTypesManager = new AttributeTypesManager();
		
		// ... and register this instance at the plugin manager
		pluginManager.addPluginManagerListener(attributeTypesManager);
		
		// construct and open the editor's main frame
		GravistoPreferences uiPrefs = prefs.node("ui");
		uiPrefs.put("showPluginManagerMenuOptions", "false");
		uiPrefs.put("showPluginMenu", "false");
		JPanel statusPanel = new JPanel();
		// statusPanel.
		mainFrame = new MainFrame(pluginManager, uiPrefs, statusPanel, false);
		
		ClassLoader cl = ClusterAnalysisMain.class.getClassLoader();
		URL r1 = cl.getResource("plugins_cluster.txt");
		
		ArrayList<String> locations = new ArrayList<String>();
		try {
			locations.addAll(new TextFile(r1));
			locations.remove("");
			
			splashScreen.setMaximum(locations.size() - 1);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
		} catch (NullPointerException npe) {
			System.err.println("Internal error: Plugin Description files could not be loaded.");
			System.err.println("Don't forget to start createfilelist from the make folder.");
			System.err.println("See make - intro.txt for details.");
			System.err.println("-- Program needs to be stopped");
			JOptionPane.showMessageDialog(
								null,
								"<html><h2>ERROR: Plugin-Description files could not be loaded</h2>"
										+ "Program execution can not continue.<br>"
										+ "Pleas check out the \"make\" project and execute<br>" +
										"the createfilelist script from the make folder.<br>"
										+ "See also the make - intro.txt in the make project for details.<br>"
										+ "The application needs to be closed.</html>");
			System.err.println("EXIT");
			System.exit(1);
		}
		
		try {
			// for (String s : locations)
			// System.out.println("Load "+s);
			myLoadPlugins(locations, splashScreen);
		} catch (PluginManagerException pme) {
			ErrorMsg.addErrorMessage(pme.getLocalizedMessage());
		}
		
		// add an empty editor session.
		// mainFrame.addSession(new EditorSession());
		
		splashScreen.setVisible(false);
		mainFrame.setVisible(true);
	}
	
	private void myLoadPlugins(Collection<String> pluginLocations,
						ProgressViewer progressViewer) throws PluginManagerException {
		// load the user's standard plugins
		int numberOfPlugins = pluginLocations.size();
		
		List<String> messages = new LinkedList<String>(); // <String>
		
		PluginEntry[] pluginEntries = new PluginEntry[numberOfPlugins];
		
		int cnt = 0;
		
		for (String pluginLocation : pluginLocations) {
			if (pluginLocation.length() > 0) {
				pluginLocation = pluginLocation.substring(2);
				if (pluginLocation.endsWith("javadoc.xml"))
					continue;
				try {
					ClassLoader cl = ClusterAnalysisMain.class.getClassLoader();
					URL pluginUrl = cl.getResource(pluginLocation);
					PluginDescription desc = PluginHelper
										.readPluginDescription(pluginUrl);
					pluginEntries[cnt++] = new DefaultPluginEntry(pluginUrl
										.toString(), desc);
				} catch (PluginManagerException err) {
					System.out.println("Plugin Manager Exception for "
										+ pluginLocation);
					ErrorMsg.addErrorMessage("Plugin Manager Exception for "
										+ pluginLocation + ". Exception: "
										+ err.getLocalizedMessage());
					System.err.println(err.getLocalizedMessage());
					messages.add(err.getLocalizedMessage());
				} catch (Exception err) {
					System.out.println("Exception for " + pluginLocation);
					ErrorMsg.addErrorMessage("Exception for " + pluginLocation
										+ ". Exception: " + err.getLocalizedMessage());
					messages.add(err.getLocalizedMessage());
				}
			}
		}
		// System.exit(1);
		int loaded = 0;
		for (int i = 0; i < cnt; i++) {
			if (pluginEntries[i] != null) {
				loaded++;
			}
		}
		PluginEntry[] loadedPlugins = new PluginEntry[loaded];
		int i2 = 0;
		for (int i = 0; i < cnt; i++) {
			if (pluginEntries[i] != null) {
				loadedPlugins[i2] = pluginEntries[i];
				i2++;
			}
		}
		
		try {
			((DefaultPluginManager) pluginManager).loadPlugins(loadedPlugins,
								progressViewer, true);
		} catch (PluginManagerException pme) {
			System.err.println("Plugin Manager Exception: "
								+ pme.getLocalizedMessage());
			ErrorMsg.addErrorMessage("PluginManagerException: "
								+ pme.getLocalizedMessage());
			messages.add(pme.getMessage());
		}
		
		// collect info of all exceptions into one exception
		if (!messages.isEmpty()) {
			String msg = "";
			
			for (Iterator<String> itr = messages.iterator(); itr.hasNext();) {
				msg += (itr.next() + "\n");
			}
			
			throw new PluginManagerException("exception.loadStartup\n", msg.trim());
		}
	}
	
	// ~ Methods ================================================================
	
	/**
	 * The editor's main method.
	 * 
	 * @param args
	 *           the command line arguments.
	 */
	public static void main(String[] args) {
		ReleaseInfo.setRunningReleaseStatus(Release.RELEASE_CLUSTERVIS);
		// System.setProperty("sun.java2d.opengl", "true");
		// try {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// } catch (Exception e) {
		// System.out
		//					.println("Info: could not activate system windows and button style"); //$NON-NLS-1$
		// }
		
		Properties p = System.getProperties();
		String os = (String) p.get("os.name");
		if (os != null && !os.toUpperCase().contains("LINUX")) {
			// do not activate system look and feel under linux!
			// problem: under linux and gnome style the mouse cursors are not
			// changed correctly
			try {
				if (!ReleaseInfo.isRunningAsApplet())
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				System.out
									.println("Info: could not activate system windows and button style"); //$NON-NLS-1$
			}
		}
		
		ClusterAnalysisMain.class.getClassLoader();
		ClusterAnalysisMain.class.getPackage().getName().replace(
							'.', '/');
		
		// URL r1 = cl.getResource(path+"/jarprefs.xml");
		// URL r2 = cl.getResource(path + "/default.gml");
		// if (r2 == null)
		// ErrorMsg
		// .addErrorMessage("Error: Default Graph file could not be loaded from resource.");
		new ClusterAnalysisMain();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

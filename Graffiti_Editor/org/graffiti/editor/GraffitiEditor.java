// ==============================================================================
//
// GraffitiEditor.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiEditor.java,v 1.1 2011-01-31 09:04:27 klukas Exp $

package org.graffiti.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.ReleaseInfo;
import org.graffiti.attributes.AttributeTypesManager;
import org.graffiti.core.StringBundle;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.managers.pluginmgr.PluginManager;
import org.graffiti.managers.pluginmgr.PluginManagerException;
import org.graffiti.options.GravistoPreferences;

/**
 * Contains the graffiti editor.
 * 
 * @version $Revision: 1.1 $
 */
public class GraffitiEditor {
	// ~ Static fields/initializers =============================================
	
	/** The logger for the current class. */
	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
	
	// ~ Instance fields ========================================================
	
	/** The editor's attribute types manager. */
	private AttributeTypesManager attributeTypesManager;
	
	/** The editor's main frame. */
	private MainFrame mainFrame;
	
	/** The editor's plugin manager. */
	private PluginManager pluginManager;
	
	/** The preferences of the editor. */
	private GravistoPreferences prefs;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new instance of the editor.
	 */
	public GraffitiEditor() {
		// create splash screen.
		SplashScreen splashScreen = new SplashScreen();
		splashScreen.setVisible(true);
		
		// initialize the editor's prefs.
		prefs = GravistoPreferences.userNodeForPackage(GraffitiEditor.class);
		
		// create an instance of the plugin manager.
		pluginManager = new DefaultPluginManager(prefs.node("pluginmgr"));
		
		// create an instance of the attribute types manager ...
		attributeTypesManager = new AttributeTypesManager();
		
		// ... and register this instance at the plugin manager
		pluginManager.addPluginManagerListener(attributeTypesManager);
		
		// construct and open the editor's main frame
		mainFrame = new MainFrame(pluginManager, prefs.node("ui"));
		
		try {
			pluginManager.loadStartupPlugins(splashScreen);
			splashScreen.dispose();
		} catch (PluginManagerException pme) {
			splashScreen.dispose();
			showMessageDialog(pme.getMessage());
		}
		
		// add an empty editor session.
		// mainFrame.addSession(new EditorSession());
		mainFrame.setVisible(true);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * The editor's main method.
	 * 
	 * @param args
	 *           the command line arguments.
	 */
	public static void main(String[] args) {
		try {
			if (ReleaseInfo.isRunningAsApplet())
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.log(Level.WARNING,
								"Exception while setting system look & feel", e);
		}
		
		// temporary added for consistency of language
		Locale.setDefault(Locale.ENGLISH);
		
		// reading the logging config file
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream(
								"org/graffiti/editor/Logging.properties"));
		} catch (IOException e) {
			logger.info("Start without specified logging properties");
		}
		
		GraffitiEditor e = new GraffitiEditor();
		
		// parse the command line arguments.
		e.parseCommandLineArguments(args);
	}
	
	/**
	 * Parses the command line arguments passed to this class.
	 * 
	 * @param args
	 *           the command line arguments passed to this class.
	 */
	private void parseCommandLineArguments(String[] args) {
		for (int i = 0; i < args.length; i++)
			mainFrame.loadGraph(new File(args[i]));
	}
	
	/**
	 * Shows an arbitrary message dialog.
	 * 
	 * @param msg
	 *           the message to be shown.
	 */
	private void showMessageDialog(String msg) {
		JOptionPane.showMessageDialog(mainFrame, msg,
							StringBundle.getInstance().getString("message.dialog.title"),
							JOptionPane.WARNING_MESSAGE);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

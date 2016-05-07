/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import info.clearthought.layout.TableLayout;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.ApplicationStatus;
import org.AttributeHelper;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.attributes.AttributeTypesManager;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.SplashScreenInterface;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.managers.pluginmgr.PluginManager;
import org.graffiti.managers.pluginmgr.PluginManagerException;
import org.graffiti.options.GravistoPreferences;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.DefaultContextMenuManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * Contains the graffiti editor.
 * 
 * @version $Revision: 1.4 $
 */
public class GravistoMain extends JApplet {
	// ~ Static fields/initializers =============================================
	
	private static final long serialVersionUID = 1L;
	
	// ~ Instance fields ========================================================
	
	/** The editor's attribute types manager. */
	private AttributeTypesManager attributeTypesManager;
	
	/** The editor's main frame. */
	MainFrame mainFrame;
	
	/** The editor's plugin manager. */
	private final PluginManager pluginManager;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new instance of the editor.
	 */
	public GravistoMain(final boolean showMainFrame, String applicationName) {
		// URL config,
		SplashScreenInterface splashScreen = new DBEsplashScreen(applicationName,
				"", new RunnableWithSplashScreenReference() {
					@Override
					public void run() {
						if (showMainFrame) {
							ClassLoader cl = this.getClass().getClassLoader();
							String path = this.getClass().getPackage().getName()
									.replace('.', '/');
							ImageIcon icon = new ImageIcon(cl.getResource(path
									+ "/ipklogo16x16_5.png"));
							mainFrame.setIconImage(icon.getImage());
							mainFrame.setVisible(true);
						}
					}
					
					@Override
					public void setSplashscreenInfo(SplashScreenInterface ss) {
						// empty
					}
				});
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName()
				.replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path
				+ "/ipklogo16x16_5.png"));
		((DBEsplashScreen) splashScreen).setIconImage(icon.getImage());
		splashScreen.setVisible(true);
		
		GravistoMainHelper.createApplicationSettingsFolder(splashScreen);
		
		splashScreen.setText("Read plugin information");
		
		GravistoPreferences prefs = GravistoPreferences
				.userNodeForPackage(GravistoMain.class);
		pluginManager = GravistoMainHelper.getPluginManager();
		
		splashScreen.setText("Read plugin information.");
		
		// create an instance of the attribute types manager ...
		attributeTypesManager = new AttributeTypesManager();
		
		// ... and register this instance at the plugin manager
		pluginManager.addPluginManagerListener(attributeTypesManager);
		
		// construct and open the editor's main frame
		GravistoPreferences uiPrefs = prefs.node("ui");
		uiPrefs.put("showPluginManagerMenuOptions", "false");
		uiPrefs.put("showPluginMenu", "false");
		
		splashScreen.setText("Read plugin information..");
		
		JPanel statusPanel = new JPanel();
		// statusPanel.
		mainFrame = new MainFrame(pluginManager, uiPrefs, statusPanel, true);
		
		// ClassLoader cl = GravistoMain.class.getClassLoader();
		URL r1 = cl.getResource("plugins1.txt");
		URL r2 = cl.getResource("plugins2.txt");
		URL r3 = cl.getResource("plugins3.txt");
		URL r4 = cl.getResource("plugins4.txt");
		
		URL rExcl = cl.getResource("plugins_exclude.txt");
		
		splashScreen.setText("Read plugin information...");
		
		ArrayList<String> locations = new ArrayList<String>();
		try {
			locations.addAll(new TextFile(r1));
			locations.addAll(new TextFile(r2));
			locations.addAll(new TextFile(r3));
			locations.addAll(new TextFile(r4));
			locations.remove("");
			ArrayList<String> locations_exclude = new ArrayList<String>();
			locations_exclude.addAll(new TextFile(rExcl));
			for (Iterator<String> it = locations_exclude.iterator(); it.hasNext();) {
				String remove = it.next();
				if (!locations.remove(remove)) {
					if (remove.startsWith("**")) {
						remove = remove.substring("**".length());
						ArrayList<String> toRemove = new ArrayList<>();
						for (String loc : locations) {
							if (loc.endsWith(remove))
								toRemove.add(loc);
						}
						locations.removeAll(toRemove);
					} else {
						// windows compatibility remove also not exact matches
						for (Iterator<String> itl = locations.iterator(); itl.hasNext();) {
							String loc = itl.next();
							remove = remove.toUpperCase();
							remove = StringManipulationTools.stringReplace(remove, "./", "");
							remove = StringManipulationTools.stringReplace(remove, "\"", "");
							remove = StringManipulationTools.stringReplace(remove, "/", "\\");
							remove = StringManipulationTools.stringReplace(remove, "\\", "");
							remove = StringManipulationTools.stringReplace(remove, " ", "");
							String loc2 = loc.toUpperCase();
							loc2 = StringManipulationTools.stringReplace(loc2, "\\", "");
							loc2 = StringManipulationTools.stringReplace(loc2, " ", "");
							if (loc2.indexOf(remove) >= 0) {
								locations.remove(loc);
								break;
							}
						}
					}
				}
			}
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
			
			System.exit(1);
		}
		
		// printLocations(locations, "info");
		splashScreen.setText("Load plugins...");
		try {
			GravistoMainHelper.loadPlugins(locations, splashScreen);
		} catch (PluginManagerException pme) {
			ErrorMsg.addErrorMessage(pme.getLocalizedMessage());
		}
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				JMenu dummyScipt = new JMenu("Dummy Script");
				DefaultContextMenuManager.returnScriptMenu(dummyScipt);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		splashScreen.setText("Processing finished");
		splashScreen.setVisible(false);
		splashScreen.setInitialisationFinished();
		ErrorMsg.setAppLoadingCompleted(ApplicationStatus.PROGRAM_LOADING_FINISHED);
	}
	
	public void appletDragStarted() {
		JFrame jf = (JFrame) ErrorMsg.findParentComponent(MainFrame.getInstance(), JFrame.class);
		if (jf != null) {
			jf.setUndecorated(false);
			jf.setTitle(MainFrame.getInstance().getTitle());
		}
	}
	
	public GravistoMain() {
		ReleaseInfo.setRunningAsApplet(this);
		
		try {
			UIManager.setLookAndFeel(
					UIManager.getCrossPlatformLookAndFeelClassName());
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// empty
		}
		
		// construct and open the editor's main frame
		GravistoPreferences prefs = GravistoPreferences.userNodeForPackage(GravistoMain.class);
		pluginManager = new DefaultPluginManager(prefs);
		
		GravistoPreferences uiPrefs = prefs.node("ui");
		uiPrefs.put("showPluginManagerMenuOptions", "false");
		uiPrefs.put("showPluginMenu", "false");
		
		// statusPanel.
		mainFrame = new MainFrame(GravistoMainHelper.getPluginManager(), uiPrefs, null, true);
		
		setLayout(new TableLayout(new double[][] { { TableLayout.FILL }, { TableLayout.FILL } }));
		JMenuBar mb = mainFrame.getJMenuBar();
		add(TableLayout.getSplitVertical(
				mb, mainFrame.getContentPane(), TableLayout.PREFERRED, TableLayout.FILL)
				, "0,0");
		validate();
		
		Thread t = new Thread() {
			
			@Override
			public void run() {
				myAppletLoad(mainFrame);
			}
		};
		
		t.start();
	}
	
	public void myAppletLoad(MainFrame statusPanel) {
		DBEgravistoHelper.DBE_GRAVISTO_VERSION = "VANTED applet (beta)"; // "DBE-Visualisation and Analysis V1.1";
		String stS = "<font color=\"#9500C0\"><b>";
		String stE = "</b></font>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME = "" + stS + "DBE-Gravisto" + stE + "<br><small>* "
				+ stS + "D" + stE + "ata integration and analysis for " + stS + "B" + stE + "iological " + stS + "E" + stE + "xperiments<br>* "
				+ stS + "Gra" + stE + "ph " + stS + "vis" + stE + "alisation " + stS + " to" + stE + "olkit<br></small>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT = "VANTED applet";
		DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME = "";
		
		// AttributeHelper.setMacOSsettings(DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT);
		
		String name = stS + "DBE-Gravisto" + stE + " - "
				+ stS + "D" + stE + "ata integration and analysis for " + stS + "B" + stE + "iological " + stS + "E" + stE + "xperiments, "
				+ stS + "Gra" + stE + "ph " + stS + "vis" + stE + "ualisation " + stS + "to" + stE + "olkit";
		JComponent result = new JPanel();
		result.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		
		String s = ""
				+
				"<html><small><br>&nbsp;&nbsp;&nbsp;</small>Welcome to "
				+ name
				+ "!<br>"
				+
				"<small>"
				+
				"&nbsp;&nbsp;&nbsp;In the <b>Help menu</b> you find a <b>tutorial section</b> which quickly gives an overview on the various features of this application.<br>"
				+
				"&nbsp;&nbsp;&nbsp;Furthermore you will find <b>[?] buttons</b> throughout the system which point directly to topics of interest.<br>"
				+
				"&nbsp;&nbsp;&nbsp;If you experience problems or would like to suggest enhancements, feel free to use the <b>Send feedback command</b> in the Help menu!<br>&nbsp;";
		
		ReleaseInfo.setHelpIntroductionText(s);
		
		// URL config,
		final SplashScreenInterface splashScreen = new SplashScreenInterface() {
			
			@Override
			public void setVisible(boolean b) {
			}
			
			@Override
			public void setValue(int value) {
				
			}
			
			@Override
			public void setText(String text) {
				MainFrame.showMessage(text, MessageType.PERMANENT_INFO);
			}
			
			@Override
			public void setMaximum(int maximum) {
				//
				
			}
			
			@Override
			public void setInitialisationFinished() {
				MainFrame.showMessage("", MessageType.INFO);
			}
			
			@Override
			public int getValue() {
				//
				return 0;
			}
			
			@Override
			public int getMaximum() {
				//
				return 0;
			}
		};
		ClassLoader cl = this.getClass().getClassLoader();
		
		String path = // this.getClass().getPackage().getName()
		"de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart".replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path
				+ "/ipklogo16x16_5.png"));
		if (splashScreen instanceof DBEsplashScreen)
			((DBEsplashScreen) splashScreen).setIconImage(icon.getImage());
		// splashScreen.setVisible(true);
		
		GravistoMainHelper.createApplicationSettingsFolder(splashScreen);
		
		splashScreen.setText("Read plugin information");
		
		// ClassLoader cl = GravistoMain.class.getClassLoader();
		URL r1 = cl.getResource("plugins1.txt");
		URL r2 = cl.getResource("plugins2.txt");
		URL r3 = cl.getResource("plugins3.txt");
		URL r4 = cl.getResource("plugins4.txt");
		
		URL rExcl = cl.getResource("plugins_exclude.txt");
		
		splashScreen.setText("Read plugin information...");
		
		ArrayList<String> locations = new ArrayList<String>();
		try {
			locations.addAll(new TextFile(r1));
			locations.addAll(new TextFile(r2));
			locations.addAll(new TextFile(r3));
			locations.addAll(new TextFile(r4));
			locations.remove("");
			ArrayList<String> locations_exclude = new ArrayList<String>();
			locations_exclude.addAll(new TextFile(rExcl));
			for (Iterator<String> it = locations_exclude.iterator(); it.hasNext();) {
				String remove = it.next();
				if (!locations.remove(remove)) {
					// windows compatibility remove also not exact matches
					for (Iterator<String> itl = locations.iterator(); itl.hasNext();) {
						String loc = itl.next();
						remove = remove.toUpperCase();
						remove = StringManipulationTools.stringReplace(remove, "./", "");
						remove = StringManipulationTools.stringReplace(remove, "\"", "");
						remove = StringManipulationTools.stringReplace(remove, "/", "\\");
						remove = StringManipulationTools.stringReplace(remove, "\\", "");
						remove = StringManipulationTools.stringReplace(remove, " ", "");
						String loc2 = loc.toUpperCase();
						loc2 = StringManipulationTools.stringReplace(loc2, "\\", "");
						loc2 = StringManipulationTools.stringReplace(loc2, " ", "");
						if (loc2.indexOf(remove) >= 0) {
							locations.remove(loc);
							break;
						}
					}
				}
			}
			splashScreen.setMaximum(locations.size() - 1);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
		} catch (NullPointerException npe) {
			System.err
					.println("Internal error: Plugin Description files could not be loaded.");
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
		
		splashScreen.setText("Load plugins...");
		try {
			GravistoMainHelper.loadPlugins(locations, splashScreen);
		} catch (PluginManagerException pme) {
			ErrorMsg.addErrorMessage(pme.getLocalizedMessage());
		}
		
		splashScreen.setText("Initialize GUI...");
		splashScreen.setVisible(false);
		splashScreen.setInitialisationFinished();
		ErrorMsg.setAppLoadingCompleted(ApplicationStatus.PROGRAM_LOADING_FINISHED);
	}
	
	//
	// /**
	// * @param defaultGraph
	// * @param splashScreen
	// */
	// private void loadDefaultGraph(final URL defaultGraph,
	// SplashScreenInterface splashScreen) {
	// try {
	// splashScreen.setText("Load Default Graph...");
	// URL u = defaultGraph;
	// final InputStream is = u.openStream();
	// if (is == null) {
	// ErrorMsg.addErrorMessage("Default Graph could not be loaded. InputStream is NULL.");
	// }
	// if (mainFrame == null) {
	// System.err.println("MAINFRAME IS NULL.");
	// } else {
	// mainFrame.loadGraph("default.gml", defaultGraph);
	// }
	// } catch (Exception e3) {
	// ErrorMsg.addErrorMessage("Default Graph could not be loaded: "
	// + e3.getLocalizedMessage() + "<br>" + "Message: "
	// + e3.getMessage() + "<br>");
	// }
	// // System.out.println((System.getProperty("java.library.path")));
	// }
	
	// private void printLocations(ArrayList<String> locations, String filter) {
	// System.out.println("==================================");
	// for (Iterator<String> it = locations.iterator(); it.hasNext();) {
	// String loc = (String) it.next();
	// if (filter == null || filter.length()<=0 || loc.indexOf(filter) >= 0)
	// System.out.println(loc);
	// }
	// System.out.println("==================================");
	// }
	
	// ~ Methods ================================================================
	
	/**
	 * The editor's main method.
	 * 
	 * @param args
	 *           the command line arguments.
	 */
	public static void main(String[] args) {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		
		DBEgravistoHelper.DBE_GRAVISTO_VERSION = "DBE-Gravisto V1.1 (beta)"; // "DBE-Visualisation and Analysis V1.1";
		String stS = "<font color=\"#9500C0\"><b>";
		String stE = "</b></font>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME = "" + stS + "DBE-Gravisto" + stE + "<br><small>* "
				+ stS + "D" + stE + "ata integration and analysis for " + stS + "B" + stE + "iological " + stS + "E" + stE + "xperiments<br>* "
				+ stS + "Gra" + stE + "ph " + stS + "vis" + stE + "alisation " + stS + " to" + stE + "olkit<br></small>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT = "DBE-Gravisto";
		DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME = "";
		
		AttributeHelper.setMacOSsettings(DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT);
		
		String name = stS + "DBE-Gravisto" + stE + " - "
				+ stS + "D" + stE + "ata integration and analysis for " + stS + "B" + stE + "iological " + stS + "E" + stE + "xperiments, "
				+ stS + "Gra" + stE + "ph " + stS + "vis" + stE + "ualisation " + stS + "to" + stE + "olkit";
		JComponent result = new JPanel();
		result.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		
		String s = ""
				+
				"<html><small><br>&nbsp;&nbsp;&nbsp;</small>Welcome to "
				+ name
				+ "!<br>"
				+
				"<small>"
				+
				"&nbsp;&nbsp;&nbsp;In the <b>Help menu</b> you find a <b>tutorial section</b> which quickly gives an overview on the various features of this application.<br>"
				+
				"&nbsp;&nbsp;&nbsp;Furthermore you will find <b>[?] buttons</b> throughout the system which point directly to topics of interest.<br>"
				+
				"&nbsp;&nbsp;&nbsp;If you experience problems or would like to suggest enhancements, feel free to use the <b>Send feedback command</b> in the Help menu!<br>&nbsp;";
		
		ReleaseInfo.setHelpIntroductionText(s);
		
		new GravistoMain(true, DBEgravistoHelper.DBE_GRAVISTO_VERSION);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

/*******************************************************************************
 * Copyright (c) 2009 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk.ag_ba.gui.webstart;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ApplicationStatus;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.SplashScreenInterface;
import org.graffiti.managers.pluginmgr.PluginManagerException;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.graffiti.util.InstanceLoader;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.image.operation.ImageConverter;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.SaveAsCsvDataProcessor;
import de.ipk.ag_ba.mongo.SaveInDatabaseDataProcessor;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.DBEsplashScreen;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.GravistoMainHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class IAPmain extends JApplet {
	private static final long serialVersionUID = 1L;
	
	static MainFrame mainFrame1;
	
	private static Runnable vantedDelayedRunnable;
	
	// static MainFrame mainFrame2;
	
	public void setAppletCloseListener(ActionListener l) {
		// empty
	}
	
	public void appletDragStarted() {
		final JFrame jf = (JFrame) ErrorMsg.findParentComponent(ReleaseInfo.getApplet(), JFrame.class);
		if (jf != null) {
			jf.setUndecorated(false);
			jf.setResizable(true);
			jf.setMaximizedBounds(null);
			jf.setTitle("Integrated Analysis Platform");
			jf.pack();
			jf.setSize(800, 600);
		} else
			System.out.println("Drag detected, but frame not found.");
	}
	
	static boolean myClassKnown = false;
	
	@Override
	public void init() {
		super.init();
		setRunMode(IAPrunMode.SWING_APPLET);
	}
	
	public static void main(String[] args) {
		setRunMode(IAPrunMode.SWING_MAIN);
		String title = IAPoptions.getInstance().getString("IAP", "window_title",
				"IAP - The Integrated Analysis Platform") + "";
		final JFrame jf = new JFrame(title);
		jf.add("Center", new IAPmain().getContentPane());
		jf.pack();
		try {
			jf.setIconImage(GravistoService.loadImage(IAPmain.class, "img/favicon.ico", 48, 48));
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SystemOptions.getInstance().addChangeListener("IAP", "window_title", new Runnable() {
			@Override
			public void run() {
				String newTitle = IAPoptions.getInstance().getString("IAP", "window_title", "IAP - The Integrated Analysis Platform");
				jf.setTitle(newTitle + "");
			}
		});
	}
	
	public IAPmain() {
		if (getRunMode() == IAPrunMode.UNKNOWN)
			setRunMode(IAPrunMode.SWING_APPLET);
		System.out.println("Initialize IAP start... (run-mode: " + getRunMode() + ")");
		if (getRunMode() == IAPrunMode.SWING_APPLET)
			ReleaseInfo.setRunningAsApplet(this);
		
		ErrorMsg.setRethrowErrorMessages(false);
		
		setupLogger();
		
		registerIOhandlers();
		
		GravistoMainHelper.setLookAndFeel();
		
		// construct and open the editor's main frame
		GravistoPreferences prefs = GravistoPreferences.userNodeForPackage(IAPmain.class);
		
		GravistoPreferences uiPrefs = prefs.node("ui");
		uiPrefs.put("showPluginManagerMenuOptions", getOptions().getBoolean("VANTED", "debug show plugin_manager_menu_options", false) + "");
		uiPrefs.put("showPluginMenu", getOptions().getBoolean("VANTED", "debug show plugin_menu", false) + "");
		JPanel statusPanel = new JPanel();
		
		mainFrame1 = new MainFrame(GravistoMainHelper.getPluginManager(), uiPrefs, statusPanel, true);
		// mainFrame2 = new MainFrame(GravistoMainHelper.getNewPluginManager(), uiPrefs, statusPanel, true);
		
		setLayout(new TableLayout(new double[][] { { TableLayout.FILL }, { TableLayout.FILL } }));
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"", "");
		JComponent advancedNavigation = IAPgui.getMainGUIcontent(myStatus, false);
		add(advancedNavigation, "0,0");
		setVisible(true);
		validate();
		repaint();
		
		Thread t = new Thread() {
			
			@Override
			public void run() {
				if (IAPmain.myClassKnown) {
					System.out.println("Reload Classes, Problems may occur");
					ErrorMsg.addErrorMessage("Reload Classes, Problems may occur");
				}
				IAPmain.myClassKnown = true;
				System.out.println("Class Loader: " + InstanceLoader.getCurrentLoader().getClass().getCanonicalName());
				myAppletLoad(mainFrame1, myStatus);
				// myAppletLoad(mainFrame2, myStatus);
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	private void setupLogger() {
		Logger rootLogger = Logger.getRootLogger();
		if (!rootLogger.getAllAppenders().hasMoreElements()) {
			rootLogger.setLevel(Level.ERROR);
			rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));
			
			Logger pkgLogger = rootLogger.getLoggerRepository().getLogger("com.mongodb");
			pkgLogger.setLevel(Level.ERROR);
			pkgLogger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
		}
	}
	
	private void registerIOhandlers() {
		// ResourceIOManager.registerIOHandler(LoadedVolumeHandler.getInstance());
		// ResourceIOManager.registerIOHandler(LoadedImageHandler.getInstance());
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler handler : m.getHandlers())
				ResourceIOManager.registerIOHandler(handler);
	}
	
	public void myAppletLoad(final MainFrame statusPanel, final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus) {
		String stS = "<font color=\"#9500C0\"><b>";
		String stE = "</b></font>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT = "IAP-Data-Navigator";
		String name = stS + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT + stE + " - " + stS + "V" + stE + "isualization and " + stS + "A" + stE
				+ "nalysis of " + stS + "N" + stE + "e" + stS + "t" + stE + "works <br>containing " + stS + "E" + stE
				+ "xperimental " + stS + "D" + stE + "ata";
		DBEgravistoHelper.DBE_GRAVISTO_VERSION = DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT + " V" + DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE;
		DBEgravistoHelper.DBE_GRAVISTO_NAME = stS + DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT + stE + "&nbsp;-&nbsp;" + stS + "I" + stE
				+ "ntegrated&nbsp;" + stS + "A" + stE + "nalysis&nbsp;" + stS + "P" + stE + "latform<br>";
		DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME = "Integrated Analysis Platform";
		
		DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME = "";
		
		// AttributeHelper.setMacOSsettings(DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT);
		
		final JComponent result = new JPanel();
		result.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		
		String s = ""
				+ "<html><small><br>&nbsp;&nbsp;&nbsp;</small>Welcome to "
				+ name
				+ "!<br>"
				+ "<small>"
				+ "&nbsp;&nbsp;&nbsp;In the <b>Help menu</b> you find a <b>tutorial section</b> which quickly gives an overview on the various "
				+ "features of this application.<br>"
				+ "&nbsp;&nbsp;&nbsp;Furthermore you will find <b>[?] buttons</b> throughout the system which point directly to topics of interest.<br>"
				+ "&nbsp;&nbsp;&nbsp;If you experience problems or would like to suggest enhancements, feel free to use the "
				+ "<b>Send feedback command</b> in the Help menu!<br>&nbsp;";
		
		ReleaseInfo.setHelpIntroductionText(s);
		
		// URL config,
		final SplashScreenInterface splashScreen = new SplashScreenInterface() {
			
			private int max, val;
			
			@Override
			public void setVisible(boolean b) {
			}
			
			@Override
			public void setValue(int value) {
				this.val = value;
				double progress = val / (double) max * 100d;
				myStatus.setCurrentStatusValueFine(progress);
			}
			
			@Override
			public void setText(String text) {
				MainFrame.showMessage(text, MessageType.PERMANENT_INFO);
				System.out.println(text);
			}
			
			@Override
			public void setMaximum(int maximum) {
				this.max = maximum;
			}
			
			@Override
			public void setInitialisationFinished() {
				MainFrame.showMessage("", MessageType.INFO);
			}
			
			@Override
			public int getValue() {
				return val;
			}
			
			@Override
			public int getMaximum() {
				return max;
			}
		};
		ClassLoader cl = this.getClass().getClassLoader();
		
		String path = // this.getClass().getPackage().getName()
		"de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart".replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/ipklogo16x16_5.png"));
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
		
		final ArrayList<String> locations = new ArrayList<String>();
		try {
			locations.addAll(new TextFile(r1));
			locations.addAll(new TextFile(r2));
			locations.addAll(new TextFile(r3));
			locations.addAll(new TextFile(r4));
			
			locations.add("./MultimodalDataHandling.xml");
			// locations.add("./HIVE.xml");
			
			locations.remove("");
			ArrayList<String> locations_exclude = new ArrayList<String>();
			locations_exclude.addAll(new TextFile(rExcl));
			
			// for (String ss : locations) {
			// // System.out.println(ss);
			// if (ss.indexOf("addon") >= 0) {
			// locations_exclude.add(ss);
			// System.out.println("Disable plugin " + ss);
			// }
			//
			// }
			
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
			System.err.println("Internal error: Plugin Description files could not be loaded.");
			System.err.println("-- Program needs to be stopped");
			JOptionPane.showMessageDialog(null, "<html><h2>ERROR: Plugin-Description files could not be loaded</h2>"
					+ "Program execution can not continue.<br>" + "The application needs to be closed.</html>");
			System.err.println("EXIT");
			System.exit(1);
		}
		
		final boolean onStartup = true; // IAPoptions.getInstance().getBoolean("VANTED", "load_plugins_on_startup", false);
		final boolean onDemand = false; // IAPoptions.getInstance().getBoolean("VANTED", "load_plugins_on_demand", true);
		// ArrayList<String> remove = new ArrayList<String>();
		// ArrayList<String> importantForEditingSettingValues = new ArrayList<String>();
		// for (String ss : locations) {
		// if (ss.endsWith("attributes\\defaults\\plugin.xml") || ss.contains("editcomponents\\defaults\\plugin.xml")) {
		// remove.add(ss);
		// System.out.println(ss);
		// importantForEditingSettingValues.add(ss);
		// }
		// }
		// try {
		// GravistoMainHelper.loadPlugins(statusPanel.getPluginManager(), importantForEditingSettingValues, splashScreen);
		// } catch (PluginManagerException pme) {
		// ErrorMsg.addErrorMessage(pme.getLocalizedMessage());
		// }
		// for (String r : remove)
		// locations.remove(r);
		if (onStartup) {
			try {
				splashScreen.setText("Load plugins...");
				GravistoMainHelper.loadPlugins(locations, splashScreen);
			} catch (PluginManagerException pme) {
				ErrorMsg.addErrorMessage(pme.getLocalizedMessage());
			}
		} else {
			vantedDelayedRunnable = new Runnable() {
				@Override
				public void run() {
					if (onDemand) {
						try {
							splashScreen.setText("Load plugins...");
							GravistoMainHelper.loadPlugins(locations, splashScreen);
						} catch (PluginManagerException pme) {
							ErrorMsg.addErrorMessage(pme.getLocalizedMessage());
						}
					}
				}
			};
		}
		ExperimentDataProcessingManager.addExperimentDataProcessor(new SaveInDatabaseDataProcessor());
		ExperimentDataProcessingManager.addExperimentDataProcessor(new SaveAsCsvDataProcessor());
		
		splashScreen.setText("Initialize GUI...");
		splashScreen.setVisible(false);
		splashScreen.setInitialisationFinished();
		ErrorMsg.setAppLoadingCompleted(ApplicationStatus.PROGRAM_LOADING_FINISHED);
	}
	
	public static void prepareVantedPlugins() {
		if (vantedDelayedRunnable != null) {
			vantedDelayedRunnable.run();
			vantedDelayedRunnable = null;
		}
	}
	
	public static JComponent showVANTED(boolean inline) {
		
		prepareVantedPlugins();
		
		// inline = false;
		// JFrame jf = (JFrame) ErrorMsg.findParentComponent(MainFrame.getInstance(), JFrame.class);
		
		// mainFrame2
		for (MainFrame jc : new MainFrame[] { mainFrame1 }) {
			JFrame jf = (JFrame) ErrorMsg.findParentComponent(jc, JFrame.class);
			if (jf != null && !jf.isVisible()) {
				if (inline) {
					MainFrame.getInstance().getViewManager().viewChanged(null);
					JComponent gui = jf.getRootPane();
					return gui;
				} else {
					jf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					jf.setVisible(true);
					MainFrame.getInstance().getViewManager().viewChanged(null);
				}
			} else {
				if (jf != null) {
					jf.setVisible(false);
					jf.setVisible(true);
				}
			}
		}
		return null;
	}
	
	private static HashMap<String, NavigationImage> cachedImages = new HashMap<String, NavigationImage>();
	
	public static synchronized NavigationImage loadIcon(String name) {
		if (cachedImages.containsKey(name))
			return cachedImages.get(name);
		NavigationImage res = new NavigationImage(
				ImageConverter.getBufferedImageFromImage(GravistoService.loadIcon(IAPmain.class, name).getImage()), name);
		cachedImages.put(name, res);
		return res;
	}
	
	public static boolean isSettingEnabled(IAPfeature feature) {
		if (SystemAnalysis.isHeadless()) {
			// don't change these return values !!!
			// see
			switch (feature) {
				case REMOTE_EXECUTION:
					return getOptions().getBoolean("IAP", "grid_remote_execution", false);
				case SAVE_DEBUG_STACK:
					return getOptions().getBoolean("IAP", "debug_image_analysis_save_stack", false);
				case TOMCAT_AUTOMATIC_HSM_BACKUP:
					return getOptions().getBoolean("Watch-Service", "Automatic Copy to Archive//enabled", false)
							&& getOptions().getBoolean("ARCHIVE", "enabled", false);
				case DELETE_CLOUD_JOBS_AND_TEMP_DATA_UPON_CLOUD_START:
					return getOptions().getBoolean("IAP", "grid_delete_jobs_when_grid_node_becomes_active", false);
			}
		} else {
			// these may be changed for interactive applet version !!!
			switch (feature) {
				case REMOTE_EXECUTION:
					return getOptions().getBoolean("IAP", "grid_remote_execution", false);
				case SAVE_DEBUG_STACK:
					return getOptions().getBoolean("IAP", "debug_image_analysis_save_stack", false);
				case TOMCAT_AUTOMATIC_HSM_BACKUP:
					return getOptions().getBoolean("Watch-Service", "Automatic Copy to Archive//enabled", false)
							&& getOptions().getBoolean("ARCHIVE", "enabled", false);
				case DELETE_CLOUD_JOBS_AND_TEMP_DATA_UPON_CLOUD_START:
					return getOptions().getBoolean("IAP", "grid_delete_jobs_when_grid_node_becomes_active", false);
			}
		}
		return false;
	}
	
	private static SystemOptions getOptions() {
		return IAPoptions.getInstance();
	}
	
	public static String getHSMfolder() {
		boolean enabled = getOptions().getBoolean("ARCHIVE", "enabled", false);
		String folder = getOptions().getString("ARCHIVE", "folder", getHSMfolderDefault());
		if (enabled)
			return folder;
		else
			return null;
	}
	
	private static String getHSMfolderDefault() {
		try {
			if (new File("/media/nfs/hsm").exists())
				return "/media/nfs/hsm";
			else
				if (new File("/Volumes/HSM").exists())
					return "/Volumes/HSM";
				else
					if (new File("E:/austausch/HSM").exists())
						return "E:/austausch/HSM";
					else
						return ReleaseInfo.getAppSubdirFolder("local-iap-hsm");
		} catch (Exception e) {
			System.out.println("ERROR: HSM file system folder not accessible: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	private static IAPrunMode currentGuiMode = IAPrunMode.UNKNOWN;
	
	public static IAPrunMode getRunMode() {
		return currentGuiMode;
	}
	
	public static void setRunMode(IAPrunMode currentGuiMode) {
		IAPmain.currentGuiMode = currentGuiMode;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

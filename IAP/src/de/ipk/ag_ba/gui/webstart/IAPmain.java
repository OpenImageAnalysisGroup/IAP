/*******************************************************************************
 * Copyright (c) 2009 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk.ag_ba.gui.webstart;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ApplicationStatus;
import org.ErrorMessageProcessor;
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
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.graffiti.plugin.view.View;
import org.graffiti.session.Session;

import application.AnimateLogoIAP;
import bsh.Interpreter;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.SaveAsCsvDataProcessor;
import de.ipk.ag_ba.mongo.SaveInDatabaseDataProcessor;
import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk.ag_ba.postgresql.LTftpHandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.DBEsplashScreen;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.GravistoMainHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
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
		main(args, null);
	}
	
	public static void main(String[] args, String[] addons) {
		long startmaintime = System.currentTimeMillis();
		for (String info : IAPmain.getMainInfoLines())
			System.out.println(info);
		
		setRunMode(IAPrunMode.SWING_MAIN);
		
		System.out.println(SystemAnalysis.getCurrentTime() + ">Initialize IAP start... (run-mode: " + getRunMode() + ")");
		
		ProgressWindow progressWindow = new AnimateLogoIAP();
		progressWindow.show(true);
		
		String title = SystemOptions.getInstance().getString("IAP", "window_title",
				"IAP - The Integrated Analysis Platform") + "";
		
		SystemOptions.getInstance().getString("IAP", "Result File Type", "png");
		SystemOptions.getInstance().getString("IAP", "Preview File Type", "png");
		SystemOptions.getInstance().getInteger("SYSTEM", "Issue GC Memory Usage Threshold Percent", 60);
		SystemOptions.getInstance().getInteger("SYSTEM", "Reduce Workload Memory Usage Threshold Percent", 70);
		
		JFrame jf = new JFrame(title);
		IAPmain iap = new IAPmain(addons, progressWindow);
		jf.add("Center", iap.getContentPane());
		jf.pack();
		try {
			java.awt.Image img = AbstractIAPplugin.getIAPicon().getImage();
			jf.setIconImage(img);
			if (SystemAnalysis.isMacRunning()) {
				Interpreter i = new Interpreter();
				i.set("img", img);
				i.eval("com.apple.eawt.Application.getApplication().setDockIconImage(img)");
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		final WeakReference<JFrame> fr = new WeakReference<JFrame>(jf);
		jf.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
				if (progressWindow != null)
					progressWindow.hide();
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				JFrame j = fr.get();
				if (j != null) {
					
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent arg0) {
			}
		});
		if (startMaximized) {
			// jf.setExtendedState(jf.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			jf.setBounds(0, 0, dim.width, dim.height);
		}
		if (startMaximized) {
			// jf.setExtendedState(jf.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			jf.setBounds(0, 0, dim.width, dim.height);
			jf.setVisible(true);
		} else {
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			jf.setSize((int) (screenDim.width * 0.75), (int) (screenDim.height * 0.75));
			jf.setLocation((screenDim.width - jf.getWidth()) / 2,
					(screenDim.height - jf.getHeight()) / 2);
			jf.setVisible(true);
		}
		long endmaintime = System.currentTimeMillis();
		SystemOptions.getInstance().setInteger("IAP", "FX//Last Startup Time", (int) (endmaintime - startmaintime));
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SystemOptions.getInstance().addChangeListener("IAP", "window_title", new Runnable() {
			@Override
			public void run() {
				JFrame j = fr.get();
				if (j != null) {
					String newTitle = SystemOptions.getInstance().getString("IAP", "window_title", "IAP - The Integrated Analysis Platform");
					j.setTitle(newTitle + "");
				}
			}
		});
	}
	
	public IAPmain() {
		this(null, null);
	}
	
	public IAPmain(final String[] addons, ProgressWindow progressWindow) {
		if (getRunMode() == IAPrunMode.UNKNOWN)
			setRunMode(IAPrunMode.SWING_APPLET);
		if (getRunMode() == IAPrunMode.SWING_APPLET)
			ReleaseInfo.setRunningAsApplet(this);
		
		if (SystemOptions.getInstance().getBoolean("IAP", "Debug - System.Exit in case of error",
				IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE)) {
			ErrorMsg.setCustomErrorHandler(new ErrorMessageProcessor() {
				@Override
				public void reportError(Exception exception) {
					if (exception != null)
						exception.printStackTrace();
					IAPmain.errorCheck(exception.getMessage());
				}
				
				@Override
				public void reportError(String errorMessage) {
					if (errorMessage != null)
						System.err.println(errorMessage);
					IAPmain.errorCheck(errorMessage);
				}
			});
		}
		
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
		
		try {
			mainFrame1.setIconImage(IAPimages.getImage("img/vanted1_0.png"));
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		
		// mainFrame2 = new MainFrame(GravistoMainHelper.getNewPluginManager(), uiPrefs, statusPanel, true);
		
		setLayout(new TableLayout(new double[][] { { TableLayout.FILL }, { TableLayout.FILL } }));
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"", "");
		
		Thread t = new Thread() {
			
			@Override
			public void run() {
				// if (IAPmain.myClassKnown) {
				// System.out.println("Reload Classes, Problems may occur");
				// ErrorMsg.addErrorMessage("Reload Classes, Problems may occur");
				// }
				// IAPmain.myClassKnown = true;
				// System.out.println("Class Loader: " + InstanceLoader.getCurrentLoader().getClass().getCanonicalName());
				myAppletLoad(mainFrame1, myStatus, addons, progressWindow);
				// myAppletLoad(mainFrame2, myStatus);
			}
		};
		t.setName("Application Loader");
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		
		JComponent advancedNavigation = IAPgui.getMainGUIcontent(myStatus, false, null);
		add(advancedNavigation, "0,0");
		setVisible(true);
		validate();
		repaint();
		
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
		GravistoService.setProxy();
		// ResourceIOManager.registerIOHandler(LoadedVolumeHandler.getInstance());
		// ResourceIOManager.registerIOHandler(LoadedImageHandler.getInstance());
		ResourceIOManager.registerIOHandler(new LTftpHandler());
		for (MongoDB m : MongoDB.getMongos())
			ResourceIOManager.registerIOHandler(m.getHandler());
		
		// IIORegistry registry = IIORegistry.getDefaultInstance();
		// registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi());
		// registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi());
	}
	
	public void myAppletLoad(
			final MainFrame statusPanel,
			final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus,
			final String[] addons, ProgressWindow progressWindow) {
		String stS = "<font color=\"#9500C0\"><b>";
		String stE = "</b></font>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT = "IAP";
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
		final SplashScreenInterface splashScreen = progressWindow != null ? progressWindow : new SplashScreenInterface() {
			
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
				if (SystemOptions.getInstance().getBoolean("IAP", "Debug-Print-Plugin-Loading-Infos", false))
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
		URL r5 = cl.getResource("pluginsIAP.txt");
		
		URL rExcl = cl.getResource("plugins_exclude.txt");
		
		splashScreen.setText("Read plugin information...");
		
		final ArrayList<String> locations = new ArrayList<String>();
		try {
			locations.addAll(new TextFile(r1));
			locations.addAll(new TextFile(r2));
			locations.addAll(new TextFile(r3));
			locations.addAll(new TextFile(r4));
			locations.addAll(new TextFile(r5));
			locations.add("./MultimodalDataHandling.xml");
			// locations.add("./HIVE.xml");
			
			if (addons != null)
				for (String p : addons)
					if (p != null)
						locations.add("//" + p);
			
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
			if (splashScreen != null)
				splashScreen.setVisible(false);
			System.err.println("Internal error: Plugin Description files could not be loaded.");
			System.err.println("-- Program needs to be stopped");
			System.err.println("EXIT in 10 seconds.");
			BackgroundThreadDispatcher.runInSeparateThread(() -> {
				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					//
				}
				System.exit(1);
			}, "System.exit in 60 seconds");
			JOptionPane.showMessageDialog(null, "<html><h2>ERROR: Plugin-Description files could not be loaded</h2>"
					+ "Program execution can not continue.<br>" + "The application needs to be closed (and will be closed in 10 sec.).<br>" +
					"<br>" +
					"Create description files by executing the script createfilelist.cmd from the console (Linux/Mac)<br>" +
					"or by executing createfilelist.bat directly from within Eclipse (works only on Windows).<br>" +
					"These scripts are stored and available within the 'make' project." +
					"</html>");
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
		if (progressWindow == null)
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
					MainFrame.doCloseApplicationOnWindowClose = false;
					jf.setVisible(true);
					MainFrame.getInstance().getViewManager().viewChanged(null);
				}
			} else {
				if (jf != null) {
					// jf.setVisible(false);
					// jf.setVisible(true);
				}
			}
			if (jf != null) {
				final JFrame jff = jf;
				BackgroundTaskHelper.executeLaterOnSwingTask(50, new Runnable() {
					@Override
					public void run() {
						jff.toFront();
						jff.requestFocusInWindow();
						Session s = MainFrame.getInstance().getActiveSession();
						if (s != null) {
							View targetView = s.getActiveView();
							if (targetView != null)
								MainFrame.getInstance().setActiveSession(s, targetView);
						}
					}
				});
			}
		}
		return null;
	}
	
	private static WeakHashMap<String, NavigationImage> cachedImages = new WeakHashMap<String, NavigationImage>();
	
	public static synchronized NavigationImage loadIcon(String name) {
		NavigationImage ni = cachedImages.get(name);
		if (ni != null)
			return ni;
		NavigationImage res = new NavigationImage(
				Image.getBufferedImageFromImage(GravistoService.loadIcon(IAPmain.class, name).getImage()), name);
		cachedImages.put(name, res);
		return res;
	}
	
	public static boolean isSettingEnabled(IAPfeature feature) {
		if (SystemAnalysis.isHeadless()) {
			// don't change these return values !!!
			// see
			switch (feature) {
				case REMOTE_EXECUTION:
					return getOptions().getBoolean("IAP", "grid_remote_execution", true);
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
					return getOptions().getBoolean("IAP", "grid_remote_execution", true);
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
		return SystemOptions.getInstance();
	}
	
	private static IAPrunMode currentGuiMode = IAPrunMode.UNKNOWN;
	
	public static boolean imageIOdiskCacheEnabled = disableDiskCache();
	
	private static boolean startMaximized;
	
	public static IAPrunMode getRunMode() {
		return currentGuiMode;
	}
	
	private static boolean disableDiskCache() {
		ImageIO.setUseCache(false);
		return false;
	}
	
	public static void setRunMode(IAPrunMode currentGuiMode) {
		IAPmain.currentGuiMode = currentGuiMode;
	}
	
	public static ArrayList<String> getMainInfoLines() {
		ArrayList<String> res = new ArrayList<String>();
		String line = "****************************************************";
		int l = line.length();
		res.add(line);
		res.add(fillLen("**", l));
		res.add(fillLen("*IAP - Integrated Analysis Platform*", l));
		res.add(fillLen("*(V" + ReleaseInfo.IAP_VERSION_STRING + ")*", l));
		res.add(fillLen("**", l));
		res.add(fillLen("* - OPEN SOURCE - *", l));
		res.add(fillLen("**", l));
		res.add(fillLen("*--  Systems Biology Cloud Computing --*", l));
		res.add(fillLen("**", l));
		res.add(fillLen("*(c) 2015-2016 Dr. C. Klukas *", l));
		res.add(fillLen("*(c) 2010-2016 Research Group Image Analysis, IPK *", l));
		res.add(fillLen("**", l));
		res.add(line);
		res.add(fillLen("**", l));
		res.add(fillLenLA("*  Design and main development:  *", " ", l, 2));
		res.add(fillLenLA("*     Dr. Christian Klukas  *", " ", l, 2));
		res.add(fillLen("**", l));
		res.add(fillLenLA("*  Contribution to pipeline development:  *", " ", l, 2));
		res.add(fillLenLA("*     Jean-Michel Pape  *", " ", l, 2));
		res.add(fillLen("**", l));
		res.add(line);
		return res;
	}
	
	private static String fillLen(String string, int len) {
		while (string.length() < len) {
			string = string.substring(0, 1) + " " + string.substring(1);
			if (string.length() < len)
				string = string.substring(0, string.length() - 1) + " " + string.substring(string.length() - 1, string.length());
		}
		return string;
	}
	
	private static String fillLenLA(String string, String fill, int len, int retainLeft) {
		while (string.length() < len) {
			string = string.substring(0, string.length() - retainLeft) + fill + string.substring(string.length() - retainLeft, string.length());
		}
		return string;
	}
	
	public static void errorCheck(String errorMessage) {
		if (errorMessage != null && errorMessage.toUpperCase().startsWith("INFO:"))
			return;
		boolean errClose = SystemOptions.getInstance().getBoolean("IAP", "Debug - System.Exit in case of error (" + IAPmain.getRunMode() + ")",
				IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE);
		int errNum = SystemOptions.getInstance().getInteger(
				"IAP", "Debug - System.Exit return value in case of error", 1);
		if (errClose) {
			Thread.dumpStack();
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">INFO: System.exit because of reported error ('Settings > IAP > Debug - System.Exit in case of error' is enabled)");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				//
			}
			System.exit(errNum);
		}
	}
	
	public static void setStartMaximized(boolean startMaximized) {
		IAPmain.startMaximized = startMaximized;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

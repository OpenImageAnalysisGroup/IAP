/*******************************************************************************
 * Copyright (c) 2009 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk.ag_ba.gui.webstart;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RepaintManager;

import org.ApplicationStatus;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;
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
import de.ipk.ag_ba.image.operations.ImageConverter;
import de.ipk.ag_ba.mongo.MongoDB;
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
	
	public IAPmain() {
		ReleaseInfo.setRunningAsApplet(this);
		
		registerIOhandlers();
		
		GravistoMainHelper.setLookAndFeel();
		
		checkServerMode();
		
		// construct and open the editor's main frame
		GravistoPreferences prefs = GravistoPreferences.userNodeForPackage(IAPmain.class);
		
		GravistoPreferences uiPrefs = prefs.node("ui");
		uiPrefs.put("showPluginManagerMenuOptions", "false");
		uiPrefs.put("showPluginMenu", "false");
		JPanel statusPanel = new JPanel();
		
		mainFrame1 = new MainFrame(GravistoMainHelper.getNewPluginManager(), uiPrefs, statusPanel, true);
		// mainFrame2 = new MainFrame(GravistoMainHelper.getNewPluginManager(), uiPrefs, statusPanel, true);
		
		setLayout(new TableLayout(new double[][] { { TableLayout.FILL }, { TableLayout.FILL } }));
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
							"", "");
		JComponent advancedNavigation = IAPgui.getNavigation(myStatus, false);
		add(advancedNavigation, "0,0");
		validate();
		
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
	
	private void checkServerMode() {
		boolean serverMode = true;
		
		if (!serverMode)
			return;
		
		RepaintManager.setCurrentManager(new RemotingRepaintManager());
	}
	
	private void registerIOhandlers() {
		// ResourceIOManager.registerIOHandler(LoadedVolumeHandler.getInstance());
		// ResourceIOManager.registerIOHandler(LoadedImageHandler.getInstance());
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler handler : m.getHandlers())
				ResourceIOManager.registerIOHandler(handler);
	}
	
	public void myAppletLoad(MainFrame statusPanel, final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus) {
		String stS = "<font color=\"#9500C0\"><b>";
		String stE = "</b></font>";
		String name = stS + "VANTED" + stE + " - " + stS + "V" + stE + "isualization and " + stS + "A" + stE
							+ "nalysis of " + stS + "N" + stE + "e" + stS + "t" + stE + "works <br>containing " + stS + "E" + stE
							+ "xperimental " + stS + "D" + stE + "ata";
		DBEgravistoHelper.DBE_GRAVISTO_VERSION = "VANTED V" + DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE;
		DBEgravistoHelper.DBE_GRAVISTO_NAME = stS + "VANTED" + stE + "&nbsp;-&nbsp;" + stS + "V" + stE
							+ "isualization&nbsp;and&nbsp;" + stS + "A" + stE + "nalysis&nbsp;of&nbsp;" + stS + "N" + stE + "e" + stS
							+ "t" + stE + "works&nbsp;<br>containing&nbsp;" + stS + "E" + stE + "xperimental&nbsp;" + stS + "D" + stE
							+ "ata<br>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT = "VANTED";
		DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME = "Integrated Analysis Platform";
		
		DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME = "";
		
		// AttributeHelper.setMacOSsettings(DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT);
		
		JComponent result = new JPanel();
		result.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		
		String s = ""
							+ "<html><small><br>&nbsp;&nbsp;&nbsp;</small>Welcome to "
							+ name
							+ "!<br>"
							+ "<small>"
							+ "&nbsp;&nbsp;&nbsp;In the <b>Help menu</b> you find a <b>tutorial section</b> which quickly gives an overview on the various features of this application.<br>"
							+ "&nbsp;&nbsp;&nbsp;Furthermore you will find <b>[?] buttons</b> throughout the system which point directly to topics of interest.<br>"
							+ "&nbsp;&nbsp;&nbsp;If you experience problems or would like to suggest enhancements, feel free to use the <b>Send feedback command</b> in the Help menu!<br>&nbsp;";
		
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
		
		ArrayList<String> locations = new ArrayList<String>();
		try {
			locations.addAll(new TextFile(r1));
			locations.addAll(new TextFile(r2));
			locations.addAll(new TextFile(r3));
			locations.addAll(new TextFile(r4));
			// locations.add("./Vanted_AddOn_DBE2.xml");
			locations.add("./MultimodalDataHandling.xml");
			locations.add("./HIVE.xml");
			// locations.add("./VIMPED.xml");
			locations.remove("");
			ArrayList<String> locations_exclude = new ArrayList<String>();
			locations_exclude.addAll(new TextFile(rExcl));
			
			for (String ss : locations) {
				// System.out.println(ss);
				if (ss.indexOf("addon") >= 0) {
					locations_exclude.add(ss);
					System.out.println("Disable plugin " + ss);
				}
				
			}
			
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
		
		splashScreen.setText("Load plugins...");
		try {
			GravistoMainHelper.loadPlugins(statusPanel.getPluginManager(), locations, splashScreen);
		} catch (PluginManagerException pme) {
			ErrorMsg.addErrorMessage(pme.getLocalizedMessage());
		}
		
		ExperimentDataProcessingManager.addExperimentDataProcessor(new SaveInDatabaseDataProcessor());
		
		splashScreen.setText("Initialize GUI...");
		splashScreen.setVisible(false);
		splashScreen.setInitialisationFinished();
		ErrorMsg.setAppLoadingCompleted(ApplicationStatus.PROGRAM_LOADING_FINISHED);
	}
	
	public static JComponent showVANTED(boolean inline) {
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
	
	public static NavigationImage loadIcon(String name) {
		return new NavigationImage(
							ImageConverter.getBufferedImageFromImage(GravistoService.loadIcon(IAPmain.class, name).getImage()));
	}
	
	public static boolean isSettingEnabled(IAPfeature feature) {
		switch (feature) {
			case REMOTE_EXECUTION:
				return true;
		}
		return false;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

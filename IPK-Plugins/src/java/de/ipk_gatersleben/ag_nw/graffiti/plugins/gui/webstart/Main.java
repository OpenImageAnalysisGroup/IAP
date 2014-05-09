/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.ApplicationStatus;
import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.SystemInfo;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.SplashScreenInterface;

import apple.dts.samplecode.osxadapter.OSXAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.MenuItemInfoDialog;

/**
 * Contains the graffiti editor.
 * 
 * @version $Revision: 1.9 $
 */
public class Main {
	// ~ Static fields/initializers =============================================
	private static final long serialVersionUID = -8853617468707668012L;
	
	// ~ Instance fields ========================================================
	
	/** The editor's main frame. */
	
	// ~ Constructors ===========================================================
	
	public Main(final boolean showMainFrame, String applicationName, String[] args, String addon) {
		this(showMainFrame, applicationName, args, new String[] { addon });
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
	
	public Main(final boolean showMainFrame, String applicationName, String[] args, String[] addon) {
		this(showMainFrame, applicationName, args, addon, new DBEsplashScreen(applicationName,
				"", new RunnableWithSplashScreenReference() {
					private SplashScreenInterface ss;
					
					@Override
					public void run() {
						if (showMainFrame) {
							ClassLoader cl = this.getClass().getClassLoader();
							String path = this.getClass().getPackage().getName()
									.replace('.', '/');
							ImageIcon icon = new ImageIcon(cl.getResource(path
									+ "/vanted_logo.png"));
							final MainFrame mainFrame = MainFrame.getInstance();
							if (icon != null && mainFrame != null)
								mainFrame.setIconImage(icon.getImage());
							if (mainFrame == null)
								System.err.println("Internal Error: MainFrame is NULL");
							else {
								Thread t = new Thread(new Runnable() {
									@Override
									public void run() {
										long waitTime = 0;
										long start = System.currentTimeMillis();
										do {
											if (ErrorMsg.getAppLoadingStatus() == ApplicationStatus.ADDONS_LOADED)
												break;
											try {
												Thread.sleep(50);
											} catch (InterruptedException e) {
											}
											waitTime = System.currentTimeMillis() - start;
										} while (waitTime < 2000);
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												ss.setVisible(false);
												mainFrame.setVisible(true);
											}
										});
									}
								}, "wait for add-on initialization");
								t.start();
							}
						}
					}
					
					@Override
					public void setSplashscreenInfo(SplashScreenInterface ss) {
						this.ss = ss;
					}
				}) {});
	}
	
	/**
	 * Constructs a new instance of the editor.
	 */
	public Main(final boolean showMainFrame, String applicationName, String[] args, String[] addon, SplashScreenInterface splashScreen) {
		
		setupLogger();
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName()
				.replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path
				+ "/vanted_logo.png"));
		
		if (splashScreen != null && (splashScreen instanceof DBEsplashScreen)) {
			((DBEsplashScreen) splashScreen).setIconImage(icon.getImage());
		}
		
		splashScreen.setVisible(showMainFrame);
		
		GravistoMainHelper.createApplicationSettingsFolder(splashScreen);
		
		if (!showMainFrame && !(new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted")).exists() &&
				!(new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected")).exists()) { // command line version automatically rejects
			// kegg
			try {
				new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").createNewFile();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		if (!(new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted")).exists() &&
				!(new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected")).exists()) {
			
			ReleaseInfo.setIsFirstRun(true);
			
			splashScreen.setVisible(false);
			splashScreen.setText("Request KEGG License Status");
			int result = askForEnablingKEGG();
			if (result == JOptionPane.YES_OPTION) {
				try {
					new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").createNewFile();
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			if (result == JOptionPane.NO_OPTION) {
				try {
					new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").createNewFile();
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			if (result == JOptionPane.CANCEL_OPTION) {
				JOptionPane.showMessageDialog(
						null,
						"Startup of VANTED is aborted.", "VANTED Program Features Initialization",
						JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
			}
			splashScreen.setVisible(true);
		}
		
		GravistoMainHelper.initApplicationExt(args, splashScreen, cl, null, addon);
	}
	
	public static boolean doEnableKEGGaskUser() {
		return askForEnablingKEGG() == JOptionPane.YES_OPTION;
	}
	
	private static int askForEnablingKEGG() {
		JOptionPane
				.showMessageDialog(
						null,
						"<html><h3>KEGG License Status Evaluation</h3>"
								+ "While VANTED is available as a academic research tool at no cost to commercial and non-commercial users, for using<br>"
								+
								"KEGG related functions, it is necessary for all users to adhere to the KEGG license.<br>"
								+
								"For using VANTED you need also be aware of information about licenses and conditions for<br>"
								+
								"usage, listed at the program info dialog and the VANTED website (http://vanted.ipk-gatersleben.de).<br><br>"
								+
								"VANTED does not distribute information from KEGG but contains functionality for the online-access to <br>"
								+
								"information from KEGG website.<br><br>"
								+
								"<b>Before these functions are available to you, you should  carefully read the following license information<br>"
								+
								"and decide if it is legit for you to use the KEGG related program functions. If you choose not to use the KEGG functions<br>"
								+
								"all other features of this application are still available and fully working.",
						"VANTED Program Features Initialization",
						JOptionPane.INFORMATION_MESSAGE);
		
		JOptionPane.showMessageDialog(
				null,
				"<html><h3>KEGG License Status Evaluation</h3>"
						+ MenuItemInfoDialog.getKEGGlibText(), "VANTED Program Features Initialization", JOptionPane.INFORMATION_MESSAGE);
		
		int result = JOptionPane.showConfirmDialog(
				null,
				"<html><h3>Enable KEGG functions?", "VANTED Program Features Initialization",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return result;
	}
	
	/**
	 * The editor's main method.
	 * 
	 * @param args
	 *           the command line arguments.
	 */
	public static void main(String[] args) {
		startVanted(args, null);
	}
	
	public static void startVantedExt(String[] args, String[] developerAddon) {
		System.out.println("Welcome! About to start the application...");
		
		if (SystemInfo.isMac()) {
			try {
				OSXAdapter.setFileHandler(new GravistoService(), GravistoService.class.getDeclaredMethod("loadFile", new Class[] { String.class }));
			} catch (Exception err) {
				ErrorMsg.addErrorMessage(err);
			}
		}
		
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		// System.setProperty("java.net.useSystemProxies","true");
		LoggingProxy ps = new LoggingProxy(ProxySelector.getDefault());
		ProxySelector.setDefault(ps);
		ReleaseInfo.setRunningReleaseStatus(Release.RELEASE_PUBLIC);
		
		GravistoMainHelper.setLookAndFeel();
		
		String stS = "<font color=\"#9500C0\"><b>";
		String stE = "</b></font>";
		String name = stS + "VANTED" + stE + " - "
				+ stS + "V" + stE + "isualization and " + stS + "A" + stE + "nalysis of " + stS + "N" + stE + "e" + stS + "t" + stE
				+ "works <br>containing " + stS + "E" + stE + "xperimental " + stS + "D" + stE + "ata";
		JComponent result = new JPanel();
		result.setLayout(TableLayout.getLayout(TableLayoutConstants.FILL, TableLayoutConstants.FILL));
		
		String s = "" +
				"<html><small><br>&nbsp;&nbsp;&nbsp;</small>Welcome to " + name.replaceAll("<br>", "") + "!<br>" +
				"<small>" +
				"&nbsp;&nbsp;&nbsp;In the <b>Help menu</b> you find a <b>tutorial section</b> which " +
				"quickly gives an overview on the various features of this application.<br>" +
				"&nbsp;&nbsp;&nbsp;Furthermore you will find <b>[?] buttons</b> throughout the " +
				"system which point directly to topics of interest.<br>" +
				"&nbsp;&nbsp;&nbsp;If you experience problems or would like to suggest enhancements, " +
				"feel free to use the <b>Send feedback command</b> in the Help menu!<br>&nbsp;";
		
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
			s = "" +
					"<html><small>&nbsp;&nbsp;&nbsp;</small>Welcome to " + name.replaceAll("<br>", "") + "!" +
					"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color='gray'>" + DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE_SUBVERSION + "</small>";// <br>"
		// +
		// "<small>" +
		// "&nbsp;&nbsp;&nbsp;The help functions may be enabled from the side panel <b>Help/Settings</b>.<br>" +
		// "&nbsp;&nbsp;&nbsp;If you experience problems or would like to suggest enhancements, " +
		// "feel free to use the <b>Send feedback command</b> in the Help menu!";
		
		ReleaseInfo.setHelpIntroductionText(s);
		
		DBEgravistoHelper.DBE_GRAVISTO_VERSION = "VANTED V" + DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE;
		DBEgravistoHelper.DBE_GRAVISTO_NAME = stS + "VANTED" + stE + "&nbsp;-&nbsp;"
				+ stS + "V" + stE + "isualization&nbsp;and&nbsp;" + stS + "A" + stE + "nalysis&nbsp;of&nbsp;" + stS + "N" + stE + "e" + stS + "t" + stE
				+ "works&nbsp;<br>containing&nbsp;" + stS + "E" + stE + "xperimental&nbsp;" + stS + "D" + stE + "ata<br>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT = "VANTED";
		DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME = "";
		
		AttributeHelper.setMacOSsettings(DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT);
		
		new Main(true,
				DBEgravistoHelper.DBE_GRAVISTO_VERSION, args, developerAddon);
	}
	
	public static void startVanted(String[] args, String adn) {
		startVantedExt(args, new String[] { adn });
		
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

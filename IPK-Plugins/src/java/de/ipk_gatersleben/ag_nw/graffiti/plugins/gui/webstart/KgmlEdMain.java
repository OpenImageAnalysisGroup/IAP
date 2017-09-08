/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ApplicationStatus;
import org.AttributeHelper;
import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.SplashScreenInterface;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.MenuItemInfoDialog;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

/**
 * Contains the graffiti editor.
 * 
 * @version $Revision: 1.3 $
 */
public class KgmlEdMain {
	// ~ Static fields/initializers =============================================
	
	// ~ Instance fields ========================================================
	
	/**
	 * Constructs a new instance of the editor.
	 */
	public KgmlEdMain(final boolean showMainFrame, String applicationName, String[] args) {
		// URL config,
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		SplashScreenInterface splashScreen = new DBEsplashScreen(applicationName,
				"", new Runnable() {
					@Override
					public void run() {
						if (showMainFrame) {
							ClassLoader cl = this.getClass().getClassLoader();
							String path = this.getClass().getPackage().getName()
									.replace('.', '/');
							ImageIcon icon = new ImageIcon(cl.getResource(path
									+ "/ipklogo16x16_5.png"));
							final MainFrame mainFrame = MainFrame.getInstance();
							mainFrame.setIconImage(icon.getImage());
							
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
									SplashScreenInterface ss = (SplashScreenInterface) tso.getParam(0, null);
									ss.setVisible(false);
									mainFrame.setVisible(true);
								}
							}, "wait for add-on initialization");
							t.start();
						}
						
					}
				});
		tso.setParam(0, splashScreen);
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName()
				.replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path
				+ "/ipklogo16x16_5.png"));
		((DBEsplashScreen) splashScreen).setIconImage(icon.getImage());
		
		splashScreen.setVisible(true);
		GravistoMainHelper.createApplicationSettingsFolder(splashScreen);
		if (!(new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted")).exists() &&
				!(new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected")).exists()) {
			
			ReleaseInfo.setIsFirstRun(true);
			
			splashScreen.setVisible(false);
			splashScreen.setText("Request KEGG License Status");
			JOptionPane
					.showMessageDialog(
							null,
							"<html><h3>KEGG License Status Evaluation</h3>"
									+ "While "
									+ DBEgravistoHelper.DBE_GRAVISTO_VERSION
									+ " is available as a academic research tool at no cost to commercial and non-commercial users, for using<br>"
									+
									"KEGG related functions, it is necessary for all users to adhere to the KEGG license.<br>"
									+
									"For using "
									+ DBEgravistoHelper.DBE_GRAVISTO_VERSION
									+ " you need also be aware of information about licenses and conditions for<br>"
									+
									"usage, listed at the program info dialog and the "
									+ DBEgravistoHelper.DBE_GRAVISTO_VERSION
									+ " website ("
									+ ReleaseInfo.getAppWebURL()
									+ ").<br><br>"
									+
									DBEgravistoHelper.DBE_GRAVISTO_VERSION
									+ " does not distribute information from KEGG but contains functionality for the online-access to  information from KEGG wesite.<br><br>"
									+
									"<b>Before these functions are available to you, you should  carefully read the following license information<br>"
									+
									"and decide if it is legit for you to use the KEGG related program functions. If you choose not to use the KEGG functions<br>"
									+
									"all other features of this application are still available and fully working.",
							DBEgravistoHelper.DBE_GRAVISTO_VERSION + " Program Features Initialization",
							JOptionPane.INFORMATION_MESSAGE);
			
			JOptionPane.showMessageDialog(
					null,
					"<html><h3>KEGG License Status Evaluation</h3>"
							+ MenuItemInfoDialog.getKEGGlibText(),
					DBEgravistoHelper.DBE_GRAVISTO_VERSION + " Program Features Initialization",
					JOptionPane.INFORMATION_MESSAGE);
			
			int result = JOptionPane.showConfirmDialog(
					null,
					"<html><h3>Enable KEGG functions?", DBEgravistoHelper.DBE_GRAVISTO_VERSION + " Program Features Initialization",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
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
						"Startup aborted.", DBEgravistoHelper.DBE_GRAVISTO_VERSION + " Program Features Initialization",
						JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
			}
			splashScreen.setVisible(true);
		}
		
		GravistoMainHelper.initApplicationExt(args, splashScreen, cl, null, null, true);
		
	}
	
	// ~ Methods ================================================================
	
	/**
	 * The editor's main method.
	 * 
	 * @param args
	 *           the command line arguments.
	 */
	public static void main(String[] args) {
		
		GravistoMainHelper.setLookAndFeel();
		
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		// System.setProperty("java.net.useSystemProxies","true");
		ReleaseInfo.setRunningReleaseStatus(Release.KGML_EDITOR);
		
		String stS = "<font><b>"; // "<font color=\"#9500C0\"><b>";
		String stE = "</b></font>";
		String name = stS + "KGML Pathway Editor" + stE + " - "
				+ "Edit, process and visualize KGML" + DBEgravistoHelper.kgmlFileVersionHint + " pathway files!";
		JComponent result = new JPanel();
		result.setLayout(TableLayout.getLayout(TableLayoutConstants.FILL, TableLayoutConstants.FILL));
		
		String s = ""
				+
				"<html><small><br>&nbsp;&nbsp;&nbsp;</small>Welcome to "
				+ name
				+ "<br>"
				+
				"<small>"
				+
				"&nbsp;&nbsp;&nbsp;If you experience problems or would like to suggest enhancements, feel free to use the <b>Send feedback command</b> in the Help menu!<br>&nbsp;";
		
		ReleaseInfo.setHelpIntroductionText(s);
		
		DBEgravistoHelper.DBE_GRAVISTO_VERSION = "KGML Pathway Editor V" + DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE;
		DBEgravistoHelper.DBE_GRAVISTO_NAME = stS + "KGML Pathway Editor" + stE + " - "
				+ "Edit, process, <br>and visualize KGML" + DBEgravistoHelper.kgmlFileVersionHint + " Pathway files!<br>";
		DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT = "KGML Pathway Editor";
		DBEgravistoHelper.DBE_INFORMATIONSYSTEM_NAME = "";
		
		AttributeHelper.setMacOSsettings(DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT);
		
		new KgmlEdMain(true, DBEgravistoHelper.DBE_GRAVISTO_VERSION, args);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

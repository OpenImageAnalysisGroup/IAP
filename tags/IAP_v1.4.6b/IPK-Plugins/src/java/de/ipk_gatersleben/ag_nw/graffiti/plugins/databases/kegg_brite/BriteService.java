/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_brite;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.HelperClass;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MemoryHog;
import org.graffiti.editor.MessageType;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class BriteService extends MemoryHog
		implements
		BackgroundTaskStatusProvider,
		FileDownloadStatusInformationProvider, HelperClass {
	
	private static boolean read_brite_DB_txt = false;
	private static HashMap<String, String> mapNumber2groupInfo = new HashMap<String, String>();
	private static HashMap<String, ArrayList<String>> ko2nameInfo = new HashMap<String, ArrayList<String>>();
	
	private static String status1;
	private static String status2;
	private static int statusVal = -1;
	private static String brite_version;
	
	@Override
	public synchronized void finishedNewDownload() {
		read_brite_DB_txt = false;
		mapNumber2groupInfo = new HashMap<String, String>();
		status1 = null;
		status2 = null;
	}
	
	private static synchronized void initService(boolean initInBackground) {
		GravistoService.addKnownMemoryHog(new BriteService());
		if (initInBackground) {
			if (!read_brite_DB_txt) {
				read_brite_DB_txt = true;
				final BriteService cs = new BriteService();
				BackgroundTaskHelper bth = new BackgroundTaskHelper(
						new Runnable() {
							@Override
							public void run() {
								statusVal = -1;
								read_brite_DB_txt = true;
								readBriteDB();
								statusVal = 100;
							}
						},
						cs,
						"KEGG BRITE Database",
						"KEGG BRITE Database Service",
						true, false);
				bth.startWork(MainFrame.getInstance());
			}
		} else {
			if (!read_brite_DB_txt) {
				read_brite_DB_txt = true;
				readBriteDB();
			}
		}
		noteRequest();
	}
	
	public static String[] getPathwayGroupFromMapNumber(String mapNumber) {
		noteRequest();
		initService(false);
		String s = mapNumber2groupInfo.get(StringManipulationTools.getNumbersFromString(mapNumber));
		if (s != null && s.length() > 0) {
			String[] res = s.split(";");
			for (int i = 0; i < res.length; i++) {
				res[i] = res[i].trim();
			}
			return res;
		}
		return new String[] { "group unknown", "not in BRITE" };
	}
	
	/**
	 * Reads the BRITE file. All methods depending on info from that file
	 * should call <code>initService</code>, first. To ensure that this
	 * service is available.
	 */
	private static void readBriteDB() {
		mapNumber2groupInfo.clear();
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
			return;
		status1 = "Analyse BRITE information...";
		File notRenamedBriteFile = new File(ReleaseInfo.getAppFolderWithFinalSep() + "ko00001.keg");
		if (notRenamedBriteFile.exists()) {
			notRenamedBriteFile.renameTo(new File(ReleaseInfo.getAppFolderWithFinalSep() + "brite"));
		}
		BufferedReader input = getFileReader("brite");
		if (input == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: 'brite'-file could not be loaded!");
		} else {
			String line = null;
			try {
				String currentA = "";
				String currentB = "";
				while ((line = input.readLine()) != null) {
					// System.out.println(line);
					line = line.trim();
					if (line.startsWith("#Last updated")) {
						brite_version = line.substring("#".length());
					}
					if (line.startsWith("A") || line.startsWith("B") || line.startsWith("C") || line.startsWith("D")) {
						if (line.startsWith("C")) {
							String currentCnumber = line.substring("C".length());
							currentCnumber = currentCnumber.trim();
							currentCnumber = currentCnumber.substring(0, currentCnumber.indexOf(" "));
							mapNumber2groupInfo.put(currentCnumber, currentA + ";" + currentB);
							status1 = "Analyse BRITE information (" + mapNumber2groupInfo.size() + ")";
						} else {
							if (line.startsWith("A")) {
								currentA = line.substring("A".length()).trim();
								currentA = StringManipulationTools.stringReplace(currentA, "<b>", "");
								currentA = StringManipulationTools.stringReplace(currentA, "</b>", "");
							} else
								if (line.startsWith("B")) {
									currentB = line.substring("B".length()).trim();
									currentB = StringManipulationTools.stringReplace(currentB, "<b>", "");
									currentB = StringManipulationTools.stringReplace(currentB, "</b>", "");
								} else
									if (line.startsWith("D")) {
										String currentDnumber = line.substring("D".length());
										currentDnumber = currentDnumber.trim();
										currentDnumber = currentDnumber.substring(0, currentDnumber.indexOf(" "));
										String currentDDnumber = line.substring("D".length());
										currentDDnumber = currentDDnumber.trim();
										if (currentDDnumber.indexOf(" ") >= 0)
											currentDDnumber = currentDDnumber.substring(currentDDnumber.indexOf(" ")).trim();
										currentDDnumber = StringManipulationTools.stringReplace(currentDDnumber, "[", "");
										currentDDnumber = StringManipulationTools.stringReplace(currentDDnumber, "]", "");
										
										ArrayList<String> ids = new ArrayList<String>();
										for (String s : currentDDnumber.split(";"))
											ids.add(s.trim());
										ko2nameInfo.put(currentDnumber, ids);
									}
						}
					}
				}
				status1 = "KEGG BRITE information analysed (" + mapNumber2groupInfo.size() + ")";
			} catch (IOException e) {
				status2 = "Error in reading BRITE info!";
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			} finally {
				if (input != null)
					try {
						input.close();
					} catch (IOException e1) {
						ErrorMsg.addErrorMessage(e1.getLocalizedMessage());
					}
			}
		}
	}
	
	public static File getFile(String fileName) {
		return new File(ReleaseInfo.getAppFolderWithFinalSep() + fileName);
	}
	
	public static BufferedReader getFileReader(String fileName) {
		ClassLoader cl = BriteService.class.getClassLoader();
		try {
			return new BufferedReader(new FileReader(ReleaseInfo.getAppFolderWithFinalSep() + fileName));
		} catch (Exception e) {
			try {
				return new BufferedReader(new InputStreamReader(cl.getResourceAsStream(fileName)));
			} catch (Exception e2) {
				MainFrame.showMessage("<html><b>Click Help/Database Status</b> for help on downloading BRITE data", MessageType.INFO);
				return null;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		return statusVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return getCurrentStatusValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	@Override
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	@Override
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseStop()
	 */
	@Override
	public void pleaseStop() {
		// pleaseStop = true;
	}
	
	@Override
	public boolean wantsToStop() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	@Override
	public void pleaseContinueRun() {
		// empty
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		statusVal = value;
	}
	
	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public JComponent getStatusPane(boolean showEmpty) {
		noteRequest();
		
		FolderPanel res = new FolderPanel("<html>" +
				"KEGG BRITE Database<br><small>" +
				"(contains information on functional hierarchies and binary relationships)");
		res.setFrameColor(Color.LIGHT_GRAY, null, 1, 5);
		
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS)) {
			res.addGuiComponentRow(null,
					new JLabel("<html>" +
							"KEGG features are disabled.<br>Use side panel Help/Settings to enable access. Then restart the program."),
					false);
			res.layoutRows();
			return res;
		}
		
		if (!showEmpty)
			initService(false);
		
		int b = 5; // normal border
		int bB = 1; // border around action buttons
		
		boolean externalAvailable = read_brite_DB_txt && mapNumber2groupInfo != null && mapNumber2groupInfo.size() > 0;
		String status2 = "";
		
		if (externalAvailable)
			status2 = "<html><b>Database is online</b>";
		else
			status2 = "<html>Database file not available";
		
		String modifiedTime = GravistoService.getFileModificationDateAndTime(getFile("brite"),
				"unknown version (file not found)");
		File f = getFile("brite");
		
		if (externalAvailable) {
			if (f.exists())
				status2 += "<br>&nbsp;&nbsp;database download: " + modifiedTime;
			else
				status2 += "<br>&nbsp;&nbsp;" + modifiedTime;
			if (brite_version != null && brite_version.length() > 0)
				status2 += "<br>&nbsp;&nbsp;version: " + brite_version;
			status2 += "<br>&nbsp;&nbsp;pathways: " + mapNumber2groupInfo.size();
			status2 += "<br>&nbsp;&nbsp;orthologs: " + ko2nameInfo.size();
		}
		
		if (showEmpty)
			status2 = "<html><b>Bringing database online...</b><br>Please wait a short moment.";
		
		res.addGuiComponentRow(
				new JLabel("<html>" +
						"Downloaded Files:&nbsp;"),
				FolderPanel.getBorderedComponent(
						new JLabel(status2), b, b, b, b),
				false);
		
		ArrayList<JComponent> actionButtons = new ArrayList<JComponent>();
		if (!showEmpty) {
			actionButtons.add(getWebsiteButton());
			actionButtons.add(getLicenseButton());
			actionButtons.add(getDownloadButton());
		}
		pretifyButtons(actionButtons);
		
		res.addGuiComponentRow(
				new JLabel("<html>" +
						"Visit Website(s)"),
				TableLayout.getMultiSplit(actionButtons, TableLayout.PREFERRED, bB, bB, bB, bB),
				false);
		
		res.layoutRows();
		return res;
	}
	
	private JComponent getDownloadButton() {
		String status = "Download";
		return GUIhelper.getWebsiteDownloadButton(
				status,
				"http://www.genome.jp/kegg-bin/get_htext?org_name=hsa&query=&htext=ko00001.keg",
				ReleaseInfo.getAppFolderWithFinalSep(),
				"<html>" +
						"The following URL and the target folder will be automatically opened in a few seconds:<br><br>" +
						"<code><b>" +
						"http://www.genome.jp/kegg-bin/get_htext?org_name=hsa&query=&htext=ko00001.keg" +
						// "http://www.genome.jp/kegg/kegg6.html" +
						"</b></code><br><br>" +
						"Please (re)evaluate your KEGG license status, before proceeding with the following steps:<br>" +
						"<ol>" +
						"	<li>Click onto the link: &quot;Download htext/&quot;" +
						"	<li>Then save the file ko00001.keg." +
						"</ol>" +
						"After downloading and saving the file, please move the file to the following location:<br><br>" +
						"<code><b>" + ReleaseInfo.getAppFolder() + "</b></code><br><br>" +
						"After closing and re-opening this application, parts of the KEGG BRITE database will be<br>" +
						"available to the system.",
				new String[] {
				"http://www.genome.jp/kegg-bin/download_htext?htext=ko00001.keg&format=htext|brite"
				},
				"Manual download instructions (automatic download failure)",
				this);
	}
	
	private JComponent getLicenseButton() {
		return GUIhelper.getWebsiteButton("License",
				"http://www.genome.jp/kegg/legal.html",
				// "http://www.genome.jp/kegg/kegg6.html",
				null, null, null);
	}
	
	private JComponent getWebsiteButton() {
		return GUIhelper.getWebsiteButton("Website", "http://www.genome.jp/kegg/brite.html", null, null, null);
	}
	
	private void pretifyButtons(ArrayList<JComponent> actionButtons) {
		for (JComponent jc : actionButtons) {
			((JButton) jc).setBackground(Color.white);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.editor.MemoryHog#freeMemory()
	 */
	@Override
	public synchronized void freeMemory() {
		if (doFreeMemory())
			finishedNewDownload();
	}
	
	public static ArrayList<String> getKoNamesFromKO(String kid) {
		return ko2nameInfo.get(kid);
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
	
	public static boolean isDatabaseAvailable() {
		initService(false);
		return read_brite_DB_txt;
	}
}
/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_reaction;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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
import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class ReactionService
					implements
					BackgroundTaskStatusProvider,
					FileDownloadStatusInformationProvider, HelperClass {
	private static boolean read_reaction_DB_txt = false;
	
	private static String reactionDBrelease = "unknown";
	
	// ligand.txt
	private static String relTagDB = "Release ";
	
	private static HashMap<String, ReactionEntry> reactionIds = new HashMap<String, ReactionEntry>();
	private static HashMap<String, HashSet<ReactionEntry>> reactionSubstrateNamesEntries = new HashMap<String, HashSet<ReactionEntry>>();
	private static HashMap<String, HashSet<ReactionEntry>> reactionProductNamesEntries = new HashMap<String, HashSet<ReactionEntry>>();
	
	private static String status1;
	private static String status2;
	private static int statusVal = -1;
	
	public synchronized void finishedNewDownload() {
		read_reaction_DB_txt = false;
		reactionDBrelease = "unknown";
		reactionIds = new HashMap<String, ReactionEntry>();
		reactionSubstrateNamesEntries = new HashMap<String, HashSet<ReactionEntry>>();
		reactionProductNamesEntries = new HashMap<String, HashSet<ReactionEntry>>();
		status1 = null;
		status2 = null;
		statusVal = -1;
	}
	
	private static synchronized void initService(boolean initInBackground) {
		if (initInBackground) {
			if (!read_reaction_DB_txt) {
				read_reaction_DB_txt = true;
				final ReactionService cs = new ReactionService();
				BackgroundTaskHelper bth = new BackgroundTaskHelper(
									new Runnable() {
										public void run() {
											statusVal = -1;
											read_reaction_DB_txt = true;
											readLigantTXTforReleaseInfo();
											readReactionDB();
											statusVal = 100;
										}
									},
									cs,
									"Reaction Database",
									"Reaction Database Service",
									true, false);
				bth.startWork(MainFrame.getInstance());
			}
		} else {
			if (!read_reaction_DB_txt) {
				read_reaction_DB_txt = true;
				readLigantTXTforReleaseInfo();
				readReactionDB();
			}
		}
	}
	
	/**
	 * Open and read ligand.txt and look for the release info.
	 */
	private static void readLigantTXTforReleaseInfo() {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
			return;
		status1 = "Read Version Info...";
		BufferedReader input = getFileReader("ligand.txt");
		String line = null;
		try {
			while ((line = input.readLine()) != null) {
				// System.out.println(line);
				if (line.contains(relTagDB)) {
					reactionDBrelease = line.trim();
					break;
				}
			}
			status2 = "KEGG LIGAND - " + reactionDBrelease;
		} catch (IOException e) {
			status2 = "Error reading version info!";
			ErrorMsg.addErrorMessage(e);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e1) {
					ErrorMsg.addErrorMessage(e1);
				}
		}
	}
	
	/**
	 * Reads the file compound. All methods depending on info from that file
	 * should call <code>initService</code>, first. To ensure that this
	 * service is available.
	 */
	private static void readReactionDB() {
		reactionIds.clear();
		reactionSubstrateNamesEntries.clear();
		reactionProductNamesEntries.clear();
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
			return;
		status1 = "Analyse reacton information...";
		BufferedReader input = getFileReader("reaction");
		String line = null;
		try {
			while ((line = input.readLine()) != null) {
				line = line.trim();
				if (ReactionEntry.isValidReactionStart(line)) {
					boolean endTagFound = false;
					ReactionEntry reactionEntry = new ReactionEntry();
					reactionEntry.processInputLine(line, new ArrayList<String>()); // R00002 => ID
					
					String lastP1 = "";
					HashMap<String, ArrayList<String>> id2lines = new HashMap<String, ArrayList<String>>();
					do {
						String entryline = input.readLine();
						endTagFound = entryline.startsWith(ReactionEntry.endTag_exists);
						if (!endTagFound) {
							if (entryline.length() >= "ENTRY       ".length()) {
								String p1 = entryline.substring(0, "ENTRY       ".length()).trim();
								String p2 = entryline.substring("ENTRY       ".length()).trim();
								if (p1.length() <= 0) {
									p1 = lastP1;
								}
								if (!id2lines.containsKey(p1))
									id2lines.put(p1, new ArrayList<String>());
								id2lines.get(p1).add(p2);
								lastP1 = p1;
							}
						}
					} while (!endTagFound);
					// ********* PROCESS LINES FOR THIS ENTRY
					for (String entryId : id2lines.keySet())
						reactionEntry.processInputLine(entryId, id2lines.get(entryId));
					
					if (reactionEntry.isValid()) {
						status1 = "Analyse reaction information (" + reactionIds.size() + ")";
						reactionIds.put(reactionEntry.getID().toUpperCase(), reactionEntry);
						for (String reacprod : reactionEntry.getProductNames()) {
							reacprod = reacprod.toUpperCase();
							if (!reactionProductNamesEntries.containsKey(reacprod))
								;
							reactionProductNamesEntries.put(reacprod,
												new HashSet<ReactionEntry>());
							reactionProductNamesEntries.get(reacprod).add(reactionEntry);
						}
						for (String reacsub : reactionEntry.getSubstrateNames()) {
							reacsub = reacsub.toUpperCase();
							if (!reactionSubstrateNamesEntries.containsKey(reacsub))
								;
							reactionSubstrateNamesEntries.put(reacsub,
												new HashSet<ReactionEntry>());
							reactionSubstrateNamesEntries.get(reacsub).add(reactionEntry);
						}
					}
				}
			}
			status1 = "Reaction information analysed (" + reactionIds.size() + ")";
		} catch (IOException e) {
			status2 = "Error in reading reaction info!";
			ErrorMsg.addErrorMessage(e);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e1) {
					ErrorMsg.addErrorMessage(e1);
				}
		}
	}
	
	private static BufferedReader getFileReader(String fileName) {
		ClassLoader cl = ReactionService.class.getClassLoader();
		try {
			return new BufferedReader(new InputStreamReader(cl.getResourceAsStream(fileName))); // path + "/" +
		} catch (Exception e) {
			try {
				return new BufferedReader(new FileReader(ReleaseInfo.getAppFolderWithFinalSep() + fileName));
			} catch (Exception e2) {
				ErrorMsg.addErrorMessage("File could not be located: " + fileName);
				return null;
			}
		}
	}
	
	public static ReactionEntry getReactionFromId(String reactionID) {
		initService(false);
		return getInfo(reactionIds, reactionID);
	}
	
	private static ReactionEntry getInfo(
						HashMap<String, ReactionEntry> entries,
						String reactionID) {
		ReactionEntry result = entries.get(reactionID);
		if (result == null) {
			reactionID = StringManipulationTools.stringReplace(reactionID, "<html>", "");
			reactionID = StringManipulationTools.stringReplace(reactionID, "<HTML>", "");
			reactionID = StringManipulationTools.stringReplace(reactionID, "<br>", "");
			reactionID = StringManipulationTools.stringReplace(reactionID, "<BR>", "");
			reactionID = StringManipulationTools.stringReplace(reactionID, "<br/>", "");
			reactionID = StringManipulationTools.stringReplace(reactionID, "<BR/>", "");
			reactionID = StringManipulationTools.stringReplace(reactionID, "cpd:", "");
			result = entries.get(reactionID);
		}
		return result;
	}
	
	public static ReactionEntry getInformationLazy(String reactionID) {
		initService(true);
		return getInfo(reactionIds, reactionID);
	}
	
	/**
	 * @return The release information of the compound file.
	 *         Read out of ligant.txt.
	 */
	public static String getReleaseVersionForEnzymeInformation() {
		initService(false);
		return reactionDBrelease;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	public int getCurrentStatusValue() {
		return statusVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	public double getCurrentStatusValueFine() {
		return getCurrentStatusValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseStop()
	 */
	public void pleaseStop() {
		// abort of file loading not supported
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	public void pleaseContinueRun() {
		// empty
	}
	
	public void setCurrentStatusValue(int value) {
		statusVal = value;
	}
	
	public static Collection<String> getAllReactionIds() {
		initService(false);
		return reactionIds.keySet();
	}
	
	public String getDescription() {
		return "";
	}
	
	public JComponent getStatusPane(boolean showEmpty) {
		FolderPanel res = new FolderPanel("<html>" +
							"KEGG Reaction Database<br><small>" +
							"(contains information about reaction substrates, products and enzymes)");
		res.setFrameColor(Color.LIGHT_GRAY, null, 1, 5);
		
		int b = 5; // normal border
		int bB = 1; // border around action buttons
		
		boolean externalAvailable = false;
		String status2 = "";
		
		if (externalAvailable)
			status2 = "<html>OK, database file found!";
		else
			status2 = "<html>Database file not available";
		
		status2 = "<html><b>Database check not yet implemented!";
		
		res.addGuiComponentRow(
							new JLabel("<html>" +
												"Downloaded File:&nbsp;"),
							FolderPanel.getBorderedComponent(
												new JLabel(status2), b, b, b, b),
							false);
		
		ArrayList<JComponent> actionButtons = new ArrayList<JComponent>();
		actionButtons.add(new JButton("<html>Website"));
		actionButtons.add(new JButton("<html>License"));
		actionButtons.add(new JButton("<html>Download"));
		
		pretifyButtons(actionButtons);
		
		res.addGuiComponentRow(
							new JLabel("<html>" +
												"Visit Website(s)"),
							TableLayout.getMultiSplit(actionButtons, TableLayoutConstants.PREFERRED, bB, bB, bB, bB),
							false);
		
		res.layoutRows();
		return res;
	}
	
	private void pretifyButtons(ArrayList<JComponent> actionButtons) {
		for (JComponent jc : actionButtons) {
			((JButton) jc).setBackground(Color.white);
		}
	}
}
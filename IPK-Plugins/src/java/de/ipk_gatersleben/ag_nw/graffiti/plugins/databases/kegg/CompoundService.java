/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.HelperClass;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MemoryHog;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class CompoundService extends MemoryHog
		implements
		BackgroundTaskStatusProvider,
		FileDownloadStatusInformationProvider, HelperClass {
	private static boolean read_compound_DB_txt = false;
	
	private static String compoundDBrelease = "unknown";
	
	// ligand.txt
	private static String relTagDB = "Release ";
	
	/**
	 * Contains a mapping from the EC number (ID) to the corresponding enzyme
	 * info
	 */
	private static Map<String, CompoundEntry> compoundEntries = Collections.synchronizedMap(new HashMap<String, CompoundEntry>());
	
	/**
	 * Contains a mapping from the uppercase synonyms to the corresponding
	 * enzyme info
	 */
	private static HashMap<String, CompoundEntry> knownCompoundAlternativeNamesEntries = new HashMap<String, CompoundEntry>();
	
	// private boolean pleaseStop=false;
	private static String status1;
	private static String status2;
	private static int statusVal = -1;
	
	@Override
	public synchronized void finishedNewDownload() {
		read_compound_DB_txt = false;
		compoundEntries = Collections.synchronizedMap(new HashMap<String, CompoundEntry>());
		knownCompoundAlternativeNamesEntries = new HashMap<String, CompoundEntry>();
		status1 = null;
		status2 = null;
	}
	
	private static synchronized void initService(boolean initInBackground) {
		GravistoService.addKnownMemoryHog(new CompoundService());
		if (initInBackground) {
			if (!read_compound_DB_txt) {
				read_compound_DB_txt = true;
				final CompoundService cs = new CompoundService();
				BackgroundTaskHelper bth = new BackgroundTaskHelper(
						new Runnable() {
							@Override
							public void run() {
								statusVal = -1;
								read_compound_DB_txt = true;
								readLigantTXTforReleaseInfo();
								readCompoundDB();
								statusVal = 100;
							}
						},
						cs,
						"Compound Database",
						"Compound Database Service",
						true, false);
				bth.startWork(MainFrame.getInstance());
			}
		} else {
			if (!read_compound_DB_txt) {
				read_compound_DB_txt = true;
				readLigantTXTforReleaseInfo();
				readCompoundDB();
			}
		}
		noteRequest();
	}
	
	/**
	 * Open and read ligand.txt and look for the release info.
	 */
	private static void readLigantTXTforReleaseInfo() {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
			return;
		status1 = "Read Version Info...";
		BufferedReader input = getFileReader("ligand.txt");
		if (input == null) {
			;// ErrorMsg.addErrorMessage("Info: 'ligand.txt'-file could not be loaded!");
		} else {
			String line = null;
			try {
				while ((line = input.readLine()) != null) {
					// System.out.println(line);
					if (line.contains(relTagDB)) {
						compoundDBrelease = line.trim();
						break;
					}
				}
				status2 = "KEGG LIGAND - " + compoundDBrelease;
			} catch (IOException e) {
				status2 = "Error reading version info!";
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
	
	/**
	 * Reads the file compound. All methods depending on info from that file
	 * should call <code>initService</code>, first. To ensure that this
	 * service is available.
	 */
	private static void readCompoundDB() {
		compoundEntries.clear();
		knownCompoundAlternativeNamesEntries.clear();
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
			return;
		status1 = "Analyse compound information...";
		BufferedReader input = getFileReader("compound");
		if (input == null) {
			;// ErrorMsg.addErrorMessage("Info: 'compound'-file could not be loaded!");
		} else {
			String line = null;
			String lastStartTag = "";
			try {
				while ((line = input.readLine()) != null) {
					// System.out.println(line);
					line = line.trim();
					if (CompoundEntry.isValidCompoundStart(line)) {
						boolean endTagFound = false;
						CompoundEntry compEntry = new CompoundEntry();
						compEntry.processInputLine(line); // C00002 => ID
						
						// ********* READ ALL LINES FOR THIS ENTRY
						ArrayList<String> entrylines = new ArrayList<String>();
						do {
							String entryline = input.readLine();
							if (entryline == null)
								return;
							// System.out.println(entryline);
							endTagFound = entryline.startsWith(CompoundEntry.endTag_exists);
							if (!endTagFound)
								if (!(entryline.startsWith("     ") && entryline.trim().startsWith("$"))) {
									if (entryline.startsWith("     "))
										entryline = lastStartTag + "  " + entryline;
									else
										if (entryline.contains(" "))
											lastStartTag = entryline.substring(0, entryline.indexOf(" "));
								}
							if (!endTagFound)
								entrylines.add(entryline);
						} while (!endTagFound);
						// ********* PROCESS LINES FOR THIS ENTRY
						for (String entryline : entrylines)
							compEntry.processInputLine(entryline);
						
						if (compEntry.isValid()) {
							compoundEntries.put(compEntry.getID(), compEntry);
							status1 = "Analyse compound information (" + compoundEntries.size() + ")";
							knownCompoundAlternativeNamesEntries.put(compEntry.getID()
									.toUpperCase(), compEntry);
							for (String name : compEntry.getNames())
								knownCompoundAlternativeNamesEntries.put(name
										.toUpperCase(), compEntry);
						}
					}
				}
				status1 = "Compound information analysed (" + compoundEntries.size() + ")";
			} catch (IOException e) {
				status2 = "Error in reading compound info!";
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
		ClassLoader cl = CompoundService.class.getClassLoader();
		try {
			return new BufferedReader(new FileReader(ReleaseInfo.getAppFolderWithFinalSep() + fileName));
		} catch (Exception e) {
			try {
				return new BufferedReader(new InputStreamReader(cl.getResourceAsStream(fileName)));
			} catch (Exception e2) {
				MainFrame.showMessage("<html><b>Click Help/Database Status</b> for help on downloading compound data", MessageType.INFO);
				return null;
			}
		}
	}
	
	/**
	 * Get a CompoundEntry with the associated information.
	 * 
	 * @param compoundIDorAnyName
	 *           A Compound ID (C01234) or known name.
	 * @return NULL, if no info is found or a corresponding entry.
	 */
	public static CompoundEntry getInformation(String compoundIDorAnyName) {
		noteRequest();
		
		/*
		 * Retrieve multiple entries:
		 * http://www.genome.jp/dbget-bin/www_bget?dbname+identifier1+identifier2+...
		 * http://www.genome.jp/dbget-bin/www_bget?dbname1:identifier1+dbname2:identifier2+...
		 * The first form is applicable only to multiple entries from a single database.
		 */
		
		initService(false);
		CompoundEntry result = compoundEntries.get(compoundIDorAnyName);
		if (result == null) {
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<html>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<HTML>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<br>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<BR>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<br/>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<BR/>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "cpd:", "");
			result = compoundEntries.get(compoundIDorAnyName);
			if (result == null)
				result = knownCompoundAlternativeNamesEntries.get(compoundIDorAnyName.toUpperCase());
		}
		return result;
	}
	
	/**
	 * Get a CompoundEntry with the associated information.
	 * This method initializes the Database in the background. If the database
	 * is not yet initialized, it immediately returns null.
	 * 
	 * @param compoundIDorAnyName
	 *           A Compound ID (C01234) or known name.
	 * @return NULL, if no info is found or a corresponding entry.
	 */
	public static CompoundEntry getInformationLazy(String compoundIDorAnyName) {
		noteRequest();
		initService(true);
		CompoundEntry result = compoundEntries.get(compoundIDorAnyName);
		if (result == null) {
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<html>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<HTML>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<br>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<BR>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<br/>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "<BR/>", "");
			compoundIDorAnyName = StringManipulationTools.stringReplace(compoundIDorAnyName, "cpd:", "");
			result = compoundEntries.get(compoundIDorAnyName);
			if (result == null)
				result = knownCompoundAlternativeNamesEntries.get(compoundIDorAnyName.toUpperCase());
		}
		return result;
	}
	
	/**
	 * @return The release information of the compound file.
	 *         Read out of ligant.txt.
	 */
	public static String getReleaseVersionForCompoundInformation() {
		noteRequest();
		initService(false);
		return compoundDBrelease;
	}
	
	// /**
	// * Get the number of nodes in this graph which could possible be
	// * valid Compound IDs. It is not checked if the Compound ID is existent,
	// * meaning a Compound ID like "C66666" which probably does not exist
	// * would increase the number count.
	// *
	// * @param graph All nodes from this graph will be checked
	// * @return The number of nodes which have a a name or
	// * substance name like Cxxxxxx where x are digits.
	// */
	// public static int getNumberOfCompoundNodes(Graph graph) {
	// int cnt = 0;
	// for (Node n : graph.getNodes()) {
	// String sn = AttributeHelper.getLabel(n, null);
	// if (sn != null && sn.length() > 1) {
	// sn = sn.toUpperCase();
	// if (sn.startsWith("C")) {
	// try {
	// Integer.parseInt(sn.substring(1));
	// cnt++;
	// } catch (NumberFormatException nfe) {
	// // empty
	// }
	// }
	// }
	// }
	// return cnt;
	// }
	
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
				"KEGG Compound Database<br><small>" +
				"(contains information about compound IDs, names and synonyms)");
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
		
		boolean externalAvailable = read_compound_DB_txt && compoundEntries != null && compoundEntries.size() > 0;
		String status2 = "";
		
		if (externalAvailable)
			status2 = "<html><b>Database is online</b>";
		else
			status2 = "<html>Database file not available";
		
		String modifiedTime = GravistoService.getFileModificationDateAndTime(
				getFile("compound"),
				"unknown version (file not found)");
		File f = getFile("ko");
		
		if (externalAvailable) {
			if (f.exists())
				status2 += "<br>&nbsp;&nbsp;database download: " + modifiedTime;
			else
				status2 += "<br>&nbsp;&nbsp;" + modifiedTime;
			// status2+="<br>&nbsp;&nbsp;compound, ligand.txt: "+compoundDBrelease;
			status2 += "<br>&nbsp;&nbsp;compound entries: " + compoundEntries.size();
		}
		
		if (showEmpty)
			status2 = "<html><b>Bringing database online...</b><br>Please wait a few moments.";
		
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
				"http://www.genome.jp/kegg/download/ftp.html",
				// "http://www.genome.jp/kegg/kegg6.html",
				ReleaseInfo.getAppFolderWithFinalSep(),
				"<html>" +
						"The following URL and the target folder will be automatically opened in a few seconds:<br><br>" +
						"<code><b>" +
						"http://www.genome.jp/kegg/download/ftp.html" +
						// "http://www.genome.jp/kegg/kegg6.html" +
						"</b></code><br><br>" +
						"Please (re)evaluate your KEGG license status, before proceeding with the following steps:<br>" +
						"<ol>" +
						"	<li>Click onto the FTP link: &quot;ligand/&quot; KEGG LIGAND (daily updated)" +
						"	<li>Download the following file:" +
						"		<ul>" +
						"			<li>ligand.txt" +
						"		</ul>" +
						"	<li>Open the sub folder &quot;compound&quot;" +
						"	<li>Download the following file:" +
						"		<ul>" +
						"			<li>compound" +
						"		</ul>" +
						"</ol>" +
						"After downloading them, please move these files to the following location:<br><br>" +
						"<code><b>" + ReleaseInfo.getAppFolder() + "</b></code><br><br>" +
						"After closing and re-opening this application, the KEGG Compound database will be<br>" +
						"available to the system.",
				new String[] {
						"ftp://ftp.genome.jp/pub/kegg/ligand/ligand.txt",
						"ftp://ftp.genome.jp/pub/kegg/ligand/compound/compound"
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
		return GUIhelper.getWebsiteButton("Website", "http://www.genome.jp/kegg/ligand.html", null, null, null);
	}
	
	private void pretifyButtons(ArrayList<JComponent> actionButtons) {
		for (JComponent jc : actionButtons) {
			((JButton) jc).setBackground(Color.white);
		}
	}
	
	public static Collection<CompoundEntry> getCompoundEntries() {
		noteRequest();
		
		initService(false);
		return compoundEntries.values();
	}
	
	public static int getNumberOfCompoundNodes(Graph myGraph) {
		noteRequest();
		
		if (myGraph == null)
			return 0;
		int cnt = 0;
		for (Node n : myGraph.getNodes()) {
			try {
				String lbl = AttributeHelper.getLabel(n, null);
				if (lbl != null) {
					CompoundEntry ce = getInformation(lbl);
					if (ce != null && ce.isValid())
						cnt++;
				}
			} catch (Exception e) {
				// ignore
			}
		}
		return cnt;
	}
	
	public static int getNumberOfCarbonAtoms(String substance) {
		noteRequest();
		int Ccount = 0;
		CompoundEntry ce = CompoundService.getInformation(substance);
		if (ce != null) {
			System.out.println(ce.getFormula());
			String formula = ce.getFormula();
			if (formula.indexOf("C") >= 0) {
				formula = formula.toUpperCase();
				formula = StringManipulationTools.stringReplace(formula, "CL", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CA", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CR", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CO", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CU", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CD", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CS", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CM", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CF", "XX");
				formula = StringManipulationTools.stringReplace(formula, "CE", "XX");
				
				if (formula.contains("C")) {
					int numberCount = 0;
					String analyze = formula.substring(formula.indexOf("C") + "C".length());
					for (char c : analyze.toCharArray()) {
						if (Character.isDigit(c)) {
							numberCount++;
						} else
							break;
					}
					if (numberCount == 0)
						Ccount = 1;
					else {
						String numberOfC = analyze.substring(0, numberCount);
						Ccount = Integer.parseInt(numberOfC);
					}
				}
			}
		}
		return Ccount;
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
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
	
	public static boolean isDatabaseAvailable() {
		return !getReleaseVersionForCompoundInformation().equals("unknown");
	}
	
}
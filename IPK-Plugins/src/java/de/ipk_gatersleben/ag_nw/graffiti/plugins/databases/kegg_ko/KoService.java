/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.HelperClass;
import org.JMButton;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MemoryHog;
import org.graffiti.editor.MessageType;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class KoService extends MemoryHog implements BackgroundTaskStatusProvider,
		FileDownloadStatusInformationProvider, HelperClass {
	private static boolean read_ko_DB_txt = false;
	
	private static String koDBrelease = "unknown";
	
	// ligand.txt
	// private static String relTagDB = "Release ";
	
	private static Map<String, KoEntry> koEntries = new HashMap<String, KoEntry>();
	
	private static HashMap<String, HashSet<KoEntry>> ec2ko = new HashMap<String, HashSet<KoEntry>>();
	private static HashMap<String, String> mapNumber2groupInfo = new HashMap<String, String>();
	private static HashMap<String, HashSet<KoEntry>> otherDBentry2ko = new HashMap<String, HashSet<KoEntry>>();
	
	private static String status1;
	private static String status2;
	private static int statusVal = -1;
	
	private static HashSet<String> selectedOrganisms = initSelectedOrganisms();
	
	@Override
	public void finishedNewDownload() {
		read_ko_DB_txt = false;
		koDBrelease = "unknown";
		koEntries = new HashMap<String, KoEntry>();
		ec2ko = new HashMap<String, HashSet<KoEntry>>();
		mapNumber2groupInfo = new HashMap<String, String>();
		otherDBentry2ko = new HashMap<String, HashSet<KoEntry>>();
		orgs.clear();
		status1 = null;
		status2 = null;
		statusVal = -1;
	}
	
	private static HashSet<String> initSelectedOrganisms() {
		HashSet<String> res = new HashSet<String>();
		
		try {
			TextFile t = new TextFile(ReleaseInfo.getAppFolderWithFinalSep() + "organisms.txt");
			for (String o : t)
				if (!o.startsWith("#"))
					res.add(o.toUpperCase().trim());
			if (res.size() == 0)
				writeDefault(null);
		} catch (IOException e) {
			writeDefault(null);
		}
		MainFrame.showMessage("Initialized list of valid organisms", MessageType.INFO);
		return res;
	}
	
	private static void writeDefault(String optSpeciesAsLineSeperatedString) {
		try {
			TextFile t = new TextFile();
			t.add("# add organism short names (e.g. HSA or ATH) in individual lines");
			t.add("# see http://www.genome.jp/kegg/catalog/org_list.html for kegg short names");
			t.add("# specify no short name, to work with all organisms (high memory requirement)");
			t.add("# it is advised to limit the working set in order to decrease the memory requirement");
			if (optSpeciesAsLineSeperatedString != null && optSpeciesAsLineSeperatedString.length() > 0)
				t.add(optSpeciesAsLineSeperatedString);
			t.write(ReleaseInfo.getAppFolderWithFinalSep() + "organisms.txt");
		} catch (IOException e1) {
			ErrorMsg.addErrorMessage(e1);
		}
	}
	
	public static boolean isSelectedOrganism(String org) {
		return selectedOrganisms.isEmpty() || selectedOrganisms.contains(org);
	}
	
	public static boolean addSelectedOrganism(String org) {
		selectedOrganisms.add(org);
		
		TextFile tf = new TextFile();
		tf.addAll(selectedOrganisms);
		try {
			tf.write(ReleaseInfo.getAppFolderWithFinalSep() + "organisms.txt");
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
			return false;
		}
		return true;
	}
	
	private static synchronized void initService(boolean initInBackground) {
		selectedOrganisms = initSelectedOrganisms();
		if (initInBackground) {
			if (!read_ko_DB_txt) {
				read_ko_DB_txt = true;
				final KoService ks = new KoService();
				BackgroundTaskHelper bth = new BackgroundTaskHelper(new Runnable() {
					@Override
					public void run() {
						statusVal = -1;
						read_ko_DB_txt = true;
						readLigantTXTforReleaseInfo();
						readKoDB();
						statusVal = 100;
					}
				}, ks, null, // "KO Database",
						"KO Database Service", true, false);
				bth.startWork(MainFrame.getInstance());
			}
		} else {
			if (!read_ko_DB_txt) {
				read_ko_DB_txt = true;
				readLigantTXTforReleaseInfo();
				readKoDB();
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
		String modifiedTime = GravistoService.getFileModificationDateAndTime(getFile("ko"),
				"unknown version (file not found)");
		File f = getFile("ko");
		if (f.exists()) {
			koDBrelease = "database download " + modifiedTime + "";
		} else
			koDBrelease = modifiedTime;
	}
	
	/**
	 * Reads the file ko. All methods depending on info from that file should
	 * call <code>initService</code>, first. To ensure that this service is
	 * available.
	 */
	private static void readKoDB() {
		koEntries.clear();
		ec2ko.clear();
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
			return;
		status1 = "Analyse KO information...";
		BufferedReader input = getFileReader("ko");
		int cnt = 1;
		if (input == null) {
			ErrorMsg.addErrorMessage("Info: 'ko'-file could not be loaded!");
		} else {
			String line = null;
			String lastStartTag = "";
			try {
				while ((line = input.readLine()) != null) {
					// System.out.println(line);
					line = line.trim();
					if (KoEntry.isValidKoStart(line)) {
						boolean endTagFound = false;
						KoEntry koEntry = new KoEntry();
						koEntry.processInputLine(line);
						
						// ********* READ ALL LINES FOR THIS ENTRY
						ArrayList<String> entrylines = new ArrayList<String>();
						do {
							String entryline = input.readLine();
							if (entryline == null)
								return;
							// System.out.println(entryline);
							endTagFound = entryline.startsWith(KoEntry.endTag_exists);
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
							koEntry.processInputLine(entryline);
						
						if (koEntry.isValid()) {
							koEntries.put(koEntry.getKoID(), koEntry);
							// koEntries.put(koEntry.getKoName(), koEntry);
							status1 = "Analyse KO information (" + koEntries.size() + ")";
							MainFrame.showMessage("Bringing KO database online, entry " + (cnt++), MessageType.INFO);
							for (String ec : koEntry.getKoDbLinks("EC")) {
								if (!ec2ko.containsKey(ec))
									ec2ko.put(ec, new HashSet<KoEntry>());
								ec2ko.get(ec).add(koEntry);
							}
							for (String otherDBentry : koEntry.getKoDbLinks()) {
								if (!otherDBentry2ko.containsKey(otherDBentry))
									otherDBentry2ko.put(otherDBentry, new HashSet<KoEntry>());
								otherDBentry2ko.get(otherDBentry).add(koEntry);
							}
							for (String s : koEntry.getKoClasses()) {
								if (s.indexOf("[PATH:") > 0) {
									String st = s.substring(s.indexOf("[PATH:") + "[PATH:".length());
									String stg = s.substring(0, s.indexOf("[PATH:"));
									if (st.indexOf("]") > 0) {
										st.substring(0, st.indexOf("]")).trim();
										for (String mn : s.split(" ")) {
											mn = mn.trim();
											mn = getDigits(mn);
											if (mn.length() <= 0)
												continue;
											if (!mapNumber2groupInfo.containsKey(mn))
												mapNumber2groupInfo.put(mn, stg);
										}
									}
								}
							}
						}
					}
				}
				status1 = "KO information analysed (" + koEntries.size() + ")";
			} catch (IOException e) {
				status2 = "Error in reading KO info!";
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			} finally {
				if (input != null)
					try {
						input.close();
					} catch (IOException e1) {
						ErrorMsg.addErrorMessage(e1.getLocalizedMessage());
					}
			}
			MainFrame.showMessage("KO database containing " + (cnt++) + " entries is online", MessageType.PERMANENT_INFO);
		}
	}
	
	private static String getDigits(String mapNumber) {
		String result = "";
		for (char c : mapNumber.toCharArray()) {
			if (Character.isDigit(c))
				result = result + c;
		}
		return result;
	}
	
	public static synchronized boolean isExternalKoFileAvailable() {
		String fn = ReleaseInfo.getAppFolderWithFinalSep() + "ko";
		return new File(fn).canRead();
	}
	
	public static BufferedReader getFileReader(String fileName) {
		try {
			return new BufferedReader(new FileReader(ReleaseInfo.getAppFolderWithFinalSep() + fileName));
		} catch (Exception e) {
			try {
				ClassLoader cl = KoService.class.getClassLoader();
				return new BufferedReader(new InputStreamReader(cl.getResourceAsStream(fileName)));
			} catch (Exception e2) {
				MainFrame.showMessage("<html><b>Click Help/Database Status</b> for help on downloading KO data",
						MessageType.INFO);
				return null;
			}
		}
	}
	
	private static File getFile(String fileName) {
		return new File(ReleaseInfo.getAppFolderWithFinalSep() + fileName);
	}
	
	/**
	 * Get a KoEntry with the associated information.
	 * 
	 * @param koIDorNameOrDefinition
	 *           A KO id, name or definition.
	 * @return NULL, if no info is found or a corresponding entry.
	 */
	public static KoEntry getInformation(String koIDorNameOrDefinition) {
		noteRequest();
		initService(false);
		KoEntry result = koEntries.get(koIDorNameOrDefinition);
		if (result == null) {
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<html>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<HTML>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<br>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<BR>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<br/>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<BR/>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "ko:", "");
			result = koEntries.get(koIDorNameOrDefinition);
		}
		return result;
	}
	
	/**
	 * Get a CompoundEntry with the associated information. This method inits the
	 * Database in the background. If the database is not yet inited, it
	 * immeadiatly returns null.
	 * 
	 * @param compoundIDorAnyName
	 *           A Compound ID (C01234) or known name.
	 * @return NULL, if no info is found or a corresponding entry.
	 */
	public static KoEntry getInformationLazy(String koIDorNameOrDefinition) {
		noteRequest();
		initService(true);
		KoEntry result = koEntries.get(koIDorNameOrDefinition);
		if (result == null) {
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<html>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<HTML>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<br>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<BR>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<br/>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "<BR/>", "");
			koIDorNameOrDefinition = StringManipulationTools.stringReplace(koIDorNameOrDefinition, "ko:", "");
			result = koEntries.get(koIDorNameOrDefinition);
		}
		return result;
	}
	
	/**
	 * @return The release information of the compound file. Read out of
	 *         ligant.txt.
	 */
	public static String getReleaseVersionForKoInformation() {
		noteRequest();
		initService(false);
		return koDBrelease;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		return statusVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return getCurrentStatusValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #getCurrentStatusMessage1()
	 */
	@Override
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #getCurrentStatusMessage2()
	 */
	@Override
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #pleaseStop()
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
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #pluginWaitsForUser()
	 */
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #pleaseContinueRun()
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
		
		FolderPanel res = new FolderPanel("<html>" + "KEGG Orthology (KO) Database<br><small>"
				+ "(classification system for orthologous genes)");
		res.setFrameColor(Color.LIGHT_GRAY, null, 1, 5);
		
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS)) {
			res
					.addGuiComponentRow(
							null,
							new JLabel(
									"<html>"
											+ "KEGG features are disabled.<br>Use side panel Help/Settings to enable access. Then restart the program."),
							false);
			res.layoutRows();
			return res;
		}
		
		if (!showEmpty)
			initService(false);
		
		int b = 5; // normal border
		int bB = 1; // border around action buttons
		
		boolean externalAvailable = read_ko_DB_txt && koEntries != null && koEntries.size() > 0;
		String status2 = "";
		
		if (externalAvailable)
			status2 = "<html><b>Database is online</b>";
		else
			status2 = "<html>Database file not available";
		
		if (externalAvailable) {
			int geneCnt = 0;
			for (KoEntry koe : koEntries.values())
				for (String organismCode : koe.getOrganismCodes())
					geneCnt += koe.getGeneIDs(organismCode).size();
			
			status2 += "<br>&nbsp;&nbsp;" + koDBrelease;
			status2 += "<br>&nbsp;&nbsp;KO entries: " + koEntries.size();
			status2 += "<br>&nbsp;&nbsp;Gene IDs: " + geneCnt;
		}
		
		if (showEmpty)
			status2 = "<html><b>Bringing database online...</b><br>Please wait a few moments.";
		
		res.addGuiComponentRow(new JLabel("<html>" + "Downloaded Files:&nbsp;"), FolderPanel.getBorderedComponent(
				new JLabel(status2), b, b, b, b), false);
		
		ArrayList<JComponent> actionButtons = new ArrayList<JComponent>();
		if (!showEmpty) {
			actionButtons.add(getWebsiteButton());
			actionButtons.add(getLicenseButton());
			actionButtons.add(getDownloadButton());
		}
		pretifyButtons(actionButtons);
		
		res.addGuiComponentRow(new JLabel("<html>" + "Visit Website(s)"), TableLayout.getMultiSplit(actionButtons,
				TableLayout.PREFERRED, bB, bB, bB, bB), false);
		
		res.layoutRows();
		return res;
	}
	
	private JComponent getDownloadButton() {
		JComponent a = GUIhelper.getWebsiteDownloadButton("Download",
				// "http://www.genome.jp/anonftp/",
				"http://www.genome.jp/kegg/download/ftp.html",
				// "http://www.genome.jp/kegg/kegg6.html",
				ReleaseInfo.getAppFolderWithFinalSep(), "<html>"
						+ "The following URL and the target folder will be automatically opened in a few seconds:<br><br>"
						+
						// "<code><b>http://www.genome.jp/anonftp/</b></code><br><br>"
						// +
						"<code><b>"
						+ "http://www.genome.jp/kegg/download/ftp.html"
						+
						// "http://www.genome.jp/kegg/kegg6.html" +
						"</b></code><br><br>"
						+ "Please (re)evaluate your KEGG license status, before proceeding with the following steps:<br>"
						+ "<ol>"
						+ "	<li>Click the FTP link with the title: &quot;genes/&quot; KEGG GENES (daily updated)"
						+
						// "	<li>Open the ftp folder &quot;tarfiles&quot;"
						// +
						// "	<li>Open the ftp folder &quot;tarfiles&quot;"
						// +
						"	<li>Download the following file:" + "		<ul>" + "			<li>ko" + "		</ul>" + "</ol>"
						+ "After downloading it, please move this file to the following location:<br><br>" + "<code><b>"
						+ ReleaseInfo.getAppFolder() + "</b></code><br><br>"
						+ "After closing and re-opening this application, the KEGG KO database will be<br>"
						+ "available to the system.", new String[] { "ftp://ftp.genome.jp/pub/kegg/genes/ko" },
				"Download Instructions", this);
		
		((JButton) a).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String organisms = "";
				for (String org : initSelectedOrganisms())
					organisms += org + "\n";
				
				Object[] res = MyInputHelper.getInput("<html>Here you can specifiy the organisms to be accounted for<br>"
						+ "in the KO database, which will reduce the database memory <br>"
						+ "needs. Please use the short species description, e.g.<br>"
						+ "HSA for human and ATH for arabidopsis." + "<ul><li>empty field: all species are loaded</li>"
						+ "<li>one species per line: only this species will be loaded</li>" + "</ul>",
						"Please Select Organisms", new Object[] { "Species Names//", organisms });
				if (res != null)
					writeDefault((String) res[0]);
				
			}
		});
		
		JButton editOrgs = new JMButton("Edit");
		editOrgs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				finishedNewDownload();
				AttributeHelper.showInBrowser(ReleaseInfo.getAppFolderWithFinalSep() + "organisms.txt");
				GravistoService.freeMemory(true);
			}
		});
		
		return TableLayout.get3Split(a, null, editOrgs, TableLayout.PREFERRED, 2, TableLayout.PREFERRED);
	}
	
	private JComponent getLicenseButton() {
		// return GUIhelper.getWebsiteButton("License",
		// "http://www.genome.jp/kegg/kegg5.html", null, null);
		// return GUIhelper.getWebsiteButton("License",
		// "http://www.genome.jp/kegg/kegg6.html", null, null);
		return GUIhelper.getWebsiteButton("License", "http://www.genome.jp/kegg/legal.html", null, null, null);
	}
	
	private JComponent getWebsiteButton() {
		return GUIhelper.getWebsiteButton("Website", "http://www.genome.jp/kegg/brite.html", null, null, null);
	}
	
	private void pretifyButtons(ArrayList<JComponent> actionButtons) {
		for (JComponent jc : actionButtons) {
			if (jc instanceof JButton)
				((JButton) jc).setBackground(Color.white);
			else {
				for (Component c : ((JPanel) jc).getComponents()) {
					if (c instanceof JButton)
						((JButton) c).setBackground(Color.white);
				}
			}
		}
	}
	
	public static Collection<KoEntry> getKoFromEnzyme(String enzymeNumber) {
		noteRequest();
		initService(false);
		Collection<KoEntry> result = ec2ko.get(enzymeNumber);
		if (result != null)
			return result;
		else
			return new ArrayList<KoEntry>();
	}
	
	private static HashMap<String, ArrayList<KoEntry>> orgCode2koList = new HashMap<String, ArrayList<KoEntry>>();
	//
	private static HashMap<String, HashSet<KoEntry>> geneId2KoList = new HashMap<String, HashSet<KoEntry>>();
	
	private static HashSet<String> processedOrganismCodes = new HashSet<String>();
	
	public static Collection<KoEntry> getKoFromGeneId(String organismCode, String geneId) {
		noteRequest();
		initService(false);
		organismCode = organismCode.toUpperCase();
		if (organismCode.length() <= 0)
			return new ArrayList<KoEntry>();
		
		String kk = organismCode + ":" + geneId;
		if (geneId2KoList.containsKey(kk))
			return geneId2KoList.get(kk);
		else
			if (processedOrganismCodes.contains(organismCode))
				return new ArrayList<KoEntry>();
		
		ArrayList<KoEntry> koEntriesWithGenesForOrganism;
		if (orgCode2koList.containsKey(organismCode)) {
			koEntriesWithGenesForOrganism = orgCode2koList.get(organismCode);
		} else {
			koEntriesWithGenesForOrganism = new ArrayList<KoEntry>();
			orgCode2koList.put(organismCode, koEntriesWithGenesForOrganism);
			for (KoEntry koe : koEntries.values())
				if (koe.getGeneIDs(organismCode).size() > 0)
					koEntriesWithGenesForOrganism.add(koe);
		}
		ArrayList<KoEntry> result = new ArrayList<KoEntry>();
		for (KoEntry koe : koEntriesWithGenesForOrganism) {
			if (koe.hasGeneMapping(organismCode, geneId)) {
				result.add(koe);
			}
			if (!processedOrganismCodes.contains(organismCode)) {
				for (String id : koe.getGeneIDs(organismCode)) {
					String k1 = organismCode + ":" + id;
					if (!geneId2KoList.containsKey(k1))
						geneId2KoList.put(k1, new HashSet<KoEntry>());
					geneId2KoList.get(k1).add(koe);
				}
			}
		}
		processedOrganismCodes.add(organismCode);
		return result;
	}
	
	public static Collection<KoEntry> getKoFromDBlink(String dbLink) {
		noteRequest();
		initService(false);
		ArrayList<KoEntry> result = new ArrayList<KoEntry>();
		if (otherDBentry2ko.get(dbLink) != null)
			for (KoEntry koe : otherDBentry2ko.get(dbLink)) {
				result.add(koe);
			}
		return result;
	}
	
	private static HashSet<String> orgs = new HashSet<String>();
	
	public static Collection<KoEntry> getKoFromGeneIdOrKO(String test) {
		noteRequest();
		initService(false);
		if (test == null) {
			ArrayList<KoEntry> result = new ArrayList<KoEntry>();
			return result;
		} else
			if (test.indexOf(":") <= 0 || test.indexOf("ko:") >= 0) {
				// System.out.println("KO?:"+test);
				if (test.indexOf("ko:") == 0)
					test = test.substring("ko:".length());
				ArrayList<KoEntry> result = new ArrayList<KoEntry>();
				if (koEntries.containsKey(test))
					result.add(koEntries.get(test));
				else {
					try {
						int a = Integer.parseInt(test);
						if (a > 0) {
							if (orgs.size() == 0)
								for (KoEntry koe : koEntries.values()) {
									orgs.addAll(koe.getOrganismCodes());
								}
							HashSet<KoEntry> res = new HashSet<KoEntry>();
							for (String org : orgs) {
								res.addAll(getKoFromGeneId(org, test));
							}
							result.addAll(res);
						}
					} catch (Exception e) {
						// empty
					}
				}
				return result;
			} else {
				String orgT = test.substring(0, test.indexOf(":")).trim();
				String genT = test.substring(test.indexOf(":") + 1).trim();
				return getKoFromGeneId(orgT, genT);
			}
	}
	
	public static String[] getPathwayGroupFromMapNumber(String mapNumber) {
		noteRequest();
		initService(false);
		String s = mapNumber2groupInfo.get(mapNumber);
		if (s != null && s.length() > 0) {
			String[] res = s.split(";");
			for (int i = 0; i < res.length; i++) {
				res[i] = res[i].trim();
			}
			return res;
		}
		return new String[] { "group unknown", "not in KO" };
	}
	
	public static Collection<String> getCompleteClassInformation() {
		noteRequest();
		initService(false);
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		for (KoEntry ko : koEntries.values()) {
			result.addAll(ko.getKoClasses());
		}
		return result;
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
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.editor.MemoryHog#lastUsageTime()
	 */
}
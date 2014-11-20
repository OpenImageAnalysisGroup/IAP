/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.FolderPanel;
import org.HelperClass;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MemoryHog;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class EnzymeService extends MemoryHog
		implements
		BackgroundTaskStatusProvider,
		FileDownloadStatusInformationProvider, HelperClass {
	// Enzyme Class related variables
	private static boolean read_enzclass_txt = false;
	
	private static String enzymeRelease = "unknown version";
	
	private static String relTag = "Release:";
	
	private static List<EnzClassEntry> enzymeClassEntries = new Vector<EnzClassEntry>();
	
	// Enzyme Database related variables
	private static boolean read_enzyme_DB_txt = false;
	
	private static String enzymeDBrelease = "unknown version";
	
	private static String relTagDB = "CC   Release ";
	
	// private boolean pleaseStop=false;
	private static String status1;
	private static String status2;
	private static int statusVal = -1;
	
	@Override
	public synchronized void finishedNewDownload() {
		read_enzclass_txt = false;
		enzymeRelease = "unknown version";
		enzymeClassEntries = new Vector<EnzClassEntry>();
		read_enzyme_DB_txt = false;
		enzymeDBrelease = "unknown version";
		status1 = null;
		status2 = null;
		statusVal = -1;
	}
	
	/**
	 * Contains a mapping from the EC number (ID) to the corresponding enzyme
	 * info
	 */
	private static HashMap<String, EnzymeEntry> enzymeEntries = new HashMap<String, EnzymeEntry>();
	
	/**
	 * Contains a mapping from the uppercase synonyms to the corresponding
	 * enzyme info
	 */
	private static HashMap<String, EnzymeEntry> knownEnzymeAlternativeNamesEntries = new HashMap<String, EnzymeEntry>();
	
	private synchronized static void initService(boolean initInBackground) {
		if (initInBackground) {
			if (!read_enzclass_txt || !read_enzyme_DB_txt) {
				final boolean todoReadEnzClass = !read_enzclass_txt;
				final boolean todoReadDB = !read_enzyme_DB_txt;
				read_enzclass_txt = true;
				read_enzyme_DB_txt = true;
				final EnzymeService enzS = new EnzymeService();
				BackgroundTaskHelper bth = new BackgroundTaskHelper(
						new Runnable() {
							@Override
							public void run() {
								statusVal = -1;
								if (todoReadEnzClass)
									readEnzymeClasses();
								if (todoReadDB)
									readEnzymeDB();
								statusVal = 100;
							}
						},
						enzS,
						"Enzyme Database",
						"Enzyme Database Service",
						true, false);
				bth.startWork(MainFrame.getInstance());
			}
		} else {
			if (!read_enzclass_txt) {
				readEnzymeClasses();
				read_enzclass_txt = true;
			}
			if (!read_enzyme_DB_txt) {
				readEnzymeDB();
				read_enzyme_DB_txt = true;
			}
		}
		noteRequest();
	}
	
	/**
	 * Reads the file enzclass.txt. All methods depending on info from that file
	 * should call <code>initService</code>, first. To ensure that this
	 * service is available.
	 */
	private static void readEnzymeClasses() {
		// the enzclass.txt is formated like this:
		// A entry starts with a number followed by a point. (e.g. 1.x.x.x)
		// Each entry is finished with a ".", if that is missing, then
		// the entry wraps to the next line(s). Any space in front of the
		// next line(s) must be condensed into one space.
		// All other lines can be ignored.
		// A line with has the content "Release", should be
		// threated specially, so that the file version is
		// extracted (e.g. "Release: 8-Jun-2004")
		status1 = "Read Enzyme-Class information...";
		synchronized (enzymeClassEntries) {
			enzymeClassEntries.clear();
			BufferedReader input;
			input = getFileReader("enzclass.txt");
			if (input != null) {
				String line = null;
				try {
					while ((line = input.readLine()) != null) {
						line = line.trim();
						if (EnzClassEntry.isValidEnzymeStart(line)) {
							// probably a valid Enzyme Class information ...
							while (!line.endsWith("."))
								line += " " + input.readLine().trim();
							if (line.endsWith(".")) {
								EnzClassEntry ece = EnzClassEntry.getEnzClassEntry(line);
								if (ece != null)
									enzymeClassEntries.add(ece);
							}
						} else {
							// check for release info
							if (line.contains(relTag)) {
								line = line.substring(line.indexOf(relTag) + relTag.length());
								line = line.trim();
								enzymeRelease = line;
								status2 = "SIB " + enzymeRelease;
							}
						}
					}
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
					status2 = "Error reading Enzyme-Classes!";
				} finally {
					if (input != null)
						try {
							input.close();
						} catch (IOException e1) {
							ErrorMsg.addErrorMessage(e1);
						}
				}
			} else
				status2 = "Error reading Enzyme-Classes!";
		}
		status1 = "Enzyme classes analysed";
	}
	
	/**
	 * Reads the file enzyme.dat. All methods depending on info from that file
	 * should call <code>initService</code>, first. To ensure that this
	 * service is available.
	 */
	private static void readEnzymeDB() {
		// the enzyme.dat is formated like this:
		// [1..1] ID Identification (EC Number)
		// [1..1] DE Description (Official name)
		// [0..n] AN Alternative name
		// [0..n] CA Catalytic activity
		// [0..n] CF Cofactor(s)
		// [0..n] CC Comments
		// [0..n] DI Diesease(s) associated with this enzyme (human)
		// [0..n] PR Cross-reference to PROSITE
		// [0..n] DR Cross-reference to SWISS-PROT
		// [1..1] // Termination line (Ends each entry)
		//
		// All lines besides the ID line end with a point "."
		// If that point is missing, the entry spans more than one line.
		// In this case each line needs to be trimmed, and one space is
		// inserted between the concatenated lines.
		// If the last character of a line is "-", this space needs to be
		// ommited,
		// the lines are concatinated directly.
		status1 = "Read Enzyme Names and IDs...";
		enzymeEntries.clear();
		knownEnzymeAlternativeNamesEntries.clear();
		BufferedReader input;
		input = getFileReader("enzyme.dat");
		if (input == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: 'enzyme.dat'-file could not be loaded!");
		} else {
			String line = null;
			try {
				while ((line = input.readLine()) != null) {
					// System.err.println(line);
					line = line.trim();
					if (EnzymeEntry.isValidEnzymeStart(line)) {
						boolean endTagFound = false;
						EnzymeEntry eze = new EnzymeEntry();
						do {
							if (line.endsWith("//")) {
								endTagFound = true;
								line = line.substring(0, line.length() - 2);
							}
							
							if (line.startsWith("PR") || line.startsWith("DR")) {
								// ignore these lines... they need also be
								// treated different,
								// because they are not ended with a ".". the
								// "//" end tag might
								// follow such a entry
							} else {
								if (line.startsWith("ID"))
									line += ".";
								while (!line.endsWith(".") && !line.contains("//")
										&& !endTagFound) {
									String rl = input.readLine();
									// System.err.println(rl);
									if (rl.endsWith("//")) {
										endTagFound = true;
										rl = rl.substring(0, rl.length() - 2);
									}
									rl = EnzymeEntry.trimKnownPrefixes(rl);
									if (line.endsWith("-"))
										line += rl.trim();
									else {
										line += " " + rl.trim();
									}
								}
								if (line.endsWith(".")) {
									eze.processInputLine(line);
								}
							}
							if (!endTagFound) {
								line = input.readLine();
								// System.err.println(line);
								line = line.trim();
							}
						} while (!endTagFound);
						if (eze.isValid()) {
							enzymeEntries.put(eze.getID(), eze);
							knownEnzymeAlternativeNamesEntries.put(eze.getDE()
									.toUpperCase(), eze);
							for (Iterator<String> it = eze.getAN().iterator(); it.hasNext();)
								knownEnzymeAlternativeNamesEntries.put(
										it.next().toUpperCase(), eze);
							status1 = "Read Enzyme Names and IDs (" + enzymeEntries.size() + ")";
						}
					} else {
						// check for release info
						if (line.contains(relTagDB)) {
							line = line.substring(line.indexOf(relTagDB)
									+ new String("CC").length());
							line = line.trim();
							enzymeDBrelease = line;
							status2 = enzymeDBrelease;
						}
					}
				}
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			} finally {
				if (input != null)
					try {
						input.close();
					} catch (IOException e1) {
						ErrorMsg.addErrorMessage(e1);
					}
			}
		}
		status1 = "Enzyme Names analysed (" + enzymeEntries.size() + ")";
	}
	
	private static BufferedReader getFileReader(String fileName) {
		ClassLoader cl = CompoundService.class.getClassLoader();
		try {
			return new BufferedReader(new FileReader(ReleaseInfo.getAppFolderWithFinalSep() + fileName));
		} catch (Exception e) {
			try {
				return new BufferedReader(new InputStreamReader(cl.getResourceAsStream(fileName)));
			} catch (Exception e2) {
				MainFrame.showMessage("<html><b>Click Help/Database Status</b> for help on downloading enzyme data", MessageType.INFO);
				return null;
			}
		}
	}
	
	public static boolean isDatabaseAvailable(boolean checkInternal) {
		noteRequest();
		String fileName = "enzyme.dat";
		ClassLoader cl = CompoundService.class.getClassLoader();
		try {
			if (checkInternal) {
				InputStreamReader isr = new InputStreamReader(cl.getResourceAsStream(fileName));
				return isr.ready();
			} else {
				FileReader fr = new FileReader(ReleaseInfo.getAppFolderWithFinalSep() + fileName);
				boolean res = fr.ready();
				fr.close();
				return res;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * @return The release information from the enzyme-classes file
	 *         (enzclass.txt).
	 */
	public static String getReleaseVersionForEnzymeClasses() {
		noteRequest();
		initService(false);
		return enzymeRelease;
	}
	
	/**
	 * Retuns all matches for the enzyme class. Example: "1.1.5.2" "1. -. -.-" ->
	 * Oxidoreductases "1. 1. -.-" -> Acting on the CH-OH group of donors "1. 1.
	 * 5.-" -> With a quinone or similar compound as acceptor. ==> RETURN: {
	 * "Oxidoreductases", "Acting on the CH-OH group of donors", "With a quinone
	 * or similar compound as acceptor" }
	 * 
	 * @param ec_number_or_synonyme
	 *           A EC Number in the form "EC1.2.3.4" or "1.2.3.4" or " 1. 2. 3 .
	 *           4" (space is ignored while matching) Also a synonym to a EC
	 *           number can be used, in this case a lookup to the known
	 *           synonyms is done to get the corresponding EC number.
	 * @param lazy
	 *           If lazy is TRUE, then the service is inited in background,
	 *           this method will return immeadeately, but might not return reliable results
	 *           until the database is inited.
	 * @return A string array of all matching categories or descriptions.
	 */
	public static List<String> getEnzymeClasses(String ec_number_or_synonyme, boolean lazy) {
		noteRequest();
		initService(lazy);
		if (ec_number_or_synonyme.toUpperCase().startsWith("EC:"))
			ec_number_or_synonyme = ec_number_or_synonyme.substring("ec:".length());
		List<String> classes = new ArrayList<String>();
		
		// check if the given parameter is a synonym
		EnzymeEntry eze = getEnzymeInformation(ec_number_or_synonyme, lazy);
		// if info is found, use this as the ID
		if (eze != null)
			ec_number_or_synonyme = eze.getID();
		synchronized (enzymeClassEntries) {
			ec_number_or_synonyme = StringManipulationTools.stringReplace(ec_number_or_synonyme, "<html>", "");
			ec_number_or_synonyme = StringManipulationTools.stringReplace(ec_number_or_synonyme, "</html>", "");
			ec_number_or_synonyme = StringManipulationTools.stringReplace(ec_number_or_synonyme, "<br>", "");
			ec_number_or_synonyme = StringManipulationTools.stringReplace(ec_number_or_synonyme, "<br/>", "");
			for (EnzClassEntry ece : enzymeClassEntries) {
				if (ece.isValidMatchFor(ec_number_or_synonyme)) {
					classes.add(ece.description);
				}
			}
		}
		return classes;
	}
	
	/**
	 * Get a EnzymeEntry with the associated information.
	 * 
	 * @param ec_or_synonyme
	 *           A EC number (1.2.3.4) or known synonyme.
	 * @return NULL, if no info is found or a corresponding entry.
	 */
	public static EnzymeEntry getEnzymeInformation(String ec_or_synonyme, boolean lazy) {
		noteRequest();
		initService(lazy);
		if (ec_or_synonyme.toUpperCase().startsWith("EC:"))
			ec_or_synonyme = ec_or_synonyme.substring("ec:".length());
		if (ec_or_synonyme.toUpperCase().startsWith("EC "))
			ec_or_synonyme = ec_or_synonyme.substring("ec ".length());
		EnzymeEntry result = enzymeEntries.get(ec_or_synonyme);
		if (result == null) {
			ec_or_synonyme = StringManipulationTools.stringReplace(ec_or_synonyme, "<html>", "");
			ec_or_synonyme = StringManipulationTools.stringReplace(ec_or_synonyme, "</html>", "");
			ec_or_synonyme = StringManipulationTools.stringReplace(ec_or_synonyme, "<br>", "");
			ec_or_synonyme = StringManipulationTools.stringReplace(ec_or_synonyme, "<br/>", "");
			result = knownEnzymeAlternativeNamesEntries.get(ec_or_synonyme
					.toUpperCase());
		}
		return result;
	}
	
	/**
	 * @return The release information of the enzyme.dat file.
	 */
	public static String getReleaseVersionForEnzymeInformation() {
		noteRequest();
		initService(false);
		return enzymeDBrelease;
	}
	
	/**
	 * Get the number of nodes in this graph which could possible be
	 * valid EC numbers. It is not checked if the EC number is existent,
	 * meaning a EC number like "6.6.6.666" which probably does not exist
	 * would increase the number count.
	 * 
	 * @param graph
	 *           All nodes from this graph will be checked
	 * @return The number of nodes which have a <code>QuadNumber</code> valid
	 *         structure, meaning, a name of substance name like a.b.c.d, where abcd are either
	 *         numbers or "-".
	 */
	public static int getNumberOfEnzymeNodes(Graph graph) {
		int cnt = 0;
		for (Node n : graph.getNodes()) {
			try {
				String sn = AttributeHelper.getLabel(n, null);
				if (sn != null) {
					QuadNumber qn = new QuadNumber(sn);
					if (qn.isValidQuadNumber())
						cnt++;
				}
			} catch (StringIndexOutOfBoundsException se) {
				// not a valid ec number string as the node name results in
				// failing to create a quadnumber
			}
		}
		return cnt;
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
		FolderPanel res = new FolderPanel("<html>SIB Enzyme Database<br><small>" +
				"(contains information about enzyme IDs, names and synonyms)");
		res.setFrameColor(Color.LIGHT_GRAY, null, 1, 5);
		
		int b = 5; // normal border
		int bB = 1; // border around action buttons
		
		if (!showEmpty)
			initService(false);
		
		boolean internalAvailable = isDatabaseAvailable(true);
		boolean externalAvailable = isDatabaseAvailable(false);
		String status1 = "";
		String status2 = "";
		if (internalAvailable)
			status1 = "<html><b>Database is online</b>";
		else
			status1 = "<html>Embedded database file not available";
		
		if (externalAvailable)
			status2 = "<html><b>Database is online</b>";
		else
			status2 = "<html>Database file not available";
		
		if (externalAvailable || internalAvailable) {
			if (externalAvailable) {
				
				status2 += "<br>";
				
				String modifiedTime = GravistoService.getFileModificationDateAndTime(getFile("enzyme.dat"),
						"unknown version (file not found)");
				File f = getFile("enzyme.dat");
				if (f.exists()) {
					status2 += "&nbsp;&nbsp;database download: " + modifiedTime + "<br>";
				} else
					status2 += "&nbsp;&nbsp;" + modifiedTime + "<br>";
				status2 +=
						// "&nbsp;&nbsp;enzclass.txt: "+getReleaseVersionForEnzymeClasses()+"<br>" +
						"&nbsp;&nbsp;version: " + getReleaseVersionForEnzymeInformation() + "<br>" +
								"&nbsp;&nbsp;enzymes: " + enzymeEntries.size();
			} else
				status1 += "<br>" +
						// "&nbsp;&nbsp;enzclass.txt: "+getReleaseVersionForEnzymeClasses()+"<br>" +
						"&nbsp;&nbsp;version: " + getReleaseVersionForEnzymeInformation() + "<br>" +
						"&nbsp;&nbsp;enzymes: " + enzymeEntries.size();
		}
		
		if (internalAvailable) {
			EnzymeService.getEnzymeInformation("dummy", false);
			EnzymeService.getEnzymeClasses("dummy", false);
			if (read_enzclass_txt) {
				synchronized (enzymeClassEntries) {
					if (externalAvailable)
						status2 += "<br>&nbsp;&nbsp;enzyme classes: " + enzymeClassEntries.size();
					else
						status1 += "<br>&nbsp;&nbsp;enzyme classes: " + enzymeClassEntries.size();
				}
			} else {
				if (externalAvailable)
					status2 += "<br>&nbsp;&nbsp;problem identified: enzyme classes information could not be retrieved";
				else
					status1 += "<br>&nbsp;&nbsp;problem identified: enzyme classes information could not be retrieved";
			}
			if (read_enzyme_DB_txt) {
				synchronized (enzymeEntries) {
					if (externalAvailable)
						status2 += "<br>&nbsp;&nbsp;enzyme entries: " + enzymeEntries.size();
					else
						status1 += "<br>&nbsp;&nbsp;enzyme entries: " + enzymeEntries.size();
				}
			} else {
				if (externalAvailable)
					status2 += "<br>&nbsp;&nbsp;problem identified: enzyme entry information could not be retrieved";
				else
					status1 += "<br>&nbsp;&nbsp;problem identified: enzyme entry information could not be retrieved";
			}
		}
		
		if (showEmpty)
			status2 = "<html><b>Bringing database online...</b><br>Please wait a few moments.";
		
		if (internalAvailable)
			res.addGuiComponentRow(
					new JLabel("<html>" +
							"Embedded Files:&nbsp;<br><small>" +
							"(this database file is automatically provided,<br>" +
							"you may download a updated file and save it<br>" +
							"in the file system of your hard drive)"),
					FolderPanel.getBorderedComponent(
							new JLabel(status1), b, b, b, b),
					false);
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
	
	public static File getFile(String fileName) {
		return new File(ReleaseInfo.getAppFolderWithFinalSep() + fileName);
	}
	
	private JComponent getDownloadButton() {
		return GUIhelper.getWebsiteDownloadButton(
				"Download",
				"ftp://ftp.expasy.org/databases/enzyme",
				ReleaseInfo.getAppFolderWithFinalSep(),
				"<html>" +
						"The following URL and the target folder will be automatically opened in a few seconds:<br><br>" +
						"<code><b>ftp://ftp.expasy.org/databases/enzyme</b></code><br><br>" +
						"Please download the following files:<br>" +
						"<ul>" +
						"	<li>enzclass.txt" +
						"	<li>enzyme.dat" +
						"</ul>" +
						"After downloading them, please move these files to the following location:<br><br>" +
						"<code><b>" + ReleaseInfo.getAppFolder() + "</b></code><br><br>" +
						"After closing and re-opening this application, the Enzyme database will be<br>" +
						"available to the system.",
				new String[] {
						"ftp://ftp.expasy.org/databases/enzyme/enzclass.txt",
						"ftp://ftp.expasy.org/databases/enzyme/enzyme.dat"
				},
				"Manual download instructions (automatic download failure)",
				this);
	}
	
	private JComponent getLicenseButton() {
		return GUIhelper.getWebsiteButton("Disclaimer", "http://www.expasy.org/disclaimer.html", null, null, null);
	}
	
	private JComponent getWebsiteButton() {
		return GUIhelper.getWebsiteButton("Website", "http://www.expasy.org/enzyme", null, null, null);
	}
	
	private void pretifyButtons(ArrayList<JComponent> actionButtons) {
		for (JComponent jc : actionButtons)
			jc.setBackground(Color.white);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.editor.MemoryHog#freeMemory()
	 */
	@Override
	public void freeMemory() {
		if (doFreeMemory())
			finishedNewDownload();
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
}

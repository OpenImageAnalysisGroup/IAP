/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.HelperClass;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class TranspathService
					implements
					BackgroundTaskStatusProvider,
					FileDownloadStatusInformationProvider, HelperClass {
	private static boolean read_transpath_DB_txt = false;
	
	private static String transpathDBrelease = "unknown";
	
	private static Map<String, TranspathGene> tpGenes = new HashMap<String, TranspathGene>();
	private static Map<String, TranspathReference> tpReferences = new HashMap<String, TranspathReference>();
	private static Map<String, TranspathMolecule> tpMolecules = new HashMap<String, TranspathMolecule>();
	private static Map<String, TranspathReaction> tpReactions = new HashMap<String, TranspathReaction>();
	private static Map<String, TranspathPathway> tpPathways = new HashMap<String, TranspathPathway>();
	
	private static String status1;
	private static String status2;
	private static int statusVal = -1;
	
	public synchronized void finishedNewDownload() {
		read_transpath_DB_txt = false;
		transpathDBrelease = "unknown";
		tpGenes = new HashMap<String, TranspathGene>();
		tpReferences = new HashMap<String, TranspathReference>();
		tpMolecules = new HashMap<String, TranspathMolecule>();
		tpReactions = new HashMap<String, TranspathReaction>();
		tpPathways = new HashMap<String, TranspathPathway>();
		status1 = null;
		status2 = null;
		statusVal = -1;
	}
	
	private static synchronized void initService(boolean initInBackground) {
		if (initInBackground) {
			if (!read_transpath_DB_txt) {
				read_transpath_DB_txt = true;
				final TranspathService tps = new TranspathService();
				BackgroundTaskHelper bth = new BackgroundTaskHelper(
									new Runnable() {
										public void run() {
											statusVal = -1;
											read_transpath_DB_txt = true;
											readLigantTXTforReleaseInfo();
											readTranspathDB();
											statusVal = 100;
										}
									},
									tps,
									"<html>TRANSPATH &reg; Database",
									"<html>TRANSPATH &reg; Database Service",
									true, false);
				bth.startWork(MainFrame.getInstance());
			}
		} else {
			if (!read_transpath_DB_txt) {
				read_transpath_DB_txt = true;
				readLigantTXTforReleaseInfo();
				readTranspathDB();
			}
		}
	}
	
	/**
	 * Open and read ligand.txt and look for the release info.
	 */
	private static void readLigantTXTforReleaseInfo() {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.TRANSPATH_ACCESS))
			return;
		File f = getFile("TRANSPATHPathway.txt");
		if (f.exists()) {
			// koDBrelease = "unknown (downloaded "+DateFormat.getTimeInstance(DateFormat.DEFAULT).format(new Date(f.lastModified())+")");
			long lm = f.lastModified();
			Date lmd = new Date(lm);
			transpathDBrelease = "unknown version (file download " + lmd.toString() + ")";
		} else
			transpathDBrelease = "unknown version (file not found)";
	}
	
	/**
	 * Reads the file ko. All methods depending on info from that file
	 * should call <code>initService</code>, first. To ensure that this
	 * service is available.
	 */
	@SuppressWarnings("unchecked")
	private static void readTranspathDB() {
		tpGenes.clear();
		tpMolecules.clear();
		tpPathways.clear();
		tpReactions.clear();
		tpReferences.clear();
		
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.TRANSPATH_ACCESS))
			return;
		
		double stepSize = 100 / 5;
		int i = 0;
		analyze("gene.xml", "Gene", TranspathGene.class, (Map) tpGenes, i * stepSize, (++i) * stepSize);
		analyze("reference.xml", "Reference", TranspathReference.class, (Map) tpReferences, i * stepSize, (++i) * stepSize);
		analyze("molecule.xml", "Molecule", TranspathMolecule.class, (Map) tpMolecules, i * stepSize, (++i) * stepSize);
		analyze("reaction.xml", "Reaction", TranspathReaction.class, (Map) tpReactions, i * stepSize, (++i) * stepSize);
		analyze("pathway.xml", "Pathway", TranspathPathway.class, (Map) tpPathways, i * stepSize, (++i) * stepSize);
	}
	
	private static void analyze(String fileName, String info, Class<?> entityType,
						Map<String, TranspathEntityType> entries, double startProgress, double endProgress) {
		status1 = "Analyse " + info + " information...";
		statusVal = (int) startProgress;
		InputSource input = getFileInputSource(fileName);
		if (input == null) {
			// TODO: Somehow report missing file
		} else {
			try {
				status1 = "Parse XML...";
				// Create a builder factory
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setValidating(false);
				factory.setNamespaceAware(false);
				factory.setXIncludeAware(false);
				
				// Create a handler to handle the SAX events generated during parsing
				DefaultHandler handler = new TranspathXMLparser(entries, entityType, info);
				// Create the builder and parse the file
				factory.newSAXParser().parse(input, handler);
				((TranspathEntity) entityType.newInstance()).printTodo();
				status1 = "File information analysed (" + entries.size() + ")";
				System.out.println("File information analysed (" + entries.size() + ")");
			} catch (IOException err) {
				status2 = "Error in reading file info!";
				ErrorMsg.addErrorMessage(err);
			} catch (SAXException err) {
				ErrorMsg.addErrorMessage(err);
			} catch (ParserConfigurationException err) {
				ErrorMsg.addErrorMessage(err);
			} catch (InstantiationException err) {
				ErrorMsg.addErrorMessage(err);
			} catch (IllegalAccessException err) {
				ErrorMsg.addErrorMessage(err);
			}
		}
		statusVal = (int) endProgress;
	}
	
	public static BufferedReader getFileReader(String fileName) {
		try {
			return new BufferedReader(new FileReader(ReleaseInfo.getAppFolderWithFinalSep() + fileName));
		} catch (Exception e) {
			try {
				ClassLoader cl = TranspathService.class.getClassLoader();
				return new BufferedReader(new InputStreamReader(cl.getResourceAsStream(fileName)));
			} catch (Exception e2) {
				ErrorMsg.addErrorMessage("Consult Help/Database Status to fix this problem: File not found: " + fileName);
				return null;
			}
		}
	}
	
	public static InputSource getFileInputSource(String fileName) {
		try {
			InputSource is = new InputSource(new FileInputStream(new File(ReleaseInfo.getAppFolderWithFinalSep() + fileName)));
			is.setSystemId(ReleaseInfo.getAppFolderWithFinalSep() + fileName);
			return is;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("Consult Help/Database Status to fix this problem: File not found: " + fileName);
			return null;
		}
	}
	
	private static File getFile(String fileName) {
		return new File(ReleaseInfo.getAppFolderWithFinalSep() + fileName);
	}
	
	public static Collection<TranspathPathway> getPathways() {
		initService(false);
		return tpPathways.values();
	}
	
	/**
	 * @return The release information of the compound file.
	 *         Read out of ligant.txt.
	 */
	public static String getReleaseVersionForTranspath() {
		initService(false);
		return transpathDBrelease;
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
		// pleaseStop = true;
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
	
	public String getDescription() {
		return "";
	}
	
	public JComponent getStatusPane(boolean showEmpty) {
		
		initService(false);
		
		FolderPanel res = new FolderPanel("<html>" +
							"TRANSPATH &reg; Database<br><small>" +
							"(" +
							"contains information about signal transduction molecules and reactions)");
		res.setFrameColor(Color.LIGHT_GRAY, null, 1, 5);
		
		int b = 5; // normal border
		int bB = 1; // border around action buttons
		
		boolean externalAvailable = read_transpath_DB_txt && tpPathways != null && tpPathways.size() > 0;
		String status2 = "";
		
		if (externalAvailable)
			status2 = "<html><b>Database is online</b>";
		else
			status2 = "<html>Database file not available";
		
		if (externalAvailable) {
			status2 += "<br>&nbsp;&nbsp;" + transpathDBrelease;
			status2 += "<br>&nbsp;&nbsp;Pathways: " + tpPathways.size();
		}
		
		res.addGuiComponentRow(
							new JLabel("<html>" +
												"Downloaded Files:&nbsp;"),
							FolderPanel.getBorderedComponent(
												new JLabel(status2), b, b, b, b),
							false);
		
		ArrayList<JComponent> actionButtons = new ArrayList<JComponent>();
		actionButtons.add(getWebsiteButton());
		actionButtons.add(getLicenseButton());
		actionButtons.add(getDownloadButton());
		
		pretifyButtons(actionButtons);
		
		res.addGuiComponentRow(
							new JLabel("<html>" +
												"Visit Website(s)"),
							TableLayout.getMultiSplit(actionButtons, TableLayoutConstants.PREFERRED, bB, bB, bB, bB),
							false);
		
		res.layoutRows();
		return res;
	}
	
	private JComponent getDownloadButton() {
		return GUIhelper.getWebsiteButton(
							"Download",
							"http://www.biobase-international.com", ReleaseInfo.getAppFolder(),
							"<html>" +
												"The TRANSPATH database is available from the website<br>" +
												"http://www.biobase-international.com<br>" +
												"A explanation on how to prepare the database files, available from<br>" +
												"Biobase for free to academic users and at a cost for commercial users,<br>" +
												"for this application may be inquired from the authors<br>" +
												"of this application, and _not_ from biobase." +
												"The specially prepared files need to be placed in following location:<br><br>" +
												"<code><b>" + ReleaseInfo.getAppFolder() + "</b></code><br><br>" +
												"After closing and re-opening this application, the TRANSPATH &reg; database<br>" +
												"will be available to the system.",
							"Download Instructions");
	}
	
	private JComponent getLicenseButton() {
		return GUIhelper.getWebsiteButton("License", "http://www.biobase-international.com", null, null, null);
	}
	
	private JComponent getWebsiteButton() {
		return GUIhelper.getWebsiteButton("Website", "http://www.biobase-international.com", null, null, null);
	}
	
	private void pretifyButtons(ArrayList<JComponent> actionButtons) {
		for (JComponent jc : actionButtons) {
			((JButton) jc).setBackground(Color.white);
		}
	}
	
	public static TranspathReaction getReaction(String reactionID) {
		return tpReactions.get(reactionID);
	}
	
	public static TranspathPathway getPathway(String pathwayID) {
		return tpPathways.get(pathwayID);
	}
	
	public static TranspathMolecule getMolecule(String moleculeID) {
		return tpMolecules.get(moleculeID);
	}
}
/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 08.06.2005 by Christian Klukas
 */
package org;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashSet;

import javax.swing.JApplet;
import javax.swing.JOptionPane;

public class ReleaseInfo implements HelperClass {
	private static Release currentRelease = Release.DEBUG;
	
	public static Release getRunningReleaseStatus() {
		return currentRelease;
	}
	
	public static void setRunningReleaseStatus(Release currentReleaseStatus) {
		currentRelease = currentReleaseStatus;
	}
	
	private static HashSet<FeatureSet> enabledFeatures = new HashSet<FeatureSet>();
	private static HashSet<FeatureSet> disabledFeatures = new HashSet<FeatureSet>();
	
	public static void enableFeature(FeatureSet fs) {
		enabledFeatures.add(fs);
	}
	
	public static void disableFeature(FeatureSet fs) {
		disabledFeatures.add(fs);
	}
	
	public static boolean getIsAllowedFeature(FeatureSet fs) {
		
		try {
			// String s = getAppFolder();
		} catch (Exception e) {
			if (fs == FeatureSet.GravistoJavaHelp)
				return false;
			if (fs == FeatureSet.KEGG_ACCESS)
				return false;
			if (fs == FeatureSet.KEGG_ACCESS_ENH)
				return false;
			return true;
		}
		
		if (disabledFeatures != null && disabledFeatures.contains(fs))
			return false;
		if (enabledFeatures != null && enabledFeatures.contains(fs))
			return true;
		
		switch (fs) {
			case ADDON_LOADING:
				if (ReleaseInfo.isRunningAsApplet())
					return false;
				else
					return true;
			case KEGG_ACCESS:
				if ((new File(getAppFolderWithFinalSep() + "license_kegg_accepted")).exists())
					return true;
				else
					return false;
			case KEGG_ACCESS_ENH:
				if (!(currentRelease == Release.RELEASE_PUBLIC
									|| currentRelease == Release.KGML_EDITOR || currentRelease == Release.DEBUG))
					return false;
				if ((new File(getAppFolderWithFinalSep() + "license_kegg_accepted"))
									.exists())
					return true;
				else
					return false;
			case TRANSPATH_ACCESS:
				if (currentRelease == Release.DEBUG
									|| currentRelease == Release.RELEASE_IPK)
					return true;
				else
					return false;
			case URL_HELPTEXT:
				if (currentRelease == Release.DEBUG
									|| currentRelease == Release.RELEASE_IPK)
					return true;
				else
					return false;
			case URL_RELEASEINFO:
				if (currentRelease == Release.DEBUG
									|| currentRelease == Release.RELEASE_IPK)
					return true;
				else
					return false;
			case MetaCrop_ACCESS:
			case RIMAS_ACCESS:
				return false; // enabled by add-on
				
			case DBE_ACCESS:
				return false;
			case DATA_CARD_ACCESS:
				return false;
				// if (currentRelease==Release.DEBUG ||
				// currentRelease==Release.RELEASE_IPK)
				// return true;
				// break;
			case METHOUSE_ACCESS:
				return false;
			case FLAREX_ACCESS:
				return false;
			case SCRIPT_ACCESS:
				return true; /*
								 * if (currentRelease==Release.DEBUG) return true;
								 * break;
								 */
			case GravistoJavaHelp:
				if (currentRelease != Release.RELEASE_CLUSTERVIS
									&& currentRelease != Release.KGML_EDITOR) {
					if ((new File(getAppFolderWithFinalSep()
										+ "setting_help_enabled")).exists())
						return true;
					else
						return false;
				}
				return false;
			case TAB_LAYOUT:
				return true; /*
								 * if (currentRelease==Release.RELEASE_CLUSTERVIS ||
								 * currentRelease==Release.KGML_EDITOR ||
								 * currentRelease==Release.DEBUG) return true;
								 */
				// break;
			case STATISTIC_FUNCTIONS:
				// if (currentRelease==Release.RELEASE_IPK ||
				// currentRelease==Release.RELEASE_PUBLIC ||
				// currentRelease==Release.DEBUG )
				// return true;
				// break;
				return false;
			case TAB_PATTERNSEARCH:
				return false;
			case DATAMAPPING:
				return false;
				// if (currentRelease!=Release.RELEASE_CLUSTERVIS &&
				// currentRelease!=Release.KGML_EDITOR)
				// return true;
			case AGLET_NETWORK:
				if (currentRelease != Release.RELEASE_CLUSTERVIS
									&& currentRelease != Release.KGML_EDITOR)
					return true;
				else
					return true;
			case FUNCAT_ACCESS:
				return true;
			case SBGN:
				return false;
			case URL_NODE_ANNOTATION:
				if (currentRelease == Release.KGML_EDITOR)
					return false;
				else
					return true;
			case TOOLTIPS:
				if (currentRelease == Release.KGML_EDITOR)
					return false;
				else
					return true;
			case PATHWAY_FILE_REFERENCE:
				if (currentRelease == Release.KGML_EDITOR)
					return false;
				else
					return true;
			case MacroRecorder:
				return false; // enabled by optional plugin
			default:
				return false;
		}
	}
	
	public static String getAppFolder() {
		String appFolder = getAppFolderName();
		try {
			if (!new File(appFolder).isDirectory()) {
				boolean success = (new File(appFolder)).mkdirs();
				if (!success) {
					appFolder = System.getenv("USERPROFILE");
					if (!new File(appFolder).isDirectory()) {
						success = (new File(appFolder)).mkdirs();
					}
				}
			}
		} catch (Exception e) {
			// empty
		}
		return appFolder;
	}
	
	private static String getAppFolderName() {
		String newStyle = getAppFolderNameNewStyle();
		
		try {
			
			String oldStyle = getAppFolderNameOldStyle();
			if (!oldStyle.equals(newStyle)) {
				if (new File(oldStyle).isDirectory()) {
					
					File src = new File(oldStyle);
					File tgt = new File(newStyle);
					boolean success = src.renameTo(tgt);
					if (success) {
						System.out.println("Moved user preferences from "
											+ oldStyle + " to " + newStyle + "!");
						JOptionPane.showMessageDialog(null, "<html>"
											+ "<h3>New Preferences Folder</h3>"
											+ "User preferences have been moved:<br>"
											+ "<ul>" + "<li>Old: " + oldStyle + ""
											+ "<li>New: " + newStyle + "</ul>",
											"Information", JOptionPane.INFORMATION_MESSAGE);
						
					}
				}
			}
			
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		return newStyle;
	}
	
	private static String getAppFolderNameNewStyle() {
		String home = System.getProperty("user.home");
		boolean windows = false;
		if (SystemInfo.isMac())
			home = home + getFileSeparator() + "Library" + getFileSeparator()
								+ "Preferences";
		else {
			if (new File(home + getFileSeparator() + "AppData"
								+ getFileSeparator() + "Roaming").isDirectory()) {
				home = home + getFileSeparator() + "AppData"
									+ getFileSeparator() + "Roaming";
				windows = true;
			} else {
				String hhh = System.getenv("APPDATA");
				if (hhh != null) {
					if (new File(hhh).isDirectory()) {
						home = hhh;
						windows = true;
					}
				}
			}
		}
		
		if (SystemInfo.isMac() || windows) {
			if (getRunningReleaseStatus() == Release.KGML_EDITOR)
				return home + getFileSeparator() + "KGML_EDITOR";
			else
				return home + getFileSeparator() + HomeFolder.WIN_MAC_HOMEFOLDER;
		} else {
			if (getRunningReleaseStatus() == Release.KGML_EDITOR)
				return home + getFileSeparator() + ".kgml_editor";
			else
				return home + getFileSeparator() + HomeFolder.LINUX_HOMEFOLDER;
		}
	}
	
	private static String getAppFolderNameOldStyle() {
		String home = System.getProperty("user.home");
		if (getRunningReleaseStatus() == Release.KGML_EDITOR)
			return home + getFileSeparator() + ".kgml_editor";
		else
			return home + getFileSeparator() + HomeFolder.WIN_MAC_HOMEFOLDER_OLD;
	}
	
	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
	
	public static String getAppFolderWithFinalSep() {
		return getAppFolder() + getFileSeparator();
	}
	
	public static String getAppWebURL() {
		if (getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "http://kgml-ed.ipk-gatersleben.de";
		else
			return "http://vanted.ipk-gatersleben.de";
	}
	
	private static String helpIntro = "";
	
	public static void setHelpIntroductionText(String statusMessage) {
		helpIntro = statusMessage;
	}
	
	public static String getHelpIntroductionText() {
		return helpIntro;
	}
	
	private static boolean applet = false;
	private static JApplet appletContext = null;
	
	public static void setRunningAsApplet(JApplet appletContext) {
		applet = true;
		ReleaseInfo.appletContext = appletContext;
	}
	
	public static boolean isRunningAsApplet() {
		return applet;
	}
	
	private static boolean firstRun = false;
	
	public static void setIsFirstRun(boolean b) {
		firstRun = b;
	}
	
	public static boolean isFirstRun() {
		return firstRun;
	}
	
	private static boolean updateCheckRun = false;
	private static String lastVersion = null;
	
	public static UpdateInfoResult isUpdated() {
		if (!updateCheckRun)
			return UpdateInfoResult.UNKNOWN;
		if (lastVersion != null)
			return UpdateInfoResult.UPDATED;
		else
			return UpdateInfoResult.NOT_UPDATED;
	}
	
	/**
	 * @param currentVersion
	 * @return null, if not updated / "", if updated but old version is unknown
	 *         / old version string, if updated.
	 */
	public static synchronized String getOldVersionIfAppHasBeenUpdated(
						String currentVersion) {
		synchronized (ReleaseInfo.class) {
			if (updateCheckRun) {
				return lastVersion;
			}
			File f = new File(getAppFolderWithFinalSep() + "version");
			String oldVersion;
			try {
				oldVersion = getTextFileContent(f);
			} catch (Exception e) {
				oldVersion = null;
			}
			try {
				if (f.exists())
					f.delete();
				Writer output = new BufferedWriter(new FileWriter(f));
				output.write(currentVersion);
				output.close();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage("Warning: could not save current version information.");
			}
			if (oldVersion != null && oldVersion.length() > 0
								&& !oldVersion.equalsIgnoreCase(currentVersion))
				lastVersion = oldVersion;
			else
				lastVersion = null;
			updateCheckRun = true;
			return lastVersion;
		}
	}
	
	public static String getTextFileContent(File aFile) throws Exception {
		StringBuilder res = new StringBuilder();
		BufferedReader input = new BufferedReader(new FileReader(aFile));
		try {
			String line = null;
			while ((line = input.readLine()) != null) {
				if (res.length() > 0)
					res.append(System.getProperty("line.separator"));
				res.append(line);
			}
		} finally {
			input.close();
		}
		return res.toString();
	}
	
	public static String getAppSubdirFolder(String folderName) {
		String folder = getAppFolderWithFinalSep() + folderName;
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdir();
		return folder;
	}
	
	public static String getAppSubdirFolder(String subDir1, String subDir2) {
		String folder1 = getAppFolderWithFinalSep() + subDir1;
		String folder2 = folder1 + getFileSeparator() + subDir2;
		File dir = new File(folder1);
		if (!dir.exists())
			dir.mkdir();
		File dir2 = new File(folder2);
		if (!dir2.exists())
			dir2.mkdir();
		return folder2;
	}
	
	public static String getAppSubdirFolderWithFinalSep(String folderName) {
		return getAppSubdirFolder(folderName) + getFileSeparator();
	}
	
	public static String getAppSubdirFolderWithFinalSep(String folderName,
						String folderName2) {
		return getAppSubdirFolder(folderName, folderName2) + getFileSeparator();
	}
	
	/**
	 * @return
	 */
	public static JApplet getApplet() {
		return appletContext;
	}
}

package de.ipk.ag_ba.gui.webstart;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.w3c.dom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * OWNER : Experiment Name : DATE : Image
 * klukas : 1107BA_Barley :experiment_314341235432.bin
 * klukas : 1107BA_Barley :experiment_314341235432.header
 * klukas : 1107BA_Barley :experiment_314341235432.annotation
 * klukas : 1107BA_Barley :experiment_314341235432.files
 * klukas : 1107BA_Barley :2011-04-27 : xyz1.png
 * klukas : 1107BA_Barley :2011-04-27 : xyz2.png
 * klukas : 1107BA_Barley :2011-04-27 : xyz3.png
 * klukas : 1107BA_Barley :2011-04-27 : xyz4.png
 * klukas : 1107BA_Barley :2011-04-27 : xyz5.png
 * klukas : 1107BA_Barley :2011-04-27 : xyz6.png
 */
public class HSMfolderTargetDataManager {
	public static final String DIRECTORY_FOLDER_NAME = "index";
	
	private static final String CONDITION_FOLDER_NAME = "conditions";
	
	private final String path;
	
	public HSMfolderTargetDataManager(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public String prepareAndGetTargetFileForContentIndex(String zefn) {
		String res = path + File.separator + DIRECTORY_FOLDER_NAME;
		if (!new File(res).exists())
			new File(res).mkdirs();
		return res + File.separator + zefn;
	}
	
	public String prepareAndGetTargetFileForConditionIndex(String zefn) {
		String res = path + File.separator + CONDITION_FOLDER_NAME;
		if (!new File(res).exists())
			new File(res).mkdirs();
		return res + File.separator + zefn;
	}
	
	public String prepareAndGetDataFileNameAndPath(ExperimentHeaderInterface experimentHeader, Date optSnapshotTime, String zefn) {
		String subPath = getTargetDirectory(experimentHeader, optSnapshotTime);
		if (subPath.startsWith(DIRECTORY_FOLDER_NAME) || subPath.startsWith("bbb_"))
			throw new UnsupportedOperationException("Invalid storage subpath calculated for experiment " + experimentHeader.getExperimentname()
					+ ". May not start with " + DIRECTORY_FOLDER_NAME + " or " + CONDITION_FOLDER_NAME + "!");
		String res = path + File.separator + "data" + File.separator + subPath;
		if (!new File(res).exists())
			new File(res).mkdirs();
		return res + File.separator + filterBadChars(zefn);
	}
	
	public String prepareAndGetPreviewFileNameAndPath(ExperimentHeaderInterface experimentHeader, Date optSnapshotTime, String zefn) {
		String subPath = getTargetDirectory(experimentHeader, optSnapshotTime);
		if (subPath.startsWith(DIRECTORY_FOLDER_NAME) || subPath.startsWith(CONDITION_FOLDER_NAME))
			throw new UnsupportedOperationException("Invalid storage subpath calculated for experiment " + experimentHeader.getExperimentname()
					+ ". May not start with " + DIRECTORY_FOLDER_NAME + " or " + CONDITION_FOLDER_NAME + "!");
		String res = path + File.separator + "icons" + File.separator + subPath;
		if (!new File(res).exists())
			new File(res).mkdirs();
		return res + File.separator + filterBadChars(zefn);
	}
	
	private String filterBadChars(String string) {
		String s = StringManipulationTools.UnicodeToURLsyntax(string);
		s = StringManipulationTools.stringReplace(s, "%32", " ");
		s = StringManipulationTools.stringReplace(s, "%95", "_");
		s = StringManipulationTools.stringReplace(s, "%40", "(");
		s = StringManipulationTools.stringReplace(s, "%41", ")");
		s = StringManipulationTools.stringReplace(s, "%45", "-");
		s = StringManipulationTools.stringReplace(s, "%46", ".");
		s = StringManipulationTools.stringReplace(s, "..", "%46%46");
		return s;
	}
	
	private static GregorianCalendar cal = new GregorianCalendar();
	
	private String getTargetDirectory(ExperimentHeaderInterface experimentHeader, Date optSnapshotTime) {
		if (optSnapshotTime != null)
			cal.setTime(optSnapshotTime);
		String pre = "";
		if (experimentHeader.getExperimentType() != null && experimentHeader.getExperimentType().length() > 0)
			pre = experimentHeader.getExperimentType() + File.separator;
		return pre +
				filterBadChars(experimentHeader.getCoordinator()) + File.separator +
				filterBadChars(experimentHeader.getExperimentname()) +
				(optSnapshotTime == null ? "" : File.separator +
						cal.get(GregorianCalendar.YEAR) + "-" + digit2(cal.get(GregorianCalendar.MONTH) + 1) + "-" + digit2(cal.get(GregorianCalendar.DAY_OF_MONTH)));
	}
	
	public static String digit2(int i) {
		if (i < 10)
			return "0" + i;
		else
			return "" + i;
	}
	
	public static String digit3(int i) {
		if (i < 10)
			return "00" + i;
		else
			if (i < 100)
				return "0" + i;
			else
				return "" + i;
	}
	
	public static ExperimentInterface getExperiment(ExperimentHeaderInterface header, BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		// hsm:/Users/klukas/Library/Preferences/VANTED/local-iap-hsm/aaa_directory/1303994908266_0_klukas_WT_H3.iap.index.csv
		String indexFileName = header.getDatabaseId().substring("hsm:".length());
		String fileName = indexFileName.substring(indexFileName.lastIndexOf(File.separator) + File.separator.length());
		// /Users/klukas/Library/Preferences/VANTED/local-iap-hsm/aaa_directory/1303994908266_0_klukas_WT_H3.iap.index.csv
		String hsmFolder = indexFileName.substring(0, indexFileName.lastIndexOf(File.separator));
		// /Users/klukas/Library/Preferences/VANTED/local-iap-hsm/aaa_directory
		// cd ..
		hsmFolder = hsmFolder.substring(0, hsmFolder.lastIndexOf(File.separator));
		// /Users/klukas/Library/Preferences/VANTED/local-iap-hsm
		HSMfolderTargetDataManager hsm = new HSMfolderTargetDataManager(hsmFolder);
		String experimentDirectory = hsm.getTargetDirectory(header, null);
		// /Users/klukas/Library/Preferences/VANTED/local-iap-hsm/Phenotyping\ Experiment\ \(unknown\ greenhouse\)/LemnaTec\ \(APH\)/klukas/WT_H3
		String fileNameOfExperimentFile = fileName.substring(0, fileName.length() - ".iap.index.csv".length()) + ".iap.vanted.bin";
		String loadFile = hsmFolder + File.separator + "data" + File.separator + experimentDirectory + File.separator + fileNameOfExperimentFile;
		// /Users/klukas/Library/Preferences/VANTED/local-iap-hsm/Phenotyping\ Experiment\ \(unknown\ greenhouse\)/LemnaTec\
		// \(APH\)/klukas/WT_H3/1303994908266_0_klukas_WT_H3.iap.vanted.bin
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document w3Doc = builder.parse(new File(loadFile));
		Experiment md = Experiment.getExperimentFromDOM(w3Doc);
		updateFileUrls(md);
		md.setHeader(header);
		return md;
	}
	
	private static void updateFileUrls(Experiment md) {
		for (NumericMeasurementInterface nmi : Substance3D.getAllFiles(md)) {
			if (nmi instanceof BinaryMeasurement) {
				BinaryMeasurement bm = (BinaryMeasurement) nmi;
				
			}
		}
	}
}

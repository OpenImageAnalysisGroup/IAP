package de.ipk.ag_ba.gui.webstart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.ipk.ag_ba.commands.vfs.ActionDataExportToVfs;
import de.ipk.ag_ba.hsm.HsmResourceIoHandler;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.databases.DBTable;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.MyImageIOhelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;

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
public class HSMfolderTargetDataManager implements DatabaseTarget {
	public static final String DIRECTORY_FOLDER_NAME = "index";
	public static final String DATA_FOLDER_NAME = "data";
	private static final String CONDITION_FOLDER_NAME = "conditions";
	
	private final String path;
	private String prefix;
	
	public HSMfolderTargetDataManager(String prefix, String path) {
		this.prefix = prefix;
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
	
	public String prepareAndGetDataFileNameAndPath(ExperimentHeaderInterface experimentHeader, Long optSnapshotTime, String zefn) {
		String subPath = getTargetDirectory(experimentHeader, optSnapshotTime);
		if (subPath.startsWith(DIRECTORY_FOLDER_NAME) || subPath.startsWith("bbb_"))
			throw new UnsupportedOperationException("Invalid storage subpath calculated for experiment " + experimentHeader.getExperimentName()
					+ ". May not start with " + DIRECTORY_FOLDER_NAME + " or " + CONDITION_FOLDER_NAME + "!");
		String res = path + File.separator + DATA_FOLDER_NAME + File.separator + subPath;
		if (!new File(res).exists())
			new File(res).mkdirs();
		return res + File.separator + filterBadChars(zefn);
	}
	
	public String prepareAndGetPreviewFileNameAndPath(ExperimentHeaderInterface experimentHeader, Long optSnapshotTime, String zefn) {
		String subPath = getTargetDirectory(experimentHeader, optSnapshotTime);
		if (subPath.startsWith(DIRECTORY_FOLDER_NAME) || subPath.startsWith(CONDITION_FOLDER_NAME))
			throw new UnsupportedOperationException("Invalid storage subpath calculated for experiment " + experimentHeader.getExperimentName()
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
		s = StringManipulationTools.stringReplace(s, "%44", ",");
		s = StringManipulationTools.stringReplace(s, "%45", "-");
		s = StringManipulationTools.stringReplace(s, "%46", ".");
		s = StringManipulationTools.stringReplace(s, "..", "%46%46");
		return s;
	}
	
	public String getTargetDirectory(ExperimentHeaderInterface experimentHeader, Long optSnapshotTime) {
		GregorianCalendar cal = new GregorianCalendar();
		if (optSnapshotTime != null)
			cal.setTime(new Date(optSnapshotTime));
		String pre = "";
		if (experimentHeader.getExperimentType() != null && experimentHeader.getExperimentType().length() > 0)
			pre = experimentHeader.getExperimentType() + File.separator;
		return pre +
				filterBadChars(experimentHeader.getCoordinator()) + File.separator +
				filterBadChars(experimentHeader.getExperimentName()) +
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
		HSMfolderTargetDataManager hsm = new HSMfolderTargetDataManager(
				HsmResourceIoHandler.getPrefix(hsmFolder), hsmFolder);
		String experimentDirectory = hsm.getTargetDirectory(header, null);
		// /Users/klukas/Library/Preferences/VANTED/local-iap-hsm/Phenotyping\ Experiment\ \(unknown\ greenhouse\)/LemnaTec\ \(APH\)/klukas/WT_H3
		String fileNameOfExperimentFile = fileName.substring(0, fileName.length() - ".iap.index.csv".length()) + ".iap.vanted.bin";
		String loadFile = hsmFolder + File.separator + DATA_FOLDER_NAME + File.separator + experimentDirectory + File.separator + fileNameOfExperimentFile;
		// /Users/klukas/Library/Preferences/VANTED/local-iap-hsm/Phenotyping\ Experiment\ \(unknown\ greenhouse\)/LemnaTec\
		// \(APH\)/klukas/WT_H3/1303994908266_0_klukas_WT_H3.iap.vanted.bin
		Experiment md = loadExperimentFromFile(header, status, loadFile);
		return md;
	}
	
	protected static Experiment loadExperimentFromFile(ExperimentHeaderInterface header, BackgroundTaskStatusProviderSupportingExternalCall status,
			String loadFile) throws ParserConfigurationException, SAXException, IOException {
		if (status != null)
			status.setCurrentStatusText1("Create XML DOM...");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document w3Doc = builder.parse(new File(loadFile));
		if (status != null)
			status.setCurrentStatusText1("Convert DOM to Experiment....");
		Experiment md = Experiment.getExperimentFromDOM(w3Doc);
		if (status != null)
			status.setCurrentStatusText1("Update URLs...");
		updateFileUrls(md);
		md.setHeader(header);
		if (status != null)
			status.setCurrentStatusText1("Experiment created");
		return md;
	}
	
	public static ExperimentInterface getExperiment(String databaseID) throws Exception {
		// hsm:/Users/klukas/Library/Preferences/VANTED/local-iap-hsm/aaa_directory/1303994908266_0_klukas_WT_H3.iap.index.csv
		String indexFileName = databaseID.substring("hsm:".length());
		
		HashMap<String, String> properties = new HashMap<String, String>();
		TextFile tf = new TextFile(indexFileName);
		properties.put("_id", "hsm:" + indexFileName);
		for (String p : tf) {
			String[] entry = p.split(",", 3);
			properties.put(entry[1], entry[2]);
		}
		ExperimentHeader eh = new ExperimentHeader(properties);
		
		return getExperiment(eh, null);
	}
	
	private static void updateFileUrls(Experiment md) {
		// hsm_media_nfs_hsm:////data/Maize Greenhouse/Klukas, Christian (BA)/1107BA_Corn/2011-03-17/label_vis.side_blob19907.png#1107BA1153 (287).png
		// loaded:////data/Maize Greenhouse/Klukas, Christian (BA)/1107BA_Corn/2011-03-16/c_1107BA1151 fluo.side DEG_271 REPL_014 day_015 2011-03-16 07_36_18
		// 1107BA1151 (271).png#1107BA1151 (271).png
		for (NumericMeasurementInterface nmi : Substance3D.getAllFiles(md)) {
			if (nmi instanceof BinaryMeasurement) {
				BinaryMeasurement bm = (BinaryMeasurement) nmi;
				if (bm.getURL() != null) {
					if ((bm.getURL() + "").startsWith("hsm__")) {
						bm.setURL(new IOurl(
								StringManipulationTools.stringReplace(
										(bm.getURL() + ""),
										"hsm__", "hsm_")));
					}
					if ((bm.getLabelURL() + "").startsWith("hsm__"))
						bm.setLabelURL(new IOurl(
								StringManipulationTools.stringReplace(
										(bm.getLabelURL() + ""),
										"hsm__", "hsm_")));
				}
			}
		}
	}
	
	@Override
	public LoadedImage saveImage(LoadedImage limg, boolean keepRemoteURLs_safe_space) throws Exception {
		ExperimentHeaderInterface ehi = limg.getParentSample().getParentCondition().getExperimentHeader();
		long snapshotTime = limg.getParentSample().getSampleFineTimeOrRowId();
		String desiredFileName = limg.getURL().getFileName();
		if (desiredFileName != null && desiredFileName.contains("#"))
			desiredFileName = desiredFileName.substring(desiredFileName.indexOf("#") + 1);
		String substanceName = limg.getSubstanceName();
		desiredFileName = ActionDataExportToVfs.determineBinaryFileName(snapshotTime, substanceName, limg, limg);// + "#" + desiredFileName;
		{
			String targetFileNameFullRes = prepareAndGetDataFileNameAndPath(ehi, snapshotTime, desiredFileName.split("#")[0]);
			InputStream mainStream = limg.getInputStream();
			ResourceIOManager.copyContent(mainStream, new FileOutputStream(new File(targetFileNameFullRes)));
			
			IOurl url = limg.getURL();
			
			String fullPath = new File(targetFileNameFullRes).getParent();
			String subPath = fullPath.substring(getPath().length());
			if (url != null) {
				url.setPrefix(getPrefix());
				url.setDetail(subPath);
				url.setFileName(desiredFileName);
			}
		}
		{
			String targetFileNamePreview = prepareAndGetPreviewFileNameAndPath(ehi, snapshotTime, desiredFileName.split("#")[0]);
			MyByteArrayInputStream previewStream = MyImageIOhelper.getPreviewImageStream(limg.getLoadedImage());
			ResourceIOManager.copyContent(previewStream, new FileOutputStream(new File(targetFileNamePreview)));
		}
		if (!keepRemoteURLs_safe_space) {
			// copy label and annotation files...
		}
		return limg;
	}
	
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public void saveVolume(LoadedVolume volume, Sample3D s3d, MongoDB m, DBTable sample, InputStream threeDvolumePreviewIcon,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		throw new UnsupportedOperationException("Saving volumes using this method is not yet supported!");
	}
}

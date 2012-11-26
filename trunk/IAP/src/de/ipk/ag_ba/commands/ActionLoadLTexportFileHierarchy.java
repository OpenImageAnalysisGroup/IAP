package de.ipk.ag_ba.commands;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.ErrorMsg;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLemnaTecExperimentNavigation;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk.ag_ba.postgresql.Snapshot;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableDataStringRow;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class ActionLoadLTexportFileHierarchy extends AbstractNavigationAction {
	
	public ActionLoadLTexportFileHierarchy(String tooltip) {
		super(tooltip);
	}
	
	private NavigationButton src;
	private ExperimentReference loaded_experiment = null;
	private ArrayList<String> messages = new ArrayList<String>();
	private boolean getInput;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		loaded_experiment = null;
		messages.clear();
		getInput = true;
		File inp = OpenFileDialogService.getDirectoryFromUser("Select Input Folder");
		getInput = false;
		if (inp != null && inp.isDirectory()) {
			processDir(inp);
		}
		this.src = src;
	}
	
	private void processDir(File inp) {
		System.out.println(SystemAnalysis.getCurrentTime() + ">Scanning input folder '" + inp.getPath() + "'...");
		ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
		long overallStorageSizeInBytes = 0;
		Timestamp storageTimeOfFirstInfoFile = null;
		String metadataFileName = SystemOptions.getInstance().getString("File_Import", "Meta-Data-Table-File-Name", "metadata.csv");
		String zoomSettingsFileName = SystemOptions.getInstance().getString("File_Import", "Zoom-Settings-Value-File-Name", "zoom.txt");
		boolean foundMetadata = false;
		ArrayList<TableDataStringRow> metadata = null;
		
		// initialize these setting, so that they show up in the settings GUI in good ordering
		for (int sideViewIndex = 1; sideViewIndex <= 12; sideViewIndex++) {
			SystemOptions.getInstance().getString("File_Import", "Camera Labels Side-View//Post-fix " + sideViewIndex,
					" SV" + sideViewIndex);
		}
		for (int sideViewIndex = 1; sideViewIndex <= 12; sideViewIndex++) {
			int def = 0;
			if (sideViewIndex == 2)
				def = 90;
			SystemOptions.getInstance().getInteger("File_Import",
					"Camera Labels Side-View//Post-fix " + sideViewIndex + " angle", def);
		}
		
		String preferedZoomSetting = null;
		
		for (String snapshotDirName : inp.list()) {
			if (snapshotDirName.equals(metadataFileName)) {
				foundMetadata = true;
				TableData td = TableData.getTableData(new File(inp + File.separator + metadataFileName));
				metadata = td.getRowsAsStringValues();
			}
			if (snapshotDirName.equals(zoomSettingsFileName)) {
				try {
					TextFile t = new TextFile(new File(inp + File.separator + zoomSettingsFileName));
					preferedZoomSetting = t.get(0).trim();
				} catch (IOException e1) {
					ErrorMsg.addErrorMessage(e1);
				}
			}
			File f = new File(inp.getPath() + File.separator + snapshotDirName);
			snapshotDirName = f.getAbsolutePath();
			File infoFile = new File(snapshotDirName + File.separator + "info.txt");
			if (!infoFile.exists()) {
				if (infoFile.isDirectory())
					messages.add("ERROR: Skipping folder " + snapshotDirName + ", it contains no info.txt file!");
				continue; // this sub folder contains no snapshot info
			} else
				if (storageTimeOfFirstInfoFile == null || infoFile.lastModified() < storageTimeOfFirstInfoFile.getTime())
					storageTimeOfFirstInfoFile = new Timestamp(infoFile.lastModified());
			try {
				InfoFile info = new InfoFile(new TextFile(infoFile));
				if (info.containsKey("IdTag")) {
					// this info file specifies the plant ID, timestamp, weight and watering info
					Snapshot s = new Snapshot();
					s.setId_tag(info.get("IdTag"));
					s.setCreator(info.get("Creator"));
					Integer wa = info.getWaterAmountData();
					if (wa != null)
						s.setWater_amount(wa);
					Double web = info.getWeightBeforeData();
					if (web != null)
						s.setWeight_before(web);
					Double wea = info.getWeightAfterData();
					if (wea != null)
						s.setWeight_before(wea);
					s.setMeasurement_label(info.get("Measurement"));
					s.setTime_stamp(new Timestamp(info.getTimestampTime()));
					snapshots.add(s);
					// the sub directories contain the image data
					for (String cameraLabelSubDir : new File(snapshotDirName).list()) {
						try {
							File cameraSubDir = new File(snapshotDirName + File.separator + cameraLabelSubDir);
							if (!cameraSubDir.isDirectory()) {
								if (!cameraLabelSubDir.equals("info.txt") && !cameraLabelSubDir.equals(".DS_Store"))
									messages.add("ERROR: Folder " + snapshotDirName + " contains an unknown file '" + cameraLabelSubDir
											+ "' which is not processed!");
							} else {
								// process camera label sub-dir
								File imageSnapshotInfoFile = new File(snapshotDirName + File.separator +
										cameraLabelSubDir + File.separator + "info.txt");
								if (!imageSnapshotInfoFile.exists())
									continue;
								InfoFile infoForCameraSnapshot = new InfoFile(new TextFile(imageSnapshotInfoFile));
								Snapshot imageSnapshot = new Snapshot();
								imageSnapshot.setId_tag(s.getId_tag());
								imageSnapshot.setCreator(s.getCreator());
								imageSnapshot.setTime_stamp(s.getTimestamp());
								imageSnapshot.setMeasurement_label(s.getMeasurement_label());
								Double xf = infoForCameraSnapshot.getXfactor();
								if (xf != null)
									imageSnapshot.setXfactor(xf);
								Double yf = infoForCameraSnapshot.getYfactor();
								if (yf != null)
									imageSnapshot.setXfactor(yf);
								if (infoForCameraSnapshot.get("Camera label") == null) {
									messages.add("INFO: Snapshot info file " + imageSnapshotInfoFile.getAbsolutePath()
											+ "' contains no 'Camera label'! Using folder name!");
									infoForCameraSnapshot.put("Camera label", cameraLabelSubDir);
								}
								imageSnapshot.setCamera_label(infoForCameraSnapshot.get("Camera label"));
								if (imageSnapshot.getCamera_label().endsWith(
										SystemOptions.getInstance().getString("File_Import", "Camera Labels Top-View//Post-fix", " TV"))) {
									imageSnapshot.setCamera_label(imageSnapshot.getCamera_label().toLowerCase() + ".top");
									imageSnapshot.setCamera_label(StringManipulationTools.stringReplace(imageSnapshot.getCamera_label(), " tv.", "."));
									imageSnapshot.setUserDefinedCameraLabeL(imageSnapshot.getCamera_label());
								} else {
									for (int sideViewIndex = 1; sideViewIndex <= 12; sideViewIndex++) {
										if (imageSnapshot.getCamera_label().endsWith(
												SystemOptions.getInstance().getString("File_Import", "Camera Labels Side-View//Post-fix " + sideViewIndex,
														" SV" + sideViewIndex))) {
											imageSnapshot.setCamera_label(imageSnapshot.getCamera_label().toLowerCase() + ".side");
											imageSnapshot.setCamera_label(StringManipulationTools.stringReplace(imageSnapshot.getCamera_label().toUpperCase(), " SV"
													+ sideViewIndex + ".", ".").toLowerCase());
											int def = 0;
											if (sideViewIndex == 2)
												def = 90;
											int degree = SystemOptions.getInstance().getInteger("File_Import",
													"Camera Labels Side-View//Post-fix " + sideViewIndex + " angle", def);
											imageSnapshot.setUserDefinedCameraLabeL(imageSnapshot.getCamera_label() + "." + degree);
										}
									}
								}
								String ffn = snapshotDirName + File.separator +
										cameraLabelSubDir + File.separator + "0_0.png";
								File ff = new File(ffn);
								if (ff.exists()) {
									overallStorageSizeInBytes += ff.length();
									imageSnapshot.setPath_image(ffn);
									snapshots.add(imageSnapshot);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							messages.add("ERROR: Could not process directory '" + cameraLabelSubDir + "': " + e.getMessage());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				messages.add("ERROR: Could not process file '" + f.getPath() + "' or sub-directory info: " + e.getMessage());
			}
		}
		messages.add("INFO: Snapshot-count: " + snapshots.size());
		HashMap<String, Condition> optIdTag2condition = null;
		if (!foundMetadata || metadata == null) {
			messages.add("<b>Found no metadata.csv file (" + metadataFileName + ") in the selected folder!</b>");
			messages
					.add("The metadata.csv file may contain up to 5 columns (or more, which would be ignored), which, if available, are processed in the following order:");
			messages.add("Column " + SystemOptions.getInstance().getInteger("File_Import", "File-Import-Columns//Plant-ID", 1) + ": Plant ID");
			messages.add("Column " + SystemOptions.getInstance().getInteger("File_Import", "File-Import-Columns//Species", 2) + ": Species Name");
			messages.add("Column " + SystemOptions.getInstance().getInteger("File_Import", "File-Import-Columns//Genotype", 3) + ": Genotype");
			messages.add("Column " + SystemOptions.getInstance().getInteger("File_Import", "File-Import-Columns//Treatment", 4) + ": Treatment");
			messages.add("Column " + SystemOptions.getInstance().getInteger("File_Import", "File-Import-Columns//Sequence", 5) + ": Sequence");
			messages.add("The CSV file should not contain column headers.");
		} else {
			// process metadata
			optIdTag2condition = new HashMap<String, Condition>();
			for (TableDataStringRow tdsr : metadata) {
				String id = tdsr.getPlantID();
				if (id != null) {
					Condition c = new Condition(null);
					c.setSpecies(tdsr.getSpecies());
					c.setGenotype(tdsr.getGenotype());
					c.setTreatment(tdsr.getTreatment());
					c.setSequence(tdsr.getSequence());
					optIdTag2condition.put(id, c);
				}
			}
		}
		// get experiment from snapshot info...
		ExperimentInterface e;
		try {
			Snapshot s = snapshots.size() > 0 ? snapshots.get(0) : null;
			ExperimentHeader eh = new ExperimentHeader(s != null ? s.getMeasurement_label() : "(no data)");
			if (s != null)
				eh.setCoordinator(s.getCreator());
			if (storageTimeOfFirstInfoFile != null)
				eh.setStorageTime(storageTimeOfFirstInfoFile);
			eh.setImportusername(SystemAnalysis.getUserName());
			eh.setSizekb(overallStorageSizeInBytes / 1024);
			eh.setExperimenttype(IAPexperimentTypes.ImportedDataset + "");
			eh.setGlobalOutlierInfo(preferedZoomSetting);
			e = LemnaTecDataExchange.getExperimentFromSnapshots(eh, snapshots, optIdTag2condition);
			loaded_experiment = new ExperimentReference(e);
		} catch (Exception e1) {
			e1.printStackTrace();
			messages.add("ERROR: Could not convert scnapshot info to Experiment structure: " + e1.getMessage());
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (loaded_experiment != null)
			res.add(new NavigationButton(new ActionMongoOrLemnaTecExperimentNavigation(loaded_experiment), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("<b>Messages:</b><br><br>" + StringManipulationTools.getStringList(messages, "<br>"));
	}
	
	@Override
	public String getDefaultTitle() {
		if (getInput)
			return "Select Input Folder...";
		else
			return "Load LT File Export";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-System-File-Manager-64.png";
	}
}

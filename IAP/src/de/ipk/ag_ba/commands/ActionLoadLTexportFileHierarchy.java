package de.ipk.ag_ba.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.commands.load_lt.InfoFile;
import de.ipk.ag_ba.commands.load_lt.TableDataHeadingRow;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.postgresql.LTdataExchange;
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
	private TreeMap<String, ExperimentReference> loaded_experiments = null;
	private final ArrayList<String> messages = new ArrayList<String>();
	private boolean getInput;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (loaded_experiments == null || loaded_experiments.size() == 0) {
			loaded_experiments = new TreeMap<String, ExperimentReference>();
			messages.clear();
			getInput = true;
			File inp = OpenFileDialogService.getDirectoryFromUser("Select Input Folder");
			getInput = false;
			if (inp != null && inp.isDirectory()) {
				try {
					processDir(inp);
				} catch (UnsupportedOperationException ue) {
					messages.add(ue.getMessage());
				} catch (Exception ue) {
					messages.add("Unexpected error: " + ue.getMessage());
				}
			}
		}
		this.src = src;
	}
	
	private void processDir(File inp) {
		System.out.println(SystemAnalysis.getCurrentTime() + ">Scanning input folder '" + inp.getPath() + "'...");
		ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
		long overallStorageSizeInBytes = 0;
		Timestamp storageTimeOfFirstInfoFile = null;
		
		boolean foundMetadata = false;
		HashMap<String, ArrayList<TableDataStringRow>> metadata = new HashMap<String, ArrayList<TableDataStringRow>>();
		HashMap<String, TableDataHeadingRow> metadataHeading = new HashMap<String, TableDataHeadingRow>();
		
		getStatusProvider().setCurrentStatusText1("Get file list (" + inp.getPath() + ")");
		String[] list = inp.list();
		int todo = list.length;
		int idx = 0;
		boolean storageCheck = SystemOptions.getInstance().getBoolean("File Import", "Determine Storage Size", false);
		for (String snapshotDirName : list) {
			idx++;
			getStatusProvider().setCurrentStatusValueFine(100d * idx / todo);
			getStatusProvider().setCurrentStatusText1("Processing folder " + idx + "/" + todo);
			if (snapshotDirName.equals(".DS_Store"))
				continue;
			String post = "";
			if (storageCheck) {
				if (overallStorageSizeInBytes / 1024 / 1024 < 10 * 1000)
					post = ", " + overallStorageSizeInBytes / 1024 / 1024 + " MB";
				else
					post = ", " + overallStorageSizeInBytes / 1024 / 1024 / 1024 + " GB";
			}
			getStatusProvider().setCurrentStatusText2("Found " + snapshots.size() + " data points" + post);
			if (snapshotDirName.endsWith(".csv") || snapshotDirName.endsWith(".xlsx") || snapshotDirName.endsWith(".xls")) {
				foundMetadata = true;
				String fn = inp + File.separator + snapshotDirName;
				TableData td = null;
				try {
					File f = new File(fn);
					if (!f.getName().startsWith("~$"))
						td = TableData.getTableData(f, true);
				} catch (Exception e) {
					throw new UnsupportedOperationException("Could not load meta data file '" + fn + "'.<br>" +
							"Please make sure that the file is not currently loaded in another program!<br>" +
							"Observed Error: " + e.getMessage());
				}
				if (td != null) {
					if (metadata.get(fn) == null)
						metadata.put(fn, new ArrayList<TableDataStringRow>());
					ArrayList<TableDataStringRow> lines = td.getRowsAsStringValues();
					if (lines.size() > 0) {
						TableDataStringRow firstLine = lines.get(0);
						boolean foundHeadingIndicator = false;
						String headingIndicator = SystemOptions.getInstance().getString("File Import", "Meta-Data-Heading-Indicator", "Genotype");
						for (String v : firstLine.getValues()) {
							if (v != null && v.equalsIgnoreCase(headingIndicator)) {
								foundHeadingIndicator = true;
								break;
							}
						}
						if (foundHeadingIndicator) {
							lines.remove(0);
							metadataHeading.put(fn, new TableDataHeadingRow(firstLine.getMap()));
						}
					}
					metadata.get(fn).addAll(lines);
				}
			}
			File f = new File(inp.getPath() + File.separator + snapshotDirName);
			snapshotDirName = f.getAbsolutePath();
			File infoFile = new File(snapshotDirName + File.separator + "info.txt");
			if (storageCheck && !infoFile.exists()) {
				if (infoFile.isDirectory())
					messages.add("ERROR: Skipping folder " + snapshotDirName + ", it contains no info.txt file!");
				continue; // this sub folder contains no snapshot info
			} else
				try {
					if (storageTimeOfFirstInfoFile == null || infoFile.lastModified() < storageTimeOfFirstInfoFile.getTime())
						storageTimeOfFirstInfoFile = new Timestamp(infoFile.lastModified());
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
							s.setWeight_after(wea);
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
										imageSnapshot.setYfactor(yf);
									if (infoForCameraSnapshot.get("Camera label") == null) {
										messages.add("WARNING: Snapshot info file " + imageSnapshotInfoFile.getAbsolutePath()
												+ "' contains no 'Camera label'! Using folder name!");
										infoForCameraSnapshot.put("Camera label", cameraLabelSubDir);
									}
									imageSnapshot.setCamera_label(infoForCameraSnapshot.get("Camera label"));
									imageSnapshot.setCamera_config(infoForCameraSnapshot.get("Camera label"));
									imageSnapshot.setUserDefinedCameraLabeL(imageSnapshot.getCamera_label());
									imageSnapshot.setCamera_label(LTdataExchange.getIAPcameraNameFromConfigLabel(imageSnapshot.getCamera_label()));
									String ffn = snapshotDirName + File.separator + cameraLabelSubDir + File.separator + "0_0.png";
									if (storageCheck) {
										File ff = new File(ffn);
										if (ff.exists()) {
											overallStorageSizeInBytes += ff.length();
											imageSnapshot.setPath_image(ffn);
											snapshots.add(imageSnapshot);
										}
									} else {
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
					if (e instanceof FileNotFoundException) {
						// empty
					} else {
						e.printStackTrace();
						messages.add("ERROR: Could not process file '" + f.getPath() + "' or sub-directory info: " + e.getMessage());
					}
				}
		}
		messages.add("Processed " + snapshots.size() + " exported snapshots.");
		messages.add("Next steps:");
		messages
				.add("<ol><li><b>Open dataset(s) and check the correct assignment of meta-data (species/genotype/treatment/...) with the 'View Data' command.</b><br>If not correct, open another window (right click any command bar at the top, and go to 'Settings &gt; Metadata &gt; Columns'. Check the content of the metadata file, stored in the location of the exported snapshots, and compare the metadata prepared metadata table columns with the IAP input metadata settings. Consult the documentation for further information.");
		messages.add("<li><b>For further processing the created datasets need to be copied to a storage site (MongoDB database / local folder / VFS)!</b></ol>");
		getStatusProvider().setCurrentStatusValueFine(0d);
		getStatusProvider().setCurrentStatusText1("Found " + snapshots.size() + " data points");
		getStatusProvider().setCurrentStatusText2("(images, weight or watering info)");
		HashMap<String, Condition> optIdTag2condition = null;
		if (!foundMetadata || metadata == null) {
			messages.add("<b>Found no meta-data files (files ending with file extension name .csv, .xlsx or .xls) in the selected folder!</b><br>" +
					"Check the 'File Import'-Settings for the meta-data assignment of column-values!<br>" +
					"Modify the settings and repeat the loading, in case data is not assigned to the pre-defined fields as desired.");
		} else {
			// process metadata
			optIdTag2condition = new HashMap<String, Condition>();
			for (String fn : metadata.keySet()) {
				TableDataHeadingRow heading = metadataHeading.get(fn);
				if (heading == null)
					heading = new TableDataHeadingRow(null); // default heading info
				ArrayList<TableDataStringRow> md = metadata.get(fn);
				LTdataExchange.extendId2ConditionList(optIdTag2condition, heading, md, true);
			}
		}
		// get experiment from snapshot info...
		HashMap<String, ArrayList<Snapshot>> measurementLabel2snapshots = new HashMap<String, ArrayList<Snapshot>>();
		if (snapshots != null)
			for (Snapshot s : snapshots) {
				String ml = s.getMeasurement_label();
				if (!measurementLabel2snapshots.containsKey(ml))
					measurementLabel2snapshots.put(ml, new ArrayList<Snapshot>());
				measurementLabel2snapshots.get(ml).add(s);
			}
		getStatusProvider().setCurrentStatusValueFine(0d);
		getStatusProvider().setCurrentStatusText1("Found " + snapshots.size() + " snapshots");
		getStatusProvider().setCurrentStatusText2("Create Experiment Structures...");
		todo = measurementLabel2snapshots.size();
		idx = 0;
		for (String ml : measurementLabel2snapshots.keySet()) {
			ArrayList<Snapshot> sl = measurementLabel2snapshots.get(ml);
			try {
				ExperimentInterface e;
				Snapshot s = snapshots.size() > 0 ? sl.get(0) : null;
				ExperimentHeader eh = new ExperimentHeader(s != null ? s.getMeasurement_label() : "(no data)");
				if (s != null)
					eh.setCoordinator(s.getCreator());
				if (storageTimeOfFirstInfoFile != null)
					eh.setStorageTime(storageTimeOfFirstInfoFile);
				eh.setImportusername(SystemAnalysis.getUserName());
				if (storageCheck)
					eh.setSizekb(overallStorageSizeInBytes / 1024);
				eh.setExperimenttype(IAPexperimentTypes.ImportedDataset + "");
				e = LTdataExchange.getExperimentFromSnapshots(eh, sl, optIdTag2condition);
				getStatusProvider().setCurrentStatusText1("Created " + eh.getExperimentName() + ".");
				getStatusProvider().setCurrentStatusText2("Create Experiment Structures...");
				loaded_experiments.put(e.getName(), new ExperimentReference(e));
			} catch (Exception e1) {
				e1.printStackTrace();
				messages.add("ERROR: Could not convert scnapshot info to Experiment structure: " + e1.getMessage() + " (Experiment " + ml + ")");
			}
			idx++;
			getStatusProvider().setCurrentStatusValueFine(100d * idx / todo);
		}
		getStatusProvider().setCurrentStatusValue(100);
		getStatusProvider().setCurrentStatusText1("Processing finished");
		getStatusProvider().setCurrentStatusText2("");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (loaded_experiments != null && loaded_experiments.size() > 0)
			for (ExperimentReference loaded_experiment : loaded_experiments.values())
				res.add(new NavigationButton(new ActionMongoOrLTexperimentNavigation(loaded_experiment), src.getGUIsetting()));
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
		return new MainPanelComponent("<b>Results</b><br><br><hr><br>" + StringManipulationTools.getStringList(messages, "<br><br>"));
	}
	
	@Override
	public String getDefaultTitle() {
		if (getInput)
			return "Select Input Folder...";
		else
			return "Create IAP Dataset(s) from LT Dump";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-System-File-Manager-64.png";
	}
}

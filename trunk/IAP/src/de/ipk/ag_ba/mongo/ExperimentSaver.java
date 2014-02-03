package de.ipk.ag_ba.mongo;

import info.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.HashType;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.postgresql.LTftpHandler;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.MyImageIOhelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.LoadedNetwork;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.IntVolumeInputStream;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeInputStream;

public class ExperimentSaver implements RunnableOnDB {
	private final ExperimentInterface experiment;
	private final boolean keepDataLinksToDataSource_safe_space;
	private final BackgroundTaskStatusProviderSupportingExternalCall status;
	private final ThreadSafeOptions err;
	private DB db;
	
	private final HashType hashType;
	private final MongoDBhandler mh;
	private final ArrayList<ExperimentHeaderInterface> experimentList;
	private final MongoDB m;
	private final boolean skipStorage;
	
	public ExperimentSaver(
			MongoDB m,
			MongoDBhandler mh, HashType hashType,
			ExperimentInterface experiment, boolean keepDataLinksToDataSource_safe_space,
			BackgroundTaskStatusProviderSupportingExternalCall status, ThreadSafeOptions err, ArrayList<ExperimentHeaderInterface> experimentList,
			boolean filesAreAlreadySavedSkipStorage) {
		this.m = m;
		this.experimentList = experimentList;
		this.mh = mh;
		this.hashType = hashType;
		this.experiment = experiment;
		this.keepDataLinksToDataSource_safe_space = keepDataLinksToDataSource_safe_space;
		this.status = status;
		this.err = err;
		skipStorage = filesAreAlreadySavedSkipStorage;
	}
	
	@Override
	public void setDB(DB db) {
		this.db = db;
	}
	
	@Override
	public void run() {
		try {
			storeExperimentInnerCall(experiment, db, status, keepDataLinksToDataSource_safe_space, experimentList);
		} catch (Exception e) {
			err.setParam(0, e);
		}
	}
	
	void storeExperimentInnerCall(
			final ExperimentInterface experiment, final DB db,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final boolean keepDataLinksToDataSource_safe_space,
			ArrayList<ExperimentHeaderInterface> experimentList) throws InterruptedException, ExecutionException {
		
		for (ExperimentHeaderInterface ehi : experimentList) {
			if (ehi.getOriginDbId() != null && !ehi.getOriginDbId().isEmpty()
					&& ehi.getOriginDbId().equals(experiment.getHeader().getDatabaseId())) {
				// preserve outlier info (add values, available at destination)
				if (ehi.getGlobalOutlierInfo() != null && !ehi.getGlobalOutlierInfo().isEmpty()) {
					String outliers = StringManipulationTools.getMergedStringItems(
							experiment.getHeader().getGlobalOutlierInfo(),
							ehi.getGlobalOutlierInfo(),
							"//");
					experiment.getHeader().setGlobalOutlierInfo(outliers);
				}
				// preserve sequence/stress info (add values, available at destination)
				if (ehi.getSequence() != null && !ehi.getSequence().isEmpty()) {
					String sequence = StringManipulationTools.getMergedStringItems(
							experiment.getHeader().getSequence(),
							ehi.getSequence(),
							"//");
					experiment.getHeader().setSequence(sequence);
				}
				// preserve remark info (add values, available at destination)
				if (ehi.getRemark() != null && !ehi.getRemark().isEmpty()) {
					String remark = StringManipulationTools.getMergedStringItems(
							experiment.getHeader().getRemark(),
							ehi.getRemark(),
							"//");
					experiment.getHeader().setRemark(remark);
				}
				// preserve analysis settings (if actual settings are empty)
				if (ehi.getSettings() != null && !ehi.getSettings().isEmpty()) {
					if (experiment.getHeader().getSettings() == null || experiment.getHeader().getSettings().isEmpty()) {
						String settings = ehi.getSettings();
						experiment.getHeader().setSettings(settings);
					}
				}
				experiment.getHeader().setExperimenttype(ehi.getExperimentType());
			}
		}
		
		String rem = experiment.getHeader().getRemark();
		if (rem != null && rem.contains("//")) {
			rem = StringManipulationTools.getMergedStringItems(experiment.getHeader().getRemark(), "", "//");
		}
		
		System.out.println(">>> " + SystemAnalysis.getCurrentTime());
		System.out.println("STORE EXPERIMENT: " + experiment.getName());
		System.out.println("DB-ORIGIN       : " + experiment.getHeader().getOriginDbId());
		System.out.println("DB-ID           : " + experiment.getHeader().getDatabaseId());
		System.out.println("DB              : " + experiment.getHeader().getDatabase());
		System.out.println("Exp.type        : " + experiment.getHeader().getExperimentType());
		System.out.println("Group           : " + experiment.getHeader().getImportusergroup());
		System.out.println("Username        : " + experiment.getHeader().getImportusername());
		System.out.println("Remark          : " + experiment.getHeader().getRemark());
		System.out.println(">>> KEEP EXTERNAL REFS?  : " + keepDataLinksToDataSource_safe_space);
		// experiment.getHeader().setImportusername(SystemAnalysis.getUserName());
		
		final HashMap<String, Object> attributes = new HashMap<String, Object>();
		
		final ObjectRef overallFileSize = new ObjectRef();
		overallFileSize.addLong(0);
		
		final ObjectRef startTime = new ObjectRef();
		startTime.setLong(System.currentTimeMillis());
		
		final DBCollection substances = db.getCollection("substances");
		
		final DBCollection conditions = db.getCollection("conditions");
		
		final ObjectRef lastTransferSum = new ObjectRef();
		lastTransferSum.setLong(0);
		final ObjectRef lastTime = new ObjectRef();
		lastTime.setLong(-1);
		final ObjectRef count = new ObjectRef();
		count.setLong(0);
		
		final StringBuilder errors = new StringBuilder();
		final int numberOfBinaryData = Substance3D.countMeasurementValues(experiment, new MeasurementNodeType[] {
				MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME, MeasurementNodeType.NETWORK });
		
		boolean updatedSizeAvailable = false;
		boolean determineSizeFromSource = false;
		
		if (determineSizeFromSource) {
			if (status != null)
				status.setCurrentStatusText1(SystemAnalysis.getCurrentTime() + ">Determine Size");
			{
				if (experiment.getHeader().getDatabaseId() != null && !experiment.getHeader().getDatabaseId().startsWith("hsm:")) {
					long l = Substance3D.getFileSize(Substance3D.getAllFiles(experiment));
					experiment.getHeader().setSizekb(l / 1024);
					updatedSizeAvailable = true;
				}
			}
		}
		
		final CollectionStorage cols = new CollectionStorage(db, MongoDB.getEnsureIndex());
		
		LinkedList<SubstanceInterface> sl = new LinkedList<SubstanceInterface>(experiment);
		Runtime r = Runtime.getRuntime();
		final ArrayList<String> substanceIDs = new ArrayList<String>();
		final ObjectRef errorCount = new ObjectRef();
		errorCount.setLong(0);
		final ThreadSafeOptions tsoIdxS = new ThreadSafeOptions();
		final int substanceCount = sl.size();
		ArrayList<LocalComputeJob> wait = new ArrayList<LocalComputeJob>();
		while (!sl.isEmpty()) {
			final SubstanceInterface s = sl.poll();
			// if (status != null && status.wantsToStop())
			// break;
			Runnable rrr = new Runnable() {
				@Override
				public void run() {
					try {
						tsoIdxS.addInt(1);
						processSubstanceSaving(cols, db, status, keepDataLinksToDataSource_safe_space, attributes,
								overallFileSize, startTime, substances, conditions,
								lastTransferSum, lastTime, count, errors, numberOfBinaryData, substanceIDs,
								errorCount, s, tsoIdxS.getInt(), substanceCount);
					} catch (InterruptedException e) {
						MongoDB.saveSystemErrorMessage("Could save experiment substance " + s.getName() + ", experiment " + experiment.getName(), e);
						ErrorMsg.addErrorMessage(e);
						errorCount.addLong(1);
						errors.append("<li>Error saving substance " + s.getName());
					}
				}
			};
			wait.add(BackgroundThreadDispatcher.addTask(rrr, "Substance Saving " + s.getName()));
		} // substance
		BackgroundThreadDispatcher.waitFor(wait);
		
		System.out.print(SystemAnalysis.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
				+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB>");
		if (status != null)
			status.setCurrentStatusText1(SystemAnalysis.getCurrentTime() + ">SAVE SUB-ELEMENTS OF SUBSTANCES FINISHED");
		
		// if (substances != null && safeOneTimeSave)
		// processSubstanceSaving(status, substances, conditions, dbSubstances, substance2conditions);
		if (status != null)
			status.setCurrentStatusText1(SystemAnalysis.getCurrentTime() + ">SAVE OF SUBSTANCE-DB ELEMENTS FINISHED");
		
		if (status != null)// || (status != null && !status.wantsToStop()))
			status.setCurrentStatusText1(SystemAnalysis.getCurrentTime() + ">Finalize Storage");
		
		// l = overallFileSize.getLong(); // in case of update the written bytes are not the right size
		experiment.getHeader().setStorageTime(new Date());
		
		experiment.fillAttributeMap(attributes);
		BasicDBObject dbExperiment = new BasicDBObject(attributes);
		dbExperiment.put("substance_ids", substanceIDs);
		
		DBCollection experiments = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString());
		
		if (true) {// || (status != null && !status.wantsToStop())
			experiments.insert(dbExperiment);
			String id = dbExperiment.get("_id").toString();
			System.out.println(SystemAnalysis.getCurrentTime() + ">STORED EXPERIMENT " + experiment.getHeader().getExperimentName() + " // DB-ID: " + id);
			for (ExperimentHeaderInterface eh : experiment.getHeaders()) {
				eh.setDatabaseId(id);
			}
			boolean storeXML = false;
			if (storeXML) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">STORE BINARY XML");
				try {
					// store XML experiment bin and attach Hash value to header
					// when loading a experiment, the quick XML bin is loaded and then the header updated from the database content
					db.getCollection("xmlFiles");
					GridFS gridfsExperimentStorage = new GridFS(db, "gridfsExperimentStorage");
					GridFSDBFile fffMain = gridfsExperimentStorage.findOne(id);
					if (fffMain != null)
						gridfsExperimentStorage.remove(fffMain);
					System.out.println(SystemAnalysis.getCurrentTime() + ">CREATE BINARY XML BYTES (UTF-8)");
					String ss = experiment.toStringWithErrorThrowing();
					byte[] bb = ss.getBytes("UTF-8");
					ss = null;
					ByteArrayInputStream in = new ByteArrayInputStream(bb);
					bb = null;
					System.out.println(SystemAnalysis.getCurrentTime() + ">STORE BINARY XML BYTES");
					gridfsExperimentStorage.createFile(in, id, true);
					in = null;
					System.out.println(SystemAnalysis.getCurrentTime() + ">BINARY XML STORED");
				} catch (Exception e) {
					MongoDB.saveSystemErrorMessage("Could not save quick XML file for experiment " + experiment.getName(), e);
				}
			}
			if (!updatedSizeAvailable)
				m.updateExperimentSize(db, experiment, status);
		}
		// System.out.print(SystemAnalysis.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
		// + " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB>");
		
		if (errorCount.getLong() > 0) {
			MainFrame.showMessageDialog(
					"<html>" + "The following files cound not properly be processed:<ul>" + errors.toString() + "</ul> "
							+ "", "Errors");
		}
	}
	
	private void processSubstanceSaving(final CollectionStorage cols,
			final DB db, final BackgroundTaskStatusProviderSupportingExternalCall status,
			final boolean keepDataLinksToDataSource_safe_space, final HashMap<String, Object> attributes, final ObjectRef overallFileSize,
			final ObjectRef startTime, DBCollection substances, final DBCollection conditions, final ObjectRef lastTransferSum, final ObjectRef lastTime,
			final ObjectRef count, final StringBuilder errors,
			final int numberOfBinaryData, ArrayList<String> substanceIDs, final ObjectRef errorCount,
			SubstanceInterface s,
			int substanceIndex, int substanceCount) throws InterruptedException {
		if (status != null) {
			status.setCurrentStatusValueFine(100d * substanceIndex / substanceCount);
			status.setCurrentStatusText1("Save " + substanceIndex + "/" + substanceCount);
			status.setCurrentStatusText2("<small><font color='gray'>(" + s.getName() + ")</font></small>");
		}
		BasicDBObject substance;
		synchronized (attributes) {
			attributes.clear();
			s.fillAttributeMap(attributes);
			substance = new BasicDBObject(filter(attributes));
		}
		// dbSubstances.add(substance);
		
		final ArrayList<String> conditionIDs = new ArrayList<String>();
		final HashSet<String> savedUrls = new HashSet<String>();
		ArrayList<DBObject> toBeSaved = new ArrayList<DBObject>();
		try {
			for (final ConditionInterface c : s) {
				BasicDBObject condition = processConditionSaving(cols, db, status,
						keepDataLinksToDataSource_safe_space, attributes,
						overallFileSize, startTime, conditions,
						errorCount,
						lastTransferSum, lastTime, count, errors,
						numberOfBinaryData, conditionIDs, c, mh, m, savedUrls);
				
				conditionIDs.add(condition.getString("_id"));
				
				toBeSaved.add(condition);
				
				if (toBeSaved.size() >= 100) {
					conditions.insert(toBeSaved);
					toBeSaved.clear();
				}
				
			} // condition
			if (toBeSaved.size() > 0) {
				conditions.insert(toBeSaved);
				toBeSaved.clear();
			}
		} catch (Exception e) {
			MongoDB.saveSystemErrorMessage("Could save experiment conditions, experiment " + experiment.getName(), e);
			ErrorMsg.addErrorMessage(e);
			errorCount.addLong(1);
			errors.append("<li>" + e.getMessage());
		}
		processSubstanceSaving(status, substances, substance, conditionIDs);
		substanceIDs.add((substance).getString("_id"));
	}
	
	public static DatabaseStorageResult saveVolumeFile(
			DB db,
			VolumeData volume,
			ObjectRef optFileSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			HashType hashType) {
		String hash;
		try {
			InputStream iis = volume.getURL().getInputStream();
			if (iis == null)
				return DatabaseStorageResult.IO_ERROR_INPUT_NOT_AVAILABLE;
			
			hash = GravistoService.getHashFromInputStream(iis, optFileSize, hashType);
			if (hash == null)
				return DatabaseStorageResult.IO_ERROR_INPUT_NOT_AVAILABLE;
			// System.out.println("SAVE VOLUME HASH: " + hash);
			volume.getURL().setDetail(hash);
			
			GridFS gridfs_volumes = new GridFS(db, MongoGridFS.FS_VOLUMES.toString());
			DBCollection collectionA = db.getCollection(MongoGridFS.FS_VOLUMES_FILES.toString());
			
			if (MongoDB.getEnsureIndex())
				collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
			
			GridFSDBFile fff = gridfs_volumes.findOne(hash);
			if (fff != null && fff.getLength() <= 0) {
				System.out.println("Found Zero-Size File." + "");
				System.out.println("Delete Existing Volume.");
				gridfs_volumes.remove(fff);
				fff = null;
			}
			if (fff != null) {
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
			} else {
				GridFS gridfs_preview = new GridFS(db, MongoGridFS.FS_PREVIEW_FILES.toString());
				DBCollection collectionB = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
				if (MongoDB.getEnsureIndex())
					collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
				
				saveVolumeFile(gridfs_volumes, gridfs_preview, volume, optFileSize, optStatus, hash);
				fff = gridfs_volumes.findOne(hash);
				if (fff != null) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">Delete generated volume from MongoDB file system (to save space and for debugging).");
					gridfs_volumes.remove(fff);
				}
				gridfs_preview = new GridFS(db, MongoGridFS.FS_PREVIEW_FILES.toString());
				
				fff = gridfs_preview.findOne(hash);
				if (fff != null && fff.getLength() <= 0) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: OK, VOLUME PREVIEW SAVED: " + hash);
				} else
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: VOLUME PREVIEW NOT SAVED: " + hash);
				return DatabaseStorageResult.STORED_IN_DB;
			}
		} catch (Exception e) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR during volume save: " + e.getMessage());
			return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
	}
	
	public DatabaseStorageResult saveNetworkFile(DB db, NetworkData network, ObjectRef optFileSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		GridFS gridfs_networks = new GridFS(db, MongoGridFS.FS_NETWORKS.toString());
		DBCollection collectionA = db.getCollection(MongoGridFS.FS_NETWORKS_FILES.toString());
		if (MongoDB.getEnsureIndex())
			collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		GridFS gridfs_preview = new GridFS(db, MongoGridFS.FS_PREVIEW.toString());
		DBCollection collectionB = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
		if (MongoDB.getEnsureIndex())
			collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		String hash;
		try {
			hash = GravistoService.getHashFromInputStream(network.getURL().getInputStream(), optFileSize, getHashType());
			
			network.getURL().setPrefix(mh.getPrefix());
			network.getURL().setDetail(hash);
			
			GridFSDBFile fff = gridfs_networks.findOne(hash);
			if (fff != null && fff.getLength() <= 0) {
				System.out.println("Found Zero-Size File.");
				System.out.println("Delete Existing Network.");
				gridfs_networks.remove(fff);
				fff = null;
			}
			if (fff != null) {
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
			} else {
				saveNetworkFile(gridfs_networks, gridfs_preview, network, optFileSize, optStatus, hash);
				return DatabaseStorageResult.STORED_IN_DB;
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
	}
	
	private Map filter(Map map) {
		if (map == null || map.isEmpty())
			return map;
		for (Object key : map.keySet().toArray()) {
			Object val = map.get(key);
			if (val == null || ((val instanceof String) && ((String) val).isEmpty())) {
				map.remove(key);
			} else
				if ((val instanceof Double) && Double.isNaN((Double) val)) {
					map.remove(key);
				}
			
		}
		return map;
	}
	
	private BasicDBObject processConditionSaving(CollectionStorage cols, DB db,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			boolean keepDataLinksToDataSource_safe_space,
			final HashMap<String, Object> attributes, final ObjectRef overallFileSize,
			final ObjectRef startTime, DBCollection conditions,
			final ObjectRef errorCount,
			final ObjectRef lastTransferSum, final ObjectRef lastTime, final ObjectRef count,
			final StringBuilder errors, final int numberOfBinaryData,
			ArrayList<String> conditionIDs,
			ConditionInterface c,
			MongoDBhandler mh, MongoDB mo, final HashSet<String> savedUrls) throws InterruptedException, ExecutionException {
		
		BasicDBObject condition;
		synchronized (attributes) {
			attributes.clear();
			c.fillAttributeMap(attributes);
			condition = new BasicDBObject(filter(attributes));
		}
		
		List<BasicDBObject> dbSamples = new ArrayList<BasicDBObject>();
		for (SampleInterface sa : c) {
			// if (status != null && status.wantsToStop())
			// break;
			final BasicDBObject sample;
			synchronized (attributes) {
				attributes.clear();
				sa.fillAttributeMap(attributes);
				sample = new BasicDBObject(filter(attributes));
				dbSamples.add(sample);
			}
			
			boolean foundNumeric = false;
			List<BasicDBObject> dbMeasurements = new ArrayList<BasicDBObject>();
			for (Measurement m : sa) {
				if (!(m instanceof BinaryMeasurement)) {
					if (!foundNumeric && !Double.isNaN(m.getValue()))
						foundNumeric = true;
					synchronized (attributes) {
						attributes.clear();
						m.fillAttributeMap(attributes);
						BasicDBObject measurement = new BasicDBObject(filter(attributes));
						dbMeasurements.add(measurement);
					}
				}
			} // measurement
			
			if (foundNumeric) {
				// only add sample average if at least one non-NaN numeric value is found
				if (sa.size() > 0) {
					synchronized (attributes) {
						attributes.clear();
						sa.getSampleAverage().fillAttributeMap(attributes);
						BasicDBObject dbSampleAverage = new BasicDBObject(filter(attributes));
						sample.put("average", dbSampleAverage);
					}
				}
			}
			
			final List<BasicDBObject> dbImages = new ArrayList<BasicDBObject>();
			List<BasicDBObject> dbVolumes = new ArrayList<BasicDBObject>();
			List<BasicDBObject> dbNetworks = new ArrayList<BasicDBObject>();
			
			if (sa instanceof Sample3D) {
				Sample3D s3 = (Sample3D) sa;
				for (NumericMeasurementInterface m : s3.getMeasurements(new MeasurementNodeType[] {
						MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME, MeasurementNodeType.NETWORK })) {
					DatabaseStorageResult res = null;
					try {
						if (m instanceof ImageData) {
							ImageData id = (ImageData) m;
							if (skipStorage)
								res = DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
							else
								res = saveImageFileDirect(cols, db, id, overallFileSize,
										keepDataLinksToDataSource_safe_space, false, mh, hashType, mo, savedUrls);
							if (res == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
								errorCount.addLong(1);
								errors.append("<li>" + id.getURL().getFileName());
							} else {
								synchronized (attributes) {
									attributes.clear();
									id.fillAttributeMap(attributes);
									BasicDBObject dbo = new BasicDBObject(filter(attributes));
									dbImages.add(dbo);
								}
							}
							count.addLong(1);
						}
						if (m instanceof VolumeData) {
							VolumeData vd = (VolumeData) m;
							res = ExperimentSaver.saveVolumeFile(db, vd,
									overallFileSize, status, getHashType());
							if (res == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
								errorCount.addLong(1);
								errors.append("<li>" + vd.getURL().getFileName());
							} else {
								synchronized (attributes) {
									attributes.clear();
									m.fillAttributeMap(attributes);
									BasicDBObject dbo = new BasicDBObject(filter(attributes));
									dbVolumes.add(dbo);
								}
							}
						}
						if (m instanceof NetworkData) {
							NetworkData nd = (NetworkData) m;
							res = saveNetworkFile(db, nd, overallFileSize, status);
							if (res == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
								errorCount.addLong(1);
								errors.append("<li>" + nd.getURL().getFileName());
							} else {
								synchronized (attributes) {
									attributes.clear();
									m.fillAttributeMap(attributes);
									BasicDBObject dbo = new BasicDBObject(filter(attributes));
									dbNetworks.add(dbo);
								}
							}
						}
					} catch (Exception e) {
						MongoDB.saveSystemErrorMessage("Could save file experiment " + experiment.getName(), e);
						ErrorMsg.addErrorMessage(e);
						res = DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
					}
					if (status != null)// && storageResults.size() == 0)
						updateStatusForFileStorage(status, overallFileSize, lastTransferSum, lastTime, count, numberOfBinaryData, res, errorCount, startTime);
				} // binary measurement
			}
			if (dbMeasurements.size() > 0)
				sample.put("measurements", dbMeasurements);
			
			if (dbImages.size() > 0)
				sample.put("images", dbImages);
			
			if (dbVolumes.size() > 0)
				sample.put("volumes", dbVolumes);
			if (dbNetworks.size() > 0)
				sample.put("networks", dbVolumes);
		} // sample
		
		condition.put("samples", dbSamples);
		return condition;
	}
	
	private long lastN = -1;
	
	private void updateStatusForFileStorage(
			BackgroundTaskStatusProviderSupportingExternalCall status,
			ObjectRef overallFileSize, ObjectRef lastTransferSum,
			ObjectRef lastTime, ObjectRef count,
			int numberOfBinaryData, DatabaseStorageResult res, ObjectRef errorCount,
			ObjectRef startTime) {
		if (lastN == count.getLong())
			return;
		lastN = count.getLong();
		double prog = count.getLong() * (100d / numberOfBinaryData);
		String err = "";
		if (errorCount.getLong() > 0)
			err = " (" + errorCount.getLong() + " errors)";
		if (res != null)
			status.setCurrentStatusText1(count.getLong() + "/" + numberOfBinaryData + ": " + res + err + " // " + SystemAnalysis.getCurrentTimeInclSec());
		status.setCurrentStatusValueFine(prog);
		long currentTime = System.currentTimeMillis();
		
		if (lastTime.getLong() > 0) {
			String mbs = "";
			// if (overallFileSize != null)
			// mbs = ", copied " + overallFileSize.getLong() / 1024 / 1024 + " MB";
			long transfered = overallFileSize.getLong() - lastTransferSum.getLong();
			if (transfered > 0) {
				String lastSpeed = SystemAnalysis.getDataTransferSpeedString(transfered, lastTime.getLong(), currentTime);
				String overallSpeed = SystemAnalysis.getDataTransferSpeedString(overallFileSize.getLong(), startTime.getLong(), currentTime);
				status.setCurrentStatusText2("last " + lastSpeed +
						" (" + SystemAnalysis.getWaitTime(currentTime - lastTime.getLong()) + ")<br>overall " + overallSpeed
						+ mbs);
			}
		}
		
		lastTransferSum.setLong(overallFileSize.getLong());
		lastTime.setLong(currentTime);
	}
	
	private static long saveVolumeFile(GridFS gridfs_volumes, GridFS gridfs_preview, VolumeData id, ObjectRef optFileSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus, String hash) throws Exception {
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Create Outputstream");
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Calculate hash");
		
		System.out.println("Saving volume with hash: " + hash);
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Save Volume");
		
		GridFSDBFile vvv = gridfs_volumes.findOne(hash);
		boolean removeExistingVolumeFile = true;
		if (removeExistingVolumeFile && vvv != null) {
			gridfs_preview.remove(vvv);
			vvv = null;
		}
		boolean saveVolume = true;
		long saved = 0;
		if (saveVolume && vvv == null) {
			IntVolumeInputStream is = (IntVolumeInputStream) id.getURL().getInputStream();
			if (is != null) {
				// System.out.println("AVAIL: " + is.available());
				// System.out.println("TARGET-LENGTH: " + (id.getDimensionX() * id.getDimensionY() * id.getDimensionZ() * 4));
				LoadedVolumeExtension lv;
				if (id instanceof LoadedVolumeExtension)
					lv = (LoadedVolumeExtension) id;
				else
					lv = new LoadedVolumeExtension(IOmodule.loadVolume(id));
				
				boolean skipVolumeSave = true;
				if (!skipVolumeSave) {
					GridFSInputFile inputFile = gridfs_volumes.createFile(is, hash);
					// inputFile.setFilename(hash);
					// id.getURL().getFileName());
					inputFile.save();
					saved += inputFile.getLength();
					
					// System.out.println("SAVED VOLUME: " + id.toString() + " // SIZE: " + inputFile.getLength());
				} else {
					// create 512x512 animated GIF instead of saving the full volume cube
					StopWatch ss = new StopWatch(SystemAnalysis.getCurrentTime() + ">CREATE GIF 512x512", true);
					InputStream inps = IOmodule.getThreeDvolumeRenderViewGif(lv, optStatus);
					ss.printTime();
					GridFSInputFile inputFile = gridfs_volumes.createFile(inps, hash);
					inputFile.save();
					saved += inputFile.getLength();
				}
				GridFSDBFile fff = gridfs_preview.findOne(hash);
				boolean removeExistingPreviewFile = true;
				if (removeExistingPreviewFile && fff != null) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">REMOVE EXISTING PREVIEW: " + hash);
					gridfs_preview.remove(fff);
					fff = null;
				}
				if (fff == null) {
					try {
						if (optStatus != null)
							optStatus.setCurrentStatusText1("Render Side Views");
						// System.out.println(SystemAnalysis.getCurrentTime() + ">Create preview: render side views GIF...");
						StopWatch ss = new StopWatch(SystemAnalysis.getCurrentTime() + ">CREATE GIF 256x256", true);
						InputStream inps = IOmodule.getThreeDvolumePreviewIcon(lv, optStatus);
						ss.printTime();
						lv = null;
						if (inps == null)
							System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: No 3-D Preview Stream!");
						else {
							if (optStatus != null)
								optStatus.setCurrentStatusText1("Save Preview Icon");
							GridFSInputFile inputFilePreview = gridfs_preview.createFile(inps, hash);
							// inputFilePreview.getMetaData().put("name", id.getURL().getFileName());
							inputFilePreview.save();
							gridfs_preview.getDB().requestStart();
							saved += inputFilePreview.getLength();
							CommandResult wr = gridfs_preview.getDB().getLastError(WriteConcern.SAFE);
							gridfs_preview.getDB().requestDone();
							System.out.println("RES: " + wr.toString());
							fff = gridfs_preview.findOne(hash);
							if (fff != null && fff.getLength() > 0) {
								System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: OK, VOLUME PREVIEW SAVED: " + hash);
							} else
								System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR #: VOLUME PREVIEW NOT SAVED: " + hash);
							
							System.out.println(SystemAnalysis.getCurrentTime() + ">SAVED PREVIEW: " + inputFilePreview.getLength() / 1024 + " KB, HASH: " + hash);
						}
					} catch (Exception e) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
					}
				}
			}
		}
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Saved Volume ("
					+ saved / 1024 / 1024 + " MB)");
		return saved;
	}
	
	private long saveNetworkFile(GridFS gridfs_networks, GridFS gridfs_preview, NetworkData network, ObjectRef optFileSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus, String hash) throws Exception {
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Create Outputstream");
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Calculate hash");
		
		System.out.println("Saving network with hash: " + hash);
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Save Network");
		GridFSDBFile vvv = gridfs_networks.findOne(hash);
		
		if (vvv != null) {
			gridfs_preview.remove(vvv);
			vvv = null;
		}
		InputStream is = network.getURL().getInputStream();
		
		GridFSInputFile inputFile = gridfs_networks.createFile(is);
		inputFile.setFilename(hash);
		// inputFile.getMetaData().put("name", network.getURL().getFileName());
		inputFile.save();
		System.out.println("SAVED NETWORK: " + network.toString() + " // SIZE: " + inputFile.getLength());
		
		GridFSDBFile fff = gridfs_preview.findOne(network.getURL().getDetail());
		if (fff != null) {
			gridfs_preview.remove(fff);
			fff = null;
		}
		if (fff == null) {
			try {
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Render Side Views");
				System.out.println("Render side view GIF...");
				LoadedNetwork ln;
				if (network instanceof LoadedNetwork)
					ln = (LoadedNetwork) network;
				else
					ln = IOmodule.loadNetwork(network);
				GridFSInputFile inputFilePreview = gridfs_preview.createFile(IOmodule
						.getNetworkPreviewIcon(ln, optStatus));
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Save Preview Icon");
				inputFilePreview.setFilename(hash);
				// inputFilePreview.getMetaData().put("name", network.getURL().getFileName());
				inputFilePreview.save();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Saved Network ("
					+ ((VolumeInputStream) network.getURL().getInputStream()).getNumberOfBytes() / 1024 / 1024 + " MB)");
		return ((VolumeInputStream) network.getURL().getInputStream()).getNumberOfBytes();
	}
	
	@SuppressWarnings("resource")
	public static DatabaseStorageResult saveImageFileDirect(CollectionStorage cols, final DB db, final ImageData image, final ObjectRef fileSize,
			final boolean keepRemoteURLs_safe_space, boolean skipProcessingOfLabel, MongoDBhandler mh, HashType hashType,
			MongoDB m, HashSet<String> savedUrls)
			throws Exception, IOException {
		// if the image data source is equal to the target (determined by the prefix),
		// the image content does not need to be copied (assumption valid while using MongoDB data storage)
		if (image.getURL() != null && image.getLabelURL() != null) {
			if (image.getURL().getPrefix().equals(mh.getPrefix()) && image.getLabelURL().getPrefix().equals(mh.getPrefix()))
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		}
		if (image.getURL() != null && image.getLabelURL() == null) {
			if (image.getURL().getPrefix().equals(mh.getPrefix()))
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		}
		if (image.getURL() != null && image.getURL().getPrefix().startsWith("mongo_") && image.getURL().getDetail().startsWith("lemna-db.")) {
			image.getURL().setPrefix(LTftpHandler.PREFIX);
		}
		if (image.getLabelURL() != null && image.getLabelURL().getPrefix().startsWith("mongo_") && image.getLabelURL().getDetail().startsWith("lemna-db.")) {
			image.getLabelURL().setPrefix(LTftpHandler.PREFIX);
		}
		
		// check if the source URL has been imported before, it is assumed that the source URL content
		// is not modified
		if (image.getURL() != null &&
				(image.getURL().getPrefix().equals(LTftpHandler.PREFIX) ||
				image.getURL().getPrefix().startsWith("hsm_"))) {
			if (keepRemoteURLs_safe_space)
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
			
			DBObject knownURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getURL().toString()));
			
			if (processLabelData(keepRemoteURLs_safe_space, image.getLabelURL()) && image.getLabelURL() != null) {
				DBObject knownLabelURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getLabelURL().toString()));
				if (knownURL != null && knownLabelURL != null) {
					String hashMain = (String) knownURL.get("hash");
					boolean fffMain = mh.hasInputStreamForHash(hashMain);
					if (fffMain) {
						String hashLabel = (String) knownLabelURL.get("hash");
						boolean fffLabel = mh.hasInputStreamForHash(hashLabel);
						if (fffMain && fffLabel && hashMain != null && hashLabel != null) {
							image.getURL().setPrefix(mh.getPrefix());
							image.getURL().setDetail(hashMain);
							image.getLabelURL().setPrefix(mh.getPrefix());
							image.getLabelURL().setDetail(hashLabel);
							return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
						}
					}
				}
			}
		}
		if (image.getURL() != null && image.getURL().getPrefix().startsWith("mongo_")) {
			if (keepRemoteURLs_safe_space)
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
			String hashMain = image.getURL().getDetail();
			if (processLabelData(keepRemoteURLs_safe_space, image.getLabelURL()) && image.getLabelURL() != null) {
				String hashLabel = image.getLabelURL().getDetail();
				if (hashMain != null && hashLabel != null) {
					boolean mainFileIsKnown = mh.hasInputStreamForHash(hashMain);
					if (mainFileIsKnown) {
						boolean labelFileIsKnown = mh.hasInputStreamForHash(hashLabel);
						if (labelFileIsKnown) {
							image.getURL().setPrefix(mh.getPrefix());
							image.getURL().setDetail(hashMain);
							image.getLabelURL().setPrefix(mh.getPrefix());
							image.getLabelURL().setDetail(hashLabel);
							return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
						}
					}
				}
			}
		}
		
		// BackgroundTaskHelper.lockGetSemaphore(image.getURL() != null ? image.getURL().getPrefix() : "in", 2);
		byte[] isMain = null;
		byte[] isLabel = null;
		// boolean skipMainSaving = false;
		// boolean skipLabelSaving = false;
		String skipMainUrl = image.getURL() + "";
		String skipLabelUrl = image.getLabelURL() + "";
		try {
			try {
				// synchronized (savedUrls) {
				// if (savedUrls.contains(skipMainUrl))
				// skipMainSaving = true;
				// }
				isMain = image.getURL() != null // && !skipMainSaving
				? ResourceIOManager.getInputStreamMemoryCached(image.getURL()).getBuffTrimmed()
						: null;
			} catch (Exception e) {
				MongoDB.saveSystemErrorMessage("Error: No Inputstream for " + image.getURL() + ". " + e.getMessage() + " // " + SystemAnalysis.getCurrentTime(), e);
			}
			try {
				if (processLabelData(keepRemoteURLs_safe_space, image.getLabelURL())) {
					// if (savedUrls.contains(skipLabelUrl))
					// skipLabelSaving = true;
					isLabel = null;
					if (!skipProcessingOfLabel && image.getLabelURL() != null) {
						MyByteArrayInputStream content = ResourceIOManager.getInputStreamMemoryCached(image.getLabelURL());
						if (content != null)
							isLabel = content.getBuffTrimmed();
						else
							MongoDB.saveSystemErrorMessage("Error: No Inputstream for " + image.getLabelURL() + ". // " + SystemAnalysis.getCurrentTime(), null);
					}
				}
			} catch (Exception e) {
				MongoDB.saveSystemErrorMessage("Error: No Inputstream for " + image.getLabelURL() + ". " + e.getMessage() + " // "
						+ SystemAnalysis.getCurrentTime(), e);
			}
		} finally {
			// BackgroundTaskHelper.lockRelease(image.getURL() != null ? image.getURL().getPrefix() : "in");
		}
		
		String[] hashes;
		
		hashes = GravistoServiceExt.getHashFromInputStream(new InputStream[] {
				isMain != null && isMain.length > 0 ? new MyByteArrayInputStream(isMain) : null,
				isLabel != null && isLabel.length > 0 ? new MyByteArrayInputStream(isLabel) : null
		}, new ObjectRef[] { fileSize, fileSize }, hashType, false);
		
		String hashMain = hashes[0];
		String hashLabel = hashes[1];
		
		if (image.getURL() != null &&
				(image.getURL().getPrefix().equals(LTftpHandler.PREFIX)) ||
				image.getURL().getPrefix().startsWith("hsm_")) {
			if (MongoDB.getEnsureIndex())
				db.getCollection("constantSrc2hash").ensureIndex("srcUrl");
			
			if (hashMain != null) {
				DBObject knownURL = cols.constantSrc2hash.findOne(new BasicDBObject("srcUrl", image.getURL().toString()));
				if (knownURL == null) {
					Map<String, String> m1 = new HashMap<String, String>();
					m1.put("srcUrl", image.getURL().toString());
					m1.put("hash", hashMain);
					cols.constantSrc2hash.insert(new BasicDBObject(m1));
				}
			}
			if (hashLabel != null) {
				DBObject knownLabelURL = cols.constantSrc2hash.findOne(new BasicDBObject("srcUrl", image.getLabelURL().toString()));
				if (knownLabelURL == null) {
					Map<String, String> m1 = new HashMap<String, String>();
					m1.put("srcUrl", image.getLabelURL().toString());
					m1.put("hash", hashLabel);
					cols.constantSrc2hash.insert(new BasicDBObject(m1));
				}
			}
		}
		
		Boolean fffMain = hashMain != null ? m.getHandler().hasInputStreamForHash(hashMain) : null;
		if (hashMain != null) {
			image.getURL().setPrefix(mh.getPrefix());
			image.getURL().setDetail(hashMain);
		}
		Boolean fffLabel = hashLabel != null ? m.getHandler().hasInputStreamForHash(hashLabel) : null;
		if (hashLabel != null && image.getLabelURL() != null) {
			image.getLabelURL().setPrefix(mh.getPrefix());
			image.getLabelURL().setDetail(hashLabel);
		}
		
		if (hashLabel == null && skipProcessingOfLabel)
			fffLabel = true;
		else
			if (hashLabel == null)
				fffLabel = false;
		
		if (fffMain != null && fffLabel != null && fffMain && fffLabel) {
			return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		} else {
			boolean saved;
			MyByteArrayInputStream a = isMain != null && isMain.length > 0 ? new MyByteArrayInputStream(isMain) : null;
			MyByteArrayInputStream b = isLabel != null && isLabel.length > 0 && !skipProcessingOfLabel ? new MyByteArrayInputStream(isLabel) : null;
			MyByteArrayInputStream c = isMain != null && isMain.length > 0 ? getPreviewImageStream(new MyByteArrayInputStream(isMain)) : null;
			saved = saveImageFile(new InputStream[] { a, b, c },
					cols.gridfs_images, cols.gridfs_label_files,
					cols.gridfs_preview_files, image, hashMain,
					hashLabel,
					a != null ? a.getCount() : 0,
					b != null ? b.getCount() : 0,
					c != null ? c.getCount() : 0,
					fffMain != null && !fffMain, fffLabel != null && !fffLabel, m,
					skipMainUrl, skipLabelUrl, savedUrls);
			
			if (saved) {
				return DatabaseStorageResult.STORED_IN_DB;
			} else
				return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
	}
	
	protected static boolean processLabelData(boolean keepRemoteURLs_safe_space, IOurl labelURL) {
		return !keepRemoteURLs_safe_space || (labelURL != null && (labelURL.getPrefix().equals(LTftpHandler.PREFIX)
				|| labelURL.getPrefix().startsWith("hsm_")));
	}
	
	private static MyByteArrayInputStream getPreviewImageStream(InputStream in) {
		try {
			return MyImageIOhelper.getPreviewImageStream(ImageIO.read(in));
		} catch (Exception e) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Could not create preview image stream: "
					+ e.getMessage());
			return null;
		}
	}
	
	public HashType getHashType() {
		return hashType;
	}
	
	private void processSubstanceSaving(BackgroundTaskStatusProviderSupportingExternalCall status, DBCollection substances,
			BasicDBObject dbSubstance, ArrayList<String> conditionIDs) {
		// if (status != null)
		// status.setCurrentStatusText1(SystemAnalysis.getCurrentTime() + ">INSERT SUBSTANCE " + dbSubstance.get("name"));
		
		dbSubstance.put("condition_ids", conditionIDs);
		substances.insert(dbSubstance);
	}
	
	// public static long saveAnnotationFile(GridFS gridfs_annotation, String hash, File file) throws IOException {
	// GridFSInputFile inputFile = gridfs_annotation.createFile(file);
	// inputFile.setFilename(hash);
	// // inputFile.getMetaData().put("name", file.getName());
	// inputFile.save();
	// return file.length();
	// }
	
	public static boolean saveImageFile(InputStream[] isImages, GridFS gridfs_images, GridFS gridfs_label_images,
			GridFS gridfs_preview_files, ImageData image, String hashMain, String hashLabel,
			long expectedLengthMain, long expectedLengthLabel, long expectedLengthPreview,
			boolean storeMain, boolean storeLabel, MongoDB m,
			String skipMainUrl, String skipLabelUrl, HashSet<String> savedUrls) throws IOException {
		boolean allOK = true;
		
		try {
			int idx = 0;
			for (InputStream is : isImages) {
				idx++;
				if (is == null)
					continue;
				GridFS fs = null;
				String hash = null;
				long expLen = 0;
				switch (idx) {
					case 1:
						if (storeMain) {
							fs = gridfs_images;
							hash = hashMain;
							expLen = expectedLengthMain;
						}
						break;
					case 2:
						if (storeLabel && gridfs_label_images != null && hashLabel != null) {
							fs = gridfs_label_images;
							hash = hashLabel;
							expLen = expectedLengthLabel;
						}
						break;
					case 3:
						if (storeMain) {
							fs = gridfs_preview_files;
							hash = hashMain;
							expLen = expectedLengthPreview;
						}
						break;
				}
				if (fs != null && hash != null && is != null)
					if (m.saveStream(hash, is, fs, expLen) < 0)
						allOK = false;
					else {
						synchronized (savedUrls) {
							if (idx == 1)
								savedUrls.add(skipMainUrl);
							if (idx == 2)
								savedUrls.add(skipLabelUrl);
						}
					}
			}
		} catch (Exception e) {
			System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: SAVING IMAGE FILE TO MONGDB FAILED WITH EXCEPTION: " + e.getMessage());
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		
		return allOK;
	}
}
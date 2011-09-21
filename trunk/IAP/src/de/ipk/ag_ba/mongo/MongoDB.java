/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.mongo;

import info.StopWatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.bson.types.ObjectId;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.HashType;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MongoCollection;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk.ag_ba.server.task_management.CloudComputingService;
import de.ipk.ag_ba.server.task_management.CloudHost;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleAverage;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.MyImageIOhelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.LoadedNetwork;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.IntVolumeInputStream;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeInputStream;

/**
 * @author klukas
 */
public class MongoDB {
	
	private static final ArrayList<MongoDB> mongos = initMongoList();
	
	public static ArrayList<MongoDB> getMongos() {
		return mongos;
	}
	
	@Override
	public String toString() {
		return displayName + " (" + databaseHost + ", db " + databaseName + ")";
	}
	
	private static ArrayList<MongoDB> initMongoList() {
		ArrayList<MongoDB> res = new ArrayList<MongoDB>();
		
		// if (IAPservice.isReachable("ba-13.ipk-gatersleben.de") || IAPservice.isReachable("ba-24.ipk-gatersleben.de")) {
		res.add(getDefaultCloud());
		// res.add(new MongoDB("Data Processing 2", "cloud2", "ba-13.ipk-gatersleben.de,", null, null, HashType.MD5));
		// } else
		// res.add(getLocalDB());
		// }
		// if (IAPservice.isReachable("localhost")) {
		// res.add(new MongoDB("local dbe3", "local_dbe3", "localhost", null, null, HashType.SHA512));
		// res.add(new MongoDB("local dbe4", "local_dbe4", "localhost", null, null, HashType.SHA512));
		// }
		return res;
	}
	
	public static MongoDB getLocalDB() {
		if (defaultLocalInstance == null)
			defaultLocalInstance = new MongoDB("Local DB", "localCloud1", "localhost", null, null, HashType.MD5);
		return defaultLocalInstance;
	}
	
	private static MongoDB defaultCloudInstance = null;
	private static MongoDB defaultLocalInstance = null;
	
	public static String getDefaultCloudHostName() {
		return "ba-13.ipk-gatersleben.de";
	}
	
	public static MongoDB getDefaultCloud() {
		if (defaultCloudInstance == null) {
			defaultCloudInstance = new MongoDB("Data Processing", "cloud1", getDefaultCloudHostName(), "iap", "iap#2011", HashType.MD5);
		}
		return defaultCloudInstance;
		// return new MongoDB("Data Processing", "cloud1", "ba-13.ipk-gatersleben.de,ba-24.ipk-gatersleben.de", "iap", "iap#2011", HashType.MD5);
	}
	
	public static MongoDB getLocalUnitTestsDB() {
		return new MongoDB("Unit Tests local", "localUnitTests", "ba-13", null, null, HashType.MD5);
	}
	
	private final MongoDBhandler mh;
	
	public ResourceIOHandler[] getHandlers() {
		return new ResourceIOHandler[] { mh };
	}
	
	private final String displayName;
	private final String databaseName;
	private final String databaseHost;
	private final String databaseLogin;
	private final String databasePass;
	private final HashType hashType;
	
	// collections:
	// preview_files
	// volumes
	// images
	// annotations
	// experiments
	// substances
	// conditions
	
	private MongoDB(String displayName, String databaseName, String hostName, String login, String password, HashType hashType) {
		if (databaseName == null || databaseName.contains("_") || databaseName.contains("/"))
			throw new UnsupportedOperationException("Database name may not be NULL and may not contain special characters!");
		this.displayName = displayName;
		this.databaseName = databaseName;
		this.databaseHost = hostName;
		this.databaseLogin = login;
		this.databasePass = password;
		this.hashType = hashType;
		
		mh = new MongoDBhandler(databaseHost, this);
		
	}
	
	public void saveExperiment(final ExperimentInterface experiment, final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		saveExperiment(experiment, status, false);
	}
	
	public void saveExperiment(final ExperimentInterface experiment,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final boolean keepDataLinksToDataSource_safe_space)
			throws Exception {
		final ThreadSafeOptions err = new ThreadSafeOptions();
		RunnableOnDB r = new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
			
			@Override
			public void run() {
				try {
					storeExperiment(experiment, db, status, keepDataLinksToDataSource_safe_space);
				} catch (Exception e) {
					err.setParam(0, e);
				}
			}
		};
		processDB(r);
		if (err.getParam(0, null) != null)
			throw (Exception) err.getParam(0, null);
	}
	
	private static Mongo m;
	
	WeakHashMap<Mongo, HashSet<String>> authenticatedDBs = new WeakHashMap<Mongo, HashSet<String>>();
	
	private static HashSet<String> dbsAnalyzedForCollectionSettings = new HashSet<String>();
	
	private void processDB(String database, String optHosts, String optLogin, String optPass,
			RunnableOnDB runnableOnDB) throws Exception {
		Exception e = null;
		try {
			boolean ok = false;
			int nrep = 0;
			int repeats = 0;
			// BackgroundTaskHelper.lockAquire(dataBase, 2);
			do {
				try {
					DB db;
					if (m == null) {
						if (optHosts == null || optHosts.length() == 0) {
							StopWatch s = new StopWatch("INFO: new Mongo()", false);
							m = new Mongo();
							m.slaveOk();
							m.getMongoOptions().connectionsPerHost = SystemAnalysis.getNumberOfCPUs();
							m.getMongoOptions().threadsAllowedToBlockForConnectionMultiplier = 1000;
							s.printTime();
						} else {
							StopWatch s = new StopWatch("INFO: new Mongo(seeds)", false);
							List<ServerAddress> seeds = new ArrayList<ServerAddress>();
							for (String h : optHosts.split(","))
								seeds.add(new ServerAddress(h));
							m = new Mongo(seeds);
							m.slaveOk();
							m.getMongoOptions().connectionsPerHost = SystemAnalysis.getNumberOfCPUs();
							m.getMongoOptions().threadsAllowedToBlockForConnectionMultiplier = 1000;
							s.printTime();
						}
						if (authenticatedDBs.get(m) == null || !authenticatedDBs.get(m).contains("admin")) {
							DB dbAdmin = m.getDB("admin");
							try {
								StopWatch s = new StopWatch("INFO: dbAdmin.authenticate()");
								dbAdmin.authenticate("iap", "iap#2011".toCharArray());
								s.printTime();
								if (authenticatedDBs.get(m) == null)
									authenticatedDBs.put(m, new HashSet<String>());
								authenticatedDBs.get(m).add(database);
							} catch (Exception err) {
								// System.err.println("ERROR: " + err.getMessage());
							}
						}
					}
					db = m.getDB(database);
					
					if (authenticatedDBs.get(m) == null || !authenticatedDBs.get(m).contains(database))
						if (optLogin != null && optPass != null && optLogin.length() > 0 && optPass.length() > 0) {
							try {
								boolean auth = db.authenticate(optLogin, optPass.toCharArray());
								if (!auth) {
									// throw new Exception("Invalid MongoDB login data provided!");
								} else {
									if (authenticatedDBs.get(m) == null)
										authenticatedDBs.put(m, new HashSet<String>());
									authenticatedDBs.get(m).add(database);
								}
							} catch (Exception err) {
								// System.err.println("ERROR: " + err.getMessage());
							}
						}
					
					if (!dbsAnalyzedForCollectionSettings.contains(database)) {
						checkforCollectionsInitialization(db, "status_maize", 100, 50000);
						checkforCollectionsInitialization(db, "status_barley", 100, 50000);
						checkforCollectionsInitialization(db, "status_phyto", 100, 50000);
						dbsAnalyzedForCollectionSettings.add(database);
					}
					
					runnableOnDB.setDB(db);
					runnableOnDB.run();
					ok = true;
					e = null;
				} catch (Exception err) {
					System.out.println("EXEC " + (nrep - repeats + 1) + " ERROR: " + err.getLocalizedMessage() + " T=" + IAPservice.getCurrentTimeAsNiceString());
					e = err;
					// Thread.sleep(5000);
				}
				repeats--;
			} while (!ok && repeats > 0);
		} finally {
			// BackgroundTaskHelper.lockRelease(dataBase);
		}
		if (e != null)
			throw e;
	}
	
	private void checkforCollectionsInitialization(DB db, String cappedCollectionName, int maxObjects, int maxBytes) {
		if (!db.collectionExists(cappedCollectionName)) {
			BasicDBObject createOptions = new BasicDBObject();
			createOptions.append("capped", true);
			createOptions.append("max", maxObjects); // max number of objects in collection (if smaller than max. size)
			createOptions.append("size", maxBytes); // max size in bytes
			db.createCollection(cappedCollectionName, createOptions);
		}
	}
	
	public void processDB(RunnableOnDB runnableOnDB) throws Exception {
		processDB(getDatabaseName(), databaseHost, databaseLogin, databasePass, runnableOnDB);
	}
	
	public void deleteUnusedBinaryFiles() {
		// collections:
		// preview_files
		// volumes
		// images
		// annotations
		// experiments
		// substances
		// conditions
	}
	
	public void deleteExperiment(final String experimentID) throws Exception {
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBObject obj = new BasicDBObject("_id", new ObjectId(experimentID));
				if (db.collectionExists(MongoExperimentCollections.EXPERIMENTS.toString())) {
					DBObject o = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString()).findOne(obj);
					if (o != null) {
						WriteResult wr = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString()).remove(o);
						System.out.println(SystemAnalysisExt.getCurrentTime() + ">DELETE WRITERESULT: ERROR? " + wr.getLastError().getErrorMessage());
					}
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	private void storeExperiment(ExperimentInterface experiment, DB db,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			boolean keepDataLinksToDataSource_safe_space) throws InterruptedException, ExecutionException {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setBval(0, true);
		Thread loadGen = new Thread(new Runnable() {
			@Override
			public void run() {
				while (tso.getBval(0, true)) {
					double v = Math.random();
					v = Math.sin(v);
					tso.setDouble(v);
				}
			}
		});
		loadGen.setPriority(Thread.MIN_PRIORITY);
		if (IAPservice.isCloudExecutionModeActive())
			loadGen.start();
		
		try {
			storeExperimentInnerCall(experiment, db, status, keepDataLinksToDataSource_safe_space);
		} finally {
			tso.setBval(0, false);
		}
	}
	
	private void storeExperimentInnerCall(ExperimentInterface experiment, DB db,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			boolean keepDataLinksToDataSource_safe_space) throws InterruptedException, ExecutionException {
		
		System.out.println(">>> " + SystemAnalysisExt.getCurrentTime());
		System.out.println("STORE EXPERIMENT: " + experiment.getName());
		System.out.println("DB-ID           : " + experiment.getHeader().getDatabaseId());
		System.out.println("DB              : " + experiment.getHeader().getDatabase());
		System.out.println("Exp.type        : " + experiment.getHeader().getExperimentType());
		System.out.println("Group           : " + experiment.getHeader().getImportusergroup());
		System.out.println("Username        : " + experiment.getHeader().getImportusername());
		System.out.println(">>> KEEP EXTERNAL REFS?  : " + keepDataLinksToDataSource_safe_space);
		// experiment.getHeader().setImportusername(SystemAnalysis.getUserName());
		
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		
		ObjectRef overallFileSize = new ObjectRef();
		overallFileSize.addLong(0);
		
		DBCollection substances = db.getCollection("substances");
		
		DBCollection conditions = db.getCollection("conditions");
		
		int errorCount = 0;
		
		long lastTransferSum = 0;
		int lastSecond = -1;
		int count = 0;
		StringBuilder errors = new StringBuilder();
		int numberOfBinaryData = countMeasurementValues(experiment, new MeasurementNodeType[] {
				MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME, MeasurementNodeType.NETWORK });
		
		if (status != null || (status != null && !status.wantsToStop()))
			status.setCurrentStatusText1(SystemAnalysisExt.getCurrentTime() + ">Determine Size");
		{
			if (!experiment.getHeader().getDatabaseId().startsWith("hsm:")) {
				long l = Substance3D.getFileSize(Substance3D.getAllFiles(experiment));
				experiment.getHeader().setSizekb(l / 1024);
			}
		}
		
		// List<DBObject> dbSubstances = new ArrayList<DBObject>();
		// HashMap<DBObject, List<BasicDBObject>> substance2conditions = new HashMap<DBObject, List<BasicDBObject>>();
		ArrayList<SubstanceInterface> sl = new ArrayList<SubstanceInterface>(experiment);
		Runtime r = Runtime.getRuntime();
		ArrayList<String> substanceIDs = new ArrayList<String>();
		while (!sl.isEmpty()) {
			System.out.print(SystemAnalysisExt.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
					+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB>");
			
			SubstanceInterface s = sl.get(0);
			sl.remove(0);
			if (status != null && status.wantsToStop())
				break;
			if (status != null)
				status.setCurrentStatusText1(SystemAnalysisExt.getCurrentTime() + ">SAVE SUBSTANCE " + s.getName());
			attributes.clear();
			s.fillAttributeMap(attributes);
			BasicDBObject substance = new BasicDBObject(filter(attributes));
			// dbSubstances.add(substance);
			
			ArrayList<String> conditionIDs = new ArrayList<String>();
			
			for (ConditionInterface c : s) {
				if (status != null && status.wantsToStop())
					break;
				attributes.clear();
				c.fillAttributeMap(attributes);
				BasicDBObject condition = new BasicDBObject(filter(attributes));
				
				List<BasicDBObject> dbSamples = new ArrayList<BasicDBObject>();
				for (SampleInterface sa : c) {
					if (status != null && status.wantsToStop())
						break;
					attributes.clear();
					sa.fillAttributeMap(attributes);
					BasicDBObject sample = new BasicDBObject(filter(attributes));
					dbSamples.add(sample);
					
					attributes.clear();
					if (sa.size() > 0) {
						sa.getSampleAverage().fillAttributeMap(attributes);
						BasicDBObject dbSampleAverage = new BasicDBObject(filter(attributes));
						
						sample.put("average", dbSampleAverage);
					}
					
					List<BasicDBObject> dbMeasurements = new ArrayList<BasicDBObject>();
					List<BasicDBObject> dbImages = new ArrayList<BasicDBObject>();
					List<BasicDBObject> dbVolumes = new ArrayList<BasicDBObject>();
					List<BasicDBObject> dbNetworks = new ArrayList<BasicDBObject>();
					
					Queue<Future<DatabaseStorageResult>> storageResults = new LinkedList<Future<DatabaseStorageResult>>();
					Queue<ImageData> imageDataQueue = new LinkedList<ImageData>();
					
					for (Measurement m : sa) {
						if (!(m instanceof BinaryMeasurement)) {
							attributes.clear();
							m.fillAttributeMap(attributes);
							BasicDBObject measurement = new BasicDBObject(filter(attributes));
							dbMeasurements.add(measurement);
						}
					} // measurement
					if (sa instanceof Sample3D) {
						Sample3D s3 = (Sample3D) sa;
						for (NumericMeasurementInterface m : s3.getMeasurements(new MeasurementNodeType[] {
								MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME, MeasurementNodeType.NETWORK })) {
							DatabaseStorageResult res = null;
							attributes.clear();
							try {
								if (m instanceof ImageData) {
									ImageData id = (ImageData) m;
									storageResults.add(saveImageFile(db, id, overallFileSize,
											keepDataLinksToDataSource_safe_space));
									imageDataQueue.add(id);
									count++;
								}
								if (m instanceof VolumeData) {
									VolumeData vd = (VolumeData) m;
									res = saveVolumeFile(db, vd, overallFileSize, status);
									if (res == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
										errorCount++;
										errors.append("<li>" + vd.getURL().getFileName());
									} else {
										m.fillAttributeMap(attributes);
										BasicDBObject dbo = new BasicDBObject(filter(attributes));
										dbVolumes.add(dbo);
									}
								}
								if (m instanceof NetworkData) {
									NetworkData nd = (NetworkData) m;
									res = saveNetworkFile(db, nd, overallFileSize, status);
									if (res == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
										errorCount++;
										errors.append("<li>" + nd.getURL().getFileName());
									} else {
										m.fillAttributeMap(attributes);
										BasicDBObject dbo = new BasicDBObject(filter(attributes));
										dbNetworks.add(dbo);
									}
								}
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
								res = DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
							}
							if (status != null) {
								if (res != null)
									count++;
								double prog = count * (100d / numberOfBinaryData);
								if (res != null)
									status.setCurrentStatusText1(count + "/" + numberOfBinaryData + ": " + res);
								status.setCurrentStatusValueFine(prog);
								int currentSecond = new GregorianCalendar().get(Calendar.SECOND);
								if (currentSecond != lastSecond) {
									if (lastSecond >= 0) {
										long transfered = overallFileSize.getLong() - lastTransferSum;
										long mbps = transfered / 1024 / 1024;
										status.setCurrentStatusText2(mbps + " MB/s");
									}
									lastSecond = currentSecond;
									lastTransferSum = overallFileSize.getLong();
								}
							}
						} // binary measurement
					}
					if (dbMeasurements.size() > 0)
						sample.put("measurements", dbMeasurements);
					{
						while (!storageResults.isEmpty()) {
							Future<DatabaseStorageResult> fres = storageResults.poll();
							ImageData id = imageDataQueue.poll();
							DatabaseStorageResult res = fres.get();
							if (res != null && status != null)
								status.setCurrentStatusText1(count + "/" + numberOfBinaryData + ": " + res);
							if (res == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
								errorCount++;
								errors.append("<li>" + id.getURL().getFileName());
							} else {
								attributes.clear();
								id.fillAttributeMap(attributes);
								BasicDBObject dbo = new BasicDBObject(filter(attributes));
								dbImages.add(dbo);
							}
						}
						if (dbImages.size() > 0)
							sample.put("images", dbImages);
					}
					if (dbVolumes.size() > 0)
						sample.put("volumes", dbVolumes);
					if (dbNetworks.size() > 0)
						sample.put("networks", dbVolumes);
				} // sample
				condition.put("samples", dbSamples);
				
				conditions.insert(condition);
				
				conditionIDs.add((condition).getString("_id"));
				
			} // condition
			processSubstanceSaving(status, substances, substance, conditionIDs);
			substanceIDs.add((substance).getString("_id"));
			
		} // substance
		
		System.out.print(SystemAnalysisExt.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
				+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB>");
		if (status != null)
			status.setCurrentStatusText1(SystemAnalysisExt.getCurrentTime() + ">SAVE SUB-ELEMENTS OF SUBSTANCES FINISHED");
		
		// if (substances != null && safeOneTimeSave)
		// processSubstanceSaving(status, substances, conditions, dbSubstances, substance2conditions);
		if (status != null)
			status.setCurrentStatusText1(SystemAnalysisExt.getCurrentTime() + ">SAVE OF SUBSTANCE-DB ELEMENTS FINISHED");
		
		if (status != null || (status != null && !status.wantsToStop()))
			status.setCurrentStatusText1(SystemAnalysisExt.getCurrentTime() + ">Finalize Storage");
		
		// l = overallFileSize.getLong(); // in case of update the written bytes are not the right size
		experiment.getHeader().setStorageTime(new Date());
		
		experiment.fillAttributeMap(attributes);
		BasicDBObject dbExperiment = new BasicDBObject(attributes);
		dbExperiment.put("substance_ids", substanceIDs);
		
		DBCollection experiments = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString());
		
		if (status == null || (status != null && !status.wantsToStop())) {
			experiments.insert(dbExperiment);
			String id = dbExperiment.get("_id").toString();
			System.out.println(">>> STORED EXPERIMENT " + experiment.getHeader().getExperimentName() + " // DB-ID: " + id + " // "
					+ SystemAnalysisExt.getCurrentTime());
			for (ExperimentHeaderInterface eh : experiment.getHeaders()) {
				eh.setDatabaseId(id);
			}
		}
		
		updateExperimentSize(db, experiment, status);
		
		// System.out.print(SystemAnalysisExt.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
		// + " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB>");
		
		if (errorCount > 0) {
			MainFrame.showMessageDialog(
					"<html>" + "The following files cound not be properly processed:<ul>" + errors.toString() + "</ul> "
							+ "", "Errors");
		}
		
	}
	
	private void processSubstanceSaving(BackgroundTaskStatusProviderSupportingExternalCall status, DBCollection substances,
			BasicDBObject dbSubstance, ArrayList<String> conditionIDs) {
		if (status != null)
			status.setCurrentStatusText1(SystemAnalysisExt.getCurrentTime() + ">INSERT SUBSTANCE " + dbSubstance.get("name"));
		
		dbSubstance.put("condition_ids", conditionIDs);
		if (status == null || (status != null && !status.wantsToStop())) {
			substances.insert(dbSubstance);
		}
	}
	
	private HashMap<String, Object> filter(HashMap<String, Object> attributes) {
		HashSet<String> keys = new HashSet<String>();
		keys.addAll(attributes.keySet());
		for (String key : keys) {
			if (attributes.get(key) == null)
				attributes.remove(key);
		}
		return attributes;
	}
	
	private int countMeasurementValues(ExperimentInterface experiment, MeasurementNodeType[] measurementNodeTypes) {
		int res = 0;
		for (MeasurementNodeType m : measurementNodeTypes) {
			res += Substance3D.getAllFiles(experiment, m).size();
		}
		return res;
	}
	
	public long saveAnnotationFile(GridFS gridfs_annotation, String hash, File file) throws IOException {
		GridFSInputFile inputFile = gridfs_annotation.createFile(file);
		inputFile.setFilename(hash);
		// inputFile.getMetaData().put("name", file.getName());
		inputFile.save();
		return file.length();
	}
	
	public boolean saveImageFile(InputStream[] isImages, GridFS gridfs_images, GridFS gridfs_label_images,
			GridFS gridfs_preview_files, ImageData image, String hashMain, String hashLabel, boolean storeMain, boolean storeLabel) throws IOException {
		boolean allOK = true;
		
		try {
			int idx = 0;
			for (InputStream is : isImages) {
				idx++;
				if (is == null)
					continue;
				GridFS fs = null;
				String hash = null;
				switch (idx) {
					case 1:
						if (storeMain) {
							fs = gridfs_images;
							hash = hashMain;
						}
						break;
					case 2:
						if (storeLabel) {
							fs = gridfs_label_images;
							hash = hashLabel;
						}
						break;
					case 3:
						if (storeMain) {
							fs = gridfs_preview_files;
							hash = hashMain;
						}
						break;
				}
				if (fs != null && saveStream(hash, is, fs) < 0)
					allOK = false;
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		return allOK;
	}
	
	/**
	 * @return -1 in case of error, 0 in case of existing storage, > 0 in case of new storage
	 * @throws IOException
	 */
	public long saveStream(String hash, InputStream is, GridFS fs) throws IOException {
		long result = -1;
		
		GridFSDBFile fff = fs.findOne(hash);
		if (fff != null) {
			fs.remove(fff);
			fff = null;
		}
		
		GridFSInputFile inputFile = fs.createFile(is, hash);
		// fs.getDB().setWriteConcern(WriteConcern.REPLICAS_SAFE);
		inputFile.save();
		result = inputFile.getLength();
		is.close();
		// CommandResult cr = fs.getDB().getLastError(WriteConcern.REPLICAS_SAFE);
		// if (!cr.ok())
		// System.out.println("ERROR: MONGODB GRIDFS STORAGE RESULT: " + cr.getErrorMessage());
		return result;
	}
	
	private long saveVolumeFile(GridFS gridfs_volumes, GridFS gridfs_preview, VolumeData id, ObjectRef optFileSize,
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
					StopWatch ss = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">CREATE GIF 512x512", true);
					InputStream inps = IOmodule.getThreeDvolumeRenderViewGif(lv, optStatus);
					ss.printTime();
					GridFSInputFile inputFile = gridfs_volumes.createFile(inps, hash);
					inputFile.save();
					saved += inputFile.getLength();
				}
				GridFSDBFile fff = gridfs_preview.findOne(hash);
				boolean removeExistingPreviewFile = true;
				if (removeExistingPreviewFile && fff != null) {
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">REMOVE EXISTING PREVIEW: " + hash);
					gridfs_preview.remove(fff);
					fff = null;
				}
				if (fff == null) {
					try {
						if (optStatus != null)
							optStatus.setCurrentStatusText1("Render Side Views");
						// System.out.println(SystemAnalysisExt.getCurrentTime() + ">Create preview: render side views GIF...");
						StopWatch ss = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">CREATE GIF 256x256", true);
						InputStream inps = IOmodule.getThreeDvolumePreviewIcon(lv, optStatus);
						ss.printTime();
						lv = null;
						if (inps == null)
							System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: No 3-D Preview Stream!");
						else {
							if (optStatus != null)
								optStatus.setCurrentStatusText1("Save Preview Icon");
							GridFSInputFile inputFilePreview = gridfs_preview.createFile(inps, hash);
							// inputFilePreview.getMetaData().put("name", id.getURL().getFileName());
							inputFilePreview.save();
							saved += inputFilePreview.getLength();
							CommandResult wr = gridfs_preview.getDB().getLastError(WriteConcern.SAFE);
							System.out.println("RES: " + wr.toString());
							fff = gridfs_preview.findOne(hash);
							if (fff != null && fff.getLength() > 0) {
								System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: OK, VOLUME PREVIEW SAVED: " + hash);
							} else
								System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR #: VOLUME PREVIEW NOT SAVED: " + hash);
							
							System.out.println(SystemAnalysisExt.getCurrentTime() + ">SAVED PREVIEW: " + inputFilePreview.getLength() / 1024 + " KB, HASH: " + hash);
						}
					} catch (Exception e) {
						System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: " + e.getMessage());
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
	
	private final ExecutorService storageTaskQueue = Executors.newFixedThreadPool(6, new ThreadFactory() {
		int n = 1;
		
		@Override
		public Thread newThread(Runnable r) {
			Thread res = new Thread(r);
			res.setName("File Stream Storage Thread " + n);
			n++;
			return res;
		}
	});
	
	public Future<DatabaseStorageResult> saveImageFile(final DB db,
			final ImageData id, final ObjectRef fileSize,
			final boolean keepDataLinksToDataSource_safe_space) throws Exception {
		
		final ImageData image = id;
		
		return storageTaskQueue.submit(new Callable<DatabaseStorageResult>() {
			@Override
			public DatabaseStorageResult call() throws Exception {
				
				// if the image data source is equal to the target (determined by the prefix),
				// the image content does not need to be copied (assumption valid while using MongoDB data storage)
				if (image.getURL() != null && image.getLabelURL() != null) {
					if (id.getURL().getPrefix().equals(mh.getPrefix()) && id.getLabelURL().getPrefix().equals(mh.getPrefix()))
						return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
				}
				/*
				if (image.getURL() != null && image.getLabelURL() != null) {
					if (id.getURL().getPrefix().equals(mh.getPrefix())
							&& id.getLabelURL().getPrefix().equals(mh.getPrefix())) {
						if ((image.getURL().getPrefix().equals(LemnaTecFTPhandler.PREFIX) 
								|| image.getURL().getPrefix().startsWith("hsm_"))) {
							if (keepDataLinksToDataSource_safe_space) {
								return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
							}
						}
					}
				}
				*/
				// check if the source URL has been imported before, it is assumed that the source URL content
				// is not modified
				if (image.getURL() != null &&
								(image.getURL().getPrefix().equals(LemnaTecFTPhandler.PREFIX) ||
								image.getURL().getPrefix().startsWith("hsm_"))) {
					if (keepDataLinksToDataSource_safe_space)
						return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
					
					DBObject knownURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getURL().toString()));
					
					if (image.getLabelURL() != null) {
						DBObject knownLabelURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getLabelURL().toString()));
						if (knownURL != null && knownLabelURL != null) {
							GridFS gridfs_images = new GridFS(db, MongoGridFS.FS_IMAGES.toString());
							GridFS gridfs_label_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
							
							String hashMain = (String) knownURL.get("hash");
							GridFSDBFile fffMain = gridfs_images.findOne(hashMain);
							
							String hashLabel = (String) knownLabelURL.get("hash");
							GridFSDBFile fffLabel = gridfs_label_files.findOne(hashLabel);
							if (fffMain != null && fffLabel != null && hashMain != null && hashLabel != null) {
								image.getURL().setPrefix(mh.getPrefix());
								image.getURL().setDetail(hashMain);
								image.getLabelURL().setPrefix(mh.getPrefix());
								image.getLabelURL().setDetail(hashLabel);
								return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
							}
						}
					}
				}
				
				// BackgroundTaskHelper.lockGetSemaphore(image.getURL() != null ? image.getURL().getPrefix() : "in", 2);
				byte[] isMain = null;
				byte[] isLabel = null;
				try {
					try {
						isMain = id.getURL() != null ? ResourceIOManager.getInputStreamMemoryCached(image.getURL()).getBuffTrimmed() : null;
					} catch (Exception e) {
						System.out.println("Error: No Inputstream for " + id.getURL() + ". " + e.getMessage() + " // " + SystemAnalysisExt.getCurrentTime());
					}
					try {
						isLabel = id.getLabelURL() != null ? ResourceIOManager.getInputStreamMemoryCached(image.getLabelURL()).getBuffTrimmed() : null;
					} catch (Exception e) {
						System.out.println("Error: No Inputstream for " + id.getLabelURL() + ". " + e.getMessage() + " // "
										+ SystemAnalysisExt.getCurrentTime());
					}
				} finally {
					// BackgroundTaskHelper.lockRelease(image.getURL() != null ? image.getURL().getPrefix() : "in");
				}
				if (isMain == null) {
					System.out.println("No input stream for source-URL:  " + image.getURL());
					return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
				}
				if (image.getLabelURL() != null && isLabel == null) {
					System.out.println("No input stream for source-URL (label):  " + image.getURL());
					return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
				}
				
				String[] hashes;
				
				hashes = GravistoServiceExt.getHashFromInputStream(new InputStream[] {
								new MyByteArrayInputStream(isMain),
								isLabel != null ? new MyByteArrayInputStream(isLabel) : null
						},
								new ObjectRef[] { fileSize, fileSize }, getHashType(), true);
				
				String hashMain = hashes[0];
				String hashLabel = hashes[1];
				
				if (image.getURL() != null &&
								(image.getURL().getPrefix().equals(LemnaTecFTPhandler.PREFIX)) ||
								image.getURL().getPrefix().startsWith("hsm_")) {
					db.getCollection("constantSrc2hash").ensureIndex("srcUrl");
					
					DBObject knownURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getURL().toString()));
					if (knownURL == null) {
						Map<String, String> m1 = new HashMap<String, String>();
						m1.put("srcUrl", image.getURL().toString());
						m1.put("hash", hashMain);
						db.getCollection("constantSrc2hash").insert(new BasicDBObject(m1));
					}
					DBObject knownLabelURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getLabelURL().toString()));
					if (knownLabelURL == null) {
						Map<String, String> m1 = new HashMap<String, String>();
						m1.put("srcUrl", image.getLabelURL().toString());
						m1.put("hash", hashLabel);
						db.getCollection("constantSrc2hash").insert(new BasicDBObject(m1));
					}
				}
				
				GridFS gridfs_images = new GridFS(db, "" + MongoGridFS.FS_IMAGES.toString());
				DBCollection collectionA = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString());
				collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
				
				GridFS gridfs_label_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
				DBCollection collectionB = db.getCollection(MongoGridFS.FS_IMAGE_LABELS_FILES.toString());
				collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
				
				GridFS gridfs_preview_files = new GridFS(db, MongoGridFS.FS_PREVIEW.toString());
				DBCollection collectionC = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
				collectionC.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
				
				GridFSDBFile fffMain = gridfs_images.findOne(hashMain);
				image.getURL().setPrefix(mh.getPrefix());
				image.getURL().setDetail(hashMain);
				
				GridFSDBFile fffLabel = gridfs_label_files.findOne(hashLabel);
				if (image.getLabelURL() != null) {
					image.getLabelURL().setPrefix(mh.getPrefix());
					image.getLabelURL().setDetail(hashLabel);
				}
				
				GridFSDBFile fffPreview = gridfs_preview_files.findOne(hashMain);
				
				if (fffMain != null && fffMain.getLength() <= 0) {
					gridfs_images.remove(fffMain);
					fffMain = null;
				}
				if (fffLabel != null && fffLabel.getLength() <= 0) {
					gridfs_images.remove(fffLabel);
					fffLabel = null;
				}
				if (fffPreview != null && fffPreview.getLength() <= 0) {
					gridfs_images.remove(fffPreview);
					fffPreview = null;
				}
				
				if (fffMain != null && fffLabel != null && fffPreview != null) {
					return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
				} else {
					// BackgroundTaskHelper.lockGetSemaphore(m, 1);
					boolean saved;
					try {
						saved = saveImageFile(new InputStream[] {
										new MyByteArrayInputStream(isMain),
										isLabel != null ? new MyByteArrayInputStream(isLabel) : null,
										getPreviewImageStream(new MyByteArrayInputStream(isMain))
								}, gridfs_images, gridfs_label_files,
										gridfs_preview_files, id, hashMain,
										hashLabel,
										fffMain == null, fffLabel == null);
					} finally {
						// BackgroundTaskHelper.lockRelease(m);
					}
					if (saved) {
						return DatabaseStorageResult.STORED_IN_DB;
					} else
						return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
				}
			}
		});
	}
	
	private InputStream getPreviewImageStream(InputStream in) {
		try {
			return MyImageIOhelper.getPreviewImageStream(ImageIO.read(in));
		} catch (Exception e) {
			System.err.println("Could not create preview image stream.");
			return null;
		}
	}
	
	public DatabaseStorageResult saveVolumeFile(DB db, VolumeData volume, ObjectRef optFileSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		String hash;
		try {
			InputStream iis = volume.getURL().getInputStream();
			if (iis == null)
				return DatabaseStorageResult.IO_ERROR_INPUT_NOT_AVAILABLE;
			
			hash = GravistoService.getHashFromInputStream(iis, optFileSize, getHashType());
			if (hash == null)
				return DatabaseStorageResult.IO_ERROR_INPUT_NOT_AVAILABLE;
			// System.out.println("SAVE VOLUME HASH: " + hash);
			volume.getURL().setDetail(hash);
			
			GridFS gridfs_volumes = new GridFS(db, MongoGridFS.FS_VOLUMES.toString());
			DBCollection collectionA = db.getCollection(MongoGridFS.FS_VOLUMES_FILES.toString());
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
				collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
				
				saveVolumeFile(gridfs_volumes, gridfs_preview, volume, optFileSize, optStatus, hash);
				fff = gridfs_volumes.findOne(hash);
				if (fff != null && fff.getLength() <= 0) {
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">Delete generated volume from MongoDB file system (to save space and for debugging).");
					gridfs_volumes.remove(fff);
				}
				gridfs_preview = new GridFS(db, MongoGridFS.FS_PREVIEW_FILES.toString());
				
				fff = gridfs_preview.findOne(hash);
				if (fff != null && fff.getLength() <= 0) {
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: OK, VOLUME PREVIEW SAVED: " + hash);
				} else
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: VOLUME PREVIEW NOT SAVED: " + hash);
				return DatabaseStorageResult.STORED_IN_DB;
			}
		} catch (Exception e) {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR during volume save: " + e.getMessage());
			return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
	}
	
	public DatabaseStorageResult saveNetworkFile(DB db, NetworkData network, ObjectRef optFileSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		GridFS gridfs_networks = new GridFS(db, MongoGridFS.FS_NETWORKS.toString());
		DBCollection collectionA = db.getCollection(MongoGridFS.FS_NETWORKS_FILES.toString());
		collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		GridFS gridfs_preview = new GridFS(db, MongoGridFS.FS_PREVIEW.toString());
		DBCollection collectionB = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
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
	
	public String getDatabaseName() {
		return databaseName;
	}
	
	public String getDefaultHost() {
		return databaseHost;
	}
	
	public String getDefaultLogin() {
		return databaseLogin;
	}
	
	public String getDefaultPass() {
		return databasePass;
	}
	
	public byte[] getPreviewData(final String hash) {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					GridFS gridfs_preview_images = new GridFS(db, MongoGridFS.FS_PREVIEW.toString());
					GridFSDBFile fff = gridfs_preview_images.findOne(hash);
					if (fff != null) {
						try {
							ByteArrayOutputStream bos = new ByteArrayOutputStream((int) fff.getLength());
							
							byte[] buffer = new byte[1024];
							int read;
							InputStream is = fff.getInputStream();
							while ((read = is.read(buffer)) != -1)
								bos.write(buffer, 0, read);
							
							byte[] bytes = bos.toByteArray();
							tso.setParam(0, bytes);
						} catch (IOException e) {
							ErrorMsg.addErrorMessage(e);
							tso.setParam(0, null);
						}
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
			return (byte[]) tso.getParam(0, null);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	public ExperimentHeaderInterface getExperimentHeader(final ObjectId experimentMongoID) {
		final ObjectRef res = new ObjectRef();
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					DBObject header = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString()).findOne(experimentMongoID);
					if (header != null)
						res.setObject(new ExperimentHeader(header.toMap()));
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		return (ExperimentHeaderInterface) res.getObject();
	}
	
	public ArrayList<ExperimentHeaderInterface> getExperimentList(final String user) {
		return getExperimentList(user, null);
	}
	
	public ArrayList<ExperimentHeaderInterface> getExperimentList(final String user, final BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		final ArrayList<ExperimentHeaderInterface> res = new ArrayList<ExperimentHeaderInterface>();
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					if (optStatus != null)
						optStatus.setCurrentStatusText1("Get Experiment List");
					DBCollection col = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString());
					if (optStatus != null)
						optStatus.setCurrentStatusText1("Iterate Experiments");
					for (DBObject header : col.find()) {
						ExperimentHeader h = new ExperimentHeader(header.toMap());
						h.setStorageTime(new Date(((ObjectId) header.get("_id")).getTime()));
						if (user == null ||
								h.getImportusername() != null && h.getImportusername().equals(user) ||
								LemnaTecDataExchange.getAdministrators().contains(user))
							res.add(h);
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Process Result");
		return ExperimentHeaderService.filterNewest(res);
	}
	
	public ExperimentInterface getExperiment(final ExperimentHeaderInterface header) {
		return getExperiment(header, false, null);
	}
	
	public ExperimentInterface getExperiment(final ExperimentHeaderInterface header, final boolean interactiveCalculateExperimentSize,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider) {
		return getExperiment(header, interactiveCalculateExperimentSize, optStatusProvider, null, null);
	}
	
	public ExperimentInterface getExperiment(final ExperimentHeaderInterface header, final boolean interactiveCalculateExperimentSize,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider,
			final ArrayList<DBObject> optDBPbjectsOfSubstances, final ArrayList<DBObject> optDBPbjectsOfConditions) {
		final ExperimentInterface experiment = new Experiment();
		if (optStatusProvider != null)
			optStatusProvider.setCurrentStatusValue(0);
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					// synchronized (db) {
					DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), new ObjectId(header.getDatabaseId()));
					DBObject expref = dbr.fetch();
					if (expref != null) {
						BasicDBList subList = (BasicDBList) expref.get("substances");
						if (subList != null)
							for (Object co : subList) {
								DBObject substance = (DBObject) co;
								if (optDBPbjectsOfSubstances != null)
									optDBPbjectsOfSubstances.add(substance);
								processSubstance(db, experiment, substance, optStatusProvider, 100d / subList.size(), optDBPbjectsOfConditions);
							}
						db.getCollection("substances").ensureIndex("_id");
						BasicDBList l = (BasicDBList) expref.get("substance_ids");
						if (l != null)
							for (Object o : l) {
								if (o == null)
									continue;
								DBRef subr = new DBRef(db, "substances", new ObjectId(o.toString()));
								if (subr != null) {
									DBObject substance = subr.fetch();
									if (substance != null) {
										processSubstance(db, experiment, substance, optStatusProvider, 100d / l.size(), optDBPbjectsOfConditions);
									}
								}
							}
					}
					experiment.setHeader(header);
					
					int numberOfImagesAndVolumes = countMeasurementValues(experiment, new MeasurementNodeType[] {
							MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME });
					experiment.getHeader().setNumberOfFiles(numberOfImagesAndVolumes);
					
					if (numberOfImagesAndVolumes > 0 && interactiveCalculateExperimentSize) {
						updateExperimentSize(db, experiment, optStatusProvider);
					}
					// }
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return experiment;
	}
	
	public void setExperimentType(final ExperimentHeaderInterface header, final String experimentType) throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				ObjectId id = new ObjectId(header.getDatabaseId());
				DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), id);
				DBObject expref = dbr.fetch();
				if (expref != null) {
					expref.put("experimenttype", experimentType);
					db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString()).save(expref);
				} else
					tso.setBval(0, true);
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		if (tso.getBval(0, false))
			throw new Exception("Experiment with ID " + header.getDatabaseId() + " not found!");
	}
	
	public void setExperimentInfo(final ExperimentHeaderInterface header) throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				ObjectId id = new ObjectId(header.getDatabaseId());
				DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), id);
				DBObject expref = dbr.fetch();
				if (expref != null) {
					HashMap<String, Object> attributes = new HashMap<String, Object>();
					header.fillAttributeMap(attributes, 0);
					for (String key : attributes.keySet()) {
						if (attributes.get(key) != null)
							expref.put(key, attributes.get(key));
					}
					
					db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString()).save(expref);
				} else
					tso.setBval(0, true);
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		if (tso.getBval(0, false))
			throw new Exception("Experiment with ID " + header.getDatabaseId() + " not found!");
	}
	
	private void updateExperimentSize(DB db, ExperimentInterface experiment, BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider) {
		boolean recalcSize = false;
		try {
			long sz = experiment.getHeader().getSizekb();
			if (sz <= 0) {
				recalcSize = true;
			}
		} catch (Exception e) {
			recalcSize = true;
		}
		if (recalcSize) {
			try {
				ObjectRef newSize = new ObjectRef();
				newSize.addLong(0);
				if (optStatusProvider != null) {
					optStatusProvider.setCurrentStatusValueFine(0);
					optStatusProvider.setCurrentStatusText1("Update Experiment Size Info");
				}
				List<NumericMeasurementInterface> abc = Substance3D.getAllFiles(experiment);
				double max = 0;
				int n = 0;
				HashMap<Class, ArrayList<GridFS>> cachedFS = new HashMap<Class, ArrayList<GridFS>>();
				for (NumericMeasurementInterface nmd : abc) {
					if (nmd instanceof BinaryMeasurement) {
						max++;
					}
				}
				for (NumericMeasurementInterface nmd : abc) {
					if (nmd instanceof BinaryMeasurement) {
						n++;
						if (optStatusProvider != null) {
							optStatusProvider.setCurrentStatusValueFine(100d * n / max);
							optStatusProvider.setCurrentStatusText2("(" + n + "/" + (int) max + ", " + newSize.getLong() / 1024 / 1024 + " MB)");
						}
						IOurl url = ((BinaryMeasurement) nmd).getURL();
						if (url != null) {
							String hash = url.getDetail();
							if (!cachedFS.containsKey(nmd.getClass()))
								cachedFS.put(nmd.getClass(), MongoGridFS.getGridFsFileCollectionsFor(db, nmd));
							ArrayList<GridFS> col = cachedFS.get(nmd.getClass());
							for (GridFS gridfs : col) {
								GridFSDBFile file = gridfs.findOne(hash);
								if (file != null) {
									newSize.addLong(file.getLength());
								}
							}
						}
					}
				}
				experiment.getHeader().setSizekb(newSize.getLong() / 1024);
				setExperimentInfo(experiment.getHeader());
				if (optStatusProvider != null) {
					optStatusProvider.setCurrentStatusValue(100);
					optStatusProvider.setCurrentStatusText1("Finished");
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	/**
	 * add task to "schedule" collection
	 * 
	 * @throws Exception
	 */
	public void batchEnqueue(final BatchCmd cmd) throws Exception {
		// HashSet<String> targetIPs, String remoteCapableAnalysisActionClassName,
		// String remoteCapableAnalysisActionParams, String experimentInputMongoID) {
		
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBCollection dbc = db.getCollection("schedule");
				dbc.setObjectClass(BatchCmd.class);
				dbc.insert(cmd);
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	/**
	 * get list of host names, with not too old ping update time
	 */
	public ArrayList<CloudHost> batchGetAvailableHosts(final long maxUpdate) throws Exception {
		final ArrayList<CloudHost> res = new ArrayList<CloudHost>();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBCollection dbc = db.getCollection("compute_hosts");
				dbc.setObjectClass(CloudHost.class);
				
				ArrayList<CloudHost> del = new ArrayList<CloudHost>();
				
				DBCursor cursor = dbc.find();
				final long curr = System.currentTimeMillis();
				while (cursor.hasNext()) {
					CloudHost h = (CloudHost) cursor.next();
					if (curr - h.getLastUpdateTime() < maxUpdate) {
						res.add(h);
					} else {
						long age = curr - h.getLastUpdateTime();
						if (age > 1000 * 60 * 15)
							del.add(h);
					}
				}
				for (CloudHost d : del)
					dbc.remove(d);
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		
		return res;
	}
	
	public void batchPingHost(final String ip,
			final int blocksExecutedWithinLastMinute,
			final int pipelineExecutedWithinCurrentHour,
			final int tasksExecutedWithinLastMinute,
			final double progress) throws Exception {
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBCollection dbc = db.getCollection("compute_hosts");
				dbc.setObjectClass(CloudHost.class);
				
				BasicDBObject query = new BasicDBObject();
				query.put(CloudHost.getHostId(), ip);
				
				CloudHost res = (CloudHost) dbc.findOne(query);
				boolean add = false;
				if (res == null) {
					System.out.println("INSERT: " + query);
					res = new CloudHost();
					add = true;
				}
				if (res != null) {
					res.updateTime();
					res.setHostName(ip);
					res.setClusterExecutionMode(IAPservice.isCloudExecutionModeActive());
					res.setOperatingSystem(SystemAnalysis.getOperatingSystem());
					res.setBlocksExecutedWithinLastMinute(blocksExecutedWithinLastMinute);
					res.setPipelineExecutedWithinCurrentHour(pipelineExecutedWithinCurrentHour);
					res.setTasksExecutedWithinLastMinute(tasksExecutedWithinLastMinute);
					res.setTaskProgress(progress);
					double load = SystemAnalysisExt.getRealSystemCpuLoad();
					boolean monitor = !CloudComputingService.getInstance().getIsCalculationPossible();
					int wl = BackgroundThreadDispatcher.getWorkLoad();
					StringBuilder diskHistory = new StringBuilder();
					if (monitor) {
						diskHistory.append("<br>storage:");
						for (File lfw : SystemAnalysisExt.myListRoots()) {
							long fs = lfw.getFreeSpace();
							long ts = lfw.getTotalSpace();
							long free = fs / 1024 / 1024 / 1024;
							long size = ts / 1024 / 1024 / 1024;
							long used = (ts - fs) / 1024 / 1024 / 1024;
							int prc = (int) (100d * (1d - free / (double) size));
							diskHistory.append("<br>" + lfw.toString() + " -> " + free + " GB free (" + size + " GB, " + prc + "% used)");
						}
					}
					res.setHostInfo(
							(monitor ? "monitoring:<br>" : "") +
									SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis.getMemoryMB() + " MB, " +
									SystemAnalysisExt.getPhysicalMemoryInGB() + " GB<br>" + SystemAnalysis.getNumberOfCPUs() +
									"/" + SystemAnalysisExt.getNumberOfCpuPhysicalCores() + "/" + SystemAnalysisExt.getNumberOfCpuLogicalCores() + " CPUs" +
									(load > 0 ? " load "
											+ StringManipulationTools.formatNumber(load, "#.#") + "" : "") +
									(wl > 0 ? ", active: " + wl : "") + diskHistory.toString());
					res.setLastPipelineTime(BlockPipeline.getLastPipelineExecutionTimeInSec());
					if (add)
						dbc.insert(res);
					else
						dbc.save(res);
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	public CloudHost batchGetUpdatedHostInfo(final CloudHost h) throws Exception {
		final ObjectRef r = new ObjectRef();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBCollection dbc = db.getCollection("compute_hosts");
				dbc.setObjectClass(CloudHost.class);
				
				BasicDBObject query = new BasicDBObject();
				query.put("_id", h.get("_id"));
				
				CloudHost res = (CloudHost) dbc.findOne(query);
				if (res != null) {
					r.setObject(res);
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		return (CloudHost) r.getObject();
	}
	
	public void batchClearJobs() throws Exception {
		processDB(new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				db.getCollection("schedule").drop();
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	// /**
	// * if a batch lastUpdate time is older then the provided limit, it is
	// * returned in the result set and will most likely be re-claimed to
	// * another host
	// */
	// public Collection<BatchCmd> batchGetCommands(final long maxUpdate) {
	// final Collection<BatchCmd> res = new ArrayList<BatchCmd>();
	// try {
	// processDB(new RunnableOnDB() {
	// private DB db;
	//
	// @Override
	// public void run() {
	// // System.out.println("---");
	// DBCollection collection = db.getCollection("schedule");
	// collection.setObjectClass(BatchCmd.class);
	// for (DBObject dbo : collection.find()) {
	// BatchCmd batch = (BatchCmd) dbo;
	// CloudAnalysisStatus s = batch.getRunStatus();
	// if (s == CloudAnalysisStatus.SCHEDULED
	// || (
	// (batch.getRunStatus() == CloudAnalysisStatus.STARTING || batch.getRunStatus() == CloudAnalysisStatus.STARTING)
	// && System.currentTimeMillis() - batch.getLastUpdateTime() > maxUpdate)) {
	// res.add(batch);
	// break;
	// }
	// // System.out.println(batch);
	// }
	// }
	//
	// @Override
	// public void setDB(DB db) {
	// this.db = db;
	// }
	// });
	// } catch (Exception e) {
	// ErrorMsg.addErrorMessage(e);
	// return null;
	// }
	// return res;
	// }
	
	public Collection<BatchCmd> batchGetAllCommands() {
		final Collection<BatchCmd> res = new ArrayList<BatchCmd>();
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					// System.out.println("---");
					DBCollection collection = db.getCollection("schedule");
					collection.setObjectClass(BatchCmd.class);
					for (DBObject dbo : collection.find()) {
						BatchCmd batch = (BatchCmd) dbo;
						res.add(batch);
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		return res;
	}
	
	public Collection<BatchCmd> batchGetWorkTasksScheduledForStart(final int maxTasks) {
		final Collection<BatchCmd> res = new ArrayList<BatchCmd>();
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					String hostName;
					try {
						hostName = SystemAnalysisExt.getHostName();
						DBCollection collection = db.getCollection("schedule");
						collection.setObjectClass(BatchCmd.class);
						boolean added = false;
						int addCnt = 0;
						for (DBObject dbo : collection.find(BatchCmd.getRunstatusMatcher(CloudAnalysisStatus.SCHEDULED))) {
							BatchCmd batch = (BatchCmd) dbo;
							if (batch.getCpuTargetUtilization() < maxTasks) {
								if (batch.getExperimentHeader() == null)
									continue;
								if (batch.getOwner() == null)
									batchClaim(batch, CloudAnalysisStatus.STARTING, false);
								if (hostName.equals("" + batch.getOwner())) {
									res.add(batch);
									added = true;
									addCnt += batch.getCpuTargetUtilization();
									if (addCnt >= maxTasks)
										break;
								}
							}
						}
						int claimed = 0;
						if (addCnt < maxTasks)
							for (DBObject dbo : collection.find()) {
								BatchCmd batch = (BatchCmd) dbo;
								if (batch.getExperimentHeader() == null)
									continue;
								if (!added && batch.getCpuTargetUtilization() < maxTasks)
									if (batch.get("lastupdate") == null || (System.currentTimeMillis() - batch.getLastUpdateTime() > 30000)) {
										// after 30 seconds tasks are taken away from other systems
										batchClaim(batch, CloudAnalysisStatus.STARTING, false);
										claimed++;
										if (claimed >= maxTasks)
											break;
									}
							}
						if (addCnt < maxTasks)
							for (DBObject dbo : collection.find(BatchCmd.getRunstatusMatcher(CloudAnalysisStatus.STARTING))) {
								BatchCmd batch = (BatchCmd) dbo;
								if (batch.getExperimentHeader() == null)
									continue;
								if (batch.getCpuTargetUtilization() < maxTasks && hostName.equals("" + batch.getOwner())) {
									res.add(batch);
									addCnt += batch.getCpuTargetUtilization();
									if (addCnt >= maxTasks)
										added = true;
									break;
								}
							}
					} catch (UnknownHostException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			return new ArrayList<BatchCmd>();
		}
		if (res.size() > 0) {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">SCHEDULED FOR START: " + res.size());
		}
		return res;
	}
	
	public void batchClaim(final BatchCmd batch, final CloudAnalysisStatus starting, final boolean requireOwnership) {
		// try to claim a batch cmd
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					try {
						DBCollection collection = db.getCollection("schedule");
						collection.setObjectClass(BatchCmd.class);
						DBObject dbo = new BasicDBObject();
						dbo.put("_id", batch.get("_id"));
						// String rs = batch.getString("runstatus");
						// dbo.put("runstatus", rs);
						if (requireOwnership) {
							dbo.put("owner", SystemAnalysisExt.getHostName());
						}
						batch.put("runstatus", starting.toString());
						batch.put("lastupdate", System.currentTimeMillis());
						batch.put("owner", SystemAnalysisExt.getHostName());
						// WriteResult r =
						collection.update(dbo, batch, false, false);
						// System.out.println("Update status: " + rs + " --> " + starting.toString() + ", res: " + r.toString());
					} catch (UnknownHostException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public BatchCmd batchGetCommand(final BatchCmd batch) {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		// try to claim a batch cmd
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					DBCollection collection = db.getCollection("schedule");
					collection.setObjectClass(BatchCmd.class);
					DBObject dbo = new BasicDBObject();
					if (batch.get("_id") != null) {
						dbo.put("_id", batch.get("_id"));
						BatchCmd res = (BatchCmd) collection.findOne(dbo);
						tso.setParam(0, res);
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			// ErrorMsg.addErrorMessage(e);
		}
		return (BatchCmd) tso.getParam(0, null);
	}
	
	public BatchCmd batchClearJob(final BatchCmd batch) {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		// try to claim a batch cmd
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					DBCollection collection = db.getCollection("schedule");
					collection.setObjectClass(BatchCmd.class);
					DBObject dbo = new BasicDBObject();
					dbo.put("_id", batch.get("_id"));
					BatchCmd res = (BatchCmd) collection.findOne(dbo);
					collection.remove(dbo);
					tso.setParam(0, res);
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			// ErrorMsg.addErrorMessage(e);
		}
		return (BatchCmd) tso.getParam(0, null);
	}
	
	private void processSubstance(DB db, ExperimentInterface experiment, DBObject substance,
			BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider, double smallProgressStep,
			ArrayList<DBObject> optDBObjectsConditions) {
		@SuppressWarnings("unchecked")
		Substance3D s3d = new Substance3D(substance.toMap());
		experiment.add(s3d);
		BasicDBList condList = (BasicDBList) substance.get("conditions");
		if (condList != null)
			for (Object co : condList) {
				DBObject cond = (DBObject) co;
				processCondition(s3d, cond);
			}
		db.getCollection("conditions").ensureIndex("_id");
		BasicDBList l = (BasicDBList) substance.get("condition_ids");
		if (l != null) {
			double max = l.size();
			for (Object o : l) {
				DBRef condr = new DBRef(db, "conditions", new ObjectId(o.toString()));
				DBObject cond = condr.fetch();
				if (cond != null) {
					if (optDBObjectsConditions != null)
						optDBObjectsConditions.add(cond);
					processCondition(s3d, cond);
				}
				if (optStatusProvider != null)
					optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep * 1 / max);
			}
		}
	}
	
	private void processCondition(Substance3D s3d, DBObject cond) {
		Condition3D condition = new Condition3D(s3d, cond.toMap());
		s3d.add(condition);
		BasicDBList sampList = (BasicDBList) cond.get("samples");
		if (sampList != null)
			for (Object so : sampList) {
				DBObject sam = (DBObject) so;
				Sample3D sample = new Sample3D(condition, sam.toMap());
				condition.add(sample);
				// average
				BasicDBObject avg = (BasicDBObject) sam.get("average");
				if (avg != null) {
					SampleAverage average = new SampleAverage(sample, avg.toMap());
					sample.setSampleAverage(average);
				}
				// measurements
				BasicDBList measList = (BasicDBList) sam.get("measurements");
				if (measList != null) {
					for (Object m : measList) {
						DBObject meas = (DBObject) m;
						NumericMeasurement3D nm = new NumericMeasurement3D(sample, meas.toMap());
						sample.add(nm);
					}
				}
				// images
				BasicDBList imgList = (BasicDBList) sam.get(MongoCollection.IMAGES.toString());
				if (imgList != null) {
					for (Object m : imgList) {
						DBObject img = (DBObject) m;
						@SuppressWarnings("unchecked")
						ImageData image = new ImageData(sample, img.toMap());
						image.getURL().setPrefix(mh.getPrefix());
						if (image.getLabelURL() != null)
							image.getLabelURL().setPrefix(mh.getPrefix());
						sample.add(image);
					}
				}
				// volumes
				BasicDBList volList = (BasicDBList) sam.get(MongoCollection.VOLUMES.toString());
				if (volList != null) {
					for (Object v : volList) {
						DBObject vol = (DBObject) v;
						@SuppressWarnings("unchecked")
						VolumeData volume = new VolumeData(sample, vol.toMap());
						if (volume.getURL() != null)
							volume.getURL().setPrefix(mh.getPrefix());
						if (volume.getLabelURL() != null)
							volume.getLabelURL().setPrefix(mh.getPrefix());
						sample.add(volume);
					}
				}
				// networks
				BasicDBList netList = (BasicDBList) sam.get(MongoCollection.NETWORKS.toString());
				if (netList != null) {
					for (Object n : netList) {
						DBObject net = (DBObject) n;
						@SuppressWarnings("unchecked")
						NetworkData network = new NetworkData(sample, net.toMap());
						if (network.getURL() != null)
							network.getURL().setPrefix(mh.getPrefix());
						if (network.getLabelURL() != null)
							network.getLabelURL().setPrefix(mh.getPrefix());
						sample.add(network);
					}
				}
			}
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public MongoDBhandler getPrimaryHandler() {
		return mh;
	}
	
	public HashType getHashType() {
		return hashType;
	}
	
	public GridFS getGridFS(final MongoResourceIOConfigObject c) throws Exception {
		
		if (c.getDatatype() == MeasurementNodeType.OMICS)
			throw new UnsupportedOperationException("Omics data can't be saved in GridFS!");
		
		final ObjectRef result = new ObjectRef();
		
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (c.getStorageType() == DataStorageType.PREVIEW_ICON) {
					GridFS gridfs_preview_files = new GridFS(db, "fs_preview_files");
					DBCollection collectionC = db.getCollection("fs_preview_files.files");
					collectionC.ensureIndex("filename");
					
					DBCollection collectionChunks = db.getCollection("fs_preview_files.chunks");
					collectionChunks.ensureIndex("files_id");
					
					result.setObject(gridfs_preview_files);
				} else
					switch (c.getDatatype()) {
						case IMAGE:
							switch (c.getStorageType()) {
								case MAIN_STREAM:
									GridFS gridfs_images = new GridFS(db, MongoGridFS.FS_IMAGES.toString());
									DBCollection collectionA = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString());
									collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString() + ".chunks");
									collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_images);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
									DBCollection collectionB = db.getCollection(MongoGridFS.FS_IMAGE_LABELS_FILES.toString());
									collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									collectionChunks = db.getCollection(MongoGridFS.FS_IMAGE_LABELS.toString() + ".chunks");
									collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_null_files);
									break;
							}
							break;
						case VOLUME:
							switch (c.getStorageType()) {
								case MAIN_STREAM:
									GridFS gridfs_volumes = new GridFS(db, MongoGridFS.FS_VOLUMES.toString());
									DBCollection collectionA = db.getCollection(MongoGridFS.FS_VOLUMES_FILES.toString());
									collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_VOLUMES_FILES.toString() + ".chunks");
									collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_volumes);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_VOLUME_LABELS.toString());
									DBCollection collectionB = db.getCollection("fs_volume_labels.files");
									collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									collectionChunks = db.getCollection(MongoGridFS.FS_VOLUME_LABELS.toString() + ".chunks");
									collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_null_files);
									break;
							}
							break;
						case NETWORK:
							switch (c.getStorageType()) {
								case MAIN_STREAM:
									GridFS gridfs_networks = new GridFS(db, MongoGridFS.FS_NETWORKS.toString());
									DBCollection collectionA = db.getCollection(MongoGridFS.FS_NETWORKS_FILES.toString());
									collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_NETWORKS.toString() + ".chunks");
									collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_networks);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_NETWORK_LABELS.toString());
									DBCollection collectionB = db.getCollection(MongoGridFS.fs_networks_labels_files.toString());
									collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									collectionChunks = db.getCollection(MongoGridFS.FS_NETWORK_LABELS.toString() + ".chunks");
									collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_null_files);
									break;
							}
							break;
					}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		
		return (GridFS) result.getObject();
	}
	
	public Collection<String> getNews(final int i) {
		final LinkedList<String> res = new LinkedList<String>();
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					SimpleDateFormat sdf = new SimpleDateFormat();
					for (DBObject newsItem : db.getCollection("news").find()) {
						Date l = (Date) newsItem.get("date");
						String text = (String) newsItem.get("text");
						String user = (String) newsItem.get("user");
						res.add(sdf.format(l) + ": " + text + " (" + user + ")");
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		while (i > 0 && res.size() > i)
			res.remove(0);
		return res;
	}
	
	public void addNewsItem(final String text, final String user) throws Exception {
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				Map<String, Object> map = new HashMap<String, Object>();
				BasicDBObject ni = new BasicDBObject(map);
				ni.put("date", new Date());
				ni.put("text", text);
				ni.put("user", user);
				db.getCollection("news").insert(ni);
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	public IOurl getURLforStoredData(final IOurl url) throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (url != null && url.getPrefix().equals(LemnaTecFTPhandler.PREFIX)) {
					db.getCollection("constantSrc2hash").ensureIndex("srcUrl");
					
					DBObject knownURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", url.toString()));
					if (knownURL != null) {
						String knownHash = (String) knownURL.get("hash");
						tso.setParam(0, knownHash);
					}
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		String hash = (String) tso.getParam(0, null);
		if (hash == null)
			return null;
		
		MongoDBhandler h = (MongoDBhandler) getHandlers()[0];
		String prefix = h.getPrefix();
		return new IOurl(prefix, hash, url.getFileName());
	}
	
	public InputStream getURLforStoredData_PreviewStream(final IOurl url) throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (url != null && url.getPrefix().equals(LemnaTecFTPhandler.PREFIX)) {
					db.getCollection("constantSrc2hash").ensureIndex("srcUrl");
					
					DBObject knownURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", url.toString()));
					if (knownURL != null) {
						String knownHash = (String) knownURL.get("hash");
						tso.setParam(0, knownHash);
					}
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		String hash = (String) tso.getParam(0, null);
		if (hash == null)
			return null;
		
		MongoDBhandler h = (MongoDBhandler) getHandlers()[0];
		String prefix = h.getPrefix();
		return h.getPreviewInputStream(new IOurl(prefix, hash, url.getFileName()));
	}
	
	public synchronized String cleanUp(
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final boolean compact_warningLongExecutionTime)
			throws Exception {
		final StringBuilder res = new StringBuilder();
		processDB(new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				System.out.println("REORGANIZATION: Create inventory... // " + SystemAnalysisExt.getCurrentTime());
				res.append("REORGANIZATION: Create inventory... // " + SystemAnalysisExt.getCurrentTime() + "<br>");
				status.setCurrentStatusText1("Create inventory");
				status.setCurrentStatusValueFine(100d / 5 * 0);
				long numberOfBinaryFilesInDatabase = 0;
				HashMap<String, Integer> fs2cnt = new HashMap<String, Integer>();
				for (String mgfs : MongoGridFS.getFileCollectionsInclPreview()) {
					GridFS gridfs = new GridFS(db, mgfs);
					int cnt = gridfs.getFileList().count();
					numberOfBinaryFilesInDatabase += cnt;
					fs2cnt.put(mgfs, cnt);
					
				}
				System.out.println("REORGANIZATION: Stored binary files: " + numberOfBinaryFilesInDatabase + " // "
						+ SystemAnalysisExt.getCurrentTime());
				res.append("REORGANIZATION: Stored binary files: " + numberOfBinaryFilesInDatabase + " // "
						+ SystemAnalysisExt.getCurrentTime() + "<br>");
				status.setCurrentStatusText1("Binary files: " + numberOfBinaryFilesInDatabase);
				status.setCurrentStatusValueFine(100d / 5 * 1);
				
				ArrayList<ExperimentHeaderInterface> el = getExperimentList(null);
				HashSet<String> linkedHashes = new HashSet<String>();
				double smallStep = 100d / 5 * 1 / el.size();
				HashSet<String> dbIdsOfSubstances = new HashSet<String>();
				HashSet<String> dbIdsOfConditions = new HashSet<String>();
				{
					DBCollection substances = db.getCollection("substances");
					DBCursor subCur = substances.find();
					while (subCur.hasNext()) {
						DBObject subO = subCur.next();
						dbIdsOfSubstances.add(subO.get("_id") + "");
					}
				}
				{
					DBCollection conditions = db.getCollection("conditions");
					DBCursor condCur = conditions.find();
					while (condCur.hasNext()) {
						DBObject condO = condCur.next();
						dbIdsOfSubstances.add(condO.get("_id") + "");
					}
				}
				
				for (ExperimentHeaderInterface ehii : el) {
					status.setCurrentStatusText2("Analyze " + ehii.getExperimentName());
					ArrayList<DBObject> substanceObjects = new ArrayList<DBObject>();
					ArrayList<DBObject> conditionObjects = new ArrayList<DBObject>();
					ExperimentInterface exp = getExperiment(ehii, false, status, substanceObjects, conditionObjects);
					{
						// check Ids of substances and conditions
						for (DBObject subO : substanceObjects) {
							dbIdsOfSubstances.remove(subO.get("_id") + "");
						}
						substanceObjects = null;
						for (DBObject subO : conditionObjects) {
							dbIdsOfConditions.remove(subO.get("_id") + "");
						}
						conditionObjects = null;
					}
					List<NumericMeasurementInterface> binaryData = Substance3D.getAllFiles(exp);
					for (NumericMeasurementInterface nmi : binaryData) {
						if (nmi instanceof BinaryMeasurement) {
							BinaryMeasurement bm = (BinaryMeasurement) nmi;
							if (bm.getURL() != null)
								linkedHashes.add(bm.getURL().getDetail());
							if (bm.getLabelURL() != null)
								linkedHashes.add(bm.getLabelURL().getDetail());
							if (nmi instanceof ImageData) {
								ImageData imData = (ImageData) nmi;
								String oldRef = imData.getAnnotationField("oldreference");
								if (oldRef != null && oldRef.length() > 0) {
									IOurl u = new IOurl(oldRef);
									linkedHashes.add(u.getDetail());
								}
							}
						}
					}
					status.setCurrentStatusValueFineAdd(smallStep);
				} // experiments
				
				{
					DBCollection substances = db.getCollection("substances");
					status.setCurrentStatusText1("Remove not linked substances: " + dbIdsOfSubstances.size());
					int n = 0;
					long max = dbIdsOfSubstances.size();
					for (String subID : dbIdsOfSubstances) {
						n++;
						substances.remove(new BasicDBObject("_id", subID));
						status.setCurrentStatusValueFine(100d / max * n);
					}
				}
				{
					int n = 0;
					long max = dbIdsOfConditions.size();
					DBCollection conditions = db.getCollection("conditions");
					status.setCurrentStatusText1("Remove not linked conditions: " + dbIdsOfConditions.size());
					for (String condID : dbIdsOfConditions) {
						n++;
						conditions.remove(new BasicDBObject("_id", condID));
						status.setCurrentStatusValueFine(100d / max * n);
					}
				}
				
				if (linkedHashes.size() >= 0) {
					long freeAll = 0;
					System.out.println("REORGANIZATION: Linked binary files: " + linkedHashes.size() + " // " + SystemAnalysisExt.getCurrentTime());
					res.append("REORGANIZATION: Linked binary files: " + linkedHashes.size() + " // " + SystemAnalysisExt.getCurrentTime() + "<br>");
					status.setCurrentStatusText1("Linked files: " + linkedHashes.size());
					status.setCurrentStatusValueFine(100d / 5 * 2);
					
					double stepSize = 100d / 5 * 3 / MongoGridFS.getFileCollectionsInclPreview().size();
					
					for (String mgfs : MongoGridFS.getFileCollectionsInclPreview()) {
						GridFS gridfs = new GridFS(db, mgfs);
						int cnt = gridfs.getFileList().count();
						if (cnt == fs2cnt.get(mgfs)) {
							ArrayList<GridFSDBFile> toBeRemoved = new ArrayList<GridFSDBFile>();
							// no changes in between
							DBCursor fl = gridfs.getFileList();
							while (fl.hasNext()) {
								DBObject dbo = fl.next();
								GridFSDBFile f = (GridFSDBFile) dbo;
								String md5 = f.getFilename();
								if (!linkedHashes.contains(md5))
									toBeRemoved.add(f);
							}
							System.out.println("REORGANIZATION: Binary files that are not linked (" + mgfs + "): " + toBeRemoved.size() + " // "
									+ SystemAnalysisExt.getCurrentTime());
							res.append("REORGANIZATION: Binary files that are not linked (" + mgfs + "): " + toBeRemoved.size() + " // "
									+ SystemAnalysisExt.getCurrentTime() + "<br>");
							long free = 0;
							int fIdx = 0;
							int fN = toBeRemoved.size();
							for (GridFSDBFile f : toBeRemoved) {
								free += f.getLength();
								gridfs.remove(f);
								fIdx++;
								status.setCurrentStatusText1("File " + fIdx + "/" + fN);
								status.setCurrentStatusText2("Removed: " + free / 1024 / 1024 + " MB");
								status.setCurrentStatusValueFine(fIdx * 100d / fN);
							}
							System.out.println("REORGANIZATION: Deleted MB (" + mgfs + "): " + free / 1024 / 1024 + " // "
									+ SystemAnalysisExt.getCurrentTime());
							res.append("REORGANIZATION: Deleted MB (" + mgfs + "): " + free / 1024 / 1024 + " // "
									+ SystemAnalysisExt.getCurrentTime() + "<br>");
							if (compact_warningLongExecutionTime) {
								System.out.println("Start compact collection (" + mgfs + ") // " + SystemAnalysisExt.getCurrentTime());
								res.append("Start compact collection (" + mgfs + ") // " + SystemAnalysisExt.getCurrentTime() + "<br>");
								HashMap<String, Object> m = new HashMap<String, Object>();
								m.put("compact", mgfs + ".files");
								m.put("force", true);
								BasicDBObject cmd = new BasicDBObject(m);
								db.command(cmd);
								
								m.clear();
								m.put("compact", mgfs);
								m.put("force", true);
								cmd = new BasicDBObject(m);
								db.command(cmd);
								
								m.clear();
								m.put("compact", mgfs + ".chunks");
								m.put("force", true);
								cmd = new BasicDBObject(m);
								db.command(cmd);
								
								System.out.println("Finished compact collection (" + mgfs + ") // " + SystemAnalysisExt.getCurrentTime());
								res.append("Finished compact collection (" + mgfs + ") // " + SystemAnalysisExt.getCurrentTime() + "<br>");
							} else {
								System.out.println("Compact operation was not requested. Database contains free space, free file system space is not increased!");
							}
							freeAll += free;
						}
						status.setCurrentStatusValueFineAdd(stepSize);
					}
					System.out.println("REORGANIZATION: Overall deleted MB: " + freeAll / 1024 / 1024 + " // "
							+ SystemAnalysisExt.getCurrentTime());
					
					res.append("REORGANIZATION: Overall deleted MB: " + freeAll / 1024 / 1024 + " // "
							+ SystemAnalysisExt.getCurrentTime() + "<br>");
					status.setCurrentStatusText1("Deleted MB: " + (freeAll / 1024 / 1024));
					status.setCurrentStatusValueFine(100d / 5 * 5);
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		return res.toString();
	}
}

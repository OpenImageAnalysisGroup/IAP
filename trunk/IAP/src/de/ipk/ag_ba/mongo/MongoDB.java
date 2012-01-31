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
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
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
import com.mongodb.MongoInternalException;
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
	
	private static boolean ensureIndex = true;
	
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
		if (IAPservice.isReachable("ba-13.ipk-gatersleben.de"))
			res.add(getDefaultCloud());
		if (IAPservice.isReachable("ba-24.ipk-gatersleben.de"))
			res.add(new MongoDB("Data Storage 2 (BA-24)", "cloud2", "ba-24.ipk-gatersleben.de", "iap24", "iap24", HashType.MD5));
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
		return new MongoDB("Unit Tests local", "localUnitTests", "ba-13", "iap", "iap#2011", HashType.MD5);
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
	
	private static HashMap<String, Mongo> m = new HashMap<String, Mongo>();
	
	WeakHashMap<Mongo, HashSet<String>> authenticatedDBs = new WeakHashMap<Mongo, HashSet<String>>();
	
	private static HashSet<String> dbsAnalyzedForCollectionSettings = new HashSet<String>();
	
	private void processDB(String database, String optHosts, String optLogin, String optPass,
			RunnableOnDB runnableOnDB) throws Exception {
		Exception e = null;
		try {
			boolean ok = false;
			int nrep = 5;
			int repeats = 5;
			// BackgroundTaskHelper.lockAquire(dataBase, 2);
			do {
				try {
					DB db;
					String key = optHosts + ";" + database;
					if (m.get(key) == null) {
						if (optHosts == null || optHosts.length() == 0) {
							StopWatch s = new StopWatch("INFO: new Mongo()", false);
							m.put(key, new Mongo());
							m.get(key).slaveOk();
							m.get(key).getMongoOptions().connectionsPerHost = SystemAnalysis.getNumberOfCPUs();
							m.get(key).getMongoOptions().threadsAllowedToBlockForConnectionMultiplier = 1000;
							s.printTime();
						} else {
							StopWatch s = new StopWatch("INFO: new Mongo(seeds)", false);
							List<ServerAddress> seeds = new ArrayList<ServerAddress>();
							for (String h : optHosts.split(","))
								seeds.add(new ServerAddress(h));
							m.put(key, new Mongo(seeds));
							m.get(key).slaveOk();
							m.get(key).getMongoOptions().connectionsPerHost = SystemAnalysis.getNumberOfCPUs();
							m.get(key).getMongoOptions().threadsAllowedToBlockForConnectionMultiplier = 1000;
							s.printTime();
						}
						if (authenticatedDBs.get(m.get(key)) == null || !authenticatedDBs.get(m.get(key)).contains("admin")) {
							DB dbAdmin = m.get(key).getDB("admin");
							try {
								StopWatch s = new StopWatch("INFO: dbAdmin.authenticate()");
								dbAdmin.authenticate(optLogin, optPass.toCharArray());
								s.printTime();
								if (authenticatedDBs.get(m.get(key)) == null)
									authenticatedDBs.put(m.get(key), new HashSet<String>());
								authenticatedDBs.get(m.get(key)).add(database);
							} catch (Exception err) {
								// System.err.println("ERROR: " + err.getMessage());
							}
						}
					}
					db = m.get(key).getDB(database);
					
					if (authenticatedDBs.get(m.get(key)) == null || !authenticatedDBs.get(m.get(key)).contains(database))
						if (optLogin != null && optPass != null && optLogin.length() > 0 && optPass.length() > 0) {
							try {
								boolean auth = db.authenticate(optLogin, optPass.toCharArray());
								if (!auth) {
									// throw new Exception("Invalid MongoDB login data provided!");
								} else {
									if (authenticatedDBs.get(m.get(key)) == null)
										authenticatedDBs.put(m.get(key), new HashSet<String>());
									authenticatedDBs.get(m.get(key)).add(database);
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
					err.printStackTrace();
					System.out.println("EXEC " + (nrep - repeats + 1) + " ERROR: " + err.getLocalizedMessage() + " T=" + IAPservice.getCurrentTimeAsNiceString());
					e = err;
					Thread.sleep(60 * 1000);
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
						System.out.println(SystemAnalysis.getCurrentTime() + ">DELETE WRITERESULT: ERROR? " + wr.getLastError().getErrorMessage());
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
		
		System.out.println(">>> " + SystemAnalysis.getCurrentTime());
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
		
		if (status != null)
			status.setCurrentStatusText1(SystemAnalysis.getCurrentTime() + ">Determine Size");
		{
			if (experiment.getHeader().getDatabaseId() != null && !experiment.getHeader().getDatabaseId().startsWith("hsm:")) {
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
			SubstanceInterface s = sl.get(0);
			sl.remove(0);
			// if (status != null && status.wantsToStop())
			// break;
			if (status != null)
				status.setCurrentStatusText1(SystemAnalysis.getCurrentTime() + ">SAVE SUBSTANCE " + s.getName());
			attributes.clear();
			s.fillAttributeMap(attributes);
			BasicDBObject substance = new BasicDBObject(filter(attributes));
			// dbSubstances.add(substance);
			
			ArrayList<String> conditionIDs = new ArrayList<String>();
			
			for (ConditionInterface c : s) {
				// if (status != null && status.wantsToStop())
				// break;
				attributes.clear();
				c.fillAttributeMap(attributes);
				BasicDBObject condition = new BasicDBObject(filter(attributes));
				
				List<BasicDBObject> dbSamples = new ArrayList<BasicDBObject>();
				for (SampleInterface sa : c) {
					// if (status != null && status.wantsToStop())
					// break;
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
									// boolean direct = true;
									// if (direct) {
									res = saveImageFileDirect(db, id, overallFileSize,
											keepDataLinksToDataSource_safe_space);
									
									attributes.clear();
									id.fillAttributeMap(attributes);
									BasicDBObject dbo = new BasicDBObject(filter(attributes));
									dbImages.add(dbo);
									
									// } else {
									// storageResults.add(saveImageFile(db, id, overallFileSize,
									// keepDataLinksToDataSource_safe_space));
									// imageDataQueue.add(id);
									// }
									// count++;
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
				try {
					conditions.insert(condition);
					conditionIDs.add((condition).getString("_id"));
				} catch (MongoInternalException mie) {
					System.out.println("Invalid condition: " + c + ", with " + c.size() + " samples");
				}
				
			} // condition
			processSubstanceSaving(status, substances, substance, conditionIDs);
			substanceIDs.add((substance).getString("_id"));
			
		} // substance
		
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
			System.out.println(">>> STORED EXPERIMENT " + experiment.getHeader().getExperimentName() + " // DB-ID: " + id + " // "
					+ SystemAnalysis.getCurrentTime());
			for (ExperimentHeaderInterface eh : experiment.getHeaders()) {
				eh.setDatabaseId(id);
			}
		}
		
		updateExperimentSize(db, experiment, status);
		
		// System.out.print(SystemAnalysis.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
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
			status.setCurrentStatusText1(SystemAnalysis.getCurrentTime() + ">INSERT SUBSTANCE " + dbSubstance.get("name"));
		
		dbSubstance.put("condition_ids", conditionIDs);
		substances.insert(dbSubstance);
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
			GridFS gridfs_preview_files, ImageData image, String hashMain, String hashLabel,
			boolean storeMain, boolean storeLabel) throws IOException {
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
						if (storeLabel && gridfs_label_images != null && hashLabel != null) {
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
				if (fs != null && hash != null && is != null)
					if (saveStream(hash, is, fs) < 0)
						allOK = false;
			}
		} catch (Exception e) {
			System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: SAVING IMAGE FILE TO MONGDB FAILED WITH EXCEPTION: " + e.getMessage());
			e.printStackTrace();
		}
		
		return allOK;
	}
	
	/**
	 * @return -1 in case of error, 0 in case of existing storage, > 0 in case of new storage
	 * @throws Exception
	 */
	public long saveStream(String hash, InputStream is, GridFS fs) throws Exception {
		long result = -1;
		
		GridFSDBFile fff = fs.findOne(hash);
		if (fff != null) {
			fs.remove(fff);
			fff = null;
		}
		
		boolean compress = false;
		
		GridFSInputFile inputFile = fs.createFile(compress ?
				ResourceIOManager.getCompressedInputStream((MyByteArrayInputStream) is) : is, hash);
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
	
	private final ExecutorService storageTaskQueue = Executors.newFixedThreadPool(3, new ThreadFactory() {
		int n = 1;
		
		@Override
		public Thread newThread(Runnable r) {
			Thread res = new Thread(r);
			res.setName("File Stream Storage Thread " + n);
			n++;
			return res;
		}
	});
	
	// public Future<DatabaseStorageResult> saveImageFile(final DB db,
	// final ImageData image, final ObjectRef fileSize,
	// final boolean keepRemoteURLs_safe_space) throws Exception {
	//
	// return storageTaskQueue.submit(new Callable<DatabaseStorageResult>() {
	// @Override
	// public DatabaseStorageResult call() throws Exception {
	// return saveImageFileDirect(db, image, fileSize, keepRemoteURLs_safe_space);
	// }
	// });
	// }
	
	public DatabaseStorageResult saveImageFile(final DB db,
			final ImageData image, final ObjectRef fileSize,
			final boolean keepRemoteURLs_safe_space) throws Exception {
		
		return saveImageFileDirect(db, image, fileSize, keepRemoteURLs_safe_space);
	}
	
	protected boolean processLabelData(boolean keepRemoteURLs_safe_space, IOurl labelURL) {
		return !keepRemoteURLs_safe_space || (labelURL != null && (labelURL.getPrefix().equals(LemnaTecFTPhandler.PREFIX)
				|| labelURL.getPrefix().startsWith("hsm_")));
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
			
			if (ensureIndex)
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
				if (ensureIndex)
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
		if (ensureIndex)
			collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		GridFS gridfs_preview = new GridFS(db, MongoGridFS.FS_PREVIEW.toString());
		DBCollection collectionB = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
		if (ensureIndex)
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
					HashMap<String, String> mapableNames = new HashMap<String, String>();
					mapableNames.put("klukas", "Christian Klukas");
					for (DBObject header : col.find()) {
						ExperimentHeader h = new ExperimentHeader(header.toMap());
						h.setStorageTime(new Date(((ObjectId) header.get("_id")).getTime()));
						if (h.getImportusername() == null || h.getImportusername().isEmpty()) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: FIXING INTERNAL PROBLEM: IMPORT USER NAME IS EMPTY, UPDATING INFO WITH LOCAL USER INFO");
							h.setImportusername(SystemAnalysis.getUserName());
							header.put("importusername", h.getImportusername());
							if (h.getImportusername() != null && !h.getImportusername().isEmpty())
								col.save(header, WriteConcern.SAFE);
							else
								System.out.println(SystemAnalysis.getCurrentTime()
										+ ">ERROR: USER INFO COULD NOT BE UPDATED (LOCAL INFO IS NULL OR EMPTY)");
						}
						if (h.getExperimentName() != null && h.getExperimentName().contains("(manual merge")) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">INFO: REMOVE _manual merge_ note from experiment name");
							h.setExperimentname(h.getExperimentName().substring(0, h.getExperimentName().indexOf(" (manual merge")));
							header.put("experimentname", h.getExperimentName());
							col.save(header, WriteConcern.SAFE);
						}
						if (mapableNames.containsKey(h.getImportusername())) {
							String newName = mapableNames.get(h.getImportusername());
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">INFO: FIXING ACCOUNT NAME (changing " + h.getImportusername() + " to " + newName + ")");
							h.setImportusername(newName);
							header.put("importusername", h.getImportusername());
							col.save(header, WriteConcern.SAFE);
						}
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
						if (ensureIndex)
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
										if (optDBPbjectsOfSubstances != null)
											optDBPbjectsOfSubstances.add(substance);
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
		int nmin = Integer.MAX_VALUE;
		int nmax = 0;
		if (experiment != null) {
			for (SubstanceInterface si : experiment) {
				if (si.size() > nmax)
					nmax = si.size();
				if (si.size() < nmin)
					nmin = si.size();
			}
			System.out.println("Condition-Count: " + nmin + "..." + nmax);
		}
		return experiment;
	}
	
	protected void visitExperiment(
			final ExperimentHeaderInterface header,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider,
			final RunnableProcessingDBid visitSubstance,
			final RunnableProcessingDBid visitCondition,
			final RunnableProcessingBinaryMeasurement visitBinaryMeasurement) {
		
		if (optStatusProvider != null)
			optStatusProvider.setCurrentStatusValue(0);
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), new ObjectId(header.getDatabaseId()));
					DBObject expref = dbr.fetch();
					if (expref != null) {
						BasicDBList subList = (BasicDBList) expref.get("substances");
						if (subList != null)
							for (Object co : subList) {
								DBObject substance = (DBObject) co;
								visitSubstance(db, substance, optStatusProvider, 100d / subList.size(),
										visitCondition,
										visitBinaryMeasurement);
							}
						if (ensureIndex)
							db.getCollection("substances").ensureIndex("_id");
						BasicDBList l = (BasicDBList) expref.get("substance_ids");
						if (l != null)
							for (Object o : l) {
								if (o == null)
									continue;
								DBRef subr = new DBRef(db, "substances", new ObjectId(o.toString()));
								if (subr != null) {
									DBObject substance = subr.fetch();
									visitSubstance.setDBid(substance.get("_id") + "");
									visitSubstance.run();
									if (substance != null) {
										visitSubstance(db, substance, optStatusProvider, 100d / l.size(), visitCondition, visitBinaryMeasurement);
									}
								}
							}
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
						if (optStatusProvider != null && n % 1000 == 0) {
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
				if (optStatusProvider != null) {
					optStatusProvider.setCurrentStatusValueFine(100d * n / max);
					optStatusProvider.setCurrentStatusText2("(" + n + "/" + (int) max + ", " + newSize.getLong() / 1024 / 1024 + " MB)");
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
			final double progress,
			final String status3) throws Exception {
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
					res.setStatus3(status3);
					double load = SystemAnalysisExt.getRealSystemCpuLoad();
					boolean monitor = !CloudComputingService.getInstance(MongoDB.this).getIsCalculationPossible();
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
					if (monitor)
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
					for (DBObject dbo : collection.find().sort(new BasicDBObject("submission", 1))) {
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
		final ArrayList<BatchCmd> res = new ArrayList<BatchCmd>();
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
						for (DBObject dbo : collection.find(BatchCmd.getRunstatusMatcher(CloudAnalysisStatus.SCHEDULED)).sort(new BasicDBObject("submission", 1))
								.limit(maxTasks)) {
							BatchCmd batch = (BatchCmd) dbo;
							if (batch.getCpuTargetUtilization() < maxTasks) {
								if (batch.getExperimentHeader() == null)
									continue;
								if (batch.getOwner() == null)
									if (batchClaim(batch, CloudAnalysisStatus.STARTING, false))
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
						if (addCnt < maxTasks) {
							loop: for (DBObject dbo : collection.find().sort(new BasicDBObject("submission", 1))) {
								BatchCmd batch = (BatchCmd) dbo;
								if (batch.getExperimentHeader() == null)
									continue;
								if (!added && batch.getCpuTargetUtilization() <= maxTasks)
									if (batch.get("lastupdate") == null || (System.currentTimeMillis() - batch.getLastUpdateTime() > 5 * 60000)) {
										// after 5 minutes tasks are taken away from other systems
										if (batchClaim(batch, CloudAnalysisStatus.STARTING, false)) {
											claimed++;
											if (claimed >= maxTasks)
												break loop;
										}
									}
							}
						}
						if (addCnt < maxTasks)
							for (DBObject dbo : collection.find(BatchCmd.getRunstatusMatcher(CloudAnalysisStatus.STARTING)).sort(new BasicDBObject("submission", 1))) {
								BatchCmd batch = (BatchCmd) dbo;
								if (batch.getExperimentHeader() == null)
									continue;
								if (batch.getCpuTargetUtilization() <= maxTasks && hostName.equals("" + batch.getOwner())) {
									res.add(batch);
									addCnt += batch.getCpuTargetUtilization();
									if (addCnt >= maxTasks)
										added = true;
									break;
								}
							}
						if (addCnt < maxTasks)
							for (DBObject dbo : collection.find(BatchCmd.getRunstatusMatcher(CloudAnalysisStatus.FINISHED_INCOMPLETE)).sort(
									new BasicDBObject("submission", 1))) {
								BatchCmd batch = (BatchCmd) dbo;
								if (batch.getExperimentHeader() == null)
									continue;
								if (batch.getCpuTargetUtilization() <= maxTasks && hostName.equals("" + batch.getOwner())) {
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
			System.out.println(SystemAnalysis.getCurrentTime() + ">SCHEDULED FOR START: " + res.size());
			java.util.Collections.sort(res, new Comparator<BatchCmd>() {
				@Override
				public int compare(BatchCmd o1, BatchCmd o2) {
					Long a = o1.getSubmissionTime();
					Long b = o2.getSubmissionTime();
					int res = a.compareTo(b);
					if (res != 0)
						return res;
					Integer m = o1.getPartIdx();
					Integer n = o2.getPartIdx();
					return m.compareTo(n);
				}
			});
		}
		return res;
	}
	
	public boolean batchClaim(final BatchCmd batch, final CloudAnalysisStatus starting, final boolean requireOwnership) {
		// try to claim a batch cmd
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setBval(0, false);
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
						db.requestStart();
						CommandResult cr = null;
						try {
							collection.update(dbo, batch, false, false, WriteConcern.NORMAL);
							cr = db.getLastError();
						} finally {
							db.requestDone();
						}
						boolean success = cr != null && cr.getBoolean("updatedExisting", false);
						if (!success) {
							try {
								DBObject dbo2 = new BasicDBObject();
								dbo2.put("_id", batch.get("_id"));
								dbo2 = collection.findOne(dbo2);
								System.err.println();
								System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: NEW OWNER: " + dbo2.get("owner"));
							} catch (Exception e) {
								// empty
							}
						}
						tso.setBval(0, success);
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
		return tso.getBval(0, false);
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
					if (batch != null && batch.get("_id") != null && collection != null) {
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
				if (optDBObjectsConditions != null)
					optDBObjectsConditions.add(cond);
				processCondition(s3d, cond);
			}
		if (ensureIndex)
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
	
	private void visitSubstance(DB db, DBObject substance,
			BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider, double smallProgressStep,
			RunnableProcessingDBid visitCondition,
			RunnableProcessingBinaryMeasurement visitBinary) {
		@SuppressWarnings("unchecked")
		Substance3D s3d = new Substance3D(substance.toMap());
		BasicDBList condList = (BasicDBList) substance.get("conditions");
		if (condList != null)
			for (Object co : condList) {
				DBObject cond = (DBObject) co;
				visitCondition(s3d, cond, visitBinary);
				
			}
		if (ensureIndex)
			db.getCollection("conditions").ensureIndex("_id");
		BasicDBList l = (BasicDBList) substance.get("condition_ids");
		if (l != null) {
			double max = l.size();
			for (Object o : l) {
				DBRef condr = new DBRef(db, "conditions", new ObjectId(o.toString()));
				DBObject cond = condr.fetch();
				if (cond != null) {
					visitCondition.setDBid(cond.get("_id") + "");
					visitCondition.run();
					visitCondition(s3d, cond, visitBinary);
				}
				if (optStatusProvider != null)
					optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep * 1 / max);
			}
		}
	}
	
	private void visitCondition(Substance3D s3d, DBObject cond, final RunnableProcessingBinaryMeasurement visitBinary) {
		BasicDBList sampList = (BasicDBList) cond.get("samples");
		if (sampList != null)
			for (Object so : sampList) {
				DBObject sam = (DBObject) so;
				// images
				BasicDBList imgList = (BasicDBList) sam.get(MongoCollection.IMAGES.toString());
				if (imgList != null) {
					for (Object m : imgList) {
						DBObject img = (DBObject) m;
						@SuppressWarnings("unchecked")
						ImageData image = new ImageData(null, filter(img.toMap()));
						image.getURL().setPrefix(mh.getPrefix());
						if (image.getLabelURL() != null)
							image.getLabelURL().setPrefix(mh.getPrefix());
						visitBinary.setBinaryMeasurement(image);
						visitBinary.run();
					}
				}
				// volumes
				BasicDBList volList = (BasicDBList) sam.get(MongoCollection.VOLUMES.toString());
				if (volList != null) {
					for (Object v : volList) {
						DBObject vol = (DBObject) v;
						@SuppressWarnings("unchecked")
						VolumeData volume = new VolumeData(null, vol.toMap());
						if (volume.getURL() != null)
							volume.getURL().setPrefix(mh.getPrefix());
						if (volume.getLabelURL() != null)
							volume.getLabelURL().setPrefix(mh.getPrefix());
						visitBinary.setBinaryMeasurement(volume);
						visitBinary.run();
					}
				}
				// networks
				BasicDBList netList = (BasicDBList) sam.get(MongoCollection.NETWORKS.toString());
				if (netList != null) {
					for (Object n : netList) {
						DBObject net = (DBObject) n;
						@SuppressWarnings("unchecked")
						NetworkData network = new NetworkData(null, net.toMap());
						if (network.getURL() != null)
							network.getURL().setPrefix(mh.getPrefix());
						if (network.getLabelURL() != null)
							network.getLabelURL().setPrefix(mh.getPrefix());
						visitBinary.setBinaryMeasurement(network);
						visitBinary.run();
					}
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
						ImageData image = new ImageData(sample, filter(img.toMap()));
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
	
	private Map filter(Map map) {
		if (map == null || map.isEmpty())
			return map;
		for (Object key : map.keySet().toArray()) {
			Object val = map.get(key);
			if (val == null ||
					((val instanceof String) && ((String) val).isEmpty())) {
				map.remove(key);
			}
		}
		return map;
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
					if (ensureIndex)
						collectionC.ensureIndex("filename");
					
					DBCollection collectionChunks = db.getCollection("fs_preview_files.chunks");
					if (ensureIndex)
						collectionChunks.ensureIndex("files_id");
					
					result.setObject(gridfs_preview_files);
				} else
					switch (c.getDatatype()) {
						case IMAGE:
							switch (c.getStorageType()) {
								case MAIN_STREAM:
									GridFS gridfs_images = new GridFS(db, MongoGridFS.FS_IMAGES.toString());
									DBCollection collectionA = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString());
									if (ensureIndex)
										collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString() + ".chunks");
									if (ensureIndex)
										collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_images);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
									DBCollection collectionB = db.getCollection(MongoGridFS.FS_IMAGE_LABELS_FILES.toString());
									if (ensureIndex)
										collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									collectionChunks = db.getCollection(MongoGridFS.FS_IMAGE_LABELS.toString() + ".chunks");
									if (ensureIndex)
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
									if (ensureIndex)
										collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_VOLUMES_FILES.toString() + ".chunks");
									if (ensureIndex)
										collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_volumes);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_VOLUME_LABELS.toString());
									DBCollection collectionB = db.getCollection("fs_volume_labels.files");
									if (ensureIndex)
										collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									collectionChunks = db.getCollection(MongoGridFS.FS_VOLUME_LABELS.toString() + ".chunks");
									if (ensureIndex)
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
									if (ensureIndex)
										collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_NETWORKS.toString() + ".chunks");
									if (ensureIndex)
										collectionChunks.ensureIndex("files_id");
									
									result.setObject(gridfs_networks);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_NETWORK_LABELS.toString());
									DBCollection collectionB = db.getCollection(MongoGridFS.fs_networks_labels_files.toString());
									if (ensureIndex)
										collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									
									collectionChunks = db.getCollection(MongoGridFS.FS_NETWORK_LABELS.toString() + ".chunks");
									if (ensureIndex)
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
					if (ensureIndex)
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
					if (ensureIndex)
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
				System.out.println("REORGANIZATION: Create inventory... // " + SystemAnalysis.getCurrentTime());
				res.append("REORGANIZATION: Create inventory... // " + SystemAnalysis.getCurrentTime() + "<br>");
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
						+ SystemAnalysis.getCurrentTime());
				res.append("REORGANIZATION: Stored binary files: " + numberOfBinaryFilesInDatabase + " // "
						+ SystemAnalysis.getCurrentTime() + "<br>");
				status.setCurrentStatusText2("Binary files: " + numberOfBinaryFilesInDatabase);
				status.setCurrentStatusValueFine(100d / 5 * 1);
				
				ArrayList<ExperimentHeaderInterface> el = getExperimentList(null);
				final HashSet<String> linkedHashes = new HashSet<String>();
				double smallStep = 100d / 5 * 1 / el.size();
				final HashSet<String> dbIdsOfSubstances = new HashSet<String>();
				final HashSet<String> dbIdsOfConditions = new HashSet<String>();
				status.setCurrentStatusText2("Read list of substances");
				double oldStatus = status.getCurrentStatusValueFine();
				status.setCurrentStatusValue(-1);
				{
					DBCollection substances = db.getCollection("substances");
					long nn = 0, max = substances.count();
					status.setCurrentStatusText2("Read list of substance IDs (" + max + ")");
					DBCursor subCur = substances.find(new BasicDBObject(), new BasicDBObject("_id", 1));
					while (subCur.hasNext()) {
						DBObject subO = subCur.next();
						dbIdsOfSubstances.add(subO.get("_id") + "");
						nn++;
						if (nn % 500 == 0) {
							status.setCurrentStatusText2("Read list of substances (" + nn + "/" + max + ")");
							status.setCurrentStatusValueFine(100d * nn / max);
						}
					}
					status.setCurrentStatusText2("Read list of substance IDs (" + nn + ")");
				}
				status.setCurrentStatusText1(status.getCurrentStatusMessage2());
				status.setCurrentStatusText2("Read list of condition IDs");
				{
					DBCollection conditions = db.getCollection("conditions");
					long nn = 0, max = conditions.count();
					status.setCurrentStatusText2("Read list of condition IDs (" + max + ")");
					DBCursor condCur = conditions.find(new BasicDBObject(), new BasicDBObject("_id", 1));
					while (condCur.hasNext()) {
						DBObject condO = condCur.next();
						dbIdsOfConditions.add(condO.get("_id") + "");
						nn++;
						if (nn % 500 == 0) {
							status.setCurrentStatusText2("Read list of condition IDs (" + nn + "/" + max + ")");
							status.setCurrentStatusValueFine(100d * nn / max);
						}
						
					}
					status.setCurrentStatusText2("Read list of condition IDs (" + nn + "/" + max + ")");
				}
				status.setCurrentStatusText1("Create inventory");
				status.setCurrentStatusValueFine(oldStatus);
				int ii = 0;
				ArrayList<ExperimentHeaderInterface> todo = new ArrayList<ExperimentHeaderInterface>();
				for (ExperimentHeaderInterface ehii : el) {
					todo.add(ehii);
					for (ExperimentHeaderInterface old : ehii.getHistory().values())
						todo.add(old);
				}
				int nn = todo.size();
				for (ExperimentHeaderInterface ehii : todo) {
					ii++;
					status.setCurrentStatusText2("Analyze " + ehii.getExperimentName() + " (" + ii + "/" + nn + ")");
					
					RunnableProcessingDBid visitSubstance = new RunnableProcessingDBid() {
						private String id;
						
						@Override
						public void run() {
							dbIdsOfSubstances.remove(id);
						}
						
						@Override
						public void setDBid(String id) {
							this.id = id;
						}
					};
					RunnableProcessingDBid visitCondition = new RunnableProcessingDBid() {
						private String id;
						
						@Override
						public void run() {
							dbIdsOfConditions.remove(id);
						}
						
						@Override
						public void setDBid(String id) {
							this.id = id;
						}
					};
					RunnableProcessingBinaryMeasurement visitBinaryMeasurement = new RunnableProcessingBinaryMeasurement() {
						private BinaryMeasurement bm;
						
						@Override
						public void run() {
							if (bm.getURL() != null)
								linkedHashes.add(bm.getURL().getDetail());
							if (bm.getLabelURL() != null)
								linkedHashes.add(bm.getLabelURL().getDetail());
							if (bm instanceof ImageData) {
								ImageData imData = (ImageData) bm;
								String oldRef = imData.getAnnotationField("oldreference");
								if (oldRef != null && oldRef.length() > 0) {
									IOurl u = new IOurl(oldRef);
									linkedHashes.add(u.getDetail());
								}
							}
						}
						
						@Override
						public void setBinaryMeasurement(BinaryMeasurement bm) {
							this.bm = bm;
						}
					};
					
					visitExperiment(ehii, status, visitSubstance, visitCondition, visitBinaryMeasurement);
					
					status.setCurrentStatusValueFineAdd(smallStep);
				} // experiments
				
				{
					DBCollection substances = db.getCollection("substances");
					long cnt = substances.count();
					long max = dbIdsOfSubstances.size();
					System.out.println("Remove stale substances: " + max + "/" + cnt);
					status.setCurrentStatusText1("Remove stale substances: " + max + "/" + cnt);
					int n = 0;
					for (String subID : dbIdsOfSubstances) {
						n++;
						DBObject del = substances.findOne(new BasicDBObject("_id", new ObjectId(subID)));
						substances.remove(del, WriteConcern.NONE);
						status.setCurrentStatusValueFine(100d / max * n);
						status.setCurrentStatusText2(n + "/" + max);
					}
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: REMOVED " + (cnt - substances.count()) + " SUBSTANCE OBJECTS");
				}
				{
					int n = 0;
					long max = dbIdsOfConditions.size();
					DBCollection conditions = db.getCollection("conditions");
					long cnt = conditions.count();
					System.out.println("Remove stale conditions: " + dbIdsOfConditions.size() + "/" + cnt);
					status.setCurrentStatusText1("Remove stale conditions: " + dbIdsOfConditions.size() + "/" + cnt);
					for (String condID : dbIdsOfConditions) {
						n++;
						conditions.remove(new BasicDBObject("_id", new ObjectId(condID)), WriteConcern.NONE);
						status.setCurrentStatusValueFine(100d / max * n);
						status.setCurrentStatusText2(n + "/" + max);
					}
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: REMOVED " + (cnt - conditions.count()) + " CONDITION OBJECTS");
				}
				
				if (linkedHashes.size() >= 0) {
					long freeAll = 0;
					System.out.println("REORGANIZATION: Linked binary files: " + linkedHashes.size() + " // " + SystemAnalysis.getCurrentTime());
					res.append("REORGANIZATION: Linked binary files: " + linkedHashes.size() + " // " + SystemAnalysis.getCurrentTime() + "<br>");
					status.setCurrentStatusText1("Linked files: " + linkedHashes.size());
					status.setCurrentStatusText2("");
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
									+ SystemAnalysis.getCurrentTime());
							res.append("REORGANIZATION: Binary files that are not linked (" + mgfs + "): " + toBeRemoved.size() + " // "
									+ SystemAnalysis.getCurrentTime() + "<br>");
							long free = 0;
							int fIdx = 0;
							int fN = toBeRemoved.size();
							for (GridFSDBFile f : toBeRemoved) {
								free += f.getLength();
								gridfs.remove(f);
								fIdx++;
								status.setCurrentStatusText1("File " + fIdx + "/" + fN + " (" + (int) (100d * fIdx / fN) + "%)");
								status.setCurrentStatusText2("Removed: " + free / 1024 / 1024 + " MB");
								status.setCurrentStatusValueFine(fIdx * 100d / fN);
							}
							System.out.println("REORGANIZATION: Deleted MB (" + mgfs + "): " + free / 1024 / 1024 + " // "
									+ SystemAnalysis.getCurrentTime());
							res.append("REORGANIZATION: Deleted MB (" + mgfs + "): " + free / 1024 / 1024 + " // "
									+ SystemAnalysis.getCurrentTime() + "<br>");
							
							freeAll += free;
						}
						status.setCurrentStatusValueFineAdd(stepSize);
					}
					System.out.println("REORGANIZATION: Overall deleted MB: " + freeAll / 1024 / 1024 + " // "
							+ SystemAnalysis.getCurrentTime());
					
					res.append("REORGANIZATION: Overall deleted MB: " + freeAll / 1024 / 1024 + " // "
							+ SystemAnalysis.getCurrentTime() + "<br>");
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
	
	public synchronized String compact(
			final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		final StringBuilder res = new StringBuilder();
		processDB(new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				Set<String> col = db.getCollectionNames();
				int n = 0;
				for (String mgfs : col) { // MongoGridFS.getFileCollectionsInclPreview()
					System.out.println("Start compact collection (" + mgfs + ") // " + SystemAnalysis.getCurrentTime());
					res.append("Start compact collection (" + mgfs + ") // " + SystemAnalysis.getCurrentTime() + "<br>");
					status.setCurrentStatusText1("Start compact of " + mgfs + " at " + SystemAnalysis.getCurrentTime());
					status.setCurrentStatusText2("Size: " + db.getCollection(mgfs).getCount());
					HashMap<String, Object> m = new HashMap<String, Object>();
					m.put("compact", mgfs);// + ".files");
					m.put("force", true);
					BasicDBObject cmd = new BasicDBObject(m);
					db.command(cmd);
					
					SystemAnalysis.sleep(10000);
					
					// m.clear();
					// m.put("compact", mgfs);
					// m.put("force", true);
					// cmd = new BasicDBObject(m);
					// db.command(cmd);
					//
					// m.clear();
					// m.put("compact", mgfs + ".chunks");
					// m.put("force", true);
					// cmd = new BasicDBObject(m);
					// db.command(cmd);
					
					System.out.println("Finished compact collection (" + mgfs + ") // " + SystemAnalysis.getCurrentTime());
					res.append("Finished compact collection (" + mgfs + ") // " + SystemAnalysis.getCurrentTime() + "<br>");
					n++;
					status.setCurrentStatusValueFine(100d * n / col.size());
				}
				System.out.println("COMPACT DATABASE FINISHED // "
						+ SystemAnalysis.getCurrentTime());
				
				res.append("COMPACT DATABASE FINISHED // "
						+ SystemAnalysis.getCurrentTime() + "<br>");
				status.setCurrentStatusText1("Compact operation finished");
				status.setCurrentStatusValueFine(100d);
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		return res.toString();
	}
	
	private DatabaseStorageResult saveImageFileDirect(final DB db, final ImageData image, final ObjectRef fileSize, final boolean keepRemoteURLs_safe_space)
			throws Exception, IOException {
		// if the image data source is equal to the target (determined by the prefix),
		// the image content does not need to be copied (assumption valid while using MongoDB data storage)
		if (image.getURL() != null && image.getLabelURL() != null) {
			if (image.getURL().getPrefix().equals(mh.getPrefix()) && image.getLabelURL().getPrefix().equals(mh.getPrefix()))
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		}
		/*
		 * if (image.getURL() != null && image.getLabelURL() != null) {
		 * if (id.getURL().getPrefix().equals(mh.getPrefix())
		 * && id.getLabelURL().getPrefix().equals(mh.getPrefix())) {
		 * if ((image.getURL().getPrefix().equals(LemnaTecFTPhandler.PREFIX)
		 * || image.getURL().getPrefix().startsWith("hsm_"))) {
		 * if (keepDataLinksToDataSource_safe_space) {
		 * return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		 * }
		 * }
		 * }
		 * }
		 */
		// check if the source URL has been imported before, it is assumed that the source URL content
		// is not modified
		if (image.getURL() != null &&
				(image.getURL().getPrefix().equals(LemnaTecFTPhandler.PREFIX) ||
				image.getURL().getPrefix().startsWith("hsm_"))) {
			if (keepRemoteURLs_safe_space)
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
			
			DBObject knownURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getURL().toString()));
			
			if (processLabelData(keepRemoteURLs_safe_space, image.getLabelURL()) && image.getLabelURL() != null) {
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
				isMain = image.getURL() != null ? ResourceIOManager.getInputStreamMemoryCached(image.getURL()).getBuffTrimmed() : null;
			} catch (Exception e) {
				System.out.println("Error: No Inputstream for " + image.getURL() + ". " + e.getMessage() + " // " + SystemAnalysis.getCurrentTime());
			}
			try {
				if (processLabelData(keepRemoteURLs_safe_space, image.getLabelURL()))
					isLabel = image.getLabelURL() != null ? ResourceIOManager.getInputStreamMemoryCached(image.getLabelURL()).getBuffTrimmed() : null;
			} catch (Exception e) {
				System.out.println("Error: No Inputstream for " + image.getLabelURL() + ". " + e.getMessage() + " // "
						+ SystemAnalysis.getCurrentTime());
			}
		} finally {
			// BackgroundTaskHelper.lockRelease(image.getURL() != null ? image.getURL().getPrefix() : "in");
		}
		if (isMain == null) {
			System.out.println("No input stream for source-URL:  " + image.getURL());
			return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
		if (image.getLabelURL() != null && isLabel == null) {
			// System.out.println("No input stream for source-URL (label):  " + image.getURL());
			// return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
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
			if (ensureIndex)
				db.getCollection("constantSrc2hash").ensureIndex("srcUrl");
			
			if (hashMain != null) {
				DBObject knownURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getURL().toString()));
				if (knownURL == null) {
					Map<String, String> m1 = new HashMap<String, String>();
					m1.put("srcUrl", image.getURL().toString());
					m1.put("hash", hashMain);
					db.getCollection("constantSrc2hash").insert(new BasicDBObject(m1));
				}
			}
			if (hashLabel != null) {
				DBObject knownLabelURL = db.getCollection("constantSrc2hash").findOne(new BasicDBObject("srcUrl", image.getLabelURL().toString()));
				if (knownLabelURL == null) {
					Map<String, String> m1 = new HashMap<String, String>();
					m1.put("srcUrl", image.getLabelURL().toString());
					m1.put("hash", hashLabel);
					db.getCollection("constantSrc2hash").insert(new BasicDBObject(m1));
				}
			}
		}
		
		GridFS gridfs_images = new GridFS(db, "" + MongoGridFS.FS_IMAGES.toString());
		DBCollection collectionA = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString());
		if (ensureIndex)
			collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		GridFS gridfs_label_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
		DBCollection collectionB = db.getCollection(MongoGridFS.FS_IMAGE_LABELS_FILES.toString());
		if (ensureIndex)
			collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		GridFS gridfs_preview_files = new GridFS(db, MongoGridFS.FS_PREVIEW.toString());
		DBCollection collectionC = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
		if (ensureIndex)
			collectionC.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		GridFSDBFile fffMain = gridfs_images.findOne(hashMain);
		image.getURL().setPrefix(mh.getPrefix());
		image.getURL().setDetail(hashMain);
		
		GridFSDBFile fffLabel = null;
		if (hashLabel != null && image.getLabelURL() != null) {
			fffLabel = gridfs_label_files.findOne(hashLabel);
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
		
		if (hashLabel != null && image.getLabelURL() != null) {
			if (fffLabel != null) {
				image.getLabelURL().setPrefix(mh.getPrefix());
				image.getLabelURL().setDetail(hashLabel);
			}
		}
		
		if (fffMain != null && fffLabel != null && fffPreview != null) {
			return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		} else {
			boolean saved;
			saved = saveImageFile(new InputStream[] {
					new MyByteArrayInputStream(isMain),
					isLabel != null ? new MyByteArrayInputStream(isLabel) : null,
					getPreviewImageStream(new MyByteArrayInputStream(isMain))
			}, gridfs_images, gridfs_label_files,
					gridfs_preview_files, image, hashMain,
					hashLabel,
					fffMain == null, fffLabel == null);
			
			if (saved) {
				return DatabaseStorageResult.STORED_IN_DB;
			} else
				return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
	}
}

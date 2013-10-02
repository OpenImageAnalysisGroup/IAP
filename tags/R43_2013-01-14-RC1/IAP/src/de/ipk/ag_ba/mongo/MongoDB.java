/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.mongo;

import info.StopWatch;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.bson.types.ObjectId;
import org.graffiti.editor.HashType;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.picture_gui.MongoCollection;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.operation.ImageConverter;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author klukas
 */
public class MongoDB {
	
	public static boolean ensureIndex = true;
	
	private static final ArrayList<MongoDB> mongos = initMongoList();
	private static MongoDB defaultCloudInstance = null;
	private static HashMap<String, MongoClient> m = new HashMap<String, MongoClient>();
	private static HashSet<String> dbsAnalyzedForCollectionSettings = new HashSet<String>();
	
	private boolean enabled;
	final ArrayList<VirtualFileSystem> vfs_file_storage = new ArrayList<VirtualFileSystem>();
	
	private final String displayName;
	private final String databaseName;
	private final String databaseHost;
	private final String databaseLogin;
	private final String databasePass;
	private final HashType hashType;
	
	private final MongoDBhandler mh;
	
	WeakHashMap<MongoClient, HashSet<String>> authenticatedDBs = new WeakHashMap<MongoClient, HashSet<String>>();
	
	public static ArrayList<MongoDB> getMongos() {
		return mongos;
	}
	
	@Override
	public String toString() {
		return displayName + " (" + databaseHost + ", db " + databaseName + ")";
	}
	
	private static ArrayList<MongoDB> initMongoList() {
		ArrayList<MongoDB> res = new ArrayList<MongoDB>();
		int n = IAPoptions.getInstance().getInteger("GRID-STORAGE", "n", 1);
		for (int i = 0; i < n; i++) {
			MongoDB c = getCloud(i);
			if (c != null)
				if (c.enabled)
					res.add(c);
		}
		return res;
	}
	
	public static MongoDB getDefaultCloud() {
		return getCloud(0);
	}
	
	public static MongoDB getCloud(int idx) {
		String sec = "GRID-STORAGE-" + (idx + 1);
		
		boolean enabled = IAPoptions.getInstance().getBoolean(sec,
				"enabled", idx == 0 ? true : false);
		String displayName = IAPoptions.getInstance().getString(sec,
				"title", idx == 0 ? "Storage 1 (BA-13)" : "Storage " + (idx + 1));
		String databaseName = IAPoptions.getInstance().getString(sec,
				"database_name", "cloud" + (idx + 1));
		String hostName = IAPoptions.getInstance().getString(sec,
				"host", "ba-13.ipk-gatersleben.de");
		String login = IAPoptions.getInstance().getString(sec,
				"login", idx == 0 ? "iap" : null);
		String password = IAPoptions.getInstance().getString(sec,
				"password", idx == 0 ? "iap#2011" : null);
		if (idx == 0) {
			if (defaultCloudInstance == null) {
				defaultCloudInstance = new MongoDB(displayName, databaseName, hostName,
						login, password, HashType.MD5);
			}
			defaultCloudInstance.enabled = enabled;
			return defaultCloudInstance;
		} else {
			MongoDB mm = new MongoDB(displayName, databaseName, hostName,
					login, password, HashType.MD5);
			mm.enabled = enabled;
			return mm;
		}
	}
	
	public ResourceIOHandler[] getHandlers() {
		return new ResourceIOHandler[] { mh };
	}
	
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
		
		for (VirtualFileSystem vfs : VirtualFileSystem.getKnown(false)) {
			if (vfs instanceof VirtualFileSystemVFS2) {
				VirtualFileSystemVFS2 vf = (VirtualFileSystemVFS2) vfs;
				if (vf.isUseForMongoFileStorage()) {
					String n = vf.getUseForMongoFileStorageCloudName();
					if (databaseName.equals(n + ""))
						vfs_file_storage.add(vfs);
				}
			}
		}
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
		RunnableOnDB r = new ExperimentSaver(
				this,
				mh,
				getHashType(),
				experiment, keepDataLinksToDataSource_safe_space, status, err,
				getExperimentList(null));
		processDB(r);
		if (err.getParam(0, null) != null)
			throw (Exception) err.getParam(0, null);
	}
	
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
					String key = optHosts + ";" + database;
					if (m.get(key) == null) {
						if (optHosts == null || optHosts.length() == 0) {
							StopWatch s = new StopWatch("INFO: new MongoClient()", false);
							MongoClient mc = new MongoClient();
							mc.setWriteConcern(WriteConcern.NONE);
							m.put(key, mc);
							m.get(key).getMongoOptions().connectionsPerHost = 1;
							m.get(key).getMongoOptions().threadsAllowedToBlockForConnectionMultiplier = 1000000;
							m.get(key).getMongoOptions().socketTimeout = 24 * 60 * 60 * 1000;
							s.printTime();
						} else {
							StopWatch s = new StopWatch("INFO: new MongoClient(seeds)", false);
							List<ServerAddress> seeds = new ArrayList<ServerAddress>();
							for (String h : optHosts.split(","))
								seeds.add(new ServerAddress(h));
							m.put(key, new MongoClient(seeds));
							m.get(key).getMongoOptions().connectionsPerHost = 1;
							m.get(key).getMongoOptions().threadsAllowedToBlockForConnectionMultiplier = 1000000;
							m.get(key).getMongoOptions().socketTimeout = 24 * 60 * 60 * 1000;
							s.printTime(1000);
						}
						if (authenticatedDBs.get(m.get(key)) == null || !authenticatedDBs.get(m.get(key)).contains("admin")) {
							DB dbAdmin = m.get(key).getDB("admin");
							try {
								StopWatch s = new StopWatch("INFO: dbAdmin.authenticate()");
								dbAdmin.authenticate(optLogin, optPass.toCharArray());
								s.printTime(1000);
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
					
					boolean initStatusCollection = false;
					if (initStatusCollection && !dbsAnalyzedForCollectionSettings.contains(database)) {
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
					if (repeats - 1 > 0)
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
				DBCollection collCond = db.getCollection("conditions");
				if (ensureIndex)
					collCond.ensureIndex("_id");
				DBCollection collSubst = db.getCollection("substances");
				if (ensureIndex)
					collSubst.ensureIndex("_id");
				
				DBCollection collExps = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString());
				if (db.collectionExists(MongoExperimentCollections.EXPERIMENTS.toString())) {
					DBObject expRef = collExps.findOne(obj);
					if (expRef != null) {
						BasicDBList sl = (BasicDBList) expRef.get("substance_ids");
						if (sl != null) {
							for (Object so : sl) {
								DBRef subr = new DBRef(db, "substances", new ObjectId(so.toString()));
								if (subr != null) {
									DBObject substance = subr.fetch();
									if (substance != null) {
										BasicDBList cl = (BasicDBList) substance.get("condition_ids");
										if (cl != null) {
											for (Object oo : cl) {
												collCond.remove(new BasicDBObject("_id", new ObjectId(oo.toString())));
											}
										}
									}
									if (substance != null)
										try {
											collSubst.remove(substance);
										} catch (Exception err) {
											err.printStackTrace();
										}
								}
							}
						}
						WriteResult wr = collExps.remove(expRef);
						System.out.println("Remove write result: " + wr);
						
					}
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	/**
	 * @param long1
	 * @return -1 in case of error, 0 in case of existing storage, > 0 in case of new storage
	 * @throws Exception
	 */
	public long saveStream(String hash, InputStream is, GridFS fs, long expectedFileSize) throws Exception {
		long result = -1;
		
		boolean stored_in_VFS = false;
		if (vfs_file_storage != null) {
			VirtualFileSystemVFS2 lastVFS = null;
			for (VirtualFileSystem v : vfs_file_storage) {
				if (v instanceof VirtualFileSystemVFS2)
					lastVFS = (VirtualFileSystemVFS2) v;
			}
			if (lastVFS != null) {
				result = lastVFS.saveStream(fs.getBucketName() + "/" + hash, is, false, expectedFileSize);
				stored_in_VFS = true;
			}
		}
		
		if (!stored_in_VFS) {
			boolean compress = false;
			GridFSInputFile inputFile = fs.createFile(compress ?
					ResourceIOManager.getCompressedInputStream((MyByteArrayInputStream) is) : is, hash);
			inputFile.save();
			result = 1;// inputFile.getLength();
			is.close();
		}
		return result;
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
	
	public ArrayList<ExperimentHeaderInterface> getExperimentList(
			final String user,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		return getExperimentList(user, optStatus, null);
	}
	
	public ArrayList<ExperimentHeaderInterface> getExperimentList(
			final String user,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			final ThreadSafeOptions opt_last_ping) {
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
						optStatus.setCurrentStatusText1("Iterate Experimentlist");
					HashMap<String, String> mapableNames = new HashMap<String, String>();
					mapableNames.put("klukas", "Christian Klukas");
					for (DBObject header : col.find()) {
						if (opt_last_ping != null)
							opt_last_ping.setLong(System.currentTimeMillis());
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
								LemnaTecDataExchange.getAdministrators().contains(user) ||
								h.getCoordinator().toUpperCase().contains(user.toUpperCase()))
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
			processDB(new ExperimentLoader(this, mh, ensureIndex,
					optDBPbjectsOfSubstances, header, optStatusProvider, interactiveCalculateExperimentSize,
					experiment,
					optDBPbjectsOfConditions));
		} catch (Exception e) {
			MongoDB.saveSystemErrorMessage("Error during experiment loading.", e);
			ErrorMsg.addErrorMessage(e);
		}
		return experiment;
	}
	
	protected void visitExperiment(
			final ExperimentHeaderInterface header,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider,
			final RunnableProcessingDBid visitSubstance,
			final RunnableProcessingDBid visitCondition,
			final RunnableProcessingBinaryMeasurement visitBinaryMeasurement,
			final ThreadSafeOptions invalid) {
		
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
						DBCollection collCond = db.getCollection("conditions");
						if (ensureIndex)
							collCond.ensureIndex("_id");
						
						if (subList != null)
							for (Object co : subList) {
								DBObject substance = (DBObject) co;
								visitSubstance(
										header,
										db, substance,
										collCond,
										optStatusProvider, 100d / subList.size(),
										visitCondition,
										visitBinaryMeasurement,
										invalid);
							}
						boolean printed = false;
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
									if (visitSubstance != null) {
										if (substance != null) {
											synchronized (visitSubstance) {
												visitSubstance.processDBid(substance.get("_id") + "");
											}
											visitSubstance(header,
													db, substance,
													collCond,
													optStatusProvider, 100d / l.size(),
													visitCondition, visitBinaryMeasurement,
													invalid);
										} else
											if (!printed) {
												System.out.println("WARNING: Missing substance(s) in experiment " + header.getExperimentName());
												printed = true;
												invalid.setBval(0, true);
											}
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
	
	/**
	 * @return Storage time.
	 */
	public Long saveExperimentHeader(final ExperimentHeaderInterface header) throws Exception {
		final ObjectRef storageTime = new ObjectRef();
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (header.getDatabaseId() == null)
					System.out.println("Cant update experiment, as header DB id is null!");
				ObjectId id = new ObjectId(header.getDatabaseId());
				DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), id);
				DBObject expref = dbr.fetch();
				if (expref != null) {
					HashMap<String, Object> attributes = new HashMap<String, Object>();
					header.fillAttributeMap(attributes, 0);
					long st = System.currentTimeMillis();
					attributes.put("lastHeaderUpdate", st);
					for (String key : attributes.keySet()) {
						if (attributes.get(key) != null)
							expref.put(key, attributes.get(key));
					}
					db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString()).save(expref);
					storageTime.setObject(st);
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
		return (Long) storageTime.getObject();
	}
	
	/**
	 * @return Storage time.
	 */
	public Long updateAndGetExperimentHeaderInfoFromDB(final ExperimentHeaderInterface header) throws Exception {
		final ObjectRef storageTime = new ObjectRef();
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (header.getDatabaseId() == null)
					System.out.println("Cant update experiment, as header DB id is null!");
				ObjectId id = new ObjectId(header.getDatabaseId());
				DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), id);
				DBObject expref = dbr.fetch();
				if (expref != null) {
					header.setAttributesFromMap(expref.toMap());
					header.setStorageTime(new Date(((ObjectId) expref.get("_id")).getTime()));
					storageTime.setObject(expref.get("lastHeaderUpdate"));
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
		return (Long) storageTime.getObject();
	}
	
	public void updateExperimentSize(DB db, ExperimentInterface experiment, BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider) {
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
						if (optStatusProvider != null && n % 100 == 0) {
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
				saveExperimentHeader(experiment.getHeader());
				if (optStatusProvider != null) {
					optStatusProvider.setCurrentStatusValue(100);
					optStatusProvider.setCurrentStatusText1("Finished");
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	Batch batchInst = null;
	
	public synchronized Batch batch() {
		if (batchInst == null)
			batchInst = new Batch(this);
		return batchInst;
	}
	
	private void visitSubstance(
			ExperimentHeaderInterface header,
			DB db, DBObject substance,
			DBCollection collCond,
			BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider, double smallProgressStep,
			RunnableProcessingDBid visitCondition,
			RunnableProcessingBinaryMeasurement visitBinary,
			ThreadSafeOptions invalid) {
		if (invalid.getBval(0, false))
			return;
		@SuppressWarnings("unchecked")
		Substance3D s3d = new Substance3D(substance.toMap());
		// if (optStatusProvider!=null)
		// optStatusProvider.setCurrentStatusText1("Process "+s3d.getName());
		BasicDBList condList = (BasicDBList) substance.get("conditions");
		if (condList != null)
			for (Object co : condList) {
				DBObject cond = (DBObject) co;
				visitCondition(s3d, cond, visitBinary);
				
			}
		BasicDBList l = (BasicDBList) substance.get("condition_ids");
		if (l != null) {
			double max = l.size();
			boolean printed = false;
			boolean singleFetch = false;
			if (collCond != null)
				if (singleFetch)
					printed = processConditionBySingleFetch(header, db, collCond,
							optStatusProvider, smallProgressStep, visitCondition,
							visitBinary, invalid, s3d, l, max, printed);
				else
					printed = processConditionByMultiFetch(header, db, collCond,
							optStatusProvider, smallProgressStep, visitCondition,
							visitBinary, invalid, s3d, l, max, printed);
		}
	}
	
	private boolean processConditionBySingleFetch(
			ExperimentHeaderInterface header,
			DB db,
			DBCollection collCond,
			BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider,
			double smallProgressStep, RunnableProcessingDBid visitCondition,
			RunnableProcessingBinaryMeasurement visitBinary,
			ThreadSafeOptions invalid, Substance3D s3d, BasicDBList l,
			double max, boolean printed) {
		for (Object o : l) {
			if (o == null)
				continue;
			boolean useRef = false;
			DBObject cond;
			if (useRef) {
				DBRef condr = new DBRef(db, "conditions", new ObjectId(o.toString()));
				cond = condr.fetch();
			} else {
				cond = collCond.findOne(
						new ObjectId(o.toString()),
						new BasicDBObject()
								.append("_id", new Integer(1))
								.append("samples." + MongoCollection.IMAGES.toString(), new Integer(1))
								.append("samples." + MongoCollection.VOLUMES.toString(), new Integer(1))
								.append("samples." + MongoCollection.NETWORKS.toString(), new Integer(1))
						);
			}
			// find objects in "condition" collection, but only fields images, volumes, networks
			if (cond != null) {
				synchronized (visitCondition) {
					visitCondition.processDBid(cond.get("_id") + "");
				}
				visitCondition(s3d, cond, visitBinary);
			} else {
				if (!printed) {
					String msg = "WARNING: Condition could not be retrieved for experiment " + header.getExperimentName();
					System.out.println(msg);
					saveSystemMessage(msg);
					printed = true;
					invalid.setBval(0, true);
				}
			}
			if (optStatusProvider != null)
				optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep * 1 / max);
		}
		return printed;
	}
	
	private boolean processConditionByMultiFetch(
			ExperimentHeaderInterface header,
			DB db,
			DBCollection collCond,
			BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider,
			double smallProgressStep, RunnableProcessingDBid visitCondition,
			RunnableProcessingBinaryMeasurement visitBinary,
			ThreadSafeOptions invalid, Substance3D s3d, BasicDBList l,
			double max, boolean printed) {
		BasicDBList ll = new BasicDBList();
		for (Object o : l)
			ll.add(new ObjectId(o + ""));
		DBCursor condL = collCond.find(
				new BasicDBObject("_id", new BasicDBObject("$in", ll))
				, new BasicDBObject()
						.append("_id", new Integer(1))
						.append("samples." + MongoCollection.IMAGES.toString(), new Integer(1))
						.append("samples." + MongoCollection.VOLUMES.toString(), new Integer(1))
						.append("samples." + MongoCollection.NETWORKS.toString(), new Integer(1))
				).hint(new BasicDBObject("_id", 1)).batchSize(l.size() % 1000);
		for (DBObject cond : condL) {
			// find objects in "condition" collection, but only fields images, volumes, networks
			synchronized (visitCondition) {
				visitCondition.processDBid(cond.get("_id") + "");
			}
			visitCondition(s3d, cond, visitBinary);
			if (optStatusProvider != null)
				optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep * 1 / max);
		}
		if (l.size() != condL.size()) {
			if (!printed) {
				String msg = "WARNING: " + (l.size() - condL.size()) + " condition could not be retrieved for experiment " + header.getExperimentName();
				System.out.println(msg);
				saveSystemMessage(msg);
				printed = true;
				invalid.setBval(0, true);
			}
		}
		return printed;
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
						visitBinary.processBinaryMeasurement(image);
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
						visitBinary.processBinaryMeasurement(volume);
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
						visitBinary.processBinaryMeasurement(network);
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
	
	public Collection<String> getNews(final int limit) throws Exception {
		final LinkedList<String> res = new LinkedList<String>();
		
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				SimpleDateFormat sdf = new SimpleDateFormat();
				for (DBObject newsItem : db.getCollection("news").find().sort(new BasicDBObject("date", -1)).limit(limit)) {
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
		processDB(new CleanupHelper(this, status, res));
		return res.toString();
	}
	
	public String compact(
			final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		final StringBuilder res = new StringBuilder();
		processDB(new CleanupCompactHelper(status, res));
		return res.toString();
	}
	
	public static void saveSystemMessage(String msg) {
		try {
			MongoDB.getDefaultCloud().addNewsItem(SystemAnalysis.getCurrentTime() + ">" + msg,
					"system-msg/" + SystemAnalysis.getUserName() + "@" + SystemAnalysisExt.getHostNameNiceNoError());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public static void saveSystemErrorMessage(String error, Exception e) {
		if (e == null) {
			saveSystemMessage(error);
			return;
		}
		System.err.println(SystemAnalysis.getCurrentTime() + ">" + error);
		e.printStackTrace();
		saveSystemMessage("System eror message: " + error + " - Exception: " + e.getMessage() +
				".<br>Stack-trace:<br>" +
				e.getStackTrace() != null ?
				StringManipulationTools.getStringList(e.getStackTrace(), "<br>") : "(no stacktrace)");
	}
	
	public boolean isDbHostReachable() {
		return IAPservice.isReachable(databaseHost);
	}
	
	public Collection<GridFSDBFile> getSavedScreenshots() throws Exception {
		final HashMap<String, Long> fn2newestStorageTime = new HashMap<String, Long>();
		final HashMap<String, GridFSDBFile> fn2newestFile = new HashMap<String, GridFSDBFile>();
		
		RunnableOnDB runnableOnDB = new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				GridFS gridfs_webcam_files = new GridFS(db, "fs_screenshots");
				List<GridFSDBFile> files = gridfs_webcam_files.find(new BasicDBObject());
				// inputFile.setMetaData(new BasicDBObject("time", screenshot.getTime()));
				// inputFile.setMetaData(new BasicDBObject("host", SystemAnalysisExt.getHostNameNoError()));
				long t = 0;
				for (GridFSDBFile o : files) {
					Long time = (Long) o.get("time");
					if (time == null)
						time = t++;
					String fn = "" + o.get("_id");
					fn2newestFile.put(fn, o);
					fn2newestStorageTime.put(fn, time);
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		};
		processDB(runnableOnDB);
		
		return fn2newestFile.values();
	}
	
	public void updateScreenshotObserver(String host, long currentTimeMillis) throws Exception {
		RunnableOnDB runnableOnDB = new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				// to do
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		};
		processDB(runnableOnDB);
	}
	
	public BufferedImage getSavedScreenshot(String filename, GridFS gridfs_webcam_files,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		try {
			GridFSDBFile f = gridfs_webcam_files.findOne(filename);
			status.setCurrentStatusText1(""
					+ f.getUploadDate());
			return ImageIO.read(f.getInputStream());
		} catch (Exception e) {
			status.setCurrentStatusText1(" // ERROR: " + e.getMessage());
			return ImageConverter.convertImage2BufferedImage(IAPimages.getImage(IAPimages.getComputerOffline()));
		}
	}
	
	public GridFS getScreenshotFS() throws Exception {
		final ThreadSafeOptions dbs = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			@Override
			public void run() {
				// empty
			}
			
			@Override
			public void setDB(DB db) {
				dbs.setParam(0, db);
			}
		});
		return new GridFS((DB) dbs.getParam(0, null), "fs_screenshots");
	}
	
	void removeFilesFromGridFS(final BackgroundTaskStatusProviderSupportingExternalCall status, String mgfs, final GridFS gridfs,
			ArrayList<GridFSDBFile> toBeRemoved, final ThreadSafeOptions free, DB db) {
		DBCollection colFE = db.getCollection(mgfs + ".files");
		DBCollection colFC = db.getCollection(mgfs + ".chunks");
		// if (mgfs.endsWith("fs_images"))
		// colFC.dropIndexes();
		boolean rmList = true;
		ArrayList<ObjectId> rList = new ArrayList<ObjectId>();
		int deleted = 0;
		for (final GridFSDBFile f : toBeRemoved) {
			free.addLong(f.getLength());
			if (!rmList)
				gridfs.remove(f.getFilename());
			else
				rList.add((ObjectId) f.getId());
			if (rmList && rList.size() > 200) {
				colFE.remove(
						new BasicDBObject("_id", new BasicDBObject("$in", rList)), WriteConcern.NONE);
				colFC.remove(
						new BasicDBObject("files_id", new BasicDBObject("$in", rList)), WriteConcern.NONE);
				rList.clear();
			}
			deleted++;
			status.setCurrentStatusText1("File "
					+ deleted + "/" + toBeRemoved.size() + " (" + (int) (100d * deleted / toBeRemoved.size()) + "%)"
					+ ", removed: " + free.getLong() / 1024 / 1024 + " MB");
			status.setCurrentStatusValueFine(deleted * 100d / toBeRemoved.size());
		}
		if (rmList && rList.size() > 0) {
			colFE.remove(
					new BasicDBObject("_id", new BasicDBObject("$in", rList)), WriteConcern.NONE);
			colFC.remove(
					new BasicDBObject("files_id", new BasicDBObject("$in", rList)), WriteConcern.NONE);
			rList.clear();
		}
	}
	
	public ArrayList<String> getWebCamStorageFileSystems() throws Exception {
		final ArrayList<String> res = new ArrayList<String>();
		RunnableOnDB runnableOnDB = new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				for (String n : db.getCollectionNames())
					if (n.startsWith("fs_webcam_") && n.endsWith(".files"))
						res.add(n);
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		};
		processDB(runnableOnDB);
		return res;
	}
	
	public long getWebCamStorageCount(final String fs, final Date startdate, final Date importdate) throws Exception {
		final ThreadSafeOptions res = new ThreadSafeOptions();
		res.setLong(-1);
		RunnableOnDB runnableOnDB = new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				res.setLong(db.getCollection(fs).count(
						new BasicDBObject("uploadDate",
								new BasicDBObject("$gt", startdate).append("$lt", importdate))));
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		};
		processDB(runnableOnDB);
		return res.getLong();
	}
	
	public ArrayList<String> getWebCamStorageFileNames(final String fs, final Date startdate, final Date importdate) throws Exception {
		final ArrayList<String> res = new ArrayList<String>();
		RunnableOnDB runnableOnDB = new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				for (DBObject o : db.getCollection(fs)
						.find(new BasicDBObject("uploadDate",
								new BasicDBObject("$gt", startdate).append("$lt", importdate)))) {
					res.add((String) o.get("filename"));
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		};
		processDB(runnableOnDB);
		return res;
	}
	
	public GridFS getGridFS(final String fileSystemName) throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		RunnableOnDB runnableOnDB = new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				tso.setParam(0, new GridFS(db, fileSystemName));
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		};
		processDB(runnableOnDB);
		return (GridFS) tso.getParam(0, null);
	}
	
	public ArrayList<VirtualFileSystem> getVirtualFileSystemForFileStorage() {
		return vfs_file_storage;
	}
	
	HashMap<String, VirtualFileSystem> lastHit = new HashMap<String, VirtualFileSystem>();
	
	public InputStream getVFSinputStream(String bucket, String detail) {
		if (vfs_file_storage != null) {
			synchronized (vfs_file_storage) {
				try {
					if (lastHit != null) {
						VirtualFileSystem i = lastHit.get(bucket);
						if (i != null) {
							IOurl h = i.getIOurlFor(bucket + "/" + detail);
							return h.getInputStream();
						}
					}
				} catch (Exception e) {
					// ignore
					e.printStackTrace();
				}
				for (VirtualFileSystem vfs : vfs_file_storage) {
					if (vfs == lastHit.get(bucket))
						continue;
					IOurl url = vfs.getIOurlFor(bucket + "/" + detail);
					InputStream is;
					try {
						is = url.getInputStream();
						if (is != null) {
							lastHit.put(bucket, vfs);
							return is;
						}
					} catch (Exception e) {
						// empty
						System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Not found: " + url);
					}
				}
			}
		}
		return null;
	}
	
	public MongoDBhandler getMongoHandler() {
		return mh;
	}
	
	public DataBaseFileStorage fileStorage() {
		return new DataBaseFileStorage(this);
	}
	
	public Long getExperimentHeaderStorageTime(final ExperimentHeaderInterface header) throws Exception {
		final ObjectRef res = new ObjectRef();
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (header.getDatabaseId() == null)
					System.out.println("Cant update experiment, as header DB id is null!");
				ObjectId id = new ObjectId(header.getDatabaseId());
				DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), id);
				DBObject expref = dbr.fetch();
				if (expref != null) {
					Long oo = (Long) expref.get("lastHeaderUpdate");
					res.setObject(oo);
				} else {
					System.out.println("CHECK UPDATE TIME: EXP INFO NOT FOUND");
					tso.setBval(0, true);
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		if (tso.getBval(0, false))
			throw new Exception("Experiment with ID " + header.getDatabaseId() + " not found!");
		return (Long) res.getObject();
	}
}
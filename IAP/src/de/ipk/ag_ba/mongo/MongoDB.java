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
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.bson.types.ObjectId;
import org.graffiti.editor.HashType;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MongoCollection;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk.ag_ba.postgresql.LTftpHandler;
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
	
	public static boolean getEnsureIndex() {
		return false;// SystemOptions.getInstance().getBoolean("GRID-STORAGE", "ensure Index", true);
	}
	
	private MongoClient m;
	private static HashSet<String> dbsAnalyzedForCollectionSettings = new HashSet<String>();
	
	private final boolean enabled;
	final ArrayList<VirtualFileSystem> vfs_file_storage = new ArrayList<VirtualFileSystem>();
	
	private final String displayName;
	private final String databaseName;
	private final String databaseHost;
	private final String databaseLogin;
	private final String databasePass;
	private final HashType hashType;
	
	private final MongoDBhandler mh;
	
	WeakHashMap<MongoClient, HashSet<String>> authenticatedDBs = new WeakHashMap<MongoClient, HashSet<String>>();
	private final int databasePort;
	
	public static ArrayList<MongoDB> getMongos() {
		return initMongoList();
	}
	
	@Override
	public String toString() {
		return displayName + " (" + databaseHost + ", db " + databaseName + ")";
	}
	
	private static ArrayList<MongoDB> initMongoList() {
		ArrayList<MongoDB> res = new ArrayList<MongoDB>();
		int n = SystemOptions.getInstance().getInteger("GRID-STORAGE", "n", 1);
		for (int i = 0; i < n; i++) {
			MongoDB c = getCloud(i);
			if (c != null)
				if (c.enabled)
					res.add(c);
		}
		return res;
	}
	
	public static MongoDB getDefaultCloud() {
		for (MongoDB db : getMongos())
			if (db.enabled)
				return db;
		return null;
	}
	
	private static HashMap<String, MongoDB> id2m = new HashMap<>();
	
	public static MongoDB getCloud(int idx) {
		String sec = "GRID-STORAGE-" + (idx + 1);
		
		boolean enabled = SystemOptions.getInstance().getBoolean(sec,
				"enabled", false);
		String displayName = SystemOptions.getInstance().getString(sec,
				"title", "Storage " + (idx + 1));
		String databaseName = SystemOptions.getInstance().getString(sec,
				"database_name", "cloud" + (idx + 1));
		String hostName = SystemOptions.getInstance().getString(sec,
				"host", "ba-13.ipk-gatersleben.de");
		Integer port = SystemOptions.getInstance().getInteger(sec, "port", 27017);
		String login = SystemOptions.getInstance().getString(sec,
				"login", null);
		String password = SystemOptions.getInstance().getString(sec,
				"password", null);
		// if (idx == 0) {
		// if (defaultCloudInstance == null) {
		// defaultCloudInstance = new MongoDB(displayName, databaseName, hostName,
		// login, password, HashType.MD5);
		// }
		// defaultCloudInstance.enabled = enabled;
		// return defaultCloudInstance;
		// } else {
		
		String id = enabled + ";" + displayName + ";" + databaseName + ";" + hostName + ";" + port
				+ ";" + login + ";" + password;
		synchronized (id2m) {
			MongoDB mc = id2m.get(id);
			if (mc != null)
				return mc;
			else
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Cache miss (" + id2m.size() + " entries known), create new grid instance: "
						+ displayName);
		}
		MongoDB mm = new MongoDB(displayName, databaseName, hostName, port,
				login, password, HashType.MD5, enabled);
		synchronized (id2m) {
			id2m.put(id, mm);
		}
		return mm;
		// }
	}
	
	public MongoDBhandler getHandler() {
		return mh;
	}
	
	// collections:
	// preview_files
	// volumes
	// images
	// annotations
	// experiments
	// substances
	// conditions
	
	private MongoDB(String displayName, String databaseName, String hostName, int port, String login, String password, HashType hashType) {
		this(displayName, databaseName, hostName, port, login, password, hashType, true);
	}
	
	private MongoDB(String displayName, String databaseName, String hostName, int port, String login, String password, HashType hashType, boolean enabled) {
		if (databaseName == null || databaseName.contains("/")) // databaseName.contains("_") ||
			throw new UnsupportedOperationException("Database name may not be NULL and may not contain special characters!");
		this.displayName = displayName;
		this.databaseName = databaseName;
		this.databaseHost = hostName;
		this.databasePort = port;
		this.databaseLogin = login;
		this.databasePass = password;
		this.hashType = hashType;
		this.enabled = enabled;
		
		if (enabled) {
			mh = new MongoDBhandler(databaseHost, port, this);
			
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
			
			// if (getEnsureIndex())
			// try {
			// BackgroundThreadDispatcher.addTask(() -> {
			// try {
			// ensureBasicIndecies();
			// } catch (Exception e) {
			// throw new RuntimeException(e);
			// }
			// }, "Ensure Indecies in Grid DB");
			// } catch (InterruptedException e) {
			// ErrorMsg.addErrorMessage(e);
			// }
		} else
			mh = null;
	}
	
	private void ensureBasicIndecies() throws Exception {
		if (true)
			return;
		
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBCollection collCond = db.getCollection("conditions");
				collCond.createIndex("_id");
				
				DBCollection collSubst = db.getCollection("substances");
				collSubst.createIndex("_id");
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	public ExperimentInterface saveExperiment(final ExperimentInterface experiment, final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		return saveExperiment(experiment, status, false, false);
	}
	
	public ExperimentInterface saveExperiment(final ExperimentInterface experiment,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final boolean keepDataLinksToDataSource_safe_space,
			final boolean filesAreAlreadySavedSkipStorage)
			throws Exception {
		final ThreadSafeOptions err = new ThreadSafeOptions();
		RunnableOnDB r = new ExperimentSaver(
				this,
				mh,
				getHashType(),
				experiment, keepDataLinksToDataSource_safe_space, status, err,
				getExperimentList(null),
				filesAreAlreadySavedSkipStorage);
		processDB(r);
		if (err.getParam(0, null) != null)
			throw (Exception) err.getParam(0, null);
		if (status != null && status.wantsToStop())
			return null;
		return experiment;
	}
	
	// private static ThreadSafeOptions dbLastAccess = new ThreadSafeOptions();
	// private static int dbMaxCon = SystemOptions.getInstance().getInteger("GRID-STORAGE", "connections per host", 5);
	// private static Semaphore runningOps = new Semaphore(dbMaxCon, true);
	
	private void processDB(String database, String optHosts, int optPort, String optLogin, String optPass,
			RunnableOnDB runnableOnDB) throws Exception {
		int repeats = SystemOptions.getInstance().getInteger("GRID-STORAGE", "retry-count in case of error", 0) + 1;
		processDB(database, optHosts, optPort, optLogin, optPass, runnableOnDB, repeats);
	}
	
	private void processDB(String database, String optHosts, int optPort, String optLogin, String optPass,
			RunnableOnDB runnableOnDB, int repeats) throws Exception {
		Exception e = null;
		String key = optHosts + ";" + optPort + ";" + database;
		
		boolean ok = false;
		int nrep = 0;
		do {
			try {
				DB db;
				// synchronized (this.getClass()) {
				createMongoConnection(database, optHosts, optPort, optLogin, optPass, key);
				db = m.getDB(database);
				// }
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
							System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: Authentication error: " + err.getMessage());
						}
					}
				
				boolean initStatusCollection = false;
				if (initStatusCollection && !dbsAnalyzedForCollectionSettings.contains(database)) {
					checkforCollectionsInitialization(db, "status_maize", 100, 50000);
					checkforCollectionsInitialization(db, "status_barley", 100, 50000);
					checkforCollectionsInitialization(db, "status_phyto", 100, 50000);
					dbsAnalyzedForCollectionSettings.add(database);
				}
				
				boolean ensureIndex = MongoDB.getEnsureIndex();
				if (ensureIndex)
					for (String fs : MongoGridFS.getFileCollections()) {
						DBCollection collectionChunks = db.getCollection(fs.toString() + ".chunks");
						collectionChunks.createIndex("files_id");
					}
				
				runnableOnDB.setDB(db);
				runnableOnDB.run();
				ok = true;
				e = null;
			} catch (Exception err) {
				err.printStackTrace();
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: EXEC " + (nrep - repeats + 1) + " ERROR: " + err.getLocalizedMessage() + " T="
						+ IAPservice.getCurrentTimeAsNiceString());
				e = err;
				if (repeats - 1 > 0)
					Thread.sleep(60 * 1000);
			}
			repeats--;
		} while (!ok && repeats > 0);
		if (e != null)
			throw e;
	}
	
	private void createMongoConnection(String database, String optHosts, int optPort, String optLogin, String optPass, String key) throws UnknownHostException {
		if (m == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Establish DB connection (" + key + ")");
			Builder mob = new MongoClientOptions.Builder();
			mob.connectionsPerHost(SystemOptions.getInstance().getInteger("GRID-STORAGE", "connections per host", 5));
			mob.connectTimeout(SystemOptions.getInstance().getInteger("GRID-STORAGE", "connect timeout", 5 * 60 * 1000));
			mob.threadsAllowedToBlockForConnectionMultiplier(SystemOptions.getInstance().getInteger("GRID-STORAGE",
					"threads allowed to wait for connection",
					10000));
			mob.socketTimeout(SystemOptions.getInstance().getInteger("GRID-STORAGE", "socket timeout", 5 * 60 * 1000));
			mob.maxWaitTime(SystemOptions.getInstance().getInteger("GRID-STORAGE", "max wait time", 5 * 60 * 1000));
			// depcricated//mob.autoConnectRetry(SystemOptions.getInstance().getBoolean("GRID-STORAGE", "auto connect retry", true));
			mob.socketKeepAlive(SystemOptions.getInstance().getBoolean("GRID-STORAGE", "socket keep alive", true));
			mob.writeConcern(WriteConcern.ACKNOWLEDGED);
			
			MongoClientOptions mco = mob.build();
			
			StopWatch s = new StopWatch("INFO: new MongoClient(...)", false);
			if (optHosts == null || optHosts.length() == 0) {
				MongoClient mc = new MongoClient("localhost:" + optPort, mco);
				m = mc;
			} else {
				List<ServerAddress> seeds = new ArrayList<ServerAddress>();
				for (String h : optHosts.split(","))
					seeds.add(new ServerAddress(h, optPort));
				MongoClient mc = new MongoClient(seeds, mco);
				m = mc;
			}
			m.getMongoOptions().connectionsPerHost = SystemOptions.getInstance().getInteger("GRID-STORAGE", "connections per host", 5);
			m.getMongoOptions().connectTimeout = SystemOptions.getInstance().getInteger("GRID-STORAGE", "connect timeout", 5 * 60 * 1000);
			m.getMongoOptions().maxWaitTime = SystemOptions.getInstance().getInteger("GRID-STORAGE", "max wait time", 5 * 60 * 1000);
			m.getMongoOptions().autoConnectRetry = SystemOptions.getInstance().getBoolean("GRID-STORAGE", "auto connect retry", true);
			m.getMongoOptions().threadsAllowedToBlockForConnectionMultiplier = SystemOptions.getInstance().getInteger("GRID-STORAGE",
					"threads allowed to wait for connection",
					1000);
			m.getMongoOptions().socketTimeout = SystemOptions.getInstance().getInteger("GRID-STORAGE", "socket timeout", 5 * 60 * 1000);
			m.getMongoOptions().socketKeepAlive = SystemOptions.getInstance().getBoolean("GRID-STORAGE", "socket keep alive", true);
			
			s.printTime(1000);
			if (authenticatedDBs.get(m) == null || !authenticatedDBs.get(m).contains("admin")) {
				DB dbAdmin = m.getDB("admin");
				try {
					s = new StopWatch("INFO: dbAdmin.authenticate()");
					dbAdmin.authenticate(optLogin, optPass.toCharArray());
					s.printTime(1000);
					if (authenticatedDBs.get(m) == null)
						authenticatedDBs.put(m, new HashSet<String>());
					authenticatedDBs.get(m).add(database);
				} catch (Exception err) {
					// System.err.println("ERROR: " + err.getMessage());
				}
			}
		}
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
		processDB(getDatabaseName(), databaseHost, databasePort, databaseLogin, databasePass, runnableOnDB);
	}
	
	public void processDB(RunnableOnDB runnableOnDB, int repeats) throws Exception {
		processDB(getDatabaseName(), databaseHost, databasePort, databaseLogin, databasePass, runnableOnDB, repeats);
	}
	
	public void processDB(long timeout, RunnableOnDB runnableOnDB) throws Exception {
		processDB(getDatabaseName(), databaseHost, databasePort, databaseLogin, databasePass, runnableOnDB);
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
				DBCollection collSubst = db.getCollection("substances");
				
				DBCollection collExps = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString());
				if (db.collectionExists(MongoExperimentCollections.EXPERIMENTS.toString())) {
					DBObject expRef = collExps.findOne(obj);
					if (expRef != null) {
						try {
							addNewsItem("Experiment " + experimentID + " (" + expRef.get("experimentname") + ") is removed by user " + SystemAnalysis.getUserName()
									+ " (" + SystemAnalysis.getCurrentTime()
									+ "), working at PC " + SystemAnalysis.getLocalHost().getCanonicalHostName(), SystemAnalysis.getUserName());
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
						BasicDBList sl = (BasicDBList) expRef.get("substance_ids");
						if (sl != null) {
							ArrayList<ObjectId> rList = new ArrayList<ObjectId>();
							for (Object so : sl) {
								DBRef subr = so != null ? new DBRef(db, "substances", new ObjectId(so.toString())) : null;
								if (subr != null) {
									DBObject substance = subr.fetch();
									if (substance != null) {
										BasicDBList cl = (BasicDBList) substance.get("condition_ids");
										if (cl == null) {
											System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Could not get condition list for substance "
													+ substance.get("name") + " (id " + so + ")");
										} else {
											for (Object oo : cl) {
												if (oo != null)
													collCond.remove(new BasicDBObject("_id", oo.toString()), WriteConcern.ACKNOWLEDGED);
												// rList.add(new ObjectId(oo.toString()));
												// if (rList.size() > 0) {
												// collCond.remove(new BasicDBObject("_id", new BasicDBObject("$in", rList)), WriteConcern.SAFE);
												// rList.clear();
												// }
											}
										}
									}
								}
							}
							if (rList.size() > 0)
								collCond.remove(new BasicDBObject("_id", new BasicDBObject("$in", rList)));
							rList.clear();
							if (sl != null) {
								for (Object oo : sl)
									if (oo != null) {
										collSubst.remove(new BasicDBObject("_id", oo.toString()), WriteConcern.ACKNOWLEDGED);
									}
								// rList.add(new ObjectId(oo.toString()));
								// if (rList.size() > 0)
								// collSubst.remove(new BasicDBObject("_id", new BasicDBObject("$in", rList)));
							}
						}
						WriteResult wr = collExps.remove(expRef, WriteConcern.FSYNC_SAFE);
						if (!wr.getLastError(WriteConcern.SAFE).ok())
							throw new RuntimeException("Remove experiment error: " + wr.getError());
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
				result = lastVFS.saveStream(fs.getBucketName() + "/" + hash, is, true, expectedFileSize);
				stored_in_VFS = result == expectedFileSize;
				if (stored_in_VFS)
					return result;
			}
		}
		
		if (!stored_in_VFS) {
			boolean compress = false;
			GridFSInputFile inputFile = fs.createFile(compress ?
					ResourceIOManager.getCompressedInputStream((MyByteArrayInputStream) is) : is, hash);
			inputFile.save();
			result = 1;// inputFile.getLength();
			is.close();
			VirtualFileSystemVFS2.writeCounter.addLong(((MyByteArrayInputStream) is).getCount());
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
					HashMap<String, String> mapableNames = new HashMap<String, String>();
					String[] mn = SystemOptions.getInstance().getStringAll("GRID-STORAGE",
							"User Name Mapping", new String[] {
							"klukas/Christian Klukas"
							});
					if (mn != null)
						for (String s : mn) {
							if (s == null || s.trim().isEmpty())
								continue;
							if (!s.contains("/"))
								System.out.println(SystemAnalysis.getCurrentTime()
										+ ">WARNING: Invalid user name mapping, should be 'username/nicename'! (" + s + ")");
							else
								mapableNames.put(s.split("/")[0], s.split("/", 2)[1]);
						}
					if (optStatus != null)
						optStatus.setCurrentStatusText1("Locate Experiments");// "Count");
						
					// long max = col.count();
					// if (optStatus != null)
					// optStatus.setCurrentStatusText1("Found " + max);
					long idx = 0;
					for (DBObject header : col.find()) {
						if (opt_last_ping != null)
							opt_last_ping.setLong(System.currentTimeMillis());
						ExperimentHeader h = new ExperimentHeader(header.toMap());
						h.setStorageTime(new Date(((ObjectId) header.get("_id")).getTime()));
						if (mapableNames.containsKey(h.getImportusername())) {
							String newName = mapableNames.get(h.getImportusername());
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">INFO: FIXING ACCOUNT NAME (changing " + h.getImportusername() + " to " + newName + ")");
							h.setImportUserName(newName);
							header.put("importusername", h.getImportusername());
							col.save(header, WriteConcern.SAFE);
						}
						if (user == null ||
								h.getImportusername() != null && h.getImportusername().equals(user) ||
								LTdataExchange.getAdministrators().contains(user) ||
								h.getCoordinator().toUpperCase().contains(user.toUpperCase()))
							res.add(h);
						idx++;
						if (optStatus != null) {
							optStatus.setCurrentStatusText1("Process Experiment " + idx);// + "/" + max + "");
							optStatus.setCurrentStatusValueFine(-1);// 100d / max * idx);
						}
					}
					if (optStatus != null) {
						optStatus.setCurrentStatusValueFine(100d);
						optStatus.setCurrentStatusText1("");
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
			processDB(new ExperimentLoader(this, mh, getEnsureIndex(),
					optDBPbjectsOfSubstances, header, optStatusProvider, interactiveCalculateExperimentSize,
					experiment,
					optDBPbjectsOfConditions));
		} catch (Exception e) {
			MongoDB.saveSystemErrorMessage("Error during experiment loading.", e);
			ErrorMsg.addErrorMessage(e);
		}
		return experiment;
	}
	
	public void visitExperiment(
			final ExperimentHeaderInterface header,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider,
			final RunnableProcessingSubstance visitSubstanceObject,
			final boolean processConditions,
			final RunnableProcessingDBid visitSubstance,
			final RunnableProcessingDBid visitCondition,
			final RunnableProcessingBinaryMeasurement visitBinaryMeasurement,
			final ThreadSafeOptions invalid) throws Exception {
		
		if (optStatusProvider != null)
			optStatusProvider.setCurrentStatusValue(0);
		
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), new ObjectId(header.getDatabaseId()));
				DBObject expref = dbr.fetch();
				DBCollection collCond = db.getCollection("conditions");
				if (expref != null) {
					{
						BasicDBList subList = (BasicDBList) expref.get("substances");
						
						if (subList != null) {
							LinkedList<DBObject> list = new LinkedList<DBObject>();
							for (Object co : subList) {
								list.add((DBObject) co);
							}
							subList.clone();
							for (DBObject substance : list) {
								visitSubstance(
										header,
										db, substance,
										collCond,
										optStatusProvider, 100d / subList.size(),
										visitSubstanceObject,
										processConditions,
										visitCondition,
										visitBinaryMeasurement,
										invalid);
							}
						}
					}
					boolean printed = false;
					if (getEnsureIndex())
						db.getCollection("substances").createIndex("_id");
					
					BasicDBList ll = new BasicDBList();
					BasicDBList l = (BasicDBList) expref.get("substance_ids");
					for (Object o : l) {
						if (o != null)
							ll.add(new ObjectId(o + ""));
					}
					DBCollection collSubst = db.getCollection("substances");
					DBCursor subList = collSubst.find(new BasicDBObject("_id", new BasicDBObject("$in", ll)))
							.hint(new BasicDBObject("_id", 1));// .batchSize(Math.max(200, ll.size()));
					for (DBObject substance : subList) {
						if (visitSubstance != null || visitSubstanceObject != null) {
							if (substance != null) {
								if (visitSubstance != null) {
									synchronized (visitSubstance) {
										visitSubstance.processDBid(substance.get("_id") + "");
									}
								}
								visitSubstance(header,
										db, substance,
										collCond,
										optStatusProvider, 100d / ll.size(),
										visitSubstanceObject, processConditions,
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
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		
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
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Cant update experiment, as header DB id is null!");
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
				int n = 0;
				double max = 0;
				
				LinkedList<NumericMeasurementInterface> fl = Substance3D.getAllFiles(experiment, MeasurementNodeType.IMAGE);
				n = fl.size();
				max = n;
				LinkedList<LinkedList<String>> fl_ids = new LinkedList<LinkedList<String>>();
				int added = 0;
				{
					LinkedList<String> currentList = null;
					while (!fl.isEmpty()) {
						if (currentList == null) {
							currentList = new LinkedList<String>();
							fl_ids.add(currentList);
						}
						currentList.add(((BinaryMeasurement) fl.poll()).getURL().getDetail());
						added++;
						if (added >= 25000) {
							added = 0;
							currentList = null;
						}
					}
				}
				n = 0;
				for (LinkedList<String> currentList : fl_ids) {
					for (String mgfs : MongoGridFS.getFileCollections()) {
						DBCollection colFE = db.getCollection(mgfs + ".files");
						DBObject groupFields = new BasicDBObject("_id", null);
						groupFields.put("result_size", new BasicDBObject("$sum", "$length"));
						
						AggregationOutput out = colFE.aggregate(
								new BasicDBObject("$match", new BasicDBObject("filename", new BasicDBObject("$in", currentList))),
								new BasicDBObject("$group", groupFields)
								);
						
						Iterator<DBObject> it = out.results().iterator();
						if (it.hasNext()) {
							newSize.addLong((Long) it.next().get("result_size"));
						}
					}
					n += currentList.size();
					optStatusProvider.setCurrentStatusValueFine(100d * n / max);
					optStatusProvider.setCurrentStatusText2("(" + n + "/" + (int) max + ", " + newSize.getLong() / 1024 / 1024 + " MB)");
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
			RunnableProcessingSubstance visitSubstance,
			boolean processConditions,
			RunnableProcessingDBid visitCondition,
			RunnableProcessingBinaryMeasurement visitBinary,
			ThreadSafeOptions invalid) {
		if (invalid.getBval(0, false))
			return;
		@SuppressWarnings("unchecked")
		Substance3D s3d = new Substance3D(substance.toMap());
		if (visitSubstance != null) {
			visitSubstance.visit(s3d);
		}
		if (processConditions) {
			// if (optStatusProvider!=null)
			// optStatusProvider.setCurrentStatusText1("Process "+s3d.getName());
			BasicDBList condList = (BasicDBList) substance.get("conditions");
			if (condList != null) {
				for (Object co : condList) {
					DBObject cond = (DBObject) co;
					visitCondition(s3d, cond, visitBinary);
					
				}
			}
			BasicDBList l = (BasicDBList) substance.get("condition_ids");
			if (l != null) {
				double max = l.size();
				boolean printed = false;
				boolean singleFetch = true;
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
		int lsize = 0;
		for (Object o : l) {
			lsize++;
			if (o != null)
				ll.add(new ObjectId(o + ""));
		}
		DBCursor condL = null;
		try {
			synchronized (visitCondition) {
				BasicDBObject fields = new BasicDBObject("settings", 0);
				fields.put("remark", 0);
				fields.put("startdate", 0);
				fields.put("experimenttype", 0);
				fields.put("importdate", 0);
				fields.put("experimentname", 0);
				fields.put("coordinator", 0);
				fields.put("storagedate", 0);
				
				condL = collCond.find(
						new BasicDBObject("_id", new BasicDBObject("$in", ll))
						, new BasicDBObject()
								.append("_id", new Integer(1))
								.append("samples." + MongoCollection.IMAGES.toString(), new Integer(1))
								.append("samples." + MongoCollection.VOLUMES.toString(), new Integer(1))
								.append("samples." + MongoCollection.NETWORKS.toString(), new Integer(1))
						// .append("remark", new Integer(0))
						// .append("startdate", new Integer(0))
						// .append("experimenttype", new Integer(0))
						// .append("importdate", new Integer(0))
						// .append("experimentname", new Integer(0))
						// .append("coordinator", new Integer(0))
						// .append("storagedate", new Integer(0))
						).hint(new BasicDBObject("_id", 1));// .batchSize(Math.max(200, l.size()));
				int condLsize = 0;
				LinkedList<DBObject> list = new LinkedList<DBObject>();
				for (DBObject cond : condL) {
					list.add(cond);
				}
				condL.close();
				for (DBObject cond : list) {
					condLsize++;
					// find objects in "condition" collection, but only fields images, volumes, networks
					visitCondition.processDBid(cond.get("_id") + "");
					visitCondition(s3d, cond, visitBinary);
					if (optStatusProvider != null)
						optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep * 1 / max);
				}
				if (lsize != condLsize) {
					if (!printed) {
						String msg = "WARNING: " + (l.size() - condL.size()) + " condition(s) could not be retrieved for experiment " + header.getExperimentName();
						System.out.println(SystemAnalysis.getCurrentTime() + ">" + msg);
						saveSystemMessage(msg);
						printed = true;
						invalid.setBval(0, true);
					}
				}
			}
		} catch (Exception e) {
			invalid.setBval(0, true);
			e.printStackTrace();
		} finally {
			if (condL != null)
				condL.close();
		}
		return printed;
	}
	
	@SuppressWarnings("rawtypes")
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
				if (c.getDatatype() == null && c.getStorageType() != DataStorageType.PREVIEW_ICON) {
					GridFS gridfs_preview_files = new GridFS(db, MongoGridFS.FS_ANNOTATION_FILES.toString());
					DBCollection collectionC = db.getCollection(MongoGridFS.FS_ANNOTATION_FILES.toString() + ".files");
					if (getEnsureIndex())
						collectionC.createIndex("filename");
					
					DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_ANNOTATION_FILES.toString() + ".chunks");
					if (getEnsureIndex())
						collectionChunks.createIndex("files_id");
					
					result.setObject(gridfs_preview_files);
				} else
					if (c.getStorageType() == DataStorageType.PREVIEW_ICON) {
						GridFS gridfs_preview_files = new GridFS(db, "fs_preview_files");
						DBCollection collectionC = db.getCollection("fs_preview_files.files");
						if (getEnsureIndex())
							collectionC.createIndex("filename");
						
						DBCollection collectionChunks = db.getCollection("fs_preview_files.chunks");
						if (getEnsureIndex())
							collectionChunks.createIndex("files_id");
						
						result.setObject(gridfs_preview_files);
					} else
						switch (c.getDatatype()) {
							case IMAGE:
								switch (c.getStorageType()) {
									case MAIN_STREAM:
										GridFS gridfs_images = new GridFS(db, MongoGridFS.FS_IMAGES.toString());
										DBCollection collectionA = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString());
										if (getEnsureIndex())
											collectionA.createIndex(MongoGridFS.FIELD_FILENAME.toString());
										
										DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString() + ".chunks");
										if (getEnsureIndex())
											collectionChunks.createIndex("files_id");
										
										result.setObject(gridfs_images);
										break;
									case LABEL_FIELD:
										GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
										DBCollection collectionB = db.getCollection(MongoGridFS.FS_IMAGE_LABELS_FILES.toString());
										if (getEnsureIndex())
											collectionB.createIndex(MongoGridFS.FIELD_FILENAME.toString());
										
										collectionChunks = db.getCollection(MongoGridFS.FS_IMAGE_LABELS.toString() + ".chunks");
										if (getEnsureIndex())
											collectionChunks.createIndex("files_id");
										
										result.setObject(gridfs_null_files);
										break;
								}
								break;
							case VOLUME:
								switch (c.getStorageType()) {
									case MAIN_STREAM:
										GridFS gridfs_volumes = new GridFS(db, MongoGridFS.FS_VOLUMES.toString());
										DBCollection collectionA = db.getCollection(MongoGridFS.FS_VOLUMES_FILES.toString());
										if (getEnsureIndex())
											collectionA.createIndex(MongoGridFS.FIELD_FILENAME.toString());
										
										DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_VOLUMES_FILES.toString() + ".chunks");
										if (getEnsureIndex())
											collectionChunks.createIndex("files_id");
										
										result.setObject(gridfs_volumes);
										break;
									case LABEL_FIELD:
										GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_VOLUME_LABELS.toString());
										DBCollection collectionB = db.getCollection("fs_volume_labels.files");
										if (getEnsureIndex())
											collectionB.createIndex(MongoGridFS.FIELD_FILENAME.toString());
										
										collectionChunks = db.getCollection(MongoGridFS.FS_VOLUME_LABELS.toString() + ".chunks");
										if (getEnsureIndex())
											collectionChunks.createIndex("files_id");
										
										result.setObject(gridfs_null_files);
										break;
								}
								break;
							case NETWORK:
								switch (c.getStorageType()) {
									case MAIN_STREAM:
										GridFS gridfs_networks = new GridFS(db, MongoGridFS.FS_NETWORKS.toString());
										DBCollection collectionA = db.getCollection(MongoGridFS.FS_NETWORKS_FILES.toString());
										if (getEnsureIndex())
											collectionA.createIndex(MongoGridFS.FIELD_FILENAME.toString());
										
										DBCollection collectionChunks = db.getCollection(MongoGridFS.FS_NETWORKS.toString() + ".chunks");
										if (getEnsureIndex())
											collectionChunks.createIndex("files_id");
										
										result.setObject(gridfs_networks);
										break;
									case LABEL_FIELD:
										GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_NETWORK_LABELS.toString());
										DBCollection collectionB = db.getCollection(MongoGridFS.fs_networks_labels_files.toString());
										if (getEnsureIndex())
											collectionB.createIndex(MongoGridFS.FIELD_FILENAME.toString());
										
										collectionChunks = db.getCollection(MongoGridFS.FS_NETWORK_LABELS.toString() + ".chunks");
										if (getEnsureIndex())
											collectionChunks.createIndex("files_id");
										
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
				// Pattern regex = Pattern.compile("/system/");
				
				for (DBObject newsItem : db.getCollection("news")
						.find()
						// new BasicDBObject()
						// .append("text", regex))
						.sort(new BasicDBObject("date", -1))
						.limit(limit)) {
					Date l = (Date) newsItem.get("date");
					String text = (String) newsItem.get("text");
					String user = (String) newsItem.get("user");
					String systemuser = (String) newsItem.get("systemuser");
					String systemhost = (String) newsItem.get("systemhost");
					String systemiap = (String) newsItem.get("systemiap");
					String inf = "";
					if (systemuser != null || systemhost != null)
						inf = " (" + systemuser + "@" + systemhost + ")";
					res.add("<b>" + sdf.format(l) + inf + ":</b> " + StringManipulationTools.removeHTMLtags(text) + " (" + user
							+ ")" + (systemiap != null ? " <small><font color='gray'>IAP V" + systemiap + "</font></small>" : ""));
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
				ni.put("systemiap", ReleaseInfo.IAP_VERSION_STRING);
				ni.put("systemuser", SystemAnalysis.getUserName());
				try {
					ni.put("systemhost", SystemAnalysis.getLocalHost().getCanonicalHostName());
				} catch (UnknownHostException e) {
					ni.put("systemhost", "error=" + e.getMessage());
				}
				db.getCollection("news").insert(ni, WriteConcern.ACKNOWLEDGED);
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
				if (url != null && url.getPrefix().equals(LTftpHandler.PREFIX)) {
					if (getEnsureIndex())
						db.getCollection("constantSrc2hash").createIndex("srcUrl");
					
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
		
		MongoDBhandler h = getHandler();
		String prefix = h.getPrefix();
		return new IOurl(prefix, hash, url.getFileName());
	}
	
	public InputStream getURLforStoredData_PreviewStream(final IOurl url) throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (url != null && url.getPrefix().equals(LTftpHandler.PREFIX)) {
					if (getEnsureIndex())
						db.getCollection("constantSrc2hash").createIndex("srcUrl");
					
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
		
		MongoDBhandler h = getHandler();
		String prefix = h.getPrefix();
		return h.getPreviewInputStream(new IOurl(prefix, hash, url.getFileName()));
	}
	
	public synchronized String cleanUp(
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final boolean compact_warningLongExecutionTime)
			throws Exception {
		final StringBuilder res = new StringBuilder();
		processDB(new CleanupHelper(this, status, res), 1);
		return res.toString();
	}
	
	public String compact(
			final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		final StringBuilder res = new StringBuilder();
		processDB(new CleanupCompactHelper(status, res));
		return res.toString();
	}
	
	public String repair(
			final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		final StringBuilder res = new StringBuilder();
		processDB(new CleanupCompactHelper(status, res, true));
		return res.toString();
	}
	
	public static void saveSystemMessage(String msg) {
		try {
			saveSystemMessage(MongoDB.getDefaultCloud(), msg);
		} catch (Exception e1) {
			ErrorMsg.addErrorMessage(e1);
		}
	}
	
	public static void saveSystemMessage(final MongoDB m, final String msg) {
		BackgroundThreadDispatcher.runWithTimeout(500, new Runnable() {
			
			@Override
			public void run() {
				try {
					if (m != null)
						m.addNewsItem(msg,
								"system-msg");
				} catch (Exception e1) {
					ErrorMsg.addErrorMessage(e1.getLocalizedMessage() + " (failed to add news '" + msg + "')");
				}
			}
		}, "Save msg to DB (" + msg + ")");
	}
	
	public static void saveSystemErrorMessage(String error, Exception e) {
		if (e == null) {
			saveSystemMessage(error);
			IAPmain.errorCheck(error);
			return;
		}
		if (e.getCause() != null && e.getCause() instanceof Exception)
			e = (Exception) e.getCause();
		if (e.getCause() != null && e.getCause() instanceof Exception)
			e = (Exception) e.getCause();
		if (e.getCause() != null && e.getCause() instanceof Exception)
			e = (Exception) e.getCause();
		System.err.println(SystemAnalysis.getCurrentTime() + ">" + error);
		e.printStackTrace();
		saveSystemMessage("System eror message " + error + " - Exception " + e.getMessage() +
				". Stack-trace: " +
				e.getStackTrace() != null ?
				StringManipulationTools.getStringList(e.getStackTrace(), " // ") : "(no stacktrace)");
		IAPmain.errorCheck(error);
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
				ArrayList<GridFSDBFile> del = new ArrayList<GridFSDBFile>();
				for (GridFSDBFile o : files) {
					Date time = (Date) o.get("uploadDate");
					if (time == null)
						time = new Date(t++);
					if (System.currentTimeMillis() - time.getTime() > 24 * 60 * 60 * 1000) {
						del.add(o);
					} else {
						String fn = "" + o.get("_id");
						fn2newestFile.put(fn, o);
						fn2newestStorageTime.put(fn, time.getTime());
					}
				}
				for (GridFSDBFile d : del) {
					MongoDB.saveSystemMessage("Remove outdated (24h limit) screenshot information of '" + d.getFilename() + "'.");
					gridfs_webcam_files.remove(d);
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
			InputStream is = f.getInputStream();
			BufferedImage img;
			try {
				img = ImageIO.read(is);
			} finally {
				is.close();
			}
			return img;
		} catch (Exception e) {
			status.setCurrentStatusText1(" // ERROR: " + e.getMessage());
			return Image.getBufferedImageFromImage(IAPimages.getImage(IAPimages.getComputerOffline()));
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
		long startTime = System.currentTimeMillis();
		for (final GridFSDBFile f : toBeRemoved) {
			if (status != null && status.wantsToStop())
				break;
			free.addLong(f.getLength());
			if (!rmList)
				gridfs.remove(f.getFilename());
			else
				rList.add((ObjectId) f.getId());
			if (rmList && rList.size() > 10) {
				colFE.remove(
						new BasicDBObject("_id", new BasicDBObject("$in", rList)));
				colFC.remove(
						new BasicDBObject("files_id", new BasicDBObject("$in", rList)));
				rList.clear();
			}
			deleted++;
			status.setCurrentStatusText1("File "
					+ deleted + "/" + toBeRemoved.size() + " (" + (int) (deleted * 1000d * 60 / (System.currentTimeMillis() - startTime + 1)) + " files/min, "
					+ (int) (100d * deleted / toBeRemoved.size()) + "%)"
					+ ", removed: "
					+ SystemAnalysis.getDataTransferSpeedString(free.getLong(), startTime, System.currentTimeMillis()));
			status.setCurrentStatusValueFine(deleted * 100d / toBeRemoved.size());
		}
		if (status != null && status.wantsToStop())
			;
		else
			if (rmList && rList.size() > 0) {
				colFE.remove(
						new BasicDBObject("_id", new BasicDBObject("$in", rList)));
				colFC.remove(
						new BasicDBObject("files_id", new BasicDBObject("$in", rList)));
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
								new BasicDBObject("$gte", startdate).append("$lte", importdate))));
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
	private CollectionStorage colls;
	protected GridFS lastHitGridFS;
	
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
	
	public boolean hasVFSinputStream(String bucket, String detail) {
		if (vfs_file_storage != null) {
			synchronized (vfs_file_storage) {
				try {
					if (lastHit != null) {
						VirtualFileSystem i = lastHit.get(bucket);
						if (i != null) {
							IOurl h = i.getIOurlFor(bucket + "/" + detail);
							return i.getFileLength(h) > 0;
						}
					}
				} catch (Exception e) {
					// ignore
				}
				boolean r = false;
				for (VirtualFileSystem vfs : vfs_file_storage) {
					if (vfs == lastHit.get(bucket))
						continue;
					IOurl url = vfs.getIOurlFor(bucket + "/" + detail);
					try {
						r = vfs.getFileLength(url) > 0;
						if (r)
							return r;
					} catch (Exception e) {
						// ignore
					}
				}
				return r;
			}
		}
		return false;
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
					// System.out.println("CHECK UPDATE TIME: EXP INFO NOT FOUND");
					tso.setBval(0, true);
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		if (tso.getBval(0, false))
			throw new Exception("Experiment with ID " + header.getDatabaseId() + " not found! (name: " + header.getExperimentname() + ")");
		return (Long) res.getObject();
	}
	
	public CollectionStorage getColls() throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		if (colls == null) {
			RunnableOnDB runnableOnDB = new RunnableOnDB() {
				@Override
				public void run() {
					// empty
				}
				
				@Override
				public void setDB(DB db) {
					tso.setParam(0, db);
				}
			};
			processDB(runnableOnDB);
			colls = new CollectionStorage((DB) tso.getParam(0, null), getEnsureIndex());
		}
		return colls;
	}
	
	public SplitResult processSplitResults() {
		return new SplitResult(this);
	}
	
	public int getDefaultPort() {
		return 27017;
	}
}

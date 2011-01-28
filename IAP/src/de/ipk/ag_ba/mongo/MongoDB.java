/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.mongo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.bson.types.ObjectId;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.HashType;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOHandler;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MongoCollection;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk.ag_ba.server.task_management.CloudHost;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleAverage;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
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
		if (IAPservice.isReachable("ba-13.ipk-gatersleben.de") || IAPservice.isReachable("ba-24.ipk-gatersleben.de")) {
			res.add(getDefaultCloud());
			res.add(new MongoDB("Backup (md5)", "cloud2", "ba-13.ipk-gatersleben.de,ba-24.ipk-gatersleben.de", null, null, HashType.MD5));
		} else
			res.add(getLocalDB());
		
		// if (IAPservice.isReachable("localhost")) {
		// res.add(new MongoDB("local dbe3", "local_dbe3", "localhost", null, null, HashType.SHA512));
		// res.add(new MongoDB("local dbe4", "local_dbe4", "localhost", null, null, HashType.SHA512));
		// }
		return res;
	}
	
	public static MongoDB getLocalDB() {
		return new MongoDB("Local DB", "localCloud1", "localhost", null, null, HashType.SHA512);
	}
	
	public static MongoDB getDefaultCloud() {
		return new MongoDB("Data Processing", "cloud1", "ba-13.ipk-gatersleben.de,ba-24.ipk-gatersleben.de", null, null, HashType.SHA512);
	}
	
	public static MongoDB getLocalUnitTestsDB() {
		return new MongoDB("Unit Tests local", "localUnitTests", "localhost", null, null, HashType.SHA512);
	}
	
	private MongoDBhandler mh;
	private MongoDBpreviewHandler mp;
	
	public ResourceIOHandler[] getHandlers() {
		String serverIP = databaseHost;
		this.mh = new MongoDBhandler(serverIP, this);
		this.mp = new MongoDBpreviewHandler(serverIP, this);
		return new ResourceIOHandler[] { mh, mp };
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
	
	public MongoDB(String displayName, String databaseName, String hostName, String login, String password, HashType hashType) {
		if (databaseName == null || databaseName.contains("_") || databaseName.contains("/"))
			throw new UnsupportedOperationException("Database name may not be NULL and may not contain special characters!");
		this.displayName = displayName;
		this.databaseName = databaseName;
		this.databaseHost = hostName;
		this.databaseLogin = login;
		this.databasePass = password;
		this.hashType = hashType;
	}
	
	public void saveExperiment(final ExperimentInterface experiment, final BackgroundTaskStatusProviderSupportingExternalCall status)
						throws Exception {
		RunnableOnDB r = new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
			
			@Override
			public void run() {
				storeExperiment(experiment, db, status);
			}
		};
		processDB(r);
		
	}
	
	private static Mongo m;
	
	private void processDB(String dataBase, String optHosts, String optLogin, String optPass,
						RunnableOnDB runnableOnDB) throws Exception {
		
		try {
			BackgroundTaskHelper.lockAquire(dataBase, 4);
			DB db;
			if (m == null) {
				if (optHosts == null || optHosts.length() == 0) {
					m = new Mongo();
				} else {
					List<ServerAddress> seeds = new ArrayList<ServerAddress>();
					for (String h : optHosts.split(","))
						seeds.add(new ServerAddress(h));
					m = new Mongo(seeds);
					// m.slaveOk();
				}
			}
			db = m.getDB(dataBase);
			if (optLogin != null && optPass != null && optLogin.length() > 0 && optPass.length() > 0) {
				boolean auth = db.authenticate(optLogin, optPass.toCharArray());
				if (!auth) {
					throw new Exception("Invalid MongoDB login data provided!");
				}
			}
			runnableOnDB.setDB(db);
			try {
				runnableOnDB.run();
			} catch (Exception err) {
				System.out.println("ERROR: " + err.getLocalizedMessage());
				System.out.println("RE-TRY LAST DATABASE COMMAND IN 10 SEC.");
				try {
					BackgroundThreadDispatcher.waitSec(10);
					runnableOnDB.run();
				} catch (Exception err2) {
					System.out.println("ERROR 2: " + err2.getLocalizedMessage());
					System.out.println("RE-TRY SECOND AND LAST TIME LAST DATABASE COMMAND IN 5 MIN.");
					BackgroundThreadDispatcher.waitSec(5 * 60);
					runnableOnDB.run();
				}
			}
		} finally {
			BackgroundTaskHelper.lockRelease(dataBase);
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
						System.out.println(wr.toString());
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
						BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		// System.out.println("STORE EXPERIMENT: " + experiment.getName());
		experiment.getHeader().setImportusername(SystemAnalysis.getUserName());
		
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		
		ObjectRef overallFileSize = new ObjectRef();
		overallFileSize.addLong(0);
		
		DBCollection substances = db.getCollection("substances");
		
		DBCollection conditions = db.getCollection("conditions");
		
		boolean inline = false;
		
		if (inline) {
			substances = null;
			conditions = null;
		}
		
		int errorCount = 0;
		
		long lastTransferSum = 0;
		int lastSecond = -1;
		
		int count = 0;
		StringBuilder errors = new StringBuilder();
		int numberOfBinaryData = countMeasurementValues(experiment, new MeasurementNodeType[] {
							MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME, MeasurementNodeType.NETWORK });
		List<DBObject> dbSubstances = new ArrayList<DBObject>();
		HashMap<DBObject, List<BasicDBObject>> substance2conditions = new HashMap<DBObject, List<BasicDBObject>>();
		for (SubstanceInterface s : experiment) {
			if (status != null && status.wantsToStop())
				break;
			
			attributes.clear();
			s.fillAttributeMap(attributes);
			BasicDBObject substance = new BasicDBObject(filter(attributes));
			dbSubstances.add(substance);
			
			List<BasicDBObject> dbConditions = new ArrayList<BasicDBObject>();
			
			for (ConditionInterface c : s) {
				if (status != null && status.wantsToStop())
					break;
				attributes.clear();
				c.fillAttributeMap(attributes);
				BasicDBObject condition = new BasicDBObject(filter(attributes));
				dbConditions.add(condition);
				
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
									res = saveImageFile(db, id, overallFileSize);
									if (res == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
										errorCount++;
										errors.append("<li>" + id.getURL().getFileName());
									} else {
										m.fillAttributeMap(attributes);
										BasicDBObject dbo = new BasicDBObject(filter(attributes));
										dbImages.add(dbo);
									}
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
								double prog = count * (100d / numberOfBinaryData / 2d);
								status.setCurrentStatusText1(count + "/" + numberOfBinaryData * 2 + ": " + res);
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
					if (dbImages.size() > 0)
						sample.put("images", dbImages);
					if (dbVolumes.size() > 0)
						sample.put("volumes", dbVolumes);
					if (dbNetworks.size() > 0)
						sample.put("networks", dbVolumes);
				} // sample
				condition.put("samples", dbSamples);
			} // condition
			if (inline)
				substance.put("conditions", dbConditions);
			else
				substance2conditions.put(substance, dbConditions);
		} // substance
		
		if (substances != null)
			for (DBObject dbSubstance : dbSubstances) {
				ArrayList<String> conditionIDs = new ArrayList<String>();
				if (substance2conditions.get(dbSubstance) != null)
					for (DBObject dbc : substance2conditions.get(dbSubstance)) {
						conditions.insert(dbc);
					}
				if (substance2conditions.get(dbSubstance) != null)
					for (DBObject dbCondition : substance2conditions.get(dbSubstance))
						conditionIDs.add(((BasicDBObject) dbCondition).getString("_id"));
				dbSubstance.put("condition_ids", conditionIDs);
				if (status == null || (status != null && !status.wantsToStop())) {
					substances.insert(dbSubstance);
				}
			}
		ArrayList<String> substanceIDs = new ArrayList<String>();
		if (dbSubstances != null)
			for (DBObject substance : dbSubstances)
				substanceIDs.add(((BasicDBObject) substance).getString("_id"));
		
		experiment.getHeader().setSizekb(overallFileSize.getLong() / 1024);
		
		experiment.fillAttributeMap(attributes);
		BasicDBObject dbExperiment = new BasicDBObject(attributes);
		if (inline)
			dbExperiment.put("substances", dbSubstances);
		else
			dbExperiment.put("substance_ids", substanceIDs);
		
		DBCollection experiments = db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString());
		
		experiments.insert(dbExperiment);
		
		String id = dbExperiment.get("_id").toString();
		for (ExperimentHeaderInterface eh : experiment.getHeaders()) {
			eh.setDatabaseId(id);
		}
		
		if (errorCount > 0) {
			MainFrame.showMessageDialog(
								"<html>" + "The following files cound not be properly processed:<ul>" + errors.toString() + "</ul> "
													+ "", "Errors");
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
		inputFile.getMetaData().put("name", file.getName());
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
			ErrorMsg.addErrorMessage(e);
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
		if (fff == null) {
			GridFSInputFile inputFile = fs.createFile(is);
			inputFile.setFilename(hash);
			inputFile.save();
			// fs.getDB().requestStart();
			result = inputFile.getLength();
			// CommandResult res = fs.getDB().getLastError(2, 180000, false);
			// if (!res.ok())
			// result = -1;
			// fs.getDB().requestDone();
		} else
			result = 0;
		is.close();
		
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
		if (vvv != null) {
			gridfs_preview.remove(vvv);
			vvv = null;
		}
		
		IntVolumeInputStream is = (IntVolumeInputStream) id.getURL().getInputStream();
		System.out.println("AVAIL: " + is.available());
		System.out.println("TARGET-LENGTH: " + (id.getDimensionX() * id.getDimensionY() * id.getDimensionZ() * 4));
		GridFSInputFile inputFile = gridfs_volumes.createFile(is);
		inputFile.setFilename(hash);
		inputFile.getMetaData().put("name", id.getURL().getFileName());
		inputFile.save();
		System.out.println("SAVED VOLUME: " + id.toString() + " // SIZE: " + inputFile.getLength());
		
		GridFSDBFile fff = gridfs_preview.findOne(id.getURL().getDetail());
		if (fff != null) {
			gridfs_preview.remove(fff);
			fff = null;
		}
		
		if (fff == null) {
			try {
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Render Side Views");
				System.out.println("Render side view GIF...");
				LoadedVolumeExtension lv;
				if (id instanceof LoadedVolumeExtension)
					lv = (LoadedVolumeExtension) id;
				else
					lv = new LoadedVolumeExtension(IOmodule.loadVolume(id));
				GridFSInputFile inputFilePreview = gridfs_preview.createFile(IOmodule
									.getThreeDvolumePreviewIcon(lv, optStatus));
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Save Preview Icon");
				inputFilePreview.setFilename(hash);
				inputFilePreview.getMetaData().put("name", id.getURL().getFileName());
				inputFilePreview.save();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Saved Volume ("
								+ ((VolumeInputStream) id.getURL().getInputStream()).getNumberOfBytes() / 1024 / 1024 + " MB)");
		return ((VolumeInputStream) id.getURL().getInputStream()).getNumberOfBytes();
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
		inputFile.getMetaData().put("name", network.getURL().getFileName());
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
				inputFilePreview.getMetaData().put("name", network.getURL().getFileName());
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
	
	public DatabaseStorageResult saveImageFile(DB db, ImageData id, ObjectRef fileSize) throws Exception {
		InputStream isMain = id.getURL().getInputStream();
		InputStream isLabel = id.getLabelURL() == null ? null : id.getLabelURL().getInputStream();
		ImageData image = id;
		
		if (isMain == null) {
			System.out.println("No input stream for source-URL:  " + id.getURL());
			return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
		if (id.getLabelURL() != null && isLabel == null) {
			System.out.println("No input stream for source-URL (label):  " + id.getURL());
			return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
		
		if (id.getURL() != null && id.getLabelURL() != null) {
			if (id.getURL().getPrefix().equals(mh.getPrefix()) && id.getLabelURL().getPrefix().equals(mh.getPrefix()))
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		}
		
		String[] hashes = GravistoService.getHashFromInputStream(new InputStream[] { isMain, isLabel },
				new ObjectRef[] { fileSize, fileSize }, getHashType());
		
		String hashMain = hashes[0];
		String hashLabel = hashes[1];
		if (isMain instanceof MyByteArrayInputStream)
			isMain = new MyByteArrayInputStream(((MyByteArrayInputStream) isMain).getBuff());
		else
			isMain = id.getURL().getInputStream();
		
		if (isLabel != null) {
			if (isLabel instanceof MyByteArrayInputStream)
				isLabel = new MyByteArrayInputStream(((MyByteArrayInputStream) isLabel).getBuff());
			else
				isLabel = id.getLabelURL().getInputStream();
		}
		
		GridFS gridfs_images = new GridFS(db, MongoGridFS.FS_IMAGES.toString());
		DBCollection collectionA = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString());
		collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
		DBCollection collectionB = db.getCollection(MongoGridFS.FS_IMAGE_LABELS_FILES.toString());
		collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		GridFS gridfs_preview_files = new GridFS(db, MongoGridFS.FS_PREVIEW.toString());
		DBCollection collectionC = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
		collectionC.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		GridFSDBFile fffMain = gridfs_images.findOne(hashMain);
		image.getURL().setPrefix(mh.getPrefix());
		image.getURL().setDetail(hashMain);
		
		GridFSDBFile fffLabel = gridfs_images.findOne(hashLabel);
		if (image.getLabelURL() != null) {
			image.getLabelURL().setPrefix(mh.getPrefix());
			image.getLabelURL().setDetail(hashLabel);
		}
		
		if (fffMain != null && fffMain.getLength() <= 0) {
			gridfs_images.remove(fffMain);
			fffMain = null;
		}
		if (fffLabel != null && fffLabel.getLength() <= 0) {
			gridfs_images.remove(fffLabel);
			fffLabel = null;
		}
		
		if (fffMain != null && fffLabel != null) {
			return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		} else {
			boolean saved = saveImageFile(new InputStream[] { isMain, isLabel }, gridfs_images, gridfs_null_files, gridfs_preview_files, id, hashMain, hashLabel,
					fffMain == null, fffLabel == null);
			if (saved) {
				return DatabaseStorageResult.STORED_IN_DB;
			} else
				return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
	}
	
	public DatabaseStorageResult saveVolumeFile(DB db, VolumeData volume, ObjectRef optFileSize,
						BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		GridFS gridfs_volumes = new GridFS(db, MongoGridFS.FS_VOLUMES.toString());
		DBCollection collectionA = db.getCollection(MongoGridFS.FS_VOLUMES_FILES.toString());
		collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		GridFS gridfs_preview = new GridFS(db, MongoGridFS.FS_PREVIEW_FILES.toString());
		DBCollection collectionB = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
		collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		String hash;
		try {
			hash = GravistoService.getHashFromInputStream(volume.getURL().getInputStream(), optFileSize, getHashType());
			
			GridFSDBFile fff = gridfs_volumes.findOne(hash);
			if (fff != null && fff.getLength() <= 0) {
				System.out.println("Found Zero-Size File.");
				System.out.println("Delete Existing Volume.");
				gridfs_volumes.remove(fff);
				fff = null;
			}
			if (fff != null) {
				return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
			} else {
				saveVolumeFile(gridfs_volumes, gridfs_preview, volume, optFileSize, optStatus, hash);
				return DatabaseStorageResult.STORED_IN_DB;
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
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
	
	public ArrayList<ExperimentHeaderInterface> getExperimentList() {
		final ArrayList<ExperimentHeaderInterface> res = new ArrayList<ExperimentHeaderInterface>();
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					for (DBObject header : db.getCollection(MongoExperimentCollections.EXPERIMENTS.toString()).find()) {
						res.add(new ExperimentHeader(header.toMap()));
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
	
	public ExperimentInterface getExperiment(final ExperimentHeaderInterface header) {
		final ExperimentInterface experiment = new Experiment();
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
								processSubstance(db, experiment, substance);
							}
						BasicDBList l = (BasicDBList) expref.get("substance_ids");
						if (l != null)
							for (Object o : l) {
								if (o == null)
									continue;
								DBRef subr = new DBRef(db, "substances", new ObjectId(o.toString()));
								if (subr != null) {
									DBObject substance = subr.fetch();
									if (substance != null) {
										processSubstance(db, experiment, substance);
									}
								}
							}
					}
					experiment.setHeader(header);
					
					int numberOfImagesAndVolumes = countMeasurementValues(experiment, new MeasurementNodeType[] {
										MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME });
					experiment.getHeader().setNumberOfFiles(numberOfImagesAndVolumes);
					
					if (numberOfImagesAndVolumes > 0) {
						updateExperimentSize(db, experiment);
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
		return experiment;
	}
	
	public void setExperimentType(final ExperimentHeaderInterface header, final String experimentType) throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				ObjectId id = new ObjectId(header.getExcelfileid());
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
			throw new Exception("Experiment with ID " + header.getExcelfileid() + " not found!");
	}
	
	public void setExperimentInfo(final ExperimentHeaderInterface header) throws Exception {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				ObjectId id = new ObjectId(header.getExcelfileid());
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
	
	private void updateExperimentSize(DB db, ExperimentInterface experiment) {
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
				for (NumericMeasurementInterface nmd : Substance3D.getAllFiles(experiment)) {
					if (nmd instanceof BinaryMeasurement) {
						IOurl url = ((BinaryMeasurement) nmd).getURL();
						if (url != null) {
							String hash = url.getDetail();
							for (String s : MongoGridFS.getFileCollectionsFor(nmd)) {
								GridFS gridfs = new GridFS(db, s);
								GridFSDBFile file = gridfs.findOne(hash);
								if (file != null) {
									newSize.addLong(file.getLength());
								}
							}
						}
					}
				}
				experiment.getHeader().setSizekb(newSize.getLong() / 1024);
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
		final long curr = System.currentTimeMillis();
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBCollection dbc = db.getCollection("compute_hosts");
				dbc.setObjectClass(CloudHost.class);
				
				DBCursor cursor = dbc.find();
				while (cursor.hasNext()) {
					CloudHost h = (CloudHost) cursor.next();
					if (curr - h.getLastUpdateTime() < maxUpdate)
						res.add(h);
					
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		
		return res;
	}
	
	public synchronized void batchPingHost(final String ip,
			final int blocksExecutedWithinLastMinute,
			final int pipelineExecutedWithinLast5Minutes,
			final int tasksExecutedWithinLastMinute) throws Exception {
		processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				DBCollection dbc = db.getCollection("compute_hosts");
				dbc.setObjectClass(CloudHost.class);
				
				BasicDBObject query = new BasicDBObject();
				query.put(CloudHost.getHostId(), ip);
				
				CloudHost res = (CloudHost) dbc.findOne(query);
				if (res != null) {
					res.updateTime();
					res.setOperatingSystem(SystemAnalysis.getOperatingSystem());
					res.setBlocksExecutedWithinLastMinute(blocksExecutedWithinLastMinute);
					res.setPipelineExecutedWithinLast5Minutes(pipelineExecutedWithinLast5Minutes);
					res.setTasksExecutedWithinLastMinute(tasksExecutedWithinLastMinute);
					res.setHostInfo(SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis.getMemoryMB() + " MB, " +
							SystemAnalysis.getRealSystemMemoryInMB() / 1024 + " GB<br>" + SystemAnalysis.getNumberOfCPUs() +
							"/" + SystemAnalysis.getRealNumberOfCPUs() + " CPUs, load: "
							+ AttributeHelper.formatNumber(SystemAnalysis.getRealSystemCpuLoad(), "#.#")
							+ ", queued: "
							+ BackgroundThreadDispatcher.getWorkLoad());
					res.setLastPipelineTime(BlockPipeline.getLastPipelineExecutionTimeInSec());
					dbc.save(res);
				} else {
					try {
						res = new CloudHost();
						dbc.insert(res);
					} catch (UnknownHostException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	public synchronized CloudHost batchGetUpdatedHostInfo(final CloudHost h) throws Exception {
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
	
	/**
	 * if a batch lastUpdate time is older then the provided limit, it is
	 * returned in the result set and will most likely be re-claimed to
	 * another host
	 */
	public Collection<BatchCmd> batchGetCommands(final long maxUpdate) {
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
						CloudAnalysisStatus s = batch.getRunStatus();
						if (s == CloudAnalysisStatus.SCHEDULED
											|| (
											(batch.getRunStatus() == CloudAnalysisStatus.STARTING || batch.getRunStatus() == CloudAnalysisStatus.STARTING)
											&& System.currentTimeMillis() - batch.getLastUpdateTime() > maxUpdate))
							res.add(batch);
						// System.out.println(batch);
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
	
	public Collection<BatchCmd> batchGetWorkTasksScheduledForStart() {
		final Collection<BatchCmd> res = new ArrayList<BatchCmd>();
		try {
			processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					String hostName;
					try {
						hostName = SystemAnalysis.getHostName();
						DBCollection collection = db.getCollection("schedule");
						collection.setObjectClass(BatchCmd.class);
						for (DBObject dbo : collection.find()) {
							BatchCmd batch = (BatchCmd) dbo;
							boolean added = false;
							if (batch.getRunStatus() != null && batch.getRunStatus() == CloudAnalysisStatus.STARTING)
								if (hostName.equals("" + batch.getOwner())) {
									res.add(batch);
									added = true;
								}
							
							if (!added)
								if (batch.getRunStatus() != null)
									if (batch.get("lastupdate") == null || (System.currentTimeMillis() - batch.getLastUpdateTime() > 60000)) {
										res.add(batch);
										batchClaim(batch, CloudAnalysisStatus.STARTING, false);
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
		if (res.size() > 0)
			System.out.println("SCHEDULED FOR START: " + res.size());
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
						batch.setOwner(SystemAnalysis.getHostName());
						DBCollection collection = db.getCollection("schedule");
						collection.setObjectClass(BatchCmd.class);
						DBObject dbo = new BasicDBObject();
						dbo.put("_id", batch.get("_id"));
						String rs = batch.getString("runstatus");
						dbo.put("runstatus", rs);
						if (requireOwnership)
							dbo.put("owner", SystemAnalysis.getHostName());
						batch.put("runstatus", starting.toString());
						batch.put("lastupdate", System.currentTimeMillis());
						WriteResult r = collection.update(dbo, batch, false, false);
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
					dbo.put("_id", batch.get("_id"));
					BatchCmd res = (BatchCmd) collection.findOne(dbo);
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
	
	private void processSubstance(DB db, ExperimentInterface experiment, DBObject substance) {
		@SuppressWarnings("unchecked")
		Substance3D s3d = new Substance3D(substance.toMap());
		experiment.add(s3d);
		BasicDBList condList = (BasicDBList) substance.get("conditions");
		if (condList != null)
			for (Object co : condList) {
				DBObject cond = (DBObject) co;
				processCondition(s3d, cond);
			}
		BasicDBList l = (BasicDBList) substance.get("condition_ids");
		if (l != null)
			for (Object o : l) {
				DBRef condr = new DBRef(db, "conditions", new ObjectId(o.toString()));
				DBObject cond = condr.fetch();
				if (cond != null) {
					processCondition(s3d, cond);
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
					result.setObject(gridfs_preview_files);
				} else
					switch (c.getDatatype()) {
						case IMAGE:
							switch (c.getStorageType()) {
								case MAIN_STREAM:
									GridFS gridfs_images = new GridFS(db, MongoGridFS.FS_IMAGES.toString());
									DBCollection collectionA = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString());
									collectionA.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
									result.setObject(gridfs_images);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
									DBCollection collectionB = db.getCollection(MongoGridFS.FS_IMAGE_LABELS_FILES.toString());
									collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
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
									result.setObject(gridfs_volumes);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_VOLUME_LABELS.toString());
									DBCollection collectionB = db.getCollection("fs_volume_labels.files");
									collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
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
									result.setObject(gridfs_networks);
									break;
								case LABEL_FIELD:
									GridFS gridfs_null_files = new GridFS(db, MongoGridFS.FS_NETWORK_LABELS.toString());
									DBCollection collectionB = db.getCollection(MongoGridFS.fs_networks_labels_files.toString());
									collectionB.ensureIndex(MongoGridFS.FIELD_FILENAME.toString());
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
}

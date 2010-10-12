/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 8, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.mongo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.HomeFolder;
import org.ObjectRef;
import org.bson.types.ObjectId;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import rmi_server.analysis.IOmodule;
import rmi_server.analysis.VolumeUploadData;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ExperimentIOManager;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.IOurl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageUploadData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementIOConfigObject;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementIOHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MyByteArrayInputStream;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;
import de.ipk_gatersleben.ag_pbi.mmd.loaders.MeasurementNodeType;

/**
 * @author klukas
 * 
 */
public class MongoDB {

	public class MongoDBpreviewHandler implements MeasurementIOHandler {
		// mongo-preview://c3fd77bc7b74388d9dcff9d09d1c16fc/000Grad.png
		public static final String PREFIX = "mongo-preview";

		@Override
		public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, MeasurementIOConfigObject config)
				throws Exception {
			return null;
		}

		@Override
		public InputStream getInputStream(final IOurl url) throws Exception {
			final ObjectRef or = new ObjectRef();

			processDB(new RunnableOnDB() {
				private DB db;

				@Override
				public void run() {
					GridFS gridfs_preview_images = new GridFS(db, "preview_files");
					GridFSDBFile fff = gridfs_preview_images.findOne(url.getDetail());
					if (fff != null) {
						try {
							InputStream is = fff.getInputStream();
							or.setObject(is);
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
				}

				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
			return (InputStream) or.getObject();
		}

		@Override
		public String getPrefix() {
			return PREFIX;
		}
	}

	public MeasurementIOHandler[] getHandlers() {
		return new MeasurementIOHandler[] { new MongoDBhandler(), new MongoDB.MongoDBpreviewHandler() };
	}

	private String defaultDBE = "dbe3";
	private String defaultHost = "nw-04.ipk-gatersleben.de,ba-24.ipk-gatersleben.de";
	private String defaultLogin = null;
	private String defaultPass = null;

	// collections:
	// preview_files
	// volumes
	// images
	// annotations
	// experiments
	// substances

	static boolean init = false;

	public MongoDB() {
		if (init)
			return;
		init = true;

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name;
		try {
			name = new ObjectName("de.ipk_gatersleben.ag_ba.mongo:type=Hello");
			Hello mbean = new Hello();
			mbs.registerMBean(mbean, name);
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		}
	}

	public void storeExperiment(String dataBase, String optHosts, String optLogin, String optPass,
			final ExperimentInterface experiment, final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		processDB(dataBase, optHosts, optLogin, optPass, new RunnableOnDB() {

			private DB db;

			@Override
			public void setDB(DB db) {
				this.db = db;
			}

			@Override
			public void run() {
				storeExperiment(experiment, db, status);
			}
		});
	}

	private static Mongo m;

	public synchronized void processDB(String dataBase, String optHosts, String optLogin, String optPass,
			RunnableOnDB runnableOnDB) throws Exception {
		DB db;
		if (m == null) {
			if (optHosts == null || optHosts.length() == 0)
				m = new Mongo();
			else {
				List<ServerAddress> seeds = new ArrayList<ServerAddress>();
				for (String h : optHosts.split(","))
					seeds.add(new ServerAddress(h));
				m = new Mongo(seeds);
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
		runnableOnDB.run();
	}

	public void processDB(RunnableOnDB runnableOnDB) throws Exception {
		processDB(getDefaultDBE(), defaultHost, defaultLogin, defaultPass, runnableOnDB);
	}

	private void storeExperiment(ExperimentInterface experiment, DB db,
			BackgroundTaskStatusProviderSupportingExternalCall status) {

		experiment.getHeader().setImportusername(SystemAnalysis.getUserName());

		HashMap<String, Object> attributes = new HashMap<String, Object>();

		ObjectRef overallFileSize = new ObjectRef();

		DBCollection substances = db.getCollection("substances");

		int errorCount = 0;
		int count = 0;
		StringBuilder errors = new StringBuilder();
		int numberOfBinaryData = countMeasurementValues(experiment, new MeasurementNodeType[] {
				MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME });
		List<DBObject> dbSubstances = new ArrayList<DBObject>();
		ArrayList<String> substanceIDs = new ArrayList<String>();
		for (SubstanceInterface s : experiment) {
			if (status.wantsToStop())
				break;
			attributes.clear();
			s.fillAttributeMap(attributes);
			BasicDBObject substance = new BasicDBObject(filter(attributes));
			dbSubstances.add(substance);

			List<BasicDBObject> dbConditions = new ArrayList<BasicDBObject>();

			for (ConditionInterface c : s) {
				if (status.wantsToStop())
					break;
				attributes.clear();
				c.fillAttributeMap(attributes);
				BasicDBObject condition = new BasicDBObject(filter(attributes));
				dbConditions.add(condition);

				List<BasicDBObject> dbSamples = new ArrayList<BasicDBObject>();
				for (SampleInterface sa : c) {
					if (status.wantsToStop())
						break;
					attributes.clear();
					sa.fillAttributeMap(attributes);
					BasicDBObject sample = new BasicDBObject(filter(attributes));
					dbSamples.add(sample);

					attributes.clear();
					sa.getSampleAverage().fillAttributeMap(attributes);
					BasicDBObject dbSampleAverage = new BasicDBObject(filter(attributes));
					if (sa.size() > 0)
						sample.put("average", dbSampleAverage);

					List<BasicDBObject> dbMeasurements = new ArrayList<BasicDBObject>();
					List<BasicDBObject> dbImages = new ArrayList<BasicDBObject>();
					List<BasicDBObject> dbVolumes = new ArrayList<BasicDBObject>();

					for (Measurement m : sa) {
						attributes.clear();
						m.fillAttributeMap(attributes);
						BasicDBObject measurement = new BasicDBObject(filter(attributes));
						dbMeasurements.add(measurement);
					} // measurement
					if (sa instanceof Sample3D) {
						Sample3D s3 = (Sample3D) sa;
						for (NumericMeasurementInterface m : s3.getBinaryMeasurements()) {
							if (m instanceof ImageData) {
								attributes.clear();
								ImageData id = (ImageData) m;
								DatabaseStorageResult res;
								try {
									res = storeImageFile(db, id, overallFileSize);
								} catch (Exception e) {
									ErrorMsg.addErrorMessage(e);
									res = DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
								}
								if (res == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
									errorCount++;
									errors.append("<li>" + id.getURL().getFileName());
								}
								if (status != null) {
									status.setCurrentStatusText1(count + "/" + numberOfBinaryData + ": " + res.toString());
								}
								m.fillAttributeMap(attributes);
								BasicDBObject image = new BasicDBObject(filter(attributes));
								dbImages.add(image);
								count++;
							}
							if (m instanceof VolumeData) {
								attributes.clear();
								m.fillAttributeMap(attributes);
								BasicDBObject volume = new BasicDBObject(filter(attributes));
								dbVolumes.add(volume);
								VolumeData vd = (VolumeData) m;
								VolumeUploadData vud = null;
								if (vd instanceof LoadedVolume)
									vud = IOmodule.getThreeDvolumeInputStream((LoadedVolume) vd);

								storeVolumeFile(db, vd, vud, overallFileSize);
								count++;
							}
							double prog = count * (100d / numberOfBinaryData);
							if (status != null) {
								status.setCurrentStatusValueFine(prog);
							}
						} // binary measurement
					}
					if (dbMeasurements.size() > 0)
						sample.put("measurements", dbMeasurements);
					if (dbImages.size() > 0)
						sample.put("images", dbImages);
					if (dbVolumes.size() > 0)
						sample.put("volumes", dbVolumes);
				} // sample
				condition.put("samples", dbSamples);
			} // condition
			substance.put("conditions", dbConditions);
		} // substance

		if (!status.wantsToStop()) {
			substances.insert(dbSubstances);

			for (DBObject substance : dbSubstances)
				substanceIDs.add(((BasicDBObject) substance).getString("_id"));

			experiment.getHeader().setSizekb(overallFileSize.getLong() / 1024 + "");

			experiment.fillAttributeMap(attributes);
			BasicDBObject dbExperiment = new BasicDBObject(attributes);
			dbExperiment.put("substance_ids", substanceIDs);

			DBCollection experiments = db.getCollection("experiments");

			experiments.insert(dbExperiment);
			String id = dbExperiment.get("_id").toString();
			for (ExperimentHeaderInterface eh : experiment.getHeaders()) {
				eh.setExcelfileid(id);
			}

			if (errorCount > 0) {
				MainFrame.showMessageDialog("<html>" + "The following files cound not be properly processed:<ul>"
						+ errors.toString() + "</ul> " + "", "Errors");
			}
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

	public long saveAnnotationFile(GridFS gridfs_annotation, String md5, File file) throws IOException {
		GridFSInputFile inputFile = gridfs_annotation.createFile(file);
		inputFile.setFilename(md5);
		inputFile.getMetaData().put("md5", md5);
		inputFile.getMetaData().put("name", file.getName());
		inputFile.save();
		return file.length();
	}

	public long saveImageFile(GridFS gridfs_images, GridFS gridfs_preview_files, ImageData image,
			ImageUploadData optIUD, String md5) throws IOException {
		long result = -1;
		ImageUploadData iud = null;

		try {
			if (!(image instanceof LoadedImage))
				image = IOmodule.loadImageFromFileOrMongo(image, null, null);
			LoadedImage id = (LoadedImage) image;
			if (optIUD != null)
				iud = optIUD;
			else
				iud = id.getImageUploadData();

			GridFSInputFile inputFile;
			if (id.getSrcFile() != null)
				inputFile = gridfs_images.createFile(id.getSrcFile());
			else {
				System.out.println("Inputstream-lenght: " + iud.getLength());
				inputFile = gridfs_images.createFile(iud.getInputStream());
			}

			inputFile.setFilename(md5);
			inputFile.save();
			result = iud.getLength();
			if (result < 0)
				System.out.println("ERROR: NNN");
			GridFSDBFile fff = gridfs_preview_files.findOne(md5);
			if (fff == null) {
				GridFSInputFile inputFilePreview = gridfs_preview_files.createFile(iud.getPreviewInputStream());
				inputFilePreview.setFilename(md5);
				inputFilePreview.save();
				iud.closeStreams();
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}

		return result;
	}

	private long saveVolumeFile(GridFS gridfs_volumes, GridFS gridfs_preview, VolumeData volume,
			VolumeUploadData threeDvolumeInputStream, ObjectRef optFileSize) throws Exception {
		LoadedVolume id = (LoadedVolume) volume;
		VolumeUploadData vud = IOmodule.getThreeDvolumeInputStream(id);
		InputStream is = vud.getStream();
		String md5 = AttributeHelper.getMD5fromInputStream(is, optFileSize);
		is = vud.getStream();
		GridFSInputFile inputFile = gridfs_volumes.createFile(is);
		inputFile.setFilename(md5);
		inputFile.getMetaData().put("md5", md5);
		inputFile.getMetaData().put("name", id.getURL().getFileName());
		inputFile.save();

		GridFSDBFile fff = gridfs_preview.findOne(volume.getURL().getDetail());
		if (fff == null) {
			GridFSInputFile inputFilePreview = gridfs_preview.createFile(IOmodule.getThreeDvolumePreviewIcon(id));
			inputFilePreview.setFilename(md5);
			inputFilePreview.getMetaData().put("md5", md5);
			inputFilePreview.getMetaData().put("name", id.getURL().getFileName());
			inputFilePreview.save();
		}
		return threeDvolumeInputStream.getLength();
	}

	public DatabaseStorageResult storeImageFile(DB db, ImageData id, ObjectRef fileSize) throws Exception {
		InputStream is;
		ImageUploadData iud = null;
		ImageData srcID = id;
		if (id instanceof LoadedImage) {
			LoadedImage li = (LoadedImage) id;
			iud = li.getImageUploadData();
			is = iud.getInputStream();
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			HomeFolder.copyContent(ExperimentIOManager.getInputStream(id.getURL()), bos);
			is = new MyByteArrayInputStream(bos.toByteArray());
			BufferedImage image = ImageIO.read(is);
			is = new MyByteArrayInputStream(((MyByteArrayInputStream) is).getBuff());
			LoadedImage li = new LoadedImage(id, image);
			id = li;
		}
		if (is == null) {
			System.out.println("No input stream for source-URL: " + id.getURL());
			return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
		String md5 = AttributeHelper.getMD5fromInputStream(is, fileSize);
		if (is instanceof MyByteArrayInputStream)
			is = new MyByteArrayInputStream(((MyByteArrayInputStream) is).getBuff());
		GridFS gridfs_images = new GridFS(db, "images");
		DBCollection collectionA = db.getCollection("images.files");
		collectionA.ensureIndex("filename");
		GridFS gridfs_preview_files = new GridFS(db, "preview_files");
		DBCollection collectionB = db.getCollection("preview_files.files");
		collectionB.ensureIndex("filename");
		GridFSDBFile fff = gridfs_images.findOne(md5);

		srcID.getURL().setPrefix(MongoDBhandler.PREFIX);
		srcID.getURL().setDetail(md5);

		if (fff != null && fff.getLength() <= 0) {
			System.out.println("Found Zero-Size File.");
			gridfs_images.remove(fff);
			fff = null;
		}
		if (fff != null) {
			return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		} else {
			long res = saveImageFile(gridfs_images, gridfs_preview_files, id, iud, md5);
			if (res >= 0) {
				return DatabaseStorageResult.STORED_IN_DB;
			} else
				return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
	}

	public DatabaseStorageResult storeVolumeFile(DB db, VolumeData volume, VolumeUploadData threeDvolumeInputStream,
			ObjectRef optFileSize) {
		GridFS gridfs_volumes = new GridFS(db, "volumes");
		DBCollection collectionA = db.getCollection("volumes.files");
		collectionA.ensureIndex("filename");
		GridFS gridfs_preview = new GridFS(db, "preview_files");
		DBCollection collectionB = db.getCollection("preview_files.files");
		collectionB.ensureIndex("filename");
		GridFSDBFile fff = gridfs_volumes.findOne(volume.getURL().getDetail());
		if (fff != null && fff.getLength() <= 0) {
			System.out.println("Found Zero-Size File.");
			gridfs_volumes.remove(fff);
			fff = null;
		}

		if (fff != null) {
			return DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED;
		} else {
			try {
				saveVolumeFile(gridfs_volumes, gridfs_preview, volume, threeDvolumeInputStream, optFileSize);
				return DatabaseStorageResult.STORED_IN_DB;
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
			}
		}
	}

	public void setDefaultDBE(String defaultDBE) {
		this.defaultDBE = defaultDBE;
	}

	public String getDefaultDBE() {
		return defaultDBE;
	}

	public String getDefaultHost() {
		return defaultHost;
	}

	public void setDefaultHost(String defaultHost) {
		this.defaultHost = defaultHost;
	}

	public String getDefaultLogin() {
		return defaultLogin;
	}

	public void setDefaultLogin(String defaultLogin) {
		this.defaultLogin = defaultLogin;
	}

	public String getDefaultPass() {
		return defaultPass;
	}

	public void setDefaultPass(String defaultPass) {
		this.defaultPass = defaultPass;
	}

	public byte[] getPreviewData(final String md5) {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		try {
			processDB(new RunnableOnDB() {
				private DB db;

				@Override
				public void run() {
					GridFS gridfs_preview_images = new GridFS(db, "preview_files");
					GridFSDBFile fff = gridfs_preview_images.findOne(md5);
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

	public ArrayList<ExperimentHeader> getExperimentList() {
		final ArrayList<ExperimentHeader> res = new ArrayList<ExperimentHeader>();
		try {
			processDB(new RunnableOnDB() {
				private DB db;

				@Override
				public void run() {
					for (DBObject header : db.getCollection("experiments").find()) {
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
					DBRef dbr = new DBRef(db, "experiments", new ObjectId(header.getExcelfileid()));
					DBObject expref = dbr.fetch();
					if (expref != null) {
						BasicDBList l = (BasicDBList) expref.get("substance_ids");
						for (Object o : l) {
							DBRef subr = new DBRef(db, "substances", new ObjectId(o.toString()));
							DBObject substance = subr.fetch();
							if (substance != null) {
								Substance3D s3d = new Substance3D(substance.toMap());
								experiment.add(s3d);
								BasicDBList condList = (BasicDBList) substance.get("conditions");
								if (condList != null)
									for (Object co : condList) {
										DBObject cond = (DBObject) co;
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
												BasicDBList imgList = (BasicDBList) sam.get("images");
												if (imgList != null) {
													for (Object m : imgList) {
														DBObject img = (DBObject) m;
														Map<Object, Object> map = img.toMap();
														String fn = (String) map.get("filename");
														String md5 = (String) map.get("md5sum");
														if (md5 != null && fn != null) {
															map.put("filename", "mongo://" + md5 + "/" + fn);
														}

														ImageData image = new ImageData(sample, map);
														sample.add(image);
													}
												}
												// volumes
												BasicDBList volList = (BasicDBList) sam.get("volumes");
												if (volList != null) {
													for (Object v : volList) {
														DBObject vol = (DBObject) v;
														VolumeData volume = new VolumeData(sample, vol.toMap());
														volume.getURL().setPrefix(MongoDBhandler.PREFIX);
														sample.add(volume);
													}
												}
											}
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
				DBRef dbr = new DBRef(db, "experiments", id);
				DBObject expref = dbr.fetch();
				if (expref != null) {
					expref.put("experimenttype", experimentType);
					db.getCollection("experiments").save(expref);
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
				DBRef dbr = new DBRef(db, "experiments", id);
				DBObject expref = dbr.fetch();
				if (expref != null) {
					HashMap<String, Object> attributes = new HashMap<String, Object>();
					header.fillAttributeMap(attributes, 0);
					for (String key : attributes.keySet()) {
						if (attributes.get(key) != null)
							expref.put(key, attributes.get(key));
					}

					db.getCollection("experiments").save(expref);
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

	private void updateExperimentSize(DB db, ExperimentInterface experiment) {
		boolean recalcSize = false;
		try {
			String sz = experiment.getHeader().getSizekb();
			double szd = Double.parseDouble(sz);
			if (szd <= 0) {
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
					IOurl url = null;
					if (nmd instanceof ImageData) {
						url = ((ImageData) nmd).getURL();
					} else if (nmd instanceof VolumeData) {
						url = ((VolumeData) nmd).getURL();
					} else if (nmd instanceof NetworkData) {
						url = ((NetworkData) nmd).getURL();
					}
					if (url != null) {
						String md5 = url.getDetail();
						Collection<String> gridFSnames = new ArrayList<String>();
						gridFSnames.add("preview_files");
						gridFSnames.add("volumes");
						gridFSnames.add("images");
						gridFSnames.add("annotations");
						for (String s : gridFSnames) {
							GridFS gridfs = new GridFS(db, s);
							GridFSDBFile file = gridfs.findOne(md5);
							if (file != null) {
								newSize.addLong(file.getLength());
							}
						}
					}
				}
				experiment.getHeader().setSizekb(newSize.getLong() / 1024 + "");
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}

	// public BufferedImage getImage(String login, String pass, final String md5)
	// {
	// final ThreadSafeOptions tso = new ThreadSafeOptions();
	// try {
	// processDB(new RunnableOnDB() {
	// private DB db;
	//
	// @Override
	// public void run() {
	// BufferedImage result = null;
	// GridFS gridfs_images = new GridFS(db, "images");
	// GridFSDBFile fff = gridfs_images.findOne(md5);
	// if (fff != null) {
	// try {
	// result = ImageIO.read(fff.getInputStream());
	// } catch (IOException e) {
	// ErrorMsg.addErrorMessage(e);
	// }
	// }
	// tso.setParam(0, result);
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
	// return (BufferedImage) tso.getParam(0, null);
	// }
}

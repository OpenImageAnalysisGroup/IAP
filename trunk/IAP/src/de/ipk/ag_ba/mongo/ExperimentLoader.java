package de.ipk.ag_ba.mongo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.bson.types.ObjectId;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.gui.picture_gui.MongoCollection;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleAverage;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public class ExperimentLoader implements RunnableOnDB {
	private final ArrayList<DBObject> optDBPbjectsOfSubstances;
	private final ExperimentHeaderInterface header;
	private final BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider;
	private final boolean interactiveCalculateExperimentSize;
	private final ExperimentInterface experiment;
	private final ArrayList<DBObject> optDBPbjectsOfConditions;
	private DB db;
	private final boolean ensureIndex;
	private final MongoDB mongoDB;
	private final MongoDBhandler mh;
	
	public ExperimentLoader(
			MongoDB mongoDB,
			MongoDBhandler mh, boolean ensureIndex,
			ArrayList<DBObject> optDBPbjectsOfSubstances, ExperimentHeaderInterface header,
			BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider, boolean interactiveCalculateExperimentSize, ExperimentInterface experiment,
			ArrayList<DBObject> optDBPbjectsOfConditions) {
		this.mongoDB = mongoDB;
		this.mh = mh;
		this.ensureIndex = ensureIndex;
		this.optDBPbjectsOfSubstances = optDBPbjectsOfSubstances;
		this.header = header;
		this.optStatusProvider = optStatusProvider;
		this.interactiveCalculateExperimentSize = interactiveCalculateExperimentSize;
		this.experiment = experiment;
		this.optDBPbjectsOfConditions = optDBPbjectsOfConditions;
	}
	
	@Override
	public void run() {
		// synchronized (db) {
		
		final DBCollection collCond = db.getCollection("conditions");
		if (MongoDB.getEnsureIndex())
			collCond.ensureIndex("_id");
		final DBCollection collSubst = db.getCollection("substances");
		if (MongoDB.getEnsureIndex())
			collSubst.ensureIndex("_id");
		
		DBRef dbr = new DBRef(db, MongoExperimentCollections.EXPERIMENTS.toString(), new ObjectId(header.getDatabaseId()));
		
		boolean quickLoaded = false;
		
		GridFS gridfsExperimentStorage = new GridFS(db, "gridfsExperimentStorage");
		GridFSDBFile fffMain = gridfsExperimentStorage.findOne(header.getDatabaseId());
		if (fffMain != null) {
			try {
				System.out.println(SystemAnalysis.getCurrentTime() + ">TRY QUICK XML BIN LOADING OF " + header.getDatabaseId() + ", "
						+ header.getExperimentName());
				InputStream in = fffMain.getInputStream();
				Experiment e = Experiment.loadFromXmlBinInputStream(in, null);
				experiment.addAll(e);
				quickLoaded = true;
				System.out.println(SystemAnalysis.getCurrentTime() + ">QUICK XML BIN LOADING OF " + header.getDatabaseId() + ", "
						+ header.getExperimentName() + " SUCCEDED!");
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR DURING QUICK XML LOADING OF " + header.getDatabaseId() + ", "
						+ header.getExperimentName() + ": " + e.getMessage());
				e.printStackTrace();
				MongoDB.saveSystemErrorMessage(
						"Quick XML BIN Loading of experiment "
								+ header.getDatabaseId()
								+ ", experiment "
								+ header.getExperimentName()
								+ " failed!", e);
			}
		}
		experiment.setHeader(header);
		
		if (!quickLoaded) {
			int nThreads = 10;
			ExecutorService executor = Executors.newFixedThreadPool(nThreads);
			
			DBObject expref = dbr.fetch();
			if (expref != null) {
				BasicDBList subList = (BasicDBList) expref.get("substances");
				if (subList != null) {
					int n = subList.size();
					int idxS = 0;
					for (Object co : subList) {
						DBObject substance = (DBObject) co;
						if (optDBPbjectsOfSubstances != null)
							optDBPbjectsOfSubstances.add(substance);
						idxS++;
						processSubstance(db, experiment, substance, collCond, optStatusProvider, 100d / subList.size(), optDBPbjectsOfConditions, idxS, n);
					}
				}
				if (MongoDB.getEnsureIndex())
					db.getCollection("substances").ensureIndex("_id");
				BasicDBList l = (BasicDBList) expref.get("substance_ids");
				if (l != null) {
					final ThreadSafeOptions tsoIdxS = new ThreadSafeOptions();
					final int n = l.size();
					for (Object o : l) {
						if (o == null)
							continue;
						DBRef subr = new DBRef(db, "substances", new ObjectId(o.toString()));
						if (subr != null) {
							final DBObject substance = subr.fetch();
							if (substance != null) {
								if (optDBPbjectsOfSubstances != null)
									optDBPbjectsOfSubstances.add(substance);
								final int lss = l.size();
								Runnable r = new Runnable() {
									@Override
									public void run() {
										tsoIdxS.addInt(1);
										processSubstance(db, experiment, substance, collCond, optStatusProvider,
												100d / lss, optDBPbjectsOfConditions,
												tsoIdxS.getInt(), n);
									}
								};
								executor.execute(r);
							}
						}
					}
				}
			}
			
			executor.shutdown();
			while (!executor.isTerminated()) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					MongoDB.saveSystemErrorMessage("InterruptedException during experiment loading concurrency.", e);
				}
			}
		}
		
		experiment.setHeader(header);
		
		int numberOfImagesAndVolumes = Substance3D.countMeasurementValues(experiment, new MeasurementNodeType[] {
				MeasurementNodeType.IMAGE, MeasurementNodeType.VOLUME });
		experiment.getHeader().setNumberOfFiles(numberOfImagesAndVolumes);
		boolean sortSubstances = false;
		if (sortSubstances)
			((Experiment) experiment).sortSubstances();
		if (numberOfImagesAndVolumes > 0 && interactiveCalculateExperimentSize) {
			mongoDB.updateExperimentSize(db, experiment, optStatusProvider);
		}
		// }
	}
	
	void processSubstance(DB db, ExperimentInterface experiment, DBObject substance,
			DBCollection collCond,
			BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider, double smallProgressStep,
			ArrayList<DBObject> optDBObjectsConditions, int idxS, int n) {
		@SuppressWarnings("unchecked")
		Substance3D s3d = new Substance3D(substance.toMap());
		if (s3d.getName() != null && s3d.getName().contains("..")) {
			s3d.setName(StringManipulationTools.stringReplace(s3d.getName(), "..", "."));
		}
		boolean speedupLoading = IAPmain.getRunMode() == IAPrunMode.SWING_MAIN || IAPmain.getRunMode() == IAPrunMode.SWING_APPLET;
		speedupLoading = false;
		if (speedupLoading) {
			if (s3d.getName().contains("histogram")) {
				// System.out.println("Skip substance loading of substance " + s3d.getName());
				if (optStatusProvider != null)
					optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep);
				return;
			}
			if (s3d.getName().contains("histogram.bin") && s3d.getName().contains("section")) {
				// System.out.println("Skip substance loading of substance " + s3d.getName());
				if (optStatusProvider != null)
					optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep);
				return;
			}
			if (experiment.getName().startsWith("Unit Test "))
				if (s3d.getName().contains("histogram")
						|| s3d.getName().contains(".angles")
						// || s3d.getName().contains(".hsv.")
						|| !s3d.getName().contains(".all.")
						// || !s3d.getName().startsWith("corr.")
						|| s3d.getName().contains(".lab.")
						// || s3d.getName().contains(".percent.")
						|| s3d.getName().contains("RESULT_")) {
					// System.out.println("Skip substance loading of substance " + s3d.getName());
					if (optStatusProvider != null)
						optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep);
					return;
				}
		}
		
		if (optStatusProvider != null)
			optStatusProvider.setCurrentStatusText1("Load subset " + idxS + "/" + n + "<br><font color='gray'><small>(" + s3d.getName() + ")</small></font>");
		synchronized (experiment) {
			boolean add = true;
			for (SubstanceInterface so : experiment)
				if (so.equals(s3d)) {
					so.addAll(s3d);
					s3d = (Substance3D) so;
					add = false;
				}
			if (add)
				experiment.add(s3d);
		}
		BasicDBList condList = (BasicDBList) substance.get("conditions");
		if (condList != null) {
			int nc = condList.size();
			for (Object co : condList) {
				DBObject cond = (DBObject) co;
				if (optDBObjectsConditions != null)
					optDBObjectsConditions.add(cond);
				processCondition(s3d, cond, optStatusProvider, nc);
			}
		}
		if (ensureIndex)
			collCond.ensureIndex("_id");
		BasicDBList l = (BasicDBList) substance.get("condition_ids");
		if (l != null) {
			double max = l.size();
			
			BasicDBList ll = new BasicDBList();
			for (Object o : l)
				ll.add(new ObjectId(o + ""));
			DBCursor condL = collCond.find(
					new BasicDBObject("_id", new BasicDBObject("$in", ll))
					).hint(new BasicDBObject("_id", 1)).batchSize(l.size() % 1000);
			for (DBObject cond : condL) {
				try {
					if (optDBObjectsConditions != null)
						optDBObjectsConditions.add(cond);
					processCondition(s3d, cond, optStatusProvider, max);
					if (optStatusProvider != null)
						optStatusProvider.setCurrentStatusValueFineAdd(smallProgressStep * 1 / max);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void processCondition(Substance3D s3d, DBObject cond, BackgroundTaskStatusProviderSupportingExternalCall optStatusProvider, double max) {
		Condition3D condition = new Condition3D(s3d, cond.toMap());
		
		s3d.add(condition);
		BasicDBList sampList = (BasicDBList) cond.get("samples");
		if (sampList != null) {
			// if (optStatusProvider != null)
			// optStatusProvider.setCurrentStatusText2("(n_c=" + (int) max + ", n_sa=" + sampList.size() + ")");
			
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
	
	@Override
	public void setDB(DB db) {
		this.db = db;
	}
}
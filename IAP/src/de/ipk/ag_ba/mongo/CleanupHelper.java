package de.ipk.ag_ba.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;
import org.bson.types.ObjectId;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class CleanupHelper implements RunnableOnDB {
	private final BackgroundTaskStatusProviderSupportingExternalCall status;
	private final StringBuilder res;
	private DB db;
	private final MongoDB mongoDB;
	
	public CleanupHelper(MongoDB mongoDB, BackgroundTaskStatusProviderSupportingExternalCall status, StringBuilder res) {
		this.mongoDB = mongoDB;
		this.status = status;
		this.res = res;
	}
	
	@Override
	public void run() {
		String msg = "Clean-up: Create inventory... // " + SystemAnalysis.getCurrentTime();
		System.out.println(msg);
		MongoDB.saveSystemMessage(msg);
		res.append("Clean-up: Create inventory... // " + SystemAnalysis.getCurrentTime() + "<br>");
		status.setCurrentStatusText1("Create inventory");
		status.setCurrentStatusValueFine(-1);
		long numberOfBinaryFilesInDatabaseOrExtern = 0;
		long numberOfBinaryFilesExternalyStored = 0;
		HashMap<String, Integer> fs2cnt = new HashMap<String, Integer>();
		for (String mgfs : MongoGridFS.getFileCollectionsInclPreview()) {
			GridFS gridfs = new GridFS(db, mgfs);
			int cnt = gridfs.getFileList().count();
			numberOfBinaryFilesInDatabaseOrExtern += cnt;
			
			for (VirtualFileSystem vfs : mongoDB.vfs_file_storage) {
				if (vfs instanceof VirtualFileSystemVFS2) {
					VirtualFileSystemVFS2 vf = (VirtualFileSystemVFS2) vfs;
					int c;
					try {
						c = vf.countFiles(mgfs);
						cnt += c;
						numberOfBinaryFilesExternalyStored += c;
					} catch (Exception e) {
						MongoDB.saveSystemErrorMessage("Could not count files in VFS " + vfs.getTargetName() + ", sub-dir " + mgfs, e);
					}
				}
			}
			
			fs2cnt.put(mgfs, cnt);
		}
		
		msg = "Clean-up: Stored binary files: " + numberOfBinaryFilesInDatabaseOrExtern + " // "
				+ " from these " + numberOfBinaryFilesExternalyStored + " are stored externally in VFS // "
				+ SystemAnalysis.getCurrentTime();
		System.out.println(msg);
		MongoDB.saveSystemMessage(msg);
		res.append("Clean-up: Stored binary files: " + numberOfBinaryFilesInDatabaseOrExtern
				+ " // from these " + numberOfBinaryFilesExternalyStored + " are stored in VFS // "
				+ SystemAnalysis.getCurrentTime() + "<br>");
		status.setCurrentStatusText2(
				(numberOfBinaryFilesInDatabaseOrExtern - numberOfBinaryFilesExternalyStored)
						+ " files in DB, "
						+ numberOfBinaryFilesInDatabaseOrExtern + " in VFS");
		
		ArrayList<ExperimentHeaderInterface> el = mongoDB.getExperimentList(null);
		final HashSet<String> linkedHashes = new HashSet<String>();
		final double smallStep = 100d / el.size();
		final HashSet<String> dbIdsOfSubstances = new HashSet<String>();
		final HashSet<String> dbIdsOfConditions = new HashSet<String>();
		status.setCurrentStatusText2("Count Substance IDs");
		double oldStatus = status.getCurrentStatusValueFine();
		status.setCurrentStatusValue(-1);
		{
			DBCollection substances = db.getCollection("substances");
			long nn = 0, max = substances.count();
			status.setCurrentStatusText2("Read list of substance IDs (" + max + ")");
			DBCursor subCur = substances.find(new BasicDBObject(), new BasicDBObject("_id", 1))
					.hint(new BasicDBObject("_id", 1)).batchSize(2000);
			while (subCur.hasNext()) {
				DBObject subO = subCur.next();
				dbIdsOfSubstances.add(subO.get("_id") + "");
				nn++;
				if (nn % 500 == 0) {
					status.setCurrentStatusText2("Read list of substances (" + nn + "/" + max + ")");
					status.setCurrentStatusValueFine(100d * nn / max);
				}
			}
			subCur.close();
			status.setCurrentStatusText2("Read list of substance IDs (" + nn + ")");
		}
		status.setCurrentStatusText1(status.getCurrentStatusMessage2());
		// status.setCurrentStatusText2("Count condition IDs");
		{
			DBCollection conditions = db.getCollection("conditions");
			// if (MongoDB.getEnsureIndex())
			// conditions.ensureIndex("_id");
			long nn = 0;// , max = conditions.count();
			status.setCurrentStatusText2("Read list of condition IDs");// (" + max + ")");
			DBCursor condCur = conditions
					.find(new BasicDBObject(), new BasicDBObject("_id", 1)).batchSize(50);// .hint(new BasicDBObject("_id", 1))
			// .batchSize(10000);
			while (condCur.hasNext()) {
				ObjectId condO = (ObjectId) condCur.next().get("_id");
				dbIdsOfConditions.add(condO.toString());
				nn++;
				if (nn % 50 == 0) {
					status.setCurrentStatusText2("Read list of condition IDs (" + nn /* + "/" + max */+ ")");
					status.setCurrentStatusValueFine(-1);// 100d * nn / max);
				}
				
			}
			condCur.close();
			status.setCurrentStatusText2("Read list of condition IDs (" + nn /* + "/" + max */+ ")");
		}
		status.setCurrentStatusText1("Create inventory");
		status.setCurrentStatusValueFine(oldStatus);
		final ThreadSafeOptions ii = new ThreadSafeOptions();
		ArrayList<ExperimentHeaderInterface> todo = new ArrayList<ExperimentHeaderInterface>();
		for (ExperimentHeaderInterface ehii : el) {
			todo.add(ehii);
			for (ExperimentHeaderInterface old : ehii.getHistory().values())
				todo.add(old);
		}
		final int nn = todo.size();
		
		final RunnableProcessingDBid visitSubstance = new RunnableProcessingDBid() {
			@Override
			public void processDBid(String id) {
				synchronized (dbIdsOfSubstances) {
					dbIdsOfSubstances.remove(id);
				}
			}
		};
		final RunnableProcessingDBid visitCondition = new RunnableProcessingDBid() {
			@Override
			public void processDBid(String id) {
				synchronized (dbIdsOfConditions) {
					dbIdsOfConditions.remove(id);
				}
			}
		};
		final RunnableProcessingBinaryMeasurement visitBinaryMeasurement = new RunnableProcessingBinaryMeasurement() {
			@Override
			public void processBinaryMeasurement(BinaryMeasurement bm) {
				synchronized (linkedHashes) {
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
			}
		};
		
		int nThreads = 2;
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		final ThreadSafeOptions fc = new ThreadSafeOptions();
		final ArrayList<ThreadSafeOptions> invalids = new ArrayList<ThreadSafeOptions>();
		status.setCurrentStatusValueFine(0);
		for (final ExperimentHeaderInterface ehii : todo) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					boolean error = false;
					synchronized (invalids) {
						error = invalids.size() > 0;
					}
					if (error)
						return;
					ThreadSafeOptions invalid = new ThreadSafeOptions();
					try {
						invalid.setBval(0, false);
						final int r = ii.addInt(1);
						status.setCurrentStatusText1("Analyze " + ehii.getExperimentName()
								+ " (" + r + "/" + nn + ")");
						BackgroundTaskConsoleLogger ss = new BackgroundTaskConsoleLogger() {
							
							@Override
							public void setCurrentStatusText1(String st) {
								status.setCurrentStatusText1("<html>" +
										"Analyze " + ehii.getExperimentName()
										+ " (" + r + "/" + nn + "):<br>" + st);
							}
							
							@Override
							public void setCurrentStatusText2(String st) {
								status.setCurrentStatusText2(st);
							}
							
						};
						ss.setEnabled(false);
						long a = System.currentTimeMillis();
						CleanupHelper.this.mongoDB.visitExperiment(
								ehii,
								ss, null, true, visitSubstance, visitCondition, visitBinaryMeasurement,
								invalid);
						long b = System.currentTimeMillis();
						fc.addInt(1);
						String msg = "Visiting " + ehii.getExperimentname() + " (" + fc.getInt() + " finished) took " +
								SystemAnalysis.getWaitTime(b - a);
						System.out.println(SystemAnalysis.getCurrentTime() + ">" + msg);
						status.setCurrentStatusText2(msg);
						status.setCurrentStatusValueFineAdd(smallStep);
						if (invalid.getBval(0, false)) {
							invalid.setParam(0,
									ehii.getDatabaseId() + ": " + ehii.getExperimentName() + " // " + ehii.getCoordinator() + " // " + ehii.getExperimentType());
							synchronized (invalids) {
								invalids.add(invalid);
							}
						}
					} catch (Exception e) {
						invalid.setBval(1, true);
						invalid.setParam(0, ehii.getDatabaseId());
						synchronized (invalids) {
							invalids.add(invalid);
						}
					}
				}
			});
		} // experiments
		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				MongoDB.saveSystemErrorMessage("InterruptedException during experiment loading concurrency.", e);
			}
		}
		
		for (ThreadSafeOptions inv : invalids) {
			String id = (String) inv.getParam(0, "");
			try {
				System.out.println(SystemAnalysis.getCurrentTime() + "Clean-up: detected incorrectly read experiment " + id);
				MongoDB.saveSystemMessage(SystemAnalysis.getCurrentTime()
						+ ">Clean-up: Detected invalid experiment (some sub-documents could not be loaded, further processing has been stopped): " + id);
				// MongoDB.getDefaultCloud().deleteExperiment(id);
			} catch (Exception e) {
				// MongoDB.saveSystemErrorMessage("Could not delete an invalid experiment " + id, e);
			}
		}
		
		if (invalids.size() > 0)
		{
			msg = "Clean-up: Interrupted processing, as at least 1 experiment could not be processed correctly. " + " // "
					+ SystemAnalysis.getCurrentTime();
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			
			res.append(msg);
			status.setCurrentStatusText1("Processing Interrupted");
			status.setCurrentStatusText2("(" + invalids.size() + " experiment(s) could not be processed)");
			status.setCurrentStatusValueFine(100d);
			return;
		}
		
		{
			DBCollection substances = db.getCollection("substances");
			status.setCurrentStatusText1("Count Substance IDs");
			// long cnt = substances.count();
			long max = dbIdsOfSubstances.size();
			msg = SystemAnalysis.getCurrentTime() + ">Remove stale substances: " + max;// + "/" + cnt;
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			status.setCurrentStatusText1("Remove Stale Substances: " + max);// + "/" + cnt);
			int n = 0;
			for (String subID : dbIdsOfSubstances) {
				n++;
				substances.remove(new BasicDBObject("_id", new ObjectId(subID)), WriteConcern.NONE);
				status.setCurrentStatusValueFine(100d / max * n);
				status.setCurrentStatusText2(n + "/" + max);
			}
			msg = SystemAnalysis.getCurrentTime() + ">Clean-up: Removed " + dbIdsOfSubstances.size() + " substance documents";
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
		}
		executor = Executors.newFixedThreadPool(3);
		{
			final ArrayList<String> ids = new ArrayList<String>();
			final ThreadSafeOptions n = new ThreadSafeOptions();
			final long max = dbIdsOfConditions.size();
			final DBCollection conditions = db.getCollection("conditions");
			// status.setCurrentStatusText1("Count Condition IDs");
			// final long cnt = conditions.count();
			msg = SystemAnalysis.getCurrentTime() + ">Clean-up: Remove stale conditions: " + dbIdsOfConditions.size();// + "/" + cnt;
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			status.setCurrentStatusText1("Remove Stale Conditions: " + dbIdsOfConditions.size());// + "/" + cnt);
			executor.submit(new Runnable() {
				@Override
				public void run() {
					for (String condID : dbIdsOfConditions) {
						ids.add(condID);
						if (ids.size() >= 100) {
							final ArrayList<String> toBeDeleted = new ArrayList<String>(ids);
							ids.clear();
							n.addLong(100);
							BasicDBList list = new BasicDBList();
							synchronized (toBeDeleted) {
								for (String coID : toBeDeleted)
									list.add(new ObjectId(coID));
								toBeDeleted.clear();
							}
							synchronized (conditions) {
								WriteResult rs = conditions.remove(
										new BasicDBObject("_id", new BasicDBObject("$in", list)), WriteConcern.SAFE);
								if (!rs.getLastError(WriteConcern.SAFE).ok())
									throw new RuntimeException("Remove condtions error: " + rs.getError());
							}
							status.setCurrentStatusValueFine(100d / max * n.getLong());
							status.setCurrentStatusText2(n.getLong() + "/" + max + " (" + (int) (100d / max * n.getLong()) + "%)");
						}
					}
					if (ids.size() > 0) {
						BasicDBList list = new BasicDBList();
						synchronized (ids) {
							for (String coID : ids)
								list.add(new ObjectId(coID));
							ids.clear();
						}
						WriteResult wr = conditions.remove(
								new BasicDBObject("_id", new BasicDBObject("$in", list)),
								WriteConcern.NONE);
						String err = wr.getError();
						if (err != null && err.length() > 0)
							System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: deleting a condition document: " + err + " // "
									+ SystemAnalysis.getCurrentTime());
						n.addLong(list.size());
						status.setCurrentStatusValueFine(100d / max * n.getLong());
						status.setCurrentStatusText2(n.getLong() + "/" + max);
					}
					
					status.setCurrentStatusValueFine(100d / max * n.getLong());
					status.setCurrentStatusText2(n + "/" + max);
					String msg = SystemAnalysis.getCurrentTime() + ">Clean-Up: Removed " + dbIdsOfConditions.size() + " condition documents";
					System.out.println(msg);
					MongoDB.saveSystemMessage(msg);
				}
			});
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				MongoDB.saveSystemErrorMessage("InterruptedException during reorganization.", e);
			}
		}
		
		if (linkedHashes.size() >= 0) {
			long freeAll = 0;
			msg = "Clean-up: Linked binary files: " + linkedHashes.size() + " // " + SystemAnalysis.getCurrentTime();
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			res.append("Clean-up: Linked binary files: " + linkedHashes.size() + " // " + SystemAnalysis.getCurrentTime() + "<br>");
			status.setCurrentStatusText1("Linked files: " + linkedHashes.size());
			status.setCurrentStatusText2("");
			status.setCurrentStatusValueFine(0);
			
			for (String mgfs : MongoGridFS.getFileCollectionsInclPreview()) {
				final GridFS gridfs = new GridFS(db, mgfs);
				int cnt = gridfs.getFileList().count();
				if (cnt == fs2cnt.get(mgfs)) {
					ArrayList<GridFSDBFile> toBeRemoved = new ArrayList<GridFSDBFile>();
					// no changes in between
					DBCursor fl = gridfs.getFileList(new BasicDBObject(), new BasicDBObject());
					while (fl.hasNext()) {
						DBObject dbo = fl.next();
						GridFSDBFile f = (GridFSDBFile) dbo;
						String md5 = f.getFilename();
						if (!linkedHashes.contains(md5)) {
							toBeRemoved.add(f);
							status.setCurrentStatusText1(mgfs + " linked files: " + linkedHashes.size() + ", not linked: " + toBeRemoved.size());
						}
					}
					msg = "Clean-up: Binary files that are not linked (" + mgfs + "): " + toBeRemoved.size() + " // "
							+ SystemAnalysis.getCurrentTime();
					System.out.println(msg);
					MongoDB.saveSystemMessage(msg);
					res.append("Clean-up: Binary files that are not linked (" + mgfs + "): " + toBeRemoved.size() + " // "
							+ SystemAnalysis.getCurrentTime() + "<br>");
					final ThreadSafeOptions free = new ThreadSafeOptions();
					final ThreadSafeOptions fIdx = new ThreadSafeOptions();
					final int fN = toBeRemoved.size();
					mongoDB.removeFilesFromGridFS(status, mgfs, gridfs, toBeRemoved, free, db);
					msg = "Clean-up: Deleted MB (" + mgfs + "): " + free.getLong() / 1024 / 1024 + " // "
							+ SystemAnalysis.getCurrentTime();
					System.out.println(msg);
					MongoDB.saveSystemMessage(msg);
					res.append("Clean-up: Deleted MB (" + mgfs + "): " + free.getLong() / 1024 / 1024 + " // "
							+ SystemAnalysis.getCurrentTime() + "<br>");
					
					freeAll += free.getLong();
				}
			}
			
			msg = "Clean-up: Overall deleted MB: " + freeAll / 1024 / 1024 + " // "
					+ SystemAnalysis.getCurrentTime();
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			
			res.append("Clean-up: Overall deleted MB: " + freeAll / 1024 / 1024 + " // "
					+ SystemAnalysis.getCurrentTime() + "<br>");
			status.setCurrentStatusText1("Deleted MB: " + (freeAll / 1024 / 1024));
			status.setCurrentStatusValueFine(100d);
		}
	}
	
	@Override
	public void setDB(DB db) {
		this.db = db;
	}
}
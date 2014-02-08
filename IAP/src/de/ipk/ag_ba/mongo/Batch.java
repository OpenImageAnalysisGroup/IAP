package de.ipk.ag_ba.mongo;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk.ag_ba.server.task_management.CloudComputingService;
import de.ipk.ag_ba.server.task_management.CloudHost;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

public class Batch {
	
	private final MongoDB mongoDB;
	
	public Batch(MongoDB mongoDB) {
		this.mongoDB = mongoDB;
	}
	
	public boolean claim(final BatchCmd batch, final CloudAnalysisStatus starting, final boolean requireOwnership) {
		// try to claim a batch cmd
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setBval(0, false);
		try {
			mongoDB.processDB(new RunnableOnDB() {
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
	
	public BatchCmd delete(final BatchCmd batch) {
		return delete(batch, null);
	}
	
	public BatchCmd delete(final BatchCmd batch, final ThreadSafeOptions returnValueSuccess) {
		if (returnValueSuccess != null)
			returnValueSuccess.setBval(0, false);
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		// try to claim a batch cmd
		try {
			mongoDB.processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					DBCollection collection = db.getCollection("schedule");
					collection.setObjectClass(BatchCmd.class);
					DBObject dbo = new BasicDBObject();
					dbo.put("_id", batch.get("_id"));
					BatchCmd res = (BatchCmd) collection.findOne(dbo);
					WriteResult wr = collection.remove(dbo);
					if (wr.getN() > 0)
						if (returnValueSuccess != null)
							returnValueSuccess.setBval(0, true);
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
	
	/**
	 * @param includeArchived
	 * @return Number of deleted compute jobs.
	 * @throws Exception
	 */
	public long deleteAll(final boolean includeArchived) throws Exception {
		final ThreadSafeOptions res = new ThreadSafeOptions();
		mongoDB.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (!includeArchived) {
					Collection<BatchCmd> jl = getAll();
					for (BatchCmd c : jl)
						if (c.getRunStatus() != CloudAnalysisStatus.ARCHIVED)
							delete(c);
					
				} else {
					res.setLong(db.getCollection("schedule").count());
					db.getCollection("schedule").drop();
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		return res.getLong();
	}
	
	/**
	 * add task to "schedule" collection
	 * 
	 * @throws Exception
	 */
	public void enqueue(final BatchCmd cmd) throws Exception {
		// HashSet<String> targetIPs, String remoteCapableAnalysisActionClassName,
		// String remoteCapableAnalysisActionParams, String experimentInputMongoID) {
		
		mongoDB.processDB(new RunnableOnDB() {
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
	
	public Collection<BatchCmd> getAll() {
		final Collection<BatchCmd> res = new ArrayList<BatchCmd>();
		try {
			mongoDB.processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					// System.out.println("---");
					DBCollection collection = db.getCollection("schedule");
					collection.setObjectClass(BatchCmd.class);
					for (DBObject dbo : collection.find().sort(new BasicDBObject("submission", -1))) {
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
			return new ArrayList<BatchCmd>();
		}
		return res;
	}
	
	/**
	 * get list of host names, with not too old ping update time
	 */
	public ArrayList<CloudHost> getAvailableHosts(final long maxUpdate) throws Exception {
		final ArrayList<CloudHost> res = new ArrayList<CloudHost>();
		mongoDB.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				try {
					DBCollection dbc = db.getCollection("compute_hosts");
					dbc.setObjectClass(CloudHost.class);
					
					ArrayList<CloudHost> del = new ArrayList<CloudHost>();
					
					DBCursor cursor = dbc.find();
					final long curr = System.currentTimeMillis();
					while (cursor.hasNext()) {
						CloudHost h = (CloudHost) cursor.next();
						// System.out.println("age: " + (curr - h.getLastUpdateTime()) / 1000 + "s: " + h.getHostName());
						if (curr - h.getLastUpdateTime() < maxUpdate) {
							res.add(h);
						} else {
							long age = curr - h.getLastUpdateTime();
							if (age > 1000 * 60 * 2)
								del.add(h);
						}
					}
					for (CloudHost d : del)
						dbc.remove(d);
				} catch (Exception e) {
					MongoDB.saveSystemErrorMessage("batchGetAvailableHosts - error " + e.getMessage(), e);
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		
		return res;
	}
	
	public BatchCmd getCommand(final BatchCmd batch) {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		// try to claim a batch cmd
		try {
			mongoDB.processDB(new RunnableOnDB() {
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
	
	public Collection<BatchCmd> getScheduledForStart(final int maxTasks) {
		final ArrayList<BatchCmd> res = new ArrayList<BatchCmd>();
		try {
			mongoDB.processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					String hostName;
					try {
						DBCollection collection = db.getCollection("schedule");
						collection.setObjectClass(BatchCmd.class);
						for (BasicDBObject rm : BatchCmd.getRunstatusMatchers(CloudAnalysisStatus.IN_PROGRESS)) {
							rm.append("lastupdate", new BasicDBObject("$lt", System.currentTimeMillis() - 30 * 60000));
							// after 30 minutes with no update, the status of the batch is changed from in_progress to scheduled
							for (DBObject dbo : collection.find(rm)) {
								BatchCmd batch = (BatchCmd) dbo;
								batch.setRunStatus(CloudAnalysisStatus.SCHEDULED);
								BasicDBObject bb = new BasicDBObject("_id", batch.get("_id"));
								bb.append("lastupdate", batch.get("lastupdate"));
								collection.update(bb, batch);
							}
						}
						
						hostName = SystemAnalysisExt.getHostName();
						if (MongoDB.getEnsureIndex())
							collection.ensureIndex("release");
						boolean added = false;
						int addCnt = 0;
						for (DBObject rm : BatchCmd.getRunstatusMatchers(CloudAnalysisStatus.SCHEDULED)) {
							if (addCnt < maxTasks)
								for (DBObject dbo : collection.find(rm)
										.sort(new BasicDBObject("part_idx", 1))
										.sort(new BasicDBObject("part_cnt", 1))
										.sort(new BasicDBObject("submission", 1)).limit(maxTasks)) {
									BatchCmd batch = (BatchCmd) dbo;
									if (!batch.desiredOperatingSystemMatchesCurrentOperatingSystem())
										continue;
									if (batch.getCpuTargetUtilization() < maxTasks) {
										if (batch.getExperimentHeader() == null)
											continue;
										if (claim(batch, CloudAnalysisStatus.STARTING, false)) {
											res.add(batch);
											added = true;
											addCnt += batch.getCpuTargetUtilization();
											if (addCnt >= maxTasks)
												break;
										}
									}
								}
						}
						int claimed = 0;
						if (addCnt < maxTasks && claimed < maxTasks) {
							loop: for (DBObject dbo : collection.find()
									.sort(new BasicDBObject("part_idx", 1))
									.sort(new BasicDBObject("part_cnt", 1))
									.sort(new BasicDBObject("submission", 1))) {
								BatchCmd batch = (BatchCmd) dbo;
								if (!batch.desiredOperatingSystemMatchesCurrentOperatingSystem())
									continue;
								if (!added && batch.getCpuTargetUtilization() <= maxTasks)
									if (batch.get("lastupdate") == null || (System.currentTimeMillis() - batch.getLastUpdateTime() > 5 * 60000)) {
										// after 5 minutes tasks are taken away from other systems
										if (batch.getRunStatus() != CloudAnalysisStatus.FINISHED && batch.getRunStatus() != CloudAnalysisStatus.ARCHIVED) {
											if (batch.getExperimentHeader() == null) {
												System.out.println(SystemAnalysis.getCurrentTime() + ">Remove batch with NULL experiment ref: " + batch);
												collection.remove(batch);
												continue;
											}
											if (claim(batch, CloudAnalysisStatus.STARTING, false)) {
												claimed++;
												if (claimed >= maxTasks)
													break loop;
											}
										}
									}
							}
						}
						for (DBObject sm : BatchCmd.getRunstatusMatchers(CloudAnalysisStatus.STARTING)) {
							if (addCnt < maxTasks) {
								for (DBObject dbo : collection.find(sm)
										.sort(new BasicDBObject("part_idx", 1))
										.sort(new BasicDBObject("part_cnt", 1))
										.sort(new BasicDBObject("submission", 1))) {
									BatchCmd batch = (BatchCmd) dbo;
									if (batch.getExperimentHeader() == null)
										continue;
									if (!batch.desiredOperatingSystemMatchesCurrentOperatingSystem())
										continue;
									if (batch.getCpuTargetUtilization() <= maxTasks && hostName.equals("" + batch.getOwner())) {
										res.add(batch);
										addCnt += batch.getCpuTargetUtilization();
										if (addCnt >= maxTasks)
											added = true;
										break;
									}
								}
							}
						}
						if (addCnt < maxTasks && !added) {
							for (DBObject sm : BatchCmd.getRunstatusMatchers(CloudAnalysisStatus.FINISHED_INCOMPLETE)) {
								for (DBObject dbo : collection.find(sm)
										.sort(new BasicDBObject("part_idx", 1))
										.sort(new BasicDBObject("part_cnt", 1))
										.sort(new BasicDBObject("submission", 1))) {
									BatchCmd batch = (BatchCmd) dbo;
									if (!batch.desiredOperatingSystemMatchesCurrentOperatingSystem())
										continue;
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
	
	public CloudHost getUpdatedHostInfo(final CloudHost h) throws Exception {
		final ObjectRef r = new ObjectRef();
		mongoDB.processDB(new RunnableOnDB() {
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
	
	public static void pingHost(final MongoDB mongoDB, final String ip,
			final int blocksExecutedWithinLastMinute,
			final int pipelineExecutedWithinCurrentHour,
			final int tasksExecutedWithinLastMinute,
			final double progress,
			final String status3) throws Exception {
		mongoDB.processDB(new RunnableOnDB() {
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
					// System.out.println("INSERT: " + query);
					res = new CloudHost();
					add = true;
				}
				if (res != null) {
					res.updateTime();
					res.setHostName(ip);
					res.setClusterExecutionMode(IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE);
					res.setExecutionMode(IAPmain.getRunMode());
					res.setOperatingSystem(SystemAnalysis.getOperatingSystem());
					res.setBlocksExecutedWithinLastMinute(blocksExecutedWithinLastMinute);
					res.setPipelineExecutedWithinCurrentHour(pipelineExecutedWithinCurrentHour);
					res.setTasksExecutedWithinLastMinute(tasksExecutedWithinLastMinute);
					res.setTaskProgress(progress);
					res.setStatus3(status3);
					boolean monitor = !CloudComputingService.getInstance(mongoDB).getIsCalculationPossible();
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
					if (monitor) {
						double load = SystemAnalysisExt.getRealSystemCpuLoad();
						res.setHostInfo(
								(monitor ? "monitoring:<br>" : "") +
										SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis.getMemoryMB() + " MB, " +
										SystemAnalysisExt.getPhysicalMemoryInGB() + " GB<br>" + SystemAnalysis.getNumberOfCPUs() +
										"/" + SystemAnalysisExt.getNumberOfCpuPhysicalCores() + "/" + SystemAnalysisExt.getNumberOfCpuLogicalCores() + " CPUs" +
										(load > 0 ? " load "
												+ StringManipulationTools.formatNumber(load, "#.#") + "" : "") +
										(wl > 0 ? ", active: " + wl : "") + diskHistory.toString());
					}
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
}

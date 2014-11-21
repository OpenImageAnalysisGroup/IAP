/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LTftpHandler;
import de.ipk.ag_ba.server.task_management.minitoring_and_networking.SnapshotCreator;
import de.ipk_gatersleben.ag_nw.graffiti.UDPreceiveStructure;
import de.ipk_gatersleben.ag_nw.graffiti.services.network.BroadCastService;
import de.ipk_gatersleben.ag_pbi.mmd.MultimodalDataHandlingAddon;

/**
 * @author klukas
 */
public class CloudComputingService {
	
	static HashMap<MongoDB, CloudComputingService> instance =
			new HashMap<MongoDB, CloudComputingService>();
	
	boolean active = false;
	
	private final CloudTaskManager cloudTaskManager;
	
	private CloudComputingService() {
		cloudTaskManager = new CloudTaskManager();
	}
	
	public synchronized static CloudComputingService getInstance(MongoDB m) {
		if (!instance.containsKey(m))
			instance.put(m, new CloudComputingService());
		
		return instance.get(m);
	}
	
	private void checkStatus() {
		// todo
	}
	
	public String getStatusImageName() {
		checkStatus();
		if (active)
			return "img/ext/network-workgroup-power.png";
		else
			return "img/ext/network-workgroup.png";
	}
	
	public String getTaskNameEnableOrDisableActionText() {
		checkStatus();
		if (active)
			return "Shutdown This Node";
		else
			return "Join Compute Grid";
	}
	
	public static void main(String[] args) {
		if (args.length == 0 || !(args[0] + "").toLowerCase().startsWith("gui")) {
			for (String info : IAPmain.getMainInfoLines())
				System.out.println(info);
			SystemAnalysis.simulateHeadless = true;
			IAPmain.setRunMode(IAPrunMode.CLOUD_HOST);
		}
		if (args.length > 0 && args[0].toLowerCase().startsWith("info")) {
			SystemInfoExt info = new SystemInfoExt();
			System.out.println("Sockets        : " + info.getCpuSockets());
			System.out.println("Cores p. sock. : " + info.getPhysicalCoresPerSocket());
			System.out.println("Physical Cores : " + info.getCpuPhysicalCores());
			System.out.println("Logical Cores  : " + info.getCpuLogicalCores());
			System.out.println("Log./phys. core: " + info.getHyperThreadingFactor());
			System.out.println("CPUs (avail.)  : " + info.getCpuCountAvailable());
			System.out.println("Phys. mem. (GB): " + info.getPhysicalMemoryInGB());
			System.out.println("System load    : " + StringManipulationTools.formatNumber(info.getLoad(), "#.#"));
			System.exit(0);
		} else
			if ((args[0] + "").toLowerCase().startsWith("gui")) {
				{
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">'gui' - Start graphical GUI");
					ArrayList<String> argsl = new ArrayList<String>();
					if (args != null && args.length > 1)
						for (String s : args)
							argsl.add(s);
					de.ipk.ag_ba.gui.webstart.IAPmain.main(argsl.toArray(new String[] {}));
				}
				
			} else
				if ((args.length > 0 && args[0].toLowerCase().startsWith("watch")) ||
						(args.length > 1 && args[1].startsWith("watch")) ||
						(args.length > 0 && args[0].toLowerCase().startsWith("watch-cmd")) ||
						(args.length > 1 && args[1].startsWith("watch-cmd"))) {
					if ((args.length > 0 && args[0].toLowerCase().startsWith("watch-cmd")) ||
							(args.length > 1 && args[1].startsWith("watch-cmd"))) {
						System.out.println(": watch-cmd - monitoring the experiment data progress (auto-closing at 2 AM)");
						IAPservice.autoCloseAt(2);
					} else
						System.out.println(": watch - monitoring the experiment data progress");
					try {
						IAPservice.monitorExperimentDataProgress();
					} catch (Exception e) {
						MongoDB.saveSystemErrorMessage("Error monitoring experiment data progress.", e);
					} finally {
						System.exit(0);
					}
				} else
					if (args.length > 0 && args[0].toLowerCase().startsWith("close") ||
							(args.length > 1 && args[1].startsWith("close"))) {
						System.out.println(": close - auto-closing after finishing compute task - " + SystemAnalysis.getNumberOfCPUs());
						IAPmain.setRunMode(IAPrunMode.CLOUD_HOST_BATCH_MODE);
					} else
						if (args.length > 0 && args[0].toLowerCase().contains("full")) {
							System.out.println(": full - enabling full CPU utilization - " + SystemAnalysis.getNumberOfCPUs());
							SystemAnalysis.setUseFullCpuPower(true);
						} else
							if (args.length > 0 && args[0].contains("half")) {
								SystemAnalysis.setUseHalfCpuPower(true);
								System.out.println(": half - enabling half CPU utilization - " + SystemAnalysis.getNumberOfCPUs());
							} else
								if (args.length > 0) {
									try {
										Integer i = Integer.parseInt(args[0]);
										System.out.println(": " + args[0] + " - using " + i + " CPUs");
										SystemAnalysis.setUseCpu(i);
									} catch (Exception e) {
										if ((args[0] + "").toLowerCase().equalsIgnoreCase("clear")) {
											try {
												for (MongoDB m : MongoDB.getMongos()) {
													m.batch().deleteAll(false);
													System.out.println(":clear - cleared scheduled jobs in database " + m.getDatabaseName());
												}
												return;
											} catch (Exception e1) {
												e1.printStackTrace();
											}
										} else
											if ((args[0] + "").toLowerCase().startsWith("back") && !(args[0] + "").toLowerCase().startsWith("backup")) {
												BackupSupport sb = BackupSupport.getInstance();
												sb.makeBackup();
												System.exit(0);
											} else
												if ((args[0] + "").toLowerCase().startsWith("monitor")) {
													{
														System.out.println(SystemAnalysis.getCurrentTime()
																+ ">'monitor' - Report system info to cloud (join, but don't perform calculations)");
														for (MongoDB m : MongoDB.getMongos()) {
															CloudComputingService cc = CloudComputingService.getInstance(m);
															cc.setEnableCalculations(false);
														}
													}
													
												} else
													if ((args[0] + "").toLowerCase().startsWith("backup")) {
														{
															for (MongoDB m : MongoDB.getMongos()) {
																CloudComputingService cc = CloudComputingService.getInstance(m);
																cc.setEnableCalculations(false);
															}
														}
														
														BackupSupport sb = BackupSupport.getInstance();
														sb.scheduleBackup();
														sb.makeBackup();
													} else
														if ((args[0] + "").toLowerCase().startsWith("close")) {
															// ignore, has been processed at the start of this method
														} else {
															if ((args[0] + "").toLowerCase().equalsIgnoreCase("merge")) {
																for (MongoDB m : MongoDB.getMongos()) {
																	System.out.println(":merge - about to merge temporary data sets in database " + m.getDatabaseName());
																	try {
																		m.processSplitResults().merge(true, null);
																		System.out.println(":merge - ^^^ merged temporary data sets in database " + m.getDatabaseName());
																	} catch (Exception e1) {
																		e1.printStackTrace();
																	}
																}
																
																return;
															} else
																if ((args[0] + "").toLowerCase().startsWith("file-mon")) {
																	FileMonitor f = new FileMonitor(args[1]);
																	try {
																		Integer sta = Integer.parseInt(args[2]);
																		Integer end = Integer.parseInt(args[3]);
																		String id = args[4];
																		Integer contentLineIdex = Integer.parseInt(args[5]);
																		f.startMonitoringAndSendMessageIfReceived(sta, end, id, contentLineIdex);
																	} catch (Exception e1) {
																		e1.printStackTrace();
																		System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Monitoring error: " + e1.getMessage());
																		System.exit(1);
																	}
																	System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Monitoring finished");
																	System.exit(0);
																} else
																	if ((args[0] + "").toLowerCase().startsWith("broadcast-rs")) {
																		try {
																			System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: Start broadcast monitor on ports ");
																			Integer sta = Integer.parseInt(args[1]);
																			Integer end = Integer.parseInt(args[2]);
																			System.out.println(sta + "-" + end + "...");
																			final String id = args[3];
																			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Message filter ID='" + id + "'");
																			BroadCastService bcs = new BroadCastService(sta, end, 100);
																			String lastMessage = "";
																			final SnapshotCreator s = new SnapshotCreator(args[4], args[5]);
																			final ThreadSafeOptions receiveCount = new ThreadSafeOptions();
																			int discarded = 0;
																			do {
																				UDPreceiveStructure res = bcs.receiveBroadcast(20);
																				if (res != null && res.data != null && res.data.length > 0) {
																					String msg = new String(res.data, StandardCharsets.UTF_8.name());
																					if (msg.startsWith(id + ":")) {
																						String msgContent = msg.substring((id + ":").length()).trim();
																						msgContent = msgContent.split("\\|")[0];
																						if (msgContent.length() > 0 && !msgContent.equals(lastMessage)) {
																							lastMessage = msgContent;
																							System.out.println(SystemAnalysis.getCurrentTimeInclSec()
																									+ ">INFO: Received new message content " + msgContent);
																							final String measurementLabel = args[6];
																							final String imageFileExt = args[7];
																							final String mcF = msgContent;
																							Runnable r = new Runnable() {
																								
																								@Override
																								public void run() {
																									try {
																										s.saveNewSnapshot(mcF, measurementLabel, imageFileExt);
																									} catch (Exception e) {
																										e.printStackTrace();
																										System.out.println(SystemAnalysis.getCurrentTime()
																												+ ">WARNING: Broadcast monitoring error: "
																												+ e.getMessage());
																										System.exit(1);
																									}
																									receiveCount.setBval(0, false);
																								}
																							};
																							Calendar rightNow = Calendar.getInstance();
																							int hour = rightNow.get(Calendar.HOUR_OF_DAY);
																							if (receiveCount.getBval(0, false)) {
																								discarded++;
																								System.out
																										.println(SystemAnalysis.lineSeparator
																												+ SystemAnalysis.getCurrentTimeInclSec()
																												+ ">INFO: Received valid change message, but previous image has not been received yet! Message is discarded!");
																								if (discarded >= 3) {
																									System.out
																											.println(SystemAnalysis.lineSeparator
																													+ SystemAnalysis.getCurrentTime()
																													+ ">WARNING: Did not receive image data 6 times in a row. Exiting to allow restart!");
																									System.exit(1);
																								}
																							} else {
																								discarded = 0;
																								// if (!(hour < 6 || hour >= 22)) {
																								Thread t = new Thread(r);
																								receiveCount.setBval(0, true);
																								t.start();
																								Thread.sleep(10);
																								ArrayList<String> params = new ArrayList<String>();
																								if (args.length > 8)
																									for (int idx = 9; idx < args.length; idx++) {
																										params.add(args[idx]);
																									}
																								if (msgContent.contains(" "))
																									params.add("\'" + msgContent + "\'");
																								else
																									params.add(msgContent);
																								System.out.println(SystemAnalysis.getCurrentTimeInclSec()
																										+ ">INFO: Execute: " + args[8] + " "
																										+ StringManipulationTools.getStringList(params, " "));
																								params.add(0, args[8]);
																								Process ls_proc = Runtime.getRuntime()
																										.exec(params.toArray(new String[] {}), null, null);
																								// } else {
																								// System.out
																								// .println(SystemAnalysis.getCurrentTimeInclSec()
																								// +
																								// ">INFO: Received valid change message, but current time (hour) is BLOCKED for further processing (DARK PERIOD).");
																								// }
																								Thread.sleep(100);
																								receiveCount.addInt(1);
																								if (receiveCount.getInt() >= 52) {
																									System.out
																											.println(SystemAnalysis.getCurrentTimeInclSec()
																													+ ">INFO: Received 52 change notifications. Stop execution, to allow restart.");
																									Thread.sleep(3000);
																									System.exit(1);
																								}
																							}
																						}
																					}
																				}
																			} while (true);
																		} catch (Exception e1) {
																			e1.printStackTrace();
																			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Broadcast monitoring error: "
																					+ e1.getMessage());
																			System.exit(1);
																		}
																	} else {
																		System.out.println(": Valid command line parameters:");
																		System.out.println("   'gui'     - start graphical GUI");
																		System.out.println("   'half'    - use half of the CPUs");
																		System.out.println("   'full'    - use all of the CPUs");
																		System.out.println("   'nnn'     - use specified number of CPUs");
																		System.out.println("   'clear'   - clear scheduled tasks");
																		System.out
																				.println("   'merge'   - in case of error (merge interrupted previously), merge temporary results");
																		System.out.println("   'close'   - close after task completion (cluster execution mode)");
																		System.out.println("   'info'    - Show CPU info");
																		System.out.println("   'monitor' - Report system info to cloud (join, but don't perform calculations)");
																		System.out.println("   'back'    - perform LT Imaging System to HSM backup now");
																		System.out.println("   'backup'  - perform LT Imaging System to HSM backup now, and then every midnight");
																		
																		System.out
																				.println("   'watch'   - periodically check the weight data for new data and report missing data by mail");
																		System.out
																				.println("   'watch-cmd' - same as watch, but auto-closing at 2 AM in the morning (for scripted execution)");
																		System.out
																				.println("   'file-mon fileName udpPortStart udpPortEnd contentID contentFileLineNumber'  - Watch a file for modification and report changes by broadcast message");
																		System.out
																				.println("   'broadcast-rs udpPortStart udpPortEnd contentID newFileDir snapshotDir measurementLabel imageFileExtension execCommand [params]'- Receive broadcase messages and send a signal to a process (Mac/Linux only)");
																		System.exit(1);
																	}
														}
									}
								}
		
		{
			for (MongoDB m : MongoDB.getMongos()) {
				CloudComputingService cc = CloudComputingService.getInstance(m);
				cc.setEnableCalculations(true);
			}
		}
		
		SystemInfoExt si = new SystemInfoExt();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: SYSTEM CPUs (sockets,physical,logical): " +
				si.getCpuSockets() + "," + si.getCpuPhysicalCores() + "," +
				si.getCpuLogicalCores() + ", using " + SystemAnalysis.getNumberOfCPUs());
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: SYSTEM MEMORY: " + SystemAnalysisExt.getPhysicalMemoryInGB() + " GB, using "
				+ SystemAnalysis.getMemoryMB() / 1024 + " GB");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: INITIALIZE CLOUD TASK MANAGER (T=" + IAPservice.getCurrentTimeAsNiceString() + ")");
		
		StopWatch s = new StopWatch(SystemAnalysis.getCurrentTime() + ">INFO: LabCube construction", false);
		ImageOperation io = new ImageOperation(new int[][] { { 0, 0 } });
		if (io != null)
			s.printTime();
		
		// register extended hierarchy and loaded image loaders (and more)
		new MultimodalDataHandlingAddon();
		
		ResourceIOManager.registerIOHandler(new LTftpHandler());
		for (MongoDB m : MongoDB.getMongos()) {
			ResourceIOManager.registerIOHandler(m.getHandler());
			
			CloudComputingService cc = CloudComputingService.getInstance(m);
			cc.switchStatus(m);
			System.out.println(SystemAnalysis.getCurrentTime() + ">START CLOUD SERVICE FOR " + m.getPrimaryHandler().getPrefix());
		}
	}
	
	public void setEnableCalculations(boolean enableCloudComputing) {
		cloudTaskManager.setDisableProcess(!enableCloudComputing);
	}
	
	public void switchStatus(MongoDB m) {
		if (!active) {
			// enable server mode
			try {
				cloudTaskManager.setHostName(SystemAnalysisExt.getHostName());
				cloudTaskManager.startWork(m);
			} catch (UnknownHostException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else {
			// disable server mode
			cloudTaskManager.stopWork();
		}
		this.active = !active;
	}
	
	public boolean getIsCalculationPossible() {
		return !cloudTaskManager.isDisableProces();
	}
	
}

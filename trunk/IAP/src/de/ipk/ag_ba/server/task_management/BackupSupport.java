package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.datasources.file_system.HsmFileSystemSource;
import de.ipk.ag_ba.gui.actions.Library;
import de.ipk.ag_ba.gui.actions.hsm.ActionDataExportToHsmFolder;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;
import de.ipk_gatersleben.ag_pbi.mmd.MultimodalDataHandlingAddon;

public class BackupSupport {
	
	private static BackupSupport instance = null;
	
	private final ArrayList<String> history = new ArrayList<String>();
	
	public static synchronized BackupSupport getInstance() {
		if (instance == null)
			instance = new BackupSupport();
		return instance;
	}
	
	private boolean backupRunning = false;
	
	private BackupSupport() {
		new MultimodalDataHandlingAddon();
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos()) {
			for (ResourceIOHandler handler : m.getHandlers())
				ResourceIOManager.registerIOHandler(handler);
		}
		final String fn = ReleaseInfo.getAppFolderWithFinalSep() + "iap_backup_history.txt";
		try {
			TextFile tf = new TextFile(fn);
			history.addAll(tf);
			print("INFO: BACKUP HISTORY LOADED (" + fn + ")");
		} catch (IOException e) {
			print("INFO: NO BACKUP HISTORY TO LOAD (" + fn + ": " + e.getMessage() + ")");
		}
		Timer t = new Timer("IAP Backup-History Saver");
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		TimerTask tT = new TimerTask() {
			@Override
			public void run() {
				if (tso.getInt() == history.size())
					return;
				try {
					TextFile tf = new TextFile();
					tf.addAll(history);
					tf.write(fn);
					tso.setInt(history.size());
				} catch (IOException e) {
					print("ERROR: BACKUP HISTORY COULD NOT BE SAVED (" + e.getMessage() + " - " + fn + ")");
				}
			}
		};
		tT.run();
		t.scheduleAtFixedRate(tT, new Date(), 1 * 60 * 1000);
		print("INFO: BACKUP SUPPORT READY");
		String hsmFolder = IAPmain.getHSMfolder();
		if (hsmFolder != null && new File(hsmFolder).exists()) {
			if (new File(hsmFolder).canRead())
				print("INFO: HSM FOLDER CAN BE READ");
			else
				print("ERROR: HSM FOLDER CAN NOT BE READ");
			if (new File(hsmFolder).canWrite())
				print("INFO: HSM FOLDER IS WRITABLE");
			else
				print("ERROR: CAN NOT WRITE TO HSM FOLDER");
			
		} else {
			print("WARNING: HSM FOLDER NOT AVAILABLE: " + hsmFolder);
		}
	}
	
	public void makeBackup() {
		if (backupRunning) {
			print("INFO: BACKUP PROCEDURE IS SKIPPED, BECAUSE PREVIOUS BACKUP OPERATION IS STILL RUNNING");
			return;
		}
		backupRunning = true;
		try {
			makeBackupInnerCall();
		} finally {
			backupRunning = true;
		}
	}
	
	private void print(String msg) {
		msg = SystemAnalysisExt.getCurrentTime() + ">" + msg;
		history.add(msg);
		System.out.println(msg);
	}
	
	private void makeBackupInnerCall() {
		try {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">START BACKUP SYNC");
			
			StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">INFO: LemnaTec to HSM Backup", false);
			
			LemnaTecDataExchange lt = new LemnaTecDataExchange();
			ArrayList<IdTime> ltIdArr = new ArrayList<IdTime>();
			ArrayList<IdTime> hsmIdArr = new ArrayList<IdTime>();
			ArrayList<IdTime> toSave = new ArrayList<IdTime>();
			
			for (String db : lt.getDatabases()) {
				for (ExperimentHeaderInterface ltExp : lt.getExperimentsInDatabase(null, db)) {
					ltIdArr.add(new IdTime(ltExp.getDatabaseId(), ltExp.getImportdate(), ltExp));
				}
			}
			
			String hsmFolder = IAPmain.getHSMfolder();
			if (hsmFolder != null && new File(hsmFolder).exists()) {
				print("HSM Folder: " + hsmFolder);
				Library lib = new Library();
				HsmFileSystemSource dataSourceHsm = new HsmFileSystemSource(lib, "HSM Archive", hsmFolder,
						IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
						IAPmain.loadIcon("img/ext/folder-remote.png"));
				dataSourceHsm.readDataSource();
				for (ExperimentHeaderInterface hsmExp : dataSourceHsm.getAllExperimentsNewest()) {
					if (hsmExp.getOriginDbId() != null)
						hsmIdArr.add(new IdTime(hsmExp.getOriginDbId(), hsmExp.getImportdate(), null));
					else
						System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: NULL EXPERIMENT IN HSM!");
				}
			}
			
			for (IdTime it : ltIdArr) {
				String db = it.getExperimentHeader().getDatabase();
				if (db == null || (!db.startsWith("CGH_") && !db.startsWith("APH_") && !db.startsWith("BGH_"))) {
					print("DATASET IGNORED (INVALID DB): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() + ")");
					continue;
				}
				
				boolean found = false;
				for (IdTime h : hsmIdArr) {
					if (h.equals(it)) {
						if (it.time.getTime() - h.time.getTime() > 1000) {
							print("BACKUP NEEDED (NEW DATA): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() + ")");
							toSave.add(it);
						}
						found = true;
						break;
					}
				}
				
				if (!found) {
					toSave.add(it);
					print("BACKUP NEEDED (NEW EXPERIMENT): " + it.Id + " (DB: "
							+ it.getExperimentHeader().getDatabase() + ")");
				}
			}
			
			print("START BACKUP OF " + toSave.size() + " EXPERIMENTS!");
			MongoDB m = MongoDB.getDefaultCloud();
			for (IdTime it : toSave) {
				ExperimentHeaderInterface src = it.getExperimentHeader();
				print("START BACKUP OF EXPERIMENT: " + it.Id);
				
				ExperimentReference er = new ExperimentReference(src);
				
				ActionDataExportToHsmFolder copyAction = new ActionDataExportToHsmFolder(m, er,
						hsmFolder);
				boolean enabled = true;
				copyAction.setStatusProvider(new BackgroundTaskConsoleLogger("", "", enabled));
				copyAction.performActionCalculateResults(null);
				print("FINISHED BACKUP OF EXPERIMENT: " + it.Id);
			}
			s.printTime();
		} catch (Exception e1) {
			print("ERROR: BACKUP INNER-CALL ERROR (" + e1.getMessage() + ")");
		}
	}
	
	public void scheduleBackup() {
		String hsmFolder = IAPmain.getHSMfolder();
		if (hsmFolder != null && new File(hsmFolder).exists()) {
			print("AUTOMATIC BACKUP FROM LT TO HSM (" + hsmFolder + ") HAS BEEN SCHEDULED EVERY DAY AT MIDNIGHT");
			Timer t = new Timer("IAP 24h-Backup-Timer");
			long period = 1000 * 60 * 60 * 24; // 24 Hours
			TimerTask tT = new TimerTask() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						BackupSupport sb = BackupSupport.getInstance();
						sb.makeBackup();
					} catch (InterruptedException e) {
						print("INFO: PROCESSING INTERRUPTED (" + e.getMessage() + ")");
					}
				}
			};
			Date startTime = new Date(); // current day at 23:59:39
			startTime.setHours(23);
			startTime.setMinutes(59);
			startTime.setSeconds(59);
			t.scheduleAtFixedRate(tT, startTime, period);
		} else {
			print("WARNING: NO AUTOMATIC BACKUP SCHEDULED! HSM FOLDER NOT AVAILABLE (" + hsmFolder + ")");
		}
	}
	
	public String getHistory(int maxLines, String pre, final String preLine, String lineBreak, String follow) {
		StringBuilder res = new StringBuilder();
		final Stack<String> news = new Stack<String>();
		ArrayList<String> h = new ArrayList<String>(history);
		if (maxLines < Integer.MAX_VALUE)
			Collections.reverse(h);
		for (int i = 0; i < history.size() && i < maxLines; i++) {
			String item = preLine + h.get(i);
			// if (maxLines < Integer.MAX_VALUE)
			// news.push(item);
			// else
			res.append(item);
		}
		while (!news.empty()) {
			String item = news.pop();
			res.append(item);
		}
		if (res != null && res.length() > 0)
			return pre + res.toString() + follow;
		else
			return res.toString();
	}
}

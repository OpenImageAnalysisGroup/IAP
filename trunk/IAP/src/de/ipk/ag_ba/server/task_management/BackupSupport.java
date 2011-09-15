package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;
import de.ipk_gatersleben.ag_pbi.mmd.MultimodalDataHandlingAddon;

public class BackupSupport {
	
	private static BackupSupport instance = null;
	
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
	}
	
	public void makeBackup() {
		if (backupRunning) {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: BACKUP PROCEDURE IS SKIPPED, BECAUSE PREVIOUS BACKUP OPERATION IS STILL RUNNING");
			return;
		}
		backupRunning = true;
		try {
			makeBackupInnerCall();
		} finally {
			backupRunning = true;
		}
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
				System.out.println("HSM Folder: " + hsmFolder);
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
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">DATASET IGNORED (INVALID DB): " + it.Id + " (DB: "
							+ it.getExperimentHeader().getDatabase() + ")");
					continue;
				}
				
				boolean found = false;
				for (IdTime h : hsmIdArr) {
					if (h.equals(it)) {
						if (it.time.getTime() - h.time.getTime() > 1000) {
							System.out.println(SystemAnalysisExt.getCurrentTime() + ">BACKUP NEEDED (NEW DATA): " + it.Id + " (DB: "
									+ it.getExperimentHeader().getDatabase() + ")");
							toSave.add(it);
						}
						found = true;
						break;
					}
				}
				
				if (!found) {
					toSave.add(it);
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">BACKUP NEEDED (NEW EXPERIMENT): " + it.Id + " (DB: "
							+ it.getExperimentHeader().getDatabase() + ")");
				}
			}
			
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">START BACKUP OF " + toSave.size() + " EXPERIMENTS!");
			MongoDB m = MongoDB.getDefaultCloud();
			for (IdTime it : toSave) {
				ExperimentHeaderInterface src = it.getExperimentHeader();
				System.out.println(SystemAnalysisExt.getCurrentTime() + ">START BACKUP OF EXPERIMENT: " + it.Id);
				
				ExperimentReference er = new ExperimentReference(src);
				
				ActionDataExportToHsmFolder copyAction = new ActionDataExportToHsmFolder(m, er,
						hsmFolder);
				boolean enabled = true;
				copyAction.setStatusProvider(new BackgroundTaskConsoleLogger("", "", enabled));
				copyAction.performActionCalculateResults(null);
				System.out.println(SystemAnalysisExt.getCurrentTime() + ">FINISHED BACKUP OF EXPERIMENT: " + it.Id);
			}
			s.printTime();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void scheduleBackup() {
		String hsmFolder = IAPmain.getHSMfolder();
		if (hsmFolder != null && new File(hsmFolder).exists()) {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">AUTOMATIC BACKUP FROM LT TO HSM (" + hsmFolder
					+ ") HAS BEEN SCHEDULED EVERY DAY AT MIDNIGHT");
			Timer t = new Timer();
			long period = 1000 * 60 * 60 * 24; // 24 Hours
			TimerTask tT = new TimerTask() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						BackupSupport sb = BackupSupport.getInstance();
						sb.makeBackup();
					} catch (InterruptedException e) {
						System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: PROCESSING INTERRUPTED");
					}
				}
			};
			Date startTime = new Date(); // current day at 23:59:39
			startTime.setHours(23);
			startTime.setMinutes(59);
			startTime.setSeconds(59);
			t.scheduleAtFixedRate(tT, startTime, period);
		} else {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">WARNING: NO AUTOMATIC BACKUP SCHEDULED! HSM FOLDER NOT AVAILABLE (" + hsmFolder + ")");
		}
	}
	
}

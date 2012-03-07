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
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.ActionCopyToMongo;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;
import de.ipk_gatersleben.ag_pbi.mmd.MultimodalDataHandlingAddon;

public class MassCopySupport {
	
	private static MassCopySupport instance = null;
	
	private final ArrayList<String> history = new ArrayList<String>();
	
	public static synchronized MassCopySupport getInstance() {
		if (instance == null)
			instance = new MassCopySupport();
		return instance;
	}
	
	private boolean massCopyRunning = false;
	
	private MassCopySupport() {
		new MultimodalDataHandlingAddon();
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos()) {
			for (ResourceIOHandler handler : m.getHandlers())
				ResourceIOManager.registerIOHandler(handler);
		}
		final String fn = ReleaseInfo.getAppFolderWithFinalSep() + "iap_mass_copy_history.txt";
		try {
			TextFile tf = new TextFile(fn);
			history.addAll(tf);
			print("INFO: MASS COPY HISTORY LOADED (" + fn + ")");
		} catch (IOException e) {
			print("INFO: NO MASS COPY HISTORY TO LOAD (" + fn + ": " + e.getMessage() + ")");
		}
		Timer t = new Timer("IAP MASS-COPY-History Saver");
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
					print("ERROR: MASS COPY HISTORY COULD NOT BE SAVED (" + e.getMessage() + " - " + fn + ")");
				}
			}
		};
		tT.run();
		t.scheduleAtFixedRate(tT, new Date(), 1 * 60 * 1000);
		print("INFO: MASS COPY SUPPORT READY");
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
	
	public void performMassCopy() {
		if (massCopyRunning) {
			print("INFO: MASS COPY PROCEDURE IS SKIPPED, BECAUSE PREVIOUS MASS COPY OPERATION IS STILL RUNNING");
			return;
		}
		massCopyRunning = true;
		try {
			makeCopyInnerCall();
		} finally {
			massCopyRunning = false;
		}
	}
	
	private void print(String msg) {
		msg = SystemAnalysis.getCurrentTime() + ">" + msg;
		history.add(msg);
		System.out.println(msg);
	}
	
	private void makeCopyInnerCall() {
		try {
			System.out.println(SystemAnalysis.getCurrentTime() + ">START MASS COPY SYNC");
			
			StopWatch s = new StopWatch(SystemAnalysis.getCurrentTime() + ">INFO: LemnaTec to MongoDBs (MASS COPY)", false);
			
			LemnaTecDataExchange lt = new LemnaTecDataExchange();
			ArrayList<IdTime> ltIdArr = new ArrayList<IdTime>();
			ArrayList<IdTime> mongoIdsArr = new ArrayList<IdTime>();
			ArrayList<IdTime> toSave = new ArrayList<IdTime>();
			
			for (String db : lt.getDatabases()) {
				for (ExperimentHeaderInterface ltExp : lt.getExperimentsInDatabase(null, db)) {
					ltIdArr.add(new IdTime(null, ltExp.getDatabaseId(), ltExp.getImportdate(), ltExp));
				}
			}
			
			for (MongoDB m : MongoDB.getMongos()) {
				print("MongoDB: " + m.getDatabaseName() + "@" + m.getDefaultHost());
				for (ExperimentHeaderInterface hsmExp : m.getExperimentList(null)) {
					if (hsmExp.getOriginDbId() != null)
						mongoIdsArr.add(new IdTime(m, hsmExp.getOriginDbId(), hsmExp.getImportdate(), null));
					else
						System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: NULL EXPERIMENT IN MongoDB (" + m.getDatabaseName() + ")!");
				}
			}
			
			for (IdTime it : ltIdArr) {
				String db = it.getExperimentHeader().getDatabase();
				if (db == null || (!db.startsWith("CGH_") && !db.startsWith("APH_") && !db.startsWith("BGH_"))) {
					print("DATASET IGNORED (INVALID DB): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() + ")");
					continue;
				} else
					if (it.getExperimentHeader().getExperimentName().equals("unknown")) {
						print("DATASET IGNORED (INVALID UKNOWN EXPERIMENT NAME): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() + ")");
						continue;
					}
				
				boolean found = false;
				for (IdTime h : mongoIdsArr) {
					if (h.equals(it)) {
						if (it.time.getTime() - h.time.getTime() > 1000) {
							print("MASS COPY INTENDED (NEW DATA): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() + ")");
							toSave.add(it);
						}
						found = true;
						break;
					}
				}
				
				if (!found) {
					toSave.add(it);
					print("MASS COPY INTENDED (NEW EXPERIMENT): " + it.Id + " (DB: "
							+ it.getExperimentHeader().getDatabase() + ")");
				}
			}
			
			print("START MASS COPY OF " + toSave.size() + " EXPERIMENTS!");
			for (IdTime it : toSave) {
				MongoDB m = it.getMongoDB();
				if (m == null) {
					// new data set, copy to last mongo instance
					m = MongoDB.getMongos().get(MongoDB.getMongos().size() - 1);
				}
				ExperimentHeaderInterface src = it.getExperimentHeader();
				print("START MASS COPY OF EXPERIMENT: " + it.Id + " to MongoDB " + m.getDatabaseName() + "@" + m.getDefaultHost());
				
				ExperimentReference er = new ExperimentReference(src);
				ActionCopyToMongo copyAction = new ActionCopyToMongo(m, er, true);
				boolean enabled = true;
				copyAction.setStatusProvider(new BackgroundTaskConsoleLogger("", "", enabled));
				boolean simulate = true;
				if (!simulate)
					copyAction.performActionCalculateResults(null);
				print("FINISHED COPY OF EXPERIMENT: " + it.Id + " to MongoDB " + m.getDatabaseName() + "@" + m.getDefaultHost());
			}
			s.printTime();
		} catch (Exception e1) {
			print("ERROR: MASS COPY INNER-CALL ERROR (" + e1.getMessage() + ")");
		}
	}
	
	public void scheduleMassCopy() {
		String hsmFolder = IAPmain.getHSMfolder();
		if (hsmFolder != null && new File(hsmFolder).exists()) {
			print("AUTOMATIC MASS COPY FROM LT TO MongoDB (" + hsmFolder + ") HAS BEEN SCHEDULED EVERY DAY AT MIDNIGHT");
			Timer t = new Timer("IAP 24h-Backup-Timer");
			long period = 1000 * 60 * 60 * 24; // 24 Hours
			TimerTask tT = new TimerTask() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						MassCopySupport sb = MassCopySupport.getInstance();
						sb.performMassCopy();
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
			print("WARNING: NO AUTOMATIC MASS COPY SCHEDULED! HSM FOLDER NOT AVAILABLE (" + hsmFolder + ")");
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

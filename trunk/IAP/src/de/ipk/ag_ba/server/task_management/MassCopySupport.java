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

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsFalse;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
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
	
	private final BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
	
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
	}
	
	public void performMassCopy() throws InterruptedException {
		if (massCopyRunning) {
			print("INFO: MASS COPY PROCEDURE IS SKIPPED, BECAUSE PREVIOUS MASS COPY OPERATION IS STILL RUNNING");
			return;
		}
		massCopyRunning = true;
		
		boolean en = new SettingsHelperDefaultIsFalse().isEnabled("sync");
		if (!en)
			return;
		for (int i = 30; i >= 0; i--) {
			status.setCurrentStatusText1("Countdown");
			status.setCurrentStatusText2("Start sync in " + i + " seconds...");
			Thread.sleep(1000);
			en = new SettingsHelperDefaultIsFalse().isEnabled("sync");
			if (!en) {
				massCopyRunning = false;
				status.setCurrentStatusText1("Sync cancelled");
				status.setCurrentStatusText2("");
				Thread.sleep(5000);
				if (!massCopyRunning) {
					status.setCurrentStatusText1("");
					status.setCurrentStatusText2("");
				}
				return;
			}
		}
		status.setCurrentStatusText1("Sync initiated");
		status.setCurrentStatusText2("Analyze data status...");
		Thread.sleep(1000);
		
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
		status.setCurrentStatusText2(msg);
	}
	
	private void makeCopyInnerCall() {
		try {
			System.out.println(SystemAnalysis.getCurrentTime() + ">START MASS COPY SYNC");
			status.setCurrentStatusText1("Start sync...");
			StopWatch s = new StopWatch(SystemAnalysis.getCurrentTime() + ">INFO: LemnaTec to MongoDBs (MASS COPY)", false);
			
			LemnaTecDataExchange lt = new LemnaTecDataExchange();
			ArrayList<IdTime> ltIdArr = new ArrayList<IdTime>();
			ArrayList<IdTime> mongoIdsArr = new ArrayList<IdTime>();
			final ArrayList<IdTime> toSave = new ArrayList<IdTime>();
			
			for (String db : lt.getDatabases()) {
				// if (db.endsWith("11"))
				// continue;
				try {
					for (ExperimentHeaderInterface ltExp : lt.getExperimentsInDatabase(null, db)) {
						ltIdArr.add(new IdTime(null, ltExp.getDatabaseId(),
								ltExp.getImportdate(), ltExp, ltExp.getNumberOfFiles()));
					}
				} catch (Exception e) {
					if (!e.getMessage().contains("relation \"snapshot\" does not exist"))
						print("Cant process DB " + db + ": " + e.getMessage());
				}
			}
			
			boolean useOnlyMainDatabase = true;
			ArrayList<MongoDB> checkM;
			if (useOnlyMainDatabase) {
				checkM = new ArrayList<MongoDB>();
				checkM.add(MongoDB.getDefaultCloud());
			} else
				checkM = MongoDB.getMongos();
			for (MongoDB m : checkM) {
				try {
					print("MongoDB: " + m.getDatabaseName() + "@" + m.getDefaultHost());
					for (ExperimentHeaderInterface hsmExp : m.getExperimentList(null)) {
						if (hsmExp.getOriginDbId() != null)
							mongoIdsArr.add(new IdTime(m, hsmExp.getOriginDbId(),
									hsmExp.getImportdate(), null, hsmExp.getNumberOfFiles()));
						else
							System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: NULL EXPERIMENT IN MongoDB (" + m.getDatabaseName() + ")!");
					}
				} catch (Exception e) {
					print("Cant process mongo DB " + m.getDatabaseName() + ": " + e.getMessage());
				}
			}
			
			for (IdTime it : ltIdArr) {
				String db = it.getExperimentHeader().getDatabase();
				if (db == null || (!db.startsWith("CGH_") && !db.startsWith("APH_") && !db.startsWith("BGH_"))) {
					// print("DATASET IGNORED (INVALID DB): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() + ")");
					continue;
				} else
					if (it.getExperimentHeader().getExperimentName().equals("unknown")) {
						// print("DATASET IGNORED (INVALID UKNOWN EXPERIMENT NAME): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() + ")");
						continue;
					}
				
				boolean found = false;
				for (IdTime h : mongoIdsArr) {
					if (h.equals(it)) {
						if (it.time.getTime() - h.time.getTime() > 1000) {
							print("MASS COPY INTENDED (MORE CURRENT DATA): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() + ")");
							toSave.add(it);
						} else
							if (it.getNumberOfFiles() != h.getNumberOfFiles()) {
								print("MASS COPY INTENDED (MORE IMAGES INSIDE EXPERIMENT): " + it.Id + " (DB: " + it.getExperimentHeader().getDatabase() +
										", LT:" + it.getNumberOfFiles() + " != M:" + h.getNumberOfFiles() + ")");
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
			status.setCurrentStatusText1("Start copy of " + toSave.size() + " experiments...");
			int done = 0;
			for (final IdTime it : toSave) {
				boolean en = new SettingsHelperDefaultIsFalse().isEnabled("sync");
				if (!en)
					continue;
				status.setCurrentStatusText1("Copy " + toSave.size() + " experiments (" + done + " finished)");
				MongoDB m = it.getMongoDB();
				if (m == null) {
					// new data set, copy to last mongo instance
					if (useOnlyMainDatabase)
						m = MongoDB.getDefaultCloud();
					else
						m = MongoDB.getMongos().get(MongoDB.getMongos().size() - 1);
				}
				ExperimentHeaderInterface src = it.getExperimentHeader();
				print("Copy " + it.Id + " to " + m.getDatabaseName());
				ExperimentReference er = new ExperimentReference(src);
				ActionCopyToMongo copyAction = new ActionCopyToMongo(m, er, true);
				status.setPrefix1("<html>Copying " + (done + 1) + "/" + toSave.size() + " (" + it.Id + ")<br>");
				copyAction.setStatusProvider(status);
				boolean simulate = false;
				if (!simulate)
					copyAction.performActionCalculateResults(null);
				print("Copied " + it.Id + " to " + m.getDatabaseName());
				done++;
				status.setCurrentStatusValueFine(100d * done / toSave.size());
				
				IAPexperimentTypes experimentType = IAPexperimentTypes.getExperimentTypeFromExperimentTypeName(er.getHeader().getExperimentType());
				NavigationAction analysisAction = null;
				switch (experimentType) {
					case BarleyGreenhouse:
					case MaizeGreenhouse:
					case Phytochamber:
					case PhytochamberBlueRubber:
				}
				if (analysisAction != null) {
					// submit analysis action analysis tasks to target cloud
					
					// available result data should be checked upon analysis start (based on experiment header date info)
					// only snapshots with a date newer than the given last newest time should be analysed
				}
				
				Thread.sleep(1000);
			}
			status.setPrefix1(null);
			status.setCurrentStatusText1("Copy complete (" + done + " finished)");
			status.setCurrentStatusText2("Next sync at 8 PM");
			status.setCurrentStatusValueFine(100d);
			s.printTime();
		} catch (Exception e1) {
			print("ERROR: MASS COPY INNER-CALL ERROR (" + e1.getMessage() + ")");
		}
	}
	
	public void scheduleMassCopy() {
		String hsmFolder = IAPmain.getHSMfolder();
		if (hsmFolder != null && new File(hsmFolder).exists()) {
			print("AUTOMATIC MASS COPY FROM LT TO MongoDB (" + hsmFolder + ") HAS BEEN SCHEDULED EVERY DAY AT 20:00");
			Timer t = new Timer("IAP 24h-Backup-Timer");
			long period = 1000 * 60 * 60 * 24; // 24 Hours
			TimerTask tT = new TimerTask() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						boolean en = new SettingsHelperDefaultIsFalse().isEnabled("sync");
						if (!en)
							return;
						
						Thread.sleep(1000);
						
						MassCopySupport sb = MassCopySupport.getInstance();
						sb.performMassCopy();
					} catch (InterruptedException e) {
						print("INFO: PROCESSING INTERRUPTED (" + e.getMessage() + ")");
					}
				}
			};
			Date startTime = new Date(); // current day at 20:00:00
			startTime.setHours(20);
			startTime.setMinutes(00);
			startTime.setSeconds(00);
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
	
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
		return status;
	}
}

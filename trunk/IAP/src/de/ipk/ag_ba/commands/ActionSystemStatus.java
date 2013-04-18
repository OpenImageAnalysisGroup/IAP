package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;

import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.commands.lt.ActionGridFSscreenshotMonitoring;
import de.ipk.ag_ba.commands.lt.ActionWebCamView;
import de.ipk.ag_ba.commands.mongodb.ActionMassCopyHistory;
import de.ipk.ag_ba.commands.settings.ActionToggleSettingDefaultIsFalse;
import de.ipk.ag_ba.commands.system_status.ActionBackupHistory;
import de.ipk.ag_ba.commands.system_status.ActionPortScan;
import de.ipk.ag_ba.commands.system_status.ActionScreenshotStorage;
import de.ipk.ag_ba.commands.system_status.ActionValidateLtStorageTimes;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.util.WebCamInfo;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BackupSupport;
import de.ipk.ag_ba.server.task_management.CloundManagerNavigationAction;
import de.ipk.ag_ba.server.task_management.MassCopySupport;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

final class ActionSystemStatus extends AbstractNavigationAction {
	private NavigationButton src;
	private final HashMap<String, ArrayList<String>> infoset = new HashMap<String, ArrayList<String>>();
	ArrayList<NavigationButton> resultNavigationButtons = new ArrayList<NavigationButton>();
	
	ActionSystemStatus(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
		infoset.clear();
		resultNavigationButtons.clear();
		
		if (MongoDB.getDefaultCloud() != null) {
			resultNavigationButtons.add(new NavigationButton(new ActionScreenshotStorage("Enable/Disable Desktop Screenshot Sharing"), src.getGUIsetting()));
		}
		
		for (WebCamInfo ur : IAPservice.getActiveWebCamURLs()) {
			IOurl url = new IOurl(ur.getUrl());
			resultNavigationButtons.add(
					ActionWebCamView.getLemnaCamButton(src.getGUIsetting(),
							"Show camera view (" + ur.getName() + ")", ur.getName(), url));
		}
		
		{
			for (MongoDB dc : MongoDB.getMongos()) {
				try {
					for (GridFSDBFile sf : dc.getSavedScreenshots()) {
						resultNavigationButtons.add(
								new NavigationButton(
										new ActionGridFSscreenshotMonitoring(
												dc, "" + sf.getId(),
												sf.getFilename(),
												dc.getScreenshotFS()),
										src.getGUIsetting()));
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		if (SystemOptions.getInstance().getBoolean("IAP", "show_grid_status_icon", true)) {
			ArrayList<NavigationAction> cloudHostList = new ArrayList<NavigationAction>();
			for (MongoDB m : MongoDB.getMongos()) {
				try {
					m.batch().getScheduledForStart(0);
					CloundManagerNavigationAction cmna = new CloundManagerNavigationAction(m, true);
					try {
						cmna.performActionCalculateResults(src);
						for (NavigationButton o : cmna.getResultNewActionSet())
							cloudHostList.add(o.getAction());
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					System.out.println(m.getDatabaseName() + " is not accessible!");
				}
			}
			
			if (cloudHostList.size() > 0) {
				ActionFolder cloudHosts = new ActionFolder(
						"Cloud Hosts", "Show overview of cloud computing hosts",
						cloudHostList.toArray(new NavigationAction[] {}), src.getGUIsetting());
				resultNavigationButtons.add(new NavigationButton(cloudHosts, src.getGUIsetting()));
			}
		}
		
		boolean showLTstorageTimeCheckIcon = SystemOptions.getInstance().getBoolean(
				"LT-DB", "Debug//system_status_show_storage_time_check_icon", false);
		if (showLTstorageTimeCheckIcon)
			resultNavigationButtons.add(new NavigationButton(new ActionValidateLtStorageTimes(null), src.getGUIsetting()));
		
		boolean showLT = SystemOptions.getInstance().getBoolean("LT-DB", "show_icon", false);
		boolean hsmEn = SystemOptions.getInstance().getBoolean("ARCHIVE", "enabled", false);
		if (showLT && hsmEn)
			resultNavigationButtons.add(new NavigationButton(new ActionToggleSettingDefaultIsFalse(
					null, null,
					"Enable or disable the automated backup of LT data sets to the HSM file system",
					"Automatic Backup to HSM",
					"Watch-Service|Automatic Copy//enabled"), src.getGUIsetting()));
		
		if (showLT && hsmEn)
			resultNavigationButtons.add(new NavigationButton(new ActionBackupHistory("Show full backup history"), src.getGUIsetting()));
		
		BackgroundTaskStatusProviderSupportingExternalCall copyToHsmStatus = MassCopySupport.getInstance().getStatusProvider();
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		Runnable startActionMassCopy = new Runnable() {
			@Override
			public void run() {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							boolean mergeOnly = tso.getBval(0, false);
							System.out.println(SystemAnalysis.getCurrentTime() + ">Mass-Copy - only merge? " + mergeOnly + ", settings object: " +
									tso);
							tso.setBval(0, !tso.getBval(0, false));
							MassCopySupport.getInstance().performMassCopy(mergeOnly);
						} catch (InterruptedException e) {
							e.printStackTrace();
							MongoDB.saveSystemErrorMessage("Mass Copy Execution Error", e);
						}
					}
				}, "mass copy sync");
				t.start();
			}
		};
		
		if (MongoDB.getMongos().size() > 0 &&
				SystemOptions.getInstance().getBoolean("LT-DB", "show_icon", false)) {
			resultNavigationButtons.add(new NavigationButton(new ActionToggleSettingDefaultIsFalse(copyToHsmStatus,
					startActionMassCopy,
					"Enable or disable the automated copy of LT data sets to the MongoDB DBs",
					"Automatic DB-Copy",
					"Watch-Service|Automatic Copy//enabled"), src.getGUIsetting()));
			
			resultNavigationButtons.add(new NavigationButton(new ActionMassCopyHistory("Show DB-Copy history"), src.getGUIsetting()));
		}
		
		boolean simpleIcons = true;
		
		String pc = IAPimages.getNetworkPConline();
		String pcOff = IAPimages.getNetworkPCoffline();
		
		// boolean rLocal = IAPservice.isReachable("localhost");
		// resultNavigationButtons.add(new NavigationButton(new ActionPortScan("localhost",
		// simpleIcons ? "img/ext/computer.png" : "img/ext/computer.png"), src
		// .getGUIsetting()));
		// if (!rLocal)
		// resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
		
		boolean checkServerAvailability = false;
		if (checkServerAvailability) {
			boolean rBA13 = IAPservice.isReachable("ba-13.ipk-gatersleben.de");
			if (!rBA13) {
				resultNavigationButtons.add(new NavigationButton(new ActionPortScan("BA-13",
						simpleIcons ? "img/ext/network-server.png" : "img/ext/dellR810_3.png"), src
						.getGUIsetting()));
				resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
			}
			
			boolean rBA24 = IAPservice.isReachable("ba-24.ipk-gatersleben.de");
			if (!rBA24) {
				resultNavigationButtons.add(new NavigationButton(new ActionPortScan("BA-24",
						simpleIcons ? (rBA24 ? pc : pcOff) : "img/ext/macPro.png"), src
						.getGUIsetting()));
				resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
			}
			
			boolean rLemnaDB = IAPservice.isReachable("lemna-db.ipk-gatersleben.de");
			if (!rLemnaDB) {
				resultNavigationButtons.add(new NavigationButton(new ActionPortScan("lemna-db",
						simpleIcons ? "img/ext/network-server.png" : "img/ext/dellR810_3.png"), src
						.getGUIsetting()));
				resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
			}
		}
		
		// boolean rBA03 = IAPservice.isReachable("ba-03.ipk-gatersleben.de");
		// resultNavigationButtons.add(new NavigationButton(new ActionPortScan("BA-03",
		// simpleIcons ? (rBA03 ? pc : pcOff) : "img/ext/delT7500.png"), src
		// .getGUIsetting()));
		// if (!rBA03)
		// resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
		
		// boolean rNW04 = IAPservice.isReachable("nw-04.ipk-gatersleben.de");
		// resultNavigationButtons.add(new NavigationButton(new PortScanAction("NW-04",
		// simpleIcons ? (rNW04 ? pc : pcOff) : "img/ext/pc.png"), src
		// .getGUIsetting()));
		// if (!rNW04)
		// resultNavigationButtons.get(resultNavigationButtons.size() - 1).setRightAligned(true);
		
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(
			ArrayList<NavigationButton> currentSet) {
		currentSet.add(src);
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return resultNavigationButtons;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> htmlTextPanels = new ArrayList<String>();
		htmlTextPanels.add(BackupSupport.getInstance().getHistory(4,
				"" +
						"<p>Backup-Status:<br><br><ul>",
				"<li>", "", ""));
		
		htmlTextPanels.add(MassCopySupport.getInstance().getHistory(4,
				"" +
						"<p>Automatic Copy-Status:<br><br><ul>",
				"<li>", "", ""));
		
		htmlTextPanels.add(SystemAnalysisExt.getStatus("<p>System-Status:<br><br><ul>", "<li>", "", ""));
		
		htmlTextPanels.add(SystemAnalysisExt.getStorageStatus("<p>Storage-Status:<br><br><ul>", "<li>", "", ""));
		
		return new MainPanelComponent(htmlTextPanels);
	}
}
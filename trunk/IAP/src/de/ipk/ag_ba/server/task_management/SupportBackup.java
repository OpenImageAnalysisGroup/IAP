package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.io.File;
import java.util.ArrayList;

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

public class SupportBackup {
	
	public SupportBackup() {
		new MultimodalDataHandlingAddon();
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos()) {
			for (ResourceIOHandler handler : m.getHandlers())
				ResourceIOManager.registerIOHandler(handler);
		}
	}
	
	public void makeBackup() {
		try {
			System.out.println(":back - perform backup now");
			
			StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">INFO: LemnaTec to HSM Backup", false);
			
			LemnaTecDataExchange lt = new LemnaTecDataExchange();
			ArrayList<IdTime> ltIdArr = new ArrayList<IdTime>();
			ArrayList<IdTime> hsmIdArr = new ArrayList<IdTime>();
			ArrayList<IdTime> toSave = new ArrayList<IdTime>();
			
			for (String db : lt.getDatabases()) {
				System.out.println("db" + db);
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
				}
			}
			
			for (IdTime it : ltIdArr) {
				boolean found = false;
				for (IdTime h : hsmIdArr) {
					if (h.equals(it)) {
						if (it.time.after(h.time))
							toSave.add(it);
						
						found = true;
						break;
					}
				}
				
				if (!found) {
					System.out.println("NOT FOUND: " + it.Id);
					toSave.add(it);
				}
			}
			
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">START BACKUP OF " + toSave.size() + " EXPERIMENTS!");
			
			for (IdTime it : toSave) {
				ExperimentHeaderInterface src = it.getExperimentHeader();
				
				ExperimentReference er = new ExperimentReference(src);
				
				ActionDataExportToHsmFolder copyAction = new ActionDataExportToHsmFolder(MongoDB.getDefaultCloud(), er,
									hsmFolder);
				boolean enabled = true;
				copyAction.setStatusProvider(new BackgroundTaskConsoleLogger("", "", enabled));
				copyAction.performActionCalculateResults(null);
			}
			s.printTime();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}

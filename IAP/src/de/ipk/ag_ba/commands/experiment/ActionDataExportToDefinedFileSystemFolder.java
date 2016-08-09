package de.ipk.ag_ba.commands.experiment;

import java.io.File;
import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.OpenFileDialogService;
import org.SystemOptions;

import com.sun.scenario.Settings;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_actions.SideGuiComponent;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

/**
 * 
 * @author pape
 *
 * Copy to specified location (e.g. HSM) in the following pre-defined structure:
 * \\hsm\...\exp_ID\exp_ID_IAP_dataset\
 * 
 * Initial path can be changed: Settings -> Archive -> Hsm path
 * 
 */
public class ActionDataExportToDefinedFileSystemFolder extends AbstractNavigationAction implements NavigationAction {

	private final ArrayList<ExperimentReferenceInterface> experimentReferences;
	private final MongoDB m;
	private final boolean ignoreOutliers;
	private final ArrayList<MainPanelComponent> results = new ArrayList<MainPanelComponent>();
	
	public ActionDataExportToDefinedFileSystemFolder(String tooltip, MongoDB m,
			ArrayList<ExperimentReferenceInterface> experimentReference, boolean ignoreOutliers) {
		super(tooltip);
		this.m = m;
		this.experimentReferences = experimentReference;
		this.ignoreOutliers = ignoreOutliers;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		results.clear();
		if (experimentReferences == null)
			return;
		String exp_name = "";
		String databasename = "";

		for (ExperimentReferenceInterface exp_info : experimentReferences) {
			exp_name = exp_info.getExperimentName();
			databasename = exp_info.getDatabaseName();
		}
		
		// check for valid name in case of analysis result
		if(exp_name.contains(" of ")) {
			exp_name = exp_name.substring((exp_name.indexOf(" of ") + 4));
		}
		
		if(databasename == null)
			databasename = "test";
		
		if (exp_name == null || databasename == null) {
			throw new Exception("Missing experiment information (please specify experiment name and/(or databasename.");
		}
		
		boolean doit = false;
		
		String s = File.separator;
		String storage_path = SystemOptions.getInstance().getString("ARCHIVE", "HSM path", s + s + "hsm" + s + "LIMS" + s +"HET" + s +"PHENOTYPING" + s +"Analysis_Archive_C" + s);
		
		if(exp_name.length() > 0 && databasename.length() > 0) {
			File currentDirectory = new File(storage_path
					+ exp_name + "_" + databasename + s + exp_name + "_IAP_dataset" + s);
			
			
			String hsm_info_msg = "<html><body><p style=\"text-align:center;\">"
					+ "[YES;NO]You are going to archive your dataset in the IPK-HSM system (hierarchical storage management).<br>"
					+ " This is for permanent archiving your analysis data.<br>";
			String hsm_info_msg_1 = 
					"By clicking \"Yes\" the copy process will start (this may takes several hours depending on the size of your dataset).<br>"
					+ "If you just want to save some temporary data or not sure what to do please click \"No\"!"
					+ "</p></body></html>";
			
			// check if path exists, is directory and it is empty
			if (currentDirectory.exists()) {
				if(currentDirectory.isDirectory()) {
					File[] ff = currentDirectory.listFiles();
					for (File f : ff) {
						System.out.println(f.getName());
						}
						if(currentDirectory.list().length != 0) {
								ParameterOptions params = new ParameterOptions(hsm_info_msg + "<br>HSM folder<br>"
										+ currentDirectory + "<br>is not empty! Data will be merged!<br><br>" + hsm_info_msg_1, new String[]{""});
								MyInputHelper ih = new MyInputHelper();
								Object[] res = ih.getInput(params.getDescription(), "Archive Dataset " + experimentReferences.get(0).getExperimentName(),
									params.getParameterField());
								if (res != null)
									doit = true;
						} else
							doit = true;
				} else
					throw new Exception("HSM folder is not a directory.");
			} else {
				ParameterOptions params = new ParameterOptions(hsm_info_msg + "<br><bold>No HSM folder with the specified path<br>"
						+ currentDirectory + "<br>found, it will be craeted!</bold><br><br>" + hsm_info_msg_1, new String[]{""});
				MyInputHelper ih = new MyInputHelper();
				Object[] res = ih.getInput(params.getDescription(), "Archive Dataset " + experimentReferences.get(0).getExperimentName(),
					params.getParameterField());
				if (res != null) {
					currentDirectory.mkdirs();
					doit = true;
				}
			}
				 
				
			if (currentDirectory != null && doit) {
				VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2(
						"user.dir." + System.currentTimeMillis(),
						VfsFileProtocol.LOCAL,
						"User Selected Directory",
						"File I/O", "",
						null,
						null,
						currentDirectory.getCanonicalPath(),
						false,
						false,
						null);
				for (ExperimentReferenceInterface er : experimentReferences) {
					results.add(vfs.saveExperiment(m, er, getStatusProvider(), ignoreOutliers));
				}
			}
		} else
			throw new Exception("No valid experiment parameters found for HSM archiving!");
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> rl = new ArrayList<String>();
		if (results != null)
			for (MainPanelComponent m : results)
				rl.addAll(m.getHTML());
		return new MainPanelComponent(rl);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Media-Tape-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "To Permanent Storage (HSM)";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}

}
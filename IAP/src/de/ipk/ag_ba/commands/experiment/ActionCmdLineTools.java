package de.ipk.ag_ba.commands.experiment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.ErrorMsg;
import org.ReleaseInfo;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.scripts.AbstractRscriptExecutionAction;
import de.ipk.ag_ba.commands.experiment.scripts.ScriptHelper;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.plugins.IAPpluginManager;

public class ActionCmdLineTools extends AbstractNavigationAction implements ActionDataProcessing {
	private ExperimentReference experimentReference;
	
	public ActionCmdLineTools(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		
		String folder = "scripts";
		
		for (ActionScriptBasedDataProcessing adp : IAPpluginManager.getInstance().getExperimentScriptActions(experimentReference)) {
			try {
				File td = new File(folder + File.separator + adp.getFileName() + ".bundle");
				if (!td.isDirectory())
					td.mkdirs();
				new ScriptHelper(folder + File.separator + adp.getFileName() + ".bundle" + File.separator + adp.getFileName() + ".ini", adp);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		
		File scanDirectories = new File(ReleaseInfo.getAppFolderWithFinalSep() + folder);
		for (String bundleDir : scanDirectories.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name != null && name.endsWith(".bundle");
			}
		})) {
			File ff = new File(scanDirectories.getPath() + File.separator + bundleDir);
			if (ff.exists())
				for (String iniFN : ff.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name != null && name.endsWith(".ini");
					}
				})) {
					try {
						res.add(new NavigationButton(new AbstractRscriptExecutionAction(null, iniFN, "scripts" + File.separator +
								bundleDir + File.separator + iniFN, experimentReference),
								guiSetting));
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
		}
		
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Scripts";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Utilities-Terminal-64.png";// IAPimages.getToolbox();
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.experimentReference = experimentReference;
	}
}
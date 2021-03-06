package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.ErrorMsg;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.commands.vfs.ActionDataExportToVfs;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemHandler;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;

/**
 * @author klukas
 */
public class ActionViewData extends AbstractNavigationAction implements ActionDataProcessing {
	private ExperimentReferenceInterface experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	
	public ActionViewData() {
		super("Show image data or plot numeric data");
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
		try {
			status.setCurrentStatusText1("Load Data");
			experiment.getData();
			status.setCurrentStatusText1("");
			SupplementaryFilePanelMongoDB sfp = new SupplementaryFilePanelMongoDB(experiment,
					experiment.getExperimentName());
			mpc = new MainPanelComponent(sfp);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		// todo add zoom slider (default, large, extra large)
		// todo add plant filter (all, ID 1, ID 2, ID 3, ...)
		
		boolean saveOk = false;
		
		if (experiment.getM() != null) {
			saveOk = true;
			res.add(new NavigationButton("Save Annotation Changes", new ActionCopyToMongo(experiment.getM(), experiment, true), src.getGUIsetting()));
		} else {
			String dbId = experiment != null && experiment.getHeader() != null ? experiment.getHeader().getDatabaseId() : null;
			if (dbId != null) {
				String id = dbId.contains(":") ? dbId.substring(0, dbId.indexOf(":")) : null;
				if (id != null && !id.isEmpty()) {
					ResourceIOHandler vfs = ResourceIOManager.getHandlerFromPrefix(id);
					if (vfs instanceof VirtualFileSystemHandler) {
						VirtualFileSystemHandler vv = (VirtualFileSystemHandler) vfs;
						if (vv.getVFS() instanceof VirtualFileSystemVFS2) {
							VirtualFileSystemVFS2 vv2 = (VirtualFileSystemVFS2) vv.getVFS();
							if (vv2.isAbleToSaveData()) {
								saveOk = true;
								res.add(new NavigationButton(null,
										getSaveAnnotationsInVfsCommand(experiment, vv2),
										src.getGUIsetting()));
							}
						}
					}
				}
			}
		}
		
		if (saveOk) {
			res.add(0, new NavigationButton("Add Marked Outliers to Global Outlier List", new ActionSetGlobalOutliers(experiment), src.getGUIsetting()));
		}
		
		return res;
	}
	
	public static ActionDataExportToVfs getSaveAnnotationsInVfsCommand(ExperimentReferenceInterface experiment, VirtualFileSystemVFS2 vv2) {
		return new ActionDataExportToVfs(experiment.getM(), experiment, vv2, false, null) {
			
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				setSkipClone(true);
				setSkipUpdateDBid(true);
				super.performActionCalculateResults(src);
			}
			
			@Override
			public boolean requestTitleUpdates() {
				return false;
			}
			
			@Override
			public String getDefaultTitle() {
				String res = "Save Annotation Changes" + (postResult != null ? "<br>" + postResult : "");
				return res;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				return null;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return currentSet;
			}
			
			@Override
			public String getDefaultImage() {
				return "img/ext/gpl2/Gnome-Emblem-Downloads-64.png";
			}
		};
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	public ExperimentReferenceInterface getExperimentReference() {
		return experiment;
	}
	
	@Override
	public String getDefaultTitle() {
		return "View Images";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Image-X-Generic-64.png";// user-desktop.png";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.experiment = experimentReference;
	}
}
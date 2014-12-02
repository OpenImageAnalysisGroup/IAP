package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;

import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ActionSaveWebCamImagesSelectSource extends AbstractNavigationAction implements ActionDataProcessing {
	private MongoDB m;
	private ExperimentReference experiment;
	private NavigationButton src;
	ArrayList<String> xmlOutput = new ArrayList<String>();
	
	ArrayList<NavigationButton> resB = new ArrayList<NavigationButton>();
	
	public ActionSaveWebCamImagesSelectSource() {
		super("Export WebCam Greenhouse Camera Images");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		resB.clear();
		getStatusProvider().setCurrentStatusText1("Determine Data Availability");
		
		try {
			ArrayList<String> fsl = m != null ? m.getWebCamStorageFileSystems() : new ArrayList<String>();
			getStatusProvider().setCurrentStatusText1("Determine Image-Count");
			for (String fs : fsl) {
				getStatusProvider().setCurrentStatusText2("Check " + fs);
				long numberOfImagesInRange = m.getWebCamStorageCount(
						fs,
						experiment.getHeader().getStartdate(),
						experiment.getHeader().getImportdate());
				if (numberOfImagesInRange > 0) {
					NavigationAction na = new ActionSaveWebCamImages(m, fs, numberOfImagesInRange,
							experiment.getHeader().getStartdate(),
							experiment.getHeader().getImportdate());
					NavigationButton nb = new NavigationButton(na, src.getGUIsetting());
					resB.add(nb);
				}
			}
			getStatusProvider().setCurrentStatusText2("");
		} catch (Exception e) {
			e.printStackTrace();
			MainFrame.showMessageDialog("Error: " + e.getMessage(), "Error");
		}
		
		getStatusProvider().setCurrentStatusText1("Select Data Source");
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
		res.addAll(resB);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (resB.isEmpty())
			return new MainPanelComponent(
					"During the run of the experiment no WebCam images have been saved in this storage location (database). "
							+
							"You need to enable the experiment Watch-Service and the connected Web-Cam storage option in order to be able to export a WebCam image series.");
		else
			return new MainPanelComponent(
					"Please select the camera source. You will be prompted to select a target folder for the storage of " +
							"a series of WebCam images, which were obtained and stored during the run of the experiment.");
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Video-X-Generic-64.png";// Gnome-Appointment-New-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Export WebCam Images";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.m = experimentReference.m;
		this.experiment = experimentReference;
	}
}
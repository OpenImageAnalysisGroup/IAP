package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;
import java.util.TreeMap;

import org.ErrorMsg;
import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ActionResetConditionFromImageName extends AbstractNavigationAction implements ActionDataProcessing {
	private MongoDB m;
	private ExperimentReference experiment;
	private NavigationButton src;
	
	public ActionResetConditionFromImageName() {
		super("Rebuild meta data and add file names to meta data");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
		try {
			ExperimentInterface res = experiment.getData();
			TreeMap<Integer, ArrayList<String>> replId2fileNames = new TreeMap<Integer, ArrayList<String>>();
			for (NumericMeasurementInterface nmi : Substance3D.getAllFiles(res)) {
				if (nmi instanceof BinaryMeasurement) {
					BinaryMeasurement bm = (BinaryMeasurement) nmi;
					if (bm.getURL() != null) {
						if (!replId2fileNames.containsKey(nmi.getReplicateID()))
							replId2fileNames.put(nmi.getReplicateID(), new ArrayList<String>());
						replId2fileNames.get(nmi.getReplicateID()).add(bm.getURL().getFileName());
					}
				}
			}
			ArrayList<MappingData3DPath> mp = MappingData3DPath.get(res, true);
			for (MappingData3DPath m : mp) {
				ConditionInterface c = m.getConditionData();
				int id = m.getMeasurement().getReplicateID();
				if (replId2fileNames.containsKey(id)) {
					c.setGenotype(StringManipulationTools.getStringList(replId2fileNames.get(id), "/"));
				}
				
			}
			res.clear();
			res.addAll(MappingData3DPath.merge(mp, false));
			((Experiment) res).sortSubstances();
			((Experiment) res).sortConditions();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
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
		res.add(new NavigationButton("Save Changes", new ActionCopyToMongo(m, experiment, true), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Existing meta data has been removed, new meta data is based on binary file names");
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Preferences-Desktop-Keyboard-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Set sequence from image file name(s)";
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
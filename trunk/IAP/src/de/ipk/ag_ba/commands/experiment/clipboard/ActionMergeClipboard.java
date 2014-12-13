package de.ipk.ag_ba.commands.experiment.clipboard;

import java.util.ArrayList;
import java.util.Date;

import org.MergeCompareRequirements;
import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentHeaderInfoPanel;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;

public class ActionMergeClipboard extends AbstractNavigationAction {
	
	private Experiment experimentResult;
	private NavigationButton srcButton;
	
	public ActionMergeClipboard(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (experimentResult != null)
			return;
		int nTodo = guiSetting.getClipboardItems().size();
		if (status != null)
			status.setCurrentStatusText1("About to merge " + nTodo + " data sets...");
		this.srcButton = src;
		
		Experiment e = new Experiment();
		int iii = 0;
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<String> sequences = new ArrayList<String>();
		Date firstExperimentStart = null;
		Date lastExperimentEnd = null;
		for (ExperimentReference i : guiSetting.getClipboardItems()) {
			String id = i.getHeader().getDatabaseId() + "";
			ids.add(id);
			String seq = i.getHeader().getSequence() + "";
			sequences.add(seq);
			ExperimentInterface ei = i.getData(false, status);
			names.add(ei.getName());
			ExperimentInterface eCopy = ei.clone();
			if (firstExperimentStart == null)
				firstExperimentStart = ei.getStartDate();
			else
				if (ei.getStartDate() != null && ei.getStartDate().compareTo(firstExperimentStart) < 0)
					firstExperimentStart = ei.getStartDate();
			if (lastExperimentEnd == null)
				lastExperimentEnd = ei.getImportDate();
			else
				if (ei.getImportDate() != null && ei.getImportDate().compareTo(lastExperimentEnd) > 0)
					lastExperimentEnd = ei.getImportDate();
			
			ei = null;
			for (SubstanceInterface si : eCopy)
				for (ConditionInterface ci : si) {
					String oldGC = ci.getGrowthconditions();
					if (oldGC != null && oldGC.length() > 0 && !oldGC.equals("not specified"))
						ci.setGrowthconditions(eCopy.getName() + ":" + oldGC);
					else
						ci.setGrowthconditions(eCopy.getName());
				}
			e.addAndMerge(null, eCopy, BackgroundThreadDispatcher.getRE(), new MergeCompareRequirements());
			iii++;
			if (status != null) {
				status.setCurrentStatusText1("Merged dataset " + iii + "/" + nTodo);
				status.setCurrentStatusValueFine(100d / nTodo * iii);
			}
		}
		e.getHeader().setDatabaseId("");
		e.getHeader().setDatabase("");
		e.getHeader().setRemark("Clipboard merge at " + SystemAnalysis.getCurrentTime());
		e.getHeader().setStartdate(firstExperimentStart);
		e.getHeader().setImportdate(lastExperimentEnd);
		e.getHeader().setExperimentname("Merged " + StringManipulationTools.getStringList(names, ", "));
		// e.getHeader().setExperimenttype(IAPexperimentTypes.AnalysisResults + "");
		// e.getHeader().setImportusergroup(IAPexperimentTypes.AnalysisResults + "");
		e.getHeader().setOriginDbId(StringManipulationTools.getStringList(ids, " // "));
		e.getHeader().setSequence(StringManipulationTools.getStringList(sequences, " // "));
		e.getHeader().setDatabaseId("");
		for (SubstanceInterface si : e) {
			for (ConditionInterface ci : si) {
				ci.setExperimentHeader(e.getHeader());
			}
		}
		boolean superMerge = false;
		if (superMerge) {
			ArrayList<MappingData3DPath> mdpl = MappingData3DPath.get(e);
			if (status != null)
				status.setCurrentStatusText1("Deep merge operation...");
			e = (Experiment) MappingData3DPath.merge(mdpl, false);
			if (status != null)
				status.setCurrentStatusText1("Operation finished");
		}
		
		if (status != null)
			status.setCurrentStatusText1("Sort substances...");
		e.sortSubstances();
		if (status != null)
			status.setCurrentStatusText1("Operation finished");
		this.experimentResult = e;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> newRes = new ArrayList<NavigationButton>();
		newRes.add(currentSet.get(0));
		newRes.add(srcButton);
		return newRes;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		
		for (ActionDataProcessing adp : IAPpluginManager.getInstance()
				.getExperimentProcessingActions(new ExperimentReference(experimentResult), true))
			res.add(new NavigationButton(adp, guiSetting));
		
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Merge Clipboard";
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ExperimentHeaderInfoPanel ip = new ExperimentHeaderInfoPanel();
		ip.setExperimentInfo(null, experimentResult.getHeader(), true, experimentResult);
		return new MainPanelComponent(ip, true);
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getMergeDatasets();
	}
}

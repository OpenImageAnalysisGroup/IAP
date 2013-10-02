package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.TreeMap;

import org.SystemAnalysis;

import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class ExperimentHistoryNavigationAction extends AbstractNavigationAction {
	
	private final TreeMap<Long, ExperimentHeaderInterface> history;
	private NavigationButton src;
	private final MongoDB m;
	
	public ExperimentHistoryNavigationAction(TreeMap<Long, ExperimentHeaderInterface> history, MongoDB m) {
		super("Access older versions");
		this.history = history;
		this.m = m;
	}
	
	@Override
	public String getDefaultTitle() {
		return "History (" + history.size() + ")";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return getDefaultImage();
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getFileHistory();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (Long time : history.keySet()) {
			ExperimentHeaderInterface ei = history.get(time);
			String t = SystemAnalysis.getCurrentTime(time);
			res.add(0, new NavigationButton(t, new ActionMongoOrLemnaTecExperimentNavigation(ei, m), src.getGUIsetting()));
		}
		res.add(0, new NavigationButton(new ActionTrash(history.values(), DeletionCommand.TRASH_GROUP_OF_EXPERIMENTS, m), src.getGUIsetting()));
		return res;
	}
}
package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;
import java.util.TreeSet;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionFilterConditions extends AbstractNavigationAction {
	
	private ArrayList<ThreadSafeOptions> metaDataColumns;
	private ExperimentReference experimentReference;
	private int ngroups = -1;
	private String oldPattern = "";
	public ArrayList<ThreadSafeOptions> groupSelection;
	private NavigationButton src;
	
	public ActionFilterConditions(String tooltip) {
		super(tooltip);
	}
	
	public ActionFilterConditions(ArrayList<ThreadSafeOptions> metaDataColumns, ArrayList<ThreadSafeOptions> groupSelection,
			ExperimentReference experimentReference) {
		this("Filter experiment groups");
		this.metaDataColumns = metaDataColumns;
		this.groupSelection = groupSelection;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (ThreadSafeOptions tso : groupSelection)
			res.add(new NavigationButton(new ActionThreadSafeOptionsBooleanEditor(tso), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		StringBuilder settings = new StringBuilder();
		for (ThreadSafeOptions tso : metaDataColumns) {
			settings.append("/" + tso.getBval(0, false));
		}
		String pattern = settings.toString();
		if (!pattern.equals(oldPattern)) {
			ngroups = -1;
		}
		if (experimentReference.getExperimentPeek() != null) {
			if (ngroups < 0)
				try {
					oldPattern = pattern;
					ngroups = countAndInitGroups();
				} catch (Exception e) {
					ngroups = -2;
				}
			int sel = 0;
			for (ThreadSafeOptions tso : groupSelection)
				if (tso.getBval(0, false))
					sel++;
			return "Filter Groups<br><font color='gray'><small>" + sel + "/" + (ngroups == -2 ? "[group count error]" : ngroups)
					+ " groups selected</small></font>";
		} else
			return "Filter Groups<br><font color='gray'><small>(data is beeing loaded)</small></font>";
	}
	
	private int countAndInitGroups() throws Exception {
		TreeSet<String> gl = new TreeSet<String>();
		ExperimentInterface ei = experimentReference.getData();
		for (SubstanceInterface si : ei) {
			for (ConditionInterface c : si) {
				gl.add(getConditionInfoString(c));
			}
		}
		groupSelection.clear();
		for (String g : gl) {
			groupSelection.add(new ThreadSafeOptions().setParam(0, g).setBval(0, true));
		}
		return gl.size();
	}
	
	private String getConditionInfoString(ConditionInterface c) {
		StringBuilder sb = new StringBuilder();
		for (ThreadSafeOptions tso : metaDataColumns) {
			if (!tso.getBval(0, false))
				continue;
			ConditionInfo ci = (ConditionInfo) tso.getParam(1, ConditionInfo.IGNORED_FIELD);
			String v = c.getField(ci);
			if (v == null || v.isEmpty())
				continue;
			if (sb.length() > 0)
				sb.append("<br>");
			sb.append(ci + ": " + v);
		}
		return sb.toString();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Sort-Selection-64 Filter.png";
	}
}

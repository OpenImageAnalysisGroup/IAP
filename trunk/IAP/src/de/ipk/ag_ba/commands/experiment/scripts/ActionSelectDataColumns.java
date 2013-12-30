package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionSelectDataColumns extends AbstractNavigationAction {
	
	private Collection<String> desiredDataColumns;
	private final ArrayList<String> confirmedColumns = new ArrayList<String>();
	private final ArrayList<String> descriptions = new ArrayList<String>();
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	public ActionSelectDataColumns(String tooltip) {
		super(tooltip);
	}
	
	public ActionSelectDataColumns(String tooltip, final Collection<String> desiredDataColumns, final ExperimentReference experimentReference) {
		this(tooltip);
		this.desiredDataColumns = desiredDataColumns;
		this.experimentReference = experimentReference;
		experimentReference.runAsDataBecomesAvailable(new Runnable() {
			
			@Override
			public void run() {
				confirmedColumns.clear();
				descriptions.clear();
				ExperimentInterface e = experimentReference.getExperimentPeek();
				if (desiredDataColumns != null) {
					for (String dc : desiredDataColumns) {
						colMatchLoop: for (String colDesired : dc.split("//")) {
							colDesired = colDesired.trim();
							String colName = colDesired.split(":", 2)[0];
							String desc = colDesired.split(":", 2).length > 1 ? colDesired.split(":", 2)[1] : colName;
							for (SubstanceInterface s : e) {
								String name = s.getName();
								if (name.equalsIgnoreCase(colName)) {
									confirmedColumns.add(colName);
									descriptions.add(desc);
									break colMatchLoop;
								}
							}
						}
					}
				}
			}
		});
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		experimentReference.getData();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		int idx = 0;
		for (String colName : confirmedColumns) {
			ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setBval(0, true);
			tso.setParam(0, descriptions.get(idx));
			tso.setParam(1, colName);
			ActionThreadSafeOptionsBooleanEditor action = new ActionThreadSafeOptionsBooleanEditor(tso);
			res.add(new NavigationButton(action, src.getGUIsetting()));
			idx++;
		}
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		if (experimentReference.getExperimentPeek() != null)
			return "Data Columns<br><font color='gray'><small>" + confirmedColumns.size() + " selected</small></font>";
		else
			return "Data Columns<br><font color='gray'><small>(data is beeing loaded)</small></font>";
		
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Applications-Engineering-64.png";
	}
	
}

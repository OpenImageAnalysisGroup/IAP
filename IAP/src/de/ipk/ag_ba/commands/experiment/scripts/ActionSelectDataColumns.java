package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionSelectDataColumns extends AbstractNavigationAction {
	
	private Collection<String> desiredDataColumns;
	private final ArrayList<String> confirmedColumns = new ArrayList<String>();
	private final ArrayList<String> descriptions = new ArrayList<String>();
	private ExperimentReferenceInterface experimentReference;
	private NavigationButton src;
	private ArrayList<ThreadSafeOptions> listOfColumns = null;
	private final ThreadSafeOptions dataEvaluated = new ThreadSafeOptions();
	
	public ActionSelectDataColumns(String tooltip) {
		super(tooltip);
	}
	
	public ActionSelectDataColumns(String tooltip, final Collection<String> desiredDataColumns,
			final ExperimentReferenceInterface experimentReference, ArrayList<ThreadSafeOptions> listOfColumns) {
		this(tooltip);
		dataEvaluated.setBval(0, false);
		this.desiredDataColumns = desiredDataColumns;
		this.experimentReference = experimentReference;
		this.listOfColumns = listOfColumns;
		boolean newInit = true;
		
		final boolean fni = newInit;
		
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
							if (colDesired.startsWith("RESULT_"))
								colDesired = colDesired.substring("RESULT_".length());
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
					
					if (fni) {
						int idx = 0;
						for (String colName : confirmedColumns) {
							ThreadSafeOptions tso;
							tso = new ThreadSafeOptions();
							tso.setBval(0, true);
							tso.setParam(0, colName);
							tso.setParam(10, descriptions.get(idx));
							tso.setInt(Integer.MAX_VALUE);
							ActionSelectDataColumns.this.listOfColumns.add(tso);
							idx++;
						}
					}
					
				}
				dataEvaluated.setBval(0, true);
			}
		});
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		experimentReference.getData();
		dataEvaluated.waitForBoolean(0);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (ThreadSafeOptions tso : listOfColumns) {
			ActionThreadSafeOptionsBooleanEditor action = new ActionThreadSafeOptionsBooleanEditor(tso);
			res.add(new NavigationButton(action, src.getGUIsetting()));
		}
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		if (experimentReference.getExperimentPeek() != null) {
			int cnt = 0;
			for (ThreadSafeOptions tso : listOfColumns) {
				if (tso.getBval(0, false))
					cnt++;
			}
			return "Data Columns<br><font color='gray'><small>" + cnt + "/" + listOfColumns.size() + " selected</small></font>";
		} else
			return "Data Columns<br><font color='gray'><small>(data is beeing loaded)</small></font>";
		
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Applications-Engineering-64.png";
	}
	
	public ArrayList<ThreadSafeOptions> getDataColumnList() {
		return listOfColumns;
	}
	
}

package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;

/**
 * @author klukas
 */
public class ActionSelectMetaDataColumns extends AbstractNavigationAction {
	
	private ArrayList<ThreadSafeOptions> metaDataColumns;
	private NavigationButton src;
	private ExperimentReferenceInterface experimentReference;
	
	public ActionSelectMetaDataColumns(String tooltip) {
		super(tooltip);
	}
	
	public ActionSelectMetaDataColumns(ArrayList<ThreadSafeOptions> metaDataColumns,
			ExperimentReferenceInterface experimentReference) {
		this("Select metadata columns");
		this.metaDataColumns = metaDataColumns;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		experimentReference.getData(getStatusProvider());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (ThreadSafeOptions tso : metaDataColumns) {
			// String propertyName = (String) tso.getParam(0, "[Unknown Metadata Field Name]");
			// ConditionInfo property = (ConditionInfo) tso.getParam(1, ConditionInfo.IGNORED_FIELD);
			// boolean sel = tso.getBval(0, false);
			NavigationButton nb = new NavigationButton(new ActionThreadSafeOptionsBooleanEditor(tso), src.getGUIsetting());
			if (tso.getInt() < 1)
				nb.setEnabled(false);
			res.add(nb);
		}
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		int n = countSelected();
		if (experimentReference.getExperimentPeek() != null)
			return "<html><center>Group Definition<br><small><font color='gray'>"
					+ n + " field" + (n != 1 ? "s" : "") + " selected</font></small></center>";
		else
			return "<html><center>Group Definition<br><font color='gray'><small>(data is beeing loaded)</small></font>";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Sort-Selection-64.png";
	}
	
	private int countSelected() {
		int r = 0;
		if (metaDataColumns != null)
			for (ThreadSafeOptions tso : metaDataColumns)
				if (tso.getBval(0, false))
					r++;
		return r;
	}
}

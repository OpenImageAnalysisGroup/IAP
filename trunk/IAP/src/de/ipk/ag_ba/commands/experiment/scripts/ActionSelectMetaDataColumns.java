package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class ActionSelectMetaDataColumns extends AbstractNavigationAction {
	
	private ArrayList<ThreadSafeOptions> metaDataColumns;
	private NavigationButton src;
	
	public ActionSelectMetaDataColumns(String tooltip) {
		super(tooltip);
	}
	
	public ActionSelectMetaDataColumns(ArrayList<ThreadSafeOptions> metaDataColumns) {
		this("Select metadata columns");
		this.metaDataColumns = metaDataColumns;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (ThreadSafeOptions tso : metaDataColumns) {
			// String propertyName = (String) tso.getParam(0, "[Unknown Metadata Field Name]");
			// ConditionInfo property = (ConditionInfo) tso.getParam(1, ConditionInfo.IGNORED_FIELD);
			// boolean sel = tso.getBval(0, false);
			res.add(new NavigationButton(new ActionThreadSafeOptionsBooleanEditor(tso), src.getGUIsetting()));
		}
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		int n = countSelected();
		return "<html><center>Metadata Columns<br><small><font color='gray'>" + n + " selected</font></small></center>";
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

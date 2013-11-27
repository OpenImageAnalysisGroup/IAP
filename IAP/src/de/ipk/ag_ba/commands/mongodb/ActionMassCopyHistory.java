package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.server.task_management.MassCopySupport;

public final class ActionMassCopyHistory extends AbstractNavigationAction {
	private NavigationButton src;
	
	public ActionMassCopyHistory(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getArchive();
	}
	
	@Override
	public String getDefaultTitle() {
		return "DB-Copy History";
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> htmlTextPanels = new ArrayList<String>();
		htmlTextPanels.add(MassCopySupport.getInstance().getHistory(Integer.MAX_VALUE,
				"" +
						"<p>Full Copy-History:<br><br><ul>",
				"<li>", "", ""));
		
		return new MainPanelComponent(htmlTextPanels);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> set = new ArrayList<NavigationButton>(currentSet);
		set.add(src);
		return set;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		
		return res;
	}
}
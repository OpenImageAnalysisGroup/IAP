package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabAglet;

/**
 * @author Christian Klukas
 */
public class ActionChat extends AbstractNavigationAction {
	
	private NavigationButton src;
	TabAglet ta = new TabAglet();
	
	public ActionChat(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<>();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(ta);
	}
	
	long lastRequest = 0;
	
	@Override
	public String getDefaultTitle() {
		return "<html>Chat<br><small>(" + getChatStatus() + ")";
	}
	
	private String getChatStatus() {
		// if (ta == null)
		return "not initialized";
		// else
		// return StringManipulationTools.removeHTMLtags(ta.status);
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Mail-Send-Receive-64.png";
	}
}

package de.ipk.ag_ba.commands.about;

import java.util.ArrayList;

import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.plugin_info.PluginInfoHelper;

/**
 * @author Christian Klukas
 */
public class ActionAboutPlugins extends AbstractNavigationAction {
	
	public ActionAboutPlugins(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> ll = new ArrayList<String>();
		ll.add("<html><h2><font face=\"Sans,Tohama,Arial\">System Features - Loaded Plugins</font></h2>" +
				"<font face=\"Sans,Tohama,Arial\">" +
				PluginInfoHelper.pretifyPluginList(MainFrame.getInstance().getPluginManager().getPluginEntries())
				+ "</font>");
		
		MainPanelComponent mp = new MainPanelComponent(ll);// , new Color(0, 0, 20));
		return mp;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Loaded Plugins";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Application-X-Executable-64.png";
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}

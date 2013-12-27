package de.ipk.ag_ba.commands.experiment.scripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.datasource.WebUrlAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class AbstractRscriptExecutionAction extends AbstractNavigationAction {
	
	private final String scriptIniLocation;
	private final ArrayList<String> res = new ArrayList<String>();
	private final ArrayList<String> urls = new ArrayList<String>();
	private final ArrayList<String> urlDescriptions = new ArrayList<String>();
	private String title;
	private String iconDef;
	String cmd;
	String[] params;
	
	public AbstractRscriptExecutionAction(String tooltip, String scriptIniLocation) throws IOException {
		super(tooltip);
		this.scriptIniLocation = scriptIniLocation;
		readInfo();
	}
	
	private void readInfo() throws IOException {
		ScriptHelper sh = new ScriptHelper(scriptIniLocation, null);
		title = sh.getTitle();
		iconDef = sh.getIcon();
		cmd = sh.getCommand();
		params = sh.getParams();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		res.clear();
		TreeMap<Long, String> ro = ScriptExecutor.start(getDefaultTitle(), cmd, params, getStatusProvider(), 1);
		res.add("<code>" + StringManipulationTools.getStringList(ro.values(), "<br>")
				+ "</code>");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> ra = new ArrayList<NavigationButton>();
		for (int i = 0; i < urls.size(); i++) {
			ra.add(
					new NavigationButton(urlDescriptions.get(i),
							new WebUrlAction(new IOurl(urls.get(i)), "Show " + urlDescriptions.get(i)), guiSetting));
		}
		return ra;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(res);
	}
	
	@Override
	public String getDefaultTitle() {
		return title;
	}
	
	@Override
	public String getDefaultImage() {
		if (iconDef != null && !iconDef.isEmpty())
			return iconDef;
		else
			return "img/ext/gpl2/Gnome-Dialog-Information-64.png";
	}
}

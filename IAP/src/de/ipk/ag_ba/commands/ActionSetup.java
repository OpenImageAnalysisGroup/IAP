package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.Collections;

import org.AttributeHelper;
import org.ReleaseInfo;
import org.SystemOptions;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class ActionSetup extends AbstractNavigationAction {
	
	ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
	private final String iniFileName;
	private final String title;
	
	public ActionSetup(String iniFileName, String tooltip, String title) {
		super(tooltip);
		this.iniFileName = iniFileName;
		this.title = title;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		res.clear();
		res.add(new NavigationButton(new AbstractNavigationAction(
				"Open file explorer and show settings file (" +
						(iniFileName == null ? "iap.ini" : iniFileName) + ")") {
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				AttributeHelper.showInFileBrowser(ReleaseInfo.getAppFolder(), iniFileName == null ? "iap.ini" : iniFileName);
			}
			
			@Override
			public String getDefaultTitle() {
				return "Show Config-File";
			}
			
			@Override
			public String getDefaultImage() {
				return "img/ext/gpl2/Gnome-Document-Open-64.png";
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return currentSet;
			}
			
			@Override
			public boolean getProvidesActions() {
				return false;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
		}, src.getGUIsetting()));
		ArrayList<String> ss = SystemOptions.getInstance(iniFileName).getSectionTitles();
		Collections.sort(ss);
		for (String s : ss) {
			res.add(new NavigationButton(new ActionSettingsEditor(iniFileName, "Change settings of section " + s, s), src.getGUIsetting()));
		}
		if (iniFileName == null)
			for (PipelineDesc pd : PipelineDesc.getKnownPipelines())
				res.add(new NavigationButton(
						new ActionSetup(pd.getIniFileName(),
								"Change settings of " + pd.getName() + " analysis pipeline",
								"Settings of " + pd.getName() + ""),
						src.getGUIsetting()));
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Click on a action button above to edit the corresponding setting.<br><br>" +
				"Most settings are active immediately, but some " +
				"options may require a restart of the progam upon setting change.");
	}
	
	@Override
	public String getDefaultTitle() {
		return title;
	}
	
	@Override
	public String getDefaultImage() {
		if (iniFileName == null)
			return IAPimages.getToolbox();
		else
			return "img/ext/gpl2/Gnome-Applications-Science-64.png";
	}
}

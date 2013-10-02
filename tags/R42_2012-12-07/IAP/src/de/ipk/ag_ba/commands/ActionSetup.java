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
	private NavigationButton src;
	
	public ActionSetup(String iniFileName, String tooltip, String title) {
		super(tooltip);
		this.iniFileName = iniFileName;
		this.title = title;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
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
		ArrayList<NavigationButton> rr = new ArrayList<NavigationButton>(res);
		Book book = new Book(null, "Help", "http://iap.ipk-gatersleben.de/", "img/dataset.png");
		rr.add(0, book.getNavigationButton(src));
		return rr;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> descs = new ArrayList<String>();
		descs.add("<b>Click on a action button above to open a settings-group or to edit the corresponding setting.</b><br><br>" +
				"Most settings are active immediately, but some " +
				"options may require a restart of the progam upon setting change.");
		descs.add("<b>The values within a specific group may be removed/reverted to their defaults:</b><br><br>" +
				"The &quot;Defaults (delayed)&quot; command, shown for a selected settings-group, removes the shown settings and their values.<br>" +
				"The settings will re-appear as soon as they are needed, and will be reverted to the " +
				"programmed defaults.");
		return new MainPanelComponent(descs);
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
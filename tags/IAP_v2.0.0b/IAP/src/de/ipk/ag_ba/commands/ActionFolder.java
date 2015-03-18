package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class ActionFolder extends AbstractNavigationAction {
	
	private NavigationAction[] navigationActions;
	private GUIsetting guIsetting;
	private NavigationButton src;
	private String title;
	private String customIcon;
	
	public ActionFolder(String tooltip) {
		super(tooltip);
	}
	
	public ActionFolder(
			String title,
			String tooltip, NavigationAction[] navigationActions, GUIsetting guIsetting) {
		this(tooltip);
		this.setTitle(title);
		this.navigationActions = navigationActions;
		this.guIsetting = guIsetting;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (navigationActions != null)
			for (NavigationAction na : navigationActions)
				res.add(new NavigationButton(na, guIsetting));
		return res;
	}
	
	@Override
	public String getDefaultImage() {
		if (customIcon == null)
			return IAPimages.getToolbox();
		else
			return customIcon;
	}
	
	@Override
	public String getDefaultTitle() {
		return getTitle();
	}
	
	public void setIcon(String icon) {
		this.customIcon = icon;
	}
	
	private String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
}

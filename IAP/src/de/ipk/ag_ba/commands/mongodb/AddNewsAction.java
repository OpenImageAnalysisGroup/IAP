/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;

import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

/**
 * @author klukas
 */
public class AddNewsAction extends AbstractNavigationAction implements NavigationAction {
	
	public AddNewsAction() {
		super("Add News Item");
	}
	
	private NavigationButton src;
	
	String result = "Internal Error";
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		return result;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>(currentSet);
		result.add(src);
		return result;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		Object[] res = MyInputHelper.getInput("Enter news text.", "Post News Item",
				new Object[] {
						"Name", SystemAnalysis.getUserName(),
						"News Text", ""
		});
		if (res != null) {
			String user = (String) res[0];
			String text = (String) res[1];
			MongoDB.getDefaultCloud().addNewsItem(text, user);
			result = "Posted news item.";
		} else
			result = "Cancelled.";
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(result);
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getAddNews();
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getAddNews();
	}
	
	@Override
	public String getDefaultTitle() {
		return "Post News";
	}
	
}

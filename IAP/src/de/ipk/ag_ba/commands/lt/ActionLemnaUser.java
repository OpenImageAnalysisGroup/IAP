/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;
import java.util.Collection;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class ActionLemnaUser extends AbstractNavigationAction implements NavigationAction {
	
	private NavigationButton src;
	private final String user;
	private final Collection<ExperimentHeaderInterface> ids;
	
	public ActionLemnaUser(String user, Collection<ExperimentHeaderInterface> ids) {
		super("Show experiments of user " + user);
		this.user = user;
		this.ids = ids;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		for (ExperimentHeaderInterface id : ids) {
			ExperimentReference exp = new ExperimentReference(id);
			result.add(new NavigationButton(new ActionMongoOrLTexperimentNavigation(exp), src.getGUIsetting()));
		}
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
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getFolderRemoteClosed();
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getFolderRemoteOpen();
	}
	
	@Override
	public String getDefaultTitle() {
		return user + " (" + ids.size() + ")";
	}
	
}

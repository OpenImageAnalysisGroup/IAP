/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import java.util.ArrayList;
import java.util.Collection;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.postgresql.LemnaExperimentNavigationAction;

/**
 * @author klukas
 * 
 */
public class LemnaUserAction extends AbstractNavigationAction implements NavigationAction {

	private NavigationGraphicalEntity src;
	private final String user;
	private final Collection<String> ids;

	public LemnaUserAction(String user, Collection<String> ids) {
		super("Show experiments of user " + user);
		this.user = user;
		this.ids = ids;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> result = new ArrayList<NavigationGraphicalEntity>();
		for (String id : ids) {
			String db = id.split(":", 2)[0];
			String experiment = id.split(":", 2)[1];
			result.add(new NavigationGraphicalEntity(new LemnaExperimentNavigationAction(db, experiment)));
		}
		return result;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> result = new ArrayList<NavigationGraphicalEntity>(currentSet);
		result.add(src);
		return result;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		this.src = src;
	}

	@Override
	public String getDefaultImage() {
		return "img/ext/folder-remote.png";
	}

	@Override
	public String getDefaultNavigationImage() {
		return "img/ext/folder-remote-open.png";
	}

	@Override
	public String getDefaultTitle() {
		return user;
	}

}

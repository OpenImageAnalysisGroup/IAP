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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 * 
 */
public class LemnaUserAction extends AbstractNavigationAction implements NavigationAction {

	private NavigationGraphicalEntity src;
	private final String user;
	private final Collection<ExperimentHeaderInterface> ids;

	public LemnaUserAction(String user, Collection<ExperimentHeaderInterface> ids) {
		super("Show experiments of user " + user);
		this.user = user;
		this.ids = ids;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> result = new ArrayList<NavigationGraphicalEntity>();
		for (ExperimentHeaderInterface id : ids) {
			result.add(new NavigationGraphicalEntity(new LemnaExperimentNavigationAction(id)));
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

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
public class LemnaDbAction extends AbstractNavigationAction implements NavigationAction {

	private NavigationGraphicalEntity src;
	private final String db;
	private final Collection<String> experiments;

	public LemnaDbAction(String db, Collection<String> experiments) {
		super("Open LemnaTec-DB " + db);
		this.db = db;
		this.experiments = experiments;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> result = new ArrayList<NavigationGraphicalEntity>();
		for (String experiment : experiments) {
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
		return "img/DBE2_logo-gray.png";
	}

	@Override
	public String getDefaultNavigationImage() {
		return "img/DBE2_logo.png";
	}

	@Override
	public String getDefaultTitle() {
		return db;
	}

}

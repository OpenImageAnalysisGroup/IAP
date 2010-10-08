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
import de.ipk_gatersleben.ag_ba.postgresql.LemnaTecDataExchange;

/**
 * @author klukas
 * 
 */
public class LemnaTecNavigationAction extends AbstractNavigationAction implements NavigationAction {

	private NavigationGraphicalEntity src;

	public LemnaTecNavigationAction() {
		super("Access LemnaTec-DB");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #getResultNewActionSet()
	 */
	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> result = new ArrayList<NavigationGraphicalEntity>();
		try {
			result.add(new NavigationGraphicalEntity(new LemnaTecUserNavigationAction()));
			for (String db : new LemnaTecDataExchange().getDatabases()) {
				try {
					Collection<String> experiments = new LemnaTecDataExchange().getExperimentInDatabase(db);
					if (experiments.size() > 0)
						result.add(new NavigationGraphicalEntity(new LemnaDbAction(db, experiments)));
					else
						System.out.println("Database " + db + " is empty.");
				} catch (Exception e) {
					System.out.println("Database " + db + " could not be processed.");
				}
			}
		} catch (Exception e) {
			// error
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #getResultNewNavigationSet(java.util.ArrayList)
	 */
	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> result = new ArrayList<NavigationGraphicalEntity>(currentSet);
		result.add(src);
		return result;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		// connect to db
		this.src = src;
	}

	@Override
	public String getDefaultImage() {
		return "img/ext/lemna.png";
	}

	@Override
	public String getDefaultNavigationImage() {
		return "img/ext/lemna-active.png";
	}

	@Override
	public String getDefaultTitle() {
		return "LemnaTec-DB";
	}

}

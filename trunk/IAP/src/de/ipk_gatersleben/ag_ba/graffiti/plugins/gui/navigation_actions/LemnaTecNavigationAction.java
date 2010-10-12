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
import java.util.TreeMap;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 * 
 */
public class LemnaTecNavigationAction extends AbstractNavigationAction implements NavigationAction {

	private NavigationGraphicalEntity src;
	private String login;
	private String pass;

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

			TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> allExperiments = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
			allExperiments.put("", new TreeMap<String, ArrayList<ExperimentHeaderInterface>>());
			allExperiments.get("").put("", new ArrayList<ExperimentHeaderInterface>());
			for (String db : new LemnaTecDataExchange().getDatabases()) {
				try {
					Collection<ExperimentHeaderInterface> experiments = new LemnaTecDataExchange()
							.getExperimentInDatabase(db);
					if (experiments.size() > 0)
						result.add(new NavigationGraphicalEntity(new LemnaDbAction(db, experiments)));
					else
						System.out.println("Database " + db + " is empty.");
					for (ExperimentHeaderInterface ehi : experiments) {
						allExperiments.get("").get("").add(ehi);
					}

				} catch (Exception e) {
					System.out.println("Database " + db + " could not be processed.");
				}
			}
			result.add(1, Other.getCalendarEntity(allExperiments, login, pass));

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

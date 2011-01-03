/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class LemnaTecNavigationAction extends AbstractNavigationAction implements NavigationAction {
	
	private NavigationButton src;
	private String login;
	private String pass;
	ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
	
	public LemnaTecNavigationAction() {
		super("Access LemnaTec-DB");
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #getResultNewActionSet()
	 */
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction
	 * #getResultNewNavigationSet(java.util.ArrayList)
	 */
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>(currentSet);
		result.add(src);
		return result;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// connect to db
		this.src = src;
		result.clear();
		try {
			result.add(new NavigationButton(new LemnaTecUserNavigationAction(), src.getGUIsetting()));
			
			TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> allExperiments = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
			allExperiments.put("", new TreeMap<String, ArrayList<ExperimentHeaderInterface>>());
			allExperiments.get("").put("", new ArrayList<ExperimentHeaderInterface>());
			for (String db : new LemnaTecDataExchange().getDatabases()) {
				try {
					Collection<ExperimentHeaderInterface> experiments = new LemnaTecDataExchange()
										.getExperimentInDatabase(db);
					if (experiments.size() > 0)
						result.add(new NavigationButton(new LemnaDbAction(db, experiments), src.getGUIsetting()));
					else
						System.out.println("Database " + db + " is empty.");
					for (ExperimentHeaderInterface ehi : experiments) {
						allExperiments.get("").get("").add(ehi);
					}
					
				} catch (Exception e) {
					System.out.println("Database " + db + " could not be processed.");
				}
			}
			result.add(1, Other.getCalendarEntity(allExperiments, null, src.getGUIsetting()));
			
		} catch (Exception e) {
			// error
		}
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

/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
	private ArrayList<String> listOfDatabases = null;
	private final TreeMap<String, Collection<ExperimentHeaderInterface>> experimentMap = new TreeMap<String, Collection<ExperimentHeaderInterface>>();
	
	public LemnaTecNavigationAction() {
		super("Access LemnaTec-DB");
	}
	
	public void setLogin(String user) {
		this.login = user;
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
			result.add(new NavigationButton(new LemnaTecLogoutAction(), src.getGUIsetting()));
			result.add(new NavigationButton(new LemnaTecUserNavigationAction(login), src.getGUIsetting()));
			
			TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> allExperiments = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
			allExperiments.put("", new TreeMap<String, ArrayList<ExperimentHeaderInterface>>());
			allExperiments.get("").put("", new ArrayList<ExperimentHeaderInterface>());
			listOfDatabases = listOfDatabases != null ? listOfDatabases : new ArrayList<String>(new LemnaTecDataExchange().getDatabases());
			Collections.sort(listOfDatabases, new Comparator<String>() {
				@Override
				public int compare(String arg0, String arg1) {
					if (known(arg0) && !known(arg1))
						return -1;
					if (!known(arg0) && known(arg1))
						return 1;
					return arg0.compareTo(arg1);
				}
			});
			ArrayList<NavigationButton> unsorted = new ArrayList<NavigationButton>();
			NavigationButton nb = new NavigationButton(new LemnaTecDatabaseCollectionAction(unsorted), src.getGUIsetting());
			for (String db : listOfDatabases) {
				try {
					if (!experimentMap.containsKey(db))
						experimentMap.put(db, new LemnaTecDataExchange()
										.getExperimentsInDatabase(login, db));
					Collection<ExperimentHeaderInterface> experiments = experimentMap.get(db);
					if (experiments.size() > 0) {
						if (!known(db))
							unsorted.add(new NavigationButton(new LemnaDbAction(db, experiments), src.getGUIsetting()));
						else
							result.add(new NavigationButton(new LemnaDbAction(db, experiments), src.getGUIsetting()));
					} else
						System.out.println("Database " + db + " is empty.");
					for (ExperimentHeaderInterface ehi : experiments) {
						allExperiments.get("").get("").add(ehi);
					}
					
				} catch (Exception e) {
					System.out.println("Database " + db + " could not be processed. (" + e.getMessage() + ")");
				}
			}
			if (unsorted.size() > 0)
				result.add(nb);
			result.add(1, Other.getCalendarEntity(allExperiments, null, src.getGUIsetting()));
			
		} catch (Exception e) {
			// error
		}
	}
	
	protected boolean known(String arg1) {
		return arg1 != null && (arg1.startsWith("CGH_") || arg1.startsWith("BGH_") || arg1.startsWith("APH_"));
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
		return "LemnaTec";
	}
	
}

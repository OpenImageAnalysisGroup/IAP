/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.lemnatec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.DataSourceNavigationAction;
import de.ipk.ag_ba.commands.Other;
import de.ipk.ag_ba.datasources.http_folder.HTTPfolderSource;
import de.ipk.ag_ba.datasources.http_folder.LemnaTecDokuSource;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class ActionLemnaTecNavigation extends AbstractNavigationAction implements NavigationAction {
	
	private NavigationButton src;
	private String login;
	private String pass;
	ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
	private ArrayList<String> listOfDatabases = null;
	private final TreeMap<String, ArrayList<ExperimentHeaderInterface>> experimentMap = new TreeMap<String, ArrayList<ExperimentHeaderInterface>>();
	
	public ActionLemnaTecNavigation() {
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
			if (IAPmain.getRunMode() == IAPrunMode.WEB)
				result.add(new NavigationButton(new ActionLemnaTecLogout(), src.getGUIsetting()));
			
			result.add(new NavigationButton(new ActionLemnaTecUserNavigation(login), src.getGUIsetting()));
			
			TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> allExperiments = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
			allExperiments.put("", new TreeMap<String, ArrayList<ExperimentHeaderInterface>>());
			allExperiments.get("").put("", new ArrayList<ExperimentHeaderInterface>());
			listOfDatabases = listOfDatabases != null ? listOfDatabases : new ArrayList<String>(new LemnaTecDataExchange().getDatabases());
			Collections.sort(listOfDatabases, new Comparator<String>() {
				@Override
				public int compare(String arg0, String arg1) {
					if (LemnaTecDataExchange.known(arg0) && !LemnaTecDataExchange.known(arg1))
						return -1;
					if (!LemnaTecDataExchange.known(arg0) && LemnaTecDataExchange.known(arg1))
						return 1;
					return arg0.compareTo(arg1);
				}
			});
			ArrayList<NavigationButton> unsorted = new ArrayList<NavigationButton>();
			NavigationButton nb = new NavigationButton(new ActionLemnaTecDatabaseCollection(unsorted), src.getGUIsetting());
			int n = 0;
			int idx = -1;
			int max = listOfDatabases.size();
			
			for (String db : listOfDatabases) {
				idx++;
				status.setCurrentStatusValueFine(idx / (double) max * 100d);
				if (!LemnaTecDataExchange.known(db))
					continue;
				status.setCurrentStatusText1(n + " experiments");
				try {
					if (!experimentMap.containsKey(db)) {
						ArrayList<ExperimentHeaderInterface> res = new LemnaTecDataExchange().
								getExperimentsInDatabase(login, db, status);
						n += res.size();
						experimentMap.put(db, res);
					}
					ArrayList<ExperimentHeaderInterface> experiments = experimentMap.get(db);
					if (experiments.size() > 0) {
						if (!LemnaTecDataExchange.known(db))
							unsorted.add(new NavigationButton(new ActionLemnaDb(db, experiments), src.getGUIsetting()));
						else
							result.add(new NavigationButton(new ActionLemnaDb(db, experiments), src.getGUIsetting()));
					}
					// else System.out.println("Database " + db + " is empty.");
					for (ExperimentHeaderInterface ehi : experiments) {
						allExperiments.get("").get("").add(ehi);
					}
					
				} catch (Exception e) {
					if (e.getMessage() == null || !e.getMessage().equals("ERROR: relation \"snapshot\" does not exist"))
						System.out.println("Database " + db + " could not be processed. (" + e.getMessage() + ")");
				}
			}
			if (unsorted.size() > 0)
				result.add(nb);
			result.add(1, Other.getCalendarEntity(allExperiments, null, src.getGUIsetting()));
			
			result.add(2, new NavigationButton(new ActionMetaData("View Meta-Data for Experiments"), src.getGUIsetting()));
			
			if (IAPoptions.getInstance().getBoolean("LemnaTec-Site-Documentation", "show_icon", true)) {
				HTTPfolderSource doku = new LemnaTecDokuSource();
				NavigationButton dokuButton = new NavigationButton(new DataSourceNavigationAction(doku), src.getGUIsetting());
				result.add(dokuButton);
			}
			
			if (IAPmain.getRunMode() == IAPrunMode.WEB)
				result.add(new NavigationButton(new ActionLemnaAssessment(), src.getGUIsetting()));
			
			status.setCurrentStatusValueFine(100d);
			status.setCurrentStatusText1("Found " + n + " experiments");
			
		} catch (Exception e) {
			// error
			status.setCurrentStatusText2("Error: " + e.getMessage());
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
		return "LemnaTec";
	}
	
}

/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import org.ErrorMsg;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionNavigateDataSource;
import de.ipk.ag_ba.commands.Other;
import de.ipk.ag_ba.datasources.http_folder.HTTPfolderSource;
import de.ipk.ag_ba.datasources.http_folder.LTdocuSource;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class ActionLTnavigation extends AbstractNavigationAction implements NavigationAction {
	
	private NavigationButton src;
	private String login;
	private String pass;
	ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
	private ArrayList<String> listOfDatabases = new ArrayList<String>();
	private final TreeMap<String, ArrayList<ExperimentHeaderInterface>> experimentMap = new TreeMap<String, ArrayList<ExperimentHeaderInterface>>();
	
	public ActionLTnavigation() {
		super("Access LT-DB");
		@SuppressWarnings("unused")
		LTdataExchange ltInitVariablesAndSettings = new LTdataExchange();
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
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (listOfDatabases != null && listOfDatabases.size() > 0)
			return super.getResultMainPanel();
		else {
			return new MainPanelComponent("<h1>Setup Required!</h1>" +
					"No databases could be found. The most likely reason is, that the settings for accessing the database are not<br>" +
					"correctly set. Click '<b>Start &gt; Settings &gt; Lt-db &gt; PostgreSQL</b>' and specify the database host name,<br>" +
					"the database user name and password.<br><br>" +
					"Click '<b>Start &gt; About &gt; User Documentation</b>' for more detailed instructions.");
		}
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
				result.add(new NavigationButton(new ActionLTlogout(), src.getGUIsetting()));
			
			listOfDatabases = listOfDatabases != null && !listOfDatabases.isEmpty() ? listOfDatabases : new ArrayList<String>(new LTdataExchange().getDatabases());
			
			if (!listOfDatabases.isEmpty())
				result.add(new NavigationButton(new ActionLTuserNavigation(login), src.getGUIsetting()));
			
			TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> allExperiments = new TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>();
			allExperiments.put("", new TreeMap<String, ArrayList<ExperimentHeaderInterface>>());
			allExperiments.get("").put("", new ArrayList<ExperimentHeaderInterface>());
			Collections.sort(listOfDatabases, new Comparator<String>() {
				@Override
				public int compare(String arg0, String arg1) {
					if (LTdataExchange.known(arg0) && !LTdataExchange.known(arg1))
						return -1;
					if (!LTdataExchange.known(arg0) && LTdataExchange.known(arg1))
						return 1;
					return arg0.compareTo(arg1);
				}
			});
			ArrayList<NavigationButton> unsorted = new ArrayList<NavigationButton>();
			NavigationButton nb = new NavigationButton(new ActionLTdtabaseCollection(unsorted), src.getGUIsetting());
			int n = 0;
			int idx = -1;
			int max = listOfDatabases.size();
			
			for (String db : listOfDatabases) {
				idx++;
				status.setCurrentStatusValueFine(idx / (double) max * 100d);
				if (!LTdataExchange.known(db))
					continue;
				status.setCurrentStatusText1(n + " experiments");
				try {
					if (!experimentMap.containsKey(db)) {
						ArrayList<ExperimentHeaderInterface> res = new LTdataExchange().
								getExperimentsInDatabase(login, db, status);
						n += res.size();
						experimentMap.put(db, res);
					}
					ArrayList<ExperimentHeaderInterface> experiments = experimentMap.get(db);
					if (experiments.size() > 0) {
						if (!LTdataExchange.known(db))
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
			
			if (result.size() > 0)
				result.add(1, Other.getCalendarEntity(allExperiments, null, src.getGUIsetting()));
			
			if (result.size() > 0)
				result.add(2, new NavigationButton(new ActionMetaData("View Meta-Data for Experiments"), src.getGUIsetting()));
			
			if (IAPoptions.getInstance().getBoolean("Imaging-System-Documentation", "show_icon", false)) {
				HTTPfolderSource doku = new LTdocuSource();
				NavigationButton dokuButton = new NavigationButton(new ActionNavigateDataSource(doku), src.getGUIsetting());
				result.add(dokuButton);
			}
			
			if (IAPmain.getRunMode() == IAPrunMode.WEB)
				result.add(new NavigationButton(new ActionLemnaAssessment(), src.getGUIsetting()));
			
			status.setCurrentStatusValueFine(100d);
			status.setCurrentStatusText1("Found " + n + " experiments");
			
		} catch (Exception e) {
			// error
			ErrorMsg.addErrorMessage(e);
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
		return "Imaging System";
	}
	
}

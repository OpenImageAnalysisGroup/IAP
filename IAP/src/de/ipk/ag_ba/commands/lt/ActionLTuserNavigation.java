/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk.ag_ba.postgresql.Snapshot;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class ActionLTuserNavigation extends AbstractNavigationAction implements NavigationAction {
	
	private final String user;
	private LTdataExchange ltde;
	
	public ActionLTuserNavigation(LTdataExchange ltde) {
		super("Show user list and their experiments");
		this.ltde = ltde;
		this.user = ltde.user_login_name;
	}
	
	private NavigationButton src;
	private final TreeMap<String, TreeSet<ExperimentHeaderInterface>> userName2dbAndExperiment = new TreeMap<String, TreeSet<ExperimentHeaderInterface>>();
	
	private String message = "";
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		for (String user : userName2dbAndExperiment.keySet()) {
			result.add(new NavigationButton(new ActionLemnaUser(user, userName2dbAndExperiment.get(user)), src
					.getGUIsetting()));
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
		// enumerate all experiment snapshots and detect users
		this.src = src;
		if (userName2dbAndExperiment.size() > 0)
			return;
		status.setCurrentStatusValueFine(-1);
		status.setCurrentStatusText1("Query Databases");
		Collection<String> dbs = ltde.getDatabases();
		status.setCurrentStatusValueFine(0);
		long snapshots = 0;
		int users = 0;
		
		int error = 0;
		
		ArrayList<String> errorList = new ArrayList<String>();
		HashSet<String> experimentNames = new HashSet<String>();
		TreeSet<String> usersUnformatted = new TreeSet<String>();
		int n = 0;
		int idx = -1;
		int max = dbs.size();
		for (String db : dbs) {
			idx++;
			status.setCurrentStatusValueFine(idx / (double) max * 100d);
			try {
				status.setCurrentStatusText1(n + " experiments");
				Collection<ExperimentHeaderInterface> res = ltde.getExperimentsInDatabase(user, db, null);
				n += res.size();
				for (ExperimentHeaderInterface experiment : res) {
					String id = experiment.getDatabase() + ":" + experiment.getExperimentName();
					boolean quickAnalysis = true;
					
					if (quickAnalysis) {
						String r = experiment.getRemark();
						if (r == null || r.length() == 0)
							continue;
						if (r.startsWith("Snapshot creators: "))
							r = r.substring("Snapshot creators: ".length());
						for (String c : r.split(";")) {
							usersUnformatted.add(c);
							if (c.length() > 3) {
								c = c.substring(0, 1).toUpperCase() + c.substring(1);
							}
							if (!userName2dbAndExperiment.containsKey(c)) {
								userName2dbAndExperiment.put(c, new TreeSet<ExperimentHeaderInterface>());
							}
							userName2dbAndExperiment.get(c).add(experiment);
							experimentNames.add(id);
						}
					} else
						for (Snapshot s : ltde.getSnapshotsOfExperiment(experiment.getDatabase(),
								experiment.getExperimentName())) {
							String c = s.getCreator();
							if (c.length() == 0)
								continue;
							usersUnformatted.add(c);
							if (c.length() > 3) {
								c = c.substring(0, 1).toUpperCase() + c.substring(1);
							}
							if (!userName2dbAndExperiment.containsKey(c)) {
								userName2dbAndExperiment.put(c, new TreeSet<ExperimentHeaderInterface>());
							}
							userName2dbAndExperiment.get(c).add(experiment);
							experimentNames.add(id);
							snapshots++;
						}
				}
			} catch (Exception e) {
				if (e.getMessage() == null || !e.getMessage().equals("ERROR: relation \"snapshot\" does not exist"))
					System.out.println("Database " + db + " problem: " + e.getMessage());
				errorList.add(db);
				error++;
			}
		}
		status.setCurrentStatusValueFine(100d);
		status.setCurrentStatusText1("Found " + n + " experiments");
		if (error > 0)
			status.setCurrentStatusText2("Errors: " + error);
		
		users = userName2dbAndExperiment.size();
		
		message = "<html><h2>Database Content</h2>"
				+ "<ul>"
				+ "<li>Databases: "
				+ dbs.size()
				+ "<li>Users: "
				+ users
				+ "<li>Experiments: "
				+ experimentNames.size()
				+ (snapshots > 0 ?
						"<li>Snapshots: "
								+ snapshots : "")
				+ (error > 0 ? "<li>Empty databases: " + error + " ("
						+ StringManipulationTools.getStringList(errorList, ", ") + ")" : "")
				+ "</ul>"
				+ "<br>Remark: Multiple users may contribute data to a single experiment. This depends on which user is logged-in into a LT PC, while imaging takes place.<br><br>"
				+ "Complete list of snapshot-creators (" + usersUnformatted.size() + ", unformatted): "
				+ StringManipulationTools.getStringList(getArray(usersUnformatted), ", ");
		
		status.setCurrentStatusText1("");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList getArray(TreeSet<String> usersUnformatted) {
		ArrayList result = new ArrayList();
		for (String s : usersUnformatted)
			result.add(s);
		return result;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(message);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/user-user_t.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return "img/ext/user-user_t.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "User List";
	}
	
}

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

import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionNavigateDataSource;
import de.ipk.ag_ba.commands.Other;
import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.datasources.http_folder.HTTPfolderSource;
import de.ipk.ag_ba.datasources.http_folder.LTdocuSource;
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
	private String errorMessage;
	private final boolean enumerateAllDBsForDetails;
	
	public ActionLTnavigation() {
		super("Access LT-DB");
		@SuppressWarnings("unused")
		LTdataExchange ltInitVariablesAndSettings = new LTdataExchange();
		this.enumerateAllDBsForDetails = SystemOptions.getInstance().getBoolean("LT-DB", "Load Complete Experiment-List for Overview", false);
		
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
		if (listOfDatabases != null && listOfDatabases.size() > 0) {
			return result;
		} else {
			Book book = new Book(null, "<html><center>" +
					"User Documentation<br>" +
					"<font color='gray'><small>(online PDF)</small></font></center>",
					new IOurl("http://iap.ipk-gatersleben.de/documentation.pdf"),
					"img/dataset.png");
			ArrayList<NavigationButton> rr = new ArrayList<NavigationButton>();
			rr.add(book.getNavigationButton(src));
			return rr;
		}
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (listOfDatabases != null && listOfDatabases.size() > 0)
			return super.getResultMainPanel();
		else {
			return new MainPanelComponent("<h2>Direct phenotyping database access</h2>"
					+ ""
					+ "This function provides functionalities, to access LT imaging data sources directly from the corresponding<br>"
					+ "systems database. If you don't have this kind hard- and software-equipment, please use the<br>"
					+ "<i>'Start' > 'Load or Create Datasets'</i> function buttons, to work with file-based image sources."
					+ "<br><br>"
					+ "<h2>Setup Required</h2>" +
					"No databases could be found. The most likely reason is, that the settings for accessing the database are not<br>" +
					"correctly set. Click '<b>Start &gt; Settings &gt; Lt-db &gt; PostgreSQL</b>' and specify the database host name,<br>" +
					"the database user name and password.<br><br>" +
					"Click '<b>Start &gt; About &gt; User Documentation</b>' for more detailed instructions.<br><br>"
					+ "<b><code>Error-Message:<br><br>" + errorMessage + "</code></b>");
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
		this.errorMessage = "";
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
				if (enumerateAllDBsForDetails) {
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
								unsorted.add(new NavigationButton(new ActionLemnaDb(db, experiments, login), src.getGUIsetting()));
							else
								result.add(new NavigationButton(new ActionLemnaDb(db, experiments, login), src.getGUIsetting()));
						}
						// else System.out.println("Database " + db + " is empty.");
						for (ExperimentHeaderInterface ehi : experiments) {
							allExperiments.get("").get("").add(ehi);
						}
						
					} catch (Exception e) {
						if (e.getMessage() == null || !e.getMessage().equals("ERROR: relation \"snapshot\" does not exist"))
							System.out.println("Database " + db + " could not be processed. (" + e.getMessage() + ")");
					}
				} else {
					result.add(new NavigationButton(new ActionLemnaDb(db, null, login), src.getGUIsetting()));
				}
			}
			if (unsorted.size() > 0)
				result.add(nb);
			
			if (result.size() > 0 && enumerateAllDBsForDetails)
				result.add(1, Other.getCalendarEntity(allExperiments, null, src.getGUIsetting()));
			
			if (result.size() > 0)
				result.add(enumerateAllDBsForDetails ? 2 : 1, new NavigationButton(new ActionMetaData("View Meta-Data for Experiments"), src.getGUIsetting()));
			
			if (SystemOptions.getInstance().getBoolean("Imaging-System-Documentation", "show_icon", false)) {
				HTTPfolderSource doku = new LTdocuSource();
				NavigationButton dokuButton = new NavigationButton(new ActionNavigateDataSource(doku), src.getGUIsetting());
				result.add(dokuButton);
			}
			
			if (IAPmain.getRunMode() == IAPrunMode.WEB)
				result.add(new NavigationButton(new ActionLemnaAssessment(), src.getGUIsetting()));
			
			status.setCurrentStatusValueFine(100d);
			if (enumerateAllDBsForDetails)
				status.setCurrentStatusText1("Found " + n + " experiments");
			else
				status.setCurrentStatusText1("Processing finished");
			
		} catch (Exception e) {
			// error
			this.errorMessage = e.getMessage();
		}
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Network-Server-64.png";// lemna.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return "img/ext/gpl2/Gnome-Network-Server-64.png";// lemna-active.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Imaging System";
	}
	
}

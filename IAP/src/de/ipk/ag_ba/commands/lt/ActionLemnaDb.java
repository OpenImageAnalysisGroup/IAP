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

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.Other;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk.ag_ba.postgresql.LTsystem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class ActionLemnaDb extends AbstractNavigationAction implements NavigationAction {
	
	private NavigationButton src;
	private final String db;
	private ArrayList<ExperimentHeaderInterface> experiments;
	private final String login;
	
	public ActionLemnaDb(String db, ArrayList<ExperimentHeaderInterface> experiments, String login) {
		super("Open LT-DB " + db);
		this.db = db;
		this.experiments = experiments;
		this.login = login;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> g2e = new TreeMap<>();
		TreeMap<String, ArrayList<ExperimentHeaderInterface>> tm = new TreeMap<>();
		tm.put(db, experiments);
		g2e.put(LTsystem.getTypeString(db), tm);
		result.add(Other.getCalendarEntity(g2e, null, src.getGUIsetting()));
		
		if (experiments != null)
			for (ExperimentHeaderInterface ehi : experiments) {
				ExperimentReference experiment = new ExperimentReference(ehi);
				result.add(new NavigationButton(new ActionMongoOrLTexperimentNavigation(experiment), src.getGUIsetting()));
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
		this.src = src;
		if (experiments == null) {
			status.setCurrentStatusText1("Enumerate experiments");
			listDBs();
			status.setCurrentStatusText1("Finished processing");
		}
		if (experiments != null)
			Collections.sort(experiments, new Comparator<ExperimentHeaderInterface>() {
				@Override
				public int compare(ExperimentHeaderInterface a, ExperimentHeaderInterface b) {
					return b.getImportdate().compareTo(a.getImportdate());
				}
			});
	}
	
	private void listDBs() {
		try {
			this.experiments = new LTdataExchange().getExperimentsInDatabase(login, db, status);
		} catch (Exception e) {
			if (e.getMessage() == null || !e.getMessage().equals("ERROR: relation \"snapshot\" does not exist"))
				System.out.println("Database " + db + " could not be processed. (" + e.getMessage() + ")");
		}
		
	}
	
	@Override
	public String getDefaultImage() {
		switch (LTsystem.getTypeFromDatabaseName(db)) {
			case Barley:
				return "img/000Grad_3_.png";
			case Maize:
				return "img/maisMultiple.png";
			case Phytochamber:
				return "img/ext/phyto.png";
			case Unknown:
			default:
				return "img/DBE2_logo-gray_s.png";
		}
	}
	
	@Override
	public String getDefaultNavigationImage() {
		switch (LTsystem.getTypeFromDatabaseName(db)) {
			case Barley:
				return "img/000Grad_3_.png";
			case Maize:
				return "img/maisMultiple.png";
			case Phytochamber:
				return "img/ext/phyto.png";
			case Unknown:
			default:
				return "img/DBE2_logo_s.png";
		}
	}
	
	@Override
	public String getDefaultTitle() {
		String ns = experiments != null ? " (" + experiments.size() + ")" : "";
		String yy = null;
		String typeString = LTsystem.getTypeString(db);
		if (typeString != null) {
			yy = "20";
			if (db.substring(typeString.length()).length() >= 2) {
				String possibleYear = db.substring(typeString.length()).substring(0, 2);
				if (!StringManipulationTools.removeNumbersFromString(possibleYear).isEmpty())
					yy = "";
			}
		}
		switch (LTsystem.getTypeFromDatabaseName(db)) {
			case Barley:
				return "Barley Greenh. " + yy + db.substring(typeString.length()) + ns;
			case Maize:
				return "Maize Greenh. " + yy + db.substring(typeString.length()) + ns;
			case Phytochamber:
				return "Phytoch. " + yy + db.substring(typeString.length()) + ns;
			case Unknown:
			default:
				return db + ns;
		}
	}
}

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

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class ActionLemnaDb extends AbstractNavigationAction implements NavigationAction {
	
	private NavigationButton src;
	private final String db;
	private final ArrayList<ExperimentHeaderInterface> experiments;
	
	public ActionLemnaDb(String db, ArrayList<ExperimentHeaderInterface> experiments) {
		super("Open LT-DB " + db);
		this.db = db;
		this.experiments = experiments;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
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
		Collections.sort(experiments, new Comparator<ExperimentHeaderInterface>() {
			@Override
			public int compare(ExperimentHeaderInterface a, ExperimentHeaderInterface b) {
				return b.getImportdate().compareTo(a.getImportdate());
			}
		});
	}
	
	@Override
	public String getDefaultImage() {
		if (db.startsWith("APH_"))
			return "img/ext/phyto.png";
		else
			if (db.startsWith("CGH_"))
				return "img/maisMultiple.png";
			else
				if (db.startsWith("BGH_"))
					return "img/000Grad_3_.png";
				else
					return "img/DBE2_logo-gray_s.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		if (db.startsWith("APH_"))
			return "img/ext/phyto.png";
		else
			if (db.startsWith("CGH_"))
				return "img/maisMultiple.png";
			else
				if (db.startsWith("BGH_"))
					return "img/000Grad_3_.png";
				else
					return "img/DBE2_logo_s.png";
	}
	
	@Override
	public String getDefaultTitle() {
		String ns = " (" + experiments.size() + ")";
		String yy = "20";
		if (db.substring("APH_".length()).length() >= 2) {
			String possibleYear = db.substring("APH_".length()).substring(0, 2);
			if (!StringManipulationTools.removeNumbersFromString(possibleYear).isEmpty())
				yy = "";
		}
		if (db.startsWith("APH_"))
			return "Phytoch. " + yy + db.substring("APH_".length()) + ns;
		else
			if (db.startsWith("CGH_"))
				return "Maize Greenh. " + yy + db.substring("CGH_".length()) + ns;
			else
				if (db.startsWith("BGH_"))
					return "Barley Greenh. " + yy + db.substring("BGH_".length()) + ns;
				else
					return db + ns;
	}
}

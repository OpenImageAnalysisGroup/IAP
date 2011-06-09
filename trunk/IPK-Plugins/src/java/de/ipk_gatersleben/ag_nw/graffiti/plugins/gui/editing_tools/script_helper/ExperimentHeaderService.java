package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.HashMap;

public class ExperimentHeaderService {
	public static ArrayList<ExperimentHeaderInterface> filterNewest(ArrayList<ExperimentHeaderInterface> in) {
		HashMap<String, ExperimentHeaderInterface> known = new HashMap<String, ExperimentHeaderInterface>();
		ArrayList<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
		for (ExperimentHeaderInterface ehi : in) {
			String key = ehi.getImportusername() + "//" + ehi.getDatabase() + "//" + ehi.getExperimentName();
			if (!known.containsKey(key)) {
				known.put(key, ehi);
				result.add(ehi);
			} else {
				ExperimentHeaderInterface prevTime = known.get(key);
				long ct = ehi.getImportdate().getTime();
				long pt = prevTime.getImportdate().getTime();
				
				if (ehi.getStorageTime() != null)
					ct = ehi.getStorageTime().getTime();
				if (prevTime.getStorageTime() != null)
					pt = prevTime.getStorageTime().getTime();
				
				boolean newer = ct > pt;
				if (newer) {
					ehi.addHistoryItem(pt, known.get(key));
					ehi.addHistoryItems(known.get(key).getHistory());
					result.remove(known.get(key));
					result.add(ehi);
					known.remove(key);
					known.put(key, ehi);
				}
			}
		}
		return result;
	}
	
}

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class ExperimentHeaderService {
	public static ArrayList<ExperimentHeaderInterface> filterNewest(ArrayList<ExperimentHeaderInterface> in) {
		HashMap<String, ExperimentHeaderInterface> known = new HashMap<String, ExperimentHeaderInterface>();
		ArrayList<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
		Collections.sort(in, new Comparator<ExperimentHeaderInterface>() {
			@Override
			public int compare(ExperimentHeaderInterface o1, ExperimentHeaderInterface o2) {
				Long ct = o1.getImportdate().getTime();
				Long pt = o2.getImportdate() != null ? o2.getImportdate().getTime() : -1;
				
				if (o1.getStorageTime() != null)
					ct = o1.getStorageTime().getTime();
				if (o2.getStorageTime() != null)
					pt = o2.getStorageTime().getTime();
				return ct.compareTo(pt);
			}
		});
		
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
				
				boolean newer = ct >= pt;
				if (newer) {
					while (ehi.getHistory().containsKey(pt))
						pt--;
					prevTime.setStorageTime(new Date(pt));
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

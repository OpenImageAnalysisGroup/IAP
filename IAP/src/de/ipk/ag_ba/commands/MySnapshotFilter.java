package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;

public class MySnapshotFilter implements SnapshotFilter {
	
	private final ArrayList<ThreadSafeOptions> toggles;
	private final String[] globalOutlierArr;
	
	public MySnapshotFilter(ArrayList<ThreadSafeOptions> toggles, String globalOutlierList) {
		this.toggles = toggles;
		this.globalOutlierArr = globalOutlierList.split(",");
	}
	
	@Override
	public boolean filterOut(SnapshotDataIAP s) {
		if (toggles == null)
			return false;
		boolean filterByTime = false;
		if (filterByTime) {
			if (s.getDay() < 10)
				return true;
			if (s.getDay() > 35)
				return true;
		}
		for (ThreadSafeOptions t : toggles) {
			if (match(t, s))
				return true;
		}
		return false;
	}
	
	private boolean match(ThreadSafeOptions t, SnapshotDataIAP s) {
		if (t.getBval(0, true))
			return false;
		// filter is active, check if snapshot matches criteria
		// e.g. tso.setParam(0, setting); // Condition, Species, Genotype, Variety, Treatment
		// e.g. tso.setParam(1, c);
		
		if (globalOutlierArr.length > 0) {
			for (String o : globalOutlierArr) {
				if (s.getPlantId() != null && s.getPlantId().equals(o))
					return true;
				else
					if (s.getTimePoint() != null && s.getTimePoint().equals(o))
						return true;
					else
						if (s.getDay() != null) {
							try {
								int day = Integer.parseInt(StringManipulationTools.getNumbersFromString(o));
								if (o.contains(">=") && s.getDay() >= day)
									return true;
								else
									if (o.contains(">") && s.getDay() > day)
										return true;
									else
										if (o.contains("<=") && s.getDay() <= day)
											return true;
										else
											if (o.contains("<") && s.getDay() < day)
												return true;
							} catch (Exception e) {
								// empty
							}
						}
			}
		}
		
		String field = (String) t.getParam(0, "");
		String content = (String) t.getParam(1, "");
		String value = null;
		if (field.equals("Condition"))
			value = s.getCondition();
		else
			if (field.equals("Species"))
				value = s.getSpecies();
			else
				if (field.equals("Genotype"))
					value = s.getGenotype();
				else
					if (field.equals("Variety"))
						value = s.getVariety();
					else
						if (field.equals("Growth condition"))
							value = s.getGrowthCondition();
						else
							if (field.equals("Treatment"))
								value = s.getTreatment();
		if (value == null)
			value = "(not specified)";
		else
			if (value.isEmpty())
				value = "(not specified)";
		
		return value.equals(content);
	}
}

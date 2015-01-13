package de.ipk.ag_ba.commands.experiment.process.report;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

/**
 * @author Christian Klukas
 */
public class MySnapshotFilter implements SnapshotFilter {
	
	private final ArrayList<ThreadSafeOptions> toggles;
	private final String[] globalOutlierArr;
	private final int[] globalOutlierDays;
	
	public MySnapshotFilter(ArrayList<ThreadSafeOptions> toggles, String globalOutlierList) {
		this.toggles = toggles;
		this.globalOutlierArr = globalOutlierList != null ? globalOutlierList.split("//") : new String[] {};
		if (globalOutlierArr != null) {
			for (int idx = 0; idx < globalOutlierArr.length; idx++) {
				globalOutlierArr[idx] = globalOutlierArr[idx].trim();
			}
		}
		this.globalOutlierDays = new int[globalOutlierArr.length];
		int idx = 0;
		for (String o : globalOutlierArr) {
			try {
				int day = Integer.parseInt(StringManipulationTools.getNumbersFromString(o));
				globalOutlierDays[idx] = day;
			} catch (Exception e) {
				globalOutlierDays[idx] = Integer.MAX_VALUE;
				globalOutlierArr[idx] = o.trim();
			}
			idx++;
		}
	}
	
	@Override
	public boolean filterOut(SnapshotDataIAP s) {
		// if (toggles == null)
		// return false;
		
		if (filterOut(s.getPlantId(), s.getDay()))
			return true;
		if (toggles != null)
			for (ThreadSafeOptions t : toggles) {
				if (match(t, s))
					return true;
			}
		return false;
	}
	
	@Override
	public boolean filterOut(String plantId, Integer timePoint) {
		if (timePoint == Integer.MAX_VALUE)
			return false;
		if (globalOutlierArr.length > 0)
			return processOutlierArray(plantId, timePoint);
		else
			return false;
	}
	
	private boolean processOutlierArray(String plantId, Integer timePoint) {
		int idx = 0;
		boolean inverOpAndUseAndInsteadOfOr = false;
		boolean anyMatch = false;
		for (String o : globalOutlierArr) {
			if (o != null && o.equals("!!")) {
				inverOpAndUseAndInsteadOfOr = true;
				anyMatch = false;
			}
			boolean match = false;
			if (plantId != null && o != null && o.endsWith("*") && !o.contains("/") && o.length() >= 2) {
				o = o.substring(0, o.length() - 1);
				if (plantId.startsWith(o))
					match = true;
			} else
				if (plantId != null && plantId.equals(o))
					match = true;
				else
					if (plantId != null && o.startsWith(plantId + "/")) {
						try {
							String fromDay = o.substring((plantId + "/").length());
							int fromD = Integer.parseInt(fromDay);
							match = timePoint >= fromD;
						} catch (Exception e) {
							System.out.println("Problematic outlier definition (ignored): " + o);
						}
					} else
						if (timePoint != null && timePoint.equals(o))
							match = true;
						else
							if (timePoint != null) {
								int day = globalOutlierDays[idx];
								if (day < Integer.MAX_VALUE)
									if (o.contains(">=") && timePoint >= day)
										match = true;
									else
										if (o.contains(">") && timePoint > day)
											match = true;
										else
											if (o.contains("<=") && timePoint <= day)
												match = true;
											else
												if (o.contains("<") && timePoint < day)
													match = true;
												else
													if (o.contains("=") && timePoint == day)
														match = true;
								
							}
			if (match && !inverOpAndUseAndInsteadOfOr)
				return true;
			if (match && inverOpAndUseAndInsteadOfOr)
				anyMatch = true;
			idx++;
		}
		if (inverOpAndUseAndInsteadOfOr)
			return !anyMatch;
		else
			return false;
	}
	
	private boolean match(ThreadSafeOptions t, SnapshotDataIAP s) {
		if (t.getBval(0, true))
			return false;
		// filter is active, check if snapshot matches criteria
		// e.g. tso.setParam(0, setting); // Condition, Species, Genotype, Variety, Treatment
		// e.g. tso.setParam(1, c);
		
		String field = (String) t.getParam(0, "");
		String content = (String) t.getParam(1, "");
		String value = null;
		if (field.equals("Condition"))
			value = s.getCondition();
		else
			if (field.equals(ConditionInfo.SPECIES.toString()))
				value = s.getSpecies();
			else
				if (field.equals(ConditionInfo.GENOTYPE.toString()))
					value = s.getGenotype();
				else
					if (field.equals(ConditionInfo.VARIETY.toString()))
						value = s.getVariety();
					else
						if (field.equals(ConditionInfo.GROWTHCONDITIONS.toString()))
							value = s.getGrowthCondition();
						else
							if (field.equals(ConditionInfo.TREATMENT.toString()))
								value = s.getTreatment();
		if (value == null)
			value = "(not specified)";
		else
			if (value.isEmpty())
				value = "(not specified)";
		
		return value.equals(content);
	}
	
	@Override
	public boolean isGlobalOutlierOrSpecificOutlier(Object measurement) {
		if (measurement == null)
			return false;
		NumericMeasurement3D nmi = (NumericMeasurement3D) measurement;
		boolean isGlobalOutlier = filterOut(nmi.getQualityAnnotation(), nmi.getParentSample().getTime());
		if (isGlobalOutlier)
			return true;
		else {
			if (nmi.isMarkedAsOutlier())
				return true;
		}
		return false;
	}
}

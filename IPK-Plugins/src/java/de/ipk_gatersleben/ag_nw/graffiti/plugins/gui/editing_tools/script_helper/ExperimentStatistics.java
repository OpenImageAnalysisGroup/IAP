package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.SystemAnalysis;

public class ExperimentStatistics {
	
	private final Experiment experiment;
	
	public ExperimentStatistics(Experiment experiment) {
		this.experiment = experiment;
	}
	
	@Override
	public String toString() {
		String s = getSummaryHTML(true, null);
		s = StringManipulationTools.stringReplace(s, "<table>", "---------\n");
		s = StringManipulationTools.stringReplace(s, "</table>", "---------\n");
		s = StringManipulationTools.stringReplace(s, "</tr>", " |\n");
		s = StringManipulationTools.stringReplace(s, "<td>", " |\t");
		s = StringManipulationTools.stringReplace(s, "<th>", " |\t");
		s = StringManipulationTools.removeHTMLtags(s);
		return s;
	}
	
	public String getSummaryHTML(boolean shortTitles,
			BackgroundTaskStatusProviderSupportingExternalCall backgroundTaskStatusProviderSupportingExternalCall) {
		ObjectStat substanceStat = new ObjectStat(shortTitles ? "Sub" : "Substances");
		ObjectStat conditionStat = new ObjectStat(shortTitles ? "Cond" : "Conditions");
		ObjectStat sampleStat = new ObjectStat(shortTitles ? "Sam" : "Samples");
		ObjectStat numericStat = new ObjectStat(shortTitles ? "Num" : "Numeric Values");
		ObjectStat binaryStat = new ObjectStat(shortTitles ? "Bin" : "Binary Entities");
		ObjectStat exprimentHeaderStat = new ObjectStat(shortTitles ? "Hea" : "Experiment Headers");
		ObjectStat qualityTagStat = new ObjectStat(shortTitles ? "Qua" : "Quality Tags");
		
		ArrayList<ObjectStat> ol = new ArrayList<ObjectStat>();
		ol.add(exprimentHeaderStat);
		ol.add(substanceStat);
		ol.add(conditionStat);
		ol.add(sampleStat);
		ol.add(numericStat);
		ol.add(binaryStat);
		ol.add(qualityTagStat);
		
		exprimentHeaderStat.add(experiment.getHeader());
		int workload = experiment.size();
		int idx = 0;
		for (SubstanceInterface s : experiment) {
			substanceStat.add(s);
			for (ConditionInterface c : s) {
				if (c.getParentSubstance() != s) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: INTERNAL PROBLEM: PARENT SUBSTANCE DIFFERS FROM SUBSTANCE");
					substanceStat.add(c.getParentSubstance());
				}
				conditionStat.add(c);
				exprimentHeaderStat.add(c.getExperimentHeader());
				for (SampleInterface sa : c) {
					if (sa.getParentCondition() != c) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: INTERNAL PROBLEM: PARENT CONDITION DIFFERS FROM CONDITION");
						conditionStat.add(sa.getParentCondition());
					}
					sampleStat.add(sa);
					for (NumericMeasurementInterface nmi : sa) {
						if (nmi.getParentSample() != sa) {
							System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: INTERNAL PROBLEM: PARENT SAMPLE DIFFERS FROM SAMPLE");
							sampleStat.add(nmi.getParentSample());
						}
						qualityTagStat.add(nmi.getQualityAnnotation());
						if (!Double.isNaN(nmi.getValue())) {
							numericStat.add(nmi, false);
						} else {
							binaryStat.add(nmi, false);
						}
					}
				}
			}
			idx++;
			if (backgroundTaskStatusProviderSupportingExternalCall != null)
				backgroundTaskStatusProviderSupportingExternalCall.setCurrentStatusValueFine(100d * idx / workload);
		}
		
		return "<table>" + ObjectStat.getTableHeader(shortTitles)
				+ StringManipulationTools.getStringList(ol, "") +
				"</table>";
	}
}

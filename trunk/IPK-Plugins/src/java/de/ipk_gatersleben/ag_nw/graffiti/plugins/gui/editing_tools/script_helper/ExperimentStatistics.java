package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;

import org.StringManipulationTools;

public class ExperimentStatistics {
	
	private final Experiment experiment;
	
	public ExperimentStatistics(Experiment experiment) {
		this.experiment = experiment;
	}
	
	public String getSummaryHTML() {
		ObjectStat substanceStat = new ObjectStat("Substances");
		ObjectStat conditionStat = new ObjectStat("Conditions");
		ObjectStat sampleStat = new ObjectStat("Samples");
		ObjectStat numericStat = new ObjectStat("Numeric Values");
		ObjectStat binaryStat = new ObjectStat("Binary Entities");
		ObjectStat exprimentHeaderStat = new ObjectStat("Experiment Headers");
		ObjectStat qualityTagStat = new ObjectStat("Quality Tags");
		
		ArrayList<ObjectStat> ol = new ArrayList<ObjectStat>();
		ol.add(exprimentHeaderStat);
		ol.add(substanceStat);
		ol.add(conditionStat);
		ol.add(sampleStat);
		ol.add(numericStat);
		ol.add(binaryStat);
		ol.add(qualityTagStat);
		
		exprimentHeaderStat.add(experiment.getHeader());
		
		for (SubstanceInterface s : experiment) {
			substanceStat.add(s);
			for (ConditionInterface c : s) {
				conditionStat.add(c);
				exprimentHeaderStat.add(c.getExperimentHeader());
				for (SampleInterface sa : c) {
					sampleStat.add(sa);
					for (NumericMeasurementInterface nmi : sa) {
						qualityTagStat.add(nmi.getQualityAnnotation());
						if (!Double.isNaN(nmi.getValue())) {
							numericStat.add(nmi);
						} else {
							binaryStat.add(nmi);
						}
					}
				}
			}
		}
		
		return "<table border='0'>" + ObjectStat.getTableHeader()
				+ StringManipulationTools.getStringList(ol, "") +
				"</table>";
	}
}

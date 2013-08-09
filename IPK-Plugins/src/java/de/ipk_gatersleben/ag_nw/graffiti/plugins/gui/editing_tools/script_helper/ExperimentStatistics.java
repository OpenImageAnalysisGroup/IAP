package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;

import org.StringManipulationTools;

public class ExperimentStatistics {
	
	private final Experiment experiment;
	
	public ExperimentStatistics(Experiment experiment) {
		this.experiment = experiment;
	}
	
	@Override
	public String toString() {
		String s = getSummaryHTML(true);
		s = StringManipulationTools.stringReplace(s, "<table>", "---------\n");
		s = StringManipulationTools.stringReplace(s, "</table>", "---------\n");
		s = StringManipulationTools.stringReplace(s, "</tr>", " |\n");
		s = StringManipulationTools.stringReplace(s, "<td>", " |\t");
		s = StringManipulationTools.stringReplace(s, "<th>", " |\t");
		s = StringManipulationTools.removeHTMLtags(s);
		return s;
	}
	
	public String getSummaryHTML(boolean sh) {
		ObjectStat substanceStat = new ObjectStat(sh ? "Sub" : "Substances");
		ObjectStat conditionStat = new ObjectStat(sh ? "Cond" : "Conditions");
		ObjectStat sampleStat = new ObjectStat(sh ? "Sam" : "Samples");
		ObjectStat numericStat = new ObjectStat(sh ? "Num" : "Numeric Values");
		ObjectStat binaryStat = new ObjectStat(sh ? "Bin" : "Binary Entities");
		ObjectStat exprimentHeaderStat = new ObjectStat(sh ? "Hea" : "Experiment Headers");
		ObjectStat qualityTagStat = new ObjectStat(sh ? "Qua" : "Quality Tags");
		
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
		
		return "<table>" + ObjectStat.getTableHeader(sh)
				+ StringManipulationTools.getStringList(ol, "") +
				"</table>";
	}
}

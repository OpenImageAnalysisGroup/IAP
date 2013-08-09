package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public class ExperimentStatistics {
	
	private final Experiment experiment;
	
	public ExperimentStatistics(Experiment experiment) {
		this.experiment = experiment;
	}
	
	public String getSummaryHTML() {
		int substanceObjectCnt = 0;
		int substanctSetCnt = 0;
		int substanceCompareCnt = 0;
		int conditionObjectCnt = 0;
		int conditionSetCnt = 0;
		int sampleObjectCnt = 0;
		int sampleSetCnt = 0;
		int numericMeasurements = 0;
		int binaryMeasurements = 0;
		int experimentHeaderObjects = 0;
		int qualityTagObjectCnt = 0;
		int qualityTagSetCnt = 0;
		
		int outliers = 0;
		int flagged = 0;
		return "<table><tr><th>Property</th><th>Object Count</th><th>Set Count</th><th>Compare To Count</th></tr>"
				+ getRow("Substances", substanceObjectCnt, -1, -1) +
				"</table>";
	}
	
	private String getRow(String property, int objCnt, int setCnt, int compareCnt) {
		return "";
	}
}

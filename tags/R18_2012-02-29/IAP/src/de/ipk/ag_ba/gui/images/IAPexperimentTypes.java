package de.ipk.ag_ba.gui.images;

public enum IAPexperimentTypes {
	Phytochamber("Arabidopsis"),
	PhytochamberBlueRubber("Arabidopsis (blue rubber cover)"),
	BarleyGreenhouse("Barley"),
	MaizeGreenhouse("Maize"),
	UnknownGreenhouse("LemnaTec (Other)"),
	AnalysisResults("Analysis Results");
	
	private String title;
	
	IAPexperimentTypes(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return title;
	}
}

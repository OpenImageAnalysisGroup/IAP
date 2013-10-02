package de.ipk.ag_ba.gui.images;

public enum IAPexperimentTypes {
	Phytochamber("Arabidopsis", "Arabidopsis"),
	PhytochamberBlueRubber("Arabidopsis (blue rubber cover)", "Arabidopsis"),
	BarleyGreenhouse("Barley", "Hordeum vulgare"),
	BarleyGreenhouseGray("Barley (gray support)", "Hordeum vulgare"),
	MaizeGreenhouse("Maize", "Zea"),
	UnknownGreenhouse("LemnaTec (Other)", "Unknown"),
	AnalysisResults("Analysis Results", "Unknown"),
	Raps("Rapeseed", "Canola");
	
	private String title, species;
	
	IAPexperimentTypes(String title, String species) {
		this.title = title;
		this.species = species;
	}
	
	@Override
	public String toString() {
		return title;
	}
	
	public static String getSpeciesFromExperimentType(String expType) {
		for (IAPexperimentTypes t : IAPexperimentTypes.values()) {
			if (expType.equals(t.toString()))
				return t.getSpecies();
		}
		return "Unknown";
	}
	
	private String getSpecies() {
		return species;
	}
}
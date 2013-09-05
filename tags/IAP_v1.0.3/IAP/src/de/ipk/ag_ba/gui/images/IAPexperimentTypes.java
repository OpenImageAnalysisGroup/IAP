package de.ipk.ag_ba.gui.images;

public enum IAPexperimentTypes {
	Phytochamber("Arabidopsis", "Arabidopsis"),
	PhytochamberBlueRubber("Arabidopsis (blue rubber cover)", "Arabidopsis"),
	BarleyGreenhouse("Barley", "Hordeum vulgare"),
	BarleyGreenhouseGray("Barley (gray support)", "Hordeum vulgare"),
	MaizeGreenhouse("Maize", "Zea"),
	UnknownGreenhouse("Generic Greenhouse", "Unknown"),
	AnalysisResults("Analysis Results", "Unknown"),
	Raps("Rapeseed", "Canola"),
	ManualMeasurements("Manual Measurements", "Unknown"),
	RootWaterScan("Roots (Scann)", "Barley Roots"),
	ImportedAnalysisResults("Imported Analysis Results", "Unknown"),
	ClimateData("Climate", "Unknown"),
	LeafImages("Leaf Images", "Unknown"),
	ImportedDataset("Imported Dataset", "Unknown"),
	Test("Test (can be deleted)", "Unknown");
	
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
	
	public static IAPexperimentTypes getExperimentTypeFromExperimentTypeName(String expType) {
		if (expType != null)
			for (IAPexperimentTypes t : IAPexperimentTypes.values()) {
				if (expType.equals(t.toString()))
					return t;
			}
		return UnknownGreenhouse;
	}
	
	private String getSpecies() {
		return species;
	}
}

package de.ipk.ag_ba.gui.images;

public enum IAPexperimentTypes {
	Phytochamber("Arabidopsis", "Arabidopsis"),
	BarleyGreenhouse("Barley", "Hordeum vulgare"),
	MaizeGreenhouse("Maize", "Zea"),
	RiceImages("Rice", "Rice"),
	TobaccoImages("Tobacco", "Tobacco"),
	Raps("Rapeseed", "Canola"),
	UnknownGreenhouse("Generic Greenhouse", "Unknown"),
	LeafImages("Leaf Images", "Unknown"),
	RootWaterScan("Roots (Scanned)", "Barley Roots"),
	ManualMeasurements("Manual Measurements", "Unknown"),
	ImportedDataset("Imported Dataset", "Unknown"),
	AnalysisResults("Analysis Results", "Unknown"),
	ImportedAnalysisResults("Imported Analysis Results", "Unknown"),
	ClimateData("Climate", "Unknown"),
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

package de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d;

/**
 * @author klukas
 */
public enum GenerationMode {
	GRAYSCALE_PROBABILITY("Gray Scale Probability"), COLORED("Colored, fixed cut off"),
	// BINARY("Binary, fixed cut off"),
	COLORED_RGBA("Colored, RGBA");
	
	String v;
	
	GenerationMode(String t) {
		this.v = t;
	}
	
	@Override
	public String toString() {
		return v;
	}
}

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

public enum LegendType {
	COMPLETE, COMPLETE_WITHOUT_EXPERIMENT, COMPLETE_WITHOUT_EXPERIMENT_AND_CONDITIONNUMBER, OLDSTYLE;
	
	@Override
	public String toString() {
		switch (this) {
			case COMPLETE:
				return "Experiment and all Condition Attributes";
			case COMPLETE_WITHOUT_EXPERIMENT:
				return "All Condition Attributes";
			case COMPLETE_WITHOUT_EXPERIMENT_AND_CONDITIONNUMBER:
				return "All Condition Attributes except Condition ID";
			case OLDSTYLE:
				return "Species, Genotype and Treatment";
		}
		return super.toString();
	}
	
}

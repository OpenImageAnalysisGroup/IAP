package de.ipk.ag_ba.commands.experiment;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public enum ExportSetting {
	ALL, NO_HIST_NO_SECTIONS, ONLY_MAIN_HISTO, ONLY_SECTIONS;
	
	public boolean ignoreSubstance(SubstanceInterface substance) {
		switch (this) {
			case ALL:
				return false;
			case ONLY_SECTIONS:
				return substance.getName() == null || !substance.getName().contains(".section_");
			case ONLY_MAIN_HISTO:
				return substance.getName() == null || substance.getName().contains(".section_") || !substance.getName().contains("histogram");
			case NO_HIST_NO_SECTIONS:
				return substance.getName() == null || substance.getName().contains(".section_") || substance.getName().contains("histogram");
		}
		return false;
	}
	
}

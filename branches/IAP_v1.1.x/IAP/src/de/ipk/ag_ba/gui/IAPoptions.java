package de.ipk.ag_ba.gui;

import org.SystemOptions;

/**
 * @author klukas
 */
public class IAPoptions {
	public enum IAPoptionFields {
		GROUP_BY_COORDINATOR, GROUP_BY_EXPERIMENT_TYPE;
	}
	
	public static boolean getSetting(IAPoptions.IAPoptionFields groupByCoordinator) {
		switch (groupByCoordinator) {
			case GROUP_BY_COORDINATOR:
				return SystemOptions.getInstance().getBoolean("EXPERIMENTS", "group_by_coordinator", true);
			case GROUP_BY_EXPERIMENT_TYPE:
				return SystemOptions.getInstance().getBoolean("EXPERIMENTS", "group_by_experiment_type", true);
		}
		return false;
	}
	
	public static SystemOptions getInstance() {
		return SystemOptions.getInstance();
	}
}

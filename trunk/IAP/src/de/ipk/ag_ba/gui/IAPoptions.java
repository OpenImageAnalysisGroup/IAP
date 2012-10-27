package de.ipk.ag_ba.gui;

import org.SystemOptions;

public class IAPoptions extends SystemOptions {
	
	private IAPoptions() {
		super();
	}
	
	public enum IAPoptionFields {
		GROUP_BY_COORDINATOR, GROUP_BY_EXPERIMENT_TYPE;
	}
	
	public static boolean getSetting(IAPoptions.IAPoptionFields groupByCoordinator) {
		switch (groupByCoordinator) {
			case GROUP_BY_COORDINATOR:
				return getInstance().getBoolean("EXPERIMENTS", "group_by_coordinator", true);
			case GROUP_BY_EXPERIMENT_TYPE:
				return getInstance().getBoolean("EXPERIMENTS", "group_by_experiment_type", true);
		}
		return false;
	}
	
	public synchronized static SystemOptions getInstance() {
		if (instance == null)
			instance = new IAPoptions();
		return instance;
	}
}

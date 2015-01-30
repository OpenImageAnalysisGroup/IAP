package de.ipk.ag_ba.commands.experiment.tools;

public enum DirectionMode {
	ALL_ONE_DIR, HALF_ONE_DIR, ALTERNATING;
	
	@Override
	public String toString() {
		switch (this) {
			case ALL_ONE_DIR:
				return "All in one direction";
			case HALF_ONE_DIR:
				return "Half in one, other half in other direction";
			case ALTERNATING:
				return "Alternating directions";
		}
		throw new RuntimeException("Internal Error: Unknown DirectionMode!");
	}
	
	public static DirectionMode fromString(String v) {
		for (DirectionMode dm : values())
			if (dm.toString().equals(v))
				return dm;
		throw new RuntimeException("Internal Error: Unknown DirectionMode!");
	}
}

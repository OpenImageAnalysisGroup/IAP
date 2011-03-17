package de.ipk.ag_ba.postgresql;

public enum LemnaTecSystem {
	Barley, Maize, Phytochamber, Unknown;
	
	public static LemnaTecSystem getTypeFromDatabaseName(String database) {
		if (database.startsWith("APH_"))
			return Phytochamber;
		if (database.startsWith("BGH_"))
			return Barley;
		if (database.startsWith("CGH_"))
			return Maize;
		return Unknown;
	}
	
}

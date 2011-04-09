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
	
	public boolean isPreAuthenticated(String user) {
		if (user == null)
			return false;
		if (this == Maize)
			return user.equals("Muraya") ||
					user.equalsIgnoreCase("Altmann");
		if (this == Unknown)
			return user.equals("Neumannk");
		if (this == Barley)
			return user.equals("Neumannk");
		if (this == Phytochamber)
			return user.equals("Fernando") ||
					user.equals("weigelt") ||
					user.equals("mary") ||
					user.equals("meyer") ||
					user.equalsIgnoreCase("Altmann");
		return false;
	}
	
}

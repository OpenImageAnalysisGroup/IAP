package de.ipk.ag_ba.postgresql;

import de.ipk.ag_ba.gui.IAPoptions;

public enum LemnaTecSystem {
	Barley, Maize, Phytochamber, Unknown;
	
	public static LemnaTecSystem getTypeFromDatabaseName(String database) {
		if (database.startsWith(IAPoptions.getInstance().getString("LT-DB", "DBs//db_prefix_Phytochamber", "APH_")))
			return Phytochamber;
		if (database.startsWith(IAPoptions.getInstance().getString("LT-DB", "DBs//db_prefix_Barley", "BGH_")))
			return Barley;
		if (database.startsWith(IAPoptions.getInstance().getString("LT-DB", "DBs//db_prefix_Maize", "CGH_")))
			return Maize;
		return Unknown;
	}
	
	public boolean isPreAuthenticated(String user) {
		if (user == null)
			return false;
		
		switch (this) {
			case Maize:
				return user.equalsIgnoreCase("Muraya") ||
						user.equalsIgnoreCase("Altmann");
			case Barley:
				return user.equalsIgnoreCase("Neumannk");
			case Phytochamber:
				return user.equalsIgnoreCase("Fernando") ||
						user.equalsIgnoreCase("weigelt") ||
						user.equalsIgnoreCase("mary") ||
						user.equalsIgnoreCase("meyer") ||
						user.equalsIgnoreCase("Altmann");
			case Unknown:
				return false;
		}
		return false;
	}
}

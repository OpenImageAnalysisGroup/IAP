package de.ipk.ag_ba.postgresql;

import org.SystemOptions;

public enum LTsystem {
	Barley, Maize, Phytochamber, Unknown;
	
	public static LTsystem getTypeFromDatabaseName(String database) {
		if (database.startsWith(SystemOptions.getInstance().getString("LT-DB", "DBs//db_prefix_Phytochamber", "APH_")))
			return Phytochamber;
		if (database.startsWith(SystemOptions.getInstance().getString("LT-DB", "DBs//db_prefix_Barley", "BGH_")))
			return Barley;
		if (database.startsWith(SystemOptions.getInstance().getString("LT-DB", "DBs//db_prefix_Maize", "CGH_")))
			return Maize;
		return Unknown;
	}
	
	public boolean isPreAuthenticated(String user) {
		if (user == null)
			return false;
		
		switch (this) {
		// examples, needs custom auth code
			case Maize:
				return user.equalsIgnoreCase("User 1") || user.equalsIgnoreCase("User 2");
			case Barley:
				return user.equalsIgnoreCase("User 3") || user.equalsIgnoreCase("User 4");
			case Phytochamber:
				return user.equalsIgnoreCase("User 5") || user.equalsIgnoreCase("User 6");
			case Unknown:
				return false;
		}
		return false;
	}
}

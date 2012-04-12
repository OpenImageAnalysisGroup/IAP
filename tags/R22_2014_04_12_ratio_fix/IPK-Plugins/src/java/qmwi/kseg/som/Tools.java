/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.util.Vector;

// Hilfsklasse f�r Routinen die mit einem Datenbestand arbeiten k�nnen
// Zuordnung zu "DataSet" eventuell sinnvoller...
// Mit der allgemeinen Konvention, dass wenn der "Vector data" == null ist,
// die Daten von der eigenen Instanz verwendet werden
public class Tools {
	
	// Liefert alle Eintr�ge des Datenbestandes data zur�ck, die in der Spalte "column"
	// dem Wert "entry" entsprechen
	@SuppressWarnings("unchecked")
	public static Vector<?> getAllWhere(Vector data, int column, String entry) {
		Vector result = new Vector<Object>();
		
		for (int j = 0; j < data.size(); j++) {
			if (((SOMdataEntry) data.elementAt(j)).getColumnData(column).equalsIgnoreCase(entry))
				result.add(data.elementAt(j));
		}
		return result;
	}
	
	// Liefert alle Eintr�ge des Datenbestandes data zur�ck, die in der Spalte "column"
	// dem Wert "entry" entsprechen
	public static int getBreite(int nodecount) {
		// Annahme: _quadratische_ Karte
		int mybreite = 0;
		while (mybreite * mybreite < nodecount)
			mybreite++;
		return mybreite;
	}
	
	public static Vector<String> getValues(String inputLine, String teiler) {
		Vector<String> res = new Vector<String>();
		inputLine += ",";
		int i = 0;
		while (inputLine.length() > 0) {
			res.add(inputLine.substring(0, inputLine.indexOf(teiler)));
			i++;
			inputLine = inputLine.substring(inputLine.indexOf(teiler) + 1);
		}
		return res;
	}
	
	public static double myMax(double z1, double z2) {
		if (z1 > z2)
			return z1;
		else
			return z2;
	}
	
	public static int myMax(int z1, int z2) {
		if (z1 > z2)
			return z1;
		else
			return z2;
	}
} // class Tools

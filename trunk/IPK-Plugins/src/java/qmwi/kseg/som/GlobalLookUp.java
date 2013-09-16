/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.util.Vector;

/**
 * Globale Klasse, die alle Variationen an Eingaben speichert und
 * einen Wert zur�ckliefert, der Anzeigt, der wievielten Variante
 * der Eingabe-String entspricht (getEntry)
 * Spalten werden unabh�ngig verwaltet
 */
class GlobalLookUp {
	private static Vector<Integer> columns = new Vector<Integer>();
	// Spaltennummer der Eintr�ge in list
	private static Vector<String> list = new Vector<String>(); // Enthält alle Varianten
	
	// an Eingaben, die bisher abgefragt wurden
	
	// Ausprägungen werden anhand der Spaltennummer und des Inhalts unterschieden
	
	public static int getEntry(int column, String what) {
		
		int i, countInGroup;
		
		// falls eine Spalte als Zahlenspalte erkannt wurde, dürfte diese
		// Funktion niemals aufgerufen werden, daher gibt sie dann den Wert -1
		// als Fehlercode zurück
		
		countInGroup = 0;
		for (i = 0; i < list.size(); i++) {
			if (columns.elementAt(i).intValue() == column) {
				countInGroup++;
				if (list.elementAt(i).equalsIgnoreCase(what)) {
					// System.out.print("col"+column+"w"+what+">"+countInGroup+":");
					return countInGroup;
				}
			}
		}
		columns.add(new Integer(column));
		list.add(what);
		return getEntry(column, what);
	}
}

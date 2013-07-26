/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import org.ErrorMsg;

/**
 * Eingabeneuron stellt Eingabevektorwert bereit
 * Hilfsklasse zur Verarbeitung von Eingabedaten
 * Liefert R�ckgabewert eines Spalteneintrags entweder direkt als Zahlenwert
 * oder falls Spalte keine Zahl enth�lt eine Umsetzung der Eingabe als Integer
 * Wert, der angibt, die wievielte Variation die Eingabe entspricht.
 * "yes" w�rde beispielsweise beim ersten Auftreten als Int=1 ermittelt werden
 * "no" w�rde dann als Int=2 ermittelt werden. Diese Werte werden dann infolge
 * f�r alle Eingaben in dieser Spalte verwendet
 * [ist dies sinnvoll???]
 * "m�nnlich" w�rde beispielsweise als "1", "weiblich" als "2" umgesetzt
 * eventuell ist es besser eine Aufteilung in 2 Input-Neuronen mit jeweils 1/0
 * vorzunehmen!
 */
class InputNeuron {
	
	String currentValue;
	double currentDoubleValue;
	
	public double getOutput() {
		return currentDoubleValue;
	}
	
	public void setDoubleInput(int column, String input) {
		currentValue = input;
		try {
			currentDoubleValue = (new Double(currentValue)).doubleValue();
		} catch (NumberFormatException e) {
			ErrorMsg.addErrorMessage(e);
			currentDoubleValue = Double.NaN;
		}
	}
	
	public void setInput(int column, String input) {
		currentValue = input;
		try {
			currentDoubleValue = (new Double(currentValue)).doubleValue();
		} catch (NumberFormatException e) {
			ErrorMsg.addErrorMessage(e);
			currentDoubleValue = GlobalLookUp.getEntry(column, input);
		}
	}
} // class InputNeuron

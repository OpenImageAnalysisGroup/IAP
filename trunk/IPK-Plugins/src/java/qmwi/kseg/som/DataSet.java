/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JProgressBar;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ObjectRef;

public class DataSet {
	private final Vector<String> groups = new Vector<String>();
	
	private final Vector<SOMdataEntry> data = new Vector<SOMdataEntry>();
	
	private Map som;
	
	private int somNodes;
	
	private double maxNachbar;
	
	private final double workingset = 100; // anteil der ausgangsdaten für
	// analyse
	
	private int inputs;
	
	private int decreaseN;
	
	private final boolean compactBits = false;
	
	private boolean trainedWithReturnNaN = false;
	
	// false: auto, fahrrad, roller --> 00, 01, 10, 11 ....
	// true: 100, 010, 001, ... (soviele Eingangsneuronen wie Ausprägungen)
	
	public Vector<String> bitSpaltenNamen = new Vector<String>();
	
	public JProgressBar progressAn = null;
	
	public JProgressBar progressZu = null;
	
	public JEditorPane statuspane = null;
	
	public boolean stopp = false;
	
	public void addEntry(SOMdataEntry sde) {
		data.add(sde);
	}
	
	public void addEntry(String inputLine) {
		addEntry(inputLine, false);
	}
	
	public SOMdataEntry addEntry(String inputLine, boolean isNormalizedData) {
		SOMdataEntry entry = new CSV_SOM_dataEntry(groups.size());
		entry.addValues(inputLine, isNormalizedData);
		data.add(entry);
		return entry;
	}
	
	public double calcAverage(Vector<SOMdataEntry> thedata, String group) {
		if (thedata == null)
			thedata = data;
		
		double summe = 0;
		int count = 0;
		
		for (int i = 0; i < groups.size(); i++) {
			if ((groups.elementAt(i)).equalsIgnoreCase(group)) {
				for (int j = 0; j < thedata.size(); j++) {
					try {
						double dummy = (new Double(thedata.elementAt(j).getColumnData(i))).doubleValue();
						summe += dummy;
						count++;
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		if (count > 0)
			return summe / count;
		else
			return -1;
	}
	
	/**
	 * Zuordnung der input-Daten in Ergebnisgruppen cols muss mit den Spalten die
	 * beim Training verwendet wurden, �bereinstimmen
	 * Z�hlt alle Eintr�ge die innerhalb der Spalte "group" den Wert "entry"
	 * haben
	 */
	public int count(String group, String entry) {
		int result = 0;
		for (int i = 0; i < groups.size(); i++) {
			if ((groups.elementAt(i)).equalsIgnoreCase(group)) {
				// System.out.println("GRUPPE "+((String)groups.elementAt(i)));
				for (int j = 0; j < data.size(); j++) {
					if ((data.elementAt(j)).getColumnData(i).equalsIgnoreCase(entry))
						result++;
				}
			}
		}
		return result;
	}
	
	/**
	 * Zählt die verschiedenen Ausprägungen innerhalb einer Spalte Diese Gruppe
	 * kann z.B. unterschiedliche Länder enthalten Der Rückgabewert würde dann
	 * der Anzahl an unterschiedlichen Ländern innerhalb des Datenbestandes
	 * entsprechen.
	 * TODO: Vorsicht: Falls eine Spalte nur Zahlen enthält, muss die Anzahl der
	 * Ausprägungen==1 sein. Dann wird nur _ein_ Eingabeneuron verwendet!!
	 */
	public int countAuspraegungen(Vector<SOMdataEntry> thedata, String group) {
		if (thedata == null)
			thedata = data;
		
		int max = 0;
		
		int zahlenEintraege = 0;
		
		for (int i = 0; i < groups.size(); i++) {
			if ((groups.elementAt(i)).equalsIgnoreCase(group)) {
				max = 0;
				// System.out.print("GRUPPE " + ((String) groups.elementAt(i)));
				
				// zuerst untersuchen, ob und wieviele Einträge Zahlenwerte sind
				// falls so gut wie alle Werte (>50%) Zahlen sind, dann
				// wird für die Spalte nur ein Input-Neuron verwendet, es
				// erfolgt eine
				// Skalierung der Eingabewerte auf den Bereich von -1..1
				for (int j = 0; j < thedata.size(); j++) {
					try {
						(new Double((thedata.elementAt(j)).getColumnData(i))).doubleValue();
						zahlenEintraege++;
					} catch (NumberFormatException e) {
					}
				}
				
				if (zahlenEintraege > thedata.size() * 0.50)
					return 1; // Abbildung von Spalten mit
				// ausschliesslich Zahlenwerten auf _ein_ Inputneuron!
				
				for (int j = 0; j < thedata.size(); j++) {
					if (max < GlobalLookUp.getEntry(i, (thedata.elementAt(j)).getColumnData(i)))
						max = GlobalLookUp.getEntry(i, (thedata.elementAt(j)).getColumnData(i));
				}
			}
		}
		return max;
	}
	
	// Spaltennummer anhand der Spaltenüberschrift ermitteln
	public int getColumn(String group) {
		int result = 0;
		boolean found = false;
		for (int i = 0; i < groups.size(); i++) {
			if ((groups.elementAt(i)).equalsIgnoreCase(group)) {
				result = i;
				found = true;
			}
		}
		if (!found)
			System.err.println("Warning: Column " + group + " not found.");
		return result;
	}
	
	public String getColumnNameAt(int i) {
		return groups.elementAt(i).toString();
	}
	
	public String[][] getData() {
		Vector<String[]> myData = new Vector<String[]>();
		for (int i = 0; i < data.size(); i++) {
			SOMdataEntry de = data.elementAt(i);
			myData.add(de.getColumnData());
		}
		return myData.toArray(new String[][] {});
	}
	
	public void initSOM(int gruppen, int kartenbreite, double maxNachb, int decN, int inputSize,
						boolean trainedWithReturnNaN) {
		som = new Map(gruppen, kartenbreite, inputSize);
		som.randomize();
		somNodes = gruppen;
		inputs = inputSize;
		maxNachbar = maxNachb;
		decreaseN = decN; // nach x Schritten maxNachbar um 1 verringern
		this.trainedWithReturnNaN = trainedWithReturnNaN;
	}
	
	public int inputNeuronsNeededFor(int anzAuspr) {
		if (compactBits) {
			java.math.BigInteger dummy = new java.math.BigInteger(new Integer(anzAuspr).toString());
			return dummy.bitLength();
		} else
			return anzAuspr;
	}
	
	public int inputNeuronsNeededFor(Vector<SOMdataEntry> thedata, String cols[]) {
		if (thedata == null)
			thedata = data;
		
		int neuronsNeeded = 0;
		for (int ic = 0; ic < cols.length; ic++) {
			int anzAuspr = countAuspraegungen(thedata, cols[ic]);
			if (anzAuspr == 1)
				neuronsNeeded += 1;
			else {
				
				if (compactBits) {
					java.math.BigInteger dummy = new java.math.BigInteger(new Integer(anzAuspr).toString());
					neuronsNeeded += dummy.bitLength();
				} else
					neuronsNeeded += anzAuspr;
			}
		}
		return neuronsNeeded;
	}
	
	public double searchMax(Vector<SOMdataEntry> thedata, String group) {
		if (thedata == null)
			thedata = data;
		
		double max = Double.MIN_VALUE;
		
		for (int i = 0; i < groups.size(); i++) {
			if ((groups.elementAt(i)).equalsIgnoreCase(group)) {
				max = 0;
				for (int j = 0; j < thedata.size(); j++) {
					try {
						double dummy = (new Double((thedata.elementAt(j)).getColumnData(i))).doubleValue();
						if (!Double.isNaN(dummy) && dummy > max)
							max = dummy;
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return max;
	}
	
	public double searchMin(Vector<SOMdataEntry> thedata, String group) {
		if (thedata == null)
			thedata = data;
		
		double min = Double.MAX_VALUE;
		
		for (int i = 0; i < groups.size(); i++) {
			if ((groups.elementAt(i)).equalsIgnoreCase(group)) {
				for (int j = 0; j < thedata.size(); j++) {
					try {
						double dummy = (new Double((thedata.elementAt(j)).getColumnData(i))).doubleValue();
						if (!Double.isNaN(dummy) && dummy < min)
							min = dummy;
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return min;
	}
	
	public void setGroupDescription(String inputLine) {
		inputLine = inputLine + ";";
		groups.clear();
		while (inputLine.length() > 0) {
			groups.add(inputLine.substring(0, inputLine.indexOf(";")));
			inputLine = inputLine.substring(inputLine.indexOf(";") + 1);
		}
	}
	
	/**
	 * Trains the SOM or uses the SOM in order to divide the input into groups
	 * 
	 * @param thedata
	 *           Vector mit den Eingabedaten (CSVdataEntry) oder null, dann
	 *           werden die zur Klasse gehörenden Daten verwendet
	 * @param learn
	 *           Parameter bestimmt arbeitsweise der Funktion. Falls "true",
	 *           Netzmodifizierung, falls "false", Klassenbildung
	 * @param somservice
	 *           If <>null, it can be used to update the status value (int).
	 * @param cols
	 *           [] Die zu bearbeitenden Spalten der Eingabematrix
	 * @param maxTrainInput
	 * @param d
	 * @param service
	 * @return In Klassen aufgeteilte Inputdaten oder null, falls learn==false
	 */
	@SuppressWarnings("unchecked")
	public Vector<SOMdataEntry>[] trainOrUseSOM(boolean learn, int nachbarF, String cols[], int anzWdh,
						BackgroundTaskStatusProviderSupportingExternalCall somservice, int maxTrainInput) {
		
		Vector<SOMdataEntry> thedata = data;
		
		InputNeuron currentInput = new InputNeuron();
		
		int[] iCols = new int[cols.length];
		for (int i = 0; i < cols.length; i++)
			iCols[i] = getColumn(cols[i]);
		
		Vector<SOMdataEntry>[] gruppen = new Vector[somNodes];
		
		if (!learn) {
			for (int t = 0; t < somNodes; t++)
				gruppen[t] = new Vector<SOMdataEntry>();
		}
		
		bitSpaltenNamen.clear();
		
		double max[] = new double[cols.length];
		double min[] = new double[cols.length];
		int anzAuspr[] = new int[cols.length];
		// System.out.println();
		for (int ic = 0; ic < cols.length; ic++) { // ic zählt die aktuelle
			// Spalte
			anzAuspr[ic] = countAuspraegungen(thedata, cols[ic]);
			if (anzAuspr[ic] == 1) {
				
				String ts = "";
				ts = "Spalte " + cols[ic] + " wird als Skalar betrachtet.";
				
				bitSpaltenNamen.add(new String(cols[ic] + " (S)"));
				
				if ((statuspane != null) && (learn)) {
					statuspane.setText(statuspane.getText() + ts);
				} else
					if (learn)
						System.out.print(ts);
				min[ic] = searchMin(thedata, cols[ic]);
				max[ic] = searchMax(thedata, cols[ic]);
				ts = "(min " + min[ic] + ", max " + max[ic] + ")";
				if ((statuspane != null) && (learn)) {
					statuspane.setText(statuspane.getText() + ts + "\n");
				} else
					if (learn)
						System.out.println(ts);
				
			} else {
				String ts = "Spalte " + cols[ic] + " wird Bitweise bearbeitet (" + anzAuspr[ic] + " Ausprägungen --> "
									+ inputNeuronsNeededFor(anzAuspr[ic]) + " bits)";
				
				for (int i = 0; i < inputNeuronsNeededFor(anzAuspr[ic]); i++)
					bitSpaltenNamen.add(new String(cols[ic] + " (Bit" + (i + 1) + ")"));
				
				if ((statuspane != null) && (learn)) {
					statuspane.setText(statuspane.getText() + ts + "\n");
				} else
					if (learn)
						System.out.println(ts);
			}
		}
		
		// neu: wh==anzahl trainingsschritte, solange per zufall element
		// entfernen und
		// trainieren, bis kein element mehr da, dann liste wieder füllen
		// auswahl per zufall aus liste
		
		if ((progressAn != null) && (progressZu != null)) {
			progressAn.setMaximum(anzWdh - 1);
			progressAn.setMinimum(0);
			progressZu.setMaximum(data.size() - 1);
			progressZu.setMinimum(0);
		} else
			System.out.println("no gui progress available");
		
		for (int wh = 0; wh < anzWdh; wh++) {
			
			if (somservice != null) {
				somservice.setCurrentStatusValueFine(((wh + 1) * 100d) / anzWdh);
				stopp = somservice.wantsToStop();
			}
			
			if ((progressAn != null) && (learn)) {
				progressAn.setValue(wh);
			}
			
			if (stopp)
				break;
			
			boolean used[] = new boolean[data.size()];
			for (int z = 0; z < data.size(); z++)
				used[z] = false;
			
			int trained = 0;
			// für alle Eingabedaten (bis max. maxTrainInput)
			for (int j = 0; j < data.size(); j++) {
				trained++;
				if (learn && trained > maxTrainInput)
					break;
				if ((progressZu != null) && (!learn)) {
					progressZu.setValue(j);
				}
				
				if (stopp)
					break;
				
				int oldJ = j;
				
				if (learn) {
					if (j > data.size() * workingset / 100)
						break;
					do {
						j = (int) (Math.random() * data.size());
						if (!used[j])
							break;
					} while (true);
					
					used[j] = true;
				}
				// binären Eingabevektor erstellen
				double[] data = new double[inputs];
				
				boolean fehler = false;
				
				int currentBit = 0; // aktuelles Eingabeneuron
				
				for (int ic = 0; ic < cols.length; ic++) { // ic zählt die
					// aktuelle Spalte
					
					/***********************************************************
					 * Anpassung für som.diagramme
					 * ***********************************
					 * *******************************************
					 **********************************************************/
					/*
					 * if (!learn) { qmwi.kseg.som.diagram.Aus.a("j", j);
					 * qmwi.kseg.som.diagram.Aus.a("ic", ic);
					 * qmwi.kseg.som.diagram.Aus.a("iCols[ic]", iCols[ic]);
					 * qmwi.kseg.som.diagram.Aus.a("eingaben[iCols[ic]])",
					 * ((CSVdataEntry) thedata.elementAt(j)).eingaben[iCols[ic]]); }
					 */
					/***********************************************************
					 * Anpassung für som.diagramme
					 * ***********************************
					 * *******************************************
					 **********************************************************/
					
					boolean doubleColumn = (anzAuspr[ic] == 1);
					// Spalte enthält zu 95% nur Fließkommazahlen
					boolean binColumn = (anzAuspr[ic] == 2);
					int neuronsNeeded;
					if (doubleColumn) {
						
						currentInput.setDoubleInput(iCols[ic], thedata.elementAt(j).getColumnData(iCols[ic]));
						
						neuronsNeeded = 1;
						double inpW = currentInput.currentDoubleValue;
						if (inpW == Double.MAX_VALUE) // Fehlercode von getEntry
							// und setInput
							fehler = true;
						
						// Normalisieren auf -1..1
						if (!thedata.elementAt(j).isAlreadyNormalized()) {
							inpW = 2 * (inpW - min[ic]) / (max[ic] - min[ic]) - 1;
							// System.out.println("NORMALIZE");
						}
						
						data[currentBit] = inpW;
						// if ((wh==0) && learn) System.out.print(inpW+"; ");
					} else
						if (binColumn) { // Binäre Spalte (ja/nein)
							currentInput.setInput(iCols[ic], thedata.elementAt(j).getColumnData(iCols[ic]));
							
							if (GlobalLookUp.getEntry(ic, currentInput.currentValue) > 0)
								data[currentBit] = 1;
							else
								data[currentBit] = -1;
							neuronsNeeded = 1;
							
						} else { // Bit Stalte (001, 010, 100, bzw. 01, 10, 11 bei
							// CompactBits)
							
							currentInput.setInput(iCols[ic], thedata.elementAt(j).getColumnData(iCols[ic]));
							
							if (compactBits) {
								java.math.BigInteger dummy = new java.math.BigInteger(new Integer(anzAuspr[ic]).toString());
								neuronsNeeded = dummy.bitLength();
								dummy = new java.math.BigInteger(new Integer(GlobalLookUp.getEntry(ic, currentInput.currentValue))
													.toString());
								for (int z = currentBit; z < currentBit + neuronsNeeded; z++) { // für
									// alle
									// benötigten
									// Neuronen für
									// Spalte ic
									if (dummy.testBit(z - currentBit))
										data[z] = 1;
									else
										data[z] = -1;
								}
							} else { // falls keine "compactBits"
								neuronsNeeded = inputNeuronsNeededFor(anzAuspr[ic]);
								int bitSetzen = GlobalLookUp.getEntry(ic, currentInput.currentValue);
								for (int z = currentBit; z < currentBit + neuronsNeeded; z++) { // für
									// alle
									// benötigten
									// Neuronen für
									// Spalte ic
									if (z == bitSetzen)
										data[z] = 1;
									else
										data[z] = -1;
								}
							}
						}
					currentBit += neuronsNeeded;
				} // for all Columns
				if (learn) {
					if (fehler) {
						if (wh == 0) {
							if ((statuspane != null) && (learn)) {
								// statuspane.setText(statuspane.getText() +
								// "e");
							} else {
								System.out.print("e");
							}
							
						}
					} else {
						if (decreaseN > 0)
							som.analyzeNewInput(wh, anzWdh, nachbarF, Tools.myMax(maxNachbar - (wh / decreaseN), 0.0), data);
						else
							som.analyzeNewInput(wh, anzWdh, nachbarF, maxNachbar, data);
					}
				} else {
					// recall (not learning)
					if (fehler) {
						if ((statuspane != null) && (learn)) {
							String ts = "\nFehler in Eingabezeile " + j + ". Datensatz wird nicht zugeordnet.";
							statuspane.setText(statuspane.getText() + "\n" + ts);
						} else {
							System.out.print("FZ: " + j + ". ");
						}
					} else {
						if (isValidData(data)) {
							ObjectRef differencesToCentroids = new ObjectRef();
							gruppen[som.getNetZ(data, differencesToCentroids)].add(thedata.elementAt(j));
							thedata.elementAt(j).setDifferences((ArrayList<Double>) differencesToCentroids.getObject());
						}
					}
				}
				j = oldJ;
			} // für alle Eingabedatensätze
				// som.printMatrix();
			if (learn && (statuspane == null))
				System.out.print(".");
		} // Wiederholung des Trainings
		if ((statuspane != null) && (learn)) {
			statuspane.setText(statuspane.getText() + "\n");
		} else {
			System.out.println("");
		}
		if (learn)
			return null;
		else
			return gruppen;
	}
	
	private boolean isValidData(double[] check) {
		return true;
	}
	
	/**
	 * Get a iterator for the data.
	 * 
	 * @return The iterator will return <code>SOMdataEntry</code> objects.
	 */
	public Iterator<SOMdataEntry> iterator() {
		return data.iterator();
	}
	
	/**
	 * Clears the dataset, use <code>addEntry</code> to fill it again.
	 */
	public void clearEntries() {
		data.clear();
	}
	
	public int getGroupSize() {
		return groups.size();
	}
	
	public void setBetaAndGamma(double betaParam, double gammaParam) {
		som.beta = betaParam;
		som.gamma = gammaParam;
	}
	
	public int getDataSetSize() {
		return data.size();
	}
	
	public Map getSOMmap() {
		return som;
	}
	
	public boolean getTrainedWithReturnNaN() {
		return trainedWithReturnNaN;
	}
} // class DataSet

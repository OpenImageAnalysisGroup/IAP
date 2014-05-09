/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;

public class MainAnalysis {
	
	/**
	 * Starts the SOM-Workbench.
	 * 
	 * @param args
	 *           an array of command-line arguments
	 */
	public static void main(String[] args) {
		boolean nochmal;
		try {
			// Eingabestream �ffnen
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.println("Kundensegmentierung - Testprogramm erstellt von Christian Klukas");
			System.out.println("----------------------------------------------------------------");
			
			String fileName;
			
			if ((args.length != 1) || (args[0].indexOf("?") != -1)) {
				System.out.println("Kein Dateiname als Parameter angegeben. Bitte Input-Datei spezifizieren.");
				System.out.print("Dateiname (Excel-csv-Datei): ");
				fileName = in.readLine();
			} else
				fileName = args[0];
			
			DataSet mydataset = CSVreader.readFile(fileName);
			
			do {
				
				System.out.println("Anzahl Datensätze: " + mydataset.getDataSetSize());
				
				System.out.println("Datensätze enthalten folgende Informationen:");
				for (int i = 0; i < mydataset.getGroupSize(); i++) {
					System.out.print(mydataset.getColumnNameAt(i));
					if (i < mydataset.getGroupSize() - 1)
						System.out.print(", ");
				}
				System.out.println();
				
				Vector<String> columnsSelected = new Vector<String>();
				System.out
									.println("Geben Sie nun an, welche der Spalten zur Klassifizierung herangezogen werden sollen (J/N, Enter=ende):");
				
				for (int i = 0; i < mydataset.getGroupSize(); i++) {
					System.out.print(mydataset.getColumnNameAt(i) + "? ");
					String eingabe = in.readLine();
					
					if ((eingabe == null) || (eingabe.equals("")))
						break;
					if (eingabe.toUpperCase().indexOf("N") == -1)
						columnsSelected.add(mydataset.getColumnNameAt(i));
					
				}
				
				String[] gr = new String[columnsSelected.size()];
				for (int i = 0; i < gr.length; i++)
					gr[i] = columnsSelected.elementAt(i);
				
				System.out.print("Anzahl der Neuronen?: ");
				String anzN = in.readLine();
				int somSize = new Integer(anzN).intValue();
				
				System.out.print("Breite der Karte? (0=autoQuadrat): ");
				String breiteS = in.readLine();
				int breite = new Integer(breiteS).intValue();
				if (breite <= 0) {
					breite = Tools.getBreite(somSize);
					System.out.println("(" + breite + ")");
				}
				
				System.out.print("Maximal zu betrachtende Nachbarschaft? (-1=unbegrenzt): ");
				String maxNachbarS = in.readLine();
				double maxNachbar = new Double(maxNachbarS).doubleValue();
				
				System.out.print("Nachbarschaft nach [x] Wiederholungen um 1 verringern: (0=konstant belassen): ");
				String decNs = in.readLine();
				int decN = new Integer(decNs).intValue();
				
				System.out.println("Erstelle SOM.");
				mydataset.initSOM(somSize, breite, maxNachbar, decN, mydataset.inputNeuronsNeededFor(null, gr), false);
				
				System.out.print("Wie oft soll der Lernvorgang wiederholt werden?: ");
				String anzWdhE = in.readLine();
				int anzWdh = new Integer(anzWdhE).intValue();
				
				System.out
									.print("Typ der Nachbarschaftsfunktion (1=Zylinder, 2=Kegel, 3=Gauss, 4=Mexican Hat, 5=Cosinus)?: ");
				String nachbarFS = in.readLine();
				int nachbarF = new Integer(nachbarFS).intValue();
				
				System.out.print("Beta? (0.1): ");
				String betaS = in.readLine();
				double betaInit = new Double(betaS).doubleValue();
				System.out.print("Gamma? (2): ");
				String gammaS = in.readLine();
				double gammaInit = new Double(gammaS).doubleValue();
				
				mydataset.setBetaAndGamma(betaInit, gammaInit);
				
				System.out.print("Analyse wird gestartet (" + mydataset.inputNeuronsNeededFor(null, gr)
									+ " Input-Neuronen verwendet)");
				
				mydataset.trainOrUseSOM(true, nachbarF, gr, anzWdh, null, mydataset.getDataSetSize());
				
				System.out.println("Analyse beendet.");
				
				System.out.println("Ordne alle Teilnehmer anhand der SOM in Gruppen ein...");
				
				Vector<SOMdataEntry> klassen[] = mydataset.trainOrUseSOM(false, nachbarF, gr, 1, null, 0);
				
				System.out.print("Gruppengroessen:");
				for (int i = 0; i < klassen.length; i++) {
					System.out.print(klassen[i].size() + " ");
				}
				System.out.println("");
				
				System.out.print("Gruppen am Bildschirm ausgeben? (J/N): ");
				String eingabe = in.readLine();
				if (!((eingabe == null) || (eingabe.equals(""))))
					if (eingabe.toUpperCase().indexOf("N") == -1)
						for (int iG = 0; iG < klassen.length; iG++) {
							// System.out.println("Gruppe:" + (iG + 1));
							System.out.print("{G" + (iG + 1));
							for (int iEl = 0; iEl < klassen[iG].size(); iEl++) {
								System.out.print("[");
								for (int iCol = 0; iCol < gr.length; iCol++) {
									System.out.print((klassen[iG].elementAt(iEl)).getColumnData(mydataset.getColumn(gr[iCol])));
									if (iCol < gr.length - 1)
										System.out.print(", ");
								}
								System.out.print("]");
								if (iEl < klassen[iG].size() - 1)
									System.out.print(";");
							}
							System.out.print("}");
							if (((iG + 1) % breite) == 0)
								System.out.println();
						}
				System.out.println();
				System.out.print("Skalar-Durchschnittsberechnung durchführen? (J/N): ");
				eingabe = in.readLine();
				if (!((eingabe == null) || (eingabe.equals(""))))
					if (eingabe.toUpperCase().indexOf("N") == -1)
						for (int iG = 0; iG < klassen.length; iG++) {
							System.out.println("Gruppe:" + (iG + 1));
							if (klassen[iG].size() == 0)
								System.out.println("- keine Einträge");
							else {
								System.out.print("[");
								for (int iCol = 0; iCol < gr.length; iCol++) {
									System.out.print(mydataset.calcAverage(klassen[iG], gr[iCol]));
									if (iCol < gr.length - 1)
										System.out.print(", ");
								}
								System.out.println("]");
							}
							
						}
				
				System.out.println();
				System.out.print("Gruppen als CSV-Datei speichern? (Dateiname=ja, Enter=nein): ");
				String fileOutputName = in.readLine();
				
				if ((fileOutputName != null) && (!(fileOutputName.equals("")))) {
					PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileOutputName)));
					// System.setOut(out);
					
					out.print("Klasse;");
					for (int i = 0; i < mydataset.getGroupSize(); i++) {
						out.print(mydataset.getColumnNameAt(i));
						if (i < mydataset.getGroupSize() - 1)
							out.print(";");
					}
					out.println();
					
					for (int iG = 0; iG < klassen.length; iG++) {
						for (int iEl = 0; iEl < klassen[iG].size(); iEl++) {
							out.print("K" + (iG + 1));
							out.print(";");
							for (int iCol = 0; iCol < mydataset.getGroupSize(); iCol++) {
								out.print((klassen[iG].elementAt(iEl)).getColumnData(iCol));
								if (iCol < mydataset.getGroupSize() - 1)
									out.print(";");
							}
							out.println();
						}
					}
					out.close();
				}
				
				nochmal = false;
				
				System.out.print("Ausgangsdaten nochmals analysieren? (J/N): ");
				eingabe = in.readLine();
				if (!((eingabe == null) || (eingabe.equals(""))))
					if (eingabe.toUpperCase().indexOf("N") == -1)
						nochmal = true;
				
			} while (nochmal);
		} catch (Exception e) {
			System.out.println("Runtime error: " + e);
		}
		System.out.println("Programm beendet.");
		
	} // main
} // class MainAnalysis

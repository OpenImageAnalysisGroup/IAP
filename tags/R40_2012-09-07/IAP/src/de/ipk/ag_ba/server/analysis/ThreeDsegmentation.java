/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package de.ipk.ag_ba.server.analysis;

import java.util.HashMap;
import java.util.Vector;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import qmwi.kseg.som.DataSet;
import qmwi.kseg.som.SOMdataEntry;
import qmwi.kseg.som.Tools;

public class ThreeDsegmentation {
	
	public static void segment(byte[][][] threeDdata, int somSize,
						BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		byte[][][] threeDdataTest = new byte[][][] { new byte[][] { new byte[] { 2, 3, 5 }, new byte[] { 2, 3, 5 } },
							new byte[][] { new byte[] { 4, 5, 6 }, new byte[] { 7, 8, 9 } } };
		
		if (threeDdata == null)
			threeDdata = threeDdataTest;
		
		DataSet mydataset = getDataSet(threeDdata);
		
		System.out.println("Number of data sets: " + mydataset.getDataSetSize());
		
		System.out.println("Analyzed information:");
		for (int i = 0; i < mydataset.getGroupSize(); i++) {
			System.out.print(mydataset.getColumnNameAt(i));
			if (i < mydataset.getGroupSize() - 1)
				System.out.print(", ");
		}
		System.out.println();
		
		Vector<String> columnsSelected = new Vector<String>();
		
		for (int i = 0; i < mydataset.getGroupSize(); i++) {
			System.out.print(mydataset.getColumnNameAt(i) + "");
			columnsSelected.add(mydataset.getColumnNameAt(i));
		}
		
		String[] gr = new String[columnsSelected.size()];
		for (int i = 0; i < gr.length; i++)
			gr[i] = columnsSelected.elementAt(i);
		
		System.out.print("Number of clusters: " + somSize);
		
		int breite = 0;
		if (breite <= 0) {
			breite = Tools.getBreite(somSize);
			System.out.println("(" + breite + ")");
		}
		
		double maxNachbar = -1;
		
		int decN = 8;
		
		System.out.println("Generate SOM...");
		mydataset.initSOM(somSize, breite, maxNachbar, decN, mydataset.inputNeuronsNeededFor(null, gr), false);
		
		int anzWdh = 20;
		
		// System.out.print("Typ der Nachbarschaftsfunktion (1=Zylinder, 2=Kegel, 3=Gauss, 4=Mexican Hat, 5=Cosinus)?: ");
		int nachbarF = 1;
		double betaInit = -0.1;
		double gammaInit = 2;
		
		mydataset.setBetaAndGamma(betaInit, gammaInit);
		
		System.out.print("Start analysis (" + mydataset.inputNeuronsNeededFor(null, gr) + " input-neurons are used)");
		
		mydataset.trainOrUseSOM(true, nachbarF, gr, anzWdh, status, (int) (mydataset.getDataSetSize() * 0.2d));
		
		System.out.println("Analysis finished.");
		
		System.out.println("Categorize input data...");
		
		Vector<SOMdataEntry> klassen[] = mydataset.trainOrUseSOM(false, nachbarF, gr, 1, status, 0);
		
		System.out.print("Number of entries in each group:");
		for (int i = 0; i < klassen.length; i++) {
			System.out.print(klassen[i].size() + " ");
		}
		System.out.println("");
		
		HashMap<Integer, Double> group2average = new HashMap<Integer, Double>();
		
		for (int iG = 0; iG < klassen.length; iG++) {
			System.out.println("Gruppe:" + (iG + 1));
			if (klassen[iG].size() == 0)
				System.out.println("- keine Einträge");
			else {
				System.out.print("[");
				for (int iCol = 0; iCol < 1 /* gr.length */; iCol++) {
					Double avg = mydataset.calcAverage(klassen[iG], gr[iCol]);
					group2average.put(iG, avg);
					System.out.print(avg);
					if (iCol < gr.length - 1)
						System.out.print(", ");
				}
				System.out.println("]");
			}
			
		}
		
		for (int iG = 0; iG < klassen.length; iG++) {
			for (int iEl = 0; iEl < klassen[iG].size(); iEl++) {
				SOMdataEntry sde = klassen[iG].elementAt(iEl);
				int[] idx = (int[]) sde.getUserData();
				Double avg = group2average.get(iG);
				threeDdata[idx[0]][idx[1]][idx[2]] = (byte) (avg % 255);
			}
		}
	} // main
	
	/**
	 * @param threeDdata
	 * @return
	 */
	private static DataSet getDataSet(byte[][][] threeDdata) {
		DataSet result = new DataSet();
		result.setGroupDescription("Value;"); // ;D");
		for (int x = 0; x < threeDdata.length; x++)
			for (int y = 0; y < threeDdata[x].length; y++)
				for (int z = 0; z < threeDdata[x][y].length; z++) {
					// double dist = Math.pow(x*x+y*y+z*z, 1d/3d);
					String inputLine = threeDdata[x][y][z] + ""; // ]+";"+x+";"+y+";"+z;
																				// // +";"+dist;
					SOMdataEntry sde = result.addEntry(inputLine, false);
					sde.setUserData(new int[] { x, y, z });
				}
		return result;
	}
} // class MainAnalysis

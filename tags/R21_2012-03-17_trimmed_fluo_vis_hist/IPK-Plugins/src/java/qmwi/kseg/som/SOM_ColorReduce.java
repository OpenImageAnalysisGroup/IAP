/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.color.ColorUtil;

public class SOM_ColorReduce {
	
	public static ArrayList<Color> findCommonColors(ArrayList<Color> inputColors, int maxColors,
						BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		System.out.println("Color data points: " + inputColors.size());
		System.out.println("Maximum number of colors: " + maxColors);
		
		DataSet mydataset = getDataSet(inputColors);
		
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
		
		System.out.print("Number of clusters: " + maxColors);
		
		int breite = 0;
		if (breite <= 0) {
			breite = Tools.getBreite(maxColors);
			System.out.println("(" + breite + ")");
		}
		
		double maxNachbar = -1;
		
		int decN = 8;
		
		System.out.println("Generate SOM...");
		mydataset.initSOM(maxColors, breite, maxNachbar, decN, mydataset.inputNeuronsNeededFor(null, gr), false);
		
		int anzWdh = 20;
		
		// System.out.print("Typ der Nachbarschaftsfunktion (1=Zylinder, 2=Kegel, 3=Gauss, 4=Mexican Hat, 5=Cosinus)?: ");
		int nachbarF = 1;
		double betaInit = -0.1;
		double gammaInit = 2;
		
		mydataset.setBetaAndGamma(betaInit, gammaInit);
		
		System.out.print("Start analysis (" + mydataset.inputNeuronsNeededFor(null, gr) + " input-neurons are used)");
		
		mydataset.trainOrUseSOM(true, nachbarF, gr, anzWdh, status, mydataset.getDataSetSize());
		
		System.out.println("Analysis finished.");
		
		System.out.println("Categorize input data...");
		
		Vector<SOMdataEntry> klassen[] = mydataset.trainOrUseSOM(false, nachbarF, gr, 1, status, mydataset
							.getDataSetSize());
		
		System.out.print("Number of entries in each group:");
		for (int i = 0; i < klassen.length; i++) {
			System.out.print(klassen[i].size() + " ");
		}
		System.out.println("");
		
		HashMap<Integer, ArrayList<Color>> group2colors = new HashMap<Integer, ArrayList<Color>>();
		for (int iG = 0; iG < klassen.length; iG++) {
			if (klassen[iG].size() == 0)
				;
			else {
				for (SOMdataEntry sde : klassen[iG]) {
					if (!group2colors.containsKey(iG))
						group2colors.put(iG, new ArrayList<Color>());
					group2colors.get(iG).add((Color) sde.getUserData());
				}
			}
			
		}
		
		ArrayList<Color> result = new ArrayList<Color>();
		for (int iG = 0; iG < klassen.length; iG++) {
			ArrayList<Color> colorsOfGroup = group2colors.get(iG);
			if (colorsOfGroup != null && colorsOfGroup.size() > 0) {
				Color c = new Color(ColorUtil.getAverageColor(colorsOfGroup));
				result.add(c);
			}
		}
		System.out.println("Found " + result.size() + " common colors!");
		return result;
		
	} // main
	
	/**
	 * @param threeDdata
	 * @return
	 */
	private static DataSet getDataSet(ArrayList<Color> colors) {
		DataSet result = new DataSet();
		// result.setGroupDescription("Red;Green;Blue"); // ;D");
		result.setGroupDescription("Hue;Sat;Val");
		float[] hsb = new float[3];
		for (Color c : colors) {
			Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getBlue(), hsb);
			// String inputLine = c.getRed()+";"+c.getGreen()+";"+c.getBlue();
			String inputLine = hsb[0] + ";" + hsb[1] + ";" + hsb[2];
			SOMdataEntry sde = result.addEntry(inputLine, false);
			sde.setUserData(c);
		}
		return result;
	}
} // class MainAnalysis

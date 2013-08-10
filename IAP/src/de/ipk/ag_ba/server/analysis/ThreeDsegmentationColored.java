/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package de.ipk.ag_ba.server.analysis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.Colors;

import qmwi.kseg.som.DataSet;
import qmwi.kseg.som.SOMdataEntry;
import qmwi.kseg.som.Tools;
import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk.ag_ba.image.color.ColorXYZ;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.IntVolumeVisitor;

public class ThreeDsegmentationColored {
	
	public static void segment(LoadedVolumeExtension volume, int somSize,
						BackgroundTaskStatusProviderSupportingExternalCall status, int sx, int sy, int sz) throws Exception {
		
		DataSet mydataset = getDataSet(volume, sx, sy, sz);
		
		status.setCurrentStatusText1("Data sets: " + mydataset.getDataSetSize());
		
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
		
		status.setCurrentStatusText1("Clusters: " + somSize);
		
		int breite = 0;
		if (breite <= 0) {
			breite = Tools.getBreite(somSize);
			System.out.println("(" + breite + ")");
		}
		
		double maxNachbar = -1;
		
		int decN = 8;
		
		status.setCurrentStatusText1("Init SOM");
		mydataset.initSOM(somSize, breite, maxNachbar, decN, mydataset.inputNeuronsNeededFor(null, gr), false);
		
		int anzWdh = 20;
		
		// System.out.print("Typ der Nachbarschaftsfunktion (1=Zylinder, 2=Kegel, 3=Gauss, 4=Mexican Hat, 5=Cosinus)?: ");
		int nachbarF = 1;
		double betaInit = -0.1;
		double gammaInit = 2;
		
		mydataset.setBetaAndGamma(betaInit, gammaInit);
		
		status.setCurrentStatusText1("Analysis (" + mydataset.inputNeuronsNeededFor(null, gr) + " neurons, "
							+ mydataset.getDataSetSize() + " data sets)");
		
		mydataset.trainOrUseSOM(true, nachbarF, gr, anzWdh, status, (int) (mydataset.getDataSetSize() * 0.5d));
		
		System.out.println("Analysis finished.");
		
		status.setCurrentStatusText1("Categorize Data");
		
		Vector<SOMdataEntry> klassen[] = mydataset.trainOrUseSOM(false, nachbarF, gr, 1, status, 0);
		
		status.setCurrentStatusText1("Post-Processing");
		System.out.print("Number of entries in each group:");
		for (int i = 0; i < klassen.length; i++) {
			System.out.print(klassen[i].size() + " ");
		}
		System.out.println("");
		
		HashMap<Integer, ArrayList<Float>> group2average = new HashMap<Integer, ArrayList<Float>>();
		
		for (int iG = 0; iG < klassen.length; iG++) {
			System.out.println("Gruppe:" + (iG + 1));
			if (klassen[iG].size() == 0)
				System.out.println("- keine Einträge");
			else {
				System.out.print("[");
				group2average.put(iG, new ArrayList<Float>());
				for (int iCol = 0; iCol < gr.length; iCol++) {
					double avg = mydataset.calcAverage(klassen[iG], gr[iCol]);
					group2average.get(iG).add((float) avg);
					System.out.print(avg);
					if (iCol < gr.length - 1)
						System.out.print(", ");
				}
				System.out.println("]");
			}
			
		}
		
		int[][][] threeDdata = volume.getLoadedVolume().getIntArray();
		
		for (int x = 0; x < sx; x++)
			for (int y = 0; y < sy; y++)
				for (int z = 0; z < sz; z++) {
					threeDdata[x][y][z] = ImageOperation.BACKGROUND_COLORint;
				}
		
		ArrayList<Color> colors = Colors.get(klassen.length, 1d);
		
		for (int iG = 0; iG < klassen.length; iG++) {
			for (int iEl = 0; iEl < klassen[iG].size(); iEl++) {
				SOMdataEntry sde = klassen[iG].elementAt(iEl);
				int[] idx = (int[]) sde.getUserData();
				ArrayList<Float> avg = group2average.get(iG);
				avg.clear();
				
				threeDdata[idx[0]][idx[1]][idx[2]] = colors.get(iG).getRGB();
			}
		}
	} // main
	
	private static DataSet getDataSet(LoadedVolumeExtension threeDdata, int mx, int my, int mz) throws Exception {
		final DataSet result = new DataSet();
		result.setGroupDescription("L;A;B");
		
		final ColorXYZ xyz = new ColorXYZ(0, 0, 0);
		final Color_CIE_Lab lab = new Color_CIE_Lab(0, false);
		
		threeDdata.getLoadedVolume().visitIntArray(new IntVolumeVisitor() {
			@Override
			public void visit(int x, int y, int z, int value) throws Exception {
				if (value != ImageOperation.BACKGROUND_COLORint) {
					ColorUtil.getLABfromRGB(value, lab, xyz);
					String inputLine = lab.getL() + ";" + lab.getA() + ";" + lab.getB();
					SOMdataEntry sde = result.addEntry(inputLine, false);
					sde.setUserData(new int[] { x, y, z });
				}
			}
		});
		
		return result;
	}
} // class MainAnalysis

/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.ErrorMsg;
import org.OpenFileDialogService;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class DistanceMatrix {
	
	HashMap<String, Integer> ij2distance = new HashMap<String, Integer>();
	HashMap<String, ArrayList<String>> ij2history = new HashMap<String, ArrayList<String>>();
	HashMap<Integer, String> i2lbl = new HashMap<Integer, String>();
	int maxI = 0;
	int maxJ = 0;
	
	public void setDistanceInformation(String labelA, String labelB, int i, int j, int distance, ArrayList<String> calculationHistory) {
		if (i > maxI)
			maxI = i;
		if (j > maxJ)
			maxJ = j;
		ij2distance.put(i + ":" + j, distance);
		ij2history.put(i + ":" + j, calculationHistory);
		i2lbl.put(i, labelA);
		System.out.println("Distance between " + i + ":" + j + " / " + labelA + ":" + labelB + " = " + distance);
	}
	
	public void printMatrix() {
		for (int j = 0; j <= maxI; j++)
			System.out.print("\t" + i2lbl.get(j));
		System.out.println();
		for (int i = 0; i <= maxI; i++) {
			System.out.print(i2lbl.get(i) + "\t");
			for (int j = 0; j <= maxI; j++) {
				System.out.print(ij2distance.get(i + ":" + j) + "\t");
			}
			System.out.println();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void saveDMF() {
		File targetFile = OpenFileDialogService.getSaveFile(new String[] { ".DMF" }, "Distance Matrix File (.dmf)");
		if (targetFile == null)
			return;
		
		TextFile tf = new TextFile();
		tf.add("   DistanceMatrix File v1.0");
		tf.add("# " + targetFile.getAbsolutePath());
		tf.add("# " + new Date().toLocaleString());
		tf.add("# User : " + System.getenv("USER"));
		tf.add("");
		tf.add("Species = " + (maxJ + 1));
		tf.add("Width = 1000");
		tf.add("Height = 1000");
		tf.add("//");
		tf.add("");
		tf.add("# Distance Matrix");
		for (int i = 0; i <= maxI; i++) {
			String line = i2lbl.get(i) + "\t";
			for (int j = 0; j <= i; j++) {
				line += ij2distance.get(i + ":" + j) + (j < i ? " " : ";");
			}
			tf.add(line);
		}
		
		/*
		 * DistanceMatrix File v1.0
		 * # C:\test.dmf
		 * # 1999/06/11 PM 09:30:01
		 * # User : chris
		 * Species = 11
		 * Width = 1000
		 * Height = 1000
		 * //
		 * # Distance Matrix
		 * 32_2_2 0;
		 * 6_6_11 42 0;
		 * 3_6_2 -3 -43 0;
		 */
		try {
			tf.write(targetFile);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
}

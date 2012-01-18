package de.ipk.ag_ba.special;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

public class LABcalculator {
	
	public static void main(String[] args) throws IOException, Exception {
		FlexibleImage f1 = new FlexibleImage(FileSystemHandler.getURL(new File(args[0])));
		FlexibleImage f2 = new FlexibleImage(FileSystemHandler.getURL(new File(args[1])));
		
		// f1 = f1.getIO().medianFilter32Bit().medianFilter32Bit().getImage();
		// f2 = f2.getIO().medianFilter32Bit().medianFilter32Bit().getImage();
		
		System.out.println("Images: file 1: " + f1.getWidth() + "x" + f1.getHeight() + ", file 2: " + f2.getWidth() + "x" + f2.getHeight());
		float[][] lab1 = f1.getLab(false);
		float[][] lab2 = f2.getLab(false);
		int[] ress = new int[lab1[0].length];
		double[][] res = new double[3][lab1[0].length];
		for (int i = 0; i < lab1[0].length; i++) {
			float l1, l2, a1, a2, b1, b2;
			
			l1 = lab1[0][i];
			l2 = lab2[0][i];
			
			a1 = lab1[1][i];
			a2 = lab2[1][i];
			
			b1 = lab1[2][i];
			b2 = lab2[2][i];
			
			int r, g, b;
			
			if (l1 < l2) {
				r = (int) Math.log(l2 - l1 + 1) * 30;
				r = (int) (l2 - l1);
				g = 0;
				b = 0;
			} else {
				r = 0;
				g = 0;
				b = (int) Math.log(l1 - l2 + 1) * 30;
				b = (int) (l1 - l2);
				// b = (int) (l1 - l2);
			}
			
			ress[i] = new Color(r, g, b).getRGB();
		}
		
		FlexibleImageStack fis = new FlexibleImageStack();
		fis.addImage("image 1", f1);
		fis.addImage("image 2", f2);
		
		FlexibleImage f3 = new FlexibleImage(f1.getWidth(), f1.getHeight(), ress);
		
		fis.addImage("l: image 1 - image 2, a,b: avg(image 1, image 2)", f3);
		
		fis.print("LAB Calculation");
	}
	
}

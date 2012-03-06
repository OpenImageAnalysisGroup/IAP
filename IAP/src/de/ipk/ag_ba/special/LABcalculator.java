package de.ipk.ag_ba.special;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

public class LABcalculator {
	
	public static void main(String[] args) throws IOException, Exception {
		ArrayList<File> files;
		if (args == null || args.length != 2) {
			files = OpenFileDialogService.getFiles(new String[] { ".png", ".jpg" }, "PNG or JPEG Images");
			if (files == null || files.size() == 0)
				System.exit(1);
		} else {
			files = new ArrayList<File>();
			files.add(new File(args[0]));
			files.add(new File(args[1]));
		}
		while (files.size() >= 2) {
			File fi1 = files.remove(0);
			File fi2 = files.remove(0);
			FlexibleImage f1 = new FlexibleImage(FileSystemHandler.getURL(fi1));
			FlexibleImage f2 = new FlexibleImage(FileSystemHandler.getURL(fi2));
			
			// f1 = f1.getIO().medianFilter32Bit().medianFilter32Bit().getImage();
			// f2 = f2.getIO().medianFilter32Bit().medianFilter32Bit().getImage();
			
			System.out.println("Images: file 1: " + f1.getWidth() + "x" + f1.getHeight() + ", file 2: " + f2.getWidth() + "x" + f2.getHeight());
			float[][] lab1 = f1.getLab(false);
			float[][] lab2 = f2.getLab(false);
			final int[] ress = new int[lab1[0].length];
			
			ExecutorService tpe = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs());
			
			final int lennn = lab1[0].length;
			
			for (int i = 0; i < lab1[0].length; i++) {
				float l1, l2, a1, a2, b1, b2;
				
				l1 = lab1[0][i];
				l2 = lab2[0][i];
				
				a1 = lab1[1][i];
				a2 = lab2[1][i];
				
				b1 = lab1[2][i];
				b2 = lab2[2][i];
				
				int r, g, b;
				boolean lab = true;
				if (lab) {
					float lf = l1 - l2 > 0 ? l1 - l2 : 0;
					float af = (a1 + a2) / 2f; // a2 - a1;
					float bf = (b1 + b2) / 2f; // b2 - b1;
					final int i_f = i;
					final float lf_f = lf;
					final float af_f = af;
					final float bf_f = bf;
					boolean search = true;
					if (search) {
						tpe.submit(new Runnable() {
							@Override
							public void run() {
								ress[i_f] = ImageOperation.searchRGBfromLAB(lf_f, af_f, bf_f);
								// System.out.print(".");
								if (i_f % 150 == 0)
									System.out.println("Progress: " + StringManipulationTools.formatNumber(100d * i_f / lennn, "#.##") + "%");
							}
						});
					} else {
						// Color sc = new Color(ImageOperation.searchRGBfromLAB(lf, af, bf));
						
						Color c = new Color_CIE_Lab((lf - 40) / 2.1, af - 98, bf - 97 - 6).getColor();
						
						// c = sc;
						
						// float l3 = ImageOperation.labCube[c.getRed()][c.getGreen()][c.getBlue()];
						// float a3 = ImageOperation.labCube[c.getRed()][c.getGreen()][c.getBlue() + 256];
						// float b3 = ImageOperation.labCube[c.getRed()][c.getGreen()][c.getBlue() + 512];
						
						// System.out.println("l: " + lf + " => " + l3);
						// System.out.println("a: " + af + " => " + a3);
						// System.out.println("b: " + bf + " => " + b3);
						
						// System.out.println("R[" + sc.getRed() + "/" + c.getRed() + "] || G[" + sc.getGreen() + "/" + c.getGreen() + "] || B[" + sc.getBlue() + "/"
						// + c.getBlue() + "]");
						ress[i] = c.getRGB();
					}
				} else {
					if (l1 < l2) {
						r = (int) (l2 - l1);
						g = 0;
						b = 0;
						ress[i] = new Color(r, g, b).getRGB();
					} else {
						r = 0;
						g = 0;
						b = (int) (l1 - l2);
						ress[i] = new Color(r, g, b).getRGB();
					}
				}
			}
			tpe.shutdown();
			tpe.awaitTermination(365, TimeUnit.DAYS);
			FlexibleImageStack fis = new FlexibleImageStack();
			fis.addImage("image 1 (" + f1.getFileName() + ")", f1);
			fis.addImage("image 2 (" + f2.getFileName() + ")", f2);
			
			FlexibleImage f3 = new FlexibleImage(f1.getWidth(), f1.getHeight(), ress);
			
			fis.addImage("lab, l1 < l2 ? r=l2-l1 : b=l1-l2", f3);
			// fis.addImage("l: image 1 - image 2, a,b: avg(image 1, image 2)", f3);
			
			fis.print("LAB Calculation (" + f1.getFileName() + ";" + f2.getFileName() + ")");
			
			String path = fi1.getAbsolutePath();
			path = path.substring(0, path.lastIndexOf(File.separator)); // remove file name to retrieve path
			String fileName = path + File.separator + f1.getFileName().substring(0, f1.getFileName().lastIndexOf(".")) + "-" + f2.getFileName();
			fileName = fileName.substring(0, fileName.lastIndexOf(".")); // remove file extension
			// fis.saveAsLayeredTif(new File(fileName + ".tif"));
			// f3.saveToFile(fileName + ".png");
		}
	}
}

/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.operation;

import ij.ImagePlus;

import java.awt.image.BufferedImage;

import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.image.structures.Image;

/**
 * @author entzian, klukas
 */
public class ImageDisplay {
	
	public static void show(ImagePlus image, String text) {
		image.setTitle(text);
		image.show(text);
	}
	
	public static void show(Image image, String text) {
		show(image.getAsBufferedImage(), text);
	}
	
	public static void show(BufferedImage image, String text) {
		show(image, text, ImageDisplayOption.IMAGEJ);
	}
	
	public static void show(BufferedImage image, String text, ImageDisplayOption typ) {
		switch (typ) {
			case GRAVISTO_SERVICE:
				printGravistoService(image, text);
				break;
			
			case IMAGEJ:
				printImagej(image, text);
				break;
			
			case CONSOLE:
				printToConsole(ImageConverter.convertBIto2A(image), text);
				break;
			
			default:
				printImagej(image, text);
				break;
		
		}
	}
	
	private static void printToConsole(int[][] image, String text) {
		System.out.println(text);
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++)
				System.out.print(image[i][j] + "\t");
			System.out.println("");
		}
	}
	
	private static void printImagej(BufferedImage image, String text) {
		ImagePlus img = ImageConverter.convertBItoIJ(image);
		img.show(text);
	}
	
	private static void printGravistoService(BufferedImage image, String text) {
		GravistoService.showImage(image, text);
	}
}

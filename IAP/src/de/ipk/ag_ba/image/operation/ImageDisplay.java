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
	
	public static ImagePlus show(ImagePlus image, String text) {
		image.setTitle(text);
		image.show(text);
		return image;
	}
	
	public static ImagePlus show(Image image, String text) {
		return show(image.getAsBufferedImage(), text);
	}
	
	public static ImagePlus show(BufferedImage image, String text) {
		return show(image, text, ImageDisplayOption.IMAGEJ);
	}
	
	public static ImagePlus show(BufferedImage image, String text, ImageDisplayOption typ) {
		switch (typ) {
			case GRAVISTO_SERVICE:
				printGravistoService(image, text);
				break;
			
			case IMAGEJ:
				return printImagej(image, text);
				
			case CONSOLE:
				printToConsole(ImageConverter.convertBIto2A(image), text);
				break;
			
			default:
				printImagej(image, text);
				break;
		
		}
		return null;
	}
	
	private static void printToConsole(int[][] image, String text) {
		System.out.println(text);
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++)
				System.out.print(image[i][j] + "\t");
			System.out.println("");
		}
	}
	
	private static ImagePlus printImagej(BufferedImage image, String text) {
		ImagePlus img = ImageConverter.convertBItoIJ(image);
		img.show(text);
		return img;
	}
	
	private static void printGravistoService(BufferedImage image, String text) {
		GravistoService.showImage(image, text);
	}
}

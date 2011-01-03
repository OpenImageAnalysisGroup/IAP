/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import ij.ImagePlus;

import java.awt.image.BufferedImage;

import org.graffiti.editor.GravistoService;

/**
 * @author entzian
 */
public class PrintImage {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
	
	// ######### 1Array #######
	// public static void printBinarImage(int[] image, int width, int height) {
	// printBinarImage(ImageConverter.convert1Ato2A(width, height, image), PrintOption.IMAGEJ);
	// }
	//
	// public static void printBinarImage(int[] image, int width, int height, String text) {
	// printBinarImage(ImageConverter.convert1Ato2A(width, height, image), text, PrintOption.IMAGEJ);
	// }
	//
	// public static void printBinarImage(int[] image, int width, int height, String text, PrintOption typ) {
	// printBinarImage(ImageConverter.convert1Ato2A(width, height, image), text, typ);
	// }
	
	public static void printImage(int[] image, int width, int height, PrintOption typ) {
		printImage(ImageConverter.convert1AtoBI(width, height, image), typ);
	}
	
	public static void printImage(int[] image, int width, int height) {
		printImage(ImageConverter.convert1AtoBI(width, height, image), PrintOption.IMAGEJ);
	}
	
	public static void printImage(int[] image, int width, int height, String text, PrintOption typ) {
		printImage(ImageConverter.convert1AtoBI(width, height, image), text, typ);
	}
	
	public static void printImage(int[] image, int width, int height, String text) {
		printImage(ImageConverter.convert1AtoBI(width, height, image), text, PrintOption.IMAGEJ);
	}
	
	// ######## 2Array ##########
	
	// public static void printBinarImage(int[][] image) {
	// printBinaer(image, "Image", PrintOption.IMAGEJ);
	// }
	//
	// public static void printBinarImage(int[][] image, PrintOption typ) {
	// printBinaer(image, "Image", typ);
	// }
	//
	// public static void printBinarImage(int[][] image, String text, PrintOption typ) {
	// printBinaer(image, text, typ);
	// }
	
	public static void printImage(int[][] image, PrintOption typ) {
		printImage(ImageConverter.convert2AtoBI(image), typ);
	}
	
	public static void printImage(int[][] image) {
		printImage(ImageConverter.convert2AtoBI(image), PrintOption.IMAGEJ);
	}
	
	public static void printImage(int[][] image, String text, PrintOption typ) {
		printImage(ImageConverter.convert2AtoBI(image), text, typ);
	}
	
	public static void printImage(int[][] image, String text) {
		printImage(ImageConverter.convert2AtoBI(image), text, PrintOption.IMAGEJ);
	}
	
	// ############# FlexibleImage ############
	
	public static void printImage(FlexibleImage fluo) {
		printImage(fluo.getBufferedImage());
	}
	
	public static void printImage(FlexibleImage fluo, String text) {
		printImage(fluo.getBufferedImage(), text);
	}
	
	public static void printImage(FlexibleImage fluo, PrintOption typ) {
		printImage(fluo.getBufferedImage(), typ);
	}
	
	public static void printImage(FlexibleImage fluo, String text, PrintOption typ) {
		printImage(fluo.getBufferedImage(), text, typ);
	}
	
	// ######### BufferedImage #########
	
	public static void printImage(BufferedImage image, PrintOption typ) {
		printImage(image, "Image", typ);
	}
	
	public static void printImage(BufferedImage image) {
		printImage(image, "Image", PrintOption.IMAGEJ);
	}
	
	public static void printImage(BufferedImage image, String text) {
		printImage(image, text, PrintOption.IMAGEJ);
	}
	
	public static void printImage(BufferedImage image, String text, PrintOption typ) {
		
		switch (typ) {
			
			case GRAVISTO_SERVICE:
				printGravistoService(image, text);
				break;
			
			case IMAGEJ:
				printImagej(image, text);
				break;
			
			case CONSOLE:
				printImageConsole(ImageConverter.convertBIto2A(image), text);
				break;
			
			default:
				printImagej(image, text);
				break;
			
		}
	}
	
	private static void printImageConsole(int[][] image, String text) {
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
	
	// private static void printBinaer(int[][] image, String text, PrintOption typ) {
	// for (int i = 0; i < image.length; i++) {
	// for (int j = 0; j < image[i].length; j++)
	// if (image[i][j] == 0)
	// image[i][j] = Color.WHITE.getRGB();
	// else
	// image[i][j] = Color.BLACK.getRGB();
	// }
	// printImage(ImageConverter.convert2ABtoBI(image), text, typ);
	//
	// }
}

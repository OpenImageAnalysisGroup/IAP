/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

/**
 * @author entzian
 *
 */
public class MaskOperation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private int[] rgbImage;
	private int[] flourImage;
	private int[] background;
	private int[] mask;
	private int[] nearIfImage;
	
	public MaskOperation(int [] rgbImage, int [] flourImage, int [] nearIfImage, int [] background){
		this.rgbImage = rgbImage;
		this.flourImage = flourImage;
		this.background = background;
		this.nearIfImage = nearIfImage;
		int [] mask = new int[background.length];
	}
	
	public MaskOperation(int [] rgbImage, int [] flourImage, int [] background){
		this(rgbImage, flourImage, new int [0], background);
	}

	public MaskOperation(int[][] rgbImage, int [][] flourImage, int [][] background){
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(flourImage), new int [0], ImageConverter.convert2Ato1A(background));
	}
	
	public MaskOperation(int [] rgbImage, int [][] flourImage, int [][] background){
		this(rgbImage, ImageConverter.convert2Ato1A(flourImage), new int [0], ImageConverter.convert2Ato1A(background));
	}
	
	public MaskOperation(int[][] rgbImage, int [] flourImage, int [][] background){
		this(ImageConverter.convert2Ato1A(rgbImage), flourImage, new int [0], ImageConverter.convert2Ato1A(background));
	}
	
	public MaskOperation(int[][] rgbImage, int [][] flourImage, int [] background){
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(flourImage), new int [0], background);
	}
	
	public MaskOperation(int[] rgbImage, int [] flourImage, int [][] background){
		this(rgbImage, flourImage, new int [0], ImageConverter.convert2Ato1A(background));
	}
	
	public MaskOperation(int[] rgbImage, int [][] flourImage, int [] background){
		this(rgbImage, ImageConverter.convert2Ato1A(flourImage), new int [0], background);
	}
	
	public MaskOperation(int[][] rgbImage, int [] flourImage, int [] background){
		this(ImageConverter.convert2Ato1A(rgbImage), flourImage, new int [0], background);
	}
	
	public MaskOperation(int[][] rgbImage, int [][] flourImage, int [][] nearIfImage, int [][] background){
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(flourImage), ImageConverter.convert2Ato1A(nearIfImage), ImageConverter.convert2Ato1A(background));
	}
	
	public MaskOperation(int [] rgbImage, int [][] flourImage, int [][] nearIfImage, int [][] background){
		this(rgbImage, ImageConverter.convert2Ato1A(flourImage), ImageConverter.convert2Ato1A(nearIfImage), ImageConverter.convert2Ato1A(background));
	}
	
	public MaskOperation(int[][] rgbImage, int [] flourImage, int [][] nearIfImage, int [][] background){
		this(ImageConverter.convert2Ato1A(rgbImage), flourImage, ImageConverter.convert2Ato1A(nearIfImage), ImageConverter.convert2Ato1A(background));
	}
	
	public MaskOperation(int[][] rgbImage, int [][] flourImage, int [] nearIfImage, int [][] background){
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(flourImage), nearIfImage, ImageConverter.convert2Ato1A(background));
	}
	
	public MaskOperation(int[][] rgbImage, int [][] flourImage, int [][] nearIfImage, int [] background){
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(flourImage), ImageConverter.convert2Ato1A(nearIfImage), background);
	}
	
//	public MaskOperation(int[] rgbImage, int [] flourImage, int [][] nearIfImage, int [][] background){
//		this(rgbImage, flourImage, new int [0], ImageConverter.convert2Ato1A(background));
//	}
//	
//	public MaskOperation(int[] rgbImage, int [][] flourImage, int [][] nearIfImage, int [] background){
//		this(rgbImage, ImageConverter.convert2Ato1A(flourImage), new int [0], background);
//	}
//	
//	public MaskOperation(int[][] rgbImage, int [] flourImage, int [][] nearIfImage, int [] background){
//		this(ImageConverter.convert2Ato1A(rgbImage), flourImage, new int [0], background);
//	}
//	
	
	
	public int [] getMaskAs1Array(){
		return mask;
	}

	public int [][] getMaskAs2Array(int w, int h){
		return ImageConverter.convert1Ato2A(w, h, mask);
	}
	
	public void doMerge(){
		
		if(flourImage.length == background.length && rgbImage.length == background.length)
			for(int i = 0; i < background.length; i++)
				if(rgbImage[i] != background[i] && flourImage[i] != background[i])
					mask[i] = 1;
				else
					mask[i] = 0;
	}
	
	

}

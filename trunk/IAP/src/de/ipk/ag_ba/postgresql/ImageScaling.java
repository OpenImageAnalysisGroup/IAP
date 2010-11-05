/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk.ag_ba.postgresql;

/**
 * @author entzian
 *
 */
public class ImageScaling {

	private final int[][] src_image;
	private int[][] image_result;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public ImageScaling(int [][] src_image){
		this.src_image = src_image;
				
	}
	
	public void doZoom(double factor, Scaling typ){
		zoom(factor, typ);
	}
	

//	public int [][] doZoomIn(int [][] src_image, double factor){
//		this.src_image = src_image;
//		
//		return image_result;
//	}
	
	
	//##################  Private  ##################
	
	private void zoom(double factor, Scaling typ){
		
		
		//!!!!!!!!!!!! Problem bei falschem Runden durch casten von double nach int!!!!!!!!!!
		
		int newWidth = (int) (src_image.length * factor);
		int newHeigth = (int) (src_image[0].length * factor);

		image_result = new int[newWidth][newHeigth];
		
		int original_i;  //entspricht y
		int original_j;	//entspricht x
		
		for (int i = 0; i < newWidth; i++) {
			for (int j = 0; j < newHeigth; j++) {
				original_i = (int)(i / factor);
				original_j = (int)(j / factor);
				image_result[i][j] = getNewValue(original_i, original_j, typ);
			}
		}
	}

	private int getNewValue(int original_i, int original_j, Scaling typ){
		
		switch(typ){
		
		case NEAREST_NEIGHBOUR: break;
		case BILINEAR: break;
		case HERMITE: break;
		case GAUSSIAN: break;
		case BELL: break;
		case BSPLINE: break;
		case MITCHELL: break;
		case LANCZOS: break;
		
		default:
				//BILINEAR;
		}
		
		
		return;
	}
	
}

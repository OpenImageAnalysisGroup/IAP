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
public class MorphologicalOperators {

	private final int[][] src_image;

	private final int foreground = 1;
	private final int background = 0;

	private int[][] image_result;
	private int[][] mask;
	
	/**
	 * Position "I" corresponds to Y
	 * Position "J" corresponds to X
	 */
	private int positionMaskJ;
	private int positionMaskI;
	
	public static void main(String[] args) {
		
		int[][] eingabe_image1 = { { 1, 0, 0, 0 },
								   { 1, 0, 0, 0 },
								   { 0, 1, 1, 0 },
								   { 0, 1, 0, 0 },
								   { 0, 1, 0, 0 },
								   { 0, 1, 0, 0 } };
		int[][] mask1 = { { 1, 1} };
		
		MorphologicalOperators testMethod = new MorphologicalOperators(eingabe_image1, mask1, 0, 0);
		testMethod.doDilatation();
		testMethod.printImage();
		

		int[][] eingabe_image2 = { { 0, 1, 0 },
								   { 0, 1, 0 },
								   { 0, 1, 0 },
								   { 0, 0, 0 } };
		int[][] mask2 = { { 1, 0, 1 } };
		
		MorphologicalOperators testMethod2 = new MorphologicalOperators(eingabe_image2, mask2, 1, 0);
		testMethod2.doDilatation();
		testMethod2.printImage();
		
		int[][] eingabe_image3 = { { 0, 0, 0, 0, 1, 0 },
								   { 0, 1, 1, 1, 1, 0 },
								   { 0, 1, 0, 0, 1, 0 },
								   { 1, 1, 1, 1, 1, 0 },
								   { 0, 1, 0, 1, 1, 1 },
								   { 0, 1, 1, 1, 1, 0 } };
		int[][] mask3 = { { 1, 1, 1 },
						  { 1, 0, 0 },
						  { 1, 1, 1 }};
		
		MorphologicalOperators testMethod3 = new MorphologicalOperators(eingabe_image3, mask3, 1, 1);
		testMethod3.doErosion();
		testMethod3.printImage();
		
		
		int[][] eingabe_image4 = { { 0, 1, 0, 0 },
								   { 1, 1, 1, 1 },
								   { 0, 1, 0, 0 },
								   { 0, 1, 0, 0 },
								   { 0, 1, 0, 0 } };
		int[][] mask4 = { { 1, 1 } };
		
		MorphologicalOperators testMethod4 = new MorphologicalOperators(eingabe_image4, mask4, 0, 0);
		testMethod4.doErosion();
		testMethod4.printImage();
		
		
		int[][] eingabe_image5 = { { 0, 0, 0, 0, 0, 0 },
								   { 0, 1, 0, 1, 1, 0 },
								   { 0, 1, 1, 1, 1, 0 },
								   { 0, 0, 0, 1, 0, 0 },
								   { 0, 1, 1, 1, 1, 0 },
								   { 0, 0, 0, 0, 0, 0 } };
		int[][] mask5 = { { 1, 1, 1 },
						  { 1, 1, 1 },
						  { 1, 1, 1 }};
		
		MorphologicalOperators testMethod5 = new MorphologicalOperators(eingabe_image5, mask5, 1, 1);
		testMethod5.doClosing();
		System.out.println("Closing Ergebnis:");
		testMethod5.printImage();
		
		int[][] eingabe_image6 = { { 0, 1, 1, 0, 0, 0 },
								   { 0, 0, 0, 0, 0, 0 },
								   { 1, 1, 1, 1, 1, 1 },
								   { 1, 1, 1, 1, 0, 0 },
								   { 1, 1, 1, 1, 0, 1 },
								   { 1, 1, 1, 1, 0, 0 } };
		int[][] mask6 = { { 1, 1, 1 },
						  { 1, 1, 1 },
						  { 1, 1, 1 }};
		
		MorphologicalOperators testMethod6 = new MorphologicalOperators(eingabe_image6, mask6, 1, 1);
		testMethod6.doOpening();
		System.out.println("Opening Ergebnis:");
		testMethod6.printImage();
		

	}

	public MorphologicalOperators(int [][] src_image){
		this(src_image, new int [][] { { 1, 1, 1 },{ 1, 1, 1 },{ 1, 1, 1 }}, 1, 1);
	}
	
	
	public MorphologicalOperators(int [][] src_image, int [][] mask, int positionMaskJ, int positionMaskI){
		
		this.src_image = src_image;
		this.image_result = new int[src_image.length][src_image[0].length];
		this.positionMaskJ = positionMaskJ;
		this.positionMaskI = positionMaskI;
		this.mask = mask;
		
	}
	
	public boolean changeMask(int [][] newMask, int posJ, int posI){
		this.mask = newMask;
		this.positionMaskJ = posJ;
		this.positionMaskI = posI;
		return true;
	}
	

	public void doDilatation(){
		dilatation();
	}

	public void doErosion(){
		erosion();
	}
	
	//beides mit der selben Maske durchführen
	public void doOpening(){
		erosion();
		int [][] newSrcImage = cloneArray();
		dilatation(newSrcImage);
	}
	
	public void doOpening(int [][] changeMask, int posJ, int posI){
		erosion();
		changeMask(changeMask, posJ, posI);
		int [][] newSrcImage = cloneArray();
		dilatation(newSrcImage);
	}
	
	//beides mit der selben Maske durchführen
	public void doClosing(){
		dilatation();	
		int [][] newSrcImage = cloneArray();
		erosion(newSrcImage);
	}
	
	public void doClosing(int [][] changeMask, int posJ, int posI){
		dilatation();
		changeMask(changeMask, posJ, posI);
		int [][] newSrcImage = cloneArray();
		erosion(newSrcImage);
	}	
	
	
	public int[][] getResultImage(){
		return this.image_result;
	}
	
	//##################### Print ##############
	
	public void printImage() {
		printImage(this.image_result);
	}

	public void printImage(int[][] image) {
		printImage(image, "ResultImage");
	}

	public void printImage(int[][] image, String text) {
		System.out.println(text);
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++)
				System.out.print(image[i][j] + "\t");
			System.out.println("");
		}
	}
	
	
	//###################  Private ######################
	
	private int [][] cloneArray(){
		return(cloneArray(this.image_result));
	}
	
	private int [][] cloneArray(int [][] existingArray){
		int [][] newArray = new int [existingArray.length][existingArray[0].length];
		
		for(int i = 0; i < newArray.length; i++)
			System.arraycopy(existingArray[i], 0, newArray[i], 0, existingArray[i].length);
		
		return newArray;
	}
	
	private void dilatation(){
		dilatation(this.src_image);
	}
	
	private void dilatation(int [][] src_image){
		for (int i = 0; i < src_image.length; i++) {
			for (int j = 0; j < src_image[i].length; j++) {
				if(src_image[i][j] == foreground){
					insertMask(i, j); 
				} 
			}
		}		
	}
	
	private void insertMask(int currentPositionI, int currentPositionJ) {
		for(int l = 0; l < mask.length; l++){
			for(int k = 0; k < mask[l].length; k++){
				if(currentPositionI-positionMaskI+l >= 0 && currentPositionJ-positionMaskJ+k >= 0 &&
						currentPositionI-positionMaskI+l <= src_image.length-1 && currentPositionJ-positionMaskJ+k <= src_image[currentPositionI].length-1)
					image_result[currentPositionI-positionMaskI+l][currentPositionJ-positionMaskJ+k] = mask[l][k];
			}	
		}
	}
	
	private void erosion(){
		erosion(this.src_image);
	}
	
	
	private void erosion(int [][] src_image){
		for (int i = 0; i < src_image.length; i++) {
			for (int j = 0; j < src_image[i].length; j++) {
				if(i-positionMaskI >= 0 && j-positionMaskJ >= 0 &&
						i+(mask.length-positionMaskI-1) <= src_image.length-1 && j+(mask[0].length-positionMaskJ-1) <= src_image[i].length-1){
					mergeMask(i, j, src_image); 
				} else {
					image_result[i][j] = background;
				}
			}
		}		
	}
	
	private void mergeMask(int currentPositionI, int currentPositionJ, int [][] src_image){
		
		boolean agrees = true;
		
		for(int l = 0; l < mask.length; l++){
			for(int k = 0; k < mask[l].length; k++){
				if(mask[l][k] == 1 && src_image[currentPositionI-positionMaskI+l][currentPositionJ-positionMaskJ+k] != 1){
					agrees = false;
					break;
				}		
			}	
		}
		
		if(agrees) 
			image_result[currentPositionI][currentPositionJ] = foreground;
		else
			image_result[currentPositionI][currentPositionJ] = background;
	}
		
}
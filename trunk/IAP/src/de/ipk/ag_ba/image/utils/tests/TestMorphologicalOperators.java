/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.utils.tests;

import org.junit.Assert;
import org.junit.Test;

import de.ipk.ag_ba.image.operations.MorphologicalOperators;

/**
 * @author entzian
 */
public class TestMorphologicalOperators {
	
	@Test
	public void test() {
		
		// Test of Dilatation
		
		int[][] eingabe_image1 = { { 1, 0, 0, 0 },
									{ 1, 0, 0, 0 },
									{ 0, 1, 1, 0 },
									{ 0, 1, 0, 0 },
									{ 0, 1, 0, 0 },
									{ 0, 1, 0, 0 } };
		
		int[][] mask1 = { { 1, 1 } };
		
		MorphologicalOperators testDilatation1 = new MorphologicalOperators(eingabe_image1, mask1, 0, 0);
		testDilatation1.doDilatation();
		int[][] result_eingabe_image1 = testDilatation1.getResultImage();
		
		Assert.assertEquals(result_eingabe_image1[0][0], 1);
		Assert.assertEquals(result_eingabe_image1[0][1], 1);
		Assert.assertEquals(result_eingabe_image1[0][2], 0);
		Assert.assertEquals(result_eingabe_image1[0][3], 0);
		
		Assert.assertEquals(result_eingabe_image1[1][0], 1);
		Assert.assertEquals(result_eingabe_image1[1][1], 1);
		Assert.assertEquals(result_eingabe_image1[1][2], 0);
		Assert.assertEquals(result_eingabe_image1[1][3], 0);
		
		Assert.assertEquals(result_eingabe_image1[2][0], 0);
		Assert.assertEquals(result_eingabe_image1[2][1], 1);
		Assert.assertEquals(result_eingabe_image1[2][2], 1);
		Assert.assertEquals(result_eingabe_image1[2][3], 1);
		
		Assert.assertEquals(result_eingabe_image1[3][0], 0);
		Assert.assertEquals(result_eingabe_image1[3][1], 1);
		Assert.assertEquals(result_eingabe_image1[3][2], 1);
		Assert.assertEquals(result_eingabe_image1[3][3], 0);
		
		Assert.assertEquals(result_eingabe_image1[4][0], 0);
		Assert.assertEquals(result_eingabe_image1[4][1], 1);
		Assert.assertEquals(result_eingabe_image1[4][2], 1);
		Assert.assertEquals(result_eingabe_image1[4][3], 0);
		
		int[][] eingabe_image2 = { { 0, 1, 0 },
									{ 0, 1, 0 },
									{ 0, 1, 0 },
									{ 0, 0, 0 } };
		
		int[][] mask2 = { { 1, 0, 1 } };
		
		MorphologicalOperators testDilatation2 = new MorphologicalOperators(eingabe_image2, mask2, 1, 0);
		testDilatation2.doDilatation();
		int[][] result_eingabe_image2 = testDilatation2.getResultImage();
		
		Assert.assertEquals(result_eingabe_image2[0][0], 1);
		Assert.assertEquals(result_eingabe_image2[0][1], 0);
		Assert.assertEquals(result_eingabe_image2[0][2], 1);
		
		Assert.assertEquals(result_eingabe_image2[1][0], 1);
		Assert.assertEquals(result_eingabe_image2[1][1], 0);
		Assert.assertEquals(result_eingabe_image2[1][2], 1);
		
		Assert.assertEquals(result_eingabe_image2[2][0], 1);
		Assert.assertEquals(result_eingabe_image2[2][1], 0);
		Assert.assertEquals(result_eingabe_image2[2][2], 1);
		
		Assert.assertEquals(result_eingabe_image2[3][0], 0);
		Assert.assertEquals(result_eingabe_image2[3][1], 0);
		Assert.assertEquals(result_eingabe_image2[3][2], 0);
		
		// Test of Erosion
		
		int[][] eingabe_image3 = { { 0, 0, 0, 0, 1, 0 },
									{ 0, 1, 1, 1, 1, 0 },
									{ 0, 1, 0, 0, 1, 0 },
									{ 1, 1, 1, 1, 1, 0 },
									{ 0, 1, 0, 1, 1, 1 },
									{ 0, 1, 1, 1, 1, 0 } };
		int[][] mask3 = { { 1, 1, 1 },
							{ 1, 0, 0 },
							{ 1, 1, 1 } };
		
		MorphologicalOperators testErosion1 = new MorphologicalOperators(eingabe_image3, mask3, 1, 1);
		testErosion1.doErosion();
		int[][] result_eingabe_image3 = testErosion1.getResultImage();
		
		Assert.assertEquals(result_eingabe_image3[0][0], 0);
		Assert.assertEquals(result_eingabe_image3[0][1], 0);
		Assert.assertEquals(result_eingabe_image3[0][2], 0);
		Assert.assertEquals(result_eingabe_image3[0][3], 0);
		Assert.assertEquals(result_eingabe_image3[0][4], 0);
		Assert.assertEquals(result_eingabe_image3[0][5], 0);
		
		Assert.assertEquals(result_eingabe_image3[1][0], 0);
		Assert.assertEquals(result_eingabe_image3[1][1], 0);
		Assert.assertEquals(result_eingabe_image3[1][2], 0);
		Assert.assertEquals(result_eingabe_image3[1][3], 0);
		Assert.assertEquals(result_eingabe_image3[1][4], 0);
		Assert.assertEquals(result_eingabe_image3[1][5], 0);
		
		Assert.assertEquals(result_eingabe_image3[2][0], 0);
		Assert.assertEquals(result_eingabe_image3[2][1], 0);
		Assert.assertEquals(result_eingabe_image3[2][2], 1);
		Assert.assertEquals(result_eingabe_image3[2][3], 0);
		Assert.assertEquals(result_eingabe_image3[2][4], 0);
		Assert.assertEquals(result_eingabe_image3[2][5], 0);
		
		Assert.assertEquals(result_eingabe_image3[3][0], 0);
		Assert.assertEquals(result_eingabe_image3[3][1], 0);
		Assert.assertEquals(result_eingabe_image3[3][2], 0);
		Assert.assertEquals(result_eingabe_image3[3][3], 0);
		Assert.assertEquals(result_eingabe_image3[3][4], 0);
		Assert.assertEquals(result_eingabe_image3[3][5], 0);
		
		Assert.assertEquals(result_eingabe_image3[4][0], 0);
		Assert.assertEquals(result_eingabe_image3[4][1], 0);
		Assert.assertEquals(result_eingabe_image3[4][2], 1);
		Assert.assertEquals(result_eingabe_image3[4][3], 0);
		Assert.assertEquals(result_eingabe_image3[4][4], 0);
		Assert.assertEquals(result_eingabe_image3[4][5], 0);
		
		Assert.assertEquals(result_eingabe_image3[5][0], 0);
		Assert.assertEquals(result_eingabe_image3[5][1], 0);
		Assert.assertEquals(result_eingabe_image3[5][2], 0);
		Assert.assertEquals(result_eingabe_image3[5][3], 0);
		Assert.assertEquals(result_eingabe_image3[5][4], 0);
		Assert.assertEquals(result_eingabe_image3[5][5], 0);
		
		int[][] eingabe_image4 = { { 0, 1, 0, 0 },
									{ 1, 1, 1, 1 },
									{ 0, 1, 0, 0 },
									{ 0, 1, 0, 0 },
									{ 0, 1, 0, 0 } };
		int[][] mask4 = { { 1, 1 } };
		
		MorphologicalOperators testErosion2 = new MorphologicalOperators(eingabe_image4, mask4, 0, 0);
		testErosion2.doErosion();
		int[][] result_eingabe_image4 = testErosion2.getResultImage();
		
		Assert.assertEquals(result_eingabe_image4[0][0], 0);
		Assert.assertEquals(result_eingabe_image4[0][1], 0);
		Assert.assertEquals(result_eingabe_image4[0][2], 0);
		Assert.assertEquals(result_eingabe_image4[0][3], 0);
		
		Assert.assertEquals(result_eingabe_image4[1][0], 1);
		Assert.assertEquals(result_eingabe_image4[1][1], 1);
		Assert.assertEquals(result_eingabe_image4[1][2], 1);
		Assert.assertEquals(result_eingabe_image4[1][3], 0);
		
		Assert.assertEquals(result_eingabe_image4[2][0], 0);
		Assert.assertEquals(result_eingabe_image4[2][1], 0);
		Assert.assertEquals(result_eingabe_image4[2][2], 0);
		Assert.assertEquals(result_eingabe_image4[2][3], 0);
		
		Assert.assertEquals(result_eingabe_image4[3][0], 0);
		Assert.assertEquals(result_eingabe_image4[3][1], 0);
		Assert.assertEquals(result_eingabe_image4[3][2], 0);
		Assert.assertEquals(result_eingabe_image4[3][3], 0);
		
		Assert.assertEquals(result_eingabe_image4[4][0], 0);
		Assert.assertEquals(result_eingabe_image4[4][1], 0);
		Assert.assertEquals(result_eingabe_image4[4][2], 0);
		Assert.assertEquals(result_eingabe_image4[4][3], 0);
		
		// Test of Closing
		
		int[][] eingabe_image5 = { { 0, 0, 0, 0, 0, 0 },
									{ 0, 1, 0, 1, 1, 0 },
									{ 0, 1, 1, 1, 1, 0 },
									{ 0, 0, 0, 1, 0, 0 },
									{ 0, 1, 1, 1, 1, 0 },
									{ 0, 0, 0, 0, 0, 0 } };
		int[][] mask5 = { { 1, 1, 1 },
							{ 1, 1, 1 },
							{ 1, 1, 1 } };
		
		MorphologicalOperators testClosing1 = new MorphologicalOperators(eingabe_image5, mask5, 1, 1);
		testClosing1.doClosing();
		int[][] result_eingabe_image5 = testClosing1.getResultImage();
		
		Assert.assertEquals(result_eingabe_image5[0][0], 0);
		Assert.assertEquals(result_eingabe_image5[0][1], 0);
		Assert.assertEquals(result_eingabe_image5[0][2], 0);
		Assert.assertEquals(result_eingabe_image5[0][3], 0);
		Assert.assertEquals(result_eingabe_image5[0][4], 0);
		Assert.assertEquals(result_eingabe_image5[0][5], 0);
		
		Assert.assertEquals(result_eingabe_image5[1][0], 0);
		Assert.assertEquals(result_eingabe_image5[1][1], 1);
		Assert.assertEquals(result_eingabe_image5[1][2], 1);
		Assert.assertEquals(result_eingabe_image5[1][3], 1);
		Assert.assertEquals(result_eingabe_image5[1][4], 1);
		Assert.assertEquals(result_eingabe_image5[1][5], 0);
		
		Assert.assertEquals(result_eingabe_image5[2][0], 0);
		Assert.assertEquals(result_eingabe_image5[2][1], 1);
		Assert.assertEquals(result_eingabe_image5[2][2], 1);
		Assert.assertEquals(result_eingabe_image5[2][3], 1);
		Assert.assertEquals(result_eingabe_image5[2][4], 1);
		Assert.assertEquals(result_eingabe_image5[2][5], 0);
		
		Assert.assertEquals(result_eingabe_image5[3][0], 0);
		Assert.assertEquals(result_eingabe_image5[3][1], 1);
		Assert.assertEquals(result_eingabe_image5[3][2], 1);
		Assert.assertEquals(result_eingabe_image5[3][3], 1);
		Assert.assertEquals(result_eingabe_image5[3][4], 1);
		Assert.assertEquals(result_eingabe_image5[3][5], 0);
		
		Assert.assertEquals(result_eingabe_image5[4][0], 0);
		Assert.assertEquals(result_eingabe_image5[4][1], 1);
		Assert.assertEquals(result_eingabe_image5[4][2], 1);
		Assert.assertEquals(result_eingabe_image5[4][3], 1);
		Assert.assertEquals(result_eingabe_image5[4][4], 1);
		Assert.assertEquals(result_eingabe_image5[4][5], 0);
		
		Assert.assertEquals(result_eingabe_image5[5][0], 0);
		Assert.assertEquals(result_eingabe_image5[5][1], 0);
		Assert.assertEquals(result_eingabe_image5[5][2], 0);
		Assert.assertEquals(result_eingabe_image5[5][3], 0);
		Assert.assertEquals(result_eingabe_image5[5][4], 0);
		Assert.assertEquals(result_eingabe_image5[5][5], 0);
		
		// Test of Opening
		
		int[][] eingabe_image6 = { { 0, 1, 1, 0, 0, 0 },
									{ 0, 0, 0, 0, 0, 0 },
									{ 1, 1, 1, 1, 1, 1 },
									{ 1, 1, 1, 1, 0, 0 },
									{ 1, 1, 1, 1, 0, 1 },
									{ 1, 1, 1, 1, 0, 0 } };
		int[][] mask6 = { { 1, 1, 1 },
							{ 1, 1, 1 },
							{ 1, 1, 1 } };
		
		MorphologicalOperators testOpening1 = new MorphologicalOperators(eingabe_image6, mask6, 1, 1);
		testOpening1.doOpening();
		int[][] result_eingabe_image6 = testOpening1.getResultImage();
		
		Assert.assertEquals(result_eingabe_image6[0][0], 0);
		Assert.assertEquals(result_eingabe_image6[0][1], 0);
		Assert.assertEquals(result_eingabe_image6[0][2], 0);
		Assert.assertEquals(result_eingabe_image6[0][3], 0);
		Assert.assertEquals(result_eingabe_image6[0][4], 0);
		Assert.assertEquals(result_eingabe_image6[0][5], 0);
		
		Assert.assertEquals(result_eingabe_image6[1][0], 0);
		Assert.assertEquals(result_eingabe_image6[1][1], 0);
		Assert.assertEquals(result_eingabe_image6[1][2], 0);
		Assert.assertEquals(result_eingabe_image6[1][3], 0);
		Assert.assertEquals(result_eingabe_image6[1][4], 0);
		Assert.assertEquals(result_eingabe_image6[1][5], 0);
		
		Assert.assertEquals(result_eingabe_image6[2][0], 1);
		Assert.assertEquals(result_eingabe_image6[2][1], 1);
		Assert.assertEquals(result_eingabe_image6[2][2], 1);
		Assert.assertEquals(result_eingabe_image6[2][3], 1);
		Assert.assertEquals(result_eingabe_image6[2][4], 0);
		Assert.assertEquals(result_eingabe_image6[2][5], 0);
		
		Assert.assertEquals(result_eingabe_image6[3][0], 1);
		Assert.assertEquals(result_eingabe_image6[3][1], 1);
		Assert.assertEquals(result_eingabe_image6[3][2], 1);
		Assert.assertEquals(result_eingabe_image6[3][3], 1);
		Assert.assertEquals(result_eingabe_image6[3][4], 0);
		Assert.assertEquals(result_eingabe_image6[3][5], 0);
		
		Assert.assertEquals(result_eingabe_image6[4][0], 1);
		Assert.assertEquals(result_eingabe_image6[4][1], 1);
		Assert.assertEquals(result_eingabe_image6[4][2], 1);
		Assert.assertEquals(result_eingabe_image6[4][3], 1);
		Assert.assertEquals(result_eingabe_image6[4][4], 0);
		Assert.assertEquals(result_eingabe_image6[4][5], 0);
		
		Assert.assertEquals(result_eingabe_image6[5][0], 1);
		Assert.assertEquals(result_eingabe_image6[5][1], 1);
		Assert.assertEquals(result_eingabe_image6[5][2], 1);
		Assert.assertEquals(result_eingabe_image6[5][3], 1);
		Assert.assertEquals(result_eingabe_image6[5][4], 0);
		Assert.assertEquals(result_eingabe_image6[5][5], 0);
		
		// public static void main(String[] args) {
		//
		// int[][] eingabe_image1 = { { 1, 0, 0, 0 }, { 1, 0, 0, 0 }, { 0, 1, 1, 0 }, { 0, 1, 0, 0 }, { 0, 1, 0, 0 },
		// { 0, 1, 0, 0 } };
		// int[][] mask1 = { { 1, 1 } };
		//
		// MorphologicalOperators testMethod = new MorphologicalOperators(eingabe_image1, mask1, 0, 0);
		// testMethod.doDilatation();
		// testMethod.printImage();
		//
		// int[][] eingabe_image2 = { { 0, 1, 0 }, { 0, 1, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };
		// int[][] mask2 = { { 1, 0, 1 } };
		//
		// MorphologicalOperators testMethod2 = new MorphologicalOperators(eingabe_image2, mask2, 1, 0);
		// testMethod2.doDilatation();
		// testMethod2.printImage();
		//
		// int[][] eingabe_image3 = { { 0, 0, 0, 0, 1, 0 }, { 0, 1, 1, 1, 1, 0 }, { 0, 1, 0, 0, 1, 0 },
		// { 1, 1, 1, 1, 1, 0 }, { 0, 1, 0, 1, 1, 1 }, { 0, 1, 1, 1, 1, 0 } };
		// int[][] mask3 = { { 1, 1, 1 }, { 1, 0, 0 }, { 1, 1, 1 } };
		//
		// MorphologicalOperators testMethod3 = new MorphologicalOperators(eingabe_image3, mask3, 1, 1);
		// testMethod3.doErosion();
		// testMethod3.printImage();
		//
		// int[][] eingabe_image4 = { { 0, 1, 0, 0 }, { 1, 1, 1, 1 }, { 0, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 1, 0, 0 } };
		// int[][] mask4 = { { 1, 1 } };
		//
		// MorphologicalOperators testMethod4 = new MorphologicalOperators(eingabe_image4, mask4, 0, 0);
		// testMethod4.doErosion();
		// testMethod4.printImage();
		//
		// int[][] eingabe_image5 = { { 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 1, 1, 0 }, { 0, 1, 1, 1, 1, 0 },
		// { 0, 0, 0, 1, 0, 0 }, { 0, 1, 1, 1, 1, 0 }, { 0, 0, 0, 0, 0, 0 } };
		// int[][] mask5 = { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } };
		//
		// MorphologicalOperators testMethod5 = new MorphologicalOperators(eingabe_image5, mask5, 1, 1);
		// testMethod5.doClosing();
		// System.out.println("Closing Ergebnis:");
		// testMethod5.printImage();
		//
		// int[][] eingabe_image6 = { { 0, 1, 1, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 1, 1, 1 },
		// { 1, 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 0, 1 }, { 1, 1, 1, 1, 0, 0 } };
		// int[][] mask6 = { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } };
		//
		// MorphologicalOperators testMethod6 = new MorphologicalOperators(eingabe_image6, mask6, 1, 1);
		// testMethod6.doOpening();
		// System.out.println("Opening Ergebnis:");
		// testMethod6.printImage();
		//
		// }
		
	}
	
}

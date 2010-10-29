package de.ipk.ag_ba.postgresql;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class TestSegmentation {

	@Test
	public void test() {
	
		int [][] eingabe_image1 = { { 0, 1 },
								   { 0, 0 } }	;
		
		int [][] eingabe_image2 = { { 0, 1, 1 },
								   { 1, 0, 0 } };
		
		int [][] eingabe_image3 = { { 0, 0, 0 },
								   { 0, 0, 0 } };
		
		int [][] eingabe_image4 = { { 1, 1, 1 },
								   { 1, 1, 1 } };
		
		int [][] eingabe_image5 = { { 0, 1, 1, 0, 1, 0, 1, 0 },
								   { 1, 1, 0, 0, 1, 1, 1, 0 },
								   { 0, 1, 1, 1, 1, 0, 1, 0 },
								   { 0, 0, 0, 0, 0, 1, 1, 0 },
								   { 0, 1, 1, 1, 0, 0, 0, 1 },
								   { 1, 1, 1, 1, 1, 0, 0, 0 } };
		
		ArrayList<int [][]> testImage = new ArrayList<int [][]>();
		testImage.add(eingabe_image1);
		testImage.add(eingabe_image2);
		testImage.add(eingabe_image3);
		testImage.add(eingabe_image4);
		testImage.add(eingabe_image5);
		
		//4er ClusterCount
		int [] testCluster4er = new int [5];
		testCluster4er[0] = 1;
		testCluster4er[1] = 2;
		testCluster4er[2] = 0;
		testCluster4er[3] = 1;
		testCluster4er[4] = 3;
		
		//8er ClusterCount
		int [] testCluster8er = new int [5];
		testCluster8er[0] = 1;
		testCluster8er[1] = 1;
		testCluster8er[2] = 0;
		testCluster8er[3] = 1;
		testCluster8er[4] = 2;
		
		//4/8er PixelCount
		int [] testPixel = new int [5];
		testPixel[0] = 1;
		testPixel[1] = 3;
		testPixel[2] = 0;
		testPixel[3] = 6;
		testPixel[4] = 25;
			
		
		pixel_segmentierung test;
		int [][] image;
		
		for (int i = 0; i < testImage.size(); i++){
			image = testImage.get(i);
			
			test = new pixel_segmentierung(image, false);
			test.doPixelSegmentation();
			Assert.assertEquals(test.getNumberOfCluster(), testCluster4er[i]);
			Assert.assertEquals(test.getNumberOfPixel(), testPixel[i]);
			
			test = new pixel_segmentierung(image, true);
			test.doPixelSegmentation();
			Assert.assertEquals(test.getNumberOfCluster(), testCluster8er[i]);
			Assert.assertEquals(test.getNumberOfPixel(), testPixel[i]);
		}
	}
}

package de.ipk.ag_ba.image_utils;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class TestSegmentation {

	@Test
	public void test() {

		int[][] eingabe_image1 = { { 0, 1 }, { 0, 0 } };

		int[][] eingabe_image2 = { { 0, 1, 1 }, { 1, 0, 0 } };

		int[][] eingabe_image3 = { { 0, 0, 0 }, { 0, 0, 0 } };

		int[][] eingabe_image4 = { { 1, 1, 1 }, { 1, 1, 1 } };

		int[][] eingabe_image5 = { { 0, 1, 1, 0, 1, 0, 1, 0 }, { 1, 1, 0, 0, 1, 1, 1, 0 }, { 0, 1, 1, 1, 1, 0, 1, 0 },
							{ 0, 0, 0, 0, 0, 1, 1, 0 }, { 0, 1, 1, 1, 0, 0, 0, 1 }, { 1, 1, 1, 1, 1, 0, 0, 0 } };

		ArrayList<int[][]> testImage = new ArrayList<int[][]>();
		testImage.add(eingabe_image1);
		testImage.add(eingabe_image2);
		testImage.add(eingabe_image3);
		testImage.add(eingabe_image4);
		testImage.add(eingabe_image5);

		// 4er ClusterCount
		int[] testCluster4er = new int[5];
		testCluster4er[0] = 1;
		testCluster4er[1] = 2;
		testCluster4er[2] = 0;
		testCluster4er[3] = 1;
		testCluster4er[4] = 3;

		// 8er ClusterCount
		int[] testCluster8er = new int[5];
		testCluster8er[0] = 1;
		testCluster8er[1] = 1;
		testCluster8er[2] = 0;
		testCluster8er[3] = 1;
		testCluster8er[4] = 2;

		// 4/8er PixelCount
		int[] testPixel = new int[5];
		testPixel[0] = 1;
		testPixel[1] = 3;
		testPixel[2] = 0;
		testPixel[3] = 6;
		testPixel[4] = 25;

		double[] testCircuitRatio = new double[5];
		testCircuitRatio[0] = 0.7853981633974483;
		testCircuitRatio[1] = 0.6981317007977318;
		testCircuitRatio[2] = 0.0;
		testCircuitRatio[3] = 0.7539822368615503;
		testCircuitRatio[4] = 0.17392900504303355;

		int[] position = new int[5];
		position[0] = 1;
		position[1] = 1;
		position[2] = 0;
		position[3] = 1;
		position[4] = 4;

		PixelSegmentation test;
		int[][] image;

		for (int i = 0; i < testImage.size(); i++) {
			image = testImage.get(i);

			test = new PixelSegmentation(image, NeighbourhoodSetting.NB4);
			test.doPixelSegmentation();
			Assert.assertEquals(test.getNumberOfCluster(), testCluster4er[i]);
			Assert.assertEquals(test.getNumberOfPixel(), testPixel[i]);
			Assert.assertEquals(test.getCircuitRatio(position[i]), testCircuitRatio[i], 15);

			test = new PixelSegmentation(image, NeighbourhoodSetting.NB8);
			test.doPixelSegmentation();
			Assert.assertEquals(test.getNumberOfCluster(), testCluster8er[i]);
			Assert.assertEquals(test.getNumberOfPixel(), testPixel[i]);

			// public static void main(String[] args) {
			// PixelSegmentation test = new PixelSegmentation(eingabe_image,
			// NeighbourhoodSetting.NB4);
			// test.doPixelSegmentation();
			// test.printOriginalImage();
			// System.out.println("ClusterIds:");
			// test.printImage();
			// test.printClusterArray();
			// System.out.println("Number of Clusters: " +
			// test.getNumberOfCluster());
			// System.out.println("Number of Pixel: " + test.getNumberOfPixel());
			// System.out.println("Area:");
			// test.printArray(test.getArea());
			// System.out.println("Perimeter: ");
			// test.printArray(test.getPerimeter());
			// System.out.println("Ratio: ");
			// test.printArray(test.getCircuitRatio());
			// }
		}
	}
}

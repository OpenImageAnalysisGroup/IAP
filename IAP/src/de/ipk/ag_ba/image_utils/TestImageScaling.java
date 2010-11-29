/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author entzian
 */
public class TestImageScaling {
	@Test
	public void test() {

		// Test of Dilatation

		int[][] eingabe_image1 = { { 1, 0, 0, 0 },
									{ 1, 0, 0, 0 },
									{ 0, 1, 1, 0 },
									{ 0, 1, 0, 0 },
									{ 0, 1, 0, 0 },
									{ 0, 1, 0, 0 } };

		int[][] test_result_eingabe_image1_zoom1 = { { 1, 1, 0, 0, 0, 0, 0, 0 },
												{ 1, 1, 0, 0, 0, 0, 0, 0 },
												{ 1, 1, 0, 0, 0, 0, 0, 0 },
												{ 1, 1, 0, 0, 0, 0, 0, 0 },
												{ 0, 0, 1, 1, 1, 1, 0, 0 },
												{ 0, 0, 1, 1, 1, 1, 0, 0 },
												{ 0, 0, 1, 1, 0, 0, 0, 0 },
												{ 0, 0, 1, 1, 0, 0, 0, 0 },
												{ 0, 0, 1, 1, 0, 0, 0, 0 },
												{ 0, 0, 1, 1, 0, 0, 0, 0 },
												{ 0, 0, 1, 1, 0, 0, 0, 0 },
												{ 0, 0, 1, 1, 0, 0, 0, 0 } };

		int[][] test_result_eingabe_image1_zoom2 = { { 0, 0 },
															{ 1, 0 },
														{ 1, 0 } };

		ImageScaling testDoZoom1 = new ImageScaling(eingabe_image1);
		testDoZoom1.doZoom(2.0, Scaling.NEAREST_NEIGHBOUR);
		int[][] result_eingabe_image1 = testDoZoom1.getResultImage();

		for (int i = 0; i < result_eingabe_image1.length; i++)
			for (int j = 0; j < result_eingabe_image1[i].length; j++)
				Assert.assertEquals(result_eingabe_image1[i][j], test_result_eingabe_image1_zoom1[i][j]);

		testDoZoom1.doZoom(0.5, Scaling.NEAREST_NEIGHBOUR);
		result_eingabe_image1 = testDoZoom1.getResultImage();

		for (int i = 0; i < result_eingabe_image1.length; i++)
			for (int j = 0; j < result_eingabe_image1[i].length; j++)
				Assert.assertEquals(result_eingabe_image1[i][j], test_result_eingabe_image1_zoom2[i][j]);

		int[][] eingabe_image2 = { { 0, 1, 0 },
									{ 0, 1, 0 },
									{ 0, 1, 0 },
									{ 0, 0, 0 } };

		int[][] test_result_eingabe_image2_zoom1 = { { 0, 0, 1, 1, 0, 0 },
														{ 0, 0, 1, 1, 0, 0 },
														{ 0, 0, 1, 1, 0, 0 },
														{ 0, 0, 1, 1, 0, 0 },
														{ 0, 0, 1, 1, 0, 0 },
														{ 0, 0, 1, 1, 0, 0 },
														{ 0, 0, 0, 0, 0, 0 },
														{ 0, 0, 0, 0, 0, 0 } };

		int[][] test_result_eingabe_image2_zoom2 = { { 1 },
														{ 0 } };

		ImageScaling testDoZoom2 = new ImageScaling(eingabe_image2);
		testDoZoom2.doZoom(2.0, Scaling.NEAREST_NEIGHBOUR);
		int[][] result_eingabe_image2 = testDoZoom2.getResultImage();

		for (int i = 0; i < result_eingabe_image2.length; i++)
			for (int j = 0; j < result_eingabe_image2[i].length; j++)
				Assert.assertEquals(result_eingabe_image2[i][j], test_result_eingabe_image2_zoom1[i][j]);

		testDoZoom2.doZoom(0.5, Scaling.NEAREST_NEIGHBOUR);
		result_eingabe_image2 = testDoZoom2.getResultImage();

		for (int i = 0; i < result_eingabe_image2.length; i++)
			for (int j = 0; j < result_eingabe_image2[i].length; j++)
				Assert.assertEquals(result_eingabe_image2[i][j], test_result_eingabe_image2_zoom2[i][j]);

	}
}

/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Sep 2, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedImage;

/**
 * @author klukas
 * 
 */
public class CutImagePreprocessor implements ImagePreProcessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.
	 * ImagePreProcessor#processImage(rmi_server.analysis.LoadedImage)
	 */
	@Override
	public boolean processImage(LoadedImage loadedImage, int[] rgbArray, int[] rgbArrayNULL, int w, int h,
			int iBackgroundFill) {

		ImageConfiguration ic = ImageConfiguration.get(loadedImage.getSubstanceName());

		if (ic == ImageConfiguration.RgbSide) {
			// for (int y = 1700; y < h; y++) {
			for (int y = 1530; y < h; y++) {
				int o = y * w;
				for (int x = 0; x < w; x++) {
					rgbArray[o + x] = iBackgroundFill;
				}
			}
		} else if (ic == ImageConfiguration.FluoSide) {
			for (int y = 1080; y < h; y++) {
				int o = y * w;
				for (int x = 0; x < w; x++) {
					rgbArray[o + x] = iBackgroundFill;
				}
			}
		} else if (ic == ImageConfiguration.FluoTop) {
			for (int y = 0; y < 50; y++) {
				int o = y * w;
				for (int x = 0; x < w; x++) {
					rgbArray[o + x] = iBackgroundFill;
				}
			}
			for (int y = h - 50; y < h; y++) {
				int o = y * w;
				for (int x = 0; x < w; x++) {
					rgbArray[o + x] = iBackgroundFill;
				}
			}
		} else {
			if (ic != ImageConfiguration.RgbTop)
				System.out.println(loadedImage.getSubstanceName() + " <> " + ImageConfiguration.FluoSide.toString());
		}
		return true;
	}

}

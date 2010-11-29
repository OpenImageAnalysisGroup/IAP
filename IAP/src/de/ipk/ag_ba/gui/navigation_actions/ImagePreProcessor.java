/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Sep 2, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author klukas
 */
public interface ImagePreProcessor {
	public boolean processImage(LoadedImage loadedImage, int[] rgbArray, int[] rgbArrayNULL, int w, int h,
						int iBackgroundFill);
}

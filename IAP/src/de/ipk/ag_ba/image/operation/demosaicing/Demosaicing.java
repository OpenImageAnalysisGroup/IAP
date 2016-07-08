package de.ipk.ag_ba.image.operation.demosaicing;

import de.ipk.ag_ba.image.structures.Image;

/**
 * @file dmbilinearcli.c
 * @brief Bilinear demosaicing command line program
 * @author Pascal Getreuer <getreuer@gmail.com>
 *         Copyright (c) 2010-2011, Pascal Getreuer
 *         All rights reserved.
 *         This program is free software: you can use, modify and/or
 *         redistribute it under the terms of the simplified BSD License. You
 *         should have received a copy of this license along this program. If
 *         not, see <http://www.opensource.org/licenses/bsd-license.html>.
 */
/**
 * Adapted from dmbilinearcli.c - (c) Pascal Getreuer <getreuer@gmail.com>
 * BSD License: https://opensource.org/licenses/bsd-license.php
 * This file is published under BSD and GPL license terms.
 * 
 * @author klukas
 */
public class Demosaicing {
	public static Image demosaic(Image floatInput, int Width, int Height, BayerPattern pattern)
	{
		if (Width < 2 || Height < 2)
			throw new RuntimeException("Image is too small (" + Width + "x" + Height + ").");
		
		float[] Output = new float[3 * Width * Height];
		
		int ParamRedX = pattern.ParamRedX();
		int ParamRedY = pattern.ParamRedY();
		
		float[] Input = floatInput.getAs1float(true, true);
		
		// BilinearDemosaicing.CfaFlatten(Input, Input, Width, Height, ParamRedX, ParamRedY);
		BilinearDemosaicing.BilinearDemosaic(Output, Input, Width, Height, ParamRedX, ParamRedY);
		
		return new Image(Width, Height, Output, FloatMode.AllRedThenAllGreenThenAllBlue);
	}
}

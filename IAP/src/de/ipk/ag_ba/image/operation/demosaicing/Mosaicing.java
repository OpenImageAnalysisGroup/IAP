package de.ipk.ag_ba.image.operation.demosaicing;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Source header info:
 * 
 * @file mosaic.c
 * @brief Tool for mosaicing images with the Bayer CFA
 * @author Pascal Getreuer <getreuer@gmail.com>
 *         Copyright (c) 2010-2011, Pascal Getreuer
 *         All rights reserved.
 *         This program is free software: you can use, modify and/or
 *         redistribute it under the terms of the simplified BSD License. You
 *         should have received a copy of this license along this program. If
 *         not, see <http://www.opensource.org/licenses/bsd-license.html>.
 */
/**
 * Adapted from mosaic.c - (c) Pascal Getreuer <getreuer@gmail.com>
 * BSD License: https://opensource.org/licenses/bsd-license.php
 * This file is published under BSD and GPL license terms.
 * 
 * @author klukas
 */
public class Mosaicing {
	public static Image mosaicImage(BayerPattern pattern, int[] uData, int uWidth, int uHeight) {
		int x, y, Green;
		
		int ParamRedX = pattern.ParamRedX();
		int ParamRedY = pattern.ParamRedY();
		
		Green = 1 - ((ParamRedX + ParamRedY) & 1);
		
		int fWidth = uWidth;
		int fHeight = uHeight;
		
		int[] fData = new int[fWidth * fHeight];
		
		for (y = 0; y < fHeight; y++)
			for (x = 0; x < fWidth; x++) {
				int c = uData[WSymExtension(uWidth, x) + uWidth * WSymExtension(uHeight, y)];
				
				int red = ((c & 0xff0000) >> 16);
				int gre = ((c & 0x00ff00) >> 8);
				int blu = (c & 0x0000ff);
				
				if (((x + y) & 1) == Green)
					blu = red = 0;
				else
					if ((y & 1) == ParamRedY)
						gre = red = 0;
					else
						blu = gre = 0;
				
				fData[x + fWidth * y] = (0xFF << 24 | red << 16) | (gre << 8) | (blu << 0);
			}
		
		return new Image(fWidth, fHeight, fData);
	}
	
	/**
	 * @brief Boundary handling function for whole-sample symmetric extension
	 * @param N
	 *           is the data length
	 * @param n
	 *           is an index into the data
	 * @return an index that is always between 0 and N - 1
	 */
	private static int WSymExtension(int N, int n) {
		while (true) {
			if (n < 0)
				n = -n;
			else
				if (n >= N)
					n = (2 * N - 2) - n;
				else
					return n;
		}
	}
}

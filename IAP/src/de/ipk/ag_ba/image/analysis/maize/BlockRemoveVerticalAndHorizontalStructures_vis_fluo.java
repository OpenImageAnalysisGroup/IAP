package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

// this might not be needed
/**
 * @author Christian Klukas
 * @deprecated
 */
@Deprecated
public class BlockRemoveVerticalAndHorizontalStructures_vis_fluo extends AbstractBlock {
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask == null)
			return null;
		if (mask.getType() == FlexibleImageType.UNKNOWN) {
			System.out.println("ERROR: Unknown image type!!!");
			return mask;
		}
		if (mask.getType() == FlexibleImageType.NIR)
			return mask;
		if (mask.getType() == FlexibleImageType.FLUO)
			return process(process(mask));
		if (mask.getType() == FlexibleImageType.VIS)
			return process(process(mask));
		
		return mask;
	}
	
	protected FlexibleImage process(FlexibleImage mask) {
		int[] img = mask.getAs1A();
		int w = mask.getWidth();
		int h = mask.getHeight();
		int[] filledPixelsPerLine = new int[h];
		int[] filledPixelsPerColumn = new int[w];
		int back = options.getBackground();
		for (int y = 0; y < h; y++) {
			int filled = 0;
			int yw = y * w;
			for (int x = 0; x < w; x++) {
				if (img[x + yw] != back) {
					filled++;
					filledPixelsPerColumn[x] = filledPixelsPerColumn[x] + 1;
				}
			}
			filledPixelsPerLine[y] = filled;
		}
		int n = 20;
		for (int scanBlock = 0; scanBlock < h * 0.1 / n; scanBlock++) {
			double avg = getAvg(filledPixelsPerLine, scanBlock * n, n);
			double stddev = getStdDev(avg, filledPixelsPerLine, scanBlock * n, n);
			double scut = stddev * 1.5;
			for (int i = 0; i < n; i++) {
				int y = scanBlock * n + i;
				int yw = y * w;
				int yw_ = (y - 1) * w;
				if (filledPixelsPerLine[y] - avg > scut) {
					for (int x = 0; x < w; x++) {
						if (y > 1) {
							img[x + yw] = img[x + yw_];
						}
					}
				}
			}
		}
		
		for (int scanBlock = 0; scanBlock < w / n; scanBlock++) {
			if (scanBlock * n > 0.3d * w && scanBlock * n < 0.7d * w)
				continue;
			double avg = getAvg(filledPixelsPerColumn, scanBlock * n, n);
			double stddev = getStdDev(avg, filledPixelsPerColumn, scanBlock * n, n);
			double scut = stddev * 1.5;
			for (int i = 0; i < n; i++) {
				int x = scanBlock * n + i;
				if (filledPixelsPerColumn[x] - avg > scut) {
					for (int y = 0; y < h; y++) {
						if (x > 1) {
							int yw = y * w;
							img[x + yw] = img[x - 1 + yw];
						}
					}
				}
			}
		}
		
		return new FlexibleImage(w, h, img).print("TEST " + System.currentTimeMillis(), false);
	}
	
	private double getAvg(int[] filledPixelsPerLine, int startIndex, int n) {
		double sum = 0;
		for (int idx = startIndex; idx < startIndex + n; idx++)
			sum += filledPixelsPerLine[idx];
		return sum / n;
	}
	
	private double getStdDev(double avg, int[] filledPixelsPerLine, int startIndex, int n) {
		double sumDiff = 0;
		for (int idx = startIndex; idx < startIndex + n; idx++)
			sumDiff += (filledPixelsPerLine[idx] - avg) * (filledPixelsPerLine[idx] - avg);
		double stdDev = Math.sqrt(sumDiff / (n - 1));
		return stdDev;
	}
}

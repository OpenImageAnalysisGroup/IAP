package iap.blocks.unused;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

// this might not be needed
/**
 * @author Christian Klukas
 */
public class BlockRemoveCameraLineArtifacts extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null)
			return null;
		if (mask.getCameraType() == CameraType.UNKNOWN) {
			System.out.println("ERROR: Unknown image type!!!");
			return mask;
		}
		if (mask.getCameraType() == CameraType.NIR)
			return mask;
		if (mask.getCameraType() == CameraType.FLUO)
			return process(process(mask));
		if (mask.getCameraType() == CameraType.VIS)
			return process(process(mask));
		
		return mask;
	}
	
	protected Image process(Image mask) {
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
		
		return new Image(w, h, img).show("TEST " + System.currentTimeMillis(), false);
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
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
}

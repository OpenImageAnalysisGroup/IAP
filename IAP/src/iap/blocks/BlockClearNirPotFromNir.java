/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Klukas
 */
public class BlockClearNirPotFromNir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processNIRimage() {
		
		boolean debug = getBoolean("debug", false);
		
		FlexibleImage nir = input().images().nir();
		
		if (nir == null || nir.getWidth() < 10 || nir.getHeight() < 10 || options.isBarleyInBarleySystem())
			return nir;
		
		nir.show("NIR pot analysis IN", debug);
		
		int[][] nirArray = nir.getAs2A();
		
		int w = nir.getWidth();
		int h = nir.getHeight();
		
		if (h > 22)
			for (int y = h - 1; y >= 21; y--) {
				int maxContinousBlack = 0;
				int currentlyBlack = 0;
				int nSkipWhite = 0;
				for (int x = 0; x < w; x++) {
					int v = nirArray[x][y] & 0x0000ff;
					int vAbove = nirArray[x][y - 20] & 0x0000ff;
					if (v < 110 || nSkipWhite < 10 || vAbove < 110) {
						currentlyBlack++;
						if (v >= 100)
							nSkipWhite++;
					} else {
						nSkipWhite = 0;
						if (currentlyBlack > maxContinousBlack)
							maxContinousBlack = currentlyBlack;
						currentlyBlack = 0;
					}
				}
				int g = 180;
				int gray = (0xFF << 24 | (g & 0xFF) << 16) | ((g & 0xFF) << 8) | ((g & 0xFF) << 0);
				double co = 0.1;
				if (!options.isBarleyInBarleySystem())
					co = 2;
				if (maxContinousBlack > co * w) {
					for (int x = 0; x < w; x++) {
						nirArray[x][y] = gray;
					}
				} else {
					int cut = y - 3;
					for (y = cut + 3; y >= cut; y--) {
						if (y > 0)
							for (int x = 0; x < w; x++) {
								nirArray[x][y] = gray;
							}
					}
					break;
				}
			}
		
		return new FlexibleImage(nirArray).show("NIR pot removed", debug);
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
}

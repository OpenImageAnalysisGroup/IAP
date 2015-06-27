package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ColorSpace;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class BlSubtractMedianLab extends AbstractBlock {
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		for (CameraType ct : CameraType.values())
			res.add(ct);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Subtract Median a*b* values (L*a*b* mode), shift L to center";
	}
	
	@Override
	public String getDescription() {
		return "Subtract the median a*, b* values from the corresponding channels in the L*a*b* color space. The median L value is adjusted to 127/2.";
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask != null && getBoolean("Process " + mask.getCameraType(), false)) {
			Image lu = mask.io().channels().getLabL().getImage();
			int w = mask.getWidth();
			int h = mask.getHeight();
			int[][] img = lu.getAs2A();
			
			float[] lValues = mask.io().channels().getLabFloatArray(Channel.LAB_L);
			float[] aValues = mask.io().channels().getLabFloatArray(Channel.LAB_A);
			float[] bValues = mask.io().channels().getLabFloatArray(Channel.LAB_B);
			
			DescriptiveStatistics statL = new DescriptiveStatistics();
			DescriptiveStatistics statA = new DescriptiveStatistics();
			DescriptiveStatistics statB = new DescriptiveStatistics();
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int color = img[x][y];
					if (color != ImageOperation.BACKGROUND_COLORint) {
						statL.addValue(lValues[x + y * w]);
						statA.addValue(aValues[x + y * w]);
						statB.addValue(bValues[x + y * w]);
					}
				}
			}
			
			float medianL = (float) statL.getPercentile(50);
			float medianA = (float) statA.getPercentile(50);
			float medianB = (float) statB.getPercentile(50);
			
			float targetL = 40;
			float offL = targetL - medianL;
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int color = img[x][y];
					if (color != ImageOperation.BACKGROUND_COLORint) {
						lValues[x + y * w] += offL;
						aValues[x + y * w] -= medianA;
						bValues[x + y * w] -= medianB;
					}
				}
			}
			
			Image res = new Image(w, h, lValues, aValues, bValues, ColorSpace.LAB_UNSHIFTED);
			int[][] resI = res.getAs2A();
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int color = img[x][y];
					if (color == ImageOperation.BACKGROUND_COLORint)
						resI[x][y] = ImageOperation.BACKGROUND_COLORint;
				}
			}
			return new Image(resI);
		} else
			return mask;
	}
}

package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.ArrayList;
import java.util.HashSet;

import org.Vector2d;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ColorSpace;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class BlColorBalanceCircularLumi extends AbstractBlock {
	
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
		return "Circular Balancing (L*a*b*-Luminescence)";
	}
	
	@Override
	public String getDescription() {
		return "Equalize luminescence of image using a circular pattern.";
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask != null && getBoolean("Process " + mask.getCameraType(), false)) {
			Image lu = mask.io().channels().getLabL().getImage();
			int w = mask.getWidth();
			int h = mask.getHeight();
			int[][] img = lu.getAs2A();
			
			double maxDistFromCenter = Math.min(mask.getWidth() / 2, mask.getHeight() / 2);
			int steps = 20;
			int rotations = 8;
			ArrayList<Double> distValues = new ArrayList<Double>(steps); // distance value
			ArrayList<Double> brightnessValues = new ArrayList<Double>(steps); // medians of rotation samples
			// perform 20x a sampling at 8 different positions, store median value
			Vector2d vec = new Vector2d();
			for (int i = 0; i < steps; i++) {
				double currentdist = maxDistFromCenter * i / steps;
				DescriptiveStatistics stat = new DescriptiveStatistics();
				for (double phi = 0; phi < 360; phi += 360 / rotations) {
					vec.x = currentdist;
					vec.y = 0;
					vec = vec.rotate(phi / 180 * Math.PI);
					int x = (int) (w / 2 + vec.x);
					int y = (int) (h / 2 + vec.y);
					int color = img[x][y];
					if (color != ImageOperation.BACKGROUND_COLORint) {
						int blue = (color & 0x0000ff);
						stat.addValue(blue);
					}
				}
				if (stat.getN() > 0) {
					double brightness = stat.getPercentile(50); // median
					distValues.add(currentdist);
					brightnessValues.add(brightness);
				}
			}
			
			PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
			WeightedObservedPoints obs = new WeightedObservedPoints();
			for (int i = 0; i < distValues.size(); i++) {
				obs.add(1, distValues.get(i), brightnessValues.get(i));
			}
			double[] coeff = fitter.fit(obs.toList());
			
			float[] lValues = mask.io().channels().getLabFloatArray(Channel.LAB_L);
			
			float base = 70;// coeff[0]*0.7;
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int color = img[x][y];
					if (color != ImageOperation.BACKGROUND_COLORint) {
						double currentdist = Math.sqrt((x - w / 2) * (x - w / 2) + (y - h / 2) * (y - h / 2));
						double scale = base / (currentdist * currentdist * coeff[2] + currentdist * coeff[1] + coeff[0]);
						lValues[x + y * w] *= scale;
					}
				}
			}
			
			float[] aValues = mask.io().channels().getLabFloatArray(Channel.LAB_A);
			float[] bValues = mask.io().channels().getLabFloatArray(Channel.LAB_B);
			
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

package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.extraction.postprocessors.MomentResultPostProcessor;

import java.awt.Point;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageMoments;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlCalcMoments extends AbstractBlock implements CalculatesProperties {
	
	@Override
	protected Image processVISmask() {
		Image img = input().masks().vis();
		if (img != null) {
			if (img.getWidth() > 1 && img.getHeight() > 1)
				calcMoments(img);
		}
		return img;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image img = input().masks().fluo();
		if (img != null) {
			if (img.getWidth() > 1 && img.getHeight() > 1)
				calcMoments(img);
		}
		return img;
	}
	
	private void calcMoments(Image img) {
		int background = ImageOperation.BACKGROUND_COLORint;
		ImageMoments im = new ImageMoments(img);
		double my20 = im.calcCentralMoment(2.0, 0.0, background);
		double my02 = im.calcCentralMoment(0.0, 2.0, background);
		double my11 = im.calcCentralMoment(1, 1, background);
		double my00 = im.calcCentralMoment(0, 0, background);
		double my10 = im.calcCentralMoment(1, 0, background);
		double my01 = im.calcCentralMoment(0, 1, background);
		double secondMoment_1_norm = im.calcNormalizedCentralMoment(2.0, 0.0, background);
		double secondMoment_2_norm = im.calcNormalizedCentralMoment(0.0, 2.0, background);
		double[] lambdas = ImageMoments.eigenValues(background);
		double eccentricity = Math.sqrt(1 - lambdas[1] / lambdas[0]);
		final Point centerOfGravity = im.getCenterOfGravity();
		final double omega = im.calcOmega(background);
		
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		final CameraType imageModality = img.getCameraType();
		
		if (lambdas[1] > lambdas[0]) {
			double temp1 = my20;
			my20 = my02;
			my02 = temp1;
			
			double temp2 = secondMoment_1_norm;
			secondMoment_1_norm = secondMoment_2_norm;
			secondMoment_2_norm = temp2;
		}
		
		rt.addValue("Result." + optionsAndResults.getCameraPosition() + imageModality + "2nd_moment_major", my20); // TRAIT NAME IS MANUALLY CREATED
		rt.addValue("Result." + optionsAndResults.getCameraPosition() + imageModality + "2nd_moment_minor", my02); // DATA IS NOT STORED??
		rt.addValue("Result." + optionsAndResults.getCameraPosition() + imageModality + "2nd_moment_major.norm", secondMoment_1_norm);
		rt.addValue("Result." + optionsAndResults.getCameraPosition() + imageModality + "2nd_moment_minor.norm", secondMoment_2_norm);
		rt.addValue("Result." + optionsAndResults.getCameraPosition() + imageModality + "eccentricity", eccentricity);
		
		// calc length for the axes (see Image Moments-Based Structuring and Tracking of Objects L OURENA ROCHA , L UIZ V ELHO , PAULO C EZAR P. C ARVALHO)
		double xc = my10 / my00;
		double yc = my01 / my00;
		double a = my20 / my00 - xc * xc;
		double b = 2 * (my11 / my00 - xc * yc);
		double c = my02 / my00 - yc * yc;
		final double length_major = Math.sqrt(1 * (a + c + Math.sqrt(b * b + (a - c) * (a - c)))); // orig 1 = 3
		final double length_minor = Math.sqrt(1 * (a + c - Math.sqrt(b * b + (a - c) * (a - c))));
		
		if (getBoolean("Mark in Result Image", false)) {
			RunnableOnImageSet ri = new MomentResultPostProcessor(length_major, centerOfGravity, imageModality, length_minor, omega);
			
			getResultSet().addImagePostProcessor(ri);
		}
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
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Calculate Image Moments";
	}
	
	@Override
	public String getDescription() {
		return "Calculates 2nd image moments for the foreground pixels, also the eccentricity.";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return null;
		
	}
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		// TODO Auto-generated method stub
		return null; // TODO !
	}
}

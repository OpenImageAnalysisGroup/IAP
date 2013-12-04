package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageMoments;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlCalcMoments extends AbstractBlock {
	
	boolean calcOnVis;
	
	@Override
	protected Image processVISmask() {
		calcOnVis = getBoolean("Process on Vis Image", false);
		Image img = input().masks().vis();
		if (img != null && calcOnVis) {
			calcMoments(img);
		}
		return img;
	}
	
	@Override
	protected Image processFLUOmask() {
		calcOnVis = getBoolean("Process on Vis Image", false);
		Image img = input().masks().fluo();
		if (img != null && !calcOnVis) {
			calcMoments(img);
		}
		return img;
	}
	
	private void calcMoments(Image img) {
		int background = ImageOperation.BACKGROUND_COLORint;
		ImageMoments im = new ImageMoments(img);
		double secondMoment_1_abs = im.calcCentralMoment(2.0, 0.0, background);
		double secondMoment_2_abs = im.calcCentralMoment(0.0, 2.0, background);
		double secondMoment_1_norm = im.calcNormalizedCentralMoment(2.0, 0.0, background);
		double secondMoment_2_norm = im.calcNormalizedCentralMoment(0.0, 2.0, background);
		double[] lambdas = ImageMoments.eigenValues(background);
		double eccentricity = Math.sqrt(1 - lambdas[1] / lambdas[0]);
		
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		String imageModality = calcOnVis == true ? "vis." : "fluo.";
		
		if (lambdas[1] > lambdas[0]) {
			double temp1 = secondMoment_1_abs;
			secondMoment_1_abs = secondMoment_2_abs;
			secondMoment_2_abs = temp1;
			
			double temp2 = secondMoment_1_norm;
			secondMoment_1_norm = secondMoment_2_norm;
			secondMoment_2_norm = temp2;
		}
		
		rt.addValue("Result." + options.getCameraPosition() + imageModality + "2nd_moment_major", secondMoment_1_abs);
		rt.addValue("Result." + options.getCameraPosition() + imageModality + "2nd_moment_minor", secondMoment_2_abs);
		rt.addValue("Result." + options.getCameraPosition() + imageModality + "2nd_moment_major.norm", secondMoment_1_norm);
		rt.addValue("Result." + options.getCameraPosition() + imageModality + "2nd_moment_minor.norm", secondMoment_2_norm);
		rt.addValue("Result." + options.getCameraPosition() + imageModality + "eccentricity", eccentricity);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public BlockType getBlockType() {
		// TODO Auto-generated method stub
		return null;
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
		return "Process Fluo images (default) or Vis images (if instead selected).";
		
	}
	
	@Override
	protected Image processMask(Image mask) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Point2d;

import de.ipk.ag_ba.image.operation.ImageMoments;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
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
		final Point centerOfGravity = im.getCenterOfGravity();
		
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
		
		final double omega = im.calcOmega(background);
		
		ArrayList<BlockPropertyValue> a = getProperties().getPropertiesSearch("RESULT_side." + imageModality + "hull.circumcircle.d");
		
		if (a.isEmpty())
			return;
		
		double circumcircle_d = a.get(0).getValue();
		
		// moments are < 1.0
		double m1 = secondMoment_1_norm + 1.;
		double m2 = secondMoment_2_norm + 1.;
		
		final double amount_m1 = m1 / (m1 + m2) * (circumcircle_d / 2);
		final double amount_m2 = m2 / (m1 + m2) * (circumcircle_d / 2);
		
		RunnableOnImageSet ri = new RunnableOnImageSet() {
			
			@Override
			public Image postProcessMask(Image img) {
				Point2d p1_start = new Point2d((centerOfGravity.x + amount_m1 * Math.cos(omega)), (centerOfGravity.y + amount_m1 * Math.sin(omega)));
				Point2d p2_start = new Point2d((centerOfGravity.x + amount_m2 * -Math.sin(omega)), (centerOfGravity.y + amount_m2 * Math.cos(omega)));
				
				Point2d p1_end = new Point2d((centerOfGravity.x - amount_m1 * Math.cos(omega)), (centerOfGravity.y - amount_m1 * Math.sin(omega)));
				Point2d p2_end = new Point2d((centerOfGravity.x - amount_m2 * -Math.sin(omega)), (centerOfGravity.y - amount_m2 * Math.cos(omega)));
				
				img = img
						.io()
						.canvas()
						.drawCircle(centerOfGravity.x, centerOfGravity.y, 10, Color.CYAN.getRGB(), 0.0, 5)
						.drawLine((int) p1_start.x, (int) p1_start.y, centerOfGravity.x, centerOfGravity.y, Color.PINK.getRGB(), 0.2, 1)
						.drawLine(centerOfGravity.x, centerOfGravity.y, (int) p1_end.x, (int) p1_end.y, Color.PINK.getRGB(), 0.2, 1)
						.drawLine((int) p2_start.x, (int) p2_start.y, centerOfGravity.x, centerOfGravity.y, Color.GREEN.getRGB(), 0.2, 1)
						.drawLine(centerOfGravity.x, centerOfGravity.y, (int) p2_end.x, (int) p2_end.y, Color.GREEN.getRGB(), 0.2, 1)
						.getImage();
				return img;
			}
			
			@Override
			public Image postProcessImage(Image image) {
				return image;
			}
			
			@Override
			public CameraType getConfig() {
				if (!calcOnVis)
					return CameraType.FLUO;
				else
					return CameraType.VIS;
			}
		};
		
		getProperties().addImagePostProcessor(ri);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		if (calcOnVis)
			res.add(CameraType.VIS);
		else
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
		return "Process Fluo images (default) or Vis images (if instead selected).";
		
	}
	
	@Override
	protected Image processMask(Image mask) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

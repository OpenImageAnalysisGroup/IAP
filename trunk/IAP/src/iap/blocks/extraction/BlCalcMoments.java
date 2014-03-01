package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashSet;

import javax.vecmath.Point2d;

import de.ipk.ag_ba.image.operation.ImageMoments;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
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
		
		String imageModality = calcOnVis == true ? "vis." : "fluo.";
		
		if (lambdas[1] > lambdas[0]) {
			double temp1 = my20;
			my20 = my02;
			my02 = temp1;
			
			double temp2 = secondMoment_1_norm;
			secondMoment_1_norm = secondMoment_2_norm;
			secondMoment_2_norm = temp2;
		}
		
		rt.addValue("Result." + optionsAndResults.getCameraPosition() + imageModality + "2nd_moment_major", my20);
		rt.addValue("Result." + optionsAndResults.getCameraPosition() + imageModality + "2nd_moment_minor", my02);
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
			RunnableOnImageSet ri = new RunnableOnImageSet() {
				
				@Override
				public Image postProcessMask(Image img) {
					Point2d p1_start = new Point2d((centerOfGravity.x + length_major * Math.cos(omega)), (centerOfGravity.y + length_major * Math.sin(omega)));
					Point2d p2_start = new Point2d((centerOfGravity.x + length_minor * -Math.sin(omega)), (centerOfGravity.y + length_minor * Math.cos(omega)));
					
					Point2d p1_end = new Point2d((centerOfGravity.x - length_major * Math.cos(omega)), (centerOfGravity.y - length_major * Math.sin(omega)));
					Point2d p2_end = new Point2d((centerOfGravity.x - length_minor * -Math.sin(omega)), (centerOfGravity.y - length_minor * Math.cos(omega)));
					
					// draw moments
					img = img
							.io()
							.canvas()
							.drawLine((int) p1_start.x, (int) p1_start.y, centerOfGravity.x, centerOfGravity.y, Color.PINK.getRGB(), 0.2, 1)
							.drawLine(centerOfGravity.x, centerOfGravity.y, (int) p1_end.x, (int) p1_end.y, Color.PINK.getRGB(), 0.2, 1)
							.drawLine((int) p2_start.x, (int) p2_start.y, centerOfGravity.x, centerOfGravity.y, Color.GREEN.getRGB(), 0.2, 1)
							.drawLine(centerOfGravity.x, centerOfGravity.y, (int) p2_end.x, (int) p2_end.y, Color.GREEN.getRGB(), 0.2, 1)
							.getImage();
					
					// draw MEE
					ImageCanvas canvas = img.io().canvas();
					Graphics2D g = (Graphics2D) canvas.getGraphics();
					g.setColor(Color.GRAY);
					g.rotate(omega, centerOfGravity.x, centerOfGravity.y);
					g.setStroke(new BasicStroke(2f));;
					g.drawOval(centerOfGravity.x - (int) length_major, centerOfGravity.y - (int) length_minor, (int) (1 + length_major) * 2,
							(int) (1 + length_minor) * 2);
					canvas.updateFromGraphics();
					img = canvas.getImage();
					
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
			
			getResultSet().addImagePostProcessor(ri);
		}
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

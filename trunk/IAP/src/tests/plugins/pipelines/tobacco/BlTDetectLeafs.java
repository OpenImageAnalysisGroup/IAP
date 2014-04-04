package tests.plugins.pipelines.tobacco;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;

import java.awt.Color;
import java.util.HashSet;

import javax.vecmath.Point2d;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlTDetectLeafs extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected Image processVISmask() {
		Image img = input().masks().vis();
		if (img == null)
			return null;
		// get rotation of plant
		Double rotation = input().images().getVisInfo().getPosition();
		if (rotation == null)
			rotation = 0d;
		
		Point2d[] tempres = (Point2d[]) doMeasure(img.copy());
		
		// save results
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		if (tempres != null) {
			
			rt.incrementCounter();
			rt.addValue("leaftip.left.y", tempres[0].y);
			rt.addValue("leaftip.right.y", tempres[1].y);
		}
		
		final int x1 = (int) tempres[0].x;
		final int y1 = (int) tempres[0].y;
		final int x2 = (int) tempres[1].x;
		final int y2 = (int) tempres[1].y;
		
		// mark tips in result
		getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
			
			@Override
			public Image postProcessMask(Image vis) {
				return vis.io().canvas().fillCircle(x1, y1, 30, Color.BLUE.getRGB(), 0.5).fillCircle(x2, y2, 30, Color.RED.getRGB(), 0.5).getImage();
			}
			
			@Override
			public CameraType getConfig() {
				return CameraType.VIS;
			}
			
			@Override
			public Image postProcessImage(Image vis) {
				return vis;
			}
		});
		
		getResultSet().storeResults("RESULT_", rt, getBlockPosition());
		
		return img;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	private Object doMeasure(Image img) {
		
		int background = ImageOperation.BACKGROUND_COLORint;
		
		// crop, use blue background
		Integer[] valuesBlue = { 0, 255, 0, 255, 0, 109 };
		Image blueBackinvert = img.copy().io().filterRemoveLAB(valuesBlue, background, false).removeSmallClusters(true, null).getImage();
		
		TopBottomLeftRight extremePoints = blueBackinvert.io().getExtremePoints(background);
		
		// convert parameter from percent
		double l = getDouble("crop left percent", 10) / 100d;
		double r = getDouble("crop right percent", 10) / 100d;
		double t = getDouble("crop top percent", 50) / 100d;
		double b = getDouble("crop bottom percent", 0) / 100d;
		
		// calculate crop values
		int cropL = (int) (extremePoints.getLeftX() + extremePoints.getWidth() * l);
		int cropT = (int) (extremePoints.getTopY() + extremePoints.getHeight() * t);
		int cropR = (int) (extremePoints.getRightX() - extremePoints.getWidth() * r);
		int cropB = (int) (extremePoints.getBottomY() - extremePoints.getHeight() * b);
		
		img = img
				.io()
				.clearOutsideRectangle(cropL, cropT, cropR, cropB)
				.getImage().show("crop for leaf detection", getBoolean("debug", false));
		
		// lab-filter
		Integer[] valuesPlant = { 65, 255, 0, 113, 0, 255 };
		Image imgLab = img.io().filterRemoveLAB(valuesPlant, background, false).removeSmallClusters(true, null).getImage();
		
		// create binary mask
		Image imgBinary = imgLab.io().threshold(255, background, Color.BLACK.getRGB()).invertImageJ().getImage();
		
		Point2d[] extrmePointsLeafs;
		
		// get max points horizontal
		extrmePointsLeafs = getExtremePointsHorizontalWithCoordinates(imgBinary, background);
		
		return extrmePointsLeafs;
	}
	
	private Point2d[] getExtremePointsHorizontalWithCoordinates(Image imgBinary, int background) {
		int[][] img2d = imgBinary.getAs2A();
		Point2d[] extrema = new Point2d[2];
		
		Point2d left = new Point2d(Integer.MAX_VALUE, 0);
		Point2d right = new Point2d(Integer.MIN_VALUE, 0);
		
		for (int x = 0; x < imgBinary.getWidth(); x++) {
			for (int y = 0; y < imgBinary.getHeight(); y++) {
				if (img2d[x][y] != background) {
					if (x < left.x) {
						left.x = x;
						left.y = y;
					}
					if (x > right.x) {
						right.x = x;
						right.y = y;
					}
				}
			}
		}
		extrema[0] = left;
		extrema[1] = right;
		return extrema;
	}
	
	@Override
	public String getName() {
		return "Detect leafs";
	}
	
	@Override
	public String getDescription() {
		return "Detection of the most outer leafs.";
	}
	
}

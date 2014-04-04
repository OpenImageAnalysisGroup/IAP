package tests.plugins.pipelines.tobacco;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Block especially for the tobacco flower detection. Only useful when images are generated with LemnaTec under usage of the blue background (or other color).
 * 
 * @author pape
 */
public class BlTCropUseBlueBackground extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		
		int background = ImageOperation.BACKGROUND_COLORint;
		Image img = input().masks().vis();
		
		if (img == null)
			return null;
		
		// filter blue background
		Integer[] valuesBlue = {
				getInt("from L", 0),
				getInt("to L", 255),
				getInt("from a", 0),
				getInt("to a", 255),
				getInt("from b", 0),
				getInt("to b", 109)
		};
		Image blueBackinvert = img.copy().io().filterRemoveLAB(valuesBlue, background, false).removeSmallClusters(true, null).getImage();
		
		TopBottomLeftRight extremePoints = blueBackinvert.io().getExtremePoints(background);
		
		double a = getDouble("crop left percent", 10) / 100d + 1;
		double b = 1 - getDouble("crop right percent", 10) / 100d;
		double c = getDouble("crop top percent", 0) / 100d + 1;
		double d = 1 - getDouble("crop bottom percent", 40) / 100d;
		
		img = img
				.io()
				.clearOutsideRectangle((int) (extremePoints.getLeftX() * a), (int) (extremePoints.getTopY() * c), (int) (extremePoints.getRightX() * b),
						(int) (extremePoints.getBottomY() * d)).getImage();
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
	
	@Override
	public String getName() {
		return "Crop image";
	}
	
	@Override
	public String getDescription() {
		return "Crop image by using blue background. If using a other background color, values can be changed.";
	}
	
}

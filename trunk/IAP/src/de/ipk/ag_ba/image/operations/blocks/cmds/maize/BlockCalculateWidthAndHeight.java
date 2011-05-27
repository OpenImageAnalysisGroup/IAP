package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Point;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockCalculateWidthAndHeight extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		int background = options.getBackground();
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			Point values = getWidthandHeightSide(getInput().getMasks().getFluo(), background);
			
			if (values != null) {
				getProperties().setNumericProperty(getBlockPosition(), "RESULT_side.width", values.x);
				getProperties().setNumericProperty(getBlockPosition(), "RESULT_side.height", values.y);
			}
		}
		if (options.getCameraTyp() == CameraTyp.TOP) {
			Point values = getWidthandHeightTop(getInput().getMasks().getFluo(), background);
			
			if (values != null) {
				getProperties().setNumericProperty(getBlockPosition(), "RESULT_top.width", values.x);
				getProperties().setNumericProperty(getBlockPosition(), "RESULT_top.height", values.y);
			}
		}
		return getInput().getMasks().getFluo();
	}
	
	private Point getWidthandHeightSide(FlexibleImage vis, int background) {
		Point values = new ImageOperation(vis).calculateWidthAndHeight(background);
		return values;
	}
	
	private Point getWidthandHeightTop(FlexibleImage image, int background) {
		if (image == null) {
			System.err.println("ERROR: BlockCalculateWidthAndHeight: Flu Mask is NULL!");
			return null;
		}
		
		int imagecentx = image.getWidth() / 2;
		int imagecenty = image.getHeight() / 2;
		int diagonal = (int) Math.sqrt((image.getWidth() * image.getWidth()) + (image.getHeight() * image.getHeight()));
		
		ImageOperation io = new ImageOperation(image);
		BlockProperty pa = getProperties().getNumericProperty(0, 1, PropertyNames.CENTROID_X);
		BlockProperty pb = getProperties().getNumericProperty(0, 1, PropertyNames.CENTROID_Y);
		
		if (pa != null && pb != null) {
			FlexibleImage resize = io.addBorder(diagonal - image.getWidth(),
					(int) (imagecentx - pa.getValue()),
					(int) (imagecenty - pb.getValue()), background).getImage();
			
			int angle = (int) getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION).getValue();
			
			resize = new ImageOperation(resize).rotate(-angle).getImage();
			// resize.print("resize");
			Point values = new ImageOperation(resize).calculateWidthAndHeight(background);
			
			return values;
		} else
			return null;
	}
}

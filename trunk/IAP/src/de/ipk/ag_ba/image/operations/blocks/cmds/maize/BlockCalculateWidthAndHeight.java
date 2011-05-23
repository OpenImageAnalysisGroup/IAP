package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Point;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockCalculateWidthAndHeight extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		int background = options.getBackground();
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			Point values = getWidthandHeightSide(getInput().getMasks().getVis(), background);
			
			getProperties().setNumericProperty(0, PropertyNames.RESULT_SIDE_WIDTH, values.x);
			getProperties().setNumericProperty(0, PropertyNames.RESULT_SIDE_HEIGHT, values.y);
		}
		if (options.getCameraTyp() == CameraTyp.TOP) {
			Point values = getWidthandHeightTop(getInput().getMasks().getVis(), background);
			
			getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_WIDTH, values.x);
			getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_HEIGHT, values.y);
		}
		return getInput().getMasks().getVis();
	}
	
	private Point getWidthandHeightSide(FlexibleImage vis, int background) {
		Point values = new ImageOperation(vis).calculateWidthAndHeight(background);
		return values;
	}
	
	private Point getWidthandHeightTop(FlexibleImage image, int background) {
		int imagecentx = image.getWidth() / 2;
		int imagecenty = image.getHeight() / 2;
		int diagonal = (int) Math.sqrt((image.getWidth() * image.getWidth()) + (image.getHeight() * image.getHeight()));
		
		ImageOperation io = new ImageOperation(image);
		FlexibleImage resize = io.addBorder(diagonal - image.getWidth(),
				(int) (imagecentx - getProperties().getNumericProperty(0, 1, PropertyNames.CENTROID_X).getValue()),
				(int) (imagecenty - getProperties().getNumericProperty(0, 1, PropertyNames.CENTROID_Y).getValue()), background).getImage();
		
		int angle = (int) getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION).getValue();
		
		resize = new ImageOperation(resize).rotate(-angle).getImage();
		
		Point values = new ImageOperation(resize).calculateWidthAndHeight(background);
		
		return values;
	}
}

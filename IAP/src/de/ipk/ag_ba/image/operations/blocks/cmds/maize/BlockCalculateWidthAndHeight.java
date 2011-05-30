package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Point;

import org.Vector2d;

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
		int realMarkerDist = 200;
		
		BlockProperty distLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_DISTANCE_MARKER_LEFT);
		BlockProperty distRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_DISTANCE_MARKER_RIGHT);
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			Point values = getWidthandHeightSide(getInput().getMasks().getFluo(), background);
			
			if (values != null) {
				
				if (distLeft != null) {
					getProperties().setNumericProperty(getBlockPosition(), "RESULT_side.width", values.x * (realMarkerDist / distLeft.getValue()));
					getProperties().setNumericProperty(getBlockPosition(), "RESULT_side.height", values.y * (realMarkerDist / distLeft.getValue()));
				}
				
				if (distLeft == null && distRight != null) {
					getProperties().setNumericProperty(getBlockPosition(), "RESULT_side.width", values.x * (realMarkerDist / distRight.getValue()));
					getProperties().setNumericProperty(getBlockPosition(), "RESULT_side.height", values.y * (realMarkerDist / distRight.getValue()));
				}
			}
		}
		
		// if (options.getCameraTyp() == CameraTyp.TOP) {
		// Point values = getWidthandHeightTop(getInput().getMasks().getFluo(), background);
		//
		// if (values != null) {
		//
		// if (distLeft != null) {
		// getProperties().setNumericProperty(getBlockPosition(), "RESULT_top.width", values.x * (realMarkerDist / distLeft.getValue()));
		// getProperties().setNumericProperty(getBlockPosition(), "RESULT_top.height", values.y * (realMarkerDist / distLeft.getValue()));
		// }
		//
		// if (distLeft == null && distRight != null) {
		// getProperties().setNumericProperty(getBlockPosition(), "RESULT_top.width", values.x * (realMarkerDist / distRight.getValue()));
		// getProperties().setNumericProperty(getBlockPosition(), "RESULT_top.height", values.y * (realMarkerDist / distRight.getValue()));
		// }
		// }
		// }
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
		FlexibleImage resize = null;
		
		if (pa != null && pb != null) {
			
			Vector2d cent = io.getCentroid(background);
			int centroidX = (int) cent.x;
			int centroidY = (int) cent.y;
			
			// size vis and fluo are the same, scalefactor cant be calculated
			// int paScale = (int) (pa.getValue() * (getInput().getMasks().getVis().getWidth() / image.getWidth()));
			// int pbScale = (int) (pa.getValue() * (getInput().getMasks().getVis().getWidth() / image.getWidth()));
			
			if (image.getWidth() > image.getHeight()) {
				resize = io.addBorder((diagonal - image.getWidth()) / 2,
						(int) (imagecentx - centroidX),
						(int) (imagecenty - centroidY), background).getImage();
			} else {
				resize = io.addBorder((diagonal - image.getHeight()) / 2,
						(int) (imagecentx - centroidX),
						(int) (imagecenty - centroidY), background).getImage();
			}
			
			int angle = (int) getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION).getValue();
			
			if (resize != null) {
				resize = new ImageOperation(resize).rotate(-angle).getImage();
				// resize.print("resize");
				Point values = new ImageOperation(resize).calculateWidthAndHeight(background);
				return values;
			} else {
				return null;
			}
		} else
			return null;
	}
}

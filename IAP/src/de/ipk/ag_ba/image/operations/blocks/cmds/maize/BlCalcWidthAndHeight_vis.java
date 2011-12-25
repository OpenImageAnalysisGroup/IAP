package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;
import java.awt.Point;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.Vector2d;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.operations.blocks.properties.RunnableOnImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class BlCalcWidthAndHeight_vis extends
		AbstractSnapshotAnalysisBlockFIS {
	
	private static final boolean debug = false;
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		
		int background = options.getBackground();
		double realMarkerDistHorizontal = options
				.getIntSetting(Setting.REAL_MARKER_DISTANCE);
		
		BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1,
				PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
		
		boolean useFluo = false;// options.isMaize();
		
		int vertYsoilLevel = -1;
		if (!options.isBarleyInBarleySystem() && options.getCameraPosition() == CameraPosition.SIDE) {
			if (useFluo) {
				if (getProperties().getNumericProperty(0, 1,
						PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO) != null)
					vertYsoilLevel = (int) getProperties()
							.getNumericProperty(
									0,
									1,
									PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO)
							.getValue();
			} else {
				if (getProperties().getNumericProperty(0, 1,
						PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS) != null)
					vertYsoilLevel = (int) getProperties()
							.getNumericProperty(
									0,
									1,
									PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS)
							.getValue();
			}
		}
		final int vertYsoilLevelF = vertYsoilLevel;
		
		FlexibleImage visRes = getInput().getMasks().getVis();
		
		FlexibleImage img = useFluo ? getInput().getMasks().getFluo()
				: getInput().getMasks().getVis();
		if (options.getCameraPosition() == CameraPosition.SIDE && img != null) {
			final TopBottomLeftRight temp = getWidthAndHeightSide(img,
					background, vertYsoilLevel);
			
			double resf = useFluo ? (double) getInput().getMasks().getVis()
					.getWidth()
					/ (double) img.getWidth()
					* (getInput().getImages().getFluo().getWidth() / (double) getInput()
							.getImages().getFluo().getHeight())
					/ (getInput().getImages().getVis().getWidth() / (double) getInput()
							.getImages().getVis().getHeight())
					: 1.0;
			
			double resfww = useFluo ? (double) getInput().getMasks().getVis()
					.getWidth()
					/ (double) img.getWidth() : 1.0;
			
			final Point values = temp != null ? new Point(Math.abs(temp
					.getRightX() - temp.getLeftX()), Math.abs(temp.getBottomY()
					- temp.getTopY())) : null;
			
			if (values != null) {
				boolean drawVerticalHeightBar = false;
				if (drawVerticalHeightBar)
					if (!useFluo) {
						getProperties().addImagePostProcessor(
								new RunnableOnImageSet() {
									@Override
									public FlexibleImage postProcessVis(
											FlexibleImage visRes) {
										if (vertYsoilLevelF > 0)
											visRes = visRes
													.getIO()
													.getCanvas()
													.fillRect(
															values.x
																	/ 2
																	+ temp.getLeftX(),
															vertYsoilLevelF,
															10,
															vertYsoilLevelF
																	- values.y,
															Color.BLUE.getRGB(),
															255).getImage()
													.print("DEBUG", debug);
										else
											visRes = visRes
													.getIO()
													.getCanvas()
													.fillRect(
															values.x
																	/ 2
																	+ temp.getLeftX(),
															temp.getTopY(),
															10,
															temp.getBottomY()
																	- temp.getTopY(),
															Color.RED.getRGB(), 255)
													.getImage()
													.print("DEBUG", debug);
										return visRes;
									}
									
									@Override
									public ImageConfiguration getConfig() {
										return ImageConfiguration.RgbSide;
									}
								});
					}
				
				if (distHorizontal != null) {
					getProperties()
							.setNumericProperty(
									getBlockPosition(),
									"RESULT_side.width.norm",
									values.x
											* (realMarkerDistHorizontal / distHorizontal
													.getValue()) * resfww);
					getProperties()
							.setNumericProperty(
									getBlockPosition(),
									"RESULT_side.height.norm",
									values.y
											* (realMarkerDistHorizontal / distHorizontal
													.getValue()) * resf);
				}
				getProperties().setNumericProperty(getBlockPosition(),
						"RESULT_side.width", values.x);
				getProperties().setNumericProperty(getBlockPosition(),
						"RESULT_side.height", values.y);
				
			}
		}
		
		// if (options.getCameraTyp() == CameraTyp.TOP) {
		// Point values = getWidthandHeightTop(getInput().getMasks().getFluo(),
		// background);
		//
		// if (values != null) {
		//
		// if (distLeft != null) {
		// getProperties().setNumericProperty(getBlockPosition(),
		// "RESULT_top.width", values.x * (realMarkerDist /
		// distLeft.getValue()));
		// getProperties().setNumericProperty(getBlockPosition(),
		// "RESULT_top.height", values.y * (realMarkerDist /
		// distLeft.getValue()));
		// }
		//
		// if (distLeft == null && distRight != null) {
		// getProperties().setNumericProperty(getBlockPosition(),
		// "RESULT_top.width", values.x * (realMarkerDist /
		// distRight.getValue()));
		// getProperties().setNumericProperty(getBlockPosition(),
		// "RESULT_top.height", values.y * (realMarkerDist /
		// distRight.getValue()));
		// }
		// }
		// }
		return visRes;
	}
	
	private TopBottomLeftRight getWidthAndHeightSide(FlexibleImage vis,
			int background, int vertYsoilLevel) {
		TopBottomLeftRight temp = new ImageOperation(vis)
				.getExtremePoints(background);
		if (temp != null) {
			if (vertYsoilLevel > 0)
				temp.setBottom(vertYsoilLevel);
			return temp;
			// Point values = new Point(Math.abs(temp.getRightX() -
			// temp.getLeftX()), Math.abs(temp.getBottomY() - temp.getTopY()));
			// return values;
		} else
			return null;
	}
	
	private Point getWidthandHeightTop(FlexibleImage image, int background) {
		if (image == null) {
			System.err
					.println("ERROR: BlockCalculateWidthAndHeight: Flu Mask is NULL!");
			return null;
		}
		
		int imagecentx = image.getWidth() / 2;
		int imagecenty = image.getHeight() / 2;
		int diagonal = (int) Math.sqrt((image.getWidth() * image.getWidth())
				+ (image.getHeight() * image.getHeight()));
		
		ImageOperation io = new ImageOperation(image);
		BlockProperty pa = getProperties().getNumericProperty(0, 1,
				PropertyNames.CENTROID_X);
		BlockProperty pb = getProperties().getNumericProperty(0, 1,
				PropertyNames.CENTROID_Y);
		FlexibleImage resize = null;
		
		if (pa != null && pb != null) {
			
			Vector2d cent = io.getCentroid(background);
			int centroidX = (int) cent.x;
			int centroidY = (int) cent.y;
			
			// size vis and fluo are the same, scalefactor cant be calculated
			// int paScale = (int) (pa.getValue() *
			// (getInput().getMasks().getVis().getWidth() / image.getWidth()));
			// int pbScale = (int) (pa.getValue() *
			// (getInput().getMasks().getVis().getWidth() / image.getWidth()));
			
			if (image.getWidth() > image.getHeight()) {
				resize = io.addBorder((diagonal - image.getWidth()) / 2,
						(imagecentx - centroidX), (imagecenty - centroidY),
						background).getImage();
			} else {
				resize = io.addBorder((diagonal - image.getHeight()) / 2,
						(imagecentx - centroidX), (imagecenty - centroidY),
						background).getImage();
			}
			
			int angle = (int) getProperties().getNumericProperty(0, 1,
					PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION).getValue();
			
			if (resize != null) {
				resize = new ImageOperation(resize).rotate(-angle).getImage();
				// resize.print("resize");
				TopBottomLeftRight temp = getWidthAndHeightSide(resize,
						background, -1);
				Point values = new Point(Math.abs(temp.getRightX()
						- temp.getLeftX()), Math.abs(temp.getBottomY()
						- temp.getTopY()));
				return values;
			} else {
				return null;
			}
		} else
			return null;
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, BlockResultSet>> time2allResultsForSnapshot,
			TreeMap<Long, BlockResultSet> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws InterruptedException {
		super.postProcessResultsForAllTimesAndAngles(plandID2time2waterData,
				time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, optStatus);
		
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] { "RESULT_side.width", "RESULT_side.width", "RESULT_side.width.norm", "RESULT_side.height.norm" });
	}
}

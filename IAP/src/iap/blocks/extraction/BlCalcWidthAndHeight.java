package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Calculates the plant width and height properties from the visible light image mask.
 * 
 * @author klukas
 */
public class BlCalcWidthAndHeight extends
		AbstractSnapshotAnalysisBlock {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected Image processVISmask() {
		
		int background = options.getBackground();
		
		Double realMarkerDistHorizontal = options.getREAL_MARKER_DISTANCE();
		Double distHorizontal = options.getCalculatedBlueMarkerDistance();
		
		boolean useFluo = false;// options.isMaize();
		
		Image visRes = input().masks().vis();
		if (visRes == null)
			return null;
		
		int vertYsoilLevel = -1;
		
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (useFluo) {
				if (getProperties().getNumericProperty(0, 1,
						PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO.getName(options.getCameraPosition())) != null)
					vertYsoilLevel = (int) getProperties()
							.getNumericProperty(
									0,
									1,
									PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO.getName(options.getCameraPosition()))
							.getValue();
			} else {
				if (getProperties().getNumericProperty(0, 1,
						PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS.getName(options.getCameraPosition())) != null)
					vertYsoilLevel = (int) getProperties()
							.getNumericProperty(
									0,
									1,
									PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS.getName(options.getCameraPosition()))
							.getValue();
			}
		}
		final int vertYsoilLevelF = vertYsoilLevel;
		
		Image img = useFluo ? input().masks().fluo()
				: input().masks().vis();
		if (options.getCameraPosition() == CameraPosition.SIDE && img != null) {
			final TopBottomLeftRight temp = getWidthAndHeightSide(img,
					background, vertYsoilLevel);
			
			double resf = useFluo ? (double) input().masks().vis()
					.getWidth()
					/ (double) img.getWidth()
					* (input().images().fluo().getWidth() / (double) input()
							.images().fluo().getHeight())
					/ (input().images().vis().getWidth() / (double) input()
							.images().vis().getHeight())
					: 1.0;
			
			final Point values = temp != null ? new Point(Math.abs(temp
					.getRightX() - temp.getLeftX()), Math.abs(temp.getBottomY()
					- temp.getTopY())) : null;
			
			if (values != null) {
				boolean drawVerticalHeightBar = getBoolean("Draw Height Line", true);
				if (drawVerticalHeightBar)
					if (!useFluo) {
						getProperties().addImagePostProcessor(
								new RunnableOnImageSet() {
									@Override
									public Image postProcessImage(
											Image visRes) {
										if (vertYsoilLevelF > 0)
											visRes = visRes
													.io()
													.canvas()
													.fillRect(
															values.x
																	/ 2
																	+ temp.getLeftX(),
															vertYsoilLevelF - values.y,
															10,
															values.y,
															Color.MAGENTA.getRGB(),
															255)
													.drawLine(values.x / 2 + temp.getLeftX() - 50, vertYsoilLevelF - values.y, values.x / 2 + temp.getLeftX() + 50,
															vertYsoilLevelF - values.y, Color.MAGENTA.getRGB(), 0.5, 5)
													.drawLine(values.x / 2 + temp.getLeftX() - 50, vertYsoilLevelF, values.x / 2 + temp.getLeftX() + 50,
															vertYsoilLevelF, Color.MAGENTA.getRGB(), 0.5, 5)
													.getImage()
													.show("DEBUG", debug);
										else {
											visRes = visRes
													.io()
													.canvas()
													.fillRect(
															values.x
																	/ 2
																	+ temp.getLeftX(),
															temp.getTopY() - temp.getBottomY()
																	+ temp.getTopY() + temp.getBottomY()
																	- temp.getTopY(),
															500,
															10,
															Color.BLUE.getRGB(), 255)
													.getImage()
													.show("DEBUG", debug);
											visRes = visRes
													.io()
													.canvas()
													.fillRect(
															values.x
																	/ 2
																	+ temp.getLeftX(),
															temp.getTopY() - temp.getBottomY()
																	+ temp.getTopY() + temp.getBottomY()
																	- temp.getTopY(),
															10,
															temp.getBottomY()
																	- temp.getTopY(),
															Color.RED.getRGB(), 255)
													.getImage()
													.show("DEBUG", debug);
										}
										return visRes;
									}
									
									@Override
									public CameraType getConfig() {
										return CameraType.VIS;
									}
									
									@Override
									public Image postProcessMask(Image mask) {
										return mask;
									}
								});
					}
				
				if (distHorizontal != null && realMarkerDistHorizontal != null) {
					getProperties()
							.setNumericProperty(
									getBlockPosition(),
									"RESULT_side.width.norm",
									values.x
											* (realMarkerDistHorizontal / distHorizontal) * resf, "mm");
					getProperties()
							.setNumericProperty(
									getBlockPosition(),
									"RESULT_side.height.norm",
									values.y
											* (realMarkerDistHorizontal / distHorizontal) * resf, "mm");
				}
				getProperties().setNumericProperty(getBlockPosition(),
						"RESULT_side.width", values.x, "px");
				getProperties().setNumericProperty(getBlockPosition(),
						"RESULT_side.height", values.y, "px");
				
			}
		}
		return visRes;
	}
	
	private TopBottomLeftRight getWidthAndHeightSide(Image vis,
			int background, int vertYsoilLevel) {
		TopBottomLeftRight temp = new ImageOperation(vis)
				.getExtremePoints(background);
		if (temp != null) {
			if (vertYsoilLevel > 0)
				temp.setBottom(vertYsoilLevel);
			return temp;
		} else
			return null;
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws InterruptedException {
		super.postProcessResultsForAllTimesAndAngles(plandID2time2waterData,
				time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, optStatus);
		
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] { "RESULT_side.width", "RESULT_side.width.norm", "RESULT_side.height.norm" });
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
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
		return "Calculate Width and Height (Side)";
	}
	
	@Override
	public String getDescription() {
		return "Calculates the plant width and height properties from the visible light image mask.";
	}
}

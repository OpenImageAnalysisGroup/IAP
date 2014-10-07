package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
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
		AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	private static final String HEIGHT = "height";
	private static final String WIDTH = "width";
	private static final String HEIGHT_NORM = "height.norm";
	private static final String WIDTH_NORM = "width.norm";
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
		
		int background = optionsAndResults.getBackground();
		
		Double realMarkerDistHorizontal = optionsAndResults.getREAL_MARKER_DISTANCE();
		Double distHorizontal = optionsAndResults.getCalculatedBlueMarkerDistance();
		
		boolean useFluo = false;
		
		Image visRes = input().masks().vis();
		if (visRes == null)
			return null;
		
		final int vertYsoilLevelF = -1;
		
		Image img = useFluo ? input().masks().fluo()
				: input().masks().vis();
		if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE && img != null) {
			final TopBottomLeftRight temp = getWidthAndHeightSide(img,
					background, vertYsoilLevelF);
			
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
						getResultSet().addImagePostProcessor(
								new RunnableOnImageSet() {
									@Override
									public Image postProcessMask(
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
									public Image postProcessImage(Image image) {
										return image;
									}
								});
					}
				
				if (distHorizontal != null && realMarkerDistHorizontal != null) {
					getResultSet()
							.setNumericResult(
									getBlockPosition(),
									new Trait(cp(), CameraType.VIS, WIDTH_NORM),
									values.x * (realMarkerDistHorizontal / distHorizontal) * resf, "mm", this);
					getResultSet()
							.setNumericResult(
									getBlockPosition(),
									new Trait(cp(), CameraType.VIS, HEIGHT_NORM),
									values.y * (realMarkerDistHorizontal / distHorizontal) * resf, "mm", this);
				}
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cp(), CameraType.VIS, WIDTH), values.x, "px", this);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cp(), CameraType.VIS, HEIGHT), values.y, "px", this);
				
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
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			CalculatesProperties propertyCalculator) throws InterruptedException {
		super.postProcessResultsForAllTimesAndAngles(plandID2time2waterData,
				time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, optStatus, this);
		
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] { "RESULT_side.width", "RESULT_side.width.norm", "RESULT_side.height.norm" }, this);
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
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty(WIDTH,
						"Width of the plant, measured as the horizontal distance in pixels "
								+ "from the most left plant pixel to the most right plant pixel."),
				new CalculatedProperty(WIDTH_NORM,
						"Width of the plant, measured as the horizontal distance in pixels "
								+ "from the most left plant pixel to the most right plant pixel normalized to mm."),
				new CalculatedProperty(HEIGHT,
						"Height of the plant, measured as the vertical distance in pixels "
								+ "from the most top plant pixel to the most bottom plant pixel."),
				new CalculatedProperty(HEIGHT_NORM,
						"Height of the plant, measured as the vertical distance in pixels "
								+ "from the most top plant pixel to the most bottom plant pixel normalized to mm.")
		};
	}
}

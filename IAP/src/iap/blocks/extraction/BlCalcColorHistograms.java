package iap.blocks.extraction;

import iap.blocks.auto.BlAdaptiveSegmentationFluo;
import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashSet;

import org.SystemAnalysis;
import org.Vector2d;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.intensity.Histogram;
import de.ipk.ag_ba.image.operations.intensity.Histogram.Mode;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Calculates overall properties of the vis, fluo and nir images, such as number of pixels, intensities, NDVI and more.
 * Is used in the current pipelines for maize and barley.
 * Does not need any input parameters.
 * 
 * @author klukas, pape
 *         status: ok, 23.11.2011, c. klukas
 */
public class BlCalcColorHistograms extends AbstractSnapshotAnalysisBlock {
	
	private boolean debug = false;
	private boolean debugRegionParts = false;
	
	private boolean calculateValuesAlsoForDifferentRegions = false;
	private boolean addHistogramValues = false;
	private boolean addHistogramValuesForSections = false;
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	Double markerDistanceHorizontally = null;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		debug = getBoolean("debug", false);
		debugRegionParts = getBoolean("Debug-Show-Region-Images", false);
		calculateValuesAlsoForDifferentRegions = getBoolean("Calculate_Values_For_Different_Regions", false);
		addHistogramValues = getBoolean("Add_Histogram_Values", false);
		addHistogramValuesForSections = getBoolean("Histogram for Regions", false);
		
		markerDistanceHorizontally = optionsAndResults.getCalculatedBlueMarkerDistance();
	}
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() != null) {
			
			ImageOperation io = new ImageOperation(input().masks().vis().copy()).show("BEFORE TRIMM", debug).bm().erode(getInt("Erode-Cnt-Vis", 2)).io();
			io = input().masks().vis().copy().io().applyMask_ResizeSourceIfNeeded(io.getImage(), ImageOperation.BACKGROUND_COLORint)
					.show("AFTER ERODE", debug);
			
			String pre = "RESULT_" + optionsAndResults.getCameraPosition();
			int regions = 5;
			if (calculateValuesAlsoForDifferentRegions) {
				if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE) {
					for (int r = 0; r < regions; r++)
						processVisibleImage(io.getBottom(r, regions).show("Side Part " + r + "/" + regions, debugRegionParts),
								pre + ".section_" + (r + 1) + "_" + regions + ".", true);
				}
				if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
					for (int r = 0; r < regions; r++)
						processVisibleImage(io.getInnerCircle(r, regions).show("Top Part " + r + "/" + regions, debugRegionParts),
								pre + ".section_" + (r + 1) + "_" + regions + ".", true);
				}
			}
			
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				// calculate average distance to center
				Vector2d gravityCenter = io.getCentroid(optionsAndResults.getBackground());
				if (gravityCenter != null) {
					double averageDistance = io.calculateAverageDistanceTo(gravityCenter);
					if (!Double.isNaN(averageDistance))
						getResultSet().setNumericResult(0, pre + ".avg_distance_to_center", averageDistance, "px");
				}
			}
			
			processVisibleImage(io, pre + ".", false);
			
			return input().masks().vis();
		} else
			return null;
	}
	
	protected void processVisibleImage(ImageOperation io, String resultPrefix, boolean isSection) {
		int visibleFilledPixels = io.countFilledPixels();
		
		double visibleIntensitySumR = io.intensitySumOfChannel(false, true, false, false);
		double visibleIntensitySumG = io.intensitySumOfChannel(false, false, true, false);
		double visibleIntensitySumB = io.intensitySumOfChannel(false, false, false, true);
		double averageVisR = visibleIntensitySumR / visibleFilledPixels;
		double averageVisG = visibleIntensitySumG / visibleFilledPixels;
		double averageVisB = visibleIntensitySumB / visibleFilledPixels;
		
		ResultsTableWithUnits rt1 = io.intensity(getInt("Bin-Cnt-Vis", 20)).calculateHistorgram(
				markerDistanceHorizontally,
				optionsAndResults.getREAL_MARKER_DISTANCE(), Histogram.Mode.MODE_HUE_VIS_ANALYSIS,
				isSection ? addHistogramValuesForSections : addHistogramValues);
		getResultSet().storeResults(resultPrefix + "vis.", rt1, getBlockPosition());
		
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		rt.addValue("ndvi.vis.red.intensity.average", averageVisR);
		rt.addValue("ndvi.vis.green.intensity.average", averageVisG);
		rt.addValue("ndvi.vis.blue.intensity.average", averageVisB);
		
		if (input().masks().nir() != null) {
			double nirIntensitySum = input().masks().nir().io().intensitySumOfChannel(false, true, false, false);
			int nirFilledPixels = input().masks().nir().io().countFilledPixels();
			double averageNir = 1 - nirIntensitySum / nirFilledPixels;
			// rt.addValue("ndvi.nir.intensity.average", averageNir);
			
			double ndvi = (averageNir - averageVisR) / (averageNir + averageVisR);
			rt.addValue("ndvi", ndvi);
		}
		
		getResultSet().storeResults(resultPrefix, rt, getBlockPosition());
	}
	
	@Override
	protected Image processFLUOmask() {
		
		if (input().masks().fluo() != null) {
			ImageOperation io = new ImageOperation(input().masks().fluo().copy()).show("BEFORE TRIMM", debug).bm().
					erode(getInt("Erode-Cnt-Fluo", 2)).io();
			io = input().masks().fluo().copy().io().applyMask_ResizeSourceIfNeeded(io.getImage(), ImageOperation.BACKGROUND_COLORint)
					.show("AFTER ERODE // Red Color Fluo Image", debug);
			
			{ // red color fluo image
				ResultsTableWithUnits rt = io.intensity(getInt("Bin-Cnt-Fluo", 20)).calculateHistorgram(markerDistanceHorizontally,
						optionsAndResults.getREAL_MARKER_DISTANCE(), Mode.MODE_HUE_VIS_ANALYSIS,
						getBoolean("Add Fluo Color Bins", false));
				if (rt != null)
					getResultSet().storeResults("RESULT_" + optionsAndResults.getCameraPosition() + ".fluo.", rt, getBlockPosition());
			}
			
			{ // blue color fluo image
				Image of = getResultSet().getImage(BlAdaptiveSegmentationFluo.RESULT_OF_FLUO_INTENSITY);
				if (of != null) {
					of = of.io().applyMask(input().masks().fluo()).getImage().show("Blue Color Fluo Image", debug);
					ResultsTableWithUnits rt = of.io().intensity(getInt("Bin-Cnt-Fluo", 20)).calculateHistorgram(markerDistanceHorizontally,
							optionsAndResults.getREAL_MARKER_DISTANCE(), Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS,
							addHistogramValues); // markerDistanceHorizontally
					if (rt != null)
						getResultSet().storeResults("RESULT_" + optionsAndResults.getCameraPosition() + ".fluo.", rt, getBlockPosition());
				}
			}
			return input().masks().fluo();// io.getImage();
		} else
			return null;
	}
	
	@Override
	protected Image processNIRmask() {
		Image nirSkel = getResultSet().getImage("nir_skeleton");
		if (nirSkel != null) {
			int nirSkeletonFilledPixels = nirSkel.io().countFilledPixels();
			double nirSkeletonIntensitySum = nirSkel.io().intensitySumOfChannel(false, true, false, false);
			double avgNirSkel = 1 - nirSkeletonIntensitySum / nirSkeletonFilledPixels;
			getResultSet().setNumericResult(getBlockPosition(),
					"RESULT_" + optionsAndResults.getCameraPosition() + ".nir.skeleton.intensity.average", avgNirSkel,
					null);
		}
		
		if (input().masks().nir() != null) {
			ImageOperation io = new ImageOperation(input().masks().nir());
			if (input().masks().nir().getHeight() > 1) {
				int nirFilledPixels = input().masks().nir().io().countFilledPixels();
				double nirIntensitySum = input().masks().nir().io().intensitySumOfChannel(false, true, false, false);
				double avgNir = 1 - nirIntensitySum / nirFilledPixels;
				getResultSet().setNumericResult(
						getBlockPosition(), "RESULT_" + optionsAndResults.getCameraPosition() + ".nir.intensity.average",
						avgNir, null);
				boolean wetnessAnalysis = false;
				if (wetnessAnalysis) {
					int[] nirImg = input().masks().nir().getAs1A();
					int filled = 0;
					double fSum = 0;
					int b = ImageOperation.BACKGROUND_COLORint;
					double weightOfPlant = 0; // fully wet: 1 unit, fully dry: 1/7 unit
					double dwf = getDouble("Dry-Weight-Factor", 1 / 7d);
					for (int x : nirImg) {
						// Feuchtigkeit (%) = -7E-05x^3 + 0,0627x^2 - 15,416x + 1156,1 // Formel: E-Mail Alex 10.8.2011
						if (x != b) {
							double f = -7E-05 * x * x * x + 0.0627 * x * x - 15.416 * x + 1156.1;
							if (f < 0)
								f = 0;
							if (f > 100)
								f = 100;
							fSum += f;
							filled++;
							double realF = 1 - (x - 80d) / (160d - 80d);
							realF *= 100;
							weightOfPlant += dwf + (1 - dwf) * realF / 100d;
						}
					}
					getResultSet().setNumericResult(getBlockPosition(),
							"RESULT_" + optionsAndResults.getCameraPosition() + ".nir.wetness.plant_weight", weightOfPlant,
							null);
					getResultSet().setNumericResult(getBlockPosition(),
							"RESULT_" + optionsAndResults.getCameraPosition() + ".nir.wetness.plant_weight_drought_loss",
							filled - weightOfPlant,
							null);
					if (filled > 0) {
						getResultSet().setNumericResult(getBlockPosition(), "RESULT_" + optionsAndResults.getCameraPosition() + ".nir.wetness.average",
								fSum / filled, null);
					} else
						getResultSet().setNumericResult(getBlockPosition(), "RESULT_" + optionsAndResults.getCameraPosition() + ".nir.wetness.average", 0d, null);
				}
				ResultsTableWithUnits rt = io.intensity(getInt("Bin-Cnt-NIR", 20)).calculateHistorgram(markerDistanceHorizontally,
						optionsAndResults.getREAL_MARKER_DISTANCE(), Mode.MODE_GRAY_NIR_ANALYSIS, addHistogramValues); // markerDistanceHorizontally
				
				if (optionsAndResults == null)
					System.err.println(SystemAnalysis.getCurrentTime() + ">SEVERE INTERNAL ERROR: OPTIONS IS NULL!");
				if (rt != null)
					getResultSet().storeResults("RESULT_" + optionsAndResults.getCameraPosition() + ".nir.",
							rt, getBlockPosition());
			}
			return io.getImage();
		} else
			return null;
	}
	
	@Override
	protected Image processIRmask() {
		Image irSkel = null;
		// getProperties().getImage("ir_skeleton");
		if (input().masks().ir() != null)
			irSkel = input().masks().ir().io().skeletonize().getImage();
		if (irSkel != null) {
			int irSkeletonFilledPixels = irSkel.io().countFilledPixels();
			double irSkeletonIntensitySum = irSkel.io().intensitySumOfChannel(false, true, false, false);
			double avgIrSkel = 1 - irSkeletonIntensitySum / irSkeletonFilledPixels;
			getResultSet().setNumericResult(getBlockPosition(),
					"RESULT_" + optionsAndResults.getCameraPosition() + ".ir.skeleton.intensity.average", avgIrSkel, null);
		}
		
		if (input().masks().ir() != null) {
			ImageOperation io = new ImageOperation(input().masks().ir());
			if (input().masks().ir().getHeight() > 1) {
				int irFilledPixels = input().masks().ir().io().countFilledPixels();
				double irIntensitySum = input().masks().ir().io().intensitySumOfChannel(false, true, false, false);
				double avgIr = 1 - irIntensitySum / irFilledPixels;
				getResultSet().setNumericResult(getBlockPosition(),
						"RESULT_" + optionsAndResults.getCameraPosition() + ".ir.intensity.average", avgIr, null);
				ResultsTableWithUnits rt = io.intensity(20).calculateHistorgram(markerDistanceHorizontally,
						optionsAndResults.getREAL_MARKER_DISTANCE(), Mode.MODE_IR_ANALYSIS, addHistogramValues); // markerDistanceHorizontally
				
				if (optionsAndResults == null)
					System.err.println(SystemAnalysis.getCurrentTime() + ">SEVERE INTERNAL ERROR: OPTIONS IS NULL!");
				if (rt != null)
					getResultSet().storeResults("RESULT_" + optionsAndResults.getCameraPosition() + ".ir.",
							rt, getBlockPosition());
			}
			return io.getImage();
		} else
			return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
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
		return "Calculate Color and Intensity Histograms";
	}
	
	@Override
	public String getDescription() {
		return "Calculates overall properties of the vis, fluo and nir images, such as number of pixels, intensities, NDVI and more.";
	}
}

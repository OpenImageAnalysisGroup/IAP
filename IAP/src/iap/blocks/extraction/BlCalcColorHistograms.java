package iap.blocks.extraction;

import iap.blocks.auto.BlAdaptiveSegmentationFluo;
import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashSet;

import org.SystemAnalysis;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.fluoop.FluoAnalysis;
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
 */
public class BlCalcColorHistograms extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
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
			
			int regions = 5;
			if (calculateValuesAlsoForDifferentRegions) {
				if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE) {
					for (int r = 0; r < regions; r++)
						processVisibleImage(io.getBottom(r, regions).show("Side Part " + r + "/" + regions, debugRegionParts),
								optionsAndResults.getCameraPosition(),
								"section_" + (r + 1) + "_" + regions, true);
				}
				if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
					for (int r = 0; r < regions; r++)
						processVisibleImage(io.getInnerCircle(r, regions).show("Top Part " + r + "/" + regions, debugRegionParts),
								optionsAndResults.getCameraPosition(), "section_" + (r + 1) + "_" + regions, true);
				}
			}
			
			processVisibleImage(io, optionsAndResults.getCameraPosition(), null, false);
			
			return input().masks().vis();
		} else
			return null;
	}
	
	protected void processVisibleImage(ImageOperation io, CameraPosition cp, String resultPrefix, boolean isSection) {
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
				isSection ? addHistogramValuesForSections : addHistogramValues,
				getBoolean("Calculate Kurtosis Values", false), true);
		getResultSet().storeResults(cp, CameraType.VIS, resultPrefix, rt1, getBlockPosition(), this);
		{
			ResultsTableWithUnits rt = new ResultsTableWithUnits();
			rt.incrementCounter();
			rt.addValue("rgb.red", averageVisR);
			rt.addValue("rgb.green", averageVisG);
			rt.addValue("rgb.blue", averageVisB);
			getResultSet().storeResults(cp, CameraType.VIS, resultPrefix, rt, getBlockPosition(), this);
		}
		
		if (input().masks().nir() != null) {
			double nirIntensitySum = input().masks().nir().io().intensitySumOfChannel(false, true, false, false);
			int nirFilledPixels = input().masks().nir().io().countFilledPixels();
			double averageNir = 1 - nirIntensitySum / nirFilledPixels;
			// rt.addValue("ndvi.nir.intensity.mean", averageNir);
			
			double ndvi = (averageNir - averageVisR) / (averageNir + averageVisR);
			ResultsTableWithUnits rt = new ResultsTableWithUnits();
			rt.incrementCounter();
			rt.addValue("ndvi", ndvi);
			getResultSet().storeResults(cp, CameraType.MULTI, resultPrefix, rt, getBlockPosition(), this);
		}
		
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
						getBoolean("Add Fluo Color Bins", false),
						getBoolean("Calculate Kurtosis Values", false), false);
				if (rt != null)
					getResultSet().storeResults(optionsAndResults.getCameraPosition(), CameraType.FLUO, rt, getBlockPosition(), this);
			}
			
			{ // blue color fluo image
				Image of = getResultSet().getImage(BlAdaptiveSegmentationFluo.RESULT_OF_FLUO_INTENSITY);
				if (of == null) {
					Image resClassic, resChlorophyll, resPhenol;
					double p1 = getDouble("minimum-intensity-classic", 220);
					double p2 = getDouble("minimum-intensity-chloro", 220);
					double p3 = getDouble("minimum-intensity-phenol", 170);
					resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, p1).getImage();
					resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, p2).getImage();
					resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, p3).getImage();
					of = new Image(resClassic, resChlorophyll, resPhenol);
				}
				if (of != null) {
					of = of.io().applyMask(input().masks().fluo()).getImage().show("Blue Color Fluo Image", debug);
					ResultsTableWithUnits rt = of.io().intensity(getInt("Bin-Cnt-Fluo", 20))
							.calculateHistorgram(markerDistanceHorizontally,
									optionsAndResults.getREAL_MARKER_DISTANCE(), Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS,
									addHistogramValues,
									getBoolean("Calculate Kurtosis Values", false), false); // markerDistanceHorizontally
					if (rt != null)
						getResultSet().storeResults(optionsAndResults.getCameraPosition(), CameraType.FLUO, rt, getBlockPosition(), this);
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
					new Trait(optionsAndResults.getCameraPosition(), CameraType.NIR, "skeleton.intensity.mean"), avgNirSkel,
					null);
		}
		
		if (input().masks().nir() != null) {
			ImageOperation io = new ImageOperation(input().masks().nir());
			if (input().masks().nir().getHeight() > 1) {
				int nirFilledPixels = input().masks().nir().io().countFilledPixels();
				double nirIntensitySum = input().masks().nir().io().intensitySumOfChannel(false, true, false, false);
				double avgNir = 1 - nirIntensitySum / nirFilledPixels;
				getResultSet().setNumericResult(
						getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), CameraType.NIR, "intensity.mean"),
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
							new Trait(optionsAndResults.getCameraPosition(), CameraType.NIR, "wetness.plant_weight"), weightOfPlant,
							null);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(optionsAndResults.getCameraPosition(), CameraType.NIR, "wetness.plant_weight_drought_loss"),
							filled - weightOfPlant,
							null);
					if (filled > 0) {
						getResultSet().setNumericResult(getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), CameraType.NIR, "wetness.mean"),
								fSum / filled, null);
					} else
						getResultSet().setNumericResult(getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), CameraType.NIR, "wetness.mean"), 0d,
								null);
				}
				ResultsTableWithUnits rt = io.intensity(getInt("Bin-Cnt-NIR", 20))
						.calculateHistorgram(markerDistanceHorizontally,
								optionsAndResults.getREAL_MARKER_DISTANCE(), Mode.MODE_GRAY_NIR_ANALYSIS, addHistogramValues,
								getBoolean("Calculate Kurtosis Values", false), false); // markerDistanceHorizontally
				
				if (rt != null)
					getResultSet().storeResults(optionsAndResults.getCameraPosition(), CameraType.NIR,
							rt, getBlockPosition(), this);
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
			irSkel = input().masks().ir().io().copy().skeletonize().getImage();
		if (irSkel != null) {
			int irSkeletonFilledPixels = irSkel.io().countFilledPixels();
			double irSkeletonIntensitySum = irSkel.io().intensitySumOfChannel(false, true, false, false);
			double avgIrSkel = 1 - irSkeletonIntensitySum / irSkeletonFilledPixels;
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(optionsAndResults.getCameraPosition(), CameraType.IR, "skeleton.intensity.mean"), avgIrSkel, null);
		}
		
		if (input().masks().ir() != null) {
			ImageOperation io = new ImageOperation(input().masks().ir());
			if (input().masks().ir().getHeight() > 1) {
				int irFilledPixels = input().masks().ir().io().countFilledPixels();
				double irIntensitySum = input().masks().ir().io().intensitySumOfChannel(false, true, false, false);
				double avgIr = 1 - irIntensitySum / irFilledPixels;
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(optionsAndResults.getCameraPosition(), CameraType.IR, "intensity.mean"), avgIr, null);
				ResultsTableWithUnits rt = io.intensity(20).calculateHistorgram(markerDistanceHorizontally,
						optionsAndResults.getREAL_MARKER_DISTANCE(), Mode.MODE_IR_ANALYSIS, addHistogramValues,
						getBoolean("Calculate Kurtosis Values", false), false); // markerDistanceHorizontally
				
				if (optionsAndResults == null)
					System.err.println(SystemAnalysis.getCurrentTime() + ">SEVERE INTERNAL ERROR: OPTIONS IS NULL!");
				if (rt != null)
					getResultSet().storeResults(optionsAndResults.getCameraPosition(), CameraType.IR, rt, getBlockPosition(), this);
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
		return "Calculate Color- and Intensity-Histograms";
	}
	
	@Override
	public String getDescription() {
		return "Calculates overall properties of the vis, fluo and nir images, such as number of pixels, intensities, NDVI and more.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("hsv.normalized.s.histogram.bin", ""),
				new CalculatedProperty("hsv.normalized.s.histogram.bin", ""),
				new CalculatedProperty("hsv.normalized.s.histogram.bin", ""),
				
				new CalculatedProperty("hsv.s.histogram.bin", ""),
				new CalculatedProperty("hsv.s.histogram.bin", ""),
				new CalculatedProperty("hsv.s.histogram.bin", ""),
				
				new CalculatedProperty("hsv.h.histogram.s_avg.bin", ""),
				new CalculatedProperty("hsv.h.histogram.v_avg.bin", ""),
				new CalculatedProperty("hsv.s.histogram.h_avg.bin", ""),
				new CalculatedProperty("hsv.s.histogram.v_avg.bin", ""),
				new CalculatedProperty("hsv.v.histogram.h_avg.bin", ""),
				new CalculatedProperty("hsv.v.histogram.s_avg.bin", ""),
				
				new CalculatedProperty("hsv.h.mean",
						"The plant average hue in the HSV/HSB colour space. The value range is normalized to a minimum of 0 and a maximum of 1. "
								+ "Value one corresponds to non-technical descriptions of 360 degrees for this colour space."),
				new CalculatedProperty("hsv.s.mean",
						"The plant average saturation in the HSV/HSB colour space. A high value indicates more 'intensive' colours, "
								+ "low values indicate pale colours. This value ranges from 0 to 1, other software or references may utilize "
								+ "different ranges, e.g. a maximum of 100."),
				new CalculatedProperty("hsv.v.mean",
						"The plant average brightness in the HSV/HSB colour space. This value ranges from 0 to 1, other software or references "
								+ "may utilize different ranges, e.g. a maximum of 100."),
				
				new CalculatedProperty("hsv.dgci.mean",
						"Numeric indication on how 'dark green' the plant appears, taking into account hue, saturation and brightness. Differs from "
								+ "calculation in other sources in that the higher the saturation, the assumption is that the plant appears greener, and thus "
								+ "the value is increasing in this case. The column 'side.vis.hsv.dgci_orig.mean' corresponds to the unintuitive "
								+ "but documented calculation of this trait."),
				
				new CalculatedProperty("hsv.h.yellow2green",
						"Proportion of yellow colour plant pixels (histogram bin 3) divided by the count of green colour pixels (bins 4 to 7). "
								+ "This value is only valid if the bin count has not been changed from 20, otherwise the involved bins represent different colors."),
				new CalculatedProperty("hsv.h.red2green",
						"Proportion of red colour plant pixels (histogram bins 0 and 1) divided by the count of green colour pixels (bins 4 to 7)."
								+ "This value is only valid if the bin count has not been changed from 20, otherwise the involved bins represent different colors."),
				new CalculatedProperty("hsv.h.brown2green",
						"Proportion of brown colour plant pixels (histogram bin 2) divided by the count of green colour pixels (bins 4 to 7). "
								+ "This value is only valid if the bin count has not been changed from 20, otherwise the involved bins represent different colors."),
				
				new CalculatedProperty("hsv.h.stddev",
						"The standard deviation of the hue values of the plant pixels. The lower this value, the more uniform is the plant colour."),
				new CalculatedProperty("hsv.s.stddev",
						"The standard deviation of the saturation values of the plant pixels. The lower this value, the more uniform is the "
								+ "saturation of the plant colours."),
				new CalculatedProperty("hsv.v.stddev",
						"The standard deviation of the brightness values of the plant pixels. The lower this value, the more uniform is the plant brightness."),
				
				new CalculatedProperty("hsv.h.skewness",
						"The 'skewness' of the hue values of the plant pixels. 'skewness' is a statistical term, indicating the tendency of the "
								+ "value distribution to lean to one side of the value range. The documentation will include a more complete description "
								+ "of this trait in the future; see reference literature for full details."),
				new CalculatedProperty("hsv.s.skewness",
						"The 'skewness' of the saturation values of the plant pixels. 'skewness' is a statistical term, indicating the tendency of the "
								+ "value distribution to lean to one side of the value range. The documentation will include a more complete description of "
								+ "this trait in the future; see reference literature for full details."),
				new CalculatedProperty("hsv.v.skewness",
						"The 'skewness' of the brightness values of the plant pixels. 'skewness' is a statistical term, indicating the tendency of the "
								+ "value distribution to lean to one side of the value range. The documentation will include a more complete description of "
								+ "this trait in the future; see reference literature for full details."),
				
				new CalculatedProperty("hsv.h.kurtosis",
						"The 'kurtosis' of the hue values of the plant pixels. 'kurtosis' is a statistical term, indicating the 'peakedness' "
								+ "of the value distribution. The documentation will include a more complete description of this trait in the future; "
								+ "see reference literature for full details."),
				new CalculatedProperty("hsv.s.kurtosis",
						"The 'kurtosis' of the saturation values of the plant pixels. 'kurtosis' is a statistical term, indicating the 'peakedness' "
								+ "of the value distribution. The Page 33documentation will include a more complete description of this trait in the future; "
								+ "see reference literature for full details."),
				new CalculatedProperty("hsv.v.kurtosis",
						"The 'kurtosis' of the brightness values of the plant pixels. 'kurtosis' is a statistical term, indicating the 'peakedness' "
								+ "of the value distribution. The documentation will include a more complete description of this trait in the future; "
								+ "see reference literature for full details."),
				
				new CalculatedProperty("lab.l.mean",
						"The plant average brightness value of the plant pixel colours in the L*a*b* colour space. Small values "
								+ "indicate low and high values high brightness. This value ranges from 0 to 255, other software or references "
								+ "may utilize different ranges."),
				new CalculatedProperty("lab.a.mean",
						"The plant average a-value of the plant pixel colours in the L*a*b* colour space. Small values indicate green "
								+ "while high values indicate magenta. This value ranges from 26 to 225, other software or references may "
								+ "utilize different ranges, e.g. higher negative together with higher positive values."),
				new CalculatedProperty("lab.b.mean",
						"The plant average b-value of the plant pixel colours in the L*a*b* colour space. Small values indicate blue and "
								+ "high values indicate yellow. This value ranges from 8 to 223, other software or references may utilize "
								+ "different ranges, e.g. higher negative values together with higher positive values."),
				
				new CalculatedProperty("lab.l.stddev",
						"The standard deviation of the brightness values L in the L*a*b* colour space of the plant pixels. "
								+ "The lower this value, the more uniform is the plant brightness."),
				new CalculatedProperty("lab.a.stddev",
						"The standard deviation of the a-values in the L*a*b* colour space of the plant pixels. "
								+ "The lower this value, the more uniform is the plant colour."),
				new CalculatedProperty("lab.b.stddev",
						"The standard deviation of the b values in the L*a*b* colour space of the plant pixels. "
								+ "The lower this value, the more uniform is the plant colour"),
				
				new CalculatedProperty("lab.l.skewness",
						"The 'skewness' of the brightness values L in the L*a*b* colour space of the plant pixels. 'skewness' "
								+ "is a statistical term, indicating the tendency of the value distribution to lean to one side of the value range."),
				new CalculatedProperty("lab.a.skewness",
						"The 'skewness' of the a-values in the L*a*b* colour space of the plant pixels. 'skewness' is a "
								+ "statistical term, indicating the tendency of the value distribution to lean to one side of the value range. "),
				new CalculatedProperty("lab.b.skewness",
						"The 'skewness' of the b values in the L*a*b* colour space of the plant pixels. 'skewness' is a statistical term, "
								+ "indicating the tendency of the value distribution to lean to one side of the value range."),
				
				new CalculatedProperty("lab.l.kurtosis",
						"The 'kurtosis' of the brightness values L in the L*a*b* colour space of the plant pixels. 'kurtosis' is a "
								+ "statistical term, indicating the 'peakedness' of the value distribution. "),
				new CalculatedProperty("lab.a.kurtosis",
						"The 'kurtosis' of the a-values in the L*a*b* colour space of the plant pixels. 'kurtosis' is a statistical term, "
								+ "indicating the 'peakedness' of the value distribution. The documentation will include a more complete "
								+ "description of this trait in thefuture; see reference literature for full details."),
				new CalculatedProperty("lab.b.kurtosis",
						"The 'kurtosis' of the b-values in the L*a*b* colour space of the plant pixels. 'kurtosis' is a statistical term, "
								+ "indicating the 'peakedness' of the value distribution. "),
				new CalculatedProperty("intensity.phenol.plant_weight", ""),
				new CalculatedProperty("intensity.phenol.plant_weight_drought_loss", ""),
				
				new CalculatedProperty("filled.pixels",
						"Number of plant pixels."),
				new CalculatedProperty("filled.percent",
						"Number of plant pixels divided by number of overall pixels in the near-infrared image. If half of the "
								+ "image is filled by the plant, the value would be 0.5. If 10% of the image is filled by the plant, the "
								+ "value would be 0.1. Excel table display may be formatted to show percentage values (ranging from 0 to 100)."),
				
				new CalculatedProperty("intensity.stddev", ""),
				new CalculatedProperty("intensity.skewness", ""),
				new CalculatedProperty("intensity.kurtosis", ""),
				
				new CalculatedProperty("intensity.chlorophyl.sum", ""),
				new CalculatedProperty("intensity.chlorophyl.mean",
						"A relative indicator of the red fluorescence intensity, not taking into account brightness but only "
								+ "the color hue (red = highest intensity, yellow = no intensity). Detailed information will "
								+ "be added to the documentation. "),
				new CalculatedProperty("intensity.phenol.sum",
						"The sum of the yellow fluorescence intensities of each pixel, calculated as described for 'intensity.phenol.mean'."),
				new CalculatedProperty("intensity.phenol.mean",
						"A relative indicator of the yellow fluorescence intensity, not taking into account brightness but only the color hue "
								+ "(red = no intensity, yellow = high intensity). Detailed information will be added to the documentation. "),
				new CalculatedProperty("intensity.classic.sum", ""),
				new CalculatedProperty("intensity.classic.mean",
						"A relative indicator of the red fluorescence intensity, taking into account brightness and colour hue "
								+ "(red = highest intensity, yellow = no intensity, bright = high intensity, dark = low intensity). "
								+ "Detailed information will be added to the documentation. Calculation formula: ( 1 - red / (255 + green) ) / 0.825"),
				new CalculatedProperty("intensity.phenol.chlorophyl.ratio",
						"The ratio of trait 'intensity.chlorophyl.sum' and 'intensity.phenol.sum'."),
				new CalculatedProperty("intensity.sum", ""),
				new CalculatedProperty("intensity.mean",
						"Deprecated. The same as 'intensity.chlorophyl.mean'. Trait may be removed at a later time point."),
				
				new CalculatedProperty("normalized.histogram.bin", ""),
				new CalculatedProperty("histogram.bin", ""),
				
				new CalculatedProperty("normalized.histogram.phenol.bin", ""),
				new CalculatedProperty("histogram.phenol.bin", ""),
				
				new CalculatedProperty("ndvi", "ndvi = (averageNir - averageVisR) / (averageNir + averageVisR))"),
				
				new CalculatedProperty("rgb.red.intensity.mean",
						"Average intensity of the red channel of the plant pixels in the visible light image."),
				new CalculatedProperty("rgb.green.intensity.mean",
						"Average intensity of the green channel of the plant pixels in the visible light image."),
				new CalculatedProperty("rgb.blue.intensity.mean",
						"Average intensity of the blue channel of the plant pixels in the visible light image.")
		
		};
	}
}

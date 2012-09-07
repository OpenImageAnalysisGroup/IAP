package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import org.SystemAnalysis;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.operations.intensity.Histogram;
import de.ipk.ag_ba.image.operations.intensity.Histogram.Mode;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Calculates overall properties of the vis, fluo and nir images, such as number of pixels, intensities, NDVI and more.
 * Is used in the current pipelines for maize and barley.
 * Does not need any input parameters.
 * 
 * @author klukas, pape
 *         status: ok, 23.11.2011, c. klukas
 */
public class BlCalcIntensity_vis_fluo_nir_ir extends AbstractSnapshotAnalysisBlockFIS {
	
	private final boolean debugRegionParts = false;
	
	private final boolean calculateValuesAlsoForDifferentRegions = true;
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	BlockProperty markerDistanceHorizontally = null;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		if (getProperties() != null && getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT) != null)
			markerDistanceHorizontally = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() != null) {
			
			ImageOperation io = new ImageOperation(input().masks().vis().copy()).print("BEFORE TRIMM", false).erode(2);
			io = input().masks().vis().copy().io().applyMask_ResizeSourceIfNeeded(io.getImage(), ImageOperation.BACKGROUND_COLORint)
					.print("AFTER ERODE", false);
			
			String pre = "RESULT_" + options.getCameraPosition();
			int regions = 5;
			if (calculateValuesAlsoForDifferentRegions) {
				if (options.getCameraPosition() == CameraPosition.SIDE) {
					for (int r = 0; r < regions; r++)
						processVisibleImage(io.getBottom(r, regions).print("Side Part " + r + "/" + regions, debugRegionParts),
								pre + ".section_" + (r + 1) + "_" + regions + ".");
				}
				if (options.getCameraPosition() == CameraPosition.TOP) {
					for (int r = 0; r < regions; r++)
						processVisibleImage(io.getInnerCircle(r, regions).print("Top Part " + r + "/" + regions, debugRegionParts),
								pre + ".section_" + (r + 1) + "_" + regions + ".");
				}
			}
			processVisibleImage(io, pre + ".");
			
			return input().masks().vis();
		} else
			return null;
	}
	
	protected void processVisibleImage(ImageOperation io, String resultPrefix) {
		int visibleFilledPixels = io.countFilledPixels();
		
		double visibleIntensitySumR = io.intensitySumOfChannel(false, true, false, false);
		double visibleIntensitySumG = io.intensitySumOfChannel(false, false, true, false);
		double visibleIntensitySumB = io.intensitySumOfChannel(false, false, false, true);
		double averageVisR = visibleIntensitySumR / visibleFilledPixels;
		double averageVisG = visibleIntensitySumG / visibleFilledPixels;
		double averageVisB = visibleIntensitySumB / visibleFilledPixels;
		
		ResultsTableWithUnits rt1 = io.intensity(20).calculateHistorgram(markerDistanceHorizontally,
				options.getIntSetting(Setting.REAL_MARKER_DISTANCE), Histogram.Mode.MODE_HUE_VIS_ANALYSIS);
		getProperties().storeResults(resultPrefix + ".vis.", rt1, getBlockPosition());
		
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
		
		getProperties().storeResults(resultPrefix, rt, getBlockPosition());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() != null) {
			ImageOperation io = new ImageOperation(input().masks().fluo().copy()).print("BEFORE TRIMM", false).
					erode(2);
			io = input().masks().fluo().copy().io().applyMask_ResizeSourceIfNeeded(io.getImage(), ImageOperation.BACKGROUND_COLORint)
					.print("AFTER ERODE", false);
			ResultsTableWithUnits rt = io.intensity(20).calculateHistorgram(markerDistanceHorizontally,
					options.getIntSetting(Setting.REAL_MARKER_DISTANCE), Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS); // markerDistanceHorizontally
			if (rt != null)
				getProperties().storeResults("RESULT_" + options.getCameraPosition() + ".fluo.", rt, getBlockPosition());
			return input().masks().fluo();// io.getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage nirSkel = getProperties().getImage("nir_skeleton");
		if (nirSkel != null) {
			int nirSkeletonFilledPixels = nirSkel.io().countFilledPixels();
			double nirSkeletonIntensitySum = nirSkel.io().intensitySumOfChannel(false, true, false, false);
			double avgNirSkel = 1 - nirSkeletonIntensitySum / nirSkeletonFilledPixels;
			getProperties().setNumericProperty(getBlockPosition(),
					"RESULT_" + options.getCameraPosition() + ".nir.skeleton.intensity.average", avgNirSkel,
					null);
		}
		
		if (input().masks().nir() != null) {
			ImageOperation io = new ImageOperation(input().masks().nir());
			if (input().masks().nir().getHeight() > 1) {
				int nirFilledPixels = input().masks().nir().io().countFilledPixels();
				double nirIntensitySum = input().masks().nir().io().intensitySumOfChannel(false, true, false, false);
				double avgNir = 1 - nirIntensitySum / nirFilledPixels;
				getProperties().setNumericProperty(
						getBlockPosition(), "RESULT_" + options.getCameraPosition() + ".nir.intensity.average",
						avgNir, null);
				boolean wetnessAnalysis = false;
				if (wetnessAnalysis) {
					int[] nirImg = input().masks().nir().getAs1A();
					int filled = 0;
					double fSum = 0;
					int b = ImageOperation.BACKGROUND_COLORint;
					double weightOfPlant = 0; // fully wet: 1 unit, fully dry: 1/7 unit
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
							weightOfPlant += 1 / 7d + (1 - 1 / 7d) * realF / 100d;
						}
					}
					getProperties().setNumericProperty(getBlockPosition(),
							"RESULT_" + options.getCameraPosition() + ".nir.wetness.plant_weight", weightOfPlant,
							null);
					getProperties().setNumericProperty(getBlockPosition(), "RESULT_" + options.getCameraPosition() + ".nir.wetness.plant_weight_drought_loss",
							filled - weightOfPlant,
							null);
					if (filled > 0) {
						getProperties().setNumericProperty(getBlockPosition(), "RESULT_" + options.getCameraPosition() + ".nir.wetness.average", fSum / filled, null);
					} else
						getProperties().setNumericProperty(getBlockPosition(), "RESULT_" + options.getCameraPosition() + ".nir.wetness.average", 0d, null);
				}
				ResultsTableWithUnits rt = io.intensity(20).calculateHistorgram(markerDistanceHorizontally,
						options.getIntSetting(Setting.REAL_MARKER_DISTANCE), Mode.MODE_GRAY_NIR_ANALYSIS); // markerDistanceHorizontally
				
				if (options == null)
					System.err.println(SystemAnalysis.getCurrentTime() + ">SEVERE INTERNAL ERROR: OPTIONS IS NULL!");
				if (rt != null)
					getProperties().storeResults("RESULT_" + options.getCameraPosition() + ".nir.",
							rt, getBlockPosition());
			}
			return io.getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processIRmask() {
		FlexibleImage irSkel = null;
		// getProperties().getImage("ir_skeleton");
		if (input().masks().ir() != null)
			irSkel = input().masks().ir().io().skeletonize().getImage();
		if (irSkel != null) {
			int irSkeletonFilledPixels = irSkel.io().countFilledPixels();
			double irSkeletonIntensitySum = irSkel.io().intensitySumOfChannel(false, true, false, false);
			double avgIrSkel = 1 - irSkeletonIntensitySum / irSkeletonFilledPixels;
			getProperties().setNumericProperty(getBlockPosition(),
					"RESULT_" + options.getCameraPosition() + ".ir.skeleton.intensity.average", avgIrSkel, null);
		}
		
		if (input().masks().ir() != null) {
			ImageOperation io = new ImageOperation(input().masks().ir());
			if (input().masks().ir().getHeight() > 1) {
				int irFilledPixels = input().masks().ir().io().countFilledPixels();
				double irIntensitySum = input().masks().ir().io().intensitySumOfChannel(false, true, false, false);
				double avgIr = 1 - irIntensitySum / irFilledPixels;
				getProperties().setNumericProperty(getBlockPosition(),
						"RESULT_" + options.getCameraPosition() + ".ir.intensity.average", avgIr, null);
				ResultsTableWithUnits rt = io.intensity(20).calculateHistorgram(markerDistanceHorizontally,
						options.getIntSetting(Setting.REAL_MARKER_DISTANCE), Mode.MODE_IR_ANALYSIS); // markerDistanceHorizontally
				
				if (options == null)
					System.err.println(SystemAnalysis.getCurrentTime() + ">SEVERE INTERNAL ERROR: OPTIONS IS NULL!");
				if (rt != null)
					getProperties().storeResults("RESULT_" + options.getCameraPosition() + ".ir.",
							rt, getBlockPosition());
			}
			return io.getImage();
		} else
			return null;
	}
	
	@Override
	public Parameter[] getParameters() {
		// no parameters are needed
		return new Parameter[] {};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		super.setParameters(params);
	}
}

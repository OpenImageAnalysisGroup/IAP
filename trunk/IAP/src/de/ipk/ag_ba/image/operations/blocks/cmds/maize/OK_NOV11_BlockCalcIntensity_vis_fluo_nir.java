package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import org.graffiti.plugin.parameter.Parameter;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

/**
 * Calculates overall properties of the vis, fluo and nir images, such as number of pixels, intensities, NDVI and more.
 * 
 * Is used in the current pipelines for maize and barley.
 * 
 * Does not need any input parameters.
 * 
 * @author klukas, pape
 * 
 * status: ok, 23.11.2011, c. klukas
 */
public class OK_NOV11_BlockCalcIntensity_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	private int visibleFilledPixels, nirFilledPixels, nirSkeletonFilledPixels;
	BlockProperty markerDistanceHorizontally = null;
	
	@Override
	protected void prepare() {
		super.prepare();
		if (getInput().getMasks().getVis() != null)
			this.visibleFilledPixels = getInput().getMasks().getVis().getIO().countFilledPixels();
		
		if (getInput().getMasks().getNir() != null)
			this.nirFilledPixels = getInput().getMasks().getNir().getIO().countFilledPixels();
		
		if (getProperties().getImage("nir_skeleton") != null)
			this.nirSkeletonFilledPixels = getProperties().getImage("nir_skeleton").getIO().countFilledPixels();
		
		if (getProperties() != null && getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT) != null)
			markerDistanceHorizontally = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
	}
	
	/**
	 * ndvi [-1-1]
	 */
	@Override
	protected FlexibleImage processVISmask() {
		
		if (getInput().getMasks().getVis() != null) {
			
			ImageOperation io = new ImageOperation(getInput().getMasks().getVis());
			double visibleIntensitySumR = io.intensitySumOfChannel(false, true, false, false);
			double visibleIntensitySumG = io.intensitySumOfChannel(false, false, true, false);
			double visibleIntensitySumB = io.intensitySumOfChannel(false, false, false, true);
			double averageVisR = visibleIntensitySumR / visibleFilledPixels;
			double averageVisG = visibleIntensitySumG / visibleFilledPixels;
			double averageVisB = visibleIntensitySumB / visibleFilledPixels;
			
			ResultsTable rt = new ResultsTable();
			rt.incrementCounter();
			rt.addValue("ndvi.vis.red.intensity.average", averageVisR);
			rt.addValue("ndvi.vis.green.intensity.average", averageVisG);
			rt.addValue("ndvi.vis.blue.intensity.average", averageVisB);
			
			if (getInput().getMasks().getNir() != null) {
				double nirIntensitySum = getInput().getMasks().getNir().getIO().intensitySumOfChannel(false, true, false, false);
				double averageNir = 1 - nirIntensitySum / nirFilledPixels;
				// rt.addValue("ndvi.nir.intensity.average", averageNir);
				
				double ndvi = (averageNir - averageVisR) / (averageNir + averageVisR);
				rt.addValue("ndvi", ndvi);
			}
			
			getProperties().storeResults("RESULT_" + options.getCameraPosition() + ".", rt, getBlockPosition());
			return getInput().getMasks().getVis();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() != null) {
			ImageOperation io = new ImageOperation(getInput().getMasks().getFluo());
			ResultsTable rt = io.intensity(10).calculateHistorgram(markerDistanceHorizontally,
						options.getIntSetting(Setting.REAL_MARKER_DISTANCE), true); // markerDistanceHorizontally
			if (rt != null)
				getProperties().storeResults("RESULT_" + options.getCameraPosition() + ".fluo.", rt, getBlockPosition());
			return io.getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		
		if (getProperties().getImage("nir_skeleton") != null) {
			double nirSkeletonIntensitySum = getProperties().getImage("nir_skeleton").getIO().intensitySumOfChannel(false, true, false, false);
			double avgNirSkel = 1 - nirSkeletonIntensitySum / nirSkeletonFilledPixels;
			getProperties().setNumericProperty(getBlockPosition(), "RESULT_" + options.getCameraPosition() + ".nir.skeleton.intensity.average", avgNirSkel);
		}
		
		if (getInput().getMasks().getNir() != null) {
			ImageOperation io = new ImageOperation(getInput().getMasks().getNir());
			if (getInput().getMasks().getNir().getHeight() > 1) {
				
				double nirIntensitySum = getInput().getMasks().getNir().getIO().intensitySumOfChannel(false, true, false, false);
				double avgNir = 1 - nirIntensitySum / nirFilledPixels;
				getProperties().setNumericProperty(getBlockPosition(), "RESULT_" + options.getCameraPosition() + ".nir.intensity.average", avgNir);
				
				int[] nirImg = getInput().getMasks().getNir().getAs1A();
				int filled = 0;
				double fSum = 0;
				int b = ImageOperation.BACKGROUND_COLORint;
				for (int x : nirImg) {
					// Feuchtigkeit (%) = -7E-05x^3 + 0,0627x^2 - 15,416x + 1156,1 // Formel: E-Mail Alex 10.8.2011
					if (x != b) {
						double f = -7E-05 * x * x * x + 0.0627 * x * x - 15.416 * x + 1156.1;
						if (f < 0)
							f = 0;
						if (f > 100)
							f = 100;
						fSum += f;
					}
				}
				if (filled > 0) {
					getProperties().setNumericProperty(getBlockPosition(), "RESULT_" + options.getCameraPosition() + ".nir.wetness.avg", fSum / filled);
				} else
					getProperties().setNumericProperty(getBlockPosition(), "RESULT_" + options.getCameraPosition() + ".nir.wetness.avg", 0);
				ResultsTable rt = io.intensity(10).calculateHistorgram(markerDistanceHorizontally,
						options.getIntSetting(Setting.REAL_MARKER_DISTANCE), false); // markerDistanceHorizontally
				
				if (options == null)
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">SEVERE INTERNAL ERROR: OPTIONS IS NULL!");
				if (rt != null)
					getProperties().storeResults("RESULT_" + options.getCameraPosition() + ".nir.", rt, getBlockPosition());
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

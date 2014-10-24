package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashMap;
import java.util.TreeMap;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

final class OptionsGenerator1 implements OptionsGenerator {
	/**
	 * 
	 */
	private final AbstractPhenotypingTask abstractPhenotypingTask;
	private final ImageData inIr;
	private final ImageSet id;
	private final TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> plantResults;
	private final ImageData inFluo;
	private final ImageData inNir;
	private final String configAndAngle;
	private final ImageData inVis;
	private final TreeMap<String, HashMap<String, BlockResultSet>> previousResultsForThisTimePoint;
	
	OptionsGenerator1(AbstractPhenotypingTask abstractPhenotypingTask, ImageData inIr, ImageSet id,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> plantResults, ImageData inFluo,
			ImageData inNir, String configAndAngle, ImageData inVis, TreeMap<String, HashMap<String, BlockResultSet>> previousResultsForThisTimePoint) {
		this.abstractPhenotypingTask = abstractPhenotypingTask;
		this.inIr = inIr;
		this.id = id;
		this.plantResults = plantResults;
		this.inFluo = inFluo;
		this.inNir = inNir;
		this.configAndAngle = configAndAngle;
		this.inVis = inVis;
		this.previousResultsForThisTimePoint = previousResultsForThisTimePoint;
	}
	
	@Override
	public ImageSet getImageSet() {
		final ImageSet input = new ImageSet();
		
		input.setImageInfo(inVis, inFluo, inNir, inIr);
		return input;
	}
	
	@Override
	public ImageSet getMaskSet() {
		final ImageSet inputMasks = new ImageSet();
		
		inputMasks.setImageInfo(inVis, inFluo, inNir, inIr);
		return inputMasks;
	}
	
	@Override
	public ImageProcessorOptionsAndResults getOptions() {
		ImageProcessorOptionsAndResults options = new ImageProcessorOptionsAndResults(this.abstractPhenotypingTask.pd.getOptions(),
				previousResultsForThisTimePoint, plantResults);
		options.setConfigAndAngle(configAndAngle);
		options.setUnitTestInfo(this.abstractPhenotypingTask.unit_test_idx, this.abstractPhenotypingTask.unit_test_steps);
		
		options.forceDebugStack = this.abstractPhenotypingTask.forceDebugStack;
		options.forcedDebugStacks = this.abstractPhenotypingTask.forcedDebugStacks;
		
		options.databaseTarget = this.abstractPhenotypingTask.databaseTarget;
		options.setCustomNullBlockPrefix("Separate Settings");
		
		{
			boolean processEarlyTimes = options.getBooleanSetting(null, "Early//Custom settings for early timepoints", false);
			boolean processLateTimes = options.getBooleanSetting(null, "Late//Custom settings for late timepoints", false);
			int earlyTimeUntilDayX = options.getIntSetting(null, "Early//Early time until time point", -1);
			int lateTimeUntilDayX = options.getIntSetting(null, "Late//Late time until time point", -1);
			String timeInfo = null;
			if (processEarlyTimes && id.getAnyInfo().getParentSample().getTime() <= earlyTimeUntilDayX)
				timeInfo = "early";
			else
				if (processLateTimes && id.getAnyInfo().getParentSample().getTime() >= lateTimeUntilDayX)
					timeInfo = "late";
			
			String info = id.getAnyInfo().getParentSample().getParentCondition().getParentSubstance().getInfo();
			if (id.isSideImage())
				options.setCameraInfos(CameraPosition.SIDE,
						info != null && options.getBooleanSetting(null, info + "//Custom settings", false) ? info : null, timeInfo, id.getAnyInfo()
								.getPosition());
			else
				options.setCameraInfos(CameraPosition.TOP,
						info != null && options.getBooleanSetting(null, info + "//Custom settings", false) ? info : null, timeInfo, id.getAnyInfo()
								.getPosition());
			options.setCustomNullBlockPrefix(null);
		}
		
		if (this.abstractPhenotypingTask.forceDebugStack) {
			this.abstractPhenotypingTask.setDebugLastSystemOptionStorageGroup(options.getSystemOptionStorageGroup(null));
		}
		return options;
	}
}
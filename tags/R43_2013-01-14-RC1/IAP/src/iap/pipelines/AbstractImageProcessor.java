/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package iap.pipelines;

import iap.blocks.data_structures.ImageAnalysisBlockFIS;

import java.util.HashMap;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author pape, klukas, entzian
 */
public abstract class AbstractImageProcessor implements ImageProcessor {
	
	private final HashMap<Integer, BlockResultSet> settings;
	private int[] debugValidTrays;
	
	public AbstractImageProcessor() {
		this(new HashMap<Integer, BlockResultSet>());
	}
	
	public AbstractImageProcessor(HashMap<Integer, BlockResultSet> settings) {
		this.settings = settings;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.image.analysis.maize.ImageProcessor#pipeline(de.ipk.ag_ba.image.structures.FlexibleImageSet,
	 * de.ipk.ag_ba.image.structures.FlexibleImageSet, int, de.ipk.ag_ba.image.structures.FlexibleImageStack, boolean, boolean)
	 */
	@Override
	public HashMap<Integer, StringAndFlexibleMaskAndImageSet> pipeline(
			ImageProcessorOptions options,
			FlexibleImageSet input,
			FlexibleImageSet optInputMasks,
			int maxThreadsPerImage,
			HashMap<Integer, FlexibleImageStack> debugStack)
			throws Exception {
		BlockPipeline pipeline = getPipeline(options);
		pipeline.setValidTrays(debugValidTrays);
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, optInputMasks != null ? optInputMasks : input);
		
		HashMap<Integer, StringAndFlexibleMaskAndImageSet> result = pipeline.execute(options, workset, debugStack, settings, getStatus());
		
		if (debugStack != null)
			for (Integer key : debugStack.keySet()) {
				StringAndFlexibleMaskAndImageSet sai = result.get(key);
				debugStack.get(key).addImage("RESULT", sai.getMaskAndImageSet().getOverviewImage(
						SystemOptions.getInstance().getInteger("IAP", "Debug-Overview-Image-Width", 1680)), sai.getSettings());
			}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.image.analysis.maize.ImageProcessor#getSettings()
	 */
	@Override
	public HashMap<Integer, BlockResultSet> getSettings() {
		return settings;
	}
	
	@Override
	public abstract BlockPipeline getPipeline(ImageProcessorOptions options);
	
	@Override
	public TreeMap<Long, HashMap<Integer, BlockResultSet>> postProcessPipelineResults(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData2,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> analysisResults,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			ImageProcessorOptions options) throws InstantiationException,
			IllegalAccessException, InterruptedException {
		BlockPipeline pipeline = getPipeline(options);
		return pipeline.postProcessPipelineResultsForAllAngles(
				plandID2time2waterData2,
				inSample,
				inImages,
				analysisResults,
				optStatus);
	}
	
	@Override
	public void setValidTrays(int[] debugValidTrays) {
		this.debugValidTrays = debugValidTrays;
	}
	
	@SuppressWarnings("unchecked")
	protected BlockPipeline getPipelineFromBlockList(
			SystemOptions so,
			String[] defaultBlockList) {
		defaultBlockList = so.getStringAll(
				"Analysis Blocks",
				"block",
				defaultBlockList);
		
		BlockPipeline p = new BlockPipeline();
		if (defaultBlockList != null)
			for (String b : defaultBlockList) {
				if (b != null && !b.startsWith("#")) {
					try {
						Class<?> c = Class.forName(b);
						if (ImageAnalysisBlockFIS.class.isAssignableFrom(c))
							p.add((Class<? extends ImageAnalysisBlockFIS>) c);
						else
							System.out.println("WARNING: ImageAnalysisBlock " + b + " is not assignable to " + ImageAnalysisBlockFIS.class.getCanonicalName()
									+ "! (block is not added to pipeline!)");
					} catch (ClassNotFoundException cnfe) {
						System.out.println("ERROR: ImageAnalysisBlock " + b + " is unknown! (start block name with '#' to disable a specific block)");
					}
				}
			}
		return p;
	}
}
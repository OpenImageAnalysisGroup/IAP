package de.ipk.ag_ba.image.operations.blocks;

import java.util.ArrayList;

import de.ipk.ag_ba.image.analysis.phytochamber.PhytoTopImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.cmds.ImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public class BlockPipeline {
	
	private final ArrayList<Class<? extends ImageAnalysisBlockFIS>> blocks = new ArrayList<Class<? extends ImageAnalysisBlockFIS>>();
	private final PhytoTopImageProcessorOptions options;
	
	public BlockPipeline(PhytoTopImageProcessorOptions options) {
		this.options = options;
		
	}
	
	public void add(Class<? extends ImageAnalysisBlockFIS> blockClass) {
		blocks.add(blockClass);
	}
	
	public FlexibleMaskAndImageSet execute(FlexibleMaskAndImageSet input, FlexibleImageStack debugStack) throws InstantiationException, IllegalAccessException {
		// System.out.println("Execute BLOCK pipeline...");
		System.out.print(".");
		long a = System.currentTimeMillis();
		nullPointerCheck(input, "PIPELINE INPUT ");
		
		BlockProperties settings = new BlockPropertiesImpl();
		
		int index = 0;
		for (Class<? extends ImageAnalysisBlockFIS> blockClass : blocks) {
			ImageAnalysisBlockFIS block = blockClass.newInstance();
			
			block.setInputAndOptions(input, options, settings, index++, debugStack);
			
			nullPointerCheck(input, "INPUT for " + blockClass.getSimpleName());
			
			input = block.process();
			
			nullPointerCheck(input, "OUTPUT of " + blockClass.getSimpleName());
			
			block.reset();
		}
		long b = System.currentTimeMillis();
		System.out.println("PIPELINE execution time: " + (b - a) / 1000 + "s");
		if (settings.getNumberOfBlocksWithPropertyResults() > 0)
			System.out.println("Results:\n" + settings.toString());
		return input;
	}
	
	private void nullPointerCheck(FlexibleMaskAndImageSet input, String name) {
		if (input.getImages() != null) {
			if (input.getImages().getVis() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (vis)!");
			if (input.getImages().getFluo() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (fluo)!");
			if (input.getImages().getNir() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (nir)!");
		}
		if (input.getMasks() != null) {
			if (input.getMasks().getVis() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (vis)!");
			if (input.getMasks().getFluo() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (fluo)!");
			if (input.getMasks().getNir() == null)
				System.out.println("WARNING: BLOCK " + name + " is NULL image (nir)!");
		}
	}
}

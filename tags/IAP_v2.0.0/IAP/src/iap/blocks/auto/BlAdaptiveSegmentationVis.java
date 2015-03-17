package iap.blocks.auto;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import ij.process.AutoThresholder;
import ij.process.AutoThresholder.Method;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas, pape
 */
public class BlAdaptiveSegmentationVis extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected synchronized Image processVISmask() {
		if (input().masks().vis() == null) {
			return null;
		}
		
		ImageOperation io = input().masks().vis().io();
		ImageOperation filter = io.copy();
		filter = filter.replaceColor(ImageOperation.BACKGROUND_COLORint, Color.WHITE.getRGB()).getG().show("Input For Auto-Threshold", debugValues);
		ArrayList<String> possibleValues = new ArrayList<String>(Arrays.asList(AutoThresholder.getMethods()));
		String methodName = optionsAndResults.getStringSettingRadio(this, "Thresholding Method", "RenyiEntropy", possibleValues);
		Method[] methods = Method.values();
		Method method = null;
		for (Method m : methods)
			if (methodName.equalsIgnoreCase(m.name()))
				method = m;
		
		filter = filter.autoThresholdingColorImageByUsingBrightnessMaxEntropy(false, method, debugValues).getImage()
				.show("Result Filter", debugValues).io();
		io = io.applyMask(filter.getImage()).show("USED FOR CALC", debugValues);
		
		return io.getImage();
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
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Auto-tuning VIS-Segmentation";
	}
	
	@Override
	public String getDescription() {
		return "Detects the noise level, background and foreground levels to segment the plant from the background (Visible).";
	}
}

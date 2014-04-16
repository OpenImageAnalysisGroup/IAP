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
import de.ipk.ag_ba.image.operation.fluoop.FluoAnalysis;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Not needed if BlRemoveBackground is used and if a reference image is available, as this already is performing a thresholding, which works fine in most cases.
 * 
 * @author klukas
 */
public class BlAdaptiveSegmentationFluo extends AbstractSnapshotAnalysisBlock {
	
	public static final String RESULT_OF_FLUO_INTENSITY = "result of fluo intensity";
	private boolean auto_tune, auto_tune_process_red_by_green;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.auto_tune = getBoolean("Auto-Tune", true);
		this.auto_tune_process_red_by_green = getBoolean("Auto-Tune - Process Mostly Red", true);
		boolean tune = false;
		if (tune) {
			if (((int) optionsAndResults.getUnitTestIdx()) % 2 == 0)
				auto_tune_process_red_by_green = true;
			else
				auto_tune_process_red_by_green = false;
		}
		
	}
	
	@Override
	protected synchronized Image processFLUOmask() {
		if (input().masks().fluo() == null) {
			return null;
		}
		
		if (debugValues) {
			input().masks().fluo().show("inp fluo");
		}
		
		ImageOperation io = input().masks().fluo().io();
		
		Image resClassic, resChlorophyll, resPhenol;
		if (!auto_tune) {
			double p1 = getDouble("minimum-intensity-classic", 220);
			double p2 = getDouble("minimum-intensity-chloro", 220);
			double p3 = getDouble("minimum-intensity-phenol", 170);
			resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, p1).getImage();
			resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, p2).getImage();
			resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, p3).getImage();
		} else {
			ImageOperation filterInp = io.copy();
			if (auto_tune_process_red_by_green)
				filterInp = filterInp.replaceColor(optionsAndResults.getBackground(), Color.BLACK.getRGB()).getR();
			else
				filterInp = filterInp.convertFluo2intensity(FluoAnalysis.CLASSIC, 255);
			ImageOperation filter = filterInp;
			filter = filter.replaceColor(ImageOperation.BACKGROUND_COLORint, Color.WHITE.getRGB()).show("Input For Auto-Threshold", debugValues);
			ArrayList<String> possibleValues = new ArrayList<String>(Arrays.asList(AutoThresholder.getMethods()));
			String methodName = optionsAndResults.getStringSettingRadio(this, "Thresholding Method", "RenyiEntropy", possibleValues);
			Method[] methods = Method.values();
			Method method = null;
			for (Method m : methods)
				if (methodName.equalsIgnoreCase(m.name()))
					method = m;
			
			boolean tune = false;
			
			if (tune) {
				int idx = (int) optionsAndResults.getUnitTestIdx();
				idx = (int) (idx / 2d);
				method = methods[idx];
			}
			
			filter = filter.autoThresholdingColorImageByUsingBrightnessMaxEntropy(auto_tune_process_red_by_green, method, debugValues).getImage()
					.show("Result Filter", debugValues).io();
			io = io.applyMask(filter.getImage()).show("USED FOR CALC", debugValues);
			
			resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, 256).getImage();
			resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, 256).getImage();
			resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, 256).getImage();
			Image res = new Image(resClassic, resChlorophyll, resPhenol);
			res.show("res", debugValues);
		}
		Image r = new Image(resClassic, resChlorophyll, resPhenol);
		if (auto_tune && auto_tune_process_red_by_green) {
			r = r.io().show("BEFORE HUE", false).filterByHSV_hue(getDouble("Auto-Tune - Minimum Result Hue", 0.4), optionsAndResults.getBackground())
					.show("AFTER HUE", false).getImage();
		}
		getResultSet().setImage(getBlockPosition(), RESULT_OF_FLUO_INTENSITY, r, true);
		return input().images().fluo().io().applyMask(r).getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
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
		return "Auto-tuning Fluo Segmentation";
	}
	
	@Override
	public String getDescription() {
		return "Detects the noise level, background and foreground levels to segment the plant from the background (FLUO).";
	}
}

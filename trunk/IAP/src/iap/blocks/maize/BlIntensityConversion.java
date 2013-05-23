package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.FluoAnalysis;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlIntensityConversion extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected synchronized FlexibleImage processFLUOmask() {
		
		// getInput().getMasks().getFluo().copy().saveToFile(ReleaseInfo.getDesktopFolder() + File.separator + "MaizeFLUOMask2.png");
		if (input().masks().fluo() == null) {
			return null;
		}
		boolean debug = getBoolean("debug", false);
		ImageOperation io = new ImageOperation(input().masks().fluo()).applyMask_ResizeSourceIfNeeded(input().images().fluo(),
				options.getBackground());
		FlexibleImageStack fis = debug ? new FlexibleImageStack() : null;
		if (debug)
			fis.addImage("FLUO", io.copy().getImage(), null);
		double min = 220;
		FlexibleImage resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, getDouble("minimum-intensity-classic", min)).getImage();
		FlexibleImage resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, getDouble("minimum-intensity-chloro", min)).getImage();
		min = getDouble("minimum-intensity-phenol", 240);
		
		FlexibleImage resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, min).getImage();
		FlexibleImage r = new FlexibleImage(resClassic, resChlorophyll, resPhenol);
		
		if (debug) {
			fis.addImage("ClChPh", r, null);
			fis.addImage("CLASSIC", resClassic, null);
			fis.addImage("CHLORO", resChlorophyll, null);
			fis.addImage("PHENO", resPhenol, null);
			/**
			 * @see IntensityAnalysis: r_intensityClassic, g_.., b_...
			 */
			fis.show("HHH");
			// r.getIO().saveImageOnDesktop("FLUO_C_P_C.png");
		}
		
		if (!getBoolean("show conversion", false)) {
			r = io.copy().applyMask(r, options.getBackground()).getImage();
		}
		
		return r;
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		processedImages.setFluo(processedMasks.fluo());
		if (processedMasks.fluo() != null)
			processedMasks.setFluo(processedMasks.fluo().io().medianFilter32Bit().getImage());
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}

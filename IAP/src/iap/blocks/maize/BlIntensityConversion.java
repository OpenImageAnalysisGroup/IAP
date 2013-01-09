package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.FluoAnalysis;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;

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
			fis.addImage("FLUO", io.copy().getImage());
		double min = 200;
		if (options.getCameraPosition() == CameraPosition.SIDE)
			min = 220;
		boolean isOldBarley = false;
		
		if (options.isBarleyInBarleySystem()) {
			min = options.getCameraPosition() == CameraPosition.SIDE ? 225 : 160;// 188;
			
			if (options.isBarleyInBarleySystem()) {
				try {
					String db = input().images().getFluoInfo().getParentSample().getParentCondition().getExperimentHeader().getDatabase();
					if (!LemnaTecDataExchange.known(db))
						isOldBarley = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (isOldBarley && options.getCameraPosition() == CameraPosition.TOP)
				min = 200;
			if (isOldBarley && options.getCameraPosition() == CameraPosition.SIDE)
				min = 240;
		}
		if (options.isArabidopsis())
			min = 220;
		if (options.isBarley() && !options.isBarleyInBarleySystem())
			min = 230;
		
		min = unitTestChange(min);
		
		FlexibleImage resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, getDouble("minimum-intensity-classic", min)).getImage();
		FlexibleImage resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, getDouble("minimum-intensity-chloro", min)).getImage();
		if (options.isBarleyInBarleySystem())
			min = options.getCameraPosition() == CameraPosition.SIDE ?
					unitTestChange(getDouble("minimum-intensity-phenol", 225)) :
					unitTestChange(getDouble("minimum-intensity-phenol", 149));
		else
			min = getDouble("minimum-intensity-phenol", min);
		
		FlexibleImage resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, min).getImage();
		FlexibleImage r = new FlexibleImage(resClassic, resChlorophyll, resPhenol);
		
		if (debug) {
			fis.addImage("ClChPh", r);
			fis.addImage("CLASSIC", resClassic);
			fis.addImage("CHLORO", resChlorophyll);
			fis.addImage("PHENO", resPhenol);
			/**
			 * @see IntensityAnalysis: r_intensityClassic, g_.., b_...
			 */
			fis.print("HHH");
			// r.getIO().saveImageOnDesktop("FLUO_C_P_C.png");
		}
		return r;
	}
	
	private double unitTestChange(double val) {
		if (true)
			return val;
		if (val == 190 && options.getUnitTestSteps() > 0) {
			val = options.getUnitTestIdx() - 4 + val;
			System.out.println(val + " // " + options.getUnitTestIdx() + "/" + options.getUnitTestSteps());
		}
		return val;
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

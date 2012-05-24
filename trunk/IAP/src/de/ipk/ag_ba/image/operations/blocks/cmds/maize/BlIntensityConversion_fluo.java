package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.FluoAnalysis;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;

public class BlIntensityConversion_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected synchronized FlexibleImage processFLUOmask() {
		
		// getInput().getMasks().getFluo().copy().saveToFile(ReleaseInfo.getDesktopFolder() + File.separator + "MaizeFLUOMask2.png");
		if (input().masks().fluo() == null) {
			return null;
		}
		boolean debug = false;
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
			min = options.getCameraPosition() == CameraPosition.SIDE ? 225 : 188;
			
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
		
		FlexibleImage resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, min).getImage();
		FlexibleImage resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, min).getImage();
		if (isOldBarley && options.getCameraPosition() == CameraPosition.SIDE)
			min = min;
		if (options.isBarleyInBarleySystem())
			min = options.getCameraPosition() == CameraPosition.SIDE ? unitTestChange(225) : unitTestChange(149);
		
		FlexibleImage resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, min).getImage();
		FlexibleImage r = new FlexibleImage(resClassic, resChlorophyll, resPhenol);
		
		if (debug) {
			// FlexibleImage black = new FlexibleImage(new int[resChlorophyll.getWidth()][resChlorophyll.getHeight()]).getIO().invert().getImage();
			// FlexibleImage chloro = new FlexibleImage(resChlorophyll, black, resChlorophyll).getIO().gamma(0.5d).getImage();
			// FlexibleImage pheno = new FlexibleImage(black, resPhenol, black).getIO().gamma(2.0d).getImage();
			// FlexibleImage chloroCol = getColorGradient(resChlorophyll, 255); // 85
			// FlexibleImage phenoCol = getColorGradient(resPhenol, 255);
			// FlexibleImage diffChloroPheno = getDiffImg(resChlorophyll, resPhenol);
			fis.addImage("ClChPh", r);
			fis.addImage("CLASSIC", resClassic);
			fis.addImage("CHLORO", resChlorophyll);
			fis.addImage("PHENO", resPhenol);
			// fis.addImage("chloro", chloro);
			// fis.addImage("pheno", pheno);
			// fis.addImage("cloro_neu", chloroCol);
			// fis.addImage("pheno_neu", phenoCol);
			// fis.addImage("diff", diffChloroPheno);
			// try {
			// fis.saveAsLayeredTif(new File(ReleaseInfo.getDesktopFolder() + "/FLUO_C_P_C.tiff"));
			// } catch (FileNotFoundException e) {
			// e.printStackTrace();
			// }
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
	
}

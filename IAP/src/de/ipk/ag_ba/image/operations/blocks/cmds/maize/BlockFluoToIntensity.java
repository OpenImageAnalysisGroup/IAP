package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.operations.FluoAnalysis;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

public class BlockFluoToIntensity extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null) {
			return null;
		}
		ImageOperation io = new ImageOperation(getInput().getMasks().getFluo());
		FlexibleImageStack fis = new FlexibleImageStack();
		fis.addImage("FLUO", io.copy().getImage());
		FlexibleImage resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL).getImage();
		FlexibleImage resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL).getImage();
		FlexibleImage resClassic = io.convertFluo2intensity(FluoAnalysis.CLASSIC).getImage();
		FlexibleImage r = new FlexibleImage(resClassic, resChlorophyll, resPhenol);
		fis.addImage("ClChPh", r);
		fis.addImage("CHLORO", resChlorophyll);
		fis.addImage("PHENO", resPhenol);
		fis.addImage("CLASSIC", resClassic);
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
		return r;
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		processedImages.setFluo(processedMasks.getFluo());
		processedMasks.setFluo(processedMasks.getFluo().getIO().medianFilter32Bit().getImage());
	}
	
}

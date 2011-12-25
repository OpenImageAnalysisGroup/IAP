package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.FluoAnalysis;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

public class BlIntensityConversion_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null) {
			return null;
		}
		boolean debug = false;
		ImageOperation io = new ImageOperation(getInput().getMasks().getFluo());
		FlexibleImageStack fis = debug ? new FlexibleImageStack() : null;
		if (debug)
			fis.addImage("FLUO", io.copy().getImage());
		double min = 150;
		if (options.getCameraPosition() == CameraPosition.SIDE)
			min = 210;
		if (options.isBarleyInBarleySystem())
			min = 225;
		FlexibleImage resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, min).getImage();
		FlexibleImage resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, min).getImage();
		FlexibleImage resClassic = io.convertFluo2intensity(FluoAnalysis.CLASSIC, min).getImage();
		FlexibleImage r = new FlexibleImage(resClassic, resChlorophyll, resPhenol);
		if (debug) {
			// FlexibleImage black = new FlexibleImage(new int[resChlorophyll.getWidth()][resChlorophyll.getHeight()]).getIO().invert().getImage();
			// FlexibleImage chloro = new FlexibleImage(resChlorophyll, black, resChlorophyll).getIO().gamma(0.5d).getImage();
			// FlexibleImage pheno = new FlexibleImage(black, resPhenol, black).getIO().gamma(2.0d).getImage();
			// FlexibleImage chloroCol = getColorGradient(resChlorophyll, 255); // 85
			// FlexibleImage phenoCol = getColorGradient(resPhenol, 255);
			// FlexibleImage diffChloroPheno = getDiffImg(resChlorophyll, resPhenol);
			fis.addImage("ClChPh", r);
			fis.addImage("CHLORO", resChlorophyll);
			fis.addImage("PHENO", resPhenol);
			fis.addImage("CLASSIC", resClassic);
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
	
	// private FlexibleImage getDiffImg(FlexibleImage resChlorophyll, FlexibleImage resPhenol) {
	// int w = resChlorophyll.getWidth();
	// int h = resChlorophyll.getHeight();
	// int[] workimg = resChlorophyll.getAs1A();
	// int[] workimg1 = resPhenol.getAs1A();
	// int[] res = new int[w * h];
	// for (int i = 0; i < workimg.length; i++) {
	// if (workimg[i] != options.getBackground() || workimg1[i] != options.getBackground()) {
	// int rf = (workimg[i] & 0xff0000) >> 16;
	// int rf1 = (workimg1[i] & 0xff0000) >> 16;
	// int diff = Math.abs(rf - rf1);
	// res[i] = (0xFF << 24 | (diff & 0xFF) << 16) | ((diff & 0xFF) << 8) | ((diff & 0xFF) << 0);
	// }
	// }
	// return new FlexibleImage(w, h, res);
	// }
	//
	// private FlexibleImage getColorGradient(FlexibleImage resChlorophyll, int maxHsv) {
	// int hue = 0;
	// int w = resChlorophyll.getWidth();
	// int h = resChlorophyll.getHeight();
	// int[] workimg = resChlorophyll.getAs1A();
	// int[] res = new int[w * h];
	// MinMax minMax = getMinMax(workimg);
	// minMax.setMin(40);
	// minMax.setMax(130);
	// for (int i = 0; i < workimg.length; i++) {
	// if (workimg[i] != options.getBackground()) {
	// int rf = (workimg[i] & 0xff0000) >> 16;
	//
	// double hsvColMax = maxHsv;
	// hue = (int) ((rf - minMax.getMin()) * (hsvColMax / (minMax.getMax() - minMax.getMin())));
	// if (hue > hsvColMax)
	// hue = (int) hsvColMax;
	// if (hue < 0)
	// hue = 0;
	// // Color hsb = Color.getHSBColor((float) ((hsvColMax - hue) / 255d), 0.8f, 0.8f);
	// hue = 255 - hue;
	// Color hsb = Color.getHSBColor(1 / 3f, hue / 255f * hue / 255f, hue / 255f);
	// res[i] = hsb.getRGB();
	// // int hueR = hue;
	// // int hueG = (int) (hue / 2.5);
	// // int hueB = hue / 5;
	// // res[i] = (0xFF << 24 | (hueR & 0xFF) << 16) | ((hueG & 0xFF) << 8) | ((hueB & 0xFF) << 0);
	// } else
	// res[i] = Color.WHITE.getRGB();
	// }
	// return new FlexibleImage(w, h, res);
	// }
	//
	// private MinMax getMinMax(int[] workimg) {
	// MinMax minMax = new MinMax(Integer.MAX_VALUE, 0);
	// for (int i = 0; i < workimg.length; i++) {
	// if (workimg[i] != options.getBackground()) {
	// int rf = (workimg[i] & 0xff0000) >> 16;
	// if (rf < minMax.getMin())
	// minMax.setMin(rf);
	// if (rf > minMax.getMax())
	// minMax.setMax(rf);
	// }
	// }
	// return minMax;
	// }
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		processedImages.setFluo(processedMasks.getFluo());
		if (processedMasks.getFluo() != null)
			processedMasks.setFluo(processedMasks.getFluo().getIO().medianFilter32Bit().getImage());
	}
	
}

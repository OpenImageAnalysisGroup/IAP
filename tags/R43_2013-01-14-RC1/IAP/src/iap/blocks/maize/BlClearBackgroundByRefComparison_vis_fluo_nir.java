/**
 * 
 */
package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Clears the background by comparison of foreground and background.
 * Additionally the border around the masks is cleared (width 2 pixels).
 * 
 * @author pape, klukas, entzian
 */
public class BlClearBackgroundByRefComparison_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	int back;
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		back = ImageOperation.BACKGROUND_COLORint;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().images().vis() != null && input().masks().vis() == null) {
			FlexibleImage in = input().images().vis();
			FlexibleImage simulatedGreen = in.io().copy().filterByHSV(getDouble("Clear-background-vis-color-distance", 0.1), Color.GREEN.getRGB()).
					print("simulated background green", debug).getImage();
			FlexibleImage simulatedGreen2 = in
					.io()
					.copy()
					.filterByHSV(
							getDouble("Clear-background-vis-color-distance", 0.1),
							new Color(
									getInt("Clear-background-vis-color-green-R", 94),
									getInt("Clear-background-vis-color-green-G", 118),
									getInt("Clear-background-vis-color-green-B", 50)).getRGB())
					.print("simulated background green 2", debug).getImage();
			FlexibleImage simulatedBlue = in
					.io()
					.copy()
					.print("mist", debug)
					.filterByHSV(
							getDouble("Clear-background-vis-color-distance", 0.1),
							new Color(
									getInt("Clear-background-vis-color-blue-R", 20),
									getInt("Clear-background-vis-color-blue-G", 36),
									getInt("Clear-background-vis-color-blue-B", 76)).getRGB())
					.print("simulated background blue", debug).getImage();
			FlexibleImage simBlueGreen = simulatedBlue.io().or(simulatedGreen).or(simulatedGreen2).print("simulated green and blue", debug).getImage();
			input().masks().setVis(in.io().xor(simBlueGreen).print("sim xor", debug).getImage());
		}
		
		if (input().images().vis() != null && input().masks().vis() != null) {
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				FlexibleImage visImg = input().images().vis().display("In VIS", debug);
				FlexibleImage visMsk = input().masks().vis().display("In Mask", debug);
				FlexibleImage cleared = visImg
						.io()
						.compare()
						.compareImages("vis", visMsk.io().blur(getDouble("Clear-background-vis-blur", 2.0)).print("Blurred Mask", debug).getImage(),
								getInt("Clear-background-vis-l-diff-side", 20),
								getInt("Clear-background-vis-l-diff-side", 20),
								getInt("Clear-background-vis-ab-diff-side", 20),
								back, true)
						.or(visMsk.copy().io()
								.filterRemainHSV(getDouble("Clear-background-vis-remain-distance", 0.02), getDouble("Clear-background-vis-remain-hue", 0.62))
								.getImage())
						.border(getInt("Clear-background-vis-border-width-side", 2))
						.getImage();
				return input().images().vis().io().applyMask_ResizeMaskIfNeeded(cleared, options.getBackground())
						.print("CLEAR RESULT", debug).getImage();
			}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				FlexibleImage visX = input().images().vis().copy();
				FlexibleImage cleared = new ImageOperation(visX).blur(getDouble("Clear-background-vis-blur", 2.0))
						.compare()
						.compareImages("vis", input().masks().vis().io().blur(getDouble("Clear-background-vis-blur", 2.0)).print("medianb", debug).getImage(),
								getInt("Clear-background-vis-l-diff-top", 40),
								getInt("Clear-background-vis-l-diff-top", 40),
								getInt("Clear-background-vis-ab-diff-top", 40),
								back, debug)
						.print("comparison result", debug)
						.or(visX.copy().io()
								.filterRemainHSV(getDouble("Clear-background-vis-remain-distance", 0.02), getDouble("Clear-background-vis-remain-hue", 0.62))
								.getImage())
						.border(getInt("Clear-background-vis-border-width-top", 1))
						.getImage();
				return input().images().vis().io().applyMask_ResizeMaskIfNeeded(cleared, options.getBackground())
						.print("CLEAR RESULT", debug).getImage();
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().images().fluo() != null && input().masks().fluo() != null) {
			if ((options.isBarley() && options.isHigherResVisCamera()))
				return input().images().fluo();
			else
				if (options.getCameraPosition() == CameraPosition.SIDE) {
					double scaleFactor = getDouble("Scale-factor-decrease-mask", 1.0);
					FlexibleImage fluo = input().images().fluo();
					fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
					
					double leftRightBorder = getDouble("Clear-background-fluo-left-right-border-side", 0.1);
					
					if (options.isArabidopsis())
						leftRightBorder = 0;
					
					double fac = getDouble("Clear-background-fluo-lab-factor-side", 0.1);
					FlexibleImage result = new ImageOperation(fluo.io().copy()
							.blur(getDouble("Clear-background-fluo-blur", 1.0)).print("Blurred fluo image", false)
							.medianFilter32Bit()
							.getImage()).compare()
							.compareImages("fluo", input().masks().fluo().io()
									.medianFilter32Bit()
									.getImage(),
									getInt("Clear-background-fluo-l-diff", 75) * fac,
									getInt("Clear-background-fluo-l-diff", 75) * fac,
									getInt("Clear-background-fluo-ab-diff", 40) * fac,
									back)
							.border(getInt("Clear-background-fluo-border-width-side", 2))
							.border_left_right((int) (fluo.getWidth() * leftRightBorder), options.getBackground())
							.getImage();
					double blueCurbWidthBarley0_1 = 0;
					double blueCurbHeightEndBarly0_8 = 1;
					FlexibleImage toBeFiltered = result.io().hq_thresholdLAB_multi_color_or_and_not(
							// black background and green pot (fluo of white pot)
							getIntArray("Clear-background-fluo-min-l-array", new Integer[] { -1, 200 - 40, 50 - 4, 0 }),
							getIntArray("Clear-background-fluo-max-l-array", new Integer[] { 115, 200 + 20, 50 + 4, 50 }),
							getIntArray("Clear-background-fluo-min-a-array", new Integer[] { 80 - 5, 104 - 15, 169 - 4, 0 }),
							getIntArray("Clear-background-fluo-max-a-array", new Integer[] { 140 + 5, 104 + 15, 169 + 4, 250 }),
							getIntArray("Clear-background-fluo-min-b-array", new Integer[] { 116 - 5, 206 - 20, 160 - 4, 0 }),
							getIntArray("Clear-background-fluo-max-b-array", new Integer[] { 175 + 5, 206 + 20, 160 + 4, 250 }),
							options.getBackground(), Integer.MAX_VALUE, false,
							new Integer[] {}, new Integer[] {},
							new Integer[] {}, new Integer[] {},
							new Integer[] {}, new Integer[] {},
							blueCurbWidthBarley0_1,
							blueCurbHeightEndBarly0_8).
							print("removed noise", debug).getImage();
					
					result = result.copy().io().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage();
					
					if (debug)
						result.copy().io().replaceColor(options.getBackground(), Color.YELLOW.getRGB()).print("Left-Over");
					
					return result;
				}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				double scaleFactor = getDouble("Scale-factor-decrease-mask", 1.0);
				FlexibleImage fluo = input().images().fluo();
				fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
				double fac = getDouble("Clear-background-fluo-lab-factor-top", 0.2);
				return new ImageOperation(fluo).compare()
						.compareImages("fluo", input().masks().fluo(),
								getInt("Clear-background-fluo-l-diff", 75) * fac,
								getInt("Clear-background-fluo-l-diff", 75) * fac,
								getInt("Clear-background-fluo-ab-diff", 40) * fac,
								back)
						.border(getInt("Clear-background-fluo-border-width-top", 2))
						.getImage();
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		FlexibleImage nir = input().images().nir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (options.isBarleyInBarleySystem()) {
				// remove horizontal bar
				if (nir != null) {
					nir = filterHorBar(nir);
				}
			}
		}
		return nir;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().images().nir() != null && input().masks().nir() == null) {
			// create simulated nir background
			int w = input().images().nir().getWidth();
			int h = input().images().nir().getHeight();
			input().masks().setNir(ImageOperation.createColoredImage(w, h, new Color(180, 180, 180)));
		}
		if (input().images().nir() != null && input().masks().nir() != null) {
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				FlexibleImage nir = input().masks().nir();
				if (options.isBarleyInBarleySystem()) {
					// remove horizontal bar
					nir = filterHorBar(nir);
				}
				if (options.isMaize()) {
					int blackDiff = getInt("Clear-background-nir-black-diff-top", 14) / 3;
					int whiteDiff = getInt("Clear-background-nir-white-diff-top", 20) / 3;
					FlexibleImage msk = new ImageOperation(nir.display("NIR MSK", debug)).compare()
							.compareGrayImages(input().images().nir(), blackDiff, whiteDiff, options.getBackground())
							.print("result nir", debug).getImage();
					msk = msk.io().replaceColor(ImageOperation.BACKGROUND_COLORint, new Color(180, 180, 180).getRGB()).getImage();
					
					return msk;
				}
				return nir;
			}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				FlexibleImage nir = input().images().nir();
				
				if (options.isMaize()) {
					int blackDiff = getInt("Clear-background-nir-black-diff-top-maize", 14);
					int whiteDiff = getInt("Clear-background-nir-white-diff-maize", 20);
					return new ImageOperation(nir).compare()
							.compareGrayImages(input().masks().nir(), blackDiff, whiteDiff, options.getBackground())
							.print("result nir", debug)
							.thresholdClearBlueBetween(getInt("Clear-background-nir-min-threshold-blue", 150), getInt("Clear-background-nir-max-threshold-blue", 169))
							.thresholdBlueHigherThan(getInt("Clear-background-nir-higher-threshold-blue", 240))
							.border(getInt("Clear-background-nir-border-width-top", 2))
							.getImage();
				} else {
					int blackDiff = getInt("Clear-background-nir-black-diff-top", 10);
					int whiteDiff = getInt("Clear-background-nir-white-diff", 23);
					if (options.isHigherResVisCamera())
						return new ImageOperation(nir).compare()
								.compareGrayImages(input().masks().nir(), blackDiff, whiteDiff, options.getBackground())
								.print("result nir", debug)
								.thresholdBlueHigherThan(getInt("Clear-background-nir-higher-threshold-blue", 240))
								.border(getInt("Clear-background-nir-border-width-top", 2))
								.getImage();
					else
						return new ImageOperation(nir).compare()
								.compareGrayImages(input().masks().nir(), blackDiff, whiteDiff, options.getBackground())
								.print("result nir", debug)
								.thresholdBlueHigherThan(getInt("Clear-background-nir-higher-threshold-blue", 240))
								.border(getInt("Clear-background-nir-border-width-top", 2))
								.getImage();
				}
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
	
	private FlexibleImage filterHorBar(FlexibleImage nirImage) {
		int[][] in = nirImage.getAs2A();
		int width = nirImage.getWidth();
		int height = nirImage.getHeight();
		int gray = new Color(180, 180, 180).getRGB();
		for (int y = (int) (height * 0.4); y < height * 0.6; y++) {
			double sum = 0;
			int n = 0;
			for (int x = 0; x < width; x++) {
				float i = (float) ((in[x][y] & 0x0000ff) / 255.0);
				sum = sum + i;
				n++;
			}
			double avg = sum / n;
			double differenceDistanceSum = 0;
			for (int x = 0; x < width; x++) {
				float i = (float) ((in[x][y] & 0x0000ff) / 255.0);
				differenceDistanceSum += Math.abs(i - avg);
			}
			if (avg < 0.6) {
				for (int x = 0; x < width; x++) {
					in[x][y] = gray;
				}
			}
		}
		FlexibleImage res = new FlexibleImage(in).display("DEBUG", debug);
		return res;
	}
	
	@Override
	protected FlexibleImage processIRmask() {
		if (input().images().ir() != null)
			return input().images().ir().copy();
		else
			return input().masks().ir();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage i = processedImages.nir();
			FlexibleImage m = processedMasks.nir();
			if (i != null && m != null) {
				i = i.io().applyMask_ResizeMaskIfNeeded(m.io().getImage(), options.getBackground()).getImage();
				i = i.io().replaceColor(ImageOperation.BACKGROUND_COLORint, new Color(180, 180, 180).getRGB()).getImage();
				processedImages.setNir(i);
				processedMasks.setNir(i.copy());
			}
		}
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}

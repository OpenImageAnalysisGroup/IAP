/**
 * 
 */
package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * Clears the background by comparison of foreground and background.
 * Additionally the border around the masks is cleared (width 2 pixels).
 * 
 * @author pape, klukas, entzian
 */
public class BlRemoveBackground extends AbstractSnapshotAnalysisBlock {
	
	int back;
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		back = ImageOperation.BACKGROUND_COLORint;
		if (input().masks().nir() != null)
			input().masks().setNir(
					input().masks().nir().io().replaceColor(
							new Color(255, 255, 255).getRGB(),
							new Color(254, 254, 254).getRGB()).getImage());
	}
	
	@Override
	protected Image processVISmask() {
		
		if (getBoolean("copy only vis image to mask", false)) {
			if (input().images().vis() != null) {
				return input().images().vis().copy();
			}
		}
		
		if (input().images().vis() != null && input().masks().vis() == null) {
			Image in = input().images().vis();
			Image simulatedGreen = in.io().copy().filterByHSV(getDouble("Clear-background-vis-color-distance", 0.1), Color.GREEN.getRGB()).
					show("simulated background green", debug).getImage();
			Image simulatedGreen2 = in
					.io()
					.copy()
					.filterByHSV(
							getDouble("Clear-background-vis-color-distance", 0.1),
							new Color(
									getInt("Clear-background-vis-color-green-R", 94),
									getInt("Clear-background-vis-color-green-G", 118),
									getInt("Clear-background-vis-color-green-B", 50)).getRGB())
					.show("simulated background green 2", debug).getImage();
			Image simulatedBlue = in
					.io()
					.copy()
					.filterByHSV(
							getDouble("Clear-background-vis-color-distance", 0.1),
							new Color(
									getInt("Clear-background-vis-color-blue-R", 20),
									getInt("Clear-background-vis-color-blue-G", 36),
									getInt("Clear-background-vis-color-blue-B", 76)).getRGB())
					.show("simulated background blue", debug).getImage();
			Image simBlueGreen = simulatedBlue.io().or(simulatedGreen).or(simulatedGreen2).show("simulated green and blue", debug).getImage();
			input().masks().setVis(in.io().xor(simBlueGreen).show("sim xor", debug).getImage());
		}
		
		if (input().images().vis() != null && input().masks().vis() != null) {
			Image visImg = input().images().vis().show("In VIS", debug);
			Image visMsk = input().masks().vis().show("In Mask", debug);
			Image cleared = visImg
					.io()
					.compare()
					.compareImages("vis", visMsk.io().blur(getDouble("Clear-background-vis-blur", 2.0)).show("Blurred Mask", debug).getImage(),
							getInt("Clear-background-vis-l-diff-side", 20),
							getInt("Clear-background-vis-l-diff-side", 20),
							getInt("Clear-background-vis-ab-diff-side", 20),
							back, true)
					.or(visMsk.copy().io()
							.filterRemainHSV(getDouble("Clear-background-vis-remain-distance", 0.02), getDouble("Clear-background-vis-remain-hue", 0.62))
							.getImage())
					.getImage();
			return input().images().vis().io().applyMask_ResizeMaskIfNeeded(cleared, optionsAndResults.getBackground())
					.show("CLEAR RESULT", debug).getImage();
		} else {
			return null;
		}
	}
	
	@Override
	protected Image processFLUOmask() {
		
		if (getBoolean("copy only fluo image to mask", false)) {
			if (input().images().fluo() != null) {
				return input().images().fluo().copy();
			}
		}
		
		if (input().images().fluo() != null && input().masks().fluo() != null) {
			Image fluo = input().images().fluo();
			
			Image result = new ImageOperation(fluo.io().copy()
					.blur(getDouble("Clear-background-fluo-blur", 1.0)).show("Blurred fluo image", false)
					.medianFilter32Bit()
					.getImage()).compare()
					.compareImages("fluo", input().masks().fluo().io()
							.medianFilter32Bit()
							.getImage(),
							getInt("Clear-background-fluo-l-diff", 7),
							getInt("Clear-background-fluo-l-diff", 7),
							getInt("Clear-background-fluo-ab-diff", 4),
							back)
					.getImage();
			double blueCurbWidthBarley0_1 = 0;
			double blueCurbHeightEndBarly0_8 = 1;
			if (getBoolean("Filter FLUO with LAB", false)) {
				Image toBeFiltered = result.io().hq_thresholdLAB_multi_color_or_and_not(
						// black background and green pot (fluo of white pot)
						getIntArray("Clear-background-fluo-min-l-array", new Integer[] { -1, 200 - 40, 50 - 4, 0 }),
						getIntArray("Clear-background-fluo-max-l-array", new Integer[] { 115, 200 + 20, 50 + 4, 50 }),
						getIntArray("Clear-background-fluo-min-a-array", new Integer[] { 80 - 5, 104 - 15, 169 - 4, 0 }),
						getIntArray("Clear-background-fluo-max-a-array", new Integer[] { 140 + 5, 104 + 15, 169 + 4, 250 }),
						getIntArray("Clear-background-fluo-min-b-array", new Integer[] { 116 - 5, 206 - 20, 160 - 4, 0 }),
						getIntArray("Clear-background-fluo-max-b-array", new Integer[] { 175 + 5, 206 + 20, 160 + 4, 250 }),
						optionsAndResults.getBackground(), Integer.MAX_VALUE, false,
						new Integer[] {}, new Integer[] {},
						new Integer[] {}, new Integer[] {},
						new Integer[] {}, new Integer[] {},
						blueCurbWidthBarley0_1,
						blueCurbHeightEndBarly0_8).
						show("removed noise", debug).getImage();
				
				result = result.copy().io().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, optionsAndResults.getBackground()).getImage();
			}
			if (debug)
				result.copy().io().replaceColor(optionsAndResults.getBackground(), Color.YELLOW.getRGB()).show("Left-Over");
			
			return result;
		} else {
			return null;
		}
	}
	
	@Override
	protected Image processNIRimage() {
		Image nir = input().images().nir();
		if (getBoolean("Remove Constant Horizontal Bar from NIR", false)) {
			// remove horizontal bar
			if (nir != null) {
				nir = filterHorBar(nir);
			}
		}
		return nir;
	}
	
	@Override
	protected Image processNIRmask() {
		
		if (getBoolean("copy only nir image to mask", false)) {
			if (input().images().nir() != null) {
				return input().images().nir().copy();
			}
		}
		
		if (input().images().nir() != null && input().masks().nir() == null) {
			// create simulated nir background
			int w = input().images().nir().getWidth();
			int h = input().images().nir().getHeight();
			input().masks().setNir(ImageOperation.createColoredImage(w, h, new Color(180, 180, 180)));
		}
		if (input().images().nir() != null && input().masks().nir() != null) {
			Image nir = input().masks().nir();
			if (getBoolean("Remove Constant Horizontal Bar from NIR", false)) {
				// remove horizontal bar
				nir = filterHorBar(nir).show("removed constant bar", debug);
			}
			if (getBoolean("Process NIR Mask", true)) {
				int blackDiff = getInt("Clear-background-nir-black-diff-top", 20);
				int whiteDiff = getInt("Clear-background-nir-white-diff-top", 20);
				Image msk = new ImageOperation(nir.show("NIR MSK", debug)).compare()
						.compareGrayImages(input().images().nir(), blackDiff, whiteDiff, optionsAndResults.getBackground())
						.show("result nir", debug).getImage();
				return input().images().nir().io().applyMask(msk, optionsAndResults.getBackground()).getImage();
			}
			return nir;
		} else {
			return null;
		}
	}
	
	private Image filterHorBar(Image nirImage) {
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
		Image res = new Image(in).show("DEBUG", debug);
		return res;
	}
	
	@Override
	protected Image processIRmask() {
		if (input().images().ir() != null)
			return input().images().ir().copy();
		else
			return input().masks().ir();
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		if (!getBoolean("copy only nir image to mask", false))
			if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE) {
				Image i = processedImages.nir();
				Image m = processedMasks.nir();
				if (i != null && m != null) {
					i = i.io().applyMask_ResizeMaskIfNeeded(m.io().getImage(), optionsAndResults.getBackground()).getImage();
					i = i.io().replaceColor(ImageOperation.BACKGROUND_COLORint, new Color(180, 180, 180).getRGB()).getImage();
					processedImages.setNir(i);
					processedMasks.setNir(i.copy());
				}
			}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
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
		return "Compare Images and Reference Images";
	}
	
	@Override
	public String getDescription() {
		return " Clears the background by comparison of foreground and background. " +
				"Additionally the border around the masks is cleared (width 2 pixels).";
	}
}

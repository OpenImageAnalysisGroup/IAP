/**
 * 
 */
package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Klukas
 */
public class BlRemovePotSoil extends AbstractSnapshotAnalysisBlock {
	
	boolean debug;
	private ImageOperation soilMask;
	
	@Override
	protected void prepare() {
		super.prepare();
		soilMask = null;
		if (input().masks().vis() != null && input().images().vis() != null) {
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				debug = getBoolean("debug", false);
				
				ImageOperation mask = input().masks().vis().copy().io();
				
				mask = mask.blur(getDouble("blur", 2));
				mask = mask.filterRemoveLAB(
						getInt("soil-lab-l-min", 90), getInt("soil-lab-l-max", 145),
						getInt("soil-lab-a-min", 120), getInt("soil-lab-a-max", 135),
						getInt("soil-lab-b-min", 125), getInt("soil-lab-b-max", 155),
						optionsAndResults.getBackground(),
						false)
						.erode(getInt("erode-cnt", 2))
						.dilate(getInt("dilate-cnt", 2))
						.grayscale()
						.threshold(100, optionsAndResults.getBackground(), new Color(100, 100, 100).getRGB())
						.show("soil region", debug);
				soilMask = mask.removeSmallElements(
						getInt("remove-noise-area", 10),
						getInt("remove-noise-dimension", 10));
			}
		}
	}
	
	@Override
	protected Image processVISmask() {
		Image vis = input().masks().vis();
		if (soilMask == null || vis == null)
			return vis;
		vis = input().masks().vis().io().applyMask(soilMask.getImage().copy(),
				optionsAndResults.getBackground()).getImage().show("Soil removed from vis", debug);
		return vis;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fluo = input().masks().fluo();
		if (soilMask == null || fluo == null)
			return fluo;
		fluo = input().masks().fluo().io().applyMask_ResizeMaskIfNeeded(soilMask.getImage().copy(),
				optionsAndResults.getBackground()).getImage().show("Soil removed from fluo", debug);
		return fluo;
	}
	
	@Override
	protected Image processNIRmask() {
		Image nir = input().masks().nir();
		if (soilMask == null || nir == null)
			return nir;
		nir = input().masks().nir().io().applyMask_ResizeMaskIfNeeded(soilMask.getImage().copy(),
				optionsAndResults.getBackground()).getImage().show("Soil removed from nir", debug);
		return nir;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Remove black pot soil (top)";
	}
	
	@Override
	public String getDescription() {
		return "Uses a LAB color filter to detect dark soil in the pot.";
	}
}

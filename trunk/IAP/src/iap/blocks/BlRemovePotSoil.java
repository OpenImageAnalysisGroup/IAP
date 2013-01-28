/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Klukas
 */
public class BlRemovePotSoil extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug;
	private ImageOperation soilMask;
	
	@Override
	protected void prepare() {
		super.prepare();
		soilMask = null;
		if (input().masks().vis() != null && input().images().vis() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				debug = getBoolean("debug", false);
				
				ImageOperation mask = input().masks().vis().copy().io();
				
				mask = mask.blur(getDouble("blur", 2));
				mask = mask.filterRemoveLAB(
						getInt("soil-lab-l-min", 90), getInt("soil-lab-l-max", 145),
						getInt("soil-lab-a-min", 120), getInt("soil-lab-a-max", 135),
						getInt("soil-lab-b-min", 125), getInt("soil-lab-b-max", 155),
						options.getBackground(),
						false)
						.erode(getInt("erode-cnt", 2))
						.dilate(getInt("dilate-cnt", 2))
						.grayscale()
						.threshold(100, options.getBackground(), new Color(100, 100, 100).getRGB())
						.show("soil region", debug);
				soilMask = mask.removeSmallElements(
						getInt("remove-noise-area", 10),
						getInt("remove-noise-dimension", 10));
			}
		}
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = input().masks().vis();
		if (soilMask == null || vis == null)
			return vis;
		vis = input().masks().vis().io().applyMask(soilMask.getImage().copy(),
				options.getBackground()).getImage().show("Soil removed from vis", debug);
		return vis;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = input().masks().fluo();
		if (soilMask == null || fluo == null)
			return fluo;
		fluo = input().masks().fluo().io().applyMask_ResizeMaskIfNeeded(soilMask.getImage().copy(),
				options.getBackground()).getImage().show("Soil removed from fluo", debug);
		return fluo;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage nir = input().masks().nir();
		if (soilMask == null || nir == null)
			return nir;
		nir = input().masks().nir().io().applyMask_ResizeMaskIfNeeded(soilMask.getImage().copy(),
				options.getBackground()).getImage().show("Soil removed from nir", debug);
		return nir;
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
}

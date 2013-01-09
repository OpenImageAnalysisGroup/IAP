package iap.blocks.arabidopsis;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlClearMasks_Arabidops extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage img = input().images().vis();
		if (img != null) {
			return img.copy().io().border(40).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		FlexibleImage img = input().images().nir();
		if (img != null) {
			return img.copy().io().translate(-3, 0).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().images().vis();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage img = input().images().fluo();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage img = input().images().nir();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}

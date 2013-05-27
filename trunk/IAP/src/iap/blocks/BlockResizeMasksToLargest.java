package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockResizeMasksToLargest extends AbstractSnapshotAnalysisBlockFIS {
	
	private int w;
	private int h;
	
	@Override
	protected void prepare() {
		w = input().masks().getLargestWidth();
		h = input().masks().getLargestHeight();
	}
	
	@Override
	protected Image processVISmask() {
		return input().masks().vis().resize(w, h);
	}
	
	@Override
	protected Image processFLUOmask() {
		return input().masks().fluo().resize(w, h);
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks().nir() != null)
			return input().masks().nir().resize(w, h);
		else
			return null;
	}
	
	// @Override
	// protected FlexibleImage processVISimage() {
	// return getInput().getImages().getVis().resize(w, h);
	// }
	//
	// @Override
	// protected FlexibleImage processFLUOimage() {
	// return getInput().getImages().getFluo().resize(w, h);
	// }
	//
	// @Override
	// protected FlexibleImage processNIRimage() {
	// return getInput().getImages().getNir().resize(w, h);
	// }
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
}

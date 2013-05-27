package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * @author Entzian
 *         process the morphological method closing
 */
public class BlClosing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		Image mask = input().masks().vis();
		
		int n = getInt("Closing-Cnt vis", 3);
		if (!getBoolean("process VIS", false)) {
			return mask;
		}
		
		return closing(mask, n);
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		Image mask = input().masks().fluo();
		
		if (!getBoolean("process FLUO", false)) {
			return mask;
		}
		
		int n = getInt("Closing-Cnt fluo", 3);
		return closing(mask, n);
	}
	
	private static Image closing(Image mask, int closingRepeat) {
		ImageOperation op = new ImageOperation(mask);
		for (int ii = 0; ii < closingRepeat; ii++) {
			op.closing();
		}
		
		return op.getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
}
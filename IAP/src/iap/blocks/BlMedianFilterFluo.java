/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Remove "peper and salt" noise from Fluo mask.
 * 
 * @author Pape, Klukas
 */
public class BlMedianFilterFluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		Image medianMask = new ImageOperation(input().masks().fluo())
				.medianFilter32Bit()
				.border(getInt("Median-fluo-border", 2))
				.getImage();
		
		return new ImageOperation(input().images().fluo()).applyMask_ResizeSourceIfNeeded(medianMask, options.getBackground()).getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		return res;
	}
	
}

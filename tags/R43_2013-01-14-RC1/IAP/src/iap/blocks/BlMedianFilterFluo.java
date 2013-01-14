/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Remove "peper and salt" noise from Fluo mask.
 * 
 * @author Pape, Klukas
 */
public class BlMedianFilterFluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		FlexibleImage medianMask = new ImageOperation(input().masks().fluo())
				.medianFilter32Bit()
				.border(getInt("Median-fluo-border", 2))
				.getImage();
		
		return new ImageOperation(input().images().fluo()).applyMask_ResizeSourceIfNeeded(medianMask, options.getBackground()).getImage();
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
}

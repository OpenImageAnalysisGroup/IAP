package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Entzian
 *         process the morphological method closing
 */
public class BlClosing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		FlexibleImage mask = input().masks().vis();
		
		int n;
		if (options.isBarleyInBarleySystem()) {
			n = getInt("Closing-Cnt vis", 3);
		} else {
			if (options.isHigherResVisCamera())
				n = getInt("Closing-Cnt vis", 5);
			else
				n = getInt("Closing-Cnt vis", 3);
		}
		
		if (!getBoolean("enable vis", false)) {
			return mask;
		}
		
		return closing(mask, n);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		FlexibleImage mask = input().masks().fluo();
		
		if (!getBoolean("enable fluo", false)) {
			return mask;
		}
		
		int n;
		if (options.isBarleyInBarleySystem()) {
			n = getInt("Closing-Cnt fluo", 3);
		} else {
			if (options.isHigherResVisCamera())
				n = getInt("Closing-Cnt fluo", 5);
			else
				n = getInt("Closing-Cnt fluo", 3);
		}
		
		return closing(mask, n);
	}
	
	private static FlexibleImage closing(FlexibleImage mask, int closingRepeat) {
		
		ImageOperation op = new ImageOperation(mask);
		for (int ii = 0; ii < closingRepeat; ii++) {
			op.closing();
		}
		
		return op.getImage();
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
}
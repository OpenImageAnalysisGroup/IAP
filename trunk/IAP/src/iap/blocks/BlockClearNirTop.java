package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockClearNirTop extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected boolean isChangingImages() {
		return options.getCameraPosition() == CameraPosition.TOP;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (options.getCameraPosition() == CameraPosition.TOP)
			return null;
		else
			return input().masks().nir();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (options.getCameraPosition() == CameraPosition.TOP)
			return null;
		else
			return input().images().nir();
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
}

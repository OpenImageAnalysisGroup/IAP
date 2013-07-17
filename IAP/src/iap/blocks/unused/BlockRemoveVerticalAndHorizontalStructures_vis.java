package iap.blocks.unused;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

public class BlockRemoveVerticalAndHorizontalStructures_vis extends BlockRemoveCameraLineArtifacts {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null)
			return null;
		if (mask.getCameraType() == CameraType.UNKNOWN) {
			System.out.println("ERROR: Unknown image type!!!");
			return mask;
		}
		if (mask.getCameraType() == CameraType.NIR)
			return mask;
		if (mask.getCameraType() == CameraType.FLUO)
			return mask;
		if (mask.getCameraType() == CameraType.VIS)
			return process(process(mask));
		
		return mask;
	}
}

package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author klukas, entzian
 */
public class BlRootsSharpenImage extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().images().vis();
		if (img != null)
			img = img.io().copy()
					.blur(getInt("blur", 2))
					.sharpen(getInt("sharpen", 3))
					.getImage();
		return img;
	}
}

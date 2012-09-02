package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Analyze individual parts of the roots.
 * 
 * @author klukas
 */
public class BlRootsAnalyzeSeparation extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().masks().vis();
		if (img != null) {
			return img;
		}
		return null;
	}
	
}

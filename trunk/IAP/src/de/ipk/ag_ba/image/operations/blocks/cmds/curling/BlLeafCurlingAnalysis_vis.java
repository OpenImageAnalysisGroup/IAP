package de.ipk.ag_ba.image.operations.blocks.cmds.curling;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlLeafCurlingAnalysis_vis extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected synchronized FlexibleImage processVISmask() {
		if (input().masks().vis() == null) {
			return null;
		}
		return input().masks().vis();
	}
}

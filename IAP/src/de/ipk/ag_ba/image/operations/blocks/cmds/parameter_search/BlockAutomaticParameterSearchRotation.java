package de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search;

import de.ipk.ag_ba.image.operation.MorphologicalOperationSearchType;
import de.ipk.ag_ba.image.operations.blocks.cmds.WorkingImageTyp;

public class BlockAutomaticParameterSearchRotation extends BlockAutomaticParameterSearch {
	
	public BlockAutomaticParameterSearchRotation() {
		super(MorphologicalOperationSearchType.ROTATION, WorkingImageTyp.FLUO_AND_NIR);
	}
	
}

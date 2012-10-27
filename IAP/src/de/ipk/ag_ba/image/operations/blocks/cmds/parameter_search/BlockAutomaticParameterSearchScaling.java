package de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search;

import de.ipk.ag_ba.image.operation.MorphologicalOperationSearchType;
import de.ipk.ag_ba.image.operations.blocks.cmds.WorkingImageTyp;

public class BlockAutomaticParameterSearchScaling extends BlockAutomaticParameterSearch {
	
	public BlockAutomaticParameterSearchScaling() {
		super(MorphologicalOperationSearchType.SCALING, WorkingImageTyp.FLUO_AND_NIR);
	}
	
}

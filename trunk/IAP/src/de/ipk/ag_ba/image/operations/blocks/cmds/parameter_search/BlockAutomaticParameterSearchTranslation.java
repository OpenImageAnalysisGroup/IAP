package de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search;

import de.ipk.ag_ba.image.operation.MorphologicalOperationSearchType;
import de.ipk.ag_ba.image.operations.blocks.cmds.WorkingImageTyp;

public class BlockAutomaticParameterSearchTranslation extends BlockAutomaticParameterSearch {
	
	public BlockAutomaticParameterSearchTranslation() {
		super(MorphologicalOperationSearchType.TRANSLATION, WorkingImageTyp.FLUO_AND_NIR);
	}
	
}

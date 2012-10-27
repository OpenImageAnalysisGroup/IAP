/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search;

import de.ipk.ag_ba.image.operation.MorphologicalOperationSearchType;
import de.ipk.ag_ba.image.operations.blocks.cmds.WorkingImageTyp;

/**
 * @author Entzian
 */
public class BlockAutomaticParameterSearchRotationOnFluo extends BlockAutomaticParameterSearch {
	
	public BlockAutomaticParameterSearchRotationOnFluo() {
		super(MorphologicalOperationSearchType.ROTATION, WorkingImageTyp.ONLY_FLUO);
	}
}

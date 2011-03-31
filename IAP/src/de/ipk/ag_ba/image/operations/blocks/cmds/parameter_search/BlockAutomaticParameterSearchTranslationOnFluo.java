/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search;

import de.ipk.ag_ba.image.operations.MorphologicalOperationSearchType;
import de.ipk.ag_ba.image.operations.blocks.cmds.WorkingImageTyp;

/**
 * @author Entzian
 */
public class BlockAutomaticParameterSearchTranslationOnFluo extends BlockAutomaticParameterSearch {
	
	public BlockAutomaticParameterSearchTranslationOnFluo() {
		super(MorphologicalOperationSearchType.TRANSLATION, WorkingImageTyp.ONLY_FLUO);
	}
}

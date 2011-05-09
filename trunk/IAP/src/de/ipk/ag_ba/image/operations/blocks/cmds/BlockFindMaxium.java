/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */
public class BlockFindMaxium extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		ResultsTable numericResult = ResultsTable.getResultsTable();
		
		ImageOperation res = new ImageOperation(getInput().getMasks().getVis()).findMax();
		// ResultsTable numericResult = res.getResultsTable();
		
		double nMaxima = numericResult.getValue("Count", numericResult.getCounter() - 1);
		getProperties().setNumericProperty(0, PropertyNames.RESULT_MAXIMUM_SEARCH, nMaxima);
		
		return new ImageOperation(getInput().getMasks().getVis()).getImage();
	}
}

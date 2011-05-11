/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */
public class BlockCountPixel extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		getProperties().setNumericProperty(0, PropertyNames.COUNT_PIXEL_VIS, countPixel(getInput().getMasks().getVis()));
		
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		getProperties().setNumericProperty(0, PropertyNames.COUNT_PIXEL_FLUO, countPixel(getInput().getMasks().getFluo()));
		
		return getInput().getMasks().getFluo();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		
		getProperties().setNumericProperty(0, PropertyNames.COUNT_PIXEL_NIR, countPixel(getInput().getMasks().getNir()));
		
		return getInput().getMasks().getNir();
	}
	
	private int countPixel(FlexibleImage workMask) {
		int count = 0;
		int[][] workArray = workMask.getAs2A();
		
		for (int i = 0; i < workMask.getWidth(); i++) {
			for (int j = 0; j < workMask.getHeight(); j++) {
				if (workArray[i][j] != options.getBackground())
					count++;
			}
		}
		return count;
	}
	
}

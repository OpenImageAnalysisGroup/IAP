/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian, Klukas
 */
public class BlockCountPixel extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		getProperties().setNumericProperty(0, PropertyNames.COUNT_PIXEL_VIS, countPixel(input().masks().vis()));
		
		return input().masks().vis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		getProperties().setNumericProperty(0, PropertyNames.COUNT_PIXEL_FLUO, countPixel(input().masks().fluo()));
		
		return input().masks().fluo();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		
		getProperties().setNumericProperty(0, PropertyNames.COUNT_PIXEL_NIR, countPixel(input().masks().nir()));
		
		return input().masks().nir();
	}
	
	private int countPixel(FlexibleImage workMask) {
		int count = 0;
		int[] workArray = workMask.getAs1A();
		int back = options.getBackground();
		for (int pi : workArray) {
			if (pi != back)
				count++;
		}
		return count;
	}
}

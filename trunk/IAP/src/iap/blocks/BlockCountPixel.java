/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

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
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return new HashSet<FlexibleImageType>();
	}
}

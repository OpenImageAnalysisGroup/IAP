package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlockRemoveVerticalAndHorizontalStructuresVis extends BlockRemoveVerticalAndHorizontalStructuresVisFluo {
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask == null)
			return null;
		if (mask.getType() == FlexibleImageType.UNKNOWN) {
			System.out.println("ERROR: Unknown image type!!!");
			return mask;
		}
		if (mask.getType() == FlexibleImageType.NIR)
			return mask;
		if (mask.getType() == FlexibleImageType.FLUO)
			return mask;
		if (mask.getType() == FlexibleImageType.VIS)
			return process(process(mask));
		
		return mask;
	}
}

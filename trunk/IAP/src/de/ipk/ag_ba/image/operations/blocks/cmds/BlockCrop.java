package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * 
 * @author klukas
 */
public class BlockCrop extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput() != null && getInput().getMasks() != null && getInput().getMasks().getVis() != null)
			return getInput().getMasks().getVis().crop();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput() != null && getInput().getMasks() != null && getInput().getMasks().getFluo() != null)
			return getInput().getMasks().getFluo().crop();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput() != null && getInput().getMasks() != null && getInput().getMasks().getNir() != null)
			return getInput().getMasks().getNir().crop();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processVISimage() {
		if (getInput() != null && getInput().getImages() != null && getInput().getImages().getVis() != null)
			return getInput().getImages().getVis().crop();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		if (getInput() != null && getInput().getImages() != null && getInput().getImages().getFluo() != null)
			return getInput().getImages().getFluo().crop();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput() != null && getInput().getImages() != null && getInput().getImages().getNir() != null)
			return getInput().getImages().getNir().crop();
		else
			return null;
	}
}

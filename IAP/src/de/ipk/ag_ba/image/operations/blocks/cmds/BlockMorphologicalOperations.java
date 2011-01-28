package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Perform some initial morphological operations on the masks.
 */
public class BlockMorphologicalOperations extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return morpholigicOperatorsToInitinalImageProcess(getInput().getImages().getVis(), getInput().getMasks().getVis(), ImageConfiguration.RgbTop);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return morpholigicOperatorsToInitinalImageProcess(getInput().getImages().getFluo(), getInput().getMasks().getFluo(), ImageConfiguration.FluoTop);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return morpholigicOperatorsToInitinalImageProcess(getInput().getImages().getNir(), getInput().getMasks().getNir(), ImageConfiguration.NirTop);
	}
	
	private FlexibleImage morpholigicOperatorsToInitinalImageProcess(FlexibleImage srcImage, FlexibleImage workImage, ImageConfiguration typ) {
		
		ImageOperation maskIo = new ImageOperation(workImage);
		
		switch (typ) {
			
			case RgbTop:
				for (int ii = 0; ii < options.getDilateRgbTop(); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getErodeRgbTop(); ii++)
					maskIo.erode();
				
				break;
			
			case FluoTop:
				for (int ii = 0; ii < options.getDilateFluoTop(); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getErodeFluoTop(); ii++)
					maskIo.erode();
				
			case NirTop:
				for (int ii = 0; ii < options.getClosingNirTop(); ii++)
					maskIo.closing();
				break;
			
		}
		return new ImageOperation(srcImage).applyMask2(maskIo.getImage(), options.getBackground()).getImage();
	}
	
}

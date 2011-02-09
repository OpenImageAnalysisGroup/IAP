package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Perform some initial morphological operations on the masks.
 */
public class BlockMorphologicalOperations extends AbstractSnapshotAnalysisBlockFIS {
	
	enum BlockMorphologicalOperationsTyp {
		VIS, FLUO, NIR
		
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		return morphologicalOperatorsToInitinalImageProcess(getInput().getImages().getVis(), getInput().getMasks().getVis(), options.getCameraTyp(),
				BlockMorphologicalOperationsTyp.VIS);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return morphologicalOperatorsToInitinalImageProcess(getInput().getImages().getFluo(), getInput().getMasks().getFluo(), options.getCameraTyp(),
				BlockMorphologicalOperationsTyp.FLUO);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return morphologicalOperatorsToInitinalImageProcess(getInput().getImages().getNir(), getInput().getMasks().getNir(), options.getCameraTyp(),
				BlockMorphologicalOperationsTyp.NIR);
	}
	
	private FlexibleImage morphologicalOperatorsToInitinalImageProcess(FlexibleImage srcImage, FlexibleImage workImage, ImageProcessorOptions.CameraTyp camera,
			BlockMorphologicalOperationsTyp typ) {
		
		ImageOperation maskIo = null;
		
		switch (camera) {
			case SIDE:
				maskIo = doMorphologicalOperationsOnSide(srcImage, workImage, typ);
				break;
			
			case TOP:
				maskIo = doMorphologicalOperationsOnTop(srcImage, workImage, typ);
				break;
			
			default:
				break;
		}
		
		return new ImageOperation(srcImage).applyMask2(maskIo.getImage(), options.getBackground()).getImage();
	}
	
	private ImageOperation doMorphologicalOperationsOnSide(FlexibleImage srcImage, FlexibleImage workImage, BlockMorphologicalOperationsTyp typ) {
		ImageOperation maskIo = new ImageOperation(workImage);
		
		switch (typ) {
			
			case VIS:
				for (int ii = 0; ii < options.getDilateRgbSide(); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getErodeRgbSide(); ii++)
					maskIo.erode();
				
				break;
			
			case FLUO:
				for (int ii = 0; ii < options.getDilateFluoSide(); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getErodeFluoSide(); ii++)
					maskIo.erode();
				
			case NIR:
				for (int ii = 0; ii < options.getClosingNirSide(); ii++)
					maskIo.closing();
				break;
			
		}
		
		return maskIo;
	}
	
	private ImageOperation doMorphologicalOperationsOnTop(FlexibleImage srcImage, FlexibleImage workImage, BlockMorphologicalOperationsTyp typ) {
		
		ImageOperation maskIo = new ImageOperation(workImage);
		
		switch (typ) {
			
			case VIS:
				for (int ii = 0; ii < options.getDilateRgbTop(); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getErodeRgbTop(); ii++)
					maskIo.erode();
				
				break;
			
			case FLUO:
				for (int ii = 0; ii < options.getDilateFluoTop(); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getErodeFluoTop(); ii++)
					maskIo.erode();
				
			case NIR:
				for (int ii = 0; ii < options.getClosingNirTop(); ii++)
					maskIo.closing();
				break;
			
		}
		
		return maskIo;
	}
}

package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.ImageTyp;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Perform some initial morphological operations on the masks.
 */
public class BlockMorphologicalOperations extends AbstractSnapshotAnalysisBlockFIS {
	
	// enum BlockMorphologicalOperationsTyp {
	// VIS, FLUO, NIR
	//
	// }
	
	@Override
	protected FlexibleImage processVISmask() {
		return morphologicalOperatorsToInitinalImageProcess(input().images().vis(), input().masks().vis(), options.getCameraPosition(),
				ImageTyp.RGB);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return morphologicalOperatorsToInitinalImageProcess(input().images().fluo(), input().masks().fluo(), options.getCameraPosition(),
				ImageTyp.FLUO);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return morphologicalOperatorsToInitinalImageProcess(input().images().nir(), input().masks().nir(), options.getCameraPosition(),
				ImageTyp.NIR);
	}
	
	private FlexibleImage morphologicalOperatorsToInitinalImageProcess(FlexibleImage srcImage, FlexibleImage workImage, ImageProcessorOptions.CameraPosition camera,
			ImageTyp typ) {
		
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
		
		return new ImageOperation(srcImage).applyMask_ResizeSourceIfNeeded(maskIo.getImage(), options.getBackground()).getImage();
	}
	
	private ImageOperation doMorphologicalOperationsOnSide(FlexibleImage srcImage, FlexibleImage workImage, ImageTyp typ) {
		ImageOperation maskIo = new ImageOperation(workImage);
		
		switch (typ) {
			
			case RGB:
				for (int ii = 0; ii < options.getIntSetting(Setting.DILATE_RGB_SIDE); ii++)
					maskIo.dilate();
				// maskIo.dilate(new int[][] { { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1 } });
				// maskIo.dilate(new int[][] { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } });
				// maskIo.dilate(new int[][] { { 1, 1 }, { 1, 1 }, { 1, 1 } });
				// maskIo.dilate(new int[][] { { 0, 0, 0 }, { 1, 1, 1 }, { 0, 0, 0 } });
				
				for (int ii = 0; ii < options.getIntSetting(Setting.ERODE_RGB_SIDE); ii++)
					maskIo.erode();
				
				break;
			
			case FLUO:
				for (int ii = 0; ii < options.getIntSetting(Setting.DILATE_FLUO_SIDE); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getIntSetting(Setting.ERODE_FLUO_SIDE); ii++)
					maskIo.erode();
				
			case NIR:
				for (int ii = 0; ii < options.getIntSetting(Setting.CLOSING_NIR_SIDE); ii++)
					maskIo.closing();
				break;
			
		}
		
		return maskIo;
	}
	
	private ImageOperation doMorphologicalOperationsOnTop(FlexibleImage srcImage, FlexibleImage workImage, ImageTyp typ) {
		
		ImageOperation maskIo = new ImageOperation(workImage);
		
		switch (typ) {
			
			case RGB:
				for (int ii = 0; ii < options.getIntSetting(Setting.DILATE_RGB_TOP); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getIntSetting(Setting.ERODE_RGB_TOP); ii++)
					maskIo.erode();
				
				break;
			
			case FLUO:
				for (int ii = 0; ii < options.getIntSetting(Setting.DILATE_FLUO_TOP); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getIntSetting(Setting.ERODE_FLUO_TOP); ii++)
					maskIo.erode();
				
			case NIR:
				for (int ii = 0; ii < options.getIntSetting(Setting.CLOSING_NIR_TOP); ii++)
					maskIo.closing();
				break;
			
		}
		
		return maskIo;
	}
}

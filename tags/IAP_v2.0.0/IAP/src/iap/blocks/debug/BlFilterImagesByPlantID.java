package iap.blocks.debug;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author pape
 */

public class BlFilterImagesByPlantID extends AbstractSnapshotAnalysisBlock {
	
	@Override
	public boolean isChangingImages() {
		return false;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		return res;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		if (processedImages.getAnyInfo() == null)
			return;
		
		if (processedImages.getAnyInfo().getParentSample().getSampleFineTimeOrRowId() == null)
			return;
		
		int filterCount = getInt("Filter Count", 1);
		boolean process = true;
		
		for (int filterIdx = 0; filterIdx < filterCount; filterIdx++) {
			String[] possibleValues1 = { "include only defined plant", "exclude defined plant" };
			String calculationMode1 = optionsAndResults.getStringSettingRadio(this, "Calculation Mode - F " + filterIdx + 1, "include only defined plant",
					StringManipulationTools.getStringListFromArray(possibleValues1));
			
			String containMode = calculationMode1;
			
			String value = getString("Plant ID " + filterIdx, "");
			String id = processedImages.getAnyInfo().getQualityAnnotation() + "";
			
			if (value.equals(id)) {
				if (containMode.equals("include only defined plant"))
					process = true;
				else
					process = false;
			} else {
				if (containMode.equals("include only defined plant"))
					process = false;
				else
					process = true;
			}
		}
		
		if (!process) {
			processedImages.setVisInfo(null);
			processedImages.setFluoInfo(null);
			processedImages.setNirInfo(null);
			processedImages.setIrInfo(null);
			
			processedMasks.setVisInfo(null);
			processedMasks.setFluoInfo(null);
			processedMasks.setNirInfo(null);
			processedMasks.setIrInfo(null);
			
			processedImages.setVis(null);
			processedImages.setFluo(null);
			processedImages.setNir(null);
			processedImages.setIr(null);
			
			processedMasks.setVis(null);
			processedMasks.setFluo(null);
			processedMasks.setNir(null);
			processedMasks.setIr(null);
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.DEBUG;
	}
	
	@Override
	public String getName() {
		return "Filter Images By Plant ID";
	}
	
	@Override
	public String getDescription() {
		return "Removes images due to specific plant ID.";
	}
}

package iap.blocks.acquisition;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;
import java.util.stream.Stream;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;

/**
 * @author pape, klukas
 */

public class BlFilterImagesByCondition extends AbstractSnapshotAnalysisBlock {
	
	@Override
	public boolean isChangingImages() {
		return true;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		return res;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		if (processedImages.getAnyInfo().getParentSample().getSampleFineTimeOrRowId() == null)
			return;
		
		int filterCount = getInt("Filter Count", 1);
		boolean process = true;
		
		for (int filterIdx = 0; filterIdx < filterCount; filterIdx++) {
			Stream<ConditionInfo> possibleValues = ConditionInfo.getList().stream().filter((ConditionInfo f) -> {
				return ((f == ConditionInfo.IGNORED_FIELD) || (f == ConditionInfo.FILES));
			});
			String calculationMode = optionsAndResults.getStringSettingRadio(this, "Annotation Mode", ConditionInfo.TREATMENT.name(),
					StringManipulationTools.getStringListFromStream(possibleValues));
			
			ConditionInfo annotationMode = ConditionInfo.valueOf(calculationMode);
			
			String[] possibleValues1 = { "must contain", "must not contain" };
			String calculationMode1 = optionsAndResults.getStringSettingRadio(this, "Calculation Mode", "must contain",
					StringManipulationTools.getStringListFromArray(possibleValues1));
			
			String containMode = calculationMode1;
			
			String value = getString("Condition", "");
			
			String condition = processedImages.getAnyInfo().getParentSample().getParentCondition().getField(annotationMode);
			if (condition != null && containMode.equals("must not contain"))
				if (condition.equals(value))
					process = false;
			
			if (condition != null && containMode.equals("must contain"))
				if (!condition.equals(value))
					process = false;
		}
		
		if (!process) {
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
		return "Filter Images By Condition";
	}
	
	@Override
	public String getDescription() {
		return "Removes images due to specific experiment condition.";
	}
}

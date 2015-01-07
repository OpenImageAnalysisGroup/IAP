package iap.blocks.debug;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;

/**
 * @author pape
 */

public class BlFilterImagesByRegularExpression extends AbstractSnapshotAnalysisBlock {
	
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
		
		boolean process = true;
		
		String[] possibleValues1 = { "skip if matches", "skip if not matches" };
		String calculationMode1 = optionsAndResults.getStringSettingRadio(this, "Calculation Mode", "skip if matches",
				StringManipulationTools.getStringListFromArray(possibleValues1));
		
		Stream<ConditionInfo> possibleValues = ConditionInfo.getList().stream().filter((ConditionInfo f) -> {
			return ((f != ConditionInfo.IGNORED_FIELD) && (f != ConditionInfo.FILES));
		});
		
		String calculationMode = optionsAndResults.getStringSettingRadio(this, "Annotation Mode", ConditionInfo.TREATMENT.getNiceName(),
				StringManipulationTools.getStringListFromStream(possibleValues));
		
		ConditionInfo annotationMode = ConditionInfo.valueOfString(calculationMode);
		
		String regex = getString("Regex-base64", "");
		
		String condition = processedImages.getAnyInfo().getParentSample().getParentCondition().getField(annotationMode);
		
		Pattern pat = Pattern.compile(regex);
		Matcher match = pat.matcher(condition);
		
		if (condition != null && calculationMode1.equals("skip if not matches"))
			if (!match.find())
				process = false;
		
		if (condition != null && calculationMode1.equals("skip if matches"))
			if (match.find())
				process = false;
		
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
		return "Filter Images By Regular Expression";
	}
	
	@Override
	public String getDescription() {
		return "Removes images due to specific experiment condition filtered by regular expression. "
				+ "Example for filtering USA and GERMANY from Varity Coondition:"
				+ "Regex\'(USA|GERMANY)\' plus Calculation Mode 'contains' willl remove all plants "
				+ "which include 'USA' or 'GERMANY' in Varity Condition.";
	}
}

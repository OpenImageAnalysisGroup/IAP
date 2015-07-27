package iap.blocks.debug;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author pape, klukas
 */

public class BlFilterImagesByDate extends AbstractSnapshotAnalysisBlock {
	
	@Override
	public boolean isChangingImages() {
		return true;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		if (processedImages.getAnyInfo() == null || processedImages.getAnyInfo().getParentSample().getSampleFineTimeOrRowId() == null)
			return;
		
		Long p = processedImages.getAnyInfo().getParentSample().getSampleFineTimeOrRowId();
		Date d = new Date(p);
		
		Calendar gc = new GregorianCalendar();
		gc.setTime(d);
		int sampleDay = processedImages.getAnyInfo().getParentSample().getTime();
		int sampleHour = gc.get(GregorianCalendar.HOUR_OF_DAY);
		int sampleMinute = gc.get(GregorianCalendar.MINUTE);
		
		int day_a = getInt("Experiment Day A", 0);
		int hour_a = getInt("Hour A", 0);
		int min_a = getInt("Minute A", 0);
		
		int day_b = getInt("Experiment Day B", 0);
		int hour_b = getInt("Hour B", 0);
		int min_b = getInt("Minute B", 0);
		
		boolean filterBefore_a = getBoolean("Filter before A", false);
		boolean filterBetween = getBoolean("Filter between A and B", false);
		boolean filterAfter_b = getBoolean("Filter after B", false);
		
		boolean process = true;
		
		int sample_time = (int) (sampleDay * 24d * 60d + sampleHour * 60d + sampleMinute);
		int time_a = (int) (day_a * 24d * 60d + hour_a * 60d + min_a);
		int time_b = (int) (day_b * 24d * 60d + hour_b * 60d + min_b);
		
		if (filterBefore_a && sample_time < time_a)
			process = false;
		
		if (filterBetween && sample_time > time_a && sample_time < time_b)
			process = false;
		
		if (filterAfter_b && sample_time > time_b)
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
		return "Filter Images by Date";
	}
	
	@Override
	public String getDescription() {
		return "Removes images due to specific imaging timepoint.";
	}
}

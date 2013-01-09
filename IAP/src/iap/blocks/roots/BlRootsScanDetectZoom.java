package iap.blocks.roots;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Detect the zoom level of the scanned image.
 * If there is a lot of white area around the gray area, then the normal
 * wide zoom is detected. If nearly all is gray, then the image zoom is too big,
 * this is the zoom level which should have been avoided. If such image is processed,
 * the setting "options.isHighResVisCamera()" is set to TRUE.
 * 
 * @author klukas
 */
public class BlRootsScanDetectZoom extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().masks().vis();
		options.setHigherResVisCamera(false);
		if (img != null) {
			ResultsTableWithUnits rt = new ResultsTableWithUnits();
			rt.incrementCounter();
			
			int whitePixels = img.copy().io().invert().thresholdBlueHigherThan(3).print("WHITE AREA", debug).countFilledPixels();
			int allPixels = img.getWidth() * img.getHeight();
			
			if (whitePixels / (double) allPixels < 0.15d) {
				options.setHigherResVisCamera(true);
				rt.addValue("zoom", 1);
			} else
				rt.addValue("zoom", 0);
			
			getProperties().storeResults("RESULT_", rt, getBlockPosition());
		}
		
		return super.processVISmask();
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
	
}

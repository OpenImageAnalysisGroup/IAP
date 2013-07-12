package iap.blocks.acquisition;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author klukas, entzian
 */

public class BlLoadImagesIfNeeded extends
		AbstractSnapshotAnalysisBlock {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected void prepare() {
		if (input() != null) {
			if (input().images() != null)
				input().setImages(
						new ImageSet(input().images()));
			if (input().masks() != null) {
				input().setMasks(new ImageSet(input().masks()));
			}
			if (input().images().vis() == null
					&& input().images().getVisInfo() != null) {
				IOurl url = input().images().getVisInfo().getURL();
				try {
					Image fi = new Image(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						input().images().setVis(fi);
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: ERROR: VIS-MAIN: " + e.getMessage()
							+ " // " + url);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: VIS-MAIN: " + e.getMessage() + " // "
							+ url);
				}
			}
			
			if (input().images().fluo() == null
					&& input().images().getFluoInfo() != null) {
				IOurl url = input().images().getFluoInfo().getURL();
				try {
					Image fi = new Image(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						input().images().setFluo(fi);
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: ERROR: FLUO-MAIN: " + e.getMessage()
							+ " // " + url);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: EXCEPTION FLUO-MAIN: " + e.getMessage()
							+ " // " + url);
				}
			}
			
			if (input().images().nir() == null
					&& input().images().getNirInfo() != null) {
				IOurl url = input().images().getNirInfo().getURL();
				try {
					Image fi = new Image(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						input().images().setNir(fi);
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: ERROR: NIR-MAIN: " + e.getMessage()
							+ " // " + url);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: NIR-MAIN: " + e.getMessage() + " // "
							+ url);
				}
			}
			
			if (input().images().ir() == null
					&& input().images().getIrInfo() != null) {
				IOurl url = input().images().getIrInfo().getURL();
				try {
					Image fi = new Image(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						input().images().setIr(fi);
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: ERROR: IR-MAIN: " + e.getMessage()
							+ " // " + url);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: IR-MAIN: " + e.getMessage() + " // "
							+ url);
				}
			}
		}
		
		checkForStrangeTVtestImageAndReplaceWithNull();
	}
	
	private void checkForStrangeTVtestImageAndReplaceWithNull() {
		if (input().images().vis() != null) {
			// check for TV test image
			Image i = input().images().vis();
			if (i.getWidth() == getInt("TV_TEST_IMAGE_WIDTH", 768)
					&& i.getHeight() == getInt("TV_TEST_IMAGE_HEIGHT", 576)) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: VISIBLE IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().images().setVis(null);
			}
		}
		if (input().images().fluo() != null) {
			// check for TV test image
			Image i = input().images().fluo();
			if (i.getWidth() == getInt("TV_TEST_IMAGE_WIDTH", 768)
					&& i.getHeight() == getInt("TV_TEST_IMAGE_HEIGHT", 576)) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: FLUO IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().images().setFluo(null);
			}
		}
		if (input().images().nir() != null) {
			// check for TV test image
			Image i = input().images().nir();
			if (i.getWidth() == getInt("TV_TEST_IMAGE_WIDTH", 768)
					&& i.getHeight() == getInt("TV_TEST_IMAGE_HEIGHT", 576)) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: NIR IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().images().setNir(null);
			}
		}
		if (input().masks().vis() != null) {
			// check for TV test image
			Image i = input().masks().vis();
			if (i.getWidth() == getInt("TV_TEST_IMAGE_WIDTH", 768)
					&& i.getHeight() == getInt("TV_TEST_IMAGE_HEIGHT", 576)) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: VISIBLE REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().masks().setVis(null);
			}
		}
		if (input().masks().fluo() != null) {
			// check for TV test image
			Image i = input().masks().fluo();
			if (i.getWidth() == getInt("TV_TEST_IMAGE_WIDTH", 768)
					&& i.getHeight() == getInt("TV_TEST_IMAGE_HEIGHT", 576)) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: FLUO REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().masks().setFluo(null);
			}
		}
		if (input().masks().nir() != null) {
			// check for TV test image
			Image i = input().masks().nir();
			if (i.getWidth() == getInt("TV_TEST_IMAGE_WIDTH", 768)
					&& i.getHeight() == getInt("TV_TEST_IMAGE_HEIGHT", 576)) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: NIR REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().masks().setNir(null);
			}
		}
	}
	
	// @Override
	// protected void postProcess(FlexibleImageSet processedImages,
	// FlexibleImageSet processedMasks) {
	// super.postProcess(processedImages, processedMasks);
	// if (options != null) // && !options.isMaize()
	// if (processedImages != null
	// && processedImages.vis() != null
	// && processedImages.fluo() != null
	// && processedImages.vis().getHeight() > processedImages
	// .fluo().getHeight())
	// options.setHigherResVisCamera(true);
	// }
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.ACQUISITION;
	}
}

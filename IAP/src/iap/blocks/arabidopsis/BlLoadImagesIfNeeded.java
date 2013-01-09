package iap.blocks.arabidopsis;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author klukas, entzian
 */

public class BlLoadImagesIfNeeded extends
		AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected void prepare() {
		if (input() != null) {
			// synchronized (options) {
			if (input().images() != null)
				input().setImages(
						new FlexibleImageSet(input().images()));
			if (input().masks() != null) {
				input().setMasks(new FlexibleImageSet(input().masks()));
			}
			if (input().images().vis() == null
					&& input().images().getVisInfo() != null) {
				IOurl url = input().images().getVisInfo().getURL();
				try {
					FlexibleImage fi = new FlexibleImage(url);
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
					FlexibleImage fi = new FlexibleImage(url);
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
					FlexibleImage fi = new FlexibleImage(url);
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
					FlexibleImage fi = new FlexibleImage(url);
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
			FlexibleImage i = input().images().vis();
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
			FlexibleImage i = input().images().fluo();
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
			FlexibleImage i = input().images().nir();
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
			FlexibleImage i = input().masks().vis();
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
			FlexibleImage i = input().masks().fluo();
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
			FlexibleImage i = input().masks().nir();
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
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		return res;
	}
}

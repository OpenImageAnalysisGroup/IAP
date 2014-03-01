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
 * @author entzian, klukas
 */

public class BlLoadImages extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected boolean isChangingImages() {
		return true;
	}
	
	@Override
	protected void prepare() {
		if (input() != null) {
			if (input().images() != null)
				input().setImages(
						new ImageSet(input().images()));
			if (input().masks() != null)
				input()
						.setMasks(new ImageSet(input().masks()));
			
			boolean loadMasks = getBoolean("Load Reference Images", true);
			
			if (input().images().vis() == null
					&& input().images().getVisInfo() != null && getBoolean("Load VIS", true)) {
				IOurl url = input().images().getVisInfo().getURL();
				try {
					Image fi = new Image(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						input().images().setVis(fi);
					if (fi.getWidth() < 200)
						System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: LOW VIS RES: " + fi + " / " + url);
					
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: ERROR: VIS-MAIN: " + e.getMessage() + " // " + url);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: VIS-MAIN: " + e.getMessage() + " // " + url);
				}
				if (input().masks() != null && loadMasks) {
					url = input().images().getVisInfo().getLabelURL();
					if (url != null) {
						try {
							Image fi = new Image(url);
							if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
								input().masks().setVis(fi);
						} catch (Error e) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: ERROR: VIS-REFERENCE: " + e.getMessage() + " // " + url);
						} catch (Exception e) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: VIS-REFERENCE: " + e.getMessage() + " // " + url);
						}
					}
				}
			}
			
			if (input().images().fluo() == null
					&& input().images().getFluoInfo() != null && getBoolean("Load FLUO", true)) {
				IOurl url = input().images().getFluoInfo().getURL();
				try {
					Image fi = new Image(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						input().images().setFluo(fi);
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: ERROR: FLUO-MAIN: " + e.getMessage() + " // " + url);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: EXCEPTION FLUO-MAIN: " + e.getMessage() + " // " + url);
				}
				if (input().masks() != null && loadMasks) {
					url = input().images().getFluoInfo().getLabelURL();
					if (url != null) {
						try {
							Image fi = new Image(url);
							if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
								input().masks().setFluo(fi);
						} catch (Error e) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: ERROR: FLUO-REFERENCE: " + e.getMessage() + " // " + url);
						} catch (Exception e) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: FLUO-REFERENCE: " + e.getMessage() + " // " + url);
						}
					}
				}
			}
			
			if (input().images().nir() == null
					&& input().images().getNirInfo() != null && getBoolean("Load NIR", true)) {
				IOurl url = input().images().getNirInfo().getURL();
				try {
					Image fi = new Image(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						input().images().setNir(fi);
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: ERROR: NIR-MAIN: " + e.getMessage() + " // " + url);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: NIR-MAIN: " + e.getMessage() + " // " + url);
				}
				if (input().masks() != null && loadMasks) {
					url = input().images().getNirInfo().getLabelURL();
					if (url != null) {
						try {
							Image fi = new Image(url);
							if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
								input().masks().setNir(fi);
						} catch (Error e) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: ERROR: NIR-REFERENCE: " + e.getMessage() + " // " + url);
						} catch (Exception e) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: NIR-REFERENCE: " + e.getMessage() + " // " + url);
						}
					}
				}
			}
			
			if (input().images().ir() == null
					&& input().images().getIrInfo() != null && getBoolean("Load IR", true)) {
				IOurl url = input().images().getIrInfo().getURL();
				try {
					Image fi = new Image(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						input().images().setIr(fi);
				} catch (Error e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: ERROR: IR-MAIN: " + e.getMessage() + " // " + url);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">ERROR: IR-MAIN: " + e.getMessage() + " // " + url);
				}
				if (input().masks() != null && loadMasks) {
					url = input().images().getIrInfo().getLabelURL();
					if (url != null) {
						try {
							Image fi = new Image(url);
							if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
								input().masks().setIr(fi);
						} catch (Error e) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: ERROR: IR-REFERENCE: " + e.getMessage() + " // " + url);
						} catch (Exception e) {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">ERROR: IR-REFERENCE: " + e.getMessage() + " // " + url);
						}
					}
				}
			}
		}
		
		checkForStrangeTVtestImageAndReplaceWithNull();
	}
	
	private void checkForStrangeTVtestImageAndReplaceWithNull() {
		if (input().images().vis() != null) {
			Image i = input().images().vis();
			if (i.getWidth() < 200)
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: LOW VIS RES: " + i);
			if (i.getWidth() == getInt("TV-test-image-width", 768) && i.getHeight() == getInt("TV-test-image-height", 576)) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">ERROR: WARNING: VISIBLE IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().images().setVis(null);
			}
		}
		if (input().images().fluo() != null) {
			Image i = input().images().fluo();
			if (i.getWidth() < 200)
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: LOW FLUO RES: " + i);
			if (i.getWidth() == getInt("TV-test-image-width", 768) && i.getHeight() == getInt("TV-test-image-height", 576)) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">ERROR: WARNING: FLUO IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().images().setFluo(null);
			}
		}
		if (input().images().nir() != null) {
			Image i = input().images().nir();
			if (i.getWidth() < 200)
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: LOW NIR RES: " + i);
			if (i.getWidth() == getInt("TV-test-image-width", 768) && i.getHeight() == getInt("TV-test-image-height", 576)) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">ERROR: WARNING: NIR IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().images().setNir(null);
			}
		}
		if (input().masks().vis() != null) {
			Image i = input().masks().vis();
			if (i.getWidth() < 200)
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: LOW VIS MASK RES: " + i);
			if (i.getWidth() == getInt("TV-test-image-width", 768) && i.getHeight() == getInt("TV-test-image-height", 576)) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">ERROR: WARNING: VISIBLE REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().masks().setVis(null);
			}
		}
		if (input().masks().fluo() != null) {
			Image i = input().masks().fluo();
			if (i.getWidth() == getInt("TV-test-image-width", 768) && i.getHeight() == getInt("TV-test-image-height", 576)) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">ERROR: WARNING: FLUO REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().masks().setFluo(null);
			}
		}
		if (input().masks().nir() != null) {
			Image i = input().masks().nir();
			if (i.getWidth() < 200)
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: LOW NIR MASK RES: " + i);
			if (i.getWidth() == getInt("TV-test-image-width", 768) && i.getHeight() == getInt("TV-test-image-height", 576)) {
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">ERROR: WARNING: NIR REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				input().masks().setNir(null);
			}
		}
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		if (processedImages.vis() != null
				&& processedMasks.vis() != null
				&& processedImages.vis().getWidth() != processedMasks.vis().getWidth()) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ "> INPUT ERROR: IMAGE AND REFERENCE IMAGE HAVE DIFFERENT SIZE (VIS)");
			processedMasks.setVis(null);
		}
		if (processedImages.fluo() != null
				&& processedMasks.fluo() != null
				&& processedImages.fluo().getWidth() != processedMasks.fluo().getWidth()) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ "> INPUT ERROR: IMAGE AND REFERENCE IMAGE HAVE DIFFERENT SIZE (FLUO)");
			processedMasks.setFluo(null);
		}
		if (processedImages.nir() != null
				&& processedMasks.nir() != null
				&& processedImages.nir().getWidth() != processedMasks.nir().getWidth()) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ "> INPUT ERROR: IMAGE AND REFERENCE IMAGE HAVE DIFFERENT SIZE (NIR)");
			processedMasks.setNir(null);
		}
		if (processedImages.ir() != null
				&& processedMasks.ir() != null
				&& processedImages.ir().getWidth() != processedMasks.ir().getWidth()) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ "> INPUT ERROR: IMAGE AND REFERENCE IMAGE HAVE DIFFERENT SIZE (IR)");
			processedMasks.setNir(null);
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		return res;
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
		return BlockType.ACQUISITION;
	}
	
	@Override
	public String getName() {
		return "Load Images";
	}
	
	@Override
	public String getDescription() {
		return "Loads images and reference images from the source URLs.";
	}
}

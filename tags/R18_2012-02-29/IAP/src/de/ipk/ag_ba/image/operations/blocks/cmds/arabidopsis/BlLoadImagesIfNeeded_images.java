package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * @author klukas
 */

public class BlLoadImagesIfNeeded_images extends
		AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected void prepare() {
		if (getInput() != null) {
			// synchronized (options) {
			if (getInput().getImages() != null)
				getInput().setImages(
						new FlexibleImageSet(getInput().getImages()));
			if (getInput().getMasks() != null)
				getInput().setMasks(new FlexibleImageSet(getInput().getMasks()));
			
			if (getInput().getImages().getVis() == null
					&& getInput().getImages().getVisInfo() != null) {
				IOurl url = getInput().getImages().getVisInfo().getURL();
				try {
					FlexibleImage fi = new FlexibleImage(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						getInput().getImages().setVis(fi);
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
			
			if (getInput().getImages().getFluo() == null
					&& getInput().getImages().getFluoInfo() != null) {
				IOurl url = getInput().getImages().getFluoInfo().getURL();
				try {
					FlexibleImage fi = new FlexibleImage(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						getInput().getImages().setFluo(fi);
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
			
			if (getInput().getImages().getNir() == null
					&& getInput().getImages().getNirInfo() != null) {
				IOurl url = getInput().getImages().getNirInfo().getURL();
				try {
					FlexibleImage fi = new FlexibleImage(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						getInput().getImages().setNir(fi);
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
			
			if (getInput().getImages().getIr() == null
					&& getInput().getImages().getIrInfo() != null) {
				IOurl url = getInput().getImages().getIrInfo().getURL();
				try {
					FlexibleImage fi = new FlexibleImage(url);
					if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
						getInput().getImages().setIr(fi);
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
		if (getInput().getImages().getVis() != null) {
			// check for TV test image
			FlexibleImage i = getInput().getImages().getVis();
			if (i.getWidth() == 768 && i.getHeight() == 576) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: VISIBLE IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				getInput().getImages().setVis(null);
			}
		}
		if (getInput().getImages().getFluo() != null) {
			// check for TV test image
			FlexibleImage i = getInput().getImages().getFluo();
			if (i.getWidth() == 768 && i.getHeight() == 576) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: FLUO IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				getInput().getImages().setFluo(null);
			}
		}
		if (getInput().getImages().getNir() != null) {
			// check for TV test image
			FlexibleImage i = getInput().getImages().getNir();
			if (i.getWidth() == 768 && i.getHeight() == 576) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: NIR IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				getInput().getImages().setNir(null);
			}
		}
		if (getInput().getMasks().getVis() != null) {
			// check for TV test image
			FlexibleImage i = getInput().getMasks().getVis();
			if (i.getWidth() == 768 && i.getHeight() == 576) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: VISIBLE REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				getInput().getMasks().setVis(null);
			}
		}
		if (getInput().getMasks().getFluo() != null) {
			// check for TV test image
			FlexibleImage i = getInput().getMasks().getFluo();
			if (i.getWidth() == 768 && i.getHeight() == 576) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: FLUO REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				getInput().getMasks().setFluo(null);
			}
		}
		if (getInput().getMasks().getNir() != null) {
			// check for TV test image
			FlexibleImage i = getInput().getMasks().getNir();
			if (i.getWidth() == 768 && i.getHeight() == 576) {
				System.out
						.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: WARNING: NIR REF-IMAGE IS TV-TEST-IMAGE (set to null) !!!");
				getInput().getMasks().setNir(null);
			}
		}
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages,
			FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		if (options != null) // && !options.isMaize()
			if (processedImages != null
					&& processedImages.getVis() != null
					&& processedImages.getFluo() != null
					&& processedImages.getVis().getHeight() > processedImages
							.getFluo().getHeight())
				options.setHighResVisCamera(true);
	}
}

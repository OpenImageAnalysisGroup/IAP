package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlCreateDummyReferenceIfNeeded_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().images().vis() != null && input().masks().vis() == null) {
			FlexibleImage n = input().images().vis();
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().io().canvas().fillRect(0, 0, w, h, new Color(
					getInt("dummy-vis-background-color", 180),
					getInt("dummy-vis-background-color", 180),
					getInt("dummy-vis-background-color", 180)).getRGB()).getImage();
		}
		return super.processVISmask();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().images().fluo() != null && input().masks().fluo() == null)
			return input().images().fluo().copy().io().
					blur(getInt("dummy-fluo-blur", 2)).
					thresholdLAB(
							getInt("dummy-fluo-minL", 0), getInt("dummy-fluo-maxL", 50),
							getInt("dummy-fluo-minA", 0), getInt("dummy-fluo-maxA", 500),
							getInt("dummy-fluo-minB", 0), getInt("dummy-fluo-maxB", 155),
							ImageOperation.BACKGROUND_COLORint, CameraPosition.SIDE, false, false).
					blur(getInt("dummy-fluo-blur", 2)).
					getImage();
		else
			return super.processFLUOmask();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage n = input().images().nir();
		if (n != null && input().masks().nir() == null) {
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().io().canvas().fillRect(0, 0, w, h, new Color(
					getInt("dummy-nir-background-color", 180),
					getInt("dummy-nir-background-color", 180),
					getInt("dummy-nir-background-color", 180)).getRGB()).getImage();
		} else
			return super.processNIRmask();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
	}
}

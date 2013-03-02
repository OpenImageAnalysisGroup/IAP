package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Clears the fluo image, based on the vis mask.
 * 
 * @author Christian Klukas
 */
public class BlUseVisMaskToClearFluo_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().vis() == null || input().masks().fluo() == null)
			return input().masks().fluo();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			// apply enlarged VIS mask to fluo
			ImageOperation fluo = input().masks().fluo().copy().io().show("FLUO", debug);
			int b = (int) (input().masks().vis().getWidth() * 0.3);
			FlexibleImage mask = input().masks().vis().copy().io().
					addBorder(b, b / 2, (b / 2), options.getBackground()).
					crop(0.23, 0.03, 0.285, 0.09).
					blur(getDouble("blur", 25)).
					binary(Color.BLACK.getRGB(), options.getBackground()).show("blurred vis mask", debug).getImage();
			if (debug)
				fluo.copy().or(mask.copy()).show("ORR");
			return fluo.applyMask_ResizeMaskIfNeeded(
					mask,
					options.getBackground()).show("FILTERED VIS", debug).getImage();
		} else
			return input().masks().fluo();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		if (processedMasks.nir() == null || processedMasks.fluo() == null ||
				processedMasks.vis() == null) {
			processedMasks.setNir(input().masks().nir());
			return;
		}
		// if (options.getCameraPosition() == CameraPosition.TOP) {
		if (processedMasks.fluo() != null) {
			boolean printOR = false;
			
			if (printOR) {
				int w = input().masks().vis().getWidth();
				int h = input().masks().vis().getHeight();
				
				processedMasks.fluo().copy().resize(w, h).io().or(
						input().masks().vis()
						).show("OR operation", true);
			}
			// apply enlarged VIS mask to nir
			ImageOperation nir = processedMasks.nir().copy().io().show("NIRRRR", debug);
			FlexibleImage mask = processedMasks.vis().copy().io().or(
					input().masks().fluo()
					).show("OR operation", debug).blur(20).
					binary(Color.BLACK.getRGB(), options.getBackground()).show("blurred vis mask", debug).getImage();
			int gray = new Color(180, 180, 180).getRGB();
			int back = options.getBackground();
			processedMasks.setNir(nir.applyMask_ResizeMaskIfNeeded(
					mask,
					back).show("FILTERED NIR MASK", debug).getImage());
			processedImages.setNir(processedImages.nir().io().applyMask_ResizeMaskIfNeeded(
					mask,
					options.getBackground()).show("FILTERED NIR IMAGE", debug).
					replaceColor(back, gray).getImage());
			return;
		}
		// }
		
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
}

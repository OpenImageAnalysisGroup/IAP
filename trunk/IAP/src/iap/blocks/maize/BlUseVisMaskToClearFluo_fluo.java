package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.CameraType;

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
	protected Image processFLUOmask() {
		if (input().masks().vis() == null || input().masks().fluo() == null)
			return input().masks().fluo();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			// apply enlarged VIS mask to fluo
			ImageOperation fluo = input().masks().fluo().copy().io().show("FLUO", debug);
			int b = (int) (input().masks().vis().getWidth() * 0.3);
			Image mask = input().masks().vis().copy().io().
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
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
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
			Image mask = processedMasks.vis().copy().io().or(
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
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		return res;
	}
	
}

/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Klukas
 */
public class BlRemoveBlackBelt extends AbstractSnapshotAnalysisBlockFIS {
	ImageOperation blackBeltMask = null;
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		debug = getBoolean("debug", false);
		
		blackBeltMask = null;
		if (input().masks().vis() != null && input().images().vis() != null) {
			ImageOperation vis = input().masks().vis().copy().io();
			
			if (options.getCameraPosition() == CameraPosition.TOP) {
				// detect black belt
				vis = vis.print("Start image", debug).blur(getDouble("blur", 3)).print("blurred", debug)
						.filterRemoveLAB(
								getInt("belt-lab-l-min", 0), getInt("belt-lab-l-max", 130),
								getInt("belt-lab-a-min", 110), getInt("belt-lab-a-max", 130),
								getInt("belt-lab-b-min", 110), getInt("belt-lab-b-max", 140),
								options.getBackground(),
								false).print("LAB filtered", debug)
						.erode(getInt("erode-cnt", 10)).print("eroded", debug)
						.dilate(getInt("dilate-cnt", 23)).print("dilated", debug)
						.grayscale().print("Gray scale for threshold 100", debug)
						.threshold(100, options.getBackground(), new Color(100, 100, 100).getRGB()); // filter out black belt
				
				vis = vis.canvas()
						.fillRect(
								getInt("ignore-pot-x", 325),
								getInt("ignore-pot-y", 325),
								getInt("ignore-pot-w", 100),
								getInt("ignore-pot-h", 100),
								new Color(150, 150, 150).getRGB()
						)
						.fillCircle(
								getInt("small-circle-x", 325),
								getInt("small-circle-y", 703),
								getInt("small-circle-d", 30),
								options.getBackground(), 0d).io()
						.print("black belt region", debug);
				
				blackBeltMask = vis;
			}
		}
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = input().masks().vis();
		if (blackBeltMask == null || vis == null)
			return vis;
		vis = input().masks().vis().io().applyMask(blackBeltMask.getImage().copy(),
				options.getBackground()).getImage().display("Black belt removed from vis", debug);
		return vis;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = input().masks().fluo();
		if (blackBeltMask == null || fluo == null)
			return fluo;
		fluo = input().masks().fluo().io().applyMask_ResizeMaskIfNeeded(blackBeltMask.getImage().copy(),
				options.getBackground()).getImage().display("Black belt removed from fluo", debug);
		return fluo;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage nir = input().masks().nir();
		if (blackBeltMask == null || nir == null)
			return nir;
		nir = input().masks().nir().io().applyMask_ResizeMaskIfNeeded(blackBeltMask.getImage().copy(),
				options.getBackground()).getImage().display("Black belt removed from nir", debug);
		return nir;
	}
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
}

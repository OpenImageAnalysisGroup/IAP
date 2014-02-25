/**
 * 
 */
package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Klukas
 */
public class BlRemoveBlackBelt extends AbstractSnapshotAnalysisBlock {
	ImageOperation blackBeltMask = null;
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		debug = getBoolean("debug", false);
		
		blackBeltMask = null;
		if (input().masks().vis() != null && input().images().vis() != null) {
			ImageOperation vis = input().masks().vis().copy().io();
			
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				// detect black belt
				vis = vis.show("Start image", debug).blur(getDouble("blur", 3)).show("blurred", debug)
						.filterRemoveLAB(
								getInt("belt-lab-l-min", 0), getInt("belt-lab-l-max", 130),
								getInt("belt-lab-a-min", 110), getInt("belt-lab-a-max", 130),
								getInt("belt-lab-b-min", 110), getInt("belt-lab-b-max", 140),
								optionsAndResults.getBackground(),
								false).show("LAB filtered", debug)
						.erode(getInt("erode-cnt", 10)).show("eroded", debug)
						.dilate(getInt("dilate-cnt", 23)).show("dilated", debug)
						.grayscale().show("Gray scale for threshold 100", debug)
						.threshold(100, optionsAndResults.getBackground(), new Color(100, 100, 100).getRGB()); // filter out black belt
				
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
								optionsAndResults.getBackground(), 0d).io()
						.show("black belt region", debug);
				
				blackBeltMask = vis;
			}
		}
	}
	
	@Override
	protected Image processVISmask() {
		Image vis = input().masks().vis();
		if (blackBeltMask == null || vis == null)
			return vis;
		vis = input().masks().vis().io().applyMask(blackBeltMask.getImage().copy(),
				optionsAndResults.getBackground()).getImage().show("Black belt removed from vis", debug);
		return vis;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fluo = input().masks().fluo();
		if (blackBeltMask == null || fluo == null)
			return fluo;
		fluo = input().masks().fluo().io().applyMask_ResizeMaskIfNeeded(blackBeltMask.getImage().copy(),
				optionsAndResults.getBackground()).getImage().show("Black belt removed from fluo", debug);
		return fluo;
	}
	
	@Override
	protected Image processNIRmask() {
		Image nir = input().masks().nir();
		if (blackBeltMask == null || nir == null)
			return nir;
		nir = input().masks().nir().io().applyMask_ResizeMaskIfNeeded(blackBeltMask.getImage().copy(),
				optionsAndResults.getBackground()).getImage().show("Black belt removed from nir", debug);
		return nir;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Remove black transport belts (top images)";
	}
	
	@Override
	public String getDescription() {
		return "Uses color filtering to detect black belts and removes these areas from VIS/FLUO/NIR images.";
	}
}

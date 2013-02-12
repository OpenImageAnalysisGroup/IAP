/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Uses a lab-based pixel filter for the vis images.
 * 
 * @author Klukas
 */
public class BlLabFilter extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug;
	int LAB_MIN_L_VALUE_VIS, LAB_MAX_L_VALUE_VIS;
	int LAB_MIN_A_VALUE_VIS, LAB_MAX_A_VALUE_VIS;
	int LAB_MIN_B_VALUE_VIS, LAB_MAX_B_VALUE_VIS;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null || input().images().vis() == null)
			return null;
		else {
			ImageOperation visMask = input().masks().vis().io().copy();
			FlexibleImageStack fis = debug ? new FlexibleImageStack() : null;
			if (fis != null)
				fis.addImage("start", visMask.getImage(), null);
			
			if (getBoolean("Filter by HSV Color Space", false)) {
				visMask = visMask
						.blur(getDouble("Blur", 1))
						.filterRemoveHSV(
								getDouble("HSV-max-disctance", (1 / 2d - 1 / 3d)),
								getDouble("HSV-hue", (2 / 3d)),
								getDouble("HSV-max-lightness", (200d / 255d))); // filter out blue
				visMask = input().images().vis().io().copy().applyMask(
						visMask.closing(
								getInt("Closing-count-dilate", 2),
								getInt("Closing-count-erode", 4)).getImage(),
						options.getBackground());
				
				if (fis != null)
					fis.addImage("blue filtered by HSV", visMask.getImage(), null);
			}
			
			if (getBoolean("Filter by LAB Color Space", true)) {
				initLABfilterValues();
				
				visMask = visMask.filterRemoveLAB(
						LAB_MIN_L_VALUE_VIS,
						LAB_MAX_L_VALUE_VIS,
						LAB_MIN_A_VALUE_VIS,
						LAB_MAX_A_VALUE_VIS,
						LAB_MIN_B_VALUE_VIS,
						LAB_MAX_B_VALUE_VIS,
						options.getBackground(), false);
				
				if (fis != null)
					fis.addImage("main lab filter", visMask.getImage(), null);
			}
			return visMask.getImage().show("VISS", debug);
		}
	}
	
	private void initLABfilterValues() {
		if (options.isBarley()) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				LAB_MIN_L_VALUE_VIS = getInt("LAB_L_MIN", 100);
				LAB_MAX_L_VALUE_VIS = getInt("LAB_L_MAX", 255);
				LAB_MIN_A_VALUE_VIS = getInt("LAB_A_MIN", 0);
				LAB_MAX_A_VALUE_VIS = getInt("LAB_A_MAX", 135);
				LAB_MIN_B_VALUE_VIS = getInt("LAB_B_MIN", 123);
				LAB_MAX_B_VALUE_VIS = getInt("LAB_B_MAX", 255);
			} else {
				LAB_MIN_L_VALUE_VIS = getInt("LAB_L_MIN", 0);
				LAB_MAX_L_VALUE_VIS = getInt("LAB_L_MAX", 255);
				LAB_MIN_A_VALUE_VIS = getInt("LAB_A_MIN", 0);
				LAB_MAX_A_VALUE_VIS = getInt("LAB_A_MAX", 255);
				LAB_MIN_B_VALUE_VIS = getInt("LAB_B_MIN", 123);
				LAB_MAX_B_VALUE_VIS = getInt("LAB_B_MAX", 255);
			}
		} else
			if (options.isArabidopsis()) {
				if (options.getCameraPosition() == CameraPosition.TOP) {
					LAB_MIN_L_VALUE_VIS = getInt("LAB_L_MIN", 125);
					LAB_MAX_L_VALUE_VIS = getInt("LAB_L_MAX", 255);
					LAB_MIN_A_VALUE_VIS = getInt("LAB_A_MIN", 0);
					LAB_MAX_A_VALUE_VIS = getInt("LAB_A_MAX", 135);
					LAB_MIN_B_VALUE_VIS = getInt("LAB_B_MIN", 120);
					LAB_MAX_B_VALUE_VIS = getInt("LAB_B_MAX", 255);
				} else {
					LAB_MIN_L_VALUE_VIS = getInt("LAB_L_MIN", 0);
					LAB_MAX_L_VALUE_VIS = getInt("LAB_L_MAX", 255);
					LAB_MIN_A_VALUE_VIS = getInt("LAB_A_MIN", 0);
					LAB_MAX_A_VALUE_VIS = getInt("LAB_A_MAX", 255);
					LAB_MIN_B_VALUE_VIS = getInt("LAB_B_MIN", 123);
					LAB_MAX_B_VALUE_VIS = getInt("LAB_B_MAX", 255);
				}
			} else
				if (options.isMaize()) {
					if (options.getCameraPosition() == CameraPosition.TOP) {
						LAB_MIN_L_VALUE_VIS = getInt("LAB_L_MIN", 50 * 255 / 100);
						LAB_MAX_L_VALUE_VIS = getInt("LAB_L_MAX", 255);
						LAB_MIN_A_VALUE_VIS = getInt("LAB_A_MIN", 0);
						LAB_MAX_A_VALUE_VIS = getInt("LAB_A_MAX", 120);
						LAB_MIN_B_VALUE_VIS = getInt("LAB_B_MIN", 125);
						LAB_MAX_B_VALUE_VIS = getInt("LAB_B_MAX", 255);
					} else {
						LAB_MIN_L_VALUE_VIS = getInt("LAB_L_MIN", 0);
						LAB_MAX_L_VALUE_VIS = getInt("LAB_L_MAX", 255);
						LAB_MIN_A_VALUE_VIS = getInt("LAB_A_MIN", 0);
						LAB_MAX_A_VALUE_VIS = getInt("LAB_A_MAX", 255);
						LAB_MIN_B_VALUE_VIS = getInt("LAB_B_MIN", 122);
						LAB_MAX_B_VALUE_VIS = getInt("LAB_B_MAX", 255);
					}
				} else {
					LAB_MIN_L_VALUE_VIS = getInt("LAB_L_MIN", 0);
					LAB_MAX_L_VALUE_VIS = getInt("LAB_L_MAX", 255);
					LAB_MIN_A_VALUE_VIS = getInt("LAB_A_MIN", 0);
					LAB_MAX_A_VALUE_VIS = getInt("LAB_A_MAX", 255);
					LAB_MIN_B_VALUE_VIS = getInt("LAB_B_MIN", 122);
					LAB_MAX_B_VALUE_VIS = getInt("LAB_B_MAX", 255);
				}
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
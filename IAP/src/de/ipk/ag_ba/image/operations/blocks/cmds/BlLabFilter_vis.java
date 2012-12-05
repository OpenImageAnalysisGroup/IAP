/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;

/**
 * Uses a lab-based pixel filter for the vis images.
 * 
 * @author Klukas
 */
public class BlLabFilter_vis extends AbstractSnapshotAnalysisBlockFIS {
	
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
			boolean isOldBarley = false;
			if (options.isBarleyInBarleySystem()) {
				try {
					String db = input().images().getVisInfo().getParentSample().getParentCondition().getExperimentHeader().getDatabase();
					if (!LemnaTecDataExchange.known(db))
						isOldBarley = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			ImageOperation vis = input().masks().vis().io();
			FlexibleImageStack fis = debug ? new FlexibleImageStack() : null;
			if (fis != null)
				fis.addImage("start", vis.getImage());
			boolean austr = true;
			
			if (austr) {
				vis = vis
						.blur(getDouble("LAB-austr-blur", 1))
						.filterRemoveHSV(
								getDouble("LAB-austr-max-disctance", (1 / 2d - 1 / 3d)),
								getDouble("LAB-austr-color-hue", (2 / 3d)),
								getDouble("LAB-austr-max-lightness", (200d / 255d))); // filter out blue
				vis = input().images().vis().io().applyMask(
						vis.closing(
								getInt("LAB-austr-closing-number-dilate", 2),
								getInt("LAB-austr-closing-number-erode", 4)).getImage(),
						options.getBackground());
			} else
				vis = vis
						.blur(getDouble("LAB-blur", 0.5))
						.filterRemoveHSV(
								getDouble("LAB-max-disctance", (1 / 2d - 1 / 3d)),
								getDouble("LAB-color-hue", (2 / 3d))); // filter out blue
			if (fis != null)
				fis.addImage("blue filtered by HSV", vis.getImage());
			
			// very light yellow and green (background shadow, esp. in maize with 4 pot)
			if (!austr && options.isHigherResVisCamera()) {
				vis = vis.filterRemoveLAB(getIntArray("LAB-light-yellow-color", new Integer[] { 240, 255, 110, 120, 125, 135 }), options.getBackground(), true)
						.print("LIGHT BACKGROUND", debug);
				if (fis != null)
					fis.addImage("removed light white/green background", vis.getImage());
			}
			// gray pot remainings
			if (!austr && options.isBarleyInBarleySystem()) {
				vis = vis.filterRemoveLAB(getIntArray("LAB-gray-pot-color", new Integer[] { 180, 220, 118, 120, 126, 128 }), options.getBackground(), true)
						.print("LIGHT WHITE POT", debug);
				if (fis != null)
					fis.addImage("removed light white pot", vis.getImage());
			}
			// black pot
			if (!austr && options.isHigherResVisCamera()) {
				vis = vis.filterRemoveLAB(getIntArray("LAB-black-pot-color", new Integer[] { 0, 150, 122, 150, 100, 133 }), options.getBackground(), true)
						.print("BLACK POT", debug);
				if (fis != null)
					fis.addImage("removed black pot", vis.getImage());
			}
			
			initLABfilterValues();
			
			if (austr)
				vis = vis.filterRemoveLAB(
						LAB_MIN_L_VALUE_VIS,
						LAB_MAX_L_VALUE_VIS,
						LAB_MIN_A_VALUE_VIS,
						LAB_MAX_A_VALUE_VIS,
						LAB_MIN_B_VALUE_VIS,
						LAB_MAX_B_VALUE_VIS,
						options.getBackground(), false);
			else
				vis = vis.filterRemoveLAB(getIntArray("LAB-main-filter", new Integer[] { 179, 255, 122, 150, 109, 127 }), options.getBackground(), true);
			if (fis != null)
				fis.addImage("main lab filter", vis.getImage());
			
			if (!austr && isOldBarley) {
				vis = vis.filterRemoveHSV(
						getDouble("LAB-oldBarley-max-disctance", 0.005),
						getDouble("LAB-oldBarley-color-hue", 0.125),
						getDouble("LAB-oldBarley-max-lightness", 0.5))
						.print("FILTERED GRAY STICKS 1", debug); // filter out gray/silver old sticks
				if (fis != null)
					fis.addImage("removed gray sticks 1", vis.getImage());
			}
			
			if (!austr && isOldBarley) {
				vis = vis.filterRemoveHSV(
						getDouble("LAB-oldBarley-max-disctance2", 0.31),
						getDouble("LAB-oldBarley-color-hue2", 0.69))
						.print("FILTERED GRAY STICKS 3", debug); // filter out gray/silver old sticks
				if (fis != null)
					fis.addImage("removed gray sticks 3", vis.getImage());
			}
			if (fis != null)
				fis.print("lab filter block");
			
			return vis.getImage().print("VISS", debug);
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
}
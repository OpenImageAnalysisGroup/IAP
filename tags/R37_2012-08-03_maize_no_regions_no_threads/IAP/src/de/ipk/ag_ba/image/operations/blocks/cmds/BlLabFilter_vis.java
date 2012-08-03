/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
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
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null
				|| input().images().vis() == null)
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
			vis = vis.blur(0.5).filterRemoveHSV(1 / 2d - 1 / 3d, 2 / 3d); // filter out blue
			if (fis != null)
				fis.addImage("blue filtered by HSV", vis.getImage());
			
			int m = (225 - 26) / 2 + 26; // 125
			int m2 = (8 + 222) / 2 + 8; // 123
			int a1 = (15 + 2 - 5);
			
			int a2 = 10;
			int b1 = 14;
			int b2 = 4;// (4 - 5); // options.getUnitTestIdx()
			// very light yellow and green (background shadow, esp. in maize with 4 pot)
			if (options.isHighResMaize()) {
				vis = vis.filterRemoveLAB(240, 255, 110, 120, 125, 135, options.getBackground(), true).print("LIGHT BACKGROUND", debug);
				if (fis != null)
					fis.addImage("removed light white/green background", vis.getImage());
			}
			// gray pot remainings
			if (options.isBarleyInBarleySystem()) {
				vis = vis.filterRemoveLAB(180, 220, 118, 120, 126, 128, options.getBackground(), true).print("LIGHT WHITE POT", debug);
				if (fis != null)
					fis.addImage("removed light white pot", vis.getImage());
			}
			if (options.isHighResMaize()) { // black pot
				vis = vis.filterRemoveLAB(0, 150, m - a1, m + a2, 100, 133, options.getBackground(), true).print("BLACK POT", debug);
				if (fis != null)
					fis.addImage("removed black pot", vis.getImage());
			}
			// if (true)
			// vis.filterRemoveLAB(180, 255, m - a1, m + a2, m2 - b1, m2 + b2, options.getBackground(), false).print("OAEA");
			vis = vis.filterRemoveLAB(179, 255, m - a1, m + a2, m2 - b1, m2 + b2, options.getBackground(), true);
			if (fis != null)
				fis.addImage("main lab filter", vis.getImage());
			
			// if (isOldBarley)
			// vis = vis.filterRemoveHSV(0.017, 0.09).print("FILTERED GRAY STICKS 0", debug); // filter out gray/silver old sticks
			if (isOldBarley) {
				vis = vis.filterRemoveHSV(0.005, 0.125, 0.5).print("FILTERED GRAY STICKS 1", debug); // filter out gray/silver old sticks
				if (fis != null)
					fis.addImage("removed gray sticks 1", vis.getImage());
			}
			// if (isOldBarley)
			// vis = vis.filterRemoveHSV(0.017, 0.167).print("FILTERED GRAY STICKS 2", debug); // filter out gray/silver old sticks
			if (isOldBarley) { // from 0.37
				vis = vis.filterRemoveHSV(0.31, 0.69).print("FILTERED GRAY STICKS 3", debug); // filter out gray/silver old sticks
				if (fis != null)
					fis.addImage("removed gray sticks 3", vis.getImage());
			}
			if (fis != null)
				fis.print("lab filter block");
			
			return vis.getImage().print("VISS", debug);
		}
	}
}

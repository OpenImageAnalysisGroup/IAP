/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
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
		if (getInput().getMasks().getVis() == null
				|| getInput().getImages().getVis() == null)
			return null;
		else {
			boolean isOldBarley = false;
			if (options.isBarleyInBarleySystem()) {
				try {
					String db = getInput().getImages().getVisInfo().getParentSample().getParentCondition().getExperimentDatabaseId();
					if (!LemnaTecDataExchange.known(db))
						isOldBarley = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			ImageOperation vis = getInput().getMasks().getVis().getIO();
			vis = vis.filterRemoveHSV(1 / 2d - 1 / 3d, 2 / 3d); // filter out blue
			int m = (225 - 26) / 2 + 26; // 125
			int m2 = (8 + 222) / 2 + 8; // 123
			int a1 = 15;
			int a2 = 10;
			int b1 = 15;
			int b2 = 4;
			// very light yellow and green
			vis = vis.filterRemoveLAB(240, 255, 110, 120, 125, 135, options.getBackground(), true).print("LIGHT BACKGROUND", debug);
			// gray pot remainings
			if (options.isBarleyInBarleySystem())
				vis = vis.filterRemoveLAB(180, 220, 118, 120, 126, 128, options.getBackground(), true).print("LIGHT WHITE POT", debug);
			if (options.isHighResMaize()) // black pot
				vis = vis.filterRemoveLAB(0, 150, m - a1, m + a2, 100, 133, options.getBackground(), true).print("BLACK POT", debug);
			if (debug)
				vis.filterRemoveLAB(150, 255, m - a1, m + a2, m2 - b1, m2 + b2, options.getBackground(), false).print("OAEA");
			vis = vis.filterRemoveLAB(170, 255, m - a1, m + a2, m2 - b1, m2 + b2, options.getBackground(), true);
			// if (isOldBarley)
			// vis = vis.filterRemoveHSV(0.017, 0.09).print("FILTERED GRAY STICKS 0", debug); // filter out gray/silver old sticks
			if (isOldBarley)
				vis = vis.filterRemoveHSV(0.005, 0.125, 0.5).print("FILTERED GRAY STICKS 1", debug); // filter out gray/silver old sticks
			// if (isOldBarley)
			// vis = vis.filterRemoveHSV(0.017, 0.167).print("FILTERED GRAY STICKS 2", debug); // filter out gray/silver old sticks
			if (isOldBarley) // from 0.37
				vis = vis.filterRemoveHSV(0.31, 0.69).print("FILTERED GRAY STICKS 3", debug); // filter out gray/silver old sticks
			return vis.getImage().print("VISS", debug);
		}
	}
}

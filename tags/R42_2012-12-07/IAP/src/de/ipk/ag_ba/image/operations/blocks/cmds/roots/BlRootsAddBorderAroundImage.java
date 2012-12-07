package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Add border of N pixels around the images (visible)
 * 
 * @author klukas, entzian
 */
public class BlRootsAddBorderAroundImage extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage img = input().images().vis();
		int white = new Color(255, 255, 255).getRGB();
		if (img != null)
			img = img
					.io()
					.addBorder(
							getInt("BORDER_SIZE_VIS", 100),
							getInt("ROOT_TRANSLATE_X", 50),
							getInt("ROOT_TRANSLATE_Y", 50),
							white
					).getImage();
		return img;
	}
}

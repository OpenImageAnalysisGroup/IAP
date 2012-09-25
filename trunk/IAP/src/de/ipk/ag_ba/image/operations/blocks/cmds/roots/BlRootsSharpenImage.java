package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author klukas
 */
public class BlRootsSharpenImage extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	int b = new Color(255, 255, 255).getRGB();
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().images().vis();
		if (img != null)
			img = img.io().copy().blur(2).sharpen().sharpen().sharpen().getImage().print("RES");
		return img;
	}
}

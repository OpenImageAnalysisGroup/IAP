package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Add border of N pixels around the images (visible)
 * 
 * @author klukas, entzian
 */
public class BlRootsAddBorderAroundImage extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	// int b = new Color(255, 255, 255).getRGB();
	
	// @Override
	// protected FlexibleImage processVISmask() {
	// FlexibleImage img = input().masks().vis();
	// if (img != null)
	// img = img.io().addBorder(100, 50, 50, b).getImage();
	// return img;
	// }
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage img = input().images().vis();
		if (img != null)
			img = img
					.io()
					.addBorder(options.getIntSetting(Setting.ROOT_BORDER_SIZE_VIS), options.getIntSetting(Setting.ROOT_TRANSLATE_X),
							options.getIntSetting(Setting.ROOT_TRANSLATE_Y), options.getIntSetting(Setting.ROOT_BORDER_COLOR)).getImage();
		return img;
	}
}

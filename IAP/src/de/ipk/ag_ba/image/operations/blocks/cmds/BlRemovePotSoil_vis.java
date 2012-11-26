/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Klukas
 */
public class BlRemovePotSoil_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = input().masks().vis();
		if (input().masks().vis() != null && input().images().vis() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				ImageOperation mask = input().masks().vis().copy().io();
				mask = mask.blur(getDouble("blur", 2))
						.filterRemoveLAB(
								getInt("soil-lab-l-min", 90), getInt("soil-lab-l-max", 145),
								getInt("soil-lab-a-min", 120), getInt("soil-lab-a-max", 135),
								getInt("soil-lab-b-min", 125), getInt("soil-lab-b-max", 155),
								options.getBackground(),
								false)
						.erode(getInt("erode-cnt", 2))
						.dilate(getInt("dilate-cnt", 2))
						.grayscale()
						.threshold(100, options.getBackground(), new Color(100, 100, 100).getRGB())
						.print("soil region", debug);
				
				if (mask == null || vis == null)
					return vis;
				vis = input().masks().vis().io().applyMask(mask.getImage().copy(),
						options.getBackground()).getImage().print("Soil removed", debug);
			}
		}
		return vis;
	}
}

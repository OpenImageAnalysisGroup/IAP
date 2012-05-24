package de.ipk.ag_ba.image.operations.blocks.cmds.Barley;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.TranslationMatch;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Moves the Fluo in X/Y direction to better match the Vis image.
 * The Nir image is moved in X/Y direction, in Y direction only for barley, not for maize.
 * 
 * @author Christian Klukas
 */
public class BlTranslateMatch_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	private final boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		if (options.getCameraPosition() == CameraPosition.SIDE)
			if (input().masks().vis() != null) {
				ImageOperation vis = input().masks().vis().copy().io().grayscaleByLab();
				
				if (debug)
					vis.copy().canvas().drawSideHistogram().io().print("VIS");
				
				TranslationMatch tm = vis.prepareTranlationMatch(debug);
				
				if (input().masks().fluo() != null) {
					FlexibleImage fluo = input().masks().fluo().copy().io().getG().getImage();
					tm.calcOffsetVerticalY(fluo);
					tm.calcOffsetHorizontalX(fluo);
					
					if (tm.getOffsetVerticalY() < 0) {
						if (input().images().fluo() != null)
							input().images().setFluo(tm.translate(input().images().fluo()));
						
						input().masks().setFluo(tm.translate(input().masks().fluo()));
					} else {
						tm.setOffsetVerticalY(-tm.getOffsetVerticalY());
						if (input().images().vis() != null)
							input().images().setVis(tm.translate(input().images().vis()));
						
						input().masks().setVis(tm.translate(input().masks().vis()));
					}
				}
				
				if (input().images().nir() != null) {
					FlexibleImage nir = input().images().nir().copy().io().adaptiveThresholdForGrayscaleImage(50, 180, options.getBackground(), 0.1).
							applyMask_ResizeMaskIfNeeded(vis.blur(40).getImage().print("VIS IMAGE AS MASK", debug), Color.black.getRGB()).
							replaceColor(Color.black.getRGB(), options.getBackground()).
							getImage().print("NIR IMAGE FOR CALCULATION", debug);
					
					tm.calcOffsetVerticalY(nir);
					if (options.isBarleyInBarleySystem())
						tm.calcOffsetHorizontalX(nir);
					else
						tm.calcOffsetHorizontalX(null);
					
					input().images().setNir(tm.translate(input().images().nir()));
					
					if (input().masks().nir() != null)
						input().masks().setNir(tm.translate(input().masks().nir()));
				}
			}
	}
	
}

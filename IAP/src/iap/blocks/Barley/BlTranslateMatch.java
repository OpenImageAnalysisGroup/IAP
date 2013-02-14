package iap.blocks.Barley;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.TranslationMatch;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Moves the Fluo in X/Y direction to better match the Vis image.
 * The Nir image is moved in X/Y direction, in Y direction only for barley, not for maize.
 * 
 * @author Christian Klukas
 */
public class BlTranslateMatch extends AbstractSnapshotAnalysisBlockFIS {
	
	private boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		debug = getBoolean("debug", false);
		
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (input().masks().vis() != null) {
				ImageOperation vis = input().masks().vis().copy().io().grayscaleByLab();
				
				if (debug)
					vis.copy().canvas().drawSideHistogram().io().show("VIS");
				
				TranslationMatch tm = vis.prepareTranslationMatch(debug);
				
				if (input().masks().fluo() != null) {
					FlexibleImage fluo = input().masks().fluo().copy().io().getG().getImage();
					tm.calcOffsetVerticalY(fluo);
					tm.calcOffsetHorizontalX(fluo);
					
					boolean dontMoveDown = getBoolean("Dont_Move_Down", true);
					if (tm.getOffsetVerticalY() < 0 && dontMoveDown) {
						if (input().images().fluo() != null)
							input().images().setFluo(tm.translate(input().images().fluo()));
						
						input().masks().setFluo(tm.translate(input().masks().fluo()));
						
						ImageOperation v = input().masks().vis().io();
						int w = v.getWidth();
						int h = v.getHeight();
						double scale = input().images().vis().getHeight() / (double) input().images().fluo().getHeight();
						int o = -(int) (tm.getOffsetVerticalY() * scale);
						FlexibleImage vi = v.canvas().fillRect(0, h - o, w, o, options.getBackground()).getImage();
						input().masks().setVis(vi);
						// if (input().images().nir() != null) {
						// v = input().masks().nir().io();
						// w = v.getWidth();
						// h = v.getHeight();
						// scale = input().images().nir().getHeight() / (double) input().images().fluo().getHeight();
						// o = -(int) (2 * tm.getOffsetVerticalY() * scale);
						// FlexibleImage ni = v.canvas().fillRect(0, h - o, w, o, options.getBackground()).getImage();
						// input().masks().setNir(ni);
						//
						// v = input().images().nir().io();
						// ni = v.canvas().fillRect(0, h - o, w, o, options.getBackground()).getImage();
						// input().images().setNir(ni);
						// }
					} else {
						tm.setOffsetVerticalY(-tm.getOffsetVerticalY());
						if (input().images().vis() != null)
							input().images().setVis(tm.translate(input().images().vis()));
						
						input().masks().setVis(tm.translate(input().masks().vis()));
					}
				}
				
				if (input().images().nir() != null && options.isBarleyInBarleySystem()) {
					FlexibleImage nir = input().images().nir().copy().io().adaptiveThresholdForGrayscaleImage(50, 180, options.getBackground(), 0.1).
							applyMask_ResizeMaskIfNeeded(vis.blur(40).getImage().show("VIS IMAGE AS MASK", debug), Color.black.getRGB()).
							replaceColor(Color.black.getRGB(), options.getBackground()).
							getImage().show("NIR IMAGE FOR CALCULATION", debug);
					
					tm.calcOffsetVerticalY(nir);
					if (options.isBarleyInBarleySystem())
						tm.calcOffsetHorizontalX(nir);
					else
						tm.calcOffsetHorizontalX(null);
					
					boolean dontMoveUp = getBoolean("Dont_Move_NIR_Pot_Up", false);
					if (!dontMoveUp) {
						// don't move NIR pot up, only down
						if (tm.getOffsetVerticalY() < 0)
							tm.setOffsetVerticalY(0);
					}
					
					boolean dontMoveDown = getBoolean("Dont_Move_NIR_Pot_Down", false);
					if (!dontMoveDown) {
						input().images().setNir(tm.translate(input().images().nir().copy()));
						
						if (input().masks().nir() != null)
							input().masks().setNir(tm.translate(input().masks().nir()));
					}
				}
			}
		} // if side
		else {
			// top
			if (options.isBarleyInBarleySystem()) {
				if (input().masks().vis() != null || input().masks().vis() != null) {
					FlexibleImage vis = input().masks().vis();
					input().masks().setVis(vis.io()
							.translate(
									getInt("Move_Top_Vis_X", -18),
									getInt("Move_Top_Vis_Y", -8)).getImage());
					vis = input().images().vis();
					input().images().setVis(vis.io()
							.translate(
									getInt("Move_Top_Vis_X", -18),
									getInt("Move_Top_Vis_Y", -8)).getImage());
				}
			}
		}
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
}

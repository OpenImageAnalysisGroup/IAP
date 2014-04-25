package iap.blocks.auto;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.image_analysis_tools.imageJ.externalPlugins.bUnwarpJ.Param;
import iap.blocks.image_analysis_tools.imageJ.externalPlugins.bUnwarpJ.bUnwarpJ_;
import ij.ImagePlus;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * This block enables the automatically alignment of the mask images using the bUnwarpJ plugin (Fiji http://fiji.sc/BUnwarpJ). Beware of the resizing of the
 * fluorescence and near-infrared images, traits like height and width or area will be modified.
 * 
 * @author pape
 */
public class BlAutoAlignImages extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask.getCameraType() == CameraType.VIS)
			return mask;
		
		Image vis = input().masks().vis();
		Image fluo = input().masks().fluo();
		Image nir = input().masks().nir();
		
		if (vis == null || fluo == null || nir == null)
			return null;
		
		int back = ImageOperation.BACKGROUND_COLORint;
		int Vwidth = vis.getWidth();
		int Vheight = vis.getHeight();
		
		// mode mode accuracy mode (0 - Fast, 1 - Accurate, 2 - Mono)
		// img_subsamp_fact image subsampling factor (from 0 to 7, representing 2^0=1 to 2^7 = 128)
		// min_scale_deformation minimum scale deformation (0 - Very Coarse, 1 - Coarse, 2 - Fine, 3 - Very Fine)
		// max_scale_deformation maximum scale deformation (0 - Very Coarse, 1 - Coarse, 2 - Fine, 3 - Very Fine, 4 - Super Fine)
		// divWeight divergence weight
		// curlWeight curl weight
		// landmarkWeight landmark weight
		// imageWeight image similarity weight
		// consistencyWeight consistency weight
		// stopThreshold stopping threshold
		Param parm = new Param(1, 0, 0, 3, 0.0, 0.0, 0.0, 1.0, 10.0, 0.5);
		
		// change background to black and re-scale
		ImagePlus visWork = vis.copy().io().replaceColor(back, Color.BLACK.getRGB()).getImageAsImagePlus();
		
		if (mask.getCameraType() == CameraType.FLUO) {
			ImagePlus fluoWork = fluo.io().resize(Vwidth, Vheight).replaceColor(back, Color.BLACK.getRGB()).getImageAsImagePlus();
			ImagePlus[] vis_fluo = bUnwarpJ_.alignImagesBatch(visWork, fluoWork, visWork.getProcessor(), fluoWork.getProcessor(), parm);
			mask = new Image(Vwidth, Vheight, (int[]) vis_fluo[1].getStack().getPixels(2));
		}
		
		if (mask.getCameraType() == CameraType.NIR) {
			ImagePlus nirWork = nir.io().resize(Vwidth, Vheight).replaceColor(back, Color.BLACK.getRGB()).getImageAsImagePlus();
			ImagePlus[] vis_nir = bUnwarpJ_.alignImagesBatch(visWork, nirWork, visWork.getProcessor(), nirWork.getProcessor(), parm);
			mask = new Image(Vwidth, Vheight, (int[]) vis_nir[1].getStack().getPixels(2));
		}
		
		// replace imagej background (BLACK = -16777216) and black border
		mask = mask.io().replaceColor(-16777216, back).getImage();
		
		return mask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Test Block Auto Alignment";
	}
	
	@Override
	public String getDescription() {
		return "Block automatically align camera images related to the image of the visible spectra.\n Algorithm uses the Fiji \'bUnwarp\' plugin."
				+ "\n Beware of the resizing of the fluorescence and near-infrared images, traits like height and width or area will be modified.";
	}
}

package iap.blocks.auto;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.image_analysis_tools.imageJ.externalPlugins.bUnwarpJ.Param;
import iap.blocks.image_analysis_tools.imageJ.externalPlugins.bUnwarpJ.bUnwarpJ_;
import ij.ImagePlus;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * This block enables the automatically alignment (registration) of the mask images using the bUnwarpJ plugin (Fiji http://fiji.sc/BUnwarpJ). Beware of the
 * resizing of the fluorescence and near-infrared images, traits like height and width or area will be modified.
 * 
 * @author pape
 */
public class BlWarpImages extends AbstractBlock {
	
	@Override
	protected Image processFLUOmask() {
		// TODO Auto-generated method stub
		return super.processFLUOmask();
	}
	
	@Override
	protected Image processNIRmask() {
		// TODO Auto-generated method stub
		return super.processNIRmask();
	}
	
	@Override
	protected Image processIRmask() {
		// TODO Auto-generated method stub
		return super.processIRmask();
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask.getCameraType() == CameraType.VIS)
			return mask;
		
		Image vis = input().masks().vis();
		Image fluo = input().masks().fluo();
		Image nir = input().masks().nir();
		Image ir = input().masks().ir();
		
		if (vis == null)
			return mask;
		
		if (fluo == null && mask.getCameraType() == CameraType.FLUO)
			return null;
		
		if (nir == null && mask.getCameraType() == CameraType.NIR)
			return null;
		
		if (ir == null && mask.getCameraType() == CameraType.IR)
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
		Param parm = new Param(1, 0, 3, 4, 0.0, 0.0, 0.0, 1.0, 10.0, 0.5);
		
		// change background to white and re-scale
		ImagePlus visWork = vis.copy().io().getImageAsImagePlus();
		
		if (mask.getCameraType() == CameraType.FLUO) {
			mask = getWarpedImage(fluo, back, Vwidth, Vheight, parm, visWork);
		}
		
		if (mask.getCameraType() == CameraType.NIR) {
			mask = getWarpedImage(nir, back, Vwidth, Vheight, parm, visWork);
		}
		
		if (mask.getCameraType() == CameraType.IR) {
			mask = getWarpedImage(ir, back, Vwidth, Vheight, parm, visWork);
		}
		// replace imagej background (BLACK = -16777216) and black border
		mask = mask.io().replaceColor(0, back).getImage();
		
		return mask;
	}
	
	private Image getWarpedImage(Image img, int back, int Vwidth, int Vheight, Param parm, ImagePlus visWork) {
		Image mask;
		ImagePlus imgCopy = img.io().copy().resize(Vwidth, Vheight).getImageAsImagePlus();
		ImagePlus[] warped = bUnwarpJ_.alignImagesBatch(visWork, imgCopy, visWork.getProcessor(), imgCopy.getProcessor(), parm);
		if (true)
			for (ImagePlus i : warped) {
				i.setTitle(img.getCameraType().name());
				i.show();
			}
		mask = new Image(Vwidth, Vheight, (int[]) warped[0].getStack().getPixels(1));
		return mask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Auto Alignment (Warping)";
	}
	
	@Override
	public String getDescription() {
		return "Block automatically align camera images related to the image of the visible spectra.\n Algorithm uses the Fiji \'bUnwarp\' plugin."
				+ "\n Beware of the resizing of the fluorescence and near-infrared images, traits like height and width or area will be modified.";
	}
}

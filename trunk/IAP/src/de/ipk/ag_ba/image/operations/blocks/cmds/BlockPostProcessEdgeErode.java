package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public abstract class BlockPostProcessEdgeErode extends AbstractSnapshotAnalysisBlockFIS {
	
	private final boolean enlarge;
	
	/**
	 * Erode/delate/erode several times, depending on image type.
	 */
	public BlockPostProcessEdgeErode(boolean enlarge) {
		this.enlarge = enlarge;
		
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		return postProcessResultImage(getInput().getImages().getVis(), getInput().getMasks().getVis(), ImageConfiguration.RgbTop, enlarge);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return postProcessResultImage(getInput().getImages().getFluo(), getInput().getMasks().getFluo(), ImageConfiguration.FluoTop, enlarge);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return postProcessResultImage(getInput().getImages().getNir(), getInput().getMasks().getNir(), ImageConfiguration.NirTop, enlarge);
	}
	
	private FlexibleImage postProcessResultImage(FlexibleImage srcImage, FlexibleImage finalImage,
			ImageConfiguration typ, boolean enlarge) {
		
		ImageOperation maskIo = new ImageOperation(finalImage);
		
		switch (typ) {
			
			case RgbTop:
				if (enlarge)
					for (int ii = 0; ii < options.getPostProcessDilateRgbTop(); ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < options.getPostProcessErodeRgbTop(); ii++)
						maskIo.erode();
				break;
			
			case FluoTop:
				if (enlarge)
					for (int ii = 0; ii < options.getPostProcessDilateFluoTop(); ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < options.getPostProcessErodeFluoTop(); ii++)
						maskIo.erode();
				break;
			
			case NirTop:
				if (enlarge)
					for (int ii = 0; ii < options.getPostProcessDilateNirTop(); ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < options.getPostProcessErodeNirTop(); ii++)
						maskIo.erode();
				break;
			
		}
		
		return new ImageOperation(srcImage).applyMask2(maskIo.getImage(), options.getBackground()).getImage();
	}
}

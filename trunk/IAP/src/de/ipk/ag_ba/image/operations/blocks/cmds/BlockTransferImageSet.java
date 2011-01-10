package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public class BlockTransferImageSet extends AbstractImageAnalysisBlockFIS {
	
	@Override
	protected FlexibleMaskAndImageSet run() {
		return transferMaskToImageSet(options.isDebugOverlayResult());
	}
	
	private FlexibleMaskAndImageSet transferMaskToImageSet(boolean overlay) {
		if (overlay)
			return new FlexibleMaskAndImageSet(
					getInput().getImages().invert().draw(getInput().getMasks(), options.getBackground()), null);
		else
			return new FlexibleMaskAndImageSet(
					getInput().getMasks(), null);
		
	}
}

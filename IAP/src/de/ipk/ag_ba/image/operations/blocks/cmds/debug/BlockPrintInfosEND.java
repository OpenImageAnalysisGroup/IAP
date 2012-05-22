package de.ipk.ag_ba.image.operations.blocks.cmds.debug;

import java.util.HashMap;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */
public class BlockPrintInfosEND extends AbstractSnapshotAnalysisBlockFIS {
	
	public enum BlockInfoProperty {
		CONSOLE_OUTPUT, HEIGHT, WIDTH
	}
	
	HashMap<BlockInfoProperty, Boolean> infoMap = new HashMap<BlockInfoProperty, Boolean>() {
		{
			put(BlockInfoProperty.CONSOLE_OUTPUT, false);
			put(BlockInfoProperty.HEIGHT, true);
			put(BlockInfoProperty.WIDTH, true);
		}
	};
	
	@Override
	protected FlexibleImage processVISmask() {
		
		printInfo(input().masks().vis(), BlockPrintInfosTyp.VisMask);
		// printInfo(getInput().getImages().getVis(), BlockPrintInfosTyp.VisImage);
		
		printInfo(input().masks().fluo(), BlockPrintInfosTyp.FluoMask);
		// getProperties().setNumericProperty(getBlockPosition(), Property.HEIGHT_FLUO_MASK.toString(), );
		// printInfo(getInput().getImages().getFluo(), BlockPrintInfosTyp.FluoImage);
		
		printInfo(input().masks().nir(), BlockPrintInfosTyp.NirMask);
		// printInfo(getInput().getImages().getNir(), BlockPrintInfosTyp.NirImage);
		
		return input().masks().vis();
	}
	
	private void printInfo(FlexibleImage workImage, BlockPrintInfosTyp typ) {
		
		switch (typ) {
			case FluoImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_HEIGHT_FLUO_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_WIDTH_FLUO_IMAGE, workImage.getWidth());
				break;
			case FluoMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_HEIGHT_FLUO_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_WIDTH_FLUO_MASK, workImage.getWidth());
				break;
			case NirImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_HEIGHT_NIR_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_WIDTH_NIR_IMAGE, workImage.getWidth());
				break;
			case NirMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_HEIGHT_NIR_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_WIDTH_NIR_MASK, workImage.getWidth());
				break;
			case VisImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_HEIGHT_VIS_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_WIDTH_VIS_IMAGE, workImage.getWidth());
				break;
			case VisMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_HEIGHT_VIS_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.END_WIDTH_VIS_MASK, workImage.getWidth());
				break;
			
		}
		
		if (infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT)) {
			
			System.out.println(typ + " - Infos: ##### Start #####");
			
			if (infoMap.get(BlockInfoProperty.HEIGHT))
				System.out.println("height: " + workImage.getHeight());
			if (infoMap.get(BlockInfoProperty.WIDTH))
				System.out.println("width: " + workImage.getWidth());
			
			System.out.println(typ + " - Infos: **** End ****");
		}
	}
}

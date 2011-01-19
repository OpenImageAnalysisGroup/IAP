package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.util.HashMap;

import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */

public class BlockPrintInfos extends AbstractSnapshotAnalysisBlockFIS {
	
	public enum BlockInfoProperty {
		CONSOLE_OUTPUT, HEIGHT, WIDTH, CONSOLE_OUTPUT_VIS, CONSOLE_OUTPUT_FLUO, CONSOLE_OUTPUT_NIR
	}
	
	HashMap<BlockInfoProperty, Boolean> infoMap = new HashMap<BlockInfoProperty, Boolean>() {
		{
			put(BlockInfoProperty.CONSOLE_OUTPUT, false);
			put(BlockInfoProperty.CONSOLE_OUTPUT_VIS, false);
			put(BlockInfoProperty.CONSOLE_OUTPUT_FLUO, true);
			put(BlockInfoProperty.CONSOLE_OUTPUT_NIR, false);
			put(BlockInfoProperty.HEIGHT, true);
			put(BlockInfoProperty.WIDTH, true);
		}
	};
	
	@Override
	protected FlexibleImage processVISmask() {
		
		printInfo(getInput().getMasks().getVis(), BlockPrintInfosTyp.VisMask);
		printInfo(getInput().getImages().getVis(), BlockPrintInfosTyp.VisImage);
		
		printInfo(getInput().getMasks().getFluo(), BlockPrintInfosTyp.FluoMask);
		printInfo(getInput().getImages().getFluo(), BlockPrintInfosTyp.FluoImage);
		
		printInfo(getInput().getMasks().getNir(), BlockPrintInfosTyp.NirMask);
		printInfo(getInput().getImages().getNir(), BlockPrintInfosTyp.NirImage);
		
		return getInput().getMasks().getVis();
	}
	
	private void printInfo(FlexibleImage workImage, BlockPrintInfosTyp typ) {
		
		switch (typ) {
			case FluoImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_FLUO_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_FLUO_IMAGE, workImage.getWidth());
				break;
			case FluoMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_FLUO_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_FLUO_MASK, workImage.getWidth());
				break;
			case NirImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_NIR_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_NIR_IMAGE, workImage.getWidth());
				break;
			case NirMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_NIR_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_NIR_MASK, workImage.getWidth());
				break;
			case VisImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_VIS_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_VIS_IMAGE, workImage.getWidth());
				break;
			case VisMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_VIS_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_VIS_MASK, workImage.getWidth());
				break;
			
		}
		
		if (infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT)) {
			
			if (typ.name().contains("Vis") && infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT_VIS) ||
					typ.name().contains("Fluo") && infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT_FLUO) ||
					typ.name().contains("Nir") && infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT_NIR)) {
				
				System.out.println(typ + " - Infos: ##### Start #####");
				
				if (infoMap.get(BlockInfoProperty.HEIGHT))
					System.out.println("height: " + workImage.getHeight());
				if (infoMap.get(BlockInfoProperty.WIDTH))
					System.out.println("width: " + workImage.getWidth());
				
				System.out.println(typ + " - Infos: **** End ****");
			}
		}
	}
}

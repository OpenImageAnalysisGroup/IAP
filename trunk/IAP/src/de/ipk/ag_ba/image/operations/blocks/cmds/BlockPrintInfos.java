package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.util.HashMap;

import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */

public class BlockPrintInfos extends AbstractSnapshotAnalysisBlockFIS {
	
	HashMap<String, Boolean> infoMap = new HashMap<String, Boolean>() {
		{
			put("height", true);
			put("width", true);
		}
	};
	
	@Override
	protected FlexibleImage processVISmask() {
		
		printInfo(getInput().getMasks().getVis(), BlockPrintInfosTyp.VisMask);
		// printInfo(getInput().getImages().getVis(), BlockPrintInfosTyp.VisImage);
		
		printInfo(getInput().getMasks().getFluo(), BlockPrintInfosTyp.FluoMask);
		// printInfo(getInput().getImages().getFluo(), BlockPrintInfosTyp.FluoImage);
		
		printInfo(getInput().getMasks().getNir(), BlockPrintInfosTyp.NirMask);
		// printInfo(getInput().getImages().getNir(), BlockPrintInfosTyp.NirImage);
		
		return getInput().getMasks().getVis();
	}
	
	private void printInfo(FlexibleImage workImage, BlockPrintInfosTyp typ) {
		System.out.println(typ + " - Infos: ##### Start #####");
		
		if (infoMap.get("height"))
			System.out.println("height: " + workImage.getHeight());
		if (infoMap.get("width"))
			System.out.println("width: " + workImage.getWidth());
		
		System.out.println(typ + " - Infos: **** End ****");
	}
	
}

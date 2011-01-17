package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.util.HashMap;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockPrintInfos extends AbstractSnapshotAnalysisBlockFIS {
	
	HashMap<String, Boolean> infoMap = new HashMap<String, Boolean>() {
		{
			put("height", true);
			put("width", true);
		}
	};
	
	@Override
	protected FlexibleImage processVISmask() {
		System.out.println("VisMask - Infos: Start");
		
		printInfo(getInput().getMasks().getVis());
		
		System.out.println("VisMask - Infos: End");
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		System.out.println("FluoMask - Infos: Start");
		
		printInfo(getInput().getMasks().getFluo());
		
		System.out.println("FluoMask - Infos: End");
		return getInput().getMasks().getFluo();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		System.out.println("NirMask - Infos: Start");
		
		printInfo(getInput().getMasks().getNir());
		
		System.out.println("NirMask - Infos: End");
		return getInput().getMasks().getNir();
	}
	
	private void printInfo(FlexibleImage workImage) {
		if (infoMap.get("height"))
			System.out.println("height: " + workImage.getHeight());
		if (infoMap.get("width"))
			System.out.println("width: " + workImage.getWidth());
		
	}
	
}

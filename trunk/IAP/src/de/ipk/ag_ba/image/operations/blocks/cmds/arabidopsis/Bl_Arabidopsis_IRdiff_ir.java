package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.mongo.IAPservice;

/**
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class Bl_Arabidopsis_IRdiff_ir extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processIRimage() {
		FlexibleImage warmBack = getInput().getImages().getIr();
		FlexibleImage coldRef = getInput().getMasks().getIr();
		if (warmBack != null && coldRef != null) {
			double warmBackground = warmBack.getIO().intensitySumOfChannel(false, false, false, false) / warmBack.getIO().countFilledPixels();
			int[] res = coldRef.copy().getAs1A();
			for (int i = 0; i < res.length; i++)
				res[i] = IAPservice.getIRintensityDifferenceColor(
						IAPservice.getIRintenstityFromRGB(res[i], options.getBackground()) - warmBackground,
						options.getBackground());
			return new FlexibleImage(coldRef.getWidth(), coldRef.getHeight(), res);
		} else
			return null;
	}
}

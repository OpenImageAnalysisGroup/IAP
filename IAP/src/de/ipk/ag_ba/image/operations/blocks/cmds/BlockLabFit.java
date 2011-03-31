/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.ImageTyp;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */
public class BlockLabFit extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		// FlexibleImage test = labFit(getInput().getMasks().getVis(), getInput().getImages().getVis(), ImageTyp.RGB);
		// ImageOperation test2 = new ImageOperation(test);
		// test2.saveImage("/Users/" + System.getProperty("user.name") + "/Desktop/test8.png");
		return labFit(getInput().getMasks().getVis(), getInput().getImages().getVis(), ImageTyp.RGB);
		// return test;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		// ImageOperation test2 = new ImageOperation(getInput().getImages().getFluo());
		// test2.saveImage("/Users/" + System.getProperty("user.name") + "/Desktop/test10.png");
		//
		// FlexibleImage test = labFit(getInput().getMasks().getFluo(), getInput().getImages().getFluo(), ImageTyp.FLUO);
		// test2 = new ImageOperation(test);
		// test2.saveImage("/Users/" + System.getProperty("user.name") + "/Desktop/test9.png");
		
		return labFit(getInput().getMasks().getFluo(), getInput().getImages().getFluo(), ImageTyp.FLUO);
		// return test;
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	//
	// return labFit(getInput().getMasks().getNir(), getInput().getImages().getNir(), ImageTyp.NIR);
	// }
	//
	private FlexibleImage labFit(FlexibleImage workMask, FlexibleImage originalImage, ImageTyp typ) {
		
		int[][] workArray = workMask.getAs2A();
		int[][] originalArray = originalImage.getAs2A();
		int[][] resultMask = new int[workMask.getWidth()][workMask.getHeight()];
		
		// for (int i = 0; i < workMask.getWidth(); i++) {
		// for (int j = 0; j < workMask.getHeight(); j++) {
		// Color_CIE_Lab lab = new Color_CIE_Lab(workArray[i][j], false);
		// lab.setL(100.0);
		// resultMask[i][j] = lab.getRGB();
		// // resultMask[i][j] = lab.getColorXYZ().getColor().getRGB();
		// }
		// }
		// ImageOperation test3 = new ImageOperation(resultMask);
		// test3.saveImage("/Users/" + System.getProperty("user.name") + "/Desktop/test6.png");
		for (int i = 0; i < workMask.getWidth(); i++) {
			for (int j = 0; j < workMask.getHeight(); j++) {
				Color_CIE_Lab lab = new Color_CIE_Lab(workArray[i][j], false);
				
				switch (typ) {
					case RGB:

						if (lab.getA() < 20.0 && lab.getB() > 0.0)
							resultMask[i][j] = originalArray[i][j];
						else
							resultMask[i][j] = options.getBackground();
						break;
					
					case FLUO:
						// rot
						if ((lab.getL() >= 10 && lab.getA() >= 10 && lab.getB() >= 20) ||
								// organe
								// (lab.getL() >= 10 && lab.getA() > 0 && lab.getB() >= 75) ||
								// gelb
								(lab.getL() >= 10 && (lab.getA() <= -15 && lab.getA() >= -22) && lab.getB() >= 80))
							
							resultMask[i][j] = originalArray[i][j];
						else
							resultMask[i][j] = options.getBackground();
						
						break;
					
					case NIR:

						if (lab.getA() < 20.0 && lab.getB() > 0.0)
							resultMask[i][j] = originalArray[i][j];
						else
							resultMask[i][j] = options.getBackground();
						break;
				}
				
				// resultMask[i][j] = lab.getColorXYZ().getColor().getRGB();
			}
		}
		
		// ImageOperation.showTwoImagesAsOne(originalImage.getAsBufferedImage(), ImageConverter.convert2AtoBI(resultMask), true);
		
		return new FlexibleImage(resultMask);
	}
}

/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Sep 12, 2010 by Christian Klukas
 */

package imaging_tools;

import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.ColorQuantizerDescriptor;

/**
 * @author klukas
 */
@SuppressWarnings("restriction")
public class ImageTools {
	
	public PlanarImage getPlanarImage(BufferedImage image) {
		return PlanarImage.wrapRenderedImage(image);
	}
	
	/**
	 * @param image
	 * @param maxColors
	 *           "2","4","8","16","32","64","128"
	 * @return
	 */
	public PlanarImage reduceColors(PlanarImage image, int maxColors) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);
		pb.add(ColorQuantizerDescriptor.MEDIANCUT);
		// pb.add(ColorQuantizerDescriptor.NEUQUANT);
		// pb.add(ColorQuantizerDescriptor.OCTTREE);
		pb.add(maxColors); // "2","4","8","16","32","64","128"
		pb.add(32768); // mediancut
		pb.add(100); // neuquant
		pb.add(65536); // octtree
		pb.add(null); // the ROI
		pb.add(1);
		pb.add(1); // the period
		PlanarImage quantizedImage = JAI.create("colorquantizer", pb);
		return quantizedImage;
	}
	
	public BufferedImage reduceColors(BufferedImage image, int maxColors) {
		return reduceColors(getPlanarImage(image), maxColors).getAsBufferedImage();
	}
	
	public PlanarImage rotate(PlanarImage image, double radian) {
		float angle = (float) radian;
		float centerX = image.getWidth() / 2f;
		float centerY = image.getHeight() / 2f;
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);
		pb.add(centerX);
		pb.add(centerY);
		pb.add(angle);
		pb.add(new InterpolationBilinear());
		PlanarImage scaledImage = JAI.create("rotate", pb);
		return scaledImage;
	}
	
	public PlanarImage smooth(PlanarImage input, int kernelSize) {
		float[] kernelMatrix = new float[kernelSize * kernelSize];
		for (int k = 0; k < kernelMatrix.length; k++)
			kernelMatrix[k] = 1.0f / (kernelSize * kernelSize);
		KernelJAI kernel = new KernelJAI(kernelSize, kernelSize, kernelMatrix);
		PlanarImage output = JAI.create("convolve", input, kernel);
		return output;
	}
	
	public PlanarImage edgeDetectionSoebel(PlanarImage input) {
		float[] kernelMatrix = { -1, -2, -1, 0, 0, 0, 1, 2, 1 };
		KernelJAI kernel = new KernelJAI(3, 3, kernelMatrix);
		PlanarImage output = JAI.create("convolve", input, kernel);
		return output;
	}
	
	public PlanarImage invertRGBimage(PlanarImage input) {
		PlanarImage output = JAI.create("bandselect", input, new int[] { 2, 1, 0 });
		return output;
	}
}

package de.ipk.ag_ba.image_utils;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class ImageOperation extends ImageConverter {
	
	private ImagePlus image;
	private ImageProcessor processor;

	
	public static void main(String[] args) {
		try {
			IOurl url = new IOurl("http://www.spiegel.de/images/image-150632-panoV9free-hldq.jpg");
			BufferedImage img = ImageIO.read(ResourceIOManager.getInputStream(url));
//			ImagePlus testImage = ImageConverter.convertBItoIJ(img);
		
			ImageOperation test = new ImageOperation(img);
			
			test.rotate(5);
//			test.resize(2.0);
			
			test.printImage();
			GravistoService.showImage(test.getImageAsBufferedImage(), "Test");
//			testImage.show();
//			Benchmark test = new Benchmark();
//			test.setup(null, testImage);
//			test.run(testImage.getProcessor());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ImageOperation(ImagePlus image) {
		this.image = image;
		this.processor = image.getProcessor();
	}
	
	public ImageOperation(BufferedImage image) {
		this(ImageConverter.convertBItoIJ(image));
	}
	
	public ImageOperation(int[] image, int width, int height) {
		this(ImageConverter.convert1AtoIJ(width, height, image));
	}

	public ImageOperation(int[][] image) {
		this(ImageConverter.convert2AtoIJ(image));
	}

	public ImageOperation(float[][] image) {
		this(new ImagePlus("JImage", new FloatProcessor(image)));
	}
	
	public void translate(){
		
		
		
		
	}
	
	public void rotate(double degree) {
		processor.rotate(degree);

	}

	public void scale(double xScale, double yScale) {
		processor.scale(xScale, yScale);
	}

	public void resize(int width, int height) {
		processor = processor.resize(width, height);
		image.setProcessor(processor);
	}
	
	public void resize(double factor) {
		resize((int) (factor * image.getWidth()), (int) (factor * image.getHeight()));
	}
	
	// get...

	public int[] getImageAs1array() {
		return ImageConverter.convertIJto1A(image);
	}
	
	public int[][] getImageAs2array() {
		return ImageConverter.convertIJto2A(image);
	}
	
	public BufferedImage getImageAsBufferedImage() {
		return ImageConverter.convertIJtoBI(image);
	}

	public ImagePlus getImageAsImagePlus() {
		return image;
	}
	
	// print
	
	public void printImage(){
		image.show();	
	}
	
}

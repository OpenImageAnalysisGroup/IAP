package de.ipk.ag_ba.image.operations;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;

public class ImageOperationTest {
	public static void main(String[] args) {
		
		boolean newTest = true;
		
		if (newTest) {
			
			int[][] img = { { 1, 1, 1, 1, 1, 1, 1 },
								{ 1, 1, 1, 1, 1, 1, 1 },
								{ 1, 1, 1, 1, 1, 1, 1 },
								{ 1, 1, 1, 1, 1, 1, 1 },
								{ 1, 1, 1, 1, 1, 1, 1 } };
			
			IOurl test = new IOurl("file:///Users/entzian/Desktop/test.png");
			try {
				BufferedImage imgTest = ImageIO.read(test.getInputStream());
				// imgTest = ImageConverter.convert2AtoBI(img);
				// PrintImage.printImage(imgTest, PrintOption.CONSOLE);
				PrintImage.printImage(imgTest, "entzian main 1");
				ImageOperation io = new ImageOperation(imgTest);
				// io.drawRect(3, 3, 10, 10);
				// io.drawAndFillRect(3, 3, 10, 10, 0);
				// io.setBackgroundValue(-1);
				// Roi testRoi = io.boundingBox();
				// io.drawBoundingBox(testRoi);
				// io.cutArea(testRoi);
				// io.drawBoundingBox(io.getBoundingBox());
				// PrintImage.printImage(io.getImageAsBufferedImage());
				// Dimension2D testPoint = io.enlarge().getDiameter();
				// io.cutArea(io.boundingBox());
				
				PrintImage.printImage(io.enlarge().getImageAsBufferedImage(), "entzian main 2");
				// io.centerOfGravity();
				PrintImage.printImage(io.getImageAsBufferedImage(), "entzian main 3");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			
			boolean tempTests = false;
			if (tempTests) {
				try {
					IOurl test;
					boolean zeigen1 = false;
					
					if (zeigen1)
						test = new IOurl("file:///Users/entzian/Desktop/test.png");
					else
						test = new IOurl("file:///Users/entzian/Desktop/nir_test.png");
					
					// IOurl test = new
					// IOurl("mongo://26b7e285fae43dac107016afb4dc2841/WT01_1385");
					// ResourceIOManager.registerIOHandler(new MongoDBhandler());
					
					// ResourceIOManager.registerIOHandler(new
					// LemnaTecFTPhandler());
					BufferedImage imgTest = ImageIO.read(test.getInputStream());
					
					double scale = 2.0;
					if (Math.abs(scale - 1) > 0.0001) {
						System.out.println("Scaling!");
						imgTest = new ImageOperation(imgTest).resize(scale).getImageAsBufferedImage();
					}
					
					GravistoService.showImage(imgTest, "Ausgang");
					
					int[] fluoImage = ImageConverter.convertBIto1A(imgTest);
					
					if (zeigen1) {
						
						ImageOperation io1 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io1.dilate();
						GravistoService.showImage(io1.getImageAsBufferedImage(), "Dilation");
						
						ImageOperation io2 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io2.erode();
						GravistoService.showImage(io2.getImageAsBufferedImage(), "Erode");
						
						// ImageOperation io2 = new ImageOperation(fluoImage,
						// imgTest.getWidth(), imgTest.getHeight());
						// io2.erode(new int[][] {{1,1,1},{1,1,1},{1,1,1}});
						// GravistoService.showImage(io2.getImageAsBufferedImage(),
						// "Erode1");
						//
						// ImageOperation io2_1 = new ImageOperation(fluoImage,
						// imgTest.getWidth(), imgTest.getHeight());
						// io2_1.erode2(new int[][] {{1,1,1},{1,1,1},{1,1,1}});
						// GravistoService.showImage(io2_1.getImageAsBufferedImage(),
						// "Erode2");
						
						ImageOperation io3 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io3.opening();
						GravistoService.showImage(io3.getImageAsBufferedImage(), "Opening");
						
						ImageOperation io4 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io4.closing();
						GravistoService.showImage(io4.getImageAsBufferedImage(), "Closing");
						
						ImageOperation io5 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io5.outline();
						GravistoService.showImage(io5.getImageAsBufferedImage(), "Outline");
						
						ImageOperation io5_2 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io5_2.outline2();
						GravistoService.showImage(io5_2.getImageAsBufferedImage(), "Outline2");
						
						ImageOperation io5_1 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io5_1.outline(new int[][] { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } });
						GravistoService.showImage(io5_1.getImageAsBufferedImage(), "Outline1");
						
						// ImageOperation io5_3 = new ImageOperation(fluoImage,
						// imgTest.getWidth(), imgTest.getHeight());
						// io5_3.outline(new int[][] {{0,1,0},{1,1,1},{0,1,0}});
						// GravistoService.showImage(io5_3.getImageAsBufferedImage(),
						// "Outline1_2");
						//
						// ImageOperation io5_4 = new ImageOperation(fluoImage,
						// imgTest.getWidth(), imgTest.getHeight());
						// io5_4.outline(new int[][] {{0,1,0},{1,2,1},{0,1,0}});
						// GravistoService.showImage(io5_4.getImageAsBufferedImage(),
						// "Outline1_3");
						
						ImageOperation io6 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io6.skeletonize();
						GravistoService.showImage(io6.getImageAsBufferedImage(), "Skeletonize");
						
						ImageOperation io7 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io7.threshold(254);
						GravistoService.showImage(io7.getImageAsBufferedImage(), "Threshold");
						
						ImageOperation io8 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io8.gamma(2.0);
						GravistoService.showImage(io8.getImageAsBufferedImage(), "Gamma");
						
					} else {
						
						ImageOperation io10 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						
						// io10.skeletonize();
						io10.outline(new int[][] { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } });
						io10.gamma(3.5);
						for (int i = 0; i < 1; i++)
							io10.dilate();
						// io10.threshold(153);
						// io10.opening(new int[][] { { 1, 1, 1 }, { 1, 1, 1 },
						// { 1, 1, 1 } });
						
						// io10.blur();
						
						GravistoService.showImage(io10.getImageAsBufferedImage(), "Ergebnis");
						
					}
					// io.printImage();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} else {
				
				try {
					IOurl urlFlu = new IOurl("mongo_ba-13.ipk-gatersleben.de://26b7e285fae43dac107016afb4dc2841/WT01_1385");
					IOurl urlVis = new IOurl("mongo_ba-13.ipk-gatersleben.de://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
					IOurl urlNIR = new IOurl("mongo_ba-13.ipk-gatersleben.de://c72e4fcc141b8b2a97851ab2fde8106a/WT01_1385");
					
					ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
					
					for (MongoDB m : MongoDB.getMongos())
						for (ResourceIOHandler io : m.getHandlers())
							ResourceIOManager.registerIOHandler(io);
					
					BufferedImage imgFluo = ImageIO.read(urlFlu.getInputStream());
					BufferedImage imgVisible = ImageIO.read(urlVis.getInputStream());
					BufferedImage imgNIR = ImageIO.read(urlNIR.getInputStream());
					
					double scale = 1.0;
					if (Math.abs(scale - 1) > 0.0001) {
						System.out.println("Scaling!");
						imgFluo = new ImageOperation(imgFluo).resize(scale).getImageAsBufferedImage();
						imgVisible = new ImageOperation(imgVisible).resize(scale).getImageAsBufferedImage();
					}
					
					// resize
					int[] fluoImage = ImageConverter.convertBIto1A(imgFluo);
					
					ImageOperation io = new ImageOperation(fluoImage, imgFluo.getWidth(), imgFluo.getHeight());
					io.resize(imgVisible.getWidth(), imgVisible.getHeight());
					io.scale(0.95, 0.87);
					io.translate(0, -15 * scale);
					io.rotate(-3);
					
					imgFluo = ImageConverter
										.convert1AtoBI(imgVisible.getWidth(), imgVisible.getHeight(), io.getImageAs1array());
					
					boolean mergeImages = false;
					
					if (mergeImages)
						ImageOperation.showTwoImagesAsOne(imgFluo, imgVisible, false);
					
					boolean test = true;
					if (test) {
						ImageOperation.testPhytokammer(urlFlu, urlVis, urlNIR, imgFluo, imgVisible, imgNIR);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}

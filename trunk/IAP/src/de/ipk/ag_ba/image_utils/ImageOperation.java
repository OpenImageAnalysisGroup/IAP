package de.ipk.ag_ba.image_utils;

import ij.ImagePlus;
import ij.process.BinaryProcessor;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.lowagie.text.ImgRaw;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.mongo.MongoDBhandler;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk.ag_ba.rmi_server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedImage;

public class ImageOperation extends ImageConverter {

	private ImagePlus image;
	private ImageProcessor processor;

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

	// ############# Operations ##############

	// public void setFilter(filterTyp typ){
	// switch(typ){
	//
	// case MEDIAN_FILTER: processor.m
	//
	//
	// }
	//
	// }

	public void translate(double x, double y) {
		processor.translate(x, y);
	}

	public void rotate(double degree) {
		processor.rotate(degree);

	}

	public void scale(double xScale, double yScale) {
		processor.scale(xScale, yScale);
	}

	public ImageOperation resize(int width, int height) {
		processor = processor.resize(width, height);
		image.setProcessor(processor);
		return this;
	}

	public ImageOperation resize(double factor) {
		return resize((int) (factor * image.getWidth()),
				(int) (factor * image.getHeight()));
	}

	public void threshold(int cutValue) {
		ImageProcessor processor2 = processor.convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) processor2);
		byteProcessor.threshold(cutValue);
		image.setProcessor(processor2.convertToRGB());
	}

	public void dilate(int[][] mask) {
		int jM = (mask.length - 1) / 2;
		int iM = (mask[0].length - 1) / 2;

		ImageProcessor tempImage = processor.createProcessor(
				processor.getWidth(), processor.getHeight());

		for (int j = 0; j < mask.length; j++)
			for (int i = 0; i < mask[j].length; i++)
				tempImage.copyBits(processor, i - iM,j-jM, Blitter.MAX);
		
//		for (int i = 0; i < mask.length; i++)
//			for (int j = 0; j < mask[i].length; j++)
//				tempImage.copyBits(processor, j - jM, i - iM, Blitter.MAX);

		processor.copyBits(tempImage, 0, 0, Blitter.COPY);
	}

	public ImageOperation dilate() { // es wird der 3x3 Minimum-Filter genutzt
		processor.dilate();
		return this;
	}

	public ImageOperation erode(ImageProcessor temp, int[][] mask) {
		temp.invert();
		dilate(mask);
		temp.invert();
		return this;
	}

	public ImageOperation erode(ImageProcessor temp) {
		temp.erode();
		return this;
	}

	public ImageOperation erode(int[][] mask) {
		return erode(processor, mask);
	}

	// public void erode2(int [][] mask){
	// processor.invert();
	// dilate(mask);
	// processor.invert();
	// }

	public ImageOperation erode() { // es wird der 3x3 Minimum-Filter genutzt
		return erode(processor);
	}

	public void closing(int[][] mask) {
		dilate(mask);
		erode(mask);
	}

	public void closing() { // es wird der 3x3 Minimum-Filter genutzt
		processor.dilate();
		processor.erode();
	}

	public void opening(int[][] mask) {
		erode(mask);
		dilate(mask);
	}

	public void opening() { // es wird der 3x3 Minimum-Filter genutzt
		processor.erode();
		processor.dilate();
	}

	public void skeletonize() {
		ImageProcessor processor2 = processor.convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) processor2);
		byteProcessor.skeletonize();
		image.setProcessor(processor2.convertToRGB());
	}

	public void outline(int[][] mask) { // starke Farbübergänge werden als Kante
										// erkannt
		ImageProcessor tempImage = processor.duplicate();
		erode(tempImage, mask);
		processor.copyBits(tempImage, 0, 0, Blitter.DIFFERENCE);
		processor.invert();
	}

	public void outline() {
		ImageProcessor processor2 = processor.convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) processor2);
		byteProcessor.outline();
		image.setProcessor(processor2.convertToRGB());
	}

	public void outline2() {
		ImageProcessor tempImage = processor.duplicate();
		erode(tempImage);
		processor.copyBits(tempImage, 0, 0, Blitter.DIFFERENCE);
		processor.invert();

	}

	public void gamma(double value) {
		processor.gamma(value);
	}

	// ################## get... ###################

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

	// ############# print #####################

	public void printImage() {
		image.updateAndDraw();
		image.show();
	}

	public static void main(String[] args) {

		boolean tempTests = false;
		if (tempTests) {
			try {
				IOurl test;
				boolean zeigen1 = false;

				if (zeigen1)
					test = new IOurl("file:///Users/entzian/Desktop/test.png");
				else
					test = new IOurl(
							"file:///Users/entzian/Desktop/nir_test.png");

				// IOurl test = new
				// IOurl("mongo://26b7e285fae43dac107016afb4dc2841/WT01_1385");
				// ResourceIOManager.registerIOHandler(new MongoDBhandler());

				// ResourceIOManager.registerIOHandler(new
				// LemnaTecFTPhandler());
				BufferedImage imgTest = ImageIO.read(test.getInputStream());

				double scale = 2.0;
				if (Math.abs(scale - 1) > 0.0001) {
					System.out.println("Scaling!");
					imgTest = new ImageOperation(imgTest).resize(scale)
							.getImageAsBufferedImage();
				}

				GravistoService.showImage(imgTest, "Ausgang");

				int[] fluoImage = ImageConverter.convertBIto1A(imgTest);

				if (zeigen1) {

					ImageOperation io1 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io1.dilate();
					GravistoService.showImage(io1.getImageAsBufferedImage(),
							"Dilation");

					ImageOperation io2 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io2.erode();
					GravistoService.showImage(io2.getImageAsBufferedImage(),
							"Erode");

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

					ImageOperation io3 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io3.opening();
					GravistoService.showImage(io3.getImageAsBufferedImage(),
							"Opening");

					ImageOperation io4 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io4.closing();
					GravistoService.showImage(io4.getImageAsBufferedImage(),
							"Closing");

					ImageOperation io5 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io5.outline();
					GravistoService.showImage(io5.getImageAsBufferedImage(),
							"Outline");

					ImageOperation io5_2 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io5_2.outline2();
					GravistoService.showImage(io5_2.getImageAsBufferedImage(),
							"Outline2");

					ImageOperation io5_1 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io5_1.outline(new int[][] { { 1, 1, 1 }, { 1, 1, 1 },
							{ 1, 1, 1 } });
					GravistoService.showImage(io5_1.getImageAsBufferedImage(),
							"Outline1");

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

					ImageOperation io6 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io6.skeletonize();
					GravistoService.showImage(io6.getImageAsBufferedImage(),
							"Skeletonize");

					ImageOperation io7 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io7.threshold(254);
					GravistoService.showImage(io7.getImageAsBufferedImage(),
							"Threshold");

					ImageOperation io8 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());
					io8.gamma(2.0);
					GravistoService.showImage(io8.getImageAsBufferedImage(),
							"Gamma");

				} else {

					ImageOperation io10 = new ImageOperation(fluoImage,
							imgTest.getWidth(), imgTest.getHeight());

					// io10.skeletonize();
					io10.outline(new int[][] { { 1, 1, 1 }, { 1, 1, 1 },
							{ 1, 1, 1 } });
					io10.gamma(3.5);
					for (int i = 0; i<1; i++)
					io10.dilate();
//					io10.threshold(153);
//					io10.opening(new int[][] { { 1, 1, 1 }, { 1, 1, 1 },
//							{ 1, 1, 1 } });

//					io10.blur();

					GravistoService.showImage(io10.getImageAsBufferedImage(),
							"Ergebnis");

				}
				// io.printImage();

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			try {
				IOurl urlFlu = new IOurl(
						"mongo://26b7e285fae43dac107016afb4dc2841/WT01_1385");
				IOurl urlVis = new IOurl(
						"mongo://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
				IOurl urlNIR = new IOurl("mongo://c72e4fcc141b8b2a97851ab2fde8106a/WT01_1385");

				ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
				ResourceIOManager.registerIOHandler(new MongoDBhandler());
				BufferedImage imgFluo = ImageIO.read(urlFlu.getInputStream());
				BufferedImage imgVisible = ImageIO.read(urlVis.getInputStream());
				BufferedImage imgNIR = ImageIO.read(urlNIR.getInputStream());

				double scale = 1.0;
				if (Math.abs(scale - 1) > 0.0001) {
					System.out.println("Scaling!");
					imgFluo = new ImageOperation(imgFluo).resize(scale)
							.getImageAsBufferedImage();
					imgVisible = new ImageOperation(imgVisible).resize(scale)
							.getImageAsBufferedImage();
				}

				// resize
				int[] fluoImage = ImageConverter.convertBIto1A(imgFluo);

				ImageOperation io = new ImageOperation(fluoImage,
						imgFluo.getWidth(), imgFluo.getHeight());
				io.resize(imgVisible.getWidth(), imgVisible.getHeight());
				io.scale(0.95, 0.87);
				io.translate(0, -15 * scale);
				io.rotate(-3);

				imgFluo = ImageConverter.convert1AtoBI(imgVisible.getWidth(),
						imgVisible.getHeight(), io.getImageAs1array());

				boolean mergeImages = false;

				if (mergeImages)
					showTwoImagesAsOne(imgFluo, imgVisible);

				boolean test = true;
				if (test) {
					testPhytokammer(urlFlu, urlVis, urlNIR, imgFluo, imgVisible, imgNIR);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void blur() {
		ImageProcessor processor2 = processor.convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor) processor2);
		
        int w = byteProcessor.getWidth();
        int h = byteProcessor.getHeight(); 
        ByteProcessor copy = (ByteProcessor) processor2.duplicate();
        for (int v=1; v<=h-2; v++) {
            for (int u=1; u<=w-2; u++) {
                //compute filter result for position (u,v)
                int sum = 0;
                for (int j=-1; j<=1; j++) {
                    for (int i=-1; i<=1; i++) {
                        int p = copy.getPixel(u+i,v+j);
                        sum = sum + p;
                    }
                }
                int q = (int) (sum / 9.0);
                byteProcessor.putPixel(u,v,q);  
            }
        }

        image.setProcessor(processor2.convertToRGB());
}

	private static void testPhytokammer(IOurl urlFlu, IOurl urlVis,
			IOurl urlNIR, BufferedImage imgFluo, BufferedImage imgVisible, BufferedImage imgNIR) {
		
		int[] fluoImageOriginal = ImageConverter.convertBIto1A(imgFluo);
		int[] rgbImageOriginal = ImageConverter.convertBIto1A(imgVisible);
		int[] nirImageOriginal = ImageConverter.convertBIto1A(imgNIR);
		
		int[] fluoImage;
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		{
			SubstanceInterface substance = new Substance();
			substance.setName(ImageConfiguration.FluoTop.toString());
			ConditionInterface condition = new Condition(substance);
			Sample sample = new Sample(condition);
			LoadedImage limg = new LoadedImage(sample, imgFluo);
			limg.setURL(urlFlu);
			
			
			
			PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(
					limg, 2, null, null, true, null, null, output,
					null, 0.5, 0.5);

//			MainFrame
//					.showMessageWindow("FluoTop Clean", new JLabel(
//							new ImageIcon(limg.getLoadedImage())));
		}
		{
			SubstanceInterface substance = new Substance();
			substance.setName(ImageConfiguration.RgbTop.toString());
			ConditionInterface condition = new Condition(substance);
			Sample sample = new Sample(condition);
			LoadedImage limg = new LoadedImage(sample, imgVisible);
			limg.setURL(urlVis);
			PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(
					limg, 2, null, null, true, null, null, output,
					null, 2.5, 2.5);

//			MainFrame.showMessageWindow("RgbTop Clean", new JLabel(
//					new ImageIcon(limg.getLoadedImage())));
		}

		int[] rgbImage = ImageConverter.convertBIto1A(imgVisible);
		fluoImage = ImageConverter.convertBIto1A(imgFluo);
		
		
		
		// modify masks
		ImageOperation ioR = new ImageOperation(rgbImage, imgVisible.getWidth(), imgVisible.getHeight());
		ioR.erode().erode();
		ioR.dilate().dilate().dilate().dilate().dilate();

		ImageOperation ioF = new ImageOperation(fluoImage, imgFluo.getWidth(), imgFluo.getHeight());
		for (int i=0; i<4; i++)
			ioF.erode();
		for (int i=0; i<20; i++)
			ioF.dilate();

		int[] rgbImageM = ioR.getImageAs1array();
		int[] fluoImageM = ioF.getImageAs1array();
		
		BufferedImage imgFluoTest = ImageConverter.convert1AtoBI(imgFluo.getWidth(),imgFluo.getHeight(), fluoImageM);
		ImagePlus imgFFTest = ImageConverter.convertBItoIJ(imgFluoTest);
		imgFFTest.show("Fluorescence Vorstufe1");
		
		
		// merge infos of both masks
		int background = PhenotypeAnalysisTask.BACKGROUND_COLOR.getRGB();
		MaskOperation o = new MaskOperation(rgbImageM, fluoImageM, background);
		o.doMerge();

		// modify source images according to merged mask
		int i = 0;
		for (int m : o.getMaskAs1Array()) {
			if (m == 0) {
				rgbImage[i] = background;
				fluoImage[i] = background;
			}
			i++;
		}
		
//		BufferedImage imgFluoTest2 = ImageConverter.convert1AtoBI(imgFluo.getWidth(),imgFluo.getHeight(), fluoImage);
//		ImagePlus imgFFTest2 = ImageConverter.convertBItoIJ(imgFluoTest2);
//		imgFFTest2.show("Fluorescence Vorstufe2");
		
		{ 
			
			
			SubstanceInterface substance = new Substance();
			substance.setName(ImageConfiguration.NirTop.toString());
			ConditionInterface condition = new Condition(substance);
			Sample sample = new Sample(condition);
			LoadedImage limg = new LoadedImage(sample, imgNIR);
			limg.setURL(urlNIR);
			PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(
					limg, 2, null, null, true, null, null, output,
					null, 1, 0.5);

			int[] nirImage = ImageConverter.convertBIto1A(imgNIR);

			
			// process NIR

			MainFrame.showMessageWindow("NIR Source", new JLabel(
					new ImageIcon(imgNIR)));

			
			int[] mask = rgbImage;
			// resize mask
			ImageOperation ioo = new ImageOperation(mask, imgVisible.getWidth(), imgVisible.getHeight());
			ioo.resize(imgNIR.getWidth(), imgNIR.getHeight());
			ioo.rotate(-9);
			i = 0;
			for (int m : ioo.getImageAs1array()) {
				if (m == background) {
					nirImage[i] = background;
				}
				i++;
			}
			imgNIR = ImageConverter.convert1AtoBI(imgNIR.getWidth(), imgNIR.getHeight(), nirImage);
		}

		
		
		{ // fluo störungen beseitigen
			ImageOperation ioFF = new ImageOperation(fluoImage, imgFluo.getWidth(), imgFluo.getHeight());
			for (int ii=0; ii<5; ii++)
				ioFF.erode();
			for (int ii=0; ii<5; ii++)
				ioFF.dilate();
			ioFF.closing();
			
			int idx = 0;
			for (int m : ioFF.getImageAs1array()) {
				if (m == background)
						fluoImage[idx] = background;
					else
						fluoImage[idx] = fluoImageOriginal[idx];
				idx++;
			}
		}
		
		{ // rgb störungen beseitigen
			ImageOperation ioFF = new ImageOperation(rgbImage, imgVisible.getWidth(), imgVisible.getHeight());
			for (int ii=0; ii<6; ii++)
				ioFF.erode();
			for (int ii=0; ii<8; ii++)
				ioFF.dilate();
			for (int ii=0; ii<2; ii++)
				ioFF.erode();
//			for (int ii=0; ii<1; ii++)
//				ioFF.erode(new int [][] {{0,0,1,0,0},{0,1,1,1,0},{1,1,1,1,1},{0,1,1,1,0},{0,0,1,0,0}});
			
			int idx = 0;
			for (int m : ioFF.getImageAs1array()) {
				if (m == background)
						rgbImage[idx] = background;
					else
						rgbImage[idx] = rgbImageOriginal[idx];
				idx++;
			}
		}

		
		imgVisible = ImageConverter.convert1AtoBI(
				imgVisible.getWidth(), imgVisible.getHeight(),
				rgbImage);
		imgFluo = ImageConverter.convert1AtoBI(imgFluo.getWidth(),
				imgFluo.getHeight(), fluoImage);

		ImagePlus imgVV = ImageConverter.convertBItoIJ(imgVisible);
		ImagePlus imgFF = ImageConverter.convertBItoIJ(imgFluo);
		ImagePlus imgNN = ImageConverter.convertBItoIJ(imgNIR);

		imgVV.show("Visible");
		imgFF.show("Fluorescence");
		imgNN.show("NIR");
	}

	private static void showTwoImagesAsOne(BufferedImage imgF2,
			BufferedImage imgV2) {

		imgF2 = ImageConverter.convert1AtoBI(imgF2.getWidth(),
				imgF2.getHeight(), ImageConverter.convertBIto1A(imgF2));
		imgV2 = ImageConverter.convert1AtoBI(imgV2.getWidth(),
				imgV2.getHeight(), ImageConverter.convertBIto1A(imgV2));

		for (int x = 0; x < imgV2.getWidth(); x++) {
			for (int y = 0; y < imgV2.getHeight(); y++) {
				boolean twoInOne = false;
				if (twoInOne) {
					Color f = new Color(imgV2.getRGB(x, y));
					Color f2 = new Color(imgF2.getRGB(x, y));
					Color f3 = new Color(f2.getRed(), 0, f.getBlue());

					imgF2.setRGB(x, y, f3.getRGB());
				} else {
					if ((y / 3) % 2 == 0)
						imgF2.setRGB(x, y, imgV2.getRGB(x, y));
					else
						imgF2.setRGB(x, y, imgF2.getRGB(x, y));
				}
			}
		}

		GravistoService.showImage(imgF2, "Vergleich");
	}

}

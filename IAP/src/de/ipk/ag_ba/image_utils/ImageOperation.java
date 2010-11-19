package de.ipk.ag_ba.image_utils;

import ij.ImagePlus;
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
	
	public void translate(double x, double y){
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
		return resize((int) (factor * image.getWidth()), (int) (factor * image.getHeight()));
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

	
	public static void main(String[] args) {
		try {
			IOurl urlFlu = new IOurl("mongo://26b7e285fae43dac107016afb4dc2841/WT01_1385");
			IOurl urlVis = new IOurl("mongo://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
			
			ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
			ResourceIOManager.registerIOHandler(new MongoDBhandler());
			BufferedImage imgFluo = ImageIO.read(urlFlu.getInputStream());
			BufferedImage imgVisible = ImageIO.read(urlVis.getInputStream());
			
			double scale = 0.7;
			if (Math.abs(scale-1)>0.0001) {
				System.out.println("Scaling!");
				imgFluo = new ImageOperation(imgFluo).resize(scale).getBufferedImage();
				imgVisible = new ImageOperation(imgVisible).resize(scale).getBufferedImage();
			}
			
//			GravistoService.showImage(new ImageOperation(imgF2).resize(0.5).getBufferedImage(), "imgF2");
//			GravistoService.showImage(new ImageOperation(imgV2).resize(0.5).getBufferedImage(), "imgV2");
			
			// resize
			int[] fluoImage =ImageConverter.convertBIto1A(imgFluo);
			
			ImageOperation io = new ImageOperation(fluoImage, imgFluo.getWidth(), imgFluo.getHeight());
			io.resize(imgVisible.getWidth(), imgVisible.getHeight());
			io.scale(0.95, 0.87);
			io.translate(0, -15*scale);
			io.rotate(-3);

			imgFluo = ImageConverter.convert1AtoBI(imgVisible.getWidth(), imgVisible.getHeight(), io.getImageAs1array());
			
			boolean mergeImages = true;
			
			if (mergeImages) 
				showTwoImagesAsOne(imgFluo, imgVisible);
				
			boolean test = true;
			if(test){
				ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
				{
					SubstanceInterface substance = new Substance();
					substance.setName(ImageConfiguration.FluoTop.toString());
					ConditionInterface condition = new Condition(substance);
					Sample sample = new Sample(condition);
					LoadedImage limg = new LoadedImage(sample, imgFluo);
					limg.setURL(urlFlu);
					PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, 2, null, null, true, null, null, output, null, 1, 0.5);
					
					MainFrame.showMessageWindow("FluoTop Clean", new JLabel(new ImageIcon(limg.getLoadedImage())));
				}
				{
					SubstanceInterface substance = new Substance();
					substance.setName(ImageConfiguration.RgbTop.toString());
					ConditionInterface condition = new Condition(substance);
					Sample sample = new Sample(condition);
					LoadedImage limg = new LoadedImage(sample, imgVisible);
					limg.setURL(urlVis);
					PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, 2, null, null, true, null, null, output, null, 5, 2.5);
					
					MainFrame.showMessageWindow("RgbTop Clean", new JLabel(new ImageIcon(limg.getLoadedImage())));
				}
				
				int[] rgbImage = ImageConverter.convertBIto1A(imgVisible);
				fluoImage = ImageConverter.convertBIto1A(imgFluo);

				int background = PhenotypeAnalysisTask.BACKGROUND_COLOR.getRGB();
				MaskOperation o = new MaskOperation(rgbImage, fluoImage, background);
				o.doMerge();
				
				int i = 0;
				for (int m : o.getMaskAs1Array()) {
					if (m==0) {
						rgbImage[i] = background; 
						fluoImage[i] = background; 
					}
					i++;
				}
				
				imgVisible = ImageConverter.convert1AtoBI(imgVisible.getWidth(), imgVisible.getHeight(), rgbImage);
				imgFluo = ImageConverter.convert1AtoBI(imgFluo.getWidth(), imgFluo.getHeight(), fluoImage);
				
				ImagePlus imgVV = ImageConverter.convertBItoIJ(imgVisible);
				ImagePlus imgFF = ImageConverter.convertBItoIJ(imgFluo);
				
				imgVV.show("Visible");
				imgFF.show("Fluorescence");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showTwoImagesAsOne(BufferedImage imgF2,
			BufferedImage imgV2) {
		
		imgF2 = ImageConverter.convert1AtoBI(imgF2.getWidth(), imgF2.getHeight(), ImageConverter.convertBIto1A(imgF2));
		imgV2 = ImageConverter.convert1AtoBI(imgV2.getWidth(), imgV2.getHeight(), ImageConverter.convertBIto1A(imgV2));
		
		for(int x=0; x<imgV2.getWidth();x++){
		    for (int y=0; y<imgV2.getHeight();y++){
		    	boolean twoInOne = false;
				if (twoInOne) {
		          Color f = new Color(imgV2.getRGB(x, y));
		          Color f2= new Color(imgF2.getRGB(x, y));
		          Color f3= new Color(f2.getRed(),0,f.getBlue());
		          
		          imgF2.setRGB(x, y, f3.getRGB());
		    	} else {
		    		if ((y/3)%2==0)
				          imgF2.setRGB(x, y, imgV2.getRGB(x, y));
		    		else
				          imgF2.setRGB(x, y, imgF2.getRGB(x, y));
		    	}
		    }
		}
		
		GravistoService.showImage(imgF2, "Vergleich");
	}

	private BufferedImage getBufferedImage() {
		return ImageConverter.convertIJtoBI(image);
	}

}

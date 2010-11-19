/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.mongo.MongoDBhandler;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;

/**
 * @author entzian
 *
 */
public class test extends JFrame {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
        new test();
    }

	
	public test() throws Exception {
        super("ImageOverlayExample");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
 
        try {
            
        	IOurl urlFlu = new IOurl("mongo://26b7e285fae43dac107016afb4dc2841/WT01_1385");
			IOurl urlVis = new IOurl("mongo://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
		
//			ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
			ResourceIOManager.registerIOHandler(new MongoDBhandler());
			BufferedImage imgF2 = ImageIO.read(urlFlu.getInputStream());	//Background
			BufferedImage imgV2 = ImageIO.read(urlVis.getInputStream());	//kommt drüber
        	
			
			// resize
//			int[] rgbImage = ImageConverter.convertBIto1A(imgV2);
			int[] fluoImage =ImageConverter.convertBIto1A(imgF2);
			ImageOperation io = new ImageOperation(fluoImage, imgF2.getWidth(), imgF2.getHeight());
			io.resize(imgV2.getWidth(), imgV2.getHeight());
			fluoImage = io.getImageAs1array();
			imgF2 = ImageConverter.convert1AtoBI(imgV2.getWidth(), imgV2.getHeight(), fluoImage);
			
            imgF2.getGraphics().drawImage(imgV2, 0, 0, this);
 
            JLabel label = new JLabel(new ImageIcon(imgF2));
 
            add(label);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
	
 
        pack();
        setVisible(true);
    }

	

}

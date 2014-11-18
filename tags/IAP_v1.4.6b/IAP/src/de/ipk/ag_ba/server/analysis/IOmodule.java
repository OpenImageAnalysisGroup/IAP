/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 14, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.analysis;

import info.StopWatch;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.LoadedNetwork;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author klukas
 */
public class IOmodule {
	
	public static LoadedImage loadImageFromFileOrMongo(ImageData id, boolean loadImage, boolean loadLabelField)
			throws Exception {
		LoadedImage result = null;
		StopWatch s = new StopWatch("Load image and null-image", false);
		BufferedImage image = null;
		if (loadImage) {
			MyByteArrayInputStream isMain = ResourceIOManager.getInputStreamMemoryCached(id.getURL());
			try {
				image = ImageIO.read(isMain);
			} finally {
				isMain.close();
			}
		}
		BufferedImage imageNULL = null;
		try {
			if (loadLabelField)
				if (id.getLabelURL() != null) {
					InputStream isLabel = ResourceIOManager.getInputStreamMemoryCached(id.getLabelURL());
					try {
						imageNULL = ImageIO.read(isLabel);
					} finally {
						isLabel.close();
					}
				}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		result = new LoadedImage(id, image, imageNULL);
		s.printTime(100);
		return result;
	}
	
	public static byte[] loadImageContent(ImageData id) throws Exception {
		MyByteArrayInputStream is = ResourceIOManager.getInputStreamMemoryCached(id.getURL());
		return is.getBuffTrimmed();
	}
	
	public static InputStream getThreeDvolumeRenderViewGif(LoadedVolumeExtension volume,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws FileNotFoundException, URISyntaxException {
		try {
			// return
			// MyImageIOhelper.getPreviewImageStream(MyImageIOhelper.getPreviewImage(volume
			// .getSideView(CubeSide.FRONT)));
			return volume.getSideViewGif(512, 512, optStatus);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new FileInputStream(new File(new URI(GravistoService.getResource(IOmodule.class,
					"img/RotationReconstruction.png").toString())));
		}
	}
	
	public static InputStream getThreeDvolumePreviewIcon(LoadedVolumeExtension volume,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws FileNotFoundException, URISyntaxException {
		try {
			// return
			// MyImageIOhelper.getPreviewImageStream(MyImageIOhelper.getPreviewImage(volume
			// .getSideView(CubeSide.FRONT)));
			return volume.getSideViewGif(256, 256, optStatus);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new FileInputStream(new File(new URI(GravistoService.getResource(IOmodule.class,
					"img/RotationReconstruction.png").toString())));
		}
	}
	
	public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
	
	public static LoadedVolume loadVolume(final VolumeData md) throws Exception {
		DataInputStream is = new DataInputStream(md.getURL().getInputStream());
		
		int xx = md.getDimensionX();
		int yy = md.getDimensionY();
		int zz = md.getDimensionZ();
		int[][][] imageDataRGBA = new int[xx][yy][zz];
		for (int z = 0; z < zz; z++)
			for (int y = 0; y < yy; y++)
				for (int x = 0; x < xx; x++) {
					imageDataRGBA[x][y][z] = is.readInt();
				}
		
		return new LoadedVolume(md, new ByteShortIntArray(imageDataRGBA));
	}
	
	public static LoadedNetwork loadNetwork(NetworkData nd) throws Exception {
		Graph graph = MainFrame.getGraph(nd.getURL());
		Graph graphLabelField = MainFrame.getGraph(nd.getLabelURL());
		return new LoadedNetwork(nd, graph, graphLabelField);
	}
	
	public static final int makeIntFromByte4(byte[] b) {
		return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
	}
	
	public static final byte[] makeByte4FromInt(int i) {
		return new byte[] { (byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i };
	}
	
	public static InputStream getNetworkPreviewIcon(LoadedNetwork ln, BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		return null;
	}
}

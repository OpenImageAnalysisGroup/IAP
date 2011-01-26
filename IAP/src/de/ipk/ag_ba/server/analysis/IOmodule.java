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
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.HomeFolder;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

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
	
	private final ArrayList<IOtask> todo = new ArrayList<IOtask>();
	private final int processed = 0;
	private final int lastKBperSecTransferSpeed = 0;
	
	public WorkerInfo getWorkerInfo() {
		return new WorkerInfo(todo.size(), 0, processed, lastKBperSecTransferSpeed, "KB/s");
	}
	
	public static LoadedImage loadImageFromFileOrMongo(ImageData id, boolean loadImage, boolean loadLabelField) throws Exception {
		LoadedImage result = null;
		StopWatch s = new StopWatch("Load image and null-image", false);
		BufferedImage image = null;
		if (loadImage)
			image = ImageIO.read(id.getURL().getInputStream());
		BufferedImage imageNULL = null;
		try {
			if (loadLabelField)
				if (id.getLabelURL() != null)
					imageNULL = ImageIO.read(id.getLabelURL().getInputStream());
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		result = new LoadedImage(id, image, imageNULL);
		s.printTime(200);
		return result;
	}
	
	public static byte[] loadImageContentFromFileOrMongo(ImageData id) throws Exception {
		InputStream is = id.getURL().getInputStream();
		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		HomeFolder.copyContent(is, out);
		return out.getBuff();
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
	
	// public static VolumeUploadData getThreeDvolumeInputStream(LoadedVolume volume) {
	// System.out.println("Create InputStream representation for volume: " + volume.getDimensionX()
	// * volume.getDimensionY() * volume.getDimensionZ() * 4 / 1024 / 1024 + " MB");
	// VolumeInputStream stream = volume.getLoadedVolume().getInputStream();
	// return new VolumeUploadData(stream, stream.getNumberOfBytes());
	// }
	
	// public static LoadedVolume loadVolumeFromDBE(VolumeData md, String login,
	// String pass) {
	// Blob blo;
	// LoadedVolume result = null;
	// try {
	// blo = CallDBE2WebService.getBlob(login, pass, md.getURL().getDetail());
	// result = new LoadedVolume(md);
	// int index = 0;
	// int mx = md.getDimensionX();
	// int my = md.getDimensionY();
	// int mz = md.getDimensionZ();
	// byte[] fileContent = blo.getBytes(index, mx * my * mz * 4);
	// result.setVolume(fileContent);
	// } catch (Exception e) {
	// ErrorMsg.addErrorMessage(e);
	// }
	// return result;
	// }
	
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
		Graph graph = MainFrame.getInstance().getGraph(nd.getURL());
		Graph graphLabelField = MainFrame.getInstance().getGraph(nd.getLabelURL());
		return new LoadedNetwork(nd, graph, graphLabelField);
	}
	
	public static final int makeIntFromByte4(byte[] b) {
		return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
	}
	
	public static final byte[] makeByte4FromInt(int i) {
		return new byte[] { (byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i };
	}
	
	public static InputStream getNetworkPreviewIcon(LoadedNetwork ln, BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		// TODO Auto-generated method stub
		return null;
	}
}

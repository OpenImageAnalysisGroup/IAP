/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 14, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.analysis;

import java.awt.Color;
import java.awt.image.BufferedImage;
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
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.gui.picture_gui.MongoCollection;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.RunnableOnDB;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;

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

	public static LoadedImage loadImageFromFileOrMongo(ImageData id, String login, String pass) throws Exception {
		BufferedImage image = ImageIO.read(ResourceIOManager.getInputStream(id.getURL()));
		BufferedImage imageNULL = null;
		try {
			if (id.getLabelURL() != null)
				imageNULL = ImageIO.read(ResourceIOManager.getInputStream(id.getLabelURL()));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		LoadedImage result = new LoadedImage(id, image, imageNULL);
		return result;
	}

	public static byte[] loadImageContentFromFileOrMongo(ImageData id, String login, String pass) throws Exception {
		InputStream is = ResourceIOManager.getInputStream(id.getURL());
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
			return volume.getSideViewGif(128, 128, optStatus);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return new FileInputStream(new File(new URI(GravistoService.getResource(IOmodule.class,
					"img/RotationReconstruction.png").toString())));
		}
	}

	public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

	public static VolumeUploadData getThreeDvolumeInputStream(LoadedVolume volume) {
		System.out.println("Create InputStream representation for volume: " + volume.getDimensionX()
				* volume.getDimensionY() * volume.getDimensionZ() * 4 / 1024 / 1024 + " MB");

		byte[] cube = volume.getLoadedVolume().getByteArray();
		return new VolumeUploadData(new MyByteArrayInputStream(cube), cube.length);
	}

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

	public static LoadedVolume loadVolumeFromMongo(final VolumeData md, String login, String pass) {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		try {
			new MongoDB().processDB(new RunnableOnDB() {
				private DB db;

				@Override
				public void run() {
					LoadedVolume result = new LoadedVolume(md);
					GridFS gridfs_images = new GridFS(db, MongoCollection.VOLUMES.toString());
					GridFSDBFile fff = gridfs_images.findOne(md.getURL().getDetail());
					if (fff != null) {
						try {
							InputStream is = fff.getInputStream();
							byte[] cube = new byte[md.getDimensionX() * md.getDimensionY() * md.getDimensionZ() * 4];
							int offset = 0;
							int read = 0;
							while ((read = is.read(cube, offset, cube.length - offset)) > 0) {
								offset += read;
							}
							System.out.println("Received " + offset + " / " + cube.length + " bytes");
							result.setVolume(new ByteShortIntArray(cube));

						} catch (Exception e) {
							result = null;
							ErrorMsg.addErrorMessage(e);
						}
					}
					tso.setParam(0, result);
				}

				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});

		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return (LoadedVolume) tso.getParam(0, null);
	}

	public static final int makeIntFromByte4(byte[] b) {
		return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
	}

	public static final byte[] makeByte4FromInt(int i) {
		return new byte[] { (byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i };
	}
}

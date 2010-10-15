/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Oct 15, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.vanted;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.ShowImage;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.CubeSide;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MyByteArrayInputStream;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MyByteArrayOutputStream;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;

/**
 * @author klukas
 */
public class LoadedVolumeExtension extends LoadedVolume {

	public LoadedVolumeExtension(Sample parent, byte[] volume) {
		super(parent, volume);
	}

	public LoadedVolumeExtension(VolumeData md) {
		super(md);
	}

	public BufferedImage getSideView(CubeSide side) {
		switch (side) {
		case FRONT:
			int width = getDimensionX();
			int height = getDimensionY();
			BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			int idx = 0;
			System.out.println(countNonZero() + " non zero");
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int[] iArray = new int[] { 255, 255, 255, 0 };
					result.getRaster().setPixel(x, y, iArray);
					boolean solidFound = false;
					for (int z = 0; z < getDimensionZ(); z++) {
						byte a = volume[idx++];
						byte r = volume[idx++];
						byte g = volume[idx++];
						byte b = volume[idx++];
						if (!solidFound && a != 0 && (r < 250 || g < 250 || b < 250)) {
							solidFound = true;
							iArray[0] = r;
							iArray[1] = g;
							iArray[2] = b;
							iArray[3] = 255;
							result.getRaster().setPixel(x, y, iArray);
						}
					}
				}
			}
			return result;
		}
		return null;
	}

	private byte[] volume2 = null;

	public void resetRenderCubeCopy() {
		volume2 = null;
	}

	public BufferedImage renderSideView(double rotation, BufferedImage result) {
		int width = getDimensionX();
		int height = getDimensionY();
		int depth = getDimensionZ();
		// System.out.println(countNonZero() + " non zero");
		if (volume2 == null || volume.length != volume2.length) {
			volume2 = new byte[volume.length];
		}

		boolean threaded = true;
		if (threaded)
			rotateVolumeThreaded(rotation);
		else {
			rotateVolume(rotation, 0, 1);
		}
		renderSideView(result, width, height, depth);
		return result;
	}

	private void renderSideView(BufferedImage result, int width, int height, int depth) {
		WritableRaster raster = result.getRaster();
		int idx = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int[] iArray = new int[] { 255, 255, 255, 255 };
				boolean solidFound = false;
				for (int z = 0; z < depth; z++) {
					if (solidFound) {
						idx += 4;
					} else {
						byte a = volume2[idx++];
						byte r = volume2[idx++];
						byte g = volume2[idx++];
						byte b = volume2[idx++];
						if (a != 0 && (r < 250 || g < 250 || b < 250)) {
							solidFound = true;
							iArray[0] = r;
							iArray[1] = g;
							iArray[2] = b;
							iArray[3] = 255;
							raster.setPixel(x, y, iArray);
						}
					}
				}
				if (!solidFound)
					raster.setPixel(x, y, iArray);
			}
		}
	}

	private void rotateVolumeThreaded(double rotation) {
		int maxCPU = SystemAnalysis.getNumberOfCPUs();
		if (maxCPU > 8)
			maxCPU = maxCPU / 2;
		ExecutorService run = Executors.newFixedThreadPool(maxCPU);

		for (int i = 0; i < maxCPU; i++)
			run.submit(getVolumeRotationRunnable(i, maxCPU, rotation));

		run.shutdown();
		try {
			run.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}

	private Runnable getVolumeRotationRunnable(final int i, final int maxCPU, final double rotation) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				rotateVolume(rotation, i, maxCPU);
			}
		};
		return r;
	}

	private void rotateVolume(double rotation, int i, int maxCPU) {
		{
			double angle = rotation / 180d * Math.PI;
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			int dimensionxH = dimensionx / 2;
			int dimensionzH = dimensionx / 2;

			for (int x = 0; x < dimensionx; x++) {
				if (x % maxCPU == i)
					for (int z = 0; z < dimensionz; z++) {
						double a = z - dimensionzH;
						double b = x - dimensionxH;
						double zn = a * cos - b * sin;
						double xn = a * sin + b * cos;
						int zni = (int) zn + dimensionzH;
						int xni = (int) xn + dimensionxH;
						int offA = z + x * dimensiony * dimensionz;
						int offB = zni + xni * dimensiony * dimensionz;
						boolean targetOK = zni >= 0 && zni < dimensionz && xni >= 0 && xni < dimensionx;
						for (int y = 0; y < dimensiony; y++) {
							int offSrc = offA + offA + offA + offA;
							int offTgt = offB + offB + offB + offB;
							if (targetOK) {
								volume2[offSrc++] = volume[offTgt++];
								volume2[offSrc++] = volume[offTgt++];
								volume2[offSrc++] = volume[offTgt++];
								volume2[offSrc] = volume[offTgt];
							}
							offA += dimensiony;
							offB += dimensiony;
						}
					}
			}
		}
	}

	public static void main(String[] args) {

		try {

			int res = 200;
			FileInputStream file = new FileInputStream(
					"/Users/klukas/Desktop/IAP_reconstruction_1284034033183.argb_volume");

			boolean high = false;
			if (high) {
				res = 400;
				file = new FileInputStream("/Users/klukas/Desktop/IAP_reconstruction_1283948591567.argb_volume");
			}

			DataInputStream in = new DataInputStream(file);

			LoadedVolumeExtension v = new LoadedVolumeExtension(null);
			v.volume = new byte[res * res * res * 4];
			v.volume2 = new byte[res * res * res * 4];
			in.readFully(v.volume);
			in.close();

			v.setDimensionX(res);
			v.setDimensionY(res);
			v.setDimensionZ(res);
			double rotation = 0;

			int cnt = 0;
			BufferedImage result = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
			ShowImage imageDisplay = null;
			while (true) {
				long t1 = System.currentTimeMillis();
				v.renderSideView(rotation, result);

				final ShowImage imageDisplayF = imageDisplay;

				if (imageDisplay == null)
					imageDisplay = GravistoService.showImage(result, "3D-Rendering");
				else {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							imageDisplayF.repaint();
						}
					});
				}
				rotation += 5;
				cnt = 1;
				long t2 = System.currentTimeMillis();
				System.out.println("T: " + (t2 - t1) + "ms, FPS: " + (int) (((double) cnt / (t2 - t1) * 1000d)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MyByteArrayInputStream getSideViewGif(int width, int height) throws Exception {
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		ArrayList<String> delayTimes = new ArrayList<String>();

		int degreeSteps = 2;
		int degree = 0;
		int delay = 3;
		while (degree < 360) {
			BufferedImage result = new BufferedImage(getDimensionX(), getDimensionY(), BufferedImage.TYPE_INT_ARGB);
			renderSideView(degree, result);
			result = GravistoService.getScaledImage(result, width, height);
			images.add(result);
			delayTimes.add("" + delay);
			degree += degreeSteps;
		}

		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		WriteAnimatedGif.saveAnimate(out, images.toArray(new BufferedImage[] {}), delayTimes.toArray(new String[] {}));
		return new MyByteArrayInputStream(out.getBuff());
	}

}

/*******************************************************************************
 * 
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
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

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.ShowImage;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.IntVolumeVisitor;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;

/**
 * @author klukas
 */
public class LoadedVolumeExtension extends LoadedVolume {

	public LoadedVolumeExtension(Sample parent, int[][][] volume) {
		super(parent, new ByteShortIntArray(volume));
	}

	public LoadedVolumeExtension(VolumeData md) {
		super(md);
	}

	public BufferedImage renderSideView(double rotation, final BufferedImage result) {
		final int width = getDimensionX();
		final int height = getDimensionY();
		final int depth = getDimensionZ();

		boolean threaded = true;
		if (threaded)
			rotateVolumeThreaded(rotation, new VolumeReceiver() {

				@Override
				public void processVolume(int[][][] volume) {
					renderSideView(result, width, height, depth, volume);
				}
			});
		else {
			renderSideView(result, width, height, depth, rotateVolume(rotation, 0, 1, null));
		}

		return result;
	}

	private void renderSideView(BufferedImage result, int width, int height, int depth, int[][][] volume2) {
		WritableRaster raster = result.getRaster();
		int idx = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int[] iArray = new int[] { 255, 255, 255, 255 };
				boolean solidFound = false;
				for (int z = 0; z < depth; z++) {
					if (solidFound) {
						idx++;
					} else {
						int v = volume.getColorVoxel(x, y, z);
						if (v != PhenotypeAnalysisTask.BACKGROUND_COLORint) {
							solidFound = true;
							raster.setPixel(x, y, new int[] { v });
						}
					}
				}
				if (!solidFound)
					raster.setPixel(x, y, iArray);
			}
		}
	}

	private void rotateVolumeThreaded(double rotation, VolumeReceiver volumeReceiver) {
		int maxCPU = SystemAnalysis.getNumberOfCPUs();
		if (maxCPU > 8)
			maxCPU = maxCPU / 2;
		ExecutorService run = Executors.newFixedThreadPool(maxCPU);

		int[][][] volume2 = new int[getDimensionX()][getDimensionY()][getDimensionZ()];
		for (int i = 0; i < maxCPU; i++)
			run.submit(getVolumeRotationRunnable(i, maxCPU, rotation, volume2));

		run.shutdown();
		try {
			run.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}

	private Runnable getVolumeRotationRunnable(final int i, final int maxCPU, final double rotation, final int[][][] volume2) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				rotateVolume(rotation, i, maxCPU, volume2);
			}
		};
		return r;
	}

	private int[][][] rotateVolume(double rotation, int i, int maxCPU, int[][][] volume2) {

		if (volume2 == null)
			volume2 = new int[getDimensionX()][getDimensionY()][getDimensionZ()];

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
					boolean targetOK = zni >= 0 && zni < dimensionz && xni >= 0 && xni < dimensionx;
					for (int y = 0; y < dimensiony; y++) {
						if (targetOK) {
							volume2[x][y][z] = volume.getColorVoxel(xni, y, zni);
						}
					}
				}
		}

		return volume2;
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

			final DataInputStream in = new DataInputStream(file);

			LoadedVolumeExtension v = new LoadedVolumeExtension(null);
			final int[][][] i1 = new int[res][res][res];
			v.volume = new ByteShortIntArray(i1);
			int[][][] i2 = new int[res][res][res];
			v.volume.visitIntArray(new IntVolumeVisitor() {
				@Override
				public void visit(int x, int y, int z, int value) throws Exception {
					i1[x][y][z] = in.read();
				}
			});

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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MyByteArrayInputStream getSideViewGif(int width, int height,
						BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		ArrayList<String> delayTimes = new ArrayList<String>();

		int degreeSteps = 2;
		int degree = 0;
		int delay = 3;
		if (optStatus != null)
			optStatus.setCurrentStatusValueFine(0);
		while (degree < 360) {
			BufferedImage result = new BufferedImage(getDimensionX(), getDimensionY(), BufferedImage.TYPE_INT_ARGB);
			renderSideView(degree, result);
			result = GravistoService.getScaledImage(result, width, height);
			images.add(result);
			delayTimes.add("" + delay);
			degree += degreeSteps;
			if (optStatus != null)
				optStatus.setCurrentStatusValueFine(degree * 100d / 360);
		}

		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		WriteAnimatedGif.saveAnimate(out, images.toArray(new BufferedImage[] {}), delayTimes.toArray(new String[] {}));
		if (optStatus != null)
			optStatus.setCurrentStatusValueFine(100);
		return new MyByteArrayInputStream(out.getBuff());
	}

	public long getVoxelCount() {
		return (long) getDimensionX() * getDimensionY() * getDimensionZ();
	}

}

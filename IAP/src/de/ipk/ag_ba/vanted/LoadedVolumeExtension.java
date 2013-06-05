/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.ShowImage;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.IntVolumeVisitor;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

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
	
	public LoadedVolumeExtension(LoadedVolume md) {
		super(md, md.getLoadedVolume());
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
	
	private static void renderSideView(BufferedImage result, int width, int height, int depth, int[][][] volume2) {
		WritableRaster raster = result.getRaster();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int[] iArray = new int[] { 255, 255, 255, 255 };
				boolean solidFound = false;
				for (int z = 0; z < depth; z++) {
					if (solidFound) {
						// empty
					} else {
						int v = volume2[x][y][z];
						int red = (v >> 16) & 0xff;
						int green = (v >> 8) & 0xff;
						int blue = (v) & 0xff;
						if (v != 0 && (red < 255 || green < 255 || blue < 255)) {
							solidFound = true;
							int alpha = 255; // not supported by gif ?! (v >> 24) &
							// 0xff;
							
							int[] col = { red, green, blue, alpha };
							raster.setPixel(x, y, col);
						}
					}
				}
				if (!solidFound)
					raster.setPixel(x, y, iArray);
				
				// if (Math.random() < 0.01) {
				// iArray = new int[] { (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255), 255 };
				// raster.setPixel(x, y, iArray);
				// }
			}
		}
		// System.out.println("Solid voxels: " + idx);
	}
	
	private void rotateVolumeThreaded(double rotation, VolumeReceiver volumeReceiver) {
		int maxCPU = SystemAnalysis.getNumberOfCPUs();
		final ThreadSafeOptions tsoLA = new ThreadSafeOptions();
		ExecutorService run = Executors.newFixedThreadPool(maxCPU, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				int i;
				synchronized (tsoLA) {
					tsoLA.addInt(1);
					i = tsoLA.getInt();
				}
				t.setName("Volume Rotation (" + i + ")");
				return t;
			}
		});
		
		int[][][] volume2 = new int[getDimensionX()][getDimensionY()][getDimensionZ()];
		for (int i = 0; i < maxCPU; i++)
			run.submit(getVolumeRotationRunnable(i, maxCPU, rotation, volume2));
		
		run.shutdown();
		try {
			run.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}
		volumeReceiver.processVolume(volume2);
	}
	
	private Runnable getVolumeRotationRunnable(final int i, final int maxCPU, final double rotation,
			final int[][][] volume2) {
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
		
		// System.out.println("Angle: " + (int) rotation + " degree");
		double angle = rotation / 180d * Math.PI;
		// System.out.println("Angle: " + angle);
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
				
				result.getGraphics().drawOval(10, 10, 50, 50);
				
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
		
		int degreeSteps = 10; // 2
		int degree = 0;
		int delay = 5;
		if (optStatus != null) {
			optStatus.setCurrentStatusText1("Processing 3-D Volume");
			optStatus.setCurrentStatusText2("Render side view GIF");
			optStatus.setCurrentStatusValueFine(0);
		}
		ImageStack fis = new ImageStack();
		while (degree < 360) {
			BufferedImage result = new BufferedImage(getDimensionX(), getDimensionY(), BufferedImage.TYPE_INT_ARGB);
			renderSideView(degree, result);
			result = GravistoService.getScaledImage(result, width, height);
			images.add(result);
			fis.addImage("Deg " + degree, new Image(result));
			delayTimes.add("" + delay);
			degree += degreeSteps;
			if (optStatus != null) {
				optStatus.setCurrentStatusValueFine(degree * 100d / 360);
				optStatus.setCurrentStatusText2("Rendered side view (" + degree + "°)");
			}
		}
		
		// fis.print("Render (Stack)");
		
		if (optStatus != null) {
			optStatus.setCurrentStatusText1("Process side view GIF");
			optStatus.setCurrentStatusText2("Saving animated GIF");
		}
		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		// GravistoService.showImage(images.get(0), "Image");
		WriteAnimatedGif.saveAnimate(out, images.toArray(new BufferedImage[] {}), delayTimes.toArray(new String[] {}));
		if (optStatus != null)
			optStatus.setCurrentStatusValueFine(100);
		
		if (optStatus != null) {
			optStatus.setCurrentStatusText1("Side views processed");
			optStatus.setCurrentStatusText2("");
		}
		
		return new MyByteArrayInputStream(out.getBuff(), out.size());
	}
	
	public long getVoxelCount() {
		return (long) getDimensionX() * getDimensionY() * getDimensionZ();
	}
	
}

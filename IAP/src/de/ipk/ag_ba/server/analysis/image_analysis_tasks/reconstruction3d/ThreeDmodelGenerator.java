package de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/*
 * Created on Dec 17, 2009 by Christian Klukas
 */

/**
 * @author klukas
 */
public class ThreeDmodelGenerator {
	
	private static int PROBABILITY_THRESHOLD = 30;
	
	private ArrayList<MyPicture> pictures = new ArrayList<MyPicture>();
	
	private int maxVoxelPerSide = 203; // 1624/2=812 1624/4=406 1624/8=203
	// Color[][][] colorCube = new
	// Color[maxVoxelPerSide][maxVoxelPerSide][maxVoxelPerSide];
	byte[][][] transparentVoxels;
	private int[][][] byteCube;
	private int[][][] rgbCube;
	
	ArrayList<Color> palette = null;
	
	private double cameraDistance;
	public double cubeSideLengthX, cubeSideLengthY, cubeSideLengthZ;
	
	private int widthFactor = 100;
	
	private boolean generated;
	
	private int maxPossibleLevels;
	
	public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
	
	public ThreeDmodelGenerator(int voxelresolution, int widthFactor) {
		this.maxVoxelPerSide = voxelresolution;
		transparentVoxels = new byte[maxVoxelPerSide][maxVoxelPerSide][maxVoxelPerSide];
		this.widthFactor = widthFactor;
	}
	
	public void setRoundViewImages(ArrayList<MyPicture> pictures) {
		this.pictures = pictures;
	}
	
	public void setCameraDistance(double dist) {
		this.cameraDistance = dist;
	}
	
	public void setCubeSideLength(double sizeX, double sizeY, double sizeZ) {
		this.cubeSideLengthX = sizeX;
		this.cubeSideLengthY = sizeY;
		this.cubeSideLengthZ = sizeZ;
	}
	
	public void calculateModel(final BackgroundTaskStatusProviderSupportingExternalCall status,
						GenerationMode colorMode, int maxIndexedColorCount) {
		final ThreadSafeOptions tsoLA = new ThreadSafeOptions();
		ExecutorService run = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs(), new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				int i;
				synchronized (tsoLA) {
					tsoLA.addInt(1);
					i = tsoLA.getInt();
				}
				t.setName("Cube cut (" + i + ")");
				return t;
			}
		});
		
		final ThreadSafeOptions tsoCO = new ThreadSafeOptions();
		ExecutorService runColor = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs(), new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				int i;
				synchronized (tsoLA) {
					tsoCO.addInt(1);
					i = tsoCO.getInt();
				}
				t.setName("Cube Coloring (" + i + ")");
				return t;
			}
		});
		
		status.setCurrentStatusText1("Init cube cut (" + maxVoxelPerSide + "x" + maxVoxelPerSide + "x" + maxVoxelPerSide
							+ ")");
		status.setCurrentStatusText2("Using " + SystemAnalysis.getNumberOfCPUs() + " threads");
		status.setCurrentStatusValueFine(0);
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final ThreadSafeOptions tsoRunCount = new ThreadSafeOptions();
		tso.setInt(0);
		tsoRunCount.setInt(0);
		for (MyPicture p : pictures) {
			final MyPicture fp = p;
			run.execute(cuttt2(status, tso, tsoRunCount, fp));
			
			if (status.wantsToStop())
				break;
		}
		run.shutdown();
		try {
			run.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		} // wait max 7 days for result
		
		status.setCurrentStatusValue(0);
		
		if (colorMode != GenerationMode.GRAYSCALE_PROBABILITY)
			if (pictures != null && pictures.size() > 0) {
				// colorize cube
				if (colorMode == GenerationMode.COLORED) {
					status.setCurrentStatusText1("Find Common Image Colors (SOM)...");
					status.setCurrentStatusText2("");
					palette = pictures.iterator().next().getColorPalette(maxIndexedColorCount, status);
				}
				status.setCurrentStatusText1("Normalize Cube...");
				status.setCurrentStatusText2("");
				generateNormalizedByteCube(colorMode);
				status.setCurrentStatusText1("Colorize Cube...");
				status.setCurrentStatusText2("");
				colorModelRGB(pictures, palette, status, colorMode == GenerationMode.COLORED_RGBA, runColor);
				
				runColor.shutdown();
				try {
					runColor.awaitTermination(7, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					ErrorMsg.addErrorMessage(e);
				} // wait max 7 days for result
				
			}
		
		status.setCurrentStatusText1("Cube Construction Finished");
		status.setCurrentStatusText2("");
		
		if (!status.wantsToStop())
			status.setCurrentStatusValueFine(100d);
	}
	
	@SuppressWarnings("unused")
	private Runnable cuttt1(final BackgroundTaskStatusProviderSupportingExternalCall status,
						final ThreadSafeOptions tso, final ThreadSafeOptions tsoRunCount, final MyPicture fp) {
		return new Runnable() {
			public void run() {
				tsoRunCount.addInt(1);
				
				BitSet bestTpv = null;
				
				fp.setAngle(fp.getAngle());
				
				BitSet tpv = new BitSet(maxVoxelPerSide * maxVoxelPerSide * maxVoxelPerSide);
				cutModel1(fp, tpv, status, 100d * 1 / pictures.size() / 11);
				long cutVolume = 0;
				synchronized (transparentVoxels) {
					for (int x = 0; x < maxVoxelPerSide; x++) {
						if (status.wantsToStop())
							break;
						for (int y = 0; y < maxVoxelPerSide; y++)
							for (int z = 0; z < maxVoxelPerSide; z++)
								if (tpv.get(x * maxVoxelPerSide * maxVoxelPerSide + y * maxVoxelPerSide + z))
									transparentVoxels[x][y][z]++;
								else
									System.out.println("Found solid voxel.");
					}
				}
				
				tso.addInt(1);
				System.out.println("Finished cut " + tso.getInt() + "/" + pictures.size() + " ("
									+ tsoRunCount.getInt() + " active)");
				status.setCurrentStatusText2("Finished cut " + tso.getInt() + "/" + pictures.size() + " ("
									+ tsoRunCount.getInt() + " active)");
				tsoRunCount.addInt(-1);
			}
		};
	}
	
	private Runnable cuttt2(final BackgroundTaskStatusProviderSupportingExternalCall status,
						final ThreadSafeOptions tso, final ThreadSafeOptions tsoRunCount, final MyPicture fp) {
		return new Runnable() {
			public void run() {
				tsoRunCount.addInt(1);
				
				cutModel2(fp, transparentVoxels, status, 100d * 1 / pictures.size());
				
				tso.addInt(1);
				status.setCurrentStatusText2("Finished cut " + tso.getInt() + "/" + pictures.size() + " ("
									+ tsoRunCount.getInt() + " active)");
				tsoRunCount.addInt(-1);
			}
		};
	}
	
	private void colorModelRGB(final ArrayList<MyPicture> pictures, final ArrayList<Color> palette,
						BackgroundTaskStatusProviderSupportingExternalCall status, final boolean rgb, ExecutorService runColor) {
		if (rgb)
			System.out.println("Recolor Cube... (using true color RGBA generation mode)");
		else
			System.out.println("Recolor Cube... (using palette with " + palette.size() + " colors)");
		double x, y, z;
		double voxelSizeX = cubeSideLengthX / maxVoxelPerSide;
		double voxelSizeY = cubeSideLengthY / maxVoxelPerSide;
		final double voxelSizeZ = cubeSideLengthZ / maxVoxelPerSide;
		x = -cubeSideLengthX / 2d;
		status.setCurrentStatusText1("Colorize Cube");
		status.setCurrentStatusText2(rgb ? "RGBA Mode active" : "Indexed Color Mode active");
		
		for (int xi = 0; xi < maxVoxelPerSide; xi++) {
			if (status.wantsToStop())
				break;
			status.setCurrentStatusValueFine(100d * xi / maxVoxelPerSide);
			y = -cubeSideLengthY / 2d;
			for (int yi = 0; yi < maxVoxelPerSide; yi++) {
				z = -cubeSideLengthZ / 2d;
				final double xF = x;
				final double yF = y;
				final double zF = z;
				final int xiF = xi;
				final int yiF = yi;
				runColor.submit(new Runnable() {
					@Override
					public void run() {
						processOneSlice(pictures, palette, rgb, xF, yF, zF, voxelSizeZ, xiF, yiF);
					}
				});
				y += voxelSizeY;
			}
			x += voxelSizeX;
		}
	}
	
	private void processOneSlice(ArrayList<MyPicture> pictures, ArrayList<Color> palette, boolean rgb, double x, double y, double z, double voxelSizeZ, int xi,
						int yi) {
		for (int zi = 0; zi < maxVoxelPerSide; zi++) {
			// determine color
			ArrayList<Color> cc = new ArrayList<Color>();
			
			if (byteCube[xi][yi][zi] < PROBABILITY_THRESHOLD) {
				
				// determine nearest 2 images for colorization
				int zii = zi - maxVoxelPerSide / 2;
				if (zii == 0)
					zii = 1;
				int yii = yi - maxVoxelPerSide / 2;
				if (yii == 0)
					yii = 1;
				int xii = xi - maxVoxelPerSide / 2;
				if (xii == 0)
					xii = 1;
				
				double voxelDegree = Math.PI - MathUtils3D.getAngle(zii, xii);
				
				MyPicture bestIdx = null;
				MyPicture bestIdx2 = null;
				double minDegreeDist = Double.MAX_VALUE;
				for (MyPicture p : pictures) {
					double angle = p.getAngle();
					if (Math.abs(angle - voxelDegree) < minDegreeDist) {
						bestIdx = p;
						minDegreeDist = Math.abs(angle - voxelDegree);
					}
				}
				if (bestIdx != null) {
					minDegreeDist = Double.MAX_VALUE;
					for (MyPicture p : pictures) {
						if (p == bestIdx)
							continue;
						double angle = p.getAngle();
						if (Math.abs(angle - voxelDegree) < minDegreeDist) {
							bestIdx2 = p;
							minDegreeDist = Math.abs(angle - voxelDegree);
						}
					}
				}
				for (MyPicture p : pictures) {
					if (p != bestIdx && p != bestIdx2)
						continue;
					
					double angle = p.getAngle();
					double cos = p.getCosAngle();
					double sin = p.getSinAngle();
					boolean isTop = p.getIsTop();
					Color c = p.getPixelColor(getTargetRelativePixel(getRotatedPoint(angle, x, y, z, cos, sin, isTop)));
					// if (c != null)
					// if (ColorUtil.deltaE2000(c, PhenotypeAnalysisTask.BACKGROUND_COLOR) < 10)
					// c = null;
					if (c == null) {
						XYcubePointRelative rel = getTargetRelativePixel(getRotatedPoint(angle, x, y, z, cos, sin,
											isTop));
						for (int sx = -20; sx <= 20; sx++)
							for (int sy = -20; sy <= 20; sy++) {
								c = p.getPixelColor(rel, sx, sy);
								if (c != null)
									cc.add(c);
							}
					} else
						cc.add(c);
				}
			}
			if (cc.size() == 0) {
				byteCube[xi][yi][zi] = (byte) 0;
				if (rgb) {
					// TRANSPARENT_COLOR;
					rgbCube[xi][yi][zi] = 0;
				}
			} else {
				if (rgb) {
					Color c = ColorUtil.getMaxSaturationColor(cc);
					// int tr = (int) (255d - 0.05d * byteCube[xi][yi][zi]);
					// if (tr < 0)
					// tr = 255;
					int tr = 255;
					rgbCube[xi][yi][zi] = ColorUtil.getInt(tr, c.getRed(), c.getGreen(), c.getBlue());
				} else {
					Color c = ColorUtil.getMaxSaturationColor(cc);
					int nearestColor = ColorUtil.findBestColorIndex(palette, c);
					byteCube[xi][yi][zi] = (byte) (nearestColor + byteCube[xi][yi][zi] * 255 / maxPossibleLevels);
				}
			}
			z += voxelSizeZ;
		}
	}
	
	private void cutModel1(MyPicture p, BitSet transparentVoxels,
						BackgroundTaskStatusProviderSupportingExternalCall status, double progressStep) {
		
		double angle = p.getAngle();
		
		double cos = p.getCosAngle();
		double sin = p.getSinAngle();
		
		double x, y, z;
		double voxelSizeX = cubeSideLengthX / maxVoxelPerSide;
		double voxelSizeY = cubeSideLengthY / maxVoxelPerSide;
		double voxelSizeZ = cubeSideLengthZ / maxVoxelPerSide;
		x = -cubeSideLengthX / 2d;
		long vcnt = (maxVoxelPerSide * maxVoxelPerSide * maxVoxelPerSide);
		double smallProgressStep = progressStep / maxVoxelPerSide;
		boolean isTopViewPicture = p.getIsTop();
		for (int xi = 0; xi < maxVoxelPerSide; xi++) {
			if (status.wantsToStop())
				break;
			status.setCurrentStatusText1("Process " + vcnt + " VOXELS " + ((int) (100d * xi / maxVoxelPerSide)) + "%...");
			y = -cubeSideLengthY / 2d;
			for (int yi = 0; yi < maxVoxelPerSide; yi++) {
				z = -cubeSideLengthZ / 2d;
				for (int zi = 0; zi < maxVoxelPerSide; zi++) {
					if (p.isTransparentPixel(getTargetRelativePixel(getRotatedPoint(angle, x, y, z, cos, sin,
										isTopViewPicture))))
						transparentVoxels.set(xi * maxVoxelPerSide * maxVoxelPerSide + yi * maxVoxelPerSide + zi);
					z += voxelSizeZ;
				}
				y += voxelSizeY;
			}
			x += voxelSizeX;
			status.setCurrentStatusValueFineAdd(smallProgressStep);
		}
	}
	
	private void cutModel2(MyPicture p, byte[][][] transparentVoxels,
						BackgroundTaskStatusProviderSupportingExternalCall status, double progressStep) {
		
		double angle = p.getAngle();
		
		double cos = p.getCosAngle();
		double sin = p.getSinAngle();
		
		double x, y, z;
		double voxelSizeX = cubeSideLengthX / maxVoxelPerSide;
		double voxelSizeY = cubeSideLengthY / maxVoxelPerSide;
		double voxelSizeZ = cubeSideLengthZ / maxVoxelPerSide;
		x = -cubeSideLengthX / 2d;
		long vcnt = (maxVoxelPerSide * maxVoxelPerSide * maxVoxelPerSide);
		double smallProgressStep = progressStep / maxVoxelPerSide;
		boolean isTopViewPicture = p.getIsTop();
		System.out.println("CUTTT 2... (angle: " + angle + ")");
		for (int xi = 0; xi < maxVoxelPerSide; xi++) {
			if (status.wantsToStop())
				break;
			status.setCurrentStatusText1("Process " + vcnt + " VOXELS " + ((int) (100d * xi / maxVoxelPerSide)) + "%...");
			y = -cubeSideLengthY / 2d;
			for (int yi = 0; yi < maxVoxelPerSide; yi++) {
				z = -cubeSideLengthZ / 2d;
				for (int zi = 0; zi < maxVoxelPerSide; zi++) {
					if (p.isTransparentPixel(getTargetRelativePixel(getRotatedPoint(angle, x, y, z, cos, sin, isTopViewPicture)))) {
						transparentVoxels[xi][yi][zi]++;
					}
					z += voxelSizeZ;
				}
				y += voxelSizeY;
			}
			x += voxelSizeX;
			status.setCurrentStatusValueFineAdd(smallProgressStep);
		}
	}
	
	private XYZpointRealDistance getRotatedPoint(double angle, double x, double y, double z, double cos, double sin,
						boolean isTop) {
		XYZpointRealDistance p = new XYZpointRealDistance(x, y, z);
		if (isTop)
			p.rotateForTopView();
		else
			p.rotateY(angle, cos, sin);
		return p;
	}
	
	private XYcubePointRelative getTargetRelativePixel(XYZpointRealDistance cubePoint) {
		double halfImageSizeX = cubeSideLengthX / 2 / (widthFactor / 100d);
		double halfImageSizeY = cubeSideLengthY / 2;
		cubePoint.z += cameraDistance;
		double xt = cubePoint.x * cubePoint.z / (cameraDistance);
		double yt = cubePoint.y * cubePoint.z / (cameraDistance);
		// double xt = cubePoint.x; // * p.z / (cameraDistance);
		// double yt = cubePoint.y; // * p.z / (cameraDistance);
		xt /= halfImageSizeX * 2;
		yt /= halfImageSizeY * 2;
		return new XYcubePointRelative(xt, yt);
	}
	
	private void generateNormalizedByteCube(GenerationMode mode) {
		if (generated)
			return;
		this.byteCube = new int[maxVoxelPerSide][maxVoxelPerSide][maxVoxelPerSide];
		
		if (mode == GenerationMode.COLORED_RGBA) {
			this.rgbCube = new int[maxVoxelPerSide][maxVoxelPerSide][maxVoxelPerSide];
		}
		
		double max = 0;
		for (int x = 0; x < maxVoxelPerSide; x++)
			for (int y = 0; y < maxVoxelPerSide; y++)
				for (int z = 0; z < maxVoxelPerSide; z++)
					if (transparentVoxels[x][y][z] > max)
						max = transparentVoxels[x][y][z];
		if (palette != null && palette.size() > 0)
			this.maxPossibleLevels = 255 / palette.size();
		else
			this.maxPossibleLevels = 255;
		for (int x = 0; x < maxVoxelPerSide; x++)
			for (int y = 0; y < maxVoxelPerSide; y++)
				for (int z = 0; z < maxVoxelPerSide; z++) {
					byteCube[x][y][z] = (int) (maxPossibleLevels * transparentVoxels[x][y][z] / max);
				}
		generated = true;
	}
	
	public int[][][] getRGBcubeResult() {
		return rgbCube;
	}
	
	public String getPaletteString() {
		// "0 0 0|0 3 0|..."
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 255; i++) {
			sb.append(getColorString(palette.get(i % palette.size())) + "|");
		}
		sb.append(getColorString(Color.WHITE) + "|");
		return sb.toString();
	}
	
	private String getColorString(Color color) {
		return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
	}
	
	public String getTransferFunctionString() {
		// "0|0|0|1|1|..."
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 256; i++) {
			if (i < palette.size())
				sb.append("255|");
			else
				sb.append("0|");
		}
		return sb.toString();
	}
	
	public int getResolution() {
		return maxVoxelPerSide;
	}
	
	public void calculateModelMotionScan(final BackgroundTaskStatusProviderSupportingExternalCall status) {
		generateNormalizedByteCube(GenerationMode.COLORED_RGBA);
		final MyPicture p1 = pictures.get(0);
		final MyPicture p2 = pictures.get(1);
		//
		final double scaleX = p1.width / (double) maxVoxelPerSide / (100 / widthFactor);
		final double scaleY = p1.height / (double) maxVoxelPerSide;
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		//
		ExecutorService run = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs());
		//
		status.setCurrentStatusValue(-1);
		//
		status.setCurrentStatusText1("Finished scanline 0 / " + maxVoxelPerSide);
		status.setCurrentStatusText2("Reconstructing Motion Structure");
		for (int vvx = 0; vvx < maxVoxelPerSide; vvx++) {
			final int vx = vvx;
			run.submit(new Runnable() {
				public void run() {
					for (int vy = 0; vy < maxVoxelPerSide; vy++) {
						if (status.wantsToStop())
							return;
						int ix = (int) (vx * scaleX + vx * 50 / widthFactor);
						int iy = (int) (vy * scaleY);
						int depth = getDepthOfPoint(20, p1, p2, ix, iy, 50) / 2 + maxVoxelPerSide / 2;
						for (int vz = 0; vz < maxVoxelPerSide; vz++) {
							byteCube[vx][vx][vz] = vz < depth ? 0 : Byte.MAX_VALUE;
							Color c;
							try {
								c = vz < depth || depth == maxVoxelPerSide / 2 ? TRANSPARENT_COLOR : new Color(p1
													.getRGB(ix, iy));
							} catch (Exception e) {
								c = Color.BLUE;
							}
							rgbCube[vx][vy][vz] = c.getRGB();
						}
					}
					tso.addInt(1);
					status.setCurrentStatusText1("Finished scanline " + vx + " / " + maxVoxelPerSide);
					status.setCurrentStatusValueFine(100d * ((tso.getInt()) / (double) maxVoxelPerSide));
				}
			});
		}
		run.shutdown();
		try {
			run.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		} // wait max 7 days for result
			//
		status.setCurrentStatusValue(0);
		//
	}
	
	private static int getDepthOfPoint(final int rectWidth, final MyPicture p1, final MyPicture p2, int mx, int my,
						int scanRange) {
		double minDiff = Double.POSITIVE_INFINITY;
		int minI = 0;
		for (int i = -scanRange / 2; i < scanRange / 2; i++) {
			double diff = compareImageParts(p1, p2, mx, my, mx + i, my, rectWidth);
			diff += Math.abs(i / 20d);
			if (diff <= minDiff) {
				minDiff = diff;
				minI = i;
			}
		}
		return minI;
	}
	
	//
	public static double compareImageParts(MyPicture p1, MyPicture p2, int x1, int y1, int x2, int y2, int wh) {
		double diff = 0;
		try {
			for (int scanX = 0; scanX < wh; scanX++) {
				for (int scanY = 0; scanY < wh; scanY++) {
					int c1 = p1.getRGB(x1 + scanX, y1 + scanY);
					int c2 = p2.getRGB(x2 + scanX, y2 + scanY);
					diff += ColorUtil.deltaE2000(new Color(c1), new Color(c2));
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
		return diff;
	}
}

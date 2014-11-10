package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.ImageOperation.HSVChannel;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.operation.fluoop.FluoAnalysis;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeColorDepth;

public class BlCalcIntensityFeature3DHistogram extends AbstractBlock implements CalculatesProperties {
	
	int cubedimension = 256;
	int background = ImageOperation.BACKGROUND_COLORint;
	
	@Override
	protected void prepare() {
		Image vis = input().masks().vis();
		Image fluo = input().masks().fluo();
		Image nir = input().masks().nir();
		
		if (vis != null && fluo != null && nir != null) {
			CubeProcessing cp = new CubeProcessing(256);
			cp.updateCube(vis, fluo, nir);
			if (true) {
				ImageStack is = cp.getCubeAsImageStack();
				is.show("cube");
			}
			int[][][] intVolume = cp.getNormalizedCube(0, 255);
			LoadedVolumeExtension volume = null;
			Sample sample = null;
			volume = new LoadedVolumeExtension(sample, intVolume);
			
			volume.setDimensionX(cubedimension);
			volume.setDimensionY(cubedimension);
			volume.setDimensionZ(cubedimension);
			
			BlockResultSet summaryResult = null;
			volume.setColorDepth(VolumeColorDepth.RGBA.toString());
			if (getBoolean("Save Volume Dataset", false)) {
				summaryResult.setVolume("RESULT_volume.plant3d.cube", volume);
			}
			
			if (getBoolean("Debug - Save 3D-Render to Desktop", false)) {
				try {
					File f = new File(ReleaseInfo.getDesktopFolder() + "/render/" + "intensity_cube" + ".gif");
					BackgroundTaskStatusProviderSupportingExternalCall optStatus = null;
					MyByteArrayInputStream cnt = volume.getSideViewGif(800, 600, optStatus);
					ResourceIOManager.copyContent(cnt, new FileOutputStream(f));
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		super.prepare();
	}
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Calculate Intensity 3-D Feature Histogram";
	}
	
	@Override
	public String getDescription() {
		return "Block creates a 3-D Histogram.";
	}
	
	class CubeProcessing {
		int[][][] cube;
		
		public CubeProcessing(int dimensionsScale) {
			cube = new int[256][256][256];
		}
		
		public void updateCube(Image vis, Image fluo, Image nir) {
			float[] hue = vis.io().getHSVChannel(HSVChannel.H, true);
			float[] fluoIntensity = fluo.io().convertFluo2intensity(FluoAnalysis.CLASSIC, 220).getImage().show("fluo2").getFloatChannel(Channel.RGB_R);
			int[] nirIntensity = nir.getAs1A();
			
			int rf, rn, rgbf, rgbn;
			
			for (int idx = 0; idx < hue.length; idx++) {
				rf = (int) (fluoIntensity[idx] * 255);
				rgbn = nirIntensity[idx] * 255;
				rn = ((rgbn >> 16) & 0xff);
				if (hue[idx] == -1.0 || rf < 1)
					continue;
				// vis is direkt intensity
				int h = (int) (hue[idx] * 255);
				// fluo and nir has to convert
				int f = rf;
				int n = rn;
				cube[h][f][n]++;
			}
		}
		
		private int[][][] getNormalizedCube(int M, int N) {
			int[][][] res = new int[cube.length][cube.length][cube.length];
			double max = 0.0;
			for (int idxL = 0; idxL < cube.length; idxL++) {
				for (int idxA = 0; idxA < cube[0].length; idxA++) {
					for (int idxB = 0; idxB < cube[0][0].length; idxB++) {
						double n = cube[idxL][idxA][idxB];
						n = Math.log(n);
						if (n > max)
							max = n;
					}
				}
			}
			for (int idxL = 0; idxL < cube.length; idxL++) {
				for (int idxA = 0; idxA < cube[0].length; idxA++) {
					for (int idxB = 0; idxB < cube[0][0].length; idxB++) {
						if (cube[idxL][idxA][idxB] > 0) {
							double val = Math.log(cube[idxL][idxA][idxB]);
							int val2 = (int) (M + val / max * N);
							res[idxL][idxA][idxB] = val2;
						} else
							if (M > 0)
								res[idxL][idxA][idxB] = M;
							else
								res[idxL][idxA][idxB] = 0;
					}
				}
			}
			return res;
		}
		
		public int[][][] getIntCube() {
			return cube;
		}
		
		public ImageStack getCubeAsImageStack() {
			int[][][] cube = getNormalizedCube(0, 255);
			
			ImageStack is = new ImageStack();
			
			for (int idxX = 0; idxX < this.cube.length; idxX++) {
				int[][] slice = new int[this.cube.length][this.cube.length];
				
				for (int idxY = 0; idxY < this.cube[0].length; idxY++) {
					for (int idxZ = 0; idxZ < this.cube[0][0].length; idxZ++) {
						if (cube[idxX][idxY][idxZ] > 0) {
							int val = cube[idxX][idxY][idxZ];
							slice[idxY][idxZ] = new Color(val, val, val).getRGB();;
						}
					}
				}
				is.addImage("Lab: " + idxX, new Image(slice));
			}
			return is;
		}
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return null; // TODO !
	};
}
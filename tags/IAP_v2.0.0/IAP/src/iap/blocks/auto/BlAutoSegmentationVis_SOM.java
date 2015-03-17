package iap.blocks.auto;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import qmwi.kseg.som.CSV_SOM_dataEntry;
import qmwi.kseg.som.DataSet;
import qmwi.kseg.som.SOMdataEntry;
import de.ipk.ag_ba.image.operation.channels.ChannelProcessing;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Use SOM to segment image into different classes.
 * 
 * @author klukas
 */
public class BlAutoSegmentationVis_SOM extends AbstractSnapshotAnalysisBlock {
	
	private boolean auto_tune;
	private final boolean useLAB = true;
	private final ArrayList<Color> colors = new ArrayList<Color>();
	private final ArrayList<Boolean> foreground = new ArrayList<Boolean>();
	
	@Override
	protected void prepare() {
		super.prepare();
		this.auto_tune = getBoolean("Auto-Tune", true);
		this.auto_tune = getBoolean("Use LAB instead of HSV", true);
		int n = getInt("N Colors", 5);
		colors.add(Color.GREEN);
		foreground.add(true);
		colors.add(Color.YELLOW);
		foreground.add(true);
		colors.add(Color.DARK_GRAY);
		foreground.add(false);
		colors.add(Color.WHITE);
		foreground.add(false);
		colors.add(Color.BLUE);
		foreground.add(false);
		for (int i = 0; i < n; i++) {
			Color ic = i < colors.size() ? colors.get(i) : Color.BLACK;
			boolean ifg = i < foreground.size() ? foreground.get(i) : false;
			Color c = getColor("Color " + (i + 1), ic);
			boolean foreg = getBoolean((i + 1) + " is foreground", ifg);
			if (i < colors.size())
				colors.set(i, c);
			else
				colors.add(c);
			if (i < foreground.size())
				foreground.set(i, foreg);
			else
				foreground.add(foreg);
		}
	}
	
	@Override
	protected synchronized Image processVISmask() {
		if (input().masks().vis() == null) {
			return null;
		}
		
		float[][] channels = useLAB ? input().masks().vis().io().channels().getLAB() : input().masks().vis().io().channels().getHSV();
		
		int w = input().masks().vis().getWidth();
		int h = input().masks().vis().getHeight();
		
		float[][] somInitChannelData = new float[colors.size()][3];
		float[] min = new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
		float[] max = new float[] { -1000, -1000, -1000 };
		for (int group = 0; group < colors.size(); group++) {
			float[] channelValues = useLAB ?
					ChannelProcessing.getLAB(colors.get(group)) :
					ChannelProcessing.getHSV(colors.get(group));
			somInitChannelData[group][0] = channelValues[0];
			somInitChannelData[group][1] = channelValues[1];
			somInitChannelData[group][2] = channelValues[2];
			for (int c = 0; c < 3; c++) {
				if (channelValues[c] < min[c])
					min[c] = channelValues[c];
				if (channelValues[0] > max[c])
					max[c] = channelValues[c];
			}
		}
		float[] range = new float[] { max[0] - min[0], max[1] - min[1], max[2] - min[2] };
		for (int group = 0; group < colors.size(); group++) {
			for (int c = 0; c < 3; c++) {
				float val = somInitChannelData[group][c];
				float mran = range[c];
				float minv = min[c];
				somInitChannelData[group][c] = (val - minv) / mran * 2 - 1;
			}
		}
		DataSet data = new DataSet();
		data.setGroupDescription("C1;C2;C3");
		data.initSOM(colors.size(), colors.size(), 0, 10, 3, false, somInitChannelData);
		
		for (int px = 0; px < channels[0].length; px++) {
			Double c1 = new Double(channels[0][px]);
			Double c2 = new Double(channels[1][px]);
			Double c3 = new Double(channels[2][px]);
			CSV_SOM_dataEntry de = new CSV_SOM_dataEntry(3, px);
			de.setColumnData(0, c1);
			de.setColumnData(1, c2);
			de.setColumnData(2, c3);
			data.addEntry(de);
		}
		
		data.setBetaAndGamma(0.1, 2);
		data.trainOrUseSOM(true, 1, new String[] { "C1", "C2", "C3" }, 1000, null, channels[0].length / 5);
		Vector<SOMdataEntry> classes[] =
				data.trainOrUseSOM(false, 1, new String[] { "C1", "C2", "C3" }, 100, null, 0);
		data.getSOMmap().printMatrix();
		int[] resImgData = new int[channels[0].length];
		
		for (int classIdx = 0; classIdx < classes.length; classIdx++) {
			int cnt = 0;
			Color cc = colors.get(classIdx);
			int ci = cc.getRGB();
			Vector<SOMdataEntry> val = classes[classIdx];
			for (SOMdataEntry sde : val) {
				Integer px = (Integer) sde.getUserData();
				resImgData[px] = ci;
				cnt++;
			}
			System.out.println("CLASS=" + classIdx + ", CNT=" + cnt + ", Color=" + cc);
		}
		return new Image(w, h, resImgData);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Auto-tuning VIS Segmentation (SOM)";
	}
	
	@Override
	public String getDescription() {
		return "Uses the Self-Organizing-Map algorithm and a list of predefined prototype colors to segment the image " +
				"into foreground and background.";
	}
}

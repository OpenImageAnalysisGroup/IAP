package iap.blocks.data_structures;

import iap.pipelines.ImageProcessorOptions;
import info.StopWatch;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public abstract class AbstractImageAnalysisBlockFIS implements ImageAnalysisBlock {
	
	private ImageStack debugStack;
	protected ImageProcessorOptions options;
	private MaskAndImageSet input;
	private BlockResultSet properties;
	private int blockPositionInPipeline;
	private int well;
	
	private static LinkedHashMap<String, ThreadSafeOptions> id2time = new LinkedHashMap<String, ThreadSafeOptions>();
	
	public AbstractImageAnalysisBlockFIS() {
		// empty
	}
	
	public void addExecutionTime(ExecutionTimeStep step, long execTime) {
		boolean error = execTime < 0;
		if (error)
			execTime = -execTime;
		String stepName = step + "";
		String blockName = getBlockType() + "//" + this.getClass().getCanonicalName() + "";
		synchronized (id2time) {
			if (!id2time.containsKey(stepName))
				id2time.put(stepName, new ThreadSafeOptions());
			id2time.get(stepName).addLong(execTime);
			id2time.get(stepName).addInt(1);
			if (error)
				id2time.get(stepName).addDouble(1);
			if (!id2time.containsKey(blockName))
				id2time.put(blockName, new ThreadSafeOptions());
			id2time.get(blockName).addLong(execTime);
			id2time.get(blockName).addInt(1);
			if (error)
				id2time.get(blockName).addDouble(1);
		}
	}
	
	protected int getWellIdx() {
		return well;
	}
	
	public boolean getBoolean(String setting, boolean defaultValue) {
		if (IAPmain.getRunMode() != IAPrunMode.SWING_MAIN && setting != null && setting.equals("debug")) {
			boolean ret = options != null ? options.getBooleanSetting(this, setting, defaultValue) : defaultValue;
			if (ret)
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Enabled debug setting is ignored, as IAP is not running in Swing GUI mode.");
			return false;
		}
		
		return options != null ? options.getBooleanSetting(this, setting, defaultValue) : defaultValue;
	}
	
	public boolean getBoolean(ImageAnalysisBlock block, String setting, boolean defaultValue) {
		return options.getBooleanSetting(block, setting, defaultValue);
	}
	
	public int getInt(String setting, int defaultValue) {
		return options.getIntSetting(this, setting, defaultValue);
	}
	
	public double getDouble(String setting, double defaultValue) {
		return options.getDoubleSetting(this, setting, defaultValue);
	}
	
	public String getString(String setting, String defaultValue) {
		return options.getStringSetting(this, setting, defaultValue);
	}
	
	public Color getColor(String setting, Color defaultValue) {
		return options.getColorSetting(this, setting, defaultValue);
	}
	
	public Integer[] getIntArray(String setting, Integer[] defaultValue) {
		return options.getIntArraySetting(this, setting, defaultValue);
	}
	
	@Override
	public void setInputAndOptions(int well, MaskAndImageSet input, ImageProcessorOptions options, BlockResultSet properties,
			int blockPositionInPipeline,
			ImageStack debugStack) {
		this.input = input;
		this.well = well;
		this.options = options;
		this.properties = properties;
		this.blockPositionInPipeline = blockPositionInPipeline;
		this.debugStack = debugStack;
	}
	
	protected boolean debugValues;
	protected boolean preventDebugValues;
	
	@Override
	public void setPreventDebugValues(boolean b) {
		preventDebugValues = b;
	}
	
	protected void prepare() {
		debugValues = !preventDebugValues && isChangingImages() && getBoolean("debug", false);
		if (debugValues) {
			if (input().images().vis() != null || input().masks().vis() != null)
				debugPipelineBlock(this.getClass(), CameraType.VIS, input(), getProperties(), options, getBlockPosition(), this);
			if (input().images().fluo() != null || input().masks().fluo() != null)
				debugPipelineBlock(this.getClass(), CameraType.FLUO, input(), getProperties(), options, getBlockPosition(), this);
			if (input().images().nir() != null || input().masks().nir() != null)
				debugPipelineBlock(this.getClass(), CameraType.NIR, input(), getProperties(), options, getBlockPosition(), this);
			if (input().images().ir() != null || input().masks().ir() != null)
				debugPipelineBlock(this.getClass(), CameraType.IR, input(), getProperties(), options, getBlockPosition(), this);
		}
	}
	
	@Override
	public final MaskAndImageSet process() throws InterruptedException {
		StopWatch w = debugStart(this.getClass().getSimpleName());
		MaskAndImageSet res = run();
		debugEnd(w);
		return res;
	}
	
	protected abstract MaskAndImageSet run() throws InterruptedException;
	
	protected StopWatch debugStart(String task) {
		if (debugStack != null && isChangingImages())
			if (getBoolean("enabled", true))
				debugStack.addImage("Input for " + task, input().getOverviewImage(
						SystemOptions.getInstance().getInteger("IAP", "Debug-Overview-Image-Width", 1680)
						), task);
		if (SystemOptions.getInstance().getBoolean("IAP", "Debug-Stop-Block-Exection-Times", false)) {
			if (SystemOptions.getInstance().getBoolean("IAP", "Debug-Display-Each-Step", false))
				if (input().masks() != null)
					input().masks().fluo().show("Mask-Input for step: " + task);
				else
					input().images().fluo().show("Image-Input for step: " + task);
			return new StopWatch("phytochamberTopImageProcessor: " + task);
		} else
			return null;
	}
	
	protected boolean isChangingImages() {
		return true;
	}
	
	protected void debugEnd(StopWatch w) {
		if (w != null) {
			w.printTime(10);
		}
	}
	
	public MaskAndImageSet input() {
		return input;
	}
	
	protected BlockResultSet getProperties() {
		return properties;
	}
	
	protected int getBlockPosition() {
		return blockPositionInPipeline;
	}
	
	/**
	 * If needed, process the results in allResultsForSnapshot, and add the new data to summaryResult
	 */
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws InterruptedException {
		
		PostProcessor pp = getPostProcessor(time2allResultsForSnapshot);
		if (pp != null)
			for (Long time : new ArrayList<Long>(time2inSamples.keySet())) {
				TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
				if (!time2summaryResult.containsKey(time))
					time2summaryResult.put(time, new HashMap<Integer, BlockResultSet>());
				for (HashMap<Integer, BlockResultSet> trayWithResult : allResultsForSnapshot.values()) {
					for (Integer tray : trayWithResult.keySet()) {
						if (!time2summaryResult.get(time).containsKey(tray))
							time2summaryResult.get(time).put(tray, new BlockResults(null));
						BlockResultSet summaryResult = time2summaryResult.get(time).get(tray);
						if (pp != null) {
							ArrayList<BlockPropertyValue> rl = pp.postProcessCalculatedProperties(time, tray);
							if (rl != null)
								for (BlockPropertyValue bpv : rl) {
									summaryResult.setNumericProperty(getBlockPosition(), bpv.getName(), bpv.getValue(), bpv.getUnit());
								}
						}
					}
				}
			}
	}
	
	/**
	 * Override this method to post-process results more easily from specific time-points and tray analysis.
	 * 
	 * @return PostProcessor, which processes previously calculated properties.
	 */
	protected PostProcessor getPostProcessor(TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot) {
		return null;
	}
	
	protected void reportError(Error error, String errorMessage) {
		System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: ERROR IN BLOCK " + getClass().getSimpleName() + ">" + errorMessage);
		if (error != null)
			error.printStackTrace();
		if (SystemOptions.getInstance().getBoolean("IAP", "Debug - System.Exit in case of pipeline error",
				IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE))
			System.exit(SystemOptions.getInstance().getInteger(
					"IAP", "Debug: System.Exit return value in case of pipeline error", 1));
	}
	
	protected void reportError(Exception error, String errorMessage) {
		System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: EXCEPTION IN BLOCK " + getClass().getSimpleName() + ">" + errorMessage);
		if (error != null)
			error.printStackTrace();
		IAPmain.errorCheck(errorMessage);
	}
	
	protected String getRemarkSetting(String remarkID, String defaultReturn) {
		ImageData info = input() != null && input.images() != null ? input().images().getVisInfo() : null;
		if (info == null)
			info = input() != null && input.images() != null ? input().images().getFluoInfo() : null;
		if (info == null)
			info = input() != null && input.images() != null ? input().images().getNirInfo() : null;
		if (info == null)
			return defaultReturn;
		else {
			try {
				String rem = info.getParentSample().getParentCondition().getExperimentHeader().getRemark();
				if (rem != null)
					for (String r : rem.split("//")) {
						r = r.trim();
						if (r.startsWith(remarkID) && r.contains(":"))
							return r.split(":", 2)[1].trim();
					}
				return defaultReturn;
			} catch (Exception e) {
				System.err.println(SystemAnalysis.getCurrentTime() + ">Error processing remark information:");
				e.printStackTrace();
				return defaultReturn;
			}
		}
	}
	
	protected void calculateRelativeValues(TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult, int blockPosition,
			String[] desiredProperties) {
		final double timeForOneDayD = 1000 * 60 * 60 * 24d;
		HashMap<String, TreeMap<String, Long>> prop2config2lastHeightAndWidthTime = new HashMap<String, TreeMap<String, Long>>();
		HashMap<String, TreeMap<String, Double>> prop2config2lastHeightAndWidth = new HashMap<String, TreeMap<String, Double>>();
		for (Long time : time2inSamples.keySet()) {
			TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (allResultsForSnapshot == null || allResultsForSnapshot.keySet() == null)
				continue;
			if (!time2summaryResult.containsKey(time))
				time2summaryResult.put(time, new HashMap<Integer, BlockResultSet>());
			HashMap<Integer, BlockResultSet> summaryResultArray = time2summaryResult.get(time);
			for (String key : allResultsForSnapshot.keySet()) {
				for (Integer tray : allResultsForSnapshot.get(key).keySet()) {
					if (!summaryResultArray.containsKey(tray))
						summaryResultArray.put(tray, new BlockResults(null));
					BlockResultSet summaryResult = summaryResultArray.get(tray);
					BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
					for (String property : desiredProperties) {
						ArrayList<BlockPropertyValue> sr = rt.getPropertiesSearch(true, property);
						// if (sr.isEmpty())
						// System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Result named '" + property + "' not found.");
						for (BlockPropertyValue v : sr) {
							if (v.getValue() != null) {
								if (!prop2config2lastHeightAndWidth.containsKey(property))
									prop2config2lastHeightAndWidth.put(property, new TreeMap<String, Double>());
								if (!prop2config2lastHeightAndWidthTime.containsKey(property))
									prop2config2lastHeightAndWidthTime.put(property, new TreeMap<String, Long>());
								
								Double lastPropertyValue = prop2config2lastHeightAndWidth.get(property).get(key);
								if (lastPropertyValue != null && lastPropertyValue > 0 && prop2config2lastHeightAndWidth.get(property).containsKey(key) &&
										time - prop2config2lastHeightAndWidthTime.get(property).get(key) > 0) {
									double currentPropertyValue = v.getValue().doubleValue();
									double ratio = currentPropertyValue / lastPropertyValue;
									double days = (time - prop2config2lastHeightAndWidthTime.get(property).get(key)) / timeForOneDayD;
									double ratioPerDay = Math.pow(ratio, 1d / days);
									summaryResult.setNumericProperty(blockPosition, property + ".relative", ratioPerDay, "relative/day");
								}
								
								double width = v.getValue().doubleValue();
								prop2config2lastHeightAndWidthTime.get(property).put(key, time);
								prop2config2lastHeightAndWidth.get(property).put(key, width);
							}
						}
					}
				}
			}
		}
	}
	
	protected void debugPipelineBlock(final Class<?> blockType, final CameraType inpImageType,
			final MaskAndImageSet in,
			final BlockResultSet brs, final ImageProcessorOptions options,
			final int blockPos, final AbstractImageAnalysisBlockFIS inst) {
		
		final MaskAndImageSet inputSet = in.copy();
		final HashMap<String, Image> storedImages = new HashMap<String, Image>(brs.getImages());
		
		final ZoomedImage ic = new ZoomedImage(null);
		final JScrollPane jsp = new JScrollPane(ic);
		jsp.setBorder(BorderFactory.createLoweredBevelBorder());
		
		final JButton okButton = new JButton();
		
		okButton.setAction(new AbstractAction("Update View") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					ImageAnalysisBlock inst = (ImageAnalysisBlock) blockType.newInstance();
					ImageSet a = inputSet.images().copy();
					ImageSet b = inputSet.masks().copy();
					MaskAndImageSet ab = new MaskAndImageSet(a, b);
					brs.setImages(storedImages);
					inst.setPreventDebugValues(true);
					inst.setInputAndOptions(well, ab, options, brs, blockPos, null);
					ab = inst.process();
					int vs = jsp.getVerticalScrollBar().getValue();
					int hs = jsp.getHorizontalScrollBar().getValue();
					Image processingResultImage = ab.masks().getImage(inpImageType);
					if (processingResultImage == null)
						processingResultImage = ab.images().getImage(inpImageType);
					if (processingResultImage == null)
						throw new Exception("Processed image not available");
					
					ic.setImage(processingResultImage.getAsBufferedImage());
					jsp.setViewportView(ic);
					jsp.revalidate();
					jsp.getVerticalScrollBar().setValue(vs);
					jsp.getHorizontalScrollBar().setValue(hs);
					okButton.setText("<html><center>Update View<br>Updated " + SystemAnalysis.getCurrentTimeInclSec());
				} catch (Exception e) {
					e.printStackTrace();
					MainFrame.showMessageDialog("Error: " + e.getMessage(), "Error");
					okButton.setText("<html><center>Update View<br>Error " + e.getMessage());
				}
			}
		});
		
		JComponent editAndUpdate = TableLayout.get3Split(
				null,
				null,
				TableLayout.get3Split(
						ic.getZoomSlider(), null, okButton, TableLayout.PREFERRED, 5, TableLayout.PREFERRED),
				TableLayout.FILL, 5, TableLayout.PREFERRED);
		JComponent v = TableLayout.get3SplitVertical(
				jsp, null, editAndUpdate, TableLayout.FILL, 5, TableLayout.PREFERRED);
		v.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final Timer timer = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean changesDetected = false;
				if (changesDetected) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">Detected settings change, updating view...");
					okButton.doClick();
				}
			}
		});
		
		final JFrame jf = MainFrame.showMessageWindow(inpImageType.name(), v);
		jf.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent arg0) {
				if (!jf.isVisible())
					timer.stop();
			}
		});
		timer.setRepeats(true);
		timer.start();
	}
	
	@Override
	public int compareTo(ImageAnalysisBlock o) {
		int res = getBlockType().compareTo(o.getBlockType());
		if (res != 0)
			return res;
		else {
			return (getName() + " ").compareToIgnoreCase(o.getName() + "");
		}
	}
	
	@Override
	public String getDescriptionForParameters() {
		return null;
	}
	
	public static LinkedHashMap<String, ThreadSafeOptions> getBlockStatistics() {
		LinkedHashMap<String, ThreadSafeOptions> res = new LinkedHashMap<String, ThreadSafeOptions>();
		synchronized (id2time) {
			for (String key : id2time.keySet()) {
				ThreadSafeOptions v = id2time.get(key);
				ThreadSafeOptions nv = new ThreadSafeOptions();
				nv.setDouble(v.getDouble());
				nv.setLong(v.getLong());
				if (key.contains("/"))
					nv.setInt(v.getInt() / 10);
				else
					if (key.toUpperCase().contains("PREPARE") || key.toUpperCase().contains("POST-PROCESS"))
						nv.setInt(v.getInt());
					else
						nv.setInt(v.getInt() / 2);
				
				res.put(key, nv);
			}
		}
		return res;
	}
	
	public static void resetBlockStatistics(boolean fullReset) {
		synchronized (id2time) {
			if (fullReset)
				id2time.clear();
			else {
				for (String key : id2time.keySet()) {
					id2time.get(key).setLong(0);
					id2time.get(key).setInt(0);
				}
			}
		}
	}
}

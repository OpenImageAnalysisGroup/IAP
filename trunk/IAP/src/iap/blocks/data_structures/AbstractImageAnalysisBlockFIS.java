package iap.blocks.data_structures;

import iap.pipelines.ImageProcessorOptions;
import info.StopWatch;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.graffiti.plugin.parameter.Parameter;

import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public abstract class AbstractImageAnalysisBlockFIS implements ImageAnalysisBlockFIS {
	
	private FlexibleImageStack debugStack;
	protected ImageProcessorOptions options;
	private FlexibleMaskAndImageSet input;
	private BlockResultSet properties;
	private int blockPositionInPipeline;
	
	public AbstractImageAnalysisBlockFIS() {
		// empty
	}
	
	protected boolean getBoolean(String setting, boolean defaultValue) {
		return options != null ? options.getBooleanSetting(this, setting, defaultValue) : defaultValue;
	}
	
	protected boolean getBoolean(ImageAnalysisBlockFIS block, String setting, boolean defaultValue) {
		return options.getBooleanSetting(block, setting, defaultValue);
	}
	
	protected int getInt(String setting, int defaultValue) {
		return options.getIntSetting(this, setting, defaultValue);
	}
	
	protected double getDouble(String setting, double defaultValue) {
		return options.getDoubleSetting(this, setting, defaultValue);
	}
	
	protected String getString(String setting, String defaultValue) {
		return options.getStringSetting(this, setting, defaultValue);
	}
	
	protected Integer[] getIntArray(String setting, Integer[] defaultValue) {
		return options.getIntArraySetting(this, setting, defaultValue);
	}
	
	@Override
	public void setInputAndOptions(FlexibleMaskAndImageSet input, ImageProcessorOptions options, BlockResultSet properties,
			int blockPositionInPipeline,
			FlexibleImageStack debugStack) {
		this.input = input;
		this.options = options;
		this.properties = properties;
		this.blockPositionInPipeline = blockPositionInPipeline;
		this.debugStack = debugStack;
	}
	
	protected boolean debugValues;
	protected boolean preventDebugValues;
	
	protected void prepare() {
		debugValues = !preventDebugValues && getBoolean("debug", false);
		if (debugValues) {
			if (input().images().vis() != null && input().masks().vis() != null)
				debugPipelineBlock(this.getClass(), FlexibleImageType.VIS, input(), getProperties(), options, getBlockPosition(), this);
			if (input().images().fluo() != null && input().masks().fluo() != null)
				debugPipelineBlock(this.getClass(), FlexibleImageType.FLUO, input(), getProperties(), options, getBlockPosition(), this);
			if (input().images().nir() != null && input().masks().nir() != null)
				debugPipelineBlock(this.getClass(), FlexibleImageType.NIR, input(), getProperties(), options, getBlockPosition(), this);
			if (input().images().ir() != null && input().masks().ir() != null)
				debugPipelineBlock(this.getClass(), FlexibleImageType.IR, input(), getProperties(), options, getBlockPosition(), this);
		}
	}
	
	@Override
	public final FlexibleMaskAndImageSet process() throws InterruptedException {
		StopWatch w = debugStart(this.getClass().getSimpleName());
		FlexibleMaskAndImageSet res = run();
		debugEnd(w);
		return res;
	}
	
	protected abstract FlexibleMaskAndImageSet run() throws InterruptedException;
	
	protected StopWatch debugStart(String task) {
		if (debugStack != null && isChangingImages())
			debugStack.addImage("Input for " + task, input().getOverviewImage(
					SystemOptions.getInstance().getInteger("IAP", "Debug-Overview-Image-Width", 1680)
					), task);
		if (SystemOptions.getInstance().getBoolean("IAP", "Debug-Stop-Block-Exection-Times", true)) {
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
	
	public FlexibleMaskAndImageSet input() {
		return input;
	}
	
	protected BlockResultSet getProperties() {
		return properties;
	}
	
	protected int getBlockPosition() {
		return blockPositionInPipeline;
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws InterruptedException {
		// If needed, process the results in allResultsForSnapshot, and add the new data to summaryResult
	}
	
	protected void reportError(Error error, String errorMessage) {
		System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: ERROR IN BLOCK " + getClass().getSimpleName() + ">" + errorMessage);
		if (error != null)
			error.printStackTrace();
		if (SystemOptions.getInstance().getBoolean("IAP", "Debug: System.Exit in case of pipeline error",
				IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE))
			System.exit(SystemOptions.getInstance().getInteger(
					"IAP", "Debug: System.Exit return value in case of pipeline error", 1));
	}
	
	protected void reportError(Exception error, String errorMessage) {
		System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: EXCEPTION IN BLOCK " + getClass().getSimpleName() + ">" + errorMessage);
		if (error != null)
			error.printStackTrace();
		IAPmain.errorCheck();
	}
	
	@Override
	public Parameter[] getParameters() {
		// empty
		return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		// empty
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
			if (!time2summaryResult.containsKey(time))
				time2summaryResult.put(time, new HashMap<Integer, BlockResultSet>());
			HashMap<Integer, BlockResultSet> summaryResultArray = time2summaryResult.get(time);
			for (String key : allResultsForSnapshot.keySet()) {
				for (Integer tray : summaryResultArray.keySet()) {
					BlockResultSet summaryResult = summaryResultArray.get(tray);
					BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
					for (String property : desiredProperties) {
						ArrayList<BlockPropertyValue> sr = rt.getPropertiesExactMatch(property);
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
	
	private static void debugPipelineBlock(final Class<?> blockType, final FlexibleImageType inpImageType,
			final FlexibleMaskAndImageSet inputSet,
			final BlockResultSet brs, final ImageProcessorOptions options,
			final int blockPos, final AbstractImageAnalysisBlockFIS inst) {
		
		final ZoomedImage ic = new ZoomedImage(null);
		final JScrollPane jsp = new JScrollPane(ic);
		jsp.setBorder(BorderFactory.createLoweredBevelBorder());
		
		final JButton okButton = new JButton();
		
		okButton.setAction(new AbstractAction("Update View") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					AbstractSnapshotAnalysisBlockFIS inst = (AbstractSnapshotAnalysisBlockFIS) blockType.newInstance();
					FlexibleImageSet a = inputSet.images().copy();
					FlexibleImageSet b = inputSet.masks().copy();
					FlexibleMaskAndImageSet ab = new FlexibleMaskAndImageSet(a, b);
					inst.preventDebugValues = true;
					inst.setInputAndOptions(ab, options, brs, blockPos, null);
					ab = inst.process();
					int vs = jsp.getVerticalScrollBar().getValue();
					int hs = jsp.getHorizontalScrollBar().getValue();
					FlexibleImage processingResultImage = ab.masks().getImage(inpImageType);
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
	
}

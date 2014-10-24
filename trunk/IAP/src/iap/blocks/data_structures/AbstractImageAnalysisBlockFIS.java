package iap.blocks.data_structures;

import iap.blocks.extraction.Trait;
import iap.blocks.extraction.TraitCategory;
import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import info.StopWatch;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.ImageAndImageData;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public abstract class AbstractImageAnalysisBlockFIS implements ImageAnalysisBlock {
	
	private ImageStack debugStack;
	protected ImageProcessorOptionsAndResults optionsAndResults;
	private MaskAndImageSet input;
	private BlockResultSet resultSet;
	private int blockPositionInPipeline;
	private String well;
	
	private static LinkedHashMap<String, ThreadSafeOptions> id2time = new LinkedHashMap<String, ThreadSafeOptions>();
	
	protected Image setImageType(Image image, CameraType ct) {
		if (image != null)
			image.setCameraType(ct);
		return image;
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
	
	public AbstractImageAnalysisBlockFIS() {
		// empty
	}
	
	protected String getWellIdx() {
		return well;
	}
	
	public boolean getBoolean(String setting, boolean defaultValue) {
		if (IAPmain.getRunMode() != IAPrunMode.SWING_MAIN && setting != null && setting.equals("debug")) {
			boolean ret = optionsAndResults != null ? optionsAndResults.getBooleanSetting(this, setting, defaultValue) : defaultValue;
			if (ret)
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Enabled debug setting is ignored, as IAP is not running in Swing GUI mode.");
			return false;
		}
		
		return optionsAndResults != null ? optionsAndResults.getBooleanSetting(this, setting, defaultValue) : defaultValue;
	}
	
	public boolean setBoolean(String setting, boolean value) {
		if (optionsAndResults != null)
			return optionsAndResults.setBooleanSetting(this, setting, value);
		else
			return false;
	}
	
	public boolean getBoolean(ImageAnalysisBlock block, String setting, boolean defaultValue) {
		return optionsAndResults.getBooleanSetting(block, setting, defaultValue);
	}
	
	public int getInt(String setting, int defaultValue) {
		return optionsAndResults.getIntSetting(this, setting, defaultValue);
	}
	
	public void setInt(String setting, int value) {
		optionsAndResults.setIntSetting(this, setting, value);
	}
	
	public double getDouble(String setting, double defaultValue) {
		return optionsAndResults.getDoubleSetting(this, setting, defaultValue);
	}
	
	public String getString(String setting, String defaultValue) {
		return optionsAndResults.getStringSetting(this, setting, defaultValue);
	}
	
	public Color getColor(String setting, Color defaultValue) {
		return optionsAndResults.getColorSetting(this, setting, defaultValue);
	}
	
	public void setColor(String setting, Color value) {
		optionsAndResults.setColorSetting(this, setting, value);
	}
	
	public Integer[] getIntArray(String setting, Integer[] defaultValue) {
		return optionsAndResults.getIntArraySetting(this, setting, defaultValue);
	}
	
	@Override
	public void setInputAndOptions(String well, MaskAndImageSet input, ImageProcessorOptionsAndResults options,
			BlockResultSet properties,
			int blockPositionInPipeline,
			ImageStack debugStack) {
		this.input = input;
		this.well = well;
		this.optionsAndResults = options;
		this.resultSet = properties;
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
			for (CameraType ct : CameraType.values())
				if (input().images().getImage(ct) != null || input().masks().getImage(ct) != null)
					if ((getCameraInputTypes() != null && getCameraInputTypes().contains(ct)) ||
							(getCameraOutputTypes() != null && getCameraOutputTypes().contains(ct)))
						debugPipelineBlock(this.getClass(), ct, input(), getResultSet(), optionsAndResults, getBlockPosition(), this);
		}
	}
	
	@Override
	public final MaskAndImageSet process() throws InterruptedException {
		StopWatch w = debugStart(getName()); // this.getClass().getSimpleName()
		MaskAndImageSet res = run();
		debugEnd(w, getName(), res);
		return res;
	}
	
	protected abstract MaskAndImageSet run() throws InterruptedException;
	
	protected StopWatch debugStart(String task) {
		if (SystemOptions.getInstance().getBoolean("IAP", "Debug-Stop-Block-Exection-Times", false)) {
			return new StopWatch("Processing '" + task + "'");
		} else
			return null;
	}
	
	protected boolean isChangingImages() {
		return true;
	}
	
	protected void debugEnd(StopWatch w, String task, MaskAndImageSet res) {
		if (w != null) {
			w.printTime(10);
		}
		
		if (res != null)
			if (optionsAndResults.forceDebugStack && debugStack != null && isChangingImages())
				if (getBoolean("enabled", true))
					debugStack.addImage("Result of " + task,
							res.getOverviewImage(SystemOptions.getInstance().getInteger("IAP", "Debug-Overview-Image-Width", 1680),
									debugStack), null);
	}
	
	public MaskAndImageSet input() {
		return input;
	}
	
	protected BlockResultSet getResultSet() {
		return resultSet;
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
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			CalculatesProperties propertyCalculator) throws InterruptedException {
		
		PostProcessor pp = getPostProcessor(time2allResultsForSnapshot);
		if (pp != null)
			for (Long time : new ArrayList<Long>(time2allResultsForSnapshot.keySet())) {
				if (!time2summaryResult.containsKey(time))
					time2summaryResult.put(time, new TreeMap<String, HashMap<String, BlockResultSet>>());
				TreeMap<String, HashMap<String, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
				for (String configName : allResultsForSnapshot.keySet()) {
					if (!time2summaryResult.get(time).containsKey(configName))
						time2summaryResult.get(time).put(configName, new HashMap<String, BlockResultSet>());
					TreeMap<String, HashMap<String, BlockResultSet>> nn = time2summaryResult.get(time);
					HashMap<String, BlockResultSet> trayWithResult = nn.get(configName);
					for (String tray : trayWithResult.keySet()) {
						if (!time2summaryResult.get(time).containsKey(tray))
							time2summaryResult.get(time).get(configName).put(tray, new BlockResults(null));
						BlockResultSet summaryResult = time2summaryResult.get(time).get(configName).get(tray);
						if (pp != null) {
							ArrayList<BlockResultValue> rl = pp.postProcessCalculatedProperties(time, tray);
							if (rl != null)
								for (BlockResultValue bpv : rl) {
									summaryResult.setNumericResult(getBlockPosition(),
											new Trait(bpv.getName()), bpv.getValue(), bpv.getUnit(), propertyCalculator, bpv.getBinary());
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
	protected PostProcessor getPostProcessor(TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2allResultsForSnapshot) {
		return null;
	}
	
	protected void reportError(Error error, String errorMessage) {
		System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: ERROR IN BLOCK " + getClass().getSimpleName() + ":" + errorMessage);
		if (error != null)
			error.printStackTrace();
		if (SystemOptions.getInstance().getBoolean("IAP", "Debug - System.Exit in case of pipeline error",
				IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE))
			System.exit(SystemOptions.getInstance().getInteger(
					"IAP", "Debug: System.Exit return value in case of pipeline error", 1));
	}
	
	protected void reportError(Exception error, String errorMessage) {
		System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: EXCEPTION IN BLOCK " + getClass().getSimpleName() + ":" + errorMessage);
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
	
	protected void calculateRelativeValues(
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2config2summaryResult, int blockPosition,
			String[] desiredProperties, CalculatesProperties propertyCalculator) {
		
		boolean relRaw = getBoolean(null, "Calculate Relative Values (Raw)", true);
		boolean relPer = getBoolean(null, "Calculate Relative Values (Percent)", true);
		boolean relLog = getBoolean(null, "Calculate Relative Values (Log)", true);
		
		if (!relRaw && !relPer && !relLog)
			return;
		
		final double timeForOneDayD = 1000 * 60 * 60 * 24d;
		
		HashMap<String, TreeMap<String, TreeMap<String, Long>>> prop2config2tray2lastTime = new HashMap<String, TreeMap<String, TreeMap<String, Long>>>();
		HashMap<String, TreeMap<String, TreeMap<String, Double>>> prop2config2tray2lastValue = new HashMap<String, TreeMap<String, TreeMap<String, Double>>>();
		
		for (Long time : time2allResultsForSnapshot.keySet()) {
			TreeMap<String, HashMap<String, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (allResultsForSnapshot == null || allResultsForSnapshot.keySet() == null)
				continue;
			
			boolean showWarning = SystemOptions.getInstance().getBoolean("Pipeline-Debugging", "DEBUG-WARN-MISSING-RESULTS", false);
			for (String configName : allResultsForSnapshot.keySet()) {
				if (!time2config2summaryResult.containsKey(time))
					time2config2summaryResult.put(time, new TreeMap<String, HashMap<String, BlockResultSet>>());
				if (!time2config2summaryResult.get(time).containsKey(configName))
					time2config2summaryResult.get(time).put(configName, new HashMap<String, BlockResultSet>());
				HashMap<String, BlockResultSet> summaryResultArray = time2config2summaryResult.get(time).get(configName);
				for (String tray : allResultsForSnapshot.get(configName).keySet()) {
					Double angle = getAngleFromConfig(configName);
					if (!summaryResultArray.containsKey(tray))
						summaryResultArray.put(tray, new BlockResults(angle));
					BlockResultSet summaryResult = summaryResultArray.get(tray);
					BlockResultSet rt = allResultsForSnapshot.get(configName).get(tray);
					for (String property : desiredProperties) {
						ArrayList<BlockResultValue> calculationResults = rt.searchResults(true, property, false);
						if (calculationResults.isEmpty() && showWarning)
							System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Result named '" + property + "' not found. Config=" + configName
									+ ", Tray=" + tray + ", Time=" + time);
						double valueSum = 0;
						NumericMeasurement3D imageRef = null;
						int valueN = 0;
						for (BlockResultValue v : calculationResults) {
							if (v.getValue() != null) {
								double value = v.getValue().doubleValue();
								imageRef = v.getBinary();
								valueSum += value;
								valueN++;
							}
						}
						if (valueN > 0) {
							initMaps(prop2config2tray2lastTime, prop2config2tray2lastValue, configName, property);
							
							if (prop2config2tray2lastValue.containsKey(property) && prop2config2tray2lastValue.get(property).containsKey(configName)
									&& prop2config2tray2lastValue.get(property).get(configName).containsKey(tray)) {
								Double lastPropertyValue = prop2config2tray2lastValue.get(property).get(configName).get(tray);
								if (lastPropertyValue != null && lastPropertyValue > 0 &&
										time - prop2config2tray2lastTime.get(property).get(configName).get(tray) > 0) {
									double currentPropertyValue = valueSum / valueN;
									double ratio = currentPropertyValue / lastPropertyValue;
									double days = (time - prop2config2tray2lastTime.get(property).get(configName).get(tray)) / timeForOneDayD;
									double ratioPerDay = Math.pow(ratio, 1d / days);
									if (relRaw)
										summaryResult.setNumericResult(blockPosition, new Trait(property + ".relative.raw"), ratioPerDay, "relative/day",
												propertyCalculator, imageRef);
									double perc = (ratioPerDay - 1) * 100d;
									if (relPer)
										summaryResult.setNumericResult(blockPosition, new Trait(property + ".relative.percent"), perc, "percent change/day",
												propertyCalculator, imageRef);
									double growth = (Math.log(currentPropertyValue) - Math.log(lastPropertyValue)) / days;
									if (relLog)
										summaryResult.setNumericResult(blockPosition, new Trait(property + ".relative.log"), growth, "relative/day", propertyCalculator,
												imageRef);
								}
							}
							
							prop2config2tray2lastTime.get(property).get(configName).put(tray, time);
							prop2config2tray2lastValue.get(property).get(configName).put(tray, valueSum / valueN);
						}
					}
				}
			}
		}
	}
	
	private Double getAngleFromConfig(String configName) {
		boolean isTop = configName.toUpperCase().contains("TOP");
		configName = configName.substring(configName.indexOf(";") + ";".length());
		Double angle = Double.parseDouble(configName);
		if (isTop && Math.abs(angle) < 0.001)
			angle = -1d;
		return isTop ? -angle : angle;
	}
	
	private void initMaps(HashMap<String, TreeMap<String, TreeMap<String, Long>>> prop2config2tray2lastTime,
			HashMap<String, TreeMap<String, TreeMap<String, Double>>> prop2config2tray2lastValue,
			String config, String property) {
		if (!prop2config2tray2lastValue.containsKey(property))
			prop2config2tray2lastValue.put(property, new TreeMap<String, TreeMap<String, Double>>());
		if (!prop2config2tray2lastTime.containsKey(property))
			prop2config2tray2lastTime.put(property, new TreeMap<String, TreeMap<String, Long>>());
		if (!prop2config2tray2lastValue.get(property).containsKey(config))
			prop2config2tray2lastValue.get(property).put(config, new TreeMap<String, Double>());
		if (!prop2config2tray2lastTime.get(property).containsKey(config))
			prop2config2tray2lastTime.get(property).put(config, new TreeMap<String, Long>());
	}
	
	protected void debugPipelineBlock(final Class<?> blockType, final CameraType inpImageType,
			final MaskAndImageSet in,
			final BlockResultSet brs, final ImageProcessorOptionsAndResults options,
			final int blockPos, final AbstractImageAnalysisBlockFIS inst) {
		
		final MaskAndImageSet inputSet = in.copy();
		final TreeMap<Integer, TreeMap<String, ImageAndImageData>> storedImages = copy(brs.getImages());
		
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
					else {
						for (RunnableOnImageSet roi : brs.getStoredPostProcessors(inpImageType)) {
							processingResultImage = roi.postProcessMask(processingResultImage);
						}
						brs.clearStoredPostprocessors();
					}
					
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
		
		Action action2 = new AbstractAction("<html>Close Additional<br>"
				+ "Image Windows (" + IAPservice.getIAPimageWindowCount() + ")") {
			@Override
			public void actionPerformed(ActionEvent e) {
				IAPservice.closeAllImageJimageWindows();
			}
			
			@Override
			public boolean isEnabled() {
				return IAPservice.getIAPimageWindowCount() > 0;
			}
			
		};
		final JButton closeWindows = new JButton(action2);
		closeWindows.setEnabled(IAPservice.getIAPimageWindowCount() > 0);
		closeWindows.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				closeWindows.setText("<html>Close Additional<br>"
						+ "Image Windows (" + IAPservice.getIAPimageWindowCount() + ")");
				closeWindows.setEnabled(IAPservice.getIAPimageWindowCount() > 0);
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				closeWindows.setText("<html>Close Additional<br>"
						+ "Image Windows (" + IAPservice.getIAPimageWindowCount() + ")");
				closeWindows.setEnabled(IAPservice.getIAPimageWindowCount() > 0);
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
		
		JComponent editAndUpdate = TableLayout.get3Split(
				null,
				null,
				TableLayout.get3Split(
						ic.getZoomSlider(-1), null,
						TableLayout.get3Split(okButton, null, closeWindows, TableLayout.PREFERRED, 5, TableLayout.PREFERRED),
						TableLayout.PREFERRED, 5, TableLayout.PREFERRED),
				TableLayout.FILL, 5, TableLayout.PREFERRED);
		JComponent v = TableLayout.get3SplitVertical(
				jsp, null, editAndUpdate, TableLayout.FILL, 5, TableLayout.PREFERRED);
		v.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final Timer timer = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean changesDetected = false;
				if (changesDetected) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">Detected change of settings, updating view...");
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
	
	private TreeMap<Integer, TreeMap<String, ImageAndImageData>> copy(TreeMap<Integer, TreeMap<String, ImageAndImageData>> images) {
		TreeMap<Integer, TreeMap<String, ImageAndImageData>> res = new TreeMap<Integer, TreeMap<String, ImageAndImageData>>();
		for (Integer key : images.keySet()) {
			res.put(key, new TreeMap<String, ImageAndImageData>());
			for (String id : images.get(key).keySet()) {
				res.get(key).put(id, new ImageAndImageData(
						images.get(key).get(id).getImage(),
						images.get(key).get(id).getImageData()
						));
			}
		}
		return res;
	}
	
	@Override
	public int compareTo(ImageAnalysisBlock o) {
		// int res = getBlockType().compareTo(o.getBlockType());
		// if (res != 0)
		// return res;
		// else {
		return (getName() + " ").compareToIgnoreCase(o.getName() + "");
		// }
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
	
	protected CameraPosition getCameraPosition() {
		return optionsAndResults.getCameraPosition();
	}
	
	protected CameraPosition cp() {
		return getCameraPosition();
	}
	
	protected boolean isBestAngle(CameraType ct) {
		HashMap<String, ArrayList<BlockResultValue>> previousResults = optionsAndResults
				.searchResultsOfCurrentSnapshot(new Trait(CameraPosition.TOP, ct, TraitCategory.GEOMETRY, "main_axis.rotation").toString(), true, getWellIdx(),
						null, false, null);
		
		double sum = 0;
		int count = 0;
		
		for (ArrayList<BlockResultValue> b : previousResults.values()) {
			for (BlockResultValue c : b) {
				count++;
				sum += c.getValue();
			}
		}
		
		if (count == 0) {
			return true;
		}
		
		ImageData currentImage = input().images().getAnyInfo();
		
		double mainRotationFromTopView = sum / count;
		double mindist = Double.MAX_VALUE;
		boolean currentImageIsBest = false;
		
		for (NumericMeasurementInterface nmi : currentImage.getParentSample()) {
			if (nmi instanceof ImageData) {
				Double r = ((ImageData) nmi).getPosition();
				if (r == null)
					r = 0d;
				double dist = Math.abs(mainRotationFromTopView - r);
				if (dist < mindist) {
					mindist = dist;
					if ((((ImageData) nmi).getPosition() + "").equals((currentImage.getPosition() + "")))
						currentImageIsBest = true;
					else
						currentImageIsBest = false;
				}
			}
		}
		
		if (!currentImageIsBest)
			return false;
		
		return true;
	}
}

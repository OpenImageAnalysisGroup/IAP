package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.IntStream;

import org.ErrorMsg;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.settings.ActionToggle;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class ActionDetermineImageFileOutputSize extends AbstractNavigationAction implements NavigationAction {
	
	private ExperimentReference experiment;
	private ThreadSafeOptions exportImages;
	private ThreadSafeOptions tsoQuality;
	private final ArrayList<ThreadSafeOptions> togglesQuality = new ArrayList<>();
	private final ThreadSafeOptions tsoSampleProgres = new ThreadSafeOptions();
	private final ThreadSafeOptions lastTitleUpdate = new ThreadSafeOptions();
	private final SummaryStatistics summaryStat = new SummaryStatistics();
	private final HashMap<Integer, SummaryStatistics> q2stat = new HashMap<>();
	private final ThreadSafeOptions tsoQualityEstimationStop = new ThreadSafeOptions();
	
	public ActionDetermineImageFileOutputSize(String tooltip) {
		super(tooltip);
	}
	
	public ActionDetermineImageFileOutputSize(ExperimentReference experiment, ThreadSafeOptions exportImages, ThreadSafeOptions tsoQuality) {
		this("Estimate JPG/PNG output size of image files");
		this.experiment = experiment;
		this.exportImages = exportImages;
		this.tsoQuality = tsoQuality;
		tsoSampleProgres.setInt(-1);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		LinkedList<NumericMeasurementInterface> list = Substance3D.getAllFiles(experiment.getData(), MeasurementNodeType.IMAGE);
		ArrayList<ImageData> imagesFullList = new ArrayList<ImageData>(list.size());
		lastTitleUpdate.setLong(System.currentTimeMillis());
		summaryStat.clear();
		synchronized (q2stat) {
			q2stat.clear();
		}
		for (NumericMeasurementInterface nmi : list)
			if (nmi instanceof ImageData)
				imagesFullList.add((ImageData) nmi);
		Runnable r = () -> {
			int nToDo = 0;
			do {
				ArrayList<ImageData> images = new ArrayList<ImageData>(imagesFullList);
				tsoQualityEstimationStop.setBval(0, false);
				nToDo = images.size();
				tsoSampleProgres.setDouble(nToDo);
				int finished = 0;
				synchronized (summaryStat) {
					summaryStat.clear();
				}
				synchronized (q2stat) {
					q2stat.clear();
				}
				do {
					int n = images.size();
					int random = (int) (Math.random() * n);
					if (random >= n)
						n = 0;
					ImageData i = images.remove(random);
					if (i != null) {
						try {
							Image img = new Image(i.getURL());
							int ss = tsoQuality.getInt();
							CameraType ct = CameraType.fromString(i.getSubstanceName());
							if (ss > 0 && ss < 100 && ct != CameraType.NIR && ct != CameraType.IR)
								img = img.resize(img.getWidth() * ss / 100, img.getHeight() * ss / 100);
							Image fimg = img;
							img = null;
							if (!exportImages.getBval(0, false))
								synchronized (summaryStat) {
									summaryStat.addValue(fimg.getAsPNGstream().getCount());
								}
							else {
								synchronized (summaryStat) {
									summaryStat.addValue(fimg.getAsJPGstream().getCount());
								}
								IntStream range = IntStream.of(20, 40, 60, 80, 90, 95, 100);
								BackgroundThreadDispatcher.stream("Size Analysis (JPG Compression)").processInts(range,
										(qq) -> {
											SummaryStatistics qstat = null;
											synchronized (q2stat) {
												if (!q2stat.containsKey(qq))
													q2stat.put(qq, new SummaryStatistics());
												qstat = q2stat.get(qq);
											}
											synchronized (qstat) {
												try {
													qstat.addValue(fimg.getAsJPGstream(qq / 100f).getCount());
												} catch (Exception e) {
													ErrorMsg.addErrorMessage(e);
												}
											}
										}, null);
							}
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
					nToDo--;
					finished++;
					tsoSampleProgres.setInt(nToDo);
					tsoSampleProgres.setLong(finished);
				} while (nToDo > 0 && System.currentTimeMillis() - lastTitleUpdate.getLong() < 20000 && !tsoQualityEstimationStop.getBval(0, false));
			} while (tsoQualityEstimationStop.getBval(0, false));
			tsoSampleProgres.setBval(10, true);
			if (nToDo == 0)
				System.out.println("Finished sampling (all images sampled)");
				else
					System.out.println("Finished sampling (time since last title request=" +
							(System.currentTimeMillis() - lastTitleUpdate.getLong() + " is greater than 20 sec.)"));
			};
		Thread sampleThread = new Thread(r);
		sampleThread.setPriority(Thread.MIN_PRIORITY);
		sampleThread.setName("Sample Image Sizes");
		sampleThread.start();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		togglesQuality.clear();
		ArrayList<NavigationButton> res = new ArrayList<>();
		{
			final ThreadSafeOptions option = new ThreadSafeOptions().setBval(0, true);
			togglesQuality.add(option);
			res.add(new NavigationButton(
					new ActionToggle("Use default or automatic JPG compression settings or normal PNG export", "Default Quality", togglesQuality.get(togglesQuality
							.size() - 1)) {
						@Override
						public String getDefaultTitle() {
							SummaryStatistics stat = summaryStat;
							if (stat == null || stat.getN() <= 0) {
								if (option.getBval(0, false))
									tsoQuality.setParam(0, null);
								return super.getDefaultTitle() + "<br><font color='gray'>- GB";
							} else {
								if (option.getBval(0, false))
									tsoQuality.setParam(0, getEstimate(stat, tsoSampleProgres.getDouble()));
								return super.getDefaultTitle() + "<br><font color='gray'>" + getEstimate(stat, tsoSampleProgres.getDouble()) + "";
							}
						}
						
						@Override
						public NavigationImage getImageIconInactive() {
							if (option.getBval(0, true))
								return IAPmain.loadIcon("img/ext/gpl2/Gnome-Insert-Object-64_save.png");// gtce.png");
							else
								return IAPmain.loadIcon("img/ext/gpl2/Gnome-Insert-Object-64_gray.png");// gtcd.png");
						}
						
						@Override
						public NavigationImage getImageIconActive() {
							return getImageIconInactive();
						}
						
						@Override
						public void performActionCalculateResults(NavigationButton src) throws Exception {
							super.performActionCalculateResults(src);
							tsoQuality.setDouble(0d);
							for (ThreadSafeOptions tso : togglesQuality)
								tso.setBval(0, tso == option);
						}
						
						@Override
						public String getDefaultImage() {
							if (option.getParam(1, null) != null)
								return "img/ext/gpl2/Gnome-System-Run-64.png";
							if (option.getBval(0, true))
								return "img/ext/gpl2/Gnome-Insert-Object-64_save.png";// gtce.png";
							else
								return "img/ext/gpl2/Gnome-Insert-Object-64_gray.png";// gtcd.png";
						}
					},
					guiSetting));
		}
		if (exportImages.getBval(0, false))
			for (int i : IntStream.of(20, 40, 60, 80, 90, 95, 100).toArray()) {
				togglesQuality.add(new ThreadSafeOptions().setBval(0, false));
				final ThreadSafeOptions option = togglesQuality.get(togglesQuality.size() - 1);
				final int qq = i;
				res.add(new NavigationButton(
						new ActionToggle("Use JPG compression level " + i, "Quality " + i, option) {
							@Override
							public String getDefaultTitle() {
								SummaryStatistics stat = null;
								synchronized (q2stat) {
									stat = q2stat.get(qq);
								}
								if (stat == null || stat.getN() <= 0) {
									if (option.getBval(0, false))
										tsoQuality.setParam(0, null);
									return super.getDefaultTitle() + "<br><font color='gray'>- GB";
								} else {
									if (option.getBval(0, false))
										tsoQuality.setParam(0, getEstimate(stat, tsoSampleProgres.getDouble()));
									return super.getDefaultTitle() + "<br><font color='gray'>" + getEstimate(stat, tsoSampleProgres.getDouble()) + "";
								}
							}
							
							@Override
							public void performActionCalculateResults(NavigationButton src) throws Exception {
								super.performActionCalculateResults(src);
								tsoQuality.setDouble(qq / 100d);
								for (ThreadSafeOptions tso : togglesQuality)
									tso.setBval(0, tso == option);
							}
							
							@Override
							public NavigationImage getImageIconInactive() {
								if (option.getBval(0, true))
									return IAPmain.loadIcon("img/ext/gpl2/Gnome-Insert-Object-64_save.png");// gtce.png");
								else
									return IAPmain.loadIcon("img/ext/gpl2/Gnome-Insert-Object-64_gray.png");// gtcd.png");
							}
							
							@Override
							public NavigationImage getImageIconActive() {
								return getImageIconInactive();
							}
							
							@Override
							public String getDefaultImage() {
								if (option.getParam(1, null) != null)
									return "img/ext/gpl2/Gnome-System-Run-64.png";
								if (option.getBval(0, true))
									return "img/ext/gpl2/Gnome-Insert-Object-64_save.png";// gtce.png";
								else
									return "img/ext/gpl2/Gnome-Insert-Object-64_gray.png";// gtcd.png";
							}
						},
						guiSetting));
			}
		
		res.add(new NavigationButton(
				new ActionToggle("Use full source image size", "Full Size", new ThreadSafeOptions()) {
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
						super.performActionCalculateResults(src);
						tsoQuality.setInt(100);
						tsoQualityEstimationStop.setBval(0, true);// request restart of sampling
					}
					
					@Override
					public NavigationImage getImageIconInactive() {
						if (tsoQuality.getInt() == 100)
							return IAPmain.loadIcon("img/ext/gpl2/Dialog-Apply-64.png");
						else
							return IAPmain.loadIcon("img/ext/gpl2/Dialog-Apply-64_gray.png");
					}
					
					@Override
					public NavigationImage getImageIconActive() {
						return getImageIconInactive();
					}
					
					@Override
					public String getDefaultImage() {
						if (tsoQuality.getInt() == 100)
							return "img/ext/gpl2/Dialog-Apply-64.png";
						else
							return "img/ext/gpl2/Dialog-Apply-64_gray.png";
					}
				},
				guiSetting));
		
		res.add(new NavigationButton(
				new ActionToggle("Scale width and height of source images (if not NIR or IR images) by 50%", "Half Size", new ThreadSafeOptions()) {
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
						super.performActionCalculateResults(src);
						tsoQuality.setInt(50);
						tsoQualityEstimationStop.setBval(0, true);// request restart of sampling
					}
					
					@Override
					public NavigationImage getImageIconInactive() {
						if (tsoQuality.getInt() == 50)
							return IAPmain.loadIcon("img/ext/gpl2/Dialog-Apply-64.png");
						else
							return IAPmain.loadIcon("img/ext/gpl2/Dialog-Apply-64_gray.png");
					}
					
					@Override
					public NavigationImage getImageIconActive() {
						return getImageIconInactive();
					}
					
					@Override
					public String getDefaultImage() {
						if (tsoQuality.getInt() == 50)
							return "img/ext/gpl2/Dialog-Apply-64.png";
						else
							return "img/ext/gpl2/Dialog-Apply-64_gray.png";
					}
				},
				guiSetting));
		
		res.add(new NavigationButton(
				new ActionToggle("Scale width and height of source images (if not NIR or IR images) by 25%", "Quarter Size", new ThreadSafeOptions()) {
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
						super.performActionCalculateResults(src);
						tsoQuality.setInt(25);
						tsoQualityEstimationStop.setBval(0, true);// request restart of sampling
					}
					
					@Override
					public NavigationImage getImageIconInactive() {
						if (tsoQuality.getInt() == 25)
							return IAPmain.loadIcon("img/ext/gpl2/Dialog-Apply-64.png");
						else
							return IAPmain.loadIcon("img/ext/gpl2/Dialog-Apply-64_gray.png");
					}
					
					@Override
					public NavigationImage getImageIconActive() {
						return getImageIconInactive();
					}
					
					@Override
					public String getDefaultImage() {
						if (tsoQuality.getInt() == 25)
							return "img/ext/gpl2/Dialog-Apply-64.png";
						else
							return "img/ext/gpl2/Dialog-Apply-64_gray.png";
					}
				},
				guiSetting));
		
		return res;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	private String getEstimate(SummaryStatistics stat, double nImages) {
		if (stat.getN() <= 0)
			return "- GB";
		synchronized (stat) {
			long dataAmount = (long) (stat.getSum() / stat.getN() * nImages);
			return (tsoSampleProgres.getInt() == 0 ? "exact " : "~ ") + org.SystemAnalysis.getDataAmountString(dataAmount);
		}
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("A random subset of the images is currently loaded. The more images are sampled during this "
				+ "background-process, the more accurate the final output size estimation will be. Once the values don't fluctuate any more, "
				+ "there is no need to wait longer for the final exact determination of the output size. After selecting the "
				+ "desired JPG compression (or confirming the non-modifiable PNG output size), you may safely return one step "
				+ "in the command history stack at the top, to start the ZIP file generation, or to create the spreadsheet file, "
				+ "with corresponding image output (if enabled). The sample process may continue for a few seconds in the background "
				+ "once you leave this screen. It will automatically be stopped after a few seconds.<br><br>"
				+ "To further reduce the output file sizes, you may reduce the image size by half or by a quarter (vertically and horizontally). "
				+ "This option does not reduce the size of images, categorized as near-infrared or infrared images. These images are "
				+ "normally low-resolution images.<br><br>"
				+ "Important: Currently the size reduction works only for the ZIP file export, not for the combined "
				+ "spreadsheet and image file export.");
	}
	
	@Override
	public String getDefaultTitle() {
		lastTitleUpdate.setLong(System.currentTimeMillis());
		String sp = null;
		if (tsoSampleProgres.getInt() < 0)
			sp = "";
		else
			if (tsoSampleProgres.getInt() == 0)
				sp = "<br>{sampling finished}";
		if (tsoSampleProgres.getInt() > 0)
			sp = "<br>{" + tsoSampleProgres.getLong() + " sampled (" + (int) (tsoSampleProgres.getLong() * 100d / tsoSampleProgres.getDouble()) + "%)}";
		if (!exportImages.getBval(0, false)) {
			String ss = tsoQuality.getInt() + "% size";
			String sizeEst = (String) tsoQuality.getParam(0, null);
			return (sizeEst == null ? "Determine PNG Output Size" : "PNG Output Size " + sizeEst) + "<br><small><font color='gray'>(PNG output, " + ss
					+ ")" + sp
					+ (tsoSampleProgres.getBval(10, false) ? " - stopped" : "");
		}
		String q = "Default Quality Selected";
		if (tsoQuality.getDouble() >= 0.01)
			q = "Quality " + ((int) ((tsoQuality.getDouble()) * 100d));
		{
			String ss = tsoQuality.getInt() + "% size";
			String sizeEst = (String) tsoQuality.getParam(0, null);
			return (sizeEst == null ? "Specify Image Storage Size" : "JPG Output Size " + sizeEst) + "<br><small><font color='gray'>(" + q + ", " + ss
					+ ")"
					+ sp + (tsoSampleProgres.getBval(10, false) ? " - stopped" : "");
		}
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Media-Optical-64.png";// Gnome-Media-Flash-64.png";
	}
	
}

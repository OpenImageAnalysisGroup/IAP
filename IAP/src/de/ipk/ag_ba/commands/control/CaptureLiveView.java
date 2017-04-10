package de.ipk.ag_ba.commands.control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.ErrorMsg;
import org.SystemOptions;
import org.apache.commons.io.IOUtils;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;

import de.ipk.ag_ba.image.structures.Image;

public class CaptureLiveView extends JComponent {
	private final Timer timer;
	private TreeMap<Integer, BufferedImage> images = new TreeMap<>();
	// protected TreeMap<Integer, byte[]> imageData = new TreeMap<>();
	private TreeMap<Integer, String> imageSubstances = new TreeMap<>();
	private TreeMap<Integer, String> imageBarcode = new TreeMap<>();
	private TreeMap<Integer, Double> imageRotation = new TreeMap<>();
	private TreeMap<Integer, ResultPoint[]> currentPoints = new TreeMap<>();
	private String bcSection;
	private String bcSettingAuto;
	protected boolean repaintPending;
	private ThreadSafeOptions tsoMaxTasks = new ThreadSafeOptions();
	
	private static int instanceCnt = 0;
	
	public CaptureLiveView(String bcSection, String bcSettingAuto) {
		this.bcSection = bcSection;
		this.bcSettingAuto = bcSettingAuto;
		this.timer = new Timer("Image capture " + (++instanceCnt));
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (CaptureLiveView.this.getParent() == null) {
					CaptureLiveView.this.timer.cancel();
					return;
				}
			}
		}, 100, 100);
		// String cmd = "/Users/klukas/Applications/ImageSnap-v0.2.5/imagesnap -w 1.0 -";
		images.clear();
		imageSubstances.clear();
		captureImages(false);
	}
	
	private void captureImages(boolean capture) {
		tsoMaxTasks.addInt(1);
		try {
			int nCameras = SystemOptions.getInstance().getInteger("Capture", "Number of cameras", 0);
			boolean captured = false;
			boolean oneReady = false;
			for (int i = 0; i < nCameras; i++) {
				boolean enabled = SystemOptions.getInstance().getBoolean("Capture", "Camera " + (i + 1) + "//enabled", false);
				String substanceName = SystemOptions.getInstance().getString("Capture", "Camera " + (i + 1) + "//id", "vis.top");
				double rotationAngle = SystemOptions.getInstance().getDouble("Capture", "Camera " + (i + 1) + "//rotation angle", -1);
				String cmd = SystemOptions.getInstance().getString("Capture", "Camera " + (i + 1) + "//capture command", "");
				String fileName = SystemOptions.getInstance().getString("Capture", "Camera " + (i + 1) + "//output", "-");
				boolean useAsBarcodeReader = SystemOptions.getInstance().getBoolean("Capture", "Camera " + (i + 1) + "//use as barcode reader", true);
				String targetCamerasForBarcode = SystemOptions.getInstance().getString("Capture", "Camera " + (i + 1) + "//barcode target cameras", "C" + (i + 1));
				boolean allowsLiveView = SystemOptions.getInstance().getBoolean("Capture", "Camera " + (i + 1) + "//enable live view", false);
				
				boolean enabled2 = enabled && (cmd.length() > 0 && (allowsLiveView || capture));
				if (enabled && cmd.length() > 0)
					oneReady = true;
				
				enabled = enabled2;
				
				if (enabled) {
					HashSet<Integer> targetList = new HashSet<>();
					try {
						if (useAsBarcodeReader)
							for (String s : targetCamerasForBarcode.split(";"))
								targetList.add(Integer.parseInt(org.StringManipulationTools.getNumbersFromString(s)));
					} catch (Exception e) {
						//
					}
					TimerTask task = getTask(i, substanceName, cmd, fileName, useAsBarcodeReader, rotationAngle, allowsLiveView, targetList);
					if (!capture)
						timer.schedule(task, 250);
					else
						task.run();
					captured = true;
				} else {
					synchronized (this) {
						images.remove(i + 1);
						// imageData.remove(i + 1);
						imageSubstances.remove(i + 1);
						imageBarcode.remove(i + 1);
						imageRotation.remove(i + 1);
						currentPoints.remove(i + 1);
					}
				}
				if (tsoMaxTasks.getInt() == 0)
					if (!captured && !capture) {
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								captureImages(false);
							}
						}, 250);
					}
			}
			oneIsReadyForCapture = oneReady;
		} finally {
			tsoMaxTasks.addInt(-1);
		}
	}
	
	private TimerTask getTask(int camera, String substanceName, String cmd, String output,
			boolean useAsBarcodeReader, double rotationAngle, boolean allowsLiveView,
			Set<Integer> targetCameraForBarcodes) {
		return new TimerTask() {
			@Override
			public void run() {
				if (tsoMaxTasks.getBval(camera, false))
					return;
				try {
					tsoMaxTasks.setBval(camera, true);
					Process p;
					try {
						p = Runtime.getRuntime().exec(cmd);
					} catch (Exception e) {
						p = Runtime.getRuntime().exec("/usr/local/bin/" + cmd);
					}
					byte[] bytes;
					if (output.length() == 0 || output.equals("-")) {
						bytes = IOUtils.toByteArray(p.getInputStream());
					} else {
						File imgFile = new File(output);
						bytes = IOUtils.toByteArray(new FileInputStream(imgFile)); // CaptureLiveView.this.images.put(camera + 1, new
																										// Image(FileSystemHandler.getURL(new File(output)),
																										// false).getAsBufferedImage(false));
						imgFile.delete();
					}
					
					BufferedImage img = ImageIO.read(new MyByteArrayInputStream(bytes));
					String barcode = null;
					
					if (SystemOptions.getInstance().getBoolean(bcSection, bcSettingAuto, false) && useAsBarcodeReader) {
						try {
							LuminanceSource source = new RGBLuminanceSource(img.getWidth(),
									img.getHeight(), new Image(img).getAs1A());
							BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
							Reader reader = new MultiFormatReader();
							Result result = reader.decode(bitmap);
							barcode = result.getText();
							synchronized (this) {
								currentPoints.put(camera + 1, result.getResultPoints());
								String oldBarcode = null;
								if (imageBarcode.containsKey(camera + 1))
									oldBarcode = imageBarcode.get(camera + 1);
								if (barcode != null && !barcode.equals(oldBarcode))
									System.out.println(barcode);
							}
						} catch (Exception nfe) {
							currentPoints.remove(camera + 1);
						}
					}
					
					synchronized (this) {
						imageSubstances.put(camera + 1, substanceName);
						if (useAsBarcodeReader) {
							setCurrentBarcode(barcode == null ? "" : barcode);
							for (Integer cam : targetCameraForBarcodes) {
								imageBarcode.put(cam, barcode);
							}
						}
						if (substanceName != null && substanceName.length() > 0)
							images.put(camera + 1, img);
						imageRotation.put(camera + 1, rotationAngle);
					}
					
					p.waitFor();
				} catch (Exception e1) {
					ErrorMsg.addErrorMessage(e1);
				} finally {
					tsoMaxTasks.setBval(camera, false);
				}
				if (!CaptureLiveView.this.repaintPending) {
					CaptureLiveView.this.repaintPending = true;
					CaptureLiveView.this.repaint();
				}
			}
		};
	}
	
	private int imageIdx = 0;
	private String currentBarcode = "";
	private boolean oneIsReadyForCapture;
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.scale(0.25, 0.25);
		int widthBefore = 0;
		int topBorder = 140;
		imageIdx++;
		Font currentFont = g.getFont();
		Font newFont = currentFont.deriveFont(currentFont.getSize() * 4F);
		g.setFont(newFont);
		for (int key : images.keySet()) {
			BufferedImage image = images.get(key);
			if (image != null) {
				g2.drawString("Camera: " + imageSubstances.get(key) + "  |  Angle: " + imageRotation.get(key), widthBefore + 12, 110);
				g.draw3DRect(widthBefore + 18, topBorder + 3, image.getWidth() + 3, image.getHeight() + 3, false);
				g.draw3DRect(widthBefore + 19, topBorder + 4, image.getWidth() + 2, image.getHeight() + 2, false);
				g.drawImage(image, widthBefore + 20, topBorder + 5, null);
				synchronized (this) {
					if (currentPoints != null && currentPoints.size() > 0 && currentPoints.containsKey(key)) {
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2.setStroke(new BasicStroke(10));
						g.setColor(Color.RED);
						for (ResultPoint p : currentPoints.get(key)) {
							float centerX = widthBefore + p.getX();
							float centerY = topBorder + p.getY();
							float radius = 15;
							Shape theCircle = new Ellipse2D.Float(centerX - radius, centerY - radius, 2.0f * radius, 2.0f * radius);
							g2.draw(theCircle);
						}
						g.setColor(Color.BLACK);
						g2.drawString("Barcode detected.", widthBefore + 12, 110 + image.getHeight() + 100);
						g2.drawString("Assigned to this image: " + imageBarcode.get(key), widthBefore + 12, 110 + image.getHeight() + 160);
					} else
						g2.drawString("Barcode: " + imageBarcode.get(key), widthBefore + 12, 110 + image.getHeight() + 100);
				}
				widthBefore += image.getWidth() + 50;
			}
		}
		g.setColor(Color.GRAY);
		g2.drawString("Paint " + (imageIdx), 12, 35);
		g.setColor(Color.BLACK);
		CaptureLiveView.this.repaintPending = false;
		if (tsoMaxTasks.getInt() == 0)
			captureImages(false);
	}
	
	@SuppressWarnings("unchecked")
	public ImageInfo getImageInfo() {
		synchronized (this) {
			return new ImageInfo(
					(TreeMap<Integer, BufferedImage>) images.clone(),
					(TreeMap<Integer, String>) imageSubstances.clone(),
					(TreeMap<Integer, String>) imageBarcode.clone(),
					(TreeMap<Integer, Double>) imageRotation.clone(),
					oneIsReadyForCapture);
		}
	}
	
	public String getCurrentBarcode() {
		return currentBarcode;
	}
	
	public void setCurrentBarcode(String barcode) {
		this.currentBarcode = barcode;
	}
	
	public ImageInfo requestImageCaptureAndWaitUntilFinished() {
		synchronized (this) {
			captureImages(true);
			return getImageInfo();
		}
	}
}

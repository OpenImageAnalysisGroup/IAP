package iap.blocks.debug;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;

import de.ipk.ag_ba.image.operation.ColorSpaceConverter;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ColorSpace;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Calculates the 3-D histogram cube for the given input images. Different color-spaces for calculations are usable. Please disable before experiment analysis,
 * this block is only for debug and information purposes.
 * 
 * @author pape
 */
public class BlShowThreeDColorHistogram extends AbstractBlock {
	
	private boolean processVis;
	private boolean processFluo;
	private boolean processNir;
	private boolean processIr;
	
	@Override
	protected void prepare() {
		processVis = getBoolean("Process Vis", false);
		processFluo = getBoolean("Process Fluo", false);
		processNir = getBoolean("Process Nir", false);
		processIr = getBoolean("Process Ir", false);
	}
	
	@Override
	protected Image processMask(Image mask) {
		CameraType ct = mask.getCameraType();
		ArrayList<String> possibleValues = StringManipulationTools.getStringListFromArray(ColorSpace.values());
		String calculationMode = optionsAndResults.getStringSettingRadio(this, "Calculation Mode", ColorSpace.RGB.getNiceString(), possibleValues);
		ColorSpace colorspace = ColorSpace.valueOfNiceString(calculationMode);
		
		if ((ct == CameraType.VIS && processVis) ||
				(ct == CameraType.FLUO && processFluo) ||
				(ct == CameraType.NIR && processNir) ||
				(ct == CameraType.IR && processIr))
			calc3DHistogram(mask, null, input().masks().getAnyInfo(), colorspace, getInt("Number of Histogram Bins", 20), getDouble("Gamma", 4d));
		
		return mask;
	}
	
	public static void calc3DHistogram(Image img, Image optImg2, NumericMeasurement3D nm, ColorSpace colorspace, int numberOfBins, double gamma) {
		BackgroundTaskStatusProviderSupportingExternalCall sp = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Initialize...", null);
		BackgroundTaskHelper.issueSimpleTaskInWindow("Show 3-D Histogram Cube", "Initialize...", new Runnable() {
			
			@Override
			public void run() {
				// get cube
				Channel[] channels = colorspace.getChannels();
				Channel ch_a = channels[0];
				Channel ch_b = channels[1];
				Channel ch_c = channels[2];
				sp.setCurrentStatusText1("Calculate channel data...");
				ColorCubeEstimation cce = new ColorCubeEstimation(img, optImg2, ch_a, ch_b, ch_c, numberOfBins);
				sp.setCurrentStatusText1("Calculate histogram..");
				double[][][] cube = cce.getHistogramCube();
				double maxValue = cce.getMaxValue();
				
				int sceneWidth = 800;
				int sceneHeight = 600;
				sp.setCurrentStatusText1("Calculation Finished");
				sp.setCurrentStatusText2("Create 3-D View");
				
				Semaphore s = BackgroundTaskHelper.lockGetSemaphore(sp, 1);
				try {
					s.acquire();
					
					JFrame frame = new JFrame("3-D Histogram of " + img.getCameraType().getNiceName() + " image (" + nm.getQualityAnnotation() + " at "
							+ nm.getParentSample().getSampleTime() + ")");
					final JFXPanel jpfx = new JFXPanel();
					frame.add(jpfx);
					frame.setSize(sceneWidth, sceneHeight);
					frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					frame.addWindowListener(new WindowListener() {
						
						@Override
						public void windowOpened(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowIconified(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowDeiconified(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowDeactivated(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowClosing(WindowEvent e) {
							Platform.runLater(() -> {
								jpfx.getScene().getWindow().hide();
								SwingUtilities.invokeLater(() -> {
									frame.setVisible(false);
								});
							});
						}
						
						@Override
						public void windowClosed(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void windowActivated(WindowEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
					frame.setLocationByPlatform(true);
					jpfx.setPreferredSize(new Dimension(sceneWidth, sceneHeight));
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							try {
								initFX(jpfx, cube, ch_a, ch_b, ch_c, numberOfBins, gamma, maxValue, colorspace, sp);
								jpfx.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, java.awt.Color.LIGHT_GRAY, java.awt.Color.DARK_GRAY));
								frame.setVisible(true);
							} finally {
								s.release();
							}
						}
					});
					s.acquire();
					sp.setCurrentStatusText1("3-D View Created");
					sp.setCurrentStatusText2("");
				} catch (InterruptedException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}, null, sp);
	}
	
	private static void initFX(JFXPanel jp, double[][][] cube, Channel ch_a, Channel ch_b, Channel ch_c, int numberOfBins, double gamma, double maxVal,
			ColorSpace colorspace, BackgroundTaskStatusProviderSupportingExternalCall sp) {
		int camZdistInit = -6000;
		Scene s = createScene(jp, cube, camZdistInit, ch_a, ch_b, ch_c, numberOfBins, colorspace, maxVal, gamma, sp);
		jp.setScene(s);
	}
	
	private static Scene createScene(JComponent jp, double[][][] cube, int camZdist, Channel ch_a, Channel ch_b, Channel ch_c, int numberOfBins,
			ColorSpace colorspace, double maxVal, double gamma, BackgroundTaskStatusProviderSupportingExternalCall sp) {
		// Container
		Group root = new Group();
		final Group cubeGroup = new Group();
		root.setRotationAxis(Rotate.Y_AXIS);
		
		// Creating 3DShape
		int radius = 100;
		sp.setCurrentStatusText2("Create Histogram Elements...");
		ArrayList<Shape3D> shl = make3DHistogram(0, 0, radius, cube, numberOfBins, maxVal, gamma, colorspace);
		sp.setCurrentStatusText2("Create Legend...");
		
		// Adding nodes inside Container
		for (Shape3D sh : shl)
			cubeGroup.getChildren().addAll(sh);
		
		// add Legend for all corners
		int n = numberOfBins;
		addLegend(cubeGroup, 7, radius, n + 1, -2, -2, ch_a.name().split("_")[1], numberOfBins);
		addLegend(cubeGroup, 7, radius, -2, n + 1, -2, ch_b.name().split("_")[1], numberOfBins);
		addLegend(cubeGroup, 7, radius, -2, -2, n + 1, ch_c.name().split("_")[1], numberOfBins);
		addLegend(cubeGroup, 7, radius, n + 1, n + 1, -2, ch_a.name().split("_")[1] + " " + ch_b.name().split("_")[1], numberOfBins);
		addLegend(cubeGroup, 7, radius, n + 1, -2, n + 1, ch_a.name().split("_")[1] + " " + ch_c.name().split("_")[1], numberOfBins);
		addLegend(cubeGroup, 7, radius, -2, n + 1, n + 1, ch_b.name().split("_")[1] + " " + ch_c.name().split("_")[1], numberOfBins);
		addLegend(cubeGroup, 7, radius, -2, -2, -2, "0", numberOfBins);
		addLegend(cubeGroup, 7, radius, n + 1, n + 1, n + 1, "1", numberOfBins);
		sp.setCurrentStatusText2("Create Lights...");
		
		// Creating Ambient Light
		AmbientLight ambient = new AmbientLight();
		ambient.setColor(Color.rgb(200, 200, 200, 1.0));
		root.getChildren().add(ambient);
		{
			// Creating Point Light
			PointLight point = new PointLight();
			point.setColor(Color.WHITE);
			point.setTranslateZ(-2500);
			point.getScope().add(cubeGroup);
			root.getChildren().add(point);
		}
		
		root.getChildren().addAll(cubeGroup);
		sp.setCurrentStatusText2("Create Scene...");
		
		// Adding to scene
		Scene scene = new Scene(root, 2600, 2600, true, SceneAntialiasing.DISABLED);
		ArrayList<Stop> stops = new ArrayList<Stop>();
		stops.add(new Stop(0, Color.DARKBLUE));
		stops.add(new Stop(500, Color.DARKGRAY));
		scene.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops));
		
		// Creating Perspective View Camera
		PerspectiveCamera cam = new PerspectiveCamera(true);
		cam.setFarClip(Integer.MAX_VALUE);
		Rotate rz = new Rotate(45.0, Rotate.X_AXIS);
		Translate tz = new Translate(0.0, 0.0, camZdist);
		cam.getTransforms().add(rz);
		cam.getTransforms().add(tz);
		scene.setCamera(cam);
		
		sp.setCurrentStatusText2("Setup Animation...");
		FxCameraAnnimation camAni = new FxCameraAnnimation(cam);
		camAni.startAnimation();
		
		sp.setCurrentStatusText2("Setup Interaction...");
		new FxCameraInteraction(scene, cam, camAni, camZdist, numberOfBins);
		
		sp.setCurrentStatusText2("Return Completed Scene...");
		return scene;
	}
	
	private static void addLegend(Group cubeGroup, int size, int radius, int x, int y, int z, String t, int numberOfBins) {
		Text text = new Text(0, 0, t);
		text.setScaleX(size);
		text.setScaleY(size);
		text.setScaleZ(size);
		text.setFill(Color.rgb(0, 0, 0, .99));
		getCoordinate(text, x, y, z, radius, numberOfBins);
		cubeGroup.getChildren().add(text);
	}
	
	public static void getCoordinate(Shape s, int binX, int binY, int binZ, int radius, int numberOfBins) {
		s.setTranslateX((binX - (numberOfBins + 1) / 2d) * radius);
		s.setTranslateY((binY - (numberOfBins + 1) / 2d) * radius);
		s.setTranslateZ((binZ - (numberOfBins + 1) / 2d) * radius);
	}
	
	public static ArrayList<Shape3D> make3DHistogram(double xc, double yc, double radius, double[][][] cube, int numberOfBins, double maxValue, double gamma,
			ColorSpace colorspace) {
		ArrayList<Shape3D> res = new ArrayList<>();
		
		int n = numberOfBins;
		for (int x = -1; x <= n; x++)
			for (int y = -1; y <= n; y++)
				for (int z = -1; z <= n; z++) {
					if (x == -1 || x == n || y == -1 || y == n || z == -1 || z == n) {
						if ((x == -1 || x == n) && (y == -1 || y == n) ||
								(y == -1 || y == n) && (z == -1 || z == n) ||
								(z == -1 || z == n) && (x == -1 || x == n)) {
							Box c = new Box(radius / 10, radius / 10, radius / 10);
							Material material = getMaterial(x, y, z, n, colorspace);
							c.setMaterial(material);
							c.setTranslateX(xc + (x - n / 2d) * radius);
							c.setTranslateY(yc + (y - n / 2d) * radius);
							c.setTranslateZ((z - n / 2d) * radius);
							c.setOpacity(0.5);
							res.add(c);
						}
					} else {
						if (cube[x][y][z] != 0) {
							double ss = 0.5 * radius * Math.pow(cube[x][y][z] / maxValue, 1 / gamma);
							Shape3D c;
							if (cube[x][y][z] < 0)
								c = new Box(ss, ss, ss);
							else
								c = new Sphere(ss, 1 + 256 / numberOfBins);
							Material material = getMaterial(x, y, z, n, colorspace);
							c.setMaterial(material);
							c.setTranslateX(xc + (x - n / 2d) * radius);
							c.setTranslateY(yc + (y - n / 2d) * radius);
							c.setTranslateZ((z - n / 2d) * radius);
							res.add(c);
						}
					}
				}
		return res;
	}
	
	private static Material getMaterial(int x, int y, int z, int n, ColorSpace colorspace) {
		PhongMaterial material = new PhongMaterial();
		Color col;
		ColorSpaceConverter spc;
		int[] conv;
		
		switch (colorspace) {
			case CMYK:
				break;
			case GRAYSCALE_RGB_BLUE:
				break;
			case HSV:
				int c = java.awt.Color.HSBtoRGB((float) (x + 1.0) / (n + 1), (float) (y + 1.0) / (n + 1), (float) (z + 1.0) / (n + 1));
				col = new Color(new java.awt.Color(c).getRed() / 255d, new java.awt.Color(c).getGreen() / 255d, new java.awt.Color(c).getBlue() / 255d, 1.0);
				// Diffuse Color
				material.setDiffuseColor(col);
				// Specular Color
				material.setSpecularColor(col);
				material.setSpecularPower(39.0);
				return material;
			case LAB:
				spc = new ColorSpaceConverter();
				conv = spc.LABtoRGB((x + 1.0) / (n + 1) * 255 / 2.55, (y + 1.0) / (n + 1) * 255 - 128, (z + 1.0) / (n + 1) * 255 - 128);
				try {
					col = new Color(conv[0] / 256.0, conv[1] / 256.0, conv[2] / 256.0, 1.0);
					// Diffuse Color
					material.setDiffuseColor(col);
					// Specular Color
					material.setSpecularColor(col);
					material.setSpecularPower(39.0);
					return material;
				} catch (Exception e) {
					return null;
				}
			case RGB:
				// Diffuse Color
				material.setDiffuseColor(new Color((x + 1.0) / (n + 1), (y + 1.0) / (n + 1), (z + 1.0) / (n + 1), 1.0));
				// Specular Color
				material.setSpecularColor(new Color((x + 1.0) / (n + 1), (z + 1.0) / (n + 1), (y + 1.0) / (n + 1), 1.0));
				material.setSpecularPower(39.0);
				return material;
			case XYZ:
				spc = new ColorSpaceConverter();
				conv = spc.XYZtoRGB((x + 1.0) / (n + 1) * 100, (y + 1.0) / (n + 1) * 100, (z + 1.0) / (n + 1) * 100);
				try {
					col = new Color(conv[0] / 255d, conv[1] / 255d, conv[2] / 255d, 1.0);
					// Diffuse Color
					material.setDiffuseColor(col);
					// Specular Color
					material.setSpecularColor(col);
					material.setSpecularPower(39.0);
					return material;
				} catch (Exception e) {
					return null;
				}
		}
		return material;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.DEBUG;
	}
	
	@Override
	public String getName() {
		return "Calculate 3-D Color Histograms";
	}
	
	@Override
	public boolean isChangingImages() {
		return false;
	}
	
	@Override
	public String getDescription() {
		return "Calculates the 3-D color histograms for the mask images, this could be useful for inspecting the color distribution of an image.";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "<ul><li>Calculation Mode - Defines used color-space for histogram calculation."
				+ "<li>Process Vis - Processes only Visible image."
				+ "<li>Process Fluo - Processes only Fluorescence image."
				+ "<li>Process Nir - Processes only Near-infrared image."
				+ "<li>Process Ir - Processes only infrared image."
				+ "<li>Number of Bins - Defines the number of used histogram bins.</ul>";
	}
	
	public static void showHistogram(Image fi, Image optfi2, ImageData id) {
		ArrayList<String> possibleValues = StringManipulationTools.getStringListFromArray(ColorSpace.values());
		
		Object[] p = MyInputHelper.getInput("Please select the disired color-space and bin-count:", "Create 3-D Histogram Cube", new Object[] {
				"Color-space", possibleValues,
				"Bin-count", 20,
				"Gamma", 10d
		});
		
		if (p != null) {
			String calculationMode = (String) p[0];
			int bincount = (int) p[1];
			double gamma = (double) p[2];
			ColorSpace colorspace = ColorSpace.valueOfNiceString(calculationMode);
			calc3DHistogram(fi, optfi2, id, colorspace, bincount, gamma);
		}
	}
}

package iap.blocks.debug;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
import javax.swing.border.BevelBorder;

import de.ipk.ag_ba.image.operation.ColorSpaceConverter;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ColorSpace;
import de.ipk.ag_ba.image.structures.Image;

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
	private ColorSpace colorspace;
	private int numberOfBins;
	private Channel ch_a;
	private Channel ch_b;
	private Channel ch_c;
	private double[][][] cube;
	private double maxValue;
	private int sceneWidth;
	private int sceneHeight;
	private int camZdist = -700;
	
	@Override
	protected void prepare() {
		processVis = getBoolean("Process Vis", false);
		processFluo = getBoolean("Process Fluo", false);
		processNir = getBoolean("Process Nir", false);
		processIr = getBoolean("Process Ir", false);
		numberOfBins = getInt("Number of Histogram Bins", 10);
		
		ArrayList<String> possibleValues = new ArrayList<String>(Arrays.asList(ColorSpace.values().toString()));
		String calculationMode = optionsAndResults.getStringSettingRadio(this, "Calculation Mode", ColorSpace.RGB.name(), possibleValues);
		this.colorspace = ColorSpace.valueOf(calculationMode);
	}
	
	@Override
	protected Image processMask(Image mask) {
		CameraType ct = mask.getCameraType();
		if (ct == CameraType.VIS && processVis ||
				ct == CameraType.FLUO && processFluo ||
				ct == CameraType.NIR && processNir ||
				ct == CameraType.IR && processIr)
			calc3DHistogram(mask);
		
		return mask;
	}
	
	private void calc3DHistogram(Image img) {
		// get cube
		colorspace = ColorSpace.RGB;
		Channel[] channels = colorspace.getChannels();
		ch_a = channels[0];
		ch_b = channels[1];
		ch_c = channels[2];
		ColorCubeEstimation cce = new ColorCubeEstimation(img, ch_a, ch_b, ch_c, numberOfBins);
		cube = cce.getHistogramCube();
		maxValue = cce.getMaxValue();
		
		// Fx stuff
		JFrame frame = new JFrame("3-D Histogram " + img.getCameraType().getNiceName());
		final JFXPanel jpfx = new JFXPanel();
		frame.add(jpfx);
		frame.setSize(sceneWidth, sceneHeight);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jpfx.setPreferredSize(new Dimension(sceneWidth, sceneHeight));
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(jpfx);
				jpfx.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, java.awt.Color.LIGHT_GRAY, java.awt.Color.DARK_GRAY));
				
			}
		});
	}
	
	private void initFX(JFXPanel jp) {
		Scene s = createScene(jp);
		jp.setScene(s);
	}
	
	double anchorX, anchorY, anchorAngle;
	
	private Scene createScene(JComponent jp) {
		// Container
		Group root = new Group();
		final Group cubeGroup = new Group();
		root.setRotationAxis(Rotate.Y_AXIS);
		
		// Creating 3DShape
		int radius = 100;
		ArrayList<Shape3D> shl = make3DHistogram(0, 0, radius);
		
		// Adding nodes inside Container
		for (Shape3D sh : shl)
			cubeGroup.getChildren().addAll(sh);
		
		// add Legend for all corners
		int n = numberOfBins;
		addLegend(cubeGroup, 7, radius, n + 1, -2, -2, ch_a.name().split("_")[1]);
		addLegend(cubeGroup, 7, radius, -2, n + 1, -2, ch_b.name().split("_")[1]);
		addLegend(cubeGroup, 7, radius, -2, -2, n + 1, ch_c.name().split("_")[1]);
		addLegend(cubeGroup, 7, radius, n + 1, n + 1, -2, ch_a.name().split("_")[1] + " " + ch_b.name().split("_")[1]);
		addLegend(cubeGroup, 7, radius, n + 1, -2, n + 1, ch_a.name().split("_")[1] + " " + ch_c.name().split("_")[1]);
		addLegend(cubeGroup, 7, radius, -2, n + 1, n + 1, ch_b.name().split("_")[1] + " " + ch_c.name().split("_")[1]);
		addLegend(cubeGroup, 7, radius, -2, -2, -2, "0");
		addLegend(cubeGroup, 7, radius, n + 1, n + 1, n + 1, "1");
		
		// Creating Ambient Light
		AmbientLight ambient = new AmbientLight();
		ambient.setColor(Color.rgb(200, 200, 200, 1.0));
		root.getChildren().add(ambient);
		{
			// Creating Point Light
			PointLight point = new PointLight();
			point.setColor(Color.ANTIQUEWHITE);
			point.setTranslateZ(-600);
			point.getScope().add(cubeGroup);
			root.getChildren().add(point);
		}
		
		root.getChildren().add(cubeGroup);
		
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
		
		addInteraction(cubeGroup, scene, cam);
		
		Timeline animation = new Timeline();
		
		animation.getKeyFrames().addAll(
				new KeyFrame(Duration.ZERO,
						new KeyValue(root.rotationAxisProperty(), Rotate.Z_AXIS),
						new KeyValue(root.rotateProperty(), 0d)),
				new KeyFrame(Duration.seconds(30),
						new KeyValue(root.rotationAxisProperty(), Rotate.Z_AXIS),
						new KeyValue(root.rotateProperty(), 360d)));
		animation.setCycleCount(Animation.INDEFINITE);
		animation.play();
		
		return scene;
	}
	
	private void addInteraction(final Group cubeGroup, Scene scene, final PerspectiveCamera cam) {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				anchorX = event.getSceneX();
				anchorY = event.getSceneY();
				anchorAngle = cubeGroup.getRotate();
			}
		});
		
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				cubeGroup.setRotate(anchorAngle + anchorX - event.getSceneX());
			}
		});
		
		scene.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				System.out.println("x: " + event.getDeltaX() + " y: " + event.getDeltaY());
				camZdist += event.getDeltaY() * 0.5 * numberOfBins;
				Translate tz = new Translate(0.0, 0.0, cam.getTranslateZ() + event.getDeltaY() * 0.5 * numberOfBins);
				cam.getTransforms().add(tz);
			}
		});
		
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode() == KeyCode.UP) {
					System.out.println("Key Pressed: " + ke.getCode() + camZdist);
					cam.getTransforms().add(new Translate(0, 0, -camZdist));
					cam.getTransforms().add(new Rotate(5, Rotate.X_AXIS));
					cam.getTransforms().add(new Translate(0, 0, camZdist));
				}
				
				if (ke.getCode() == KeyCode.DOWN) {
					System.out.println("Key Pressed: " + ke.getCode());
					cam.getTransforms().add(new Translate(0, 0, -camZdist));
					cam.getTransforms().add(new Rotate(-5, Rotate.X_AXIS));
					cam.getTransforms().add(new Translate(0, 0, camZdist));
				}
				
				if (ke.getCode() == KeyCode.LEFT) {
					System.out.println("Key Pressed: " + ke.getCode());
					Translate tx = new Translate(-40 * numberOfBins, 0);
					cam.getTransforms().add(tx);
				}
				
				if (ke.getCode() == KeyCode.RIGHT) {
					System.out.println("Key Pressed: " + ke.getCode());
					Translate tx = new Translate(40 * numberOfBins, 0);
					cam.getTransforms().add(tx);
				}
			}
		});
	}
	
	private void addLegend(Group cubeGroup, int size, int radius, int x, int y, int z, String t) {
		Text text = new Text(0, 0, t);
		text.setScaleX(size);
		text.setScaleY(size);
		text.setScaleZ(size);
		text.setFill(Color.rgb(0, 0, 0, .99));
		getCoordinate(text, x, y, z, radius);
		cubeGroup.getChildren().add(text);
	}
	
	public void getCoordinate(Shape s, int binX, int binY, int binZ, int radius) {
		s.setTranslateX((binX - (numberOfBins + 1) / 2d) * radius);
		s.setTranslateY((binY - (numberOfBins + 1) / 2d) * radius);
		s.setTranslateZ((binZ - (numberOfBins + 1) / 2d) * radius);
	}
	
	public ArrayList<Shape3D> make3DHistogram(double xc, double yc, double radius) {
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
							Material material = getMaterial(x, y, z, n);
							c.setMaterial(material);
							c.setTranslateX(xc + (x - n / 2d) * radius);
							c.setTranslateY(yc + (y - n / 2d) * radius);
							c.setTranslateZ((z - n / 2d) * radius);
							c.setOpacity(0.5);
							res.add(c);
						}
					} else {
						if (cube[x][y][z] > 0) {
							Sphere c = new Sphere(radius * Math.pow(cube[x][y][z] / maxValue, 0.5), 1 + 256 / numberOfBins);
							Material material = getMaterial(x, y, z, n);
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
	
	private Material getMaterial(int x, int y, int z, int n) {
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
	
	public class ColorCubeEstimation {
		
		private final double[][][] histCube;
		private final int numberOfBins;
		private double maxVal = 0.0;
		
		public ColorCubeEstimation(Image img, Channel a, Channel b, Channel c, int numberOfBins) {
			histCube = new double[numberOfBins][numberOfBins][numberOfBins];
			this.numberOfBins = numberOfBins;
			int[] ch_a = img.io().channels().get(a).getAs1D();
			int[] ch_b = img.io().channels().get(b).getAs1D();
			int[] ch_c = img.io().channels().get(c).getAs1D();
			
			calcCube(ch_a, ch_b, ch_c);
		}
		
		private void calcCube(int[] ch_a, int[] ch_b, int[] ch_c) {
			double sizeOfBins = 256 / (double) numberOfBins;
			for (int idx = 0; idx < ch_a.length; idx++) {
				int a = (int) ((ch_a[idx] & 0x0000ff) / sizeOfBins);
				int b = (int) ((ch_b[idx] & 0x0000ff) / sizeOfBins);
				int c = (int) ((ch_c[idx] & 0x0000ff) / sizeOfBins);
				histCube[a][b][c]++;
				if (histCube[a][b][c] > maxVal)
					maxVal = histCube[a][b][c];
			}
		}
		
		public double[][][] getHistogramCube() {
			return histCube;
		}
		
		public double getMaxValue() {
			return maxVal;
		}
	};
	
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
}

package application;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.SystemOptions;

import util.Appearance;
import util.CameraView;
import util.FxLogoObjects;
import de.ipk.ag_ba.gui.webstart.ProgressWindow;

/***
 * @author Ulrich, Klukas
 *         Output class for rotation of earth
 */

public class AnimateLogoIAP extends Application implements ProgressWindow {
	
	private Pane root;
	private final Group scene_planet = new Group();
	private final Group scene_I = new Group();
	private final Group scene_A = new Group();
	private final Group scene_P = new Group();
	
	private final PerspectiveCamera camera_earth = new PerspectiveCamera(true);
	private final PerspectiveCamera camera_I = new PerspectiveCamera(true);
	private final PerspectiveCamera camera_A = new PerspectiveCamera(true);
	private final PerspectiveCamera camera_P = new PerspectiveCamera(true);
	private final double cameraDistanceToEarth = -70;
	private final double cameraDistanceToLetter = -50;// -30; // -7
	private final FxLogoObjects objectCreator = new FxLogoObjects();
	
	public AnimateLogoIAP() {
		// empty
	}
	
	// build all necessary components in the scene
	private Scene buildScene(double width, double height) {
		
		int STAR_COUNT = 1500;// SystemOptions.getInstance().getInteger("IAP", "FX//Splash Star Count", 1500);
		boolean star = false;// SystemOptions.getInstance().getBoolean("IAP", "FX//Splash Add Stars", false);
		
		Rectangle[] nodes = new Rectangle[STAR_COUNT];
		double[] angles = new double[STAR_COUNT];
		double[] sx = new double[STAR_COUNT];
		double[] sy = new double[STAR_COUNT];
		long[] ss = new long[STAR_COUNT];
		
		Group group = star ? getStarGroup(STAR_COUNT, nodes, angles, sx, sy, ss) : null;
		if (group != null)
			root = new Pane(group);
		else
			root = new Pane();
		// define main scene which contains the rectangle with white border
		Scene scene = new Scene(root, width, height, Color.BLACK);
		
		if (star)
			prepareStarAnim(scene, STAR_COUNT, nodes, angles, sx, sy, ss);
		
		double sc = 1;
		// the subscene contains the 3d rotating earth sphere
		int offx = 50;
		int offy = 20;
		int offxx = -20;
		int offyy = 00;
		SubScene subscene_plant = new SubScene(scene_planet, offx + 81 + 200, offy + height - 5, true, SceneAntialiasing.BALANCED);
		subscene_plant.setCamera(camera_earth);
		subscene_plant.setTranslateX(0);
		subscene_plant.setFill(Color.BLACK);
		subscene_plant.setTranslateY(0);
		
		SubScene subscene_I = new SubScene(scene_I, offx + 80 + offxx, offy + 200 + offyy, true, SceneAntialiasing.BALANCED);
		subscene_I.setCamera(camera_I);
		// subscene2.setFill(Color.YELLOW);
		subscene_I.setTranslateX(205 - 20);
		subscene_I.setTranslateY(30);
		
		SubScene subscene_A = new SubScene(scene_A, offx + 150 + offxx, offy + 200 + offyy, true, SceneAntialiasing.BALANCED);
		subscene_A.setCamera(camera_A);
		// subscene3.setFill(Color.RED);
		subscene_A.setTranslateX(268 + 10 - 10);
		subscene_A.setTranslateY(30);
		
		SubScene subscene_P = new SubScene(scene_P, offx + 100 + offxx, offy + 200 + offyy, true, SceneAntialiasing.BALANCED);
		subscene_P.setCamera(camera_P);
		// subscene4.setFill(Color.GREEN);
		subscene_P.setTranslateX(370 + 60);// 408);
		subscene_P.setTranslateY(30);
		root.setScaleX(sc);
		root.setScaleY(sc);
		root.setManaged(true);
		root.setMaxHeight(Double.MAX_VALUE);
		root.setMaxWidth(Double.MAX_VALUE);
		root.getChildren().addAll(
				// rect,
				subscene_plant, subscene_I, subscene_A, subscene_P);
		String planet = getPlanet();
		Color planetColor = getPlanetColor(planet);
		try {
			objectCreator.buildPlanet(planet, scene_planet, -3, 11, 22, 150, camera_earth);
		} catch (IOException e) {
			e.printStackTrace();
		}
		long animTime = SystemOptions.getInstance().getInteger("IAP", "FX//Last Startup Time", 2000);
		if (animTime < 1000)
			animTime = 1000;
		if (animTime > 10000)
			animTime = 10000;
		double ts = animTime / 12000d;
		int start = (int) (65 * 7 * ts + 700 * ts);
		int step = (int) (60 * 7 * ts);
		int delay = (int) (70 * 7 * ts);
		int add = 1;
		int id = (int) (1000 * ts);
		// Group group, String fxmlFile, double rate, int rotFromTime, int rotToTime, double angleStart, double xpos,
		// double angleFrom, double angleTo, double colorFadeFrom, double colorFadeTo
		objectCreator.buildSingleLetter(scene_I, "I.fxml", 1,
				-1.8, 2,
				-20, -90,
				id + start + 3 * step + 3 * delay, id + start + (4 + add) * step + 3 * delay,
				0, 90,
				start + 0 * step + 0 * delay, start + (1 + add) * step + 0 * delay, 1, planetColor);
		objectCreator.buildSingleLetter(scene_A, "A.fxml", 1,
				-2.7, 2,
				-20, -90,
				id + start + 4 * step + 4 * delay, id + start + (5 + add) * step + 4 * delay,
				0, 90,
				start + 1 * step + 1 * delay, start + (2 + add) * step + 1 * delay, 1, planetColor);
		objectCreator.buildSingleLetter(scene_P, "P.fxml", 1,
				-2.7, 2,
				-20, -90,
				id + start + 5 * step + 5 * delay, id + start + (6 + add) * step + 4 * delay,
				0, 85,
				start + 2 * step + 2 * delay, start + (3 + add) * step + 2 * delay, 1, planetColor);
		
		scene_planet.getChildren().addAll(Appearance.getPointLight_planet(Color.WHITE, 0, 300, planet.startsWith("sun")));
		
		CameraView.buildCamera_subscene_planet(scene_planet, camera_earth, cameraDistanceToEarth - 80, 11, 12);
		CameraView.buildCamera_subscenes_letters(scene_I, camera_I, cameraDistanceToLetter, 0, 0);
		CameraView.buildCamera_subscenes_letters(scene_A, camera_A, cameraDistanceToLetter, 0, 0);
		CameraView.buildCamera_subscenes_letters(scene_P, camera_P, cameraDistanceToLetter, 0, 0);
		
		return scene;
	}
	
	private static int planetIndex = SystemOptions.getInstance().getInteger("IAP", "FX//Logo Index", 3);
	
	private String getPlanet() {
		String[] planets = new String[] {
				"sun", "mercury", "venus", "earth5", "moon", "mars", "jupiter",
				"saturn", "uranus2", "neptun2", "pluto"
		};
		if (planetIndex < 0)
			planetIndex = 2;
		int idx = (planetIndex++) % 11;
		SystemOptions.getInstance().setInteger("IAP", "FX//Logo Index", idx);
		return planets[idx] + ".jpg";
	}
	
	private Color getPlanetColor(String planet) {
		switch (planet) {
			case "earth5.jpg":
				return Color.rgb(103, 103, 192).darker();
			case "jupiter.jpg":
				return Color.rgb(218, 211, 185);
			case "mars.jpg":
				return Color.rgb(165, 91, 56);
			case "mercury.jpg":
				return Color.rgb(167, 167, 167);
			case "moon.jpg":
				return Color.rgb(167, 167, 167);
			case "sun.jpg":
				return Color.rgb(255, 255, 180);
			case "venus.jpg":
				return Color.rgb(229, 178, 121);
			case "neptun2.jpg":
				return Color.rgb(94, 127, 220);
			case "pluto.jpg":
				return Color.rgb(130, 130, 150);
			case "saturn.jpg":
				return Color.rgb(220, 190, 90);
			case "uranus2.jpg":
				return Color.rgb(131, 185, 209);
		}
		return null;
	}
	
	private Scene buildScene2(double width, double height) {
		
		int STAR_COUNT = 500;// SystemOptions.getInstance().getInteger("IAP", "FX//Splash Star Count", 500);
		boolean star = false;// SystemOptions.getInstance().getBoolean("IAP", "FX//Splash Add Stars", false);
		Rectangle[] nodes = new Rectangle[STAR_COUNT];
		double[] angles = new double[STAR_COUNT];
		double[] sx = new double[STAR_COUNT];
		double[] sy = new double[STAR_COUNT];
		long[] ss = new long[STAR_COUNT];
		
		Group group = star ? getStarGroup(STAR_COUNT, nodes, angles, sx, sy, ss) : null;
		if (group != null)
			root = new Pane(group);
		else
			root = new Pane();
		// define main scene which contains the rectangle with white border
		Scene scene = new Scene(root, width, height, false, SceneAntialiasing.DISABLED);
		// scene.getStylesheets().add("application/application.css");
		// scene.setFill(Color.BLACK);
		
		if (star)
			prepareStarAnim(scene, STAR_COUNT, nodes, angles, sx, sy, ss);
		
		double sc = 1;
		// the subscene contains the 3d rotating earth sphere
		int offx = 50;
		int offy = 20;
		int offyy = 10;
		SubScene subscene1 = new SubScene(scene_planet, offx + 200, offy + height - 5, true, SceneAntialiasing.BALANCED);
		subscene1.setCamera(camera_earth);
		subscene1.setTranslateX(2);
		subscene1.setTranslateY(2);
		
		SubScene subscene2 = new SubScene(scene_I, offx + 80, offy + 200 + offyy, true, SceneAntialiasing.BALANCED);
		subscene2.setCamera(camera_I);
		subscene2.setTranslateX(205 + 20);
		subscene2.setTranslateY(35);
		
		SubScene subscene3 = new SubScene(scene_A, offx + 150, offy + 200 + offyy, true, SceneAntialiasing.BALANCED);
		subscene3.setCamera(camera_A);
		subscene3.setTranslateX(268 + 20);
		subscene3.setTranslateY(35);
		
		SubScene subscene4 = new SubScene(scene_P, offx + 150, offy + 200 + offyy, true, SceneAntialiasing.BALANCED);
		subscene4.setCamera(camera_P);
		subscene4.setTranslateX(370 + 20);// 408);
		subscene4.setTranslateY(35);
		root.setScaleX(sc);
		root.setScaleY(sc);
		root.setManaged(true);
		root.setMaxHeight(Double.MAX_VALUE);
		root.setMaxWidth(Double.MAX_VALUE);
		root.getChildren().addAll(
				// rect,
				subscene1, subscene2, subscene3, subscene4);
		
		String planet = getPlanet();
		Color planetColor = getPlanetColor(planet);
		
		try {
			objectCreator.buildPlanet(planet, scene_planet, 12, 12, 12, 50, camera_earth);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int start = 65 * 7 + 700;
		int step = 550 * 7;
		int delay = 20 * 7;
		int add = 1;
		// Group group, String fxmlFile, double rate, int rotFromTime, int rotToTime, double angleStart, double xpos,
		// double angleFrom, double angleTo, double colorFadeFrom, double colorFadeTo
		objectCreator.buildSingleLetter(scene_I, "I.fxml", 1,
				-2.01, 2,
				0, -90, start + 3 * step + 3 * delay, start + (4 + add) * step + 3 * delay, 90, 0,
				start + 0 * step + 0 * delay, start + (1 + add) * step + 0 * delay, Timeline.INDEFINITE, planetColor);
		objectCreator.buildSingleLetter(scene_A, "A.fxml", 1,
				-2.75, 2,
				0, -90, start + 3 * step + 3 * delay, start + (4 + add) * step + 3 * delay, 90, 0,
				start + 0 * step + 0 * delay, start + (1 + add) * step + 0 * delay, Timeline.INDEFINITE, planetColor);
		objectCreator.buildSingleLetter(scene_P, "P.fxml", 1,
				-2.7, 2,
				0, -90, start + 3 * step + 3 * delay, start + (4 + add) * step + 3 * delay, 90, 0,
				start + 0 * step + 0 * delay, start + (1 + add) * step + 0 * delay, Timeline.INDEFINITE, planetColor);
		
		scene_planet.getChildren().addAll(Appearance.getPointLight_planet(Color.WHITE, 0, 300, planet.startsWith("sun")));
		
		CameraView.buildCamera_subscene_planet(scene_planet, camera_earth, cameraDistanceToEarth, 11, 12);
		CameraView.buildCamera_subscenes_letters(scene_I, camera_I, cameraDistanceToLetter, 0, 0);
		CameraView.buildCamera_subscenes_letters(scene_A, camera_A, cameraDistanceToLetter, 0, 0);
		CameraView.buildCamera_subscenes_letters(scene_P, camera_P, cameraDistanceToLetter, 0, 0);
		
		return scene;
	}
	
	private void prepareStarAnim(Scene s, int STAR_COUNT, Node[] nodes, double[] angles, double[] sx, double[] sy,
			long[] start) {
		Platform.runLater(() -> {
			new AnimationTimer() {
				@Override
				public void handle(long now) {
					final double width = 00;// 0.5 * s.getWidth();
					final double height = 130;// 0.5 * s.getHeight();
					final double radius = Math.sqrt(2) * Math.max(s.getWidth(), s.getHeight());
					for (int i = 0; i < STAR_COUNT; i++) {
						Node node = nodes[i];
						double angle = angles[i];
						long t = ((now - start[i]) / 2) % 1000000000;
						double d = t / 2 * radius / 1000000000.0;
						double offX = i < STAR_COUNT / 3 ? 0 : (i < STAR_COUNT * 2 / 3 ? 150 : 280);
						// node.setTranslateX(Math.cos(angle) * d + width + sx[i] + offX);
						// node.setTranslateY(Math.sin(angle) * d + height + sy[i]);
						node.setTranslateX(
								// Math.cos(angle) * Math.sin(d) * 5 +
								width + sx[i] + offX);
						node.setTranslateY(Math.sin(angle) * d + height + sy[i]);
					}
				}
			}.start();
		});
	}
	
	private Group getStarGroup(int STAR_COUNT, Node[] nodes, double[] angles, double[] sx, double[] sy,
			long[] start) {
		
		Random random1 = new Random();
		Random random2 = new Random();
		Random random3 = new Random();
		int bound = 200000000;
		for (int i = 0; i < STAR_COUNT / 3; i++) {
			random1.nextInt(bound);
			random1.nextInt(bound);
		}
		for (int i = 0; i < STAR_COUNT / 3; i++)
			random2.nextInt(bound);
		for (int i = 0; i < STAR_COUNT; i++) {
			nodes[i] = new Rectangle(1, 1, i < STAR_COUNT / 3 ? Color.RED : (i < (STAR_COUNT * 2) / 3 ? Color.GREEN : Color.BLUE));
			angles[i] = 2.0 * Math.PI * random1.nextDouble();
			start[i] =
					i < STAR_COUNT / 3 ? random1.nextInt(bound) : (i < (STAR_COUNT * 2) / 3 ? random2.nextInt(bound) : random3.nextInt(bound));
			sx[i] = random1.nextDouble() * 300;
			sy[i] = random1.nextDouble() * 100 - 50;
		}
		return new Group(nodes);
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		// output window as stage
		final double STAGEWIDTH = 621;
		final double STAGEHEIGHT = 261;
		
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		primaryStage.setScene(buildScene(STAGEWIDTH, STAGEHEIGHT));
		primaryStage.setX((screenDim.width - STAGEWIDTH) / 2);
		primaryStage.setY((screenDim.height - STAGEHEIGHT) / 2);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	final double STAGEWIDTH = 580;// 601;
	final double STAGEHEIGHT = 260;// 1;
	private FXsplash jf;
	private Scene mainScene;
	protected boolean keep;
	private int value;
	private int maximum;
	
	public JComponent getFX(boolean directInit, boolean undecorated) {
		JFXPanel jp = new JFXPanel();
		if (undecorated)
			jp.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					AnimateLogoIAP.this.hide();
					ProgressWindow progressWindow = new AnimateLogoIAP();
					progressWindow.show(false);
				}
			});
		jp.setPreferredSize(new Dimension((int) STAGEWIDTH, (int) STAGEHEIGHT));
		// jp.setSize(new Dimension(800, 600));
		
		if (directInit) {
			initFX(jp);
		} else
			Platform.runLater(() -> {
				initFX(jp);
			});
		jp.setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY));
		// jp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, java.awt.Color.LIGHT_GRAY, java.awt.Color.DARK_GRAY));
		return jp;
	}
	
	private void initFX(JFXPanel jp) {
		Scene s = buildScene(STAGEWIDTH, STAGEHEIGHT);
		// s.getRoot().setPickOnBounds(true);
		s.setFill(Color.BLACK);
		
		jp.setScene(s);
		this.mainScene = s;
	}
	
	@Override
	public void show(boolean undecorated) {
		this.jf = new FXsplash((int) STAGEWIDTH, (int) STAGEHEIGHT, getFX(false, undecorated), undecorated);
		// center on display
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		Dimension screenDim = new Dimension(width, height);// Toolkit.getDefaultToolkit().getScreenSize();
		int w = jf.getWidth();
		int h = jf.getHeight();
		jf.setLocation((screenDim.width - w) / 2,
				(screenDim.height - h) / 2);
		
		jf.setVisible(true);
		
		// center on display
		jf.setLocation((screenDim.width - w) / 2,
				(screenDim.height - h) / 2);
		
	}
	
	@Override
	public void hide() {
		if (!jf.isVisible())
			return;
		if (keep) {
			// jf.setVisible(false);
			// jf.dispose();
		} else {
			jf.setVisible(false);
			// jf.dispose();
		}
	}
	
	public Scene getScene() {
		
		Scene s = buildScene(STAGEWIDTH, STAGEHEIGHT);
		s.setFill(Color.BLACK);
		return s;
	}
	
	public Scene getScene2() {
		
		Scene s = buildScene2(STAGEWIDTH, STAGEHEIGHT);
		s.setFill(Color.BLACK);
		return s;
	}
	
	@Override
	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}
	
	@Override
	public void setText(String text) {
		// empty
	}
	
	@Override
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public int getValue() {
		return value;
	}
	
	@Override
	public void setVisible(boolean b) {
		if (b)
			show(true);
		else
			hide();
	}
	
	@Override
	public void setInitialisationFinished() {
		// empty
	}
	
	@Override
	public int getMaximum() {
		return maximum;
	}
}

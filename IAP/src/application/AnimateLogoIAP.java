package application;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import util.Appearance;
import util.CameraView;
import util.Objects;
import de.ipk.ag_ba.gui.webstart.ProgressWindow;

/***
 * @author Ulrich
 *         Output class for rotation of earth
 */

public class AnimateLogoIAP extends Application implements ProgressWindow {
	
	private final Group root = new Group();
	private final Group root_subscene1 = new Group();
	private final Group root_subscene2 = new Group();
	private final Group root_subscene3 = new Group();
	private final Group root_subscene4 = new Group();
	
	private final PerspectiveCamera camera_subscene1 = new PerspectiveCamera(true);
	private final PerspectiveCamera camera_subscene2 = new PerspectiveCamera(true);
	private final PerspectiveCamera camera_subscene3 = new PerspectiveCamera(true);
	private final PerspectiveCamera camera_subscene4 = new PerspectiveCamera(true);
	private final double cameraDistanceToEarth = -70;
	private final double cameraDistanceToLetter = -80;
	private final Objects object = new Objects();
	
	public AnimateLogoIAP() {
		// empty
	}
	
	// build all necessary components in the scene
	private Scene buildScene(double width, double height) {
		
		// define main scene which contains the rectangle with white border
		Scene scene = new Scene(root, width, height, false, SceneAntialiasing.DISABLED);
		scene.getStylesheets().add("application/application.css");
		scene.setFill(Color.BLACK);
		
		// Rectangle rect = new Rectangle(width - 2, height - 2);
		// rect.setLayoutX(1);
		// rect.setLayoutY(1);
		// rect.getStyleClass().add("borderAround");
		
		// the subscene contains the 3d rotating earth sphere
		SubScene subscene1 = new SubScene(root_subscene1, 200, height - 5, true, SceneAntialiasing.BALANCED);
		subscene1.setFill(Color.BLACK);
		subscene1.setCamera(camera_subscene1);
		subscene1.setTranslateX(2);
		subscene1.setTranslateY(2);
		
		SubScene subscene2 = new SubScene(root_subscene2, 80, 212, true, SceneAntialiasing.BALANCED);
		subscene2.setFill(Color.BLACK);
		subscene2.setCamera(camera_subscene2);
		subscene2.setTranslateX(205);
		subscene2.setTranslateY(35);
		
		SubScene subscene3 = new SubScene(root_subscene3, 150, 212, true, SceneAntialiasing.BALANCED);
		subscene3.setFill(Color.BLACK);
		subscene3.setCamera(camera_subscene3);
		subscene3.setTranslateX(268);
		subscene3.setTranslateY(35);
		
		SubScene subscene4 = new SubScene(root_subscene4, 150, 212, true, SceneAntialiasing.BALANCED);
		subscene4.setFill(Color.BLACK);
		subscene4.setCamera(camera_subscene4);
		subscene4.setTranslateX(408);
		subscene4.setTranslateY(35);
		
		root.getChildren().addAll(
				// rect,
				subscene1, subscene2, subscene3, subscene4);
		
		object.buildEarth(root_subscene1);
		
		int start = 65;
		int step = 50;
		int delay = 20;
		int add = 1;
		// Group group, String fxmlFile, double rate, int rotFromTime, int rotToTime, double angleStart, double xpos,
		// double angleFrom, double angleTo, double colorFadeFrom, double colorFadeTo
		object.buildSingleLetter(root_subscene2, "I.fxml", 0.15,
				start + 3 * step + 3 * delay, start + (4 + add) * step + 3 * delay,
				-90, -2.01,
				0, 90,
				start + 0 * step + 0 * delay, start + (1 + add) * step + 0 * delay);
		object.buildSingleLetter(root_subscene3, "A.fxml", 0.15,
				start + 4 * step + 4 * delay, start + (5 + add) * step + 4 * delay,
				-85, -2.75, 0, 90,
				start + 1 * step + 1 * delay, start + (2 + add) * step + 1 * delay);
		object.buildSingleLetter(root_subscene4, "P.fxml", 0.15,
				start + 5 * step + 5 * delay, start + (6 + add) * step + 4 * delay,
				-90, -2.7, 0, 85,
				start + 2 * step + 2 * delay, start + (3 + add) * step + 2 * delay);
		
		root_subscene1.getChildren().addAll(Appearance.getPointLight_earth(Color.WHITE, 0, 65));
		
		CameraView.buildCamera_subscene(root_subscene1, camera_subscene1, cameraDistanceToEarth, 11, 12);
		CameraView.buildCamera_subscenes(root_subscene2, camera_subscene2, cameraDistanceToLetter + 73, 0, 0);
		CameraView.buildCamera_subscenes(root_subscene3, camera_subscene3, cameraDistanceToLetter + 73, 0, 0);
		CameraView.buildCamera_subscenes(root_subscene4, camera_subscene4, cameraDistanceToLetter + 73, 0, 0);
		
		return scene;
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		// output window as stage
		final double STAGEWIDTH = 601;
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
	
	final double STAGEWIDTH = 601;
	final double STAGEHEIGHT = 261;
	private FXsplash jf;
	private Scene mainScene;
	protected boolean keep;
	
	public JComponent getFX(boolean directInit, boolean undecorated) {
		JFXPanel jp = new JFXPanel();
		if (undecorated)
			jp.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
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
		jp.setScene(s);
		this.mainScene = s;
	}
	
	@Override
	public void show(boolean undecorated) {
		this.jf = new FXsplash((int) STAGEWIDTH, (int) STAGEHEIGHT, getFX(false, undecorated), undecorated);
		// center on display
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
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
}

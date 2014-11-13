package de.ipk.ag_ba.commands.about;

import java.awt.Dimension;
import java.util.ArrayList;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author Christian Klukas
 */
public class ActionJavaFX extends AbstractNavigationAction {
	
	private NavigationButton src;
	
	public ActionJavaFX(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		MainPanelComponent mp = new MainPanelComponent(getFX());
		return mp;
	}
	
	private JComponent getFX() {
		JFXPanel jp = new JFXPanel();
		jp.setPreferredSize(new Dimension(800, 600));
		
		Platform.runLater(() -> {
			initFX(jp);
		});
		jp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, java.awt.Color.LIGHT_GRAY, java.awt.Color.DARK_GRAY));
		return jp;
	}
	
	private void initFX(JFXPanel jp) {
		Scene s = createScene(jp);
		jp.setScene(s);
	}
	
	public ArrayList<Shape3D> make3DObjects(double xc, double yc, double radius) {
		ArrayList<Shape3D> res = new ArrayList<>();
		
		int n = SystemOptions.getInstance().getInteger("IAP", "FX//Demo Cube N", 5);
		for (int x = 0; x < n; x++)
			for (int y = 0; y < n; y++)
				for (int z = 0; z < n; z++) {
					// Cylinder c = new Cylinder(radius, radius);
					Box c = new Box(radius / n, radius / n, radius / n);
					// Creating PhongMaterial
					PhongMaterial material = new PhongMaterial();
					// Diffuse Color
					material.setDiffuseColor(new Color((x + 0.5) / (n + 1), (y + 0.5) / (n + 1), (z + 0.5) / (n + 1), 1.0));
					// Specular Color
					material.setDiffuseColor(new Color((x + 0.5) / (n + 1), (z + 0.5) / (n + 1), (y + 0.5) / (n + 1), 1.0));
					c.setMaterial(material);
					c.setLayoutX(xc + (x - n / 2d) * radius / 4);
					c.setLayoutY(yc + (y - n / 2d) * radius / 4);
					c.setTranslateZ((z - n / 2d) * radius / 4);
					// c.getTransforms().add(new Rotate(20, Rotate.X_AXIS));
					// c.getTransforms().add(new Rotate(10, Rotate.Z_AXIS));
					// c.getTransforms().add(new Rotate(30, Rotate.Y_AXIS));
					res.add(c);
				}
		return res;
	}
	
	double anchorX, anchorY, anchorAngle;
	
	private Scene createScene(JComponent jp) {
		// Container
		Group root = new Group();
		root.setTranslateZ(jp.getWidth() / 2);
		root.setRotationAxis(Rotate.Y_AXIS);
		// Creating 3DShape
		ArrayList<Shape3D> shl = make3DObjects(800, 350, 100);
		// Creating Ambient Light
		AmbientLight ambient = new AmbientLight();
		ambient.setColor(Color.rgb(110, 255, 110, 0.6));
		{
			// Creating Point Light
			PointLight point = new PointLight();
			point.setColor(Color.ANTIQUEWHITE);
			point.setLayoutX(jp.getWidth() + 700);
			point.setLayoutY(jp.getHeight() / 2);
			point.setTranslateZ(-600);
			for (Shape3D sh : shl)
				point.getScope().add(sh);
			
			root.getChildren().addAll(point, ambient);
			
			// Adding nodes inside Container
			for (Shape3D sh : shl)
				root.getChildren().addAll(sh);
		}
		// Adding to scene
		Scene scene = new Scene(root, 2600, 2600, true);
		// Creating Perspective View Camera
		PerspectiveCamera cam = new PerspectiveCamera(false);
		scene.setCamera(cam);
		
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				anchorX = event.getSceneX();
				anchorY = event.getSceneY();
				anchorAngle = root.getRotate();
			}
		});
		
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				root.setRotate(anchorAngle + anchorX - event.getSceneX());
			}
		});
		
		Timeline animation = new Timeline();
		for (Shape3D sh : shl)
			animation.getKeyFrames().addAll(
					new KeyFrame(Duration.ZERO,
							new KeyValue(sh.rotationAxisProperty(), Rotate.Z_AXIS),
							new KeyValue(sh.rotateProperty(), 0d)),
					new KeyFrame(Duration.seconds(5),
							new KeyValue(sh.rotationAxisProperty(), Rotate.Z_AXIS),
							new KeyValue(sh.rotateProperty(), 360d)));
		animation.setCycleCount(Animation.INDEFINITE);
		animation.play();
		
		return scene;
	}
	
	@Override
	public String getDefaultTitle() {
		return "FX";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/cube.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<>(currentSet);
		currentSet.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}

package iap.blocks.debug;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class FxInteraction {
	
	/**
	 * 
	 */
	private int camZdist;
	private final int numberOfBins;
	double anchorX, anchorY, anchorAngle;
	
	public FxInteraction(Group cubeGroup, Scene scene, PerspectiveCamera cam, int camZdist, int numberOfBins) {
		this.camZdist = camZdist;
		this.numberOfBins = numberOfBins;
		addInteraction(cubeGroup, scene, cam);
	}
	
	public void addInteraction(final Group cubeGroup, Scene scene, final PerspectiveCamera cam) {
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
				cubeGroup.setRotate(anchorAngle + anchorX
						- event.getSceneX());
			}
		});
		
		scene.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				// System.out.println("x: " + event.getDeltaX() + " y: " + event.getDeltaY());
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
					System.out.println("Key Pressed: " + ke.getCode() + camZdist);
					cam.getTransforms().add(new Translate(0, 0, -camZdist));
					cam.getTransforms().add(new Rotate(-5, Rotate.X_AXIS));
					cam.getTransforms().add(new Translate(0, 0, camZdist));
				}
				
				if (ke.getCode() == KeyCode.LEFT) {
					// System.out.println("Key Pressed: " + ke.getCode());
					Translate tx = new Translate(-40 * numberOfBins, 0);
					cam.getTransforms().add(tx);
				}
				
				if (ke.getCode() == KeyCode.RIGHT) {
					// System.out.println("Key Pressed: " + ke.getCode());
					Translate tx = new Translate(40 * numberOfBins, 0);
					cam.getTransforms().add(tx);
				}
			}
		});
	};
}
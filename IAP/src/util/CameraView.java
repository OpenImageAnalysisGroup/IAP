package util;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;

public class CameraView {
	
	// set camera configuration for subscene1
	public static void buildCamera_subscene(Group group, PerspectiveCamera camera, double cameraDistance, double camX, double camY) {
		
		group.getChildren().add(camera);
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(cameraDistance);
		camera.setTranslateX(camX);
		camera.setTranslateY(camY);
		
	}
	
	// set camera configuration for other subscene1
	public static void buildCamera_subscenes(Group group, PerspectiveCamera camera, double cameraDistance, double camX, double camY) {
		
		Universe cameraXform = new Universe();
		Universe cameraXform2 = new Universe();
		Universe cameraXform3 = new Universe();
		
		group.getChildren().add(cameraXform);
		cameraXform.getChildren().add(cameraXform2);
		cameraXform2.getChildren().add(cameraXform3);
		cameraXform3.getChildren().add(camera);
		
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(cameraDistance);
		camera.setTranslateX(camX);
		camera.setTranslateY(camY);
		
		cameraXform.rx.setAngle(0);
		cameraXform.ry.setAngle(0);
		
	}
	
}

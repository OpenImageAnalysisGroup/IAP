package iap.blocks.debug;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class FxCameraAnnimation {
	Timeline animation;
	
	public FxCameraAnnimation(PerspectiveCamera cam) {
		animation = new Timeline();
		
		animation.getKeyFrames().addAll(
				new KeyFrame(Duration.ZERO,
						new KeyValue(cam.rotationAxisProperty(), Rotate.Z_AXIS),
						new KeyValue(cam.rotateProperty(), 0d)),
				new KeyFrame(Duration.seconds(30),
						new KeyValue(cam.rotationAxisProperty(), Rotate.Z_AXIS),
						new KeyValue(cam.rotateProperty(), 360d)));
		animation.setCycleCount(Animation.INDEFINITE);
	}
	
	public void startAnimation() {
		animation.play();
	}
	
	public void pauseAnimation() {
		animation.pause();
	}
	
	public void stopAnimation() {
		animation.stop();
	}
}

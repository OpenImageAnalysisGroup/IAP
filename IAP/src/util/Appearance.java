package util;

import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

public class Appearance {
	
	// get the texture for sphere as material
	public static PhongMaterial getMaterial4() throws IOException {
		
		Image image = new Image(new Appearance().getClass().getResource("images/earth5.jpg").openStream(),
				1536, 768, false, true);
		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseMap(image);
		
		return mat;
	}
	
	// get the texture for sphere as material
	public static PhongMaterial getMaterial5() {
		
		PhongMaterial mat = new PhongMaterial();
		mat.setSpecularColor(Color.rgb(123, 93, 172));
		
		return mat;
	}
	
	// set lights for scene
	public static AmbientLight getAmbientLight(Color c) {
		
		AmbientLight light_a = new AmbientLight();
		light_a.setColor(c);
		return light_a;
	}
	
	public static PointLight getPointLight_earth(Color c, double from, double to) {
		
		PointLight light_p = new PointLight();
		light_p.setTranslateX(1500);
		light_p.setTranslateY(-3200);
		light_p.setTranslateZ(-1500);
		
		light_p.setColor(Color.BLACK);
		
		Timeline rot = new Timeline();
		rot.setCycleCount(1);
		rot.setRate(1);
		// rot.setRate(1);
		rot.getKeyFrames().addAll(
				new KeyFrame(Duration.millis(from), new KeyValue(light_p.colorProperty(), Color.BLACK)),
				new KeyFrame(Duration.millis(to), new KeyValue(light_p.colorProperty(), c))
				
				);
		
		rot.playFromStart();
		
		return light_p;
		
	}
	
	public static PointLight getPointLight_letter(double rate, Color c, double from, double to) {
		
		PointLight light_p = new PointLight();
		
		light_p.setTranslateX(1900);
		light_p.setTranslateY(-3800);
		light_p.setTranslateZ(-15000);
		
		Timeline rot = new Timeline();
		rot.setCycleCount(1);
		rot.setRate(rate);
		// rot.setRate(1);
		rot.getKeyFrames().addAll(
				new KeyFrame(Duration.millis(from), new KeyValue(light_p.colorProperty(), Color.BLACK)),
				new KeyFrame(Duration.millis(to), new KeyValue(light_p.colorProperty(), c))
				// new KeyFrame(Duration.seconds(from), new KeyValue(light_p.colorProperty(), Color.BLACK)),
				// new KeyFrame(Duration.seconds(to), new KeyValue(light_p.colorProperty(),c))
				
				);
		light_p.setColor(Color.BLACK);
		rot.playFromStart();
		
		return light_p;
	}
	
}

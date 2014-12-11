package util;

import java.io.IOException;
import java.io.InputStream;

import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class FxLogoObjects {
	
	// create the rotating earth
	public void buildEarth(Group group, double x, double y, double r, int div) throws IOException {
		
		Sphere s1 = new Sphere(r, div);
		s1.setTranslateX(x);
		s1.setTranslateY(y);
		s1.setRotationAxis(Rotate.Z_AXIS);
		s1.setRotate(-90);
		s1.setMaterial(Appearance.getMaterial4());
		
		// pane for rotation
		Group branch_rot_earth = new Group();
		branch_rot_earth.getChildren().addAll(s1);
		branch_rot_earth.setRotationAxis(Rotate.X_AXIS);
		int sw = 0;
		Timeline rot_earth = MakeAnimation.rotateTimeline(branch_rot_earth, 1, 0, 4 * 60 * 1000, Timeline.INDEFINITE, sw, 360 + sw);
		rot_earth.playFromStart();
		
		group.getChildren().add(branch_rot_earth);
		
	}
	
	public void buildSingleLetter(Group group, String fxmlFile, double rate, double xpos, double ypos, double zpos,
			double angleStart, int rotFromTime, int rotToTime,
			double angleFrom, double angleTo, double colorFadeFrom, double colorFadeTo) {
		
		Group mv = new Group();
		Group branch_rot_letter = new Group();
		MeshView m = new MeshView();
		
		try {
			InputStream s = new Appearance().getClass().getResource("fxml/" + fxmlFile).openStream();
			m = new FXMLLoader().load(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PointLight pl = Appearance.getPointLight_letter(rate,
				// Color.rgb(123, 93, 172),
				Color.WHITE,
				colorFadeFrom, colorFadeTo);
		
		m.setDrawMode(DrawMode.FILL);
		m.setCullFace(CullFace.NONE);
		m.setMaterial(new PhongMaterial(Color.rgb(103, 103, 192).darker()));
		// m.setMaterial(new PhongMaterial(Color.rgb(123, 93, 172)));
		
		m.setDepthTest(DepthTest.ENABLE);
		
		mv.setTranslateX(xpos);
		mv.setTranslateY(ypos);
		mv.setTranslateZ(zpos);
		mv.setRotationAxis(Rotate.Y_AXIS);
		mv.setRotate(angleStart);
		
		mv.getChildren().addAll(m);
		
		// group for rotation
		branch_rot_letter.getChildren().addAll(mv);
		branch_rot_letter.setRotationAxis(Rotate.Y_AXIS);
		Timeline rot_letter = MakeAnimation.rotateTimeline(branch_rot_letter, rate, rotFromTime, rotToTime, 1, angleFrom, angleTo);
		rot_letter.playFromStart();
		
		// group.getChildren().addAll(branch_rot_letter, Appearance.getPointLight_letter(Color.rgb(123,93,172), colorFadeFrom, colorFadeTo));
		group.getChildren().addAll(branch_rot_letter, pl);
		
	}
	
}

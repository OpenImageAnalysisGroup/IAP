package util;

import java.io.IOException;

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

import org.ErrorMsg;

public class Objects {
	
	// create the rotating earth
	public void buildEarth(Group group) {
		
		Group gsphere = new Group();
		Sphere s1 = new Sphere(12, 50);
		s1.setTranslateX(12);
		s1.setTranslateY(12);
		s1.setRotationAxis(Rotate.Z_AXIS);
		s1.setRotate(-90);
		s1.setMaterial(Appearance.getMaterial4());
		
		gsphere.getChildren().addAll(s1);
		
		// pane for rotation
		Group branch_rot_earth = new Group();
		branch_rot_earth.getChildren().addAll(gsphere);
		branch_rot_earth.setRotationAxis(Rotate.X_AXIS);
		int sw = 0;
		Timeline rot_earth = MakeAnimation.rotateTimeline(branch_rot_earth, 0.035 / 1.4, 0, 4000, Timeline.INDEFINITE, sw, 360 + sw);
		// Timeline rot_earth = MakeAnimation.rotateTimeline(branch_rot_earth,0.2, 0,25,Timeline.INDEFINITE,0,360);
		rot_earth.playFromStart();
		
		group.getChildren().add(branch_rot_earth);
		
	}
	
	public void buildSingleLetter(Group group, String fxmlFile, double rate, int rotFromTime, int rotToTime, double angleStart, double xpos,
			double angleFrom, double angleTo, double colorFadeFrom, double colorFadeTo) {
		
		Group mv = new Group();
		Group branch_rot_letter = new Group();
		MeshView m = new MeshView();
		
		try {
			m = FXMLLoader.load(getClass().getResource("../util/fxml/" + fxmlFile));
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		PointLight pl = Appearance.getPointLight_letter(
				// Color.rgb(123, 93, 172),
				Color.WHITE,
				colorFadeFrom, colorFadeTo);
		
		m.setDrawMode(DrawMode.FILL);
		m.setCullFace(CullFace.NONE);
		m.setMaterial(new PhongMaterial(Color.rgb(123, 93, 172)));
		
		m.setDepthTest(DepthTest.ENABLE);
		
		mv.setTranslateX(xpos);
		mv.setTranslateY(2);
		mv.setTranslateZ(0);
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

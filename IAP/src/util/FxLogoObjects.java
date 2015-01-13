package util;

import java.io.IOException;
import java.time.Year;
import java.util.Calendar;

import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import org.graffiti.util.InstanceLoader;

public class FxLogoObjects {
	
	// create the rotating earth
	public void buildPlanet(String planet, Group group, double x, double y, double r, int div, PerspectiveCamera camera_planet) throws IOException {
		
		Sphere s1 = new Sphere(r, div);
		s1.setTranslateX(x);
		s1.setTranslateY(y);
		s1.setRotationAxis(Rotate.Z_AXIS);
		s1.setRotate(-90);
		s1.setMaterial(Appearance.getMaterial(planet));
		
		boolean isEarth = planet.startsWith("earth");
		if (!isEarth) {
			// camera_planet.setRotationAxis(Rotate.Z_AXIS);
			// camera_planet.setRotate(-2 * 23.4);
		}
		// pane for rotation
		Group branch_rot_earth = new Group();
		branch_rot_earth.getChildren().addAll(s1);
		branch_rot_earth.setRotationAxis(Rotate.X_AXIS);
		int sw = 270;
		Timeline rot_earth = MakeAnimation.rotateTimeline(
				branch_rot_earth, 1, 0, 4 * 60 * 1000, Timeline.INDEFINITE, sw, 360 + sw);
		rot_earth.playFromStart();
		
		if (isEarth) {
			// get season
			Calendar cw = Calendar.getInstance();
			cw.set(Calendar.YEAR, Year.now().getValue());
			cw.set(Calendar.MONTH, 11);
			cw.set(Calendar.DAY_OF_MONTH, 22);
			int winterStartDay = cw.get(Calendar.DAY_OF_YEAR) % 365;
			
			int todayDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % 365;
			
			long daysAwayFromWinter;
			daysAwayFromWinter = Math.abs(winterStartDay - todayDay) % 365;
			if (daysAwayFromWinter > 365 / 2)
				daysAwayFromWinter = 365 - daysAwayFromWinter;
			
			Group branch_rot_earth_yearly = new Group();
			branch_rot_earth_yearly.getChildren().addAll(branch_rot_earth);
			branch_rot_earth_yearly.setRotationAxis(Rotate.Z_AXIS);
			branch_rot_earth_yearly.setRotate(2 * 23.4 * daysAwayFromWinter / (365 / 2));
			group.getChildren().add(branch_rot_earth_yearly);
		} else {
			Group branch_rot_earth_yearly = new Group();
			branch_rot_earth_yearly.getChildren().addAll(branch_rot_earth);
			branch_rot_earth_yearly.setRotationAxis(Rotate.Z_AXIS);
			branch_rot_earth_yearly.setRotate(27);
			group.getChildren().add(branch_rot_earth_yearly);
		}
	}
	
	public void buildSingleLetter(Group group, String fxmlFile, double rate, double xpos, double ypos, double zpos,
			double angleStart, int rotFromTime, int rotToTime,
			double angleFrom, double angleTo, double colorFadeFrom, double colorFadeTo, int nn, Color planetColor) {
		
		Group mv = new Group();
		Group branch_rot_letter = new Group();
		MeshView m = new MeshView();
		
		try {
			FXMLLoader.setDefaultClassLoader(InstanceLoader.getCurrentLoader());
			m = FXMLLoader.load(getClass().getResource("fxml/" + fxmlFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PointLight pl = Appearance.getPointLight_letter(rate,
				// Color.rgb(123, 93, 172),
				Color.WHITE,
				colorFadeFrom, colorFadeTo);
		
		m.setDrawMode(DrawMode.FILL);
		m.setCullFace(CullFace.NONE);
		m.setMaterial(new PhongMaterial(planetColor));
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
		Timeline rot_letter = MakeAnimation.rotateTimeline(branch_rot_letter, rate, rotFromTime, rotToTime, nn, angleFrom, angleTo);
		rot_letter.playFromStart();
		
		// group.getChildren().addAll(branch_rot_letter, Appearance.getPointLight_letter(Color.rgb(123,93,172), colorFadeFrom, colorFadeTo));
		group.getChildren().addAll(branch_rot_letter, pl);
		
	}
	
}

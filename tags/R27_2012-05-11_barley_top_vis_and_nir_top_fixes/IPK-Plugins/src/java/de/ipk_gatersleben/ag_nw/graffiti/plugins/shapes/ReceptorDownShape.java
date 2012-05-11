package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.util.ArrayList;
import java.util.Collection;

import org.Vector2d;

public class ReceptorDownShape extends RelativePolyShape {
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		Collection<Vector2d> points = new ArrayList<Vector2d>();
		double offA = 0.2;
		double offB = 0.8;
		points.add(new Vector2d(0, 0));
		points.add(new Vector2d(0.5, offA));
		points.add(new Vector2d(1, 0));
		points.add(new Vector2d(1, offB));
		points.add(new Vector2d(0.5, 1));
		points.add(new Vector2d(0, offB));
		return points;
	}
	
}

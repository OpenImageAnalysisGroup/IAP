package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.util.ArrayList;
import java.util.Collection;

import org.Vector2d;

public class TagRightShape extends RelativePolyShape {
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		Collection<Vector2d> points = new ArrayList<Vector2d>();
		double offB = 0.8;
		points.add(new Vector2d(0, 0));
		points.add(new Vector2d(offB, 0));
		points.add(new Vector2d(1, 0.5));
		points.add(new Vector2d(offB, 1));
		points.add(new Vector2d(0, 1));
		return points;
	}
	
}

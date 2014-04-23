package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.util.ArrayList;
import java.util.Collection;

import org.Vector2d;

public class ComplexShape extends RelativePolyShape {
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		Collection<Vector2d> points = new ArrayList<Vector2d>();
		double off = 0.05;
		double offA = off;
		double offB = (1 - off);
		
		points.add(new Vector2d(0, -offA));
		points.add(new Vector2d(offA, 0));
		points.add(new Vector2d(offB, 0));
		points.add(new Vector2d(1, -offA));
		points.add(new Vector2d(1, -offB));
		points.add(new Vector2d(offB, 1));
		points.add(new Vector2d(offA, 1));
		points.add(new Vector2d(0, -offB));
		return points;
	}
	
}

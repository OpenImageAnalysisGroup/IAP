package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.util.ArrayList;
import java.util.Collection;

import org.Vector2d;

public class DiamondShape extends RelativePolyShape {
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		Collection<Vector2d> points = new ArrayList<Vector2d>();
		points.add(new Vector2d(0.5, 0));
		points.add(new Vector2d(1, 0.5));
		points.add(new Vector2d(0.5, 1));
		points.add(new Vector2d(0, 0.5));
		return points;
	}
	
}

package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.Vector2d;

public class PaperShape extends RelativePolyShape {
	
	public PaperShape() {
		ignorePoints = new HashSet<Integer>();
		ignorePoints.add(3);
		ignorePoints.add(4);
	}
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		Collection<Vector2d> points = new ArrayList<Vector2d>();
		double off = roundingRadius;
		points.add(new Vector2d(0, 0)); // 0
		points.add(new Vector2d(1 - off, 0)); // 1
		points.add(new Vector2d(1, -off)); // 2
		points.add(new Vector2d(1 - off, -off)); // 3
		points.add(new Vector2d(1 - off, 0)); // 4
		points.add(new Vector2d(1, -off)); // 5
		points.add(new Vector2d(1, 1)); // 6
		points.add(new Vector2d(0, 1)); // 7
		points.add(new Vector2d(0, 0)); // 8
		return points;
	}
}

package de.ipk.ag_ba.image.operations;

import java.awt.geom.Rectangle2D;

import org.Vector2d;

public class LargeCluster implements Comparable<LargeCluster> {
	
	private final Vector2d dimension;
	private final Vector2d center;
	private final int size;
	private final int index;
	
	public LargeCluster(Vector2d dimension, Vector2d center, int size, int index) {
		this.dimension = dimension;
		this.center = center;
		this.size = size;
		this.index = index;
	}
	
	@Override
	public int compareTo(LargeCluster o) {
		if (size == o.size)
			return 0;
		return size < o.size ? 1 : -1;
	}
	
	public Integer getIndex() {
		return index;
	}
	
	public boolean intersects(Rectangle2D lc) {
		return lc.intersects(center.x - dimension.x / 2, center.y - dimension.y / 2, dimension.x, dimension.y);
	}
	
	public Rectangle2D getBoundingBox(int increaseSizeBy) {
		return new Rectangle2D.Double(center.x - dimension.x / 2 - increaseSizeBy / 2, center.y - dimension.y / 2 - increaseSizeBy / 2,
				dimension.x + increaseSizeBy, dimension.y + increaseSizeBy);
	}
	
	public double distanceTo(LargeCluster o1) {
		return center.distance(o1.center);
	}
	
}

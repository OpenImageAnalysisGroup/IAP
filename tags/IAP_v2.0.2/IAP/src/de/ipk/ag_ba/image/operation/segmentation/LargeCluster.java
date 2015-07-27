package de.ipk.ag_ba.image.operation.segmentation;

import java.awt.geom.Rectangle2D;

import org.Vector2i;

public class LargeCluster implements Comparable<LargeCluster> {
	
	private Vector2i dimension;
	private final Vector2i center;
	private final int size;
	private final int index;
	
	public LargeCluster(Vector2i dimension, Vector2i center, int size, int index) {
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
	
	public Rectangle2D getBoundingBox(double multiplySizeBy) {
		double increaseSizeXby = dimension.x * multiplySizeBy - dimension.x;
		double increaseSizeYby = dimension.y * multiplySizeBy - dimension.y;
		return new Rectangle2D.Double(center.x - dimension.x / 2 - increaseSizeXby / 2, center.y - dimension.y / 2 - increaseSizeYby / 2,
				dimension.x + increaseSizeXby, dimension.y + increaseSizeYby);
	}
	
	public double distanceTo(LargeCluster o1) {
		return center.distance(o1.center);
	}
	
	public void scaleSizeBy(double factor) {
		dimension = dimension.scale(factor);
	}
	
}

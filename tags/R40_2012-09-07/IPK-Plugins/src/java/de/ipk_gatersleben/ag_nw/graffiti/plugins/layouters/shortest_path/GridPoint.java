package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.shortest_path;

public class GridPoint
					implements Comparable<GridPoint> {
	
	private int x = 0;
	private int y = 0;
	private Integer xy;
	
	public GridPoint(int x, int y) {
		this.x = x;
		this.y = y;
		xy = new Integer(x * y);
	}
	
	public int compareTo(GridPoint a) {
		return xy.compareTo(a.xy);
	}
	
	@Override
	public int hashCode() {
		return xy.hashCode();
	}
	
	@Override
	public String toString() {
		return "[" + x + "/" + y + "]";
	}
	
}

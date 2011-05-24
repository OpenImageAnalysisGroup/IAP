package de.ipk.ag_ba.image.operations.complex_hull;

class Line {
	public Point p0;
	public Point p1;
	
	public Line(Point p0, Point p1) {
		this.p0 = p0;
		this.p1 = p1;
	}
	
	public int getlength() {
		return (int) Math.sqrt(((p0.x-p0.y)*(p0.x-p0.y))+((p1.x-p1.y)*(p1.x-p1.y)));
	}
}

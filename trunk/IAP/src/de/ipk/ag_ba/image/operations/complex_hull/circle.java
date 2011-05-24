package de.ipk.ag_ba.image.operations.complex_hull;

public class circle {

	int x;
	int y;
	int r;
	
	public circle(int x, int y, int r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	public double area() {
		return Math.PI * r * r;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}
	
}

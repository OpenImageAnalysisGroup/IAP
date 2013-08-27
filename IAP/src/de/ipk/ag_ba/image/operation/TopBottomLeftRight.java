package de.ipk.ag_ba.image.operation;

public class TopBottomLeftRight {
	
	private int top;
	private int bottom;
	private int left;
	private int right;
	
	public TopBottomLeftRight(int top, int bottom, int left, int right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}
	
	public int getLeftX() {
		return left;
	}
	
	public int getRightX() {
		return right;
	}
	
	public int getTopY() {
		return top;
	}
	
	public int getBottomY() {
		return bottom;
	}
	
	public void setTop(int top) {
		this.top = top;
	}
	
	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
	
	public void setRight(int right) {
		this.right = right;
	}
	
	public void setLeft(int left) {
		this.left = left;
	}
	
	public double getHeight() {
		return this.bottom - this.top;
	}
	
	public double getWidth() {
		return this.right - this.left;
	}
}

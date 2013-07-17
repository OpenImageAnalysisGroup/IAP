package de.ipk.ag_ba.image.operation;

import org.Vector2d;

public class MarkerPair {
	
	public Vector2d left;
	public Vector2d right;
	
	public MarkerPair(Vector2d l, Vector2d r, int imageWidth) {
		left = l;
		right = r;
		calculateMissingLeftOrRight(imageWidth);
	}
	
	private void calculateMissingLeftOrRight(int imageWidth) {
		if (left == null && right == null)
			throw new UnsupportedOperationException("MarkerPair left AND right is NULL");
		if (left == null) {
			left = new Vector2d(imageWidth - right.x, right.y);
		}
		if (right == null) {
			right = new Vector2d(imageWidth - left.x, left.y);
		}
	}
	
	public Vector2d getLeft() {
		return left;
	}
	
	public void setLeft(Vector2d left) {
		this.left = left;
	}
	
	public Vector2d getRight() {
		return right;
	}
	
	public void setRight(Vector2d right) {
		this.right = right;
	}
	
	public double getMinY() {
		double minY = left != null ? left.y : right.y;
		if (right != null && right.y < minY)
			minY = right.y;
		return minY;
	}
	
	@Override
	public String toString() {
		return "[" + left + " ; " + right + "]";
	}
	
	public void scale(double factorX, double factorY) {
		if (left != null) {
			left.x = left.x * factorX;
			left.y = left.y * factorY;
		}
		if (right != null) {
			right.x = right.x * factorX;
			right.y = right.y * factorY;
		}
	}
	
}

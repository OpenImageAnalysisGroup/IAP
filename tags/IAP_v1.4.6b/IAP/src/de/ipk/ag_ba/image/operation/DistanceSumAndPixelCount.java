package de.ipk.ag_ba.image.operation;

public class DistanceSumAndPixelCount {
	
	private final double dist;
	private final int pixelCount;
	private int angle;
	
	public DistanceSumAndPixelCount(double dist, int pixelCount) {
		this.dist = dist;
		this.pixelCount = pixelCount;
	}
	
	public double getDistanceSum() {
		return dist;
	}
	
	public int getPixelCount() {
		return pixelCount;
	}
	
	public void setAngle(int angle) {
		this.angle = angle;
	}
	
	public int getAngle() {
		return angle;
	}
	
}

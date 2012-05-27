package de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d;

/**
 * @author klukas
 */
public class XYZpointRealDistance {
	
	public double x;
	public double y;
	public double z;
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public XYZpointRealDistance(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * @param angle
	 */
	public void rotateY(double angle, double cos, double sin) {
		// double xn = z*cos-x*sin; // Z-Axis Rot
		// double yn = z*sin+x*cos; // Z-Axis Rot
		// x = xn;
		// y = yn;
		
		// double yn = y*cos-z*sin; // X-Axis Rot
		// double zn = -y*sin+z*cos; // X-Axis Rot
		// y = yn;
		// z = zn;
		
		double zn = z * cos - x * sin; // Y-Axis Rot
		double xn = z * sin + x * cos; // Y-Axis Rot
		z = zn;
		x = xn;
	}
	
	/**
	 * rotate 90 degree, Z-axis is exchanged with Y-axis coordinate
	 */
	public void rotateForTopView() {
		double t = this.y;
		this.y = this.z;
		this.z = t;
	}
	
}

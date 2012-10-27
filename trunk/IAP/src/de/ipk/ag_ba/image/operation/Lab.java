package de.ipk.ag_ba.image.operation;

public class Lab {
	
	double AverageL;
	double AverageA;
	double AverageB;
	
	public Lab(double l, double a, double b) {
		this.AverageL = l;
		this.AverageA = a;
		this.AverageB = b;
	}
	
	public double getAverageL() {
		return AverageL;
	}
	
	public void setAverageL(double averageL) {
		AverageL = averageL;
	}
	
	public double getAverageA() {
		return AverageA;
	}
	
	public void setAverageA(double averageA) {
		AverageA = averageA;
	}
	
	public double getAverageB() {
		return AverageB;
	}
	
	public void setAverageB(double averageB) {
		AverageB = averageB;
	}
}

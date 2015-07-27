package iap.blocks.unused;

import java.awt.color.ColorSpace;

/*
 * W. Burger, M. J. Burge: "Digitale Bildverarbeitung"
 * © Springer-Verlag, 2005
 * www.imagingbook.com
 */

/** Implementation of the CIEL*a*b* color space. */

public class Lab_ColorSpace extends ColorSpace {
	private static final long serialVersionUID = 1L;
	
	// D65 reference illuminant coordinates:
	private final double Xref = 0.95047;
	private final double Yref = 1.00000;
	private final double Zref = 1.08883;
	
	protected Lab_ColorSpace(int type, int numcomponents) {
		super(type, numcomponents);
	}
	
	public Lab_ColorSpace() {
		super(TYPE_Lab, 3);
	}
	
	// XYZ -> CIELab
	@Override
	public float[] fromCIEXYZ(float[] XYZ) {
		double xx = f1(XYZ[0] / Xref);
		double yy = f1(XYZ[1] / Yref);
		double zz = f1(XYZ[2] / Zref);
		
		float L = (float) (116 * yy - 16);
		float a = (float) (500 * (xx - yy));
		float b = (float) (200 * (yy - zz));
		return new float[] { L, a, b };
	}
	
	double f1(double c) {
		if (c > 0.008856)
			return Math.pow(c, 1.0 / 3);
		else
			return (7.787 * c) + (16.0 / 116);
	}
	
	// CIELab -> XYZ
	@Override
	public float[] toCIEXYZ(float[] Lab) {
		double yy = (Lab[0] + 16) / 116;
		float X = (float) (Xref * f2(Lab[1] / 500 + yy));
		float Y = (float) (Yref * f2(yy));
		float Z = (float) (Zref * f2(yy - Lab[2] / 200));
		return new float[] { X, Y, Z };
	}
	
	double f2(double c) {
		double c3 = Math.pow(c, 3.0);
		if (c3 > 0.008856)
			return c3;
		else
			return (c - 16.0 / 116) / 7.787;
	}
	
	// sRGB -> CIELab
	@Override
	public float[] fromRGB(float[] sRGB) {
		ColorSpace sRGBcs = ColorSpace.getInstance(CS_sRGB);
		float[] XYZ = sRGBcs.toCIEXYZ(sRGB);
		return this.fromCIEXYZ(XYZ);
	}
	
	// CIELab -> sRGB
	@Override
	public float[] toRGB(float[] Lab) {
		float[] XYZ = this.toCIEXYZ(Lab);
		ColorSpace sRGBcs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		return sRGBcs.fromCIEXYZ(XYZ);
	}
	
	@Override
	public String getName(int idx) {
		switch (idx) {
			case 0:
				return "L*";
			case 1:
				return "a*";
			case 2:
				return "b*";
			default:
				return "none";
		}
	}
	
}

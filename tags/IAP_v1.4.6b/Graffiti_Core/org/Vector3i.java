/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.10.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org;

/**
 * @author Christian Klukas
 */
public class Vector3i extends Vector2i {
	public int z;
	
	public Vector3i() {
		// empty
	}
	
	public Vector3i(int a, int b) {
		super(a, b);
		z = 0;
	}
	
	public Vector3i(int a, int b, int c) {
		x = a;
		y = b;
		z = c;
	}
	
	@Override
	public String toString() {
		return "Vector3i[" + x + ", " + y + ", " + z + "]";
	}
	
	public Vector3i(Vector3i position) {
		x = position.x;
		y = position.y;
		z = position.z;
	}
	
	public Vector3i(Vector3i p1, Vector3i p2) {
		x = (p1.x + p2.x) / 2;
		y = (p1.y + p2.y) / 2;
		z = (p1.z + p2.z) / 2;
	}
	
	public double distance(Vector3i point) {
		return Math.pow((point.x - x) * (point.x - x) + (point.y - y) * (point.y - y) + (point.z - z) * (point.z - z), 1d / 3d);
	}
	
	public void applyGrid(int xg, int yg, int zg) {
		x = x - (x % xg);
		y = y - (y % yg);
		z = z - (z % yg);
	}
}

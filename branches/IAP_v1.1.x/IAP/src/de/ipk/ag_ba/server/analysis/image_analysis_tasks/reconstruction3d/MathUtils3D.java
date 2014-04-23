/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import de.ipk.ag_ba.image.color.ColorUtil;

/**
 * @author klukas
 */
public class MathUtils3D {
	private static double epsilon = 0.0001;
	
	/**
	 * @param x
	 * @param y
	 * @return Angle in the region of 0..2pi.
	 */
	public static double getAngle(double x, double y) {
		if (Math.abs(y) < epsilon && x > 0)
			return 0;
		if (Math.abs(y) < epsilon && x < 0)
			return Math.PI;
		double angle = Math.atan(y / x);
		
		if (x >= 0 && y >= 0)
			return angle;
		
		if (x < 0)
			return angle + Math.PI;
		
		return Math.PI * 2 + angle;
	}
	
	public Voxel[] scanline3d(Voxel start, Voxel end) {
		Collection<Voxel> result = new LinkedList<Voxel>();
		
		int px = start.x;
		int py = start.y;
		int pz = start.z;
		
		int dx = end.x - start.x;
		int dy = end.y - start.y;
		int dz = end.z - start.z;
		
		int diffX = Math.abs(dx);
		int diffY = Math.abs(dy);
		int diffZ = Math.abs(dz);
		
		int diffX2 = diffX / 2;
		int diffY2 = diffY / 2;
		int diffZ2 = diffZ / 2;
		
		int xStep = (dx < 0 ? -1 : 1);
		int yStep = (dy < 0 ? -1 : 1);
		int zStep = (dz < 0 ? -1 : 1);
		
		if ((diffX >= diffY) & (diffX >= diffZ)) {
			int err_1 = diffY2 - diffX;
			int err_2 = diffZ2 - diffX;
			for (int i = 1; i <= diffX; i++) {
				result.add(new Voxel(px, py, pz));
				if (err_1 > 0) {
					py = py + yStep;
					err_1 = err_1 - diffX2;
				}
				if (err_2 > 0) {
					pz = pz + zStep;
					err_2 = err_2 - diffX2;
				}
				err_1 = err_1 + diffY2;
				err_2 = err_2 + diffZ2;
				px = px + xStep;
			}
		} else
			if ((diffY >= diffX) & (diffY >= diffZ)) {
				int err_1 = diffX2 - diffY;
				int err_2 = diffZ2 - diffY;
				for (int i = 1; i <= diffY; i++) {
					result.add(new Voxel(px, py, pz));
					if (err_1 > 0) {
						px = px + xStep;
						err_1 = err_1 - diffY2;
					}
					if (err_2 > 0) {
						pz = pz + zStep;
						err_2 = err_2 - diffY2;
					}
					err_1 = err_1 + diffX2;
					err_2 = err_2 + diffZ2;
					py = py + yStep;
				}
			} else {
				int err_1 = diffY2 - diffZ;
				int err_2 = diffX2 - diffZ;
				for (int i = 1; i <= diffZ; i++) {
					result.add(new Voxel(px, py, pz));
					if (err_1 > 0) {
						py = py + yStep;
						err_1 = err_1 - diffZ2;
					}
					if (err_2 > 0) {
						px = px + xStep;
						err_2 = err_2 - diffZ2;
					}
					err_1 = err_1 + diffY2;
					err_2 = err_2 + diffX2;
					pz = pz + zStep;
				}
			}
		result.add(new Voxel(px, py, pz));
		return result.toArray(new Voxel[] {});
	}
	
	public static double compareImageParts(MyPicture p1, MyPicture p2, int x1, int y1, int x2, int y2, int wh) {
		wh = wh / 2;
		double diff = 0;
		for (int scanX = -wh; scanX <= wh; scanX++) {
			for (int scanY = -wh; scanY <= wh; scanY++) {
				int c1 = p1.getRGB(x1 + scanX, y1 + scanY);
				int c2 = p2.getRGB(x2 + scanX, y2 + scanY);
				diff += ColorUtil.deltaE2000(new Color(c1), new Color(c2));
			}
		}
		return diff;
	}
	
	@Test
	public void test() {
		assertEquals("angle 4,0 = 0°", 0, getAngle(4, 0), epsilon);
		assertEquals("angle -4,0 = 180°", Math.PI, getAngle(-4, 0), epsilon);
		assertEquals("angle 0,4 = 90°", Math.PI / 2, getAngle(0, 4), epsilon);
		assertEquals("angle 0,-4 = 270°", Math.PI + Math.PI / 2, getAngle(0, -4), epsilon);
		assertEquals("angle -2,4 = 116.565°", 116.565 / 180 * Math.PI, getAngle(-2, 4), epsilon);
		assertEquals("angle -4,-4 = 225°", Math.PI + Math.PI / 4, getAngle(-4, -4), epsilon);
		assertEquals("angle 2,-4 = 296.565°", 296.565 / 180 * Math.PI, getAngle(2, -4), epsilon);
	}
}

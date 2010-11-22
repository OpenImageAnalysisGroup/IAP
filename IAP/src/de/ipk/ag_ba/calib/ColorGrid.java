/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Nov 20, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.calib;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author klukas
 */
public class ColorGrid extends JPanel {
	private static final long serialVersionUID = 1L;

	private final ColorTile[][] tiles;

	public ColorGrid(int nx, int ny) {
		tiles = new ColorTile[nx][ny];
		double as = (150 + 100) / nx;
		double bs = (100 + 150) / ny;
		double a = -150;
		for (int xg = 0; xg < nx; xg++) {
			double b = 100;
			for (int yg = 0; yg < ny; yg++) {
				tiles[xg][yg] = new ColorTile(a, a + as, b, b - bs, xg, yg);
				b -= bs;
			}
			a += as;
		}
		this.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL, nx, ny));
		for (int xg = 0; xg < nx; xg++) {
			for (int yg = 0; yg < ny; yg++) {
				this.add(tiles[xg][yg], xg + "," + yg);
			}
		}
	}

	public static void main(String args[]) {
		ColorGrid cg = new ColorGrid(50, 50);
		cg.setPreferredSize(new Dimension(300, 300));

		JFrame w = new JFrame();
		w.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		w.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		w.add(cg, "0,0");
		w.setSize(300, 200);
		w.setVisible(true);
	}
}

/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Mar 4, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

/**
 * @author klukas
 */
public enum SizeSettingZoom {
	LVIEW, L25, L50, L75, L100, L125, L150, L200, L300, L400, L500;
	
	public String toString() {
		switch (this) {
			case LVIEW:
				return "View Zoom";
			case L25:
				return "25%";
			case L50:
				return "50%";
			case L75:
				return "75%";
			case L100:
				return "100%";
			case L125:
				return "125%";
			case L150:
				return "150%";
			case L200:
				return "200%";
			case L300:
				return "300%";
			case L400:
				return "400%";
			case L500:
				return "500%";
			default:
				return null;
		}
	}
	
	public double getScale(double zoomScaleFromView) {
		switch (this) {
			case LVIEW:
				return zoomScaleFromView;
			case L25:
				return 0.25d;
			case L50:
				return 0.5d;
			case L75:
				return 0.75d;
			case L100:
				return 1d;
			case L125:
				return 1.25d;
			case L150:
				return 1.5d;
			case L200:
				return 2d;
			case L300:
				return 3d;
			case L400:
				return 4d;
			case L500:
				return 5d;
			default:
				return 1;
		}
	}
}

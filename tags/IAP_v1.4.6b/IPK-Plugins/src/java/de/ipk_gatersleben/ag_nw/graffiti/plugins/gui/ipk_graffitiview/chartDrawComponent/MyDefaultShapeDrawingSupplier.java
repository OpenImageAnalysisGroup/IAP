/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.04.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;

public class MyDefaultShapeDrawingSupplier extends DefaultDrawingSupplier
					implements DrawingSupplier {
	
	public MyDefaultShapeDrawingSupplier(float shapeSize) {
		DEFAULT_SHAPE_SEQUENCE = createStandardSeriesShapes(shapeSize);
	}
	
	private static final long serialVersionUID = 1L;
	
}

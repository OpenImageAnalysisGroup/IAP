package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import org.AttributeHelper;
import org.HelperClass;

public class SearchAttributeHelper implements HelperClass {
	
	String oldW = AttributeHelper.getNiceIdFromAttributeId("width");
	String oldH = AttributeHelper.getNiceIdFromAttributeId("height");
	String oldX = AttributeHelper.getNiceIdFromAttributeId("x");
	String oldY = AttributeHelper.getNiceIdFromAttributeId("y");
	String oldSh = AttributeHelper.getNiceIdFromAttributeId("empty_border_width");
	String oldSv = AttributeHelper.getNiceIdFromAttributeId("empty_border_width_vert");
	
	public void prepareSearch() {
		AttributeHelper.setNiceId("width", "Size (Width)");
		AttributeHelper.setNiceId("height", "Size (Height)");
		AttributeHelper.setNiceId("x", "Position (X)");
		AttributeHelper.setNiceId("y", "Position (Y)");
		AttributeHelper.setNiceId("empty_border_width", "Charting : Space (horizontal)");
		AttributeHelper.setNiceId("empty_border_width_vert", "Charting : Space (vertical)");
	}
	
	public void restoreDefintions() {
		AttributeHelper.setNiceId("width", oldW);
		AttributeHelper.setNiceId("height", oldH);
		AttributeHelper.setNiceId("x", oldX);
		AttributeHelper.setNiceId("y", oldY);
		AttributeHelper.setNiceId("empty_border_width", oldSh);
		AttributeHelper.setNiceId("empty_border_width_vert", oldSv);
	}
	
}

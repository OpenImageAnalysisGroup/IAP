package de.ipk.ag_ba.gui.picture_gui;

/**
 * @author Christian Klukas
 */
public enum CameraSelection {
	THIS_CAMERA, ALL_CAMERAS_AND_MEASURMENT_TYPES;
	
	@Override
	public String toString() {
		switch (this) {
			case ALL_CAMERAS_AND_MEASURMENT_TYPES:
				return "All Measurement Types";
			case THIS_CAMERA:
				return "This Measurement Type";
			default:
				return "[Unknown Measurement Selection Setting: " + super.toString() + "]";
		}
	}
}

package de.ipk.ag_ba.gui.picture_gui;

/**
 * @author Christian Klukas
 */
public enum PlantSelection {
	THIS_PLANT, ALL_PLANTS;
	
	@Override
	public String toString() {
		switch (this) {
			case ALL_PLANTS:
				return "All Plants";
			case THIS_PLANT:
				return "This Plant";
			default:
				return "[Unknown Plant Selection Mode: " + super.toString() + "]";
		}
	}
}

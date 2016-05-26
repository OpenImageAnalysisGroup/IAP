package de.ipk.ag_ba.gui.picture_gui;

/**
 * @author Christian Klukas
 */
public enum DaySelection {
	THIS_SNAPSHOT, THIS_DAY, ALL_DAYS, FROM_THIS_DAY, UNTIL_THIS_DAY, UNTIL_THIS_DAY_FLOAT, FROM_THIS_DAY_FLOAT;
	
	@Override
	public String toString() {
		switch (this) {
			case THIS_SNAPSHOT:
				return "This Timepoint";
			case THIS_DAY:
				return "This Day";
			case ALL_DAYS:
				return "All Days";
			case FROM_THIS_DAY:
				return "From This Day";
			case UNTIL_THIS_DAY:
				return "Until This Day";
			case UNTIL_THIS_DAY_FLOAT:
				return "Until This Day (Fine Time)";
			case FROM_THIS_DAY_FLOAT:
				return "From This Day (Fine Time)";
			default:
				return "[Unknown Day Selection Mode: " + super.toString() + "]";
		}
	}
}

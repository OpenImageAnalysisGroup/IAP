package de.ipk.ag_ba.gui.picture_gui;

/**
 * @author Christian Klukas
 */
public enum AnnotionChangeCommand {
	FLAG, UNFLAG, TOGGLE_FLAG, MARK_AS_OUTLIER, REMOVE_OUTLIER_MARK, TOGGLE_OUTLIER_MARK;
	
	@Override
	public String toString() {
		switch (this) {
			case FLAG:
				return "Flag (Select)";
			case MARK_AS_OUTLIER:
				return "Mark as Outlier";
			case REMOVE_OUTLIER_MARK:
				return "Remove Outlier Mark";
			case TOGGLE_FLAG:
				return "Toggle Flag (Invert Selection)";
			case TOGGLE_OUTLIER_MARK:
				return "Toggle Outlier Value";
			case UNFLAG:
				return "Unflag (Deselect)";
			default:
				return "[Unknown Command Mode: " + super.toString() + "]";
		}
	}
	
	public String getAnnotation() {
		switch (this) {
			case MARK_AS_OUTLIER:
			case REMOVE_OUTLIER_MARK:
			case TOGGLE_OUTLIER_MARK:
				return "outlier";
			case FLAG:
			case UNFLAG:
			case TOGGLE_FLAG:
				return "flagged";
			default:
				throw new UnsupportedOperationException("Invalid Command Mode: " + this.toString());
		}
	}
	
	public SetMode getSetMode() {
		switch (this) {
			case MARK_AS_OUTLIER:
			case FLAG:
				return SetMode.SET_VALUE;
			case REMOVE_OUTLIER_MARK:
			case UNFLAG:
				return SetMode.REMOVE_VALUE;
			case TOGGLE_OUTLIER_MARK:
			case TOGGLE_FLAG:
				return SetMode.TOGGLE;
			default:
				throw new UnsupportedOperationException("Invalid Command Mode: " + this.toString());
		}
	}
}

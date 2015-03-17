package de.ipk.ag_ba.gui.navigation_actions;

import javax.swing.JComponent;

import org.ObjectRef;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class SideGuiComponent {
	
	private final JComponent sideGui;
	private final double sideGuiSpace;
	private final double sideGuiWidth;
	private final ObjectRef navButtonRef;
	
	public SideGuiComponent(JComponent sideGui, double sideGuiSpace, double sideGuiWidth, ObjectRef navButtonRef) {
		this.sideGui = sideGui;
		this.sideGuiSpace = sideGuiSpace;
		this.sideGuiWidth = sideGuiWidth;
		this.navButtonRef = navButtonRef;
	}
	
	public JComponent getSideGui() {
		return sideGui;
	}
	
	public double getSideGuiSpace() {
		return sideGuiSpace;
	}
	
	public double getSideGuiWidth() {
		return sideGuiWidth;
	}
	
	public void setButton(NavigationButton navigationButton) {
		navButtonRef.setObject(navigationButton);
	}
	
}

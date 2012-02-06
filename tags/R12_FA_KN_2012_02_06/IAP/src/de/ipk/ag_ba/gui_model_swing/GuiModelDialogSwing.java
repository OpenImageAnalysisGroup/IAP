package de.ipk.ag_ba.gui_model_swing;

import javax.swing.JComponent;

import de.ipk.ag_ba.gui_model.GuiModelDialog;

public class GuiModelDialogSwing extends GuiModelDialog {
	
	private Runnable okActionRunnable;
	
	public void setOKactionCode(Runnable r) {
		this.okActionRunnable = r;
	}
	
	@Override
	public void performOKaction() {
		okActionRunnable.run();
	}
	
	@Override
	public void updateGUI() {
		//
	}
	
	public JComponent getGUI() {
		return null;
	}
}

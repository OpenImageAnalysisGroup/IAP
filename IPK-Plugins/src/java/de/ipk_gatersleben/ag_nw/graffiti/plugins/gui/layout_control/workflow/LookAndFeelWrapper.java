package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import javax.swing.UIManager.LookAndFeelInfo;

public class LookAndFeelWrapper {
	
	private LookAndFeelInfo info;
	private String name;
	
	public LookAndFeelWrapper(LookAndFeelInfo info) {
		this.info = info;
		this.name = info.getName();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isValid() {
		try {
			Class<?> c = Class.forName(info.getClassName());
			if (c != null)// &&!c.getCanonicalName().equals("de.muntjak.tinylookandfeel.TinyLookAndFeel"))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void activateTheme() {
		if (info instanceof ThemedLookAndFeelInfo)
			((ThemedLookAndFeelInfo) info).activateTheme();
	}
	
	// public LookAndFeelInfo getLookAndFeelInfo() {
	// return info;
	// }
	
	public String getClassName() {
		if (info instanceof ThemedLookAndFeelInfo) {
			return info.getClassName();
		} else
			return info.getClassName();
	}
	
}

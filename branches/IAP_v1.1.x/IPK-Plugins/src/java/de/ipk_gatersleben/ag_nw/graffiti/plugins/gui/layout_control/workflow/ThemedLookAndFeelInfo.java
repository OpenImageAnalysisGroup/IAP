package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import javax.swing.UIManager.LookAndFeelInfo;

import de.muntjak.tinylookandfeel.Theme;
import de.muntjak.tinylookandfeel.ThemeDescription;

public class ThemedLookAndFeelInfo extends LookAndFeelInfo {
	
	private static String activeThemeName;
	private String themename;
	
	public ThemedLookAndFeelInfo(String arg0, String arg1, String theme) {
		super(arg0, arg1);
		this.themename = theme;
		Theme.loadTheme(Theme.getThemeDescription(theme));
	}
	
	public void activateTheme() {
		loadTheme(themename);
	}
	
	public static void loadTheme(String themename) {
		ThemeDescription td = Theme.getThemeDescription(themename);
		if (td != null)
			Theme.loadTheme(td);
		
		activeThemeName = td.getName();
	}
	
	public String getThemeName() {
		return themename;
	}
	
	public static String getActiveTheme() {
		return activeThemeName;
	}
	
}

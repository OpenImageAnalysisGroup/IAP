package de.ipk.ag_ba.gui.navigation_model;

import info.clearthought.layout.TableLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.ipk.ag_ba.gui.enums.ButtonDrawStyle;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;

/**
 * @author klukas
 */
public final class DBEexperimentNavigation extends NavigationButton {
	/**
	 * 
	 */
	private final JTextField login;
	/**
	 * 
	 */
	private final boolean publicLogin;
	/**
	 * 
	 */
	private final JTextField pass;
	
	/**
	 * @param navigationAction
	 * @param title
	 * @param image
	 * @param login
	 * @param publicLogin
	 * @param pass
	 * @param guiSettings
	 */
	public DBEexperimentNavigation(NavigationAction navigationAction, String title, String image, JTextField login,
						boolean publicLogin, JTextField pass, GUIsetting guiSettings) {
		super(navigationAction, title, image, guiSettings);
		this.login = login;
		this.publicLogin = publicLogin;
		this.pass = pass;
	}
	
	@Override
	public void setButtonStyle(ButtonDrawStyle style) {
		if (publicLogin)
			return;
		if (style == ButtonDrawStyle.TEXT) {
			JComponent loginFields = TableLayout.get3Split(login, null, pass, TableLayout.PREFERRED, 2,
								TableLayout.PREFERRED, 0, 0);
			setSideGUI(loginFields, 5, TableLayout.PREFERRED);
		} else {
			JComponent loginFields = TableLayout.get3SplitVertical(TableLayout.get4SplitVertical(null, new JLabel(
								"User/Password:"), null, login, 4, TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 0, 0), null, pass,
								TableLayout.PREFERRED, 1, TableLayout.PREFERRED);
			setSideGUI(loginFields, 5, 100);
		}
	}
}
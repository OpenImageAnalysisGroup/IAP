/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.HelperClass;
import org.graffiti.editor.dialog.DefaultParameterDialog;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

public class MyInputHelper implements HelperClass {
	/**
	 * @param description
	 *           In case the description is of type JComponent, this GUI element
	 *           will be shown at the top of the dialog. If this parameter is of
	 *           type String, a <code>JLabel</code> object will show the provided
	 *           text. If the description text starts with "[OK]", only the OK
	 *           and not the Cancel button will be shown. If the description text
	 *           starts with "[]", no button will be shown. If the description
	 *           starts with "[Hello]", the single OK Button will be titled
	 *           "Hello". If the description starts with [Yes;No], two buttons,
	 *           titles 'Yes' and 'No' will be shown.
	 * @param title
	 *           The shown dialog window will use this value as its window title.
	 * @param parameters
	 * @return The return value depends on the selected button (OK/Cancel).
	 */
	public static Object[] getInput(final Object description, final String title, final Object... parameters) {
		if (!SwingUtilities.isEventDispatchThread()) {
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setBval(0, false); // finished ?
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Object[] res = DefaultParameterDialog.getInput(description, title, parameters);
					tso.setParam(0, res);
					tso.setBval(0, true); // finished !
				}
			});
			while (!tso.getBval(0, false)) { // finished ?
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			// finished !
			Object[] res = (Object[]) tso.getParam(0, null);
			return res;
		} else
			return DefaultParameterDialog.getInput(description, title, parameters);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<ArrayList<Object>> getMultipleInput(Object... desc_title_and_params) {
		if (desc_title_and_params.length % 3 != 0)
			throw new RuntimeException(
								"Parameter count needs to be multiple of 3! (description, title, params[], description, title, params[], ...)");
		ArrayList<ArrayList<Object>> res = new ArrayList<ArrayList<Object>>();
		int max = desc_title_and_params.length / 3;
		for (int i = 0; i < max; i++) {
			Object desc = desc_title_and_params[i * 3];
			String titl = (String) desc_title_and_params[i * 3 + 1];
			ArrayList<Object> pa = (ArrayList<Object>) desc_title_and_params[i * 3 + 2];
			String buttons = "";
			// if (i == 0 && max > 1)
			// buttons = "[>>>;Cancel]";
			// else if (i > 0 && i + 1 < max)
			// buttons = "[>>>;<<<]";
			// else
			// buttons = "[OK;Cancel]";
			
			Object[] r = getInput(buttons + desc, "Step " + (i + 1) + "/" + max + ": " + titl, pa.toArray());
			if (r == null) {
				res = null;
				break;
			} else {
				ArrayList<Object> al = new ArrayList<Object>();
				for (Object o : r)
					al.add(o);
				res.add(al);
			}
		}
		return res;
	}
}
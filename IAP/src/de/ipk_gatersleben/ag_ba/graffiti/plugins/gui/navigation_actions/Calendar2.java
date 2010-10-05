/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 9, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.enums.ButtonDrawStyle;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.StyleAware;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;

/**
 * @author klukas
 * 
 */
public class Calendar2 extends NavigationGraphicalEntity implements StyleAware {
	private final GregorianCalendar cal = new GregorianCalendar();
	private Runnable postUpdateRunner;
	private boolean showSpecificDay = true;
	private ButtonDrawStyle style;

	public Calendar2(String title, String image, NavigationAction navigationAction) {
		super(navigationAction, title, image);
	}

	public void setButtonStyle(ButtonDrawStyle style) {
		this.style = style;
	}

	public GregorianCalendar getCalendar() {
		return cal;
	}

	private static SimpleDateFormat sdfDaySpecific = new SimpleDateFormat("MM/dd/yyyy");
	private static SimpleDateFormat sdfMonthSpecific = new SimpleDateFormat("MMM yyyy");

	@Override
	public String getTitle() {
		if (style == null || style != ButtonDrawStyle.TEXT)
			return super.getTitle();

		String title = super.getTitle();

		if (isShowSpecificDay()) {
			String date = sdfDaySpecific.format(cal.getTime());
			title = title + " (" + date + ")";
		} else {
			String date = sdfMonthSpecific.format(cal.getTime());
			title = title + " (" + date + ")";
		}
		return title;
	}

	public void setPostUpdateRunner(Runnable r) {
		this.postUpdateRunner = r;
	}

	public void updateGUI() {
		if (postUpdateRunner != null)
			postUpdateRunner.run();
	}

	public boolean isShowSpecificDay() {
		return showSpecificDay;
	}

	public void setShowSpecificDay(boolean showSpecificDay) {
		this.showSpecificDay = showSpecificDay;
	}
}

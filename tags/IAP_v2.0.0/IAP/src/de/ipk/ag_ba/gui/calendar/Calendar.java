/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 8, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.calendar;

import info.clearthought.layout.TableLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.SystemAnalysis;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class Calendar extends JComponent {
	private static final long serialVersionUID = 1L;
	GregorianCalendar cal;
	private final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei;
	private final NavigationButtonCalendar2 calEnt;
	
	ArrayList<DayComponent> days;
	
	public Calendar(TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, NavigationButtonCalendar2 action) {
		setOpaque(false);
		calEnt = action;
		cal = action.getCalendar();
		
		this.group2ei = group2ei;
		GregorianCalendar gc = new GregorianCalendar();
		boolean virtual = SystemAnalysis.isHeadless();
		updateGUI(sameDay(gc, cal), virtual);
	}
	
	public void updateGUI(boolean doMark, boolean virtual) {
		removeAll();
		days = new ArrayList<DayComponent>();
		
		days.add(new DayComponent("Sunday"));
		days.add(new DayComponent("Monday"));
		days.add(new DayComponent("Tuesday"));
		days.add(new DayComponent("Wednesday"));
		days.add(new DayComponent("Thursday"));
		days.add(new DayComponent("Friday"));
		days.add(new DayComponent("Saturday"));
		
		GregorianCalendar today = cal;
		
		int firstDay = new GregorianCalendar(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH), 1)
							.get(GregorianCalendar.DAY_OF_WEEK);
		GregorianCalendar pcal = new GregorianCalendar(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH),
							1);
		pcal.add(GregorianCalendar.DAY_OF_MONTH, -firstDay + 1);
		if (firstDay == 2 || firstDay == 1)
			pcal.add(GregorianCalendar.DAY_OF_MONTH, -7);
		while (pcal.get(GregorianCalendar.MONTH) != cal.get(GregorianCalendar.MONTH)) {
			days.add(new DayComponent(group2ei, false, false, calCopy(pcal), calEnt));
			pcal.add(GregorianCalendar.DAY_OF_MONTH, 1);
		}
		
		pcal = new GregorianCalendar(cal.get(GregorianCalendar.YEAR), cal.get(GregorianCalendar.MONTH), 1);
		while (days.size() < 7 * 7) {
			days.add(new DayComponent(group2ei, cal.get(GregorianCalendar.MONTH) == pcal.get(GregorianCalendar.MONTH),
								doMark && sameDay(today, pcal), calCopy(pcal), calEnt));
			pcal.add(GregorianCalendar.DAY_OF_MONTH, +1);
		}
		
		if (!virtual) {
			JPanel jp = new JPanel();
			jp.setLayout(new TableLayout(new double[][] {
							{ TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL,
												TableLayout.FILL, TableLayout.FILL },
							{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL,
												TableLayout.FILL, TableLayout.FILL } }));
			int row = 0;
			while (days.size() > 0) {
				for (int day = 0; day < 7 && days.size() > 0; day++) {
					jp.add(days.get(0), day + "," + row);
					days.remove(0);
				}
				row++;
			}
			
			jp.setOpaque(false);
			
			setLayout(TableLayout.getLayout(TableLayout.FILL, new double[] { TableLayout.FILL }));
			add(jp, "0,0");
			
			validate();
		}
		calEnt.updateGUI();
	}
	
	private GregorianCalendar calCopy(GregorianCalendar pcal) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(pcal.getTime());
		return c;
	}
	
	/**
	 * @param cal2
	 * @param pcal
	 * @return
	 */
	private boolean sameDay(GregorianCalendar a, GregorianCalendar b) {
		SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");
		String dateA = sdf.format(a.getTime());
		String dateB = sdf.format(b.getTime());
		
		return dateA.equals(dateB);
	}
	
	public GregorianCalendar getCalendar() {
		return cal;
	}
	
	public ArrayList<DayComponent> getCalendarEntries() {
		ArrayList<DayComponent> result = new ArrayList<DayComponent>(days);
		return result;
	}
	
	// private JComponent getNavBar(final Calendar calendar) {
	// JButton left = new JButton();
	// left.setIcon(FolderPanel.getLeftRightIcon(Iconsize.MIDDLE, true));
	// JButton right = new JButton();
	// right.setIcon(FolderPanel.getLeftRightIcon(Iconsize.MIDDLE, false));
	// left.addActionListen8er(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// cal.add(GregorianCalendar.MONTH, -1);
	// calendar.updateGUI(false);
	// }
	// });
	// right.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// cal.add(GregorianCalendar.MONTH, 1);
	// calendar.updateGUI(false);
	// }
	// });
	// JComponent nav = TableLayout.getSplit(left, right, TableLayout.PREFERRED,
	// TableLayout.PREFERRED);
	// int b = 4;
	// nav.setBorder(BorderFactory.createEmptyBorder(b, b, b, b));
	//
	// SimpleDateFormat sdf = new SimpleDateFormat("MMMMMMMMM yyyy");
	// String date = sdf.format(cal.getTime());
	// return TableLayout.getSplit(new
	// JLabel("<html><h3>&nbsp;&nbsp;&nbsp;"+date), nav, TableLayout.FILL,
	// TableLayout.PREFERRED);
	// }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		ArrayList<DayComponent> days = getCalendarEntries();
		int row = 0;
		String m = DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(cal.getTime());
		sb.append("<table width=\"1000px\" height=\"400px\"><tr><td colspan=\"7\"><center><b><large>" + m
				+ "</large></b></center></td></tr>");
		while (days.size() > 0) {
			sb.append("<tr>");
			for (int day = 0; day < 7 && days.size() > 0; day++) {
				sb.append(days.get(0).getAsHTML());
				days.remove(0);
			}
			sb.append("</tr>");
			row++;
		}
		sb.append("</table>");
		return sb.toString();
	}
}

/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 8, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.calendar;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.Colors;
import org.StringManipulationTools;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.Calendar2;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class DayComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	private final Color color;
	private Color color2;
	static double colorInt = 0.25;
	private ArrayList<Color> colors = Colors.get(12, colorInt);
	boolean compact = false;
	private GregorianCalendar calendar;
	private Calendar2 calEnt;

	private static ArrayList<Color> userColors = Colors.get(20, colorInt * 1.3);

	public DayComponent(TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, boolean main,
			boolean mark, GregorianCalendar calendar, Calendar2 calEnt) {
		this.calEnt = calEnt;
		addMouseListener(getMouseListener());
		setLayout(TableLayout.getLayout(TableLayout.FILL, new double[] { TableLayout.FILL, TableLayout.PREFERRED }));
		this.calendar = calendar;
		int month = calendar.get(GregorianCalendar.MONTH);
		int dayOfMonth = calendar.get(GregorianCalendar.DAY_OF_MONTH);
		int dayOfWeek = calendar.get(GregorianCalendar.DAY_OF_WEEK);
		if (!main) {
			colors = Colors.get(12, colorInt / 2);
		}
		if (mark)
			colors = Colors.get(12, colorInt * 2);
		else if (main && (dayOfWeek == 1 || dayOfWeek == 7)) {
			// main = false;
			colors = Colors.get(12, colorInt * 0.8);
		}
		color = colors.get(month);
		color2 = color.darker();
		color2 = Colors.getColor((float) 0.4, 1, color, color2);
		add(center(main, new JLabel("" + dayOfMonth)), "0,0");
		ArrayList<JComponent> reservations = new ArrayList<JComponent>();
		SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");
		String date = sdf.format(calendar.getTime());
		TreeMap<String, ArrayList<String>> exp = new TreeMap<String, ArrayList<String>>();
		// TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>>
		TreeSet<ExperimentHeaderInterface> experiments = new TreeSet<ExperimentHeaderInterface>();
		for (String s : group2ei.keySet())
			for (String s2 : group2ei.get(s).keySet())
				for (ExperimentHeaderInterface ehi : group2ei.get(s).get(s2))
					experiments.add(ehi);
		for (ExperimentHeaderInterface ei : experiments) {
			if (ei.getStartdate() == null)
				continue;
			String dateA = sdf.format(ei.getStartdate().getTime());
			String dateB = sdf.format(ei.getImportdate().getTime());
			boolean startOrEnd = false;
			if (date.equals(dateA)) {
				if (!exp.containsKey(ei.getImportusername()))
					exp.put(ei.getImportusername(), new ArrayList<String>());
				exp.get(ei.getImportusername()).add("experiment start: " + ei.getExperimentname());
				startOrEnd = true;
			}
			if (date.equals(dateB)) {
				if (!exp.containsKey(ei.getImportusername()))
					exp.put(ei.getImportusername(), new ArrayList<String>());
				exp.get(ei.getImportusername()).add("experiment upload: " + ei.getExperimentname());
				startOrEnd = true;
			}
			if (!startOrEnd) {
				if (calendar.getTime().after(ei.getStartdate()) && calendar.getTime().before(ei.getImportdate())) {
					if (!exp.containsKey(ei.getImportusername()))
						exp.put(ei.getImportusername(), new ArrayList<String>());
					exp.get(ei.getImportusername()).add("experiment running: " + ei.getExperimentname());
				}
			}
		}
		for (String user : exp.keySet()) {
			int n = exp.get(user).size();
			String s = user;

			if (n > 1) {
				s += " (" + n + ")";
			} else {
				if (exp.get(user).iterator().next().indexOf("upload") >= 0)
					s = "finished";
				else
					s = exp.get(user).iterator().next().split(":", 2)[1];
			}
			JLabel lbl = new JLabel(s);
			lbl.setToolTipText("<html>" + user + ": " + StringManipulationTools.getStringList(exp.get(user), "<br>"));
			lbl.addMouseListener(getMouseListener());
			reservations.add(center2(main, lbl, 1, 1, userColors.get(Math.abs(user.hashCode()) % userColors.size())));
		}

		add(TableLayout.getMultiSplitVertical(reservations), "0,1");
		// setBorder(BorderFactory.createRaisedBevelBorder());
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, color2.darker()));
		validate();
	}

	/**
	 * @return
	 */
	private MouseListener getMouseListener() {
		MouseListener m = new MouseListener() {
			Border oldBorder = null;

			@Override
			public void mouseReleased(MouseEvent e) {
				setBorder(BorderFactory.createRaisedBevelBorder());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				setBorder(BorderFactory.createLoweredBevelBorder());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (oldBorder != null)
					setBorder(oldBorder);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (oldBorder == null)
					oldBorder = getBorder();
				setBorder(BorderFactory.createRaisedBevelBorder());
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				calEnt.setShowSpecificDay(true);
				calEnt.getCalendar().set(calendar.get(GregorianCalendar.YEAR), calendar.get(GregorianCalendar.MONTH),
						calendar.get(GregorianCalendar.DAY_OF_MONTH));
				calEnt.updateGUI();
				calEnt.performAction();
				// calendarGUI.cal.set(
				// calendar.get(GregorianCalendar.YEAR),
				// calendar.get(GregorianCalendar.MONTH),
				// calendar.get(GregorianCalendar.DAY_OF_MONTH));
				// calendarGUI.updateGUI(true);
			}
		};
		return m;
	}

	public DayComponent(String day) {
		compact = true;
		color = Color.LIGHT_GRAY.brighter();
		color2 = color.darker();
		color2 = Colors.getColor((float) 0.4, 1, color, color2);
		setLayout(TableLayout.getLayout(TableLayout.FILL, new double[] { TableLayout.FILL, TableLayout.PREFERRED }));
		add(center(true, new JLabel(day)), "0,0");
		setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
		validate();
	}

	@Override
	protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		int w = getWidth();
		int h = getHeight();

		// Paint a gradient from top to bottom
		GradientPaint gp = new GradientPaint(0, 0, color, compact ? 0 : (int) (h / 8 * 0), h, compact ? color : color2);

		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
	}

	private JComponent center(boolean main, JLabel jLabel) {
		jLabel.setHorizontalAlignment(JLabel.CENTER);
		jLabel.setFont(new Font(jLabel.getFont().getName(), Font.PLAIN, compact ? 15 : 20));
		jLabel.setOpaque(false);
		if (!main)
			jLabel.setForeground(Color.GRAY);
		// jLabel.setBackground(c);
		// jLabel.setForeground(c);
		return jLabel;
	}

	private JComponent center2(boolean main, JLabel jLabel, int i, int max, Color color3) {
		jLabel.setHorizontalAlignment(JLabel.CENTER);
		jLabel.setFont(new Font(jLabel.getFont().getName(), Font.PLAIN, compact ? 11 : 11));
		jLabel.setOpaque(true);
		// ArrayList<Color> colors1 = Colors.get(12, main ? 0.4 : 0.1);
		// ArrayList<Color> colors2 = Colors.get(12, main ? 0.6 : 0.2);
		// Color color1 = colors1.get(calendar.get(GregorianCalendar.MONTH));
		// Color color2 = colors2.get(calendar.get(GregorianCalendar.MONTH));
		if (!main)
			jLabel.setForeground(Color.GRAY);
		else
			jLabel.setForeground(Color.BLACK);
		// jLabel.setBackground(Colors.getColor((float)i/(float)max, 1, color1,
		// color3));
		if (main)
			jLabel.setBackground(new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), 60));
		else
			jLabel.setBackground(new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), 20));
		// jLabel.setForeground(c);
		return jLabel;
	}

}

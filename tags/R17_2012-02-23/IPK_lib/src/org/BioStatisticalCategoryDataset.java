/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 02.11.2004 by Christian Klukas
 */
package org;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.jfree.data.RangeInfo;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class BioStatisticalCategoryDataset
					extends DefaultStatisticalCategoryDataset
					implements RangeInfo {

	private static final long serialVersionUID = 1L;
	private HashSet<String> units = new LinkedHashSet<String>(1);
	private HashSet<String> timeUnits = new LinkedHashSet<String>(1);
	private float ttestMarkCircleSize;

	private double lboundVal = Double.MAX_VALUE;
	private double uboundVal = Double.NEGATIVE_INFINITY;

	public BioStatisticalCategoryDataset(float ttestMarkCircleSize) {
		this.ttestMarkCircleSize = ttestMarkCircleSize;
	}

	private HashMap<Comparable, Color> rowDesc2paint1 = new HashMap<Comparable, Color>();
	private HashMap<Comparable, Color> rowDesc2paint2 = new HashMap<Comparable, Color>();

	public void add(final double mean, final double standardDeviation,
						final Comparable rowKey, final Comparable columnKey,
						boolean ttestIsRef, boolean ttestIsDiff,
						String unit, String timeunit, boolean datasetWillBeShownWithStdDev,
						Color color1, Color color2, boolean showOnlyHalfErrorBar) {
		add(mean, standardDeviation,
							rowKey, columnKey,
							ttestIsRef, ttestIsDiff,
							unit, timeunit, datasetWillBeShownWithStdDev, showOnlyHalfErrorBar);
		// System.out.println("ADD: "+rowKey+" COLOR: "+color1);
		if (color1 != null && !rowDesc2paint1.containsKey(rowKey))
			rowDesc2paint1.put(rowKey, color1);
		if (color2 != null && !rowDesc2paint2.containsKey(rowKey))
			rowDesc2paint2.put(rowKey, color2);
	}

	/**
	 * Adds a mean, a standard deviation and the info about the t test to the table.
	 * 
	 * @param mean
	 *           the mean.
	 * @param standardDeviation
	 *           the standard deviation.
	 * @param rowKey
	 *           the row key.
	 * @param columnKey
	 *           the column key.
	 * @param ttestIsRef
	 *           True, if this is the reference, False if not
	 * @param ttestIsDiff
	 *           True, if the t test is done and H1 found to be probable,
	 *           false if H0 is probable
	 * @param unit
	 *           The unit of the measurement e.g. mm
	 * @param timeunit
	 *           The time unit of the measurement e.g. day
	 */
	public void add(final double mean, final double standardDeviation,
							final Comparable rowKey, final Comparable columnKey,
							boolean ttestIsRef, boolean ttestIsDiff,
							String unit, String timeunit, boolean datasetWillBeShownWithStdDev,
							boolean showOnlyHalfErrorBar) {

		final BioMeanAndStandardDeviation item =
							new BioMeanAndStandardDeviation(
												new Double(mean),
												new Double(standardDeviation),
												ttestIsRef, ttestIsDiff);
		addDataObject(item, rowKey, columnKey);

		// if the standard deviation will be shown, it must be considered
		// while determining the minimum and maximum values,
		// if not, it should not affect the minimum and maximum calculation (0).
		if (datasetWillBeShownWithStdDev)
			addStep2(mean, standardDeviation, rowKey, columnKey, showOnlyHalfErrorBar);
		else
			addStep2(mean, 0d, rowKey, columnKey, showOnlyHalfErrorBar);

		if (!timeUnits.contains(timeunit))
			timeUnits.add(timeunit);
		if (!units.contains(unit))
			units.add(unit);
		// if (!Double.isInfinite(mean) && !Double.isInfinite(standardDeviation)
		// && !Double.isNaN(mean) && !Double.isNaN(standardDeviation)) {
		// if (mean-standardDeviation < lboundVal)
		// lboundVal = mean-standardDeviation;
		// if (mean+standardDeviation > uboundVal)
		// uboundVal = mean+standardDeviation;
		// }
	}

	/**
	 * Returns the true, if this is the reference sample or if no calculation is
	 * done. False, if it is no rerence sample. The case that no calculation is
	 * done can be recognized be calling <code>getTTestIsH1</code>, if this is also
	 * true, then no calculation is done (true, true : would make no sense).
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return True, if reference or no calculation is done for this sample/value.
	 */
	public boolean getTTestIsRef(final int row, final int column) {
		// Number result = null;
		final BioMeanAndStandardDeviation masd = (BioMeanAndStandardDeviation) this.data.getObject(row, column);
		if (masd != null) {
			return masd.isReference;
		}
		return true;
	}

	/**
	 * Returns the true, if this is the reference sample or if no calculation is
	 * done. False, if it is no rerence sample. The case that no calculation is
	 * done can be recognized be calling <code>getTTestIsH1</code>, if this is also
	 * true, then no calculation is done (true, true : would make no sense).
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return True, if H1 is probable, False if H0 is probable,
	 *         also True, if no calculation is done.
	 */
	public boolean getTTestIsH1(final int row, final int column) {
		// Number result = null;
		final BioMeanAndStandardDeviation masd = (BioMeanAndStandardDeviation) this.data.getObject(row, column);
		if (masd != null) {
			return masd.isSignificantDifferent;
		}
		return true;
	}

	public float getTtestMarkCircleSize() {
		return ttestMarkCircleSize;
	}

	public String getRangeUnits() {
		return getStringList(units, ",");
	}

	public String getDomainUnits() {
		return getStringList(timeUnits, ",");
	}

	public Number getMaximumRangeValue() {
		return uboundVal;
	}

	public Number getMinimumRangeValue() {
		return lboundVal;
	}

	private String getStringList(Collection<String> col, String div) {
		StringBuilder sb = new StringBuilder();
		for (Iterator it = col.iterator(); it.hasNext();) {
			sb.append((String) it.next());
			if (it.hasNext())
				sb.append(div);
		}
		return sb.toString();
	}

	public Color getColor1ForRowKey(Comparable rowKey) {
		return rowDesc2paint1.get(rowKey);
	}

	public Color getColor2ForRowKey(Comparable rowKey) {
		return rowDesc2paint2.get(rowKey);
	}
}

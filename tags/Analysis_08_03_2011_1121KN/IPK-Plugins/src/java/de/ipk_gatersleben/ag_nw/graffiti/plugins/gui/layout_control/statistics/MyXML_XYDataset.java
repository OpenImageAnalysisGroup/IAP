/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 01.10.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ErrorMsg;
import org.apache.commons.collections.SequencedHashMap;
import org.jfree.data.AbstractXYDataset;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
@SuppressWarnings("deprecation")
public class MyXML_XYDataset extends AbstractXYDataset {
	
	private static final long serialVersionUID = 1L;
	
	private boolean ranksCalculated = false;
	private boolean calcMerge = false;
	private int calcOffset = Integer.MAX_VALUE;
	private HashMap<String, MyDouble> ranksX, ranksY;
	
	/**
	 * Contains ArrayLists, which contain the data for a particular series.
	 */
	@SuppressWarnings("unchecked")
	private final Map<String, ArrayList<MyComparableDataPoint>> seriesData1 = new SequencedHashMap();
	
	@SuppressWarnings("unchecked")
	private final Map<String, ArrayList<MyComparableDataPoint>> seriesData2 = new SequencedHashMap();
	
	/*
	 * (non-Javadoc)
	 * @see org.jfree.data.SeriesDataset#getSeriesCount()
	 */
	@Override
	public int getSeriesCount() {
		if (seriesData1.size() < seriesData2.size())
			return seriesData1.size();
		else
			return seriesData2.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jfree.data.SeriesDataset#getSeriesName(int)
	 */
	@Override
	public String getSeriesName(int series) {
		String s1 = (String) seriesData1.keySet().toArray()[series];
		String s2 = (String) seriesData2.keySet().toArray()[series];
		if (!s1.equals(s2)) {
			if (seriesData2.get(s1) == null)
				seriesData1.remove(s1);
			if (seriesData1.get(s2) == null)
				seriesData2.remove(s2);
			s1 = (String) seriesData1.keySet().toArray()[series];
		}
		return s1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jfree.data.XYDataset#getItemCount(int)
	 */
	public int getItemCount(int series) {
		ArrayList<MyComparableDataPoint> al1 = seriesData1.get(getSeriesName(series));
		ArrayList<MyComparableDataPoint> al2 = seriesData2.get(getSeriesName(series));
		if (al1 == null || al2 == null)
			return 0;
		int c1 = al1.size();
		int c2 = al2.size();
		if (c1 > c2)
			return c2;
		else
			return c1; // return smallest value
	}
	
	@Override
	public String toString() {
		return "Dataset: " + getSeriesCount() + " series";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jfree.data.XYDataset#getXValue(int, int)
	 */
	public Number getXValue(int series, int item) {
		String seriesName = getSeriesName(series);
		ArrayList<MyComparableDataPoint> serData = seriesData1.get(seriesName);
		try {
			MyComparableDataPoint mcdp = serData.get(item);
			return new Double(mcdp.mean);
		} catch (IndexOutOfBoundsException ioobe) {
			return null;
		}
	}
	
	public MyComparableDataPoint getXsrcValue(int series, int item) {
		String seriesName = getSeriesName(series);
		ArrayList<MyComparableDataPoint> serData = seriesData1.get(seriesName);
		try {
			MyComparableDataPoint mcdp = serData.get(item);
			return mcdp;
		} catch (IndexOutOfBoundsException ioobe) {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jfree.data.XYDataset#getYValue(int, int)
	 */
	public Number getYValue(int series, int item) {
		MyComparableDataPoint mcdp = (MyComparableDataPoint) ((ArrayList<?>) seriesData2.get(getSeriesName(series)))
							.get(item);
		return new Double(mcdp.mean);
	}
	
	public MyComparableDataPoint getYsrcValue(int series, int item) {
		MyComparableDataPoint mcdp = (MyComparableDataPoint) ((ArrayList<?>) seriesData2.get(getSeriesName(series)))
							.get(item);
		return mcdp;
	}
	
	/**
	 * Adds the corresponding data points from the specified mapping data.
	 * Pairwise replicate and series names need to correspond
	 */
	public void addXmlDataSeries(SubstanceInterface xmldata1, SubstanceInterface xmldata2, String seriesDescription,
						boolean useSampleAverageValues) {
		ranksCalculated = false;
		calcOffset = Integer.MAX_VALUE;
		List<MyComparableDataPoint> ss1, ss2;
		if (useSampleAverageValues) {
			ss1 = NodeTools.getSortedAverageDataSetValues(xmldata1);
			ss2 = NodeTools.getSortedAverageDataSetValues(xmldata2);
		} else {
			ss1 = NodeTools.getSortedDataSetValues(xmldata1);
			ss2 = NodeTools.getSortedDataSetValues(xmldata2);
		}
		ArrayList<MyComparableDataPoint> workQueue1 = new ArrayList<MyComparableDataPoint>();
		workQueue1.addAll(ss1);
		ArrayList<MyComparableDataPoint> workQueue2 = new ArrayList<MyComparableDataPoint>();
		workQueue2.addAll(ss2);
		while (workQueue1.size() > 0 && workQueue2.size() > 0) {
			MyComparableDataPoint mcdp1 = workQueue1.remove(0);
			String sd1 = getSeriesDesc(mcdp1, seriesDescription);
			int rep1 = mcdp1.replicate;
			// search for corresponding measurement value, with the same replicate
			// number
			MyComparableDataPoint mcdp2 = null;
			String sd2 = "";
			for (int i = 0; i < workQueue2.size(); i++) {
				MyComparableDataPoint testMCDP = workQueue2.get(i);
				sd2 = getSeriesDesc(testMCDP, seriesDescription);
				if ((useSampleAverageValues || testMCDP.replicate == rep1) && mcdp1.serie.equalsIgnoreCase(testMCDP.serie)
									&& mcdp1.timeValueForComparision == testMCDP.timeValueForComparision) {
					mcdp2 = testMCDP;
					break;
				}
			}
			if (mcdp2 == null)
				continue;
			workQueue2.remove(mcdp2);
			ArrayList<MyComparableDataPoint> serDataList1 = seriesData1.get(getSeriesDesc(mcdp1, seriesDescription));
			ArrayList<MyComparableDataPoint> serDataList2 = seriesData2.get(getSeriesDesc(mcdp2, seriesDescription));
			if (serDataList1 == null) {
				serDataList1 = new ArrayList<MyComparableDataPoint>();
				seriesData1.put(sd1, serDataList1);
			}
			if (serDataList2 == null) {
				serDataList2 = new ArrayList<MyComparableDataPoint>();
				seriesData2.put(sd2, serDataList2);
			}
			serDataList1.add(mcdp1);
			serDataList2.add(mcdp2);
		}
	}
	
	public void addXmlDataSeriesXY(SubstanceInterface xmlSeries1, SubstanceInterface xmlSeries2, String newSeriesName,
						boolean useSampleAverageValues) {
		ranksCalculated = false;
		calcOffset = Integer.MAX_VALUE;
		List<MyComparableDataPoint> ss1, ss2;
		if (useSampleAverageValues) {
			ss1 = NodeTools.getSortedAverageDataSetValues(xmlSeries1);
			ss2 = NodeTools.getSortedAverageDataSetValues(xmlSeries2);
		} else {
			ss1 = NodeTools.getSortedDataSetValues(xmlSeries1);
			ss2 = NodeTools.getSortedDataSetValues(xmlSeries2);
		}
		ArrayList<MyComparableDataPoint> workQueue1 = new ArrayList<MyComparableDataPoint>();
		workQueue1.addAll(ss1);
		ArrayList<MyComparableDataPoint> workQueue2 = new ArrayList<MyComparableDataPoint>();
		workQueue2.addAll(ss2);
		while (workQueue1.size() > 0 && workQueue2.size() > 0) {
			MyComparableDataPoint mcdp1 = workQueue1.remove(0);
			int rep1 = mcdp1.replicate;
			// search for corresponding measurement value, with the same replicate
			// number
			MyComparableDataPoint mcdp2 = null;
			for (int i = 0; i < workQueue2.size(); i++) {
				MyComparableDataPoint testMCDP = workQueue2.get(i);
				if ((useSampleAverageValues || testMCDP.replicate == rep1)
									&& mcdp1.timeValueForComparision == testMCDP.timeValueForComparision) {
					mcdp2 = testMCDP;
					break;
				}
			}
			if (mcdp2 == null)
				continue;
			workQueue2.remove(mcdp2);
			ArrayList<MyComparableDataPoint> serDataList1 = seriesData1.get(newSeriesName);
			ArrayList<MyComparableDataPoint> serDataList2 = seriesData2.get(newSeriesName);
			if (serDataList1 == null) {
				serDataList1 = new ArrayList<MyComparableDataPoint>();
				seriesData1.put(newSeriesName, serDataList1);
			}
			if (serDataList2 == null) {
				serDataList2 = new ArrayList<MyComparableDataPoint>();
				seriesData2.put(newSeriesName, serDataList2);
			}
			serDataList1.add(mcdp1);
			serDataList2.add(mcdp2);
		}
	}
	
	private static String getSeriesDesc(MyComparableDataPoint mcdp, String seriesDescription) {
		return mcdp.serie + " - " + seriesDescription;
	}
	
	public double getX(int series, int item, boolean rankOrder, boolean mergeDataset, int dataset2offset) {
		if (!rankOrder)
			return getX(series, item);
		else {
			if (!ranksCalculated || calcMerge != mergeDataset || calcOffset != dataset2offset)
				calcRanks(mergeDataset, dataset2offset);
			// if (mergeDataset)
			// series = -1;
			if (!ranksX.containsKey(series + "$" + item))
				throw new IndexOutOfBoundsException("Error");
			return ranksX.get(series + "$" + item).doubleValue();
		}
	}
	
	public double getY(int series, int item, boolean rankOrder, boolean mergeDataset, int dataset2offset) {
		if (!rankOrder)
			return getY(series, item);
		else {
			if (!ranksCalculated || calcMerge != mergeDataset || calcOffset != dataset2offset)
				calcRanks(mergeDataset, dataset2offset);
			// if (mergeDataset)
			// series = -1;
			if (!ranksY.containsKey(series + "$" + item))
				throw new IndexOutOfBoundsException("Error");
			return ranksY.get(series + "$" + item).doubleValue();
		}
	}
	
	private void calcRanks(boolean mergeDataset, int dataset2offset) {
		calcMerge = mergeDataset;
		calcOffset = dataset2offset;
		ranksX = new HashMap<String, MyDouble>();
		ranksY = new HashMap<String, MyDouble>();
		ArrayList<MyDouble> workListX = new ArrayList<MyDouble>();
		ArrayList<MyDouble> workListY = new ArrayList<MyDouble>();
		for (int series = 0; series < getSeriesCount(); series++) {
			if (!mergeDataset) {
				workListX.clear();
				workListY.clear();
			}
			for (int item = 0; item < getItemCount(series); item++) {
				try {
					double x = getX(series, item);
					double y = getY(series, item + dataset2offset);
					if (Double.isNaN(x) || Double.isNaN(y)) {
						// empty
					} else {
						// todo
						workListX.add(new MyDouble(x, series, item));
						workListY.add(new MyDouble(y, series, item));
					}
				} catch (IndexOutOfBoundsException e) {
					// empty
				}
			}
			if (!mergeDataset)
				process(workListX, workListY, ranksX, ranksY);
		}
		if (mergeDataset)
			process(workListX, workListY, ranksX, ranksY);
		ranksCalculated = true;
	}
	
	static final double epsilon = 0.000000000001;
	
	@SuppressWarnings("unchecked")
	private void process(ArrayList<MyDouble> workListXzero, ArrayList<MyDouble> workListYzero,
						HashMap<String, MyDouble> rX, HashMap<String, MyDouble> rY) {
		
		ArrayList<MyDouble> workListX = new ArrayList<MyDouble>();
		for (MyDouble d : workListXzero)
			workListX.add(new MyDouble(d.doubleValue(), d.getSeries(), d.getIdx()));
		ArrayList<MyDouble> workListY = new ArrayList<MyDouble>();
		for (MyDouble d : workListYzero)
			workListY.add(new MyDouble(d.doubleValue(), d.getSeries(), d.getIdx()));
		ArrayList<MyDouble> workListCopyX = new ArrayList<MyDouble>(workListX);
		ArrayList<MyDouble> workListCopyY = new ArrayList<MyDouble>(workListY);
		
		Collections.sort(workListX, new Comparator() {
			public int compare(Object o1, Object o2) {
				MyDouble a = (MyDouble) o1;
				MyDouble b = (MyDouble) o2;
				if (Math.abs(a.doubleValue() - b.doubleValue()) < epsilon)
					return 0;
				if (a.doubleValue() < b.doubleValue())
					return -1;
				else
					return 1;
			}
		});
		Collections.sort(workListY, new Comparator() {
			public int compare(Object o1, Object o2) {
				MyDouble a = (MyDouble) o1;
				MyDouble b = (MyDouble) o2;
				if (Math.abs(a.doubleValue() - b.doubleValue()) < epsilon)
					return 0;
				if (a.doubleValue() < b.doubleValue())
					return -1;
				else
					return 1;
			}
		});
		try {
			for (int i = 0; i < workListX.size(); i++) {
				MyDouble d = workListX.get(i);
				workListCopyX.indexOf(d);
				double val = lookForSameValues(i + 1, workListX);
				rX.put(d.getSeries() + "$" + d.getIdx(), new MyDouble(val, d.getSeries(), d.getIdx()));
				// System.out.println("Added: Series="+d.getSeries()+", IDX="+d.getIdx()+", VALUE="+val);
			}
			for (int i = 0; i < workListY.size(); i++) {
				MyDouble d = workListY.get(i);
				workListCopyY.indexOf(d);
				rY.put(d.getSeries() + "$" + d.getIdx(), new MyDouble(lookForSameValues(i + 1, workListY), d.getSeries(), d
									.getIdx()));
			}
		} catch (IndexOutOfBoundsException iob) {
			ErrorMsg.addErrorMessage(iob);
		}
	}
	
	private double lookForSameValues(int i, ArrayList<MyDouble> workList) {
		MyDouble a = workList.get(i - 1);
		int minimumIndexOfSameNumber = i;
		for (int search = i - 2; search >= 0; search--) {
			MyDouble check = workList.get(search);
			if (Math.abs(check.doubleValue() - a.doubleValue()) > epsilon)
				break;
			minimumIndexOfSameNumber--;
		}
		int maximumIndexOfSameNumber = i;
		for (int search = i; search < workList.size(); search++) {
			MyDouble check = workList.get(search);
			if (Math.abs(check.doubleValue() - a.doubleValue()) > epsilon)
				break;
			maximumIndexOfSameNumber++;
		}
		if (maximumIndexOfSameNumber == minimumIndexOfSameNumber)
			return minimumIndexOfSameNumber;
		else {
			double sum = 0;
			for (int s = minimumIndexOfSameNumber; s <= maximumIndexOfSameNumber; s++)
				sum += s;
			return sum / (maximumIndexOfSameNumber - minimumIndexOfSameNumber + 1);
		}
	}
}

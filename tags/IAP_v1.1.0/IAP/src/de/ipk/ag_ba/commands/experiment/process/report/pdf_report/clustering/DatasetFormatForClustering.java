package de.ipk.ag_ba.commands.experiment.process.report.pdf_report.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.junit.Test;

public class DatasetFormatForClustering {
	
	public HashMap<Integer, HashMap<Integer, Object>> reformatMultipleFactorsToSingleFactor(
			HashMap<Integer, HashMap<Integer, Object>> row2col2val,
			HashSet<Integer> singleFactorCol,
			HashSet<Integer> otherFactorCols,
			HashSet<Integer> valueCols) {
		otherFactorCols.remove(singleFactorCol); // should not be needed
		
		LinkedHashSet<String> singeFactorValues = getFactors(singleFactorCol, row2col2val);
		
		TreeSet<String> otherFactorValueIDs = getOtherFactorIDs(otherFactorCols, row2col2val);
		LinkedHashMap<Object, LinkedHashMap<Object, Double>> fOne2fTwo2value = new LinkedHashMap<Object, LinkedHashMap<Object, Double>>();
		for (int col : valueCols) {
			String zeroFactorVal = row2col2val.get(0).get(col) + "";
			for (String otherFactorID : otherFactorValueIDs) {
				for (String newMainColValue : singeFactorValues) {
					// search for matching values
					ArrayList<Number> values = new ArrayList<Number>();
					for (Integer inRow : row2col2val.keySet()) {
						if (inRow < 1)
							continue;
						StringBuilder s = new StringBuilder();
						for (int sfc : singleFactorCol) {
							if (s.length() > 0)
								s.append(".");
							s.append(row2col2val.get(inRow).get(sfc));
						}
						
						String mainFactor = s.toString();
						
						if (mainFactor.equals(newMainColValue)) {
							// mainFactor is matching
							// search all values matching the current otherFactorValueID
							if (getUnifiedID(inRow, otherFactorCols, row2col2val).equals(otherFactorID)) {
								Object val = row2col2val.get(inRow).get(col);
								if (val != null) {
									if (val instanceof Number)
										values.add((Number) val);
									else {
										try {
											values.add(Double.parseDouble((String) val));
										} catch (Exception e) {
											System.out.println("Non-numeric value: " + val);
										}
									}
								}
							}
						}
					}
					if (values.size() > 0) {
						if (!fOne2fTwo2value.containsKey(newMainColValue))
							fOne2fTwo2value.put(newMainColValue, new LinkedHashMap<Object, Double>());
						fOne2fTwo2value.get(newMainColValue).put(zeroFactorVal + "." + otherFactorID, getAvg(values));
					}
				}
			}
		}
		HashMap<Integer, HashMap<Integer, Object>> res_row2col2val = new HashMap<Integer, HashMap<Integer, Object>>();
		// set column header
		int col = 1;
		res_row2col2val.put(0, new HashMap<Integer, Object>()); // first column contains rowKey
		res_row2col2val.get(0).put(0, "UniID");
		LinkedHashMap<Object, Integer> rowKey2rowNumber = new LinkedHashMap<Object, Integer>();
		for (Object colHeader : fOne2fTwo2value.keySet()) {
			res_row2col2val.put(col, new HashMap<Integer, Object>());
			res_row2col2val.get(col).put(0, colHeader);
			for (Object rowKey : fOne2fTwo2value.get(colHeader).keySet()) {
				if (!rowKey2rowNumber.containsKey(rowKey)) {
					rowKey2rowNumber.put(rowKey, rowKey2rowNumber.size() + 1);
					res_row2col2val.get(0).put(rowKey2rowNumber.size(), rowKey);
				}
				Integer rowNumber = rowKey2rowNumber.get(rowKey);
				res_row2col2val.get(col).put(rowNumber, fOne2fTwo2value.get(colHeader).get(rowKey));
			}
			col++;
		}
		
		return transpose(res_row2col2val);
	}
	
	private HashMap<Integer, HashMap<Integer, Object>> transpose(HashMap<Integer, HashMap<Integer, Object>> row2col2val) {
		HashMap<Integer, HashMap<Integer, Object>> res = new HashMap<Integer, HashMap<Integer, Object>>();
		for (Integer row : row2col2val.keySet()) {
			for (Integer col : row2col2val.get(row).keySet()) {
				Object val = row2col2val.get(row).get(col);
				put(res, col, row, val);
			}
		}
		return res;
	}
	
	private void put(HashMap<Integer, HashMap<Integer, Object>> res, Integer row, Integer col, Object val) {
		if (!res.containsKey(row))
			res.put(row, new HashMap<Integer, Object>());
		res.get(row).put(col, val);
	}
	
	private Double getAvg(ArrayList<Number> values) {
		if (values != null && values.size() > 0) {
			double sum = 0;
			int n = 0;
			for (Number v : values)
				if (v != null) {
					sum += v.doubleValue();
					n++;
				}
			if (n == 0)
				return null;
			else
				return sum / n;
		} else
			return null;
	}
	
	private TreeSet<String> getOtherFactorIDs(HashSet<Integer> otherFactorCols, HashMap<Integer, HashMap<Integer, Object>> row2col2val) {
		TreeSet<String> res = new TreeSet<String>();
		for (int row : row2col2val.keySet())
			if (row > 0)
				res.add(getUnifiedID(row, otherFactorCols, row2col2val));
		return res;
	}
	
	private String getUnifiedID(int row, HashSet<Integer> otherFactorCols, HashMap<Integer, HashMap<Integer, Object>> row2col2val) {
		StringBuilder s = new StringBuilder();
		for (int col : otherFactorCols) {
			s.append(row2col2val.get(row).get(col));
			s.append(".");
		}
		if (s.length() > 0)
			return s.substring(0, s.length() - 1);
		else
			return s.toString();
	}
	
	private LinkedHashSet<String> getFactors(HashSet<Integer> singleFactorCol, HashMap<Integer, HashMap<Integer, Object>> row2col2val) {
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		for (Integer row : row2col2val.keySet())
			if (row > 0) {
				StringBuilder s = new StringBuilder();
				for (int sfc : singleFactorCol) {
					if (s.length() > 0)
						s.append(".");
					s.append(row2col2val.get(row).get(sfc));
				}
				res.add(s.toString());
			}
		return res;
	}
	
	@Test
	public void testDataFormatReformatting() {
		HashMap<Integer, HashMap<Integer, Object>> row2col2val = new HashMap<Integer, HashMap<Integer, Object>>();
		Object[][] val = new Object[][] {
				{ "Plant ID", "Day (Int)", "H", "WW", "FI" },
				{ "G1", 5, 2, 3, 4 },
				{ "G2", 5, 3, 5, 8 },
				{ "G1", 6, 3, 2, 7 },
				{ "G2", 6, 2, 3, 4 } };
		addValues(row2col2val, val);
		HashSet<Integer> singleFactorCol = new HashSet<Integer>();
		singleFactorCol.add(0);
		HashSet<Integer> otherFactorCols = new HashSet<Integer>();
		otherFactorCols.add(1);
		HashSet<Integer> valueCols = new HashSet<Integer>();
		valueCols.add(2);
		valueCols.add(3);
		valueCols.add(4);
		
		print(row2col2val, "\t");
		HashMap<Integer, HashMap<Integer, Object>> res = reformatMultipleFactorsToSingleFactor(
				row2col2val, singleFactorCol, otherFactorCols, valueCols);
		print(res, "\t");
	}
	
	public static byte[] print(HashMap<Integer, HashMap<Integer, Object>> row2col2val, String separator) {
		StringBuilder s = new StringBuilder();
		System.out.println("---");
		for (int row : row2col2val.keySet()) {
			boolean first = true;
			int max = 0;
			for (int col : row2col2val.get(row).keySet())
				if (col > max)
					max = col;
			
			for (int col = 0; col <= max; col++) {
				if (first)
					first = false;
				else {
					System.out.print(separator);
					s.append(separator);
				}
				Object v = row2col2val.get(row).get(col);
				// if (v != null) {
				System.out.print(v + "");
				if (v != null)
					s.append(v + "");
				
				// }
			}
			System.out.println();
			s.append("\r\n");
		}
		return s.toString().getBytes();
	}
	
	private void addValues(HashMap<Integer, HashMap<Integer, Object>> row2col2val, Object[][] val) {
		for (int row = 0; row < val.length; row++) {
			for (int col = 0; col < val[0].length; col++) {
				if (!row2col2val.containsKey(row))
					row2col2val.put(row, new HashMap<Integer, Object>());
				row2col2val.get(row).put(col, val[row][col]);
			}
		}
	}
}

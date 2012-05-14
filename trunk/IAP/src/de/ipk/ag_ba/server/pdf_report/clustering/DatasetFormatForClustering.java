package de.ipk.ag_ba.server.pdf_report.clustering;

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
			int singleFactorCol,
			HashSet<Integer> otherFactorCols,
			HashSet<Integer> mergeAverageCols,
			HashSet<Integer> valueCols) {
		otherFactorCols.remove(singleFactorCol); // should not be needed
		
		LinkedHashSet<Object> singeFactorValues = getFactors(singleFactorCol, row2col2val);
		
		TreeSet<Object> otherFactorValueIDs = getOtherFactorIDs(otherFactorCols, row2col2val);
		HashMap<Integer, String> row2otherFactorValueIDs = new HashMap<Integer, String>();
		LinkedHashMap<Object, LinkedHashMap<Object, Double>> fOne2fTwo2value = new LinkedHashMap<Object, LinkedHashMap<Object, Double>>();
		for (int col : valueCols) {
			String zeroFactorVal = row2col2val.get(0).get(col) + "";
			for (Object otherFactorID : otherFactorValueIDs) {
				for (Object newMainColValue : singeFactorValues) {
					// search for matching values
					ArrayList<Number> values = new ArrayList<Number>();
					for (Integer inRow : row2col2val.keySet()) {
						if (row2col2val.get(inRow).containsKey(singleFactorCol)) {
							Object mainFactor = row2col2val.get(inRow).get(singleFactorCol);
							if (mainFactor == newMainColValue) {
								// mainFactor is matching
								// search all values matching the current otherFactorValueID
								Object val = row2col2val.get(inRow).get(col);
								if (val != null && val instanceof Number)
									values.add((Number) val);
							}
						}
					}
					if (!fOne2fTwo2value.containsKey(newMainColValue))
						fOne2fTwo2value.put(newMainColValue, new LinkedHashMap<Object, Double>());
					fOne2fTwo2value.get(newMainColValue).put(zeroFactorVal + "." + otherFactorID, getAvg(values));
				}
			}
		}
		HashMap<Integer, HashMap<Integer, Object>> res_row2col2val = new HashMap<Integer, HashMap<Integer, Object>>();
		for (int row : res_row2col2val.keySet())
			row2otherFactorValueIDs.put(row, getUnifiedID(row, otherFactorCols, res_row2col2val));
		// set column header
		if (!res_row2col2val.containsKey(0))
			res_row2col2val.put(0, new HashMap<Integer, Object>());
		// res_row2col2val.get(0).put(currentOutputColumn, newMainColValue);
		return res_row2col2val;
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
	
	private TreeSet<Object> getOtherFactorIDs(HashSet<Integer> otherFactorCols, HashMap<Integer, HashMap<Integer, Object>> row2col2val) {
		TreeSet<Object> res = new TreeSet<Object>();
		for (int row : row2col2val.keySet())
			res.add(getUnifiedID(row, otherFactorCols, row2col2val));
		return res;
	}
	
	private String getUnifiedID(int row, HashSet<Integer> otherFactorCols, HashMap<Integer, HashMap<Integer, Object>> row2col2val) {
		StringBuilder s = new StringBuilder();
		for (int col : otherFactorCols) {
			s.append(row2col2val.get(row).get(col));
			s.append(".");
		}
		return s.substring(0, s.length() - 1);
	}
	
	private LinkedHashSet<Object> getFactors(int singleFactorCol, HashMap<Integer, HashMap<Integer, Object>> row2col2val) {
		LinkedHashSet<Object> res = new LinkedHashSet<Object>();
		for (Integer row : row2col2val.keySet())
			if (row > 0)
				for (Integer col : row2col2val.get(row).keySet())
					res.add(row2col2val.get(row).get(col));
		return res;
	}
	
	@Test
	public void testDataFormatReformatting() {
		HashMap<Integer, HashMap<Integer, Object>> row2col2val = new HashMap<Integer, HashMap<Integer, Object>>();
		Object[][] val = new Object[][] {
				{ "Genotype", "Time", "A", "B", "C" },
				{ "G1", 5, 2, 3, 4 },
				{ "G2", 5, 3, 5, 8 },
				{ "G1", 6, 3, 2, 7 },
				{ "G2", 6, 2, 3, 4 } };
		addValues(row2col2val, val);
		int singleFactorCol = 0;
		HashSet<Integer> otherFactorCols = new HashSet<Integer>();
		otherFactorCols.add(1);
		HashSet<Integer> mergeAverageCols = new HashSet<Integer>();
		HashSet<Integer> valueCols = new HashSet<Integer>();
		valueCols.add(2);
		valueCols.add(3);
		
		print(row2col2val);
		HashMap<Integer, HashMap<Integer, Object>> res = reformatMultipleFactorsToSingleFactor(
				row2col2val, singleFactorCol, otherFactorCols,
				mergeAverageCols, valueCols);
		print(res);
	}
	
	private void print(HashMap<Integer, HashMap<Integer, Object>> row2col2val) {
		System.out.println("---");
		for (int row : row2col2val.keySet()) {
			for (int col : row2col2val.get(row).keySet())
				System.out.print(row2col2val.get(row).get(col) + " / ");
			System.out.println();
		}
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

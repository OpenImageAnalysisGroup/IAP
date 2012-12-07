/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.07.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JComponent;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

public class SearchOption {
	private LogicConnection logicalConnection = LogicConnection.AND;
	private NodeOrEdge searchNodeOrEdge = NodeOrEdge.Nodes;
	private String searchAttributePath;
	private String searchAttributeName;
	private String searchAttributeString = "";
	private int searchAttributeInteger = 0;
	private double searchAttributeDouble = 0d;
	private boolean searchAttributeBoolean = false;
	private SearchType searchType = null;
	private SearchLogic searchLogic = SearchLogic.searchMatched;
	private SearchOperation searchOperation = SearchOperation.include;
	
	private static double doubleEpsilon = 0.0001d;
	
	public boolean doesMatch(Attributable attr, Collection<GraphElement> searchScope, int idxOfAttr) {
		if (attr == null || attr instanceof Graph)
			return false;
		if (searchNodeOrEdge == NodeOrEdge.Edges && (attr instanceof Node))
			return false;
		if (searchNodeOrEdge == NodeOrEdge.Nodes && (attr instanceof Edge))
			return false;
		if (getSearchOperation() == SearchOperation.bottomN || getSearchOperation() == SearchOperation.topN) {
			return considerSearchLogic(idxOfAttr >= 0 && idxOfAttr < searchAttributeInteger);
		} else {
			switch (searchType) {
				// TODO: similar to mathString for integer, boolean etc (respect alternative paths), in order to be able
				// to search also in the annotations for label-fontsize >= x
				case searchString:
					return considerSearchLogic(matchString(attr));
				case searchBoolean:
					return considerSearchLogic(matchBoolean(attr));
				case searchDouble:
					return considerSearchLogic(matchDouble(attr));
				case searchInteger:
					return considerSearchLogic(matchInteger(attr));
			}
			return false;
		}
	}
	
	public HashMap<GraphElement, Integer> getPositionsOfGraphElementsForThisSearchOption(Collection<GraphElement> searchScope) {
		ArrayList<ValueAndGraphElement> scope = new ArrayList<ValueAndGraphElement>();
		for (GraphElement ge : searchScope) {
			switch (searchType) {
				case searchString:
					String[] ss = getStringValues(ge);
					if (ss == null || ss.length <= 0)
						scope.add(new ValueAndGraphElement(null, ge));
					else
						scope.add(new ValueAndGraphElement(ss[0], ge));
					break;
				case searchBoolean:
					scope.add(new ValueAndGraphElement(getBoolean(ge), ge));
					break;
				case searchDouble:
					scope.add(new ValueAndGraphElement(getDouble(ge), ge));
					break;
				case searchInteger:
					scope.add(new ValueAndGraphElement(getInteger(ge), ge));
					break;
			}
		}
		scope = getSortedListWithoutNullValues(scope, getSearchOperation() == SearchOperation.topN);
		int cnt = 0;
		HashMap<GraphElement, Integer> result = new HashMap<GraphElement, Integer>();
		for (ValueAndGraphElement vge : scope) {
			result.put(vge.getGraphElement(), new Integer(cnt));
			cnt++;
		}
		return result;
	}
	
	private ArrayList<ValueAndGraphElement> getSortedListWithoutNullValues(ArrayList<ValueAndGraphElement> scope, final boolean ascending) {
		ArrayList<ValueAndGraphElement> result = new ArrayList<ValueAndGraphElement>();
		for (ValueAndGraphElement veg : scope)
			if (veg.getValue() != null)
				result.add(veg);
		Collections.sort(result, new Comparator<ValueAndGraphElement>() {
			@SuppressWarnings("unchecked")
			public int compare(ValueAndGraphElement o1, ValueAndGraphElement o2) {
				if (ascending)
					return o1.getValue().compareTo(o2.getValue());
				else
					return o2.getValue().compareTo(o1.getValue());
			}
		});
		return result;
	}
	
	public SearchOption() {
		// empty
	}
	
	public SearchOption(LogicConnection logicalConnection, NodeOrEdge searchNodeOrEdge,
						String searchAttributePath, String searchAttributeName,
						String searchAttributeString,
						int searchAttributeInteger, double searchAttributeDouble, boolean searchAttributeBoolean,
						SearchType searchType, SearchLogic searchLogic, SearchOperation searchOperation) {
		this.logicalConnection = logicalConnection;
		this.searchNodeOrEdge = searchNodeOrEdge;
		this.searchAttributePath = searchAttributePath;
		this.searchAttributeName = searchAttributeName;
		this.searchAttributeString = searchAttributeString;
		this.searchAttributeInteger = searchAttributeInteger;
		this.searchAttributeDouble = searchAttributeDouble;
		this.searchAttributeBoolean = searchAttributeBoolean;
		this.searchType = searchType;
		this.searchLogic = searchLogic;
		this.searchOperation = searchOperation;
	}
	
	public String getScriptCommandForRecreationOfThisObject() {
		return "new SearchOption(" +
							"LogicConnection." + logicalConnection.name() + ", " +
							"NodeOrEdge." + searchNodeOrEdge.name() + ", " +
							"\"" + searchAttributePath + "\", " +
							"\"" + searchAttributeName + "\", " +
							"\"" + searchAttributeString + "\", " +
							searchAttributeInteger + ", " +
							searchAttributeDouble + ", " +
							searchAttributeBoolean + ", " +
							"SearchType." + searchType.name() + ", " +
							"SearchLogic." + searchLogic.name() + ", " +
							"SearchOperation." + searchOperation.name() +
							")";
	}
	
	public JComponent getSearchOptionEditorGUI(Collection<AttributePathNameSearchType> possibleAttributes, boolean showAndOrField, boolean isFindReplaceDialog) {
		return new SearchOptionEditorGUI(this, possibleAttributes, showAndOrField, isFindReplaceDialog);
	}
	
	public JComponent getSearchOptionEditorGUI(Collection<AttributePathNameSearchType> possibleAttributes, boolean showAndOrField) {
		return new SearchOptionEditorGUI(this, possibleAttributes, showAndOrField);
	}
	
	private boolean considerSearchLogic(boolean matched) {
		switch (searchLogic) {
			case searchMatched:
				return matched;
			case searchNotMatched:
				return !matched;
			default:
				ErrorMsg.addErrorMessage("Invalid Search Logic (internal error)!");
				return false;
		}
	}
	
	private Integer getInteger(Attributable attr) {
		Object val = AttributeHelper.getAttributeValue(attr, searchAttributePath, searchAttributeName, null, new Integer(Integer.MAX_VALUE), false);
		if (val == null || !(val instanceof Integer))
			return null;
		else
			return (Integer) val;
	}
	
	private boolean matchInteger(Attributable attr) {
		Integer i = getInteger(attr);
		if (i == null)
			return false;
		switch (searchOperation) {
			case smaller:
				return i.intValue() < searchAttributeInteger;
			case greater:
				return i.intValue() > searchAttributeInteger;
			case equals:
				return i.intValue() == searchAttributeInteger;
			case startswith:
				return i.toString().startsWith(new Integer(searchAttributeInteger).toString());
			case endswith:
				return i.toString().endsWith(new Integer(searchAttributeInteger).toString());
			case include:
				return i.toString().indexOf(new Integer(searchAttributeInteger).toString()) >= 0;
			case regexpsearch:
				ErrorMsg.addErrorMessage("Invalid Search-Option: Regular expression search on integer values not supported!");
				return false;
			default:
				ErrorMsg.addErrorMessage("Invalid Search-Option: unknown/internal error!");
				return false;
		}
	}
	
	private Double getDouble(Attributable attr) {
		Object val = AttributeHelper.getAttributeValue(attr, searchAttributePath, searchAttributeName, null, new Double(Double.NaN), false);
		if (val == null || !(val instanceof Double))
			return null;
		else
			return (Double) val;
	}
	
	private boolean matchDouble(Attributable attr) {
		Double d = getDouble(attr);
		if (d == null)
			return false;
		switch (searchOperation) {
			case smaller:
				return d.doubleValue() < searchAttributeDouble;
			case greater:
				return d.doubleValue() > searchAttributeDouble;
			case equals:
				return Math.abs(searchAttributeDouble - d.doubleValue()) < doubleEpsilon;
			case startswith:
				return stripEndNull(d.toString()).startsWith(stripEndNull(new Double(searchAttributeDouble).toString()));
			case endswith:
				return stripEndNull(d.toString()).endsWith(stripEndNull(new Double(searchAttributeDouble).toString()));
			case include:
				return stripEndNull(d.toString()).indexOf(stripEndNull(new Double(searchAttributeDouble).toString())) >= 0;
			case regexpsearch:
				ErrorMsg.addErrorMessage("Invalid Search-Option: Regular expression search on double values not supported!");
				return false;
			default:
				ErrorMsg.addErrorMessage("Invalid Search-Option: unknown/internal error!");
				return false;
		}
	}
	
	private String stripEndNull(String s) {
		if (s.endsWith(".0"))
			return s.substring(0, s.length() - 2);
		else
			return s;
	}
	
	private Boolean getBoolean(Attributable attr) {
		Object val = AttributeHelper.getAttributeValue(attr, searchAttributePath, searchAttributeName, null, new Boolean(false), false);
		if (val == null || !(val instanceof Boolean))
			return null;
		else
			return (Boolean) val;
	}
	
	private boolean matchBoolean(Attributable attr) {
		Boolean b = getBoolean(attr);
		if (b == null)
			return false;
		switch (searchOperation) {
			case equals:
				return searchAttributeBoolean == b.booleanValue();
			case smaller:
			case greater:
			case startswith:
			case endswith:
			case include:
			case regexpsearch:
				ErrorMsg.addErrorMessage("Invalid Search-Option (search type not supported)!");
				return false;
			default:
				ErrorMsg.addErrorMessage("Invalid Search-Option: unknown/internal error!");
				return false;
		}
	}
	
	private String[] getStringValues(Attributable attr) {
		if (attr instanceof Node) {
			ArrayList<Object> result = new ArrayList<Object>();
			ArrayList<String> paths = SearchDialog.getAlternativePaths((Node) attr, searchAttributePath);
			paths.add(0, searchAttributePath);
			for (String path : paths) {
				String val = getValue(attr, path);
				if (val != null && val instanceof String)
					result.add(val);
			}
			return result.toArray(new String[] {});
		} else {
			String val = getValue(attr, searchAttributePath);
			if (val == null || !(val instanceof String))
				return null;
			else
				return new String[] { val };
		}
	}
	
	private String getValue(Attributable attr, String path) {
		return (String) AttributeHelper.getAttributeValue(attr, path, searchAttributeName, null, "", false);
	}
	
	private boolean matchString(Attributable attr) {
		String[] ss = getStringValues(attr);
		if (ss == null || ss.length <= 0)
			return false;
		
		boolean matches = false;
		for (String s : ss) {
			switch (searchOperation) {
				case smaller:
					matches = s.compareTo(searchAttributeString) < 0;
					if (matches)
						return true;
					else
						break;
				case greater:
					matches = s.compareTo(searchAttributeString) > 0;
					if (matches)
						return true;
					else
						break;
				case equals:
					matches = searchAttributeString.equalsIgnoreCase(s);
					if (matches)
						return true;
					else
						break;
				case startswith:
					matches = s.toUpperCase().startsWith(searchAttributeString.toUpperCase());
					if (matches)
						return true;
					else
						break;
				case endswith:
					matches = s.toUpperCase().endsWith(searchAttributeString.toUpperCase());
					if (matches)
						return true;
					else
						break;
				case include:
					matches = s.toUpperCase().indexOf(searchAttributeString.toUpperCase()) >= 0;
					if (matches)
						return true;
					else
						break;
				case regexpsearch:
					matches = s.matches(searchAttributeString);
					if (matches)
						return true;
					else
						break;
				default:
					ErrorMsg.addErrorMessage("Invalid Search-Option: unexpected internal error!");
					return false;
			}
		}
		return matches;
	}
	
	public LogicConnection getLogicalConnection() {
		return logicalConnection;
	}
	
	public void setLogicalConnection(LogicConnection logicalConnection) {
		this.logicalConnection = logicalConnection;
	}
	
	public void setSearchLogic(SearchLogic searchLogic) {
		this.searchLogic = searchLogic;
	}
	
	public SearchLogic getSearchLogic() {
		return searchLogic;
	}
	
	public void setSearchNodeOrEdge(NodeOrEdge searchNodeOrEdge) {
		this.searchNodeOrEdge = searchNodeOrEdge;
	}
	
	public NodeOrEdge getSearchNodeOrEdge() {
		return searchNodeOrEdge;
	}
	
	public void setSearchAttributePath(String searchAttributePath) {
		this.searchAttributePath = searchAttributePath;
	}
	
	public String getSearchAttributePath() {
		return searchAttributePath;
	}
	
	public void setSearchAttributeName(String searchAttributeName) {
		this.searchAttributeName = searchAttributeName;
	}
	
	public String getSearchAttributeName() {
		return searchAttributeName;
	}
	
	public void setSearchAttributeString(String searchAttributeString) {
		this.searchAttributeString = searchAttributeString;
	}
	
	public String getSearchAttributeString() {
		return searchAttributeString;
	}
	
	public void setSearchAttributeInteger(int searchAttributeInteger) {
		this.searchAttributeInteger = searchAttributeInteger;
	}
	
	public int getSearchAttributeInteger() {
		return searchAttributeInteger;
	}
	
	public void setSearchAttributeDouble(double searchAttributeDouble) {
		this.searchAttributeDouble = searchAttributeDouble;
	}
	
	public double getSearchAttributeDouble() {
		return searchAttributeDouble;
	}
	
	public void setSearchAttributeBoolean(boolean searchAttributeBoolean) {
		this.searchAttributeBoolean = searchAttributeBoolean;
	}
	
	public boolean isSearchAttributeBoolean() {
		return searchAttributeBoolean;
	}
	
	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}
	
	public SearchType getSearchType() {
		return searchType;
	}
	
	public void setSearchOperation(SearchOperation searchOperation) {
		this.searchOperation = searchOperation;
	}
	
	public SearchOperation getSearchOperation() {
		return searchOperation;
	}
	
	public static String getImportStatements() {
		return "import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.*;";
	}
	
	public static void getSearchScriptCommands(ArrayList<SearchOption> searchOptions,
						boolean addElementsTrue, ArrayList<String> tf, String pre) {
		String sos = "";
		for (SearchOption so : searchOptions) {
			if (sos.length() > 0)
				sos = sos + ", " + so.getScriptCommandForRecreationOfThisObject();
			else
				sos = "   " + so.getScriptCommandForRecreationOfThisObject();
		}
		tf.add(pre + "SearchOption[] so = new SearchOption[] {" + sos + "};" + "");
		tf.add(pre + "SearchDialog.doSearch(so, " + (addElementsTrue ? "true" : "false") + ");" + "");
	}
}

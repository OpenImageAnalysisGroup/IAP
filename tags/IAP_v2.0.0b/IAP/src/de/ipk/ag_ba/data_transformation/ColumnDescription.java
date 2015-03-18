package de.ipk.ag_ba.data_transformation;

import java.util.ArrayList;

import org.NiceNameSupport;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;

/**
 * @author klukas
 */
public class ColumnDescription implements NiceNameSupport {
	private final String id;
	private final String title;
	private final boolean isMetaData;
	private final String fieldPart;
	private final String mainPart;
	private boolean splitIndexAccess;
	private String splitString;
	private int splitIndex;
	
	public ColumnDescription(String id, String title) {
		this(id, title, false);
	}
	
	public ColumnDescription(String id, String title, boolean isMetaData) {
		this.id = id;
		this.mainPart = getID().split("\\.", 2)[0];
		this.fieldPart = getID().split("\\.", 2)[1];
		if (fieldPart.indexOf("$[") > 0) {
			this.splitString = fieldPart.substring(fieldPart.indexOf("$[") + 2);
			if (this.splitString.contains("/") && splitString.endsWith("]")) {
				splitString = splitString.substring(0, splitString.length() - 1);
				this.splitIndexAccess = true;
				String[] splitStringArr = splitString.split("/", 2);
				String splCh = splitStringArr[0];
				String index = splitStringArr[1];
				try {
					this.splitIndex = Integer.parseInt(index);
					this.splitString = splCh;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
		}
		this.title = title;
		this.isMetaData = isMetaData;
	}
	
	public String getID() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isMetaData() {
		return isMetaData;
	}
	
	@Override
	public String getNiceName() {
		boolean titleEqualsID = id.endsWith("." + title.toLowerCase());
		if (titleEqualsID) {
			if (!isMetaData)
				return "[" + id + "]";
			else
				return "{" + id + "}";
		} else {
			if (!isMetaData)
				return "[" + id + ":" + title + "]";
			else
				return "{" + id + ":" + title + "}";
		}
	}
	
	public boolean allowGroupBy() {
		if (isMetaData() && !id.startsWith("sample"))
			return true;
		return false;
	}
	
	public static boolean isMeasurementRelevant(ArrayList<ColumnDescription> relevantColumns) {
		boolean measurementRelevant = false;
		searchloop: for (ColumnDescription cd : relevantColumns) {
			String valueHolder = cd.getIDmainPart();
			
			switch (valueHolder) {
				case "measurement":
					measurementRelevant = true;
					break searchloop;
			}
		}
		return measurementRelevant;
		
	}
	
	public static boolean isSampleRelevant(ArrayList<ColumnDescription> relevantColumns) {
		boolean sampleRelevant = false;
		searchloop: for (ColumnDescription cd : relevantColumns) {
			String valueHolder = cd.getIDmainPart();
			
			switch (valueHolder) {
				case "sample":
					sampleRelevant = true;
					break searchloop;
			}
		}
		return sampleRelevant;
		
	}
	
	public static String extractDataString(ArrayList<ColumnDescription> relevantColumns, NumericMeasurementInterface nmi) {
		StringBuilder r = new StringBuilder();
		
		for (ColumnDescription cd : relevantColumns) {
			String valueHolder = cd.getIDmainPart();
			String fieldID = cd.getIDfieldPart();
			String val = null;
			
			if (cd.isSplitIndexAccess())
				fieldID = fieldID.split("\\$", 2)[0];
			
			// experiment, condition, sample, measurement, see DataTableLoader
			Object o;
			switch (valueHolder) {
				case "measurement":
					o = nmi.getAttributeField(fieldID);
					if (o != null)
						if (o instanceof String)
							val = (String) o;
						else
							val = o + "";
					break;
				case "sample":
					val = (String) nmi.getParentSample().getAttributeField(fieldID);
					break;
				case "condition":
					val = (String) nmi.getParentSample().getParentCondition().getAttributeField(fieldID);
					break;
				case "experiment":
					o = nmi.getParentSample().getParentCondition().getExperimentHeader().getAttributeField(fieldID);
					if (o != null)
						if (o instanceof String)
							val = (String) o;
						else
							val = o + "";
					break;
			}
			if (cd.isSplitIndexAccess())
				val = cd.getSplitContent(val);
			if (val != null) {
				if (r.length() > 0)
					r.append("//");
				r.append(cd.id + ":" + val);
			}
		}
		return r.toString();
	}
	
	public static String extractDataStringAndResetAllFields(ArrayList<ColumnDescription> relevantColumns, ArrayList<ColumnDescription> notRelevantColumns,
			NumericMeasurementInterface nmi) {
		StringBuilder r = new StringBuilder();
		
		for (ColumnDescription cd : relevantColumns) {
			String valueHolder = cd.getIDmainPart();
			String fieldID = cd.getIDfieldPart();
			String val = null;
			
			if (cd.isSplitIndexAccess())
				fieldID = fieldID.split("\\$", 2)[0];
			
			// experiment, condition, sample, measurement, see DataTableLoader
			Object o;
			switch (valueHolder) {
				case "measurement":
					o = nmi.getAttributeField(fieldID);
					if (o != null)
						if (o instanceof String)
							val = (String) o;
						else
							val = o + "";
					break;
				case "sample":
					val = (String) nmi.getParentSample().getAttributeField(fieldID);
					break;
				case "condition":
					val = (String) nmi.getParentSample().getParentCondition().getAttributeField(fieldID);
					break;
				case "experiment":
					o = nmi.getParentSample().getParentCondition().getExperimentHeader().getAttributeField(fieldID);
					if (o != null)
						if (o instanceof String)
							val = (String) o;
						else
							val = o + "";
					break;
			}
			if (cd.isSplitIndexAccess())
				val = cd.getSplitContent(val);
			if (val != null) {
				if (r.length() > 0)
					r.append("//");
				boolean addColName = false;
				if (addColName)
					r.append(cd.id + ":" + val);
				else
					r.append(val);
			}
		}
		ArrayList<ColumnDescription> allCol = new ArrayList<ColumnDescription>(relevantColumns.size() + notRelevantColumns.size());
		allCol.addAll(relevantColumns);
		allCol.addAll(notRelevantColumns);
		for (ColumnDescription cd : allCol) {
			String valueHolder = cd.getIDmainPart();
			String fieldID = cd.getIDfieldPart();
			
			if (fieldID.contains("$"))
				fieldID = fieldID.split("\\$", 2)[0];
			
			// experiment, condition, sample, measurement, see DataTableLoader
			switch (valueHolder) {
				case "measurement":
					nmi.setAttributeField(fieldID, null);
					break;
				case "sample":
					// nmi.getParentSample().setAttributeField(fieldID, null);
					break;
				case "condition":
					nmi.getParentSample().getParentCondition().setAttributeField(fieldID, null);
					break;
				case "experiment":
					nmi.getParentSample().getParentCondition().getExperimentHeader().setAttributeField(fieldID, null);
					break;
			}
		}
		return r.toString();
	}
	
	private String getSplitContent(String val) {
		if (val == null || !splitIndexAccess)
			return val;
		if (val.contains(splitString)) {
			String[] va = val.split(splitString);
			if (va.length > splitIndex && !va[splitIndex].isEmpty()) {
				return va[splitIndex];
			} else
				return null;
		}
		if (splitIndex == 0 && !val.isEmpty())
			return val;
		else
			return null;
	}
	
	private boolean isSplitIndexAccess() {
		return splitIndexAccess;
	}
	
	public static String extractDataString(ArrayList<ColumnDescription> relevantColumns, SampleInterface sample) {
		StringBuilder r = new StringBuilder();
		
		for (ColumnDescription cd : relevantColumns) {
			String valueHolder = cd.getIDmainPart();
			String fieldID = cd.getIDfieldPart();
			String val = null;
			
			// experiment, condition, sample, measurement, see DataTableLoader
			switch (valueHolder) {
				case "measurement":
					throw new UnsupportedOperationException("Internal error");
				case "sample":
					val = (String) sample.getAttributeField(fieldID);
					break;
				case "condition":
					val = (String) sample.getParentCondition().getAttributeField(fieldID);
					break;
				case "experiment":
					Object o = sample.getParentCondition().getExperimentHeader().getAttributeField(fieldID);
					if (o != null)
						if (o instanceof String)
							val = (String) o;
						else
							val = o + "";
					break;
			}
			if (cd.isSplitIndexAccess())
				val = cd.getSplitContent(val);
			if (val != null) {
				if (r.length() > 0)
					r.append("//");
				r.append(cd.id + ":" + val);
			}
		}
		return r.toString();
	}
	
	public static String extractDataString(ArrayList<ColumnDescription> relevantColumns, ConditionInterface condition) {
		StringBuilder r = new StringBuilder();
		
		for (ColumnDescription cd : relevantColumns) {
			String valueHolder = cd.getIDmainPart();
			String fieldID = cd.getIDfieldPart();
			String subQuery = null;
			int subQueryIdx = fieldID.indexOf("$");
			if (subQueryIdx > 0) {
				subQuery = fieldID.substring(subQueryIdx + 1);
				fieldID = fieldID.substring(0, subQueryIdx);
			}
			String val = null;
			
			// experiment, condition, sample, measurement, see DataTableLoader
			switch (valueHolder) {
				case "measurement":
				case "sample":
					throw new UnsupportedOperationException("Internal error");
				case "condition":
					val = (String) condition.getAttributeField(fieldID);
					break;
				case "experiment":
					Object o = condition.getExperimentHeader().getAttributeField(fieldID);
					if (o != null)
						if (o instanceof String)
							val = (String) o;
						else
							val = o + "";
					break;
			}
			if (val != null) {
				if (subQuery != null)
					for (String subS : val.split("//")) {
						int colIdx = subS.indexOf(":");
						if (colIdx > 0) {
							subS = subS.trim();
							if (subS.startsWith(subQuery) && colIdx > 0) {
								subS = subS.substring(subQueryIdx + 1).trim();
								val = subS;
								break;
							}
						}
					}
				if (cd.isSplitIndexAccess())
					val = cd.getSplitContent(val);
				if (val != null) {
					if (r.length() > 0)
						r.append("//");
					r.append(cd.id + ":" + val);
				}
			}
		}
		return r.toString();
	}
	
	private String getIDfieldPart() {
		return fieldPart;
	}
	
	private String getIDmainPart() {
		return mainPart;
	}
}

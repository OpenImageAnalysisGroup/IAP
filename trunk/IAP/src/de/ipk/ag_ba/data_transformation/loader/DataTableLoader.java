package de.ipk.ag_ba.data_transformation.loader;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.StringManipulationTools;
import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.data_transformation.ColumnDescription;
import de.ipk.ag_ba.data_transformation.DataTable;
import de.ipk.ag_ba.data_transformation.RowData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.AttributeValuePairSupport;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;

/**
 * @author klukas
 */
public class DataTableLoader {
	private final DataTable table;
	private final LinkedHashMap<String, ColumnDescription> columns;
	private final Collection<RowData> rows;
	
	public DataTableLoader() {
		this.columns = new LinkedHashMap<String, ColumnDescription>();
		this.rows = new LinkedList<RowData>();
		this.table = new DataTable(columns.values(), rows);
	}
	
	public DataTable loadFromExperiment(ExperimentInterface experiment) {
		Map<String, Object> attributeValueMap = new LinkedHashMap<String, Object>();
		MappingData3DPath.getStream(experiment).forEach((MappingData3DPath mp) -> {
			extendColumnDescriptionsFrom(mp, attributeValueMap);
		});
		return table;
	}
	
	private void extendColumnDescriptionsFrom(MappingData3DPath mp, Map<String, Object> attributeValueMap) {
		// metadata columns
		processMetaDataProvider(mp.getSampleData().getParentCondition().getExperimentHeader(), "experiment", attributeValueMap, true, "remark", "excelfileid",
				"origin", "outliers", "settings");
		processMetaDataProvider(mp.getSampleData().getParentCondition(), "condition", attributeValueMap, true, Condition.getExperimentFields());
		processMetaDataProvider(mp.getSampleData(), "sample", attributeValueMap, true);
		// value columns
		processMetaDataProvider(mp.getSampleData().getParentCondition().getParentSubstance(), "trait", attributeValueMap, false);
		processMetaDataProvider(mp.getMeasurement(), "measurement", attributeValueMap, true, "value", "labelurl", "filename", "annotation", "id");
	}
	
	private void processMetaDataProvider(AttributeValuePairSupport provider, String prefix, Map<String, Object> attributeValueMap, boolean isMetaData,
			String... remove) {
		attributeValueMap.clear();
		provider.fillAttributeMap(attributeValueMap);
		
		attributeValueMap.remove("id");
		
		for (String r : remove)
			attributeValueMap.remove(r);
		
		for (String id : attributeValueMap.keySet()) {
			if (attributeValueMap.get(id) == null)
				attributeValueMap.put(id, "");
			if (attributeValueMap.get(id) != null) {
				// add id itself
				if (!columns.containsKey(prefix + "." + id)) {
					columns.put(prefix + "." + id, new ColumnDescription(prefix + "." + id, WordUtils.capitalize(id), isMetaData));
				}
				// check if value contains '//', then split and add virtual columns, if split content contains a ':'. The string before the ':' is interpreted as a
				// sub-column name. The ':' is also interpreted, if no '//' is found.
				Object val = attributeValueMap.get(id);
				if (val instanceof String) {
					String v = (String) val;
					for (String subval : v.split("//")) {
						if (subval.contains(":")) {
							String virtCol = subval.substring(0, subval.indexOf(":"));
							virtCol = virtCol.trim();
							if (!virtCol.isEmpty()) {
								String subCol = prefix + "." + id + "$" + virtCol;
								if (!columns.containsKey(subCol)) {
									columns.put(subCol, new ColumnDescription(subCol, WordUtils.capitalize(id + "$" + virtCol), isMetaData));
								}
							}
						} else {
							int hc = StringManipulationTools.count(subval, "-");
							if (hc > 0) {
								for (int idx = 0; idx <= hc; idx++) {
									String subCol = prefix + "." + id + "$[-/" + idx + "]";
									if (!columns.containsKey(subCol)) {
										columns.put(prefix + "." + id + "$" + subCol, new ColumnDescription(subCol, WordUtils.capitalize(id) + "$[-/" + idx + "]",
												isMetaData));
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

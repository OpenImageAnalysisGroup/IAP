package de.ipk.ag_ba.commands.load_lt;

import java.util.ArrayList;
import java.util.HashMap;

import org.SystemOptions;

import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableDataStringRow;

public class TableDataHeadingRow {
	
	private Integer[] plantIDcol, speciesCol, genotypeCol,
			treatmentCol, sequenceCol, varietyCol, growthconditionsCol, replicateIdCol;
	
	private final String separator;
	
	public TableDataHeadingRow(HashMap<Integer, String> col2heading) {
		separator = SystemOptions.getInstance().getString("Metadata",
				"Multiple-Value-Meta-Data-Connector", " // ");
		if (col2heading == null) {
			plantIDcol = SystemOptions.getInstance().getIntArray("Metadata",
					"No Heading Row//Columns_Plant-ID", new Integer[] { 1 });
			speciesCol = SystemOptions.getInstance().getIntArray("Metadata",
					"No Heading Row//Columns_Species", new Integer[] { 2 });
			genotypeCol = SystemOptions.getInstance().getIntArray("Metadata",
					"No Heading Row//Columns_Genotype", new Integer[] { 3 });
			treatmentCol = SystemOptions.getInstance().getIntArray("Metadata",
					"No Heading Row//Columns_Treatment", new Integer[] { 4 });
			sequenceCol = SystemOptions.getInstance().getIntArray("Metadata",
					"No Heading Row//Columns_Sequence", new Integer[] { 5 });
			varietyCol = SystemOptions.getInstance().getIntArray("Metadata",
					"No Heading Row//Columns_Variety", new Integer[] { -1 });
			growthconditionsCol = SystemOptions.getInstance().getIntArray("Metadata",
					"No Heading Row//Columns_Growthconditions", new Integer[] { 7 });
			replicateIdCol = SystemOptions.getInstance().getIntArray("Metadata",
					"No Heading Row//Columns_Replicate_Id (only for Load Files command)", new Integer[] { -1 });
		} else {
			ArrayList<Integer> plantIDcolARR = new ArrayList<Integer>();
			ArrayList<Integer> speciesColARR = new ArrayList<Integer>();
			ArrayList<Integer> genotypeColARR = new ArrayList<Integer>();
			ArrayList<Integer> treatmentColARR = new ArrayList<Integer>();
			ArrayList<Integer> sequenceColARR = new ArrayList<Integer>();
			ArrayList<Integer> varietyColARR = new ArrayList<Integer>();
			ArrayList<Integer> growthconditionsColARR = new ArrayList<Integer>();
			ArrayList<Integer> replicateIdColARR = new ArrayList<Integer>();
			
			ArrayList<String> possibleValues = new ArrayList<String>();
			possibleValues.add("Ignored Column");
			possibleValues.add("Plant ID");
			possibleValues.add("Replicate ID (for Load Files command)");
			
			for (ConditionInfo ci : ConditionInfo.values())
				if (ci != ConditionInfo.IGNORED_FIELD)
					if (ci != ConditionInfo.FILES)
						possibleValues.add(ci + "");
			
			HashMap<ConditionInfo, ArrayList<Integer>> ci2arr =
					new HashMap<ConditionInfo, ArrayList<Integer>>();
			ci2arr.put(ConditionInfo.SPECIES, speciesColARR);
			ci2arr.put(ConditionInfo.GENOTYPE, genotypeColARR);
			ci2arr.put(ConditionInfo.TREATMENT, treatmentColARR);
			ci2arr.put(ConditionInfo.SEQUENCE, sequenceColARR);
			ci2arr.put(ConditionInfo.VARIETY, varietyColARR);
			ci2arr.put(ConditionInfo.GROWTHCONDITIONS, growthconditionsColARR);
			for (Integer col : col2heading.keySet()) {
				String heading = col2heading.get(col);
				String sel = SystemOptions.getInstance().getStringRadioSelection(
						"Metadata",
						"Columns//" + heading,
						possibleValues, LTdataExchange.getDefaultSelection(col, heading, possibleValues), true);
				if (sel != null && sel.equals("Plant ID")) {
					plantIDcolARR.add(col);
				} else
					if (sel != null && sel.equals("Replicate ID (for Load Files command)")) {
						replicateIdColARR.add(col);
					} else
						if (sel != null && !sel.equals("Ignored Column")) {
							ConditionInfo ciSel = ConditionInfo.valueOfString(sel);
							if (ciSel != null) {
								ArrayList<Integer> arr = ci2arr.get(ciSel);
								arr.add(col);
							}
						}
			}
			plantIDcol = plantIDcolARR.toArray(new Integer[] {});
			speciesCol = speciesColARR.toArray(new Integer[] {});
			genotypeCol = genotypeColARR.toArray(new Integer[] {});
			treatmentCol = treatmentColARR.toArray(new Integer[] {});
			sequenceCol = sequenceColARR.toArray(new Integer[] {});
			varietyCol = varietyColARR.toArray(new Integer[] {});
			growthconditionsCol = growthconditionsColARR.toArray(new Integer[] {});
			replicateIdCol = replicateIdColARR.toArray(new Integer[] {});
		}
	}
	
	public String getPlantID(TableDataStringRow tdsr) {
		return tdsr.getString(plantIDcol, separator);
	}
	
	public String getSpecies(TableDataStringRow tdsr) {
		return tdsr.getString(speciesCol, separator);
	}
	
	public String getGenotype(TableDataStringRow tdsr) {
		return tdsr.getString(genotypeCol, separator);
	}
	
	public String getVariety(TableDataStringRow tdsr) {
		return tdsr.getString(varietyCol, separator);
	}
	
	public String getSequence(TableDataStringRow tdsr) {
		return tdsr.getString(sequenceCol, separator);
	}
	
	public String getTreatment(TableDataStringRow tdsr) {
		return tdsr.getString(treatmentCol, separator);
	}
	
	public String getGrowthconditions(TableDataStringRow tdsr) {
		return tdsr.getString(growthconditionsCol, separator);
	}
	
	public String getReplicateID(TableDataStringRow tdsr) {
		return tdsr.getString(replicateIdCol, separator);
	}
}

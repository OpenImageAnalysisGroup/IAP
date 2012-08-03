package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.process_alternative_ids;

public enum SetLabelModeOfOperation {
	setLabelCreateTableUseExistingLabelAsTableHeaderCreateColumns,
	setLabelCreateTableUseExistingLabelAsTableHeaderCreateRows,
	setLabelCreateTableCreateColumns,
	setLabelCreateTableCreateRows,
	setLabelFromAllMappingsDivideByDivider,
	setLabelAsSingleUniqueListDivideByDivider;
	
	@Override
	public String toString() {
		switch (this) {
			case setLabelCreateTableUseExistingLabelAsTableHeaderCreateColumns:
				return "Create table and put multiple mapping data in separate columns. Use existing node label as table header.";
			case setLabelCreateTableUseExistingLabelAsTableHeaderCreateRows:
				return "Create table and put multiple mapping data in separate rows. Use existing node label as table header.";
			case setLabelCreateTableCreateColumns:
				return "Create table and put multiple mapping data in separate columns.";
			case setLabelCreateTableCreateRows:
				return "Create table and put multiple mapping data in separate rows.";
			case setLabelFromAllMappingsDivideByDivider:
				return "Separate multiple mapping data by specified divider.";
			case setLabelAsSingleUniqueListDivideByDivider:
				return "Create list of unique non-empty annotation values. Divide by list divider.";
			default:
				return "No description available (" + super.toString() + ")";
		}
	}
}

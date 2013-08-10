package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.ArrayList;
import java.util.Iterator;

public class ListOfDataRowsExcelExport extends ArrayList<DataRowExcelExport> {
	
	private static final long serialVersionUID = -6089215718924489926L;
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof DataRowExcelExport) {
			DataRowExcelExport data = (DataRowExcelExport) o;
			Iterator<DataRowExcelExport> it = this.iterator();
			DataRowExcelExport actual;
			while (it.hasNext()) {
				actual = it.next();
				if (actual.equals(data))
					return true;
			}
		}
		return false;
	}
	
	/***
	 * Iterate over TreeSet - if the Object with the specified key (cond, time, repl) returns.
	 * If the Object is not found, returns null
	 * 
	 * @param conditionID
	 * @param timeID
	 * @param replicateID
	 * @return
	 */
	public DataRowExcelExport get(int conditionID, int timeID, int replicateID) {
		Iterator<DataRowExcelExport> it = this.iterator();
		DataRowExcelExport d;
		while (it.hasNext()) {
			d = it.next();
			if (d.getConditionID() == conditionID && d.getTimeID() == timeID && d.getReplicateID() == replicateID)
				return d;
		}
		return null;
	}
}

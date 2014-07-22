package iap.blocks.postprocessing;

import org.SystemOptions;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author Christian Klukas
 */
public class WellProcessing {
	
	public static String getWellID(int wellIdx, int well_Cnt, ImageData img) {
		String sn = img.getParentSample().getParentCondition().getParentSubstance().getName();
		if (sn != null && sn.toUpperCase().startsWith("TOP."))
			sn = "Top View ";
		else
			if (sn != null && sn.toUpperCase().startsWith("SIDE."))
				sn = "Side View ";
			else
				sn = "";
		String ra = img.getPosition() != null ? " (" + sn + "Rotation Angle " + img.getPosition().intValue() + ")" : "";
		String id = SystemOptions.getInstance().getString("Multi-Tray Wells", "Tray with " + well_Cnt + " wells//Well " + (wellIdx + 1) + ra, "" + wellIdx);
		return id;
	}
	
	public static String getWellID(Integer wellIdx, int well_Cnt, NumericMeasurement3D m) {
		String sn = m.getParentSample().getParentCondition().getParentSubstance().getName();
		if (sn != null && sn.toUpperCase().startsWith("TOP."))
			sn = "Top View ";
		else
			if (sn != null && sn.toUpperCase().startsWith("SIDE."))
				sn = "Side View ";
			else
				sn = "";
		String ra = m.getPosition() != null ? " (" + sn + "Rotation Angle " + m.getPosition().intValue() + ")" : "";
		String id = SystemOptions.getInstance().getString("Multi-Tray Wells", "Tray with " + well_Cnt + " wells//Well " + (wellIdx + 1) + ra, "" + wellIdx);
		return id;
	}
}

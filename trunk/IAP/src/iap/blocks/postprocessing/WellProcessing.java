package iap.blocks.postprocessing;

import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import org.SystemOptions;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author Christian Klukas
 */
public class WellProcessing {
	
	public static String getWellID(int wellIdx, int well_Cnt, CameraPosition cp, Double optRotationAngle,
			ImageProcessorOptionsAndResults options) {
		if (well_Cnt <= 1)
			return "";
		String sn;
		if (cp == CameraPosition.TOP)
			sn = "Top View ";
		else
			if (cp == CameraPosition.SIDE)
				sn = "Side View ";
			else
				sn = "";
		String ra = optRotationAngle != null ? " (" + sn + "Rotation Angle " + optRotationAngle.intValue() + ")" : "";
		String id = SystemOptions.getInstance().getString("Multi-Tray Wells", "Tray with " + well_Cnt + " wells//Well " + (wellIdx + 1) + ra, "X" + wellIdx);
		SystemOptions o = options.getOptSystemOptions();
		if (o != null)
			id = SystemOptions.getInstance().getString("Multi-Tray Wells",
					"Tray with " + well_Cnt + " wells//Well " + (wellIdx + 1) + ra, id);
		return id;
	}
	
	public static String getWellID(int wellIdx, int well_Cnt, ImageData img,
			ImageProcessorOptionsAndResults options) {
		String sn = img.getParentSample().getParentCondition().getParentSubstance().getName();
		if (sn != null && sn.toUpperCase().startsWith("TOP."))
			sn = "Top View ";
		else
			if (sn != null && sn.toUpperCase().startsWith("SIDE."))
				sn = "Side View ";
			else
				sn = "";
		String ra = img.getPosition() != null ? " (" + sn + "Rotation Angle " + img.getPosition().intValue() + ")" : "";
		String id = SystemOptions.getInstance().getString("Multi-Tray Wells", "Tray with " + well_Cnt + " wells//Well " + (wellIdx + 1) + ra, "X" + wellIdx);
		SystemOptions o = options.getOptSystemOptions();
		if (o != null)
			id = SystemOptions.getInstance().getString("Multi-Tray Wells",
					"Tray with " + well_Cnt + " wells//Well " + (wellIdx + 1) + ra, id);
		return id;
	}
	
	public static String getWellID(Integer wellIdx, int well_Cnt, NumericMeasurement3D m,
			ImageProcessorOptionsAndResults options) {
		String sn = m.getParentSample().getParentCondition().getParentSubstance().getName();
		if (sn != null && sn.toUpperCase().startsWith("TOP."))
			sn = "Top View ";
		else
			if (sn != null && sn.toUpperCase().startsWith("SIDE."))
				sn = "Side View ";
			else
				sn = "";
		String ra = m.getPosition() != null ? " (" + sn + "Rotation Angle " + m.getPosition().intValue() + ")" : "";
		String id = SystemOptions.getInstance().getString("Multi-Tray Wells", "Tray with " + well_Cnt + " wells//Well " + (wellIdx + 1) + ra, "X" + wellIdx);
		SystemOptions o = options.getOptSystemOptions();
		if (o != null)
			id = SystemOptions.getInstance().getString("Multi-Tray Wells",
					"Tray with " + well_Cnt + " wells//Well " + (wellIdx + 1) + ra, id);
		return id;
	}
}

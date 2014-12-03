package de.ipk.ag_ba.gui;

import org.NiceStringSupport;
import org.StringManipulationTools;

import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public enum ExperimentSortingMode implements NiceStringSupport {
	GROUP_BY_TYPE_THEN_COORDINATOR("Group by type then coordinator"),
	GROUP_BY_TYPE_THEN_IMPORT_USER("Group by type then import user"),
	GROUP_BY_GROUP_THEN_IMPORT_USER("Group by import user group then import user"),
	GROUP_BY_COORDINATOR_THEN_TYPE("Group by coordinator then type"),
	GROUP_BY_IMPORT_USR_THEN_COORDINATOR("Group by import user then coordinator"),
	GROUP_BY_TYPE_ONLY("Group by type only");
	
	private final String nice;
	
	ExperimentSortingMode(String nice) {
		this.nice = nice;
	}
	
	@Override
	public String getNiceString() {
		return nice;
	}
	
	public static ExperimentSortingMode fromNiceString(String stringRadioSelection) {
		for (ExperimentSortingMode e : values())
			if (e.getNiceString().equals(stringRadioSelection))
				return e;
		return GROUP_BY_TYPE_THEN_COORDINATOR;
	}
	
	public String getFirstField(ExperimentHeaderInterface eh, String ifNotFound) {
		String v = null;
		switch (this) {
			case GROUP_BY_COORDINATOR_THEN_TYPE:
				v = eh.getCoordinator();
				break;
			case GROUP_BY_IMPORT_USR_THEN_COORDINATOR:
				v = eh.getImportusername();
				break;
			case GROUP_BY_GROUP_THEN_IMPORT_USER:
				v = eh.getImportusergroup();
				break;
			case GROUP_BY_TYPE_ONLY:
				v = eh.getExperimentType();
				break;
			case GROUP_BY_TYPE_THEN_COORDINATOR:
				v = eh.getExperimentType();
				break;
			case GROUP_BY_TYPE_THEN_IMPORT_USER:
				v = eh.getExperimentType();
				break;
			default:
				break;
		}
		if (v == null || v.isEmpty())
			return ifNotFound;
		else
			return v;
	}
	
	public String getSecondField(ExperimentHeaderInterface eh, String ifNotFound) {
		String v = null;
		switch (this) {
			case GROUP_BY_COORDINATOR_THEN_TYPE:
				v = eh.getExperimentType();
				break;
			case GROUP_BY_IMPORT_USR_THEN_COORDINATOR:
				v = eh.getCoordinator();
				break;
			case GROUP_BY_GROUP_THEN_IMPORT_USER:
				v = eh.getImportusername();
				break;
			case GROUP_BY_TYPE_ONLY:
				v = ifNotFound;
				break;
			case GROUP_BY_TYPE_THEN_COORDINATOR:
				v = eh.getCoordinator();
				break;
			case GROUP_BY_TYPE_THEN_IMPORT_USER:
				v = eh.getImportusername();
				break;
			default:
				break;
		}
		if (v == null || v.isEmpty())
			return ifNotFound;
		else
			return v;
		
	}
	
	public String getIconForGroup1(String group) {
		switch (this) {
			case GROUP_BY_COORDINATOR_THEN_TYPE:
			case GROUP_BY_GROUP_THEN_IMPORT_USER:
			case GROUP_BY_IMPORT_USR_THEN_COORDINATOR:
				return getUserIcon(group);
			case GROUP_BY_TYPE_ONLY:
			case GROUP_BY_TYPE_THEN_COORDINATOR:
			case GROUP_BY_TYPE_THEN_IMPORT_USER:
				if (group.toUpperCase().contains("ANALYSIS RESULTS"))
					return IAPimages.getCloudResult();
				if (group.toUpperCase().startsWith("APH_") || group.toUpperCase().contains("(APH)") || group.startsWith(IAPexperimentTypes.Phytochamber + ""))
					return IAPimages.getPhytochamber();
				else
					if (group.toUpperCase().startsWith("BGH_") || group.toUpperCase().contains("(BGH)")
							|| group.startsWith(IAPexperimentTypes.BarleyGreenhouse + ""))
						return IAPimages.getBarleyGreenhouse();
					else
						if (group.toUpperCase().startsWith("CGH_") || group.toUpperCase().contains("(CGH)")
								|| group.startsWith(IAPexperimentTypes.MaizeGreenhouse + ""))
							return IAPimages.getMaizeGreenhouse();
						else
							if (group.toUpperCase().startsWith("ROOT_") || group.toUpperCase().contains("(ROOT)")
									|| group.startsWith(IAPexperimentTypes.RootWaterScan + ""))
								return IAPimages.getRoots();
							else
								if (group.startsWith(IAPexperimentTypes.LeafImages + ""))
									return IAPimages.getLeafDiseaseImage();
								else
									if (group.startsWith(IAPexperimentTypes.Raps + ""))
										return IAPimages.getRapeseedImage();
									else
										if (group.startsWith(IAPexperimentTypes.TobaccoImages + ""))
											return IAPimages.getTobaccoImage();
										else
											return "img/ext/network-workgroup.png";
		}
		return null;
	}
	
	private String getUserIcon(String group) {
		boolean nm = StringManipulationTools.count(group, "(") > 1;
		if (!nm)
			return "img/groups/Gnome-stock_person.0.png";
		if (group.contains("(") && StringManipulationTools.count(group, "(") > 1)
			group = group.substring(0, group.lastIndexOf("("));
		if (group.contains("/") && group.indexOf("/") > 1) {
			group = group.substring(group.indexOf("/") + 1);
		}
		if (group.contains("(") && group.indexOf("(") > 1) {
			group = group.substring(group.indexOf("(") + 1);
		}
		if (group.contains(")") && group.indexOf(")") > 1)
			group = group.substring(0, group.indexOf(")"));
		if (group.contains(",") && group.indexOf(",") > 1)
			group = group.substring(0, group.indexOf(","));
		group = group.trim();
		int b = 0;
		for (byte v : group.getBytes())
			b += v;
		return "img/groups/Gnome-stock_person." + ((b % 7) + 1) + ".png";
	}
	
	public String getTitleGroup1(String group) {
		String db = group;
		if (db.startsWith("APH_"))
			return "Phytoch. (20" + db.substring("APH_".length()) + ")";
		else
			if (db.startsWith("CGH_"))
				return "Maize Greenh. (20" + db.substring("CGH_".length()) + ")";
			else
				if (db.startsWith("BGH_"))
					return "Barley Greenh. (20" + db.substring("BGH_".length()) + ")";
				else
					return db;
	}
	
	public String getIconForGroup2(String group) {
		if (group.toUpperCase().contains("ANALYSIS RESULTS"))
			return IAPimages.getCloudResult();
		if (group.toUpperCase().startsWith("APH_") || group.toUpperCase().contains("(APH)") || group.startsWith(IAPexperimentTypes.Phytochamber + ""))
			return IAPimages.getPhytochamber();
		else
			if (group.toUpperCase().startsWith("BGH_") || group.toUpperCase().contains("(BGH)")
					|| group.startsWith(IAPexperimentTypes.BarleyGreenhouse + ""))
				return IAPimages.getBarleyGreenhouse();
			else
				if (group.toUpperCase().startsWith("CGH_") || group.toUpperCase().contains("(CGH)")
						|| group.startsWith(IAPexperimentTypes.MaizeGreenhouse + ""))
					return IAPimages.getMaizeGreenhouse();
				else
					if (group.toUpperCase().startsWith("ROOT_") || group.toUpperCase().contains("(ROOT)")
							|| group.startsWith(IAPexperimentTypes.RootWaterScan + ""))
						return IAPimages.getRoots();
					else
						if (group.startsWith(IAPexperimentTypes.LeafImages + ""))
							return IAPimages.getLeafDiseaseImage();
						else
							if (group.startsWith(IAPexperimentTypes.Raps + ""))
								return IAPimages.getRapeseedImage();
							else
								if (group.startsWith(IAPexperimentTypes.TobaccoImages + ""))
									return IAPimages.getTobaccoImage();
								else
									return "img/ext/network-workgroup.png";
	}
}

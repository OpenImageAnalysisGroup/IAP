package iap.blocks.data_structures;

import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class CalculatedProperty implements CalculatedPropertyDescription {
	
	private final String name;
	private final boolean isAbsolute;
	private final String desc;
	
	public CalculatedProperty(String name, boolean isAbsolute, String desc) {
		this.isAbsolute = isAbsolute;
		this.name = name;
		this.desc = desc;
	}
	
	public CalculatedProperty(String name, String desc) {
		this.isAbsolute = false;
		this.name = name;
		this.desc = desc;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isAbsoluteName() {
		return isAbsolute;
	}
	
	@Override
	public String getDescription() {
		return desc;
	}
	
	public static String getDescriptions(CalculatedPropertyDescription[] descs) {
		if (descs == null || descs.length == 0)
			return null;
		StringBuilder res = new StringBuilder();
		res.append("<ul>");
		for (CalculatedPropertyDescription d : descs)
			res.append("<li><b>" + d.getName() + "</b><br>" + d.getDescription());
		res.append("</ul>");
		return res.toString();
	}
	
	public static String getDescriptionFor(MappingDataEntity targetEntity) {
		String res = null;
		SubstanceInterface si = null;
		if (targetEntity instanceof SubstanceInterface)
			si = (SubstanceInterface) targetEntity;
		if (targetEntity instanceof ConditionInterface)
			si = ((ConditionInterface) targetEntity).getParentSubstance();
		if (si != null && si.getName() != null && !si.getName().isEmpty()) {
			res = IAPpluginManager.getInstance().getDescriptionForCalculatedProperty(si.getName());
		}
		if (res != null && !res.isEmpty())
			return res;
		else
			return null;
	}
}

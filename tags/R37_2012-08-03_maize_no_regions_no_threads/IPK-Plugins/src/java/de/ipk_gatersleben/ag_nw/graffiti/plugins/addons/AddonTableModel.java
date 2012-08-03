package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import org.graffiti.editor.GravistoService;
import org.graffiti.managers.pluginmgr.PluginDescription;

public class AddonTableModel extends DefaultTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public int getColumnCount() {
		return 3;
	}
	
	@Override
	public int getRowCount() {
		return AddonManagerPlugin.getInstance().getAddons().size();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == ManageAddonDialog.iconcolumn && AddonManagerPlugin.getInstance().getAddon(rowIndex).getIcon() != null)
			return new ImageIcon(GravistoService.getScaledImage(AddonManagerPlugin.getInstance().getAddon(rowIndex).getIcon().getImage(), 32, 32));
		if (columnIndex == ManageAddonDialog.namecolumn)
			return getDescription(AddonManagerPlugin.getInstance().getAddon(rowIndex).getDescription());
		// if(columnIndex==1) return addons.getDescription(rowIndex).getVersion();
		if (columnIndex == ManageAddonDialog.checkcolumn)
			return AddonManagerPlugin.getInstance().getAddon(rowIndex).isActive();
		return null;
	}
	
	private String getDescription(PluginDescription pd) {
		return "<html><b>" + pd.getName() + "</b>  <small>v" + pd.getVersion() + "<br>" +
							pd.getAuthor();
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == ManageAddonDialog.iconcolumn)
			return ImageIcon.class;
		if (columnIndex == ManageAddonDialog.checkcolumn)
			return Boolean.class;
		return String.class;
	}
	
	@Override
	public String getColumnName(int column) {
		if (column == ManageAddonDialog.iconcolumn)
			return "";
		if (column == ManageAddonDialog.namecolumn)
			return "Add-on";
		// if(column==1) return "Version";
		if (column == ManageAddonDialog.checkcolumn)
			return "Active";
		return "";
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return column == ManageAddonDialog.checkcolumn;
	}
	
}

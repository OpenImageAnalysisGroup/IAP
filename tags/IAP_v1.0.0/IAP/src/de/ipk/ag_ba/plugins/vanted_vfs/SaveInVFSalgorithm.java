package de.ipk.ag_ba.plugins.vanted_vfs;

import javax.swing.ImageIcon;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;

public class SaveInVFSalgorithm extends AbstractEditorAlgorithm {
	
	@Override
	public boolean activeForView(View v) {
		return v != null;
	}
	
	@Override
	public String getName() {
		return "Save in Remote Location ...";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	@Override
	public ImageIcon getIcon() {
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(
				path + "/img/save.png"));
		return icon;
	}
	
	@Override
	public boolean showMenuIcon() {
		return true;
	}
	
	@Override
	public void execute() {
		MainFrame.showMessageDialog("Not yet implemented", "ToDo");
	}
	
}

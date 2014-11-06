package de.ipk.ag_ba.plugins.vanted_vfs;

import javax.swing.ImageIcon;

import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;

import de.ipk.ag_ba.commands.AbstractGraphUrlNavigationAction;
import de.ipk.ag_ba.commands.experiment.hsm.ActionHsmDataSourceNavigation;
import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class OpenFromVFSalgorithm extends AbstractEditorAlgorithm {
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public String getName() {
		return "Open Remote Location ...";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	@Override
	public void execute() {
		IAPnavigationPanel.getIAPwindow(null, "Load Remote Dataset", 600, 480, new NavigationButtonFilter() {
			@Override
			public boolean accept(NavigationButton nb) {
				System.out.println(nb.getTitle());
				if (nb.getAction() != null)
					System.out.println(nb.getAction().getClass().getCanonicalName());
				else
					System.out.println("NULL");
				if (nb != null && nb.getAction() != null &&
						((nb.getAction() instanceof ActionHsmDataSourceNavigation) ||
						(nb.getAction() instanceof AbstractGraphUrlNavigationAction)
						)) {
					if (nb.getAction() instanceof AbstractGraphUrlNavigationAction) {
						AbstractGraphUrlNavigationAction gn = (AbstractGraphUrlNavigationAction) nb.getAction();
						gn.setLoadDirectlyInNavigator();
					}
					return true;
				} else
					return false;
			}
		}, true);
	}
	
	@Override
	public ImageIcon getIcon() {
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(
				path + "/img/load.png"));
		return icon;
	}
	
	@Override
	public boolean showMenuIcon() {
		return true;
	}
}

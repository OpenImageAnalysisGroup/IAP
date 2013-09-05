package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.JMButton;
import org.graffiti.editor.GravistoService;

public class MappingButton extends JMButton {
	private static final long serialVersionUID = 2330434266087612795L;
	
	private AbstractExperimentDataProcessor p;
	
	public MappingButton(AbstractExperimentDataProcessor p) {
		super("<html>" + p.getName());
		setOpaque(false);
		this.p = p;
	}
	
	public AbstractExperimentDataProcessor getExperimentProcessor() {
		return p;
	}
	
	@Override
	public Icon getIcon() {
		ImageIcon i = p.getIcon();
		if (i == null)
			return null;
		else
			return new ImageIcon(GravistoService.getScaledImage(i.getImage(), -1, 32));
	}
}

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class PutIntoSidePanel extends AbstractExperimentDataProcessor {
	private ExperimentInterface md;
	private JComponent optGui;
	
	public PutIntoSidePanel() {
		super();
	}
	
	public PutIntoSidePanel(boolean register) {
		super(register);
	}
	
	@Override
	public String getName() {
		return "Put data in 'Experiments' tab";
	}
	
	@Override
	public void processData() {
		String tabtitle;
		try {
			tabtitle = md.getName();
		} catch (Exception e) {
			tabtitle = null;
		}
		if (tabtitle == null || tabtitle.equals(""))
			tabtitle = ExperimentInterface.UNSPECIFIED_EXPERIMENTNAME;
		
		TabDBE.addOrUpdateExperimentPane(new ProjectEntity(tabtitle, md, optGui));
	}
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public void setExperimentData(ExperimentInterface md) {
		this.md = md;
	}
	
	@Override
	public void setComponent(JComponent optSupplementaryPanel) {
		this.optGui = optSupplementaryPanel;
	}
	
	@Override
	public ImageIcon getIcon() {
		return new ImageIcon(GravistoService.getResource(getClass(), "putintosidepanel.png", null));
	}
}

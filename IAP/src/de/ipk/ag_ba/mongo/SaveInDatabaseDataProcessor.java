package de.ipk.ag_ba.mongo;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.view.View;

import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.postgresql.CommandLineBackgroundTaskStatusProvider;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;

public class SaveInDatabaseDataProcessor extends AbstractExperimentDataProcessor {
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public String getName() {
		return "Transfer to IAP Cloud Storage";
	}
	
	@Override
	protected void processData() {
		MyExperimentInfoPanel panel = new MyExperimentInfoPanel();
		MongoDB m;
		
		mappingData = mappingData.clone();
		mappingData.getHeader().setExcelfileid("");
		
		Object[] sel = MyInputHelper.getInput("Select the database-target:", "Target Selection", new Object[] {
				"Target", MongoDB.getMongos()
		});
		
		if (sel == null)
			return;
		
		m = (MongoDB) sel[0];
		
		panel.setCancelText("Revert Changes");
		panel.setExperimentInfo(m, mappingData.getHeader(), true, mappingData);
		
		sel = MyInputHelper.getInput("[Store in database;Cancel]Modify dataset before upload:", "Copy into IAP Cloud Storage", new Object[] {
				"", panel
		});
		
		if (sel == null) {
			mappingData = null;
		} else {
			try {
				m.saveExperiment(mappingData, new CommandLineBackgroundTaskStatusProvider(true));
				mappingData = null;
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	BufferedImage bc = null;
	private ExperimentInterface mappingData;
	
	@Override
	public ImageIcon getIcon() {
		if (bc != null)
			return new ImageIcon(bc);
		bc = ImageOperation.blur(GravistoService.getBufferedImage(GravistoService.loadIcon(IAPmain.class, "img/ext/network-mongo.png").getImage()));
		
		return new ImageIcon(bc);
	}
	
	@Override
	public void setExperimentData(ExperimentInterface mappingData) {
		this.mappingData = mappingData;
	}
	
}

package de.ipk.ag_ba.mongo;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.view.View;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.ExperimentHeaderInfoPanel;
import de.ipk.ag_ba.gui.webstart.IAPmain;
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
		return "Copy to IAP Storage";
	}
	
	@Override
	protected void processData() {
		try {
			ExperimentHeaderInfoPanel panel = new ExperimentHeaderInfoPanel();
			MongoDB m = null;
			
			mappingData = mappingData.clone();
			mappingData.getHeader().setDatabaseId("");
			
			Object[] sel = MyInputHelper.getInput("Select the database-target:", "Storage System", new Object[] {
					"MongoDB", MongoDB.getMongos(),
					"Store not in MongoDB but in VFS?", false,
					"VFS", VirtualFileSystem.getKnown(true, true)
			});
			
			if (sel == null)
				return;
			
			boolean useVFS = (Boolean) sel[1];
			if (useVFS) {
				panel.setCancelText("Revert Changes");
				panel.setExperimentInfo(null, mappingData.getHeader(), true, mappingData);
				
				VirtualFileSystem vfs = (VirtualFileSystem) sel[2];
				
				sel = MyInputHelper.getInput("[Store in database;Cancel]You may modify the dataset annotation before storage:", "Copy to IAP Storage",
						new Object[] {
								"", panel
						});
				
				if (sel == null) {
					mappingData = null;
				} else {
					try {
						ExperimentReference er = new ExperimentReference(mappingData);
						vfs.saveExperiment(null, er, new CommandLineBackgroundTaskStatusProvider(true), false);
						mappingData = null;
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			} else {
				m = (MongoDB) sel[0];
				
				panel.setCancelText("Revert Changes");
				panel.setExperimentInfo(m, mappingData.getHeader(), true, mappingData);
				
				sel = MyInputHelper.getInput("[Store in database;Cancel]You may modify dataset before storage:", "Copy into IAP Cloud Storage", new Object[] {
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
		} finally {
			mappingData = null;
		}
	}
	
	BufferedImage bc = null;
	private ExperimentInterface mappingData;
	
	@Override
	public ImageIcon getIcon() {
		if (bc != null)
			return new ImageIcon(bc);
		bc = GravistoService.getBufferedImage(GravistoService.loadIcon(IAPmain.class, "img/ext/network-mongo.png").getImage());
		// bc = ImageOperation.blur(GravistoService.getBufferedImage(GravistoService.loadIcon(IAPmain.class, "img/ext/network-mongo.png").getImage()));
		
		return new ImageIcon(bc);
	}
	
	@Override
	public void setExperimentData(ExperimentInterface mappingData) {
		this.mappingData = mappingData;
	}
	
}

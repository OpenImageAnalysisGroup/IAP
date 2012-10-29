/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe;

import java.util.ArrayList;

import javax.swing.JLabel;

import org.FolderPanel;
import org.GuiRow;
import org.HelperClass;
import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_brite.BriteService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class DatabaseFileStatusService implements HelperClass {
	
	public static void showStatusDialog() {
		if (BackgroundTaskHelper.isTaskWithGivenReferenceRunning("Download")) {
			MainFrame.showMessageDialog("Please wait until the active download is finished.", "Download in progress");
			return;
		}
		final FolderPanel exp = new FolderPanel("");
		
		ArrayList<FileDownloadStatusInformationProvider> statusProviders =
				new ArrayList<FileDownloadStatusInformationProvider>();
		
		statusProviders.add(new EnzymeService());
		statusProviders.add(new BriteService());
		// statusProviders.add(new CompoundService());
		// statusProviders.add(new KoService());
		// if (ReleaseInfo.getRunningReleaseStatus()!=Release.KGML_EDITOR)
		// if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG)
		// statusProviders.add(new TranspathService());
		
		exp.setFrameColor(null, null, 0, 0);
		exp.addGuiComponentRow(
				null,
				new JLabel(
						"<html><br><font color='#BB22222'>&nbsp;&nbsp;Important: Evaluate website, license and/or disclaimers before downloading database files.&nbsp;&nbsp;<br><br>"),
				false);
		final int b = 5; // border
		for (FileDownloadStatusInformationProvider sp : statusProviders) {
			final GuiRow guiRow = new GuiRow(new JLabel(sp.getDescription()),
					FolderPanel.getBorderedComponent(sp.getStatusPane(true), b, b, b, b));
			exp.addGuiComponentRow(guiRow, true);
			final FileDownloadStatusInformationProvider spf = sp;
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// empty
					}
					exp.exchangeGuiComponentRow(guiRow, new GuiRow(new JLabel(spf.getDescription()),
							FolderPanel.getBorderedComponent(spf.getStatusPane(false), b, b, b, b)),
							true);
					exp.dialogSizeUpdate();
				}
			};
			Thread t = new Thread(r);
			t.start();
		}
		exp.layoutRows();
		exp.setBackground(null);
		MainFrame.showMessageDialogPlain("Database Status", exp);
	}
	
}

package de.ipk.ag_ba.mongo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.view.View;

import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class SaveAsCsvDataProcessor extends AbstractExperimentDataProcessor {
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public String getName() {
		return "Save XLSX Spreadsheet File";
	}
	
	@Override
	protected void processData() {
		try {
			String fn = FileHelper.getFileName(
					"xlsx",
					"XLSX Spreadsheet File",
					ActionPdfCreation3.replaceInvalidChars(mappingData.getName()));
			if (fn == null)
				return;
			BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Processing data", "");
			Runnable task = getTask(fn, status, mappingData);
			BackgroundTaskHelper.issueSimpleTask("Create XLSX file", "Processing data", task, null, status);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		} finally {
			mappingData = null;
		}
	}
	
	private Runnable getTask(
			final String fn,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final ExperimentInterface mappingData) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					createXLSX(fn, status, mappingData);
					status.setCurrentStatusText1("Processing finished");
					status.setCurrentStatusText2("Opening target folder...");
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		};
	}
	
	private void createXLSX(
			String fn,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			ExperimentInterface mappingData) throws Exception {
		boolean xlsx = true;
		HashMap<String, Integer> indexInfo = new HashMap<String, Integer>();
		boolean exportIndividualAngles = false;
		LinkedList<SnapshotDataIAP> snapshots = IAPservice.getSnapshotsFromExperiment(
				null, mappingData, indexInfo, false,
				exportIndividualAngles, xlsx, null, status, null);
		
		TreeMap<Integer, String> cola = new TreeMap<Integer, String>();
		for (String val : indexInfo.keySet())
			cola.put(indexInfo.get(val), val);
		StringBuilder indexHeader = new StringBuilder();
		for (String val : cola.values())
			indexHeader.append(ActionPdfCreation3.separator + val);
		
		StringBuilder csv = new StringBuilder();
		String csvHeader = ActionPdfCreation3.getCSVheader();
		
		csvHeader = StringManipulationTools.stringReplace(csvHeader, "\r\n", "");
		csvHeader = StringManipulationTools.stringReplace(csvHeader, "\n", "");
		csv.append(csvHeader + indexHeader.toString() + "\r\n");
		
		System.out.println(SystemAnalysis.getCurrentTime() +
				">Snapshot data set has been created (" + snapshots.size() + " snapshots)");
		status.setCurrentStatusText2("Snapshot data set has been created (" + snapshots.size() + " snapshots)");
		
		Workbook wb = xlsx ? new XSSFWorkbook() : null;
		Sheet sheet = xlsx ? wb.createSheet(ActionPdfCreation3.replaceInvalidChars(mappingData.getName())) : null;
		ArrayList<String> excelColumnHeaders = new ArrayList<String>();
		if (sheet != null) {
			// create Header row
			Row row = sheet.createRow(0);
			int col = 0;
			String c = csv.toString().trim();
			c = StringManipulationTools.stringReplace(c, "\r\n", "");
			c = StringManipulationTools.stringReplace(c, "\n", "");
			for (String h : c.split(ActionPdfCreation3.separator)) {
				row.createCell(col++).setCellValue(h);
				excelColumnHeaders.add(h);
			}
			
			mappingData = null;
			ActionPdfCreation3.setExcelSheetValues(
					snapshots, sheet, excelColumnHeaders, status);
			wb.write(new FileOutputStream(fn));
			String tempDirectory = new File(fn).getParent();
			AttributeHelper.showInFileBrowser(tempDirectory + "", new File(fn).getName());
		}
	}
	
	BufferedImage bc = null;
	private ExperimentInterface mappingData;
	
	@Override
	public ImageIcon getIcon() {
		if (bc != null)
			return new ImageIcon(bc);
		bc = GravistoService.getBufferedImage(GravistoService.loadIcon(IAPmain.class, "img/ext/applications-office.png").getImage());
		return new ImageIcon(bc);
	}
	
	@Override
	public void setExperimentData(ExperimentInterface mappingData) {
		this.mappingData = mappingData;
	}
}

/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.MultiFileSelectionParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.OpenExcelFileDialogService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

/**
 * @author Christian Klukas
 *         (c) 2006, 2007 IPK Gatersleben, Group Network Analysis
 */
public class ReplaceLabelAlgorithm extends AbstractAlgorithm {
	
	private boolean ignoreFirstRow = true;
	private boolean left2right = true;
	private boolean saveLabel = true;
	private ArrayList<IOurl> urls = new ArrayList<IOurl>();
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Replace labels using file tables...";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"This command reads (multiple) mapping tables<br>" +
							" and uses these to replace the labels.<br>" +
							"<br>" +
							"Layout of input file:<br><br>" +
							"<code>" +
							"[Label in Graph | New Label]<br>" +
							"Graph label 1| new label<br>" +
							"Graph label 2| new label<br>" +
							"Graph label 3| new label<br>" +
							"</code><br><br>";
	}
	
	@Override
	public String getCategory() {
		return null;// "Elements";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new BooleanParameter(ignoreFirstRow, "Skip first row", "<html>" +
												"If enabled, the first row is not processed.<br>" +
												"Useful in case the first row contains column headers."),
							new BooleanParameter(left2right, "Old label in column 1, new label in column 2", "<html>" +
												"If enabled, the current label is expected in column 1 and the new one in column 2.<br>" +
												"If disabled, the current label is expected in column 2 and the new one in column 1."),
							new BooleanParameter(saveLabel, "Memorize current label", "<html>" +
												"If enabled, the current label is stored within a newly created attribute.<br>" +
												"Use command <b>Elements/Restore Label</b> to revert the label changes."),
							new MultiFileSelectionParameter(urls, "Mapping Table Files", "tooltip", OpenExcelFileDialogService.EXCELFILE_EXTENSIONS,
												OpenExcelFileDialogService.SPREADSHEET_DESCRIPTION, true) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		ignoreFirstRow = ((BooleanParameter) params[i++]).getBoolean();
		left2right = ((BooleanParameter) params[i++]).getBoolean();
		saveLabel = ((BooleanParameter) params[i++]).getBoolean();
		urls = ((MultiFileSelectionParameter) params[i++]).getFileList();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		Collection<File> excelFiles = new ArrayList<File>();
		for (IOurl u : urls)
			excelFiles.add(FileSystemHandler.getFile(u));
		if (excelFiles != null && excelFiles.size() > 0) {
			HashMap<String, String> id2alternatives = new HashMap<String, String>();
			
			for (File excelFile : excelFiles) {
				TableData myData = ExperimentDataFileReader.getExcelTableData(excelFile);
				
				int startRow = (ignoreFirstRow ? 2 : 1);
				
				for (int row = startRow; row <= myData.getMaximumRow(); row++) {
					String currentName;
					String newName;
					if (left2right) {
						currentName = myData.getUnicodeStringCellData(1, row);
						newName = myData.getUnicodeStringCellData(2, row);
					} else {
						newName = myData.getUnicodeStringCellData(1, row);
						currentName = myData.getUnicodeStringCellData(2, row);
					}
					id2alternatives.put(currentName, newName);
				}
			}
			int idCnt = 0;
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR && ge instanceof Edge)
					continue;
				String label = AttributeHelper.getLabel(ge, null);
				if (label != null) {
					if (id2alternatives.containsKey(label)) {
						if (saveLabel) {
							AttributeHelper.setAttribute(ge, "", "oldlabel", label);
						}
						AttributeHelper.setLabel(ge, id2alternatives.get(label));
						idCnt++;
					}
				}
			}
			MainFrame.showMessageDialog(idCnt + " labels have been replaced (" + (saveLabel ? "old label text is saved" : "old label text is not saved") + ")",
								"Information");
		}
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}

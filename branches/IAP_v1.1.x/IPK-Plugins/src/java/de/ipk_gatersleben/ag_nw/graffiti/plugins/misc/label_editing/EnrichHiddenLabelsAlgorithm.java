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

import org.AlignmentSetting;
import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
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
public class EnrichHiddenLabelsAlgorithm extends AbstractAlgorithm {
	
	private boolean ignoreFirstRow = true, removeallhiddenlabels;
	private ArrayList<IOurl> urls = new ArrayList<IOurl>();
	
	public String getName() {
		return "Add hidden labels using file tables...";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"This command reads (multiple) mapping table files<br>" +
							"and uses these to add hidden labels, e.g. in order<br>" +
							"to prepare networks for more flexible data mapping.<br>" +
							"<br>" +
							"Layout of input file:<br><br>" +
							"<code>" +
							"[Label in Graph | hidden Label 1 | ...]<br>" +
							"Graph label 1| hidden label| ...<br>" +
							"Graph label 2| hidden label| ...<br>" +
							"Graph label 3| hidden label| ...<br>" +
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
							new BooleanParameter(removeallhiddenlabels, "Remove old labels",
												"<html>if enabled, all existing hidden labels will be deleted before adding new labels.<br>" +
																	"Otherwise the new labels will be appended"),
							new MultiFileSelectionParameter(urls, "Table Files", "Select the list of mapping table files to be used",
												OpenExcelFileDialogService.EXCELFILE_EXTENSIONS, OpenExcelFileDialogService.SPREADSHEET_DESCRIPTION, true) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		ignoreFirstRow = ((BooleanParameter) params[i++]).getBoolean();
		removeallhiddenlabels = ((BooleanParameter) params[i++]).getBoolean();
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
			HashMap<String, ArrayList<String>> id2alternatives = new HashMap<String, ArrayList<String>>();
			
			for (File excelFile : excelFiles) {
				TableData myData = ExperimentDataFileReader.getExcelTableData(excelFile);
				
				int startRow = (ignoreFirstRow ? 2 : 1);
				
				for (int row = startRow; row <= myData.getMaximumRow(); row++) {
					String currentName = myData.getUnicodeStringCellData(1, row);
					int cntCols = 2;
					String cell = myData.getUnicodeStringCellData(cntCols, row);
					while (cell != null) {
						if (!id2alternatives.containsKey(currentName))
							id2alternatives.put(currentName, new ArrayList<String>());
						id2alternatives.get(currentName).add(cell);
						cell = myData.getUnicodeStringCellData(++cntCols, row);
						
					}
				}
			}
			int addedCnt = 0, deletedCnt = 0;
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR && ge instanceof Edge)
					continue;
				String label = AttributeHelper.getLabel(ge, null);
				if (label != null) {
					if (id2alternatives.containsKey(label)) {
						if (removeallhiddenlabels) {
							for (int k = 1; k < 100; k++)
								if (AttributeHelper.hasAttribute(ge, GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k))) {
									ge.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k));
									deletedCnt++;
								}
						}
						int k = 1;
						while (AttributeHelper.hasAttribute(ge, GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k)))
							k++;
						
						if (ge instanceof Edge)
							AttributeHelper.setLabel(ge, id2alternatives.get(label).get(0));
						else {
							for (String s : id2alternatives.get(label)) {
								AttributeHelper.setLabel(k++, (Node) ge, s, null, AlignmentSetting.HIDDEN.toGMLstring());
								addedCnt++;
							}
						}
					}
				}
			}
			MainFrame.showMessageDialog("<html>" + (deletedCnt > 0 ? deletedCnt + " hidden labels have been deleted,<br>" : "") + addedCnt
								+ " labels have been added to graph<p><i>" + graph.getName(), "Information");
		}
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}

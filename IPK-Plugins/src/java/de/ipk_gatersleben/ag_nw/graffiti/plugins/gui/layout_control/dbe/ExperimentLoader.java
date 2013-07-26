package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression.KeggExpressionConverter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression.KeggExpressionDataset;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression.KeggExpressionReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression.TextFileColumnInformation;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ExperimentLoader {
	
	public static final String[] supportedMagicFieldStrings = new String[] { "V1.0", "V1.0T", "V1.1", "V1.1T", "V1.2", "V1.2T" };
	
	public static boolean canLoadFile(File f) {
		String fileName = f.getName();
		if (fileName.toUpperCase().endsWith(".XLSX") ||
				fileName.toUpperCase().endsWith(".XLS") ||
				fileName.toUpperCase().endsWith(".DAT") ||
				fileName.toUpperCase().endsWith(".TXT") ||
				fileName.toUpperCase().endsWith(".BIN") ||
				fileName.toUpperCase().endsWith(".CSV")) {
			
			if (fileName.toUpperCase().endsWith(".XLS") || fileName.toUpperCase().endsWith(".XLSX")) {
				TableData td = ExperimentDataFileReader.getExcelTableDataPeak(f, 5);
				if (td == null)
					return false;
				boolean oneStartsWithV = false;
				String vv = td.getUnicodeStringCellData(10, 4);
				if (vv != null && vv.startsWith("V"))
					oneStartsWithV = true;
				if (vv != null)
					for (String field : supportedMagicFieldStrings)
						if (field.equals(vv))
							return true;
				vv = td.getUnicodeStringCellData(9, 4);
				if (vv != null && vv.startsWith("V"))
					oneStartsWithV = true;
				if (vv != null)
					for (String field : supportedMagicFieldStrings)
						if (field.equals(vv))
							return true;
				
				if (oneStartsWithV)
					return false; // another template loader, which uses the magic
				// field, will load the file
				else
					return true;
			}
			
			return true; // will read csv, txt, ...
		}
		return false;
	}
	
	public static void loadFile(final Collection<File> fileList, final ExperimentDataPresenter receiver) {
		synchronized (ExperimentLoader.class) {
			final ArrayList<File> keggExpressionFiles = new ArrayList<File>();
			final ArrayList<File> rawTextFiles = new ArrayList<File>();
			for (File f : fileList) {
				String fileName = f.getName();
				if (fileName.toUpperCase().endsWith(".DAT")) {
					keggExpressionFiles.add(f);
				}
				if (fileName.toUpperCase().endsWith(".TXT") || fileName.toUpperCase().endsWith(".CSV")) {
					rawTextFiles.add(f);
				}
			}
			fileList.removeAll(keggExpressionFiles);
			fileList.removeAll(rawTextFiles);
			
			final HashMap<File, ArrayList<TextFileColumnInformation>> rawFile2relevantColumns =
					new HashMap<File, ArrayList<TextFileColumnInformation>>();
			
			final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Process Data...", "");
			BackgroundTaskHelper.issueSimpleTask("Dataset-Loader", "Process Data...",
					new Runnable() {
						@Override
						public void run() {
							HashMap<File, TableData> unformatedExcelData = new HashMap<File, TableData>();
							ExperimentLoader.processExcelTemplateFiles(fileList, status, receiver, unformatedExcelData);
							status.setCurrentStatusText2("");
							ExperimentLoader.processRawAndExpressionTextFiles(keggExpressionFiles, rawTextFiles, unformatedExcelData,
									rawFile2relevantColumns, status, receiver);
						}
					},
					null, status);
		}
	}
	
	public synchronized static void loadExcelFileWithBackGroundService(
			final ExperimentDataFileReader excelReader,
			final TableData myData,
			final File excelFile,
			final RunnableWithXMLexperimentData finishTask) {
		MainFrame.showMessage("Load project data from Excel File...",
				MessageType.PERMANENT_INFO);
		BackgroundTaskHelper excelLoader = new BackgroundTaskHelper(
				new Runnable() {
					@Override
					public void run() {
						try {
							ExperimentInterface md = excelReader.getXMLdata(excelFile, myData,
									new BackgroundTaskStatusProviderSupportingExternalCallImpl("Load Data...", ""));
							finishTask.setExperimenData(md);
							finishTask.run();
						} catch (final Exception err) {
							ErrorMsg.addErrorMessage(err);
							finishTask.setExperimenData(null);
							finishTask.run();
						}
					}
				}, excelReader, "Construct Dataset",
				(excelFile != null ? "<html>Read Excel File " + excelFile.getName() : "Process Data"), true, false);
		excelLoader.startWork(MainFrame.getInstance());
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				excelLoader.getRunThread().join();
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			}
		}
	}
	
	static void processRawTextFiles(ArrayList<File> rawTextFiles,
			ArrayList<KeggExpressionDataset> datasets, BackgroundTaskStatusProviderSupportingExternalCall status,
			HashMap<File, ArrayList<TextFileColumnInformation>> rawTextFile2relevantColumns, HashMap<File, Boolean> transposeContent) {
		for (File kef : rawTextFiles) {
			if (rawTextFile2relevantColumns.get(kef) == null)
				continue;
			status.setCurrentStatusText2("Read content of file: " + kef.getName());
			TableData expdata = ExperimentDataFileReader.
					getExcelTableData(kef, -1, rawTextFile2relevantColumns.get(kef), status);
			if (transposeContent.containsKey(kef)) {
				if (transposeContent.get(kef))
					expdata = expdata.getTransposedDataset();
			}
			KeggExpressionReader ker = new KeggExpressionReader(expdata);
			String organism = ker.getOrganism();
			// 0404-1(COL_GP_A_7)_Signal 0404-1(COL_GP_A_7)_Detection
			// 0404-10(CRO_MX_A_34)_Signal 0404-10(CRO_MX_A_34)_Detection
			// AFFX-BioB-5_at 175.6 P 196.4 P
			
			int colGeneId = 1;
			for (TextFileColumnInformation ci : rawTextFile2relevantColumns.get(kef)) {
				if (ci == null)
					continue;
				KeggExpressionDataset ked = new KeggExpressionDataset(ci.getName(), false, "");
				datasets.add(ked);
				int colDataSignal = ci.getSignalColumn();
				Integer colDataSignalQuality = ci.getDetectionColumn();
				status.setCurrentStatusText2("Process File: " + kef.getName() +
						", column(s) " + colDataSignal + "/" + (colDataSignalQuality != null ? colDataSignalQuality : "-"));
				int headerRow = 1;
				for (int row = headerRow + 1; row <= expdata.getMaximumRow(); row++) {
					String geneId;
					String geneIdInInputFile = expdata.getUnicodeStringCellData(colGeneId, row);
					if (geneIdInInputFile == null || geneIdInInputFile.length() <= 0) {
						ErrorMsg.addErrorMessage("Gene Id missing in line " + row + " (file " + kef.getName() + "). Ignoring input data in this row!");
						status.setCurrentStatusText2("Gene Id missing in line " + row + ". Check error-log!");
					} else {
						if (organism != null && organism.length() > 0)
							geneId = organism + ":" + geneIdInInputFile;
						else
							geneId = geneIdInInputFile;
						Object val = expdata.getCellData(colDataSignal, row, null);
						if (val != null) {
							try {
								Double cSig;
								if (val instanceof Double)
									cSig = (Double) val;
								else
									if (val instanceof String) {
										String mes_val_s = (String) val;
										if (mes_val_s.equalsIgnoreCase("-") || mes_val_s.equalsIgnoreCase("n/a") || mes_val_s.equalsIgnoreCase("na"))
											cSig = Double.NaN;
										else {
											cSig = null;
											boolean addNonNumeric = true;
											if (addNonNumeric) {
												ked.addDatapoint(geneId, null, null, null, null,
														Double.NaN, Double.NaN, Double.NaN, Double.NaN, mes_val_s);
											} else
												ErrorMsg.addErrorMessage("Invalid dataformat (non-numeric, non 'n/a', 'na', '-') in column " + colDataSignal + ", row "
														+ row
														+ ", content: " + val);
										}
									} else {
										cSig = null;
										ErrorMsg.addErrorMessage("Invalid dataformat (non-numeric) in column " + colDataSignal + ", row " + row + ", content: " + val);
									}
								if (cSig != null) {
									String signalQuality = null;
									if (colDataSignalQuality != null) {
										signalQuality = expdata.getUnicodeStringCellData(colDataSignalQuality, row);
									}
									ked.addDatapoint(geneId, null, null, null, null, cSig, Double.NaN, Double.NaN, Double.NaN, signalQuality);
								}
							} catch (ClassCastException cce) {
								ErrorMsg.addErrorMessage("Invalid dataformat (non-numeric) in column " + colDataSignal + ", row " + row + ", content: " + val);
							}
						}
					}
				}
			}
		}
		status.setCurrentStatusText2("");
	}
	
	static void processKeggExpressionTextFiles(ArrayList<File> keggExpressionFiles, ArrayList<KeggExpressionDataset> datasets,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		for (File kef : keggExpressionFiles) {
			status.setCurrentStatusText2("Process File: " + kef.getName());
			TableData expdata = ExperimentDataFileReader.getExcelTableData(kef, null);
			KeggExpressionReader ker = new KeggExpressionReader(expdata);
			String organism = ker.getOrganism();
			
			KeggExpressionDataset ked = new KeggExpressionDataset(kef.getName(), true, organism);
			datasets.add(ked);
			
			// #ORF x y Control-sig Control-bkg Target-sig Target-bkg
			int colORF = ker.getColumn("ORF");
			int colX = ker.getColumn("X");
			int colY = ker.getColumn("Y");
			int colControlSig = ker.getColumn("Control-sig");
			int colControlBgk = ker.getColumn("Control-bkg");
			int colTargetSig = ker.getColumn("Target-sig");
			int colTargetBgk = ker.getColumn("Target-bkg");
			int headerRow = ker.findCommentRowStartingWith("ORF");
			for (int row = headerRow + 1; row <= expdata.getMaximumRow(); row++) {
				status.setCurrentStatusText2("Process File: " + kef.getName() + ", row " + row);
				String geneId = /* organism + ":" + */expdata.getUnicodeStringCellData(colORF, row);
				Double x = (Double) expdata.getCellData(colX, row, null);
				Double y = (Double) expdata.getCellData(colY, row, null);
				Double cSig = (Double) expdata.getCellData(colControlSig, row, null);
				Double cBgk = (Double) expdata.getCellData(colControlBgk, row, null);
				Double tSig = (Double) expdata.getCellData(colTargetSig, row, null);
				Double tBgk = (Double) expdata.getCellData(colTargetBgk, row, null);
				Double c = cSig - cBgk;
				Double t = tSig - tBgk;
				if (c < 0)
					c = 1d;
				if (t < 0)
					t = 1d;
				ked.addDatapoint(geneId, cSig, cBgk, tSig, tBgk, c, t, x, y, null);
			}
		}
		status.setCurrentStatusText2("");
	}
	
	protected static void loadExcelOrBinaryFile(
			File excelOrBinaryFile,
			final ExperimentDataPresenter receiver,
			HashMap<File, TableData> rawTextFilesReceiver) throws JDOMException {
		if (excelOrBinaryFile != null) {
			final String experimentName = excelOrBinaryFile.getName();
			if (experimentName.toUpperCase().endsWith(".BIN")) {
				InputStream in = null;
				try {
					// SAXBuilder builder = new SAXBuilder();
					try {
						in = new FileInputStream(excelOrBinaryFile);
						// org.jdom.Document binDoc = builder.build(in);
						// Document w3Doc = JDOM2DOM.getDOMfromJDOM(binDoc);
						
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document w3Doc = builder.parse(in);
						Experiment md = Experiment.getExperimentFromDOM(w3Doc, null);
						receiver.processReceivedData(null, excelOrBinaryFile.getName(), md, null);
						// } catch (JDOMException e) {
						// ErrorMsg.addErrorMessage(e);
					} catch (IOException e) {
						ErrorMsg.addErrorMessage(e);
					} catch (ParserConfigurationException e) {
						ErrorMsg.addErrorMessage(e);
					} catch (SAXException e) {
						ErrorMsg.addErrorMessage(e);
					}
				} finally {
					if (in != null)
						try {
							in.close();
						} catch (IOException e) {
							ErrorMsg.addErrorMessage(e);
						}
				}
			} else {
				ExperimentDataFileReader gefr = null;
				
				final TableData myData = ExperimentDataFileReader.getExcelTableData(excelOrBinaryFile, null);
				boolean loadDBEform = myData.isDBEinputForm();
				if (loadDBEform) {
					gefr = new DBEinputFileReader();
				} else {
					if (myData.isGeneExpressionFileFormatForm())
						gefr = new GeneExpressionFileReader();
					else {
						gefr = null;
						rawTextFilesReceiver.put(excelOrBinaryFile, myData);
					}
				}
				if (gefr != null)
					loadExcelFileWithBackGroundService(
							gefr,
							myData,
							excelOrBinaryFile,
							new RunnableWithXMLexperimentData() {
								private ExperimentInterface md = null;
								
								/**
								 * <code>setExperimentData</code> will be
								 * automatically
								 * called before this method is called.
								 * This is a two step solution as the loading
								 * of the
								 * data is done in background.
								 */
								@Override
								public void run() {
									receiver.processReceivedData(myData, experimentName, md, null);
								}
								
								@Override
								public void setExperimenData(ExperimentInterface md) {
									this.md = md;
								}
							});
			}
		}
	}
	
	static void processExcelTemplateFiles(final Collection<File> fileList,
			final BackgroundTaskStatusProviderSupportingExternalCallImpl status,
			ExperimentDataPresenter receiver,
			HashMap<File, TableData> rawTextFilesReceiver) {
		synchronized (ExperimentLoader.class) {
			for (File f : fileList) {
				status.setCurrentStatusText2("Read File: " + f.getName());
				try {
					loadExcelOrBinaryFile(f, receiver, rawTextFilesReceiver);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				status.setCurrentStatusText2("Process Data of File: " + f.getName());
			}
		}
	}
	
	private static void processRawAndExpressionTextFiles(
			final ArrayList<File> keggExpressionFiles,
			final ArrayList<File> rawTextFiles,
			final HashMap<File, TableData> unformatedExcelData,
			final HashMap<File, ArrayList<TextFileColumnInformation>> rawFile2relevantColumns,
			final BackgroundTaskStatusProviderSupportingExternalCallImpl status,
			final ExperimentDataPresenter receiver) {
		synchronized (ExperimentLoader.class) {
			HashMap<File, Boolean> transposeContent = new HashMap<File, Boolean>();
			rawTextFiles.addAll(unformatedExcelData.keySet());
			if (keggExpressionFiles.size() > 0 || rawTextFiles.size() > 0) {
				for (File rtf : rawTextFiles) {
					TableData expdata = unformatedExcelData.get(rtf);
					if (expdata == null)
						expdata = ExperimentDataFileReader.getExcelTableDataPeak(rtf, 1); // read
					// first
					// 2
					// rows
					Object[] res = MyInputHelper.getInput(
							"Raw text file " + rtf.getName() + " has been analyzed.<br>" +
									"It contains " + expdata.getMaximumCol() + " columns.<br>" +
									"Different lines, treatments and/or time points should be in separate columns,<br>" +
									"measured substances should be in different rows.<br>" +
									"The first column should contain the measured substances identifiers.<br>" +
									"<br>" +
									"Optionally you may now transpose the input file content.",
							"Text file analyzed. Transpose?", new Object[] {
									"Transpose Content", false
							});
					if (res == null) {
						MainFrame.showMessageDialog("File " + rtf.getName() + " will not be processed!", "Information");
					} else {
						if ((Boolean) res[0]) {
							transposeContent.put(rtf, true);
							expdata = ExperimentDataFileReader.getExcelTableData(rtf, 0, null, null); // read
							// fully
							expdata = expdata.getTransposedDataset(); // and transpose
						}
						KeggExpressionReader ker = new KeggExpressionReader(expdata);
						ArrayList<TextFileColumnInformation> relevantColumns = ker.getRawTextFileColumnInformation(true);
						rawFile2relevantColumns.put(rtf, relevantColumns);
					}
				}
				ArrayList<KeggExpressionDataset> datasets = new ArrayList<KeggExpressionDataset>();
				processKeggExpressionTextFiles(keggExpressionFiles, datasets, status);
				processRawTextFiles(rawTextFiles, datasets, status, rawFile2relevantColumns, transposeContent);
				if (status.wantsToStop())
					return;
				if (datasets.size() > 0) {
					KeggExpressionConverter kec = new KeggExpressionConverter(datasets);
					kec.getDescriptionDataFromUser();
					final TableData resultingData = kec.getDatasetTable();
					if (resultingData != null) {
						ExperimentDataFileReader gefr = new GeneExpressionFileReader();
						gefr.setCoordinator(kec.getDesiredCoordinatorValue());
						gefr.setExperimentName(kec.getDesiredExperimentName());
						gefr.setTimeUnit(kec.getDesiredTimeUnit());
						gefr.setMeasurementUnit(kec.getDesiredMeasurementUnit());
						final String experimentName = kec.getDesiredExperimentName();
						loadExcelFileWithBackGroundService(
								gefr,
								resultingData,
								null,
								new RunnableWithXMLexperimentData() {
									private ExperimentInterface md = null;
									
									@Override
									public void run() {
										receiver.processReceivedData(resultingData, experimentName, md, null);
									}
									
									@Override
									public void setExperimenData(ExperimentInterface md) {
										this.md = md;
									}
								});
					}
				}
			}
		}
	}
	
	static void processRawStringTableData(String tabledata,
			ArrayList<KeggExpressionDataset> datasets, BackgroundTaskStatusProviderSupportingExternalCall status,
			ArrayList<TextFileColumnInformation> relevantColumns) {
		status.setCurrentStatusText2("Analyse Table Data (String)");
		TableData expdata = ExperimentDataFileReader.
				getExcelTableData(tabledata, -1, relevantColumns, status);
		KeggExpressionReader ker = new KeggExpressionReader(expdata);
		String organism = ker.getOrganism();
		// 0404-1(COL_GP_A_7)_Signal 0404-1(COL_GP_A_7)_Detection
		// 0404-10(CRO_MX_A_34)_Signal 0404-10(CRO_MX_A_34)_Detection
		// AFFX-BioB-5_at 175.6 P 196.4 P
		
		int colGeneId = 1;
		for (TextFileColumnInformation ci : relevantColumns) {
			KeggExpressionDataset ked = new KeggExpressionDataset(ci.getName(), false, "");
			datasets.add(ked);
			int colDataSignal = ci.getSignalColumn();
			Integer colDataSignalQuality = ci.getDetectionColumn();
			status.setCurrentStatusText2("Process Table Data: " +
					"column(s) " + colDataSignal + "/" + (colDataSignalQuality != null ? colDataSignalQuality : "-"));
			int headerRow = 1;
			for (int row = headerRow + 1; row <= expdata.getMaximumRow(); row++) {
				String geneId;
				if (organism != null && organism.length() > 0)
					geneId = organism + ":" + expdata.getUnicodeStringCellData(colGeneId, row);
				else
					geneId = expdata.getUnicodeStringCellData(colGeneId, row);
				Double cSig = (Double) expdata.getCellData(colDataSignal, row, null);
				String signalQuality = null;
				if (colDataSignalQuality != null) {
					signalQuality = expdata.getUnicodeStringCellData(colDataSignalQuality, row);
				}
				ked.addDatapoint(geneId, null, null, null, null, cSig, Double.NaN, Double.NaN, Double.NaN, signalQuality);
			}
		}
		status.setCurrentStatusText2("");
	}
	
	public static String[] getValidInputFileExtension() {
		return new String[] { "xlsx", "xls", "txt", "csv", "bin", "dat" };
	}
	
	public static boolean isValidInputFileExtension(String end) {
		for (String ext : getValidInputFileExtension())
			if (ext.equalsIgnoreCase(end))
				return true;
		return false;
	}
	
}

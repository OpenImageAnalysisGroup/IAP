/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.io.File;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.apps.rasterizer.SVGConverterException;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.session.Session;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NeedsSwingThread;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * The print algorithm.
 * 
 * @author Christian Klukas
 */
public class PDFAlgorithm
		extends AbstractAlgorithm
		implements NeedsSwingThread,
		BackgroundTaskStatusProvider {
	
	private int currentProgressStatusValue = -1;
	private String currentStatus1;
	private String currentStatus2;
	private boolean pleaseStop = false;
	private boolean userBreak = false;
	private Boolean addBorder = false;
	private static String filename;
	
	/**
	 * Empty contructor.
	 */
	public PDFAlgorithm() {
		// Do nothing than calling inherit contructor.
		super();
	}
	
	public PDFAlgorithm(Boolean addBorder) {
		this();
		this.addBorder = addBorder;
	}
	
	/**
	 * Returns the display name (in menu area) for this plugin.
	 * <p>
	 * If graffiti sometimes supports mutiple languages, this method have to be refactored.
	 * </p>
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	@Override
	public String getName() {
		return "Create PDF image";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	/**
	 * Unused for this plugin.
	 * 
	 * @throws PreconditionException
	 * @see org.graffiti.plugin.algorithm.Algorithm#check()
	 */
	@Override
	public void check() throws PreconditionException {
		PngJpegAlgorithm.checkZoom();
	}
	
	/**
	 * This method is called by the plugin environment to start the action
	 * this plugin is for.
	 * <p>
	 * This method starts the printprocess
	 * </p>
	 * <p>
	 * This method needs the activeSession set by the {@link #setActiveSession(Session) setActiveSession(Session)} Method. Make shure, that
	 * <code>setActiveSession(Session)</code> is called <strong>BEFORE</strong><code>execute()</code> method!!!
	 * </p>
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 * @see #setActiveSession(Session)
	 */
	@Override
	public void execute() {
		filename = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getGraph().getName();
		if (filename != null)
			filename = PngJpegAlgorithm.replaceExtension(filename, "pdf");
		final SVGAlgorithm svgAlgo = new SVGAlgorithm(addBorder);
		MainFrame.showMessage("Step 1/2: creating SVG image, which will be converted to PDF...", MessageType.INFO, 10000);
		String fileName = FileHelper.getFileName("pdf", "Image File", filename);
		if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
			MainFrame.showMessage("Cancel", MessageType.INFO, 1000);
			return;
		}
		PDFAlgorithm.filename = fileName;
		final String newFileName = StringManipulationTools.stringReplace(fileName, ".pdf", ".svg");
		
		MainFrame.showMessage("", MessageType.INFO, 10000);
		BackgroundTaskHelper bth =
				new BackgroundTaskHelper(new Runnable() {
					@Override
					public void run() {
						createSVGandPDF(svgAlgo, newFileName);
					}
				},
						this, "Create PDF...", "Create PDF Task", false, false
				);
		bth.startWork(this);
	}
	
	/**
	 * @param svgAlgo
	 * @param fileName
	 */
	private void createSVGandPDF(SVGAlgorithm svgAlgo, String fileName) {
		Graph g = MainFrame.getInstance().getSessionManager().getActiveSession().getGraph();
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR && (g.getNumberOfNodes() > 50)) {
			currentProgressStatusValue = 100;
			// currentStatus1="Information: This command may need a very";
			// currentStatus2="long time to complete. Continue execution?";
			// pleaseStop = false;
			// userBreak = true;
			// while (userBreak) {
			// try {
			// Thread.sleep(100);
			// } catch(InterruptedException ie) {
			// ErrorMsg.addErrorMessage(ie);
			// userBreak = false;
			// }
			// }
			
			if (pleaseStop) {
				currentStatus1 = "SVG and PDF not created.";
				currentStatus2 = "Processing stopped.";
				currentProgressStatusValue = 100;
				return;
			}
		}
		
		currentProgressStatusValue = -1;
		currentStatus1 = "Step 1/2: Create SVG file...";
		currentStatus2 = "Please wait. The app will be unresponsive now.";
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// empty
		}
		svgAlgo.attach(g, selection);
		svgAlgo.execute(fileName);
		
		if (pleaseStop) {
			currentStatus1 = "Temporary SVG file created. PDF not is created.";
			currentStatus2 = "Processing stopped.";
			currentProgressStatusValue = 100;
			return;
		}
		
		currentStatus1 = "Step 2/2: SVG file created (" + (new File(fileName).length() / 1024) + " KB). Convert to PDF...";
		currentStatus2 = "This task may take a few moments...";
		String pdfName = fileName.substring(0, fileName.length() - 4) + ".pdf";
		try {
			myMain(fileName);
			currentStatus1 = "Step 2: PDF created...";
			currentProgressStatusValue = 100;
			if (fileName != null) {
				File f = new File(fileName);
				String svgRem = "The temporary SVG file " + fileName + "<br>could <i>not</i> be removed from the system.";
				if (f != null) {
					if (f.delete())
						svgRem = "Temporary SVG file has been deleted.";
				}
				boolean succes = new File(pdfName).exists();
				if (succes) {
					currentStatus1 = "<html>PDF (" + (new File(pdfName).length() / 1024) + " KB) saved as <i>" + pdfName + "</i>";
					currentStatus2 = svgRem;
				} else {
					currentStatus1 = "<html><b>Error:</b> the PDF Image <i>" + pdfName + "</i> could not be created.";
					currentStatus2 = svgRem;
				}
			}
			currentProgressStatusValue = 100;
		} catch (Exception e) {
			currentProgressStatusValue = 100;
			currentStatus1 = "<html><b>Error:</b> the PDF Image <i>" + pdfName + "</i> could not be created.";
			currentStatus2 = e.getMessage();
		}
		/*
		 * //System.out.println(org.apache.batik.apps.rasterizer.Main.USAGE);
		 * org.apache.batik.apps.rasterizer.Main.main(new String[] {
		 * fileName,
		 * "-scriptSecurityOff",
		 * "-m", "application/pdf"
		 * });
		 */
		
	}
	
	private void myMain(String fileName) {
		SVGConverter c = new SVGConverter();
		try {
			c.setSources(new String[] { fileName });
			String tgtFile = StringManipulationTools.stringReplace(fileName, ".svg", ".pdf");
			c.setDst(new File(tgtFile));
			c.setDestinationType(DestinationType.PDF);
			c.execute();
		} catch (SVGConverterException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		return currentProgressStatusValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	@Override
	public String getCurrentStatusMessage1() {
		return currentStatus1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	@Override
	public String getCurrentStatusMessage2() {
		return currentStatus2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#pleaseStop()
	 */
	@Override
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return getCurrentStatusValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	@Override
	public boolean pluginWaitsForUser() {
		return userBreak;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	@Override
	public void pleaseContinueRun() {
		userBreak = false;
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		currentProgressStatusValue = value;
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
}

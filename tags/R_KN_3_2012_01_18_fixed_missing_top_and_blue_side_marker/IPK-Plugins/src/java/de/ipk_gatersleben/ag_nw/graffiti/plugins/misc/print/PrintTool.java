/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $ID:$
 * Created on 10.07.2003
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.print;

import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Collection;

import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.view.View;

public class PrintTool {
	
	/**
	 * Prints a <code>View</code>.
	 * <p>
	 * If the <code>View</code> supports printing its <code>print()</code> method will be invoked otherwise a {@link DefaultPrintable DefaultPrintable} instance
	 * will be wrapped around.
	 * </p>
	 * 
	 * @param view
	 *           - the view to print
	 */
	public static void print(View view) {
		if (view == null)
			return;
		
		// check whether the view supports printing by itself or not
		Printable printObject =
							(view instanceof Printable)
												? (Printable) view
												: null;
		
		if (view == null) {
			MainFrame.showMessageDialog("Can not print this kind of view.", "Error");
			return;
		}
		
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setJobName(ReleaseInfo.getRunningReleaseStatus().toString() + " print " + view.getGraph().getName(true));
		printJob.setPrintable(printObject);
		if (printJob.printDialog())
			try {
				printJob.print();
			} catch (PrinterException e) {
				MainFrame.showMessageDialog(e.getMessage(), "Error");
			}
	}
	
	public static void print(Collection<View> views) {
		if (views == null || views.size() <= 0)
			return;
		
		PrinterJob printJob = PrinterJob.getPrinterJob();
		if (printJob.printDialog()) {
			try {
				for (View view : views) {
					printJob.setJobName(ReleaseInfo.getRunningReleaseStatus().toString() + " print " + view.getGraph().getName(true));
					Printable printObject =
										(view instanceof Printable)
															? (Printable) view
															: null;
					if (printObject == null)
						continue;
					printJob.setPrintable(printObject);
					printJob.print();
				}
			} catch (PrinterException e) {
				MainFrame.showMessageDialog(e.getMessage(), "Error");
			}
		}
	}
}

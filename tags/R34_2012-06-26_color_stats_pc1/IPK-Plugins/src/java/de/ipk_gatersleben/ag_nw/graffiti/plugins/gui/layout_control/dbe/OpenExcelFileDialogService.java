/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.util.Collection;

import org.HelperClass;
import org.OpenFileDialogService;

/**
 * @author Christian Klukas
 *         (c) 2004, 2009 IPK-Gatersleben
 */
public class OpenExcelFileDialogService implements HelperClass {
	
	public static final String EXCEL_OR_BINARY_DESCRIPTION = "Dataset (.xlsx|.xls|.csv|.bin|.dat|.txt|.list)";
	public static final String[] EXCEL_OR_BINARY_EXTENSION = new String[] { ".XLSX", ".XLS", ".CSV", ".BIN", ".DAT", ".TXT", ".LIST" };
	private static final String ANNOTATION_DESCRIPTION = "Annotation (.xlsx|.xls|.csv|.txt|.list|.dat)";
	public static final String SPREADSHEET_DESCRIPTION = "Spreadsheet Files (.xlsx|.xls|.csv|.txt|.list|.dat)";
	public static final String[] EXCELFILE_EXTENSIONS = new String[] { ".XLSX", ".XLS", ".CSV", ".TXT", ".LIST", ".DAT" };
	
	public static File getExcelFile() {
		return OpenFileDialogService.getFile(EXCELFILE_EXTENSIONS, SPREADSHEET_DESCRIPTION);
	}
	
	public static Collection<File> getExcelFiles() {
		return OpenFileDialogService.getFiles(EXCELFILE_EXTENSIONS, SPREADSHEET_DESCRIPTION);
	}
	
	public static Collection<File> getExcelOrAnnotationFiles() {
		return OpenFileDialogService.getFiles(EXCELFILE_EXTENSIONS, ANNOTATION_DESCRIPTION);
	}
	
	public static Collection<File> getAffyOrAgilAnnotationFiles() {
		return OpenFileDialogService.getFiles(new String[] {
							".ANNOT.CSV", ".XLSX", ".XLS", ".CSV", ".TXT" },
							"Annotation (.annot.csv|xlsx|xls|csv|txt)");
	}
	
	public static File getExcelOrBinaryFile() {
		return OpenFileDialogService.getFile(EXCEL_OR_BINARY_EXTENSION, EXCEL_OR_BINARY_DESCRIPTION);
	}
	
	public static Collection<File> getExcelOrBinaryFiles() {
		return OpenFileDialogService.getFiles(EXCEL_OR_BINARY_EXTENSION, EXCEL_OR_BINARY_DESCRIPTION);
	}
	
}

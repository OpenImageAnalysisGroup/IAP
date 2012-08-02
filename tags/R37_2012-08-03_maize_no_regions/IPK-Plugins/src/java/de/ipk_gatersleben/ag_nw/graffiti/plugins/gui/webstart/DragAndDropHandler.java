package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import java.io.File;
import java.util.List;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;

/**
 * Use {@link DataDragAndDropHandler} or {@link ExperimentDataDragAndDropHandler} in case
 * this handler processes input files for the creation of {@link Substance} datasets.
 * If the handler fully processes the input files, implement this interface.
 */
public interface DragAndDropHandler {
	
	public boolean process(List<File> files);
	
	/**
	 * @param f
	 *           Input file to be analyzed for compatibility.
	 * @return True, if this handler might handle the input file. E.g. based on
	 *         the file extension.
	 */
	public boolean canProcess(File f);
	
	/**
	 * @return True, if the handler should be called before other handlers
	 *         are executed, which return False.
	 */
	public boolean hasPriority();
}

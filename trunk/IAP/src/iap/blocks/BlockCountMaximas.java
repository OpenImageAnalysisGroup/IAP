/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Entzian
 */
public class BlockCountMaximas extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		// ImageOperation res = new ImageOperation(getInput().getMasks().getVis()).findMax(options.getDoubleSetting(Setting.FIND_MAXIMUM_TOLERANCE),
		// options.getIntSetting(Setting.FIND_MAXIMUM_TYP));
		
		ImageOperation res = new ImageOperation(input().masks().vis()).findMax(
				getInt("FIND_MAXIMUM_TOLERANCE", 50),
				MaximumFinder.COUNT);
		ResultsTable numericResult = res.getResultsTable();
		
		getProperties().setNumericProperty(0, PropertyNames.RESULT_MAXIMUM_SEARCH_COUNT, numericResult.getValue("Count", numericResult.getCounter() - 1));
		
		// switch (options.getIntSetting(Setting.FIND_MAXIMUM_TYP)) {
		// case MaximumFinder.COUNT:
		// getProperties().setNumericProperty(0, PropertyNames.RESULT_MAXIMUM_SEARCH_COUNT, numericResult.getValue("Count", numericResult.getCounter() - 1));
		// break;
		//
		// case MaximumFinder.LIST:
		//
		// // ... Funktion für Blaupunkte
		// //
		// // dann Zeug in die Properties speichern
		//
		// for (int i = 1; i < numericResult.getCounter(); i = i + 2) {
		// getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), numericResult.getValue("X", numericResult.getCounter() - i));
		// getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), numericResult.getValue("Y", numericResult.getCounter() - (i + 1)));
		// }
		//
		// default:
		// break;
		// }
		//
		// System.out.println("getCounter: " + numericResult.getCounter());
		// System.out.println("Toleranz: " + options.getDoubleSetting(Setting.FIND_MAXIMUM_TOLERANCE));
		// System.out.println("Typ: " + options.getDoubleSetting(Setting.FIND_MAXIMUM_TYP));
		//
		// // double nMaxima = numericResult.getValue("Count", numericResult.getCounter() - 1);
		
		return new ImageOperation(res.getImage()).getImage();
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}

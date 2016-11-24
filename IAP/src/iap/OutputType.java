package iap;

import java.util.HashMap;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;

/**
 * @author klukas
 */
public class OutputType {
	
	private boolean nullOutput = false;
	private boolean stdOutput;
	private boolean fileOutput;
	private String fileName;
	
	public OutputType(String definition) {
		if (definition == null || definition.equalsIgnoreCase("NULL"))
			nullOutput = true;
		else
			if (definition.equals("."))
				stdOutput = true;
			else {
				fileOutput = true;
				fileName = definition;
			}
	}
	
	public void process(HashMap<String, BlockResultSet> blockResults) {
		if (stdOutput) {
			for (String key : blockResults.keySet()) {
				System.out.println("#" + key);
				blockResults.get(key).printAnalysisResults();
			}
		}
	}
	
}

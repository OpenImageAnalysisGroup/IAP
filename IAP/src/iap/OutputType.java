package iap;

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
	
}

package org.graffiti.event;

import org.graffiti.attributes.Attribute;

public enum PathComparisonResult {
	SECOND_MORE_GENERAL, FIRST_MORE_GENERAL, EQUAL_PATH, PATH_COMPLETELY_DIFFERENT;
	
	private String commonPath;
	
	public static PathComparisonResult compare(
						String previousPath,
						String currentPath) {
		
		if (previousPath == null || currentPath == null)
			return PATH_COMPLETELY_DIFFERENT;
		
		String[] folders1 = previousPath.split("\\" + Attribute.SEPARATOR);
		String[] folders2 = currentPath.split("\\" + Attribute.SEPARATOR);
		
		if (folders1.length == 0 || folders2.length == 0)
			return PATH_COMPLETELY_DIFFERENT;
		
		if (!folders1[0].equals(folders2[0]))
			return PATH_COMPLETELY_DIFFERENT;
		
		if (previousPath.equals(currentPath))
			return EQUAL_PATH;
		
		// now either second more general or first !
		PathComparisonResult result;
		if (folders1.length < folders2.length)
			result = FIRST_MORE_GENERAL;
		else
			result = SECOND_MORE_GENERAL;
		
		StringBuilder commonPath = new StringBuilder("");
		for (int i = 0; i < Math.min(folders1.length, folders2.length); i++) {
			if (folders1[i].equals(folders2[i])) {
				if (commonPath.length() > 0)
					commonPath.append(Attribute.SEPARATOR);
				commonPath.append(folders1[i]);
			} else {
				result.setCommonPath(commonPath.toString());
				return result;
			}
		}
		result.setCommonPath(commonPath.toString());
		return result;
	}
	
	public void setCommonPath(String commonPath) {
		this.commonPath = commonPath;
	}
	
	public String getCommonPath() {
		return commonPath;
	}
}
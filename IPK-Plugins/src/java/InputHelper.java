import org.HelperClass;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public class InputHelper implements HelperClass {
	public static Object[] getInput(String description, String title,
						Object... parameters) {
		return MyInputHelper.getInput(description, title, parameters);
	}
	
	public static void inspect(Object o) {
		System.out.println("null? " + (o != null));
	}
}
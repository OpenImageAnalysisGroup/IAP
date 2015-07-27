package debug;

import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public class IAPwithAddonForDebugging {
	
	public static void main(String[] args) {
		IAPmain.main(args, new String[] { "IAPexampleAddOn.xml" });
	}
	
}

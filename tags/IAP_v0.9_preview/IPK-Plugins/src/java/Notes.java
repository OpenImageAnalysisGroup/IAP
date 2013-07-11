import java.io.IOException;
import java.util.Collections;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class Notes {
	public static void writeCompoundList(String fileName) {
		TextFile tf = new TextFile();
		for (CompoundEntry ce : CompoundService.getCompoundEntries()) {
			String l = ce.getID() + ";" + ce.getNames().iterator().next() + ";" + ce.getFormula();
			tf.add(l);
		}
		Collections.sort(tf);
		try {
			tf.write(fileName);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
}

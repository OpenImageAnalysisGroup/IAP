package iap;

import java.awt.Color;
import java.io.File;

import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Christian Klukas
 */

public class Overlay {
	public static void main(String[] args) {
		try {
			String url = args[0];
			AbstractResourceIOHandler ha = new FileSystemHandler();
			Image image = new Image(ha.getInputStream(FileSystemHandler.getURL(new File(url))));
			
			int x = 0, y = 0, w = image.getWidth(), h = image.getHeight();
			double alpha = 0.5;
			int color = new Color(50, 50, 50).getRGB();
			int sz = 128 - 5;
			
			Color lr = new Color(255, 127, 127);
			int lri = lr.getRGB();
			
			image = image.io().canvas().fillRect(x, y, w, h, color, alpha).getImage().io().addBorder(120, 0, 0, lri).getImage();
			image = image.io().drawLine(0, 0, sz, sz, lr, 5).drawLine(sz, 0, 0, sz, lr, 5).addBorder(5, 0, 0, lri)
					.getImage();
			
			image.saveToFile(args[1]);
			
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

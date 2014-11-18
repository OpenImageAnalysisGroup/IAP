package iap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.FTPhandler;
import org.graffiti.plugin.io.resources.HTTPhandler;
import org.graffiti.plugin.io.resources.IOurl;

/**
 * @author Christian Klukas
 */
public class Download {
	public static void main(String[] args) {
		try {
			String url = args[0];
			AbstractResourceIOHandler h;
			if (url.startsWith("http"))
				h = new HTTPhandler();
			else
				if (url.startsWith("ftp"))
					h = new FTPhandler();
				else {
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Currently, only http or ftp downloads are supported by this function!");
					h = null;
				}
			BufferedInputStream in = new BufferedInputStream(h.getInputStream(new IOurl(url)));
			FileOutputStream fos = new FileOutputStream(args[1]);
			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[1024];
			int x = 0;
			while ((x = in.read(data, 0, 1024)) >= 0) {
				bout.write(data, 0, x);
			}
			bout.close();
			in.close();
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Help: call as following: java -jar iap.jar http://server/url target.file.name");
			e.printStackTrace();
			System.exit(1);
		}
	}
}

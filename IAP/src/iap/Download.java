package iap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URL;

public class Download {
	public static void main(String[] args) {
		try {
			BufferedInputStream in = new BufferedInputStream(new URL(args[0]).openStream());
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

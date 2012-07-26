package iap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URL;

public class download {
	public static int main(String[] args) {
		try {
			BufferedInputStream in = new BufferedInputStream(new URL(args[0]).openStream());
			FileOutputStream fos = new FileOutputStream(args[1]);
			BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
			byte data[] = new byte[1024];
			while (in.read(data, 0, 1024) >= 0) {
				bout.write(data);
			}
			bout.close();
			in.close();
			return 0;
		} catch (Exception e) {
			System.out.println("Help: call as follows java -jar iap.jar http://server/url target.file.name");
			e.printStackTrace();
			return 1;
		}
	}
}

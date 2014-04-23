import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TextReplace {
	public static void main(String[] args) {
		try {
			ArrayList<String> replaceA = new ArrayList<String>();
			ArrayList<String> replaceB = new ArrayList<String>();
			BufferedReader in2 = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
			String s2;
			while ((s2 = in2.readLine()) != null) {
				if (s2.indexOf("/") > 0) {
					String a = s2.substring(0, s2.indexOf("/"));
					String b = s2.substring(s2.indexOf("/") + 1);
					replaceA.add(a);
					replaceB.add(b);
				}
			}
			in2.close();
			
			StringBuffer sb = new StringBuffer();
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			String s;
			while ((s = in.readLine()) != null) {
				for (int i = 0; i < replaceA.size(); i++) {
					s = s.trim();
					if (s.endsWith("-" + replaceA.get(i)) || s.endsWith("+" + replaceA.get(i))) {
						s = s.replace(replaceA.get(i), replaceB.get(i));
						break;
					}
				}
				sb.append(s);
				sb.append("\n");
			}
			in.close();
			System.out.println(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
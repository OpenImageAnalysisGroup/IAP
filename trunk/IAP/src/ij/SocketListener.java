package ij;
import ij.*;
import ij.io.*;
import java.io.*;
import java.net.*;

/** Runs commands sent to the port returned by ImageJ.getPort(). 
<pre>
  Commands:
    open path (opens a file)
    macro path  (runs a macro file)
    run command-name  (runs an ImageJ menu command)
    eval macro  (evaluates a macro)
    user.dir path  (sets the current directory)
</pre>
*/
public class SocketListener implements Runnable {

	public SocketListener() {
		Thread thread = new Thread(this, "SocketListener");
		thread.start(); 
	}

	public void run() {
		ServerSocket serverSocket = null;
		BufferedReader is;
		Socket clientSocket;
		try {
			serverSocket = new ServerSocket(ImageJ.getPort());
			if (IJ.debugMode) IJ.log("SocketListener: new ServerSocket("+ImageJ.getPort()+")");
			while (true) {
				clientSocket = serverSocket.accept();
				InetAddress address = clientSocket.getInetAddress();
				boolean isLocal = address!=null && address.isLoopbackAddress();
				if (IJ.debugMode)  IJ.log("SocketListener: client="+address+"  "+ isLocal);
				try {
					is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String cmd = is.readLine();
					if (IJ. debugMode) IJ.log("SocketListener: command=\""+ cmd+"\"");
					if (cmd.startsWith("open "))
						(new Opener()).openAndAddToRecent(cmd.substring(5));
					else if (isLocal && cmd.startsWith("macro ")) {
						String name = cmd.substring(6);
						String name2 = name;
						String arg = null;
						if (name2.endsWith(")")) {
							int index = name2.lastIndexOf("(");
							if (index>0) {
								name = name2.substring(0, index);
								arg = name2.substring(index+1, name2.length()-1);
							}
						}
						IJ.runMacroFile(name, arg);
					} else if (isLocal && cmd.startsWith("run "))
						IJ.run(cmd.substring(4));
					else if (isLocal && cmd.startsWith("eval ")) {
						String rtn = IJ.runMacro(cmd.substring(5));
						if (rtn!=null)
							System.out.print(rtn);
					} else if (cmd.startsWith("user.dir "))
						OpenDialog.setDefaultDirectory(cmd.substring(9));
				} catch (Throwable e) {}
				clientSocket.close();
			}
 		} catch (IOException e) {}
	}

}

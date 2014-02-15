package de.ipk.ag_ba.server.task_management;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_nw.graffiti.services.network.BroadCastService;

/**
 * @author klukas
 */
public class FileMonitor {
	
	private final Path path;
	private final File f;
	
	public FileMonitor(String fileName) {
		this.f = new File(fileName);
		if (!f.exists() || !f.canRead())
			throw new UnsupportedOperationException("Error: Can't access file '" + fileName + "'!");
		this.path = Paths.get(f.getParent());
	}
	
	public void startMonitoringAndSendMessageIfReceived(int udpPortStart, int udpPortEnd, String id, int contentLineIndex) throws IOException,
			InterruptedException {
		System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">INFO: Start file / directory monitoring! // "
				+ SystemAnalysis.getCurrentTime());
		WatchService watchService = FileSystems.getDefault().newWatchService();
		path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
		
		BroadCastService bcs = new BroadCastService(udpPortStart, udpPortEnd, 1000);
		System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">INFO: MESSAGES WILL BE SEND ON PORTS " + udpPortStart + "-" + udpPortEnd);
		long lastModify = 0;
		for (;;) {
			WatchKey key = watchService.take();
			
			for (WatchEvent<?> event : key.pollEvents()) {
				boolean success = false;
				do {
					long lm = f.lastModified();
					if (lm > lastModify) {
						lastModify = lm;
					} else
						continue;
					System.out.print(SystemAnalysis.getCurrentTimeInclSec() + ">");
					WatchEvent.Kind<?> kind = event.kind();
					System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Modified: " + event.context() + ", event type: " + kind.name() + " // "
							+ SystemAnalysis.getCurrentTime());
					TextFile tf = new TextFile(f);
					if (tf.size() > contentLineIndex) {
						int availLines = Integer.parseInt(StringManipulationTools.getNumbersFromString((tf.get(0))));
						int lastContentLine = availLines + 2;
						String msg = id + ":" + tf.get(contentLineIndex);
						for (int lineIdx = contentLineIndex + 1; lineIdx < lastContentLine && lineIdx < tf.size() && msg.length() < 500; lineIdx++) {
							msg += "|" + tf.get(lineIdx);
						}
						System.out.print(SystemAnalysis.getCurrentTimeInclSec() + ">INFO: Sent message: " + msg + "...");
						for (int i = 0; i < 5; i++) {
							bcs.sendBroadcast(msg.getBytes(StandardCharsets.UTF_8));
							Thread.sleep(5);
						}
						System.out.println("OK!");
						success = true;
					} else {
						System.out.println("NOT OK! (MONITORED FILE CONTAINS TOO FEW LINES)");
						Thread.sleep(10);
						success = false;
					}
				} while (!success);
			}
			boolean valid = key.reset();
			// If the key is invalid, just exit.
			if (!valid) {
				break;
			}
			
		}
	}
}

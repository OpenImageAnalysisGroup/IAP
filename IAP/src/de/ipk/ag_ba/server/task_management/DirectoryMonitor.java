package de.ipk.ag_ba.server.task_management;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class DirectoryMonitor {
	
	private final String inputFileDir;
	
	public DirectoryMonitor(String inputFileDir) throws IOException {
		this.inputFileDir = inputFileDir;
	}
	
	public String getNextAppearingFile(String inputFileDir) throws InterruptedException, IOException {
		WatchService watchService;
		Path path;
		watchService = FileSystems.getDefault().newWatchService();
		path = Paths.get(inputFileDir);
		path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
		for (;;) {
			WatchKey key = watchService.poll(500, TimeUnit.MILLISECONDS);
			if (key == null)
				continue;
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();
				System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Detected new file: " + event.context() + ", event type: " + kind.name() + " // "
						+ SystemAnalysis.getCurrentTime());
				watchService.close();
				return "" + event.context();
			}
			boolean valid = key.reset();
			// If the key is invalid, just exit.
			if (!valid) {
				break;
			}
		}
		watchService.close();
		return null;
	}
}

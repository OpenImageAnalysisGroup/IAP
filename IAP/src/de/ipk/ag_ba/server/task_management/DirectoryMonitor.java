package de.ipk.ag_ba.server.task_management;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class DirectoryMonitor {
	private final WatchService watchService;
	private final Path path;
	
	public DirectoryMonitor(String inputFileDir) throws IOException {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.path = Paths.get(inputFileDir);
		path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
	}
	
	public String getNextAppearingFile(String inputFileDir) throws InterruptedException {
		for (;;) {
			WatchKey key = watchService.take();
			
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();
				System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Detected new file: " + event.context() + ", event type: " + kind.name() + " // "
						+ SystemAnalysis.getCurrentTime());
				return "" + event.context();
			}
			boolean valid = key.reset();
			// If the key is invalid, just exit.
			if (!valid) {
				break;
			}
		}
		return null;
	}
}

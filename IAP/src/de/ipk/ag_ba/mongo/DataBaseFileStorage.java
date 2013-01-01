package de.ipk.ag_ba.mongo;

import java.util.ArrayList;
import java.util.List;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;

public class DataBaseFileStorage {
	
	private final MongoDB mongoDB;
	
	public DataBaseFileStorage(MongoDB mongoDB) {
		this.mongoDB = mongoDB;
	}
	
	public void move(final VirtualFileSystemVFS2 vfs, final int gb, final BackgroundTaskStatusProviderSupportingExternalCall status,
			final StringBuilder messages) throws Exception {
		mongoDB.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				ThreadSafeOptions free = new ThreadSafeOptions();
				BackgroundTaskStatusProviderSupportingExternalCall delStatus = new BackgroundTaskConsoleLogger("", "", false);
				long n = 0;
				long start = System.currentTimeMillis();
				for (String mgfs : MongoGridFS.getFileCollectionsInclPreview()) {
					final GridFS gridfs = new GridFS(db, mgfs);
					List<GridFSDBFile> files = gridfs.find(new BasicDBObject());
					int nFiles = files.size();
					for (GridFSDBFile f : files) {
						String md5 = f.getFilename();
						try {
							long saved = vfs.saveStream(mgfs + "/" + md5, f.getInputStream());
							if (saved != f.getLength()) {
								messages.append("Could not move file " + md5 + " from "
										+ gridfs + " to " + vfs.getTargetName() + ", storage size differs from input size: "
										+ saved + " saved, " + f.getLength() + " input size.");
								MongoDB.saveSystemMessage("Could not move file " + md5
										+ " from " + gridfs + " to " + vfs.getTargetName()
										+ ", storage size differs from input size: "
										+ saved + " saved, " + f.getLength() + " input size.");
								
							} else {
								ArrayList<GridFSDBFile> toBeRemoved = new ArrayList<GridFSDBFile>();
								toBeRemoved.add(f);
								mongoDB.removeFilesFromGridFS(delStatus, mgfs, gridfs, toBeRemoved, free, db);
								n++;
								status.setCurrentStatusText1("Moving data from " + mgfs + " (" + n + ")");
								status.setCurrentStatusText2(free.getLong() / 1024 / 1024 / 1024 + " GB moved ("
										+ SystemAnalysis.getDataTransferSpeedString(free.getLong(), start, System.currentTimeMillis()) + ")");
							}
							if (gb > 0 && free.getLong() / 1024 / 1024 / 1024 >= gb)
								break;
							if (gb <= 0)
								status.setCurrentStatusValueFine(n * 100d / nFiles);
							else
								status.setCurrentStatusValueFine(free.getLong() * 100d / 1024d / 1024d / 1024d / gb);
						} catch (Exception e) {
							messages.append("Could not move file " + md5 + " from " + gridfs + " to " + vfs.getTargetName());
							MongoDB.saveSystemErrorMessage("Could not move file " + md5 + " from " + gridfs + " to " + vfs.getTargetName(), e);
						}
					}
				}
				messages.append("Moved " + n + " files: " + free.getLong() / 1024 / 1024 / 1024 + " GB");
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
}

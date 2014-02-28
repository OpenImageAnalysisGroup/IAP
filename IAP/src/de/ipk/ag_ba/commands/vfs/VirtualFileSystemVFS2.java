package de.ipk.ag_ba.commands.vfs;

import info.StopWatch;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk.vanted.util.VfsFileObjectUtil;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.PriorityLock;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.PriorityLock.Priority;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.MyImageIOhelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;

public class VirtualFileSystemVFS2 extends VirtualFileSystem implements DatabaseTarget {
	public static final String DIRECTORY_FOLDER_NAME = "index";
	public static final String DATA_FOLDER_NAME = "data";
	public static final String CONDITION_FOLDER_NAME = "conditions";
	public static final String ICON_FOLDER_NAME = "icons";
	
	private final VfsFileProtocol vfs_type;
	private String description;
	private final String protocoll;
	private String host;
	private String user;
	private String pass;
	private boolean askForPassword = false;
	private String folder;
	private final String prefix;
	private boolean useForMongoFileStorage;
	private boolean useOnlyForMongoFileStorage;
	private String useForMongoFileStorageCloudName;
	
	public VirtualFileSystemVFS2(
			String prefix,
			VfsFileProtocol vfs_type,
			String description,
			String protocoll,
			String host,
			String user,
			String pass,
			String folder,
			boolean useForMongoFileStorage,
			boolean useOnlyForMongoFileStorage,
			String useForMongoFileStorageCloudName) {
		this.prefix = prefix;
		this.vfs_type = vfs_type;
		this.description = description;
		this.protocoll = protocoll;
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.folder = folder;
		
		this.useForMongoFileStorage = useForMongoFileStorage;
		this.useOnlyForMongoFileStorage = useOnlyForMongoFileStorage;
		this.useForMongoFileStorageCloudName = useForMongoFileStorageCloudName;
		// if (this.vfs_type == VfsFileProtocol.LOCAL) {
		// ResourceIOManager.registerIOHandler(new FileSystemHandler(this.prefix, this.folder));
		// } else
		ResourceIOManager.registerIOHandler(new VirtualFileSystemHandler(this));
	}
	
	@Override
	public String getTargetName() {
		return description;
	}
	
	@Override
	public String getTransferProtocolName() {
		return protocoll;
	}
	
	@Override
	public String getTargetPathName() {
		return folder;
	}
	
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public ArrayList<String> listFiles(String optSubDirectory) throws Exception {
		String path = folder;
		if (optSubDirectory != null)
			path = path + "/" + optSubDirectory;
		
		try {
			VfsFileObject file = VfsFileObjectUtil.createVfsFileObject(vfs_type,
					host, path, user, getPass());
			if (!file.exists()) {
				if (doPrintStatus())
					System.out.println(">>>>>> create directory " + path);
				file.mkdir();
			}
			ArrayList<String> res = new ArrayList<String>();
			for (String s : file.list()) {
				res.add(s);
			}
			return res;
		} catch (Exception e) {
			if (askForPassword)
				pass = "?";
			throw e;
		}
	}
	
	@Override
	public ArrayList<String> listFolders(String optSubDirectory) throws Exception {
		String path = folder;
		if (optSubDirectory != null)
			path = path + "/" + optSubDirectory;
		
		try {
			VfsFileObject file = VfsFileObjectUtil.createVfsFileObject(vfs_type,
					host, path, user, getPass());
			if (!file.exists()) {
				if (doPrintStatus())
					System.out.println(">>>>>> create directory " + path);
				file.mkdir();
			}
			ArrayList<String> res = new ArrayList<String>();
			for (String s : file.listFolders()) {
				res.add(s);
			}
			return res;
		} catch (Exception e) {
			if (askForPassword)
				pass = "?";
			throw e;
		}
	}
	
	private boolean doPrintStatus() {
		return SystemOptions.getInstance().getBoolean("VFS", "print activity info to console", false);
	}
	
	@Override
	public IOurl getIOurlFor(String fileNameInclSubFolderPathName) {
		try {
			return new IOurl(prefix, "", fileNameInclSubFolderPathName);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public VfsFileObject newVfsFile(String fileNameInclSubFolderPathName) throws Exception {
		return newVfsFile(fileNameInclSubFolderPathName, false);
	}
	
	public VfsFileObject newVfsFile(String fileNameInclSubFolderPathName, boolean absoluteDirName) throws Exception {
		try {
			if (fileNameInclSubFolderPathName != null && fileNameInclSubFolderPathName.startsWith("/"))
				fileNameInclSubFolderPathName = fileNameInclSubFolderPathName.substring("/".length());
			if (fileNameInclSubFolderPathName != null && fileNameInclSubFolderPathName.startsWith("\\"))
				fileNameInclSubFolderPathName = fileNameInclSubFolderPathName.substring("\\".length());
			return VfsFileObjectUtil.createVfsFileObject(
					vfs_type, host,
					(absoluteDirName ? "" : folder + "/") +
							fileNameInclSubFolderPathName, user, getPass());
		} catch (Exception e) {
			if (askForPassword)
				pass = "?";
			throw e;
		}
	}
	
	public VfsFileProtocol getProtocolType() {
		return vfs_type;
	}
	
	public boolean isUseForMongoFileStorage() {
		return useForMongoFileStorage;
	}
	
	public boolean isUseOnlyForMongoFileStorage() {
		return useOnlyForMongoFileStorage;
	}
	
	public String getUseForMongoFileStorageCloudName() {
		return useForMongoFileStorageCloudName;
	}
	
	public synchronized long saveStream(String fileNameInclSubFolderPathName, InputStream is, boolean skipKnown, long expectedLengthIfKnown) throws Exception {
		try {
			if (doLocking())
				lock.lock(Priority.LOW);
			if (doPrintStatus())
				System.out.print("[s");
			VfsFileObject file = newVfsFile(fileNameInclSubFolderPathName);
			if (skipKnown && file.exists()) {
				long l = file.length();
				if (l > 0 && l == expectedLengthIfKnown) {
					if (doPrintStatus())
						System.out.println(SystemAnalysis.getCurrentTime() + ">Skipping known file in VFS: " + fileNameInclSubFolderPathName);
					return expectedLengthIfKnown;
				}
			}
			OutputStream os = file.getOutputStream();
			if (os == null)
				System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: Output stream for VFS file could not be created (NULL result)!");
			is = ResourceIOManager.getInputStreamMemoryCached(is);
			long copied = ResourceIOManager.copyContent(is, os);
			writeCounter.addLong(copied);
			if (doPrintStatus())
				System.out.print("]");
			return copied;
		} finally {
			if (doLocking())
				lock.unlock();
		}
	}
	
	private boolean doLocking() {
		return SystemOptions.getInstance().getBoolean("VFS", vfs_type.name() + " - limit concurrent use", true);
	}
	
	Object in = new Object();
	
	public static ThreadSafeOptions readCounter = new ThreadSafeOptions();
	public static ThreadSafeOptions writeCounter = new ThreadSafeOptions();
	
	private static PriorityLock lock = new PriorityLock();
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		try {
			InputStream res = null;
			if (doLocking())
				lock.lock(Priority.HIGH);
			if (doPrintStatus())
				System.out.print("[l");
			String fn = url.getDetail() + "/" + url.getFileName().split("#", 2)[0];
			VfsFileObject file = newVfsFile(fn);
			if (file == null)
				return null;
			if (!file.exists()) {
				if (fn.endsWith(".jpg")) {
					String test_fn = fn.substring(0, fn.length() - ".jpg".length()) + ".png";
					VfsFileObject test_file = newVfsFile(test_fn);
					if (test_file.exists()) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: '" + fn + "' could not be found! Corrected problem by accessing '" + test_fn
								+ "'");
						fn = test_fn;
						file = test_file;
					}
				}
			}
			if (!file.exists()) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Could not find file '" + file.getFile() + "'");
				return null;
			}
			boolean cachedLoader = false;
			if (cachedLoader) {
				MyByteArrayInputStream is = ResourceIOManager.getInputStreamMemoryCached(file.getInputStream());
				readCounter.addLong(is.getCount());
				res = is;
			} else {
				res = file.getInputStream();
				readCounter.addLong(file.length());
			}
			if (doPrintStatus())
				System.out.print("]");
			return res;
		} finally {
			if (doLocking())
				lock.unlock();
		}
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url) throws Exception {
		String detail = url.getDetail();
		if (detail != null && detail.startsWith("/"))
			detail = detail.substring(1);
		if (detail != null && detail.startsWith("\\"))
			detail = detail.substring(1);
		if (detail != null && detail.startsWith("data")) {
			VfsFileObject file = newVfsFile(ICON_FOLDER_NAME + detail.substring("data".length()) + "/" + url.getFileName().split("#", 2)[0]);
			if (file != null && file.exists()) {
				InputStream is = file.getInputStream();
				if (is != null)
					return is;
			}
		}
		VfsFileObject file = newVfsFile(url.getDetail() + "/" + url.getFileName());
		if (file == null)
			return null;
		if (!file.exists())
			return null;
		InputStream is = file.getInputStream();
		return is;
	}
	
	@Override
	public long getFileLength(IOurl url) throws Exception {
		String fn = url.getFileName();
		if (fn != null && fn.contains("#"))
			fn = fn.substring(0, fn.lastIndexOf("#"));
		VfsFileObject file = newVfsFile(url.getDetail() + "/" + fn);
		return file.length();
	}
	
	public int countFiles(String optSubDirectory) throws Exception {
		return listFiles(optSubDirectory).size();
	}
	
	@Override
	public VfsFileObject getFileObjectFor(String fileName) throws Exception {
		VfsFileObject file = newVfsFile(fileName);
		return file;
	}
	
	@Override
	public LoadedImage saveImage(String[] optFileNameMainAndLabelPrefix, LoadedImage limg,
			boolean keepRemoteLabelURLs_safe_space, boolean ignoreLabelURL) throws Exception {
		ExperimentHeaderInterface ehi = limg.getParentSample().getParentCondition().getExperimentHeader();
		long snapshotTime = limg.getParentSample().getSampleFineTimeOrRowId();
		String pre = "";
		String firstPre = "";
		String finalMainName = null;
		{ // save main
			boolean alreadyInSameStorageLocation = false;
			if (limg.getURL() != null && limg.getURL().getPrefix() != null && limg.getURL().getPrefix().equals(getPrefix()))
				alreadyInSameStorageLocation = true;
			if (!alreadyInSameStorageLocation) {
				String desiredFileName = limg.getURL().getFileName();
				if (desiredFileName != null && desiredFileName.contains("#"))
					desiredFileName = desiredFileName.substring(desiredFileName.indexOf("#") + 1);
				String substanceName = limg.getSubstanceName();
				desiredFileName = ActionDataExportToVfs.determineBinaryFileName(snapshotTime, substanceName, limg, limg);// + "#" + desiredFileName;
				desiredFileName = desiredFileName.substring(0, desiredFileName.length() - limg.getURL().getFileNameExtension().length()) + "."
						+ IAPservice.getTargetFileExtension(false, limg.getURL().getFileNameExtension());
				finalMainName = desiredFileName;
				if (optFileNameMainAndLabelPrefix != null && optFileNameMainAndLabelPrefix.length > 0) {
					pre = optFileNameMainAndLabelPrefix[0];
					firstPre = pre;
				}
				String targetFileNameFullRes = prepareAndGetDataFileNameAndPath(ehi, snapshotTime, pre + desiredFileName.split("#")[0]);
				MyByteArrayInputStream mainStream = ResourceIOManager.getInputStreamMemoryCached(limg.getInputStream());
				if (mainStream != null && mainStream.getCount() > 0)
					saveStream(targetFileNameFullRes, mainStream, false, mainStream.getCount());
				
				IOurl url = limg.getURL();
				
				String fullPath = new File(targetFileNameFullRes).getParent();
				String subPath = fullPath.startsWith(getTargetPathName()) ? fullPath.substring(getTargetPathName().length()) : fullPath;
				if (url != null) {
					url.setPrefix(getPrefix());
					url.setDetail(subPath);
					if (!pre.isEmpty())
						url.setFileName(pre + desiredFileName);
					else
						url.setFileName(desiredFileName);
				}
			}
		}
		if (limg.getLabelURL() != null && !ignoreLabelURL) { // save label
			boolean alreadyInSameStorageLocation = false;
			if (limg.getLabelURL() != null && limg.getLabelURL().getPrefix() != null && limg.getLabelURL().getPrefix().equals(getPrefix()))
				alreadyInSameStorageLocation = true;
			if (!alreadyInSameStorageLocation) {
				String desiredFileName = limg.getLabelURL().getFileName();
				if (desiredFileName != null && desiredFileName.contains("#"))
					desiredFileName = desiredFileName.substring(desiredFileName.indexOf("#") + 1);
				if (optFileNameMainAndLabelPrefix != null && optFileNameMainAndLabelPrefix.length > 1) {
					pre = optFileNameMainAndLabelPrefix[1];
					String substanceName = limg.getSubstanceName();
					desiredFileName = ActionDataExportToVfs.determineBinaryFileName(snapshotTime, substanceName, limg, limg);// + "#" + desiredFileName;
					desiredFileName = desiredFileName.substring(0, desiredFileName.length() - limg.getLabelURL().getFileNameExtension().length()) + "."
							+ IAPservice.getTargetFileExtension(false, limg.getLabelURL().getFileNameExtension());
				}
				String targetFileNameFullRes = prepareAndGetDataFileNameAndPath(ehi, snapshotTime, pre + desiredFileName.split("#")[0]);
				MyByteArrayInputStream labelStream = ResourceIOManager.getInputStreamMemoryCached(
						limg.getLabelURL().getInputStream());
				if (labelStream != null && labelStream.getCount() > 0)
					saveStream(targetFileNameFullRes, labelStream, false, labelStream.getCount());
				
				IOurl url = limg.getLabelURL();
				
				String fullPath = new File(targetFileNameFullRes).getParent();
				String subPath = fullPath.startsWith(getTargetPathName()) ? fullPath.substring(getTargetPathName().length()) : fullPath;
				if (url != null) {
					url.setPrefix(getPrefix());
					url.setDetail(subPath);
					if (!pre.isEmpty())
						url.setFileName(pre + desiredFileName);
					else
						url.setFileName(desiredFileName);
				}
			}
		}
		{
			String targetFileNamePreview = prepareAndGetPreviewFileNameAndPath(ehi,
					snapshotTime, firstPre + finalMainName.split("#")[0]);
			targetFileNamePreview = targetFileNamePreview.substring(0, targetFileNamePreview.length() - limg.getURL().getFileNameExtension().length()) + "."
					+ SystemOptions.getInstance().getString("IAP", "Preview File Type", "png");
			MyByteArrayInputStream previewStream = MyImageIOhelper.getPreviewImageStream(limg.getLoadedImage());
			if (previewStream != null && previewStream.getCount() > 0)
				saveStream(targetFileNamePreview, previewStream, false, previewStream.getCount());
		}
		if (!keepRemoteLabelURLs_safe_space) {
			// copy label and annotation files...
		}
		return limg;
	}
	
	public String prepareAndGetPreviewFileNameAndPath(ExperimentHeaderInterface experimentHeader, Long optSnapshotTime, String zefn) {
		String subPath = getTargetDirectory(experimentHeader, optSnapshotTime);
		if (subPath.startsWith(DIRECTORY_FOLDER_NAME) || subPath.startsWith(CONDITION_FOLDER_NAME))
			throw new UnsupportedOperationException("Invalid storage subpath calculated for experiment " + experimentHeader.getExperimentName()
					+ ". May not start with " + DIRECTORY_FOLDER_NAME + " or " + CONDITION_FOLDER_NAME + "!");
		String res = ICON_FOLDER_NAME + File.separator + subPath;
		// if (!new File(res).exists())
		// new File(res).mkdirs();
		return res + File.separator + filterBadChars(zefn, true);
	}
	
	public String prepareAndGetDataFileNameAndPath(ExperimentHeaderInterface experimentHeader, Long optSnapshotTime, String zefn) {
		String subPath = getTargetDirectory(experimentHeader, optSnapshotTime);
		if (subPath.startsWith(DIRECTORY_FOLDER_NAME) || subPath.startsWith("bbb_"))
			throw new UnsupportedOperationException("Invalid storage subpath calculated for experiment " + experimentHeader.getExperimentName()
					+ ". May not start with " + DIRECTORY_FOLDER_NAME + " or " + CONDITION_FOLDER_NAME + "!");
		String res = DATA_FOLDER_NAME + File.separator + subPath;
		// if (!new File(res).exists())
		// new File(res).mkdirs();
		if (zefn.contains("#"))
			return res + File.separator + filterBadChars(zefn.split("#", 2)[0], true) + "#" + zefn.split("#", 2)[1];
		else
			return res + File.separator + filterBadChars(zefn, true);
	}
	
	public String getTargetDirectory(ExperimentHeaderInterface experimentHeader, Long optSnapshotTime) {
		GregorianCalendar cal = new GregorianCalendar();
		if (optSnapshotTime != null)
			cal.setTime(new Date(optSnapshotTime));
		String pre = "";
		if (experimentHeader.getExperimentType() != null && experimentHeader.getExperimentType().length() > 0)
			pre = experimentHeader.getExperimentType() + File.separator;
		return pre +
				filterBadChars(experimentHeader.getCoordinator(), false) + File.separator +
				filterBadChars(experimentHeader.getExperimentName(), true) +
				(optSnapshotTime == null ? "" : File.separator +
						cal.get(GregorianCalendar.YEAR) + "-" + digit2(cal.get(GregorianCalendar.MONTH) + 1) + "-" + digit2(cal.get(GregorianCalendar.DAY_OF_MONTH)));
	}
	
	private String filterBadChars(String string, boolean isFinalFileName) {
		return HSMfolderTargetDataManager.filterBadChars(string, isFinalFileName);
	}
	
	public static String digit2(int i) {
		if (i < 10)
			return "0" + i;
		else
			return "" + i;
	}
	
	public static String digit3(int i) {
		if (i < 10)
			return "00" + i;
		else
			if (i < 100)
				return "0" + i;
			else
				return "" + i;
	}
	
	@Override
	public void saveVolume(LoadedVolume volume, Sample3D s3d, MongoDB m,
			InputStream threeDvolumePreviewIcon,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		//
		ExperimentHeaderInterface ehi = volume.getParentSample().getParentCondition().getExperimentHeader();
		long snapshotTime = volume.getParentSample().getSampleFineTimeOrRowId();
		String pre = "";
		String firstPre = "";
		String finalMainName = null;
		{ // save main
			String desiredFileName = volume.getURL().getFileName();
			if (desiredFileName != null && desiredFileName.contains("#"))
				desiredFileName = desiredFileName.substring(desiredFileName.indexOf("#") + 1);
			String substanceName = volume.getSubstanceName();
			desiredFileName = ActionDataExportToVfs.determineBinaryFileName(snapshotTime, substanceName, volume, volume);// + "#" + desiredFileName;
			finalMainName = desiredFileName;
			// if (optFileNameMainAndLabelPrefix != null && optFileNameMainAndLabelPrefix.length > 0) {
			// pre = optFileNameMainAndLabelPrefix[0];
			// firstPre = pre;
			// }
			String targetFileNameFullRes = prepareAndGetDataFileNameAndPath(ehi, snapshotTime, pre + desiredFileName.split("#")[0]);
			MyByteArrayInputStream mainStream = ResourceIOManager.getInputStreamMemoryCached(volume.getInputStream());
			if (mainStream != null && mainStream.getCount() > 0)
				saveStream(targetFileNameFullRes, mainStream, false, mainStream.getCount());
			
			IOurl url = volume.getURL();
			
			String fullPath = new File(targetFileNameFullRes).getParent();
			String subPath = fullPath.startsWith(getTargetPathName()) ? fullPath.substring(getTargetPathName().length()) : fullPath;
			if (url != null) {
				url.setPrefix(getPrefix());
				url.setDetail(subPath);
				if (!pre.isEmpty())
					url.setFileName(pre + desiredFileName);
				else
					url.setFileName(desiredFileName);
			}
		}
		// if (limg.getLabelURL() != null && !ignoreLabelURL) { // save label
		// String desiredFileName = limg.getLabelURL().getFileName();
		// if (desiredFileName != null && desiredFileName.contains("#"))
		// desiredFileName = desiredFileName.substring(desiredFileName.indexOf("#") + 1);
		// if (optFileNameMainAndLabelPrefix != null && optFileNameMainAndLabelPrefix.length > 1) {
		// pre = optFileNameMainAndLabelPrefix[1];
		// String substanceName = limg.getSubstanceName();
		// desiredFileName = ActionDataExportToVfs.determineBinaryFileName(snapshotTime, substanceName, limg, limg);// + "#" + desiredFileName;
		// }
		// String targetFileNameFullRes = prepareAndGetDataFileNameAndPath(ehi, snapshotTime, pre + desiredFileName.split("#")[0]);
		// MyByteArrayInputStream labelStream = ResourceIOManager.getInputStreamMemoryCached(
		// limg.getLabelURL().getInputStream());
		// if (labelStream != null && labelStream.getCount() > 0)
		// saveStream(targetFileNameFullRes, labelStream, false, labelStream.getCount());
		//
		// IOurl url = limg.getLabelURL();
		//
		// String fullPath = new File(targetFileNameFullRes).getParent();
		// String subPath = fullPath.startsWith(getTargetPathName()) ? fullPath.substring(getTargetPathName().length()) : fullPath;
		// if (url != null) {
		// url.setPrefix(getPrefix());
		// url.setDetail(subPath);
		// if (!pre.isEmpty())
		// url.setFileName(pre + desiredFileName);
		// else
		// url.setFileName(desiredFileName);
		// }
		// }
		{
			String targetFileNamePreview = prepareAndGetPreviewFileNameAndPath(ehi,
					snapshotTime, firstPre + finalMainName.split("#")[0]);
			
			if (threeDvolumePreviewIcon == null) {
				StopWatch ss = new StopWatch(SystemAnalysis.getCurrentTime() + ">CREATE GIF 512x512", true);
				threeDvolumePreviewIcon = IOmodule.getThreeDvolumeRenderViewGif((LoadedVolumeExtension) volume, optStatus);
				ss.printTime();
			}
			MyByteArrayInputStream previewStream = (MyByteArrayInputStream) threeDvolumePreviewIcon;// MyImageIOhelper.getPreviewImageStream(limg.getLoadedImage());
			if (previewStream != null && previewStream.getCount() > 0)
				saveStream(targetFileNamePreview, previewStream, false, previewStream.getCount());
		}
	}
	
	public static VirtualFileSystemVFS2 getKnownFromDatabaseId(String databaseId) {
		String prefix = databaseId.split(":")[0];
		for (VirtualFileSystem k : getKnown(true))
			if (k.getPrefix().equals(prefix))
				return (VirtualFileSystemVFS2) k;
		ResourceIOHandler h = ResourceIOManager.getHandlerFromPrefix(prefix);
		if (h != null && h instanceof VirtualFileSystemHandler)
			if (((VirtualFileSystemHandler) h).getVFS() != null && ((VirtualFileSystemHandler) h).getVFS() instanceof VirtualFileSystemVFS2)
				return (VirtualFileSystemVFS2) ((VirtualFileSystemHandler) h).getVFS();
		return null;
	}
	
	private static long lastSpeedInfoRequest = 0;
	private static long lastRead = 0;
	private static long lastWrite = 0;
	private static String lastSpeed = "";
	
	public static String getVFSspeedInfo(String pre, String post) {
		long currentSpeedInfoRequest = System.currentTimeMillis();
		String info = "";
		if (lastSpeedInfoRequest > 0 && lastSpeedInfoRequest < currentSpeedInfoRequest) {
			if (currentSpeedInfoRequest - lastSpeedInfoRequest < 15000)
				return lastSpeed;
			long time = currentSpeedInfoRequest - lastSpeedInfoRequest;
			long read = readCounter.getLong() - lastRead;
			long write = writeCounter.getLong() - lastWrite;
			
			String readSpeed = SystemAnalysis.getDataTransferSpeedString(readCounter.getLong(), read, 0, time);
			info = readSpeed;
			
			String writeSpeed = SystemAnalysis.getDataTransferSpeedString(writeCounter.getLong(), write, 0, time);
			info = "in " + info + ", out " + writeSpeed;
		}
		lastSpeedInfoRequest = currentSpeedInfoRequest;
		lastRead = readCounter.getLong();
		lastWrite = writeCounter.getLong();
		
		if (info.length() > 0) {
			lastSpeed = pre + info + post;
			return lastSpeed;
		} else
			return "";
	}
	
	private String getPass() {
		if (user != null && pass != null && pass.equals("?")) {
			askForPassword = true;
			Object[] inp = MyInputHelper.getInput("For accessing " + host + " as user " + user + " you need to provide a password:",
					"Enter Password", "Password", "");
			if (inp != null)
				pass = (String) inp[0];
			else
				pass = null;
		}
		return pass;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setFolder(String dir) {
		this.folder = dir;
	}
	
	public void setUseForMongo(boolean useForMongoFileStorage) {
		this.useForMongoFileStorage = useForMongoFileStorage;
	}
	
	public void setMongoFileStorageName(String useForMongoFileStorageCloudName) {
		this.useForMongoFileStorageCloudName = useForMongoFileStorageCloudName;
	}
	
	public void setUseOnlyForMongoFileStorage(boolean useOnlyForMongoFileStorage) {
		this.useOnlyForMongoFileStorage = useOnlyForMongoFileStorage;
	}
	
	public void setPassword(String pass) {
		this.pass = pass;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public boolean isAbleToSaveData() {
		switch (vfs_type) {
			case FTP:
				return true;
			case FTPS:
				return true;
			case HTTP:
				return false;
			case HTTPS:
				return false;
			case LOCAL:
				return true;
			case SFTP:
				return true;
			case WebDAV:
				return true;
			default:
				return false;
		}
	}
}

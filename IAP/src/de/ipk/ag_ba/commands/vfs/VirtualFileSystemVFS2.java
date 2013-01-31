package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk.vanted.util.VfsFileObjectUtil;
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
	private static final String CONDITION_FOLDER_NAME = "conditions";
	
	private final VfsFileProtocol vfs_type;
	private final String description;
	private final String protocoll;
	private final String host;
	private final String user;
	private final String pass;
	private final String folder;
	private final String prefix;
	private final boolean useForMongoFileStorage;
	private final boolean useOnlyForMongoFileStorage;
	private final String useForMongoFileStorageCloudName;
	
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
		
		VfsFileObject file = VfsFileObjectUtil.createVfsFileObject(vfs_type,
				host, path, user, pass);
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
		if (fileNameInclSubFolderPathName != null && fileNameInclSubFolderPathName.startsWith("/"))
			fileNameInclSubFolderPathName = fileNameInclSubFolderPathName.substring("/".length());
		if (fileNameInclSubFolderPathName != null && fileNameInclSubFolderPathName.startsWith("\\"))
			fileNameInclSubFolderPathName = fileNameInclSubFolderPathName.substring("\\".length());
		return VfsFileObjectUtil.createVfsFileObject(
				vfs_type, host,
				(absoluteDirName ? "" : folder + "/") +
						fileNameInclSubFolderPathName, user, pass);
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
		return SystemOptions.getInstance().getBoolean("VFS", vfs_type.name() + " - limit concurrent use", false);
	}
	
	Object in = new Object();
	
	public static ThreadSafeOptions readCounter = new ThreadSafeOptions();
	public static ThreadSafeOptions writeCounter = new ThreadSafeOptions();
	
	private static PriorityLock lock = new PriorityLock();
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		try {
			if (doLocking())
				lock.lock(Priority.HIGH);
			if (doPrintStatus())
				System.out.print("[l");
			String fn = url.getDetail() + "/" + url.getFileName().split("#", 2)[0];
			VfsFileObject file = newVfsFile(fn);
			if (file == null)
				return null;
			if (!file.exists())
				return null;
			MyByteArrayInputStream is = ResourceIOManager.getInputStreamMemoryCached(file.getInputStream());
			readCounter.addLong(is.getCount());
			if (doPrintStatus())
				System.out.print("]");
			return is;
		} finally {
			if (doLocking())
				lock.unlock();
		}
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url) throws Exception {
		if (url.getDetail() != null && url.getDetail().startsWith("data")) {
			VfsFileObject file = newVfsFile("icons" + url.getDetail().substring("data".length()) + "/" + url.getFileName().split("#", 2)[0]);
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
		VfsFileObject file = newVfsFile(url.getFileName());
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
			boolean keepRemoteLabelURLs_safe_space) throws Exception {
		ExperimentHeaderInterface ehi = limg.getParentSample().getParentCondition().getExperimentHeader();
		long snapshotTime = limg.getParentSample().getSampleFineTimeOrRowId();
		String pre = "";
		String firstPre = "";
		String finalMainName = null;
		{ // save main
			String desiredFileName = limg.getURL().getFileName();
			if (desiredFileName != null && desiredFileName.contains("#"))
				desiredFileName = desiredFileName.substring(desiredFileName.indexOf("#") + 1);
			String substanceName = limg.getSubstanceName();
			desiredFileName = ActionDataExportToVfs.determineBinaryFileName(snapshotTime, substanceName, limg, limg);// + "#" + desiredFileName;
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
		if (limg.getLabelURL() != null) { // save label
			String desiredFileName = limg.getLabelURL().getFileName();
			if (desiredFileName != null && desiredFileName.contains("#"))
				desiredFileName = desiredFileName.substring(desiredFileName.indexOf("#") + 1);
			if (optFileNameMainAndLabelPrefix != null && optFileNameMainAndLabelPrefix.length > 1) {
				pre = optFileNameMainAndLabelPrefix[1];
				String substanceName = limg.getSubstanceName();
				desiredFileName = ActionDataExportToVfs.determineBinaryFileName(snapshotTime, substanceName, limg, limg);// + "#" + desiredFileName;
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
		{
			String targetFileNamePreview = prepareAndGetPreviewFileNameAndPath(ehi,
					snapshotTime, firstPre + finalMainName.split("#")[0]);
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
		String res = "icons" + File.separator + subPath;
		// if (!new File(res).exists())
		// new File(res).mkdirs();
		return res + File.separator + filterBadChars(zefn);
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
			return res + File.separator + filterBadChars(zefn.split("#", 2)[0]) + "#" + zefn.split("#", 2)[1];
		else
			return res + File.separator + filterBadChars(zefn);
	}
	
	public String getTargetDirectory(ExperimentHeaderInterface experimentHeader, Long optSnapshotTime) {
		GregorianCalendar cal = new GregorianCalendar();
		if (optSnapshotTime != null)
			cal.setTime(new Date(optSnapshotTime));
		String pre = "";
		if (experimentHeader.getExperimentType() != null && experimentHeader.getExperimentType().length() > 0)
			pre = experimentHeader.getExperimentType() + File.separator;
		return pre +
				filterBadChars(experimentHeader.getCoordinator()) + File.separator +
				filterBadChars(experimentHeader.getExperimentName()) +
				(optSnapshotTime == null ? "" : File.separator +
						cal.get(GregorianCalendar.YEAR) + "-" + digit2(cal.get(GregorianCalendar.MONTH) + 1) + "-" + digit2(cal.get(GregorianCalendar.DAY_OF_MONTH)));
	}
	
	private String filterBadChars(String string) {
		String s = StringManipulationTools.UnicodeToURLsyntax(string);
		s = StringManipulationTools.stringReplace(s, "%32", " ");
		s = StringManipulationTools.stringReplace(s, "%95", "_");
		s = StringManipulationTools.stringReplace(s, "%40", "(");
		s = StringManipulationTools.stringReplace(s, "%41", ")");
		s = StringManipulationTools.stringReplace(s, "%44", ",");
		s = StringManipulationTools.stringReplace(s, "%45", "-");
		s = StringManipulationTools.stringReplace(s, "%46", ".");
		s = StringManipulationTools.stringReplace(s, "..", "%46%46");
		return s;
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
	}
	
	public static VirtualFileSystemVFS2 getKnownFromDatabaseId(String databaseId) {
		String prefix = databaseId.split(":")[0];
		for (VirtualFileSystem k : getKnown(true))
			if (k.getPrefix().equals(prefix))
				return (VirtualFileSystemVFS2) k;
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
			if (currentSpeedInfoRequest - lastSpeedInfoRequest < 10000)
				return lastSpeed;
			long time = currentSpeedInfoRequest - lastSpeedInfoRequest;
			long read = readCounter.getLong() - lastRead;
			long write = writeCounter.getLong() - lastWrite;
			
			String readSpeed = SystemAnalysis.getDataTransferSpeedString(read, 0, time);
			info = readSpeed;
			
			String writeSpeed = SystemAnalysis.getDataTransferSpeedString(write, 0, time);
			info = "in " + info + ", out " + writeSpeed + " ("
					+ SystemAnalysis.getDataAmountString(readCounter.getLong())
					+ ", "
					+ SystemAnalysis.getDataAmountString(writeCounter.getLong()) + ")";
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
}

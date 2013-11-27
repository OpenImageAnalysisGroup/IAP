package de.ipk.vanted.test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

public class TestFtpVFS {

	public static void main(String[] args) {
		FileSystemManager manager;
		try {
			manager = VFS.getManager();
			FileObject ftpFile = manager
					.resolveFile("ftp://phenomics:sdf3hdf@173.231.32.249:21/");
			// .resolveFile("ftp://chendijun:chendijun@10.71.115.70:21/home/clone");
			FileObject[] children = ftpFile.getChildren();
			System.out.println("Children of " + ftpFile.getName().getURI());
			for (FileObject child : children) {
				String baseName = child.getName().getBaseName();
				System.out.println(baseName);
			}
		} catch (FileSystemException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

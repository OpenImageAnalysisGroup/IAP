package org;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

public class Screenshot {
	
	private final String screenshotFileName;
	private final InputStream screenshotImage;
	private final long time;
	
	public Screenshot() throws IOException, AWTException {
		this.time = System.currentTimeMillis();
		this.screenshotImage = getImage();
		this.screenshotFileName = getFileName();
	}
	
	private static InputStream getImage() throws IOException, AWTException {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		final BufferedImage capture = new Robot().createScreenCapture(screenRect);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(capture, "png", baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}
	
	private static String getFileName() {
		String fileN;
		try {
			fileN = SystemAnalysis.getLocalHost().getCanonicalHostName() + " (" + SystemAnalysis.getCurrentTimeInclSec() + ").png";
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			fileN = "unknown host (" + SystemAnalysis.getCurrentTimeInclSec() + ").png";
		}
		return fileN;
	}
	
	public String getScreenshotFileName() {
		return screenshotFileName;
	}
	
	public InputStream getScreenshotImage() {
		return screenshotImage;
	}
	
	public long getTime() {
		return time;
	}
	
}

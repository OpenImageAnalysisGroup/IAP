package util;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.time.Instant;

import javax.imageio.ImageIO;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;

import de.ipk.ag_ba.gui.util.SystemAnalysisExtWin;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

public class Screenshot {
	
	private static final long startTime = System.currentTimeMillis();
	private final String screenshotFileName;
	private final String screenshotStaticFileName;
	private final MyByteArrayInputStream screenshotImage;
	private final long time;
	
	public Screenshot() throws IOException, AWTException {
		this.time = System.currentTimeMillis();
		this.screenshotImage = getImage();
		this.screenshotFileName = getFileName();
		this.screenshotStaticFileName = getStaticFileName();
	}
	
	private static MyByteArrayInputStream getImage() throws IOException, AWTException {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage capture = new Robot().createScreenCapture(screenRect);
		
		Integer idleTimeMS = SystemAnalysisExtWin.getIdleTimeMillis();
		Integer uptimeMS = SystemAnalysisExtWin.getUptimeMillis();
		Integer lastUserInput = SystemAnalysisExtWin.getLastUserInputTimeMillis();
		
		if (idleTimeMS != null) {
			try {
				Image i = new Image(capture);
				int privacyMinutes = SystemOptions.getInstance().getInteger("Watch-Service", "Screenshot//Screenshot-Privacy_User_Activity_min", 15);
				boolean active = idleTimeMS.longValue() < privacyMinutes * 60l * 1000l;
				if (active) {
					i = i.io().canvas().fillRect(10, 10, i.getWidth() - 10, i.getHeight() - 10, Color.RED.getRGB(), 0.9).io()
							.blurImageJ(40).getImage();
					i = i.io().canvas().text(100, 150, "Privacy Protection - User Active on Host", Color.BLACK, 50).getImage();
					i = i.io().canvas().text(100, 220, "Screen capture will resume after " + privacyMinutes + " min of inactivity", Color.BLACK, 30).getImage();
				}
				i = i.io().addBorder(0, 40, 0, -40, Color.BLACK.getRGB()).getImage();
				i = i.io().canvas().text(10, i.getHeight() - 42,
						SystemAnalysis.getUserName() + "@" + SystemAnalysisExt.getHostNameWithNoStartupTimeAndNoError() + ", "
								+ "System Boot: " + SystemAnalysis.getCurrentTime(System.currentTimeMillis() - uptimeMS) + " (up since "
								+ SystemAnalysis.getWaitTime(0l + uptimeMS.intValue()) +
								")\nLast User Input: " + SystemAnalysis.getCurrentTime(System.currentTimeMillis()
										- idleTimeMS.longValue()) +
								" (" +
								(!active ?
										StringManipulationTools.stringReplace(
												SystemAnalysis.getWaitTime(idleTimeMS.longValue()),
												"&lt;", "less than ") + " ago" : "user active on console")
								+ "), Current Time: " + SystemAnalysis.getCurrentTime() + ":" + (Instant.now().getEpochSecond() % 60)
						,
						Color.WHITE, 30).getImage();
				capture = i.getAsBufferedImage(false);
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Could not add user input and windows uptime information to image: "
						+ e.getMessage());
			}
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(capture, SystemOptions.getInstance().getString("IAP", "Screenshot File Type", "png"), baos);
		MyByteArrayInputStream is = new MyByteArrayInputStream(baos.toByteArray());
		return is;
	}
	
	private static String getFileName() {
		String fileN;
		try {
			fileN = SystemAnalysis.getLocalHost().getCanonicalHostName() + " (" + SystemAnalysis.getCurrentTimeInclSec() + ").png";
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			fileN = "unknown host (" + SystemAnalysis.getCurrentTimeInclSec() + ")." + SystemOptions.getInstance().getString("IAP", "Preview File Type", "png");
		}
		if (fileN != null)
			fileN = StringManipulationTools.getFileSystemName(fileN);
		return fileN;
	}
	
	private static String getStaticFileName() {
		String fileN;
		try {
			fileN = SystemAnalysis.getLocalHost().getCanonicalHostName() + "." + SystemOptions.getInstance().getString("IAP", "Preview File Type", "png");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			fileN = "unknown host (up " + SystemAnalysis.getCurrentTime(startTime) + ")."
					+ SystemOptions.getInstance().getString("IAP", "Preview File Type", "png");
		}
		if (fileN != null)
			fileN = StringManipulationTools.getFileSystemName(fileN);
		return fileN;
	}
	
	public String getScreenshotFileName() {
		return screenshotFileName;
	}
	
	public InputStream getScreenshotImage() {
		return new MyByteArrayInputStream(screenshotImage.getBuffTrimmed());
	}
	
	public long getTime() {
		return time;
	}
	
	public String getScreenshotStaticFileName() {
		return screenshotStaticFileName;
	}
	
}

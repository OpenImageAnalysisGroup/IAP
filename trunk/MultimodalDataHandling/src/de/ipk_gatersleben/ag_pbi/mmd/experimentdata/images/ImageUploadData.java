/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.ErrorMsg;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;

/**
 * @author klukas
 */
public class ImageUploadData {
	
	private final MyByteArrayInputStream is, isLabelField;
	private final ByteArrayInputStream bisPreview;
	
	/**
	 * Length of primary input stream.
	 */
	private final long length;
	private File srcFile;
	
	/**
	 * @param length
	 *           Length of primary input stream.
	 * @param is
	 * @param isLabelField
	 * @param bisPreview
	 */
	public ImageUploadData(long length, MyByteArrayInputStream is, MyByteArrayInputStream isLabelField,
						ByteArrayInputStream bisPreview) {
		this.is = is;
		this.isLabelField = isLabelField;
		this.bisPreview = bisPreview;
		this.length = length;
	}
	
	public ImageUploadData(long length, File srcFile, MyByteArrayInputStream bisPreview) {
		this.is = null;
		this.srcFile = srcFile;
		this.bisPreview = bisPreview;
		this.isLabelField = null;
		this.length = length;
	}
	
	public InputStream getInputStream() {
		if (srcFile != null)
			try {
				return new FileInputStream(srcFile);
			} catch (FileNotFoundException e) {
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		else
			return new MyByteArrayInputStream(is.getBuff(), is.available());
	}
	
	public InputStream getPreviewInputStream() {
		return bisPreview;
	}
	
	public InputStream getInputStreamLabelField() {
		return isLabelField;
	}
	
	public long getLength() {
		if (length < 0)
			System.out.println("Error: negative length!");
		return length;
	}
	
	public void closeStreams() {
		if (is != null)
			try {
				is.close();
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		if (bisPreview != null)
			try {
				bisPreview.close();
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		if (isLabelField != null)
			try {
				isLabelField.close();
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
	}
}

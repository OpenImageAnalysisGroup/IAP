package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults;

import java.awt.image.BufferedImage;
import java.util.HashSet;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlQRCodeReader extends AbstractBlock implements WellProcessor {
	
	@Override
	public int getDefinedWellCount(ImageProcessorOptionsAndResults options) {
		return options.getIntSetting(this, "Maximum QR Code Count", 10);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public BlockType getBlockType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void prepare() {
		optionsAndResults.getIntSetting(this, "Grid x-dimension", 1);
		optionsAndResults.getIntSetting(this, "Grid y-dimension", 1);
	};
	
	@Override
	protected Image processMask(Image mask) {
		com.google.zxing.Result[] results = detectQRCodes(mask);
		int numOfdetectedQR = results.length;
		int currentWell = getWellIdx();
		Result validCode;
		int w = mask.getWidth();
		int h = mask.getHeight();
		int[] pixels = mask.getAs1A();
		int[] result = new int[pixels.length];
		int width = mask.getWidth();
		int height = mask.getHeight();
		
		if (currentWell < numOfdetectedQR) {
			validCode = results[currentWell];
			ResultPoint[] pos = validCode.getResultPoints();
			ResultPoint actualResultPoint = pos[getWellIdx()];
			int back = optionsAndResults.getBackground();
			int filled = 0;
			for (int i = 0; i < pixels.length; i++) {
				int xPos = i % width;
				int yPos = i / width;
				if (Math.abs(xPos - actualResultPoint.getX()) < 5 || Math.abs(yPos - actualResultPoint.getY()) < 5)
					result[i] = pixels[i];
			}
		} else
			return null;
		
		return new Image(w, h, result);
	}
	
	private Result[] detectQRCodes(Image mask) {
		Result[] results = null;
		try {
			BufferedImage barCodeBufferedImage = mask.getAsBufferedImage();
			LuminanceSource source = new BufferedImageLuminanceSource(barCodeBufferedImage);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			// MultiFormatReader reader = new MultiFormatReader();
			QRCodeMultiReader multiReader = new QRCodeMultiReader();
			results = multiReader.decodeMultiple(bitmap);
			for (Result res : results) {
				String plantID = res.getText();
				System.out.println("Detected plant ID " + plantID + ", create snapshot data...");
			}
		} catch (Exception e) {
			System.out.println(": NO Barcode detected (" + e.getMessage() + ")");
		}
		return results;
	}
	
}

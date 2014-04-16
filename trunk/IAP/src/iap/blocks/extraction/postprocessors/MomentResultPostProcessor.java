package iap.blocks.extraction.postprocessors;

import iap.blocks.data_structures.RunnableOnImageSet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.vecmath.Point2d;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public final class MomentResultPostProcessor implements RunnableOnImageSet {
	private final double length_major;
	private final Point centerOfGravity;
	private final CameraType imageModality;
	private final double length_minor;
	private final double omega;
	
	public MomentResultPostProcessor(double length_major, Point centerOfGravity, CameraType imageModality, double length_minor, double omega) {
		this.length_major = length_major;
		this.centerOfGravity = centerOfGravity;
		this.imageModality = imageModality;
		this.length_minor = length_minor;
		this.omega = omega;
	}
	
	@Override
	public Image postProcessMask(Image img) {
		Point2d p1_start = new Point2d((centerOfGravity.x + length_major * Math.cos(omega)), (centerOfGravity.y + length_major * Math.sin(omega)));
		Point2d p2_start = new Point2d((centerOfGravity.x + length_minor * -Math.sin(omega)), (centerOfGravity.y + length_minor * Math.cos(omega)));
		
		Point2d p1_end = new Point2d((centerOfGravity.x - length_major * Math.cos(omega)), (centerOfGravity.y - length_major * Math.sin(omega)));
		Point2d p2_end = new Point2d((centerOfGravity.x - length_minor * -Math.sin(omega)), (centerOfGravity.y - length_minor * Math.cos(omega)));
		
		// draw moments
		img = img
				.io()
				.canvas()
				.drawLine((int) p1_start.x, (int) p1_start.y, centerOfGravity.x, centerOfGravity.y, Color.PINK.getRGB(), 0.2, 1)
				.drawLine(centerOfGravity.x, centerOfGravity.y, (int) p1_end.x, (int) p1_end.y, Color.PINK.getRGB(), 0.2, 1)
				.drawLine((int) p2_start.x, (int) p2_start.y, centerOfGravity.x, centerOfGravity.y, Color.GREEN.getRGB(), 0.2, 1)
				.drawLine(centerOfGravity.x, centerOfGravity.y, (int) p2_end.x, (int) p2_end.y, Color.GREEN.getRGB(), 0.2, 1)
				.getImage();
		
		// draw MEE
		ImageCanvas canvas = img.io().canvas();
		Graphics2D g = (Graphics2D) canvas.getGraphics();
		g.setColor(Color.GRAY);
		g.rotate(omega, centerOfGravity.x, centerOfGravity.y);
		g.setStroke(new BasicStroke(2f));
		g.drawOval(centerOfGravity.x - (int) length_major, centerOfGravity.y - (int) length_minor, (int) (1 + length_major) * 2,
				(int) (1 + length_minor) * 2);
		canvas.updateFromGraphics();
		img = canvas.getImage();
		
		return img;
	}
	
	@Override
	public Image postProcessImage(Image image) {
		return image;
	}
	
	@Override
	public CameraType getConfig() {
		return imageModality;
	}
}
package de.ipk.ag_ba.apps;

import java.util.ArrayList;

import org.Vector2i;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Christian Klukas
 */
public class IAPstich {
	private final ArrayList<Image> images;
	private final ArrayList<Vector2i> positions;
	private final int columns;
	private final int rows;
	private Image[][] grid;
	private Integer[][] gridIndex;
	
	public IAPstich(ArrayList<Image> images, ArrayList<Vector2i> positions, int columns, int rows) {
		this.images = images;
		this.positions = positions;
		this.columns = columns;
		this.rows = rows;
	}
	
	public void prepare() {
		this.grid = new Image[columns][rows];
		this.gridIndex = new Integer[columns][rows];
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (Vector2i p : positions) {
			if (p.x < minX)
				minX = p.x;
			if (p.y < minY)
				minY = p.y;
			if (p.x > minX)
				maxX = p.x;
			if (p.y < maxY)
				maxY = p.y;
		}
		int gridOffsetX = (maxX - minX) / columns;
		int gridOffsetY = (maxY - minY) / rows;
		ArrayList<Vector2i> toBeProcessedPos = new ArrayList<>(positions);
		ArrayList<Image> toBeProcessedImg = new ArrayList<>(images);
		for (int col = 0; col <= columns; col++) {
			for (int row = 0; row <= rows; row++) {
				int desiredX = minX + col * gridOffsetX;
				int desiredY = minY + col * gridOffsetY;
				Vector2i desire = new Vector2i(desiredX, desiredY);
				int bestOffset = -1;
				double lowestDist = Double.MAX_VALUE;
				int idx = 0;
				for (Vector2i p : toBeProcessedPos) {
					if (p.distance(desire) < lowestDist) {
						bestOffset = idx;
						lowestDist = p.distance(desire);
					}
					idx++;
				}
				grid[col][row] = toBeProcessedImg.remove(bestOffset);
				gridIndex[col][row] = images.indexOf(grid[col][row]);
				
				toBeProcessedImg.remove(bestOffset);
				toBeProcessedPos.remove(bestOffset);
			}
		}
	}
	
	public Image stich() {
		int bl = gridIndex[columns - 1][rows - 1];
		int imageSizeWidth = positions.get(bl).x + images.get(bl).getWidth();
		int imageSizeHeight = positions.get(bl).y + images.get(bl).getHeight();
		
		ImageCanvas c = new Image(imageSizeWidth, imageSizeHeight, 0).io().canvas();
		for (int row = rows - 1; row >= 0; row--) {
			for (int col = columns - 1; col >= 0; col--) {
				Image i_current = grid[col][row];
				Vector2i p_current = col < columns - 1 ? positions.get(gridIndex[col][row]) : null;
				Vector2i p_right = col < columns - 1 ? positions.get(gridIndex[col + 1][row]) : null;
				Vector2i p_below = row < rows - 1 ? positions.get(gridIndex[col][row + 1]) : null;
				if (p_right != null) {
					// cut right part of image
					int endXofCurrentImage = p_current.x + i_current.getWidth();
					int startXofImageRight = p_right.x;
					if (endXofCurrentImage > startXofImageRight) {
						// they overlap
						int middle = (endXofCurrentImage + startXofImageRight) / 2;
						int maxWidth = middle - p_current.x;
						grid[col][row] = i_current.io().cropAbs(0, maxWidth, 0, i_current.getHeight()).getImage();
						i_current = grid[col][row];
					}
				}
				if (p_below != null) {
					// cut lower part of image
					int endYofCurrentImage = p_current.y + i_current.getHeight();
					int startYofImageRight = p_right.y;
					if (endYofCurrentImage > startYofImageRight) {
						// they overlap
						int middle = (endYofCurrentImage + startYofImageRight) / 2;
						int maxHeight = middle - p_current.y;
						grid[col][row] = i_current.io().cropAbs(0, i_current.getWidth(), 0, maxHeight).getImage();
						i_current = grid[col][row];
					}
				}
				c = c.drawImage(i_current, p_current.x, p_current.y);
			}
		}
		
		return c.getImage();
	}
}

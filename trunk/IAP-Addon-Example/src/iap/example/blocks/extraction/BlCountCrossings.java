package iap.example.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.Vector2i;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * @author klukas
 */
public class BlCountCrossings extends AbstractBlock {
	private double circleDist = Double.NaN;
	
	@Override
	public void prepare() {
		this.circleDist = getDouble("Circle Segment Distance", 50);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		return CameraType.getHashSet(CameraType.VIS);
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Count circle crossings";
	}
	
	@Override
	public String getDescription() {
		return "Count crossings of roots with circular rings "
				+ "around the root origin.";
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (circleDist <= 0)
			return mask;
		
		HashMap<String, ArrayList<BlockResultValue>> pr = optionsAndResults.searchResultsOfCurrentSnapshot("root.graph", true, getWellIdx(),
				optionsAndResults.getConfigAndAngle(), false, null);
		if (!pr.isEmpty()) {
			ArrayList<BlockResultValue> br = pr.values().iterator().next();
			if (!br.isEmpty()) {
				Graph g = (Graph) br.iterator().next().getObject();
				double startX = 0, startY = 0;
				int startN = 0;
				for (Node n : g.getNodes()) {
					try {
						if (n.getBoolean("thickStart")) {
							Vector2i p = new NodeHelper(n).getPosition2i();
							startX += p.x;
							startY += p.y;
							startN++;
						}
					} catch (AttributeNotFoundException anf) {
						// empty
					}
				}
				int crossingCount;
				double currDist = 0;
				if (startN > 0) {
					startX = startX / startN;
					startY = startY / startN;
					do {
						currDist += circleDist;
						crossingCount = 0;
						for (Edge e : g.getEdges()) {
							Node nA = e.getSource();
							Node nB = e.getTarget();
						}
					} while (crossingCount > 0);
				}
			}
		}
		return mask;
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "Circle segment distance";
	}
	
}

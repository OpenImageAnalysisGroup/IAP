package tests.JMP.leaf_clustering;

import org.junit.Test;

import tests.JMP.leaf_clustering.LeafTipMatcher.Vismode;

public class TestLeafTipMatchingCorn {
	
	@Test
	public void testMatch() throws Exception {
		String path = "/Schreibtisch/report.csv";
		LeafTipMatcher ltm = new LeafTipMatcher(path);
		ltm.setMinDist(200);
		ltm.matchLeafTips();
		ltm.draw(Vismode.PERDAY, 100, 100);
		ltm.draw(Vismode.PERLEAF, 100, 100);
		Thread.sleep(1000000);
	}
}
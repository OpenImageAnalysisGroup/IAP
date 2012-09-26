/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
/*
 * Created on Aug 10, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author klukas
 */
public class TestFileNameScanner {
	
	public TestFileNameScanner() {
		// empty
	}
	
	@Test
	public void Test() throws Exception {
		FileNameScanner s7 = new FileNameScanner("X-V-G-R", "Ep2-P1-K5-P2093.jpg");
		assertEquals("P1", s7.getVariety());
		assertEquals("K5", s7.getGenotype());
		assertEquals(2093, s7.getReplicateID());
		
		FileNameScanner s3 = new FileNameScanner("V-G-R", "C1-17-P1256.jpg");
		assertEquals("C1", s3.getVariety());
		assertEquals("17", s3.getGenotype());
		assertEquals(1256, s3.getReplicateID());
		
		FileNameScanner s4 = new FileNameScanner("X-V-G-R", "Ep2-C1-3-P2428.jpg");
		assertEquals(s4.getVariety(), "C1");
		assertEquals(s4.getGenotype(), "3");
		assertEquals(s4.getReplicateID(), 2428);
		
		FileNameScanner s10 = new FileNameScanner("X-V-G-R", "Ep2-C1-185-P1044.jpg");
		assertEquals(s10.getVariety(), "C1");
		assertEquals(s10.getGenotype(), "185");
		assertEquals(s10.getReplicateID(), 1044);
		
		FileNameScanner s5 = new FileNameScanner("V-G-R", "C1-1-P1056.jpg");
		assertEquals(s5.getVariety(), "C1");
		assertEquals(s5.getGenotype(), "1");
		assertEquals(s5.getReplicateID(), 1056);
		
		FileNameScanner s6 = new FileNameScanner("X-V-G-R", "Ep2-P1-3-2215.jpg");
		assertEquals("P1", s6.getVariety());
		assertEquals("3", s6.getGenotype());
		assertEquals(2215, s6.getReplicateID());
		
		FileNameScanner s1 = new FileNameScanner("R_D X_X_X_X_S_S_A'Grad'", "001_2010-03-04 16_24_03_LT_FLUO_Side_90Grad.png");
		assertEquals(s1.getReplicateID(), 1);
		assertEquals(s1.getDateYear(), 2010);
		assertEquals(s1.getDateMonth(), 3);
		assertEquals(s1.getDateDay(), 4);
		assertEquals(s1.getSubstance(), "FLUOSide");
		assertEquals(90, s1.getRotation(), 0.001);
		
		FileNameScanner s2 = new FileNameScanner("G_X_R_S_A_X_X_D_X", "H13_02_1020_RgbSide_0_Grad_HL2_2010-06-28_07_44_49.png");
		assertEquals(s2.getGenotype(), "H13");
		assertEquals(s2.getReplicateID(), 1020);
		assertEquals(s2.getDateYear(), 2010);
		assertEquals(s2.getDateMonth(), 6);
		assertEquals(s2.getDateDay(), 28);
		assertEquals(s2.getSubstance(), "RgbSide");
		assertEquals(s2.getRotation(), 0 / 180d * Math.PI, 0.001);
		
		/*
		 * G = genotype, R = replicate ID, X = ignore, A = rotation (degree), D =
		 * date (yyyy-mm-dd), 'some string' = some string (ignored, but may be used
		 * to divide strings), S = substance, V = variety, P = species
		 * Examples: "R_D X_X_X_X_S_S_A'Grad'", "G_X_R_S_A_X_X_D_X", "G_X_R_S_S_D_X"
		 */
		
		FileNameScanner s8 = new FileNameScanner("X_?-T_D_R_C_X_X", "0024 calibration wheat_000668-LETL_2010-09-18_01-20-19_27426_FLUO TV_0_0.png");
		assertEquals(s8.getGenotype(), "H13");
		assertEquals(s8.getTreatment(), "LETL");
		assertEquals(s8.getReplicateID(), 27426);
		assertEquals(s8.getDateYear(), 2010);
		assertEquals(s8.getDateMonth(), 9);
		assertEquals(s8.getDateDay(), 18);
		assertEquals(s8.getSubstance(), "fluo.top");
		assertEquals(s8.getRotation(), 0 / 180d * Math.PI, 0.001);
		
		FileNameScanner s9 = new FileNameScanner("G_X_R_S_A_X_X_D_X", "0024 calibration wheat_000668-LETL_2010-09-18_01-20-19_27426_FLUO TV_0_0.png");
		assertEquals(s9.getGenotype(), "H13");
		assertEquals(s9.getTreatment(), "LETL");
		assertEquals(s9.getReplicateID(), 1020);
		assertEquals(s9.getDateYear(), 2010);
		assertEquals(s9.getDateMonth(), 9);
		assertEquals(s9.getDateDay(), 18);
		assertEquals(s9.getSubstance(), "fluo.top");
		assertEquals(s9.getRotation(), 0 / 180d * Math.PI, 0.001);
	}
}

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
public class MyScannerTest {
	
	public MyScannerTest() {
		// empty
	}
	
	@Test
	public void Test() {
		try {
			MyScanner s1 = new MyScanner("R_D X_X_X_X_S_S_A'Grad'", "001_2010-03-04 16_24_03_LT_FLUO_Side_90Grad.png");
			assertEquals(s1.getReplicateID(), 1);
			assertEquals(s1.getDateYear(), 2010);
			assertEquals(s1.getDateMonth(), 3);
			assertEquals(s1.getDateDay(), 4);
			assertEquals(s1.getSubstance(), "FLUOSide");
			assertEquals(90, s1.getRotation(), 0.001);
			
			MyScanner s2 = new MyScanner("G_X_R_S_A_X_X_D_X", "H13_02_1020_RgbSide_0_Grad_HL2_2010-06-28_07_44_49.png");
			assertEquals(s2.getCondition(), "H13");
			assertEquals(s2.getReplicateID(), 1020);
			assertEquals(s2.getDateYear(), 2010);
			assertEquals(s2.getDateMonth(), 6);
			assertEquals(s2.getDateDay(), 28);
			assertEquals(s2.getSubstance(), "RgbSide");
			assertEquals(s2.getRotation(), 0 / 180d * Math.PI, 0.001);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

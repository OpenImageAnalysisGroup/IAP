package de.ipk.ag_ba.server.task_management;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.MongoDBhandler;
import de.ipk.ag_ba.server.task_management.maize.TestMaize;

/**
 * @author Entzian, Klukas
 */
public class PerformanceTestImages {
	
	public enum ImageNames {
		// TestImageGeneral.class
		RGB_TOP_DAY_0_WT01_1385, FLU_TOP_DAY_0_WT01_1385, NIR_TOP_DAY_0_WT01_1385, RGB_TOP_DAY_2_H31_02_1229, FLU_TOP_DAY_2_H31_02_1229, NIR_TOP_DAY_2_H31_02_1229, REMOVE_SMALL_CLUSTER_TEST_IMAGE_FLUO_TOP, REMOVE_SMALL_CLUSTER_TEST_IMAGE_VIS_TOP, REMOVE_SMALL_CLUSTER_TEST_IMAGE_NIR_TOP,

		// TestImagePhyto.class
		VIS_TOP_JUNITIMAGE3, FLUO_TOP_JUNITIMAGE3, NIR_TOP_JUNITIMAGE3, VIS_TOP_IMAGETEST, FLU_TOP_IMAGETEST, NIR_TOP_IMAGETEST,

		// TestImageBarley.class
		I1036CS109_VIS_SIDE, I1036CS109_FLUO_SIDE, I1036CS109_NIR_SIDE,

		// FindBlueMarkers.class
		MAIZE_VIS_SIDE_0, MAIZE_VIS_SIDE_1,

		// TestFindCentroid.class
		MAIZE_VIS_Top_0, MAIZE_VIS_TOP_1,

		// Pollen
		SCHALE1,
		SCHALE4,
		SCHALE5,

		// Maize
		MAIZE_VIS_1116BA_DAY_63_SIDE,
		MAIZE_FLUO_1116BA_DAY_63_SIDE,
		MAIZE_NIR_1116BA_DAY_63_SIDE,

		// TestSubtractImages
		MAIZE_VIS_REFERENCE_0,
		MAIZE_VIS_BELONG_TO_REFERENCE_0,
		MAIZE_VIS_REFERENCE_1,
		MAIZE_VIS_BELONG_TO_REFERENCE_1,
		MAIZE_TOP_REFERENCE_0,
		MAIZE_TOP_BELONG_TO_REFERENCE_0,
		MAIZE_FLU_REFERENCE_0,
		MAIZE_FLU_BELONG_TO_REFERENCE_0,
		MAIZE_NIR_REFERENCE_0,
		MAIZE_NIR_BELONG_TO_REFERENCE_0,
		MAIZE_TOP_NIR_REFERENCE_0,
		MAIZE_TOP_NIR_BELONG_TO_REFERENCE_0,
		MAIZE_TOP_FLU_REFERENCE_0,
		MAIZE_TOP_FLU_BELONG_TO_REFERENCE_0,

		MAIZE_TOP_VIS_REFERENCE_1,
		MAIZE_TOP_VIS_BELONG_TO_REFERENCE_1,

		MAIZE_VIS_SIDE_REFERENCE_1386,
		MAIZE_VIS_SIDE_BELONG_TO_REFERENCE_1386,
		MAIZE_FLU_SIDE_REFERENCE_1386,
		MAIZE_FLU_SIDE_BELONG_TO_REFERENCE_1386,
		MAIZE_NIR_SIDE_REFERENCE_1386,
		MAIZE_NIR_SIDE_BELONG_TO_REFERENCE_1386,

		MAIZE_TOP_GREEN_BACK0,
		MAIZE_TOP_GREEN_BACK1,
	}
	
	enum ServerTyp {
		MONGO_SERVER, LOCAL_SEVER, LOCAL_FILE
	}
	
	private static HashMap<ImageNames, String> imageMap = new HashMap<ImageNames, String>() {
		private static final long serialVersionUID = 1L;
		
		{
			// TestRandomCalculateGeneralImage
			put(ImageNames.VIS_TOP_JUNITIMAGE3, "vis_top_JUnitImage3.png");
			put(ImageNames.FLUO_TOP_JUNITIMAGE3, "fluo_top_JUnitImage3.png");
			put(ImageNames.NIR_TOP_JUNITIMAGE3, "nir_top_JUnitImage3.png");
			
			// TestPhytoTopAnalysis - BigPlants
			put(ImageNames.RGB_TOP_DAY_0_WT01_1385,
					"rgb_top_day_0_WT01_1385.png");
			put(ImageNames.FLU_TOP_DAY_0_WT01_1385,
					"flu_top_day_0_WT01_1385.png");
			put(ImageNames.NIR_TOP_DAY_0_WT01_1385,
					"nir_top_day_0_WT01_1385.png");
			
			// TestPhytoTopAnalysis - SmallPlants
			put(ImageNames.RGB_TOP_DAY_2_H31_02_1229,
					"rgb_top_day_2_H31_02_1229.png");
			put(ImageNames.FLU_TOP_DAY_2_H31_02_1229,
					"flu_top_day_2_H31_02_1229.png");
			put(ImageNames.NIR_TOP_DAY_2_H31_02_1229,
					"nir_top_day_2_H31_02_1229.png");
			
			// TestShowGeneralImage
			put(ImageNames.VIS_TOP_IMAGETEST, "vis_top_ImageTest.png");
			put(ImageNames.FLU_TOP_IMAGETEST, "flu_top_ImageTest.png");
			put(ImageNames.NIR_TOP_IMAGETEST, "nir_top_ImageTest.png");
			
			// TestRemoveSmallCluster
			put(ImageNames.REMOVE_SMALL_CLUSTER_TEST_IMAGE_VIS_TOP,
					"removeSmallClusterTestImage_vis_top.png");
			put(ImageNames.REMOVE_SMALL_CLUSTER_TEST_IMAGE_FLUO_TOP,
					"removeSmallClusterTestImage_fluo_top.png");
			put(ImageNames.REMOVE_SMALL_CLUSTER_TEST_IMAGE_NIR_TOP,
					"removeSmallClusterTestImage_nir_top.png");
			
			// TestBarleySide
			put(ImageNames.I1036CS109_VIS_SIDE, "1036CS109_vis_side.png");
			put(ImageNames.I1036CS109_FLUO_SIDE, "1036CS109_fluo_side.png");
			put(ImageNames.I1036CS109_NIR_SIDE, "1036CS109_nir_side.png");
			
			// TestFindBlueMarkers
			put(ImageNames.MAIZE_VIS_SIDE_0,
					"vis.side 0Grad day_10 2011-2-11 7_50_24.png");
			put(ImageNames.MAIZE_VIS_SIDE_1,
					"vis.side 225Grad  day_13 2011-2-14 15_32_48.png");
			
			// TestPollenAnalysis
			put(ImageNames.SCHALE1, "SCHALE1.png");
			put(ImageNames.SCHALE4, "SCHALE4.png");
			put(ImageNames.SCHALE5, "SCHALE5.png");
			
			// TestLabFilter -> Maize
			put(ImageNames.MAIZE_VIS_1116BA_DAY_63_SIDE, "maize_VIS_1116BA_day_63_SIDE.png");
			put(ImageNames.MAIZE_FLUO_1116BA_DAY_63_SIDE, "maize_FLUO_1116BA_day_63_SIDE.png");
			put(ImageNames.MAIZE_NIR_1116BA_DAY_63_SIDE, "maize_NIR_1116BA_day_63_SIDE.png");
			
			// Test Find Centroid
			put(ImageNames.MAIZE_VIS_Top_0,
					"vis.top DEG_000 REPL_27 1107BA1354 day_57 2011-04-27 10_23_12.png");
			put(ImageNames.MAIZE_VIS_TOP_1,
					"vis.top DEG_091 REPL_1 1107BA1008 day_39 2011-04-09 08_55_25.png");
			
			// TestSubtractImages
			put(ImageNames.MAIZE_VIS_REFERENCE_0, "referenceMaize0.png");
			put(ImageNames.MAIZE_VIS_BELONG_TO_REFERENCE_0, "belongToRef0.png");
			put(ImageNames.MAIZE_VIS_REFERENCE_1, "1107BA1354 (000)_ref.png");
			put(ImageNames.MAIZE_VIS_BELONG_TO_REFERENCE_1, "1107BA1354 (000).png");
			put(ImageNames.MAIZE_TOP_REFERENCE_0, "1107BA1354 (045)_top_ref.png");
			put(ImageNames.MAIZE_TOP_BELONG_TO_REFERENCE_0, "1107BA1354 (045)_top.png");
			put(ImageNames.MAIZE_FLU_REFERENCE_0, "1107BA1354 (210)_flu_ref.png");
			put(ImageNames.MAIZE_FLU_BELONG_TO_REFERENCE_0, "1107BA1354 (210)_flu.png");
			put(ImageNames.MAIZE_NIR_REFERENCE_0, "1107BA1354 (015)_nir_ref.png");
			put(ImageNames.MAIZE_NIR_BELONG_TO_REFERENCE_0, "1107BA1354 (015)_nir.png");
			put(ImageNames.MAIZE_TOP_NIR_REFERENCE_0, "1107BA1354 (045)_top_nir_ref.png");
			put(ImageNames.MAIZE_TOP_NIR_BELONG_TO_REFERENCE_0, "1107BA1354 (045)_top_nir.png");
			put(ImageNames.MAIZE_TOP_FLU_REFERENCE_0, "1107BA1310 (000)_top_flo_ref.png");
			put(ImageNames.MAIZE_TOP_FLU_BELONG_TO_REFERENCE_0, "1107BA1310 (000)_top_flu.png");
			
			put(ImageNames.MAIZE_TOP_VIS_REFERENCE_1, "1107BA1146 (091)top_vis_ref.png");
			put(ImageNames.MAIZE_TOP_VIS_BELONG_TO_REFERENCE_1, "1107BA1146 (091)_top_vis.png");
			
			put(ImageNames.MAIZE_VIS_SIDE_REFERENCE_1386, "vis.side_ref_1107BA1386(225).png");
			put(ImageNames.MAIZE_VIS_SIDE_BELONG_TO_REFERENCE_1386, "vis.side1107BA1386(225).png");
			put(ImageNames.MAIZE_FLU_SIDE_REFERENCE_1386, "fluo.side_ref_1107BA1386(225).png");
			put(ImageNames.MAIZE_FLU_SIDE_BELONG_TO_REFERENCE_1386, "fluo.side1107BA1386(225).png");
			put(ImageNames.MAIZE_NIR_SIDE_REFERENCE_1386, "nir.side_ref_1107BA1386(225).png");
			put(ImageNames.MAIZE_NIR_SIDE_BELONG_TO_REFERENCE_1386, "nir.side1107BA1386(225).png");
			
			put(ImageNames.MAIZE_TOP_GREEN_BACK0, "1116BA1192 (000).png");
			put(ImageNames.MAIZE_TOP_GREEN_BACK1, "1116BA1265 (000).png");
		}
	};
	
	public static FlexibleImage getImage(ImageNames imageName)
			throws IOException, Exception {
		
		String nameAsStringPlusTyp = getName(imageName);
		Class ref = getClassPath(imageName);
		
		InputStream is = null;
		
		IOurl imageIOurl = getIOurlFromFile(ref, nameAsStringPlusTyp);
		try {
			is = imageIOurl.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new FlexibleImage(imageIOurl);
	}
	
	private static Class getClassPath(ImageNames imageName) {
		Class ref = null;
		
		switch (imageName) {
			
			case VIS_TOP_JUNITIMAGE3:
			case FLUO_TOP_JUNITIMAGE3:
			case NIR_TOP_JUNITIMAGE3:
			case VIS_TOP_IMAGETEST:
			case FLU_TOP_IMAGETEST:
			case NIR_TOP_IMAGETEST:
			case REMOVE_SMALL_CLUSTER_TEST_IMAGE_VIS_TOP:
			case REMOVE_SMALL_CLUSTER_TEST_IMAGE_FLUO_TOP:
			case REMOVE_SMALL_CLUSTER_TEST_IMAGE_NIR_TOP:

				// ref = TestImageGeneral.class;
				break;
			
			case RGB_TOP_DAY_2_H31_02_1229:
			case FLU_TOP_DAY_2_H31_02_1229:
			case NIR_TOP_DAY_2_H31_02_1229:
			case RGB_TOP_DAY_0_WT01_1385:
			case FLU_TOP_DAY_0_WT01_1385:
			case NIR_TOP_DAY_0_WT01_1385:

				// ref = TestImagePhyto.class;
				break;
			
			case I1036CS109_VIS_SIDE:
			case I1036CS109_FLUO_SIDE:
			case I1036CS109_NIR_SIDE:
				// ref = TestImageBarley.class;
				break;
			
			case MAIZE_VIS_SIDE_0:
			case MAIZE_VIS_SIDE_1:
			case MAIZE_VIS_Top_0:
			case MAIZE_VIS_TOP_1:
			case MAIZE_VIS_1116BA_DAY_63_SIDE:
			case MAIZE_FLUO_1116BA_DAY_63_SIDE:
			case MAIZE_NIR_1116BA_DAY_63_SIDE:
			case MAIZE_VIS_REFERENCE_0:
			case MAIZE_VIS_BELONG_TO_REFERENCE_0:
			case MAIZE_VIS_REFERENCE_1:
			case MAIZE_VIS_BELONG_TO_REFERENCE_1:
			case MAIZE_FLU_REFERENCE_0:
			case MAIZE_FLU_BELONG_TO_REFERENCE_0:
			case MAIZE_NIR_REFERENCE_0:
			case MAIZE_NIR_BELONG_TO_REFERENCE_0:
			case MAIZE_TOP_REFERENCE_0:
			case MAIZE_TOP_BELONG_TO_REFERENCE_0:
			case MAIZE_TOP_NIR_REFERENCE_0:
			case MAIZE_TOP_NIR_BELONG_TO_REFERENCE_0:
			case MAIZE_TOP_FLU_REFERENCE_0:
			case MAIZE_TOP_FLU_BELONG_TO_REFERENCE_0:
			case MAIZE_TOP_VIS_REFERENCE_1:
			case MAIZE_TOP_VIS_BELONG_TO_REFERENCE_1:
			case MAIZE_VIS_SIDE_REFERENCE_1386:
			case MAIZE_VIS_SIDE_BELONG_TO_REFERENCE_1386:
			case MAIZE_FLU_SIDE_REFERENCE_1386:
			case MAIZE_FLU_SIDE_BELONG_TO_REFERENCE_1386:
			case MAIZE_NIR_SIDE_REFERENCE_1386:
			case MAIZE_NIR_SIDE_BELONG_TO_REFERENCE_1386:
			case MAIZE_TOP_GREEN_BACK0:
			case MAIZE_TOP_GREEN_BACK1:
				ref = TestMaize.class;
				break;
			
			case SCHALE1:
			case SCHALE4:
			case SCHALE5:
				// ref = TestImagePollen.class;
				break;
			
			default:
				// ref = TestImageGeneral.class;
				break;
		}
		
		return ref;
	}
	
	private static IOurl getIOurlFromFile(Class ref, String nameAsStringPlusTyp)
			throws IOException {
		return new IOurl(GravistoService.getIOurl(ref, nameAsStringPlusTyp,
				null));
	}
	
	private static IOurl getIOurlFromServer(String shaValue,
			String nameAsStringPlusTyp, ServerTyp server) {
		
		MongoDB dc = getMongoServer(server);
		String pre = new MongoDBhandler(dc.getDefaultHost(), dc).getPrefix();
		
		return new IOurl(pre + "://" + shaValue + "/" + nameAsStringPlusTyp);
	}
	
	private static MongoDB getMongoServer(ServerTyp server) {
		
		MongoDB dc = null;
		
		switch (server) {
			case MONGO_SERVER:
				dc = MongoDB.getDefaultCloud();
				break;
			case LOCAL_SEVER:
				dc = MongoDB.getLocalDB();
				break;
		}
		
		return dc;
	}
	
	private static String getName(ImageNames imageName) {
		return imageMap.get(imageName);
	}
}

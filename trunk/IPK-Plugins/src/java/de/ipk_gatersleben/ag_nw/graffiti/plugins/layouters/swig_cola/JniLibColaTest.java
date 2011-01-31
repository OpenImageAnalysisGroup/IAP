package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.swig_cola;

import colajava.ColaEdge;
import colajava.ConstrainedFDLayout;
import colajava.EdgeVector;
import colajava.RectPtrVector;
import colajava.Rectangle;

public class JniLibColaTest {
	public static void main(String[] args) {
		System.load("/home/klukas/Desktop/tims_daten/cola/libcolajni.so");
		Rectangle v1 = new Rectangle(0, 10, 0, 10);
		Rectangle v2 = new Rectangle(20, 30, 0, 10);
		Rectangle v3 = new Rectangle(0, 10, 20, 30);
		Rectangle v4 = new Rectangle(20, 30, 20, 30);
		RectPtrVector rs = new RectPtrVector();
		rs.add(v1);
		rs.add(v2);
		rs.add(v3);
		rs.add(v4);
		EdgeVector es = new EdgeVector();
		es.add(new ColaEdge(0, 1));
		es.add(new ColaEdge(1, 2));
		es.add(new ColaEdge(2, 3));
		es.add(new ColaEdge(1, 3));
		ConstrainedFDLayout alg = new ConstrainedFDLayout(rs, es, 40);
		alg.run();
		System.out.printf("v1=(%f,%f,%f,%f)\n", v1.getMinX(), v1.getMinY(), v1
							.getMaxX(), v1.getMaxY());
		System.out.printf("v2=(%f,%f,%f,%f)\n", v2.getMinX(), v2.getMinY(), v2
							.getMaxX(), v2.getMaxY());
		System.out.printf("v3=(%f,%f,%f,%f)\n", v3.getMinX(), v3.getMinY(), v3
							.getMaxX(), v3.getMaxY());
		System.out.printf("v4=(%f,%f,%f,%f)\n", v4.getMinX(), v4.getMinY(), v4
							.getMaxX(), v4.getMaxY());
	}
}

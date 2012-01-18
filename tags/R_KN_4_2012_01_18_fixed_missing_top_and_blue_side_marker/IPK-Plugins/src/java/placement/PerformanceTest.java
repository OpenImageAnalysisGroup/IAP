package placement;

import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Random;

class Result {
	int size;
	
	double k;
	
	Cost as, as_split, as_oo, as_split_oo, mosek, mosek_oo, fsa;
	
	void writeToFile() {
		File f = new File("PerformanceResults.log");
		Writer output = null;
		try {
			// use buffering
			output = new BufferedWriter(new FileWriter(f, true));
			output.write(toString() + "\n");
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		String s = size + "," + k + "," + as + "," + as_split + "," + as_oo
							+ "," + as_split_oo + "," + mosek + "," + mosek_oo + "," + fsa;
		return s;
	}
	
	public static void writeHeader() {
		File f = new File("PerformanceResults.log");
		Writer output = null;
		try {
			// use buffering
			output = new BufferedWriter(new FileWriter(f, true));
			output
								.write("size,k,as_time,as_disp,as_split_time,as_split_disp,as_oo_time,as_oo_disp,as_split_oo_time,as_split_oo_disp,mosek_time,mosek_disp,mosek_oo_time,mosek_oo_disp,fsa_time,fsa_disp\n");
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Cost {
	long time;
	
	double displacement;
	
	@Override
	public String toString() {
		String s = time + "," + displacement;
		return s;
	}
}

public class PerformanceTest {
	
	PerformanceTest() {
		ArrayList<Result> results = new ArrayList<Result>();
		Result.writeHeader();
		for (int size = 10; size <= 2000; size += 10) {
			Result result = new Result();
			result.size = size;
			ArrayList<Rectangle2D> rs = generateRandom(result, 100, 100, Math
								.sqrt(size) / 5);
			result.fsa = run(new PFS(0, 0), rs);
			result.as = run(new QPRectanglePlacement(false, false, false,
								false, 0, 0, false), rs);
			result.mosek = run(new QPRectanglePlacement(false, false, false,
								true, 0, 0, false), rs);
			result.as_split = run(new QPRectanglePlacement(true, false, false,
								false, 0, 0, false), rs);
			result.as_oo = run(new QPRectanglePlacement(false, false, true,
								false, 0, 0, false), rs);
			result.as_split_oo = run(new QPRectanglePlacement(true, false,
								true, false, 0, 0, false), rs);
			result.mosek_oo = run(new QPRectanglePlacement(false, false, true,
								true, 0, 0, false), rs);
			results.add(result);
			result.writeToFile();
		}
		System.out
							.println("size,k,as_time,as_disp,as_split_time,as_split_disp,as_oo_time,as_oo_disp,as_split_oo_time,as_split_oo_disp,mosek_time,mosek_disp,mosek_oo_time,mosek_oo_disp,fsa_time,fsa_disp");
		for (Result r : results) {
			System.out.println(r);
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// PerformanceTest p = new PerformanceTest();
	}
	
	private Cost run(RectanglePlacement p, ArrayList<Rectangle2D> orig) {
		System.gc();
		Cost c = new Cost();
		long t0 = System.currentTimeMillis();
		ArrayList<Rectangle2D> rs = new ArrayList<Rectangle2D>();
		IdentityHashMap<Rectangle2D, Rectangle2D> rMap = new IdentityHashMap<Rectangle2D, Rectangle2D>();
		for (Rectangle2D r : orig) {
			Rectangle2D r2 = new Rectangle2D.Double();
			r2.setRect(r);
			rs.add(r2);
			rMap.put(r2, r);
		}
		p.place(rs);
		c.displacement = 0;
		for (Rectangle2D r : rs) {
			double dx = Math.abs(r.getMinX() - rMap.get(r).getMinX());
			double dy = 0;// Math.abs(r.getMinY() - rMap.get(r).getMinY());
			c.displacement += Math.sqrt(dx * dx + dy * dy);
		}
		c.time = System.currentTimeMillis() - t0;
		return c;
	}
	
	static ArrayList<Rectangle2D> generateRandom(Result result, double w,
						double h, double rSize) {
		Random rand = new Random();
		ArrayList<Rectangle2D> rectangles = new ArrayList<Rectangle2D>();
		for (int i = 0; i < result.size; i++) {
			Rectangle2D r = new Rectangle2D.Double(w * rand.nextDouble(), h
								* rand.nextDouble(), (w / rSize) * rand.nextDouble(),
								(h / rSize) * rand.nextDouble());
			rectangles.add(r);
		}
		int overlapCount = 0;
		for (int i = 0; i < rectangles.size(); i++) {
			Rectangle2D u = rectangles.get(i);
			for (int j = i + 1; j < rectangles.size(); j++) {
				Rectangle2D v = rectangles.get(j);
				if (u.intersects(v))
					overlapCount++;
			}
		}
		double k = (double) overlapCount / (double) result.size;
		System.out.println("Random graph has k=" + k);
		result.k = k;
		return rectangles;
	}
	
}

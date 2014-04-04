package tests.JMP.methods;

import org.junit.Test;

import de.ipk.ag_ba.image.structures.Image;

public class BHT {
	
	// Image img = HelperMethods.read("");
	// int histogram = getHistogram(img);
	
	@Test
	public void start() {
		counter();
	}
	
	private int BHThreshold(int[] histogram) {
		// only assumptions
		int i_s = 0;
		int i_e = 255;
		
		int i_m = (int) ((i_s + i_e) / 2.0f); // center of the weighing scale I_m
		int w_l = get_weight(i_s, i_m + 1, histogram); // weight on the left W_l
		int w_r = get_weight(i_m + 1, i_e + 1, histogram); // weight on the right W_r
		while (i_s <= i_e) {
			if (w_r > w_l) { // right side is heavier
				w_r -= histogram[i_e--];
				if (((i_s + i_e) / 2) < i_m) {
					w_r += histogram[i_m];
					w_l -= histogram[i_m--];
				}
			} else
				if (w_l >= w_r) { // left side is heavier
					w_l -= histogram[i_s++];
					if (((i_s + i_e) / 2) > i_m) {
						w_l += histogram[i_m + 1];
						w_r -= histogram[i_m + 1];
						i_m++;
					}
				}
		}
		return i_m;
	}
	
	private int get_weight(int i_s, int i, int[] histogram2) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private int getHistogram(Image img2) {
		int[] hist = new int[255];
		
		return 0;
	}
	
	public void counter() {
		int o = 0;
		int p = 0;
		int count = 0;
		while (o <= 5) {
			// meansAndSD.length) {
			// temp += "," + f.format(meansAndSD[o][p]);
			System.out.println(o + " : " + p);
			count++;
			p++;
			if ((count % 3) == 0) {
				o++;
				count = count - 3;
			}
			p = p % 3;
		}
	}
}

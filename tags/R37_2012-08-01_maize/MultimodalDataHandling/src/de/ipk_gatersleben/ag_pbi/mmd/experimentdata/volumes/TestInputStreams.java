package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class TestInputStreams {
	
	@Test
	public void test() {
		int[][][] vol = new int[5][5][5];
		vol[0][0][0] = -1; // this catches signed / unsigned problems, if this would be returned, reading of input stream would stop
		vol[0][0][1] = 0;
		vol[0][0][1] = 1;
		ByteShortIntArray b = new ByteShortIntArray(vol);
		try {
			assertEquals(4 * 5 * 5 * 5, b.getInputStream().available());
			InputStream is = b.getInputStream();
			long sum = 0;
			for (int x = 0; x < 5; x++)
				for (int y = 0; y < 5; y++)
					for (int z = 0; z < 5; z++)
						for (byte i = 0; i < 4; i++)
							sum += is.read();
			assertEquals(0, sum);
			assertEquals(0, is.available());
			assertEquals(-1, is.read());
		} catch (IOException e) {
			assertEquals(null, e);
		}
		b = new ByteShortIntArray(new byte[5][5][5]);
		try {
			assertEquals(5 * 5 * 5, b.getInputStream().available());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

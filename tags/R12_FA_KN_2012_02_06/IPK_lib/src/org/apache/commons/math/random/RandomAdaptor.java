/*
 * Copyright 2005 The Apache Software Foundation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math.random;

import java.util.Random;

/**
 * Extension of <code>java.util.Random</code> wrapping a {@link RandomGenerator}.
 * 
 * @since 1.1
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:03:09 $
 */
public class RandomAdaptor extends Random implements RandomGenerator {

	/** Wrapped randomGenerator instance */
	private RandomGenerator randomGenerator = null;

	/**
	 * Prevent instantiation without a generator argument
	 */
	private RandomAdaptor() {
	}

	/**
	 * Construct a RandomAdaptor wrapping the supplied RandomGenerator.
	 * 
	 * @param randomGenerator
	 *           the wrapped generator
	 */
	public RandomAdaptor(RandomGenerator randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

	/**
	 * Factory method to create a <code>Random</code> using the supplied <code>RandomGenerator</code>.
	 * 
	 * @param randomGenerator
	 *           wrapped RandomGenerator instance
	 * @return a Random instance wrapping the RandomGenerator
	 */
	public static Random createAdaptor(RandomGenerator randomGenerator) {
		return new RandomAdaptor(randomGenerator);
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed <code>boolean</code> value from this random number generator's
	 * sequence.
	 * 
	 * @return the next pseudorandom, uniformly distributed <code>boolean</code> value from this random number generator's
	 *         sequence
	 */
	public boolean nextBoolean() {
		return randomGenerator.nextBoolean();
	}

	/**
	 * Generates random bytes and places them into a user-supplied
	 * byte array. The number of random bytes produced is equal to
	 * the length of the byte array.
	 * 
	 * @param bytes
	 *           the non-null byte array in which to put the
	 *           random bytes
	 */
	public void nextBytes(byte[] bytes) {
		randomGenerator.nextBytes(bytes);
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed <code>double</code> value between <code>0.0</code> and <code>1.0</code> from this random number
	 * generator's sequence.
	 * 
	 * @return the next pseudorandom, uniformly distributed <code>double</code> value between <code>0.0</code> and <code>1.0</code> from this random number
	 *         generator's sequence
	 */
	public double nextDouble() {
		return randomGenerator.nextDouble();
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed <code>float</code> value between <code>0.0</code> and <code>1.0</code> from this random
	 * number generator's sequence.
	 * 
	 * @return the next pseudorandom, uniformly distributed <code>float</code> value between <code>0.0</code> and <code>1.0</code> from this
	 *         random number generator's sequence
	 */
	public float nextFloat() {
		return randomGenerator.nextFloat();
	}

	/**
	 * Returns the next pseudorandom, Gaussian ("normally") distributed <code>double</code> value with mean <code>0.0</code> and standard
	 * deviation <code>1.0</code> from this random number generator's sequence.
	 * 
	 * @return the next pseudorandom, Gaussian ("normally") distributed <code>double</code> value with mean <code>0.0</code> and
	 *         standard deviation <code>1.0</code> from this random number
	 *         generator's sequence
	 */
	public double nextGaussian() {
		return randomGenerator.nextGaussian();
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed <code>int</code> value from this random number generator's sequence.
	 * All 2<font size="-1"><sup>32</sup></font> possible <tt>int</tt> values
	 * should be produced with (approximately) equal probability.
	 * 
	 * @return the next pseudorandom, uniformly distributed <code>int</code> value from this random number generator's sequence
	 */
	public int nextInt() {
		return randomGenerator.nextInt();
	}

	/**
	 * Returns a pseudorandom, uniformly distributed <tt>int</tt> value
	 * between 0 (inclusive) and the specified value (exclusive), drawn from
	 * this random number generator's sequence.
	 * 
	 * @param n
	 *           the bound on the random number to be returned. Must be
	 *           positive.
	 * @return a pseudorandom, uniformly distributed <tt>int</tt> value between 0 (inclusive) and n (exclusive).
	 * @throws IllegalArgumentException
	 *            if n is not positive.
	 */
	public int nextInt(int n) {
		return randomGenerator.nextInt(n);
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed <code>long</code> value from this random number generator's sequence. All
	 * 2<font size="-1"><sup>64</sup></font> possible <tt>long</tt> values
	 * should be produced with (approximately) equal probability.
	 * 
	 * @return the next pseudorandom, uniformly distributed <code>long</code> value from this random number generator's sequence
	 */
	public long nextLong() {
		return randomGenerator.nextLong();
	}

	/**
	 * Sets the seed of the underyling random number generator using a <code>long</code> seed. Sequences of values generated starting with the
	 * same seeds should be identical.
	 * 
	 * @param seed
	 *           the seed value
	 */
	public void setSeed(long seed) {
		if (randomGenerator != null) { // required to avoid NPE in constructor
			randomGenerator.setSeed(seed);
		}
	}
}

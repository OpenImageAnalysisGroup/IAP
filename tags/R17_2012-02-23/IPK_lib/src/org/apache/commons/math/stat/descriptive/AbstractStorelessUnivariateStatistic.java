/*
 * Copyright 2003-2004 The Apache Software Foundation.
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
package org.apache.commons.math.stat.descriptive;

import java.io.Serializable;

import org.apache.commons.math.util.MathUtils;

/**
 * Abstract implementation of the {@link StorelessUnivariateStatistic} interface.
 * <p>
 * Provides default <code>evaluate()</code> and <code>incrementAll(double[])<code>
 * implementations. 
 * <p>
 * <strong>Note that these implementations are not synchronized.</strong>
 * 
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:03:13 $
 */
public abstract class AbstractStorelessUnivariateStatistic
					extends AbstractUnivariateStatistic
					implements StorelessUnivariateStatistic, Serializable {

	/** Serialization UID */
	private static final long serialVersionUID = -44915725420072521L;

	/**
	 * This default implementation calls {@link #clear}, then invokes {@link #increment} in a loop over the the input array, and then uses {@link #getResult} to
	 * compute the return value.
	 * <p>
	 * Note that this implementation changes the internal state of the statistic. Its side effects are the same as invoking {@link #clear} and then
	 * {@link #incrementAll(double[])}.
	 * <p>
	 * Implementations may override this method with a more efficient implementation that works directly with the input array.
	 * <p>
	 * If the array is null, an IllegalArgumentException is thrown.
	 * 
	 * @see org.apache.commons.math.stat.descriptive.UnivariateStatistic#evaluate(double[])
	 */
	public double evaluate(final double[] values) {
		if (values == null) {
			throw new IllegalArgumentException("input value array is null");
		}
		return evaluate(values, 0, values.length);
	}

	/**
	 * This default implementation calls {@link #clear}, then invokes {@link #increment} in a loop over the specified portion of the input
	 * array, and then uses {@link #getResult} to compute the return value.
	 * <p>
	 * Note that this implementation changes the internal state of the statistic. Its side effects are the same as invoking {@link #clear} and then
	 * {@link #incrementAll(double[], int, int)}.
	 * <p>
	 * Implementations may override this method with a more efficient implementation that works directly with the input array.
	 * <p>
	 * If the array is null or the index parameters are not valid, an IllegalArgumentException is thrown.
	 * 
	 * @see org.apache.commons.math.stat.descriptive.UnivariateStatistic#evaluate(double[], int, int)
	 */
	public double evaluate(final double[] values, final int begin, final int length) {
		if (test(values, begin, length)) {
			clear();
			incrementAll(values, begin, length);
		}
		return getResult();
	}

	/**
	 * @see org.apache.commons.math.stat.descriptive.StorelessUnivariateStatistic#clear()
	 */
	public abstract void clear();

	/**
	 * @see org.apache.commons.math.stat.descriptive.StorelessUnivariateStatistic#getResult()
	 */
	public abstract double getResult();

	/**
	 * @see org.apache.commons.math.stat.descriptive.StorelessUnivariateStatistic#increment(double)
	 */
	public abstract void increment(final double d);

	/**
	 * This default implementation just calls {@link #increment} in a loop over
	 * the input array.
	 * <p>
	 * Throws IllegalArgumentException if the input values array is null.
	 * 
	 * @param values
	 *           values to add
	 * @throws IllegalArgumentException
	 *            if values is null
	 * @see org.apache.commons.math.stat.descriptive.StorelessUnivariateStatistic#incrementAll(double[])
	 */
	public void incrementAll(double[] values) {
		if (values == null) {
			throw new IllegalArgumentException("input values array is null");
		}
		incrementAll(values, 0, values.length);
	}

	/**
	 * This default implementation just calls {@link #increment} in a loop over
	 * the specified portion of the input array.
	 * <p>
	 * Throws IllegalArgumentException if the input values array is null.
	 * 
	 * @param values
	 *           array holding values to add
	 * @param begin
	 *           index of the first array element to add
	 * @param length
	 *           number of array elements to add
	 * @throws IllegalArgumentException
	 *            if values is null
	 * @see org.apache.commons.math.stat.descriptive.StorelessUnivariateStatistic#incrementAll(double[], int, int)
	 */
	public void incrementAll(double[] values, int begin, int length) {
		if (test(values, begin, length)) {
			int k = begin + length;
			for (int i = begin; i < k; i++) {
				increment(values[i]);
			}
		}
	}

	/**
	 * Returns true iff <code>object</code> is an <code>AbstractStorelessUnivariateStatistic</code> returning the same
	 * values as this for <code>getResult()</code> and <code>getN()</code>
	 * 
	 * @param object
	 *           object to test equality against.
	 * @return true if object returns the same value as this
	 */
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof AbstractStorelessUnivariateStatistic == false) {
			return false;
		}
		AbstractStorelessUnivariateStatistic stat = (AbstractStorelessUnivariateStatistic) object;
		return (MathUtils.equals(stat.getResult(), this.getResult()) && MathUtils.equals(stat.getN(), this.getN()));
	}

	/**
	 * Returns hash code based on getResult() and getN()
	 * 
	 * @return hash code
	 */
	public int hashCode() {
		return 31 * (31 + MathUtils.hash(getResult())) + MathUtils.hash(getN());
	}

}
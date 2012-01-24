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

/**
 * Base evaluation interface implemented by all statistics.
 * <p>
 * Includes "stateless" <code>evaluate</code> methods that take <code>double[]</code> arrays as input and return the value of the statistic applied to the input
 * values.
 * 
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:03:14 $
 */
public interface UnivariateStatistic {

	/**
	 * Returns the result of evaluating the statistic over the input array.
	 * 
	 * @param values
	 *           input array
	 * @return the value of the statistic applied to the input array
	 */
	double evaluate(double[] values);

	/**
	 * Returns the result of evaluating the statistic over the specified entries
	 * in the input array.
	 * 
	 * @param values
	 *           the input array
	 * @param begin
	 *           the index of the first element to include
	 * @param length
	 *           the number of elements to include
	 * @return the value of the statistic applied to the included array entries
	 */
	double evaluate(double[] values, int begin, int length);

}
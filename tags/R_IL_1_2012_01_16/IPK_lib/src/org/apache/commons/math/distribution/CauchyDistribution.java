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

package org.apache.commons.math.distribution;

/**
 * Cauchy Distribution.
 * Instances of CauchyDistribution objects should be created using {@link DistributionFactory#createCauchyDistribution(double, double)}.
 * <p>
 * <p>
 * References:
 * <p>
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/CauchyDistribution.html"> Cauchy Distribution</a></li>
 * </ul>
 * </p>
 * 
 * @since 1.1
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:02:43 $
 */
public interface CauchyDistribution extends ContinuousDistribution {

	/**
	 * Access the median.
	 * 
	 * @return median for this distribution
	 */
	double getMedian();

	/**
	 * Access the scale parameter.
	 * 
	 * @return scale parameter for this distribution
	 */
	double getScale();

	/**
	 * Modify the median.
	 * 
	 * @param median
	 *           for this distribution
	 */
	void setMedian(double median);

	/**
	 * Modify the scale parameter.
	 * 
	 * @param s
	 *           scale parameter for this distribution
	 */
	void setScale(double s);
}

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

package org.apache.commons.math.distribution;

/**
 * The Hypergeometric Distribution.
 * Instances of HypergeometricDistribution objects should be created using {@link DistributionFactory#createHypergeometricDistribution(int, int, int)}.
 * <p>
 * References:
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/HypergeometricDistribution.html"> Hypergeometric Distribution</a></li>
 * </ul>
 * </p>
 * 
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:02:43 $
 */
public interface HypergeometricDistribution extends IntegerDistribution {
	/**
	 * Access the number of successes.
	 * 
	 * @return the number of successes.
	 */
	public abstract int getNumberOfSuccesses();

	/**
	 * Access the population size.
	 * 
	 * @return the population size.
	 */
	public abstract int getPopulationSize();

	/**
	 * Access the sample size.
	 * 
	 * @return the sample size.
	 */
	public abstract int getSampleSize();

	/**
	 * Modify the number of successes.
	 * 
	 * @param num
	 *           the new number of successes.
	 */
	public abstract void setNumberOfSuccesses(int num);

	/**
	 * Modify the population size.
	 * 
	 * @param size
	 *           the new population size.
	 */
	public abstract void setPopulationSize(int size);

	/**
	 * Modify the sample size.
	 * 
	 * @param size
	 *           the new sample size.
	 */
	public abstract void setSampleSize(int size);
}

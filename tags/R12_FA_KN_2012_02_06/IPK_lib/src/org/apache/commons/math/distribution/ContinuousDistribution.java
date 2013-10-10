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

import org.apache.commons.math.MathException;

/**
 * Base interface for continuous distributions.
 * 
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:02:44 $
 */
public interface ContinuousDistribution extends Distribution {

	/**
	 * For this disbution, X, this method returns x such that P(X &lt; x) = p.
	 * 
	 * @param p
	 *           the cumulative probability.
	 * @return x.
	 * @throws MathException
	 *            if the inverse cumulative probability can not be
	 *            computed due to convergence or other numerical errors.
	 */
	double inverseCumulativeProbability(double p) throws MathException;
}
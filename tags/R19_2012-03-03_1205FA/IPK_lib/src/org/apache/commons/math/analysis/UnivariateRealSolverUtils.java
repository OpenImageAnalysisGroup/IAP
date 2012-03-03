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
package org.apache.commons.math.analysis;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;

/**
 * Utility routines for {@link UnivariateRealSolver} objects.
 * 
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:02:56 $
 */
public class UnivariateRealSolverUtils {
	/**
	 * Default constructor.
	 */
	private UnivariateRealSolverUtils() {
		super();
	}

	/** Cached solver factory */
	private static UnivariateRealSolverFactory factory = null;

	/**
	 * Convenience method to find a zero of a univariate real function. A default
	 * solver is used.
	 * 
	 * @param f
	 *           the function.
	 * @param x0
	 *           the lower bound for the interval.
	 * @param x1
	 *           the upper bound for the interval.
	 * @return a value where the function is zero.
	 * @throws ConvergenceException
	 *            if the iteration count was exceeded
	 * @throws FunctionEvaluationException
	 *            if an error occurs evaluating
	 *            the function
	 * @throws IllegalArgumentException
	 *            if f is null or the endpoints do not
	 *            specify a valid interval
	 */
	public static double solve(UnivariateRealFunction f, double x0, double x1)
						throws ConvergenceException, FunctionEvaluationException {
		setup(f);
		return factory.newDefaultSolver(f).solve(x0, x1);
	}

	/**
	 * Convenience method to find a zero of a univariate real function. A default
	 * solver is used.
	 * 
	 * @param f
	 *           the function
	 * @param x0
	 *           the lower bound for the interval
	 * @param x1
	 *           the upper bound for the interval
	 * @param absoluteAccuracy
	 *           the accuracy to be used by the solver
	 * @return a value where the function is zero
	 * @throws ConvergenceException
	 *            if the iteration count is exceeded
	 * @throws FunctionEvaluationException
	 *            if an error occurs evaluating the
	 *            function
	 * @throws IllegalArgumentException
	 *            if f is null, the endpoints do not
	 *            specify a valid interval, or the absoluteAccuracy is not valid for the
	 *            default solver
	 */
	public static double solve(UnivariateRealFunction f, double x0, double x1,
						double absoluteAccuracy) throws ConvergenceException,
						FunctionEvaluationException {

		setup(f);
		UnivariateRealSolver solver = factory.newDefaultSolver(f);
		solver.setAbsoluteAccuracy(absoluteAccuracy);
		return solver.solve(x0, x1);
	}

	/**
	 * This method attempts to find two values a and b satisfying
	 * <ul>
	 * <li> <code> lowerBound <= a < initial < b <= upperBound</code></li>
	 * <li> <code> f(a) * f(b) < 0 </code></li>
	 * </ul>
	 * If f is continuous on <code>[a,b],</code> this means that <code>a</code> and <code>b</code> bracket a root of f.
	 * <p>
	 * The algorithm starts by setting <code>a := initial -1; b := initial +1,</code> examines the value of the function at <code>a</code> and <code>b</code> and
	 * keeps moving the endpoints out by one unit each time through a loop that terminates when one of the following happens:
	 * <ul>
	 * <li> <code> f(a) * f(b) < 0 </code> -- success!</li>
	 * <li> <code> a = lower </code> and <code> b = upper</code> -- ConvergenceException</li>
	 * <li> <code> Integer.MAX_VALUE</code> iterations elapse -- ConvergenceException</li>
	 * </ul>
	 * <p>
	 * <strong>Note: </strong> this method can take <code>Integer.MAX_VALUE</code> iterations to throw a <code>ConvergenceException.</code> Unless you are
	 * confident that there is a root between <code>lowerBound</code> and <code>upperBound</code> near <code>initial,</code> it is better to use
	 * {@link #bracket(UnivariateRealFunction, double, double, double, int)}, explicitly specifying the maximum number of iterations.
	 * 
	 * @param function
	 *           the function
	 * @param initial
	 *           initial midpoint of interval being expanded to
	 *           bracket a root
	 * @param lowerBound
	 *           lower bound (a is never lower than this value)
	 * @param upperBound
	 *           upper bound (b never is greater than this
	 *           value)
	 * @return a two element array holding {a, b}
	 * @throws ConvergenceException
	 *            if a root can not be bracketted
	 * @throws FunctionEvaluationException
	 *            if an error occurs evaluating the
	 *            function
	 * @throws IllegalArgumentException
	 *            if function is null, maximumIterations
	 *            is not positive, or initial is not between lowerBound and upperBound
	 */
	public static double[] bracket(UnivariateRealFunction function,
						double initial, double lowerBound, double upperBound)
						throws ConvergenceException, FunctionEvaluationException {
		return bracket(function, initial, lowerBound, upperBound,
							Integer.MAX_VALUE);
	}

	/**
	 * This method attempts to find two values a and b satisfying
	 * <ul>
	 * <li> <code> lowerBound <= a < initial < b <= upperBound</code></li>
	 * <li> <code> f(a) * f(b) < 0 </code></li>
	 * </ul>
	 * If f is continuous on <code>[a,b],</code> this means that <code>a</code> and <code>b</code> bracket a root of f.
	 * <p>
	 * The algorithm starts by setting <code>a := initial -1; b := initial +1,</code> examines the value of the function at <code>a</code> and <code>b</code> and
	 * keeps moving the endpoints out by one unit each time through a loop that terminates when one of the following happens:
	 * <ul>
	 * <li> <code> f(a) * f(b) < 0 </code> -- success!</li>
	 * <li> <code> a = lower </code> and <code> b = upper</code> -- ConvergenceException</li>
	 * <li> <code> maximumIterations</code> iterations elapse -- ConvergenceException</li>
	 * </ul>
	 * 
	 * @param function
	 *           the function
	 * @param initial
	 *           initial midpoint of interval being expanded to
	 *           bracket a root
	 * @param lowerBound
	 *           lower bound (a is never lower than this value)
	 * @param upperBound
	 *           upper bound (b never is greater than this
	 *           value)
	 * @param maximumIterations
	 *           maximum number of iterations to perform
	 * @return a two element array holding {a, b}.
	 * @throws ConvergenceException
	 *            if the algorithm fails to find a and b
	 *            satisfying the desired conditions
	 * @throws FunctionEvaluationException
	 *            if an error occurs evaluating the
	 *            function
	 * @throws IllegalArgumentException
	 *            if function is null, maximumIterations
	 *            is not positive, or initial is not between lowerBound and upperBound
	 */
	public static double[] bracket(UnivariateRealFunction function,
						double initial, double lowerBound, double upperBound,
						int maximumIterations) throws ConvergenceException,
						FunctionEvaluationException {

		if (function == null) {
			throw new IllegalArgumentException("function is null.");
		}
		if (maximumIterations <= 0) {
			throw new IllegalArgumentException("bad value for maximumIterations: " + maximumIterations);
		}
		if (initial < lowerBound || initial > upperBound || lowerBound >= upperBound) {
			throw new IllegalArgumentException("Invalid endpoint parameters:  lowerBound=" + lowerBound +
								" initial=" + initial + " upperBound=" + upperBound);
		}
		double a = initial;
		double b = initial;
		double fa;
		double fb;
		int numIterations = 0;

		do {
			a = Math.max(a - 1.0, lowerBound);
			b = Math.min(b + 1.0, upperBound);
			fa = function.value(a);

			fb = function.value(b);
			numIterations++;
		} while ((fa * fb > 0.0) && (numIterations < maximumIterations) &&
							((a > lowerBound) || (b < upperBound)));

		if (fa * fb >= 0.0) {
			throw new ConvergenceException("Number of iterations= " + numIterations +
								" maximum iterations= " + maximumIterations +
								" initial= " + initial + " lowerBound=" + lowerBound +
								" upperBound=" + upperBound + " final a value=" + a +
								" final b value=" + b + " f(a)=" + fa + " f(b)=" + fb);
		}

		return new double[] { a, b };
	}

	/**
	 * Compute the midpoint of two values.
	 * 
	 * @param a
	 *           first value.
	 * @param b
	 *           second value.
	 * @return the midpoint.
	 */
	public static double midpoint(double a, double b) {
		return (a + b) * .5;
	}

	/**
	 * Checks to see if f is null, throwing IllegalArgumentException if so.
	 * Also initializes factory if factory is null.
	 * 
	 * @param f
	 *           input function
	 * @throws IllegalArgumentException
	 *            if f is null
	 */
	private static void setup(UnivariateRealFunction f) {

		if (f == null) {
			throw new IllegalArgumentException("function can not be null.");
		}

		if (factory == null) {
			factory = UnivariateRealSolverFactory.newInstance();
		}
	}
}

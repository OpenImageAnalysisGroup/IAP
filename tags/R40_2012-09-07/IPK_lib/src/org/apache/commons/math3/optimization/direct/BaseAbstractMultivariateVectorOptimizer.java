/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math3.optimization.direct;

import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optimization.BaseMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.optimization.SimpleVectorValueChecker;

/**
 * Base class for implementing optimizers for multivariate scalar functions.
 * This base class handles the boiler-plate methods associated to thresholds
 * settings, iterations and evaluations counting.
 *
 * @param <FUNC> the type of the objective function to be optimized
 *
 * @version $Id: BaseAbstractMultivariateVectorOptimizer.java,v 1.1 2012-05-22 20:42:17 klukas Exp $
 * @since 3.0
 */
public abstract class BaseAbstractMultivariateVectorOptimizer<FUNC extends MultivariateVectorFunction>
    implements BaseMultivariateVectorOptimizer<FUNC> {
    /** Evaluations counter. */
    protected final Incrementor evaluations = new Incrementor();
    /** Convergence checker. */
    private ConvergenceChecker<PointVectorValuePair> checker;
    /** Target value for the objective functions at optimum. */
    private double[] target;
    /** Weight for the least squares cost computation. */
    private double[] weight;
    /** Initial guess. */
    private double[] start;
    /** Objective function. */
    private MultivariateVectorFunction function;

    /**
     * Simple constructor with default settings.
     * The convergence check is set to a {@link SimpleVectorValueChecker} and
     * the allowed number of evaluations is set to {@link Integer#MAX_VALUE}.
     */
    protected BaseAbstractMultivariateVectorOptimizer() {
        this(new SimpleVectorValueChecker());
    }
    /**
     * @param checker Convergence checker.
     */
    protected BaseAbstractMultivariateVectorOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
        this.checker = checker;
    }

    /** {@inheritDoc} */
    public int getMaxEvaluations() {
        return evaluations.getMaximalCount();
    }

    /** {@inheritDoc} */
    public int getEvaluations() {
        return evaluations.getCount();
    }

    /** {@inheritDoc} */
    public ConvergenceChecker<PointVectorValuePair> getConvergenceChecker() {
        return checker;
    }

    /**
     * Compute the objective function value.
     *
     * @param point Point at which the objective function must be evaluated.
     * @return the objective function value at the specified point.
     * @throws TooManyEvaluationsException if the maximal number of evaluations is
     * exceeded.
     */
    protected double[] computeObjectiveValue(double[] point) {
        try {
            evaluations.incrementCount();
        } catch (MaxCountExceededException e) {
            throw new TooManyEvaluationsException(e.getMax());
        }
        return function.value(point);
    }

    /** {@inheritDoc} */
    public PointVectorValuePair optimize(int maxEval, FUNC f, double[] t, double[] w,
                                            double[] startPoint) {
        // Checks.
        if (f == null) {
            throw new NullArgumentException();
        }
        if (t == null) {
            throw new NullArgumentException();
        }
        if (w == null) {
            throw new NullArgumentException();
        }
        if (startPoint == null) {
            throw new NullArgumentException();
        }
        if (t.length != w.length) {
            throw new DimensionMismatchException(t.length, w.length);
        }

        // Reset.
        evaluations.setMaximalCount(maxEval);
        evaluations.resetCount();

        // Store optimization problem characteristics.
        function = f;
        target = t.clone();
        weight = w.clone();
        start = startPoint.clone();

        // Perform computation.
        return doOptimize();
    }

    /**
     * @return the initial guess.
     */
    public double[] getStartPoint() {
        return start.clone();
    }

    /**
     * Perform the bulk of the optimization algorithm.
     *
     * @return the point/value pair giving the optimal value for the
     * objective function.
     */
    protected abstract PointVectorValuePair doOptimize();

    /**
     * @return a reference to the {@link #target array}.
     */
    protected double[] getTargetRef() {
        return target;
    }
    /**
     * @return a reference to the {@link #weight array}.
     */
    protected double[] getWeightRef() {
        return weight;
    }
}

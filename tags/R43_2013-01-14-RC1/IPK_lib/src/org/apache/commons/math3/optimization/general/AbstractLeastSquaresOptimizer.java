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

package org.apache.commons.math3.optimization.general;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.analysis.DifferentiableMultivariateVectorFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.DifferentiableMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.optimization.direct.BaseAbstractMultivariateVectorOptimizer;
import org.apache.commons.math3.util.FastMath;

/**
 * Base class for implementing least squares optimizers.
 * It handles the boilerplate methods associated to thresholds settings,
 * jacobian and error estimation.
 * <br/>
 * This class uses the {@link DifferentiableMultivariateVectorFunction#jacobian()}
 * of the function argument in method
 * {@link #optimize(int,DifferentiableMultivariateVectorFunction,double[],double[],double[])
 * optimize} and assumes that, in the matrix returned by the
 * {@link MultivariateMatrixFunction#value(double[]) value} method, the rows
 * iterate on the model functions while the columns iterate on the parameters; thus,
 * the numbers of rows is equal to the length of the {@code target} array while the
 * number of columns is equal to the length of the {@code startPoint} array.
 *
 * @version $Id: AbstractLeastSquaresOptimizer.java,v 1.1 2012-05-22 20:42:49 klukas Exp $
 * @since 1.2
 */
public abstract class AbstractLeastSquaresOptimizer
    extends BaseAbstractMultivariateVectorOptimizer<DifferentiableMultivariateVectorFunction>
    implements DifferentiableMultivariateVectorOptimizer {
    /** Singularity threshold (cf. {@link #getCovariances(double)}). */
    private static final double DEFAULT_SINGULARITY_THRESHOLD = 1e-14;
    /**
     * Jacobian matrix of the weighted residuals.
     * This matrix is in canonical form just after the calls to
     * {@link #updateJacobian()}, but may be modified by the solver
     * in the derived class (the {@link LevenbergMarquardtOptimizer
     * Levenberg-Marquardt optimizer} does this).
     */
    protected double[][] weightedResidualJacobian;
    /** Number of columns of the jacobian matrix. */
    protected int cols;
    /** Number of rows of the jacobian matrix. */
    protected int rows;
    /** Current point. */
    protected double[] point;
    /** Current objective function value. */
    protected double[] objective;
    /** Weighted residuals */
    protected double[] weightedResiduals;
    /** Cost value (square root of the sum of the residuals). */
    protected double cost;
    /** Objective function derivatives. */
    private MultivariateMatrixFunction jF;
    /** Number of evaluations of the Jacobian. */
    private int jacobianEvaluations;

    /**
     * Simple constructor with default settings.
     * The convergence check is set to a {@link
     * org.apache.commons.math3.optimization.SimpleVectorValueChecker}.
     */
    protected AbstractLeastSquaresOptimizer() {}
    /**
     * @param checker Convergence checker.
     */
    protected AbstractLeastSquaresOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
        super(checker);
    }

    /**
     * @return the number of evaluations of the Jacobian function.
     */
    public int getJacobianEvaluations() {
        return jacobianEvaluations;
    }

    /**
     * Update the jacobian matrix.
     *
     * @throws DimensionMismatchException if the Jacobian dimension does not
     * match problem dimension.
     */
    protected void updateJacobian() {
        ++jacobianEvaluations;
        weightedResidualJacobian = jF.value(point);
        if (weightedResidualJacobian.length != rows) {
            throw new DimensionMismatchException(weightedResidualJacobian.length, rows);
        }

        final double[] residualsWeights = getWeightRef();

        for (int i = 0; i < rows; i++) {
            final double[] ji = weightedResidualJacobian[i];
            double wi = FastMath.sqrt(residualsWeights[i]);
            for (int j = 0; j < cols; ++j) {
                //ji[j] *=  -1.0;
                weightedResidualJacobian[i][j] = -ji[j]*wi;
            }
        }
    }

    /**
     * Update the residuals array and cost function value.
     * @throws DimensionMismatchException if the dimension does not match the
     * problem dimension.
     * @throws org.apache.commons.math3.exception.TooManyEvaluationsException
     * if the maximal number of evaluations is exceeded.
     */
    protected void updateResidualsAndCost() {
        objective = computeObjectiveValue(point);
        if (objective.length != rows) {
            throw new DimensionMismatchException(objective.length, rows);
        }

        final double[] targetValues = getTargetRef();
        final double[] residualsWeights = getWeightRef();

        cost = 0;
        for (int i = 0; i < rows; i++) {
            final double residual = targetValues[i] - objective[i];
            weightedResiduals[i]= residual*FastMath.sqrt(residualsWeights[i]);
            cost += residualsWeights[i] * residual * residual;
        }
        cost = FastMath.sqrt(cost);
    }

    /**
     * Get the Root Mean Square value.
     * Get the Root Mean Square value, i.e. the root of the arithmetic
     * mean of the square of all weighted residuals. This is related to the
     * criterion that is minimized by the optimizer as follows: if
     * <em>c</em> if the criterion, and <em>n</em> is the number of
     * measurements, then the RMS is <em>sqrt (c/n)</em>.
     *
     * @return RMS value
     */
    public double getRMS() {
        return FastMath.sqrt(getChiSquare() / rows);
    }

    /**
     * Get a Chi-Square-like value assuming the N residuals follow N
     * distinct normal distributions centered on 0 and whose variances are
     * the reciprocal of the weights.
     * @return chi-square value
     */
    public double getChiSquare() {
        return cost * cost;
    }

    /**
     * Get the covariance matrix of the optimized parameters.
     *
     * @return the covariance matrix.
     * @throws org.apache.commons.math3.linear.SingularMatrixException
     * if the covariance matrix cannot be computed (singular problem).
     *
     * @see #getCovariances(double)
     */
    public double[][] getCovariances() {
        return getCovariances(DEFAULT_SINGULARITY_THRESHOLD);
    }

    /**
     * Get the covariance matrix of the optimized parameters.
     * <br/>
     * Note that this operation involves the inversion of the
     * <code>J<sup>T</sup>J</code> matrix, where {@code J} is the
     * Jacobian matrix.
     * The {@code threshold} parameter is a way for the caller to specify
     * that the result of this computation should be considered meaningless,
     * and thus trigger an exception.
     *
     * @param threshold Singularity threshold.
     * @return the covariance matrix.
     * @throws org.apache.commons.math3.linear.SingularMatrixException
     * if the covariance matrix cannot be computed (singular problem).
     */
    public double[][] getCovariances(double threshold) {
        // Set up the jacobian.
        updateJacobian();

        // Compute transpose(J)J, without building intermediate matrices.
        double[][] jTj = new double[cols][cols];
        for (int i = 0; i < cols; ++i) {
            for (int j = i; j < cols; ++j) {
                double sum = 0;
                for (int k = 0; k < rows; ++k) {
                    sum += weightedResidualJacobian[k][i] * weightedResidualJacobian[k][j];
                }
                jTj[i][j] = sum;
                jTj[j][i] = sum;
            }
        }

        // Compute the covariances matrix.
        final DecompositionSolver solver
            = new QRDecomposition(MatrixUtils.createRealMatrix(jTj), threshold).getSolver();
        return solver.getInverse().getData();
    }

    /**
     * Guess the errors in optimized parameters.
     * Guessing is covariance-based: It only gives a rough order of magnitude.
     *
     * @return errors in optimized parameters
     * @throws org.apache.commons.math3.linear.SingularMatrixException
     * if the covariances matrix cannot be computed.
     * @throws NumberIsTooSmallException if the number of degrees of freedom is not
     * positive, i.e. the number of measurements is less or equal to the number of
     * parameters.
     */
    public double[] guessParametersErrors() {
        if (rows <= cols) {
            throw new NumberIsTooSmallException(LocalizedFormats.NO_DEGREES_OF_FREEDOM,
                                                rows, cols, false);
        }
        double[] errors = new double[cols];
        final double c = FastMath.sqrt(getChiSquare() / (rows - cols));
        double[][] covar = getCovariances();
        for (int i = 0; i < errors.length; ++i) {
            errors[i] = FastMath.sqrt(covar[i][i]) * c;
        }
        return errors;
    }

    /** {@inheritDoc} */
    @Override
    public PointVectorValuePair optimize(int maxEval,
                                            final DifferentiableMultivariateVectorFunction f,
                                            final double[] target, final double[] weights,
                                            final double[] startPoint) {
        // Reset counter.
        jacobianEvaluations = 0;

        // Store least squares problem characteristics.
        jF = f.jacobian();

        // Arrays shared with the other private methods.
        point = startPoint.clone();
        rows = target.length;
        cols = point.length;

        weightedResidualJacobian = new double[rows][cols];
        this.weightedResiduals = new double[rows];

        cost = Double.POSITIVE_INFINITY;

        return super.optimize(maxEval, f, target, weights, startPoint);
    }
}

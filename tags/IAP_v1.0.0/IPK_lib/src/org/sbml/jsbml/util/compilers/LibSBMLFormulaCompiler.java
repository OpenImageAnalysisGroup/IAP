package org.sbml.jsbml.util.compilers;

import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.SBMLException;

public class LibSBMLFormulaCompiler extends FormulaCompiler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccos(org.sbml.jsbml.ASTNode
	 * )
	 */
	@Override
	public ASTNodeValue arccos(ASTNode node) throws SBMLException {
		return function("acos", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccosh(org.sbml.jsbml.
	 * ASTNode)
	 */
	@Override
	public ASTNodeValue arccosh(ASTNode node) throws SBMLException {
		return function("arccosh", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccot(org.sbml.jsbml.ASTNode
	 * )
	 */
	@Override
	public ASTNodeValue arccot(ASTNode node) throws SBMLException {
		return function("arccot", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccoth(org.sbml.jsbml.
	 * ASTNode)
	 */
	@Override
	public ASTNodeValue arccoth(ASTNode node) throws SBMLException {
		return function("arccoth", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccsc(org.sbml.jsbml.ASTNode
	 * )
	 */
	public ASTNodeValue arccsc(ASTNode node) throws SBMLException {
		return function("arccsc", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arccsch(org.sbml.jsbml.
	 * ASTNode)
	 */
	@Override
	public ASTNodeValue arccsch(ASTNode node) throws SBMLException {
		return function("arccsch", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsec(org.sbml.jsbml.ASTNode
	 * )
	 */
	@Override
	public ASTNodeValue arcsec(ASTNode node) throws SBMLException {
		return function("arcsec", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsech(org.sbml.jsbml.
	 * ASTNode)
	 */
	@Override
	public ASTNodeValue arcsech(ASTNode node) throws SBMLException {
		return function("arcsech", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsin(org.sbml.jsbml.ASTNode
	 * )
	 */
	@Override
	public ASTNodeValue arcsin(ASTNode node) throws SBMLException {
		return function("asin", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arcsinh(org.sbml.jsbml.
	 * ASTNode)
	 */
	@Override
	public ASTNodeValue arcsinh(ASTNode node) throws SBMLException {
		return function("arcsinh", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arctan(org.sbml.jsbml.ASTNode
	 * )
	 */
	@Override
	public ASTNodeValue arctan(ASTNode node) throws SBMLException {
		return function("atan", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#arctanh(org.sbml.jsbml.
	 * ASTNode)
	 */
	@Override
	public ASTNodeValue arctanh(ASTNode node) throws SBMLException {
		return function("arctanh", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#and(java.util.List)
	 */
	@Override
	public ASTNodeValue and(List<ASTNode> nodes) throws SBMLException {
		return function("and", nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#or(java.util.List)
	 */
	@Override
	public ASTNodeValue or(List<ASTNode> nodes) throws SBMLException {
		return function("or", nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#xor(java.util.List)
	 */
	@Override
	public ASTNodeValue xor(List<ASTNode> nodes) throws SBMLException {
		return function("xor", nodes);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#eq(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue eq(ASTNode left, ASTNode right) throws SBMLException {
		return function("eq", left, right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#neq(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue neq(ASTNode left, ASTNode right) throws SBMLException {
		return function("neq", left, right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#geq(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue geq(ASTNode left, ASTNode right) throws SBMLException {
		return function("geq", left, right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#eq(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue gt(ASTNode left, ASTNode right) throws SBMLException {
		return function("gt", left, right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#eq(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue leq(ASTNode left, ASTNode right) throws SBMLException {
		return function("leq", left, right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#eq(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue lt(ASTNode left, ASTNode right) throws SBMLException {
		return function("lt", left, right);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getNegativeInfinity()
	 */
	@Override
	public ASTNodeValue getNegativeInfinity() {
		return new ASTNodeValue("-INF", this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getPositiveInfinity()
	 */
	@Override
	public ASTNodeValue getPositiveInfinity() {
		return new ASTNodeValue("INF", this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#pow(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue pow(ASTNode left, ASTNode right) throws SBMLException {
		return function("pow", left, right);		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue ln(ASTNode node) throws SBMLException {
		return function("log", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue log(ASTNode node) throws SBMLException {
		return function("log10", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue log(ASTNode left, ASTNode right) throws SBMLException {
		if (left.getReal() == 10) {
			return function("log10", right);
		} else {
			return function("log", left, right);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#getConstantE()
	 */
	@Override
	public ASTNodeValue getConstantE() {
		return new ASTNodeValue("exponentiale", this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#factorial(org.sbml.jsbml
	 * .ASTNode)
	 */
	@Override
	public ASTNodeValue factorial(ASTNode node) {
		try {
			return function("factorial", node);
		} catch (SBMLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#root(org.sbml.jsbml.ASTNode
	 * , org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue root(ASTNode rootExponent, ASTNode radiant)
			throws SBMLException 
	{
		return function("root", rootExponent, radiant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.util.compilers.ASTNodeCompiler#root(double,
	 * org.sbml.jsbml.ASTNode)
	 */
	@Override
	public ASTNodeValue root(double rootExponent, ASTNode radiant)
			throws SBMLException 	
	{
		return function("root", new ASTNode(rootExponent), radiant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.util.compilers.ASTNodeCompiler#sqrt(org.sbml.jsbml.ASTNode
	 * )
	 */
	@Override
	public ASTNodeValue sqrt(ASTNode node) throws SBMLException {
		return function("sqrt", node);
	}

	
}

/*
 * $Id: SpeciesReference.java,v 1.1 2012-11-07 14:43:34 klukas Exp $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/core/src/org/sbml/jsbml/SpeciesReference.java $
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2012 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */

package org.sbml.jsbml;

import java.util.Locale;
import java.util.Map;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.TreeNodeChangeEvent;

/**
 * Represents the speciesReference XML element of a SBML file.
 * 
 * @author Andreas Dr&auml;ger
 * @author Marine Dumousseau
 * @author Nicolas Rodriguez
 * @since 0.8
 * @version $Rev: 1153 $
 */
public class SpeciesReference extends SimpleSpeciesReference implements
		Variable {

	/**
	 * Message to be displayed in case that an illegal stoichiometric value has been set.
	 */
	private static final String ILLEGAL_STOCHIOMETRY_VALUE = "Only positive integer values can be set as %s. Invalid value %d.";
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 4400834403773787677L;
	/**
	 * Represents the 'constant' XML attribute of this SpeciesReference.
	 */
	private Boolean constant;
	/**
	 * Represents the 'denominator' XML attribute of this SpeciesReference.
	 */
	private Integer denominator;
	/**
	 * 
	 */
	private boolean isSetConstant;
	/**
	 * Boolean value to know if the SpeciesReference denominator has been set.
	 */
	private boolean isSetDenominator;
	/**
	 * 
	 */
	private boolean isSetStoichiometry;
	/**
	 * Represents the 'stoichiometry' XML attribute of this SpeciesReference.
	 */
	private Double stoichiometry;
	/**
	 * Contains the MathML expression for the stoichiometry of this
	 * SpeciesReference.
	 * 
	 * @deprecated
	 */
	@Deprecated
	private StoichiometryMath stoichiometryMath;

	/**
	 * Creates a SpeciesReference instance. By default, if the level is superior
	 * or equal to 3, the constant, stoichiometryMath and stoichiometry are
	 * null.
	 * 
	 * @param spec
	 */
	public SpeciesReference() {
		super();
		initDefaults();
	}

	/**
	 * 
	 * @param level
	 * @param version
	 */
	public SpeciesReference(int level, int version) {
		this(null, level, version);
	}
	
	/**
	 * Creates a SpeciesReference instance from a Species. By default, if the
	 * level is superior or equal to 3, the constant, stoichiometryMath and
	 * stoichiometry are null.
	 * 
	 * @param speciesReference
	 */
	public SpeciesReference(Species species) {
		super(species);
		initDefaults();
	}

	/**
	 * Creates a SpeciesReference instance from a given SpeciesReference.
	 * 
	 * @param speciesReference
	 */
	@SuppressWarnings("deprecation")
	public SpeciesReference(SpeciesReference speciesReference) {
		super(speciesReference);
		if (speciesReference.isSetStoichiometryMath()) {
			setStoichiometryMath(speciesReference.getStoichiometryMath()
					.clone());
		}
		if (speciesReference.isSetStoichiometry()) {
			setStoichiometry(new Double(speciesReference.getStoichiometry()));
		} else {
			stoichiometry = speciesReference.stoichiometry == null ? null : new Double(speciesReference.stoichiometry);
		}
		if (speciesReference.isSetConstant()) {
			setConstant(new Boolean(speciesReference.isConstant()));
		} else {
			constant = speciesReference.constant == null ? null : new Boolean(speciesReference.constant);
		}
		if (speciesReference.isSetDenominator) {
			setDenominator(new Integer(speciesReference.getDenominator()));
		} else {
			denominator = speciesReference.denominator == null ? null : new Integer(speciesReference.denominator);
		}
	}

	/**
	 * 
	 * @param id
	 */
	public SpeciesReference(String id) {
		super(id);
		initDefaults();
	}

	/**
	 * 
	 * @param id
	 * @param level
	 * @param version
	 */
	public SpeciesReference(String id, int level, int version) {
		this(id, null, level, version);
	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param level
	 * @param version
	 */
	public SpeciesReference(String id, String name, int level, int version) {
		super(id, name, level, version);
		initDefaults();
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.element.SBase#clone()
	 */
	@Override
	public SpeciesReference clone() {
		return new SpeciesReference(this);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBaseWithDerivedUnit#containsUndeclaredUnits()
	 */
	public boolean containsUndeclaredUnits() {
		if (isSetStoichiometryMath()) {
			return getStoichiometryMath().containsUndeclaredUnits();
		}
		return isSetStoichiometry() ? false : true;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SimpleSpeciesReference#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		boolean equals = super.equals(object);
		if (equals) {
			SpeciesReference sr = (SpeciesReference) object;
			equals &= sr.isSetStoichiometry() == isSetStoichiometry();
			if (equals && isSetStoichiometry()) {
				equals &= sr.getStoichiometry() == getStoichiometry();
			}
			equals &= sr.isSetConstant() == isSetConstant();
			if (equals && isSetConstant()) {
				equals &= sr.isConstant() == isConstant();
			}
			equals &= sr.isSetDenominator() == isSetDenominator();
			if (equals && isSetDenominator()) {
				equals &= sr.getDenominator() == getDenominator();
			}
		}
		return equals;
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractSBase#getAllowsChildren()
	 */
	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	/**
	 * This method computes the fraction of the stoichiometry and the
	 * denominator. Actually, the denominator is only defined in SBML Level 1.
	 * For convenience, this method might be usefull.
	 * 
	 * @return The fraction between {@link #stoichiometry} and
	 *         {@link #denominator}.
	 */
	public double getCalculatedStoichiometry() {
		int denominator = getDenominator();
		return (denominator != 1) ? getStoichiometry() / denominator
				: getStoichiometry();
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractSBase#getChildAt(int)
	 */
	@Override
	public TreeNode getChildAt(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException(index + " < 0");
		}
		int count = super.getChildCount(), pos = 0;
		if (index < count) {
			return super.getChildAt(index);
		} else {
			index -= count;
		}
		if (isSetStoichiometryMath()) {
			if (pos == index) {
				return getStoichiometryMath();
			}
			pos++;
		}
		throw new IndexOutOfBoundsException(String.format("Index %d >= %d",
				index, +((int) Math.min(pos, 0))));
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractSBase#getChildCount()
	 */
	@Override
	public int getChildCount() {
		return super.getChildCount() + (isSetStoichiometryMath() ? 1 : 0);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.State#getConstant()
	 */
	public boolean getConstant() {
		return isConstant();
	}

	/**
	 * 
	 * @return the denominator value if it is set, 1 otherwise
	 * @deprecated Use for Level 1 only.
	 */
	@Deprecated
	public int getDenominator() {
		return isSetDenominator ? denominator.intValue() : 1;
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.Quantity#getDerivedUnitInstance()
	 */
	public UnitDefinition getDerivedUnitDefinition() {
		if (isSetStoichiometryMath()) {
			return stoichiometryMath.getDerivedUnitDefinition();
		}
		UnitDefinition ud = new UnitDefinition(getLevel(), getVersion());
		ud.addUnit(Unit.Kind.DIMENSIONLESS);
		return ud;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.Quantity#getDerivedUnit()
	 */
	public String getDerivedUnits() {
		if (isSetStoichiometryMath()) {
			return stoichiometryMath.getDerivedUnits();
		}
		return Unit.Kind.DIMENSIONLESS.toString();
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractSBase#getElementName()
	 */
	@Override
	public String getElementName() {
		if ((getLevel() == 1) && (getVersion() == 1)) {
			return "specieReference";
		}
		return super.getElementName();
	}

	/**
	 * 
	 * @return the stoichiometry value of this {@link SpeciesReference} if it is
	 *         set, otherwise, depending on the Level attribute, 1 for Level < 3
	 *         or {@link Double#NaN}.
	 */
	public double getStoichiometry() {
		if (isSetStoichiometry()) {
			return stoichiometry.doubleValue();
		}
		return (getLevel() < 3) ? 1d : Double.NaN;
	}

	/**
	 * 
	 * @return the stoichiometryMath of this SpeciesReference. Can be null if
	 *         the stoichiometryMath is not set.
	 * @deprecated since SBML Level 3 this should not be used anymore.
	 */
	@Deprecated
	public StoichiometryMath getStoichiometryMath() {
		return stoichiometryMath;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.Quantity#getValue()
	 */
	public double getValue() {
		return getStoichiometry();
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SimpleSpeciesReference#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 937;
		int hashCode = super.hashCode();
		if (isSetStoichiometry()) {
			hashCode += prime * stoichiometry.hashCode();
		}
		if (isSetConstant()) {
			hashCode += prime * constant.hashCode();
		}
		if (isSetDenominator()) {
			hashCode += prime * denominator.hashCode();
		}
		return hashCode;
	}

	/**
	 * Initializes the default values using the current Level/Version configuration.
	 */
	public void initDefaults() {
		initDefaults(getLevel(), getVersion());
	}
	
	/**
	 * Initializes the default values of this SpeciesReference.
	 */
	public void initDefaults(int level, int version) {
		// See
		// http://sbml.org/Community/Wiki/SBML_Level_3_Core/Reaction_changes/Changes_to_stoichiometry
		if (level <= 2) {
			constant = Boolean.valueOf(true);
			stoichiometry = Double.valueOf(1d);
			denominator = Integer.valueOf(1);
		} else {
			isSetConstant = false;
			isSetDenominator = false;
			isSetStoichiometry = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.State#isConstant()
	 */
	public boolean isConstant() {
		return constant != null ? constant.booleanValue() : false;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.State#isSetConstant()
	 */
	public boolean isSetConstant() {
		return isSetConstant;
	}

	/**
	 * 
	 * @return true if the denominator is not null.
	 */
	public boolean isSetDenominator() {
		return denominator != null;
	}

	/**
	 * 
	 * @return true if the stoichiometry of this SpeciesReference is not null.
	 */
	public boolean isSetStoichiometry() {
		return isSetStoichiometry;
	}

	/**
	 * 
	 * @return true if the stoichiometryMath of this SpeciesReference is not
	 *         null.
	 */
	public boolean isSetStoichiometryMath() {
		return stoichiometryMath != null;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.Quantity#isSetValue()
	 */
	public boolean isSetValue() {
		return isSetStoichiometry();
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.element.SBase#readAttribute(String attributeName, String prefix, String value)
	 */
	@Override
	public boolean readAttribute(String attributeName, String prefix,
			String value) {
		boolean isAttributeRead = super.readAttribute(attributeName, prefix,
				value);

		if (!isAttributeRead) {
			isAttributeRead = true;
			
			if (attributeName.equals("stoichiometry")) {
				setStoichiometry(StringTools.parseSBMLDouble(value));
			} else if (attributeName.equals("constant")) {
				setConstant(StringTools.parseSBMLBoolean(value));
			} else if (attributeName.equals("denominator")) {
				setDenominator(StringTools.parseSBMLInt(value));
			} else {
				isAttributeRead = false;
			}
		}
		return isAttributeRead;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.State#setConstant(boolean)
	 */
	public void setConstant(boolean constant) {
		if (getLevel() < 3) {
			throw new PropertyNotAvailableException(TreeNodeChangeEvent.constant,
					this);
		}
		Boolean oldConstant = this.constant;
		this.constant = Boolean.valueOf(constant);
		isSetConstant = true;
		firePropertyChange(TreeNodeChangeEvent.constant, oldConstant,
				this.constant);
	}

	/**
	 * Sets the denominator of this {@link SpeciesReference}.
	 * 
	 * @param denominator
	 * @deprecated
	 */
	@Deprecated
	public void setDenominator(int denominator) {
		if ((getLevel() == 1) && (getVersion() == 2)) {
			if (denominator < 0) {
				throw new IllegalArgumentException(String.format(
										ILLEGAL_STOCHIOMETRY_VALUE,
										"denominator", stoichiometry));
			}
		}
		Integer oldDenominator = this.denominator;
		this.denominator = denominator;
		isSetDenominator = true;
		firePropertyChange(TreeNodeChangeEvent.denominator, oldDenominator, this.denominator);
	}

	/**
	 * Sets the stoichiometry of this {@link SpeciesReference}.
	 * 
	 * @param stoichiometry
	 */
	public void setStoichiometry(double stoichiometry) {
		if ((getLevel() == 1) && (getVersion() == 2)) {
			int stoch = (int) stoichiometry;
			if ((stoch < 0) || (stoch - stoichiometry != 0d)) {
				throw new IllegalArgumentException(String.format(
						ILLEGAL_STOCHIOMETRY_VALUE, "stoichiometry",
						stoichiometry));
			}
		}
		Double oldStoichiometry = this.stoichiometry;
		this.stoichiometry = Double.valueOf(stoichiometry);
		if (isSetStoichiometryMath()) {
			stoichiometryMath = null;
		}
		if (Double.isNaN(stoichiometry)) {
			isSetStoichiometry = false;
		} else {
			isSetStoichiometry = true;
		}
		firePropertyChange(TreeNodeChangeEvent.stoichiometry, oldStoichiometry,
				this.stoichiometry);
	}

	/**
	 * Sets the {@link StoichiometryMath} of this {@link SpeciesReference).
	 * 
	 * @param math
	 * @deprecated
	 */
	@Deprecated
	public void setStoichiometryMath(StoichiometryMath math) {
		unsetStoichiometryMath();
		this.stoichiometryMath = math;
		setThisAsParentSBMLObject(this.stoichiometryMath);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.Quantity#setValue(double)
	 */
	public void setValue(double value) {
		setStoichiometry(value);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.Variable#unsetConstant()
	 */
	public void unsetConstant() {
		if(this.constant != null){
			Boolean oldConstant = this.constant;
			this.constant = null;
			isSetConstant = false;
			firePropertyChange(TreeNodeChangeEvent.constant, oldConstant,
					this.constant);
		}
	}

	/**
	 * Unsets the stoichiometry property of this element.
	 */
	public void unsetStoichiometry() {
		if(this.stoichiometry != null){
			Double oldStoichiometry = this.stoichiometry;
			this.stoichiometry = null;
			isSetStoichiometry = false;
			firePropertyChange(TreeNodeChangeEvent.stoichiometry, oldStoichiometry,
					this.stoichiometry);
		}
	}

	/**
	 * 
	 * @return
	 * @deprecated
	 */
	@Deprecated
	public boolean unsetStoichiometryMath() {
		if (this.stoichiometryMath != null) {
			StoichiometryMath oldStoichiometryMath = this.stoichiometryMath;
			this.stoichiometryMath = null;
			oldStoichiometryMath.fireNodeRemovedEvent();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.Quantity#unsetValue()
	 */
	public void unsetValue() {
		unsetStoichiometry();
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.element.SBase#writeXMLAttributes()
	 */
	@Override
	public Map<String, String> writeXMLAttributes() {
	  Map<String, String> attributes = super.writeXMLAttributes();

		if (isSetStoichiometry()) {
			attributes.put("stoichiometry", StringTools.toString(
					Locale.ENGLISH, getStoichiometry()));
		}
		if (isSetConstant()) {
			attributes.put("constant", Boolean.toString(isConstant()));
		}
		if (isSetDenominator() && (getLevel() == 1)) {
			int denominator = getDenominator();
			if (denominator != 1) {
				attributes.put("denominator", Integer.toString(denominator));
			}
		}

		return attributes;
	}

}

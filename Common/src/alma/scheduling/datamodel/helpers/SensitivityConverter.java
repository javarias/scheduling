/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
/**
 * 
 */
package alma.scheduling.datamodel.helpers;

import alma.entity.xmlbinding.valuetypes.DoubleWithUnitT;
import alma.entity.xmlbinding.valuetypes.SensitivityT;
import alma.entity.xmlbinding.valuetypes.types.SensitivityTUnitType;

/**
 * @author dclarke
 *
 */
public class SensitivityConverter {

	final static double MilliJyToJy = 1.0 / 1000.0;
	
	/*
	 * ================================================================
	 * Public interface
	 * ================================================================
	 */
	static public double convertedValue(SensitivityT sensitivity, SensitivityTUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(sensitivity, unit);
	}
	/* End Public interface
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Internal superclass interface
	 * ================================================================
	 */
	static private double convertedGenericValue(DoubleWithUnitT sensitivity, SensitivityTUnitType unit) 
		throws ConversionException {
		double result;

		if (sensitivity.getUnit().equals(unit.toString())) {
			result = sensitivity.getContent();
		} else {
			final double standard = valueConvertedToStandardUnits(sensitivity);
			result = valueInStandardUnitsConvertedTo(standard, unit);
		}

		return result;
	}
	/* End Internal superclass interface
	 * ============================================================= */



	/*
	 * ================================================================
	 * Actual conversions
	 * ================================================================
	 */
	static private double valueConvertedToStandardUnits(DoubleWithUnitT sensitivity) 
			throws ConversionException {
		final SensitivityTUnitType unit;
		try {
			unit = SensitivityTUnitType.valueOf(sensitivity.getUnit());
		} catch (IllegalArgumentException e) {
			throw new ConversionException(String.format(
					"Unrecognised input units for sensitivity: %s",
					sensitivity.getUnit()));
		}

		double result = sensitivity.getContent();

		switch (unit.getType()) {
			case SensitivityTUnitType.MJY_TYPE:
				result *= MilliJyToJy;
				break;
			case SensitivityTUnitType.JY_TYPE:
				// no-op, Jy are the standard
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for sensitivity: %s",
						unit));
		}
		
		return result;
	}
	
	static private double valueInStandardUnitsConvertedTo(double standard, SensitivityTUnitType unit) 
			throws ConversionException {
		double result = standard;

		switch (unit.getType()) {
			case SensitivityTUnitType.MJY_TYPE:
				result /= MilliJyToJy;
				break;
			case SensitivityTUnitType.JY_TYPE:
				// no-op, Jy are the standard
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for sensitivity: %s",
						unit));
		}

		return result;
	}
	/* End Actual conversions
	 * ============================================================= */
}

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
import alma.entity.xmlbinding.valuetypes.FrequencyT;
import alma.entity.xmlbinding.valuetypes.types.FrequencyTUnitType;

/**
 * @author dclarke
 *
 */
public class FrequencyConverter {

	final static double KHZtoHZ = 1000.0;
	final static double MHZtoHZ = 1000.0 * 1000.0;
	final static double GHZtoHZ = 1000.0 * 1000.0 * 1000.0;
	
	/*
	 * ================================================================
	 * Public interface
	 * ================================================================
	 */
	static public double convertedValue(FrequencyT frequency, FrequencyTUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(frequency, unit);
	}
	/* End Public interface
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Internal superclass interface
	 * ================================================================
	 */
	static private double convertedGenericValue(DoubleWithUnitT frequency, FrequencyTUnitType unit) 
		throws ConversionException {
		double result;

		if (frequency.getUnit().equals(unit.toString())) {
			result = frequency.getContent();
		} else {
			final double standard = valueConvertedToStandardUnits(frequency);
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
	static private double valueConvertedToStandardUnits(DoubleWithUnitT frequency) 
			throws ConversionException {
		final FrequencyTUnitType unit;
		try {
			unit = FrequencyTUnitType.valueOf(frequency.getUnit());
		} catch (IllegalArgumentException e) {
			throw new ConversionException(String.format(
					"Unrecognised input units for frequency: %s",
					frequency.getUnit()));
		}

		double result = frequency.getContent();

		switch (unit.getType()) {
			case FrequencyTUnitType.HZ_TYPE:
				// no-op, Hz are the standard
				break;
			case FrequencyTUnitType.KHZ_TYPE:
				result *= KHZtoHZ;
				break;
			case FrequencyTUnitType.MHZ_TYPE:
				result *= MHZtoHZ;
				break;
			case FrequencyTUnitType.GHZ_TYPE:
				result *= GHZtoHZ;
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for frequency: %s",
						unit));
		}
		
		return result;
	}
	
	static private double valueInStandardUnitsConvertedTo(double standard, FrequencyTUnitType unit) 
			throws ConversionException {
		double result = standard;

		switch (unit.getType()) {
			case FrequencyTUnitType.HZ_TYPE:
				// no-op, Hz are the standard
				break;
			case FrequencyTUnitType.KHZ_TYPE:
				result /= KHZtoHZ;
				break;
			case FrequencyTUnitType.MHZ_TYPE:
				result /= MHZtoHZ;
				break;
			case FrequencyTUnitType.GHZ_TYPE:
				result /= GHZtoHZ;
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for frequency: %s",
						unit));
		}

		return result;
	}
	/* End Actual conversions
	 * ============================================================= */

}

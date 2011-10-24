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
import alma.entity.xmlbinding.valuetypes.TimeT;
import alma.entity.xmlbinding.valuetypes.types.TimeTUnitType;

/**
 * @author dclarke
 *
 */
public class TimeConverter {

	final static double NanoSToS  = 1.0e-9;
	final static double MicroSToS = 1.0e-6;
	final static double MilliSToS = 1.0e-3;
	final static double MinToS    =   60.0;
	final static double HToS      = 3600.0;
	final static double DToS      = HToS * 24;
	
	/*
	 * ================================================================
	 * Public interface
	 * ================================================================
	 */
	static public double convertedValue(TimeT time, TimeTUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(time, unit);
	}
	/* End Public interface
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Internal superclass interface
	 * ================================================================
	 */
	static private double convertedGenericValue(DoubleWithUnitT time, TimeTUnitType unit) 
		throws ConversionException {
		double result;

		if (time.getUnit().equals(unit.toString())) {
			result = time.getContent();
		} else {
			final double standard = valueConvertedToStandardUnits(time);
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
	static private double valueConvertedToStandardUnits(DoubleWithUnitT time) 
			throws ConversionException {
		final TimeTUnitType unit;
		try {
			unit = TimeTUnitType.valueOf(time.getUnit());
		} catch (IllegalArgumentException e) {
			throw new ConversionException(String.format(
					"Unrecognised input units for time: %s",
					time.getUnit()));
		}

		double result = time.getContent();

		switch (unit.getType()) {
			case TimeTUnitType.NS_TYPE:
				result *= NanoSToS;
				break;
			case TimeTUnitType.US_TYPE:
				result *= MicroSToS;
				break;
			case TimeTUnitType.MS_TYPE:
				result *= MilliSToS;
				break;
			case TimeTUnitType.S_TYPE:
				// no-op, Seconds are the standard
				break;
			case TimeTUnitType.MIN_TYPE:
				result *= MinToS;
				break;
			case TimeTUnitType.H_TYPE:
				result *= HToS;
				break;
			case TimeTUnitType.D_TYPE:
				result *= DToS;
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for time: %s",
						unit));
		}
		
		return result;
	}
	
	static private double valueInStandardUnitsConvertedTo(double standard, TimeTUnitType unit) 
			throws ConversionException {
		double result = standard;

		switch (unit.getType()) {
			case TimeTUnitType.NS_TYPE:
				result /= NanoSToS;
				break;
			case TimeTUnitType.US_TYPE:
				result /= MicroSToS;
				break;
			case TimeTUnitType.MS_TYPE:
				result /= MilliSToS;
				break;
			case TimeTUnitType.S_TYPE:
				// no-op, Seconds are the standard
				break;
			case TimeTUnitType.MIN_TYPE:
				result /= MinToS;
				break;
			case TimeTUnitType.H_TYPE:
				result /= HToS;
				break;
			case TimeTUnitType.D_TYPE:
				result /= DToS;
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for time: %s",
						unit));
		}

		return result;
	}
	/* End Actual conversions
	 * ============================================================= */
}

/**
 * 
 */
package alma.scheduling.datamodel.helpers;

import alma.entity.xmlbinding.valuetypes.DoubleWithUnitT;
import alma.entity.xmlbinding.valuetypes.AngularVelocityT;
import alma.entity.xmlbinding.valuetypes.types.AngularVelocityTUnitType;

/**
 * @author dclarke
 *
 */
public class AngularVelocityConverter {

	final static double ARCSEC_StoDEG_S = 1.0 / (60.0 * 60.0);
	final static double ARCMIN_StoDEG_S = 1.0 / 60.0;
	/* The number of seconds in a year used is the same as used in
	 * Casa's quanta libraries, and is based on the Julian year.
	 */
	final static double SecondsPerYear = 365.25 * 24.0 * 60.0 * 60.0;
	final static double MAS_YRtoDEG_S = 1.0 / (SecondsPerYear * 60.0 * 60.0 * 1000.0);
	
	/*
	 * ================================================================
	 * Public interface
	 * ================================================================
	 */
	static public double convertedValue(AngularVelocityT angularVelocity, AngularVelocityTUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(angularVelocity, unit);
	}
	/* End Public interface
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Internal superclass interface
	 * ================================================================
	 */
	static private double convertedGenericValue(DoubleWithUnitT angularVelocity, AngularVelocityTUnitType unit) 
		throws ConversionException {
		double result;

		if (angularVelocity.getUnit().equals(unit.toString())) {
			result = angularVelocity.getContent();
		} else {
			final double standard = valueConvertedToStandardUnits(angularVelocity);
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
	static private double valueConvertedToStandardUnits(DoubleWithUnitT angularVelocity) 
			throws ConversionException {
		final AngularVelocityTUnitType unit = AngularVelocityTUnitType.valueOf(angularVelocity.getUnit());
		double result = angularVelocity.getContent();

		switch (unit.getType()) {
			case AngularVelocityTUnitType.ARCSEC_S_TYPE:
				result *= ARCSEC_StoDEG_S;
				break;
			case AngularVelocityTUnitType.ARCMIN_S_TYPE:
				result *= ARCMIN_StoDEG_S;
				break;
			case AngularVelocityTUnitType.DEG_S_TYPE:
				// no-op, deg/s are the standard
				break;
			case AngularVelocityTUnitType.MAS_YR_TYPE:
				result *= MAS_YRtoDEG_S;
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for angularVelocity: %s",
						unit));
		}
		
		return result;
	}
	
	static private double valueInStandardUnitsConvertedTo(double standard, AngularVelocityTUnitType unit) 
			throws ConversionException {
		double result = standard;

		switch (unit.getType()) {
		case AngularVelocityTUnitType.ARCSEC_S_TYPE:
			result /= ARCSEC_StoDEG_S;
			break;
		case AngularVelocityTUnitType.ARCMIN_S_TYPE:
			result /= ARCMIN_StoDEG_S;
			break;
		case AngularVelocityTUnitType.DEG_S_TYPE:
			// no-op, deg/s are the standard
			break;
		case AngularVelocityTUnitType.MAS_YR_TYPE:
			result /= MAS_YRtoDEG_S;
			break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for angularVelocity: %s",
						unit));
		}

		return result;
	}
	/* End Actual conversions
	 * ============================================================= */

}

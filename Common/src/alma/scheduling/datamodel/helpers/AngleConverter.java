/**
 * 
 */
package alma.scheduling.datamodel.helpers;

import alma.entity.xmlbinding.valuetypes.AngleT;
import alma.entity.xmlbinding.valuetypes.DoubleWithUnitT;
import alma.entity.xmlbinding.valuetypes.LatitudeT;
import alma.entity.xmlbinding.valuetypes.LongitudeT;
import alma.entity.xmlbinding.valuetypes.types.AngleTUnitType;
import alma.entity.xmlbinding.valuetypes.types.UserAngleTUserUnitType;

/**
 * @author dclarke
 *
 */
public class AngleConverter {

	final static double DEGtoRAD    = Math.PI/180.0;
	final static double ARCMINtoRAD = 1.0/60.0 * DEGtoRAD;
	final static double ARCSECtoRAD = 1.0/60.0 * ARCMINtoRAD;
	final static double MAStoRAD    = 1.0/1000.0 * ARCSECtoRAD;
	final static double HtoRAD      = 360.0/24.0 * DEGtoRAD;
	
	/*
	 * ================================================================
	 * Public interface
	 * ================================================================
	 */
	static public double convertedValue(AngleT angle, AngleTUnitType unit) 
			throws ConversionException {
		return convertedGenericValue(angle, unit);
	}
	
	static public double convertedValue(AngleT angle, UserAngleTUserUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(angle, unit);
	}
	
	static public double convertedValue(LatitudeT angle, AngleTUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(angle, unit);
	}
	
	static public double convertedValue(LatitudeT angle, UserAngleTUserUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(angle, unit);
	}
	
	static public double convertedValue(LongitudeT angle, AngleTUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(angle, unit);
	}
	
	static public double convertedValue(LongitudeT angle, UserAngleTUserUnitType unit) 
		throws ConversionException {
		return convertedGenericValue(angle, unit);
	}
	/* End Public interface
	 * ============================================================= */
	
	
	
	/*
	 * ================================================================
	 * Internal superclass interface
	 * ================================================================
	 */
	static private double convertedGenericValue(DoubleWithUnitT angle, AngleTUnitType unit) 
		throws ConversionException {
		double result;

		if (angle.getUnit().equals(unit.toString())) {
			result = angle.getContent();
		} else {
			final double standard = valueConvertedToStandardUnits(angle);
			result = valueInStandardUnitsConvertedTo(standard, unit);
		}
		
		return result;
	}
	
	static private double convertedGenericValue(DoubleWithUnitT angle, UserAngleTUserUnitType unit) 
		throws ConversionException {
		double result;

		if (angle.getUnit().equals(unit.toString())) {
			result = angle.getContent();
		} else {
			final double standard = valueConvertedToStandardUnits(angle);
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
	static private double valueConvertedToStandardUnits(DoubleWithUnitT angle) 
			throws ConversionException {
		final AngleTUnitType unit;
		try {
			unit = AngleTUnitType.valueOf(angle.getUnit());
		} catch (IllegalArgumentException e) {
			throw new ConversionException(String.format(
					"Unrecognised input units for angle: %s",
					angle.getUnit()));
		}
		
		double result = angle.getContent();

		switch (unit.getType()) {
			case AngleTUnitType.DEG_TYPE:
				result *= DEGtoRAD;
				break;
			case AngleTUnitType.RAD_TYPE:
				// no-op, radians are the standard
				break;
			case AngleTUnitType.ARCMIN_TYPE:
				result *= ARCMINtoRAD;
				break;
			case AngleTUnitType.ARCSEC_TYPE:
				result *= ARCSECtoRAD;
				break;
			case AngleTUnitType.MAS_TYPE:
				result *= MAStoRAD;
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for angle: %s",
						unit));
		}
		
		return result;
	}
	
	static private double valueInStandardUnitsConvertedTo(double standard, AngleTUnitType unit) 
			throws ConversionException {
		double result = standard;

		switch (unit.getType()) {
			case AngleTUnitType.DEG_TYPE:
				result /= DEGtoRAD;
				break;
			case AngleTUnitType.RAD_TYPE:
				// no-op, radians are the standard
				break;
			case AngleTUnitType.ARCMIN_TYPE:
				result /= ARCMINtoRAD;
				break;
			case AngleTUnitType.ARCSEC_TYPE:
				result /= ARCSECtoRAD;
				break;
			case AngleTUnitType.MAS_TYPE:
				result /= MAStoRAD;
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for angle: %s",
						unit));
		}
		
		return result;
	}
	
	static private double valueInStandardUnitsConvertedTo(double standard, UserAngleTUserUnitType unit) 
			throws ConversionException {
		double result = standard;
		
		switch (unit.getType()) {
			case UserAngleTUserUnitType.DEG_TYPE:
				result /= DEGtoRAD;
				break;
			case UserAngleTUserUnitType.RAD_TYPE:
				// no-op, radians are the standard
				break;
			case UserAngleTUserUnitType.ARCMIN_TYPE:
				result /= ARCMINtoRAD;
				break;
			case UserAngleTUserUnitType.ARCSEC_TYPE:
				result /= ARCSECtoRAD;
				break;
			case UserAngleTUserUnitType.MAS_TYPE:
				result /= MAStoRAD;
				break;
			case UserAngleTUserUnitType.FRACTION_OF_MAIN_BEAM_TYPE:
				throw new NeedsContextException(String.format(
						"Conversion to %s needs context",
						unit));
			case UserAngleTUserUnitType.H_TYPE:
				result /= HtoRAD;
				break;
			default:
				throw new ConversionException(String.format(
						"Unrecognised units for angle: %s",
						unit));
		}
	
		return result;
	}
	/* End Actual conversions
	 * ============================================================= */
	
	
	
}

/**
 * 
 */
package alma.scheduling.datamodel.helpers;

/**
 * @author dclarke
 *
 */
public class ConversionException extends Exception {

	/**
	 * @param message
	 */
	public ConversionException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ConversionException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConversionException(String message, Throwable cause) {
		super(message, cause);
	}

}

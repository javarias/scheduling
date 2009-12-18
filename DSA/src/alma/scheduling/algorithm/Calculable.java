package alma.scheduling.algorithm;

/**
 * This interface should be implemented by the classes which want to be used as
 * parameter of my algorithm
 * 
 * @see MyLittleScoreAlg
 * @author javarias
 *
 */

public interface Calculable {

	/**
	 * This method should be called to calculate a number 
	 * 
	 * @return A number after do the math stuff
	 */
	public double calculateScore();
}

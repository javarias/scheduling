package alma.scheduling.algorithm;

import java.util.Collection;

import alma.scheduling.datamodel.Entity;

/**
 * Class used to calculate the score of the Entity. It use injected parameters, 
 * each parameter assigned must implement {@link alma.scheduling.algorithm.Calculable} 
 * interface.
 * </br>
 * This example include two implemented classes: 
 * {@link alma.scheduling.algorithm.Param1}
 * {@link alma.scheduling.algorithm.Param2}
 * 
 * @author javarias
 *
 */
public class MyLittleScoreAlg {

	private Collection<Calculable> parameters;
	
	/**
	 * Calculate the Score of an Entity based on the parameters assigned.
	 * 
	 * @param e The entity to assign the score
	 */
	public void calculateScore(Entity e) {
		double result = 0.0;
		for(Calculable param: parameters){
			result += param.calculateScore();
		}
		e.setScore(result);
	}

	public Collection<Calculable> getParameters() {
		return parameters;
	}

	public void setParameters(Collection<Calculable> parameters) {
		this.parameters = parameters;
	}

}

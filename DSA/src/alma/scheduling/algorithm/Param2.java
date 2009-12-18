package alma.scheduling.algorithm;

import java.util.Random;

/**
 * Parameter to calculate a part of the score of my algorithm
 * @see MyLittleScoreAlg
 * @author javarias
 *
 */
public class Param2 implements Calculable {
	
	private double param1;
	private double param2;
	private double param3;
	private boolean random;

	public Param2(){
		random = false;
		Random rand = new Random();
		param1 = rand.nextDouble() *10;
		param2 = rand.nextDouble() *10;
		param3 = rand.nextDouble() *10;
	}
	
	@Override
	public double calculateScore() {
		if(random){
			Random rand = new Random();
			param1 = rand.nextDouble() *10;
			param2 = rand.nextDouble() *10;
			param3 = rand.nextDouble() *10;
		}
		return (param1 + param2 + param3)/3;
	}

	public double getParam1() {
		return param1;
	}

	public void setParam1(double param1) {
		this.param1 = param1;
	}

	public double getParam2() {
		return param2;
	}

	public void setParam2(double param2) {
		this.param2 = param2;
	}

	public double getParam3() {
		return param3;
	}

	public void setParam3(double param3) {
		this.param3 = param3;
	}

	public boolean isRandom() {
		return random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}

}

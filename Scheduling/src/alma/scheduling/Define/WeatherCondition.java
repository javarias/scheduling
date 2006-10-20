/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File WeatherCondition.java
 */
 
package alma.scheduling.Define;

import java.util.StringTokenizer;
/**
 * The WeatherCondition class defines a measure of the favorable conditions 
 * under which a scheduling block may be executed.  After being created, the principle 
 * method is 'evaluate()'.  This method returns a float that is between 0.0 
 * and 1.0.
 * <ul>
 * 		<li>	1.0 means "it doesn't get better than this". 
 * 		<li>	0.0 means "forget it".
 * </ul>  
 * For intermediate values, the higher the value the more favorable are 
 * the conditions.  The constructor takes an array of strings.  Each string 
 * is of the form:
 * <ul>
 * 		<li> &lt;logical_expression&gt; -&gt; &lt;target&gt;
 * </ul>
 * where target is a floating point number between 0.0 and 1.0.  All the
 * environmental variables have names (humidity, temperature, pressure, 
 * windVelocity, windDirection, etc.) and these names may appear in the 
 * logical expression.
 * When the favorable condition 'evaluate()' method is called, the logical
 * expressions are evaluated in the order in which they were defined.  The 
 * first expression that is true, results in the 'target' that is associated 
 * with that condition being returned as the favorable condition indicator.
 * 
 * The syntax of a string is defined below.
 * 
 * Syntax:
 * 		<favorable_condition> := <logical_expression> '->' <target>
 * 		<target> := a floating point numeric value between 0.0 and 1.0
 * 		<logical_expression> :=
 * 			a. '(' <logical_expression ')'
 * 			b. '!' '(' <logical_expression ')'
 * 			c. <logical_expression> '&&' 	<logical_expression>
 * 			d. <logical_expression> '||' <logical_expression>
 * 			e. <factor_name> <comparison_operator> <numeric_value>
 * 			f. <numeric_value> <comparison_operator> <factor_name>
 * 		<comparison_operator> := '==' | '!=' | '<' | '<=' | '>' | '>='
 * 		<numeric_value> := a valid floating point number
 * 		<factor_name> := one of the names of the environmental factors
 * 
 * An example will make this clearer.
 * 
 * Suppose we have the following array of strings as defining the 
 * WeatherCondition.
 * 
 * 			"humidity <= 10.0 -> 1.0",
 * 		 	"humidity <= 20.0 -> 0.8",
 * 		 	"humidity <= 30.0 -> 0.6",
 * 		 	"humidity <= 40.0 -> 0.4",
 * 		 	"humidity <= 50.0 -> 0.2",
 * 		 	"humidity >  50.0 -> 0.0"
 * 
 * If the current value for humidity is 35.25, the "evaluste' method would 
 * return 0.4.  This result has the meaning that under the current conditions, 
 * the favorability rating is 40%.
 * 
 * @version $Id: WeatherCondition.java,v 1.7 2006/10/20 15:38:45 sslucero Exp $
 * @author Allen Farris
 */
public class WeatherCondition {

	private Expression[] condition;
	private float[] result;
	
	/**
	 * Create a WeatherCondition object given an array of strings, as
	 * defined above.
	 */
	public WeatherCondition(String[] x) {
		condition = new Expression [x.length];
		result = new float [x.length];
		int n = 0;
		for (int i = 0; i < x.length; ++i) {
			n = x[i].indexOf("->");
			if (n == -1)
				throw new IllegalArgumentException("Invalid syntax in expression \"" + x[i] +
					"\"  Missing '->'");
			try {
				result[i] = Float.parseFloat(x[i].substring(n + 2));
				if (result[i] < 0.0F || result[i] > 1.0F)
					throw new IllegalArgumentException("Invalid target result!  (" +
						result[1] + ")  Floating point target result must be between 0.0 and 1.0.");
			} catch (NumberFormatException err) {
				throw new IllegalArgumentException("Invalid syntax in expression \"" + x[i] +
					"\"  Invalid floating point number.");
			}
			condition[i] = new Expression(x[i].substring(0,n));
		}
	}

    //create a weather condition based on a condition-word 
    //which is associated with pre-defined condition strings.
    //public WeatherCondition(String conditionWord) {
    //}
	
	/**
	 * The evaluate method returns a floating point number between 0.0 and 1.0,
	 * as a measure of the favorablity rating.
	 */
	public float evaluate(Object... args) {
        float value =0.0F; 
        String currName = null;
        String prevName = null;
        boolean doesCurrNameHaveValue = false;
		for (int i = 0; i < condition.length; ++i) {
            currName = (new StringTokenizer(condition[i].toString(),",")).nextToken();
            if(prevName == null) {
                if(evaluateCondition(condition[i], currName, args)){
                    doesCurrNameHaveValue = true;
                    if(value ==0.0F){
                        value = result[i];
                    } else {
                        value = value * result[i];
                    }
                }
            } else if (currName.equals(prevName) && !doesCurrNameHaveValue) {
                if(evaluateCondition(condition[i], currName, args)){
                    doesCurrNameHaveValue = true;
                    if(value ==0.0F){
                        value = result[i];
                    } else {
                        value = value * result[i];
                    }
                }
            } else if (!currName.equals(prevName)) {
                doesCurrNameHaveValue = false;
                if(evaluateCondition(condition[i], currName, args)){
                    doesCurrNameHaveValue = true;
                    if(value ==0.0F){
                        value = result[i];
                    } else {
                        value = value * result[i];
                    }
                }
            } else {
                //already have a value for it
                //basically ....
                //(currName.equals(prevName && doesCurrNameHaveValue == true) == true
            }
            prevName = currName;
        }
        return value;
	}

    /**
      * In this method we want to scale the value coming from the real weather data file
      * or the actual weather stations, so that its scaled in accordance to the sb we want 
      * to schedule.
      * NOTE: If the scaling changes it must also be changed in PlanningModeSim's opacityModel 
      * and/or RmsModel
      * The scales objects will be 2 or 3 objects. Frequency, Elevation, (and possibly) max Baseline
      */
    private Expression scaleCondition(Expression e, Object... scales) {
        StringTokenizer st = new StringTokenizer(e.toString(), ",");
        String word = st.nextToken();
        word = word.trim();
        String s_val = st.nextToken();
        s_val = s_val.trim();
        double val = (new Double(s_val)).doubleValue();
        String oper = st.nextToken();
        if(word.equals("opacity")){
            val = val * 
                (((Double)scales[0]).doubleValue() / 225) * 
                ( 1 / Math.sin(((Double)scales[1]).doubleValue()));
        }else if(word.equals("rms")){
            val = val * (((Double)scales[0]).doubleValue() /11.2) * 
                    (Math.sin(36*Math.PI/180) / Math.sin(((Double)scales[1]).doubleValue())) * 
                    Math.pow( (((Double)scales[2]).doubleValue() / 300), (1/3) ) ;
        }
        Expression expr = new Expression(word+" "+oper+" "+val );
        return expr;
    }
    
    private boolean evaluateCondition(Expression exp, String name, Object... args){
        if(name.equals("rms") ){
            return exp.evaluate(args);
        } else {
            return exp.evaluate(args[0], args[1]);
        }
    }
            

	/**
	 * The toString method returns that targets together with the parsed
	 * strings in postfix notation.
	 */	
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < result.length; ++i) {
			s.append(result[i] + " <- " + condition[i] + "\n");
		}
		return s.toString();
	}
	
	/**
	 * Provide a unit test.
	 */
	public static void main(String[] args) {
		/* <<< This test needs to be fixed.  It doesn't work any more.

		// First we must configure the Expression class.
		String[] names = {
			"temperature",
			"humidity",
			"pressure",
			"windVelocity",
			"windDirection"
		};
		Expression.setFunctionNames(names);
		System.out.println("OK, static functions are set.");
		//Simulator sim = new Simulator(null,null,null,
		//					new Time(2003,2,26,10,0,0), new Time(2003,2,28,10,0,0));
		//EnvironmentalModel.setSimulator(sim);
		String[] methods = {
			"schedule.EnvironmentalModel.getTemperature",
			"schedule.EnvironmentalModel.getHumidity",
			"schedule.EnvironmentalModel.getPressure",
			"schedule.EnvironmentalModel.getWindVelocity",
			"schedule.EnvironmentalModel.getWindDirection"
		};
		Expression.setMethods(methods);
		System.out.println("OK, methods are set.");


		String[] x = {
			"humidity <= 10.0 -> 1.0",
			"humidity <= 20.0 -> 0.8",
			"humidity <= 30.0 -> 0.6",
			"humidity <= 40.0 -> 0.4",
			"humidity <= 50.0 -> 0.2",
			"humidity >  50.0 -> 0.0"
		};		 
		WeatherCondition f1 = new WeatherCondition (x);
		System.out.println("f1: " + f1);
		System.out.println("f1's current value is " + f1.evaluate());
		*/
	}
	
	
}

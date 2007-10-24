
package alma.scheduling.PlanningModeSim;

import alma.scheduling.Define.DateTime;

public class OpacityModel extends RealWeatherModel{
    public OpacityModel(String f) throws Exception{
        super(f,"opacity");
    }

    /**
      * Used when retrieving opacity value to record in execution stats file
      */
    public double compute(DateTime t, Double freq, Double el){
        double tau0_225 = super.compute(t);
        double value = tau0_225 ;//* ( freq/225 ) * ( 1/Math.sin(el) );
        //System.out.println("opacity with time");
        //System.out.println("Current Opacity: time="+ t.toString()+"; Freq="+freq+"; Elev.="+el+"; tau0_225="+tau0_225+"; scaled="+value);
        return value;
    }

    public double compute(Double freq, Double el){
        double tau0_225 = super.compute();
        double value = tau0_225 ;//* ( freq/225 ) * ( 1/Math.sin(el) );
//        System.out.println("opacity with NO time");
        return value;
    }
}

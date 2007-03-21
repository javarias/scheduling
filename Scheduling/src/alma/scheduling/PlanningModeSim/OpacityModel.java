
package alma.scheduling.PlanningModeSim;

import alma.scheduling.Define.DateTime;

public class OpacityModel extends RealWeatherModel{
    public OpacityModel(String f) throws Exception{
        super(f,"opacity");
    }

    public double compute(DateTime t, Double freq, Double el){
        double tau0_225 = super.compute(t);
        double value = tau0_225 ;//* ( freq/225 ) * ( 1/Math.sin(el) );
        //System.out.println("Current Opacity: time="+ t.toString()+"; Freq="+freq+"; Elev.="+el+"; tau0_225="+tau0_225+"; scaled="+value);
        return value;
    }

    public double compute(Double freq, Double el){
        double tau0_225 = super.compute();
        double value = tau0_225 ;//* ( freq/225 ) * ( 1/Math.sin(el) );
        //System.out.println("Current Opacity: Freq="+freq+"; Elev.="+el+"; tau0_225="+tau0_225+"; scaled="+value);
        return value;
    }
}

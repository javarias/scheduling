
package alma.scheduling.PlanningModeSim;

public class OpacityModel extends RealWeatherModel{
    public OpacityModel(String f) throws Exception{
        super(f,"opacity");
    }

    public double compute(Double freq, Double el){
        double tau0_225 = super.compute();
        double value = tau0_225 * ( freq/225 ) * ( 1/Math.sin(el) );
        //System.out.println("Opacity at "+freq+" (GHz) & "+ el+" is "+value);
        return value;
    }
}

package alma.scheduling.PlanningModeSim;

public class RmsModel extends RealWeatherModel{
    public RmsModel(String f) throws Exception{
        super(f, "rms");
    }
    public double compute(Double freq, Double el, Double baseline){
        double phase_11_2 = super.compute();
        double value = phase_11_2 * ( freq/11.2 ) * (Math.sin(36*Math.PI/180) / Math.sin(el)) * 
          Math.pow( (baseline.doubleValue() / 300), (1/3) ) ;
        
        //System.out.println("Current RMS: Freq="+freq+"; Elev.="+el+"; baseline="+baseline+"; phase_11.2="+phase_11_2+"; scaled="+value);

        return value;
    }
}

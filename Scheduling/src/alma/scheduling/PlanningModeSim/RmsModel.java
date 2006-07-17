package alma.scheduling.PlanningModeSim;

public class RmsModel extends RealWeatherModel{
    public RmsModel(String f) throws Exception{
        super(f, "rms");
    }
    public double compute(Double freq, Double el, Double baseline){
        double phase_11_2 = super.compute();
        //temporarily using 300 as our baseline
        int bl = 300;
        //double value = phase_11_2 * ( freq/11.2 ) * (1 / Math.sin(el)) * 1;
        double value = phase_11_2 * ( freq/11.2 ) * (Math.sin(36*Math.PI/180) / Math.sin(el)) * 
          Math.pow( (baseline.doubleValue() / 300), (1/3) ) ;
        //System.out.println("RMS Phase at "+baseline+"(m), elevation "+el+" & phase stability "+phase_11_2+"  = "+value);
        return value;
    }
}

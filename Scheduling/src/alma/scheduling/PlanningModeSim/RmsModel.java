package alma.scheduling.PlanningModeSim;
import alma.scheduling.Define.DateTime;
public class RmsModel extends RealWeatherModel{
    public RmsModel(String f) throws Exception{
        super(f, "rms");
    }

    public double compute(Double freq, Double el, Double baseline){
        double phase_11_2 = super.compute();
        double value = phase_11_2 * ( freq/11.2 ) * (Math.sin(36*Math.PI/180) / Math.sin(el)) * 
          Math.pow( (baseline.doubleValue() / 300), (1/3) ) ;
        //System.out.println("/////////start////////////");
        //System.out.println("RMS in file = "+phase_11_2+"; scaled = "+value+" at "+
        //        clock.getDateTime().toString());
        //System.out.println("freq = "+freq+"; el= "+el+"; baseline= "+baseline);
        //System.out.println("//////////end/////////////\n");
        return value;
    }
    public double compute(DateTime t, Double freq, Double el, Double baseline){
        double phase_11_2 = super.compute(t);
        double value = phase_11_2 * ( freq/11.2 ) * (Math.sin(36*Math.PI/180) / Math.sin(el)) * 
          Math.pow( (baseline.doubleValue() / 300), (1/3) ) ;
        //System.out.println("/////////start a///////////");
        //System.out.println("a.RMS in file = "+phase_11_2+"; scaled = "+value+" at "+
        //        t.toString());
        //System.out.println("a. freq = "+freq+"; el= "+el+"; baseline= "+baseline);
        //System.out.println("//////////end a/////////////\n");
        return value;
    }
    
    
    public double computeWithNoScale(DateTime t, Double freq, Double el, Double baseline){
        double phase_11_2 = super.compute(t);
        return phase_11_2;
    }
}

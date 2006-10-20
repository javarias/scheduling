package alma.scheduling.PlanningModeSim;

import alma.scheduling.Define.DateTime;
public class WindSpeedModel extends RealWeatherModel {
    public WindSpeedModel(String f) throws Exception {
        super(f,"wind");
    }
    public double compute(Double x, Double y){
        double tmp = super.compute();
        //System.out.println("Currnet Wind: value="+tmp);
        return tmp;
    }
    public double compute(DateTime t){
        double tmp = super.compute(t);
        //System.out.println("Currnet Wind: time="+t.toString()+"; value="+tmp);
        return tmp;
    }
}

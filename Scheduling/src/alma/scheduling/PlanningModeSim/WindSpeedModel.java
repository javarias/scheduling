package alma.scheduling.PlanningModeSim;

import alma.scheduling.Define.DateTime;
public class WindSpeedModel extends RealWeatherModel {
    public WindSpeedModel(String f) throws Exception {
        super(f,"wind");
    }
    public double compute(Double x, Double y){
        return super.compute();
    }
    public double compute(DateTime t){
        return super.compute(t);
    }
}

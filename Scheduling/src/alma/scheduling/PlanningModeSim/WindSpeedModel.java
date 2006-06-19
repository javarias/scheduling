package alma.scheduling.PlanningModeSim;

public class WindSpeedModel extends RealWeatherModel {
    public WindSpeedModel(String f) throws Exception {
        super(f,"wind");
    }
    public double compute(Double x, Double y){
        return super.compute();
    }
}

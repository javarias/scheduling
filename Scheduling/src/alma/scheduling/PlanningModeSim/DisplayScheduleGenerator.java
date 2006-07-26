package alma.scheduling.PlanningModeSim;

public class DisplayScheduleGenerator {

    private String proj_name;
    private String sb_name;
    private String priority;
    private String frequency;
    private double ra;
    private double dec;
    private String[] lstRange;
    private double opacity;
    private double rms;
    private double wind;
    private String weatherConstraint;

    public DisplayScheduleGenerator() {
    }

    public void setProjectName(String pn){
        proj_name = pn;
    }
    public void setSBName(String sb){
        sb_name = sb;
    }
    public void setPriority(String pri){
        priority = pri;
    }
    public void setFrequency(String freq){
        frequency = freq;
    }
    public void setRA(double r){
        ra = r;
    }
    public void setDEC(double d){
        dec = d;
    }
    public void setLSTRange(String[] lst){
        lstRange = lst;
    }
    public void setOpacity(double op){
        opacity = op;
    }
    public void setRMS(double rms){
        this.rms = rms;
    }
    public void setWind(double w){
        wind = w;
    }
    public void setWeatherConstraint(String w){
        weatherConstraint = w;
    }
}


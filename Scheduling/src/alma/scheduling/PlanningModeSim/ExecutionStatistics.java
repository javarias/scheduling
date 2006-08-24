package alma.scheduling.PlanningModeSim;

public class ExecutionStatistics {

    private String proj_name;
    private String sb_name;
    private String priority;
    private double frequency;
    private double ra;
    private double dec;
    private double[] lstRange; //0 = rise, 1 = max, 2 = set
    private double opacity;
    private double rms;
    private double wind;
    private String weatherConstraint;
    
    private String arrayname;
    private String startTime;
    private String endTime;
    private double score;
    private double success;
    private double rank;
    private String exec_id;

    public ExecutionStatistics() {
        lstRange = new double[3];
    }
    
    public void setExecId(String id){
        exec_id = id;
    }
    public void setArrayName(String ar){
        arrayname =ar;
    }
    public void setStartTime(String st){
        startTime = st;
    }
    public void setEndTime(String et){
        endTime = et;
    }
    public void setScore(double s){
        score = s;
    }
    public void setSuccess(double s){
        success = s * 100;
    }
    public void setRank(double r){
        rank = r;
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
    public void setFrequency(double freq){
        frequency = freq;
    }
    public void setRA(double r){
        ra = r;
    }
    public void setDEC(double d){
        dec = d;
    }
    public void setLSTRange(double[] lst){
        lstRange = lst;
    }
    public void setLSTRise(double lstrise){
        lstRange[0] = lstrise;
    }
    public void setLSTMax(double lstmax){
        lstRange[1] = lstmax;
    }
    public void setLSTSet(double lstset){
        lstRange[2] = lstset;
    }
    public void setOpacity(double op){
        opacity = op;
    }
    public void setRMS(double r){
        rms = r;
    }
    public void setWind(double w){
        wind = w;
    }
    public void setWeatherConstraint(String w){
        weatherConstraint = w;
    }
//////////
    public String getExecId(){
        return exec_id;
    }
    public String getArrayName(){
        return arrayname;
    }
    public String getStartTime(){
        return startTime;
    }
    public String getEndTime(){
        return endTime;
    }
    public double getScore(){
        return score;
    }
    public double getSuccess(){
        return success;
    }
    public double getRank(){
        return rank;
    }
    public String getProjectName(){
        return proj_name;
    }
    public String getSBName(){
        return sb_name;
    }
    public String getPriority(){
        return priority;
    }
    public double getFrequency(){
        return frequency;
    }
    public double getRA(){
        return ra;
    }
    public double getDEC(){
        return dec;
    }
    public double getLSTRise(){
        return lstRange[0];
    }
    public double getLSTMax(){
        return lstRange[1];
    }
    public double getLSTSet(){
        return lstRange[2];
    }
    public double getOpacity(){
        return opacity;
    }
    public double getRMS(){
        return rms;
    }
    public double getWind(){
        return wind;
    }
    public String getWeatherConstraint(){
        return weatherConstraint;
    }

    /**
      * NOTE: The order in toString and getColumnsInfoString MUST MATCH other wise
      * there will be confusion if both are printed out together!!!
      */
    public String toString() {
        return getSBName()+"; "+
               getProjectName() +"; "+
               getExecId()+"; "+ 
               getPriority() +"; "+
               getFrequency() +"; "+
               getRA() +"; "+
               getDEC() +"; "+
               getOpacity() +"; "+
               getRMS() +"; "+
               getWind() +"; "+
               getArrayName()+"; "+
               getStartTime() +"; "+
               getEndTime()+"; "+
               getLSTRise() +"; "+
               getLSTMax() +"; "+
               getLSTSet() +"; "+
              // getWeatherConstraint()+"; "+
               getScore() +"; "+
               getSuccess() +"; "+
               getRank();
              
    }
    /**
      * See comment for toString
      */
    public String getColumnsInfoString(){
        return "## 1.  SB Name \n"+
               "## 2.  Project Name \n"+
               "## 3.  Exec ID \n"+
               "## 4.  Priority \n"+
               "## 5.  Frequency \n"+
               "## 6.  RA (Max) \n"+
               "## 7.  DEC (Max) \n"+
               "## 8.  Opacity \n"+
               "## 9.  RMS \n"+
               "## 10. Wind Speed (M/S) \n"+
               "## 11. Array Name \n"+
               "## 12. Start Time \n"+
               "## 13. End Time \n"+
               "## 14. LST Rise \n"+
               "## 15. LST Max \n"+
               "## 16. LST Set \n"+
               "## 17. Score \n"+
               "## 18. Success (%)\n"+
               "## 19. Rank \n\n ";
     //   "## 12. Weather Constraint \n"+
    }
    public String getAllStatistics(){
        return toString();
    }

    public String getExecutedExecutionStatistics(){
        return "";
    }
}


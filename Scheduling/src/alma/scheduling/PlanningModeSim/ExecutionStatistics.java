package alma.scheduling.PlanningModeSim;
import alma.scheduling.Define.Date;
import alma.scheduling.Define.Time;
import alma.scheduling.Define.DateTime;
public class ExecutionStatistics {

    private String proj_name;
    private String sb_name;
    private String priority;
    private double frequency;
    private String freqBandName;
    private double ra; //center ra in hours
    private double dec; //center dec in degrees
    private double elevation; // in degrees
    private double[] lstRange; //0 = rise, 1 = max, 2 = set
    private double opacity;
    private double rms;
    private double wind;
    private String weatherConstraintName;
    private double scheduleStartMNLst; //lst at midnight of start day
    private double scheduleEndMNLst; //lst at midnight of end day
    
    private String arrayname;
    private DateTime startTime;
    private DateTime endTime;
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
    public void setStartTime(DateTime st){
        startTime = st;
    }
    public void setEndTime(DateTime et){
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
    public void setFrequencyBandName(String s){
        freqBandName = s;
    }
    public void setRA(double r){
        ra = r;
    }
    public void setDEC(double d){
        dec = d;
    }
    public void setElevation(double el){
        elevation = el;
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
    public void setWeatherConstraintName(String w){
        weatherConstraintName = w;
    }
    public void setLSTatVeryStartMidnight(double l){
        scheduleStartMNLst = l;
    }
    public void setLSTatVeryEndMidnight(double l){
        scheduleEndMNLst = l;
    }
//////////
    public String getExecId(){
        return exec_id;
    }
    public String getArrayName(){
        return arrayname;
    }
    public int getStartDay(){
        return startTime.getDate().getDay();
    }
    public double getLSTatMidnight(){
        Date d = startTime.getDate();
        Time t = new Time(0,0,0);
        DateTime midnight = new DateTime(d,t);
        return midnight.getLocalSiderealTime();
    }

    public int getEndDay() {
        return endTime.getDate().getDay();
    }
    public DateTime getStartTime(){
        return startTime;
    }
    public DateTime getEndTime(){
        return endTime;
    }
    public double getExecutionHourLength() {
        double jd =DateTime.difference(endTime, startTime); 
        return jd * 24.0;
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
    public String getFrequencyBandName() {
        return freqBandName;
    }
    public double getRA(){
        return ra;
    }
    public double getDEC(){
        return dec;
    }
    public double getElevation(){
        return elevation;
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
    public String getWeatherConstraintName(){
        return weatherConstraintName;
    }
    public double getLSTatVeryStartMidnight(){
        return scheduleStartMNLst;
    }
    public double getLSTatVeryEndMidnight(){
        return scheduleEndMNLst;
    }
    public int getStartMonth() {
        return getStartTime().getDate().getMonth();
    }
    public int getEndMonth() {
        return getEndTime().getDate().getMonth();
    }

    /**
      * NOTE: The order in toString and getColumnsInfoString MUST MATCH other wise
      * there will be confusion if both are printed out together!!!
      * NOTE 2: if the order is changed the python plots will have to
      * be modified.
      */
    public String toString() {
        return getSBName()+", "+
               getStartTime() +", "+
               getStartTime().getTimeOfDay() +", "+
               getEndTime()+", "+
               getExecutionHourLength()+", "+
               getFrequency() +", "+
               getFrequencyBandName() +", "+
               getRA() +", "+
               getDEC() +", "+
               getElevation() +", "+
               getWeatherConstraintName() +", "+
               getOpacity() +", "+
               getRMS() +", "+
               getWind() +", "+
               getStartDay()+", "+
               getEndDay()+", "+
               getLSTatMidnight()+", "+
               getLSTatVeryStartMidnight()+", "+
               getLSTatVeryEndMidnight()+", "+
               getStartMonth()+", "+
               getEndMonth()+
               "";
            /*
               getProjectName() +", "+
               getExecId()+", "+ 
               getPriority() +", "+
               getArrayName()+", "+
               getStartDay()+", "+
               getStartTime() +", "+
               getEndDay()+", "+
               getEndTime()+", "+
               getLSTRise() +", "+
               getLSTMax() +", "+
               getLSTSet() +", "+
               getScore() +", "+
               getSuccess() +", "+
               getRank();
              */
    }
    /**
      * See comment for toString
      */
    public String getColumnsInfoString(){
        return "## 1.  SB Name \n"+
               "## 2.  Start Time \n"+
               "## 3.  Start Time (LST)\n"+
               "## 4.  End Time \n"+
               "## 5.  Length of Execution in hours\n"+
               "## 6.  Frequency\n"+
               "## 7.  Frequency Band\n"+
               "## 8.  RA (Radians)\n"+
               "## 9.  DEC (Radians)\n"+
               "## 10. Elevation at execution Time\n"+
               "## 11. Weather Constraint Name\n"+
               "## 12. Opacity \n"+
               "## 13. RMS \n"+
               "## 14. Wind Speed (M/S) \n"+
               "## 15. Start Day  \n"+
               "## 16. End Day \n"+
               "## 17. LST at Midnight on startday\n"+
               "## 18. LST at Midnight on startday of simulation\n"+
               "## 19. LST at Midnight on endday of simulation\n"+
               "## 20. Start Month\n"+
               "## 21. End Month\n"+
               "";
        /*
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
               "## 12. Start Day\n"+
               "## 13. Start Time \n"+
               "## 14. Start Time (LST)\n"+
               "## 15. End Day\n"+
             2006-01-01T20:18:42  "## 16. End Time\n"+
               "## 17. End Time (LST)\n"+
               "## 18. LST Rise \n"+
               "## 19. LST Max \n"+
               "## 20. LST Set \n"+
               "## 21. Score \n"+
               "## 22. Success (%)\n"+
               "## 23. Rank \n\n ";*/
     //   "## 12. Weather Constraint \n"+
    }
    public String getCurrentWeatherColumnInfoString(){
        return "##1. StartDay\n"+
               "##2. EndDay\n"+
               "##3. LST Start\n"+
               "##4. LST End\n"+
               "##5. Opacity\n"+
               "##6. RMS \n"+
               "##7. Wind \n";
    }
    public String getCurrentWeatherInfo(){
        return getStartDay() +", "+ getEndDay()+", "+getStartTime() +", "+ 
               getEndTime() +"; "+getOpacity() +", "+ getRMS() +", "+ getWind();
    }
    public String getAllStatistics(){
        return toString();
    }

    public String getExecutedExecutionStatistics(){
        return "";
    }
}


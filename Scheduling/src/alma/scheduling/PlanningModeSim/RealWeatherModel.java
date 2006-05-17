package alma.scheduling.PlanningModeSim;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*; 

import alma.scheduling.Define.Clock;
import alma.scheduling.Define.Time;
import alma.scheduling.Define.Date;
import alma.scheduling.Define.DateTime;

public class RealWeatherModel {
    private DateTime initializedTime;
    private String name;
    private String wind_filename, rms_filename, opacity_filename;
   // private File wind_file, rms_file, opacity_file;
    private BufferedReader wind_br, rms_br, opacity_br;
    private Clock clock;
    private Map<DateTime, Double> windSpeedMap; //key = datetime as a string & value = corresponding windspeed
    private Map<DateTime, Double> rmsMap; //key = datetime as a string & value = corresponding rms 
    private Map<DateTime, Double> opacityMap; //key = datetime as a string & value = corresponding opacity

    public RealWeatherModel(String f1, String f2, String f3) throws Exception{
        initializedTime = new DateTime(System.currentTimeMillis());
        wind_filename = f1;
        rms_filename = f2;
        opacity_filename = f3;
        openFileStreams();
    }

    public void setClock(Clock c) {
        clock = c;
    }

    private void openFileStreams() throws Exception{
        //buffered readers
        wind_br = new BufferedReader(new FileReader(new File(wind_filename)));
        createWindSpeedMap();
        rms_br = new BufferedReader(new FileReader(new File(rms_filename)));
        createRmsMap();
        opacity_br = new BufferedReader(new FileReader(new File(opacity_filename)));
        createOpacityMap();
    }

    private void createWindSpeedMap() throws Exception{
        windSpeedMap = new LinkedHashMap<DateTime, Double>();
        //get line from file
        StringTokenizer st;
        DateTime dt;
        String tmp;
        String line=wind_br.readLine();
        while(line !=null){
            if(!line.startsWith("#")) {
                st = new StringTokenizer(line);
                tmp = st.nextToken(); //ignore coz its year and we don't want to use it.
                dt = new DateTime(
                        new Date(initializedTime.getDate().getYear(), //replace to current year for comparision
                            Integer.parseInt(st.nextToken()), 
                            Integer.parseInt(st.nextToken())) ,
                        new Time(new Double(st.nextToken())) );
                tmp = st.nextToken();
                windSpeedMap.put(dt, new Double(tmp));
            }
            line = wind_br.readLine();
        }
    }
    private void createRmsMap() throws Exception {
        rmsMap = new LinkedHashMap<DateTime, Double>();
        //get line from file
        StringTokenizer st;
        DateTime dt;
        String tmp;
        String line = rms_br.readLine();
        while(line !=null){
            if(!line.startsWith("#")) {
                st = new StringTokenizer(line);
                tmp = st.nextToken(); //ignore coz its year and we don't want to use it.
                dt = new DateTime(
                        new Date(initializedTime.getDate().getYear(), //replace to current year for comparision
                            Integer.parseInt(st.nextToken()), 
                            Integer.parseInt(st.nextToken())) ,
                        new Time(new Double(st.nextToken())) );
                tmp = st.nextToken();
                rmsMap.put(dt, new Double(tmp));
            }
            line = rms_br.readLine();
        }
    }

    private void createOpacityMap() throws Exception {
        opacityMap = new LinkedHashMap<DateTime, Double>();
        //get line from file
        StringTokenizer st;
        DateTime dt;
        String tmp;
        String line = opacity_br.readLine();
        while(line !=null){
            if(!line.startsWith("#")) {
                st = new StringTokenizer(line);
                tmp = st.nextToken(); //ignore coz its year and we don't want to use it.
                dt = new DateTime(
                        new Date(initializedTime.getDate().getYear(), //replace to current year for comparision
                            Integer.parseInt(st.nextToken()), 
                            Integer.parseInt(st.nextToken())) ,
                        new Time(new Double(st.nextToken())) );
                tmp = st.nextToken();
                opacityMap.put(dt, new Double(tmp));
            }
            line = opacity_br.readLine();
        }
    }    

    public double[] compute(DateTime dt) throws Exception{
        int year = dt.getDate().getYear();
        int month = dt.getDate().getMonth();
        int day = dt.getDate().getDay();
        double decimal_time = dt.getTime().getTime();
        double[] values = new double[3];
        values[0] = computeWind (dt);
        values[1] = computeRMS(dt);
        values[2] = computeOpacity(dt);
        return values;
    }
    
    //eventually change these functions to read through all the data files
    //and if a value for one year isn't available pick the one from a 
    //different year.
    
    private double computeWind(DateTime now){
        //System.out.println( "DateTime:now = "+now.toString());
        double result=0.0;
        //get time key closest to given time.
        Iterator<DateTime> i = windSpeedMap.keySet().iterator();
        DateTime tmp1; //initially is nothing coz in our loop we set it to the first one
        DateTime tmp2 = (DateTime)i.next();
        DateTime key=null;
        for(; i.hasNext(); ){
            tmp1 = tmp2;
            tmp2 = (DateTime)i.next();
            if(now.ge(tmp1) && now.lt(tmp2)){
                result = windSpeedMap.get(tmp1);
            }
        }
        return result;
    }
    
    private double computeRMS(DateTime now) {
        double result=0.0;
        //get time key closest to given time.
        Iterator<DateTime> i = rmsMap.keySet().iterator();
        DateTime tmp1; //initially is nothing coz in our loop we set it to the first one
        DateTime tmp2 = (DateTime)i.next();
        for(; i.hasNext(); ){
            tmp1 = tmp2;
            tmp2 = (DateTime)i.next();
            if(now.ge(tmp1) && now.lt(tmp2)){
                result = rmsMap.get(tmp1);
            }
        }
        return result;
    }

    private double computeOpacity(DateTime now) {
        double result=0.0;
        //get time key closest to given time.
        Iterator<DateTime> i = opacityMap.keySet().iterator();
        DateTime tmp1; //initially is nothing coz in our loop we set it to the first one
        DateTime tmp2 = (DateTime)i.next();
        for(; i.hasNext(); ){
            tmp1 = tmp2;
            tmp2 = (DateTime)i.next();
            if(now.ge(tmp1) && now.lt(tmp2)){
                result = opacityMap.get(tmp1);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println("Wrong number of arguements");
            for(int i=0; i <  args.length; i++){
                System.out.println(args[i]);
            }
            return;
        }
        System.out.println("Creating Real Weather Model with weather files: ");
        System.out.println("## "+args[0]);
        System.out.println("## "+ args[1]);
        System.out.println("## "+ args[2]);
        
        try{
            RealWeatherModel m = new RealWeatherModel(args[0], args[1], args[2]);
        
            DateTime time = new DateTime(System.currentTimeMillis());
            System.out.println("Time in main: "+time.toString());
            double[] x = m.compute(time);
            System.out.println("Wind speed = "+x[0]);
            System.out.println("RMS  = "+x[1]);
            System.out.println("Opacity = "+x[2]);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
}

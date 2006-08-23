package alma.scheduling.PlanningModeSim;

import java.util.*; 
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileOutputStream;

import alma.scheduling.Define.Clock;
import alma.scheduling.Define.Time;
import alma.scheduling.Define.Date;
import alma.scheduling.Define.DateTime;

public class RealWeatherModel {
    private DateTime initializedTime;
    private String name;
    private String filename;
    private BufferedReader reader;
    private Clock clock;
    private Map<DateTime, Double> dataMap; //key = datetime as a string & value = corresponding value

    public RealWeatherModel(String f, String n) throws Exception{
        filename = f;
        name = n;
        //initialize();
    }

    public void setClock(Clock c) {
        clock = c;
    }
    public String getName(){
        return name;
    }

    public void initialize() throws Exception{
        //initializedTime = new DateTime(System.currentTimeMillis());
        initializedTime = clock.getDateTime();
        //buffered readers
        File f = new File(filename); 
        System.out.println(f.toString());
        reader = new BufferedReader(new FileReader(f));
        createDataMap();
    }

    private void createDataMap() throws Exception{
        dataMap = new LinkedHashMap<DateTime, Double>();
        //get line from file
        StringTokenizer st;
        DateTime dt;
        String tmp;
        String line= reader.readLine();
        while(line !=null){
            if(!line.startsWith("#")) {
                st = new StringTokenizer(line);
                tmp = st.nextToken(); //ignore coz its year and we don't want to use it.
                try {
                    dt = new DateTime(
                            new Date(initializedTime.getDate().getYear(), //replace to current year for comparision
                                Integer.parseInt(st.nextToken()), 
                                Integer.parseInt(st.nextToken())) ,
                            new Time(new Double(st.nextToken())) );
                    tmp = st.nextToken(); //value
                    dataMap.put(dt, new Double(tmp));
                } catch(Exception e){
                    //System.out.println("Ignoring bad line: "+line);
                }
            }
            line =  reader.readLine();
        }
    }

    
    //eventually change these functions to read through all the data files
    //and if a value for one year isn't available pick the one from a 
    //different year.
    
    public double compute(DateTime now, Object... args){
        //System.out.println( "DateTime:now = "+now.toString());
        double result=0.0;
        //get time key closest to given time.
        Iterator<DateTime> i = dataMap.keySet().iterator();
        DateTime tmp1; //initially is nothing coz in our loop we set it to the first one
        DateTime tmp2 = (DateTime)i.next();
        DateTime key=null;
        for(; i.hasNext(); ){
            tmp1 = tmp2;
            tmp2 = (DateTime)i.next();
            //System.out.println("Checking "+tmp1.toString() +" and "+
            //        tmp2.toString() +" with "+now.toString());
            if(now.ge(tmp1) && now.lt(tmp2)){
                result = dataMap.get(tmp1);
            }
        }
        return result;
    }
    
    public double compute(Object... args) {
        //System.out.println("COMPUTING REAL WEATHER MODEL");
        return compute(clock.getDateTime(), args);
    }

    public double compute(Double d1, Double d2){
        return compute(clock.getDateTime());
    }

    /**
      * Write a file with 2 columns, time/value for ease in plotting the values.
      */
    public void writeToFile() throws Exception{
        Iterator<DateTime> i = dataMap.keySet().iterator();
        PrintStream ps = new PrintStream (new FileOutputStream(getName() + "."+clock.getDateTime()));
        DateTime tmp=null;
        for(; i.hasNext(); ){
            tmp = (DateTime)i.next();
            ps.println(tmp.toString()+"; "+dataMap.get(tmp));
        }
        
    }
    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println("Wrong number of arguements");
            System.out.println("Usage: RealWeatherModel wind_file opacity_file rms_file");
            System.out.println("Usage: RealWeatherModel wind_file opacity_file rms_file");
            System.out.println("");
            for(int i=0; i <  args.length; i++){
                System.out.println(args[i]);
            }
            return;
        }
        System.out.println("Creating Real Weather Model with weather files: ");
        System.out.println("#Wind File# "+args[0]);
        System.out.println("#Opacity File# "+ args[1]);
        System.out.println("#RMS File# "+ args[2]);
        
        try{
            RealWeatherModel m1 = new WindSpeedModel(args[0]);
            RealWeatherModel m2 = new OpacityModel(args[1]);
            RealWeatherModel m3 = new RmsModel(args[2]);
        
            m1.writeToFile();
            m2.writeToFile();
            m3.writeToFile();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
}

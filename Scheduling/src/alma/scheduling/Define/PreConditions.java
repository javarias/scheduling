package alma.scheduling.Define;

import java.lang.reflect.*;

public class PreConditions {

    private PreCond maxPWVC;
    private PreCond seeing;
    private PreCond phase;
    private PreCond windVel;
    private PreCond hourAngle;
//    private PreCond maxHA;

    public static String[] functionName;
    public static Method[] method;
    public static Object[] obj;

    public static void setFunctionNames(String[] function){
        functionName = new String [function.length];
		for (int i = 0; i < function.length; ++i){
			functionName[i] = function [i];
        }
    }
    public static String[] getFuntionNames(){
        return functionName;
    }

    public static void setMethods(Object[] o) {
		if (o.length != functionName.length)
			throw new IllegalArgumentException(
			"Expression initialization error!  The number of objects (" + 
			o.length + ") is not equal to the number of function names (" + 
			functionName.length + ").");
		method = new Method [functionName.length];
		obj = new Object [functionName.length];
		for (int i = 0; i < obj.length; ++i)
			obj[i] = o[i];
		Class classObj = null;
		for (int i = 0; i < functionName.length; ++i) {
			try {
				classObj = obj[i].getClass();
                if (classObj.getName().equals("alma.scheduling.PlanningModeSim.DiurnalModel")){
    				method[i] = classObj.getMethod("compute", Double.class);
                } else if (!classObj.getName().equals("alma.scheduling.PlanningModeSim.RmsModel")){
    				method[i] = classObj.getMethod("compute", Double.class, Double.class);
                } else { //so if we're doing RMS we have 3 doubles to pass in
    				method[i] = classObj.getMethod("compute", Double.class, Double.class, Double.class);
                }
			} catch (NoSuchMethodException err) {
                err.printStackTrace();
				System.out.println("Invalid syntax defining functions! " +
					"There is no \"compute()\" method in " + classObj.getName());
				System.exit(0);
			}
		}
	}
    
    ////////////////////////////////////////////////////
    
    public float execute(Object... args){
		try {
            float res=1.0F;
            int n;
            Double d;
            for(int i=0; i < functionName.length; i++){
                //get current value
               /* if(functionName[i].equals("rms")){
                    d =(Double)(method[i].invoke(obj[i], args));
                } else {
                    d =(Double)(method[i].invoke(obj[i], args[0], args[1]));
                }
                */
                //calculate weight
                
                if(functionName[i].equals("opacity")){
                    d =(Double)(method[i].invoke(obj[i], args[0], args[1]));
                    //System.out.println("Current value = "+d.doubleValue());
        		//	res = res * (float)(getWeight(functionName[i], d.doubleValue()));
        			res = (float)(getWeight(functionName[i], d.doubleValue()));
                }
            }
            return res;
		} catch (Exception err) {
			System.out.println("Oops! This isn't supposed to happen.");
			err.printStackTrace(System.out);
			System.exit(0);
		}
		return 0.0F;        
    }
    
    private static int methodLocation(String name) {
        name = name.toLowerCase();
        for(int i=0; i < functionName.length; i++){
            if(name.equals(functionName[i])){
                return i;
            }
        }
        return -1;
    }
    
    ////////////////////
    public double getWeight(String name, double cur) {
        double w=0.0;
     //   if(name.equals("pwvc")){
            //if(maxPWVC.getValue() > cur) {
          //      w = 0.25;
        //    } else {
           //     w=0.0;
         //   }
       // }else 
        if (name.equals("opacity")){
            System.out.println("Current opacity = "+cur);
            double limit= seeing.getMaxValue();
            System.out.println("limit opacity = "+limit);
            if(limit >= cur) {
                w=0.9;
            } else {
                w=0.0;
            }
        }//else if (name.equals("rms")){
        /*
            if(phase.getValue() > cur) {
                w = 0.25;
            } else {
                w=0.0;
            }
        }else if (name.equals("wind")){
            if(windVel.getValue() > cur) {
                w = 0.25;
            } else {
                w=0.0;
            }
            
        //}else if (name.equals("")){
        //}else if (name.equals("")){
        }
        */
        return w;
    }

    /////////////////////////////////////////    
    
    public PreConditions(){
        setDefaults();
    }

    public void setDefaults(){
        maxPWVC = new PreCond("pwvc",0.0, 0.0,"mm");
        seeing = new PreCond("opacity", 0.0,0.0,"arcsec");
        phase = new PreCond("rms", 0.0,0.0,"degrees");
        windVel= new PreCond("wind", 0.0,0.0,"km/s");
        //TODO make this into one coz they are already min/max...
        hourAngle= new PreCond("hourAngle", 0.0,0.0,"degrees");
        //maxHA= new PreCond("maxHA", 0.0,0.0,"degrees");
    }

    public void setMaxPWVC(double d1, double d2, String u){
        maxPWVC = new PreCond("pwvc",d1,d2,u);
    }
    public void setSeeing(double d1, double d2, String u){
        seeing = new PreCond("opacity",d1,d2,u);
    }
    public void setPhase(double d1, double d2, String u){
        phase = new PreCond("rms", d1,d2,u);
    }
    public void setWindVel(double d1, double d2, String u){
        windVel = new PreCond("wind",d1,d2,u);
    }
    public void setHourAngle(double d1, double d2, String u){
        hourAngle = new PreCond("hourAngle", d1,d2,u);
    }
    //public void setMaxHA(double d1, double d2, String u){
    //    maxHA = new PreCond("maxHA",d1,d2,u);
    //}

    public PreCond getMaxPWVC(){
        return maxPWVC;
    }
    public PreCond getSeeing(){
        return seeing;
    }
    public PreCond getPhase(){
        return phase;
    }
    public PreCond getWindVel(){
        return windVel;
    }
    public PreCond getHourAngle(){
        return hourAngle;
    }
    //public PreCond getMaxHA(){
    //    return maxHA;
   //}

    public String toString() {
        return "Max PWVC = "+maxPWVC.toString()+"; Seeing = "+seeing.toString()+"; Phase = "+
            phase.toString()+"; Wind Velocity = "+windVel.toString()+"; Hour Angle = "+
            hourAngle.toString();
    }

    class PreCond {
        String name;
        double minValue;
        double maxValue;
        String unit;
        public PreCond(String n, double v1, double v2, String u){
            name = n;
            minValue = v1;
            maxValue = v2;
            unit = u;
        }

        public String getName(){
            return name;
        }

        public double getMinValue(){
            return minValue;
        }
        public double getMaxValue(){
            return maxValue;
        }

        public String getUnit(){
            return unit;
        }

        public String toString(){
            return name+": "+minValue+" :: "+maxValue+" "+unit;
        }
    }
}

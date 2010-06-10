package alma.scheduling.datamodel.helpers;

public class TimeConverter2 {
   public static double toHours(double value, String unit) {
       double FACTOR = 1.0; // to seconds first
       if (unit.equals("ns")) {
               FACTOR = 1.0E-9;
       } else if (unit.equals("us")) {
               FACTOR = 1.0E-6;
       } else if (unit.equals("ms")) {
               FACTOR = 1.0E-3;
       } else if (unit.equals("s")) {
               FACTOR = 1.0;
       } else if (unit.equals("min")) {
               FACTOR = 60.0;
       } else if (unit.equals("h")) {
               FACTOR = 3600.0;
       }
       return FACTOR * value / 3600.0;
   }
}

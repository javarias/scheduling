package alma.scheduling.dataload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class WeatherDataReader extends BufferedReader {

    private static final String COMMENT_CHAR = "#";
    
    public final class WeatherData {
        private final double time;
        private final double value;
        private final double rms;
        private final double slope;
        
        public WeatherData(double time, double value, double rms, double slope) {
            this.time = time;
            this.value = value;
            this.rms = rms;
            this.slope = slope;
        }
        
        public double getTime() {
            return time;
        }
        
        public double getValue() {
            return value;
        }
        
        public double getRms() {
            return rms;
        }
        
        public double getSlope() {
            return slope;
        }
    }
        
    public WeatherDataReader(Reader reader) {
        super(reader);
    }
    
    public WeatherDataReader(Reader reader, int sz) {
        super(reader, sz);
    }
    
    public WeatherData getWeatherData() throws NumberFormatException, IOException {
        String line;
        while ((line = readLine()) != null) {
            if (line.startsWith(COMMENT_CHAR)) {
                continue;
            }
            line = line.trim();
            String[] tokens = line.split("\\s+");
            if (tokens.length != 4) {
                // fail("invalid source format");
            }
            double time = Double.valueOf(tokens[0]);
            double val = Double.valueOf(tokens[1]);
            double rms = Double.valueOf(tokens[2]);
            double slope = Double.valueOf(tokens[3]);            
            return new WeatherData(time, val, rms, slope);
        }
        return null;
    }
}

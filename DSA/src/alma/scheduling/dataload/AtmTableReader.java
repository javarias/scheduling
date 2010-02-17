package alma.scheduling.dataload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class AtmTableReader extends BufferedReader {

    private static final String COMMENT_CHAR = "#";
    
    public final class AtmData {
        private final double freq;
        private final double opacity;
        private final double temperature;

        public AtmData(double freq, double opacity, double temperature) {
            super();
            this.freq = freq;
            this.opacity = opacity;
            this.temperature = temperature;
        }

        public double getFreq() {
            return freq;
        }

        public double getOpacity() {
            return opacity;
        }

        public double getTemperature() {
            return temperature;
        }
    }
        
    public AtmTableReader(Reader reader) {
        super(reader);
    }
    
    public AtmTableReader(Reader reader, int sz) {
        super(reader, sz);
    }
    
    public AtmData getAtmData() throws NumberFormatException, IOException {
        String line;
        while ((line = readLine()) != null) {
            if (line.startsWith(COMMENT_CHAR)) {
                continue;
            }
            line = line.trim();
            String[] tokens = line.split("\\s+");
            if (tokens.length != 3) {
                // fail("invalid source format");
            }
            double freq = Double.valueOf(tokens[0]);
            double op = Double.valueOf(tokens[1]);
            double temp = Double.valueOf(tokens[2]);
            return new AtmData(freq, op, temp);
        }
        return null;
    }
}

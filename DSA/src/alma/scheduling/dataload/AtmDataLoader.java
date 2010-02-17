package alma.scheduling.dataload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import alma.scheduling.dataload.AtmTableReader.AtmData;
import alma.scheduling.datamodel.weather.AtmParameters;
import alma.scheduling.datamodel.weather.dao.AtmParametersDao;

public class AtmDataLoader implements DataLoader {

    protected String file;
    protected AtmParametersDao dao;
    protected int maxNumRecords;
    private AtmTableReader reader;
    private int count = 0;
    private double pwc;

    public AtmDataLoader() {
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setDao(AtmParametersDao dao) {
        this.dao = dao;
    }

    public void setMaxNumRecords(int maxNumRecords) {
        this.maxNumRecords = maxNumRecords;
    }

    public void setPwc(double pwc) {
        this.pwc = pwc;
    }
    
    private void createDataReader() throws FileNotFoundException {
        File file = new File(this.file);
        FileReader fr = new FileReader(file);
        reader = new AtmTableReader(fr);                
    }

    protected AtmData getNextAtmDatum() throws NumberFormatException,
            IOException {
        if (reader == null)
            createDataReader();
        if ((maxNumRecords >= 0) && (count++ > maxNumRecords)) {
            return null;
        }
        return reader.getAtmData();
    }

    @Override
    public void load() {
        try {
            AtmData ad;
            while ((ad = getNextAtmDatum()) != null) {
                AtmParameters params = new AtmParameters();
                params.setPWV(pwc);
                params.setFreq(ad.getFreq());
                params.setOpacity(ad.getOpacity());
                params.setAtmBrightnessTemp(ad.getTemperature());
                dao.loadAtmParameter(params);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
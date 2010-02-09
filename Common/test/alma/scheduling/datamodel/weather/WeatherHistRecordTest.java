package alma.scheduling.datamodel.weather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.persistence.HibernateUtil;

import junit.framework.TestCase;

public class WeatherHistRecordTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(WeatherHistRecordTest.class);
    private Session session;
    
    public WeatherHistRecordTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        session = HibernateUtil.getSessionFactory().openSession();
    }

    protected void tearDown() throws Exception {
        session.close();
        HibernateUtil.shutdown();        
        super.tearDown();
    }
 
    public void testSimpleRecordCreation() throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            TemperatureHistRecord record1 = new TemperatureHistRecord(1.0, 0.0, 0.0, 0.0);
            TemperatureHistRecord record2 = new TemperatureHistRecord(1.1, 0.0, 0.0, 0.0);
            TemperatureHistRecord record3 = new TemperatureHistRecord(1.2, 0.0, 0.0, 0.0);
            session.save(record1);
            session.save(record2);
            session.save(record3);
            HumidityHistRecord record4 = new HumidityHistRecord(1.0, 0.0, 0.0, 0.0);
            HumidityHistRecord record5 = new HumidityHistRecord(1.1, 0.0, 0.0, 0.0);
            HumidityHistRecord record6 = new HumidityHistRecord(1.2, 0.0, 0.0, 0.0);
            session.save(record4);
            session.save(record5);
            session.save(record6);
            // ...
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
        try {
            tx = session.beginTransaction();
            session.createQuery("DELETE FROM TemperatureHistRecord").executeUpdate();
            session.createQuery("DELETE FROM HumidityHistRecord").executeUpdate();
            tx.commit();            
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }
    
    public void noPleaseTestLotsOfRecords() throws Exception {
        Transaction tx = null;        
        try {
            tx = session.beginTransaction();
            Scanner scanner = new Scanner(new File("./data/Temp_good_15min_all.dat"));
            // skip two first comment lines
            scanner.skip("#.*\\n");
            scanner.skip("#.*\\n");
            scanner.useDelimiter("\\s+");
            while ( scanner.hasNext() ) {
                Double time = scanner.nextDouble();
                Double temp = scanner.nextDouble();
                Double rms = scanner.nextDouble();
                Double slope = scanner.nextDouble();
                WeatherHistRecord record = new TemperatureHistRecord(time, temp, rms, slope);
                session.save(record);
            }
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }
    
    public void noPleasetestLotsOfRecords2() throws Exception {
        Transaction tx = null;        
        try {
            tx = session.beginTransaction();
            File file = new File("./data/Temp_good_15min_all.dat");
            FileReader fr = new FileReader(file);
            BufferedReader in = new BufferedReader(fr);
            String commentToken = "#";
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith(commentToken)) {
                    continue;
                }
                line = line.trim();
                String[] tokens = line.split("\\s+");
                if (tokens.length != 4) {
                    fail("invalid source format");
                }
                Double time = Double.valueOf(tokens[0]);
                Double temp = Double.valueOf(tokens[1]);
                Double rms = Double.valueOf(tokens[2]);
                Double slope = Double.valueOf(tokens[3]);            
                WeatherHistRecord record = new TemperatureHistRecord(time, temp, rms, slope);
                session.save(record);
            }
            tx.commit();
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
        try {
            tx = session.beginTransaction();
            session.createQuery("DELETE FROM TemperatureHistRecord").executeUpdate();
            tx.commit();            
        } catch(Exception ex) {
            tx.rollback();
            throw ex;
        }
    }
}

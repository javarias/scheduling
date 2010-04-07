package alma.scheduling.datamodel.executive.dao;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.CannotParseDataException;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.input.executive.generated.ExecutiveData;

public class XmlExecutiveDaoImpl implements XmlExecutiveDAO {

    private static Logger logger = LoggerFactory.getLogger(XmlExecutiveDaoImpl.class);
    private boolean ready = false;

    private ArrayList<Executive> exec;
    private ArrayList<PI> pi;
    private ArrayList<ExecutivePercentage> ep;
    private ArrayList<ObservingSeason> os;
    private String pathToExecDataXML = null;

    private ConfigurationDao configDao;

    public XmlExecutiveDaoImpl() {
        exec = new ArrayList<Executive>();
        pi = new ArrayList<PI>();
        ep = new ArrayList<ExecutivePercentage>();
        os = new ArrayList<ObservingSeason>();
    }
    
    public XmlExecutiveDaoImpl(String pathToExecDataXML){
        this();
        logger.debug("Setting to read XML data from: " + pathToExecDataXML);
        this.pathToExecDataXML = pathToExecDataXML;
    }

    public ConfigurationDao getConfigDao() {
        return configDao;
    }
    


    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    private void processXMLProjectFiles() throws CannotParseDataException {
        if(pathToExecDataXML!=null){
            logger.info("Reading Executive XML data from: " + pathToExecDataXML);
            ExecutiveData execData = null;
            try {
                execData = ExecutiveData.unmarshalExecutiveData(
                        new FileReader(pathToExecDataXML));
                copyExecutiveFromXMLGenerated(execData, exec, pi, ep, os);
            } catch (MarshalException e) {
                throw new CannotParseDataException(pathToExecDataXML, e);
            } catch (ValidationException e) {
                throw new CannotParseDataException(pathToExecDataXML, e);
            } catch (FileNotFoundException e) {
                throw new CannotParseDataException(pathToExecDataXML, e);
            }
            
        }
        else{
            logger.info("Reading configuration");
            List<String> files = configDao.getConfiguration().getExecutiveFiles();
            // TODO: Validate against XML schema files
            for (Iterator<String> iter = files.iterator(); iter.hasNext();) {
                String file = iter.next();
                logger.info("Reading Executive XML data from: " + file);
                ExecutiveData execData = null;
                try {
                    execData = ExecutiveData.unmarshalExecutiveData(
                            new FileReader(file));
                    copyExecutiveFromXMLGenerated(execData, exec, pi, ep, os);
                } catch (MarshalException e) {
                    throw new CannotParseDataException(file, e);
                } catch (ValidationException e) {
                    throw new CannotParseDataException(file, e);
                } catch (FileNotFoundException e) {
                    throw new CannotParseDataException(file, e);
                }
            }
        }
    }

    private void copyExecutiveFromXMLGenerated(
            alma.scheduling.input.executive.generated.ExecutiveData data,
            List<Executive> execOut, List<PI> piOut,
            List<ExecutivePercentage> epOut, List<ObservingSeason> osOut) {
        XmlExecutiveDaoImpl xmlExDao = new XmlExecutiveDaoImpl();
        if (data == null)
            throw new NullPointerException(
                    "Executive data input cannot be null");
        if (execOut == null)
            throw new NullPointerException("execOut cannot be null");
        if (piOut == null)
            throw new NullPointerException("piOut cannot be null");
        if (epOut == null)
            throw new NullPointerException("epOut cannot be null");
        if (osOut == null)
            throw new NullPointerException("osOut cannot be null");

        HashMap<String, Executive> exec = new HashMap<String, Executive>();
        HashMap<String, ExecutivePercentage> ep = new HashMap<String, ExecutivePercentage>();
        HashMap<String, ObservingSeason> os = new HashMap<String, ObservingSeason>();

        for (int i = 0; i < data.getExecutivePercentageCount(); i++) {
            ep.put(data.getExecutivePercentage(i).getId(), xmlExDao
                    .copyExecutivePercentage(data.getExecutivePercentage(i)));
        }
        for (int i = 0; i < data.getObservingSeasonCount(); i++) {
            ObservingSeason tmp = xmlExDao.copyObservingSeason(data
                    .getObservingSeason(i));
            os.put(data.getObservingSeason(i).getId(), tmp);
            for (int j = 0; j < data.getObservingSeason(i)
                    .getExecutivePercentageRefCount(); j++) {
                ExecutivePercentage epTmp = ep.get(data.getObservingSeason(i)
                        .getExecutivePercentageRef(j).getIdRef());
                tmp.getExecutivePercentage().add(epTmp);
            }
        }
        for (int i = 0; i < data.getExecutiveCount(); i++) {
            Executive tmp = xmlExDao.copyExecutive(data.getExecutive(i));
            exec.put(data.getExecutive(i).getName(), tmp);
            for (int j = 0; j < data.getExecutive(i)
                    .getExecutivePercentageRefCount(); j++) {
                ExecutivePercentage epTmp = ep.get(data.getExecutive(i)
                        .getExecutivePercentageRef(j).getIdRef());
                tmp.getExecutivePercentage().add(epTmp);
            }
        }

        for (int i = 0; i < data.getPICount(); i++) {
            PI tmp = xmlExDao.copyPI(data.getPI(i), exec);
            piOut.add(tmp);
        }

        execOut.addAll(exec.values());
        epOut.addAll(ep.values());
        osOut.addAll(os.values());

    }

    private Executive copyExecutive(
            alma.scheduling.input.executive.generated.Executive in) {
        Executive exec = new Executive();
        exec.setName(in.getName());
        exec.setDefaultPercentage(in.getDefaultPercentage());
        if (exec.getExecutivePercentage() == null)
            exec.setExecutivePercentage(new HashSet<ExecutivePercentage>());
        return exec;
    }

    private ExecutivePercentage copyExecutivePercentage(
            alma.scheduling.input.executive.generated.ExecutivePercentage in) {
        ExecutivePercentage execp = new ExecutivePercentage();
        execp.setPercentage(in.getPercentage());
        execp.setTotalObsTimeForSeason(new Double(in.getTotalObsTimeForSeason()));
        return execp;
    }

    private ObservingSeason copyObservingSeason(
            alma.scheduling.input.executive.generated.ObservingSeason in) {
        ObservingSeason os = new ObservingSeason();
        os.setEndDate(in.getEndDate().toDate());
        os.setName(in.getName());
        os.setStartDate(in.getStartDate().toDate());
        if (os.getExecutivePercentage() == null)
            os.setExecutivePercentage(new HashSet<ExecutivePercentage>());
        return os;
    }

    private PI copyPI(alma.scheduling.input.executive.generated.PI in,
            HashMap<String, Executive> execs) {
        PI pi = new PI();
        pi.setName(in.getName());
        if (pi.getPIMembership() == null)
            pi.setPIMembership(new HashSet<PIMembership>());
        for (int i = 0; i < in.getPIMembershipCount(); i++) {
            Executive e = execs.get(in.getPIMembership(i).getExecutiveRef()
                    .getNameRef());
            PIMembership pim = copyPIMembership(in.getPIMembership(i));
            pim.setExecutive(e);
            pi.getPIMembership().add(pim);
        }
        return pi;
    }

    private PIMembership copyPIMembership(
            alma.scheduling.input.executive.generated.PIMembership in) {
        PIMembership pim = new PIMembership();
        pim.setMembershipPercentage(pim.getMembershipPercentage());
        return pim;
    }

    @Override
    public List<Executive> getAllExecutive() {
        if(!ready){
            try {
                processXMLProjectFiles();
                ready = true;
            } catch (CannotParseDataException e) {
                e.printStackTrace();
            }
        }
        return exec;
    }

    @Override
    public List<ObservingSeason> getAllObservingSeason() {
        if(!ready){
            try {
                processXMLProjectFiles();
                ready = true;
            } catch (CannotParseDataException e) {
                e.printStackTrace();
            }
        }        
        return os;
    }

    @Override
    public List<PI> getAllPi() {
        if(!ready){
            try {
                processXMLProjectFiles();
                ready = true;
            } catch (CannotParseDataException e) {
                e.printStackTrace();
            }
        }        
        return pi;
    }

    @Override
    public ObservingSeason getCurrentSeason() {
        if(!ready){
            try {
                processXMLProjectFiles();
                ready = true;
            } catch (CannotParseDataException e) {
                e.printStackTrace();
            }
        }
        ObservingSeason[] osArray = new ObservingSeason[os.size()];
        os.toArray(osArray);
        Arrays.sort(osArray);
        return osArray[0];
    }

    @Override
    public Executive getExecutive(String piName) {
        //TODO: implement
        return null;
    }

    @Override
    public ExecutivePercentage getExecutivePercentage(Executive ex,
            ObservingSeason os) {
        //TODO: implement
        return null;
    }

    @Override
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex,
            ObservingSeason os) {
        //TODO: implement
        return null;
    }

}

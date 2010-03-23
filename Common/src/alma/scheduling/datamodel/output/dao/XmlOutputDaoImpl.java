package alma.scheduling.datamodel.output.dao;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.output.Affiliation;
import alma.scheduling.datamodel.output.Array;
import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.Results;
import alma.scheduling.datamodel.output.SchedBlockResult;


/**
 * Export to XML output file
 * 
 * This class is not thread safe
 * 
 * @author javarias
 *
 */
public class XmlOutputDaoImpl implements OutputDao {

    private ConfigurationDao configDao;
    private Configuration config;
    private FileWriter fw = null;
    
    public ConfigurationDao getConfigDao() {
        return configDao;
    }

    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    @Override
    public List<Results> getResults() {
        //alma.scheduling.output.generated.Results r;
        //Configuration config = configDao.getConfiguration();
        //config.getOutputDirectory();
        return null;
    }

    @Override
    public void saveResults(Results results) {
        // TODO Auto-generated method stub
        alma.scheduling.output.generated.Results r = 
            new alma.scheduling.output.generated.Results();
        r.setAvailableTime(results.getAvailableTime());
        r.setMaintenanceTime(results.getMaintenanceTime());
        r.setOperationTime(results.getOperationTime());
        r.setScientificTime(results.getScientificTime());
        //set the observation projects
        alma.scheduling.output.generated.ObservationProject op[] =
            new alma.scheduling.output.generated.ObservationProject[results.getObservationProject().size()];
        Iterator<ObservationProject> itOp = results.getObservationProject().iterator();
        r.setObservationProject(op);
      	
        for(int i = 0; i < op.length; i++){
            ObservationProject tmpOp = itOp.next();
        	op[i] = new alma.scheduling.output.generated.ObservationProject();
            op[i].setExecutionTime(tmpOp.getExecutionTime());
            op[i].setScienceRating((int) tmpOp.getScienceRating());
            //TODO: Fix this presseted status. Using valueof() method returns null pointer.
            op[i].setStatus( alma.scheduling.output.generated.types.ExecutionStatus.COMPLETE );
            //set the affiliations
            alma.scheduling.output.generated.Affiliation aff[] = 
                new alma.scheduling.output.generated.Affiliation[tmpOp.getAffiliation().size()];
            Iterator<Affiliation> itAff = tmpOp.getAffiliation().iterator();
            op[i].setAffiliation(aff);
            for(int j = 0; j < aff.length; j++){
                Affiliation tmpAff = itAff.next();
                aff[j].setExecutive(tmpAff.getExecutive());
                aff[j].setPercentage(tmpAff.getPercentage());
            }
            //set the sched blocks results
            alma.scheduling.output.generated.SchedBlock sb[] = 
                new alma.scheduling.output.generated.SchedBlock[tmpOp.getSchedBlock().size()];
            Iterator<SchedBlockResult> itSb = tmpOp.getSchedBlock().iterator();
            op[i].setSchedBlock(sb);
            for(int j = 0; j < sb.length; j++){
                SchedBlockResult tmpSb = itSb.next();
                sb[j].setEndDate(new org.exolab.castor.types.Date(tmpSb.getEndDate()));
                sb[j].setExecutionTime(tmpSb.getExecutionTime());
                sb[j].setMode(tmpSb.getMode());
                sb[j].setStartDate(new org.exolab.castor.types.Date(tmpSb.getStartDate()));
                sb[j].setStatus(alma.scheduling.output.generated.types.ExecutionStatus.valueOf(
                        tmpSb.getStatus().name()));
                sb[j].setType(tmpSb.getType());  
                alma.scheduling.output.generated.ArrayRef aRef= 
                    new alma.scheduling.output.generated.ArrayRef();
                aRef.setArrayRef(Integer.toString(tmpSb.getArrayRef().hashCode()));
                sb[j].setArrayRef(aRef);
            }
        }
        
        //set the Arrays
        alma.scheduling.output.generated.Array a[] =
            new alma.scheduling.output.generated.Array[results.getArray().size()];
        Iterator<Array> itA = results.getArray().iterator();
        r.setArray(a);
        for(int i = 0; i < a.length; i++){
            Array tmpA = itA.next();
            a[i].setAvailableTime(tmpA.getAvailableTime());
            a[i].setBaseline(tmpA.getBaseline());
            a[i].setCreationDate(new org.exolab.castor.types.Date(tmpA.getCreationDate()));
            a[i].setDeletionDate(new org.exolab.castor.types.Date(tmpA.getDeletionDate()));
            a[i].setMaintenanceTime(tmpA.getMaintenanceTime());
            a[i].setScientificTime(tmpA.getScientificTime());
            a[i].setArrayID(Integer.toString(tmpA.hashCode()));
        }
        if (fw == null){
            config = configDao.getConfiguration();
            Date d = new Date();
            String pathToFile = config.getWorkDirectory() + "/" + config.getOutputDirectory() 
            + "/" + "output_" + d.getTime() + ".xml";
            try {
                fw = new FileWriter(pathToFile);
                r.marshal(fw);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MarshalException e) {
                e.printStackTrace();
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            finally{
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            try {
                r.marshal(fw);
            } catch (MarshalException e) {
                e.printStackTrace();
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveResults(Collection<Results> results) {
        config = configDao.getConfiguration();
        Date d = new Date();
        String pathToFile = config.getWorkDirectory() + "/" + config.getOutputDirectory() 
        + "/" + "output_" + d.getTime() + ".xml";
        try {
            fw = new FileWriter(pathToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Results r: results){
            saveResults(r);
        }
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fw = null;
    }

}

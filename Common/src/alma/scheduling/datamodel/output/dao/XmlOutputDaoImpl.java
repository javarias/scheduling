/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
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
    public void saveResults(Results results) {
        // TODO Auto-generated method stub
        alma.scheduling.output.generated.Results r = 
            new alma.scheduling.output.generated.Results();
        r.setAvailableTime(results.getAvailableTime());
        r.setMaintenanceTime(results.getMaintenanceTime());
        r.setOperationTime(results.getOperationTime());
        r.setScientificTime(results.getScientificTime());
        r.setObsSeasonEnd(results.getObsSeasonEnd());
        r.setObsSeasonStart(results.getObsSeasonStart());
        r.setStartSimDate(results.getStartSimDate());
        r.setStopSimDate(results.getStartSimDate());
        r.setStartRealDate(results.getStartRealDate());
        r.setStopRealDate(results.getStopRealDate());
        
        //set the observation projects
        alma.scheduling.output.generated.ObservationProject op[] =
            new alma.scheduling.output.generated.ObservationProject[results.getObservationProject().size()];
        Iterator<ObservationProject> itOp = results.getObservationProject().iterator();
              	
        for(int i = 0; i < op.length; i++){
            ObservationProject tmpOp = itOp.next();
        	op[i] = new alma.scheduling.output.generated.ObservationProject();
            op[i].setExecutionTime(tmpOp.getExecutionTime());
            op[i].setScienceRank( tmpOp.getScienceRank());
            op[i].setScienceScore( tmpOp.getScienceScore());
            op[i].setId(tmpOp.getId());
            op[i].setOriginalId(tmpOp.getOriginalId());
            //TODO: Fix this presseted status. Using valueof() method returns null pointer.
            op[i].setStatus( alma.scheduling.output.generated.types.ExecutionStatus.valueOf( tmpOp.getStatus().toString() ) );
            
            // Setting affiliations
            alma.scheduling.output.generated.Affiliation aff[] = 
                new alma.scheduling.output.generated.Affiliation[tmpOp.getAffiliation().size()];
            Iterator<Affiliation> itAff = tmpOp.getAffiliation().iterator();
            for(int j = 0; j < aff.length; j++){
                Affiliation tmpAff = itAff.next();
                aff[j] = new alma.scheduling.output.generated.Affiliation();
                aff[j].setExecutive(tmpAff.getExecutive());
                aff[j].setPercentage(tmpAff.getPercentage());
            }
            op[i].setAffiliation(aff);
            
            //set the sched blocks results
            alma.scheduling.output.generated.SchedBlock sb[] = 
                new alma.scheduling.output.generated.SchedBlock[tmpOp.getSchedBlock().size()];
            Iterator<SchedBlockResult> itSb = tmpOp.getSchedBlock().iterator();
            for(int j = 0; j < sb.length; j++){
                SchedBlockResult tmpSb = itSb.next();
                sb[j] = new alma.scheduling.output.generated.SchedBlock();
                sb[j].setEndDate(tmpSb.getEndDate());
                sb[j].setExecutionTime(tmpSb.getExecutionTime());
                sb[j].setId(tmpSb.getId()); 
                sb[j].setOriginalId(tmpSb.getOriginalId());
                sb[j].setMode(tmpSb.getMode());
                sb[j].setRepresentativeFrequency(tmpSb.getRepresentativeFrequency());
                sb[j].setGoalSensitivity(tmpSb.getGoalSensitivity());
                sb[j].setAchievedSensitivity(tmpSb.getAchievedSensitivity());
                sb[j].setStartDate(tmpSb.getStartDate());
                sb[j].setStatus(alma.scheduling.output.generated.types.ExecutionStatus.valueOf(
                        tmpSb.getStatus().name()));
                sb[j].setType(tmpSb.getType());  
                alma.scheduling.output.generated.ArrayRef aRef = 
                    new alma.scheduling.output.generated.ArrayRef();
//                aRef.setArrayRef(Long.toString(tmpSb.getArrayRef().getId())); //TODO:Fix references
                sb[j].setArrayRef(aRef);
            }
            op[i].setSchedBlock(sb);
        }
        
        r.setObservationProject(op);
        
        //set the Arrays
        alma.scheduling.output.generated.Array a[] =
            new alma.scheduling.output.generated.Array[results.getArray().size()];
        Iterator<Array> itA = results.getArray().iterator();
        for(int i = 0; i < a.length; i++){
            Array tmpA = itA.next();
            a[i] = new alma.scheduling.output.generated.Array();
            a[i].setAvailablelTime( tmpA.getAvailableTime());
            a[i].setCreationDate(tmpA.getCreationDate());
            a[i].setDeletionDate(tmpA.getDeletionDate());
            // Id needs to be a string, as it is used for XPath reference check in XML schema.
 //           a[i].setOriginalId( tmpA.getId() ); //TODO: Fix references
            a[i].setMaintenanceTime(tmpA.getMaintenanceTime());
            a[i].setScientificTime(tmpA.getScientificTime());
            a[i].setResolution( tmpA.getResolution());
            a[i].setUvCoverage( tmpA.getUvCoverage());
        }
        r.setArray(a);
        
        if (fw == null){
            config = configDao.getConfiguration();
            String pathToFile = config.getWorkDirectory() + "/" + 
            					config.getOutputDirectory() + "/" + 
            					"output_" + 
            					results.getStartRealDate().getTime() + 
            					".xml";
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

    @Override
    public void deleteAll() {
        // TODO Do nothing?
        
    }

	@Override
	public Results getResult(long id) {
		return null;
	}

	@Override
	public Results getLastResult() {
		return null;
	}
	
    @Override
    public List<Results> getResults() {
        //alma.scheduling.output.generated.Results r;
        //Configuration config = configDao.getConfiguration();
        //config.getOutputDirectory();
        return null;
    }

	@Override
	public long getLastResultId() {
		// TODO Auto-generated method stub
		return 0;
	}

}

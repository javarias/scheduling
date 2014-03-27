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

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.obsproject.ArrayType;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.output.Affiliation;
import alma.scheduling.datamodel.output.Array;
import alma.scheduling.datamodel.output.ExecutionStatus;
import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.SimulationResults;
import alma.scheduling.datamodel.output.SchedBlockResult;
import alma.scheduling.output.generated.Results;
import alma.scheduling.output.generated.SkyCoordinatesT;


/**
 * Export to XML output file
 * 
 * This class is not thread safe
 * 
 * @author javarias
 *
 */
public class XmlOutputDaoImpl implements StreamBasedOutputDao {

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
    @Transactional(readOnly=true)
    public void saveResults(SimulationResults results) {
        Results r = prepareOutput(results);
        
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
    public void saveResults(Collection<SimulationResults> results) {
        config = configDao.getConfiguration();
        Date d = new Date();
        String pathToFile = config.getWorkDirectory() + "/" + config.getOutputDirectory() 
        + "/" + "output_" + d.getTime() + ".xml";
        try {
            fw = new FileWriter(pathToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(SimulationResults r: results){
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
	public SimulationResults getResult(long id) {
		return null;
	}

	@Override
	public SimulationResults getLastResult() {
		return null;
	}
	
    @Override
    public List<SimulationResults> getResults() {
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

	@Override
	public List<InputStream> getResultsAsStream(
			Collection<SimulationResults> results) {
		List<InputStream> ret =  new ArrayList<InputStream>(results.size());
		for(SimulationResults sr: results) {
			Results r = prepareOutput(sr);
			Writer writer = new CharArrayWriter();
			try {
				r.marshal(writer);
				InputStream is = new ByteArrayInputStream(writer.toString().getBytes());
				ret.add(is);
			} catch (MarshalException e) {
				e.printStackTrace();
				continue;
			} catch (ValidationException e) {
				e.printStackTrace();
				continue;
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					//Nothing to do
				}
			}
		}
		return ret;
	}

	@Override
	public List<SimulationResults> loadResults(List<InputStream> streams) {
		List<SimulationResults> simResults = new ArrayList<SimulationResults>(streams.size());
		for(InputStream is: streams) {
			Reader reader = new InputStreamReader(is);
			try {
				Results r = Results.unmarshalResults(reader);
				simResults.add(loadfromOutput(r));
			} catch (MarshalException e) {
				e.printStackTrace();
				continue;
			} catch (ValidationException e) {
				e.printStackTrace();
				continue;
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					//Nothing to do
				}
			}
		}
		return simResults;
	}
	
    private Results prepareOutput (SimulationResults results) {
    	alma.scheduling.output.generated.Results r = 
                new alma.scheduling.output.generated.Results();
            r.setName(results.getName());
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
            
            //set the Arrays
            {
            alma.scheduling.output.generated.Array a[] =
                new alma.scheduling.output.generated.Array[results.getArray().size()];
            int i = 0;
            for(Array tmpA: results.getArray()){
                a[i] = new alma.scheduling.output.generated.Array();
            	a[i].setId(new Long(i).toString());
            	tmpA.setId(i);
                a[i].setAvailablelTime( tmpA.getAvailableTime());
                a[i].setCreationDate(tmpA.getCreationDate());
                a[i].setDeletionDate(tmpA.getDeletionDate());
                // Id needs to be a string, as it is used for XPath reference check in XML schema.
                a[i].setOriginalId(i);
                a[i].setMaintenanceTime(tmpA.getMaintenanceTime());
                a[i].setScientificTime(tmpA.getScientificTime());
                a[i].setResolution(tmpA.getResolution());
                a[i].setUvCoverage(tmpA.getUvCoverage());
                a[i].setMinBaseline(tmpA.getMinBaseline());
                a[i].setMaxBaseline(tmpA.getMaxBaseline());
                a[i].setConfigurationName(tmpA.getConfigurationName());
                a[i].setType(tmpA.getType().toString());
                i++;
            }
            r.setArray(a);
            }
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
                op[i].setArchive_uid(tmpOp.getArchiveUid());
                op[i].setCode(tmpOp.getCode());
                //TODO: Fix this pre-set status. Using valueof() method returns null pointer.
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
                    //TODO: Missing ALMA UID
                    sb[j].setMode(tmpSb.getMode());
                    sb[j].setRepresentativeFrequency(tmpSb.getRepresentativeFrequency());
                    sb[j].setGoalSensitivity(tmpSb.getGoalSensitivity());
                    sb[j].setAchievedSensitivity(tmpSb.getAchievedSensitivity());
                    sb[j].setStartDate(tmpSb.getStartDate());
                    sb[j].setStatus(alma.scheduling.output.generated.types.ExecutionStatus.valueOf(
                            tmpSb.getStatus().name()));
                    sb[j].setType(tmpSb.getType());
                    sb[j].setRepresentativeBand((tmpSb.getRepresentativeBand() != null)?tmpSb.getRepresentativeBand().shortValue():0);
                    SkyCoordinatesT source = new SkyCoordinatesT();
                    source.setRA(tmpSb.getRepresentativeSource().getRA());
                    source.setDec(tmpSb.getRepresentativeSource().getDec());
                    sb[j].setSource(source);
                    alma.scheduling.output.generated.ArrayRef aRef = 
                        new alma.scheduling.output.generated.ArrayRef();
                    aRef.setArrayRef(Long.toString(tmpSb.getArrayRef().getId()));
                    sb[j].setArrayRef(aRef);
                }
                op[i].setSchedBlock(sb);
            }
            
            r.setObservationProject(op);
            
            return r;
    }
    
    private SimulationResults loadfromOutput(Results output) {
    	SimulationResults sr = new SimulationResults();
    	sr.setName(output.getName());
    	sr.setAvailableTime(output.getAvailableTime());
    	sr.setMaintenanceTime(output.getMaintenanceTime());
        sr.setMaintenanceTime(output.getMaintenanceTime());
        sr.setOperationTime(output.getOperationTime());
        sr.setScientificTime(output.getScientificTime());
        sr.setObsSeasonEnd(output.getObsSeasonEnd());
        sr.setObsSeasonStart(output.getObsSeasonStart());
        sr.setStartSimDate(output.getStartSimDate());
        sr.setStopSimDate(output.getStartSimDate());
        sr.setStartRealDate(output.getStartRealDate());
        sr.setStopRealDate(output.getStopRealDate());
        
        //set the Arrays
        HashMap<String, Array> arrayMap = new HashMap<String, Array>();
        for(alma.scheduling.output.generated.Array xmlArray: output.getArray()){
            Array tmpArray = new Array();
            arrayMap.put(xmlArray.getId(), tmpArray);
            tmpArray.setAvailableTime(xmlArray.getAvailablelTime());
            tmpArray.setCreationDate(xmlArray.getCreationDate());
            tmpArray.setDeletionDate(xmlArray.getDeletionDate());
            tmpArray.setMaintenanceTime(xmlArray.getMaintenanceTime());
            tmpArray.setScientificTime(xmlArray.getScientificTime());
            tmpArray.setResolution( xmlArray.getResolution());
            tmpArray.setUvCoverage( xmlArray.getUvCoverage());
            tmpArray.setMinBaseline(xmlArray.getMinBaseline());
            tmpArray.setMaxBaseline(xmlArray.getMaxBaseline());
            tmpArray.setConfigurationName(xmlArray.getConfigurationName());
            tmpArray.setType(ArrayType.valueOf(xmlArray.getType()));
        }
        sr.setArray(new HashSet<Array>(arrayMap.values()));
        
         //set the observation projects
         HashSet<ObservationProject> opSet = new HashSet<ObservationProject>();
         for(alma.scheduling.output.generated.ObservationProject xmlOp: output.getObservationProject()){
            ObservationProject tmpOp = new ObservationProject();
            opSet.add(tmpOp);
            tmpOp.setExecutionTime(xmlOp.getExecutionTime());
            tmpOp.setScienceRank( xmlOp.getScienceRank());
            tmpOp.setScienceScore( xmlOp.getScienceScore());
            tmpOp.setOriginalId(xmlOp.getOriginalId());
            tmpOp.setStatus(ExecutionStatus.valueOf(xmlOp.getStatus().toString()));
             
             // Setting affiliations
            HashSet<Affiliation> affSet = new HashSet<Affiliation>();
            for(alma.scheduling.output.generated.Affiliation xmlAff: xmlOp.getAffiliation()){
				Affiliation aff = new Affiliation();
				affSet.add(aff);
				aff.setExecutive(xmlAff.getExecutive());
				aff.setPercentage(xmlAff.getPercentage());
             }
             tmpOp.setAffiliation(affSet);
             
             //set the sched blocks results
             HashSet<SchedBlockResult> sbSet = new HashSet<SchedBlockResult>();
             for(alma.scheduling.output.generated.SchedBlock xmlSb: xmlOp.getSchedBlock()){
                 SchedBlockResult tmpSb = new SchedBlockResult();
                 sbSet.add(tmpSb);
                 tmpSb.setEndDate(xmlSb.getEndDate());
                 tmpSb.setExecutionTime(xmlSb.getExecutionTime());
                 tmpSb.setId(xmlSb.getId());
                 tmpSb.setOriginalId(xmlSb.getOriginalId());
                 tmpSb.setMode(xmlSb.getMode());
                 tmpSb.setRepresentativeFrequency(xmlSb.getRepresentativeFrequency());
                 tmpSb.setGoalSensitivity(xmlSb.getGoalSensitivity());
                 tmpSb.setAchievedSensitivity(xmlSb.getAchievedSensitivity());
                 tmpSb.setStartDate(xmlSb.getStartDate());
                 tmpSb.setStatus(ExecutionStatus.valueOf(xmlSb.getStatus().toString()));
                 tmpSb.setType(xmlSb.getType());
                 tmpSb.setRepresentativeBand(new Short(xmlSb.getRepresentativeBand()).intValue());
                 SkyCoordinates source = new SkyCoordinates();
                 source.setRA(xmlSb.getSource().getRA());
                 source.setDec(xmlSb.getSource().getDec());
                 tmpSb.setRepresentativeSource((source));
                 tmpSb.setArrayRef(arrayMap.get(xmlSb.getArrayRef().getArrayRef()));
             }
             tmpOp.setSchedBlock(sbSet);
         }
         
         sr.setObservationProject(opSet);
         
         return sr;
    }

}

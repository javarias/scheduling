/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */
package alma.scheduling.datamodel.observatory.dao;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.observatory.Antenna;
import alma.scheduling.datamodel.observatory.AntennaInstallation;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.AssemblyContainer;
import alma.scheduling.datamodel.observatory.AssemblyContainerOperation;
import alma.scheduling.datamodel.observatory.AssemblyGroupType;
import alma.scheduling.datamodel.observatory.AssemblyOperation;
import alma.scheduling.datamodel.observatory.Pad;
import alma.scheduling.datamodel.observatory.Receiver;
import alma.scheduling.datamodel.observatory.ReceiverBand;
import alma.scheduling.datamodel.observatory.TelescopeEquipment;
import alma.scheduling.input.observatory.generated.AntennaInstallationT;
import alma.scheduling.input.observatory.generated.AntennaT;
import alma.scheduling.input.observatory.generated.ArrayConfigurationLiteT;
import alma.scheduling.input.observatory.generated.ArrayConfigurationT;
import alma.scheduling.input.observatory.generated.AssemblyContainerOperationT;
import alma.scheduling.input.observatory.generated.EquipmentT;
import alma.scheduling.input.observatory.generated.FrontEndT;
import alma.scheduling.input.observatory.generated.ObservatoryCharacteristics;
import alma.scheduling.input.observatory.generated.PadT;
import alma.scheduling.input.observatory.generated.ReceiverT;

/**
 * XML DAO for Observatory Characteristics.
 *
 */
public class XmlObservatoryDaoImpl implements XmlObservatoryDao {

    private static Logger logger = LoggerFactory.getLogger(XmlObservatoryDaoImpl.class);

    private Map<String, TelescopeEquipment> equipments;
    
    private List<ArrayConfiguration> arrayConfigurations; 
    
    // --- Spring managed properties ---
    
    private ConfigurationDao configurationDao;
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    // --- Ctors ---
    
    public XmlObservatoryDaoImpl() { }
    
    // --- ObservatoryDao impl ---
    
    @Override
    public List<ArrayConfiguration> getAllArrayConfigurations() {
        if (arrayConfigurations == null)
            process();
        return arrayConfigurations;
    }

    @Override
    public List<TelescopeEquipment> getAllEquipments() {
        if (equipments == null)
            process();
        return new ArrayList<TelescopeEquipment>(equipments.values());
    }

    // --- Internal functions ---
    
    private void process() {
        equipments = new HashMap<String, TelescopeEquipment>();
        arrayConfigurations = new ArrayList<ArrayConfiguration>();
        
        List<String> files = configurationDao.getConfiguration().getObservatoryFiles();
        for (Iterator<String> iter = files.iterator(); iter.hasNext();) {
            String file = iter.next();
            logger.debug("observatory configuration file = " + file);
            try {
                // process the TelescopeEquipment section
                ObservatoryCharacteristics xmlRoot =
                    ObservatoryCharacteristics.unmarshalObservatoryCharacteristics(new FileReader(file));
                AntennaT[] xmlAnts = xmlRoot.getTelescopeEquipment().getAntenna();
                for (AntennaT xmlAnt : xmlAnts) {
                    Antenna ant = createAntenna(xmlAnt);
                    equipments.put(xmlAnt.getId(), ant);
                }
                PadT[] xmlPads = xmlRoot.getTelescopeEquipment().getPad();
                for (PadT xmlPad : xmlPads) {
                    Pad pad = createPad(xmlPad);
                    equipments.put(xmlPad.getId(), pad);
                }
                EquipmentT[] xmlEqs = xmlRoot.getTelescopeEquipment().getEquipment();
                for (EquipmentT xmlEq : xmlEqs) {
                    logger.debug("equipment id = " + xmlEq.getId());
                    TelescopeEquipment eq = new TelescopeEquipment();
                    fillEquipmentData(eq, xmlEq);
                    equipments.put(xmlEq.getId(), eq);
                }
                // process the EquipmentOperation part
                AssemblyContainerOperationT[] xmlOps = xmlRoot.getEquipmentOperation().getOperation();
                for (AssemblyContainerOperationT xmlOp : xmlOps) {
                    AssemblyContainerOperation op = createOperation(xmlOp);
                    Set<AssemblyContainer> acs = new HashSet<AssemblyContainer>(equipments.values());
                    AssemblyContainer ac = searchEquipment(op.getAssemblyGroup(), acs);
                    TelescopeEquipment targetEquip = (TelescopeEquipment) ac;
                    if (targetEquip == null)
                        throw new NullPointerException("null target equipment in operation: " +
                                op.getAssemblyGroup() + " not found");
                    targetEquip.getOperations().add(op);
                }
                // finally process the ArrayConfigurations
                if (xmlRoot.getObservatoryCharacteristicsChoice().getArray() != null) {
                ArrayConfigurationT[] xmlACs = xmlRoot.getObservatoryCharacteristicsChoice().getArray().getArrayConfiguration();
	                for (ArrayConfigurationT xmlAC : xmlACs) {
	                    ArrayConfiguration ac = new ArrayConfiguration();
	                    ac.setStartTime(xmlAC.getStartTime());
	                    ac.setEndTime(xmlAC.getEndTime());
	                    ac.setResolution(xmlAC.getResolution());
	                    ac.setUvCoverage(xmlAC.getUvCoverage());
	                    AntennaInstallationT[] xmlAIs = xmlAC.getAntennaInstallation();
	                    for (AntennaInstallationT xmlAI : xmlAIs) {
	                        AntennaInstallation ai = new AntennaInstallation();
	                        ai.setStartTime(xmlAI.getStartTime());
	                        ai.setEndTime(xmlAI.getEndTime());
	                        Set<AssemblyContainer> acs = new HashSet<AssemblyContainer>(equipments.values());
	                        Antenna a = (Antenna) searchEquipment(xmlAI.getAntenna(), acs);
	                        ai.setAntenna(a);
	                        Pad p = (Pad) searchEquipment(xmlAI.getPad(), acs);
	                        ai.setPad(p);
	                        ac.getAntennaInstallations().add(ai);
	                    }
	                    arrayConfigurations.add(ac);
	                }
                }
                else if (xmlRoot.getObservatoryCharacteristicsChoice().getArrayLite() != null) {
                	ArrayConfigurationLiteT[] xmlACs = xmlRoot.getObservatoryCharacteristicsChoice().getArrayLite().getArrayConfiguration();
                	for (ArrayConfigurationLiteT xmlAC: xmlACs) {
                		ArrayConfiguration ac = new ArrayConfiguration();
                		ac.setStartTime(xmlAC.getStartTime());
                		ac.setEndTime(xmlAC.getEndTime());
                		ac.setConfigurationName(xmlAC.getConfigurationName());
                		ac.setArrayName(xmlAC.getArrayName());
                		ac.setNumberOfAntennas(xmlAC.getNumberOfAntennas());
                		ac.setMinBaseline(xmlAC.getMinBaseLine());
                		ac.setMaxBaseline(xmlAC.getMaxBaseLine());
                		if (ac.getArrayName().toLowerCase().equals("12-m"))
                			ac.setAntennaDiameter(12.0);
                		else if (ac.getArrayName().toLowerCase().equals("7-m"))
                			ac.setAntennaDiameter(7.0);
                		else
                			ac.setAntennaDiameter(12.0);
                    	arrayConfigurations.add(ac);
                	}
                }
            } catch (MarshalException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ValidationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }        
    }
    
    private Antenna createAntenna(AntennaT xmlAnt) {
        logger.trace("parsing antenna " + xmlAnt.getId());
        Antenna retVal = new Antenna();
        retVal.setAntennaType(xmlAnt.getAntennaType());
        retVal.setDiameter(xmlAnt.getDiameter());
        retVal.setEffectiveCollectingArea(xmlAnt.getEffectiveCollectingArea());
        retVal.setSystemTemperature(xmlAnt.getSystemTemperature());
        fillEquipmentData(retVal, xmlAnt);
        FrontEndT[] xmlFEs = xmlAnt.getFrontEnd();
        for (FrontEndT xmlFE : xmlFEs) {
            TelescopeEquipment fe = new TelescopeEquipment();
            fillEquipmentData(fe, xmlFE);
            ReceiverT[] xmlRcvs = xmlFE.getReceiver();
            for (ReceiverT xmlRcv : xmlRcvs) {
                Receiver rcv = new Receiver();
                rcv.setBand(ReceiverBand.valueOf(xmlRcv.getBand().toString()));
                fillEquipmentData(rcv, xmlRcv);
                fe.getAssemblyGroups().add(rcv);
            }
            retVal.getAssemblyGroups().add(fe);
        }
        return retVal;
    }
    
    private Pad createPad(PadT xmlPad) {
        logger.trace("parsing pad " + xmlPad.getId());
        Pad retVal = new Pad();
        retVal.setxPosition(xmlPad.getXPosition());
        retVal.setyPosition(xmlPad.getYPosition());
        retVal.setzPosition(xmlPad.getZPosition());
        fillEquipmentData(retVal, xmlPad);
        return retVal;
    }
    
    /**
     * Fill in the TelescopeEquipment general fields.
     * This function is recursive
     * @param equip TelescopeEquipment object to fill in
     */
    private void fillEquipmentData(TelescopeEquipment equip, EquipmentT xmlEquip) {
        logger.trace("filling equipment data for " + xmlEquip.getId());
        equip.setName(xmlEquip.getId());
        equip.setType(getEquipmentType(xmlEquip));
        equip.setCommissionDate(xmlEquip.getCommissionDate());
        AssemblyContainerOperationT[] xmlOps = xmlEquip.getInitialOperation();
        for (AssemblyContainerOperationT xmlOp : xmlOps) {
            AssemblyContainerOperation op = createOperation(xmlOp);
            equip.getOperations().add(op);
        }
        // recurse to sub equipments
        EquipmentT[] xmlSubEquips = xmlEquip.getEquipment();
        for (EquipmentT xmlSubEquip : xmlSubEquips) {
            TelescopeEquipment subEquip = new TelescopeEquipment();
            fillEquipmentData(subEquip, xmlSubEquip); // recurse
            equip.addAssemblyGroup(subEquip);
        }
    }
    
    private AssemblyContainerOperation createOperation(AssemblyContainerOperationT xmlOp) {
        AssemblyContainerOperation op = new AssemblyContainerOperation();
        op.setTime(xmlOp.getTime());
        op.setOperation(AssemblyOperation.valueOf(xmlOp.getOperation().toString()));
        String assembly = null;
        if (xmlOp.getAssembly() != null) assembly = xmlOp.getAssembly().toString();
        op.setAssemblyType(assembly);
        op.setAssemblyGroup(xmlOp.getEquipmentRefId());
        return op;
    }
    
    private AssemblyContainer searchEquipment(String equipmentName, Set<AssemblyContainer> equips) {
        logger.trace("searching for equipment named " + equipmentName);
        AssemblyContainer retVal = null;
        for (Iterator<AssemblyContainer> iter = equips.iterator(); iter.hasNext();) {
            TelescopeEquipment equip = (TelescopeEquipment) iter.next();
            logger.debug("equipment name = " + equip.getName());
            if (equip.getName().equals(equipmentName)) {
                retVal = equip;
                break;
            }
            retVal = searchEquipment(equipmentName, equip.getAssemblyGroups());
            if (retVal != null) break;
        }
        return retVal;
    }
    
    /**
     * Extract the type from an XML Equipment.
     * 
     * This function is necessary only because I don't know how to do it at the 
     * XML Schema level.
     * @param xmlEquip XML Equipment
     * @return equipment type
     */
    private AssemblyGroupType getEquipmentType(EquipmentT xmlEquip) {
        if (xmlEquip instanceof AntennaT) {
            return AssemblyGroupType.ANTENNA;
        } else if (xmlEquip instanceof FrontEndT) {
            return AssemblyGroupType.FRONTEND;
        } else if (xmlEquip instanceof PadT) {
            return AssemblyGroupType.PAD;
        } else if (xmlEquip instanceof ReceiverT) {
            return AssemblyGroupType.RECEIVER;
        }
        if (xmlEquip.getType() == null)
            throw new NullPointerException("type hasn't been defined in " + xmlEquip.getId());
        return AssemblyGroupType.valueOf(xmlEquip.getType().toString());
    }

    @Override
    public void deleteAll() {
//        equipments.clear();
        equipments = null;
//        arrayConfigurations.clear();
        arrayConfigurations = null;
        
    }
}

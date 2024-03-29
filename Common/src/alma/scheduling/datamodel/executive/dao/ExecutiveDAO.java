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
package alma.scheduling.datamodel.executive.dao;

import java.util.Collection;
import java.util.List;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;

public interface ExecutiveDAO {

    public Collection<Executive> getAllExecutive();
    
    public Collection<ObservingSeason> getAllObservingSeason();
    
    public Collection<PI> getAllPi();
    
    public ObservingSeason getCurrentSeason();
    
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex, ObservingSeason os);
    
    public Executive getExecutive(String piEmail);
    
    public PI getPIFromEmail(String piEmail);

    public void saveObservingSeasonsAndExecutives(List<ObservingSeason> seasons,
            List<Executive> executives);
    
    public ExecutivePercentage getExecutivePercentage(Executive exec, ObservingSeason os);
    
    public void deleteAll();
    
    public void saveOrUpdate(ExecutiveTimeSpent execTS);
    
    public void saveOrUpdate(Collection<PI> pis);
    
    public void cleanExecutiveTimeSpent();
    
}

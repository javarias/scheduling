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
package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ScienceGrade;

public interface ObsProjectDao extends GenericDao {
    
    int countAll();

    void hydrateSchedBlocks(ObsProject prj);
    
    public ObsUnit getObsUnitForProject(ObsProject prj);
    
    List<ObsProject> getObsProjectsOrderBySciRank();
    
    void saveOrUpdate(ObsProject prj);
    
    ObsProject getObsProject(ObsUnit ou);

    public ObsProject findByEntityId(String entityId);
    
    public void deleteAll();
    
    /**
     * This method must first remove the ObsProject if it exists and its entire hierarchy
     * from the Data Base, then reinsert it into DB. This method must be executed
     * in just one transaction ensuring completely isolation.
     * 
     * @param prj the ObsProject to refresh
     */
    public void refreshProject(ObsProject prj);
    
    /**
     * See {@link refreshProject}
     * 
     * @param list of projects to refresh.
     */
    public void refreshProjects(List<ObsProject> list);
    
    /**
     * Search the SWDB for project matching the code.
     * 
     * @param code the project code to search, it could contains SQL wildcards.
     * @return A list with project uids.
     */
    public List<String> getObsProjectsUidsByCode (String code);
    
    public List<String> getObsProjectsUidsbySciGrade (List<ScienceGrade> grades);
    
    /**
     * Updates the state of the projects and its SchedBlocks to READY
     */
    public void setObsProjectStatusAsReady();
    
}

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
 * "@(#) $Id: ProjectIncrementalDao.java,v 1.2 2010/05/17 23:34:16 dclarke Exp $"
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import alma.scheduling.datamodel.DAOException;
import alma.scheduling.datamodel.obsproject.ObsProject;

/**
 * @author dclarke
 *
 */
public interface ProjectIncrementalDao extends ProjectDao {

    /**
     * Get all the ObsProjects that are changed in or new to the
     * implementor's data set.
     * 
     * @return A <code>List</code> of all the ObsProjects.
     * @throws DAOException - if there's a non-recoverable problem (but
     *                        not if individual projects fail).
     */
    List<ObsProject> getNewObsProjects() throws DAOException;
}
